package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Signatur
import no.kartverket.matrikkel.bygning.application.models.error.BygningNotFound
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import java.time.Instant
import java.util.*

internal class LocalBygningClient : BygningClient {
    private val bruksenheter: List<Bruksenhet> = listOf(
        Bruksenhet(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            bruksenhetId = 1L,
            bygningId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        ),
        Bruksenhet(
            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
            bruksenhetId = 2L,
            bygningId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        ),
        Bruksenhet(
            id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
            bruksenhetId = 3L,
            bygningId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        ),
        Bruksenhet(
            id = UUID.fromString("00000000-0000-0000-0000-000000000004"),
            bruksenhetId = 4L,
            bygningId = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        ),
    )

    private val bygninger: List<Bygning> = listOf(
        Bygning(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            bygningId = 1L,
            bygningsnummer = 100L,
            bruksenheter = bruksenheter.subList(0, 2),
            bruksareal = Multikilde(
                autoritativ = Bruksareal(
                    data = 150.0,
                    metadata = RegisterMetadata(
                        registreringstidspunkt = Instant.parse("2024-01-01T12:00:00.00Z"),
                        registrertAv = Signatur("norola"),
                        kildemateriale = null,
                        prosess = null,
                    ),
                ),
            ),
            etasjer = emptyList(),
        ),
        Bygning(
            bygningId = 2L,
            bygningsnummer = 200L,
            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
            bruksenheter = bruksenheter.subList(2, 4),
            etasjer = emptyList(),
        ),
    )

    override fun getBygningById(id: Long): Result<Bygning, DomainError> {
        return bygninger
            .find { it.bygningId == id }
            .toResultOr {
                BygningNotFound(message = "Bygning med ID $id finnes ikke i matrikkelen")
            }
    }

    override fun getBygningByBygningsnummer(bygningsnummer: Long): Result<Bygning, DomainError> {
        return bygninger
            .find { it.bygningsnummer == bygningsnummer }
            .toResultOr {
                BygningNotFound(
                    message = "Bygning med bygningsnummer $bygningsnummer finnes ikke i matrikkelen",
                )
            }
    }
}
