package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelBygningsstatusKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.toLocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikk
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.BygningsstatusKodeId
import java.time.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

private val EARLIEST_DATE_FOR_DERIVING_BYGGEAAR = LocalDate.of(2009, 4, 25)

internal fun deriveByggeaarForBygning(bygning: MatrikkelBygning): Int? {
    return bygning.bygningsstatusHistorikker.item
        .filter { isAfterThresholdDate(it) }
        .filter { isCorrectBygningsstatusKode(it.bygningsstatusKodeId) }
        .filter { isNotDeleted(it) }
        .filter { isRegistrertDatoAfterVedtaksdato(it) }
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

private fun isRegistrertDatoAfterVedtaksdato(bygningsstatus: BygningsstatusHistorikk): Boolean = bygningsstatus.dato.toLocalDate() <= bygningsstatus.registrertDato.toLocalDate()
