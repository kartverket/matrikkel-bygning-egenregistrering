package no.kartverket.matrikkel.bygning.routes.v1.dto.response

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.Avlop
import no.kartverket.matrikkel.bygning.models.Bruksareal
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Byggeaar
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Energikilde
import no.kartverket.matrikkel.bygning.models.Oppvarming
import no.kartverket.matrikkel.bygning.models.Vannforsyning
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class BygningResponse(
    val bygningId: Long,
    val bygningNummer: Long,
    val byggeaar: ByggeaarResponse? = null,
    val bruksareal: BruksarealResponse? = null,
    val vannforsyning: VannforsyningKodeResponse? = null,
    val avlop: AvlopKodeResponse? = null,
    val bruksenheter: List<BruksenhetResponse>,
)

@Serializable
data class RegisterMetadataResponse(@Serializable(with = InstantSerializer::class) val registreringsTidspunkt: Instant)

@Serializable
data class ByggeaarResponse(val data: Int, val metadata: RegisterMetadataResponse)

@Serializable
data class BruksarealResponse(val data: Double, val metadata: RegisterMetadataResponse)

@Serializable
data class VannforsyningKodeResponse(val data: VannforsyningKode, val metadata: RegisterMetadataResponse)

@Serializable
data class AvlopKodeResponse(val data: AvlopKode, val metadata: RegisterMetadataResponse)

@Serializable
data class EnergikildeResponse(val data: EnergikildeKode, val metadata: RegisterMetadataResponse)

@Serializable
data class OppvarmingResponse(val data: OppvarmingKode, val metadata: RegisterMetadataResponse)

@Serializable
data class BruksenhetResponse(
    val bruksenhetId: Long,
    val bruksareal: BruksarealResponse? = null,
    val energikilder: List<EnergikildeResponse> = emptyList(),
    val oppvarminger: List<OppvarmingResponse> = emptyList(),
)

fun Bygning.toBygningResponse(): BygningResponse = BygningResponse(
    bygningId = this.bygningId,
    bygningNummer = this.bygningNummer,
    byggeaar = this.byggeaar?.toByggeaarResponse(),
    bruksareal = this.bruksareal?.toBruksarealResponse(),
    bruksenheter = this.bruksenheter.map { it.toBruksenhetResponse() },
    vannforsyning = this.vannforsyning?.toVannforsyningResponse(),
    avlop = this.avlop?.toAvlopKodeResponse(),
)

private fun Byggeaar.toByggeaarResponse(): ByggeaarResponse = ByggeaarResponse(
    data = this.data,
    metadata = RegisterMetadataResponse(
        registreringsTidspunkt = metadata.registreringstidspunkt
    )
)

private fun Bruksareal.toBruksarealResponse(): BruksarealResponse = BruksarealResponse(
    data = this.data,
    metadata = RegisterMetadataResponse(
        registreringsTidspunkt = metadata.registreringstidspunkt,
    ),
)

private fun Vannforsyning.toVannforsyningResponse(): VannforsyningKodeResponse = VannforsyningKodeResponse(
    data = this.data,
    metadata = RegisterMetadataResponse(
        registreringsTidspunkt = metadata.registreringstidspunkt
    )
)

private fun Avlop.toAvlopKodeResponse(): AvlopKodeResponse = AvlopKodeResponse(
    data = this.data,
    metadata = RegisterMetadataResponse(
        registreringsTidspunkt = metadata.registreringstidspunkt
    )
)

fun Bruksenhet.toBruksenhetResponse(): BruksenhetResponse = BruksenhetResponse(
    bruksenhetId = this.bruksenhetId,
    bruksareal = this.bruksareal?.toBruksarealResponse(),
    energikilder = this.energikilder.map { it.toEnergikildeResponse() },
    oppvarminger = this.oppvarminger.map { it.toOppvarmingResponse() },
)

private fun Energikilde.toEnergikildeResponse() = EnergikildeResponse(
    data = this.data,
    metadata = RegisterMetadataResponse(
        registreringsTidspunkt = metadata.registreringstidspunkt
    )
)

private fun Oppvarming.toOppvarmingResponse() = OppvarmingResponse(
    data = this.data,
    metadata = RegisterMetadataResponse(
        registreringsTidspunkt = metadata.registreringstidspunkt
    )
)
