package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth

import jakarta.xml.ws.BindingProvider
import jakarta.xml.ws.WebServiceException
import jakarta.xml.ws.handler.MessageContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApi
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelBubbleIdList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bruker.Bruker
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bruker.BrukerRettighetListe
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bruker.LandForvaltningsomrade

class MatrikkelAuthService(private val matrikkelApiProvider: MatrikkelApi) : AuthService {
    /**
     * Returnerer brukernavnet hvis brukeren har tilgang til alle bygningsdata på landsbasis, ellers {@code null}.
     */
    override suspend fun harMatrikkeltilgang(token: String, rolle: Matrikkelrolle): String? {
        val matrikkelApi = matrikkelApiProvider.withAuth(token)
        val brukerService = matrikkelApi.brukerService()

        return withContext(Dispatchers.IO) {
            val brukerId = try {
                brukerService.getPaloggetBruker(matrikkelApi.matrikkelContext)
            } catch (e: WebServiceException) {
                val responseContext = (brukerService as BindingProvider).responseContext
                val code = responseContext[MessageContext.HTTP_RESPONSE_CODE] as Int?
                if (code == 401 || code == 403) null
                else throw e
            }

            brukerId?.let {
                val brukerrettighetlisteId = brukerService.findBrukerRettigheterForPaaloggetBruker(matrikkelApi.matrikkelContext)

                val storeService = matrikkelApi.storeService()
                val objects = storeService.getObjects(
                    MatrikkelBubbleIdList().apply {
                        item.add(brukerId)
                        item.add(brukerrettighetlisteId)
                    },
                    matrikkelApi.matrikkelContext,
                )

                val bruker = objects.item.filterIsInstance<Bruker>().first()
                val brukerrettighetliste = objects.item.filterIsInstance<BrukerRettighetListe>().first()

                val harAkseptabelRettighet = brukerrettighetliste.rettigheter.item
                    .filter { it.fjernetDato == null }
                    .filter { it.forvaltningsomrade is LandForvaltningsomrade }
                    .any { it.rolleId.value in rolle.akseptableRolleIds }

                if (harAkseptabelRettighet) bruker.brukernavn else null
            }
        }
    }
}
