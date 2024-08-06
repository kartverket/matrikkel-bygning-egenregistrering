package no.kartverket.matrikkel.bygning.matrikkel


interface BygningClient {
    fun getBygningById(id: Long): Bygning?
    fun getBygningByBygningNummer(bygningNummer: Long): Bygning?
}