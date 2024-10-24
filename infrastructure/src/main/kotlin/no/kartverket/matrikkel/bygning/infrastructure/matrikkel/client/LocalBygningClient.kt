package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.application.models.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.*
import java.time.Instant

internal class LocalBygningClient : BygningClient {
    private val bruksenheter: List<Bruksenhet> = listOf(
        Bruksenhet(
            bruksenhetId = 1L,
            bygningId = 1L,
        ),
        Bruksenhet(
            bruksenhetId = 2L,
            bygningId = 1L,
        ),
        Bruksenhet(
            bruksenhetId = 3L,
            bygningId = 2L,
        ),
        Bruksenhet(
            bruksenhetId = 4L,
            bygningId = 2L,
        ),
    )

    private val bygninger: List<Bygning> = listOf(
        Bygning(
            bygningId = 1L,
            bygningsnummer = 100L,
            bruksenheter = bruksenheter.subList(0, 2),
            bruksareal = Multikilde(
                autoritativ = Bruksareal(
                    data = 150.0,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
                        registrertAv = Signatur("norola"),
                    ),
                ),
            ),
        ),
        Bygning(
            bygningId = 2L,
            bygningsnummer = 200L,
            bruksenheter = bruksenheter.subList(2, 4),
        ),
    )

    override fun getBygningById(id: Long): Result<Bygning, ErrorDetail> {
        return bygninger
            .find { it.bygningId == id }
            .toResultOr { ErrorDetail(detail = "Bygning med ID $id finnes ikke i matrikkelen") }
    }

    override fun getBygningByBygningsnummer(bygningsnummer: Long): Result<Bygning, ErrorDetail> {
        return bygninger
            .find { it.bygningsnummer == bygningsnummer }
            .toResultOr {
                ErrorDetail(
                    detail = "Bygning med bygningsnummer $bygningsnummer finnes ikke i matrikkelen",
                )
            }
    }
}
