package no.kartverket.matrikkel.bygning.models

import kotlinx.serialization.Serializable

@Serializable
data class Bygning(
    val id: String,
    val byggeaar: Int,
    val areal: Double,
    val energikilder: List<Energikilde>,
    val oppvarming: List<Oppvarming>,
    val vann: Boolean,
    val avlop: Boolean,
)

enum class Oppvarming {
    VARMEPUMPE
}

enum class Energikilde {
    GEOTERMISK,
}