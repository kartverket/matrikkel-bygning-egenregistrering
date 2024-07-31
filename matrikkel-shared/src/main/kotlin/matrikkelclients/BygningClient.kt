package matrikkelclients

import models.Bygning


interface BygningClient {
    fun getBygningById(id: String): Bygning?
    fun getBygningByBygningNummer(bygningNummer: String): Bygning?
}