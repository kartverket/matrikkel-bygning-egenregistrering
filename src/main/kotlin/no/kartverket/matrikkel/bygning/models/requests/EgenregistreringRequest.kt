package no.kartverket.matrikkel.bygning.models.requests

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopsKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingsKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningsKode

@Serializable
data class RegistreringMetadataRequest(
    val registreringstidspunkt: Instant,
    val gyldigFra: LocalDate?,
    val gyldigTil: LocalDate?,
) {
    init {
        if (gyldigFra != null && gyldigTil != null) {
            require(gyldigTil >= gyldigFra) {
                "Gyldig til dato kan ikke være satt til før/samtidig som gyldig fra dato"
            }
        }
    }
}

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
    val vannforsyning: VannforsyningsKode, val metadata: RegistreringMetadataRequest
)

@Serializable
data class AvlopRegistrering(
    val avlop: AvlopsKode, val metadata: RegistreringMetadataRequest
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
    val bruksareal: BruksarealRegistrering?,
    val byggeaar: ByggeaarRegistrering?,
    val vannforsyning: VannforsyningsRegistrering?,
    val avlop: AvlopRegistrering?
)

@Serializable
data class BruksenhetRegistrering(
    val bruksenhetId: Long,
    val bruksareal: BruksarealRegistrering?,
    val energikilde: EnergikildeRegistrering?,
    val oppvarming: OppvarmingRegistrering?
)

@Serializable
data class EgenregistreringRequest(
    val bygningsRegistrering: BygningsRegistrering, val bruksenhetRegistreringer: List<BruksenhetRegistrering>?
)
