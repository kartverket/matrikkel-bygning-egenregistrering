package no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering

import io.github.smiley4.schemakenerator.core.annotations.Name
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import no.kartverket.matrikkel.bygning.application.models.AvinstalleringKommando
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering2
import no.kartverket.matrikkel.bygning.application.models.EnergikildeDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.util.*

@Serializable
data class EgenregistreringRequest(
    val bruksenhetId: Long,
    val bruksarealRegistrering: BruksarealRegistreringRequest?,
    val byggeaarRegistrering: ByggeaarRegistreringRequest?,
    val vannforsyningRegistrering: VannforsyningRegistreringRequest?,
    val avlopRegistrering: AvlopRegistreringRequest?,
    val oppvarmingRegistrering: OppvarmingRegistreringRequest?,
    val energikildeRegistrering: EnergikildeRegistreringRequest?,
)

@Serializable
data class EgenregistreringRequest2(
    val bruksenhetId: Long,
    val feltRegistrering: FeltRegistreringRequest
)

@Serializable
sealed class FeltRegistreringRequest {

    @Serializable
    @SerialName("vannforsyning")
    @Name("vannforsyning")
    data class VannforsyningFeltRegistreringRequest(
        val vannforsyning: VannforsyningKode,
        val kildemateriale: KildematerialeKode,
        val gyldighetsaar: Int,
    ) : FeltRegistreringRequest()

    @Serializable
    sealed class EnergikildeFeltRegistreringRequest : FeltRegistreringRequest() {
        @Serializable
        @SerialName("harIkkeEnergikilder")
        @Name("harIkkeEnergikilder")
        data class HarIkke(val kildemateriale: KildematerialeKode) : EnergikildeFeltRegistreringRequest()

        @Serializable
        @SerialName("energikilder")
        @Name("energikilder")
        data class Energikilder(val energikilder: List<EnergikildeFeltDataRequest>) : EnergikildeFeltRegistreringRequest()

        @Serializable
        data class EnergikildeFeltDataRequest(
            val energikilde: EnergikildeKode,
            val kildemateriale: KildematerialeKode,
            val gyldighetsaar: Int,
        )

    }
}

@Serializable
sealed class AvinstallerRequest {
    abstract val bruksenhetId: Long

    @Serializable
    @SerialName("vannforsyning")
    @Name("vannforsyning")
    data class AvinstallerVannforsyningRequest(
        override val bruksenhetId: Long,
        val vannforsyning: VannforsyningKode,
        val opphoersaar: Int
    ) : AvinstallerRequest()
}

@Serializable
data class ByggeaarRegistreringRequest(
    val byggeaar: Int,
    val kildemateriale: KildematerialeKode,
)

@Serializable
data class EtasjeBetegnelseRequest(
    val etasjeplanKode: String,
    val etasjenummer: Int,
)

@Serializable
data class EtasjeBruksarealRegistreringRequest(
    val bruksareal: Double,
    val etasjebetegnelse: EtasjeBetegnelseRequest,
)

@Serializable
data class BruksarealRegistreringRequest(
    val totaltBruksareal: Double,
    val etasjeRegistreringer: List<EtasjeBruksarealRegistreringRequest>?,
    val kildemateriale: KildematerialeKode,
)

@Serializable
data class VannforsyningRegistreringRequest(
    val vannforsyning: VannforsyningKode,
    val kildemateriale: KildematerialeKode,
    val gyldighetsaar: Int? = null,
    val opphoersaar: Int? = null
)

@Serializable
data class AvlopRegistreringRequest(
    val avlop: AvlopKode,
    val kildemateriale: KildematerialeKode,
    val gyldighetsaar: Int? = null,
    val opphoersaar: Int? = null
)


fun AvinstallerRequest.toAvinstallering() = when(this) {
    is AvinstallerRequest.AvinstallerVannforsyningRequest -> AvinstalleringKommando.AvinstallerVannforsyning(
        bruksenhetId = BruksenhetBubbleId(this.bruksenhetId),
        vannforsyning = this.vannforsyning,
        opphoersaar = this.opphoersaar,
    )
}

@Serializable
sealed class EnergikildeRegistreringRequest {

    @Serializable
    @SerialName("harIkke")
    @Name("harIkke")
    data class HarIkke(val kildemateriale: KildematerialeKode) : EnergikildeRegistreringRequest()

    @Serializable
    @SerialName("data")
    @Name("data")
    data class Data(val data: List<EnergikildeDataRequest>) : EnergikildeRegistreringRequest()
}

