package no.kartverket.matrikkel.bygning.application.hendelser

import java.time.Instant

enum class BygningHendelseType {
    BRUKSENHET_OPPDATERT,
}

data class Hendelse(
    val sekvensnummer: Long,
    val payload: HendelsePayload
)

interface HendelsePayload {
    val objectId: Long
    val registreringstidspunkt: Instant
    val type: BygningHendelseType

    data class BruksenhetOppdatertPayload(
        override val objectId: Long,
        override val registreringstidspunkt: Instant,
    ) : HendelsePayload {
        override val type: BygningHendelseType = BygningHendelseType.BRUKSENHET_OPPDATERT
    }
}
