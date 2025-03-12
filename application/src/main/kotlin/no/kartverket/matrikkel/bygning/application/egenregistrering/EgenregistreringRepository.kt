package no.kartverket.matrikkel.bygning.application.egenregistrering

import kotliquery.TransactionalSession
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId

interface EgenregistreringRepository {
    fun saveEgenregistrering(egenregistrering: Egenregistrering, tx: TransactionalSession)
    fun deleteEgenregistrering(id: EgenregistreringId, tx: TransactionalSession): Egenregistrering?
    fun getGjeldendeEgenregistreringerForBruksenhet(bruksenhetBubbleId: BruksenhetBubbleId, tx: TransactionalSession): List<Egenregistrering>
}
