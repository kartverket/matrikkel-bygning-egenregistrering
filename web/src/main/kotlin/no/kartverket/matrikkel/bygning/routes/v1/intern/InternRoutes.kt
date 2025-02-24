package no.kartverket.matrikkel.bygning.routes.v1.intern

import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.bygning.BygningService
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import no.kartverket.matrikkel.bygning.plugins.OpenApiSpecIds
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.arkivRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.bygningRouting
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.egenregistreringRouting
import no.kartverket.matrikkel.bygning.routes.v1.kodeliste.kodelisteRouting

fun Route.internRouting(
    egenregistreringService: EgenregistreringService,
    bygningService: BygningService
) {
    route(
        "/intern",
        {
            specId = OpenApiSpecIds.INTERN
        },
    ) {
        route("kodelister") {
            kodelisteRouting()
        }
        route("egenregistreringer") {
            egenregistreringRouting(egenregistreringService)
        }
        route("bygninger") {
            bygningRouting(bygningService)
        }
        route("arkiv") {
            arkivRouting(bygningService)
        }
    }
}
