package no.kartverket.matrikkel.bygning.application.egenregistrering

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isTrue
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.OppvarmingDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.util.UUID
import kotlin.test.Test

class EgenregistreringValidatorTest {
    private val baseEgenregistrering =
        Egenregistrering(
            id = EgenregistreringId(UUID.randomUUID()),
            registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
            eier = Foedselsnummer("66860475309"),
            prosess = ProsessKode.Egenregistrering,
            bruksenhetRegistrering =
                BruksenhetRegistrering(
                    bruksenhetBubbleId = BruksenhetBubbleId(1L),
                    bruksarealRegistrering = null,
                    energikildeRegistrering = null,
                    oppvarmingRegistrering = null,
                    byggeaarRegistrering = null,
                    vannforsyningRegistrering = null,
                    avlopRegistrering = null,
                ),
        )

    @Test
    fun `egenregistrering med totalt BRA != sum av BRA skal feile`() {
        val validationResult =
            EgenregistreringValidator.validateEgenregistrering(
                egenregistrering =
                    baseEgenregistrering.copy(
                        bruksenhetRegistrering =
                            BruksenhetRegistrering(
                                bruksenhetBubbleId = BruksenhetBubbleId(1L),
                                bruksarealRegistrering =
                                    BruksarealRegistrering(
                                        totaltBruksareal = 50.0,
                                        etasjeRegistreringer =
                                            listOf(
                                                EtasjeBruksarealRegistrering(
                                                    bruksareal = 55.0,
                                                    etasjebetegnelse =
                                                        Etasjebetegnelse.of(
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
            )

        assertThat(validationResult.isErr).isTrue()
        assertThat(validationResult.error.errors).hasSize(1)
        assertThat(
            validationResult.error.errors
                .first()
                .message,
        ).contains("totalt BRA stemmer ikke overens")
    }

    @Test
    fun `egenregistrering med duplikatverdier i listeregistrering skal feile`() {
        val egenregistreringWithDuplicate =
            baseEgenregistrering.copy(
                bruksenhetRegistrering =
                    baseEgenregistrering.bruksenhetRegistrering.copy(
                        oppvarmingRegistrering =
                            OppvarmingRegistrering.Data(
                                data =
                                    listOf(
                                        OppvarmingDataRegistrering(
                                            oppvarming = OppvarmingKode.Elektrisk,
                                            kildemateriale = KildematerialeKode.Selvrapportert,
                                            gyldighetsaar = 2024,
                                            opphoersaar = 2024,
                                        ),
                                        OppvarmingDataRegistrering(
                                            oppvarming = OppvarmingKode.Elektrisk,
                                            kildemateriale = KildematerialeKode.Selvrapportert,
                                            gyldighetsaar = 2025,
                                            opphoersaar = 2026,
                                        ),
                                    ),
                            ),
                    ),
            )

        val result = EgenregistreringValidator.validateEgenregistrering(egenregistreringWithDuplicate)

        assertThat(result.isErr).isTrue()
        assertThat(result.error.errors).hasSize(1)
        assertThat(
            result.error.errors
                .first()
                .message,
        ).contains("har dupliserte registreringer")
    }
}
