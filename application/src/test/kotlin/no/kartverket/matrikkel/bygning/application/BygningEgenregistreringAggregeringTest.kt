package no.kartverket.matrikkel.bygning.application

import BruksenhetMother.standardBruksenhet
import BruksenhetRegistreringMother.standardBruksenhetRegistrering
import BygningMother.standardBygning
import EgenregistreringMother.standardEgenregistrering
import assertk.all
import assertk.assertThat
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isNullOrEmpty
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
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
import no.kartverket.matrikkel.bygning.application.models.applyEgenregistreringer
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.time.Year
import kotlin.test.Test

class BygningEgenregistreringAggregeringTest {
    private val registeringsTidspunkt = Instant.parse("2024-01-01T12:00:00.00Z")

    @Test
    fun `bruksenhet med to egenregistreringer paa ett felt skal kun gi nyeste kildemateriale felt`() {
        val laterRegistrering =
            standardEgenregistrering()
                .withRegistreringstidspunkt(registeringsTidspunkt.plusSeconds(60))
                .withBruksenhetRegistrering(
                    standardBruksenhetRegistrering()
                        .withByggeaarRegistrering(
                            byggeaar = 2011,
                            kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                        ).build(),
                ).build()

        val aggregatedBygning =
            standardBygning()
                .build()
                .applyEgenregistreringer(listOf(laterRegistrering, standardEgenregistrering().build()))

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
        val firstRegistrering = standardEgenregistrering().build()

        val aggregatedBygning =
            standardBygning()
                .build()
                .applyEgenregistreringer(listOf(standardEgenregistrering().build(), firstRegistrering))

        assertThat(
            aggregatedBygning.bruksenheter
                .single()
                .totaltBruksareal.egenregistrert
                ?.metadata
                ?.prosess,
        ).isEqualTo(ProsessKode.Egenregistrering)
    }

    @Test
    fun `bruksenhet med en ny egenregistrering får kildemateriale fylt`() {
        val firstRegistrering =
            standardEgenregistrering()
                .withRegistreringstidspunkt(registeringsTidspunkt.plusSeconds(60))
                .withBruksenhetRegistrering(
                    standardBruksenhetRegistrering()
                        .withByggeaarRegistrering(byggeaar = 2011, kildemateriale = KildematerialeKode.Byggesaksdokumenter)
                        .build(),
                ).build()

        val aggregatedBygning =
            standardBygning()
                .build()
                .applyEgenregistreringer(listOf(firstRegistrering, standardEgenregistrering().build()))

        assertThat(
            aggregatedBygning.bruksenheter
                .single()
                .byggeaar.egenregistrert
                ?.metadata
                ?.kildemateriale,
        ).isEqualTo(KildematerialeKode.Byggesaksdokumenter)
    }

    @Test
    fun `bruksenhet med to egenregistreringer paa ett felt skal kun gi nyeste feltet`() {
        val laterRegistrering =
            standardEgenregistrering()
                .withRegistreringstidspunkt(registeringsTidspunkt.plusSeconds(60))
                .withBruksenhetRegistrering(
                    bruksenhetRegistrering =
                        standardBruksenhetRegistrering()
                            .withBruksarealRegistrering(
                                bruksareal = 150.0,
                                etasjeRegistreringer = emptyList(),
                                kildemateriale = KildematerialeKode.Byggesaksdokumenter,
                            ).build(),
                ).build()

        val aggregatedBygning =
            standardBygning()
                .build()
                .applyEgenregistreringer(listOf(laterRegistrering, standardEgenregistrering().build()))

        assertThat(
            aggregatedBygning.bruksenheter
                .single()
                .totaltBruksareal.egenregistrert
                ?.data,
        ).isEqualTo(150.0)
    }

    @Test
    fun `bruksarealregistrering skal kun sette total hvis kun total ble registrert nyere enn etasje bruksareal`() {
        val firstRegistrering =
            standardEgenregistrering()
                .withRegistreringstidspunkt(registeringsTidspunkt.minusSeconds(60))
                .withBruksenhetRegistrering(
                    standardBruksenhetRegistrering()
                        .withBruksarealRegistrering(
                            bruksareal = 50.0,
                            etasjeRegistreringer =
                                listOf(
                                    EtasjeBruksarealRegistrering(
                                        bruksareal = 50.0,
                                        etasjebetegnelse =
                                            Etasjebetegnelse.of(
                                                etasjenummer = Etasjenummer.of(1),
                                                etasjeplanKode = EtasjeplanKode.Hovedetasje,
                                            ),
                                    ),
                                ),
                            kildemateriale = KildematerialeKode.Salgsoppgave,
                        ).build(),
                ).build()

        val aggregatedBygning =
            standardBygning()
                .build()
                .applyEgenregistreringer(listOf(standardEgenregistrering().build(), firstRegistrering))

        assertThat(
            aggregatedBygning.bruksenheter
                .single()
                .etasjer.egenregistrert,
        ).isNull()

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
        val bruksenhet =
            standardBruksenhet().build().applyEgenregistreringer(listOf(standardEgenregistrering().build()))

        assertThat(bruksenhet.oppvarming).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }

