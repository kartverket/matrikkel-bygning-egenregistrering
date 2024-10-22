package no.kartverket.matrikkel.bygning.routes.v1.egenregistrering

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.models.RegistreringAktoer.*
import no.kartverket.matrikkel.bygning.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import java.time.Instant
import java.util.*


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
    val bygningId: Long,
    val eier: String,
    val bruksenhetRegistreringer: List<BruksenhetRegistreringRequest>?
)

@Serializable
data class ByggeaarRegistreringRequest(
    val byggeaar: Int?,
)

@Serializable
data class BruksarealRegistreringRequest(
    val bruksareal: Double?,
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

fun EgenregistreringRequest.toEgenregistrering(): Egenregistrering {
    val registreringstidspunkt = Instant.now()
    return Egenregistrering(
        id = UUID.randomUUID(),
        eier = Foedselsnummer(this.eier),
        registreringstidspunkt = registreringstidspunkt,
        bygningRegistrering = BygningRegistrering(
            bygningId = this.bygningId,
            bruksenhetRegistreringer = this.bruksenhetRegistreringer?.map { bruksenhetRegistrering ->
                BruksenhetRegistrering(
                    bruksenhetId = bruksenhetRegistrering.bruksenhetId,
                    bruksarealRegistrering = bruksenhetRegistrering.bruksarealRegistrering?.let {
                        BruksarealRegistrering(
                            bruksareal = it.bruksareal,
                        )
                    },
                    byggeaarRegistrering = bruksenhetRegistrering.byggeaarRegistrering?.let {
                        ByggeaarRegistrering(
                            byggeaar = it.byggeaar,
                        )
                    },
                    vannforsyningRegistrering = bruksenhetRegistrering.vannforsyningRegistrering?.let {
                        VannforsyningRegistrering(
                            vannforsyning = it.vannforsyning,
                        )
                    },
                    avlopRegistrering = bruksenhetRegistrering.avlopRegistrering?.let {
                        AvlopRegistrering(
                            avlop = it.avlop,
                        )
                    },
                    energikildeRegistrering = bruksenhetRegistrering.energikildeRegistrering?.let {
                        EnergikildeRegistrering(
                            energikilder = it.energikilder,
                        )
                    },
                    oppvarmingRegistrering = bruksenhetRegistrering.oppvarmingRegistrering?.let {
                        OppvarmingRegistrering(
                            oppvarminger = it.oppvarminger,
                        )
                    },
                )
            } ?: emptyList(),
        ),
    )
}
