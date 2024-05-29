package no.kartverket.matrikkel.bygning.models


data class BygningDTO(
    val id: String,
    val byggaar: Int?,
    val vann: Boolean,
    val avlop: Boolean,
)

data class OppvarmingDTO(
    val id: Int,
    val navn: String,
)

data class EnergikildeDTO(
    val id: Int,
    val navn: String,
)