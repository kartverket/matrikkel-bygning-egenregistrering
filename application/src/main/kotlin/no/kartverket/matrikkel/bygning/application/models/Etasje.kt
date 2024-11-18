package no.kartverket.matrikkel.bygning.application.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode

sealed interface Etasje {
    val etasjenummer: Etasjenummer

    data class BygningEtasje(override val etasjenummer: Etasjenummer, val etasjeId: Long) : Etasje

    data class BruksenhetEtasje(override val etasjenummer: Etasjenummer, val bruksareal: Multikilde<Bruksareal> = Multikilde()) : Etasje
}


@Serializable
data class Etasjeteller private constructor (
    val teller: Int
) {
    companion object {
        fun of(teller: Int): Etasjeteller {
            if (teller > 99 || teller < 0) {
                // TODO Hvordan bør vi egentlig håndtere init feil? Skal vi returne en result her med en custom constructor?
                throw IllegalArgumentException("Ugyldig etasjeteller: $teller")
            }
            return Etasjeteller(teller)
        }
    }
    override fun toString(): String {
        return teller.toString().padStart(2, '0')
    }
}

@Serializable
data class Etasjenummer private constructor(
    val etasjeplanKode: EtasjeplanKode,
    // TODO Har dette et annet vettugt navn enn "etasjeteller"?
    val etasjeteller: Etasjeteller,
) {
    // Bare et eksempel på at det kan være nice å lage Etasjenummer til en egen klasse fremfor å bare bruke en string
    // kunne kanskje til og med vært en compareTo, men anyways!
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
                EtasjeplanKode.Hovedetasje -> annenEtasjenummer.etasjeplanKode != EtasjeplanKode.Loftetasje
                EtasjeplanKode.Underetasje -> annenEtasjenummer.etasjeplanKode == EtasjeplanKode.Kjelleretasje
                EtasjeplanKode.Kjelleretasje -> false
            }
        }
    }

    companion object {
        fun of(etasjenummer: String): Etasjenummer {
            val etasjeplanKode = getEtasjeplanKodeFromString(etasjenummer.slice(0..0))
            val etasjeteller = etasjenummer.slice(1..2).toIntOrNull()

            if (etasjeteller != null && etasjenummer.length == 3) {
                return Etasjenummer(
                    etasjeplanKode = etasjeplanKode,
                    etasjeteller = Etasjeteller.of(etasjeteller),
                )
            }

            throw IllegalArgumentException("Ugyldig etasjenummer: $etasjenummer")
        }

        private fun getEtasjeplanKodeFromString(etasjeplanKode: String): EtasjeplanKode {
            return when (etasjeplanKode) {
                "L" -> EtasjeplanKode.Loftetasje
                "H" -> EtasjeplanKode.Hovedetasje
                "U" -> EtasjeplanKode.Underetasje
                "K" -> EtasjeplanKode.Kjelleretasje
                else -> throw IllegalArgumentException("Ugyldig etasjeplankode: $etasjeplanKode")
            }
        }
    }

    override fun toString(): String {
        return "${etasjeplanKode.toString().first()}${etasjeteller}"
    }

}
