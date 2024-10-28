package no.kartverket.matrikkel.bygning.routes.v1.kodeliste

import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.IKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import kotlin.reflect.KClass

fun Route.kodelisteRouting() {
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
        get {
            call.respond(kodeClass.toKodeList())
        }
    }
}
