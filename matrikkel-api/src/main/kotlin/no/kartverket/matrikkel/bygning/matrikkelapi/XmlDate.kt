package no.kartverket.matrikkel.bygning.matrikkelapi

import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.LocalDate as MatrikkelLocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.Timestamp as MatrikkelTimestamp

fun MatrikkelTimestamp.toInstant(): Instant {
    val calendar = timestamp.toGregorianCalendar() // TODO: Må sjekke litt mer på hvor trygt dette er
    return calendar.toInstant()
}

// TODO: Jeg aner ikke om dette blir riktig
fun MatrikkelLocalDate.toLocalDate(): LocalDate {
    val calendar = date.toGregorianCalendar()

    return LocalDate.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}
