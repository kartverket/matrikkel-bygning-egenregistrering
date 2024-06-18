package no.kartverket.matrikkel.bygning.models.requests

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.AvlopKode
import no.kartverket.matrikkel.bygning.models.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.OppvarmingsKode
import no.kartverket.matrikkel.bygning.models.VannforsyningKode

@Serializable
data class RegistreringMetadataRequest(
    val registreringstidspunkt: Instant,
    val gyldigFra: LocalDate?,
    val gyldigTil: LocalDate?,
)

@Serializable
data class ByggeaarRegistrering(
    val byggeaar: Int, val metadata: RegistreringMetadataRequest
)

@Serializable
data class BruksarealRegistrering(
    val bruksareal: Double, val metadata: RegistreringMetadataRequest
)

@Serializable
data class VannforsyningsRegistrering(
    val vannforsyning: VannforsyningKode, val metadata: RegistreringMetadataRequest
)

@Serializable
data class AvlopRegistrering(
    val avlop: AvlopKode, val metadata: RegistreringMetadataRequest
)

@Serializable
data class EnergikildeRegistrering(
    val energikilder: List<EnergikildeKode>, val metadata: RegistreringMetadataRequest
)

@Serializable
data class OppvarmingRegistrering(
    val oppvarminger: List<OppvarmingsKode>, val metadata: RegistreringMetadataRequest
)

@Serializable
data class BygningsRegistrering(
    val bygningId: String,
    val bruksareal: BruksarealRegistrering?,
    val byggeaar: ByggeaarRegistrering?,
    val vannforsyning: VannforsyningsRegistrering?,
    val avlop: AvlopRegistrering?
)

@Serializable
data class BruksenhetRegistrering(
    val bruksenhetId: String,
    val bruksareal: BruksarealRegistrering?,
    val energikilde: EnergikildeRegistrering?,
    val oppvarming: OppvarmingRegistrering?
)

@Serializable
data class EgenregistreringRequest(
    val bygningsRegistrering: BygningsRegistrering, val bruksenhetRegistreringer: List<BruksenhetRegistrering>
)
