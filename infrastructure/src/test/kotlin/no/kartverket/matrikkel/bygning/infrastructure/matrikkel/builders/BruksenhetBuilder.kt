package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders

import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelKjokkentilgangKode
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bruksenhet

fun bruksenhet(scope: Bruksenhet.() -> Unit) = Bruksenhet()
    .apply {
        // Fyll inn verdier som aldri vil være null i matrikkelen, bortsett fra de man alltid bør spesifisere eksplisitt i testen.
        antallRom = 0
        antallBad = 0
        antallWC = 0
        bruksareal = 0.0
        kjokkentilgangId = MatrikkelKjokkentilgangKode.IkkeOppgitt()
        isSkalUtga = false
        isByggSkjermingsverdig = false
    }
    .apply(scope)
