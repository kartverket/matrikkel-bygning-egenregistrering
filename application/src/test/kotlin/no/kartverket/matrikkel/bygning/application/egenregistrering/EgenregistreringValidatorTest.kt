package no.kartverket.matrikkel.bygning.application.egenregistrering

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isTrue
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class EgenregistreringValidatorTest {
    private val baseBygning = Bygning(
        bygningId = 1L, bygningsnummer = 100L,
        bruksenheter = listOf(
            Bruksenhet(
                bruksenhetId = 1L, bygningId = 1L, etasjer = Multikilde(),
            ),
        ),
        etasjer = emptyList(),
    )

    private val baseEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        eier = Foedselsnummer("31129956715"),
        bygningRegistrering = BygningRegistrering(
            bygningId = 1L,
            bruksenhetRegistreringer = emptyList(),
        ),
    )

    @Test
    fun `egenregistrering med flere registreringer paa samme bruksenhet skal feile`() {
        val validationResult = EgenregistreringValidator.validateEgenregistrering(
            baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetId = 1L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                        BruksenhetRegistrering(
                            bruksenhetId = 1L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                    ),
                ),
            ),
            baseBygning,
        )

        assertThat(validationResult.isErr).isTrue()
        assertThat(validationResult.error.errors).hasSize(1)
        assertThat(validationResult.error.errors.first().message).contains("flere registreringer")
    }

    @Test
    fun `egenregistrering med bruksenhetregistrering hvor bruksenhet ikke finnes paa bygning skal feile`() {
        val validationResult = EgenregistreringValidator.validateEgenregistrering(
            baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetId = 3L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                    ),
                ),
            ),
            baseBygning,
        )

        assertThat(validationResult.isErr).isTrue()
        assertThat(validationResult.error.errors).hasSize(1)
        assertThat(validationResult.error.errors.first().message).contains("finnes ikke")
    }

    @Test
    fun `egenregistrering med bruksarealregistrering med baade total og etasjeregistrering skal feile`() {
        val validationResult = EgenregistreringValidator.validateEgenregistrering(
            baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetId = 1L,
                            bruksarealRegistrering = BruksarealRegistrering(
                                totaltBruksareal = 125.0,
                                etasjeRegistreringer = listOf(
                                    EtasjeBruksarealRegistrering(
                                        bruksareal = 125.0,
                                        etasjebetegnelse = Etasjebetegnelse.of(
                                            etasjeplanKode = EtasjeplanKode.Hovedetasje,
                                            etasjenummer = Etasjenummer.of(1),
                                        ),
                                    ),
                                ),
                            ),
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                        ),
                    ),
                ),
            ),
            baseBygning,
        )

        assertThat(validationResult.isErr).isTrue()
        assertThat(validationResult.error.errors).hasSize(1)
        assertThat(validationResult.error.errors.first().message).contains("totalt bruksareal og areal per etasje")
    }
}
