package no.kartverket.matrikkel.bygning.routes.v1.common

import java.time.Instant
import java.time.format.DateTimeParseException

fun String.toInstant(): Instant =
    try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Ugyldig datoformat for: '$this'. Forventet format: ISO-8601.", e)
    }
