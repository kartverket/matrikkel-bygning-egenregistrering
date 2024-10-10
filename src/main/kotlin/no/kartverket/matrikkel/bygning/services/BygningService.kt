package no.kartverket.matrikkel.bygning.services

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr
import no.kartverket.matrikkel.bygning.matrikkel.BygningClient
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import no.kartverket.matrikkel.bygning.models.withEgenregistrertData

class BygningService(
    private val bygningClient: BygningClient,
    private val egenregistreringService: EgenregistreringService
) {
    fun getBygningWithEgenregistrertData(bygningId: Long): Result<Bygning, ErrorDetail> {
        return bygningClient
            .getBygningById(bygningId)
            .andThen { bygning ->
                egenregistreringService
                    .findAllEgenregistreringerForBygning(bygningId)
                    .map { egenregistreringerForBygning ->
                        bygning.withEgenregistrertData(egenregistreringerForBygning)
                    }
            }
    }

    fun getBruksenhetWithEgenregistrertData(bygningId: Long, bruksenhetId: Long): Result<Bruksenhet, ErrorDetail> {
        return bygningClient
            .getBygningById(bygningId)
            .andThen { bygning ->
                egenregistreringService
                    .findAllEgenregistreringerForBygning(bygningId)
                    .flatMap { egenregistreringerForBygning ->
                        val bruksenhet = bygning.bruksenheter
                            .find { it.bruksenhetId == bruksenhetId }
                            ?.withEgenregistrertData(egenregistreringerForBygning)

                        bruksenhet.toResultOr {
                            ErrorDetail(detail = "Bruksenhet finnes ikke på bygningen")
                        }
                    }
            }
    }
}
