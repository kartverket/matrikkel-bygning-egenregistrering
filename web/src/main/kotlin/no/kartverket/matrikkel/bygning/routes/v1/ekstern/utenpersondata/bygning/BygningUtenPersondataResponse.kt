package no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.AvlopKodeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksarealUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.ByggeaarUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.EnergikildeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.OppvarmingUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.VannforsyningKodeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class BygningUtenPersondataResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetUtenPersondataResponse>,
)

@Serializable
data class BruksenhetUtenPersondataResponse(
    val bruksenhetId: Long,
    val byggeaar: ByggeaarUtenPersondataResponse?,
    val totaltBruksareal: BruksarealUtenPersondataResponse?,
    val vannforsyning: VannforsyningKodeUtenPersondataResponse?,
    val avlop: AvlopKodeUtenPersondataResponse?,
    val energikilder: EnergikildeUtenPersondataResponse?,
    val oppvarming: OppvarmingUtenPersondataResponse?,
)

sealed interface FeltUtenPersondataResponse<T> {
    val data: T
    val metadata: RegisterMetadataUtenPersondataResponse

    @Serializable
    data class ByggeaarUtenPersondataResponse(
        override val data: Int,
        override val metadata: RegisterMetadataUtenPersondataResponse
    ) : FeltUtenPersondataResponse<Int>

    @Serializable
    data class BruksarealUtenPersondataResponse(
        override val data: Double,
        override val metadata: RegisterMetadataUtenPersondataResponse
    ) : FeltUtenPersondataResponse<Double>

    @Serializable
    data class VannforsyningKodeUtenPersondataResponse(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadataUtenPersondataResponse
    ) : FeltUtenPersondataResponse<VannforsyningKode>

    @Serializable
    data class AvlopKodeUtenPersondataResponse(
        override val data: AvlopKode,
        override val metadata: RegisterMetadataUtenPersondataResponse
    ) : FeltUtenPersondataResponse<AvlopKode>

    @Serializable
    data class EnergikildeUtenPersondataResponse(
        override val data: List<EnergikildeKode>,
        override val metadata: RegisterMetadataUtenPersondataResponse
    ) : FeltUtenPersondataResponse<List<EnergikildeKode>>

    @Serializable
    data class OppvarmingUtenPersondataResponse(
        override val data: List<OppvarmingKode>,
        override val metadata: RegisterMetadataUtenPersondataResponse
    ) : FeltUtenPersondataResponse<List<OppvarmingKode>>
}


@Serializable
data class RegisterMetadataUtenPersondataResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val kildemateriale: KildematerialeKode? = null
)


fun Bygning.toBygningUtenPersondataResponse(): BygningUtenPersondataResponse = BygningUtenPersondataResponse(
    bygningId = bygningBubbleId.value,
    bygningsnummer = bygningsnummer,
    bruksenheter = bruksenheter.map {
        it.toBruksenhetUtenPersondataResponse()
    },
)

private fun <U, T : Felt<U>, O : FeltUtenPersondataResponse<U>?> toFeltUtenPersondataResponse(
    felt: T?,
    constructor: (U, RegisterMetadataUtenPersondataResponse) -> O,
): O? {
    if (felt == null) {
        return null
    }

    return constructor(
        felt.data,
        RegisterMetadataUtenPersondataResponse(
            registreringstidspunkt = felt.metadata.registreringstidspunkt,
            kildemateriale = felt.metadata.kildemateriale,
        ),
    )
}

fun Bruksenhet.toBruksenhetUtenPersondataResponse(): BruksenhetUtenPersondataResponse = BruksenhetUtenPersondataResponse(
        bruksenhetId = this.bruksenhetBubbleId.value,
        byggeaar = toFeltUtenPersondataResponse(this.byggeaar.egenregistrert, ::ByggeaarUtenPersondataResponse),
        totaltBruksareal = toFeltUtenPersondataResponse(this.totaltBruksareal.egenregistrert, ::BruksarealUtenPersondataResponse),
        vannforsyning = toFeltUtenPersondataResponse(this.vannforsyning.egenregistrert, ::VannforsyningKodeUtenPersondataResponse),
        avlop = toFeltUtenPersondataResponse(this.avlop.egenregistrert, ::AvlopKodeUtenPersondataResponse),
        energikilder = toFeltUtenPersondataResponse(this.energikilder.egenregistrert, ::EnergikildeUtenPersondataResponse),
        oppvarming = TODO() //toFeltUtenPersondataResponse(this.oppvarminger.egenregistrert, ::OppvarmingUtenPersondataResponse),
)
