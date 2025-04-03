package no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering

import io.github.smiley4.schemakenerator.core.annotations.Name
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.AvsluttEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.AvsluttFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.AvlopFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.BruksarealFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.ByggeaarFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.EnergikildeFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.OppvarmingFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.FeltRegistrering.VannforsyningFeltRegistrering
import no.kartverket.matrikkel.bygning.application.models.KorrigerEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingDataRegistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegistrerEgenregistrering
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
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.AvlopFeltRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.BruksarealFeltRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.ByggeaarFeltRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.EnergikilderFeltRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.HarIkkeEnergikilderFeltRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.HarIkkeOppvarmingFeltRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.OppvarmingFeltRegistreringRequest
import no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering.FeltRegistreringRequest.VannforsyningFeltRegistreringRequest
import java.time.Instant
import java.time.Year
import java.util.UUID

@Serializable
data class EgenregistreringRequest(
    val bruksenhetId: Long,
    val bruksarealRegistrering: BruksarealRegistreringRequest?,
    val byggeaarRegistrering: ByggeaarRegistreringRequest?,
    val vannforsyningRegistrering: VannforsyningRegistreringRequest?,
    val avlopRegistrering: AvlopRegistreringRequest?,
    val energikildeRegistrering: List<EnergikildeRegistreringRequest>?,
    val oppvarmingRegistrering: List<OppvarmingRegistreringRequest>?,
)

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
    val opphoersaar: Int? = null,
)

@Serializable
data class AvlopRegistreringRequest(
    val avlop: AvlopKode,
    val kildemateriale: KildematerialeKode,
    val gyldighetsaar: Int? = null,
    val opphoersaar: Int? = null,
)

@Serializable
data class EnergikildeRegistreringRequest(
    val energikilde: EnergikildeKode,
    val kildemateriale: KildematerialeKode,
    val gyldighetsaar: Int? = null,
    val opphoersaar: Int? = null,
)

@Serializable
data class OppvarmingRegistreringRequest(
    val oppvarming: OppvarmingKode,
    val kildemateriale: KildematerialeKode,
    val gyldighetsaar: Int? = null,
    val opphoersaar: Int? = null,
)

fun EtasjeBruksarealRegistreringRequest.toEtasjeBruksarealRegistrering(): EtasjeBruksarealRegistrering =
    EtasjeBruksarealRegistrering(
        bruksareal = this.bruksareal,
        etasjebetegnelse =
            Etasjebetegnelse.of(
                etasjenummer = Etasjenummer.of(this.etasjebetegnelse.etasjenummer),
                etasjeplanKode = EtasjeplanKode.of(this.etasjebetegnelse.etasjeplanKode),
            ),
    )

fun EgenregistreringRequest.toBruksenhetRegistrering(): BruksenhetRegistrering =
    BruksenhetRegistrering(
        bruksenhetBubbleId = BruksenhetBubbleId(bruksenhetId),
        bruksarealRegistrering =
            bruksarealRegistrering?.let { bruksarealRegistrering ->
                BruksarealRegistrering(
                    totaltBruksareal = bruksarealRegistrering.totaltBruksareal,
                    etasjeRegistreringer =
                        bruksarealRegistrering.etasjeRegistreringer?.map {
                            it.toEtasjeBruksarealRegistrering()
                        },
                    kildemateriale = bruksarealRegistrering.kildemateriale,
                )
            },
        byggeaarRegistrering =
            byggeaarRegistrering?.let {
                ByggeaarRegistrering(
                    byggeaar = it.byggeaar,
                    kildemateriale = it.kildemateriale,
                )
            },
        vannforsyningRegistrering =
            vannforsyningRegistrering?.let {
                VannforsyningRegistrering(
                    vannforsyning = it.vannforsyning,
                    kildemateriale = it.kildemateriale,
                    gyldighetsaar = it.gyldighetsaar,
                    opphoersaar = it.opphoersaar,
                )
            },
        avlopRegistrering =
            avlopRegistrering?.let {
                AvlopRegistrering(
                    avlop = it.avlop,
                    kildemateriale = it.kildemateriale,
                    gyldighetsaar = it.gyldighetsaar,
                    opphoersaar = it.opphoersaar,
                )
            },
        energikildeRegistrering =
            energikildeRegistrering?.map {
                EnergikildeRegistrering(
                    energikilde = it.energikilde,
                    kildemateriale = it.kildemateriale,
                    gyldighetsaar = it.gyldighetsaar,
                    opphoersaar = it.opphoersaar,
                )
            },
        oppvarmingRegistrering =
            oppvarmingRegistrering?.map {
                OppvarmingRegistrering(
                    oppvarming = it.oppvarming,
                    kildemateriale = it.kildemateriale,
                    gyldighetsaar = it.gyldighetsaar,
                    opphoersaar = it.opphoersaar,
                )
            },
    )

