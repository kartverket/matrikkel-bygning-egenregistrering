package no.kartverket.matrikkel.bygning.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.RegistreringAktoer.*
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import java.time.Instant
import java.util.*

@Serializable
data class ByggeaarRegistrering(
    val byggeaar: Int?,
)

@Serializable
data class BruksarealRegistrering(
    val bruksareal: Double?,
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
