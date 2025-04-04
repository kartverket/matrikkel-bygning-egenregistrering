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
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.bygning.RegistrertEierClient
import no.kartverket.matrikkel.bygning.application.models.MatrikkelenhetEier
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.error.ValidationError
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// import io.ktor.http.ContentType
// import io.ktor.http.contentType

class DefaultRegistrertEierClient : RegistrertEierClient {
    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }

    private val registrertEierBaseUrl = "http://localhost:8090"
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun finnEierforhold(
        matrikkelenhetId: MatrikkelenhetBubbleId,
        fnr: RegistreringAktoer.Foedselsnummer,
    ): Result<MatrikkelenhetEier, DomainError> {
        log.info("Finn eierforhold for matrikkelenhetId: ${matrikkelenhetId.value} og fnr: ${fnr.value}")
        val res =
            runBlocking {
                client
                    .post("$registrertEierBaseUrl/v1/finnEierforhold") {
                        contentType(ContentType.Application.Json)
                        setBody(EierMatrikkelenhetRequest(fnr = fnr.value, matrikkelenhetId = matrikkelenhetId.value))
                    }.body<EierMatrikkelenhetResponse>()
            }
        log.info(res.eierforholdinfo.toString())
        if (res.eierforholdinfo == null) {
            return Err(ValidationError("Fant ikke eierforhold"))
        }

        return Ok(MatrikkelenhetEier(res.eierforholdinfo.ultimatEier, MatrikkelenhetBubbleId(matrikkelenhetId.value), fnr))
    }
}

@Serializable
data class EierMatrikkelenhetRequest(
    val fnr: String,
    val matrikkelenhetId: Long,
)

@Serializable
data class EierMatrikkelenhetResponse(
    val eierforholdinfo: EierforholdinfoResponse? = null,
)

@Serializable
data class EierforholdinfoResponse(
    val eierforholdkode: String,
    val andel: BroekResponse? = null,
    val ultimatEier: Boolean,
)

@Serializable
data class BroekResponse(
    val teller: Long,
    val nevner: Long,
)
