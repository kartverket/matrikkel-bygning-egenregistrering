package no.kartverket.matrikkel.bygning.repositories

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.db.executeQueryAndMapPreparedStatement
import no.kartverket.matrikkel.bygning.db.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.db.prepareBatchAndExecuteUpdate
import no.kartverket.matrikkel.bygning.db.withTransaction
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.Registrering
import no.kartverket.matrikkel.bygning.models.Result
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.util.UUID
import javax.sql.DataSource

class EgenregistreringRepository(private val dataSource: DataSource) {
    fun findNewestBruksenhetRegistrering(bruksenhetId: Long): BruksenhetRegistrering? {
        return findNewestRegistrering<BruksenhetRegistrering>(bruksenhetId)
    }

    fun findNewestBygningRegistrering(bygningId: Long): BygningRegistrering? {
        return findNewestRegistrering<BygningRegistrering>(bygningId)
    }

    private inline fun <reified T : Registrering> findNewestRegistrering(id: Long): T? {
        val idName = when (T::class) {
            BygningRegistrering::class -> "bygningId"
            BruksenhetRegistrering::class -> "bruksenhetId"
            else -> null
        } ?: throw RuntimeException("Det ble forsøkt å hente registrering med feil type, ${T::class}")

        return dataSource.executeQueryAndMapPreparedStatement(
            """
                select r.registrering, e.registrering_tidspunkt
                from bygning.registrering r
                    left join bygning.egenregistrering e on e.id = r.egenregistrering_id
                where r.registrering ->> ? = ?
                order by e.registrering_tidspunkt DESC
                limit 1;
            """.trimIndent(),
            {
                it.setString(1, idName)
                it.setString(2, id.toString())
            },
        ) {
            Json.decodeFromString<T>(it.getString("registrering"))
        }.firstOrNull()
    }

    fun saveEgenregistrering(egenregistrering: Egenregistrering): Result<Unit> {
        return dataSource.withTransaction { connection ->
            connection.prepareAndExecuteUpdate(
                "INSERT INTO bygning.egenregistrering values (?, ?)",
            ) {
                it.setObject(1, egenregistrering.id)
                it.setTimestamp(2, Timestamp.from(egenregistrering.registreringstidspunkt))
            }

            connection.prepareBatchAndExecuteUpdate(
                "INSERT INTO bygning.registrering values (?, ?, ?)",
                buildList {
                    egenregistrering.bygningRegistrering?.let { bygningRegistrering ->
                        add(createRegistreringBatchSetter(egenregistrering.id, bygningRegistrering))
                    }

                    egenregistrering.bruksenhetRegistreringer?.forEach { bruksenhetRegistrering ->
                        add(createRegistreringBatchSetter(egenregistrering.id, bruksenhetRegistrering))
                    }
                },
            )
        }
    }

    private inline fun <reified T : Registrering> createRegistreringBatchSetter(
        egenregistreringId: UUID, registrering: T
    ): (PreparedStatement) -> Unit = { preparedStatement ->
        preparedStatement.setObject(1, registrering.registreringId)
        preparedStatement.setObject(2, egenregistreringId)
        preparedStatement.setObject(
            3,
            PGobject().apply {
                this.type = "jsonb"
                this.value = Json.encodeToString(registrering)
            },
        )
    }
}
