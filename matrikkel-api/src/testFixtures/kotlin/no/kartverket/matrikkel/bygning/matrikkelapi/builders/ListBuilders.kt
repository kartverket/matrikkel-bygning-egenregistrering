package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleObject
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleObjectList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EnergikildeKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EnergikildeKodeIdList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.OppvarmingsKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.OppvarmingsKodeIdList

fun matrikkelBubbleObjectList(vararg bubbles: MatrikkelBubbleObject) = MatrikkelBubbleObjectList().apply {
    item.addAll(bubbles)
}

fun energikildeKodeIdList(vararg ids: EnergikildeKodeId) = EnergikildeKodeIdList().apply {
    item.addAll(ids)
}

fun oppvarmingsKodeIdList(vararg ids: OppvarmingsKodeId) = OppvarmingsKodeIdList().apply {
    item.addAll(ids)
}
