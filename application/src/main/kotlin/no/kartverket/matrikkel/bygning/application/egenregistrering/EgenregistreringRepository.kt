package no.kartverket.matrikkel.bygning.application.egenregistrering

import no.kartverket.matrikkel.bygning.application.models.Egenregistrering

interface EgenregistreringRepository {
    fun getAllEgenregistreringerForBygning(bygningId: Long): List<Egenregistrering>
    fun saveEgenregistrering(egenregistrering: Egenregistrering)
}
