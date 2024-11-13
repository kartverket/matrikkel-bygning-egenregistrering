package no.kartverket.matrikkel.bygning.routes.v1.egenregistrering

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.*
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.application.models.toEtasjenummer
import java.time.Instant
import java.util.*
import kotlin.collections.map


@Serializable
data class BruksenhetRegistreringRequest(
    val bruksenhetId: Long,
    val bruksarealRegistrering: BruksarealRegistreringRequest?,
    val byggeaarRegistrering: ByggeaarRegistreringRequest?,
    val energikildeRegistrering: EnergikildeRegistreringRequest?,
    val oppvarmingRegistrering: OppvarmingRegistreringRequest?,
    val vannforsyningRegistrering: VannforsyningRegistreringRequest?,
    val avlopRegistrering: AvlopRegistreringRequest?,
)

@Serializable
data class EgenregistreringRequest(
    val bygningId: Long, val eier: String, val bruksenhetRegistreringer: List<BruksenhetRegistreringRequest>?
)

@Serializable
data class ByggeaarRegistreringRequest(
    val byggeaar: Int?,
)

@Serializable
data class EtasjeBruksarealRegistreringRequest(
    val bruksareal: Double?, val etasjenummer: String
)

@Serializable
data class BruksarealRegistreringRequest(
    val totalBruksareal: Double?, val etasjeRegistreringer: List<EtasjeBruksarealRegistreringRequest>?
)

@Serializable
data class VannforsyningRegistreringRequest(
    val vannforsyning: VannforsyningKode?,
)

@Serializable
data class AvlopRegistreringRequest(
    val avlop: AvlopKode?,
)

@Serializable
data class EnergikildeRegistreringRequest(
    val energikilder: List<EnergikildeKode>?,
)

@Serializable
data class OppvarmingRegistreringRequest(
    val oppvarminger: List<OppvarmingKode>?,
)

fun EtasjeBruksarealRegistreringRequest.toEtasjeBruksarealRegistrering(): Result<EtasjeBruksarealRegistrering, ErrorDetail> {
    return this.etasjenummer.toEtasjenummer().map { etasjenummer ->
        EtasjeBruksarealRegistrering(
            bruksareal = this.bruksareal,
            etasjenummer = etasjenummer,
        )
    }
}

fun BruksenhetRegistreringRequest.toBruksenhetRegistrering(): Result<BruksenhetRegistrering, ErrorDetail> {
    return Ok(
        BruksenhetRegistrering(
            bruksenhetId = bruksenhetId,
            bruksarealRegistrering = bruksarealRegistrering?.let {
                BruksarealRegistrering(
                    totalBruksareal = it.totalBruksareal,
                    etasjeRegistreringer = it.etasjeRegistreringer?.map {
                        val etasjeBruksarealRegistrering = it.toEtasjeBruksarealRegistrering()
                        if (etasjeBruksarealRegistrering.isOk) {
                            etasjeBruksarealRegistrering.value
                        } else {
                            return Err(etasjeBruksarealRegistrering.error)
                        }
                    },
                )
            },
            byggeaarRegistrering = byggeaarRegistrering?.let {
                ByggeaarRegistrering(
                    byggeaar = it.byggeaar,
                )
            },
            vannforsyningRegistrering = vannforsyningRegistrering?.let {
                VannforsyningRegistrering(
                    vannforsyning = it.vannforsyning,
                )
            },
            avlopRegistrering = avlopRegistrering?.let {
                AvlopRegistrering(
                    avlop = it.avlop,
                )
            },
            energikildeRegistrering = energikildeRegistrering?.let {
                EnergikildeRegistrering(
                    energikilder = it.energikilder,
                )
            },
            oppvarmingRegistrering = oppvarmingRegistrering?.let {
                OppvarmingRegistrering(
                    oppvarminger = it.oppvarminger,
                )
            },
        ),
    )
}

fun EgenregistreringRequest.toEgenregistrering(): Result<Egenregistrering, ErrorDetail> {
    val registreringstidspunkt = Instant.now()

    // Synes ikke feilhåndteringen ble veldig smud her, så gjerne kom med innspill på hvordan best gjøre det når det
    // er potensielle feilkilder i instansieringen
    // Gjelder forsåvidt både her og i .toBruksenhetRegistrering()
    return Ok(
        Egenregistrering(
            id = UUID.randomUUID(),
            eier = Foedselsnummer(this.eier),
            registreringstidspunkt = registreringstidspunkt,
            bygningRegistrering = BygningRegistrering(
                bygningId = this.bygningId,
                bruksenhetRegistreringer = this.bruksenhetRegistreringer?.map { bruksenhetRegistrering ->
                    val bruksenhetRegistrering = bruksenhetRegistrering.toBruksenhetRegistrering()

                    if (bruksenhetRegistrering.isOk) {
                        bruksenhetRegistrering.value
                    } else {
                        return Err(bruksenhetRegistrering.error)
                    }
                } ?: emptyList(),
            ),
        ),
    )


}
