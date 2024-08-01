
import clients.LocalBygningClient
import matrikkelclients.BygningClient
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.clients.MatrikkelBygningClient
import java.net.URI

class MatrikkelFactory {
    private val matrikkelUsername = System.getenv("MATRIKKEL_USERNAME")
    private val matrikkelPassword = System.getenv("MATRIKKEL_PASSWORD")
    private val matrikkelBaseUrl = System.getenv("MATRIKKEL_BASE_URL")

    var bygningClient: BygningClient

    init {
        // TODO Med denne løsningen så slipper man på en måte ekstra env vars for å vurdere miljø, men risikerer å feile ganske stille
        // hvis noe ikke fungerer i prod, f. eks. Tenker denne gjerne kan diskuteres
        if (matrikkelUsername == null || matrikkelPassword == null || matrikkelBaseUrl == null) {
            bygningClient = LocalBygningClient()
        } else {
            val matrikkelApi = MatrikkelApi(
                URI(matrikkelBaseUrl),
            ).withAuth(matrikkelUsername, matrikkelPassword)

            bygningClient = MatrikkelBygningClient(matrikkelApi)
        }
    }
}