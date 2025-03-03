package no.kartverket.matrikkel.bygning.application.hendelser

import kotliquery.TransactionalSession

interface HendelseRepository {
    fun saveHendelse(payload: HendelsePayload, tx: TransactionalSession)
    fun getHendelser(fra: Long, antall: Long) : List<Hendelse>
}
