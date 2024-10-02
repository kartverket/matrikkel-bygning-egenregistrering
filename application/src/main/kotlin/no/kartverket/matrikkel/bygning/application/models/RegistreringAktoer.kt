package no.kartverket.matrikkel.bygning.application.models

import no.bekk.bekkopen.person.FodselsnummerValidator

sealed class RegistreringAktoer {
    abstract val value: String

    data class Foedselsnummer(override val value: String) : RegistreringAktoer() {
        init {
            // TODO Dette burde ikke være lov når vi er i prod, da må vi sjekke miljøet vi er i
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true

            if (!FodselsnummerValidator.isValid(value)) {
                throw IllegalArgumentException("Fødselsnummer er ikke gyldig")
            }
        }

        fun getMaskedValue(): String = value.replace(Regex("\\d"), "*")
    }

    data class Signatur(override val value: String) : RegistreringAktoer()
}
