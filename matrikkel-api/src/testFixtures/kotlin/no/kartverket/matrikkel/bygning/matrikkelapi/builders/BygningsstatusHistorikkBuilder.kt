package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikk
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikkList

fun bygningsstatusHistorikkList(vararg historikker: BygningsstatusHistorikk) = BygningsstatusHistorikkList().apply {
    item.addAll(historikker)
}

fun bygningsstatusHistorikk(scope: BygningsstatusHistorikk.() -> Unit) = BygningsstatusHistorikk().apply(scope)
