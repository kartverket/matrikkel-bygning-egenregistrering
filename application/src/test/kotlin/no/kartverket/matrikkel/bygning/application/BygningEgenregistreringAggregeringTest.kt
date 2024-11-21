package no.kartverket.matrikkel.bygning.application

import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import assertk.assertions.single
import no.kartverket.matrikkel.bygning.application.models.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetEtasje
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.*
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.withEgenregistrertData
import java.time.Instant
import java.util.*
import kotlin.test.Test

class BygningEgenregistreringAggregeringTest {
    private val defaultBruksenhet = Bruksenhet(
        bruksenhetId = 1L,
        bygningId = 1L,
        totalBruksareal = Multikilde(),
        energikilder = Multikilde(),
        oppvarminger = Multikilde(),
        etasjer = Multikilde(),
    )

    private val defaultBygning = Bygning(
        bygningId = 1L,
        bygningsnummer = 100,
        bruksenheter = listOf(defaultBruksenhet),
        byggeaar = Multikilde(),
        bruksareal = Multikilde(),
        vannforsyning = Multikilde(),
        avlop = Multikilde(),
        etasjer = emptyList(),
    )

    private val defaultBruksenhetRegistrering = BruksenhetRegistrering(
        bruksenhetId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            totalBruksareal = 50.0,
            etasjeRegistreringer = null,
        ),
        byggeaarRegistrering = null,
        energikildeRegistrering = null,
        oppvarmingRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
    )

    private val defaultBygningRegistrering = BygningRegistrering(
        bygningId = 1L,
        bruksenhetRegistreringer = listOf(defaultBruksenhetRegistrering),
    )

    private val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        eier = Foedselsnummer("31129956715"),
        bygningRegistrering = defaultBygningRegistrering,
    )

    @Test
    fun `bruksenhet med to egenregistreringer paa ett felt skal kun gi nyeste feltet`() {
        val laterRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bygningRegistrering = defaultEgenregistrering.bygningRegistrering.copy(
                bruksenhetRegistreringer = listOf(
                    defaultBruksenhetRegistrering.copy(
                        bruksarealRegistrering = BruksarealRegistrering(
                            totalBruksareal = 150.0,
                            etasjeRegistreringer = null,
                        ),
                    ),
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.withEgenregistrertData(listOf(laterRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning.bruksenheter.single().totalBruksareal.egenregistrert?.data).isEqualTo(150.0)
    }

    @Test
    fun `bruksarealregistrering skal sette nyeste av etasje og total bruksareal`() {
        val laterRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bygningRegistrering = defaultEgenregistrering.bygningRegistrering.copy(
                bruksenhetRegistreringer = listOf(
                    defaultBruksenhetRegistrering.copy(
                        bruksarealRegistrering = BruksarealRegistrering(
                            totalBruksareal = null,
                            etasjeRegistreringer = listOf(
                                EtasjeBruksarealRegistrering(
                                    bruksareal = 125.0,
                                    etasjeBetegnelse = Etasjebetegnelse.of(
                                        etasjenummer = Etasjenummer.of(1),
                                        etasjeplanKode = EtasjeplanKode.Hovedetasje,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.withEgenregistrertData(listOf(laterRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning.bruksenheter.single().totalBruksareal.egenregistrert).isNull()

        assertThat(aggregatedBygning).all {
            prop(Bygning::bruksenheter).index(0).all {
                prop(Bruksenhet::etasjer).all {
                    prop(Multikilde<List<BruksenhetEtasje>>::egenregistrert).isNotNull().single().all {
                        prop(BruksenhetEtasje::bruksareal).isNotNull().all {
                            prop(Bruksareal::data).isEqualTo(125.0)
                        }
                        prop(BruksenhetEtasje::etasjeBetegnelse).all {
                            prop(Etasjebetegnelse::etasjenummer).isEqualTo(Etasjenummer.of(1))
                            prop(Etasjebetegnelse::etasjeplanKode).isEqualTo(EtasjeplanKode.Hovedetasje)
                        }
                    }
                }
                prop(Bruksenhet::totalBruksareal).all {
                    prop(Multikilde<Bruksareal>::egenregistrert).isNull()
                }
            }
        }
    }

    @Test
    fun `registrering med tom liste paa listeregistering skal ikke sette felt paa bruksenhet`() {
        val bruksenhet = defaultBruksenhet.withEgenregistrertData(listOf(defaultEgenregistrering))

        assertThat(bruksenhet.oppvarminger).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }
}
