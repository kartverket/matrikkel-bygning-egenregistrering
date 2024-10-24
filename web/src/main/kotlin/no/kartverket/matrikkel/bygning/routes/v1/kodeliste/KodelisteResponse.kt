package no.kartverket.matrikkel.bygning.routes.v1.kodeliste

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.kodelister.IKode
import kotlin.reflect.KClass

@Serializable
data class KodelisterResponse(
    val vannforsyningKoder: List<Kode>,
    val avlopKoder: List<Kode>,
    val energikildeKoder: List<Kode>,
    val oppvarmingKoder: List<Kode>,
)

@Serializable
data class Kode(
    val kode: String,
    val presentasjonsnavn: String,
    val beskrivelse: String
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
