package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.time.LocalDate

data class Bygning(
    val id: BygningId,
    val bygningBubbleId: BygningBubbleId,
    val bygningsnummer: Long,
    val etasjer: List<BygningEtasje>,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val bruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<Energikilde> = Multikilde(),
    val oppvarming: Multikilde<List<Oppvarming>> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

data class Bruksenhet(
    val id: BruksenhetId,
    val bruksenhetBubbleId: BruksenhetBubbleId,
    val etasjer: Multikilde<BruksenhetEtasjer> = Multikilde(),
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val totaltBruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<Energikilde> = Multikilde(),
    val oppvarming: Multikilde<List<Oppvarming>> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

data class Multikilde<T : Any>(val autoritativ: T? = null, val egenregistrert: T? = null) {
    fun withEgenregistrert(verdi: T?): Multikilde<T> = copy(egenregistrert = verdi)
}

// TODO Er det noen vurderinger som må tas ifbm. om datoer er inklusiv eller eksklusiv?
data class Gyldighetsperiode(
    val gyldighetsdato: LocalDate? = null,
    val opphoersdato: LocalDate? = null,
) {
    // TODO Burde vi egentlig bruke init?
    init {
        if (gyldighetsdato != null && opphoersdato != null) {
            require(gyldighetsdato < opphoersdato) { "Gyldighetsdato må være før opphørsdato" }
        }
    }
}

data class RegisterMetadata(
    val registreringstidspunkt: Instant,
    val registrertAv: RegistreringAktoer,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?,
    val gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode(),
)

sealed interface Felt<T> {
    val data: T
    val metadata: RegisterMetadata

    data class Byggeaar(
        override val data: Int,
        override val metadata: RegisterMetadata
    ) : Felt<Int>

    data class Vannforsyning(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadata
    ) : Felt<VannforsyningKode>

    data class Bruksareal(
        override val data: Double,
        override val metadata: RegisterMetadata
    ) : Felt<Double>

    data class BruksenhetEtasjer(
        override val data: List<BruksenhetEtasje>,
        override val metadata: RegisterMetadata
    ) : Felt<List<BruksenhetEtasje>>

    data class Avlop(
        override val data: AvlopKode,
        override val metadata: RegisterMetadata
    ) : Felt<AvlopKode>

    data class Energikilde(
        override val data: List<EnergikildeKode>,
        override val metadata: RegisterMetadata
    ) : Felt<List<EnergikildeKode>>

    data class Oppvarming(
        override val data: OppvarmingKode,
        override val metadata: RegisterMetadata
    ) : Felt<OppvarmingKode>
}
