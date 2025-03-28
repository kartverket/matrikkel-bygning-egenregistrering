package no.kartverket.matrikkel.bygning.v1.common

import assertk.Assert
import assertk.assertions.isBetween
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.RegisterMetadataInternResponse
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.AvlopRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.BruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.ByggeaarRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EnergikildeRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBetegnelseRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.EtasjeBruksarealRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.OppvarmingRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.VannforsyningRegistreringRequest
import java.time.Instant

internal fun EgenregistreringRequest.Companion.gyldigRequest(bruksenhetId: Long = 1L) =
    EgenregistreringRequest(
        bruksenhetId = bruksenhetId,
        bruksarealRegistrering =
            BruksarealRegistreringRequest(
                totaltBruksareal = 125.0,
                etasjeRegistreringer = null,
                kildemateriale = KildematerialeKode.Salgsoppgave,
            ),
        byggeaarRegistrering = ByggeaarRegistreringRequest(2010, KildematerialeKode.Selvrapportert),
        vannforsyningRegistrering =
            VannforsyningRegistreringRequest(
                vannforsyning = VannforsyningKode.OffentligVannverk,
                kildemateriale = KildematerialeKode.Salgsoppgave,
                gyldighetsaar = 2010,
                opphoersaar = null,
            ),
        avlopRegistrering =
            AvlopRegistreringRequest(
                avlop = AvlopKode.OffentligKloakk,
                kildemateriale = KildematerialeKode.Selvrapportert,
            ),
        energikildeRegistrering =
            listOf(
                EnergikildeRegistreringRequest(
                    energikilde = EnergikildeKode.Elektrisitet,
                    kildemateriale = KildematerialeKode.Selvrapportert,
                ),
            ),
        oppvarmingRegistrering =
            listOf(
                OppvarmingRegistreringRequest(
                    oppvarming = OppvarmingKode.Elektrisk,
                    kildemateriale = KildematerialeKode.Selvrapportert,
                ),
            ),
    )

internal fun EgenregistreringRequest.Companion.ugyldigRequestKunBruksarealPerEtasje() =
    EgenregistreringRequest(
        bruksenhetId = 1L,
        byggeaarRegistrering = null,
        bruksarealRegistrering =
            BruksarealRegistreringRequest(
                totaltBruksareal = 50.0,
                etasjeRegistreringer =
                    listOf(
                        EtasjeBruksarealRegistreringRequest(
                            bruksareal = 125.0,
                            EtasjeBetegnelseRequest(
                                etasjeplanKode = "H",
                                etasjenummer = 1,
                            ),
                        ),
                    ),
                kildemateriale = KildematerialeKode.Salgsoppgave,
            ),
        energikildeRegistrering = null,
        oppvarmingRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
    )

internal fun Assert<RegisterMetadataInternResponse>.hasRegistreringstidspunktWithinThreshold(now: Instant): () -> Unit =
    {
        prop(RegisterMetadataInternResponse::registreringstidspunkt).isBetween(now, now.plusSeconds(1))
    }
