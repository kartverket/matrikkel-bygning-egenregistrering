package no.kartverket.matrikkel.bygning.routes.v1.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.Avlop
import no.kartverket.matrikkel.bygning.models.Bruksareal
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Byggeaar
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Energikilde
import no.kartverket.matrikkel.bygning.models.Multikilde
import no.kartverket.matrikkel.bygning.models.Oppvarming
import no.kartverket.matrikkel.bygning.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.models.Vannforsyning
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class MultikildeResponse<T : Any>(
    val autoritativ: T? = null,
    val egenregistrert: T? = null
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
data class BruksenhetResponse(
    val bruksenhetId: Long,
    val bruksareal: MultikildeResponse<BruksarealResponse>?,
    val energikilder: MultikildeResponse<List<EnergikildeResponse>>?,
    val oppvarminger: MultikildeResponse<List<OppvarmingResponse>>?,
)

// TODO Ikke noe ordentlig, bare for å teste
enum class Rolle {
    Eier,
    Foerer,
}

@Serializable
data class RegisterMetadataResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String? = null,
    val rolle: Rolle,
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


/**
 * Kan være det er en bedre idé å lage to forskjellige typer metadataresponse, men akkurat nå så fungerer dette som en løsning
 *
 * Føler dette kanskje er nok et symptom på at det tidvis kjennes litt snålt ut å skulle representere egenregistrert og autoritativ
 * data på bygninger på så likt format som vi gjør, og at det kanskje er hensiktsmessig å skille ut
 *
 */
fun RegisterMetadata.toRegisterMetadataResponse() = RegisterMetadataResponse(
    registreringstidspunkt = this.registreringstidspunkt,
    registrertAv = when (val metadata = this) {
        is RegisterMetadata.Autoritativ -> metadata.registrertAv
        is RegisterMetadata.Egenregistrert -> metadata.eier?.getValue()
    },
    // TODO Her skal man nok ikke automatisk anta at det er Eier som har ført, men heller sjekke registreringen om eier = registrertAv,
    // men vet ikke om vi skal legge opp for muligheten for en "på vegne av" registrering helt ennå
    rolle = when (this) {
        is RegisterMetadata.Autoritativ -> Rolle.Foerer
        is RegisterMetadata.Egenregistrert -> Rolle.Eier
    },
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

fun Bruksenhet.toBruksenhetResponse(): BruksenhetResponse = BruksenhetResponse(
    bruksenhetId = this.bruksenhetId,
    bruksareal = this.bruksareal.toMultikildeResponse(Bruksareal::toBruksarealResponse),
    energikilder = this.energikilder.toMultikildeResponse { map(Energikilde::toEnergikildeResponse) },
    oppvarminger = this.oppvarminger.toMultikildeResponse { map(Oppvarming::toOppvarmingResponse) },
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
