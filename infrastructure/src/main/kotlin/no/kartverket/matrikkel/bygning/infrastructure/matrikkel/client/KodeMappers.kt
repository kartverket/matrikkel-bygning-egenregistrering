package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.AvlopsKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EnergikildeKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EtasjeplanKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.OppvarmingsKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.VannforsyningsKodeId

fun mapEnergikilde(kodeId: EnergikildeKodeId): EnergikildeKode =
    when (kodeId.value) {
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

fun mapOppvarming(kodeId: OppvarmingsKodeId): OppvarmingKode =
    when (kodeId.value) {
        0L -> OppvarmingKode.Elektrisk
        1L -> OppvarmingKode.Sentralvarme
        2L -> OppvarmingKode.AnnenOppvarming
        else -> throw RuntimeException("Ukjent oppvarmingskode: ${kodeId.value}")
    }

fun mapAvloep(kodeId: AvlopsKodeId): AvlopKode? =
    when (kodeId.value) {
        0L -> null
        1L -> AvlopKode.OffentligKloakk
        2L -> AvlopKode.PrivatKloakk
        3L -> AvlopKode.IngenKloakk
        else -> throw RuntimeException("Ukjent avløpskode: ${kodeId.value}")
    }

fun mapVannforsyning(kodeId: VannforsyningsKodeId): VannforsyningKode? =
    when (kodeId.value) {
        0L -> null
        1L -> VannforsyningKode.OffentligVannverk
        2L -> VannforsyningKode.TilknyttetPrivatVannverk
        3L -> VannforsyningKode.AnnenPrivatInnlagtVann
        4L -> VannforsyningKode.AnnenPrivatIkkeInnlagtVann
        else -> throw RuntimeException("Ukjent vannforsyningskode: ${kodeId.value}")
    }

fun mapEtasjeplanKode(kodeId: EtasjeplanKodeId): EtasjeplanKode =
    when (kodeId.value) {
        0L -> EtasjeplanKode.IkkeOppgitt
        1L -> EtasjeplanKode.Hovedetasje
        2L -> EtasjeplanKode.Kjelleretasje
        3L -> EtasjeplanKode.Loftetasje
        4L -> EtasjeplanKode.Underetasje
        else -> throw RuntimeException("Ukjent etasjeplankode: ${kodeId.value}")
    }
