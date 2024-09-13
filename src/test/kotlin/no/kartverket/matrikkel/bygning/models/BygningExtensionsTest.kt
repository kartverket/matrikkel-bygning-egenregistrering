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

    val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        bygningRegistrering = BygningRegistrering(
            bygningId = 1L,
            bruksarealRegistrering = BruksarealRegistrering(
                bruksareal = 125.0,
            ),
            byggeaarRegistrering = null,
            vannforsyningRegistrering = null,
            avlopRegistrering = null,
            bruksenhetRegistreringer = emptyList(),
        ),
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
}
