package no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.ByggeaarMedPersondataResponse
import java.time.Instant
import kotlin.test.Test

class FeltMedPersondataResponseTest {
    @Test
    fun `mapping av felt med foedselsnummer`() {
        val byggeaar = Felt.Byggeaar(
            2000,
            RegisterMetadata(
                Instant.now(),
                RegistreringAktoer.Foedselsnummer("21904798557"),
                null,
                null,
            ),
        )

        val responseObject = toFeltMedPersondataResponse(byggeaar, ::ByggeaarMedPersondataResponse)
        assertThat(responseObject).isNotNull()
            .prop(FeltMedPersondataResponse<*>::metadata)
            .prop(RegisterMetadataMedPersondataResponse::registrertAv)
            .isEqualTo("21904798557")
    }

    @Test
    fun `mapping av felt med signatur`() {
        val byggeaar = Felt.Byggeaar(
            2000,
            RegisterMetadata(
                Instant.now(),
                RegistreringAktoer.Signatur("Kongen"),
                null,
                null,
            ),
        )

        val responseObject = toFeltMedPersondataResponse(byggeaar, ::ByggeaarMedPersondataResponse)
        assertThat(responseObject).isNotNull()
            .prop(FeltMedPersondataResponse<*>::metadata)
            .prop(RegisterMetadataMedPersondataResponse::registrertAv)
            .isEqualTo("Kongen")
    }
}
