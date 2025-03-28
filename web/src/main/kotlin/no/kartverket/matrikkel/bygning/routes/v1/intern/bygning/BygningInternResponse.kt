package no.kartverket.matrikkel.bygning.routes.v1.intern.bygning

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.EnergikildeOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.OppvarmingOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class MultikildeInternResponse<T : Any>(
    val autoritativ: T? = null, val egenregistrert: T? = null
)

@Serializable
data class BygningInternResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val byggeaar: MultikildeInternResponse<ByggeaarInternResponse>?,
    val bruksareal: MultikildeInternResponse<BruksarealInternResponse>?,
    val vannforsyning: MultikildeInternResponse<VannforsyningKodeInternResponse>?,
    val avlop: MultikildeInternResponse<AvlopKodeInternResponse>?,
    val energikilder: MultikildeInternResponse<List<EnergikildeInternResponse>>?,
    val oppvarming: MultikildeInternResponse<List<OppvarmingInternResponse>>?,
    val bruksenheter: List<BruksenhetInternResponse>,
)

@Serializable
data class BygningSimpleResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetSimpleResponse>
)

@Serializable
data class BruksenhetEtasjerInternResponse(
    val data: List<BruksenhetEtasjeInternResponse>,
    val metadata: RegisterMetadataInternResponse
)

@Serializable
data class BruksenhetEtasjeInternResponse(
    val etasjebetegnelse: EtasjeBetegnelseInternResponse,
    val bruksareal: Double,
)

@Serializable
data class EtasjeBetegnelseInternResponse(
    val etasjeplanKode: String,
    val etasjenummer: Int,
)

@Serializable
data class BruksenhetInternResponse(
    val bruksenhetId: Long,
    val etasjer: MultikildeInternResponse<BruksenhetEtasjerInternResponse>?,
    val byggeaar: MultikildeInternResponse<ByggeaarInternResponse>?,
    val totaltBruksareal: MultikildeInternResponse<BruksarealInternResponse>?,
    val vannforsyning: MultikildeInternResponse<VannforsyningKodeInternResponse>?,
    val avlop: MultikildeInternResponse<AvlopKodeInternResponse>?,
    val energikilder: MultikildeInternResponse<List<EnergikildeInternResponse>>?,
    val oppvarming: MultikildeInternResponse<List<OppvarmingInternResponse>>?,
)

@Serializable
data class BruksenhetSimpleResponse(
    val bruksenhetId: Long,
    val etasjer: BruksenhetEtasjerInternResponse?,
    val byggeaar: ByggeaarInternResponse?,
    val totaltBruksareal: BruksarealInternResponse?,
    val vannforsyning: VannforsyningKodeInternResponse?,
    val avlop: AvlopKodeInternResponse?,
    val energikilder: List<EnergikildeInternResponse>,
    val oppvarming: List<OppvarmingInternResponse>,
)

@Serializable
data class RegisterMetadataInternResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String,
    val kildemateriale: KildematerialeKode?,
    val prosess: ProsessKode?,
    val gyldighetsaar: Int?,
    val opphoersaar: Int?,
)

@Serializable
data class ByggeaarInternResponse(val data: Int, val metadata: RegisterMetadataInternResponse)

@Serializable
data class BruksarealInternResponse(val data: Double, val metadata: RegisterMetadataInternResponse)

@Serializable
data class VannforsyningKodeInternResponse(val data: VannforsyningKode, val metadata: RegisterMetadataInternResponse)

@Serializable
data class AvlopKodeInternResponse(val data: AvlopKode, val metadata: RegisterMetadataInternResponse)

@Serializable
data class EnergikildeInternResponse(val data: EnergikildeKode, val metadata: RegisterMetadataInternResponse)

@Serializable
data class OppvarmingInternResponse(val data: OppvarmingKode, val metadata: RegisterMetadataInternResponse)


fun RegisterMetadata.toRegisterMetadataInternResponse() = RegisterMetadataInternResponse(
    registreringstidspunkt = this.registreringstidspunkt,
    registrertAv = this.registrertAv.value,
    kildemateriale = this.kildemateriale,
    prosess = this.prosess,
    gyldighetsaar = this.gyldighetsperiode.gyldighetsaar?.value,
    opphoersaar = this.gyldighetsperiode.opphoersaar?.value,
)

fun <T : Any, R : Any> Multikilde<T>.toMultikildeInternResponse(mapper: T.() -> R): MultikildeInternResponse<R>? {
    return MultikildeInternResponse(
        autoritativ?.mapper(),
        egenregistrert?.mapper(),
    ).takeUnless { autoritativ == null && egenregistrert == null }
}

