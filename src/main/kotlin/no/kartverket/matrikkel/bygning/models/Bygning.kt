package no.kartverket.matrikkel.bygning.models

import kotlinx.serialization.Serializable

@Serializable
data class Bygning(
    val id: String,
    val bruksarealRegistreringer: MutableList<BruksarealRegistrering>,
    val byggeaarRegistreringer: MutableList<ByggeaarRegistrering>,
    val vannforsyningsRegistreringer: MutableList<VannforsyningsRegistrering>,
    val avlopRegistreringer: MutableList<AvlopRegistrering>,
)

@Serializable
data class Bruksenhet(
    val id: String,
    val bygningId: String,
    val bruksarealRegistreringer: MutableList<BruksarealRegistrering>,
    val energikildeRegistreringer: MutableList<EnergikildeRegistrering>,
    val oppvarmingRegistreringer: MutableList<OppvarmingRegistrering>
)

