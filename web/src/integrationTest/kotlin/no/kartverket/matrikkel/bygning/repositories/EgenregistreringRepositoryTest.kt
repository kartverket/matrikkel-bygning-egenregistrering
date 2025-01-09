package no.kartverket.matrikkel.bygning.repositories

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.assertions.single
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.EgenregistreringRepositoryImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class EgenregistreringRepositoryTest : TestWithDb() {
    private val egenregistreringRepository = EgenregistreringRepositoryImpl(dataSource)

    private val defaultBygningRegistrering = BygningRegistrering(
        bygningId = 1L,
        bruksenhetRegistreringer = emptyList()
    )

    private val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        eier = Foedselsnummer("31129956715"),
        bygningRegistrering = defaultBygningRegistrering,
        prosess = ProsessKode.Egenregistrering
    )

    @Test
    fun `lagring av 1 egenregistrering skal kun returnere 1 egenregistrering`() {
        egenregistreringRepository.saveEgenregistrering(defaultEgenregistrering)

        val bygningRegistreringer = egenregistreringRepository.getAllEgenregistreringerForBygning(1L)

        assertThat(bygningRegistreringer).hasSize(1)

        assertThat(bygningRegistreringer).single().all {
            prop(Egenregistrering::id).isEqualTo(defaultEgenregistrering.id)
            prop(Egenregistrering::registreringstidspunkt).isEqualTo(defaultEgenregistrering.registreringstidspunkt)
            prop(Egenregistrering::eier).isEqualTo(defaultEgenregistrering.eier)
        }
    }

    @Test
    fun `lagring av 2 egenregistreringer skal returneres i riktig rekkefolge med seneste registreringer forst i listen`() {
        val laterRegistrering = defaultEgenregistrering.copy(
            id = UUID.randomUUID(),
            registreringstidspunkt = defaultEgenregistrering.registreringstidspunkt.plusSeconds(60)
        )

        egenregistreringRepository.saveEgenregistrering(defaultEgenregistrering)
        egenregistreringRepository.saveEgenregistrering(laterRegistrering)

        val registreringer = egenregistreringRepository.getAllEgenregistreringerForBygning(1L)

        assertThat(registreringer).index(0).all {
            prop(Egenregistrering::id).isEqualTo(laterRegistrering.id)
        }
        assertThat(registreringer).index(1).all {
            prop(Egenregistrering::id).isEqualTo(defaultEgenregistrering.id)
        }
    }

    @Test
    fun `henting av registreringer skal gi tom liste hvis bygningen ikke har registreringer`() {
        val registreringer = egenregistreringRepository.getAllEgenregistreringerForBygning(1L)

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
