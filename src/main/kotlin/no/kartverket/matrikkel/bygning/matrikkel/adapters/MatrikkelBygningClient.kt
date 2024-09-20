package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.bygningId
import no.kartverket.matrikkel.bygning.matrikkelapi.getBruksenheter
import no.kartverket.matrikkel.bygning.matrikkelapi.getBygning
import no.kartverket.matrikkel.bygning.matrikkelapi.toInstant
import no.kartverket.matrikkel.bygning.matrikkelapi.toLocalDate
import no.kartverket.matrikkel.bygning.models.Avlop
import no.kartverket.matrikkel.bygning.models.Bruksareal
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Byggeaar
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Energikilde
import no.kartverket.matrikkel.bygning.models.Multikilde
import no.kartverket.matrikkel.bygning.models.Oppvarming
import no.kartverket.matrikkel.bygning.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.models.Vannforsyning
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

// TODO Håndtering av at matrikkel servicene thrower på visse vanlige HTTP koder, ikke bare full try/catch
internal class MatrikkelBygningClient(
    private val matrikkelApi: MatrikkelApi.WithAuth
) : BygningClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun getBygningById(id: Long): Bygning? {
        val bygningId: BygningId = bygningId(id)

        try {
            val bygning = matrikkelApi.storeService().getBygning(bygningId, matrikkelApi.matrikkelContext)

            val bruksenheter = matrikkelApi.storeService().getBruksenheter(bygning.bruksenhetIds.item, matrikkelApi.matrikkelContext)

            val bygningsmetadata = RegisterMetadata(
                bygning.oppdateringsdato.toInstant(),
            )
            return Bygning(
                bygningId = bygning.id.value,
                bygningsnummer = bygning.bygningsnummer,
                byggeaar = Multikilde(
                    autoritativ = Byggeaar(
                        data = calculateByggeaarForBygning(bygning),
                        metadata = bygningsmetadata,
                    ),
                ),
                // TODO: Hvordan innse at arealet er ukjent og hvordan håndtere dette
                bruksareal = Multikilde(
                    autoritativ = Bruksareal(
                        bygning.etasjedata.bruksarealTotalt,
                        bygningsmetadata,
                    ),
                ),
                // TODO: Burde vi ha kode for Ikke oppgitt?
                vannforsyning = Multikilde(
                    autoritativ = mapVannforsyning(bygning.vannforsyningsKodeId)?.let {
                        Vannforsyning(
                            it,
                            bygningsmetadata,
                        )
                    },
                ),
                // TODO: Burde vi ha kode for Ikke oppgitt?
                avlop = Multikilde(
                    autoritativ = mapAvloep(bygning.avlopsKodeId)?.let {
                        Avlop(
                            it,
                            bygningsmetadata,
                        )
                    },
                ),
                // TODO: Skal tom liste i matrikkelen tolkes som "vet ikke" eller "ingen"?
                energikilder = Multikilde(
                    autoritativ = bygning.energikildeKodeIds.item.map {
                        Energikilde(
                            mapEnergikilde(it),
                            bygningsmetadata,
                        )
                    }.ifEmpty { null }, // Tolker som "vet ikke"
                ),
                // TODO: Skal tom liste i matrikkelen tolkes som "vet ikke" eller "ingen"?
                oppvarminger = Multikilde(
                    autoritativ = bygning.oppvarmingsKodeIds.item.map {
                        Oppvarming(
                            mapOppvarming(it),
                            bygningsmetadata,
                        )
                    }.ifEmpty { null }, // Tolker som "vet ikke"
                ),
                // TODO: Burde vi ha en måte å angi ukjent / ikke oppgitt?
                bruksenheter = bruksenheter.map {
                    val bruksenhetsmetadata = RegisterMetadata(
                        it.oppdateringsdato.toInstant(),
                    )
                    Bruksenhet(
                        bruksenhetId = it.id.value,
                        bygningId = it.byggId.value,
                        // TODO: Hvordan innse at arealet er ukjent og hvordan håndtere dette
                        bruksareal = Multikilde(
                            autoritativ = Bruksareal(
                                it.bruksareal,
                                bruksenhetsmetadata,
                            ),
                        ),
                    )
                },
            )
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med id {}", bygningId.value, exception)
            return null
        }
    }

    override fun getBygningByBygningsnummer(bygningsnummer: Long): Bygning? {
        try {
            val bygningId = matrikkelApi.bygningService().findBygning(bygningsnummer, matrikkelApi.matrikkelContext)

            return getBygningById(bygningId.value)
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med bygningsnummer {}", bygningsnummer, exception)
            return null
        }
    }

    // TODO Hvilken dato fra bygningsstatus er det som faktisk regnes som riktig dato å sjekke mot?
    private fun calculateByggeaarForBygning(bygning: MatrikkelBygning): Int {
        val bygningsstatuserWithDerivableByggeaar = bygning.bygningsstatusHistorikker.item.filter {
            // TODO Sjekke faktisk dato
            val earliestDateForDerivingByggeaar = LocalDate.of(2013, 1, 1)

            val isBygningsstatusDerivable = (it.dato.toLocalDate() >= earliestDateForDerivingByggeaar)

            // TODO Hvilke verdier skal vi faktisk bruke her? Trenger vi å lage en mapper
            val isCorrectBygningsstatusType = it.bygningsstatusKodeId.value == 0L || it.bygningsstatusKodeId.value == 1L

            isCorrectBygningsstatusType && isBygningsstatusDerivable
        }

        // Nullability her?
        if (bygningsstatuserWithDerivableByggeaar.isEmpty()) return -1

        // TODO Hvis det er flere bygningsstatuser som er innafor for å derivere, hvilken skal vi egentlig bruke?
        return bygningsstatuserWithDerivableByggeaar.sortedBy { it.dato.toLocalDate() }.first().dato.toLocalDate().year
    }
}
