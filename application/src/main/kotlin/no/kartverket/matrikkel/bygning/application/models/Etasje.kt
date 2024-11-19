package no.kartverket.matrikkel.bygning.application.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import kotlin.text.slice

data class BygningEtasje(val etasjeBetegnelse: Etasjebetegnelse, val etasjeId: Long)

data class BruksenhetEtasje(val etasjeBetegnelse: Etasjebetegnelse, val bruksareal: Bruksareal?)

@ConsistentCopyVisibility
@Serializable
data class Etasjenummer private constructor(val teller: Int) {
    companion object {
        fun of(teller: Int): Etasjenummer {
            // TODO Ta opp med fag: Trenger dette egentlig være en begrensning? Hva om Norge i 2050 får en bygning som har 101 hovedetasjer?
            if (teller > 99 || teller < 0) {
                throw IllegalArgumentException("Ugylding etasjenummer: $teller")
            }

            return Etasjenummer(teller)
        }
    }

    override fun toString(): String {
        return teller.toString().padStart(2, '0')
    }
}

@ConsistentCopyVisibility
@Serializable
data class Etasjebetegnelse private constructor(
    val etasjeplanKode: EtasjeplanKode,
    val etasjenummer: Etasjenummer,
) {
    // Bare et eksempel på at det kan være nice å lage Etasjenummer til en egen klasse fremfor å bare bruke en string
    // kunne kanskje til og med vært en compareTo, men anyways!
    fun isEtasjenummerOverAnnenEtasjenummer(annenEtasjebetegnelse: Etasjebetegnelse): Boolean {
        val etasjeplanKoderOekende = listOf(EtasjeplanKode.Hovedetasje, EtasjeplanKode.Loftetasje)

        return if (etasjeplanKode == annenEtasjebetegnelse.etasjeplanKode) {
            if (etasjeplanKoderOekende.contains(etasjeplanKode)) {
                etasjenummer.teller > annenEtasjebetegnelse.etasjenummer.teller
            } else {
                etasjenummer.teller < annenEtasjebetegnelse.etasjenummer.teller
            }
        } else {
            when (etasjeplanKode) {
                EtasjeplanKode.Loftetasje -> true
                EtasjeplanKode.Hovedetasje -> annenEtasjebetegnelse.etasjeplanKode != EtasjeplanKode.Loftetasje
                EtasjeplanKode.Underetasje -> annenEtasjebetegnelse.etasjeplanKode == EtasjeplanKode.Kjelleretasje
                EtasjeplanKode.Kjelleretasje -> false
            }
        }
    }

    companion object {
        fun of(etasjeBetegnelse: String): Etasjebetegnelse {
            val etasjeplanKode = getEtasjeplanKodeFromString(etasjeBetegnelse.slice(0..0))

            val etasjenummer = etasjeBetegnelse.slice(1..2).toIntOrNull()

            if (etasjenummer != null && etasjeBetegnelse.length == 3) {
                val etasjenummer = Etasjenummer.of(etasjenummer)

                return Etasjebetegnelse(
                    etasjeplanKode = etasjeplanKode,
                    etasjenummer = etasjenummer,
                )
            }

            throw IllegalArgumentException("Ugyldig etasjebetegnelse: $etasjeBetegnelse")
        }

        fun of(etasjenummer: Int, etasjeplanKode: EtasjeplanKode): Etasjebetegnelse {
            return Etasjebetegnelse(
                etasjeplanKode = etasjeplanKode,
                etasjenummer = Etasjenummer.of(etasjenummer),
            )
        }


        fun getEtasjeplanKodeFromString(etasjeplanKode: String): EtasjeplanKode {
            return when (etasjeplanKode) {
                "H" -> EtasjeplanKode.Hovedetasje
                "U" -> EtasjeplanKode.Underetasje
                "L" -> EtasjeplanKode.Loftetasje
                "K" -> EtasjeplanKode.Kjelleretasje
                else -> throw IllegalArgumentException("Ugyldig etasjeplankode: $etasjeplanKode")
            }
        }
    }

    override fun toString(): String {
        return "${etasjeplanKode.toString().first()}${etasjenummer}"
    }
}
