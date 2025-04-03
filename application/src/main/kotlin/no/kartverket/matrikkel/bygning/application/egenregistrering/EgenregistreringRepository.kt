package no.kartverket.matrikkel.bygning.application.egenregistrering

import kotliquery.TransactionalSession
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EgenregistreringBase

interface EgenregistreringRepository {
    fun saveEgenregistrering(
        egenregistrering: Egenregistrering,
        tx: TransactionalSession,
    )

    fun saveEgenregistrering(
        egenregistrering: EgenregistreringBase,
        tx: TransactionalSession,
    )
}
