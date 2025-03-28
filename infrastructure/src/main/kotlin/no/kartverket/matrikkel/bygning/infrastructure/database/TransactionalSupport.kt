package no.kartverket.matrikkel.bygning.infrastructure.database

import kotliquery.TransactionalSession
import kotliquery.sessionOf
import no.kartverket.matrikkel.bygning.application.transaction.Transactional
import javax.sql.DataSource

class TransactionalSupport(
    private val dataSource: DataSource,
) : Transactional {
    override fun <T> withTransaction(operation: (TransactionalSession) -> T) {
        sessionOf(dataSource).use {
            it.transaction { tx ->
                operation(tx)
            }
        }
    }
}
