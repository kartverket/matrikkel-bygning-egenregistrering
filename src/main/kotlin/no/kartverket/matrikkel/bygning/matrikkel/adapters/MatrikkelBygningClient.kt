package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.getObjectAs
import no.kartverket.matrikkel.bygning.matrikkelapi.getObjectsAs
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bruksenhet as MatrikkelBruksenhet
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

// TODO Håndtering av at matrikkel servicene thrower på visse vanlige HTTP koder, ikke bare full try/catch
// TODO Logging? Skal vi bruke sl4j for logging i klasser f. eks? Sikkert lurt å ta en runde på logging generelt
internal class MatrikkelBygningClient(
    val matrikkelApi: MatrikkelApi.WithAuth
) : BygningClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun getBygningById(id: Long): Bygning? {
        val bygningId: BygningId = BygningId().apply { value = id }

        try {
            val bygning =
                matrikkelApi.storeService().getObjectAs<MatrikkelBygning>(bygningId, matrikkelApi.matrikkelContext)

            val bruksenheter = matrikkelApi.storeService()
                .getObjectsAs<MatrikkelBruksenhet>(bygning.bruksenhetIds.item, matrikkelApi.matrikkelContext)

            return Bygning(
                bygningId = bygning.id.value,
                bygningNummer = bygning.bygningsnummer,
                bruksenheter = bruksenheter.map {
                    Bruksenhet(
                        bruksenhetId = it.id.value, bygningId = it.byggId.value,
                    )
                },
            )
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med id {}", bygningId, exception)
            return null
        }
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
