package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BruksenhetId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BruksenhetIdList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning

fun bygning(scope: Bygning.() -> Unit): Bygning = Bygning().apply(scope).apply {
    if (bruksenhetIds == null) bruksenhetIds = BruksenhetIdList()
}

fun Bygning.bruksenhetIds(vararg bruksenhetIds: BruksenhetId) {
    this.bruksenhetIds = BruksenhetIdList().apply {
        item.addAll(bruksenhetIds)
    }
}
