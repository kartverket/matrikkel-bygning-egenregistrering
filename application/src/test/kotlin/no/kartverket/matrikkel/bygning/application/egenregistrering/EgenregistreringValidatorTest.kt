package no.kartverket.matrikkel.bygning.application.egenregistrering

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isTrue
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningId
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.util.*
import kotlin.test.Test

class EgenregistreringValidatorTest {
    val bygningId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val baseBygning = Bygning(
        id = bygningId,
        bygningBubbleId = BygningId(1L), bygningsnummer = 100L,
        bruksenheter = listOf(
            Bruksenhet(
                id = UUID.fromString("00000000-0000-0000-0001-000000000001"),
                bruksenhetBubbleId = BruksenhetId(1L),
                bygningId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                etasjer = Multikilde(),
            ),
        ),
        etasjer = emptyList(),
    )

    private val baseEgenregistrering = Egenregistrering(
        id = UUID.randomUUID(),
        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
        eier = Foedselsnummer("31129956715"),
        prosess = ProsessKode.Egenregistrering,
        bygningRegistrering = BygningRegistrering(
            bygningId = 1L,
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
                            bruksenhetBubbleId = 1L,
                            bruksarealRegistrering = null,
                            energikildeRegistrering = null,
                            oppvarmingRegistrering = null,
                            byggeaarRegistrering = null,
                            vannforsyningRegistrering = null,
                            avlopRegistrering = null,
                        ),
                        BruksenhetRegistrering(
                            bruksenhetBubbleId = 1L,
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
                            bruksenhetBubbleId = 3L,
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
                            bruksenhetBubbleId = 1L,
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

    @Test
    fun `egenregistrering med registrert etasje BRA men ikke totalt skal feile`() {
        val validationResult = EgenregistreringValidator.validateEgenregistrering(
            egenregistrering = baseEgenregistrering.copy(
                bygningRegistrering = baseEgenregistrering.bygningRegistrering.copy(
                    bruksenhetRegistreringer = listOf(
                        BruksenhetRegistrering(
                            bruksenhetBubbleId = 1L,
                            bruksarealRegistrering = BruksarealRegistrering(
                                totaltBruksareal = null,
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
        assertThat(validationResult.error.errors.first().message).contains("Totalt BRA er obligatorisk")
    }
}
