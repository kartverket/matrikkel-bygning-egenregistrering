package no.kartverket.matrikkel.bygning.matrikkelapi.clients

import matrikkelclients.BygningClient
import models.Bruksenhet
import models.Bygning
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.getObjectAs
import no.kartverket.matrikkel.bygning.matrikkelapi.getObjectsAs
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.ServiceException
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bruksenhet as MatrikkelBruksenhet
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning as MatrikkelBygning

// TODO Håndtering av at matrikkel servicene thrower på visse vanlige HTTP koder, ikke bare full try/catch
// TODO Skal man bare bruke Longs i stedet for å være innom strings hele tiden?
class MatrikkelBygningClient(
    val matrikkelApi: MatrikkelApi.WithAuth
) : BygningClient {
    override fun getBygningById(id: String): Bygning? {
        val bygningId: BygningId = BygningId().apply { value = id.toLong() }

        try {
            val bygning =
                matrikkelApi.storeService().getObjectAs<MatrikkelBygning>(bygningId, matrikkelApi.matrikkelContext)

            val bruksenheter = matrikkelApi.storeService()
                .getObjectsAs<MatrikkelBruksenhet>(bygning.bruksenhetIds.item, matrikkelApi.matrikkelContext)

            return Bygning(
                bygningId = bygning.id.toString(),
                bygningNummer = bygning.bygningsnummer.toString(),
                bruksenheter = bruksenheter.map {
                    Bruksenhet(
                        bruksenhetId = it.id.toString(), bygningId = it.byggId.toString()
                    )
                })
        } catch (exception: ServiceException) {
            return null
        }
    }

    override fun getBygningByBygningNummer(bygningNummer: String): Bygning? {
        try {
            val bygningId =
                matrikkelApi.bygningService().findBygning(bygningNummer.toLong(), matrikkelApi.matrikkelContext)

            return getBygningById(bygningId.toString())
        } catch (exception: ServiceException) {
            return null
        }
    }
}