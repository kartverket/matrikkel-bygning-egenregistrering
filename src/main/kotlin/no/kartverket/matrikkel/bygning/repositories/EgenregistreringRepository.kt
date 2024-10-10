package no.kartverket.matrikkel.bygning.repositories

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import kotlinx.serialization.json.Json
import no.kartverket.matrikkel.bygning.db.executeQueryAndMapPreparedStatement
import no.kartverket.matrikkel.bygning.db.prepareAndExecuteUpdate
import no.kartverket.matrikkel.bygning.db.withTransaction
import no.kartverket.matrikkel.bygning.models.BygningRegistrering
import no.kartverket.matrikkel.bygning.models.Egenregistrering
import no.kartverket.matrikkel.bygning.models.RegistreringAktoer.*
import no.kartverket.matrikkel.bygning.models.responses.ErrorDetail
import org.postgresql.util.PGobject
import java.sql.Timestamp
import java.util.UUID
import javax.sql.DataSource

// TODO Ikke egentlig blodfan av hvordan ting håndteres ut av repository. Kan man få noe mer informasjon ut i ErrorDetail? Det er jo antageligvis en exception som skjer
// hvis noe først går gæli her

class EgenregistreringRepository(private val dataSource: DataSource) {
    fun getAllEgenregistreringerForBygning(bygningId: Long): Result<List<Egenregistrering>, ErrorDetail> {
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
            )
        }.toResultOr {
            ErrorDetail(
                detail = "Noe gikk galt under henting av egenregistreringer",
            )
        }
    }

    fun saveEgenregistrering(egenregistrering: Egenregistrering): Result<Unit, ErrorDetail> {
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
        }.toResultOr {
            ErrorDetail(
                detail = "Noe gikk galt under lagring av egenregistrering",
            )
        }
    }
}
