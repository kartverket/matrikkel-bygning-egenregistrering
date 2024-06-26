package no.kartverket.matrikkel.bygning.models.kodelister

// Kan det finnes flere presentasjonsnavn på én kode? For eksempel per ett på bokmål og ett på nynorsk?
interface Kode {
    val kodenavn: String
    val presentasjonsnavn: String
    val beskrivelse: String
}

enum class VannforsyningsKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : Kode {
    OV("OffentligVannverk", "Offentlig vannverk", "Bygget er tilknyttet offentlig vannverk"),
    TPV(
        "TilknyttetPrivatVannverk",
        "Tilknyttet privat vannverk",
        "Bygget er tilknyttet privat vannverk. Privat vannverk er nett som forsyner mer enn 100 personer eller 20 husstander."
    ),
    API(
        "AnnenPrivatInnlagtVann",
        "Annen privat med innlagt vann",
        "Annen privat vannforsyning, bygget har innlagt vann"
    ),
    AP(
        "AnnenPrivatIkkeInnlagtVann",
        "Annen privat men ikke innlagt vann",
        "Annen privat vannforsyning, bygget har ikke innlagt vann"
    )
}

enum class AvlopsKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : Kode {
    O("OffentligKloakk", "Offentlig kloakk", "Avløp er offentlig kloakk"),
    P("PrivatKloakk", "Privat kloakk", "Avløp er privat kloakk"),
    I("IngenKloakk", "Ingen kloakk", "Ingen tilknytning til kloakk")
}

enum class EnergikildeKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : Kode {
    A(
        "AnnenEnergikilde",
        "Annen energikilde",
        "Energikildekode for annen energikilde enn de som har ferdigdefinerte koder"
    ),
    B("Biobrensel", "Biobrensel", "Energikildekode for biobrensel"),
    E("Elektrisitet", "Elektrisitet", "Energikildekode for elektrisitet"),
    F("Fjernvarme", "Fjernvarme", "Energikildekode for fjernvarme"),
    G("Gass", "Gass", "Energikildekode for gass"),
    O("OljeParafin", "Olje eller parafin", "Energikildekode for olje eller parafin"),
    S("Solenergi", "Solenergi", "Energikildekode for solenergi"),
    V("Varmepumpe", "Varmepumpe", "Energikildekode for varmepumpe"),
}

enum class OppvarmingsKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : Kode {
    E("Elektrisk", "Elektrisk", "Elektrisk oppvarming"),
    S("Sentralvarme", "Sentralvarme", "Sentralvarme"),
    A("AnnenOppvarming", "Annen oppvarming", "Annen oppvarming")
}

