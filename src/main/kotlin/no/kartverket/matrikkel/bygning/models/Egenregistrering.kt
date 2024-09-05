package no.kartverket.matrikkel.bygning.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.serializers.UUIDSerializer
import java.time.Instant
import java.util.*

@Serializable
data class ByggeaarRegistrering(
    val byggeaar: Int,
)

@Serializable
data class BruksarealRegistrering(
    val bruksareal: Double,
)

@Serializable
data class VannforsyningRegistrering(
    val vannforsyning: VannforsyningKode,
)

@Serializable
data class AvlopRegistrering(
    val avlop: AvlopKode,
)

@Serializable
data class EnergikildeRegistrering(
    val energikilder: List<EnergikildeKode>,
)

@Serializable
data class OppvarmingRegistrering(
    val oppvarminger: List<OppvarmingKode>,
)

sealed interface Registrering {
    val registreringId: UUID
    val bruksarealRegistrering: BruksarealRegistrering?
}

@Serializable
data class BygningRegistrering(
    @Serializable(with = UUIDSerializer::class)
    override val registreringId: UUID,
    val bygningId: Long,
    override val bruksarealRegistrering: BruksarealRegistrering?,
    val byggeaarRegistrering: ByggeaarRegistrering?,
    val vannforsyningRegistrering: VannforsyningRegistrering?,
    val avlopRegistrering: AvlopRegistrering?,
) : Registrering

@Serializable
data class BruksenhetRegistrering(
    @Serializable(with = UUIDSerializer::class)
    override val registreringId: UUID,
    val bruksenhetId: Long,
    override val bruksarealRegistrering: BruksarealRegistrering?,
    val energikildeRegistrering: EnergikildeRegistrering?,
    val oppvarmingRegistrering: OppvarmingRegistrering?,
) : Registrering

// Ikke så fan av at egenregistrering har bygningID på toppnivå, og en bygning også har bygningId
// Per nå trenger man det for at bygningRegistreringer kan hentes opp på bygningId, samt at man må hente riktig bygning og bruksenheter
// når man egenregistrerer bruksenheter
data class Egenregistrering(
    val id: UUID,
    val registrerer: String,
    val registreringTidspunkt: Instant,
    val bygningId: Long,
    val bygningRegistrering: BygningRegistrering?,
    val bruksenhetRegistreringer: List<BruksenhetRegistrering>?
)
