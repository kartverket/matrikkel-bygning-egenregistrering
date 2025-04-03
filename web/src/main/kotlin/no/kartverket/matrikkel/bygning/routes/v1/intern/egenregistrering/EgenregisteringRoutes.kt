package no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering

import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.IDPORTEN_PROVIDER_NAME
import no.kartverket.matrikkel.bygning.routes.getFnr
import no.kartverket.matrikkel.bygning.routes.v1.common.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse
import no.kartverket.matrikkel.bygning.routes.v1.common.exceptionToDomainError

fun Route.egenregistreringGammelRouting(egenregistreringService: EgenregistreringService) {
    authenticate(IDPORTEN_PROVIDER_NAME) {
        post(
            {
                summary = "Egenregistrering på bruksenhet"
                description = "Legg til en egenregistrering på en bruksenhet"
                deprecated = true
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

            val (status, body) =
                runCatching { egenregistreringRequest.toEgenregistrering(eier = fnr) }
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

fun Route.egenregistreringRouting(egenregistreringService: EgenregistreringService) {
    authenticate(IDPORTEN_PROVIDER_NAME) {
        post(
            {
                summary = "Registrer data på et felt for en bruksenhet"
                description = "Registrer data på et felt for en bruksenhet"
                securitySchemeNames = listOf(IDPORTEN_PROVIDER_NAME)
                request {
                    body<RegistrertFeltEgenregistreringRequest> {
                        required = true
                        example("Bruksenhet #1 vannforsyning") {
                            description = "Bruksenhet med id = 1"
                            value = registrertFeltEgenregistreringRequest
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
                    code(HttpStatusCode.Unauthorized) {
                        description = "Manglende eller ugyldig token"
                    }
                    code(HttpStatusCode.Conflict) {
                        body<ErrorResponse.ConflictError>()
                        description = "Det finnes allerede data for feltet det prøves å egenregistrere på"
                    }
                    code(HttpStatusCode.InternalServerError) {
                        body<ErrorResponse.InternalServerError>()
                        description = "Noe gikk galt på server"
                    }
                }
            },
        ) {
            val fnr = call.getFnr()
            // Kan også wrappes i en runCatching. Enten her eller ved å lage en custom receive-metode.
            val egenregistreringRequest = call.receive<RegistrertFeltEgenregistreringRequest>()

            val (status, body) =
                runCatching { egenregistreringRequest.toRegistrerEgenregistrering(eier = fnr) }
                    .mapError(::exceptionToDomainError)
                    .andThen { egenregistreringService.handleEgenregistrering(it) }
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
        put(
            "korriger",
            {
                summary = "Korriger et felt med nye data"
                description = "Korriger et felt med nye data"
                securitySchemeNames = listOf(IDPORTEN_PROVIDER_NAME)
                request {
                    body<KorrigerFeltEgenregistreringRequest> {
                        required = true
                        example("Bruksenhet #1 vannforsyning") {
                            description = "Bruksenhet med id = 1"
                            value = korrigerFeltEgenregistreringRequest
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
                    code(HttpStatusCode.Unauthorized) {
                        description = "Manglende eller ugyldig token"
                    }
                    code(HttpStatusCode.Conflict) {
                        description = "Det finnes ikke noe data å overskrive for det aktuelle elementet"
                    }
                    code(HttpStatusCode.InternalServerError) {
                        body<ErrorResponse.InternalServerError>()
                        description = "Noe gikk galt på server"
                    }
                }
            },
        ) {
            val fnr = call.getFnr()
            // Kan også wrappes i en runCatching. Enten her eller ved å lage en custom receive-metode.
            val egenregistreringRequest = call.receive<KorrigerFeltEgenregistreringRequest>()

            val (status, body) =
                runCatching { egenregistreringRequest.toKorrigerEgenregistrering(eier = fnr) }
                    .mapError(::exceptionToDomainError)
                    .andThen { egenregistreringService.handleEgenregistrering(it) }
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

        put(
            "avslutt",
            {
                summary = "Sett opphoersaar på et spesifikt felt"
                description = "Sett opphoersaar på et spesifikt felt"
                securitySchemeNames = listOf(IDPORTEN_PROVIDER_NAME)
                request {
                    body<SettOpphoersaarRequest> {
                        required = true
                        example("#1 vannforsyning") {
                            description = "Bruksenhet med id = 1"
                            value = settOpphoersdatoRequest
                        }
                    }
                }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Opphørsdato ble satt"
                    }
                    code(HttpStatusCode.BadRequest) {
                        body<ErrorResponse.ValidationError>()
                        description = "Validering av egenregistreringsdata har gått feil"
                    }
                    code(HttpStatusCode.Unauthorized) {
                        description = "Manglende eller ugyldig token"
                    }
                    code(HttpStatusCode.InternalServerError) {
                        body<ErrorResponse.InternalServerError>()
                        description = "Noe gikk galt på server"
                    }
                }
            },
        ) {
            val fnr = call.getFnr()
            val settOpphoersaarRequest = call.receive<SettOpphoersaarRequest>()

            val (status, body) =
                runCatching { settOpphoersaarRequest.toEgenregistrering(eier = fnr) }
                    .mapError(::exceptionToDomainError)
                    .andThen { egenregistreringService.handleEgenregistrering(it) }
                    .mapBoth(
                        success = { HttpStatusCode.OK to null },
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

private val registrertFeltEgenregistreringRequest =
    RegistrertFeltEgenregistreringRequest(
        bruksenhetId = 1L,
        registrertFelt =
            FeltRegistreringRequest.VannforsyningFeltRegistreringRequest(
                vannforsyning = VannforsyningKode.AnnenPrivatInnlagtVann,
                kildemateriale = KildematerialeKode.Selvrapportert,
                gyldighetsaar = 1976,
            ),
    )

private val korrigerFeltEgenregistreringRequest =
    KorrigerFeltEgenregistreringRequest(
        bruksenhetId = 1L,
        korrigerFelt =
            FeltRegistreringRequest.VannforsyningFeltRegistreringRequest(
                vannforsyning = VannforsyningKode.OffentligVannverk,
                kildemateriale = KildematerialeKode.Selvrapportert,
                gyldighetsaar = 1982,
            ),
    )

private val settOpphoersdatoRequest =
    SettOpphoersaarRequest.SettOpphoersaarVannforsyningRequest(
        bruksenhetId = 1L,
        vannforsyning = VannforsyningKode.AnnenPrivatInnlagtVann,
        opphoersaar = 2012,
    )

private val egenregistreringExample =
    EgenregistreringRequest(
        bruksenhetId = 1L,
        bruksarealRegistrering =
            BruksarealRegistreringRequest(
                totaltBruksareal = 80.0,
                etasjeRegistreringer =
                    listOf(
                        EtasjeBruksarealRegistreringRequest(
                            bruksareal = 50.0,
                            etasjebetegnelse =
                                EtasjeBetegnelseRequest(
                                    etasjeplanKode = "H",
                                    etasjenummer = 1,
                                ),
                        ),
                        EtasjeBruksarealRegistreringRequest(
                            bruksareal = 30.0,
                            etasjebetegnelse =
                                EtasjeBetegnelseRequest(
                                    etasjeplanKode = "H",
                                    etasjenummer = 2,
                                ),
                        ),
                    ),
                kildemateriale = KildematerialeKode.Salgsoppgave,
            ),
        byggeaarRegistrering =
            ByggeaarRegistreringRequest(
                byggeaar = 2021,
                kildemateriale = KildematerialeKode.Selvrapportert,
            ),
        oppvarmingRegistrering =
            listOf(
                OppvarmingRegistreringRequest(
                    oppvarming = OppvarmingKode.Elektrisk,
                    kildemateriale = KildematerialeKode.Salgsoppgave,
                    gyldighetsaar = 2021,
                ),
            ),
        avlopRegistrering =
            AvlopRegistreringRequest(
                avlop = AvlopKode.OffentligKloakk,
                kildemateriale = KildematerialeKode.Selvrapportert,
            ),
        energikildeRegistrering = null,
        vannforsyningRegistrering = null,
    )