fun Bygning.toBygningInternResponse(): BygningInternResponse = BygningInternResponse(
    bygningId = this.bygningBubbleId.value,
    bygningsnummer = this.bygningsnummer,
    byggeaar = this.byggeaar.toMultikildeInternResponse(Byggeaar::toByggeaarInternResponse),
    bruksareal = this.bruksareal.toMultikildeInternResponse(Bruksareal::toBruksarealResponse),
    bruksenheter = this.bruksenheter.map(Bruksenhet::toBruksenhetResponse),
    vannforsyning = this.vannforsyning.toMultikildeInternResponse(Vannforsyning::toVannforsyningResponse),
    avlop = this.avlop.toMultikildeInternResponse(Avlop::toAvlopKodeResponse),
    energikilder = this.energikilder.toMultikildeInternResponse { map { it.toEnergikildeResponse() } },
    oppvarming = this.oppvarming.toMultikildeInternResponse { map { it.toOppvarmingResponse() } },
)

fun Bygning.toBygningSimpleResponseFromEgenregistrertData(): BygningSimpleResponse = BygningSimpleResponse(
    bygningId = this.bygningBubbleId.value,
    bygningsnummer = this.bygningsnummer,
    bruksenheter = this.bruksenheter.map { it.toBruksenhetSimpleResponseFromEgenregistrertData() },
)

fun Bruksenhet.toBruksenhetResponse(): BruksenhetInternResponse = BruksenhetInternResponse(
    bruksenhetId = this.bruksenhetBubbleId.value,
    byggeaar = this.byggeaar.toMultikildeInternResponse(Byggeaar::toByggeaarInternResponse),
    etasjer = this.etasjer.toMultikildeInternResponse(BruksenhetEtasjer::toBruksenhetEtasjeResponse),
    totaltBruksareal = this.totaltBruksareal.toMultikildeInternResponse(Bruksareal::toBruksarealResponse),
    energikilder = this.energikilder.toMultikildeInternResponse(EnergikildeOpplysning::toEnergikildeResponse),
    oppvarming = this.oppvarming.toMultikildeInternResponse(OppvarmingOpplysning::toOppvarmingResponse),
    vannforsyning = this.vannforsyning.toMultikildeInternResponse(Vannforsyning::toVannforsyningResponse),
    avlop = this.avlop.toMultikildeInternResponse(Avlop::toAvlopKodeResponse),
)

fun Bruksenhet.toBruksenhetSimpleResponseFromEgenregistrertData(): BruksenhetSimpleResponse = BruksenhetSimpleResponse(
    bruksenhetId = this.bruksenhetBubbleId.value,
    byggeaar = this.byggeaar.egenregistrert?.toByggeaarInternResponse(),
    etasjer = this.etasjer.egenregistrert?.toBruksenhetEtasjeResponse(),
    totaltBruksareal = this.totaltBruksareal.egenregistrert?.toBruksarealResponse(),
    vannforsyning = this.vannforsyning.egenregistrert?.toVannforsyningResponse(),
    avlop = this.avlop.egenregistrert?.toAvlopKodeResponse(),
    energikilder = this.energikilder.egenregistrert?.toEnergikildeResponse() ?: emptyList(),
    oppvarming = this.oppvarming.egenregistrert?.toOppvarmingResponse() ?: emptyList(),
)

private fun BruksenhetEtasjer.toBruksenhetEtasjeResponse(): BruksenhetEtasjerInternResponse = BruksenhetEtasjerInternResponse(
    data = this.data.map {
        BruksenhetEtasjeInternResponse(
            bruksareal = it.bruksareal,
            etasjebetegnelse = EtasjeBetegnelseInternResponse(
                etasjeplanKode = it.etasjebetegnelse.etasjeplanKode.toString(),
                etasjenummer = it.etasjebetegnelse.etasjenummer.loepenummer,
            ),
        )
    },
    metadata = this.metadata.toRegisterMetadataInternResponse(),
)

private fun Byggeaar.toByggeaarInternResponse(): ByggeaarInternResponse = ByggeaarInternResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataInternResponse(),
)

private fun Bruksareal.toBruksarealResponse(): BruksarealInternResponse = BruksarealInternResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataInternResponse(),
)

private fun Vannforsyning.toVannforsyningResponse(): VannforsyningKodeInternResponse = VannforsyningKodeInternResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataInternResponse(),
)

private fun Avlop.toAvlopKodeResponse(): AvlopKodeInternResponse = AvlopKodeInternResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataInternResponse(),
)

private fun Energikilde.toEnergikildeResponse() = EnergikildeInternResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataInternResponse(),
)

private fun EnergikildeOpplysning.toEnergikildeResponse() = this.toEnergikilder().map {
    EnergikildeInternResponse(
        data = it.data,
        metadata = it.metadata.toRegisterMetadataInternResponse(),
    )
}

private fun Oppvarming.toOppvarmingResponse() = OppvarmingInternResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataInternResponse(),
)

private fun OppvarmingOpplysning.toOppvarmingResponse() = this.toOppvarming().map {
    OppvarmingInternResponse(
        data = it.data,
        metadata = it.metadata.toRegisterMetadataInternResponse(),
    )
}
