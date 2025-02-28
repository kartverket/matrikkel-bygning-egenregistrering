package no.kartverket.matrikkel.bygning.application.egenregistrering

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isTrue
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.util.*
import kotlin.test.Test

class EgenregistreringValidatorTest {
    val bygningId = BygningId("00000000-0000-0000-0000-000000000001")

    private val baseBygning = Bygning(
        id = bygningId,
        bygningBubbleId = BygningBubbleId(1L), bygningsnummer = 100L,
        bruksenheter = listOf(
            Bruksenhet(
                id = BruksenhetId("00000000-0000-0000-0001-000000000001"),
                bruksenhetBubbleId = BruksenhetBubbleId(1L),
                etasjer = Multikilde(),
            ),
        ),
        etasjer = emptyList(),
    )

    private val baseEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        eier = Foedselsnummer("66860475309"),
        prosess = ProsessKode.Egenregistrering,
        bygningRegistrering = BygningRegistrering(
            bygningBubbleId = BygningBubbleId(1L),
            bruksenhetRegistreringer = emptyList(),
        ),
    )

    @Test
    fun `egenregistrering med flere registreringer paa samme bruksenhet skal feile`() {
        val validationResult = EgenregistreringValidator.validateEgenregistrering(
            baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetBubbleId = BruksenhetBubbleId(1L),
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                        BruksenhetRegistrering(
                            bruksenhetBubbleId = BruksenhetBubbleId(1L),
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                    ),
                ),
            ),
            baseBygning,
        )

        assertThat(validationResult.isErr).isTrue()
        assertThat(validationResult.error.errors).hasSize(1)
        assertThat(validationResult.error.errors.first().message).contains("flere registreringer")
    }

    @Test
    fun `egenregistrering med bruksenhetregistrering hvor bruksenhet ikke finnes paa bygning skal feile`() {
        val validationResult = EgenregistreringValidator.validateEgenregistrering(
            baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetBubbleId = BruksenhetBubbleId(3L),
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                    ),
                ),
            ),
            baseBygning,
        )

        assertThat(validationResult.isErr).isTrue()
        assertThat(validationResult.error.errors).hasSize(1)
        assertThat(validationResult.error.errors.first().message).contains("finnes ikke")
    }

    @Test
    fun `egenregistrering med totalt BRA != sum av BRA skal feile`() {
        val validationResult = EgenregistreringValidator.validateEgenregistrering(
            egenregistrering = baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetBubbleId = BruksenhetBubbleId(1L),
                            bruksarealRegistrering = BruksarealRegistrering(
                                totaltBruksareal = 50.0,
                                etasjeRegistreringer = listOf(
                                    EtasjeBruksarealRegistrering(
                                        bruksareal = 55.0,
                                        etasjebetegnelse = Etasjebetegnelse.of(
                                            etasjenummer = Etasjenummer.of(1),
                                            etasjeplanKode = EtasjeplanKode.Kjelleretasje,
                                        ),
                                    ),
                                ),
                                kildemateriale = KildematerialeKode.Selvrapportert,
                            ),
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                    ),
                ),
            ),
            bygning = baseBygning,
        )

        assertThat(validationResult.isErr).isTrue()
        assertThat(validationResult.error.errors).hasSize(1)
        assertThat(validationResult.error.errors.first().message).contains("totalt BRA stemmer ikke overens")
    }
}
