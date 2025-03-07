package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.time.LocalDate
import java.util.*

sealed interface HasKildemateriale {
    val kildemateriale: KildematerialeKode
}

interface HasGyldighetPeriode {
    val gyldighetsdato: LocalDate?
    val opphoersdato: LocalDate?
}

data class ByggeaarRegistrering(
    val byggeaar: Int,
    override val kildemateriale: KildematerialeKode
) : HasKildemateriale

data class BruksarealRegistrering(
    val totaltBruksareal: Double,
    val etasjeRegistreringer: List<EtasjeBruksarealRegistrering>?,
    override val kildemateriale: KildematerialeKode
) : HasKildemateriale {
    fun isTotaltBruksarealEqualTotaltEtasjeArealIfSet(): Boolean {
        if (etasjeRegistreringer == null) {
            return true
        }

        return totaltBruksareal == totaltEtasjeAreal()
    }

    fun totaltEtasjeAreal(): Double {
        return etasjeRegistreringer?.sumOf { it.bruksareal } ?: 0.0
    }
}

data class EtasjeBruksarealRegistrering(
    val bruksareal: Double,
    val etasjebetegnelse: Etasjebetegnelse,
)

data class VannforsyningRegistrering(
    val vannforsyning: VannforsyningKode,
    override val kildemateriale: KildematerialeKode
) : HasKildemateriale

data class AvlopRegistrering(
    val avlop: AvlopKode,
    override val kildemateriale: KildematerialeKode
) : HasKildemateriale

data class EnergikildeRegistrering(
    val energikilder: List<EnergikildeKode>,
    override val kildemateriale: KildematerialeKode
) : HasKildemateriale

data class OppvarmingskildeRegistrering(
    val oppvarming: OppvarmingKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsdato: LocalDate?,
    override val opphoersdato: LocalDate?
) : HasKildemateriale, HasGyldighetPeriode

sealed class OppvarmingRegistrering() {
    data class OppvarmingskilderRegistrering(
        val oppvarmingskilder: List<OppvarmingskildeRegistrering>,
    )

    // TODO Skal en eventuell mangelregistrering ha kilde + gyldighetsperiode?
    data class MangelRegistrering(
        val mangel: String,
        override val kildemateriale: KildematerialeKode,
        override val gyldighetsdato: LocalDate?,
        override val opphoersdato: LocalDate?,
    ) : HasKildemateriale, HasGyldighetPeriode
}

data class BruksenhetRegistrering(
    val bruksenhetBubbleId: BruksenhetBubbleId,
    val bruksarealRegistrering: BruksarealRegistrering?,
    val byggeaarRegistrering: ByggeaarRegistrering?,
    val vannforsyningRegistrering: VannforsyningRegistrering?,
    val avlopRegistrering: AvlopRegistrering?,
    val energikildeRegistrering: EnergikildeRegistrering?,
    val oppvarmingRegistrering: OppvarmingRegistrering?,
)

data class Egenregistrering(
    val id: UUID,
    val eier: Foedselsnummer,
    val registreringstidspunkt: Instant,
    val prosess: ProsessKode,
    val bruksenhetRegistrering: BruksenhetRegistrering,
)
