package no.kartverket.matrikkel.bygning.routes.v1.ekstern.berettigetinteresse.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode

@Serializable
data class BygningBerettigetInteresseResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetBerettigetInteresseResponse>,
)

@Serializable
data class BruksenhetBerettigetInteresseResponse(
    val bruksenhetId: Long,
    val byggeaar: Int?,
    val totaltBruksareal: Double?,
    val etasjer: List<BruksenhetEtasjeBerettigetInteresseResponse>?,
    val vannforsyning: VannforsyningKode?,
    val avlop: AvlopKode?,
    val energikilder: List<EnergikildeKode>?,
    val oppvarming: List<OppvarmingKode>?,
)

@Serializable
data class BruksenhetEtasjeBerettigetInteresseResponse(
    val etasjeBetegnelse: String,
    val bruksareal: Double,
)

fun Bygning.toBygningBerettigetInteresseResponse(): BygningBerettigetInteresseResponse = BygningBerettigetInteresseResponse(
    bygningId = bygningBubbleId.value,
    bygningsnummer = bygningsnummer,
    bruksenheter = bruksenheter.map {
        it.toBruksenhetBerettigetInteresseResponse()
    },
)

fun Bruksenhet.toBruksenhetBerettigetInteresseResponse(): BruksenhetBerettigetInteresseResponse = BruksenhetBerettigetInteresseResponse(
    bruksenhetId = this.bruksenhetBubbleId.value,
    byggeaar = this.byggeaar.egenregistrert?.data,
    totaltBruksareal = this.totaltBruksareal.egenregistrert?.data,
    etasjer = this.etasjer.egenregistrert?.data?.map {
        BruksenhetEtasjeBerettigetInteresseResponse(
            etasjeBetegnelse = it.etasjebetegnelse.toString(),
            bruksareal = it.bruksareal,
        )
    },
    vannforsyning = this.vannforsyning.egenregistrert?.data,
    avlop = this.avlop.egenregistrert?.data,
    energikilder = this.energikilder.egenregistrert?.map { it.data },
    oppvarming = this.oppvarming.egenregistrert?.map { it.data },
)
