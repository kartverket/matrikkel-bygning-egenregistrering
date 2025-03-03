package no.kartverket.matrikkel.bygning.repositories

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import assertk.assertions.size
import no.kartverket.matrikkel.bygning.application.hendelser.BygningHendelseType
import no.kartverket.matrikkel.bygning.application.hendelser.Hendelse
import no.kartverket.matrikkel.bygning.application.hendelser.HendelsePayload
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.HendelseRepositoryImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class HendelseRepositoryTest : TestWithDb() {
    private val hendelseRepositoryTest = HendelseRepositoryImpl(dataSource)

    @Test
    fun `skal kunne hente ut lagrede hendelser`() {
        hendelseRepositoryTest.saveHendelse(
            payload = HendelsePayload.BruksenhetOppdatertPayload(
                objectId = 1L,
                registreringstidspunkt = Instant.parse("2025-01-01T12:00:00.00Z"),
            ),
            tx = session,
        )

        hendelseRepositoryTest.saveHendelse(
            payload = HendelsePayload.BruksenhetOppdatertPayload(
                objectId = 2L,
                registreringstidspunkt = Instant.parse("2025-01-01T12:00:00.00Z"),
            ),
            tx = session,
        )

        val lagredeHendelser = hendelseRepositoryTest.getHendelser(0, 2)

        assertThat(lagredeHendelser).each {
            it.all {
                prop(Hendelse::payload).all {
                    prop(HendelsePayload::type).isEqualTo(BygningHendelseType.BRUKSENHET_OPPDATERT)
                }
            }
        }
        assertThat(lagredeHendelser).size().isEqualTo(2)
    }

    @BeforeEach
    fun clearBruksenheter() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                @Suppress("SqlWithoutWhere")
                statement.execute("DELETE FROM bygning.hendelse")
            }
        }
    }
}
