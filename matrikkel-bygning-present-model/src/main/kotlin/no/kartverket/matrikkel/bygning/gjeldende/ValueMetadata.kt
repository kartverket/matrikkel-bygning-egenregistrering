package no.kartverket.matrikkel.bygning.gjeldende

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ValueMetadata(
    val registrertAv: Registreringsinformasjon,
    val avsluttetAv: Registreringsinformasjon?,
    val gyldigFra: LocalDate?,
    val gyldigTil: LocalDate?,
)
