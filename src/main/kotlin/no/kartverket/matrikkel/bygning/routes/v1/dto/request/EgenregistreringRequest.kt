package no.kartverket.matrikkel.bygning.routes.v1.dto.request

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.models.VannforsyningRegistrering
import java.time.Instant
import java.util.*


@Serializable
data class BygningRegistreringRequest(
    val bruksarealRegistrering: BruksarealRegistrering?,
    val byggeaarRegistrering: ByggeaarRegistrering?,
    val vannforsyningRegistrering: VannforsyningRegistrering?,
    val avlopRegistrering: AvlopRegistrering?
)

@Serializable
data class BruksenhetRegistreringRequest(
    val bruksenhetId: Long,
    val bruksarealRegistrering: BruksarealRegistrering?,
    val energikildeRegistrering: EnergikildeRegistrering?,
    val oppvarmingRegistrering: OppvarmingRegistrering?
)

@Serializable
data class EgenregistreringRequest(
    val bygningId: Long,
    val bygningRegistrering: BygningRegistreringRequest?,
    val bruksenhetRegistreringer: List<BruksenhetRegistreringRequest>?
)

fun EgenregistreringRequest.toEgenregistrering(): Egenregistrering {
    return Egenregistrering(
        id = UUID.randomUUID(),
        registreringTidspunkt = Instant.now(),
        bygningId = this.bygningId,
        bygningRegistrering = BygningRegistrering(
            registreringId = UUID.randomUUID(),
            bygningId = this.bygningId,
            byggeaarRegistrering = this.bygningRegistrering?.byggeaarRegistrering,
            bruksarealRegistrering = this.bygningRegistrering?.bruksarealRegistrering,
            vannforsyningRegistrering = this.bygningRegistrering?.vannforsyningRegistrering,
            avlopRegistrering = this.bygningRegistrering?.avlopRegistrering,
        ),
        bruksenhetRegistreringer = this.bruksenhetRegistreringer?.map { bruksenhetRegistrering ->
            BruksenhetRegistrering(
                registreringId = UUID.randomUUID(),
                bruksenhetId = bruksenhetRegistrering.bruksenhetId,
                bruksarealRegistrering = bruksenhetRegistrering.bruksarealRegistrering,
                energikildeRegistrering = bruksenhetRegistrering.energikildeRegistrering,
                oppvarmingRegistrering = bruksenhetRegistrering.oppvarmingRegistrering,
            )
        },
    )
}
