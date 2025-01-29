package no.kartverket.matrikkel.bygning.application.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.application.serializers.InstantSerializer
import no.kartverket.matrikkel.bygning.application.serializers.UUIDSerializer
import java.time.Instant
import java.util.*

@Serializable
data class Bygning(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val bygningBubbleId: BygningId,
    val bygningsnummer: Long,
    val etasjer: List<BygningEtasje>,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val bruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<List<Energikilde>> = Multikilde(),
    val oppvarminger: Multikilde<List<Oppvarming>> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

@Serializable
data class Multikilde<T : Any>(val autoritativ: T? = null, val egenregistrert: T? = null) {
    fun withEgenregistrert(verdi: T?): Multikilde<T> = copy(egenregistrert = verdi)
}

@Serializable
data class RegisterMetadata(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: RegistreringAktoer,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?
)

sealed interface Felt<T> {
    val data: T?
    val metadata: RegisterMetadata

    @Serializable
    data class Byggeaar(override val data: Int?, override val metadata: RegisterMetadata) : Felt<Int?>
    @Serializable
    data class Vannforsyning(override val data: VannforsyningKode?, override val metadata: RegisterMetadata) : Felt<VannforsyningKode?>
    @Serializable
    data class Bruksareal(override val data: Double?, override val metadata: RegisterMetadata) : Felt<Double?>
    @Serializable
    data class Avlop(override val data: AvlopKode?, override val metadata: RegisterMetadata) : Felt<AvlopKode?>
    @Serializable
    data class Energikilde(override val data: EnergikildeKode, override val metadata: RegisterMetadata) : Felt<EnergikildeKode?>
    @Serializable
    data class Oppvarming(override val data: OppvarmingKode, override val metadata: RegisterMetadata) : Felt<OppvarmingKode?>
}


@Serializable
data class Bruksenhet(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val bruksenhetBubbleId: BruksenhetId,
    @Serializable(with = UUIDSerializer::class)
    val bygningId: UUID,
    val etasjer: Multikilde<List<BruksenhetEtasje>> = Multikilde(),
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val totaltBruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<List<Energikilde>> = Multikilde(),
    val oppvarminger: Multikilde<List<Oppvarming>> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

