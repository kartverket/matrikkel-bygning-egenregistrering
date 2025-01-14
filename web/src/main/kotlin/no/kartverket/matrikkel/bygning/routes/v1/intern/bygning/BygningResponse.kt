package no.kartverket.matrikkel.bygning.routes.v1.intern.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.*
import no.kartverket.matrikkel.bygning.application.models.Felt.*
import no.kartverket.matrikkel.bygning.application.models.kodelister.*
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class MultikildeResponse<T : Any>(
    val autoritativ: T? = null, val egenregistrert: T? = null
)

@Serializable
data class BygningResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val byggeaar: MultikildeResponse<ByggeaarResponse>?,
    val bruksareal: MultikildeResponse<BruksarealResponse>?,
    val vannforsyning: MultikildeResponse<VannforsyningKodeResponse>?,
    val avlop: MultikildeResponse<AvlopKodeResponse>?,
    val energikilder: MultikildeResponse<List<EnergikildeResponse>>?,
    val oppvarming: MultikildeResponse<List<OppvarmingResponse>>?,
    val bruksenheter: List<BruksenhetResponse>,
)

@Serializable
data class BygningSimpleResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetSimpleResponse>
)

@Serializable
    data class BruksenhetEtasjeResponse(
    val etasjebetegnelse: EtasjeBetegnelseResponse,
    val bruksareal: BruksarealResponse?,
)

@Serializable
data class EtasjeBetegnelseResponse(
    val etasjeplanKode: String,
    val etasjenummer: Int,
)

@Serializable
data class BruksenhetResponse(
    val bruksenhetId: Long,
    val etasjer: MultikildeResponse<List<BruksenhetEtasjeResponse>>?,
    val byggeaar: MultikildeResponse<ByggeaarResponse>?,
    val totaltBruksareal: MultikildeResponse<BruksarealResponse>?,
    val vannforsyning: MultikildeResponse<VannforsyningKodeResponse>?,
    val avlop: MultikildeResponse<AvlopKodeResponse>?,
    val energikilder: MultikildeResponse<List<EnergikildeResponse>>?,
    val oppvarminger: MultikildeResponse<List<OppvarmingResponse>>?,
)

@Serializable
data class BruksenhetSimpleResponse(
    val bruksenhetId: Long,
    val etasjer: List<BruksenhetEtasjeResponse>?,
    val byggeaar: ByggeaarResponse?,
    val totaltBruksareal: BruksarealResponse?,
    val vannforsyning: VannforsyningKodeResponse?,
    val avlop: AvlopKodeResponse?,
    val energikilder: List<EnergikildeResponse>?,
    val oppvarminger: List<OppvarmingResponse>?,
)

@Serializable
data class RegisterMetadataResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?
)

@Serializable
data class ByggeaarResponse(val data: Int?, val metadata: RegisterMetadataResponse)

@Serializable
data class BruksarealResponse(val data: Double?, val metadata: RegisterMetadataResponse)

@Serializable
data class VannforsyningKodeResponse(val data: VannforsyningKode?, val metadata: RegisterMetadataResponse)

@Serializable
data class AvlopKodeResponse(val data: AvlopKode?, val metadata: RegisterMetadataResponse)

@Serializable
data class EnergikildeResponse(val data: EnergikildeKode?, val metadata: RegisterMetadataResponse)

@Serializable
data class OppvarmingResponse(val data: OppvarmingKode?, val metadata: RegisterMetadataResponse)


fun RegisterMetadata.toRegisterMetadataResponse() = RegisterMetadataResponse(
    registreringstidspunkt = this.registreringstidspunkt,
    registrertAv = this.registrertAv.value,
    kildemateriale = this.kildemateriale,
    prosess = this.prosess
)


fun <T : Any, R : Any> Multikilde<T>.toMultikildeResponse(mapper: T.() -> R): MultikildeResponse<R>? {
    return MultikildeResponse(
        autoritativ?.mapper(),
        egenregistrert?.mapper(),
    ).takeUnless { autoritativ == null && egenregistrert == null }
}

