package no.kartverket.matrikkel.bygning.application.hendelser

class HendelseService(
    private val hendelseRepository: HendelseRepository
) {
    fun getHendelser(fra: Long, antall: Long): List<Hendelse> {
        return hendelseRepository.getHendelser(fra, antall)
    }
}
