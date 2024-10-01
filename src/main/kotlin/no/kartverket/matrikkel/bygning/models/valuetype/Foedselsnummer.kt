package no.kartverket.matrikkel.bygning.models.valuetype

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Foedselsnummer(private val value: String) {
    fun getValue(): String = value

    fun getMaskedValue(): String  = ""
}
