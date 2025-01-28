package no.kartverket.matrikkel.bygning.application.bygning

import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import java.util.*

interface BygningRepository {
    fun saveBruksenhet(bruksenhet: Bruksenhet)

    fun getBruksenhetById(bruksenhetId: UUID): Bruksenhet?
}
