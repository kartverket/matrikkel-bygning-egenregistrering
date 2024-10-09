package no.kartverket.matrikkel.bygning.models

import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import java.time.Instant

data class Bygning(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val bruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<List<Energikilde>> = Multikilde(),
    val oppvarminger: Multikilde<List<Oppvarming>> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

data class Multikilde<T : Any>(val autoritativ: T? = null, val egenregistrert: T? = null) {
    fun withEgenregistrert(verdi: T?): Multikilde<T> = copy(egenregistrert = verdi)
}

data class RegisterMetadata(
    val registreringstidspunkt: Instant,
    val registrertAv: RegistreringAktoer
)

data class Bruksareal(val data: Double?, val metadata: RegisterMetadata)
data class Byggeaar(val data: Int?, val metadata: RegisterMetadata)
data class Vannforsyning(val data: VannforsyningKode?, val metadata: RegisterMetadata)
data class Avlop(val data: AvlopKode?, val metadata: RegisterMetadata)
data class Energikilde(val data: EnergikildeKode, val metadata: RegisterMetadata)
data class Oppvarming(val data: OppvarmingKode, val metadata: RegisterMetadata)

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
    val bruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<List<Energikilde>> = Multikilde(),
    val oppvarminger: Multikilde<List<Oppvarming>> = Multikilde(),
)
