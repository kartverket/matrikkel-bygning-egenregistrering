package no.kartverket.matrikkel.bygning.matrikkelclients

import no.kartverket.matrikkel.bygning.models.Bygning


interface IBygningClient {
    fun getBygningById(id: String): Bygning
    fun getBygningByBygningNummer(bygningNummer: String): Bygning
}