package no.kartverket.matrikkel.bygning.application.models

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.util.*

// Er HasKildemateriale det beste navnet her?
sealed interface HasKildemateriale {
    // Skal den være nullable?
    val kildemateriale: KildematerialeKode?
}

@Serializable
data class ByggeaarRegistrering(
    val byggeaar: Int?,
    override val kildemateriale: KildematerialeKode?
) : HasKildemateriale

@Serializable
data class BruksarealRegistrering(
    val totaltBruksareal: Double?,
    val etasjeRegistreringer: List<EtasjeBruksarealRegistrering>?,
    override val kildemateriale: KildematerialeKode?
) : HasKildemateriale {
    fun isTotaltBruksarealEqualTotaltEtasjeArealIfSet(): Boolean {
        if (totaltBruksareal == null || etasjeRegistreringer == null) {
            return true
        }

        return totaltBruksareal == totaltEtasjeAreal()
    }

    fun totaltEtasjeAreal(): Double {
        return etasjeRegistreringer?.sumOf { it.bruksareal ?: 0.0 } ?: 0.0
    }
}

@Serializable
data class EtasjeBruksarealRegistrering(
    val bruksareal: Double?,
    val etasjebetegnelse: Etasjebetegnelse,
)

@Serializable
data class VannforsyningRegistrering(
    val vannforsyning: VannforsyningKode?,
    override val kildemateriale: KildematerialeKode?
) : HasKildemateriale

@Serializable
data class AvlopRegistrering(
    val avlop: AvlopKode?,
    override val kildemateriale: KildematerialeKode?
) : HasKildemateriale

@Serializable
data class EnergikildeRegistrering(
    val energikilder: List<EnergikildeKode>?,
    override val kildemateriale: KildematerialeKode?
) : HasKildemateriale

@Serializable
data class OppvarmingRegistrering(
    val oppvarminger: List<OppvarmingKode>?,
    override val kildemateriale: KildematerialeKode?
) : HasKildemateriale


@Serializable
data class BygningRegistrering(
    val bygningId: Long,
    val bruksenhetRegistreringer: List<BruksenhetRegistrering>
)

@Serializable
data class BruksenhetRegistrering(
    val bruksenhetBubbleId: Long,
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
    val bygningRegistrering: BygningRegistrering,
)
