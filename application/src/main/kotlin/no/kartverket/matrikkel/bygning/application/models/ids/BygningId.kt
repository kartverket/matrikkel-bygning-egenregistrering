package no.kartverket.matrikkel.bygning.application.models.ids

import java.util.*

@JvmInline
value class BygningId(
    val value: UUID
) {
    companion object {
        operator fun invoke(uuidString: String): BygningId =
            BygningId(UUID.fromString(uuidString))

        operator fun invoke(uuid: UUID): BygningId =
            BygningId(uuid)
    }
}
