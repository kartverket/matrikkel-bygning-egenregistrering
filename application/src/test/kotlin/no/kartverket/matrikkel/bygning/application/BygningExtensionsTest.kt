package no.kartverket.matrikkel.bygning.application

import assertk.assertThat
import assertk.assertions.isEqualTo
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.withEgenregistrertData
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.*
import java.time.Instant
import java.util.*
import kotlin.test.Test

class BygningExtensionsTest {

    private val defaultBruksenhet = Bruksenhet(
        bruksenhetId = 1L,
        bygningId = 1L,
        totalBruksareal = Multikilde(),
        energikilder = Multikilde(),
        oppvarminger = Multikilde(),
    )

    private val defaultBygning = Bygning(
        bygningId = 1L,
        bygningsnummer = 100,
        bruksenheter = listOf(defaultBruksenhet),
        byggeaar = Multikilde(),
        bruksareal = Multikilde(),
        vannforsyning = Multikilde(),
        avlop = Multikilde(),
    )

    private val defaultBruksenhetRegistrering = BruksenhetRegistrering(
        bruksenhetId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            totalBruksareal = 50.0
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
        bygningRegistrering = defaultBygningRegistrering
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
                        ),
                    )
                ),
            ),
        )

        val aggregatedBygning = defaultBygning.withEgenregistrertData(listOf(laterRegistrering, defaultEgenregistrering))

        assertThat(aggregatedBygning.bruksenheter.single().totalBruksareal.egenregistrert?.data).isEqualTo(150.0)
    }

    @Test
    fun `registrering med tom liste paa listeregistering skal ikke sette felt paa bruksenhet`() {
        val bruksenhet = defaultBruksenhet.withEgenregistrertData(listOf(defaultEgenregistrering))

       assertThat(bruksenhet.oppvarminger).isEmpty()
        assertThat(bruksenhet.energikilder).isEmpty()
    }
}
