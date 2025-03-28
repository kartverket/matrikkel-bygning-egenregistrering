package no.kartverket.matrikkel.bygning.routes.v1.ekstern.hendelse

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.hendelser.BygningHendelseType
import no.kartverket.matrikkel.bygning.application.hendelser.Hendelse
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class HendelseResponse(
    val sekvensnummer: Long,
    val objectId: Long,
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val type: BygningHendelseType,
)

@Serializable
data class HendelseContainerResponse(
    val hendelser: List<HendelseResponse>,
)

fun List<Hendelse>.toHendelseContainerResponse(): HendelseContainerResponse =
    HendelseContainerResponse(
        hendelser =
            this.map { hendelse ->
                HendelseResponse(
                    sekvensnummer = hendelse.sekvensnummer,
                    objectId = hendelse.payload.objectId,
                    registreringstidspunkt = hendelse.payload.registreringstidspunkt,
                    type = hendelse.payload.type,
                )
            },
    )
