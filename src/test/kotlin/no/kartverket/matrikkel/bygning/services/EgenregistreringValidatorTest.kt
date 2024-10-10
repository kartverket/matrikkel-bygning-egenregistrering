package no.kartverket.matrikkel.bygning.services

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class EgenregistreringValidatorTest {
    private val baseBygning = Bygning(
        bygningId = 1L, bygningsnummer = 100L, bruksenheter = listOf(
            Bruksenhet(
                bruksenhetId = 1L, bygningId = 1L
            )
        )
    )

    private val baseEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        bygningRegistrering = BygningRegistrering(
            bygningId = 1L,
            bruksarealRegistrering = null,
            byggeaarRegistrering = null,
            vannforsyningRegistrering = null,
            avlopRegistrering = null,
            bruksenhetRegistreringer = emptyList()
        )
    )

    @Test
    fun `egenregistrering med flere registreringer paa samme bruksenhet skal feile`() {
        val validationErrors = EgenregistreringValidator.validateEgenregistrering(
            baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetId = 1L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                        ),
                        BruksenhetRegistrering(
                            bruksenhetId = 1L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                        ),
                    )
                )
            ), baseBygning
        )

        assertThat(validationErrors).hasSize(1)
        assertThat(validationErrors.first().detail).contains("flere registreringer")
    }

    @Test
    fun `egenregistrering med bruksenhetregistrering hvor bruksenhet ikke finnes paa bygning skal feile`() {
        val validationErrors = EgenregistreringValidator.validateEgenregistrering(
            baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetId = 3L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                        ),
                    )
                )
            ), baseBygning
        )

        assertThat(validationErrors).hasSize(1)
        assertThat(validationErrors.first().detail).contains("finnes ikke")
    }
}
