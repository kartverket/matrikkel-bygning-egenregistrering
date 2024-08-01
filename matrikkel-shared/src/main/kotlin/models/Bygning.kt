package models

data class Bygning(
    val bygningId: Long,
    val bygningNummer: Long,
    val bruksenheter: List<Bruksenhet>,
)

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
)
