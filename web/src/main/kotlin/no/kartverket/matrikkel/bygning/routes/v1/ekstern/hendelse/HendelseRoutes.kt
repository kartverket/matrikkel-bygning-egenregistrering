package no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse

import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.routes.v1.common.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.HendelseRoutingConstants.ANTALL_QUERY_PARAM_MAX

object HendelseRoutingConstants {
    const val FRA_QUERY_PARAM = "fra"
    const val FRA_QUERY_PARAM_DEFAULT: Long = 1
    const val ANTALL_QUERY_PARAM = "antall"
    const val ANTALL_QUERY_PARAM_MAX: Long = 1000
}

fun Route.virksomhetHendelserRouting(hendelseService: HendelseService) {
    get(
        {
            summary = "Henter hendelser som har skjedd"
            description =
                "Henter en liste med hendelser som har skjedd i systemet gitt et fra-sekvensnummer og et antall"
            request {
                queryParameter<Long>(HendelseRoutingConstants.FRA_QUERY_PARAM) {
                    required = false
                    description = "Sekvensnummeret som det skal hentes fra, inklusivt med default = 1"
                }
                queryParameter<Long>(HendelseRoutingConstants.ANTALL_QUERY_PARAM) {
                    required = true
                    description =
                        "Antall hendelser som skal hentes. Maks antall som hentes er satt til $ANTALL_QUERY_PARAM_MAX hendelser." +
                        "Påkrevd felt, da vi ønsker at man har tatt stilling til behov for datamengde."
                }
            }

            response {
                code(HttpStatusCode.OK) {
                    body<HendelseContainerResponse> {
                        description = "Hendelsene som er innenfor antall og fra-sekvensnummer"
                    }
                    description = "Alle parametre var gyldig og hendelsene ble hentet"
                }
                code(HttpStatusCode.BadRequest) {
                    body<ErrorResponse.BadRequestError> {}
                    description = "Ugyldige parametre for henting av hendelser"
                }
            }
        },
    ) {
        val hendelserQueryParams = call.getHendelserQuery()

        val hendelser =
            hendelseService.getHendelser(
                fra = hendelserQueryParams.fra,
                antall = hendelserQueryParams.antall,
            )

        call.respond(HttpStatusCode.OK, hendelser.toHendelseContainerResponse())
    }
}

class HendelserQuery private constructor(
    val fra: Long,
    val antall: Long,
) {
    companion object {
        fun of(
            fra: Long,
            antall: Long?,
        ): HendelserQuery {
            if (antall == null) {
                throw BadRequestException("Ugyldige parametre for henting av hendelser. Antall må være definert")
            }
            if (fra <= 0L || antall <= 0L) {
                throw BadRequestException("Ugyldige parametre for henting av hendelser. Både fra og antall må være større enn 0")
            }
            return HendelserQuery(fra, minOf(antall, ANTALL_QUERY_PARAM_MAX))
        }
    }
}

private fun RoutingCall.getHendelserQuery(): HendelserQuery =
    HendelserQuery.of(
        fra =
            parameters[HendelseRoutingConstants.FRA_QUERY_PARAM]?.toLong()
                ?: HendelseRoutingConstants.FRA_QUERY_PARAM_DEFAULT,
        antall = parameters[HendelseRoutingConstants.ANTALL_QUERY_PARAM]?.toLong(),
    )
