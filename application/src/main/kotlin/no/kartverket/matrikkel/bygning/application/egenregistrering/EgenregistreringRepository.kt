package no.kartverket.matrikkel.bygning.application.egenregistrering

import kotliquery.TransactionalSession
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering2

interface EgenregistreringRepository {
    fun saveEgenregistrering(egenregistrering: Egenregistrering, tx: TransactionalSession)
    fun saveEgenregistrering(egenregistrering: Egenregistrering2, tx: TransactionalSession)
}
