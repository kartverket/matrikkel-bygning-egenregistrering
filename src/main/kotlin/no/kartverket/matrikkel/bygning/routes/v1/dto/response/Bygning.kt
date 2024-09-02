package no.kartverket.matrikkel.bygning.routes.v1.dto.response

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode

@Serializable
data class BygningResponse(
    val bygningId: Long,
    val bygningNummer: Long,
    val byggeaar: Int? = null,
    val bruksareal: Double? = null,
    val vannforsyning: VannforsyningKode? = null,
    val avlop: AvlopKode? = null,
    val bruksenheter: List<BruksenhetResponse>,
)

fun Bygning.toBygningResponse(): BygningResponse = BygningResponse(
    bygningId = this.bygningId,
    bygningNummer = this.bygningNummer,
    bruksareal = this.bruksareal,
    byggeaar = this.byggeaar,
    bruksenheter = this.bruksenheter.map { it.toBruksenhetResponse() },
    vannforsyning = this.vannforsyning,
    avlop = this.avlop,
)

@Serializable
data class BruksenhetResponse(
    val bruksenhetId: Long,
    val bruksareal: Double? = null,
    val energikilder: List<EnergikildeKode> = emptyList(),
    val oppvarminger: List<OppvarmingKode> = emptyList(),
)

fun Bruksenhet.toBruksenhetResponse(): BruksenhetResponse = BruksenhetResponse(
    bruksenhetId = this.bruksenhetId,
    bruksareal = this.bruksareal,
    energikilder = this.energikilder,
    oppvarminger = this.oppvarminger,
)
