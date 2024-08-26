package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.kartverket.matrikkel.bygning.models.Result.ErrorResult
import no.kartverket.matrikkel.bygning.models.Result.Success
import no.kartverket.matrikkel.bygning.models.responses.ErrorResponse
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.BruksenhetResponse
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.BygningResponse
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.toBruksenhetResponse
import no.kartverket.matrikkel.bygning.routes.v1.dto.response.toBygningResponse
import no.kartverket.matrikkel.bygning.services.BygningService

fun Route.bygningRouting(
    bygningService: BygningService
) {
    route("{bygningId}") {
        bygningDoc()

        get {
            val bygningId = call.parameters.getOrFail("bygningId").toLong()

            when (val result = bygningService.getBygningWithEgenregistrertData(bygningId)) {
                is Success -> call.respond(HttpStatusCode.OK, result.data.toBygningResponse())
                is ErrorResult -> call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse.NotFoundError(
                        details = result.errors,
                    ),
                )
            }
        }

        route("bruksenheter") {
            route("{bruksenhetId}") {
                bruksenhetDoc()

                get {
                    val bygningId = call.parameters.getOrFail("bygningId").toLong()
                    val bruksenhetId = call.parameters.getOrFail("bruksenhetId").toLong()

                    when (val result = bygningService.getBruksenhetWithEgenregistrertData(bygningId, bruksenhetId)) {
                        is Success -> call.respond(HttpStatusCode.OK, result.data.toBruksenhetResponse())
                        is ErrorResult -> call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse.NotFoundError(
                                details = result.errors,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun Route.bygningDoc() {
    install(NotarizedRoute()) {
        tags = setOf("Bygninger")
        parameters = listOf(
            Parameter(
                name = "bygningId", `in` = Parameter.Location.path, schema = TypeDefinition.STRING,
            ),
        )

        get = GetInfo.builder {
            summary("Hent en bygning")
            description("Henter en bygning med tilhørende bruksenheter.")

            response {
                responseCode(HttpStatusCode.OK)
                responseType<BygningResponse>()
                description("Bygning med tilhørende bruksenheter")
            }

            canRespond {
                responseCode(HttpStatusCode.NotFound)
                responseType<ErrorResponse.NotFoundError>()
                description("Fant ikke bygning med gitt id")
            }
        }
    }
}

private fun Route.bruksenhetDoc() {
    install(NotarizedRoute()) {
        tags = setOf("Bygninger")
        parameters = listOf(
            Parameter(
                name = "bygningId", `in` = Parameter.Location.path, schema = TypeDefinition.STRING,
            ),
            Parameter(
                name = "bruksenhetId", `in` = Parameter.Location.path, schema = TypeDefinition.STRING,
            ),
        )

        get = GetInfo.builder {
            summary("Hent en bruksenhet")
            description("Henter en bruksenhet")

            response {
                responseCode(HttpStatusCode.OK)
                responseType<BruksenhetResponse>()
                description("Bruksenhet")
            }

            canRespond {
                responseCode(HttpStatusCode.NotFound)
                responseType<ErrorResponse.NotFoundError>()
                description("Fant ikke bruksenhet med gitt id")
            }
        }
    }
}

