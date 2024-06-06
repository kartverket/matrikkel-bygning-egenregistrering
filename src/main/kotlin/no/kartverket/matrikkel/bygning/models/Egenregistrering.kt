package no.kartverket.matrikkel.bygning.models

import kotlinx.serialization.Serializable


enum class MetadataStatus {
    Gjeldende, Slettet, Uenighet
}

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

@Serializable
data class RegistreringMetadata(
    val registrerer: String,
    val registreringstidspunkt: Long,
    val gyldigFra: Long,
    val gyldigTil: Long?,
    val status: MetadataStatus,
)

@Serializable
data class ByggeaarRegistrering(
    val byggeaar: Int, val registreringMetadata: RegistreringMetadata
)

@Serializable
data class BruksarealRegistrering(
    val bruksareal: Double, val registreringMetadata: RegistreringMetadata
)

@Serializable
data class VannforsyningsRegistrering(
    val vannforsyning: VannforsyningKode, val registreringMetadata: RegistreringMetadata
)

@Serializable
data class AvlopRegistrering(
    val avlop: AvlopKode, val registreringMetadata: RegistreringMetadata
)

@Serializable
data class EnergikildeRegistrering(
    val energikilde: EnergikildeKode, val registreringMetadata: RegistreringMetadata
)

@Serializable
data class OppvarmingRegistrering(
    val oppvarming: OppvarmingsKode, val registreringMetadata: RegistreringMetadata
)

@Serializable
data class BygningsRegistrering(
    val bygningsId: String,
    val bruksarealRegistrering: BruksarealRegistrering,
    val byggeaarRegistrering: ByggeaarRegistrering,
    val vannforsyningsRegistrering: VannforsyningsRegistrering,
    val avlopRegistrering: AvlopRegistrering
)

@Serializable
data class BruksenhetRegistrering(
    val bruksenhetId: String,
    val bruksarealRegistrering: BruksarealRegistrering,
    val energikildeRegistreringer: List<EnergikildeRegistrering>,
    val oppvarmingRegistreringer: List<OppvarmingRegistrering>
)

@Serializable
data class Egenregistrering(
    val bygningsRegistrering: BygningsRegistrering, val bruksenhetRegistreringer: List<BruksenhetRegistrering>
)
