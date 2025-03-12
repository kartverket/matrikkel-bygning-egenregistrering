package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import no.kartverket.matrikkel.bygning.application.bygning.BygningClient
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningEtasje
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Signatur
import no.kartverket.matrikkel.bygning.application.models.error.BygningNotFound
import no.kartverket.matrikkel.bygning.application.models.error.DomainError
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApi
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.getBruksenhet
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.getBruksenheter
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.getBygning
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.bruksenhetId
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.bygningId
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.toInstant
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BruksenhetId as MatrikkelBruksenhetId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId as MatrikkelBygningId

// TODO Håndtering av at matrikkel servicene thrower på visse vanlige HTTP koder, ikke bare full try/catch
class MatrikkelBygningClient(
    private val matrikkelApi: MatrikkelApi.WithAuth
) : BygningClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun getBygningByBubbleId(bygningBubbleId: Long): Result<Bygning, DomainError> {
        val bygningId: MatrikkelBygningId = bygningId(bygningBubbleId)

        try {
            val bygning = matrikkelApi.storeService().getBygning(bygningId, matrikkelApi.matrikkelContext)

            val bruksenheter = matrikkelApi.storeService().getBruksenheter(bygning.bruksenhetIds.item, matrikkelApi.matrikkelContext)

            val bygningsmetadata = RegisterMetadata(
                bygning.oppdateringsdato.toInstant(),
                Signatur(bygning.oppdatertAv),
                prosess = null,
                egenregistreringId = TODO(),
                kildemateriale = TODO(),
                gyldighetsperiode = TODO(),
            )

            // TODO: Hvordan håndtere ukjent vs. ikke oppgitt fra matrikkel? Hva betyr en tom liste? Hva betyr null?
            return Ok(
                Bygning(
                    id = BygningId(bygning.uuid.uuid),
                    bygningBubbleId = BygningBubbleId(bygning.id.value),
                    bygningsnummer = bygning.bygningsnummer,
                    byggeaar = Multikilde(
                        autoritativ = deriveByggeaarForBygning(bygning),
                    ),
                    bruksareal = Multikilde(
                        autoritativ = Bruksareal(
                            bygning.etasjedata.bruksarealTotalt,
                            bygningsmetadata,
                        ),
                    ),
                    vannforsyning = Multikilde(
                        autoritativ = mapVannforsyning(bygning.vannforsyningsKodeId)?.let {
                            Vannforsyning(
                                it,
                                bygningsmetadata,
                            )
                        },
                    ),
                    avlop = Multikilde(
                        autoritativ = mapAvloep(bygning.avlopsKodeId)?.let {
                            Avlop(
                                it,
                                bygningsmetadata,
                            )
                        },
                    ),
                    energikilder = Multikilde(
                        autoritativ = Energikilde(
                            data = bygning.energikildeKodeIds.item.map { mapEnergikilde(it) },
                            metadata = bygningsmetadata,
                        ).takeUnless { bygning.energikildeKodeIds.item.isEmpty() }, // Tolker som "vet ikke"
                    ),
                    oppvarming = Multikilde(
                        autoritativ = bygning.oppvarmingsKodeIds.item.map {
                            Oppvarming(
                                data = mapOppvarming(it),
                                metadata = bygningsmetadata,
                            )
                        },
                    ),
                    bruksenheter = bruksenheter.map {
                        val bruksenhetsmetadata = RegisterMetadata(
                            it.oppdateringsdato.toInstant(),
                            Signatur(it.oppdatertAv),
                            kildemateriale = null,
                            prosess = null,
                            egenregistreringId = TODO(),
                            gyldighetsperiode = TODO(),
                        )

                        Bruksenhet(
                            id = BruksenhetId(it.uuid.uuid),
                            bruksenhetBubbleId = BruksenhetBubbleId(it.id.value),
                            totaltBruksareal = Multikilde(
                                autoritativ = Bruksareal(
                                    it.bruksareal,
                                    bruksenhetsmetadata,
                                ),
                            ),
                        )
                    },
                    etasjer = bygning.etasjer.item.mapNotNull { etasje ->
                        BygningEtasje(
                            etasjeId = etasje.id,
                            etasjebetegnelse = Etasjebetegnelse.of(
                                etasjenummer = Etasjenummer.of(etasje.etasjenummer),
                                etasjeplanKode = mapEtasjeplanKode(etasje.etasjeplanKodeId),
                            ),
                        )
                    },
                ),
            )
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med id {}", bygningId.value, exception)
            return Err(
                BygningNotFound(
                    message = "Bygning med ID ${bygningId.value} finnes ikke i matrikkelen",
                ),
            )
        }
    }

    override fun getBygningByBygningsnummer(bygningsnummer: Long): Result<Bygning, DomainError> {
        try {
            val bygningId = matrikkelApi.bygningService().findBygning(bygningsnummer, matrikkelApi.matrikkelContext)

            return getBygningByBubbleId(bygningId.value)
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med bygningsnummer {}", bygningsnummer, exception)
            return Err(
                BygningNotFound(
                    message = "Bygning med nummer $bygningsnummer finnes ikke i matrikkelen",
                ),
            )
        }
    }

    override fun getBruksenhetByBubbleId(bruksenhetBubbleId: Long): Result<Bruksenhet, DomainError> {
        try {
            val bruksenhetId: MatrikkelBruksenhetId = bruksenhetId(bruksenhetBubbleId)
            val bruksenhet = matrikkelApi.storeService().getBruksenhet(bruksenhetId, matrikkelApi.matrikkelContext)

            return Ok(
                Bruksenhet(
                    id = BruksenhetId(bruksenhet.uuid.uuid),
                    bruksenhetBubbleId = BruksenhetBubbleId(bruksenhetId.value),
                ),
            )
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bruksenhet med id bruksenhetBubbleId {}", bruksenhetBubbleId, exception)
            return Err(
                BygningNotFound(
                    message = "Bruksenhet med id $bruksenhetBubbleId finnes ikke i matrikkelen",
                ),
            )
        }
    }
}
