package no.kartverket.matrikkel.bygning.infrastructure.database.repositories.bygning

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.kartverket.matrikkel.bygning.application.bygning.BygningRepository
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import org.intellij.lang.annotations.Language
import org.postgresql.util.PGobject
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class BygningRepositoryImpl(private val dataSource: DataSource) : BygningRepository {

    private val objectMapper =
        jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun saveBruksenheter(bruksenheter: List<Bruksenhet>, registreringstidspunkt: Instant, tx: TransactionalSession) {
        @Language("PostgreSQL")
        val sql = """
            INSERT INTO bygning.bruksenhet
            (id, bruksenhet_bubble_id, registreringstidspunkt, data)
            VALUES (:id, :bruksenhetBubbleId, :registreringstidspunkt, :data)
        """.trimIndent()

        tx.batchPreparedNamedStatement(
            sql,
            bruksenheter.map {
                mapOf(
                    "id" to PGobject().apply {
                        this.type = "uuid"
                        this.value = it.id.value.toString()
                    },
                    "bruksenhetBubbleId" to it.bruksenhetBubbleId.value,
                    "registreringstidspunkt" to Timestamp.from(registreringstidspunkt),
                    "data" to PGobject().apply {
                        this.type = "jsonb"
                        this.value = objectMapper.writeValueAsString(it)
                    },
                )
            },
        )
    }

    override fun getBruksenhetById(bruksenhetId: UUID, registreringstidspunkt: Instant): Bruksenhet? {
        @Language("PostgreSQL")
        val sql = """
            SELECT data
            FROM bygning.bruksenhet
            WHERE id = :id
            AND registreringstidspunkt <= :registreringstidspunkt
            ORDER BY registreringstidspunkt DESC
            LIMIT 1
        """.trimIndent()

        return sessionOf(dataSource).use {
            it.run(
                queryOf(
                    sql,
                    mapOf(
                        "id" to PGobject().apply {
                            this.type = "uuid"
                            this.value = bruksenhetId.toString()
                        },
                        "registreringstidspunkt" to Timestamp.from(registreringstidspunkt),
                    ),
                ).map { row ->
                    objectMapper.readValue<Bruksenhet>(row.string("data"))
                }.asSingle,
            )
        }
    }
}

