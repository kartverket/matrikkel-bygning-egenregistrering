package no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning

import kotlinx.serialization.Serializable
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.Felt
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.AvlopKodeMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.BruksarealMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.ByggeaarMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.EnergikildeMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.OppvarmingMedPersondataResponse
import no.kartverket.matrikkel.bygning.routes.v1.ekstern.medpersondata.bygning.FeltMedPersondataResponse.VannforsyningKodeMedPersondataResponse
import no.kartverket.matrikkel.bygning.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class BygningMedPersondataResponse(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<BruksenhetMedPersondataResponse>,
)

@Serializable
data class BruksenhetMedPersondataResponse(
    val bruksenhetId: Long,
    val byggeaar: ByggeaarMedPersondataResponse?,
    val totaltBruksareal: BruksarealMedPersondataResponse?,
    val vannforsyning: VannforsyningKodeMedPersondataResponse?,
    val avlop: AvlopKodeMedPersondataResponse?,
    val energikilder: List<EnergikildeMedPersondataResponse>?,
    val oppvarming: List<OppvarmingMedPersondataResponse>?,
)

sealed interface FeltMedPersondataResponse<T> {
    val data: T
    val metadata: RegisterMetadataMedPersondataResponse

    @Serializable
    data class ByggeaarMedPersondataResponse(
        override val data: Int,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<Int>

    @Serializable
    data class BruksarealMedPersondataResponse(
        override val data: Double,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<Double>

    @Serializable
    data class VannforsyningKodeMedPersondataResponse(
        override val data: VannforsyningKode,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<VannforsyningKode>

    @Serializable
    data class AvlopKodeMedPersondataResponse(
        override val data: AvlopKode,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<AvlopKode>

    @Serializable
    data class EnergikildeMedPersondataResponse(
        override val data: EnergikildeKode,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<EnergikildeKode>

    @Serializable
    data class OppvarmingMedPersondataResponse(
        override val data: OppvarmingKode,
        override val metadata: RegisterMetadataMedPersondataResponse
    ) : FeltMedPersondataResponse<OppvarmingKode>
}


@Serializable
data class RegisterMetadataMedPersondataResponse(
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val registrertAv: String,
    val kildemateriale: KildematerialeKode? = null,
    val prosess: ProsessKode?,
    val gyldighetsaar: Int?,
    val opphoersaar: Int?,
)

fun Bygning.toBygningMedPersondataResponse(): BygningMedPersondataResponse = BygningMedPersondataResponse(
    bygningId = bygningBubbleId.value,
    bygningsnummer = bygningsnummer,
    bruksenheter = bruksenheter.map {
        it.toBruksenhetMedPersondataResponse()
    },
)

private fun RegisterMetadata.toRegisterMetadataMedPersondataResponse(): RegisterMetadataMedPersondataResponse = RegisterMetadataMedPersondataResponse(
    registreringstidspunkt = registreringstidspunkt,
    kildemateriale = kildemateriale,
    prosess = prosess,
    registrertAv = registrertAv.value,
    gyldighetsaar = gyldighetsperiode.gyldighetsaar?.value,
    opphoersaar = gyldighetsperiode.opphoersaar?.value
)

internal fun <U, T : Felt<U>, O : FeltMedPersondataResponse<U>?> toFeltMedPersondataResponse(
    felt: T?,
    constructor: (U, RegisterMetadataMedPersondataResponse) -> O,
): O? {
    if (felt == null) {
        return null
    }

    return constructor(
        felt.data,
        felt.metadata.toRegisterMetadataMedPersondataResponse()
    )
}

internal fun <U, T : Felt<U>, O : FeltMedPersondataResponse<U>?> toListeFeltMedPersondataResponse(
    list: List<T>?,
    constructor: (U, RegisterMetadataMedPersondataResponse) -> O,
): List<O>? {
    if (list == null) {
        return null
    }

    return list.map { felt ->
        constructor(
            felt.data,
            felt.metadata.toRegisterMetadataMedPersondataResponse()
        )
    }
}

fun Bruksenhet.toBruksenhetMedPersondataResponse(): BruksenhetMedPersondataResponse = BruksenhetMedPersondataResponse(
    bruksenhetId = this.bruksenhetBubbleId.value,
    byggeaar = toFeltMedPersondataResponse(this.byggeaar.egenregistrert, ::ByggeaarMedPersondataResponse),
    totaltBruksareal = toFeltMedPersondataResponse(
        this.totaltBruksareal.egenregistrert,
        ::BruksarealMedPersondataResponse
    ),
    vannforsyning = toFeltMedPersondataResponse(
        this.vannforsyning.egenregistrert,
        ::VannforsyningKodeMedPersondataResponse
    ),
    avlop = toFeltMedPersondataResponse(this.avlop.egenregistrert, ::AvlopKodeMedPersondataResponse),
    energikilder = toListeFeltMedPersondataResponse(this.energikilder.egenregistrert?.toEnergikilder(), ::EnergikildeMedPersondataResponse),
    oppvarming = toListeFeltMedPersondataResponse(this.oppvarming.egenregistrert?.toOppvarming(), ::OppvarmingMedPersondataResponse),
)
