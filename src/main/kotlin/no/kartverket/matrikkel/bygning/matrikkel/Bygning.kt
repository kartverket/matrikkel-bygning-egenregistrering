package no.kartverket.matrikkel.bygning.matrikkel

import kotlinx.serialization.Serializable

@Serializable
data class Bygning(
    val bygningId: Long,
    val bygningNummer: Long,
    val bruksenheter: List<Bruksenhet>,
)

@Serializable
data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
)
