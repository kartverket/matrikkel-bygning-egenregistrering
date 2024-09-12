package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleObject
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleObjectList

fun matrikkelBubbleObjectList(vararg bubbles: MatrikkelBubbleObject) = MatrikkelBubbleObjectList().apply {
    item.addAll(bubbles)
}