@Serializable
data class EnergikildeDataRequest(
    val energikilde: EnergikildeKode,
    val kildemateriale: KildematerialeKode,
    val gyldighetsaar: Int? = null,
    val opphoersaar: Int? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class OppvarmingRegistreringRequest {

    @Serializable
    @SerialName("harIkke")
    data class HarIkke(val kildemateriale: KildematerialeKode) : OppvarmingRegistreringRequest()

    @Serializable
    @SerialName("data")
    data class Data(val data: List<OppvarmingDataRequest>) : OppvarmingRegistreringRequest()
}

@Serializable
data class OppvarmingDataRequest(
    val oppvarming: OppvarmingKode,
    val kildemateriale: KildematerialeKode,
    val gyldighetsaar: Int? = null,
    val opphoersaar: Int? = null
)

fun EtasjeBruksarealRegistreringRequest.toEtasjeBruksarealRegistrering(): EtasjeBruksarealRegistrering {
    return EtasjeBruksarealRegistrering(
        bruksareal = this.bruksareal,
        etasjebetegnelse = Etasjebetegnelse.of(
            etasjenummer = Etasjenummer.of(this.etasjebetegnelse.etasjenummer),
            etasjeplanKode = EtasjeplanKode.of(this.etasjebetegnelse.etasjeplanKode),
        ),
    )
}

fun EgenregistreringRequest.toBruksenhetRegistrering(): BruksenhetRegistrering {
    return BruksenhetRegistrering(
        bruksenhetBubbleId = BruksenhetBubbleId(bruksenhetId),
        bruksarealRegistrering = bruksarealRegistrering?.let { bruksarealRegistrering ->
            BruksarealRegistrering(
                totaltBruksareal = bruksarealRegistrering.totaltBruksareal,
                etasjeRegistreringer = bruksarealRegistrering.etasjeRegistreringer?.map {
                    it.toEtasjeBruksarealRegistrering()
                },
                kildemateriale = bruksarealRegistrering.kildemateriale,
            )
        },
        byggeaarRegistrering = byggeaarRegistrering?.let {
            ByggeaarRegistrering(
                byggeaar = it.byggeaar,
                kildemateriale = it.kildemateriale,
            )
        },
        vannforsyningRegistrering = vannforsyningRegistrering?.let {
            VannforsyningRegistrering(
                vannforsyning = it.vannforsyning,
                kildemateriale = it.kildemateriale,
                gyldighetsaar = it.gyldighetsaar,
                opphoersaar = it.opphoersaar,
            )
        },
        avlopRegistrering = avlopRegistrering?.let {
            AvlopRegistrering(
                avlop = it.avlop,
                kildemateriale = it.kildemateriale,
                gyldighetsaar = it.gyldighetsaar,
                opphoersaar = it.opphoersaar,
            )
        },
        energikildeRegistrering = energikildeRegistrering?.let { energikildeRegistrering ->
            when (energikildeRegistrering) {
                is EnergikildeRegistreringRequest.HarIkke -> EnergikildeRegistrering.HarIkke(
                    kildemateriale = energikildeRegistrering.kildemateriale,
                )

                is EnergikildeRegistreringRequest.Data -> EnergikildeRegistrering.Data(
                    data = energikildeRegistrering.data.map {
                        EnergikildeDataRegistrering(
                            energikilde = it.energikilde,
                            kildemateriale = it.kildemateriale,
                            gyldighetsaar = it.gyldighetsaar,
                            opphoersaar = it.opphoersaar,
                        )
                    },
                )
            }
        },
        oppvarmingRegistrering = oppvarmingRegistrering?.let { oppvarmingRegistrering ->
            when (oppvarmingRegistrering) {
                is OppvarmingRegistreringRequest.HarIkke -> OppvarmingRegistrering.HarIkke(
                    kildemateriale = oppvarmingRegistrering.kildemateriale,
                )

                is OppvarmingRegistreringRequest.Data -> OppvarmingRegistrering.Data(
                    data = oppvarmingRegistrering.data.map {
                        OppvarmingDataRegistrering(
                            oppvarming = it.oppvarming,
                            kildemateriale = it.kildemateriale,
                            gyldighetsaar = it.gyldighetsaar,
                            opphoersaar = it.opphoersaar,
                        )
                    },
                )
            }
        }
    )
}

fun EgenregistreringRequest.toEgenregistrering(eier: String): Egenregistrering = Egenregistrering(
    id = EgenregistreringId(UUID.randomUUID()),
    eier = Foedselsnummer(eier),
    registreringstidspunkt = Instant.now(),
    prosess = ProsessKode.Egenregistrering,
    bruksenhetRegistrering = this.toBruksenhetRegistrering(),
)

fun EgenregistreringRequest2.toEgenregistrering2(eier: String) = Egenregistrering2(
    id = EgenregistreringId(UUID.randomUUID()),
    eier = Foedselsnummer(eier),
    registreringstidspunkt = Instant.now(),
    prosess = ProsessKode.Egenregistrering,
    bruksenhetId = BruksenhetBubbleId(bruksenhetId),
    feltRegistrering = when (val felt = this.feltRegistrering) {
        is FeltRegistreringRequest.VannforsyningFeltRegistreringRequest -> FeltRegistrering.VannforsyningFeltRegistrering(
            vannforsyning = felt.vannforsyning,
            kildemateriale = felt.kildemateriale,
            gyldighetsaar = felt.gyldighetsaar,
        )
        is FeltRegistreringRequest.EnergikildeFeltRegistreringRequest.Energikilder -> FeltRegistrering.EnergikildeFeltRegistrering.Data(
            data = felt.energikilder.map { EnergikildeDataRegistrering(
                energikilde = it.energikilde,
                kildemateriale = it.kildemateriale,
                gyldighetsaar = it.gyldighetsaar,
                opphoersaar = null
            ) }
        )
        is FeltRegistreringRequest.EnergikildeFeltRegistreringRequest.HarIkke -> FeltRegistrering.EnergikildeFeltRegistrering.HarIkke(
            kildemateriale = felt.kildemateriale,
        )
    }
)
