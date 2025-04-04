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
import no.kartverket.matrikkel.bygning.application.models.error.BruksenhetNotFound
import no.kartverket.matrikkel.bygning.application.models.error.BygningNotFound
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import java.time.Instant

class LocalBygningClient : BygningClient {
    private val bruksenheter: List<Bruksenhet> =
        listOf(
            Bruksenhet(
                id = BruksenhetId("00000000-0000-0000-0000-000000000001"),
                bruksenhetBubbleId = BruksenhetBubbleId(1L),
            ),
            Bruksenhet(
                id = BruksenhetId("00000000-0000-0000-0000-000000000002"),
                bruksenhetBubbleId = BruksenhetBubbleId(2L),
            ),
            Bruksenhet(
                id = BruksenhetId("00000000-0000-0000-0000-000000000003"),
                bruksenhetBubbleId = BruksenhetBubbleId(3L),
            ),
            Bruksenhet(
                id = BruksenhetId("00000000-0000-0000-0000-000000000004"),
                bruksenhetBubbleId = BruksenhetBubbleId(4L),
            ),
        )

    private val bygninger: List<Bygning> =
        listOf(
            Bygning(
                id = BygningId("00000000-0000-0000-0000-000000000001"),
                bygningBubbleId = BygningBubbleId(1L),
                bygningsnummer = 100L,
                bruksenheter = bruksenheter.subList(0, 2),
                bruksareal =
                    Multikilde(
                        autoritativ =
                            Bruksareal(
                                data = 150.0,
                                metadata =
                                    RegisterMetadata(
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
                bygningBubbleId = BygningBubbleId(2L),
                bygningsnummer = 200L,
                id = BygningId("00000000-0000-0000-0000-000000000002"),
                bruksenheter = bruksenheter.subList(2, 4),
                etasjer = emptyList(),
            ),
        )

    override fun getBygningByBubbleId(bygningBubbleId: Long): Result<Bygning, DomainError> =
        bygninger
            .find { it.bygningBubbleId.value == bygningBubbleId }
            .toResultOr {
                BygningNotFound(message = "Bygning med ID $bygningBubbleId finnes ikke i matrikkelen")
            }

    override fun getBygningByBygningsnummer(bygningsnummer: Long): Result<Bygning, DomainError> =
        bygninger
            .find { it.bygningsnummer == bygningsnummer }
            .toResultOr {
                BygningNotFound(
                    message = "Bygning med bygningsnummer $bygningsnummer finnes ikke i matrikkelen",
                )
            }

    override fun getBruksenhetByBubbleId(bruksenhetBubbleId: Long): Result<Bruksenhet, DomainError> =
        bruksenheter
            .find { it.bruksenhetBubbleId.value == bruksenhetBubbleId }
            .toResultOr {
                BruksenhetNotFound(
                    message = "Bruksenhet med ID $bruksenhetBubbleId finnes ikke i matrikkelen",
                )
            }
}
