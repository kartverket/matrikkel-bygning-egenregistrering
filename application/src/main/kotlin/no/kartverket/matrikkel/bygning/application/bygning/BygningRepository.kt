package no.kartverket.matrikkel.bygning.application.bygning

import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import java.util.*

interface BygningRepository {
    fun saveBruksenhet(bruksenhet: Bruksenhet)

    fun getBygningById(bygningId: UUID): Bygning?
    fun getBruksenhetById(bruksenhetId: UUID): Bruksenhet?
}
