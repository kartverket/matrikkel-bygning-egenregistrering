package no.kartverket.matrikkel.bygning.models

import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import java.time.Instant
import java.time.Year

data class Registreringsinformasjon(
    val registreringstidspunkt: Instant,
)

enum class KildeKode {
    Matrikkelfoert,
    Egenregistrert,
}

data class ValueMetadata(
    val registrertAv: Registreringsinformasjon,
    val kilde: KildeKode,
)

data class Byggeaar(
    val metadata: ValueMetadata,
    val byggeaar: Year,
)

sealed interface Bruksareal {
    val metadata: ValueMetadata
    val totaltBruksareal: Double // TODO: Double er en for unøyaktig type for dette
}

data class TotaltBruksareal(
    override val metadata: ValueMetadata,
    override val totaltBruksareal: Double,
) : Bruksareal

data class Bruksareal2023(
    override val metadata: ValueMetadata,
    val interntBruksareal: Double,
    val eksterntBruksareal: Double,
    val balkongBruksareal: Double,
) : Bruksareal {
    override val totaltBruksareal: Double
        get() = interntBruksareal + eksterntBruksareal + balkongBruksareal
}

// TODO Sette opp DTOer for Bygning/Bruksenhet hentet fra Matrikkel
data class Bygning(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: List<Byggeaar> = listOf(),
    val bruksareal: List<Bruksareal> = listOf(),
    val vannforsyning: VannforsyningKode? = null,
    val avlop: AvlopKode? = null,
) {
    fun withEgenregistrertData(bygningRegistrering: BygningRegistrering): Bygning {
        val registrertAv = Registreringsinformasjon(bygningRegistrering.registreringTidspunkt)
        return this.copy(
            byggeaar = when (val aar = bygningRegistrering.byggeaarRegistrering?.byggeaar) {
                null -> byggeaar
                else -> {
                    byggeaar + Byggeaar(
                        ValueMetadata(
                            registrertAv,
                            KildeKode.Egenregistrert
                        ),
                        Year.of(aar)
                    )
                }
            },
            bruksareal = when (val areal = bygningRegistrering.bruksarealRegistrering?.bruksareal) {
                null -> bruksareal
                else -> bruksareal + TotaltBruksareal(
                    ValueMetadata(
                        registrertAv,
                        KildeKode.Egenregistrert
                    ),
                    areal
                )
            },
            vannforsyning = bygningRegistrering.vannforsyningRegistrering?.vannforsyning,
            avlop = bygningRegistrering.avlopRegistrering?.avlop,
        )
    }

    fun withBruksenheter(bruksenheter: List<Bruksenhet>): Bygning {
        return this.copy(
            bruksenheter = bruksenheter
        )
    }
}

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
    val bruksareal: List<Bruksareal> = listOf(),
    val energikilder: List<EnergikildeKode> = emptyList(),
    val oppvarminger: List<OppvarmingKode> = emptyList(),
) {
    fun withEgenregistrertData(bruksenhetRegistrering: BruksenhetRegistrering): Bruksenhet {
        val registrertAv = Registreringsinformasjon(bruksenhetRegistrering.registreringTidspunkt)
        return this.copy(
            bruksareal = when (val areal = bruksenhetRegistrering.bruksarealRegistrering?.bruksareal) {
                null -> bruksareal
                else -> bruksareal + TotaltBruksareal(
                    ValueMetadata(
                        registrertAv,
                        KildeKode.Egenregistrert
                    ),
                    areal
                )
            },
            energikilder = bruksenhetRegistrering.energikildeRegistrering?.energikilder ?: emptyList(),
            oppvarminger = bruksenhetRegistrering.oppvarmingRegistrering?.oppvarminger ?: emptyList(),
        )
    }
}
