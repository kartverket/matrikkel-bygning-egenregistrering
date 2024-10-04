package no.kartverket.matrikkel.bygning.models.valuetype

import kotlinx.serialization.Serializable
import kotlin.text.Regex


private val kontrollsiffer1Numbers = listOf(3, 7, 6, 1, 8, 9, 4, 5, 2, 1)
private val kontrollsiffer2Numbers = listOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 1)

@JvmInline
@Serializable
value class Foedselsnummer(private val value: String) {
    fun getValue(): String = value

    fun getMaskedValue(): String = value.replace(Regex("\\d"), "*")

    fun validate(): Boolean {
        if (value.length != 11) return false

        if (value.any { !it.isDigit() }) return false

        val kontrollsiffer1IsValid =
            value.substring(0, 10).mapIndexed { index, s -> s.digitToInt() * kontrollsiffer1Numbers[index] }.sum() % 11 == 0

        val kontrollsiffer2IsValid =
            value.substring(0, 11).mapIndexed { index, s -> s.digitToInt() * kontrollsiffer2Numbers[index] }.sum() % 11 == 0

        return kontrollsiffer1IsValid && kontrollsiffer2IsValid
    }
}
