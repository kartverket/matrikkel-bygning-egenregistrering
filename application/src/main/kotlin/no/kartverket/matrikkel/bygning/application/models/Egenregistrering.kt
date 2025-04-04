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

interface HasGyldighetsaar {
    val gyldighetsaar: Year
}

data class ByggeaarRegistrering(
    val byggeaar: Int,
    override val kildemateriale: KildematerialeKode,
) : HasKildemateriale

data class BruksarealRegistrering(
    val totaltBruksareal: Double,
    val etasjeRegistreringer: List<EtasjeBruksarealRegistrering>?,
    override val kildemateriale: KildematerialeKode,
) : HasKildemateriale {
    fun checkIsTotaltBruksarealEqualTotaltEtasjeArealIfSet(): Boolean {
        if (etasjeRegistreringer == null) {
            return true
        }

        return totaltBruksareal == totaltEtasjeAreal()
    }

    fun totaltEtasjeAreal(): Double = etasjeRegistreringer?.sumOf { it.bruksareal } ?: 0.0
}

data class EtasjeBruksarealRegistrering(
    val bruksareal: Double,
    val etasjebetegnelse: Etasjebetegnelse,
)

data class VannforsyningRegistrering(
    val vannforsyning: VannforsyningKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?,
) : HasKildemateriale,
    HasGyldighetPeriode

data class AvlopRegistrering(
    val avlop: AvlopKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?,
) : HasKildemateriale,
    HasGyldighetPeriode

data class EnergikildeRegistrering(
    val energikilde: EnergikildeKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?,
) : HasKildemateriale,
    HasGyldighetPeriode

data class OppvarmingRegistrering(
    val oppvarming: OppvarmingKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Int?,
    override val opphoersaar: Int?,
) : HasKildemateriale,
    HasGyldighetPeriode

data class BruksenhetRegistrering(
    val bruksenhetBubbleId: BruksenhetBubbleId,
    val bruksarealRegistrering: BruksarealRegistrering?,
    val byggeaarRegistrering: ByggeaarRegistrering?,
    val vannforsyningRegistrering: VannforsyningRegistrering?,
    val avlopRegistrering: AvlopRegistrering?,
    val energikildeRegistrering: List<EnergikildeRegistrering>?,
    val oppvarmingRegistrering: List<OppvarmingRegistrering>?,
)

data class Egenregistrering(
    val id: EgenregistreringId,
    val eier: Foedselsnummer,
    val registreringstidspunkt: Instant,
    val prosess: ProsessKode,
    val bruksenhetRegistrering: BruksenhetRegistrering,
)

sealed interface EgenregistreringBase {
    val id: EgenregistreringId
    val eier: Foedselsnummer
    val registreringstidspunkt: Instant
    val prosess: ProsessKode
    val bruksenhetId: BruksenhetBubbleId
    val type: FeltRegistreringType
}

data class RegistrerEgenregistrering(
    override val id: EgenregistreringId,
    override val eier: Foedselsnummer,
    override val bruksenhetId: BruksenhetBubbleId,
    val feltRegistrering: FeltRegistrering,
) : EgenregistreringBase {
    override val registreringstidspunkt: Instant = Instant.now()
    override val prosess = ProsessKode.Egenregistrering
    override val type = FeltRegistreringType.LEGG_TIL
}

data class KorrigerEgenregistrering(
    override val id: EgenregistreringId,
    override val eier: Foedselsnummer,
    override val bruksenhetId: BruksenhetBubbleId,
    val feltRegistrering: FeltRegistrering,
) : EgenregistreringBase {
    override val registreringstidspunkt: Instant = Instant.now()
    override val prosess = ProsessKode.Egenregistrering
    override val type = FeltRegistreringType.KORRIGER
}

data class AvsluttEgenregistrering(
    override val id: EgenregistreringId,
    override val eier: Foedselsnummer,
    override val bruksenhetId: BruksenhetBubbleId,
    val feltRegistrering: AvsluttFeltRegistrering,
) : EgenregistreringBase {
    override val registreringstidspunkt: Instant = Instant.now()
    override val prosess = ProsessKode.Egenregistrering
    override val type = FeltRegistreringType.AVSLUTT
}