fun EgenregistreringRequest.toEgenregistrering(eier: String): Egenregistrering =
    Egenregistrering(
        id = EgenregistreringId(UUID.randomUUID()),
        eier = Foedselsnummer(eier),
        registreringstidspunkt = Instant.now(),
        prosess = ProsessKode.Egenregistrering,
        bruksenhetRegistrering = this.toBruksenhetRegistrering(),
    )

@Serializable
data class RegistrertFeltEgenregistreringRequest(
    val bruksenhetId: Long,
    val registrertFelt: FeltRegistreringRequest,
)

@Serializable
data class KorrigerFeltEgenregistreringRequest(
    val bruksenhetId: Long,
    val korrigerFelt: FeltRegistreringRequest,
)

@Serializable
sealed class FeltRegistreringRequest {
    @Serializable
    @SerialName("byggeaar")
    @Name("byggeaar")
    data class ByggeaarFeltRegistreringRequest(
        val byggeaar: Int,
        val kildemateriale: KildematerialeKode,
    ) : FeltRegistreringRequest()

    @Serializable
    @SerialName("bruksareal")
    @Name("bruksareal")
    data class BruksarealFeltRegistreringRequest(
        val totaltBruksareal: Double,
        val etasjeRegistreringer: List<EtasjeBruksarealRegistreringRequest>?,
        val kildemateriale: KildematerialeKode,
    ) : FeltRegistreringRequest()

    @Serializable
    @SerialName("vannforsyning")
    @Name("vannforsyning")
    data class VannforsyningFeltRegistreringRequest(
        val vannforsyning: VannforsyningKode,
        val kildemateriale: KildematerialeKode,
        val gyldighetsaar: Int,
    ) : FeltRegistreringRequest()

    @Serializable
    @SerialName("avlop")
    @Name("avlop")
    data class AvlopFeltRegistreringRequest(
        val avlop: AvlopKode,
        val kildemateriale: KildematerialeKode,
        val gyldighetsaar: Int,
    ) : FeltRegistreringRequest()

    @Serializable
    @SerialName("harIkkeEnergikilder")
    @Name("harIkkeEnergikilder")
    data class HarIkkeEnergikilderFeltRegistreringRequest(
        val kildemateriale: KildematerialeKode,
    ) : FeltRegistreringRequest()

    @Serializable
    @SerialName("energikilder")
    @Name("energikilder")
    data class EnergikilderFeltRegistreringRequest(
        val energikilder: List<EnergikildeFeltDataRequest>,
    ) : FeltRegistreringRequest()

    @Serializable
    data class EnergikildeFeltDataRequest(
        val energikilde: EnergikildeKode,
        val kildemateriale: KildematerialeKode,
        val gyldighetsaar: Int,
    )

    @Serializable
    @SerialName("harIkkeOppvarming")
    @Name("harIkkeOppvarming")
    data class HarIkkeOppvarmingFeltRegistreringRequest(
        val kildemateriale: KildematerialeKode,
    ) : FeltRegistreringRequest()

    @Serializable
    @SerialName("oppvarming")
    @Name("oppvarming")
    data class OppvarmingFeltRegistreringRequest(
        val oppvarming: List<OppvarmingFeltDataRequest>,
    ) : FeltRegistreringRequest() {
        @Serializable
        data class OppvarmingFeltDataRequest(
            val oppvarming: OppvarmingKode,
            val kildemateriale: KildematerialeKode,
            val gyldighetsaar: Int,
        )
    }
}

fun RegistrertFeltEgenregistreringRequest.toRegistrerEgenregistrering(eier: String) =
    RegistrerEgenregistrering(
        id = EgenregistreringId(UUID.randomUUID()),
        eier = Foedselsnummer(eier),
        bruksenhetId = BruksenhetBubbleId(bruksenhetId),
        feltRegistrering = mapFeltRegistreringer(registrertFelt),
    )

fun KorrigerFeltEgenregistreringRequest.toKorrigerEgenregistrering(eier: String) =
    KorrigerEgenregistrering(
        id = EgenregistreringId(UUID.randomUUID()),
        eier = Foedselsnummer(eier),
        bruksenhetId = BruksenhetBubbleId(bruksenhetId),
        feltRegistrering = mapFeltRegistreringer(korrigerFelt),
    )

