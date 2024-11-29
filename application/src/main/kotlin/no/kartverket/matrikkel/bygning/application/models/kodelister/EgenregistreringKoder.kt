package no.kartverket.matrikkel.bygning.application.models.kodelister

enum class VannforsyningKode(
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    OffentligVannverk("Offentlig vannverk", "Bygget er tilknyttet offentlig vannverk"),
    TilknyttetPrivatVannverk(
        "Tilknyttet privat vannverk",
        "Bygget er tilknyttet privat vannverk. Privat vannverk er nett som forsyner mer enn 100 personer eller 20 husstander.",
    ),
    AnnenPrivatInnlagtVann(
        "Annen privat med innlagt vann",
        "Annen privat vannforsyning, bygget har innlagt vann",
    ),
    AnnenPrivatIkkeInnlagtVann(
        "Annen privat men ikke innlagt vann",
        "Annen privat vannforsyning, bygget har ikke innlagt vann",
    )
}

enum class AvlopKode(
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    OffentligKloakk("Offentlig kloakk", "Avløp er offentlig kloakk"),
    PrivatKloakk("Privat kloakk", "Avløp er privat kloakk"),
    IngenKloakk("Ingen kloakk", "Ingen tilknytning til kloakk")
}

enum class EnergikildeKode(
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    AnnenEnergikilde(
        "Annen energikilde",
        "Energikildekode for annen energikilde enn de som har ferdigdefinerte koder",
    ),
    Biobrensel("Biobrensel", "Energikildekode for biobrensel"),
    Elektrisitet("Elektrisitet", "Energikildekode for elektrisitet"),
    Fjernvarme("Fjernvarme", "Energikildekode for fjernvarme"),
    Gass("Gass", "Energikildekode for gass"),
    OljeParafin("Olje eller parafin", "Energikildekode for olje eller parafin"),
    Solenergi("Solenergi", "Energikildekode for solenergi"),
    Varmepumpe("Varmepumpe", "Energikildekode for varmepumpe"),
}

enum class OppvarmingKode(
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    Elektrisk("Elektrisk", "Elektrisk oppvarming"),
    Sentralvarme("Sentralvarme", "Sentralvarme"),
    AnnenOppvarming("Annen oppvarming", "Annen oppvarming")
}

enum class KildematerialeKode(
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    Selvrapportert("Selvrapportert", "Når kilden ikke er dokumenterbar. Du velger dette når du selv gjør en vurdering  av hvilken løsning som er riktig. Dette alternative velges når kilden er feks den som opprinnelig oppførte bygningen, står i hyttebok"),
    Salgsoppgave("Salgsoppgave", "Ved kjøp og salg av eiendom utarbeides flere rapporter og de har hatt ulikt benevnelse. Denne kilden velges hvis du har en salgsrapport, tilstandsrapport, boligsalgsrapport, takstrapport."),
    Byggesaksdokumenter("Byggesaksdokumenter", "Saksdokumenter fra byggesaken som for eksempel en byggetillatelse (igansettingstillatelse, midlertidig brukstillatelse, ferdisattest)."),
    Plantegninger("Plantegninger", "Utarbeidet av en profesjonell. Velges hvis det ikke er plantegninger i en byggesak, men utarebeidet i annen sammenheng som feks en seksjoneringssak eller rehabilitering eller andre arbeider som ikke krever søknad og tillatelse etter plan- og bygningsloven."),
    AnnenDokumentasjon("Annen dokumentasjon", "Når du har dokumentasjon som ikke passer i de andre alternativene, men det er mulig å dokumentere valget ditt, kan dette velges.")
}
