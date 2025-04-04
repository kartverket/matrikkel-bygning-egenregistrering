package no.kartverket.matrikkel.bygning.infrastructure.registrerteier.client

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import no.kartverket.matrikkel.bygning.application.models.MatrikkelenhetEier
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.FantIkkeEierForhold
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId
import no.kartverket.matrikkel.bygning.application.registrerteier.RegistrertEierClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultRegistrertEierClient(
    private val baseUrl: String,
) : RegistrertEierClient {
    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun finnEierforhold(
        matrikkelenhetId: MatrikkelenhetBubbleId,
        fnr: RegistreringAktoer.Foedselsnummer,
    ): Result<MatrikkelenhetEier, DomainError> {
        log.info("Finn eierforhold for matrikkelenhetId: ${matrikkelenhetId.value} og fnr: ${fnr.value}")
        val res =
            runBlocking {
                client
                    .post("$baseUrl/v1/finnEierforhold") {
                        contentType(ContentType.Application.Json)
                        setBody(RegistrertEierRequest(fnr = fnr.value, matrikkelenhetId = matrikkelenhetId.value))
                    }.body<EierMatrikkelenhetResponse>()
            }
        log.info(res.eierforholdinfo.toString())
        if (res.eierforholdinfo == null) {
            return Err(FantIkkeEierForhold("Fant ikke eierforhold"))
        }

        return Ok(MatrikkelenhetEier(res.eierforholdinfo.ultimatEier, MatrikkelenhetBubbleId(matrikkelenhetId.value), fnr))
    }
}
