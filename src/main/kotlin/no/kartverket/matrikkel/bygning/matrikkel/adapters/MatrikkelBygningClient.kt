package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.getBruksenheter
import no.kartverket.matrikkel.bygning.matrikkelapi.getBygning
import no.kartverket.matrikkel.bygning.matrikkelapi.toInstant
import no.kartverket.matrikkel.bygning.matrikkelapi.toJavaLocalDate
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Byggeaar
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.KildeKode
import no.kartverket.matrikkel.bygning.models.Registreringsinformasjon
import no.kartverket.matrikkel.bygning.models.TotaltBruksareal
import no.kartverket.matrikkel.bygning.models.ValueMetadata
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import java.time.ZonedDateTime
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

val matrikkellovensIkrafttredelse: ZonedDateTime = LocalDate.of(2010, 1, 1).atStartOfDay(ZoneId.of("Europe/Oslo"))

// TODO Håndtering av at matrikkel servicene thrower på visse vanlige HTTP koder, ikke bare full try/catch
internal class MatrikkelBygningClient(
    private val matrikkelApi: MatrikkelApi.WithAuth
) : BygningClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun getBygningById(id: Long): Bygning? {
        val bygningId: BygningId = BygningId().apply { value = id }

        try {
            val bygning = matrikkelApi.storeService().getBygning(bygningId, matrikkelApi.matrikkelContext)

            val bruksenheter = matrikkelApi.storeService().getBruksenheter(bygning.bruksenhetIds.item, matrikkelApi.matrikkelContext)

            return Bygning(
                bygningId = bygning.id.value,
                bygningsnummer = bygning.bygningsnummer,
                bruksenheter = bruksenheter.map {
                    Bruksenhet(
                        bruksenhetId = it.id.value,
                        bygningId = it.byggId.value,
                    )
                },
                byggeaar = calculateByggeaar(bygning),
                bruksareal = listOf(
                    TotaltBruksareal(
                        ValueMetadata(
                            Registreringsinformasjon(
                                bygning.oppdateringsdato.toInstant()
                            ),
                            KildeKode.Matrikkelfoert,
                        ),
                        bygning.etasjedata.bruksarealTotalt
                    )
                )
            )
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med id {}", bygningId.value, exception)
            return null
        }
    }

    private fun calculateByggeaar(bygning: MatrikkelBygning): List<Byggeaar> {
        return listOfNotNull(
            bygning.bygningsstatusHistorikker.item
                .filter { it.slettetDato == null }
                .filter {
                    when (it.bygningsstatusKodeId.value) {
                        2L, 3L, 4L -> true
                        else -> false
                    }
                }
                .filter { it.registrertDato?.toInstant()?.isBefore(matrikkellovensIkrafttredelse.toInstant())?.not() ?: false }
                .minByOrNull { it.dato.toJavaLocalDate() }
                ?.let {
                    Byggeaar(
                        ValueMetadata(
                            Registreringsinformasjon(it.oppdateringsdato.toInstant()),
                            KildeKode.Matrikkelfoert,
                        ),
                        Year.from(it.dato.toJavaLocalDate())
                    )
                }
        )
    }

    override fun getBygningByBygningNummer(bygningNummer: Long): Bygning? {
        try {
            val bygningId = matrikkelApi.bygningService().findBygning(bygningNummer, matrikkelApi.matrikkelContext)

            return getBygningById(bygningId.value)
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med nummer {}", bygningNummer, exception)
            return null
        }
    }
}
