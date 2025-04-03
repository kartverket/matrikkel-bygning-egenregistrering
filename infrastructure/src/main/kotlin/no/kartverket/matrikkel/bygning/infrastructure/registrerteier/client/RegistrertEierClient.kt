package no.kartverket.matrikkel.bygning.infrastructure.registrerteier.client

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
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

// import io.ktor.http.ContentType
// import io.ktor.http.contentType
// import kotlinx.serialization.Serializable

class DefaultRegistrertEierClient : RegistrertEierClient {
    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }

    private val registrertEierBaseUrl = "localhost:8090"

    override fun finnEierforhold(
        matrikkelenhetId: MatrikkelenhetBubbleId,
        fnr: RegistreringAktoer.Foedselsnummer,
    ): Result<MatrikkelenhetEier, DomainError> {
        val res =
            runBlocking {
                client
                    .post("$registrertEierBaseUrl/v1/finnEierforhold") {
                        contentType(ContentType.Application.Json)
                        setBody(EierMatrikkelenhetRequest(fnr = fnr.value, matrikkelenhetId = matrikkelenhetId.value))
                    }.body<EierMatrikkelenhetResponse>()
            }

        if (res.eierforholdinfo == null) {
            return Err(ValidationError("Fant ikke eierforhold"))
        }

        return Ok(MatrikkelenhetEier(res.eierforholdinfo.ultimatEier, MatrikkelenhetBubbleId(matrikkelenhetId.value), fnr))
    }
}

@Serializable
data class EideMatrikkelenheterRequest(
    val fnr: String,
)

@Serializable
data class EideMatrikkelenheterResponse(
    val matrikkelenhetIds: Set<Long>,
)

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
