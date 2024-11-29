package no.kartverket.matrikkel.bygning.application.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import java.time.Instant
import java.util.*

@Serializable
data class ByggeaarRegistrering(
    val byggeaar: Int?,
    val kildemateriale: KildematerialeKode?
)

@Serializable
data class BruksarealRegistrering(
    val totaltBruksareal: Double?,
    val etasjeRegistreringer: List<EtasjeBruksarealRegistrering>?
)

@Serializable
data class EtasjeBruksarealRegistrering(
    val bruksareal: Double?,
    val etasjebetegnelse: Etasjebetegnelse
)

@Serializable
data class VannforsyningRegistrering(
    val vannforsyning: VannforsyningKode?,
    val kildemateriale: KildematerialeKode?
)

@Serializable
data class AvlopRegistrering(
    val avlop: AvlopKode?,
    val kildemateriale: KildematerialeKode?
)

@Serializable
data class EnergikildeRegistrering(
    val energikilder: List<EnergikildeKode>?,
    val kildemateriale: KildematerialeKode?
)

@Serializable
data class OppvarmingRegistrering(
    val oppvarminger: List<OppvarmingKode>?,
    val kildemateriale: KildematerialeKode?
)


@Serializable
data class BygningRegistrering(
    val bygningId: Long,
    val bruksenhetRegistreringer: List<BruksenhetRegistrering>
)

@Serializable
data class BruksenhetRegistrering(
    val bruksenhetId: Long,
    val bruksarealRegistrering: BruksarealRegistrering?,
    val byggeaarRegistrering: ByggeaarRegistrering?,
    val vannforsyningRegistrering: VannforsyningRegistrering?,
    val avlopRegistrering: AvlopRegistrering?,
    val energikildeRegistrering: EnergikildeRegistrering?,
    val oppvarmingRegistrering: OppvarmingRegistrering?,
)

data class Egenregistrering(
    val id: UUID,
    val eier: Foedselsnummer,
    val registreringstidspunkt: Instant,
    val bygningRegistrering: BygningRegistrering,
)
