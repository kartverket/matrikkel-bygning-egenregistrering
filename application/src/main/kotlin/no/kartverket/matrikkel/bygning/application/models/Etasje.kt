package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode

data class BygningEtasje(val etasjebetegnelse: Etasjebetegnelse, val etasjeId: Long)

data class BruksenhetEtasje(val etasjebetegnelse: Etasjebetegnelse, val bruksareal: Double)

data class Etasjenummer private constructor(val loepenummer: Int) {
    companion object {
        fun of(loepenummer: Int): Etasjenummer {
            if (loepenummer > 99 || loepenummer <= 0) {
                throw IllegalArgumentException("Ugyldig etasjenummer: $loepenummer")
            }

            return Etasjenummer(loepenummer)
        }
    }

    override fun toString(): String {
        return loepenummer.toString().padStart(2, '0')
    }
}

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
                etasjenummer.loepenummer > annenEtasjebetegnelse.etasjenummer.loepenummer
            } else {
                etasjenummer.loepenummer < annenEtasjebetegnelse.etasjenummer.loepenummer
            }
        } else {
            when (etasjeplanKode) {
                EtasjeplanKode.Loftetasje -> true
                EtasjeplanKode.Hovedetasje -> annenEtasjebetegnelse.etasjeplanKode != EtasjeplanKode.Loftetasje
                EtasjeplanKode.Underetasje -> annenEtasjebetegnelse.etasjeplanKode == EtasjeplanKode.Kjelleretasje
                EtasjeplanKode.Kjelleretasje -> false
                EtasjeplanKode.IkkeOppgitt -> false
            }
        }
    }

    companion object {
        fun of(etasjenummer: Etasjenummer, etasjeplanKode: EtasjeplanKode): Etasjebetegnelse {
            return Etasjebetegnelse(
                etasjeplanKode = etasjeplanKode,
                etasjenummer = etasjenummer,
            )
        }
    }

    override fun toString(): String {
        return "${etasjeplanKode}${etasjenummer.loepenummer}"
    }
}
