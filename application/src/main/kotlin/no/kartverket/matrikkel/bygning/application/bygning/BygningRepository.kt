package no.kartverket.matrikkel.bygning.application.bygning

import kotliquery.TransactionalSession
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import java.time.Instant
import java.util.*

interface BygningRepository {
    fun saveBruksenheter(bruksenheter: List<Bruksenhet>, tx: TransactionalSession)
    fun getBruksenhetById(bruksenhetId: UUID, registreringstidspunkt: Instant): Bruksenhet?
}
