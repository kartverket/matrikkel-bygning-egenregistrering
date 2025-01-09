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
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApi
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.getBruksenheter
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.getBygning
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.bygningId
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.toInstant
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO Håndtering av at matrikkel servicene thrower på visse vanlige HTTP koder, ikke bare full try/catch
internal class MatrikkelBygningClient(
    private val matrikkelApi: MatrikkelApi.WithAuth
) : BygningClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun getBygningById(id: Long): Result<Bygning, DomainError> {
        val bygningId: BygningId = bygningId(id)

        try {
            val bygning = matrikkelApi.storeService().getBygning(bygningId, matrikkelApi.matrikkelContext)

            val bruksenheter = matrikkelApi.storeService().getBruksenheter(bygning.bruksenhetIds.item, matrikkelApi.matrikkelContext)

            val bygningsmetadata = RegisterMetadata(
                bygning.oppdateringsdato.toInstant(),
                Signatur(bygning.oppdatertAv),
                prosess = null,
            )

            return Ok(
                Bygning(
                    bygningId = bygning.id.value,
                    bygningsnummer = bygning.bygningsnummer,
                    byggeaar = Multikilde(
                        autoritativ = deriveByggeaarForBygning(bygning),
                    ),
                    // TODO: Hvordan innse at arealet er ukjent og hvordan håndtere dette
                    bruksareal = Multikilde(
                        autoritativ = Bruksareal(
                            bygning.etasjedata.bruksarealTotalt,
                            bygningsmetadata,
                        ),
                    ),
                    // TODO: Burde vi ha kode for Ikke oppgitt?
                    vannforsyning = Multikilde(
                        autoritativ = mapVannforsyning(bygning.vannforsyningsKodeId)?.let {
                            Vannforsyning(
                                it,
                                bygningsmetadata,
                            )
                        },
                    ),
                    // TODO: Burde vi ha kode for Ikke oppgitt?
                    avlop = Multikilde(
                        autoritativ = mapAvloep(bygning.avlopsKodeId)?.let {
                            Avlop(
                                it,
                                bygningsmetadata,
                            )
                        },
                    ),
                    // TODO: Skal tom liste i matrikkelen tolkes som "vet ikke" eller "ingen"?
                    energikilder = Multikilde(
                        autoritativ = bygning.energikildeKodeIds.item.map {
                            Energikilde(
                                mapEnergikilde(it),
                                bygningsmetadata,
                            )
                        }.ifEmpty { null }, // Tolker som "vet ikke"
                    ),
                    // TODO: Skal tom liste i matrikkelen tolkes som "vet ikke" eller "ingen"?
                    oppvarminger = Multikilde(
                        autoritativ = bygning.oppvarmingsKodeIds.item.map {
                            Oppvarming(
                                mapOppvarming(it),
                                bygningsmetadata,
                            )
                        }.ifEmpty { null }, // Tolker som "vet ikke"
                    ),
                    // TODO: Burde vi ha en måte å angi ukjent / ikke oppgitt?
                    bruksenheter = bruksenheter.map {
                        val bruksenhetsmetadata = RegisterMetadata(
                            it.oppdateringsdato.toInstant(),
                            Signatur(it.oppdatertAv),
                            kildemateriale = null,
                            prosess = ProsessKode.Egenregistrering,
                        )

                        Bruksenhet(
                            bruksenhetId = it.id.value,
                            bygningId = it.byggId.value,
                            // TODO: Hvordan innse at arealet er ukjent og hvordan håndtere dette
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

            return getBygningById(bygningId.value)
        } catch (exception: ServiceException) {
            log.warn("Noe gikk galt under henting av bygning med bygningsnummer {}", bygningsnummer, exception)
            return Err(
                BygningNotFound(
                    message = "Bygning med nummer $bygningsnummer finnes ikke i matrikkelen",
                ),
            )
        }
    }
}
