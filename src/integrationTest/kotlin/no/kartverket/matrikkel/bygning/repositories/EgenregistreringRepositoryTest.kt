package no.kartverket.matrikkel.bygning.repositories

import no.kartverket.matrikkel.bygning.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class EgenregistreringRepositoryTest : TestWithDb() {
    val egenregistreringRepository = EgenregistreringRepository(dataSource)

    val defaultBygningRegistrering = BygningRegistrering(
        registreringId = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        bygningId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            bruksareal = 125.0,
        ),
        byggeaarRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
    )

    val defaultBruksenhetRegistrering = BruksenhetRegistrering(
        registreringId = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        bruksenhetId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            bruksareal = 125.0,
        ),
        energikildeRegistrering = null,
        oppvarmingRegistrering = null,
    )

    val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        bygningId = 1L,
        bygningRegistrering = defaultBygningRegistrering,
        bruksenhetRegistreringer = emptyList(),
    )

    val defaultBruksenhetEgenregistrering = defaultEgenregistrering.copy(
        bygningRegistrering = null,
        bruksenhetRegistreringer = listOf(
            defaultBruksenhetRegistrering,
        ),
    )

    @Test
    fun `lagring av to egenregistreringer med bygningregistrering skal returnere nyeste registrering ved henting`() {
        val newUUIDEgenregistrering = UUID.randomUUID()
        val newUUIDBygningRegistrering = UUID.randomUUID()

        val registrering2 = defaultEgenregistrering.copy(
            id = newUUIDEgenregistrering,
            defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bygningRegistrering = defaultBygningRegistrering.copy(
                registreringId = newUUIDBygningRegistrering,
                bruksarealRegistrering = BruksarealRegistrering(
                    bruksareal = 130.0,
                ),
            ),
        )

        egenregistreringRepository.saveEgenregistrering(defaultEgenregistrering)
        egenregistreringRepository.saveEgenregistrering(registrering2)

        val newestBygningRegistrering = egenregistreringRepository.findNewestBygningRegistrering(1L)

        assertThat(newestBygningRegistrering?.bruksarealRegistrering?.bruksareal).isEqualTo(130.0)
    }


    @Test
    fun `lagring av to egenregistreringer med bruksenhetregistrering skal returnere nyeste registrering ved henting`() {
        val newUUIDEgenregistrering = UUID.randomUUID()
        val newUUIDBruksenhetRegistrering = UUID.randomUUID()

        val registrering2 = defaultBruksenhetEgenregistrering.copy(
            id = newUUIDEgenregistrering,
            defaultEgenregistrering.registreringstidspunkt.plusSeconds(60),
            bruksenhetRegistreringer = listOf(
                defaultBruksenhetRegistrering.copy(
                    registreringId = newUUIDBruksenhetRegistrering,
                    bruksarealRegistrering = BruksarealRegistrering(
                        bruksareal = 130.0,
                    ),
                ),
            ),
        )

        egenregistreringRepository.saveEgenregistrering(defaultBruksenhetEgenregistrering)
        egenregistreringRepository.saveEgenregistrering(registrering2)

        val newestBruksenhetRegistrering = egenregistreringRepository.findNewestBruksenhetRegistrering(1L)

        assertThat(newestBruksenhetRegistrering?.bruksarealRegistrering?.bruksareal).isEqualTo(130.0)
    }
}
