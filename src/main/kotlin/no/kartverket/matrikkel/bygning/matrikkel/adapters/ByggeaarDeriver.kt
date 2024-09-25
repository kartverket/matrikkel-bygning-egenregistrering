package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkelapi.id.MatrikkelBygningsstatusKode
import no.kartverket.matrikkel.bygning.matrikkelapi.toLocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikk
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.BygningsstatusKodeId
import java.time.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

// TODO: Hvordan dokumentere hvor denne datoen kommer fra?
private val EARLIEST_DATE_FOR_DERIVING_BYGGEAAR = LocalDate.of(2009, 4, 25)

// TODO: Det vil være mulig å få byggeår hvor bygningsstatus er veldig langt tilbakedatert, og vi kan nok anta at disse er feil.
// Må gå opp hva "smerteterskelen" for hva som regnes som en ikke-godkjent bygningsstatusdato er
internal fun deriveByggeaarForBygning(bygning: MatrikkelBygning): Int? {
    return bygning.bygningsstatusHistorikker.item
        .filter { isAfterThresholdDate(it) }
        .filter { isCorrectBygningsstatusKode(it.bygningsstatusKodeId) }
        .filter { isNotDeleted(it) }
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
