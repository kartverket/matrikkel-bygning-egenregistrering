package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.Timestamp
import javax.xml.datatype.DatatypeFactory

private val datatypeFactory by lazy { DatatypeFactory.newDefaultInstance() }

fun timestampUtc(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0) = Timestamp().apply {
    this.timestamp = datatypeFactory.newXMLGregorianCalendar(year, month, day, hour, minute, second, 0, 0)
}
