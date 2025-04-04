package no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning

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
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.AvlopKodeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.BruksarealVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse.BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.ByggeaarVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.EnergikildeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.OppvarmingVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidetutenpii.bygning.FeltVirksomhetUtvidetUtenPIIResponse.VannforsyningKodeVirksomhetUtvidetUtenPIIResponse
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class BygningVirksomhetUtvidetUtenPIIResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetVirksomhetUtvidetUtenPIIResponse>,
)

@Serializable
data class BruksenhetVirksomhetUtvidetUtenPIIResponse(
    val bruksenhetId: Long,
    val byggeaar: ByggeaarVirksomhetUtvidetUtenPIIResponse?,
    val totaltBruksareal: BruksarealVirksomhetUtvidetUtenPIIResponse?,
    val etasjer: BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse?,
    val vannforsyning: VannforsyningKodeVirksomhetUtvidetUtenPIIResponse?,
    val avlop: AvlopKodeVirksomhetUtvidetUtenPIIResponse?,
    val energikilder: List<EnergikildeVirksomhetUtvidetUtenPIIResponse>?,
    val oppvarming: List<OppvarmingVirksomhetUtvidetUtenPIIResponse>?,
)

sealed interface FeltVirksomhetUtvidetUtenPIIResponse<T> {
    val data: T
    val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse

    @Serializable
    data class ByggeaarVirksomhetUtvidetUtenPIIResponse(
        override val data: Int,
        override val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse,
    ) : FeltVirksomhetUtvidetUtenPIIResponse<Int>

    @Serializable
    data class BruksarealVirksomhetUtvidetUtenPIIResponse(
        override val data: Double,
        override val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse,
    ) : FeltVirksomhetUtvidetUtenPIIResponse<Double>

    @Serializable
    data class BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse(
        override val data: List<BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse>,
        override val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse,
    ) : FeltVirksomhetUtvidetUtenPIIResponse<List<BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse>> {
        @Serializable
        data class BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse(
            val etasjeBetegnelse: EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse,
            val bruksareal: Double,
        )

        @Serializable
        data class EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse(
            val etasjeplanKode: String,
            val etasjenummer: Int,
        )
    }

    @Serializable
    data class VannforsyningKodeVirksomhetUtvidetUtenPIIResponse(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse,
    ) : FeltVirksomhetUtvidetUtenPIIResponse<VannforsyningKode>

    @Serializable
    data class AvlopKodeVirksomhetUtvidetUtenPIIResponse(
        override val data: AvlopKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse,
    ) : FeltVirksomhetUtvidetUtenPIIResponse<AvlopKode>

    @Serializable
    data class EnergikildeVirksomhetUtvidetUtenPIIResponse(
        override val data: EnergikildeKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse,
    ) : FeltVirksomhetUtvidetUtenPIIResponse<EnergikildeKode>

    @Serializable
    data class OppvarmingVirksomhetUtvidetUtenPIIResponse(
        override val data: OppvarmingKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetUtenPIIResponse,
    ) : FeltVirksomhetUtvidetUtenPIIResponse<OppvarmingKode>
}

@Serializable
data class RegisterMetadataVirksomhetUtvidetUtenPIIResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?,
)

fun Bygning.toBygningVirksomhetUtvidetUtenPIIResponse(): BygningVirksomhetUtvidetUtenPIIResponse =
    BygningVirksomhetUtvidetUtenPIIResponse(
        bygningId = bygningBubbleId.value,
        bygningsnummer = bygningsnummer,
        bruksenheter =
            bruksenheter.map {
                it.toBruksenhetVirksomhetUtvidetUtenPIIResponse()
            },
    )

private fun RegisterMetadata.toRegisterMetadataVirksomhetUtvidetUtenPIIResponse(): RegisterMetadataVirksomhetUtvidetUtenPIIResponse =
    RegisterMetadataVirksomhetUtvidetUtenPIIResponse(
        registreringstidspunkt = registreringstidspunkt,
        kildemateriale = kildemateriale,
        prosess = prosess,
    )

private fun <U, T : Felt<U>, O : FeltVirksomhetUtvidetUtenPIIResponse<U>?> toFeltVirksomhetUtvidetUtenPIIResponse(
    felt: T?,
    constructor: (U, RegisterMetadataVirksomhetUtvidetUtenPIIResponse) -> O,
): O? {
    if (felt == null) {
        return null
    }

    return constructor(
        felt.data,
        felt.metadata.toRegisterMetadataVirksomhetUtvidetUtenPIIResponse(),
    )
}

private fun <U, T : Felt<U>, O : FeltVirksomhetUtvidetUtenPIIResponse<U>?> toListeFeltVirksomhetUtvidetUtenPIIResponse(
    list: List<T>?,
    constructor: (U, RegisterMetadataVirksomhetUtvidetUtenPIIResponse) -> O,
): List<O>? {
    if (list == null) {
        return null
    }

    return list.map { felt ->
        constructor(
            felt.data,
            felt.metadata.toRegisterMetadataVirksomhetUtvidetUtenPIIResponse(),
        )
    }
}

private fun Felt.BruksenhetEtasjer.toEtasjeVirksomhetUtvidetUtenPIIResponse(): BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse =
    BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse(
        data =
            this.data.map {
                BruksenhetEtasjeVirksomhetUtvidetUtenPIIResponse(
                    etasjeBetegnelse =
                        BruksenhetEtasjerVirksomhetUtvidetUtenPIIResponse.EtasjeBetegnelseVirksomhetUtvidetUtenPIIResponse(
                            etasjeplanKode = it.etasjebetegnelse.etasjeplanKode.toString(),
                            etasjenummer = it.etasjebetegnelse.etasjenummer.loepenummer,
                        ),
                    bruksareal = it.bruksareal,
                )
            },
        metadata = this.metadata.toRegisterMetadataVirksomhetUtvidetUtenPIIResponse(),
    )

fun Bruksenhet.toBruksenhetVirksomhetUtvidetUtenPIIResponse(): BruksenhetVirksomhetUtvidetUtenPIIResponse =
    BruksenhetVirksomhetUtvidetUtenPIIResponse(
        bruksenhetId = this.bruksenhetBubbleId.value,
        byggeaar =
            toFeltVirksomhetUtvidetUtenPIIResponse(
                this.byggeaar.egenregistrert,
                ::ByggeaarVirksomhetUtvidetUtenPIIResponse,
            ),
        totaltBruksareal =
            toFeltVirksomhetUtvidetUtenPIIResponse(
                this.totaltBruksareal.egenregistrert,
                ::BruksarealVirksomhetUtvidetUtenPIIResponse,
            ),
        etasjer = this.etasjer.egenregistrert?.toEtasjeVirksomhetUtvidetUtenPIIResponse(),
        vannforsyning =
            toFeltVirksomhetUtvidetUtenPIIResponse(
                this.vannforsyning.egenregistrert,
                ::VannforsyningKodeVirksomhetUtvidetUtenPIIResponse,
            ),
        avlop = toFeltVirksomhetUtvidetUtenPIIResponse(this.avlop.egenregistrert, ::AvlopKodeVirksomhetUtvidetUtenPIIResponse),
        energikilder =
            toListeFeltVirksomhetUtvidetUtenPIIResponse(
                this.energikilder.egenregistrert,
                ::EnergikildeVirksomhetUtvidetUtenPIIResponse,
            ),
        oppvarming =
            toListeFeltVirksomhetUtvidetUtenPIIResponse(
                this.oppvarming.egenregistrert,
                ::OppvarmingVirksomhetUtvidetUtenPIIResponse,
            ),
    )
