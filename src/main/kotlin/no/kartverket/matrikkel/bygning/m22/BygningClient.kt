package no.kartverket.matrikkel.bygning.m22


interface BygningClient {
    fun getBygningById(id: Long): Bygning?
    fun getBygningByBygningNummer(bygningNummer: Long): Bygning?
}
