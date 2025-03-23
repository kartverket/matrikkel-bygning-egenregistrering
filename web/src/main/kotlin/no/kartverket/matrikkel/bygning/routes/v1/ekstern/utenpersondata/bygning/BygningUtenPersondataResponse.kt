package no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.AvlopKodeUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksarealUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksenhetEtasjerUtenPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.utenpersondata.bygning.FeltUtenPersondataResponse.BruksenhetEtasjerUtenPersondataResponse.BruksenhetEtasjeUtenPersondataResponse
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
    val etasjer: BruksenhetEtasjerUtenPersondataResponse?,
    val vannforsyning: VannforsyningKodeUtenPersondataResponse?,
    val avlop: AvlopKodeUtenPersondataResponse?,
    val energikilder: List<EnergikildeUtenPersondataResponse>?,
    val oppvarming: List<OppvarmingUtenPersondataResponse>?,
)

sealed interface FeltUtenPersondataResponse<T> {
    val data: T
    val metadata: RegisterMetadataUtenPersondataResponse

    @Serializable
    data class ByggeaarUtenPersondataResponse(
        override val data: Int,
        override val metadata: RegisterMetadataUtenPersondataResponse,
    ) : FeltUtenPersondataResponse<Int>

    @Serializable
    data class BruksarealUtenPersondataResponse(
        override val data: Double,
        override val metadata: RegisterMetadataUtenPersondataResponse,
    ) : FeltUtenPersondataResponse<Double>

    @Serializable
    data class BruksenhetEtasjerUtenPersondataResponse(
        override val data: List<BruksenhetEtasjeUtenPersondataResponse>,
        override val metadata: RegisterMetadataUtenPersondataResponse,
    ) : FeltUtenPersondataResponse<List<BruksenhetEtasjeUtenPersondataResponse>> {
        @Serializable
        data class BruksenhetEtasjeUtenPersondataResponse(
            val etasjeBetegnelse: EtasjeBetegnelseUtenPersondataResponse,
            val bruksareal: Double,
        )

        @Serializable
        data class EtasjeBetegnelseUtenPersondataResponse(
            val etasjeplanKode: String,
            val etasjenummer: Int,
        )
    }

    @Serializable
    data class VannforsyningKodeUtenPersondataResponse(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadataUtenPersondataResponse,
    ) : FeltUtenPersondataResponse<VannforsyningKode>

    @Serializable
    data class AvlopKodeUtenPersondataResponse(
        override val data: AvlopKode,
        override val metadata: RegisterMetadataUtenPersondataResponse,
    ) : FeltUtenPersondataResponse<AvlopKode>

    @Serializable
    data class EnergikildeUtenPersondataResponse(
        override val data: EnergikildeKode,
        override val metadata: RegisterMetadataUtenPersondataResponse,
    ) : FeltUtenPersondataResponse<EnergikildeKode>

    @Serializable
    data class OppvarmingUtenPersondataResponse(
        override val data: OppvarmingKode,
        override val metadata: RegisterMetadataUtenPersondataResponse,
    ) : FeltUtenPersondataResponse<OppvarmingKode>
}

@Serializable
data class RegisterMetadataUtenPersondataResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?,
)

fun Bygning.toBygningUtenPersondataResponse(): BygningUtenPersondataResponse =
    BygningUtenPersondataResponse(
        bygningId = bygningBubbleId.value,
        bygningsnummer = bygningsnummer,
        bruksenheter =
            bruksenheter.map {
                it.toBruksenhetUtenPersondataResponse()
            },
    )

private fun RegisterMetadata.toRegisterMetadataUtenPersondataResponse(): RegisterMetadataUtenPersondataResponse =
    RegisterMetadataUtenPersondataResponse(
        registreringstidspunkt = registreringstidspunkt,
        kildemateriale = kildemateriale,
        prosess = prosess,
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
        felt.metadata.toRegisterMetadataUtenPersondataResponse(),
    )
}

private fun <U, T : Felt<U>, O : FeltUtenPersondataResponse<U>?> toListeFeltUtenPersondataResponse(
    list: List<T>?,
    constructor: (U, RegisterMetadataUtenPersondataResponse) -> O,
): List<O>? {
    if (list == null) {
        return null
    }

    return list.map { felt ->
        constructor(
            felt.data,
            felt.metadata.toRegisterMetadataUtenPersondataResponse(),
        )
    }
}

private fun Felt.BruksenhetEtasjer.toEtasjeUtenPersondataResponse(): BruksenhetEtasjerUtenPersondataResponse =
    BruksenhetEtasjerUtenPersondataResponse(
        data =
            this.data.map {
                BruksenhetEtasjeUtenPersondataResponse(
                    etasjeBetegnelse =
                        BruksenhetEtasjerUtenPersondataResponse.EtasjeBetegnelseUtenPersondataResponse(
                            etasjeplanKode = it.etasjebetegnelse.etasjeplanKode.toString(),
                            etasjenummer = it.etasjebetegnelse.etasjenummer.loepenummer,
                        ),
                    bruksareal = it.bruksareal,
                )
            },
        metadata = this.metadata.toRegisterMetadataUtenPersondataResponse(),
    )

fun Bruksenhet.toBruksenhetUtenPersondataResponse(): BruksenhetUtenPersondataResponse =
    BruksenhetUtenPersondataResponse(
        bruksenhetId = this.bruksenhetBubbleId.value,
        byggeaar = toFeltUtenPersondataResponse(this.byggeaar.egenregistrert, ::ByggeaarUtenPersondataResponse),
        totaltBruksareal = toFeltUtenPersondataResponse(this.totaltBruksareal.egenregistrert, ::BruksarealUtenPersondataResponse),
        etasjer = this.etasjer.egenregistrert?.toEtasjeUtenPersondataResponse(),
        vannforsyning = toFeltUtenPersondataResponse(this.vannforsyning.egenregistrert, ::VannforsyningKodeUtenPersondataResponse),
        avlop = toFeltUtenPersondataResponse(this.avlop.egenregistrert, ::AvlopKodeUtenPersondataResponse),
        energikilder =
            toListeFeltUtenPersondataResponse(
                this.energikilder.egenregistrert?.toEnergikilder(),
                ::EnergikildeUtenPersondataResponse,
            ),
        oppvarming =
            toListeFeltUtenPersondataResponse(
                this.oppvarming.egenregistrert?.toOppvarming(),
                ::OppvarmingUtenPersondataResponse,
            ),
    )
