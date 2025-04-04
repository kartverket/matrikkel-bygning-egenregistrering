package no.kartverket.matrikkel.bygning.infrastructure.registrerteier.client

import kotlinx.serialization.Serializable

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
