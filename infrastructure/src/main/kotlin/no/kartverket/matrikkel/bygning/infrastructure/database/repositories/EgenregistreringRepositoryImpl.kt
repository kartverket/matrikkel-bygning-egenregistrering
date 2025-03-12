package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringRepository
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import org.intellij.lang.annotations.Language
import org.postgresql.util.PGobject

class EgenregistreringRepositoryImpl : EgenregistreringRepository {
    private val objectMapper =
        jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun saveEgenregistrering(egenregistrering: Egenregistrering, tx: TransactionalSession) {
        @Language("PostgreSQL")
        val sql = """
           INSERT INTO bygning.egenregistrering
           (id, registreringstidspunkt, eier, registrering, gjeldende)
           VALUES (:id, :registreringstidspunkt, :eier, :registrering, :gjeldende)
        """.trimIndent()

        tx.run(
            queryOf(
                sql,
                mapOf(
                    "id" to egenregistrering.id.value,
                    "registreringstidspunkt" to egenregistrering.registreringstidspunkt,
                    "eier" to egenregistrering.eier.value,
                    "registrering" to PGobject().apply {
                        this.type = "jsonb"
                        this.value = objectMapper.writeValueAsString(egenregistrering.bruksenhetRegistrering)
                    },
                    "gjeldende" to true,
                ),
            ).asUpdate,
        )
    }

    override fun deleteEgenregistrering(id: EgenregistreringId, tx: TransactionalSession): Egenregistrering? {
        @Language("PostgreSQL")
        val sql = """
           UPDATE bygning.egenregistrering
           SET gjeldende = false
           WHERE id = :id
           RETURNING *
        """.trimIndent()

        return tx.run(
            queryOf(
                sql,
                mapOf(
                    "id" to id.value,
                ),
            ).map { it.toEgenregistrering() }.asSingle,
        )
    }

    override fun getGjeldendeEgenregistreringerForBruksenhet(
        bruksenhetBubbleId: BruksenhetBubbleId,
        tx: TransactionalSession
    ): List<Egenregistrering> {
        @Language("PostgreSQL")
        val sql = """
           SELECT *
           FROM bygning.egenregistrering
           WHERE registrering->>'bruksenhetBubbleId' = :bruksenhetBubbleId
           AND gjeldende = true
        """.trimIndent()

        return tx.run(
            queryOf(
                sql,
                mapOf(
                    // Må toStringe denne siden det er json
                    "bruksenhetBubbleId" to bruksenhetBubbleId.value.toString(),
                ),
            ).map { it.toEgenregistrering() }.asList,
        )
    }

    private fun Row.toEgenregistrering(): Egenregistrering = Egenregistrering(
        id = EgenregistreringId(uuid("id")),
        eier = RegistreringAktoer.Foedselsnummer(string("eier")),
        registreringstidspunkt = sqlTimestamp("registreringstidspunkt").toInstant(),
        prosess = ProsessKode.Egenregistrering,
        bruksenhetRegistrering = objectMapper.readValue<BruksenhetRegistrering>(string("registrering")),
        gjeldende = boolean("gjeldende"),
    )
}
