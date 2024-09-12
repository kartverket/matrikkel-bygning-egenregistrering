package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.getBruksenheter
import no.kartverket.matrikkel.bygning.matrikkelapi.getBygning
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
            log.warn("Noe gikk galt under henting av bygning med nummer {}", bygningsnummer, exception)
            return null
        }
    }
}
