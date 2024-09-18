package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.AvlopsKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EnergikildeKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.OppvarmingsKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.VannforsyningsKodeId

fun mapEnergikilde(kodeId: EnergikildeKodeId): EnergikildeKode = when (kodeId.value) {
    0L -> EnergikildeKode.Elektrisitet
    1L -> EnergikildeKode.OljeParafin
    2L -> EnergikildeKode.Biobrensel
    3L -> EnergikildeKode.Solenergi
    4L -> EnergikildeKode.Varmepumpe
    5L -> EnergikildeKode.Gass
    6L -> EnergikildeKode.Fjernvarme
    7L -> EnergikildeKode.AnnenEnergikilde
    else -> throw RuntimeException("Ukjent energikildekode: ${kodeId.value}")
}

fun mapOppvarming(kodeId: OppvarmingsKodeId): OppvarmingKode = when (kodeId.value) {
    0L -> OppvarmingKode.Elektrisk
    1L -> OppvarmingKode.Sentralvarme
    2L -> OppvarmingKode.AnnenOppvarming
    else -> throw RuntimeException("Ukjent oppvarmingskode: ${kodeId.value}")
}

fun mapAvloep(kodeId: AvlopsKodeId): AvlopKode? = when (kodeId.value) {
    0L -> null
    1L -> AvlopKode.OffentligKloakk
    2L -> AvlopKode.PrivatKloakk
    3L -> AvlopKode.IngenKloakk
    else -> throw RuntimeException("Ukjent avløpsskode: ${kodeId.value}")
}

fun mapVannforsyning(kodeId: VannforsyningsKodeId): VannforsyningKode? = when (kodeId.value) {
    0L -> null
    1L -> VannforsyningKode.OffentligVannverk
    2L -> VannforsyningKode.TilknyttetPrivatVannverk
    3L -> VannforsyningKode.AnnenPrivatInnlagtVann
    4L -> VannforsyningKode.AnnenPrivatIkkeInnlagtVann
    else -> throw RuntimeException("Ukjent vannforsyningskode: ${kodeId.value}")
}
