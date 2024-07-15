@file:UseSerializers(UUIDSerializer::class)
package no.kartverket.matrikkel.bygning.gjeldende

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class Bygning(
    val metadata: ValueMetadata,
    val matrikkelId: Long,
    val uuid: UUID,
    val bygningsnummer: Long,
    val kommunenummer: String,
    val etasjer: List<Etasje>,
    val vannforsyning: List<Vannforsyning>,
    val avloep: List<Avloep>,
)

@Serializable
data class Etasje(
    val metadata: ValueMetadata,
    val etasjeplan: String, // TODO: Kode
    val etasjenummer: Int,
)

@Serializable
data class Vannforsyning(
    val metadata: ValueMetadata,
    val vannforsyning: String, // TODO: Kode
)

@Serializable
data class Avloep(
    val metadata: ValueMetadata,
    val avloep: String, // TODO: Kode
)
