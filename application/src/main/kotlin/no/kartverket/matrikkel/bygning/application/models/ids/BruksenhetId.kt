package no.kartverket.matrikkel.bygning.application.models.ids

import java.util.*

@JvmInline
value class BruksenhetId(
    val value: UUID
) {
    companion object {
        operator fun invoke(uuidString: String): BruksenhetId =
            BruksenhetId(UUID.fromString(uuidString))

        operator fun invoke(uuid: UUID): BruksenhetId =
            BruksenhetId(uuid)
    }
}
