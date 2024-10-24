package no.kartverket.matrikkel.bygning.application.bygning

import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail
import no.kartverket.matrikkel.bygning.application.models.withEgenregistrertData
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringService
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr

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
