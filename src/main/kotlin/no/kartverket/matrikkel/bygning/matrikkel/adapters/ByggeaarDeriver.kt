package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelBygningsstatusKode
import no.kartverket.matrikkel.bygning.matrikkelapi.toLocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.Timestamp
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.BygningsstatusKodeId
import java.time.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

private val earliestDateForDerivingByggeaar = LocalDate.of(2009, 4, 25)

// TODO: Det vil være mulig å få byggeår hvor bygningsstatus er veldig langt tilbakedatert, og vi kan nok anta at disse er feil.
// Må gå opp hva "smerteterskelen" for hva som regnes som en ikke-godkjent bygningsstatusdato er
internal fun deriveByggeaarForBygning(bygning: MatrikkelBygning): Int? {
    return bygning.bygningsstatusHistorikker.item
        .filter { isAfterThresholdDate(it.registrertDato) }
        .filter { isCorrectBygningsstatusKode(it.bygningsstatusKodeId) }
        .filter { isNotDeleted(it.slettetDato) }
        .minByOrNull { it.dato.toLocalDate() }
        ?.dato
        ?.toLocalDate()
        ?.year
}

private fun isNotDeleted(slettetDato: Timestamp?): Boolean = slettetDato == null

private fun isCorrectBygningsstatusKode(bygningsstatusKodeId: BygningsstatusKodeId): Boolean =
    bygningsstatusKodeId == MatrikkelBygningsstatusKode.FerdigAttest() || bygningsstatusKodeId == MatrikkelBygningsstatusKode.MidlertidigBrukstillatelse()

private fun isAfterThresholdDate(registrertDato: Timestamp): Boolean =
    (registrertDato.toLocalDate() > earliestDateForDerivingByggeaar)
