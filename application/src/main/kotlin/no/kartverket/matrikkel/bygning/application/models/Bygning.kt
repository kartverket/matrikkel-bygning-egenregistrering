package no.kartverket.matrikkel.bygning.application.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer.Companion.getEtasjeplanKodeFromString
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
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
    val registreringstidspunkt: Instant, val registrertAv: RegistreringAktoer
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
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val bruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<List<Energikilde>> = Multikilde(),
    val oppvarminger: Multikilde<List<Oppvarming>> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

@Serializable
data class Etasjeteller(
    val teller: Int
) {
    init {
        if (teller > 99 || teller < 0) {
            throw RuntimeException("Isje lov")
        }
    }

    override fun toString(): String {
        return teller.toString().padStart(2, '0')
    }
}

@Serializable
data class Etasjenummer(
    val etasjeplanKode: EtasjeplanKode,
    // dette har sikkert et annet navn enn teller
    val etasjeteller: Etasjeteller,
) {
    // Eksempel på fordel ved å sette plankode + teller til en egen klasse
    fun isEtasjenummerOverAnnenEtasjenummer(annenEtasjenummer: Etasjenummer): Boolean {
        val etasjeplanKoderOekende = listOf(EtasjeplanKode.Hovedetasje, EtasjeplanKode.Loftetasje)

        return if (etasjeplanKode == annenEtasjenummer.etasjeplanKode) {
            if (etasjeplanKoderOekende.contains(etasjeplanKode)) {
                etasjeteller.teller > annenEtasjenummer.etasjeteller.teller
            } else {
                etasjeteller.teller < annenEtasjenummer.etasjeteller.teller
            }
        } else {
            when (etasjeplanKode) {
                EtasjeplanKode.Loftetasje -> true
                EtasjeplanKode.Hovedetasje -> annenEtasjenummer.etasjeplanKode == EtasjeplanKode.Loftetasje
                EtasjeplanKode.Kjelleretasje -> false
            }
        }
    }

    companion object {
        fun getEtasjeplanKodeFromString(etasjeplanKode: String): EtasjeplanKode? {
            return when (etasjeplanKode) {
                "L" -> EtasjeplanKode.Loftetasje
                "H" -> EtasjeplanKode.Hovedetasje
                "K" -> EtasjeplanKode.Kjelleretasje
                else -> null
            }
        }
    }



}

fun String.toEtasjenummer(): Etasjenummer? {
    val etasjeplanKode = getEtasjeplanKodeFromString(this.slice(0..0))

    val etasjeteller = this.slice(1..2).toInt()

    if (etasjeplanKode != null) {
        return Etasjenummer(
            etasjeplanKode = etasjeplanKode,
            etasjeteller = Etasjeteller(etasjeteller),
        )
    }

    return null
}
