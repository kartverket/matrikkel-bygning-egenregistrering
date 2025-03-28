package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.kartverket.matrikkel.bygning.application.hendelser.BygningHendelseType
import no.kartverket.matrikkel.bygning.application.hendelser.Hendelse
import no.kartverket.matrikkel.bygning.application.hendelser.HendelsePayload
import no.kartverket.matrikkel.bygning.application.hendelser.HendelsePayload.BruksenhetOppdatertPayload
import no.kartverket.matrikkel.bygning.application.hendelser.HendelseRepository
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class HendelseRepositoryImpl(
    private val dataSource: DataSource,
) : HendelseRepository {
    override fun saveHendelse(
        payload: HendelsePayload,
        tx: TransactionalSession,
    ) {
        @Language("PostgreSQL")
        val sql =
            """
            INSERT INTO bygning.hendelse (object_id, registreringstidspunkt, type)
            VALUES(:id, :registreringstidspunkt, :type)
            """.trimIndent()

        tx.run(
            queryOf(
                sql,
                mapOf(
                    "id" to payload.objectId,
                    "registreringstidspunkt" to payload.registreringstidspunkt,
                    "type" to payload.type.name,
                ),
            ).asUpdate,
        )
    }

    override fun getHendelser(
        fra: Long,
        antall: Long,
    ): List<Hendelse> =
        sessionOf(dataSource).use {
            @Language("PostgreSQL")
            val sql =
                """
                SELECT 
                    sekvensnummer,
                    object_id,
                    registreringstidspunkt,
                    type
                FROM bygning.hendelse
                WHERE sekvensnummer >= :fra
                LIMIT :antall
                """.trimIndent()

            it.run(
                queryOf(
                    sql,
                    mapOf(
                        "fra" to fra,
                        "antall" to antall,
                    ),
                ).map { row ->
                    Hendelse(
                        sekvensnummer = row.long("sekvensnummer"),
                        payload = row.toHendelsePayload(),
                    )
                }.asList,
            )
        }

    private fun Row.toHendelsePayload(): HendelsePayload =
        when (val type = string("type")) {
            BygningHendelseType.BRUKSENHET_OPPDATERT.name ->
                BruksenhetOppdatertPayload(
                    objectId = long("object_id"),
                    registreringstidspunkt = sqlTimestamp("registreringstidspunkt").toInstant(),
                )

            else -> {
                throw IllegalArgumentException("Ukjent hendelsestype: $type")
            }
        }
}
