package no.kartverket.matrikkel.bygning.models.kodelister

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// Kan det finnes flere presentasjonsnavn på én kode? For eksempel per ett på bokmål og ett på nynorsk?
interface IKode {
    val kodenavn: String
    val presentasjonsnavn: String
    val beskrivelse: String
}

@Serializable
data class Kode(
    val kode: String,
    val kodenavn: String,
    val presentasjonsnavn: String,
    val beskrivelse: String
)

@Serializable
enum class VannforsyningsKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
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

@Serializable
enum class AvlopsKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    O("OffentligKloakk", "Offentlig kloakk", "Avløp er offentlig kloakk"),
    P("PrivatKloakk", "Privat kloakk", "Avløp er privat kloakk"),
    I("IngenKloakk", "Ingen kloakk", "Ingen tilknytning til kloakk")
}

@Serializable
enum class EnergikildeKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
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

@Serializable
enum class OppvarmingsKode(
    override val kodenavn: String,
    override val presentasjonsnavn: String,
    override val beskrivelse: String
) : IKode {
    E("Elektrisk", "Elektrisk", "Elektrisk oppvarming"),
    S("Sentralvarme", "Sentralvarme", "Sentralvarme"),
    A("AnnenOppvarming", "Annen oppvarming", "Annen oppvarming")
}

@Serializable
data class KodelisterResponse(
    val vannforsyningsKoder: List<Kode>,
    val avlopsKoder: List<Kode>,
    val energikildeKoder: List<Kode>,
    val oppvarmingsKoder: List<Kode>,
)

/*
 * Enums i Kotlin returnerer kun name parameteret ved bruk av EnumClass.entries(). Denne extension functionen er til for å kunne returnere
 * alle relevante parametere
 */
inline fun <reified T> KClass<T>.toKodeList(): List<Kode> where T : Enum<T>, T : IKode {
    return enumValues<T>().map {
        Kode(
            kode = it.name,
            kodenavn = it.kodenavn,
            presentasjonsnavn = it.presentasjonsnavn,
            beskrivelse = it.beskrivelse
        )
    }
}