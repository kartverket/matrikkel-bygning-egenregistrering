package no.kartverket.matrikkel.bygning.models


data class Bygning(
    val id: String,
    val egenregistreringer: MutableList<BygningsRegistrering>
)

data class Bruksenhet(
    val id: String,
    val bygningId: String,
    val egenregistreringer: MutableList<BruksenhetRegistrering>
)

