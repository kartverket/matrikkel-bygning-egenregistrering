package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelBygningsstatusKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.toLocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikk
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.BygningsstatusKodeId
import java.time.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

// TODO: Hvordan dokumentere hvor denne datoen kommer fra?
private val EARLIEST_DATE_FOR_DERIVING_BYGGEAAR = LocalDate.of(2009, 4, 25)

internal fun deriveByggeaarForBygning(bygning: MatrikkelBygning): Int? {
    return bygning.bygningsstatusHistorikker.item
        .filter { isAfterThresholdDate(it) }
        .filter { isCorrectBygningsstatusKode(it.bygningsstatusKodeId) }
        .filter { isNotDeleted(it) }
        .filter { isDatesNotDubious(it) }
        .minByOrNull { it.dato.toLocalDate() }
        ?.dato
        ?.toLocalDate()
        ?.year
}

private fun isAfterThresholdDate(bygningsstatus: BygningsstatusHistorikk): Boolean =
    bygningsstatus.registrertDato.toLocalDate() > EARLIEST_DATE_FOR_DERIVING_BYGGEAAR

private fun isCorrectBygningsstatusKode(bygningsstatusKodeId: BygningsstatusKodeId): Boolean =
    bygningsstatusKodeId.value == MatrikkelBygningsstatusKode.FerdigAttest().value || bygningsstatusKodeId.value == MatrikkelBygningsstatusKode.MidlertidigBrukstillatelse().value

private fun isNotDeleted(bygningsstatus: BygningsstatusHistorikk): Boolean = bygningsstatus.slettetDato == null

private fun isDatesNotDubious(bygningsstatus: BygningsstatusHistorikk): Boolean {
    val today = LocalDate.now()
    val inOneHundredYears = today.plusYears(100)
    val tooOldDate = today.withYear(1000)

    // Vet ikke hvor bra sammenlikning med matrikkelens LocalDate fungerer, så bare oversetter disse til Kotlins for å være safe
    val isVedtaksdatoAfterRegistrertDato = bygningsstatus.dato.toLocalDate() > bygningsstatus.registrertDato.toLocalDate()

    val isDatesTooFarIntoFuture =
        bygningsstatus.dato.toLocalDate() > inOneHundredYears || bygningsstatus.registrertDato.toLocalDate() > inOneHundredYears

    val isVedtaksDatoTooOld = bygningsstatus.dato.toLocalDate() < tooOldDate

    return !(isVedtaksdatoAfterRegistrertDato || isDatesTooFarIntoFuture || isVedtaksDatoTooOld)
}
