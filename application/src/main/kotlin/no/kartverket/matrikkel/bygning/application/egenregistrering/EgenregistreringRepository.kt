package no.kartverket.matrikkel.bygning.application.egenregistrering

import no.kartverket.matrikkel.bygning.application.models.Egenregistrering

interface EgenregistreringRepository {
    fun saveEgenregistrering(egenregistrering: Egenregistrering)
}
