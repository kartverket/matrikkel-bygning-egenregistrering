package no.kartverket.matrikkel.bygning.routes.v1.ekstern.virksomhetbegrenset.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode

@Serializable
data class BygningVirksomhetBegrensetResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetVirksomhetBegrensetResponse>,
)

@Serializable
data class BruksenhetVirksomhetBegrensetResponse(
    val bruksenhetId: Long,
    val byggeaar: Int?,
    val totaltBruksareal: Double?,
    val etasjer: List<BruksenhetEtasjeVirksomhetBegrensetResponse>?,
    val vannforsyning: VannforsyningKode?,
    val avlop: AvlopKode?,
    val energikilder: List<EnergikildeKode>?,
    val oppvarming: List<OppvarmingKode>?,
)

@Serializable
data class BruksenhetEtasjeVirksomhetBegrensetResponse(
    val etasjeBetegnelse: EtasjeBetegnelseVirksomhetBegrensetResponse,
    val bruksareal: Double,
) {
    @Serializable
    data class EtasjeBetegnelseVirksomhetBegrensetResponse(
        val etasjeplanKode: String,
        val etasjenummer: Int,
    )
}

fun Bygning.toBygningVirksomhetBegrensetResponse(): BygningVirksomhetBegrensetResponse =
    BygningVirksomhetBegrensetResponse(
        bygningId = bygningBubbleId.value,
        bygningsnummer = bygningsnummer,
        bruksenheter =
            bruksenheter.map {
                it.toBruksenhetVirksomhetBegrensetResponse()
            },
    )

fun Bruksenhet.toBruksenhetVirksomhetBegrensetResponse(): BruksenhetVirksomhetBegrensetResponse =
    BruksenhetVirksomhetBegrensetResponse(
        bruksenhetId = this.bruksenhetBubbleId.value,
        byggeaar = this.byggeaar.egenregistrert?.data,
        totaltBruksareal = this.totaltBruksareal.egenregistrert?.data,
        etasjer =
            this.etasjer.egenregistrert?.data?.map {
                BruksenhetEtasjeVirksomhetBegrensetResponse(
                    etasjeBetegnelse =
                        BruksenhetEtasjeVirksomhetBegrensetResponse.EtasjeBetegnelseVirksomhetBegrensetResponse(
                            etasjeplanKode = it.etasjebetegnelse.etasjeplanKode.toString(),
                            etasjenummer = it.etasjebetegnelse.etasjenummer.loepenummer,
                        ),
                    bruksareal = it.bruksareal,
                )
            },
        vannforsyning = this.vannforsyning.egenregistrert?.data,
        avlop = this.avlop.egenregistrert?.data,
        energikilder = this.energikilder.egenregistrert?.map { it.data },
        oppvarming = this.oppvarming.egenregistrert?.map { it.data },
    )
