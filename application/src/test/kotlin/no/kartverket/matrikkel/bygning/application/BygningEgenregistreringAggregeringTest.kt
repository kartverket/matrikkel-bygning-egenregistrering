package no.kartverket.matrikkel.bygning.application

import BruksenhetBuilder
import BruksenhetMother
import BruksenhetRegistreringBuilder1
import BygningBuilder
import EgenregistreringBuilder
import StandardObjectBuilder
import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.applyEgenregistreringer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.util.*
import kotlin.test.Test

class BygningEgenregistreringAggregeringTest {
    private val standardObject = StandardObjectBuilder.standardBygning

    private val defaultBruksenhet = BruksenhetBuilder()
        .id(BruksenhetId("00000000-0000-0000-0000-000000000002"))
        .bruksenhetBubbleId(BruksenhetBubbleId(1L))
        .build()

    private val defaultBygning = BygningBuilder()
        .id(BygningId("00000000-0000-0000-0000-000000000001"))
        .bygningBubbleId(BygningBubbleId(1L))
        .bygningsnummer(100)
        .bruksenheter(listOf(defaultBruksenhet))
        .etasjer(emptyList())
        .build()

    val defaultBruksarealRegistrering = BruksarealRegistrering(
        totaltBruksareal = 50.0,
        etasjeRegistreringer = null,
        kildemateriale = KildematerialeKode.Salgsoppgave,
    )

    private val defaultBruksenhetRegistrering = BruksenhetRegistreringBuilder1()
        .bruksenhetBubbleId(BruksenhetBubbleId(1L))
        .bruksareal(
            defaultBruksarealRegistrering,
        ).build()

    val defaultEgenregistrering = EgenregistreringBuilder()
        .id(UUID.randomUUID())
        .eier(Foedselsnummer("66860475309"))
        .registreringstidspunkt(Instant.parse("2024-01-01T12:00:00.00Z"))
        .prosess(ProsessKode.Egenregistrering)
        .bruksenhet(
            BruksenhetMother.standardBruksenhet,
        )
        .build()

    @Test
    fun `bruksenhet med to egenregistreringer paa ett felt skal kun gi nyeste kildemateriale felt`() {
        val laterRegistrering = EgenregistreringBuilder()
            .eier(Foedselsnummer("66860475309"))
            .bruksenhet(
                BruksenhetRegistreringBuilder1()
                    .byggeaar(
                        ByggeaarRegistrering(
                            byggeaar = 2011,
                            kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                        ),
                    ).build(),
                ).build()

        //her brukes en predefinert standarObject som
        val aggregatedBygning = standardObject.applyEgenregistreringer(listOf(laterRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning).all {
            prop(Bygning::bruksenheter).index(0).all {
                prop(Bruksenhet::byggeaar).all {
                    prop(Multikilde<Byggeaar>::egenregistrert).isNotNull().all {
                        prop(Byggeaar::data).isEqualTo(2011)
                        prop(Byggeaar::metadata).all {
                            prop(RegisterMetadata::kildemateriale).isEqualTo(KildematerialeKode.Byggesaksdokumenter)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `bruksenhet med en ny egenregistring får prosess fylt`() {
        val firstRegistrering = defaultEgenregistrering

        val aggregatedBygning = defaultBygning.applyEgenregistreringer(listOf(defaultEgenregistrering, firstRegistrering))

        assertThat(aggregatedBygning.bruksenheter.single().totaltBruksareal.egenregistrert?.metadata?.prosess)
            .isEqualTo(ProsessKode.Egenregistrering)
    }

    @Test
    fun `bruksenhet med en ny egenregistring får kildemateriale fylt`() {
        val firstRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                byggeaarRegistrering = ByggeaarRegistrering(
                    byggeaar = 2011,
                    kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                ),
            ),
        )

        val aggregatedBygning =
            defaultBygning.applyEgenregistreringer(listOf(firstRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning.bruksenheter.single().byggeaar.egenregistrert?.metadata?.kildemateriale)
            .isEqualTo(KildematerialeKode.Byggesaksdokumenter)
    }

    @Test
    fun `bruksenhet med to egenregistreringer paa ett felt skal kun gi nyeste feltet`() {
        val laterRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                bruksarealRegistrering = BruksarealRegistrering(
                    totaltBruksareal = 150.0,
                    etasjeRegistreringer = null,
                    kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.applyEgenregistreringer(listOf(laterRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning.bruksenheter.single().totaltBruksareal.egenregistrert?.data)
            .isEqualTo(150.0)
    }

    @Test
    fun `bruksarealregistrering skal kun sette total hvis kun total ble registrert nyere enn etasje bruksareal`() {
        val firstRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.minusSeconds(60),
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                bruksarealRegistrering = BruksarealRegistrering(
                    totaltBruksareal = 50.0,
                    etasjeRegistreringer = listOf(
                        EtasjeBruksarealRegistrering(
                            bruksareal = 50.0,
                            etasjebetegnelse = Etasjebetegnelse.of(
                                etasjenummer = Etasjenummer.of(1),
                                etasjeplanKode = EtasjeplanKode.Hovedetasje,
                            ),
                        ),
                    ),
                    kildemateriale = KildematerialeKode.Salgsoppgave,
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.applyEgenregistreringer(listOf(defaultEgenregistrering, firstRegistrering))

        assertThat(aggregatedBygning.bruksenheter.single().etasjer.egenregistrert).isNull()

        assertThat(aggregatedBygning).all {
            prop(Bygning::bruksenheter).index(0).all {
                prop(Bruksenhet::etasjer).all {
                    prop(Multikilde<BruksenhetEtasjer>::egenregistrert).isNull()
                }
                prop(Bruksenhet::totaltBruksareal).all {
                    prop(Multikilde<Bruksareal>::egenregistrert).isNotNull().all {
                        prop(Bruksareal::data).isEqualTo(50.0)
                    }
                }
            }
        }
    }

    @Test
    fun `registrering med tom liste paa listeregistering skal ikke sette felt paa bruksenhet`() {
        val bruksenhet = defaultBruksenhet.applyEgenregistreringer(listOf(defaultEgenregistrering))

        assertThat(bruksenhet.oppvarminger).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }
}
