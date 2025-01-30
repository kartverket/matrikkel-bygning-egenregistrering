package no.kartverket.matrikkel.bygning.repositories

import assertk.all
import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning.BygningRepositoryImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class BygningRepositoryTest : TestWithDb() {
    private val bygningRepository = BygningRepositoryImpl(dataSource)

    private val bruksenhetId = BruksenhetId("00000000-0000-0000-0000-000000000001")
    private val bygningId = BygningId("00000000-0000-0000-0001-000000000001")
    private val defaultBruksenhet = Bruksenhet(
        id = bruksenhetId,
        bruksenhetBubbleId = BruksenhetBubbleId(1L),
        bygningId = bygningId,
        avlop = Multikilde(
            egenregistrert = Felt.Avlop(
                data = AvlopKode.OffentligKloakk,
                metadata = RegisterMetadata(
                    registreringstidspunkt = Instant.now(),
                    registrertAv = Foedselsnummer("31129956715"),
                    kildemateriale = KildematerialeKode.Salgsoppgave,
                    prosess = ProsessKode.Egenregistrering,
                ),
            ),
        ),
    )

    @Test
    fun `lagret bruksenhet skal kunne hentes ut igjen`() {
        bygningRepository.saveBruksenhet(defaultBruksenhet)

        val retrievedBruksenhet = bygningRepository.getBruksenhetById(defaultBruksenhet.id.value)


        assertThat(retrievedBruksenhet).isNotNull().all {
            prop(Bruksenhet::id).equals(BruksenhetId("00000000-0000-0000-0000-000000000001"))
            prop(Bruksenhet::bruksenhetBubbleId).equals(BruksenhetBubbleId(1L))
            prop(Bruksenhet::bygningId).equals(BygningId("00000000-0000-0000-0001-000000000001"))
            prop(Bruksenhet::avlop).all {
                prop(Multikilde<Felt.Avlop>::egenregistrert).isNotNull().all {
                    prop(Felt.Avlop::data).equals(AvlopKode.OffentligKloakk)
                    prop(Felt.Avlop::metadata).all {
                        prop(RegisterMetadata::registreringstidspunkt).isNotNull()
                        prop(RegisterMetadata::registrertAv).equals(Foedselsnummer("31129956715"))
                        prop(RegisterMetadata::kildemateriale).equals(KildematerialeKode.Salgsoppgave)
                        prop(RegisterMetadata::prosess).equals(ProsessKode.Egenregistrering)
                    }
                }
            }
        }
    }

    @Test
    fun `lagring av bruksenhet med ny data skal kun hente nyeste versjon`() {
        bygningRepository.saveBruksenhet(defaultBruksenhet)
        bygningRepository.saveBruksenhet(defaultBruksenhet.copy(
            avlop = Multikilde(
                egenregistrert = Felt.Avlop(
                    data = AvlopKode.PrivatKloakk,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = Instant.now(),
                        registrertAv = Foedselsnummer("31129956715"),
                        kildemateriale = KildematerialeKode.Salgsoppgave,
                        prosess = ProsessKode.Egenregistrering,
                    ),
                ),
            ),
        ))

        val retrievedBruksenhet = bygningRepository.getBruksenhetById(defaultBruksenhet.id.value)


        assertThat(retrievedBruksenhet).isNotNull().all {
            prop(Bruksenhet::id).equals(BruksenhetId("00000000-0000-0000-0000-000000000001"))
            prop(Bruksenhet::bruksenhetBubbleId).equals(BruksenhetBubbleId(1L))
            prop(Bruksenhet::bygningId).equals(BygningId("00000000-0000-0000-0001-000000000001"))
            prop(Bruksenhet::avlop).all {
                prop(Multikilde<Felt.Avlop>::egenregistrert).isNotNull().all {
                    prop(Felt.Avlop::data).equals(AvlopKode.PrivatKloakk)
                    prop(Felt.Avlop::metadata).all {
                        prop(RegisterMetadata::registreringstidspunkt).isNotNull()
                        prop(RegisterMetadata::registrertAv).equals(Foedselsnummer("31129956715"))
                        prop(RegisterMetadata::kildemateriale).equals(KildematerialeKode.Salgsoppgave)
                        prop(RegisterMetadata::prosess).equals(ProsessKode.Egenregistrering)
                    }
                }
            }
        }
    }

    // Aner ikke om dette er en vettug måte å gjøre dette på? Vi må ha en måte å ha en tom db mellom tester, hvert fall
    @BeforeEach
    fun clearBruksenheter() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("DELETE FROM bygning.bruksenhet")
            }
        }
    }
}
