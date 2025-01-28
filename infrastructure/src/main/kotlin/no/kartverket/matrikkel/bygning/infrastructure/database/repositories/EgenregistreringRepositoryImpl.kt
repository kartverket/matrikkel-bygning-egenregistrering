package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringRepository
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.infrastructure.database.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.infrastructure.database.withTransaction
import org.postgresql.util.PGobject
import java.sql.Timestamp
import javax.sql.DataSource

class EgenregistreringRepositoryImpl(private val dataSource: DataSource) : EgenregistreringRepository {
    override fun saveEgenregistrering(egenregistrering: Egenregistrering) {
        return dataSource.withTransaction { connection ->
            connection.prepareAndExecuteUpdate(
                """
                   INSERT INTO bygning.egenregistrering
                   (id, registreringstidspunkt, eier, registrering)
                   VALUES (?, ?, ?, ?)
                """.trimIndent(),
            ) {
                it.setObject(1, egenregistrering.id)
                it.setTimestamp(2, Timestamp.from(egenregistrering.registreringstidspunkt))
                it.setString(3, egenregistrering.eier.value)
                it.setObject(
                    4,
                    PGobject().apply {
                        this.type = "jsonb"
                        // Av en eller annen grunn må jeg eksplisitt nevne serializer typen her
                        this.value = Json.encodeToString(egenregistrering.bygningRegistrering)
                    },
                )
            }
        }
    }
}
