package no.kartverket.matrikkel.bygning.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.requests.*

@Serializable
data class Bygning(
    val bygningId: Long,
    val bruksenheter: List<Bruksenhet>,
    val bruksarealRegistreringer: List<BruksarealRegistrering>,
    val byggeaarRegistreringer: List<ByggeaarRegistrering>,
    val vannforsyningsRegistreringer: List<VannforsyningsRegistrering>,
    val avlopRegistreringer: List<AvlopRegistrering>,
)

@Serializable
data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
    val bruksarealRegistreringer: List<BruksarealRegistrering>,
    val energikildeRegistreringer: List<EnergikildeRegistrering>,
    val oppvarmingRegistreringer: List<OppvarmingRegistrering>
)

// Disse er dummy klasser som brukes runtime mens vi ikke har en databasetilkobling
data class BygningStorage(
    val bygningId: Long,
    val bruksarealRegistreringer: MutableList<BruksarealRegistrering>,
    val byggeaarRegistreringer: MutableList<ByggeaarRegistrering>,
    val vannforsyningsRegistreringer: MutableList<VannforsyningsRegistrering>,
    val avlopRegistreringer: MutableList<AvlopRegistrering>,
)

data class BruksenhetStorage(
    val bruksenhetId: Long,
    val bygningId: Long,
    val bruksarealRegistreringer: MutableList<BruksarealRegistrering>,
    val energikildeRegistreringer: MutableList<EnergikildeRegistrering>,
    val oppvarmingRegistreringer: MutableList<OppvarmingRegistrering>
)