package no.kartverket.matrikkel.bygning.models

import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import java.time.Instant

// TODO Sette opp DTOer for Bygning/Bruksenhet hentet fra Matrikkel
data class Bygning(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: Byggeaar? = null,
    val bruksareal: Bruksareal? = null,
    val vannforsyning: Vannforsyning? = null,
    val avlop: Avlop? = null,
) {
    fun withBruksenheter(bruksenheter: List<Bruksenhet>): Bygning {
        return this.copy(
            bruksenheter = bruksenheter,
        )
    }
}

data class RegisterMetadata(val registreringstidspunkt: Instant)
data class Bruksareal(val data: Double?, val metadata: RegisterMetadata)
data class Byggeaar(val data: Int?, val metadata: RegisterMetadata)
data class Vannforsyning(val data: VannforsyningKode?, val metadata: RegisterMetadata)
data class Avlop(val data: AvlopKode?, val metadata: RegisterMetadata)
data class Energikilde(val data: EnergikildeKode, val metadata: RegisterMetadata)
data class Oppvarming(val data: OppvarmingKode, val metadata: RegisterMetadata)

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
    val bruksareal: Bruksareal? = null,
    val energikilder: List<Energikilde> = emptyList(),
    val oppvarminger: List<Oppvarming> = emptyList(),
)
