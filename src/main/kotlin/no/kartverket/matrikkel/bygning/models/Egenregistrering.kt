package no.kartverket.matrikkel.bygning.models

// TODO Hent fra kodelister i stedet for å definere selv
enum class OppvarmingsKode {
    Elektrisk, Sentralvarme, Annen
}

enum class EnergikildeKode {
    Biobrensel, Elektrisitet, Fjernvarme, Gass, Oljeparafin, Solenergi, Varmepumpe, Annen
}

enum class AvlopKode {
    OffentligKloakk, PrivatKloakk, Ingen,
}

enum class VannforsyningKode {
    OffentligVannverk, TilknyttetPrivatVannverk, AnnenPrivatInnlagtVann, AnnenPrivatIkkeInnlagtVann,
}
