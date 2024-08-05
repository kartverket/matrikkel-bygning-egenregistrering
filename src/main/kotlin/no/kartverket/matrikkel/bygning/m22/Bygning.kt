package no.kartverket.matrikkel.bygning.m22

data class Bygning(
    val bygningId: Long,
    val bygningNummer: Long,
    val bruksenheter: List<Bruksenhet>,
)

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
)
