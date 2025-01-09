package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringRepository
import no.kartverket.matrikkel.bygning.application.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.*
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.infrastructure.database.executeQueryAndMapPreparedStatement
import no.kartverket.matrikkel.bygning.infrastructure.database.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.infrastructure.database.withTransaction
import org.postgresql.util.PGobject
import java.sql.Timestamp
import java.util.UUID
import javax.sql.DataSource

class EgenregistreringRepositoryImpl(private val dataSource: DataSource) : EgenregistreringRepository {
    override fun getAllEgenregistreringerForBygning(bygningId: Long): List<Egenregistrering> {
        return dataSource.executeQueryAndMapPreparedStatement(
            """
                SELECT er.id AS id, 
                er.eier AS eier, 
                er.registreringstidspunkt AS registreringstidspunkt, 
                er.registrering AS bygningregistrering
                FROM bygning.egenregistrering er
                WHERE er.registrering ->> 'bygningId' = ?
                ORDER BY er.registreringstidspunkt DESC;
            """.trimIndent(),
            {
                it.setString(1, bygningId.toString())
            },
        ) {
            Egenregistrering(
                id = UUID.fromString(it.getString("id")),
                eier = Foedselsnummer(it.getString("eier")),
                registreringstidspunkt = it.getTimestamp("registreringstidspunkt").toInstant(),
                bygningRegistrering = Json.decodeFromString<BygningRegistrering>(it.getString("bygningregistrering")),
                prosess = ProsessKode.Egenregistrering
            )
        }
    }

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
                        this.value = Json.encodeToString(BygningRegistrering.serializer(), egenregistrering.bygningRegistrering)
                    },
                )
            }
        }
    }
}
