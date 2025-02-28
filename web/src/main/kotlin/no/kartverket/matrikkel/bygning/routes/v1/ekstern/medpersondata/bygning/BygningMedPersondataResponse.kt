package no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.kodelister.*
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.*
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class BygningMedPersondataResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetMedPersondataResponse>,
)

@Serializable
data class BruksenhetMedPersondataResponse(
    val bruksenhetId: Long,
    val byggeaar: ByggeaarMedPersondataResponse?,
    val totaltBruksareal: BruksarealMedPersondataResponse?,
    val vannforsyning: VannforsyningKodeMedPersondataResponse?,
    val avlop: AvlopKodeMedPersondataResponse?,
    val energikilder: EnergikildeMedPersondataResponse?,
    val oppvarminger: OppvarmingMedPersondataResponse?,
)

sealed interface FeltMedPersondataResponse<T> {
    val data: T
    val metadata: RegisterMetadataMedPersondataResponse

    @Serializable
    data class ByggeaarMedPersondataResponse(
        override val data: Int,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<Int>

    @Serializable
    data class BruksarealMedPersondataResponse(
        override val data: Double,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<Double>

    @Serializable
    data class VannforsyningKodeMedPersondataResponse(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<VannforsyningKode>

    @Serializable
    data class AvlopKodeMedPersondataResponse(
        override val data: AvlopKode,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<AvlopKode>

    @Serializable
    data class EnergikildeMedPersondataResponse(
        override val data: List<EnergikildeKode>,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<List<EnergikildeKode>>

    @Serializable
    data class OppvarmingMedPersondataResponse(
        override val data: List<OppvarmingKode>,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<List<OppvarmingKode>>
}


@Serializable
data class RegisterMetadataMedPersondataResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String,
    val kildemateriale: KildematerialeKode? = null
)

fun Bygning.toBygningMedPersondataResponse(): BygningMedPersondataResponse = BygningMedPersondataResponse(
    bygningId = bygningBubbleId.value,
    bygningsnummer = bygningsnummer,
    bruksenheter = bruksenheter.map {
        it.toBruksenhetMedPersondataResponse()
    },
)

internal fun <U, T : Felt<U>, O : FeltMedPersondataResponse<U>?> toFeltMedPersondataResponse(
    felt: T?,
    constructor: (U, RegisterMetadataMedPersondataResponse) -> O,
): O? {
    if (felt == null) {
        return null
    }

    return constructor(
        felt.data,
        RegisterMetadataMedPersondataResponse(
            registreringstidspunkt = felt.metadata.registreringstidspunkt,
            registrertAv = felt.metadata.registrertAv.value,
            kildemateriale = felt.metadata.kildemateriale,
        ),
    )
}

fun Bruksenhet.toBruksenhetMedPersondataResponse(): BruksenhetMedPersondataResponse = BruksenhetMedPersondataResponse(
    bruksenhetId = this.bruksenhetBubbleId.value,
    byggeaar = toFeltMedPersondataResponse(this.byggeaar.egenregistrert, ::ByggeaarMedPersondataResponse),
    totaltBruksareal = toFeltMedPersondataResponse(
        this.totaltBruksareal.egenregistrert,
        ::BruksarealMedPersondataResponse
    ),
    vannforsyning = toFeltMedPersondataResponse(
        this.vannforsyning.egenregistrert,
        ::VannforsyningKodeMedPersondataResponse
    ),
    avlop = toFeltMedPersondataResponse(this.avlop.egenregistrert, ::AvlopKodeMedPersondataResponse),
    energikilder = toFeltMedPersondataResponse(this.energikilder.egenregistrert, ::EnergikildeMedPersondataResponse),
    oppvarminger = toFeltMedPersondataResponse(this.oppvarminger.egenregistrert, ::OppvarmingMedPersondataResponse),
)
