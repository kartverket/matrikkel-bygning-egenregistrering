package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.Timestamp
import javax.xml.datatype.DatatypeFactory

private val datatypeFactory by lazy { DatatypeFactory.newDefaultInstance() }

fun timestampUtc(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0) = Timestamp().apply {
    this.timestamp = datatypeFactory.newXMLGregorianCalendar(year, month, day, hour, minute, second, 0, 0)
}

fun localDateUtc(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0) = LocalDate().apply {
    this.date = datatypeFactory.newXMLGregorianCalendar(year, month, day, hour, minute, second, 0, 0)
}
