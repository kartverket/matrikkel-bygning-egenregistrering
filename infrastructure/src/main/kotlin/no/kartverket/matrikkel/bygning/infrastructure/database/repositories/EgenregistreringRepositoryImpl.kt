package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringRepository
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import org.intellij.lang.annotations.Language
import org.postgresql.util.PGobject

class EgenregistreringRepositoryImpl : EgenregistreringRepository {
    private val objectMapper =
        jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun saveEgenregistrering(
        egenregistrering: Egenregistrering,
        tx: TransactionalSession,
    ) {
        @Language("PostgreSQL")
        val sql =
            """
            INSERT INTO bygning.egenregistrering
            (id, registreringstidspunkt, eier, registrering)
            VALUES (:id, :registreringstidspunkt, :eier, :registrering)
            """.trimIndent()

        tx.run(
            queryOf(
                sql,
                mapOf(
                    "id" to egenregistrering.id.value,
                    "registreringstidspunkt" to egenregistrering.registreringstidspunkt,
                    "eier" to egenregistrering.eier.value,
                    "registrering" to
                        PGobject().apply {
                            this.type = "jsonb"
                            this.value = objectMapper.writeValueAsString(egenregistrering.bruksenhetRegistrering)
                        },
                ),
            ).asUpdate,
        )
    }
}
