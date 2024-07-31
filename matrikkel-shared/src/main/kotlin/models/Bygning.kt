package models

data class Bygning(
    val bygningId: String,
    val bygningNummer: String,
    val bruksenheter: List<Bruksenhet>,
)

data class Bruksenhet(
    val bruksenhetId: String,
    val bygningId: String,
)
