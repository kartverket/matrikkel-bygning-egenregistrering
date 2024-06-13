package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.core.routes.swagger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.repositories.DemoRepository
import no.kartverket.matrikkel.bygning.repositories.DemospokelseDTO
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService

fun Application.baseRoutesV1(
    egenregistreringsService: EgenregistreringsService, demoRepository: DemoRepository
) {
    routing {
        swagger()

        // TODO Remove after checking connection between Egenreg and Bygning
        route("/dummy") {
            get {
                call.respondText("Hei Egenregistrering!", status = HttpStatusCode.OK)
            }
        }

        route("/v1") {
            route("/demospokelser") {
                demoDoc()
                get {
                    val spokelser = demoRepository.getDemospokelser().sortedBy { it.id };

                    call.respond(spokelser)
                }
            }
            egenregistreringRouting(egenregistreringsService)
        }

    }
}

private fun Route.demoDoc() {
    install(NotarizedRoute()) {
        get = GetInfo.builder {
            summary("Henter demospøkelser")
            description("Henter demospøkelser med navn og id")

            response {
                responseCode(HttpStatusCode.OK)
                responseType<List<DemospokelseDTO>>()
                description("Demospøkelsene i databasen")
            }
        }
    }
}