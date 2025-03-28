package no.kartverket.matrikkel.bygning.application.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import no.bekk.bekkopen.person.FodselsnummerValidator
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Signatur

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = Foedselsnummer::class, name = "foedselsnummer"),
        JsonSubTypes.Type(value = Signatur::class, name = "signatur"),
    ],
)
sealed class RegistreringAktoer {
    abstract val value: String

    @JsonTypeName("foedselsnummer")
    data class Foedselsnummer(
        override val value: String,
    ) : RegistreringAktoer() {
        init {
            // TODO Dette burde ikke være lov når vi er i prod, da må vi sjekke miljøet vi er i
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true

            if (!FodselsnummerValidator.isValid(value)) {
                throw IllegalArgumentException("Fødselsnummer er ikke gyldig")
            }
        }

        fun toMaskedValue(): String = value.replace(Regex("\\d"), "*")
    }

    @JsonTypeName("signatur")
    data class Signatur(
        override val value: String,
    ) : RegistreringAktoer()
}
