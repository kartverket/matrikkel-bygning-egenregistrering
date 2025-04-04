package no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning

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
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.AvlopKodeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.BruksarealVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.BruksenhetEtasjerVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.BruksenhetEtasjerVirksomhetUtvidetResponse.BruksenhetEtasjeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.ByggeaarVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.EnergikildeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.OppvarmingVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetutvidet.bygning.FeltVirksomhetUtvidetResponse.VannforsyningKodeVirksomhetUtvidetResponse
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class BygningVirksomhetUtvidetResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetVirksomhetUtvidetResponse>,
)

@Serializable
data class BruksenhetVirksomhetUtvidetResponse(
    val bruksenhetId: Long,
    val byggeaar: ByggeaarVirksomhetUtvidetResponse?,
    val totaltBruksareal: BruksarealVirksomhetUtvidetResponse?,
    val etasjer: BruksenhetEtasjerVirksomhetUtvidetResponse?,
    val vannforsyning: VannforsyningKodeVirksomhetUtvidetResponse?,
    val avlop: AvlopKodeVirksomhetUtvidetResponse?,
    val energikilder: List<EnergikildeVirksomhetUtvidetResponse>?,
    val oppvarming: List<OppvarmingVirksomhetUtvidetResponse>?,
)

sealed interface FeltVirksomhetUtvidetResponse<T> {
    val data: T
    val metadata: RegisterMetadataVirksomhetUtvidetResponse

    @Serializable
    data class ByggeaarVirksomhetUtvidetResponse(
        override val data: Int,
        override val metadata: RegisterMetadataVirksomhetUtvidetResponse,
    ) : FeltVirksomhetUtvidetResponse<Int>

    @Serializable
    data class BruksarealVirksomhetUtvidetResponse(
        override val data: Double,
        override val metadata: RegisterMetadataVirksomhetUtvidetResponse,
    ) : FeltVirksomhetUtvidetResponse<Double>

    @Serializable
    data class BruksenhetEtasjerVirksomhetUtvidetResponse(
        override val data: List<BruksenhetEtasjeVirksomhetUtvidetResponse>,
        override val metadata: RegisterMetadataVirksomhetUtvidetResponse,
    ) : FeltVirksomhetUtvidetResponse<List<BruksenhetEtasjeVirksomhetUtvidetResponse>> {
        @Serializable
        data class BruksenhetEtasjeVirksomhetUtvidetResponse(
            val etasjeBetegnelse: EtasjeBetegnelseVirksomhetUtvidetResponse,
            val bruksareal: Double,
        )

        @Serializable
        data class EtasjeBetegnelseVirksomhetUtvidetResponse(
            val etasjeplanKode: String,
            val etasjenummer: Int,
        )
    }

    @Serializable
    data class VannforsyningKodeVirksomhetUtvidetResponse(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetResponse,
    ) : FeltVirksomhetUtvidetResponse<VannforsyningKode>

    @Serializable
    data class AvlopKodeVirksomhetUtvidetResponse(
        override val data: AvlopKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetResponse,
    ) : FeltVirksomhetUtvidetResponse<AvlopKode>

    @Serializable
    data class EnergikildeVirksomhetUtvidetResponse(
        override val data: EnergikildeKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetResponse,
    ) : FeltVirksomhetUtvidetResponse<EnergikildeKode>

    @Serializable
    data class OppvarmingVirksomhetUtvidetResponse(
        override val data: OppvarmingKode,
        override val metadata: RegisterMetadataVirksomhetUtvidetResponse,
    ) : FeltVirksomhetUtvidetResponse<OppvarmingKode>
}

@Serializable
data class RegisterMetadataVirksomhetUtvidetResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?,
    val gyldighetsaar: Int?,
    val opphoersaar: Int?,
)

fun Bygning.toBygningVirksomhetUtvidetResponse(): BygningVirksomhetUtvidetResponse =
    BygningVirksomhetUtvidetResponse(
        bygningId = bygningBubbleId.value,
        bygningsnummer = bygningsnummer,
        bruksenheter =
            bruksenheter.map {
                it.toBruksenhetVirksomhetUtvidetResponse()
            },
    )

