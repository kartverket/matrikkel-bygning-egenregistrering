package no.kartverket.matrikkel.bygning.infrastructure.database.repositories

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.kartverket.matrikkel.bygning.application.egenregistrering.EgenregistreringRepository
import no.kartverket.matrikkel.bygning.application.models.AvsluttEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EgenregistreringBase
import no.kartverket.matrikkel.bygning.application.models.FeltRegistreringType
import no.kartverket.matrikkel.bygning.application.models.KorrigerEgenregistrering
import no.kartverket.matrikkel.bygning.application.models.RegistrerEgenregistrering
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
            (id, bruksenhet_bubble_id, registreringstidspunkt, eier, type_registrering, felt_type, registrering)
            VALUES (:id, :bruksenhet_bubble_id, :registreringstidspunkt, :eier, :type_registrering, :felt_type, :registrering)
            """.trimIndent()

        tx.run(
            queryOf(
                sql,
                mapOf(
                    "id" to egenregistrering.id.value,
                    "bruksenhet_bubble_id" to egenregistrering.bruksenhetRegistrering.bruksenhetBubbleId.value,
                    "registreringstidspunkt" to egenregistrering.registreringstidspunkt,
                    "eier" to egenregistrering.eier.value,
                    "type_registrering" to FeltRegistreringType.LEGG_TIL.name, // Hardkoder for eksisterende egenregistrering
                    "felt_type" to "",
                    "registrering" to
                        PGobject().apply {
                            this.type = "jsonb"
                            this.value = objectMapper.writeValueAsString(egenregistrering.bruksenhetRegistrering)
                        },
                ),
            ).asUpdate,
        )
    }

    override fun saveEgenregistrering(
        egenregistrering: EgenregistreringBase,
        tx: TransactionalSession,
    ) {
        @Language("PostgreSQL")
        val sql =
            """
            INSERT INTO bygning.egenregistrering
            (id, bruksenhet_bubble_id, registreringstidspunkt, eier, type_registrering, felt_type, registrering)
            VALUES (:id, :bruksenhet_bubble_id, :registreringstidspunkt, :eier, :type_registrering, :felt_type, :registrering)
            """.trimIndent()

        tx.run(
            queryOf(
                sql,
                mapOf(
                    "id" to egenregistrering.id.value,
                    "bruksenhet_bubble_id" to egenregistrering.bruksenhetId.value,
                    "registreringstidspunkt" to egenregistrering.registreringstidspunkt,
                    "eier" to egenregistrering.eier.value,
                    "type_registrering" to egenregistrering.type.name,
                    "felt_type" to feltType(egenregistrering),
                    "registrering" to
                        PGobject().apply {
                            this.type = "jsonb"
                            this.value =
                                when (egenregistrering) {
                                    is AvsluttEgenregistrering -> objectMapper.writeValueAsString(egenregistrering.feltRegistrering)
                                    is KorrigerEgenregistrering -> objectMapper.writeValueAsString(egenregistrering.feltRegistrering)
                                    is RegistrerEgenregistrering -> objectMapper.writeValueAsString(egenregistrering.feltRegistrering)
                                }
                        },
                ),
            ).asUpdate,
        )
    }

    private fun feltType(egenregistrering: EgenregistreringBase): String =
        when (egenregistrering) {
            is AvsluttEgenregistrering -> egenregistrering.feltRegistrering.javaClass.simpleName
            is KorrigerEgenregistrering -> egenregistrering.feltRegistrering.javaClass.simpleName
            is RegistrerEgenregistrering -> egenregistrering.feltRegistrering.javaClass.simpleName
        }
}
