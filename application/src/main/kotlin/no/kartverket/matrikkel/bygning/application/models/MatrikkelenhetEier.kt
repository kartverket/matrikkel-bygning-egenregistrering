package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.ids.MatrikkelenhetBubbleId

data class MatrikkelenhetEier(
    val ultimatEier: Boolean,
    val matrikkelenhetBubbleId: MatrikkelenhetBubbleId,
    val eier: Foedselsnummer,
)
