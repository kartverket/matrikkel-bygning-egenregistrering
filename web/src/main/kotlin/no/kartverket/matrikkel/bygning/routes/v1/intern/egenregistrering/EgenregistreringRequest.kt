package no.kartverket.matrikkel.bygning.routes.v1.intern.egenregistrering

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Etasjebetegnelse
import no.kartverket.matrikkel.bygning.application.models.Etasjenummer
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EtasjeplanKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.serializers.LocalDateSerializer
import java.time.Instant
import java.time.LocalDate
import java.util.*


@Serializable
data class EgenregistreringRequest(
    val bruksenhetId: Long,
    val bruksarealRegistrering: BruksarealRegistreringRequest?,
    val byggeaarRegistrering: ByggeaarRegistreringRequest?,
    val energikildeRegistrering: EnergikilderRegistreringRequest?,
    val oppvarmingRegistrering: OppvarmingRegistreringContainer?,
    val vannforsyningRegistrering: VannforsyningRegistreringRequest?,
    val avlopRegistrering: AvlopRegistreringRequest?,
)

interface HasGyldighetsperiode {
    @Serializable(with = LocalDateSerializer::class)
    val gyldighetsdato: LocalDate?

    @Serializable(with = LocalDateSerializer::class)
    val opphoersdato: LocalDate?
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
)

@Serializable
data class AvlopRegistreringRequest(
    val avlop: AvlopKode,
    val kildemateriale: KildematerialeKode,
)

@Serializable
data class EnergikilderRegistreringRequest(
    val energikilde: List<EnergikildeKode>,
    val kildemateriale: KildematerialeKode,
)

// TODO Finne på noe bedre enn mangel?
// TODO Egen klasse for mangel så man ikke bruker string?
// TODO Skal det være kilde og gyldighet for mangel på noe?
@Serializable
sealed class OppvarmingRegistreringRequest() {
    @Serializable
    data class MangelRegistreringRequest(
        val data: String,
    ) : OppvarmingRegistreringRequest()

    @Serializable
    data class OppvarmingskildeRegistreringRequest(
        val data: OppvarmingKode,
        val kildemateriale: KildematerialeKode,
    ) : OppvarmingRegistreringRequest()
}

@Serializable(with = OppvarmingRegistreringContainerSerializer::class)
sealed class OppvarmingRegistreringContainer {
    @Serializable
    @SerialName("MangelRegistrering")
    data class MangelRegistrering(
        val data: String
    ) : OppvarmingRegistreringContainer()

    @Serializable
    @SerialName("OppvarmingskilderRegistrering")
    data class OppvarmingskilderRegistrering(
        val items: List<OppvarmingRegistreringRequest.OppvarmingskildeRegistreringRequest>, // Replace with proper data type for OppvarmingKode if needed
    ) : OppvarmingRegistreringContainer()
}


object OppvarmingRegistreringContainerSerializer : KSerializer<OppvarmingRegistreringContainer> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("OppvarmingRegistreringContainer")

    override fun deserialize(decoder: Decoder): OppvarmingRegistreringContainer {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("Expected JSON input")

        val jsonElement = input.decodeJsonElement()

        return when {
            // Check if it's a single object (MangelRegistrering)
            jsonElement is JsonObject && jsonElement.containsKey("data") && !jsonElement.containsKey("items") -> {
                val data = jsonElement["data"]!!.jsonPrimitive.content
                OppvarmingRegistreringContainer.MangelRegistrering(data)
            }
            // Check if it's an array of objects (OppvarmingskilderRegistrering)
            jsonElement is JsonArray -> {
                val items = jsonElement.map { jsonItem ->
                    val data = jsonItem.jsonObject["data"]!!.jsonPrimitive.content
                    val kildemateriale = jsonItem.jsonObject["kildemateriale"]!!.jsonPrimitive.content
                    OppvarmingRegistreringRequest.OppvarmingskildeRegistreringRequest(
                        data = OppvarmingKode.valueOf(data),
                        kildemateriale = KildematerialeKode.valueOf(kildemateriale),
                    )
                }
                OppvarmingRegistreringContainer.OppvarmingskilderRegistrering(items)
            }

            else -> throw SerializationException("Unknown format")
        }
    }

    override fun serialize(encoder: Encoder, value: OppvarmingRegistreringContainer) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("Expected JSON output")

        when (value) {
            is OppvarmingRegistreringContainer.MangelRegistrering -> {
                jsonEncoder.encodeJsonElement(JsonObject(mapOf("data" to JsonPrimitive(value.data))))
            }

            is OppvarmingRegistreringContainer.OppvarmingskilderRegistrering -> {
                val listJson = value.items.map {
                    JsonObject(
                        mapOf(
                            "data" to JsonPrimitive(it.data.toString()),
                            "kildemateriale" to JsonPrimitive(it.kildemateriale.toString()),
                        ),
                    )
                }
                jsonEncoder.encodeJsonElement(JsonArray(listJson))
            }
        }
    }
}


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
            )
        },
        avlopRegistrering = avlopRegistrering?.let {
            AvlopRegistrering(
                avlop = it.avlop,
                kildemateriale = it.kildemateriale,
            )
        },
        energikildeRegistrering = energikildeRegistrering?.let {
            EnergikildeRegistrering(
                energikilder = it.energikilde,
                kildemateriale = it.kildemateriale,
            )
        },
        oppvarmingRegistrering = null,
//            oppvarmingRegistrering?.let {
//            OppvarmingRegistrering(
//                oppvarminger = it.oppvarminger,
//                kildemateriale = it.kildemateriale,
//            )
//        },
    )
}

fun EgenregistreringRequest.toEgenregistrering(eier: String): Egenregistrering =
    Egenregistrering(
        id = UUID.randomUUID(),
        eier = Foedselsnummer(eier),
        registreringstidspunkt = Instant.now(),
        prosess = ProsessKode.Egenregistrering,
        bruksenhetRegistrering = this.toBruksenhetRegistrering(),
    )
