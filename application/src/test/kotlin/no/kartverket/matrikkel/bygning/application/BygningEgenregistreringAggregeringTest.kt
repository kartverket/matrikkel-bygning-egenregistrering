package no.kartverket.matrikkel.bygning.application

import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isNullOrEmpty
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.applyEgenregistreringer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.time.Year
import java.util.*
import kotlin.test.Test

class BygningEgenregistreringAggregeringTest {
    private val bygningId = BygningId("00000000-0000-0000-0000-000000000001")

    private val defaultBruksenhet = Bruksenhet(
        id = BruksenhetId("00000000-0000-0000-0000-000000000002"),
        bruksenhetBubbleId = BruksenhetBubbleId(1L),
    )

    private val defaultBygning = Bygning(
        id = bygningId,
        bygningBubbleId = BygningBubbleId(1L),
        bygningsnummer = 100,
        bruksenheter = listOf(defaultBruksenhet),
        etasjer = emptyList(),
    )

    private val defaultBruksenhetRegistrering = BruksenhetRegistrering(
        bruksenhetBubbleId = BruksenhetBubbleId(1L),
        bruksarealRegistrering = BruksarealRegistrering(
            totaltBruksareal = 50.0,
            etasjeRegistreringer = null,
            kildemateriale = KildematerialeKode.Salgsoppgave,
        ),
        byggeaarRegistrering = null,
        energikildeRegistrering = null,
        oppvarmingRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
    )

    private val defaultEgenregistrering = Egenregistrering(
        id = EgenregistreringId(UUID.randomUUID()),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        eier = Foedselsnummer("66860475309"),
        bruksenhetRegistrering = defaultBruksenhetRegistrering,
        prosess = ProsessKode.Egenregistrering,
    )

    private val bruksenhetRegistreringMedKildematerialeKode = BruksenhetRegistrering(
        bruksenhetBubbleId = BruksenhetBubbleId(1L),
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
            id = EgenregistreringId(UUID.randomUUID()),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bruksenhetRegistrering = bruksenhetRegistreringMedKildematerialeKode.copy(
                byggeaarRegistrering = ByggeaarRegistrering(
                    byggeaar = 2011,
                    kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.applyEgenregistreringer(listOf(laterRegistrering, defaultEgenregistrering))

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
    fun `bruksenhet med en ny egenregistrering får prosess fylt`() {
        val firstRegistrering = defaultEgenregistrering

        val aggregatedBygning = defaultBygning.applyEgenregistreringer(listOf(defaultEgenregistrering, firstRegistrering))

        assertThat(aggregatedBygning.bruksenheter.single().totaltBruksareal.egenregistrert?.metadata?.prosess)
            .isEqualTo(ProsessKode.Egenregistrering)
    }

    @Test
    fun `bruksenhet med en ny egenregistrering får kildemateriale fylt`() {
        val firstRegistrering = defaultEgenregistrering.copy(
            id = EgenregistreringId(UUID.randomUUID()),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bruksenhetRegistrering = bruksenhetRegistreringMedKildematerialeKode.copy(
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
            id = EgenregistreringId(UUID.randomUUID()),
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
            id = EgenregistreringId(UUID.randomUUID()),
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

        assertThat(bruksenhet.oppvarming).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }

    @Test
    fun `registrering av listeverdier skal beholde gamle, oppdatere like, og legge inn nye verdier`() {
        val registrering1 = defaultEgenregistrering.copy(
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                energikildeRegistrering = listOf(
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.Elektrisitet,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2010,
                        opphoersaar = null,
                    ),
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.AnnenEnergikilde,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2010,
                        opphoersaar = null,
                    ),
                ),
            ),
        )

        val registrering2 = defaultEgenregistrering.copy(
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(10),
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                energikildeRegistrering = listOf(
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.Elektrisitet,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2015,
                        opphoersaar = null,
                    ),
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.Fjernvarme,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2020,
                        opphoersaar = null,
                    ),
                ),
            ),
        )

        val result1 = defaultBruksenhet.applyEgenregistreringer(emptyList())
        val result2 = defaultBruksenhet.applyEgenregistreringer(listOf(registrering1))
        val result3 = defaultBruksenhet.applyEgenregistreringer(listOf(registrering1, registrering2))

        assertThat(result1).all {
            prop(Bruksenhet::energikilder).all {
                prop(Multikilde<List<Felt.Energikilde>>::egenregistrert).isNull()
            }
        }

        assertThat(result2).all {
            prop(Bruksenhet::energikilder).all {
                prop(Multikilde<List<Felt.Energikilde>>::egenregistrert).isNotNull().all {
                    index(0).all {
                        prop(Felt.Energikilde::data).isEqualTo(EnergikildeKode.Elektrisitet)
                        prop(Felt.Energikilde::metadata).all {
                            prop(RegisterMetadata::gyldighetsperiode).all {
                                prop(Gyldighetsperiode::gyldighetsaar).isEqualTo(Year.of(2010))
                            }
                        }
                    }
                    index(1).all {
                        prop(Felt.Energikilde::data).isEqualTo(EnergikildeKode.AnnenEnergikilde)
                        prop(Felt.Energikilde::metadata).all {
                            prop(RegisterMetadata::gyldighetsperiode).all {
                                prop(Gyldighetsperiode::gyldighetsaar).isEqualTo(Year.of(2010))
                            }
                        }
                    }
                }
            }
        }

