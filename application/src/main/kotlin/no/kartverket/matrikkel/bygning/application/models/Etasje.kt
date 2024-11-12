package no.kartverket.matrikkel.bygning.application.models

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer.Companion.getEtasjeplanKodeFromString
import no.kartverket.matrikkel.bygning.application.models.error.ErrorDetail
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode

sealed interface Etasje {
    val etasjenummer: Etasjenummer

    data class BygningEtasje(override val etasjenummer: Etasjenummer, val etasjeId: Long) : Etasje

    data class BruksenhetEtasje(override val etasjenummer: Etasjenummer, val bruksareal: Multikilde<Bruksareal> = Multikilde()) : Etasje
}


@Serializable
data class Etasjeteller(
    val teller: Int
) {
    // Trenger dette egentlig være en begrensning? Hva om Norge i 2050 får en bygning som er 101 hovedetasjer?
    init {
        if (teller > 99 || teller < 0) {
            // TODO Hvordan bør vi egentlig håndtere init feil? Skal vi returne en result her med en custom constructor?
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
        fun getEtasjeplanKodeFromString(etasjeplanKode: String): EtasjeplanKode? {
            return when (etasjeplanKode) {
                "L" -> EtasjeplanKode.Loftetasje
                "H" -> EtasjeplanKode.Hovedetasje
                "U" -> EtasjeplanKode.Underetasje
                "K" -> EtasjeplanKode.Kjelleretasje
                else -> null
            }
        }
    }

    override fun toString(): String {
        return "${etasjeplanKode.toString().first()}${etasjeteller}"
    }

}

// TODO Validering før man kommer hit? Eller returnere result?
fun String.toEtasjenummer(): Result<Etasjenummer, ErrorDetail> {
    val etasjeplanKode = getEtasjeplanKodeFromString(this.slice(0..0))

    val etasjeteller = this.slice(1..2).toIntOrNull()

    if (etasjeplanKode != null && etasjeteller != null && this.length == 3) {
        return Ok(
            Etasjenummer(
                etasjeplanKode = etasjeplanKode,
                etasjeteller = Etasjeteller(etasjeteller),
            ),
        )
    }

    return Err(
        ErrorDetail(
            detail = "Etasjenummer var ikke gyldig, forventet format er for eksempel: H10, K01, L02",
        ),
    )
}
