package no.kartverket.matrikkel.bygning.routes.v1

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.IKode
import no.kartverket.matrikkel.bygning.models.kodelister.Kode
import no.kartverket.matrikkel.bygning.models.kodelister.KodelisterResponse
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.models.kodelister.toKodeList
import kotlin.reflect.KClass

fun Route.kodelisteRouting() {
    kodelisterDoc()
    get {
        call.respond(
            KodelisterResponse(
                energikildeKoder = EnergikildeKode::class.toKodeList(),
                vannforsyningKoder = VannforsyningKode::class.toKodeList(),
                avlopKoder = AvlopKode::class.toKodeList(),
                oppvarmingKoder = OppvarmingKode::class.toKodeList(),
            ),
        )
    }

    kodelisteRoute("avlop", AvlopKode::class)
    kodelisteRoute("oppvarming", OppvarmingKode::class)
    kodelisteRoute("energikilde", EnergikildeKode::class)
    kodelisteRoute("vannforsyning", VannforsyningKode::class)
}

private inline fun <reified T> Route.kodelisteRoute(
    name: String, kodeClass: KClass<T>
): Route where T : Enum<T>, T : IKode {
    return route(name) {
        kodelisteDoc(name)
        get {
            call.respond(kodeClass.toKodeList())
        }
    }
}

private fun Route.kodelisterDoc() {
    install(NotarizedRoute()) {
        tags = setOf("Kodelister")
        get = GetInfo.builder {
            summary("Henter alle kodelister relatert til egenregistreringer")
            description("Henter alle kodelister relatert til egenregistreringer, med tilhørende kode, kodenavn, presentasjonsnavn og beskrivelse")

            response {
                responseCode(HttpStatusCode.OK)
                responseType<KodelisterResponse>()
                description("Kodelister")
            }
        }
    }
}

private fun Route.kodelisteDoc(name: String) {
    install(NotarizedRoute()) {
        tags = setOf("Kodelister")
        get = GetInfo.builder {
            summary("Henter kodeliste relatert til $name")
            description("Henter kodeliste relatert til $name, med tilhørende kode, kodenavn, presentasjonsnavn og beskrivelse")

            response {
                responseCode(HttpStatusCode.OK)
                responseType<List<Kode>>()
                description("Kodeliste for $name")
            }
        }
    }
}
