package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringRepository
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.infrastructure.database.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.infrastructure.database.setUUID
import no.kartverket.matrikkel.bygning.infrastructure.database.withTransaction
import org.postgresql.util.PGobject
import java.sql.Timestamp
import javax.sql.DataSource

class EgenregistreringRepositoryImpl(private val dataSource: DataSource) : EgenregistreringRepository {
    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun saveEgenregistrering(egenregistrering: Egenregistrering) {
        return dataSource.withTransaction { connection ->
            connection.prepareAndExecuteUpdate(
                """
                   INSERT INTO bygning.egenregistrering
                   (id, registreringstidspunkt, eier, registrering)
                   VALUES (?, ?, ?, ?)
                """.trimIndent(),
            ) {
                it.setUUID(1, egenregistrering.id)
                it.setTimestamp(2, Timestamp.from(egenregistrering.registreringstidspunkt))
                it.setString(3, egenregistrering.eier.value)
                it.setObject(
                    4,
                    PGobject().apply {
                        this.type = "jsonb"
                        this.value = objectMapper.writeValueAsString(egenregistrering.bygningRegistrering)
                    },
                )
            }
        }
    }
}
