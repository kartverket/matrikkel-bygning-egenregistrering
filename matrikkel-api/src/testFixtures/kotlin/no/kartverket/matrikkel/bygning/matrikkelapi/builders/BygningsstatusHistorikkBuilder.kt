package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikk
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikkList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.BygningsstatusKodeId

fun bygningsstatusHistorikkList(vararg historikker: BygningsstatusHistorikk) = BygningsstatusHistorikkList().apply {
    item.addAll(historikker)
}

fun bygningsstatusHistorikk(scope: BygningsstatusHistorikk.() -> Unit) = BygningsstatusHistorikk().apply(scope)

fun BygningsstatusHistorikk.copy(): BygningsstatusHistorikk {
    return bygningsstatusHistorikk {
        bygningsstatusKodeId = this@copy.bygningsstatusKodeId
        registrertDato = this@copy.registrertDato
        dato = this@copy.dato
    }
}

fun BygningsstatusHistorikk.withRegistrertDato(year: Int, month: Int, dayOfMonth: Int): BygningsstatusHistorikk {
    return apply {
        registrertDato = timestampUtc(year, month, dayOfMonth)
    }
}

fun BygningsstatusHistorikk.withDato(year: Int, month: Int, dayOfMonth: Int): BygningsstatusHistorikk {
    return apply {
        dato = localDateUtc(year, month, dayOfMonth)
    }
}

fun BygningsstatusHistorikk.withBygningsstatusKodeId(kodeId: BygningsstatusKodeId): BygningsstatusHistorikk {
    return apply {
        bygningsstatusKodeId = kodeId
    }
}
