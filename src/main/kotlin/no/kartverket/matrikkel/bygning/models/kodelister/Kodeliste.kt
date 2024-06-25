package no.kartverket.matrikkel.bygning.models.kodelister

data class Kode(
    val initialVerdi: String,
    val kodenavn: String,
    // Kan det finnes flere presentasjonsnavn på én kode? For eksempel per ett på bokmål og ett på nynorsk?
    val presentasjonsnavn: String,
    val beskrivelse: String,
)

data class Kodeliste(
    val koder: List<Kode>,
)

val VannforsyningKodeliste = Kodeliste(
    koder = listOf(
        Kode("O", "OffentligVannverk", "Offentlig vannverk", "Bygget er tilknyttet offentlig vannverk"),
        Kode(
            "P",
            "TilknyttetPrivatVannverk",
            "Tilknyttet privat vannverk",
            "Bygget er tilknyttet privat vannverk. Privat vannverk er nett som forsyner mer enn 100 personer eller 20 husstander."
        ),
        Kode(
            "API",
            "AnnenPrivatInnlagtVann",
            "Annen privat med innlagt vann",
            "Annen privat vannforsyning, bygget har innlagt vann"
        ),
        Kode(
            "AP",
            "AnnenPrivat",
            "Annen privat men ikke innlagt vann",
            "Annen privat vannforsyning, bygget har innlagt vann"
        ),
    )
)