sealed class FeltRegistrering {
    data class ByggeaarFeltRegistrering(
        val byggeaar: Int,
        override val kildemateriale: KildematerialeKode,
    ) : FeltRegistrering(),
        HasKildemateriale

    sealed class BruksarealFeltRegistrering {
        data class TotaltBruksarealFeltRegistrering(
            val totaltBruksareal: Double,
            override val kildemateriale: KildematerialeKode,
        ) : FeltRegistrering(),
            HasKildemateriale

        data class TotaltOgEtasjeBruksarealFeltRegistrering private constructor(
            val totaltBruksareal: Double,
            val etasjeRegistreringer: List<EtasjeBruksarealRegistrering>,
            override val kildemateriale: KildematerialeKode,
        ) : FeltRegistrering(),
            HasKildemateriale {
            companion object {
                fun of(
                    totaltBruksareal: Double,
                    etasjeRegistreringer: List<EtasjeBruksarealRegistrering>,
                    kildemateriale: KildematerialeKode,
                ): TotaltOgEtasjeBruksarealFeltRegistrering {
                    if (etasjeRegistreringer.sumOf { it.bruksareal } != totaltBruksareal) {
                        throw IllegalArgumentException("Totalt BRA stemmer ikke overens med totalen av BRA per etasje")
                    }
                    return TotaltOgEtasjeBruksarealFeltRegistrering(totaltBruksareal, etasjeRegistreringer, kildemateriale)
                }
            }
        }
    }

    data class VannforsyningFeltRegistrering(
        val vannforsyning: VannforsyningKode,
        override val kildemateriale: KildematerialeKode,
        override val gyldighetsaar: Year,
    ) : FeltRegistrering(),
        HasKildemateriale,
        HasGyldighetsaar

    data class AvlopFeltRegistrering(
        val avlop: AvlopKode,
        override val kildemateriale: KildematerialeKode,
        override val gyldighetsaar: Year,
    ) : FeltRegistrering(),
        HasKildemateriale,
        HasGyldighetsaar

    sealed class EnergikildeFeltRegistrering {
        data class HarIkke(
            val kildemateriale: KildematerialeKode,
        ) : FeltRegistrering()

        data class Energikilder private constructor(
            val energikilder: List<EnergikildeDataRegistrering>,
        ) : FeltRegistrering() {
            companion object {
                fun of(energikilder: List<EnergikildeDataRegistrering>): Energikilder {
                    if (energikilder.groupBy { it.energikilde }.any { it.value.size > 1 }) {
                        throw IllegalArgumentException("Energikilder inneholder dupliserte kodeverdier")
                    }
                    return Energikilder(energikilder)
                }
            }
        }
    }

    // TODO: Må støtte HarIkke
    sealed class OppvarmingFeltRegistrering {
        data class HarIkke(
            val kildemateriale: KildematerialeKode,
        ) : FeltRegistrering()

        data class Oppvarming private constructor(
            val oppvarming: List<OppvarmingDataRegistrering>,
        ) : FeltRegistrering() {
            companion object {
                fun of(oppvarming: List<OppvarmingDataRegistrering>): Oppvarming {
                    if (oppvarming.groupBy { it.oppvarming }.any { it.value.size > 1 }) {
                        throw IllegalArgumentException("Oppvarming inneholder dupliserte kodeverdier")
                    }
                    return Oppvarming(oppvarming)
                }
            }
        }
    }
}

data class EnergikildeDataRegistrering(
    val energikilde: EnergikildeKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Year,
) : HasKildemateriale,
    HasGyldighetsaar

data class OppvarmingDataRegistrering(
    val oppvarming: OppvarmingKode,
    override val kildemateriale: KildematerialeKode,
    override val gyldighetsaar: Year,
) : HasKildemateriale,
    HasGyldighetsaar

enum class FeltRegistreringType {
    LEGG_TIL,
    KORRIGER,
    AVSLUTT,
}

sealed class AvsluttFeltRegistrering {
    data class AvsluttVannforsyning(
        val vannforsyning: VannforsyningKode,
        val opphoersaar: Year,
    ) : AvsluttFeltRegistrering()

    data class AvsluttAvlop(
        val avlop: AvlopKode,
        val opphoersaar: Year,
    ) : AvsluttFeltRegistrering()
}
