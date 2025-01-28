package no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning

import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import java.time.Instant


internal fun Bruksenhet.toDTO() = BruksenhetDTO(
    id = id,
    bruksenhetId = bruksenhetId,
    bygningId = bygningId,
    // Kanskje ikke beste stedet å lage registreringstidspunktet?
    registreringstidspunkt = Instant.now(),
    bruksenhetData = this,
)