fun Bygning.toBygningResponse(): BygningResponse = BygningResponse(
    bygningId = this.bygningId,
    bygningsnummer = this.bygningsnummer,
    byggeaar = this.byggeaar.toMultikildeResponse(Byggeaar::toByggeaarResponse),
    bruksareal = this.bruksareal.toMultikildeResponse(Bruksareal::toBruksarealResponse),
    bruksenheter = this.bruksenheter.map(Bruksenhet::toBruksenhetResponse),
    vannforsyning = this.vannforsyning.toMultikildeResponse(Vannforsyning::toVannforsyningResponse),
    avlop = this.avlop.toMultikildeResponse(Avlop::toAvlopKodeResponse),
    energikilder = this.energikilder.toMultikildeResponse { map(Energikilde::toEnergikildeResponse) },
    oppvarming = this.oppvarminger.toMultikildeResponse { map(Oppvarming::toOppvarmingResponse) },
)

fun Bygning.toBygningSimpleResponseFromEgenregistrertData(): BygningSimpleResponse = BygningSimpleResponse(
    bygningId = this.bygningId,
    bygningsnummer = this.bygningsnummer,
    bruksenheter = this.bruksenheter.map { it.toBruksenhetSimpleResponseFromEgenregistrertData() },
)

fun Bruksenhet.toBruksenhetResponse(): BruksenhetResponse = BruksenhetResponse(
    bruksenhetId = this.bruksenhetId,
    byggeaar = this.byggeaar.toMultikildeResponse(Byggeaar::toByggeaarResponse),
    etasjer = this.etasjer.toMultikildeResponse { map(BruksenhetEtasje::toBruksenhetEtasjeResponse) },
    totaltBruksareal = this.totaltBruksareal.toMultikildeResponse(Bruksareal::toBruksarealResponse),
    energikilder = this.energikilder.toMultikildeResponse { map(Energikilde::toEnergikildeResponse) },
    oppvarminger = this.oppvarminger.toMultikildeResponse { map(Oppvarming::toOppvarmingResponse) },
    vannforsyning = this.vannforsyning.toMultikildeResponse(Vannforsyning::toVannforsyningResponse),
    avlop = this.avlop.toMultikildeResponse(Avlop::toAvlopKodeResponse),
)

fun Bruksenhet.toBruksenhetSimpleResponseFromEgenregistrertData(): BruksenhetSimpleResponse = BruksenhetSimpleResponse(
    bruksenhetId = this.bruksenhetId,
    byggeaar = this.byggeaar.egenregistrert?.toByggeaarResponse(),
    etasjer = this.etasjer.egenregistrert?.map { it.toBruksenhetEtasjeResponse() },
    totaltBruksareal = this.totaltBruksareal.egenregistrert?.toBruksarealResponse(),
    vannforsyning = this.vannforsyning.egenregistrert?.toVannforsyningResponse(),
    avlop = this.avlop.egenregistrert?.toAvlopKodeResponse(),
    energikilder = this.energikilder.egenregistrert?.map { it.toEnergikildeResponse() },
    oppvarminger = this.oppvarminger.egenregistrert?.map { it.toOppvarmingResponse() },
)

private fun BruksenhetEtasje.toBruksenhetEtasjeResponse(): BruksenhetEtasjeResponse = BruksenhetEtasjeResponse(
    etasjebetegnelse = EtasjeBetegnelseResponse(
        etasjeplanKode = etasjebetegnelse.etasjeplanKode.toString(),
        etasjenummer = etasjebetegnelse.etasjenummer.loepenummer,
    ),
    bruksareal = this.bruksareal?.toBruksarealResponse(),
)

private fun Byggeaar.toByggeaarResponse(): ByggeaarResponse = ByggeaarResponse(
    data = this.data,
    metadata = metadata.toRegisterMetadataResponse(),
)

private fun Bruksareal.toBruksarealResponse(): BruksarealResponse = BruksarealResponse(
    data = this.data,
    metadata = metadata.toRegisterMetadataResponse(),
)

private fun Vannforsyning.toVannforsyningResponse(): VannforsyningKodeResponse = VannforsyningKodeResponse(
    data = this.data,
    metadata = metadata.toRegisterMetadataResponse(),
)

private fun Avlop.toAvlopKodeResponse(): AvlopKodeResponse = AvlopKodeResponse(
    data = this.data,
    metadata = metadata.toRegisterMetadataResponse(),
)

private fun Energikilde.toEnergikildeResponse() = EnergikildeResponse(
    data = this.data,
    metadata = metadata.toRegisterMetadataResponse(),
)

private fun Oppvarming.toOppvarmingResponse() = OppvarmingResponse(
    data = this.data,
    metadata = metadata.toRegisterMetadataResponse(),
)
