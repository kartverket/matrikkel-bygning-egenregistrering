package no.kartverket.matrikkel.bygning.v1.common

import assertk.Assert
import assertk.assertions.isBetween
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.bygning.RegisterMetadataResponse
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.AvlopRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.BruksenhetRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.ByggeaarRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.EnergikildeRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.OppvarmingRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.egenregistrering.VannforsyningRegistreringRequest
import java.time.Instant

internal fun EgenregistreringRequest.Companion.validEgenregistrering() = EgenregistreringRequest(
    bygningId = 1L,
    eier = "31129956715",
    bruksenhetRegistreringer = listOf(
        BruksenhetRegistreringRequest(
            bruksenhetId = 1L,
            bruksarealRegistrering = BruksarealRegistreringRequest(
                totalBruksareal = 125.0,
                etasjeRegistreringer = null
            ),
            byggeaarRegistrering = ByggeaarRegistreringRequest(2010),
            vannforsyningRegistrering = VannforsyningRegistreringRequest(
                VannforsyningKode.OffentligVannverk,
            ),
            avlopRegistrering = AvlopRegistreringRequest(
                avlop = AvlopKode.OffentligKloakk,
            ),
            energikildeRegistrering = EnergikildeRegistreringRequest(
                listOf(EnergikildeKode.Elektrisitet),
            ),
            oppvarmingRegistrering = OppvarmingRegistreringRequest(
                listOf(OppvarmingKode.Elektrisk),
            ),
        ),
    ),
)

internal fun Assert<RegisterMetadataResponse>.hasRegistreringstidspunktWithinThreshold(now: Instant): () -> Unit {
    return {
        prop(RegisterMetadataResponse::registreringstidspunkt).isBetween(now, now.plusSeconds(1))
    }
}
