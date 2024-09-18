package no.kartverket.matrikkel.bygning.matrikkelapi

import java.time.Instant
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.Timestamp as MatrikkelTimestamp

fun MatrikkelTimestamp.toInstant(): Instant {
    val calendar = timestamp.toGregorianCalendar() // TODO: Må sjekke litt mer på hvor trygt dette er
    return calendar.toInstant()
}
