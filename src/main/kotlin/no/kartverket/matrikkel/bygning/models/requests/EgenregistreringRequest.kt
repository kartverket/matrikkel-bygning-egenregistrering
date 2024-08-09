package no.kartverket.matrikkel.bygning.models.requests

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopsKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingsKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningsKode
import no.kartverket.matrikkel.bygning.services.EgenregistreringsService.Validator.EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR

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
    val bygningsRegistrering: BygningsRegistrering, val bruksenhetRegistreringer: List<BruksenhetRegistrering>
)

enum class EgenregistreringValidationError(val errorMessage: String) {
    DateTooEarly("Gyldighetsdato er satt til å være for langt bak i tid, tidligste mulige registrering er $EARLIEST_POSSIBLE_EGENREGISTRERING_YEAR"),
    DateTooLate("Gyldighetsdato er satt til å være for langt frem i tid"),
    BygningDoesNotExist("Bygningen finnes ikke i matrikkelen"),
    BruksenhetIsNotConnectedToBygning("Bruksenheten finnes ikke i bygningen")
}

fun EgenregistreringValidationError.toErrorResponse(field: String?): EgenregistreringValidationErrorResponse {
    return EgenregistreringValidationErrorResponse(
        this.name,
        this.errorMessage,
        field = field
    )
}

@Serializable
data class EgenregistreringValidationErrorResponse(
    val code: String,
    val description: String,
    val field: String?,
)