package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.matchers

import io.mockk.MockKMatcherScope
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleIdList
import kotlin.reflect.KClass

inline fun <reified I : MatrikkelBubbleId> MockKMatcherScope.matchId(id: I) = match<I> {
    it.value == id.value
}

fun MockKMatcherScope.matchIds(vararg ids: MatrikkelBubbleId): MatrikkelBubbleIdList {
    val idMap = HashMap<KClass<*>, HashSet<Long>>()
    ids.forEach { id -> idMap.getOrPut(id::class, ::HashSet).add(id.value) }

    return match<MatrikkelBubbleIdList> { idList ->
        idList.item.all { idMap[it::class]?.contains(it.value) ?: false }
    }
}
