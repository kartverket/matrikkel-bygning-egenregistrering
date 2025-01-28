package no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.serializers.InstantSerializer
import no.kartverket.matrikkel.bygning.application.serializers.UUIDSerializer
import no.kartverket.matrikkel.bygning.infrastructure.database.executeQueryAndMapPreparedStatement
import no.kartverket.matrikkel.bygning.infrastructure.database.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.infrastructure.database.withTransaction
import org.postgresql.util.PGobject
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.sql.DataSource


@Serializable
data class BruksenhetDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val bruksenhetId: Long,
    @Serializable(with = UUIDSerializer::class)
    val bygningId: UUID,
    @Serializable(with = InstantSerializer::class)
    val registreringstidspunkt: Instant,
    val bruksenhetData: Bruksenhet,
)

class BygningRepositoryImpl(private val dataSource: DataSource) : BygningRepository {
    override fun saveBruksenhet(bruksenhet: Bruksenhet) {
        val bruksenhetToSave = bruksenhet.toDTO()

        return dataSource.withTransaction { connection ->
            connection.prepareAndExecuteUpdate(
                """
                    INSERT INTO bygning.bruksenhet
                    (id, bruksenhetId, bygningId, registreringstidspunkt, data)
                    VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
            ) {
                it.setObject(
                    1,
                    PGobject().apply {
                        this.type = "uuid"
                        this.value = bruksenhetToSave.id.toString()
                    },
                )
                it.setLong(2, bruksenhetToSave.bruksenhetId)
                it.setObject(
                    3,
                    PGobject().apply {
                        this.type = "uuid"
                        this.value = bruksenhetToSave.bygningId.toString()
                    },
                )
                it.setTimestamp(4, Timestamp.from(bruksenhetToSave.registreringstidspunkt))
                it.setObject(
                    5,
                    PGobject().apply {
                        this.type = "jsonb"
                        this.value = Json.encodeToString(BruksenhetDTO.serializer(), bruksenhetToSave)
                    },
                )
            }
        }
    }

    override fun getBygningById(bygningId: UUID): Bygning? {
        TODO("Not yet implemented")
    }

    override fun getBruksenhetById(id: UUID): Bruksenhet? {
        return dataSource.executeQueryAndMapPreparedStatement(
            """
                SELECT data
                FROM bygning.bruksenhet
                WHERE id = ?
                ORDER BY registreringstidspunkt DESC
                LIMIT 1
            """.trimIndent(),
            {
                it.setObject(
                    1,
                    PGobject().apply {
                        this.type = "uuid"
                        this.value = id.toString()
                    },
                )
            },
        ) {
            val bruksenhetDTO = Json.decodeFromString<BruksenhetDTO>(it.getString("data"))

            Bruksenhet(
                id = bruksenhetDTO.id,
                bruksenhetBubbleId = bruksenhetDTO.bruksenhetId,
                bygningId = bruksenhetDTO.bygningId,
                etasjer = bruksenhetDTO.bruksenhetData.etasjer,
                byggeaar = bruksenhetDTO.bruksenhetData.byggeaar,
                totaltBruksareal = bruksenhetDTO.bruksenhetData.totaltBruksareal,
                energikilder = bruksenhetDTO.bruksenhetData.energikilder,
                oppvarminger = bruksenhetDTO.bruksenhetData.oppvarminger,
                vannforsyning = bruksenhetDTO.bruksenhetData.vannforsyning,
                avlop = bruksenhetDTO.bruksenhetData.avlop,
            )
        }.singleOrNull()
    }
}
