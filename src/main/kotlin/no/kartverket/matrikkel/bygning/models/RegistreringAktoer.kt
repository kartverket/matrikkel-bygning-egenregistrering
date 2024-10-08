package no.kartverket.matrikkel.bygning.models

import no.bekk.bekkopen.person.FodselsnummerValidator

sealed class RegistreringAktoer {
    abstract val value: String

    data class Foedselsnummer(override val value: String) : RegistreringAktoer() {
        init {
            if (!FodselsnummerValidator.isValid(value)) {
                throw IllegalArgumentException("Fødselsnummer er ikke gyldig")
            }
        }

        fun getMaskedValue(): String = value.replace(Regex("\\d"), "*")
    }

    data class Signatur(override val value: String) : RegistreringAktoer()
}
