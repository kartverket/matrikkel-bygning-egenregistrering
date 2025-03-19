package no.kartverket.matrikkel.bygning.application.models.ids

import java.util.*

@JvmInline
value class EgenregistreringId(
    val value: UUID
) {
    companion object {
        operator fun invoke(uuidString: String): EgenregistreringId =
            EgenregistreringId(UUID.fromString(uuidString))

        operator fun invoke(uuid: UUID): EgenregistreringId =
            EgenregistreringId(uuid)
    }
}
