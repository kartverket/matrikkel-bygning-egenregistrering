package no.kartverket.matrikkel.bygning.routes.v1.dto.request

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.models.VannforsyningRegistrering


@Serializable
data class BygningRegistreringRequest(
    val bruksarealRegistrering: BruksarealRegistrering?,
    val byggeaarRegistrering: ByggeaarRegistrering?,
    val vannforsyningRegistrering: VannforsyningRegistrering?,
    val avlopRegistrering: AvlopRegistrering?
)

@Serializable
data class BruksenhetRegistreringRequest(
    val bruksenhetId: Long,
    val bruksarealRegistrering: BruksarealRegistrering?,
    val energikildeRegistrering: EnergikildeRegistrering?,
    val oppvarmingRegistrering: OppvarmingRegistrering?
)

@Serializable
data class EgenregistreringRequest(
    val bygningId: Long,
    val bygningRegistrering: BygningRegistreringRequest?,
    val bruksenhetRegistreringer: List<BruksenhetRegistreringRequest>?
)
