package no.kartverket.matrikkel.bygning.application.models

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode

data class BygningEtasje(val etasjeIdentifikator: EtasjeIdentifikator, val etasjeId: Long)

data class BruksenhetEtasje(val etasjeIdentifikator: EtasjeIdentifikator, val bruksareal: Bruksareal?)

@Serializable
class Etasjenummer(val teller: Int) {
    // Dette unngår jo ikke egentlig at noen bare lager Etasjenummer(101) som blir "gyldig".
    // Er det noen måte å force dette? Unngå en konstruktør av noe slag?
    companion object {
        fun of(teller: Int): Result<Etasjenummer, ErrorDetail> {
            // TODO Ta opp med fag: Trenger dette egentlig være en begrensning? Hva om Norge i 2050 får en bygning som har 101 hovedetasjer?
            if (teller > 99 || teller < 0) {
                return Err(
                    ErrorDetail(
                        detail = "Etasjenummer kan kun være et tall mellom 1 og 99",
                    ),
                )
            }

            return Ok(Etasjenummer(teller))
        }
    }

    override fun toString(): String {
        return teller.toString().padStart(2, '0')
    }
}

@Serializable
data class EtasjeIdentifikator(
    val etasjeplanKode: EtasjeplanKode,
    val etasjenummer: Etasjenummer,
) {
    // Bare et eksempel på at det kan være nice å lage Etasjenummer til en egen klasse fremfor å bare bruke en string
    // kunne kanskje til og med vært en compareTo, men anyways!
    fun isEtasjenummerOverAnnenEtasjenummer(annenEtasjeIdentifikator: EtasjeIdentifikator): Boolean {
        val etasjeplanKoderOekende = listOf(EtasjeplanKode.Hovedetasje, EtasjeplanKode.Loftetasje)

        return if (etasjeplanKode == annenEtasjeIdentifikator.etasjeplanKode) {
            if (etasjeplanKoderOekende.contains(etasjeplanKode)) {
                etasjenummer.teller > annenEtasjeIdentifikator.etasjenummer.teller
            } else {
                etasjenummer.teller < annenEtasjeIdentifikator.etasjenummer.teller
            }
        } else {
            when (etasjeplanKode) {
                EtasjeplanKode.Loftetasje -> true
                EtasjeplanKode.Hovedetasje -> annenEtasjeIdentifikator.etasjeplanKode != EtasjeplanKode.Loftetasje
                EtasjeplanKode.Underetasje -> annenEtasjeIdentifikator.etasjeplanKode == EtasjeplanKode.Kjelleretasje
                EtasjeplanKode.Kjelleretasje -> false
            }
        }
    }

    companion object {
        fun getEtasjeplanKodeFromString(etasjeplanKode: String): EtasjeplanKode? {
            return when (etasjeplanKode) {
                "L" -> EtasjeplanKode.Loftetasje
                "H" -> EtasjeplanKode.Hovedetasje
                "U" -> EtasjeplanKode.Underetasje
                "K" -> EtasjeplanKode.Kjelleretasje
                else -> null
            }
        }


        fun String.toEtasjeIdentifikator(): Result<EtasjeIdentifikator, ErrorDetail> {
            val etasjeplanKode = getEtasjeplanKodeFromString(this.slice(0..0))

            val etasjenummer = this.slice(1..2).toIntOrNull()

            if (etasjeplanKode != null && etasjenummer != null && this.length == 3) {
                val etasjenummerIfValid = Etasjenummer.of(etasjenummer)

                return etasjenummerIfValid.map {
                    EtasjeIdentifikator(
                        etasjeplanKode = etasjeplanKode,
                        etasjenummer = it,
                    )
                }
            }

            return Err(
                ErrorDetail(
                    detail = "Etasjenummer var ikke gyldig, forventet format er for eksempel: H10, K01, L02",
                ),
            )
        }

    }

    override fun toString(): String {
        return "${etasjeplanKode.toString().first()}${etasjenummer}"
    }
}
