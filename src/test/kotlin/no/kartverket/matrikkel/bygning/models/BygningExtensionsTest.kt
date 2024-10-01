package no.kartverket.matrikkel.bygning.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import no.kartverket.matrikkel.bygning.models.valuetype.Foedselsnummer
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class BygningExtensionsTest {

    private val defaultBygning = Bygning(
        bygningId = 1L,
        bygningsnummer = 100,
        bruksenheter = emptyList(),
        byggeaar = Multikilde(),
        bruksareal = Multikilde(),
        vannforsyning = Multikilde(),
        avlop = Multikilde(),
    )

    private val defaultBruksenhet = Bruksenhet(
        bruksenhetId = 1L,
        bygningId = 1L,
        bruksareal = Multikilde(),
        energikilder = Multikilde(),
        oppvarminger = Multikilde(),
    )

    private val defaultBruksenhetRegistrering = BruksenhetRegistrering(
        bruksenhetId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            bruksareal = 50.0
        ),
        energikildeRegistrering = null,
        oppvarmingRegistrering = null
    )

    private val defaultBygningRegistrering = BygningRegistrering(
        bygningId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            bruksareal = 125.0,
        ),
        byggeaarRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
        bruksenhetRegistreringer = listOf(defaultBruksenhetRegistrering),
    )

    private val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        registrerer = Foedselsnummer("01010154321"),
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

        assertThat(aggregatedBygning.bruksareal.egenregistrert?.data).isEqualTo(150.0)
    }

    @Test
    fun `registrering med tom liste paa listeregistering skal ikke sette felt paa bruksenhet`() {
        val bruksenhet = defaultBruksenhet.withEgenregistrertData(listOf(defaultEgenregistrering))

        assertThat(bruksenhet.oppvarminger).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }
}

