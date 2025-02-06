package no.kartverket.matrikkel.bygning.routes.v1.intern.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
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
    val energikilder: MultikildeResponse<EnergikildeResponse>?,
    val oppvarming: MultikildeResponse<OppvarmingResponse>?,
    val bruksenheter: List<BruksenhetResponse>,
)

@Serializable
data class BygningSimpleResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetSimpleResponse>
)

@Serializable
data class BruksenhetEtasjerResponse(
    val data: List<BruksenhetEtasjeResponse>,
    val metadata: RegisterMetadataResponse
)

@Serializable
data class BruksenhetEtasjeResponse(
    val etasjebetegnelse: EtasjeBetegnelseResponse,
    val bruksareal: Double,
)

@Serializable
data class EtasjeBetegnelseResponse(
    val etasjeplanKode: String,
    val etasjenummer: Int,
)

@Serializable
data class BruksenhetResponse(
    val bruksenhetId: Long,
    val etasjer: MultikildeResponse<BruksenhetEtasjerResponse>?,
    val byggeaar: MultikildeResponse<ByggeaarResponse>?,
    val totaltBruksareal: MultikildeResponse<BruksarealResponse>?,
    val vannforsyning: MultikildeResponse<VannforsyningKodeResponse>?,
    val avlop: MultikildeResponse<AvlopKodeResponse>?,
    val energikilder: MultikildeResponse<EnergikildeResponse>?,
    val oppvarminger: MultikildeResponse<OppvarmingResponse>?,
)

@Serializable
data class BruksenhetSimpleResponse(
    val bruksenhetId: Long,
    val etasjer: BruksenhetEtasjerResponse?,
    val byggeaar: ByggeaarResponse?,
    val totaltBruksareal: BruksarealResponse?,
    val vannforsyning: VannforsyningKodeResponse?,
    val avlop: AvlopKodeResponse?,
    val energikilder: EnergikildeResponse?,
    val oppvarminger: OppvarmingResponse?,
)

@Serializable
data class RegisterMetadataResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String,
    val kildemateriale: KildematerialeKode?,
    val prosess: ProsessKode?
)

@Serializable
data class ByggeaarResponse(val data: Int, val metadata: RegisterMetadataResponse)

@Serializable
data class BruksarealResponse(val data: Double, val metadata: RegisterMetadataResponse)

@Serializable
data class VannforsyningKodeResponse(val data: VannforsyningKode, val metadata: RegisterMetadataResponse)

@Serializable
data class AvlopKodeResponse(val data: AvlopKode, val metadata: RegisterMetadataResponse)

@Serializable
data class EnergikildeResponse(val data: List<EnergikildeKode>, val metadata: RegisterMetadataResponse)

@Serializable
data class OppvarmingResponse(val data: List<OppvarmingKode>, val metadata: RegisterMetadataResponse)


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
    bygningId = this.bygningBubbleId.value,
    bygningsnummer = this.bygningsnummer,
    byggeaar = this.byggeaar.toMultikildeResponse(Byggeaar::toByggeaarResponse),
    bruksareal = this.bruksareal.toMultikildeResponse(Bruksareal::toBruksarealResponse),
    bruksenheter = this.bruksenheter.map(Bruksenhet::toBruksenhetResponse),
    vannforsyning = this.vannforsyning.toMultikildeResponse(Vannforsyning::toVannforsyningResponse),
    avlop = this.avlop.toMultikildeResponse(Avlop::toAvlopKodeResponse),
    energikilder = this.energikilder.toMultikildeResponse(Energikilde::toEnergikildeResponse),
    oppvarming = this.oppvarminger.toMultikildeResponse(Oppvarming::toOppvarmingResponse),
)

fun Bygning.toBygningSimpleResponseFromEgenregistrertData(): BygningSimpleResponse = BygningSimpleResponse(
    bygningId = this.bygningBubbleId.value,
    bygningsnummer = this.bygningsnummer,
    bruksenheter = this.bruksenheter.map { it.toBruksenhetSimpleResponseFromEgenregistrertData() },
)

fun Bruksenhet.toBruksenhetResponse(): BruksenhetResponse = BruksenhetResponse(
    bruksenhetId = this.bruksenhetBubbleId.value,
    byggeaar = this.byggeaar.toMultikildeResponse(Byggeaar::toByggeaarResponse),
    etasjer = this.etasjer.toMultikildeResponse(BruksenhetEtasjer::toBruksenhetEtasjeResponse),
    totaltBruksareal = this.totaltBruksareal.toMultikildeResponse(Bruksareal::toBruksarealResponse),
    energikilder = this.energikilder.toMultikildeResponse(Energikilde::toEnergikildeResponse),
    oppvarminger = this.oppvarminger.toMultikildeResponse(Oppvarming::toOppvarmingResponse),
    vannforsyning = this.vannforsyning.toMultikildeResponse(Vannforsyning::toVannforsyningResponse),
    avlop = this.avlop.toMultikildeResponse(Avlop::toAvlopKodeResponse),
)

fun Bruksenhet.toBruksenhetSimpleResponseFromEgenregistrertData(): BruksenhetSimpleResponse = BruksenhetSimpleResponse(
    bruksenhetId = this.bruksenhetBubbleId.value,
    byggeaar = this.byggeaar.egenregistrert?.toByggeaarResponse(),
    etasjer = this.etasjer.egenregistrert?.toBruksenhetEtasjeResponse(),
    totaltBruksareal = this.totaltBruksareal.egenregistrert?.toBruksarealResponse(),
    vannforsyning = this.vannforsyning.egenregistrert?.toVannforsyningResponse(),
    avlop = this.avlop.egenregistrert?.toAvlopKodeResponse(),
    energikilder = this.energikilder.egenregistrert?.toEnergikildeResponse(),
    oppvarminger = this.oppvarminger.egenregistrert?.toOppvarmingResponse(),
)

private fun BruksenhetEtasjer.toBruksenhetEtasjeResponse(): BruksenhetEtasjerResponse = BruksenhetEtasjerResponse(
    data = this.data.map {
        BruksenhetEtasjeResponse(
            bruksareal = it.bruksareal,
            etasjebetegnelse = EtasjeBetegnelseResponse(
                etasjeplanKode = it.etasjebetegnelse.etasjeplanKode.toString(),
                etasjenummer = it.etasjebetegnelse.etasjenummer.loepenummer
            )
        )
    },
    metadata = this.metadata.toRegisterMetadataResponse()
)

private fun Byggeaar.toByggeaarResponse(): ByggeaarResponse = ByggeaarResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataResponse(),
)

private fun Bruksareal.toBruksarealResponse(): BruksarealResponse = BruksarealResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataResponse(),
)

private fun Vannforsyning.toVannforsyningResponse(): VannforsyningKodeResponse = VannforsyningKodeResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataResponse(),
)

private fun Avlop.toAvlopKodeResponse(): AvlopKodeResponse = AvlopKodeResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataResponse(),
)

private fun Energikilde.toEnergikildeResponse() = EnergikildeResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataResponse(),
)

private fun Oppvarming.toOppvarmingResponse() = OppvarmingResponse(
    data = this.data,
    metadata = this.metadata.toRegisterMetadataResponse(),
)
