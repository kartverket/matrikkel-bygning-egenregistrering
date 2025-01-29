package no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
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
    val bygningId: Long, val bruksenhetRegistreringer: List<BruksenhetRegistreringRequest>?
)

@Serializable
data class ByggeaarRegistreringRequest(
    val byggeaar: Int?,
    val kildemateriale: KildematerialeKode?,
)

@Serializable
data class EtasjeBetegnelseRequest(
    val etasjeplanKode: String,
    val etasjenummer: Int,
)

@Serializable
data class EtasjeBruksarealRegistreringRequest(
    val bruksareal: Double?,
    val etasjebetegnelse: EtasjeBetegnelseRequest,
)

@Serializable
data class BruksarealRegistreringRequest(
    val totaltBruksareal: Double?,
    val etasjeRegistreringer: List<EtasjeBruksarealRegistreringRequest>?,
    val kildemateriale: KildematerialeKode?,
)

@Serializable
data class VannforsyningRegistreringRequest(
    val vannforsyning: VannforsyningKode?,
    val kildemateriale: KildematerialeKode?,
)

@Serializable
data class AvlopRegistreringRequest(
    val avlop: AvlopKode?,
    val kildemateriale: KildematerialeKode?,
)

@Serializable
data class EnergikildeRegistreringRequest(
    val energikilder: List<EnergikildeKode>?,
    val kildemateriale: KildematerialeKode?,
)

@Serializable
data class OppvarmingRegistreringRequest(
    val oppvarminger: List<OppvarmingKode>?,
    val kildemateriale: KildematerialeKode?,
)

fun EtasjeBruksarealRegistreringRequest.toEtasjeBruksarealRegistrering(): EtasjeBruksarealRegistrering {
    return EtasjeBruksarealRegistrering(
        bruksareal = this.bruksareal,
        etasjebetegnelse = Etasjebetegnelse.of(
            etasjenummer = Etasjenummer.of(this.etasjebetegnelse.etasjenummer),
            etasjeplanKode = EtasjeplanKode.of(this.etasjebetegnelse.etasjeplanKode),
        ),
    )
}

fun BruksenhetRegistreringRequest.toBruksenhetRegistrering(): BruksenhetRegistrering {
    return BruksenhetRegistrering(
        bruksenhetBubbleId = bruksenhetId,
        bruksarealRegistrering = bruksarealRegistrering?.let {
            BruksarealRegistrering(
                totaltBruksareal = it.totaltBruksareal,
                etasjeRegistreringer = it.etasjeRegistreringer?.map {
                    it.toEtasjeBruksarealRegistrering()
                },
                kildemateriale = it.kildemateriale,
            )
        },
        byggeaarRegistrering = byggeaarRegistrering?.let {
            ByggeaarRegistrering(
                byggeaar = it.byggeaar,
                kildemateriale = it.kildemateriale,
            )
        },
        vannforsyningRegistrering = vannforsyningRegistrering?.let {
            VannforsyningRegistrering(
                vannforsyning = it.vannforsyning,
                kildemateriale = it.kildemateriale,
            )
        },
        avlopRegistrering = avlopRegistrering?.let {
            AvlopRegistrering(
                avlop = it.avlop,
                kildemateriale = it.kildemateriale,
            )
        },
        energikildeRegistrering = energikildeRegistrering?.let {
            EnergikildeRegistrering(
                energikilder = it.energikilder,
                kildemateriale = it.kildemateriale,
            )
        },
        oppvarmingRegistrering = oppvarmingRegistrering?.let {
            OppvarmingRegistrering(
                oppvarminger = it.oppvarminger,
                kildemateriale = it.kildemateriale,
            )
        },
    )
}

fun EgenregistreringRequest.toEgenregistrering(eier: String): Egenregistrering =
    Egenregistrering(
        id = UUID.randomUUID(),
        eier = Foedselsnummer(eier),
        registreringstidspunkt = Instant.now(),
        prosess = ProsessKode.Egenregistrering,
        bygningRegistrering = BygningRegistrering(
            bygningId = this.bygningId,
            bruksenhetRegistreringer = this.bruksenhetRegistreringer?.map { bruksenhetRegistrering ->
                bruksenhetRegistrering.toBruksenhetRegistrering()
            } ?: emptyList(),
        ),
    )
