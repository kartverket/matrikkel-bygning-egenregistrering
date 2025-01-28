package no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.kodelister.*
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.bygning.FeltEksternResponse.*
import no.kartverket.matrikkel.bygning.application.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class BygningEksternResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetEksternResponse>,
)


@Serializable
data class BruksenhetEksternResponse(
    val bruksenhetId: Long,
    val byggeaar: ByggeaarEksternResponse?,
    val totaltBruksareal: BruksarealEksternResponse?,
    val vannforsyning: VannforsyningKodeEksternResponse?,
    val avlop: AvlopKodeEksternResponse?,
    val energikilder: List<EnergikildeEksternResponse>?,
    val oppvarminger: List<OppvarmingEksternResponse>?,
)

sealed interface FeltEksternResponse<T> {
    val data: T?
    val metadata: RegisterMetadataEksternResponse

    @Serializable
    data class ByggeaarEksternResponse(
        override val data: Int?,
        override val metadata: RegisterMetadataEksternResponse
    ) :
        FeltEksternResponse<Int?>

    @Serializable
    data class BruksarealEksternResponse(
        override val data: Double?,
        override val metadata: RegisterMetadataEksternResponse
    ) :
        FeltEksternResponse<Double?>

    @Serializable
    data class VannforsyningKodeEksternResponse(
        override val data: VannforsyningKode?,
        override val metadata: RegisterMetadataEksternResponse
    ) : FeltEksternResponse<VannforsyningKode?>

    @Serializable
    data class AvlopKodeEksternResponse(
        override val data: AvlopKode?,
        override val metadata: RegisterMetadataEksternResponse
    ) :
        FeltEksternResponse<AvlopKode?>

    @Serializable
    data class EnergikildeEksternResponse(
        override val data: EnergikildeKode?,
        override val metadata: RegisterMetadataEksternResponse
    ) :
        FeltEksternResponse<EnergikildeKode?>

    @Serializable
    data class OppvarmingEksternResponse(
        override val data: OppvarmingKode?,
        override val metadata: RegisterMetadataEksternResponse
    ) :
        FeltEksternResponse<OppvarmingKode>
}


@Serializable
data class RegisterMetadataEksternResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val kildemateriale: KildematerialeKode? = null
)


fun Bygning.toBygningEksternResponse(): BygningEksternResponse = BygningEksternResponse(
    bygningId = bygningId,
    bygningsnummer = bygningsnummer,
    bruksenheter = bruksenheter.map {
        it.toBruksenhetEksternResponse()
    },
)

private fun <U, T : Felt<U?>, O : FeltEksternResponse<U>?> toFeltEksternResponse(
    felt: T?,
    constructor: (U?, RegisterMetadataEksternResponse) -> O,
): O? {
    if (felt == null) {
        return null
    }

    return constructor(
        felt.data,
        RegisterMetadataEksternResponse(
            registreringstidspunkt = felt.metadata.registreringstidspunkt,
            kildemateriale = felt.metadata.kildemateriale,
        ),
    )
}

private fun Bruksenhet.toBruksenhetEksternResponse(): BruksenhetEksternResponse = BruksenhetEksternResponse(
    bruksenhetId = this.bruksenhetId,
    byggeaar = toFeltEksternResponse(this.byggeaar.egenregistrert, ::ByggeaarEksternResponse),
    totaltBruksareal = toFeltEksternResponse(this.totaltBruksareal.egenregistrert, ::BruksarealEksternResponse),
    vannforsyning = toFeltEksternResponse(this.vannforsyning.egenregistrert, ::VannforsyningKodeEksternResponse),
    avlop = toFeltEksternResponse(this.avlop.egenregistrert, ::AvlopKodeEksternResponse),
    energikilder = this.energikilder.egenregistrert?.mapNotNull { energikilde ->
        toFeltEksternResponse(energikilde, ::EnergikildeEksternResponse)
    },
    oppvarminger = this.oppvarminger.egenregistrert?.mapNotNull { oppvarming ->
        toFeltEksternResponse(oppvarming, ::OppvarmingEksternResponse)
    },
)