private fun RegisterMetadata.toRegisterMetadataVirksomhetUtvidetResponse(): RegisterMetadataVirksomhetUtvidetResponse =
    RegisterMetadataVirksomhetUtvidetResponse(
        registreringstidspunkt = registreringstidspunkt,
        kildemateriale = kildemateriale,
        prosess = prosess,
        registrertAv = registrertAv.value,
        gyldighetsaar = gyldighetsperiode.gyldighetsaar?.value,
        opphoersaar = gyldighetsperiode.opphoersaar?.value,
    )

internal fun <U, T : Felt<U>, O : FeltVirksomhetUtvidetResponse<U>?> toFeltVirksomhetUtvidetResponse(
    felt: T?,
    constructor: (U, RegisterMetadataVirksomhetUtvidetResponse) -> O,
): O? {
    if (felt == null) {
        return null
    }

    return constructor(
        felt.data,
        felt.metadata.toRegisterMetadataVirksomhetUtvidetResponse(),
    )
}

internal fun <U, T : Felt<U>, O : FeltVirksomhetUtvidetResponse<U>?> toListeFeltVirksomhetUtvidetResponse(
    list: List<T>?,
    constructor: (U, RegisterMetadataVirksomhetUtvidetResponse) -> O,
): List<O>? {
    if (list == null) {
        return null
    }

    return list.map { felt ->
        constructor(
            felt.data,
            felt.metadata.toRegisterMetadataVirksomhetUtvidetResponse(),
        )
    }
}

private fun Felt.BruksenhetEtasjer.toEtasjeVirksomhetUtvidetResponse(): BruksenhetEtasjerVirksomhetUtvidetResponse =
    BruksenhetEtasjerVirksomhetUtvidetResponse(
        data =
            this.data.map {
                BruksenhetEtasjeVirksomhetUtvidetResponse(
                    etasjeBetegnelse =
                        BruksenhetEtasjerVirksomhetUtvidetResponse.EtasjeBetegnelseVirksomhetUtvidetResponse(
                            etasjeplanKode = it.etasjebetegnelse.etasjeplanKode.toString(),
                            etasjenummer = it.etasjebetegnelse.etasjenummer.loepenummer,
                        ),
                    bruksareal = it.bruksareal,
                )
            },
        metadata = this.metadata.toRegisterMetadataVirksomhetUtvidetResponse(),
    )

fun Bruksenhet.toBruksenhetVirksomhetUtvidetResponse(): BruksenhetVirksomhetUtvidetResponse =
    BruksenhetVirksomhetUtvidetResponse(
        bruksenhetId = this.bruksenhetBubbleId.value,
        byggeaar = toFeltVirksomhetUtvidetResponse(this.byggeaar.egenregistrert, ::ByggeaarVirksomhetUtvidetResponse),
        totaltBruksareal =
            toFeltVirksomhetUtvidetResponse(
                this.totaltBruksareal.egenregistrert,
                ::BruksarealVirksomhetUtvidetResponse,
            ),
        etasjer = this.etasjer.egenregistrert?.toEtasjeVirksomhetUtvidetResponse(),
        vannforsyning =
            toFeltVirksomhetUtvidetResponse(
                this.vannforsyning.egenregistrert,
                ::VannforsyningKodeVirksomhetUtvidetResponse,
            ),
        avlop = toFeltVirksomhetUtvidetResponse(this.avlop.egenregistrert, ::AvlopKodeVirksomhetUtvidetResponse),
        energikilder = toListeFeltVirksomhetUtvidetResponse(this.energikilder.egenregistrert, ::EnergikildeVirksomhetUtvidetResponse),
        oppvarming = toListeFeltVirksomhetUtvidetResponse(this.oppvarming.egenregistrert, ::OppvarmingVirksomhetUtvidetResponse),
    )
