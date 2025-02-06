package no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.infrastructure.database.executeQueryAndMapPreparedStatement
import no.kartverket.matrikkel.bygning.infrastructure.database.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.infrastructure.database.setUUID
import no.kartverket.matrikkel.bygning.infrastructure.database.withTransaction
import org.postgresql.util.PGobject
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.sql.DataSource


class BygningRepositoryImpl(private val dataSource: DataSource) : BygningRepository {
    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun saveBruksenhet(bruksenhet: Bruksenhet) {
        return dataSource.withTransaction { connection ->
            connection.prepareAndExecuteUpdate(
                """
                    INSERT INTO bygning.bruksenhet
                    (id, bruksenhet_bubble_id, bygning_id, registreringstidspunkt, data)
                    VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
            ) {
                it.setUUID(1, bruksenhet.id.value)
                it.setLong(2, bruksenhet.bruksenhetBubbleId.value)
                it.setUUID(3, bruksenhet.bygningId.value)
                it.setTimestamp(4, Timestamp.from(Instant.now()))
                it.setObject(
                    5,
                    PGobject().apply {
                        this.type = "jsonb"
                        this.value = objectMapper.writeValueAsString(bruksenhet)
                    },
                )
            }
        }
    }

    override fun getBruksenhetById(bruksenhetId: UUID): Bruksenhet? {
        return dataSource.executeQueryAndMapPreparedStatement(
            """
                SELECT data
                FROM bygning.bruksenhet
                WHERE id = ?
                ORDER BY registreringstidspunkt DESC
                LIMIT 1
            """.trimIndent(),
            {
                it.setUUID(1, bruksenhetId)
            },
        ) {
            objectMapper.readValue<Bruksenhet>(it.getString("data"))
        }.singleOrNull()
    }
}

