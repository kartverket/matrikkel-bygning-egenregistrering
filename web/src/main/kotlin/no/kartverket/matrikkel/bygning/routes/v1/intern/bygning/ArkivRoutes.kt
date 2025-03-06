package no.kartverket.matrikkel.bygning.routes.v1.intern.bygning

import com.github.michaelbull.result.mapBoth
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.ENTRA_ID_ARKIVARISK_HISTORIKK_NAME
import no.kartverket.matrikkel.bygning.routes.v1.common.domainErrorToResponse
import no.kartverket.matrikkel.bygning.routes.v1.common.toInstant
import java.time.Instant

fun Route.arkivRouting(
    bygningService: BygningService
) {
    route("bygninger") {
        route("{bygningId}") {
            route("egenregistrert") {
                get(
                    {
                        summary = "Hent egenregistrert data for en bygning for et gitt registreringstidspunkt"
                        description =
                            "Henter tidligere versjon av egenregistrert data for en bygning basert på informasjonsgrunnlaget ved gitt registreringstidspunkt"
                        securitySchemeNames = listOf(ENTRA_ID_ARKIVARISK_HISTORIKK_NAME)
                        request {
                            pathParameter<String>("bygningId") {
                                required = true
                            }
                            queryParameter<Instant>("registreringstidspunkt") {
                                description = "Tidspunktet du ønsker å hente data basert på. Format: ISO-8601. Default: nåtidspunkt"
                            }
                        }
                        response {
                            code(HttpStatusCode.OK) {
                                body<BygningSimpleResponse> {
                                    description = "Bygning med én datakilde - kun egenregistrerte data"
                                }
                                description = "Bygningen finnes og ble hentet"
                            }
                            code(HttpStatusCode.NotFound) {
                                description = "Fant ikke bygning med gitt bygningId"
                            }
                            code(HttpStatusCode.BadRequest) {
                                description = "Forespørselen inneholder ugyldige parametere. Kontroller at alle felt er korrekt formatert."
                            }
                        }
                    },
                ) {
                    val bygningId = call.parameters.getOrFail("bygningId").toLong()
                    val registreringstidspunkt = call.request.queryParameters["registreringstidspunkt"]?.toInstant() ?: Instant.now()

                    val (status, body) = bygningService.getBygningByBubbleId(
                        bygningBubbleId = bygningId,
                        registreringstidspunkt = registreringstidspunkt,
                    ).mapBoth(
                        success = { HttpStatusCode.OK to it.toBygningSimpleResponseFromEgenregistrertData() },
                        failure = ::domainErrorToResponse,
                    )

                    call.respond(status, body)
                }
            }
        }
    }

    route("bruksenheter") {
        route("{bruksenhetId}") {
            route("egenregistrert") {
                get(
                    {
                        summary = "Hent egenregistrert data for en bruksenhet for et gitt registreringstidspunkt"
                        description =
                            "Henter tidligere versjon av egenregistrert data for en bruksenhet basert på informasjonsgrunnlaget ved gitt registreringstidspunkt"
                        securitySchemeNames = listOf(ENTRA_ID_ARKIVARISK_HISTORIKK_NAME)
                        request {
                            pathParameter<String>("bruksenhetId") {
                                required = true
                            }
                            queryParameter<Instant>("registreringstidspunkt") {
                                description = "Tidspunktet du ønsker å hente data basert på. Format: ISO-8601. Default: nåtidspunkt"
                            }
                        }
                        response {
                            code(HttpStatusCode.OK) {
                                body<BruksenhetSimpleResponse> {
                                    description = "Bruksenhet med én datakilde - kun egenregistrerte data"
                                }
                                description = "Bruksenhet finnes og ble hentet"
                            }
                            code(HttpStatusCode.NotFound) {
                                description = "Fant ikke bruksenhet med gitt bruksenhetId"
                            }
                            code(HttpStatusCode.BadRequest) {
                                description =
                                    "Forespørselen inneholder ugyldige parametere. Kontroller at alle felt er korrekt formatert."
                            }
                        }
                    },
                ) {
                    val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()
                    val registreringstidspunkt =
                        call.request.queryParameters["registreringstidspunkt"]?.toInstant() ?: Instant.now()

                    val (status, body) = bygningService.getBruksenhetByBubbleId(
                        bruksenhetBubbleId = bruksenhetId,
                        registreringstidspunkt = registreringstidspunkt,
                    ).mapBoth(
                        success = { HttpStatusCode.OK to it.toBruksenhetSimpleResponseFromEgenregistrertData() },
                        failure = ::domainErrorToResponse,
                    )

                    call.respond(status, body)
                }
            }
        }
    }
}
