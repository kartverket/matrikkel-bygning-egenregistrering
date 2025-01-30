package no.kartverket.matrikkel.bygning.application.models.ids

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.serializers.UUIDSerializer
import java.util.*

@Serializable
@JvmInline
value class BygningId(
    @Serializable(with = UUIDSerializer::class)
    val value: UUID
) {
    companion object {
        operator fun invoke(uuidString: String): BygningId =
            BygningId(UUID.fromString(uuidString))

        operator fun invoke(uuid: UUID): BygningId =
            BygningId(uuid)
    }
}
