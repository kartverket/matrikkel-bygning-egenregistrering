package no.kartverket.matrikkel.bygning.models.valuetype

import kotlinx.serialization.Serializable
import no.bekk.bekkopen.person.FodselsnummerValidator
import kotlin.text.Regex

@JvmInline
@Serializable
value class Foedselsnummer(private val value: String) {
    init {
        if (!FodselsnummerValidator.isValid(value)) (throw IllegalArgumentException("Fødselsnummer er ikke gyldig"))
    }

    fun getValue(): String = value

    fun getMaskedValue(): String = value.replace(Regex("\\d"), "*")
}
