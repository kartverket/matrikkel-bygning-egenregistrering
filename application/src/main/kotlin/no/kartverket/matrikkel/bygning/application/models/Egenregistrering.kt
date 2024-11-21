package no.kartverket.matrikkel.bygning.application.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.util.*

@Serializable
data class ByggeaarRegistrering(
    val byggeaar: Int?,
)

@Serializable
data class BruksarealRegistrering(
    val totaltBruksareal: Double?,
    val etasjeRegistreringer: List<EtasjeBruksarealRegistrering>?
)

@Serializable
data class EtasjeBruksarealRegistrering(
    val bruksareal: Double?,
    val etasjeBetegnelse: Etasjebetegnelse
)

@Serializable
data class VannforsyningRegistrering(
    val vannforsyning: VannforsyningKode?,
)

@Serializable
data class AvlopRegistrering(
    val avlop: AvlopKode?,
)

@Serializable
data class EnergikildeRegistrering(
    val energikilder: List<EnergikildeKode>?,
)

@Serializable
data class OppvarmingRegistrering(
    val oppvarminger: List<OppvarmingKode>?,
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
