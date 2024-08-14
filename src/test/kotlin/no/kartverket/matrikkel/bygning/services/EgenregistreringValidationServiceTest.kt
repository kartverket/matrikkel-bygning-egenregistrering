package no.kartverket.matrikkel.bygning.services

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import no.kartverket.matrikkel.bygning.models.requests.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.requests.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.models.requests.BygningsRegistrering
import no.kartverket.matrikkel.bygning.models.requests.EgenregistreringRequest
import no.kartverket.matrikkel.bygning.models.requests.RegistreringMetadataRequest
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.services.EgenregistreringValidationService.Validator.validateEgenregistreringRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EgenregistreringValidationServiceTest {
    private val defaultInstant = Instant.DISTANT_PAST

    @Test
    fun `egenregistrering fra aar 1000 skal feile validering`() {
        val tooEarlyRegisteredEgenregistrering = EgenregistreringRequest(
            bygningsRegistrering = BygningsRegistrering(
                bruksareal = BruksarealRegistrering(
                    100.0,
                    metadata = RegistreringMetadataRequest(
                        defaultInstant,
                        gyldigFra = LocalDate(1000, 1, 1),
                        gyldigTil = null,
                    ),
                ),
                null,
                null,
                null,
            ),
            emptyList(),
        )


        val errors = validateEgenregistreringRequest(tooEarlyRegisteredEgenregistrering)

        assertThat(errors).hasSize(1)
        assertThat(errors[0]).isEqualTo(
            ErrorDetail(
                pointer = "bygningRegistreringer.bruksareal.metadata",
                detail = "Gyldighetsdato er satt til å være for langt bak i tid, tidligste mulige registrering er 1700",
            ),
        )
    }

    @Test
    fun `egenregistrering for langt frem i tid skal feile validering`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val tooLateRegisteredEgenregistrering = EgenregistreringRequest(
            bygningsRegistrering = BygningsRegistrering(
                bruksareal = BruksarealRegistrering(
                    100.0,
                    metadata = RegistreringMetadataRequest(
                        defaultInstant,
                        gyldigFra = today.plus(6, DateTimeUnit.MONTH).plus(1, DateTimeUnit.DAY),
                        gyldigTil = null,
                    ),
                ),
                null,
                null,
                null,
            ),
            emptyList(),
        )


        val errors = validateEgenregistreringRequest(tooLateRegisteredEgenregistrering)

        assertThat(errors).hasSize(1)
        assertThat(errors[0]).isEqualTo(
            ErrorDetail(
                pointer = "bygningRegistreringer.bruksareal.metadata",
                detail = "Gyldighetsdato er satt til å være for langt frem i tid",
            ),
        )
    }

    @Test
    fun `egenregistrering med flere feil skal gi flere feil`() {
        val tooEarlyRegisteredEgenregistrering = EgenregistreringRequest(
            bygningsRegistrering = BygningsRegistrering(
                bruksareal = BruksarealRegistrering(
                    100.0,
                    metadata = RegistreringMetadataRequest(
                        defaultInstant,
                        gyldigFra = LocalDate(1000, 1, 1),
                        gyldigTil = null,
                    ),
                ),
                byggeaar = ByggeaarRegistrering(
                    byggeaar = 2024,
                    metadata = RegistreringMetadataRequest(
                        defaultInstant,
                        gyldigFra = LocalDate(1000, 1, 1),
                        gyldigTil = null,
                    ),
                ),
                null,
                null,
            ),
            emptyList(),
        )


        val errors = validateEgenregistreringRequest(tooEarlyRegisteredEgenregistrering)

        assertThat(errors).hasSize(2)
    }

    @Test
    fun `egenregistrering med riktig formaterte datoer skal ikke gi feilmelding`() {
        val correctlyFormattedEgenregistrering = EgenregistreringRequest(
            bygningsRegistrering = BygningsRegistrering(
                bruksareal = BruksarealRegistrering(
                    100.0,
                    metadata = RegistreringMetadataRequest(
                        defaultInstant,
                        gyldigFra = LocalDate(2024, 1, 1),
                        gyldigTil = null,
                    ),
                ),
                null,
                null,
                null,
            ),
            emptyList(),
        )


        val errors = validateEgenregistreringRequest(correctlyFormattedEgenregistrering)

        assertThat(errors).isEmpty()
    }
}
