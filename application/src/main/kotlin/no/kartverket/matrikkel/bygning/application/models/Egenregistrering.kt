package no.kartverket.matrikkel.bygning.application.models

import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.time.Year

sealed interface HasKildemateriale {
    val kildemateriale: KildematerialeKode
}

interface HasGyldighetPeriode {
    val gyldighetsaar: Int?
    val opphoersaar: Int?
}

interface HasGyldighetPeriode2<T> {
    val gyldighetsaar: Year?
    val opphoersaar: Year?

    fun withGyldighet(gyldighetsaar: Year?, opphoersaar: Year?) : T
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
    fun checkIsTotaltBruksarealEqualTotaltEtasjeArealIfSet(): Boolean {
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
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?
) : HasKildemateriale, HasGyldighetPeriode

data class AvlopRegistrering(
    val avlop: AvlopKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?
) : HasKildemateriale, HasGyldighetPeriode

sealed class EnergikildeRegistrering {
    data class HarIkke(val kildemateriale: KildematerialeKode) : EnergikildeRegistrering()
    data class Data(val data: List<EnergikildeDataRegistrering>) : EnergikildeRegistrering()
}

data class EnergikildeDataRegistrering(
    val energikilde: EnergikildeKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?
) : HasKildemateriale, HasGyldighetPeriode

sealed class OppvarmingRegistrering {
    data class HarIkke(val kildemateriale: KildematerialeKode) : OppvarmingRegistrering()
    data class Data(val data: List<OppvarmingDataRegistrering>) : OppvarmingRegistrering()
}

data class OppvarmingDataRegistrering(
    val oppvarming: OppvarmingKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?
) : HasKildemateriale, HasGyldighetPeriode

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
    val id: EgenregistreringId,
    val eier: Foedselsnummer,
    val registreringstidspunkt: Instant,
    val prosess: ProsessKode,
    val bruksenhetRegistrering: BruksenhetRegistrering,
)

data class Egenregistrering2(
    val id: EgenregistreringId,
    val eier: Foedselsnummer,
    val registreringstidspunkt: Instant,
    val prosess: ProsessKode,
    val bruksenhetId: BruksenhetBubbleId,
    val feltRegistrering: FeltRegistrering,
)

sealed class FeltRegistrering {
    data class ByggeaarFeltRegistrering(val byggeaar: Int, override val kildemateriale: KildematerialeKode) : HasKildemateriale,
        FeltRegistrering()

    sealed class BruksarealFeltRegistrering(val byggeaar: Int, override val kildemateriale: KildematerialeKode) : HasKildemateriale,
        FeltRegistrering() {
        data class TotaltBruksArealFeltRegistrering(
            val totaltBruksareal: Double,
            override val kildemateriale: KildematerialeKode
        ) :
            HasKildemateriale, FeltRegistrering()

        data class TotaltOgEtasjeBruksArealFeltRegistrering(
            val totaltBruksareal: Double,
            val etasjeRegistreringer: List<EtasjeBruksarealRegistrering>,
            override val kildemateriale: KildematerialeKode
        ) : HasKildemateriale, FeltRegistrering()
    }

    data class VannforsyningFeltRegistrering(
        val vannforsyning: VannforsyningKode,
        override val kildemateriale: KildematerialeKode,
        override val gyldighetsaar: Int,
        override val opphoersaar: Int? = null,
    ) : HasKildemateriale, HasGyldighetPeriode, FeltRegistrering()

    data class AvlopFeltRegistrering(
        val avlop: AvlopKode,
        override val kildemateriale: KildematerialeKode,
        override val gyldighetsaar: Int?,
        override val opphoersaar: Int?
    ) : HasKildemateriale, HasGyldighetPeriode, FeltRegistrering()

    sealed class EnergikildeFeltRegistrering : FeltRegistrering() {
        data class HarIkke(val kildemateriale: KildematerialeKode) : EnergikildeFeltRegistrering()
        data class Data(val data: List<EnergikildeDataRegistrering>) : EnergikildeFeltRegistrering()
    }
}

