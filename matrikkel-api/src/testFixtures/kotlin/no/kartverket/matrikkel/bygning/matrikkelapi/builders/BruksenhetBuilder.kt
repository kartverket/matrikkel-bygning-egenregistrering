package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bruksenhet

fun bruksenhet(scope: Bruksenhet.() -> Unit) = Bruksenhet().apply(scope)
