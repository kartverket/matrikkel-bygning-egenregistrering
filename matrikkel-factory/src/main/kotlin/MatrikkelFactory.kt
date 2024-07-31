
import matrikkelclients.BygningClient
import models.Bruksenhet
import models.Bygning
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.clients.MatrikkelBygningClient
import java.net.URI

data class MatrikkelConfig(
    val baseUrl: String,
    val username: String,
    val password: String,
)

// TODO Leve et annet sted den her sikkert
class LocalBygningClient : BygningClient {
    val bruksenheter: List<Bruksenhet> = listOf(
        Bruksenhet(
            "a", "1"
        ), Bruksenhet(
            "b", "1"
        ), Bruksenhet(
            "c", "2"
        ), Bruksenhet(
            "d", "2"
        )
    )
    val bygninger: List<Bygning> = listOf(
        Bygning(
            "1", "100", bruksenheter = bruksenheter.subList(0, 2)
        ), Bygning(
            "2", "200", bruksenheter = bruksenheter.subList(2, 4)
        )
    )

    override fun getBygningById(id: String): Bygning? {
        return bygninger.find { it.bygningId == id }
    }

    override fun getBygningByBygningNummer(bygningNummer: String): Bygning? {
        return bygninger.find { it.bygningNummer == bygningNummer }
    }

}

class MatrikkelFactory(matrikkelConfig: MatrikkelConfig) {
    var bygningClient: BygningClient

    init {
        // IsCloud? Idk hva vi skal kalle den
        val localEnv = System.getenv("IS_LOCAL")
        val isLocal = localEnv == null;

        bygningClient = if (!isLocal) {
            val matrikkelApi = MatrikkelApi(
                URI(matrikkelConfig.baseUrl),
            ).withAuth(matrikkelConfig.username, matrikkelConfig.password)

            MatrikkelBygningClient(matrikkelApi)
        } else {
            LocalBygningClient()
        }

    }
}