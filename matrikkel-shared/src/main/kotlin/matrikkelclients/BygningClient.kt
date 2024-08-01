package matrikkelclients

import models.Bygning


interface BygningClient {
    fun getBygningById(id: Long): Bygning?
    fun getBygningByBygningNummer(bygningNummer: Long): Bygning?
}