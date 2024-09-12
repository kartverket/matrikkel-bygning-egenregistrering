package no.kartverket.matrikkel.bygning.matrikkelapi

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleIdList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelContext
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bruksenhet
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BruksenhetId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.StoreService

fun StoreService.getBygning(id: BygningId, matrikkelContext: MatrikkelContext): Bygning {
    return getObject(id, matrikkelContext) as Bygning
}

fun StoreService.getBruksenheter(ids: Iterable<BruksenhetId>, matrikkelContext: MatrikkelContext): List<Bruksenhet> {
    val objects = getObjects(MatrikkelBubbleIdList().apply { item.addAll(ids) }, matrikkelContext)
    return objects.item.map { it as Bruksenhet }
}
