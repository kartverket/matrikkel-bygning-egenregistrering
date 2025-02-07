package no.kartverket.matrikkel.bygning.application.transaction

import kotliquery.TransactionalSession

/**
 * Interface for å støtte transaksjoner som går på tvers av flere repositories.
 */
interface Transactional {
    fun <T> withTransaction(block: (tx: TransactionalSession) -> T)
}