private fun mapFeltRegistreringer(felt: FeltRegistreringRequest) =
    when (felt) {
        is ByggeaarFeltRegistreringRequest ->
            ByggeaarFeltRegistrering(
                byggeaar = felt.byggeaar,
                kildemateriale = felt.kildemateriale,
            )

        is BruksarealFeltRegistreringRequest ->
            when (felt.etasjeRegistreringer) {
                null ->
                    BruksarealFeltRegistrering.TotaltBruksarealFeltRegistrering(
                        totaltBruksareal = felt.totaltBruksareal,
                        kildemateriale = felt.kildemateriale,
                    )

                else ->
                    BruksarealFeltRegistrering.TotaltOgEtasjeBruksarealFeltRegistrering.of(
                        totaltBruksareal = felt.totaltBruksareal,
                        etasjeRegistreringer =
                            felt.etasjeRegistreringer.map {
                                it.toEtasjeBruksarealRegistrering()
                            },
                        kildemateriale = felt.kildemateriale,
                    )
            }

        is VannforsyningFeltRegistreringRequest ->
            VannforsyningFeltRegistrering(
                vannforsyning = felt.vannforsyning,
                kildemateriale = felt.kildemateriale,
                gyldighetsaar = Year.of(felt.gyldighetsaar),
            )

        is AvlopFeltRegistreringRequest ->
            AvlopFeltRegistrering(
                avlop = felt.avlop,
                kildemateriale = felt.kildemateriale,
                gyldighetsaar = Year.of(felt.gyldighetsaar),
            )

        is EnergikilderFeltRegistreringRequest ->
            EnergikildeFeltRegistrering.Energikilder.of(
                energikilder =
                    felt.energikilder.map {
                        EnergikildeDataRegistrering(
                            energikilde = it.energikilde,
                            kildemateriale = it.kildemateriale,
                            gyldighetsaar = Year.of(it.gyldighetsaar),
                        )
                    },
            )

        is HarIkkeEnergikilderFeltRegistreringRequest ->
            EnergikildeFeltRegistrering.HarIkke(
                kildemateriale = felt.kildemateriale,
            )

        is OppvarmingFeltRegistreringRequest ->
            OppvarmingFeltRegistrering.Oppvarming.of(
                oppvarming =
                    felt.oppvarming.map {
                        OppvarmingDataRegistrering(
                            oppvarming = it.oppvarming,
                            kildemateriale = it.kildemateriale,
                            gyldighetsaar = Year.of(it.gyldighetsaar),
                        )
                    },
            )

        is HarIkkeOppvarmingFeltRegistreringRequest ->
            OppvarmingFeltRegistrering.HarIkke(
                kildemateriale = felt.kildemateriale,
            )
    }

@Serializable
sealed class SettOpphoersaarRequest {
    abstract val bruksenhetId: Long

    @Serializable
    @SerialName("vannforsyning")
    @Name("vannforsyning")
    data class SettOpphoersaarVannforsyningRequest(
        override val bruksenhetId: Long,
        val vannforsyning: VannforsyningKode,
        val opphoersaar: Int,
    ) : SettOpphoersaarRequest()

    @Serializable
    @SerialName("avlop")
    @Name("avlop")
    data class SettOpphoersaarAvlopRequest(
        override val bruksenhetId: Long,
        val avlop: AvlopKode,
        val opphoersaar: Int,
    ) : SettOpphoersaarRequest()
}

fun SettOpphoersaarRequest.toEgenregistrering(eier: String) =
    AvsluttEgenregistrering(
        id = EgenregistreringId(UUID.randomUUID()),
        eier = Foedselsnummer(eier),
        bruksenhetId = BruksenhetBubbleId(bruksenhetId),
        feltRegistrering =
            when (this) {
                is SettOpphoersaarRequest.SettOpphoersaarVannforsyningRequest ->
                    AvsluttFeltRegistrering.AvsluttVannforsyning(
                        vannforsyning = vannforsyning,
                        opphoersaar = Year.of(opphoersaar),
                    )

                is SettOpphoersaarRequest.SettOpphoersaarAvlopRequest ->
                    AvsluttFeltRegistrering.AvsluttAvlop(
                        avlop = avlop,
                        opphoersaar = Year.of(opphoersaar),
                    )
            },
    )
