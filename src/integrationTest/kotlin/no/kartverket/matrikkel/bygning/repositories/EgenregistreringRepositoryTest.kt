package no.kartverket.matrikkel.bygning.repositories

import no.kartverket.matrikkel.bygning.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class EgenregistreringRepositoryTest : TestWithDb() {
    val egenregistreringRepository = EgenregistreringRepository(dataSource)

    val defaultBygningRegistrering = BygningRegistrering(
        bygningId = 1L,
        bruksarealRegistrering = BruksarealRegistrering(
            bruksareal = 125.0,
        ),
        byggeaarRegistrering = null,
        vannforsyningRegistrering = null,
        avlopRegistrering = null,
        bruksenhetRegistreringer = emptyList()
    )

    val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        bygningRegistrering = defaultBygningRegistrering,
    )

    @Test
    fun `lagring av 1 egenregistrering skal kun returnere 1 egenregistrering`() {
        egenregistreringRepository.saveEgenregistrering(defaultEgenregistrering)

        val bygningRegistreringer = egenregistreringRepository.getAllEgenregistreringerForBygning(1L)

        assertThat(bygningRegistreringer).size().isEqualTo(1)
        assertThat(bygningRegistreringer[0]).satisfies(
            { egenregistrering ->
                assertThat(egenregistrering.id).isEqualTo(defaultEgenregistrering.id)
                assertThat(egenregistrering.registreringstidspunkt).isEqualTo(defaultEgenregistrering.registreringstidspunkt)
            }
        )
    }

    @Test
    fun `lagring av 2 egenregistreringer skal returneres i riktig rekkefolge med seneste registreringer forst i listen`() {
        val laterRegistreringId = UUID.randomUUID()
        val laterRegistrering = defaultEgenregistrering.copy(
            id = laterRegistreringId,
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60)
        )

        egenregistreringRepository.saveEgenregistrering(defaultEgenregistrering)
        egenregistreringRepository.saveEgenregistrering(laterRegistrering)

        val registreringer = egenregistreringRepository.getAllEgenregistreringerForBygning(1L)

        assertThat(registreringer[0].id).isEqualTo(laterRegistrering.id)
        assertThat(registreringer[1].id).isEqualTo(defaultEgenregistrering.id)
    }

    @Test
    fun `henting av registreringer skal gi tom liste hvis bygningen ikke har registreringer`() {
        egenregistreringRepository.saveEgenregistrering(defaultEgenregistrering)

        val registreringer = egenregistreringRepository.getAllEgenregistreringerForBygning(2L)

        assertThat(registreringer).isEmpty()
    }

    // Aner ikke om dette er en vettug måte å gjøre dette på? Vi må ha en måte å ha en tom db mellom tester, hvert fall
    @BeforeEach
    fun clearEgenregistreringer() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("DELETE FROM bygning.egenregistrering")
            }
        }
    }
}
