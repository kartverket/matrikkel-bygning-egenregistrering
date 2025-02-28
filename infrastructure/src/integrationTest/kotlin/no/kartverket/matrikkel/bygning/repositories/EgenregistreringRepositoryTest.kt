package no.kartverket.matrikkel.bygning.repositories

import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.EgenregistreringRepositoryImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class EgenregistreringRepositoryTest : TestWithDb() {
    private val egenregistreringRepository = EgenregistreringRepositoryImpl()

    private val defaultBygningRegistrering = BygningRegistrering(
        bygningBubbleId = BygningBubbleId(1L),
        bruksenhetRegistreringer = emptyList()
    )

    private val defaultEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        eier = Foedselsnummer("66860475309"),
        bygningRegistrering = defaultBygningRegistrering,
        prosess = ProsessKode.Egenregistrering
    )

    @Test
    fun `lagring av gyldig egenregistrering skal ikke feile`() {
        egenregistreringRepository.saveEgenregistrering(defaultEgenregistrering, session)
    }

    @BeforeEach
    fun clearEgenregistreringer() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                @Suppress("SqlWithoutWhere")
                statement.execute("DELETE FROM bygning.egenregistrering")
            }
        }
    }
}
