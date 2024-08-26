package no.kartverket.matrikkel.bygning.matrikkel

import no.kartverket.matrikkel.bygning.models.Bygning


interface BygningClient {
    fun getBygningById(id: Long): Bygning?
    fun getBygningByBygningNummer(bygningNummer: Long): Bygning?
}
