package no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering

import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.IDPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.routes.getFnr
import no.kartverket.matrikkel.bygning.routes.v1.common.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse
import no.kartverket.matrikkel.bygning.routes.v1.common.exceptionToDomainError

fun Route.egenregistreringRouting(egenregistreringService: EgenregistreringService) {
    authenticate(IDPORTEN_PROVIDER_NAME) {
        post(
            {
                summary = "Egenregistrering på bruksenhet"
                description = "Legg til en egenregistrering på en bruksenhet"
                securitySchemeNames = listOf(IDPORTEN_PROVIDER_NAME)
                request {
                    body<EgenregistreringRequest> {
                        required = true
                        example("Bruksenhet #1") {
                            description = "Bruksenhet med id = 1"
                            value = egenregistreringExample
                        }
                    }
                }
                response {
                    code(HttpStatusCode.Created) {
                        description = "Egenregistrering ble registrert"
                    }
                    code(HttpStatusCode.BadRequest) {
                        body<ErrorResponse.ValidationError>()
                        description = "Validering av egenregistreringsdata har gått feil"
                    }
                    code(HttpStatusCode.InternalServerError) {
                        body<ErrorResponse.InternalServerError>()
                        description = "Noe gikk galt på server"
                    }
                    code(HttpStatusCode.Unauthorized) {
                        description = "Manglende eller ugyldig token"
                    }
                }
            },
        ) {
            val fnr = call.getFnr()
            // Kan også wrappes i en runCatching. Enten her eller ved å lage en custom receive-metode.
            val egenregistreringRequest = call.receive<EgenregistreringRequest>()

            val (status, body) = runCatching { egenregistreringRequest.toEgenregistrering(eier = fnr) }
                .mapError(::exceptionToDomainError)
                .andThen { egenregistreringService.addEgenregistrering(it) }
                .mapBoth(
                    success = { HttpStatusCode.Created to null },
                    failure = ::domainErrorToResponse,
                )

            if (body == null) {
                call.respond(status)
            } else {
                call.respond(status, body)
            }
        }
    }
}

private val egenregistreringExample = EgenregistreringRequest(
    bruksenhetId = 1L,
    bruksarealRegistrering = BruksarealRegistreringRequest(
        totaltBruksareal = 80.0,
        etasjeRegistreringer = listOf(
            EtasjeBruksarealRegistreringRequest(
                bruksareal = 50.0,
                etasjebetegnelse = EtasjeBetegnelseRequest(
                    etasjeplanKode = "H",
                    etasjenummer = 1,
                ),
            ),
            EtasjeBruksarealRegistreringRequest(
                bruksareal = 30.0,
                etasjebetegnelse = EtasjeBetegnelseRequest(
                    etasjeplanKode = "H",
                    etasjenummer = 2,
                ),
            ),
        ),
        kildemateriale = KildematerialeKode.Salgsoppgave,
    ),
    byggeaarRegistrering = ByggeaarRegistreringRequest(
        byggeaar = 2021,
        kildemateriale = KildematerialeKode.Selvrapportert,
    ),
    oppvarmingRegistrering = OppvarmingRegistreringRequest.Data(
        data = listOf(
            OppvarmingDataRequest(
                oppvarming = OppvarmingKode.Elektrisk,
                kildemateriale = KildematerialeKode.Salgsoppgave,
                gyldighetsaar = 2021,
            ),
        ),
    ),
    avlopRegistrering = AvlopRegistreringRequest(
        avlop = AvlopKode.OffentligKloakk,
        kildemateriale = KildematerialeKode.Selvrapportert,
    ),
    energikildeRegistrering = EnergikildeRegistreringRequest.HarIkke(
        kildemateriale = KildematerialeKode.Selvrapportert,
    ),
    vannforsyningRegistrering = null,
)
