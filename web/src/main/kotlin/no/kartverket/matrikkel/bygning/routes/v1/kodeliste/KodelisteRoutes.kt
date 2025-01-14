package no.kartverket.matrikkel.bygning.routes.v1.kodeliste

import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.IKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import kotlin.reflect.KClass

fun Route.kodelisteRouting() {
    get(
        {
            summary = "Henter alle kodelister relatert til egenregistreringer"
            description =
                "Henter alle kodelister relatert til egenregistreringer, med tilhørende kode, kodenavn, presentasjonsnavn og beskrivelse"
            response {
                code(HttpStatusCode.OK) {
                    body<KodelisterResponse>()
                }
            }
        },
    ) {
        call.respond(
            KodelisterResponse(
                energikildeKoder = EnergikildeKode::class.toKodeList(),
                vannforsyningKoder = VannforsyningKode::class.toKodeList(),
                avlopKoder = AvlopKode::class.toKodeList(),
                oppvarmingKoder = OppvarmingKode::class.toKodeList(),
                kildematerialeKoder = KildematerialeKode::class.toKodeList(),
            ),
        )
    }

    kodelisteRoute("avlop", AvlopKode::class)
    kodelisteRoute("oppvarming", OppvarmingKode::class)
    kodelisteRoute("energikilde", EnergikildeKode::class)
    kodelisteRoute("vannforsyning", VannforsyningKode::class)
    kodelisteRoute("kildemateriale", KildematerialeKode::class)
    kodelisteRoute("etasjeplan", EtasjeplanKode::class)
}

private inline fun <reified T> Route.kodelisteRoute(
    name: String, kodeClass: KClass<T>
): Route where T : Enum<T>, T : IKode {
    return route(name) {
        get(
            {
                summary = "Henter kodeliste relatert til $name"
                description = "Henter kodeliste relatert til $name, med tilhørende kode, kodenavn, presentasjonsnavn og beskrivelse"
                response {
                    code(HttpStatusCode.OK) {
                        body<List<Kode>>() {
                            description = "Kodeliste for $name"
                        }
                    }
                }
            },
        ) {
            call.respond(kodeClass.toKodeList())
        }
    }
}
