package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelBygningsstatusKode
import no.kartverket.matrikkel.bygning.matrikkelapi.toLocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikk
import java.time.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

private val earliestDateForDerivingByggeaar = LocalDate.of(2009, 4, 25)

// TODO Hvilken dato fra bygningsstatus er det som faktisk regnes som riktig dato å sjekke mot?
internal fun deriveByggeaarForBygning(bygning: MatrikkelBygning): Int? {
    return bygning.bygningsstatusHistorikker.item
        .filter { isAfterThresholdDate(it) }
        .filter { isCorrectBygningsstatusKode(it) }
        .filter { isNotDeleted(it) }
        .minByOrNull { it.dato.toLocalDate() }
        ?.dato
        ?.toLocalDate()
        ?.year
}

private fun isNotDeleted(historikk: BygningsstatusHistorikk): Boolean = historikk.slettetDato == null

private fun isCorrectBygningsstatusKode(historikk: BygningsstatusHistorikk): Boolean =
    historikk.bygningsstatusKodeId == MatrikkelBygningsstatusKode.FerdigAttest() || historikk.bygningsstatusKodeId == MatrikkelBygningsstatusKode.MidlertidigBrukstillatelse()

private fun isAfterThresholdDate(historikk: BygningsstatusHistorikk): Boolean =
    (historikk.dato.toLocalDate() > earliestDateForDerivingByggeaar)