    @Test
    fun `registrering av listeverdier skal beholde gamle, oppdatere like, og legge inn nye verdier`() {
        val registrering1 =
            standardEgenregistrering()
                .withBruksenhetRegistrering(
                    standardBruksenhetRegistrering()
                        .withEnergikilderRegistreringer(
                            energikilder =
                                listOf(
                                    EnergikildeRegistrering(
                                        EnergikildeKode.Elektrisitet,
                                        kildemateriale = KildematerialeKode.Salgsoppgave,
                                        gyldighetsaar = 2010,
                                        opphoersaar = null,
                                    ),
                                    EnergikildeRegistrering(
                                        EnergikildeKode.AnnenEnergikilde,
                                        kildemateriale = KildematerialeKode.Salgsoppgave,
                                        gyldighetsaar = 2010,
                                        opphoersaar = null,
                                    ),
                                ),
                        ).build(),
                ).build()

        val registrering2 =
            standardEgenregistrering()
                .withRegistreringstidspunkt(registeringsTidspunkt.plusSeconds(10))
                .withBruksenhetRegistrering(
                    standardBruksenhetRegistrering()
                        .withEnergikilderRegistreringer(
                            energikilder =
                                listOf(
                                    EnergikildeRegistrering(
                                        EnergikildeKode.Elektrisitet,
                                        kildemateriale = KildematerialeKode.Salgsoppgave,
                                        gyldighetsaar = 2015,
                                        opphoersaar = null,
                                    ),
                                    EnergikildeRegistrering(
                                        EnergikildeKode.Fjernvarme,
                                        kildemateriale = KildematerialeKode.Salgsoppgave,
                                        gyldighetsaar = 2020,
                                        opphoersaar = null,
                                    ),
                                ),
                        ).build(),
                ).build()

        val result1 = standardBruksenhet().build().applyEgenregistreringer(emptyList())
        val result2 = standardBruksenhet().build().applyEgenregistreringer(listOf(registrering1))
        val result3 = standardBruksenhet().build().applyEgenregistreringer(listOf(registrering1, registrering2))

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
        val registreringUtenOpphoer =
            standardEgenregistrering()
                .withBruksenhetRegistrering(
                    bruksenhetRegistrering =
                        standardBruksenhetRegistrering()
                            .withAvlopRegistrering(
                                AvlopRegistrering(
                                    avlop = AvlopKode.OffentligKloakk,
                                    kildemateriale = KildematerialeKode.Salgsoppgave,
                                    gyldighetsaar = 2010,
                                    opphoersaar = null,
                                ),
                            ).withEnergikilderRegistreringer(
                                listOf(
                                    EnergikildeRegistrering(
                                        energikilde = EnergikildeKode.Elektrisitet,
                                        kildemateriale = KildematerialeKode.Salgsoppgave,
                                        gyldighetsaar = 2010,
                                        opphoersaar = null,
                                    ),
                                ),
                            ).build(),
                ).build()

        val registreringMedOpphoer =
            standardEgenregistrering()
                .withRegistreringstidspunkt(registeringsTidspunkt.plusSeconds(10))
                .withBruksenhetRegistrering(
                    standardBruksenhetRegistrering()
                        .withAvlopRegistrering(
                            AvlopRegistrering(
                                avlop = AvlopKode.OffentligKloakk,
                                kildemateriale = KildematerialeKode.Salgsoppgave,
                                gyldighetsaar = 2010,
                                opphoersaar = 2015,
                            ),
                        ).withEnergikilderRegistreringer(
                            listOf(
                                EnergikildeRegistrering(
                                    EnergikildeKode.Elektrisitet,
                                    kildemateriale = KildematerialeKode.Salgsoppgave,
                                    gyldighetsaar = 2010,
                                    opphoersaar = 2015,
                                ),
                            ),
                        ).build(),
                ).build()

        val bruksenhet =
            standardBruksenhet().build().applyEgenregistreringer(
                listOf(
                    registreringUtenOpphoer,
                    registreringMedOpphoer,
                ),
            )

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
        val registreringMedOpphoer =
            standardEgenregistrering()
                .withBruksenhetRegistrering(
                    bruksenhetRegistrering =
                        standardBruksenhetRegistrering()
                            .withAvlopRegistrering(
                                AvlopRegistrering(
                                    avlop = AvlopKode.OffentligKloakk,
                                    kildemateriale = KildematerialeKode.Salgsoppgave,
                                    gyldighetsaar = 2010,
                                    opphoersaar = 2015,
                                ),
                            ).withEnergikilderRegistreringer(
                                listOf(
                                    EnergikildeRegistrering(
                                        EnergikildeKode.Elektrisitet,
                                        kildemateriale = KildematerialeKode.Salgsoppgave,
                                        gyldighetsaar = 2010,
                                        opphoersaar = 2015,
                                    ),
                                ),
                            ).build(),
                ).build()

        val registreringUtenOpphoer =
            registreringMedOpphoer.copy(
                registreringstidspunkt = registeringsTidspunkt.plusSeconds(10),
                bruksenhetRegistrering =
                    standardBruksenhetRegistrering()
                        .withAvlopRegistrering(
                            AvlopRegistrering(
                                avlop = AvlopKode.OffentligKloakk,
                                kildemateriale = KildematerialeKode.Salgsoppgave,
                                gyldighetsaar = 2016,
                                opphoersaar = null,
                            ),
                        ).withEnergikilderRegistreringer(
                            listOf(
                                EnergikildeRegistrering(
                                    energikilde = EnergikildeKode.Elektrisitet,
                                    kildemateriale = KildematerialeKode.Salgsoppgave,
                                    gyldighetsaar = 2016,
                                    opphoersaar = null,
                                ),
                            ),
                        ).build(),
            )

        val bruksenhet =
            standardBruksenhet()
                .build()
                .applyEgenregistreringer(listOf(registreringUtenOpphoer, registreringMedOpphoer))

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