        assertThat(result3).all {
            prop(Bruksenhet::energikilder).all {
                prop(Multikilde<List<Felt.Energikilde>>::egenregistrert).isNotNull().all {
                    index(0).all {
                        prop(Felt.Energikilde::data).isEqualTo(EnergikildeKode.Elektrisitet)
                        prop(Felt.Energikilde::metadata).all {
                            prop(RegisterMetadata::gyldighetsperiode).all {
                                prop(Gyldighetsperiode::gyldighetsaar).isEqualTo(Year.of(2015))
                            }
                        }
                    }
                    index(1).all {
                        prop(Felt.Energikilde::data).isEqualTo(EnergikildeKode.AnnenEnergikilde)
                        prop(Felt.Energikilde::metadata).all {
                            prop(RegisterMetadata::gyldighetsperiode).all {
                                prop(Gyldighetsperiode::gyldighetsaar).isEqualTo(Year.of(2010))
                            }
                        }
                    }
                    index(2).all {
                        prop(Felt.Energikilde::data).isEqualTo(EnergikildeKode.Fjernvarme)
                        prop(Felt.Energikilde::metadata).all {
                            prop(RegisterMetadata::gyldighetsperiode).all {
                                prop(Gyldighetsperiode::gyldighetsaar).isEqualTo(Year.of(2020))
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `registrering av opphoersdato skal gjøre at verdien ikke returneres`() {
        val registreringUtenOpphoer = defaultEgenregistrering.copy(
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                avlopRegistrering = AvlopRegistrering(
                    avlop = AvlopKode.OffentligKloakk,
                    kildemateriale = KildematerialeKode.Salgsoppgave,
                    gyldighetsaar = 2010,
                    opphoersaar = null,
                ),
                energikildeRegistrering = listOf(
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.Elektrisitet,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2010,
                        opphoersaar = null,
                    ),
                ),
            ),
        )

        val registreringMedOpphoer = defaultEgenregistrering.copy(
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(10),
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                avlopRegistrering = AvlopRegistrering(
                    avlop = AvlopKode.OffentligKloakk,
                    kildemateriale = KildematerialeKode.Salgsoppgave,
                    gyldighetsaar = 2010,
                    opphoersaar = 2015,
                ),
                energikildeRegistrering = listOf(
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.Elektrisitet,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2010,
                        opphoersaar = 2015,
                    ),
                ),
            ),
        )

        val bruksenhet = defaultBruksenhet.applyEgenregistreringer(listOf(registreringUtenOpphoer, registreringMedOpphoer))

        assertThat(bruksenhet).all {
            prop(Bruksenhet::energikilder).all {
                prop(Multikilde<List<Felt.Energikilde>>::egenregistrert).isNullOrEmpty()
            }
            prop(Bruksenhet::avlop).all {
                prop(Multikilde<Felt.Avlop>::egenregistrert).isNull()
            }
        }
    }

    @Test
    fun `registrering uten opphoersdato der det tidligere var opphørt skal returnere data`() {
        val registreringMedOpphoer = defaultEgenregistrering.copy(
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                avlopRegistrering = AvlopRegistrering(
                    avlop = AvlopKode.OffentligKloakk,
                    kildemateriale = KildematerialeKode.Salgsoppgave,
                    gyldighetsaar = 2010,
                    opphoersaar = 2015,
                ),
                energikildeRegistrering = listOf(
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.Elektrisitet,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2010,
                        opphoersaar = 2015,
                    ),
                ),
            ),
        )

        val registreringUtenOpphoer = defaultEgenregistrering.copy(
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(10),
            bruksenhetRegistrering = defaultBruksenhetRegistrering.copy(
                avlopRegistrering = AvlopRegistrering(
                    avlop = AvlopKode.OffentligKloakk,
                    kildemateriale = KildematerialeKode.Salgsoppgave,
                    gyldighetsaar = 2016,
                    opphoersaar = null,
                ),
                energikildeRegistrering = listOf(
                    EnergikildeRegistrering(
                        energikilde = EnergikildeKode.Elektrisitet,
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        gyldighetsaar = 2016,
                        opphoersaar = null,
                    ),
                ),
            ),
        )

        val bruksenhet = defaultBruksenhet.applyEgenregistreringer(listOf(registreringUtenOpphoer, registreringMedOpphoer))

        assertThat(bruksenhet).all {
            prop(Bruksenhet::energikilder).all {
                prop(Multikilde<List<Felt.Energikilde>>::egenregistrert).isNotNull().all {
                    index(0).all {
                        prop(Felt.Energikilde::metadata).all {
                            prop(RegisterMetadata::gyldighetsperiode).all {
                                prop(Gyldighetsperiode::gyldighetsaar).isEqualTo(Year.of(2016))
                                prop(Gyldighetsperiode::opphoersaar).isNull()
                            }
                        }
                    }
                }
            }
            prop(Bruksenhet::avlop).all {
                prop(Multikilde<Felt.Avlop>::egenregistrert).isNotNull().all {
                    prop(Felt.Avlop::metadata).all {
                        prop(RegisterMetadata::gyldighetsperiode).all {
                            prop(Gyldighetsperiode::gyldighetsaar).isEqualTo(Year.of(2016))
                            prop(Gyldighetsperiode::opphoersaar).isNull()
                        }
                    }
                }
            }
        }
    }
}
