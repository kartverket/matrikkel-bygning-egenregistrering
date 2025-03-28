package no.kartverket.matrikkel.bygning.infrastructure.matrikkel

import jakarta.xml.ws.BindingProvider
import jakarta.xml.ws.handler.MessageContext
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelContext
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.geometri.koder.KoordinatsystemKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.bruker.BrukerService
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.bruker.BrukerServiceWS
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.bygning.BygningService
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.bygning.BygningServiceWS
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.kommune.KommuneService
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.kommune.KommuneServiceWS
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.StoreService
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.StoreServiceWS
import org.slf4j.MDC
import java.net.URI

data class MatrikkelApiConfig(
    val useStub: Boolean = false,
    val baseUrl: String,
    val username: String,
    val password: String,
) {
    companion object {
        fun forStubbing() = MatrikkelApiConfig(useStub = true, "", "", "")
    }
}

class MatrikkelApi(private val baseUrl: URI) {
    private val storeServiceFactory = StoreServiceWS()
    private val kommuneServiceFactory = KommuneServiceWS()
    private val bygningServiceFactory = BygningServiceWS()
    private val brukerServiceFactory = BrukerServiceWS()

    // TODO: Uheldig at denne er mutabel, for da kan noe ødelegge for andre. Kanskje er det ikke for dyrt å lage en nye hver gang.
    val matrikkelContext = MatrikkelContext().apply {
        systemVersion = "4.18"
        locale = "no_NO_B" // TODO: Dette skal kanskje variere fra bruker til bruker
        klientIdentifikasjon = "matrikkel-bygning"
        koordinatsystemKodeId = KoordinatsystemKodeId().apply { value = 24 }
        isBrukOriginaleKoordinater = true
    }

    fun withAuth(username: String, password: String): WithAuth {
        return WithAuthImpl(BasicAuthAuthenticator(username, password))
    }

    fun withAuth(bearerToken: String): WithAuth {
        return WithAuthImpl(BearerTokenAuthenticator(bearerToken))
    }

    interface WithAuth {
        fun storeService(): StoreService
        fun kommuneService(): KommuneService
        fun bygningService(): BygningService
        fun brukerService(): BrukerService
        val matrikkelContext: MatrikkelContext
    }

    private inner class WithAuthImpl(private val authenticator: Authenticator) : WithAuth {
        private fun BindingProvider.configure(endpointPath: String) {
            requestContext[BindingProvider.ENDPOINT_ADDRESS_PROPERTY] = baseUrl.resolve(endpointPath).toString()
            MDC.get("request_id")?.let {
                @Suppress("UNCHECKED_CAST")
                (requestContext.computeIfAbsent(MessageContext.HTTP_REQUEST_HEADERS) { HashMap<String, MutableList<String>>() } as HashMap<String, MutableList<String>>)
                    .computeIfAbsent("X-Request-ID") { ArrayList() }
                    .add(it)
            }
            authenticator(requestContext)
        }

        override fun storeService(): StoreService {
            @Suppress("UsePropertyAccessSyntax") // getStoreServicePort er ikke en getter for en property
            val storeService = storeServiceFactory.getStoreServicePort()
            (storeService as BindingProvider).configure("/matrikkelapi/wsapi/v1/StoreServiceWS")
            return storeService
        }

        override fun kommuneService(): KommuneService {
            @Suppress("UsePropertyAccessSyntax") // getStoreServicePort er ikke en getter for en property
            val kommuneService = kommuneServiceFactory.getKommuneServicePort()
            (kommuneService as BindingProvider).configure("/matrikkelapi/wsapi/v1/KommuneServiceWS")
            return kommuneService
        }

        override fun bygningService(): BygningService {
            @Suppress("UsePropertyAccessSyntax") // getBygningServicePort er ikke en getter for en property
            val bygningService = bygningServiceFactory.getBygningServicePort()
            (bygningService as BindingProvider).configure("/matrikkelapi/wsapi/v1/BygningServiceWS")
            return bygningService
        }

        override fun brukerService(): BrukerService {
            @Suppress("UsePropertyAccessSyntax") // getBrukerServicePort er ikke en getter for en property
            val brukerService = brukerServiceFactory.getBrukerServicePort()
            (brukerService as BindingProvider).configure("/matrikkelapi/wsapi/v1/BrukerServiceWS")
            return brukerService
        }

        override val matrikkelContext: MatrikkelContext
            get() = this@MatrikkelApi.matrikkelContext
    }

    private interface Authenticator {
        operator fun invoke(requestContext: MutableMap<String, Any?>)
    }

    private class BasicAuthAuthenticator(private val username: String, private val password: String) :
        Authenticator {
        override fun invoke(requestContext: MutableMap<String, Any?>) {
            requestContext[BindingProvider.USERNAME_PROPERTY] = username
            requestContext[BindingProvider.PASSWORD_PROPERTY] = password
        }
    }

    private class BearerTokenAuthenticator(private val bearerToken: String) : Authenticator {
        override fun invoke(requestContext: MutableMap<String, Any?>) {
            requestContext[MessageContext.HTTP_REQUEST_HEADERS] = mapOf(
                "Authorization" to listOf("Bearer $bearerToken"),
            )
        }
    }
}
