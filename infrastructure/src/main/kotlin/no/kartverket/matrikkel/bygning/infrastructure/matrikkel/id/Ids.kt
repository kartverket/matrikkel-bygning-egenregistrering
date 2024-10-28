package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BruksenhetId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId

fun bygningId(value: Long): BygningId = BygningId().apply { this.value = value }

fun bruksenhetId(value: Long): BruksenhetId = BruksenhetId().apply { this.value = value }
