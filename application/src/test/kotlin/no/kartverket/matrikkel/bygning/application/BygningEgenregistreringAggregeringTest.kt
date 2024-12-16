package no.kartverket.matrikkel.bygning.application

import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetEtasje
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.withEgenregistrertData
import java.time.Instant
import java.util.*
import kotlin.test.Test

class BygningEgenregistreringAggregeringTest {
    private val defaultBruksenhet = Bruksenhet(
        bruksenhetId = 1L,
        bygningId = 1L,
    )

    private val defaultBygning = Bygning(
        bygningId = 1L,
        bygningsnummer = 100,
        bruksenheter = listOf(defaultBruksenhet),
        etasjer = emptyList(),
    )

    private val defaultBruksenhetRegistrering = BruksenhetRegistrering(
        bruksenhetId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            totaltBruksareal = 50.0,
            etasjeRegistreringer = null,
            kildemateriale = null,
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

    private val bruksenhetRegistreringMedKildematerialeKode = BruksenhetRegistrering(
        bruksenhetId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            totaltBruksareal = 50.0,
            etasjeRegistreringer = null,
            kildemateriale = KildematerialeKode.AnnenDokumentasjon,
        ),
        byggeaarRegistrering = ByggeaarRegistrering(2010, KildematerialeKode.Selvrapportert),
        energikildeRegistrering = null,
        oppvarmingRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
    )

    @Test
    fun `bruksenhet med to egenregistreringer paa ett felt skal kun gi nyeste kildemateriale felt`() {
        val laterRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bygningRegistrering = defaultEgenregistrering.bygningRegistrering.copy(
                bruksenhetRegistreringer = listOf(
                    bruksenhetRegistreringMedKildematerialeKode.copy(
                        byggeaarRegistrering = ByggeaarRegistrering(
                            byggeaar = 2011,
                            kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                        ),
                    ),
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.withEgenregistrertData(listOf(laterRegistrering, defaultEgenregistrering))

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
    fun `bruksenhet med to egenregistreringer paa ett felt skal kun gi nyeste feltet`() {
        val laterRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bygningRegistrering = defaultEgenregistrering.bygningRegistrering.copy(
                bruksenhetRegistreringer = listOf(
                    defaultBruksenhetRegistrering.copy(
                        bruksarealRegistrering = BruksarealRegistrering(
                            totaltBruksareal = 150.0,
                            etasjeRegistreringer = null,
                            kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                        ),
                    ),
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.withEgenregistrertData(listOf(laterRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning.bruksenheter.single().totaltBruksareal.egenregistrert?.data)
            .isEqualTo(150.0)
        assertThat(aggregatedBygning.bruksenheter.single().totaltBruksareal.egenregistrert?.metadata?.kildemateriale)
            .isEqualTo(KildematerialeKode.Byggesaksdokumenter)
    }

    @Test
    fun `bruksarealregistrering skal kun sette total hvis kun total ble registrert nyere enn etasje bruksareal`() {
        val firstRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.minusSeconds(60),
            bygningRegistrering = defaultEgenregistrering.bygningRegistrering.copy(
                bruksenhetRegistreringer = listOf(
                    defaultBruksenhetRegistrering.copy(
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
                            kildemateriale = null,
                        ),

                        ),
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.withEgenregistrertData(listOf(defaultEgenregistrering, firstRegistrering))

        assertThat(aggregatedBygning.bruksenheter.single().etasjer.egenregistrert).isNull()

        assertThat(aggregatedBygning).all {
            prop(Bygning::bruksenheter).index(0).all {
                prop(Bruksenhet::etasjer).all {
                    prop(Multikilde<List<BruksenhetEtasje>>::egenregistrert).isNull()
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
        val bruksenhet = defaultBruksenhet.withEgenregistrertData(listOf(defaultEgenregistrering))

        assertThat(bruksenhet.oppvarminger).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }
}
