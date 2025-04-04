package no.kartverket.matrikkel.bygning.routes.v1.ekstern

import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseService
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_BEGRENSET
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_HENDELSER
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_UTVIDET
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.VIRKSOMHET_UTVIDET_UTEN_PII
import no.kartverket.matrikkel.bygning.plugins.authentication.EksternRouteConfig
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse.virksomhetHendelserRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetbegrenset.virksomhetBegrensetRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.virksomhetUtvidetRouting
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.virksomhetUtvidetUtenPIIRouting

fun Route.eksternRouting(
    bygningService: BygningService,
    hendelseService: HendelseService,
) {
    route("/") {
        eksternApiRoute(VIRKSOMHET_BEGRENSET) {
            virksomhetBegrensetRouting(bygningService)
        }

        eksternApiRoute(VIRKSOMHET_UTVIDET_UTEN_PII) {
            virksomhetUtvidetUtenPIIRouting(bygningService)
        }

        eksternApiRoute(VIRKSOMHET_UTVIDET) {
            virksomhetUtvidetRouting(bygningService)
        }

        eksternApiRoute(VIRKSOMHET_HENDELSER) {
            virksomhetHendelserRouting(hendelseService)
        }
    }
}

private fun Route.eksternApiRoute(
    eksternRouteConfig: EksternRouteConfig,
    build: Route.() -> Unit,
) = route(
    eksternRouteConfig.path,
    {
        specName = eksternRouteConfig.openApiSpecId
        securitySchemeNames =
            listOfNotNull(eksternRouteConfig.maskinportenAuthSchemeName, eksternRouteConfig.matrikkelAuthSchemeName)
        response {
            code(HttpStatusCode.Unauthorized) {
                description = "Manglende eller ugyldig token"
            }
        }
    },
) {
    if (eksternRouteConfig.matrikkelAuthSchemeName != null) {
        authenticate(
            eksternRouteConfig.maskinportenAuthSchemeName,
            eksternRouteConfig.matrikkelAuthSchemeName,
            build = build,
        )
    } else {
        authenticate(eksternRouteConfig.maskinportenAuthSchemeName, build = build)
    }
}
