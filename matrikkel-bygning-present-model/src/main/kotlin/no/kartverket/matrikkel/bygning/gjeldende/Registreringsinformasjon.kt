package no.kartverket.matrikkel.bygning.gjeldende

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Registreringsinformasjon {
    abstract val registreringstidspunkt: Instant
}

@Serializable
@SerialName("KonvertertFraMatrikkelen")
data class KonvertertFraMatrikkelen(
    override val registreringstidspunkt: Instant
) : Registreringsinformasjon()
