package no.kartverket.matrikkel.bygning.infrastructure.registrerteier.client

import kotlinx.serialization.Serializable

@Serializable
data class RegistrertEierRequest(
    val fnr: String,
    val matrikkelenhetId: Long,
)
