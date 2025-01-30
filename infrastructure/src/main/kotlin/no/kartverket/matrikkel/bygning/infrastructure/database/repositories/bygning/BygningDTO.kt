package no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning

import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import java.time.Instant


internal fun Bruksenhet.toDTO() = BruksenhetDTO(
    id = id.value,
    bruksenhetBubbleId = bruksenhetBubbleId.value,
    bygningId = bygningId.value,
    // Kanskje ikke beste stedet å lage registreringstidspunktet?
    registreringstidspunkt = Instant.now(),
    bruksenhetData = this,
)
