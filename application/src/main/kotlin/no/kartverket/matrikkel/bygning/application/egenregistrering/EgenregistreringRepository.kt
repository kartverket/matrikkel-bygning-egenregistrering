package no.kartverket.matrikkel.bygning.application.egenregistrering

import kotliquery.TransactionalSession
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering

interface EgenregistreringRepository {
    fun saveEgenregistrering(egenregistrering: Egenregistrering, tx: TransactionalSession)
}
