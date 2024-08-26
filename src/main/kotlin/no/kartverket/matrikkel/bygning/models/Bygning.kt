package no.kartverket.matrikkel.bygning.models

import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode

// TODO Sette opp DTOer for Bygning/Bruksenhet hentet fra Matrikkel
data class Bygning(
    val bygningId: Long,
    val bygningNummer: Long,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: Int? = null,
    val bruksareal: Double? = null,
    val vannforsyning: VannforsyningKode? = null,
    val avlop: AvlopKode? = null,
) {
    fun withEgenregistrertData(bygningRegistrering: BygningRegistrering): Bygning {
        return this.copy(
            byggeaar = bygningRegistrering.byggeaarRegistrering?.byggeaar,
            bruksareal = bygningRegistrering.bruksarealRegistrering?.bruksareal,
            vannforsyning = bygningRegistrering.vannforsyningRegistrering?.vannforsyning,
            avlop = bygningRegistrering.avlopRegistrering?.avlop,
        )
    }

    fun withBruksenheter(bruksenheter: List<Bruksenhet>): Bygning {
        return this.copy(
            bruksenheter = bruksenheter
        )
    }
}

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
    val bruksareal: Double? = null,
    val energikilder: List<EnergikildeKode> = emptyList(),
    val oppvarminger: List<OppvarmingKode> = emptyList(),
) {
    fun withEgenregistrertData(bruksenhetRegistrering: BruksenhetRegistrering): Bruksenhet {
        return this.copy(
            bruksareal = bruksenhetRegistrering.bruksarealRegistrering?.bruksareal,
            energikilder = bruksenhetRegistrering.energikildeRegistrering?.energikilder ?: emptyList(),
            oppvarminger = bruksenhetRegistrering.oppvarmingRegistrering?.oppvarminger ?: emptyList(),
        )
    }
}
