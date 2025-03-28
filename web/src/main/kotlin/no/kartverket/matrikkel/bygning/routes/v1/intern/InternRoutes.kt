package no.kartverket.matrikkel.bygning.routes.v1.intern

import io.github.smiley4.ktoropenapi.route
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.plugins.OpenApiSpecIds
import no.kartverket.matrikkel.bygning.plugins.authentication.AuthenticationConstants.ENTRA_ID_ARKIVARISK_HISTORIKK_NAME
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.arkivRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.bruksenhetRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.bygningRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.egenregistrering2Routing
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.egenregistreringRouting
import no.kartverket.matrikkel.bygning.routes.v1.kodeliste.kodelisteRouting

fun Route.internRouting(
    egenregistreringService: EgenregistreringService,
    bygningService: BygningService
) {
    route(
        "/intern",
        {
            specName = OpenApiSpecIds.INTERN
        },
    ) {
        route("kodelister") {
            kodelisteRouting()
        }
        route("egenregistreringer") {
            egenregistreringRouting(egenregistreringService)
        }
        route("egenregistreringer2") {
            egenregistrering2Routing(egenregistreringService)
        }
        route("bygninger") {
            bygningRouting(bygningService)
        }
        route("bruksenheter") {
            bruksenhetRouting(bygningService)
        }
        authenticate(ENTRA_ID_ARKIVARISK_HISTORIKK_NAME) {
            route("arkiv") {
                arkivRouting(bygningService)
            }
        }
    }
}
