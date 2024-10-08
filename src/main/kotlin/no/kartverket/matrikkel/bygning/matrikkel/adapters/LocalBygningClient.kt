package no.kartverket.matrikkel.bygning.matrikkel.adapters

import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bruksareal
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.Multikilde
import no.kartverket.matrikkel.bygning.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.models.RegistreringAktoer
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
                        registrertAv = RegistreringAktoer.Signatur("norola"),
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

    override fun getBygningById(id: Long): Bygning? {
        return bygninger.find { it.bygningId == id }
    }

    override fun getBygningByBygningsnummer(bygningsnummer: Long): Bygning? {
        return bygninger.find { it.bygningsnummer == bygningsnummer }
    }
}
