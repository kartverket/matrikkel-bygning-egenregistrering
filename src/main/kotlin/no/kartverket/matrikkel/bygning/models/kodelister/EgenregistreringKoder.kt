package no.kartverket.matrikkel.bygning.models.kodelister

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// Kan det finnes flere presentasjonsnavn på én kode? For eksempel per ett på bokmål og ett på nynorsk?
interface IKode {
    val presentasjonsnavn: String
    val beskrivelse: String
}

@Serializable
data class Kode(
    val kode: String,
    val presentasjonsnavn: String,
    val beskrivelse: String
)

@Serializable
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

@Serializable
enum class AvlopKode(
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    OffentligKloakk("Offentlig kloakk", "Avløp er offentlig kloakk"),
    PrivatKloakk("Privat kloakk", "Avløp er privat kloakk"),
    IngenKloakk("Ingen kloakk", "Ingen tilknytning til kloakk")
}

@Serializable
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

@Serializable
enum class OppvarmingKode(
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    Elektrisk("Elektrisk", "Elektrisk oppvarming"),
    Sentralvarme("Sentralvarme", "Sentralvarme"),
    AnnenOppvarming("Annen oppvarming", "Annen oppvarming")
}

@Serializable
data class KodelisterResponse(
    val vannforsyningKoder: List<Kode>,
    val avlopKoder: List<Kode>,
    val energikildeKoder: List<Kode>,
    val oppvarmingKoder: List<Kode>,
)

/*
 * Enums i Kotlin returnerer kun name parameteret ved bruk av EnumClass.entries(). Denne extension functionen er til for å kunne returnere
 * alle relevante parametere
 */
inline fun <reified T> KClass<T>.toKodeList(): List<Kode> where T : Enum<T>, T : IKode {
    return enumValues<T>().map {
        Kode(
            kode = it.name,
            presentasjonsnavn = it.presentasjonsnavn,
            beskrivelse = it.beskrivelse,
        )
    }
}
