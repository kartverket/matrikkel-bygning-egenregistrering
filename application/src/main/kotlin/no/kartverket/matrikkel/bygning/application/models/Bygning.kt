package no.kartverket.matrikkel.bygning.application.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.EnergikildeOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.OppvarmingOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.time.Year

data class Bygning(
    val id: BygningId,
    val bygningBubbleId: BygningBubbleId,
    val bygningsnummer: Long,
    val etasjer: List<BygningEtasje>,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val bruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<List<Energikilde>> = Multikilde(),
    val oppvarming: Multikilde<List<Oppvarming>> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

data class Bruksenhet(
    val id: BruksenhetId,
    val bruksenhetBubbleId: BruksenhetBubbleId,
    val etasjer: Multikilde<BruksenhetEtasjer> = Multikilde(),
    val byggeaar: Multikilde<Byggeaar> = Multikilde(),
    val totaltBruksareal: Multikilde<Bruksareal> = Multikilde(),
    val energikilder: Multikilde<EnergikildeOpplysning> = Multikilde(),
    val oppvarming: Multikilde<OppvarmingOpplysning> = Multikilde(),
    val vannforsyning: Multikilde<Vannforsyning> = Multikilde(),
    val avlop: Multikilde<Avlop> = Multikilde(),
)

data class Multikilde<T : Any>(val autoritativ: T? = null, val egenregistrert: T? = null) {
    fun withEgenregistrert(verdi: T?): Multikilde<T> = copy(egenregistrert = verdi)
}

data class Gyldighetsperiode private constructor(
    val gyldighetsaar: Year?,
    val opphoersaar: Year?,
) {
    companion object {
        fun of(gyldighetsaar: Year?, opphoersaar: Year?): Gyldighetsperiode {
            if (gyldighetsaar != null && opphoersaar != null) {
                require(gyldighetsaar <= opphoersaar) {
                    "Gyldighetsår må være før eller lik opphørsår"
                }
            }
            return Gyldighetsperiode(
                gyldighetsaar = gyldighetsaar,
                opphoersaar = opphoersaar,
            )
        }

        fun of(gyldighetsaar: Int? = null, opphoersaar: Int? = null): Gyldighetsperiode {
            return of(
                gyldighetsaar = gyldighetsaar?.let { Year.of(it) },
                opphoersaar = opphoersaar?.let { Year.of(it) },
            )
        }
    }
}

data class RegisterMetadata(
    val registreringstidspunkt: Instant,
    val registrertAv: RegistreringAktoer,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?,
    val gyldighetsperiode: Gyldighetsperiode = Gyldighetsperiode.of(),
)

sealed interface Felt<T> {
    val data: T
    val metadata: RegisterMetadata

    data class Byggeaar(
        override val data: Int,
        override val metadata: RegisterMetadata
    ) : Felt<Int>

    data class Vannforsyning(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadata
    ) : Felt<VannforsyningKode>

    data class Bruksareal(
        override val data: Double,
        override val metadata: RegisterMetadata
    ) : Felt<Double>

    data class BruksenhetEtasjer(
        override val data: List<BruksenhetEtasje>,
        override val metadata: RegisterMetadata
    ) : Felt<List<BruksenhetEtasje>>

    data class Avlop(
        override val data: AvlopKode,
        override val metadata: RegisterMetadata
    ) : Felt<AvlopKode>

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
    )
    @JsonSubTypes(
        value = [
            Type(value = EnergikildeOpplysning.HarIkke::class, name = "harIkke"),
            Type(value = EnergikildeOpplysning.Data::class, name = "data"),
        ],
    )
    sealed class EnergikildeOpplysning {
        abstract fun toEnergikilder(): List<Energikilde>

        data class HarIkke private constructor(val metadata: RegisterMetadata) : EnergikildeOpplysning() {

            companion object {
                fun of(metadata: RegisterMetadata) = HarIkke(metadata)
            }

            override fun toEnergikilder(): List<Energikilde> = listOf(
                Energikilde(
                    data = EnergikildeKode.HarIkke,
                    metadata = metadata,
                ),
            )
        }

        data class Data private constructor(val data: List<Energikilde>) : EnergikildeOpplysning() {

            companion object {
                fun of(data: List<Energikilde>): Data {
                    require(
                        !data.map { it.data }
                            .any { it == EnergikildeKode.HarIkke },
                    ) { "${EnergikildeKode.HarIkke} er ikke en gyldig kode for energikilder" }
                    return Data(data)
                }
            }

            override fun toEnergikilder(): List<Energikilde> = data
        }
    }

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
    )
    @JsonSubTypes(
        value = [
            Type(value = EnergikildeOpplysning.HarIkke::class, name = "harIkke"),
            Type(value = EnergikildeOpplysning.Data::class, name = "data"),
        ],
    )
    sealed class OppvarmingOpplysning {
        abstract fun toOppvarming(): List<Oppvarming>

        data class HarIkke private constructor(val metadata: RegisterMetadata) : OppvarmingOpplysning() {

            companion object {
                fun of(metadata: RegisterMetadata) = HarIkke(metadata)
            }

            override fun toOppvarming(): List<Oppvarming> = listOf(
                Oppvarming(
                    data = OppvarmingKode.HarIkke,
                    metadata = metadata,
                ),
            )
        }

        data class Data private constructor(val data: List<Oppvarming>) : OppvarmingOpplysning() {

            companion object {
                fun of(data: List<Oppvarming>): Data {
                    require(
                        !data.map { it.data }
                            .any { it == OppvarmingKode.HarIkke },
                    ) { "${OppvarmingKode.HarIkke} er ikke en gyldig kode for oppvarming" }
                    return Data(data)
                }
            }

            override fun toOppvarming(): List<Oppvarming> = data
        }
    }

    data class Energikilde(
        override val data: EnergikildeKode,
        override val metadata: RegisterMetadata
    ) : Felt<EnergikildeKode>

    data class Oppvarming(
        override val data: OppvarmingKode,
        override val metadata: RegisterMetadata
    ) : Felt<OppvarmingKode>
}
