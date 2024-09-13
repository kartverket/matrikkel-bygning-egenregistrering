package no.kartverket.matrikkel.bygning.repositories

import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.db.executeQueryAndMapPreparedStatement
import no.kartverket.matrikkel.bygning.db.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.db.withTransaction
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.Result
import org.postgresql.util.PGobject
import java.sql.Timestamp
import java.util.UUID
import javax.sql.DataSource

class EgenregistreringRepository(private val dataSource: DataSource) {
    fun getAllEgenregistreringerForBygning(bygningId: Long): List<Egenregistrering> {
        return dataSource.executeQueryAndMapPreparedStatement(
            """
                select er.id, er.registreringstidspunkt, er.registrering as bygningregistrering
                from bygning.egenregistrering er
                where er.registrering ->> 'bygningId' = ?
                order by er.registreringstidspunkt DESC;
            """.trimIndent(),
            {
                it.setString(1, bygningId.toString())
            },
        ) {
            Egenregistrering(
                id = UUID.fromString(it.getString("id")),
                registreringstidspunkt = it.getTimestamp("registreringstidspunkt").toInstant(),
                bygningRegistrering = Json.decodeFromString<BygningRegistrering>(it.getString("bygningregistrering")),
            )
        }
    }

    fun saveEgenregistrering(egenregistrering: Egenregistrering): Result<Unit> {
        return dataSource.withTransaction { connection ->
            connection.prepareAndExecuteUpdate(
                "INSERT INTO bygning.egenregistrering values (?, ?, ?)",
            ) {
                it.setObject(1, egenregistrering.id)
                it.setTimestamp(2, Timestamp.from(egenregistrering.registreringstidspunkt))
                it.setObject(3, PGobject().apply {
                    this.type = "jsonb"
                    // Av en eller annen grunn må jeg eksplisitt nevne serializer typen her
                    this.value =
                        Json.encodeToString(BygningRegistrering.serializer(), egenregistrering.bygningRegistrering)
                })
            }
        }
    }
}
