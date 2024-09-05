package no.kartverket.matrikkel.bygning.repositories

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.db.connection
import no.kartverket.matrikkel.bygning.db.executeQueryAndMapPreparedStatement
import no.kartverket.matrikkel.bygning.db.prepareStatement
import no.kartverket.matrikkel.bygning.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.Registrering
import org.intellij.lang.annotations.Language
import org.postgresql.util.PGobject
import java.sql.Connection
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

class EgenregistreringRepository(private val dataSource: DataSource) {
    private inline fun <reified T> findNewestRegistrering(id: Long): T? {
        val idName = when (T::class) {
            BygningRegistrering::class -> "bygningId"
            BruksenhetRegistrering::class -> "bruksenhetId"
            else -> null
        } ?: return null

        @Language("PostgreSQL") val getRegistrering = """
                select r.registrering, e.registrering_tidspunkt
                from bygning.registrering r
                    left join bygning.egenregistrering e on e.id = r.egenregistrering_id
                where r.registrering ->> ? = ?
                order by e.registrering_tidspunkt DESC
                limit 1;
            """.trimIndent()

        val newestRegistrering = dataSource.executeQueryAndMapPreparedStatement(
            getRegistrering,
            { it.setString(1, idName) },
            { it.setString(2, id.toString()) },
        ) {
            Json.decodeFromString<T>(it.getString("registrering"))
        }.firstOrNull()

        return newestRegistrering
    }

    fun findNewestBruksenhetRegistrering(bruksenhetId: Long): BruksenhetRegistrering? {
        return findNewestRegistrering<BruksenhetRegistrering>(bruksenhetId)
    }

    fun findNewestBygningRegistrering(bygningId: Long): BygningRegistrering? {
        return findNewestRegistrering<BygningRegistrering>(bygningId)
    }

    private inline fun <reified T : Registrering> saveRegistrering(
        connection: Connection, registrering: T, registreringId: UUID, egenregistreringId: UUID
    ): Boolean {
        @Language("PostgreSQL") val createRegistreringSQL = "INSERT INTO bygning.registrering values (?, ?, ?)"

        return connection.prepareStatement(createRegistreringSQL) { preparedStatement ->
            preparedStatement.setObject(1, registreringId)
            preparedStatement.setObject(2, egenregistreringId)
            preparedStatement.setObject(
                3,
                PGobject().apply {
                    this.type = "jsonb"
                    this.value = Json.encodeToString(registrering)
                },
            )

            return@prepareStatement preparedStatement.executeUpdate() > 0
        }
    }

    fun saveEgenregistrering(egenregistrering: Egenregistrering): Boolean {
        // Try catch noe sted ??
        dataSource.connection { connection ->
            connection.autoCommit = false

            @Language("PostgreSQL") val createEgenregistreringSQL = "INSERT INTO bygning.egenregistrering values (?, ?, ?)"

            val didSaveEgenregistrering = connection.prepareStatement(createEgenregistreringSQL) { preparedStatement ->
                preparedStatement.setObject(1, egenregistrering.id)
                preparedStatement.setString(2, egenregistrering.registrerer)
                // TODO Hvordan sette timestamp riktig mellom java typer?
                preparedStatement.setTimestamp(3, Timestamp.from(egenregistrering.registreringTidspunkt))

                return@prepareStatement preparedStatement.executeUpdate() > 0
            }


            val didSaveBygningRegistrering = egenregistrering.bygningRegistrering?.let { bygningRegistrering ->
                saveRegistrering(
                    connection,
                    bygningRegistrering,
                    bygningRegistrering.registreringId,
                    egenregistrering.id,
                )
            }

            val didSaveBruksenhetRegistreringer = egenregistrering.bruksenhetRegistreringer?.map { bruksenhetRegistrering ->
                saveRegistrering(
                    connection,
                    bruksenhetRegistrering,
                    bruksenhetRegistrering.registreringId,
                    egenregistrering.id,
                )
            }

            // Lol
            if (
                didSaveEgenregistrering &&
                (didSaveBygningRegistrering != null && didSaveBygningRegistrering) &&
                (didSaveBruksenhetRegistreringer != null && didSaveBruksenhetRegistreringer.all { it })
            ) {
                connection.commit()
                return true
            }

            return false
        }
    }
}
