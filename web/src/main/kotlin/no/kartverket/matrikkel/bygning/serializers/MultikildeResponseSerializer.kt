package no.kartverket.matrikkel.bygning.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.kartverket.matrikkel.bygning.routes.v1.intern.bygning.MultikildeInternResponse

class MultikildeInternResponseSerializer<T : Any>(dataSerializer: KSerializer<T>) : KSerializer<MultikildeInternResponse<T>> {
    private val generated = MultikildeInternResponse.generatedSerializer(dataSerializer)

    override val descriptor: SerialDescriptor = SerialDescriptor(generated.descriptor.serialName + "<" + dataSerializer.descriptor.serialName + ">", generated.descriptor)

    override fun deserialize(decoder: Decoder): MultikildeInternResponse<T> = generated.deserialize(decoder)

    override fun serialize(encoder: Encoder, value: MultikildeInternResponse<T>) = generated.serialize(encoder, value)
}
