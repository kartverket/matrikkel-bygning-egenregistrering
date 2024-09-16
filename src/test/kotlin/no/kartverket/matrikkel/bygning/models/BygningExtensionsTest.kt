package no.kartverket.matrikkel.bygning.models

import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class BygningExtensionsTest {
    val defaultBygning = Bygning(
        bygningId = 1L,
        bygningsnummer = 100,
        bruksenheter = emptyList(),
        byggeaar = null,
        bruksareal = null,
        vannforsyning = null,
        avlop = null,
    )

    val defaultBruksenhet = Bruksenhet(
        bruksenhetId = 1L,
        bygningId = 1L,
        bruksareal = null,
        energikilder = emptyList(),
        oppvarminger = emptyList(),
    )

    val defaultBruksenhetRegistrering = BruksenhetRegistrering(
        bruksenhetId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            bruksareal = 50.0
        ),
        energikildeRegistrering = null,
        oppvarmingRegistrering = null
    )

    val defaultBygningRegistrering = BygningRegistrering(
        bygningId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            bruksareal = 125.0,
        ),
        byggeaarRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
        bruksenhetRegistreringer = listOf(defaultBruksenhetRegistrering),
    )

    val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        bygningRegistrering = defaultBygningRegistrering
    )

    @Test
    fun `bygning med to egenregistreringer paa ett felt skal kun gi nyeste feltet`() {
        val laterRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bygningRegistrering = defaultEgenregistrering.bygningRegistrering.copy(
                bruksarealRegistrering = BruksarealRegistrering(
                    bruksareal = 150.0,
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.withEgenregistrertData(listOf(laterRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning.bruksareal?.data).isEqualTo(150.0)
    }

    @Test
    fun `registrering med tom liste paa listeregistering skal ikke sette felt paa bruksenhet`() {
        val bruksenhet = defaultBruksenhet.withEgenregistrertData(listOf(defaultEgenregistrering))

        assertThat(bruksenhet.oppvarminger).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }
}
