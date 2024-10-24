package no.kartverket.matrikkel.bygning.infrastructure.matrikkel

import java.time.Instant
import java.time.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.LocalDate as MatrikkelLocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.Timestamp as MatrikkelTimestamp

fun MatrikkelTimestamp.toInstant(): Instant {
    val calendar = timestamp.toGregorianCalendar() // TODO: Må sjekke litt mer på hvor trygt dette er
    return calendar.toInstant()
}

fun MatrikkelTimestamp.toLocalDate(): LocalDate {
    return LocalDate.of(
        timestamp.year,
        timestamp.month,
        timestamp.day,
    )
}

fun MatrikkelLocalDate.toLocalDate(): LocalDate {
    return LocalDate.of(
        date.year,
        date.month,
        date.day,
    )
}
