package no.kartverket.matrikkel.bygning.models

import no.kartverket.matrikkel.bygning.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.models.kodelister.VannforsyningKode
import java.time.Instant

// TODO Sette opp DTOer for Bygning/Bruksenhet hentet fra Matrikkel
data class Bygning(
    val bygningId: Long,
    val bygningsnummer: Long,
    val bruksenheter: List<Bruksenhet>,
    val byggeaar: Byggeaar? = null,
    val bruksareal: Bruksareal? = null,
    val vannforsyning: Vannforsyning? = null,
    val avlop: Avlop? = null,
) {
    fun withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bygning {
        return egenregistreringer.fold(this) { bygningAggregate, egenregistrering ->
            Bygning(
                bygningId = bygningAggregate.bygningId,
                bygningNummer = bygningAggregate.bygningNummer,
                bruksenheter = bygningAggregate.bruksenheter,
                byggeaar = bygningAggregate.byggeaar ?: if (egenregistrering.bygningRegistrering.byggeaarRegistrering != null) {
                    Byggeaar(
                        data = egenregistrering.bygningRegistrering.byggeaarRegistrering.byggeaar,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
                    )
                } else null,
                bruksareal = bygningAggregate.bruksareal ?: if (egenregistrering.bygningRegistrering.bruksarealRegistrering != null) {
                    Bruksareal(
                        data = egenregistrering.bygningRegistrering.bruksarealRegistrering.bruksareal,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
                    )
                } else null,
                vannforsyning = bygningAggregate.vannforsyning
                    ?: if (egenregistrering.bygningRegistrering.vannforsyningRegistrering != null) {
                        Vannforsyning(
                            data = egenregistrering.bygningRegistrering.vannforsyningRegistrering.vannforsyning,
                            metadata = RegisterMetadata(
                                registreringstidspunkt = egenregistrering.registreringstidspunkt,
                            ),
                        )
                    } else null,
                avlop = bygningAggregate.avlop ?: if (egenregistrering.bygningRegistrering.avlopRegistrering != null) {
                    Avlop(
                        data = egenregistrering.bygningRegistrering.avlopRegistrering.avlop,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.registreringstidspunkt,
                        ),
                    )
                } else null,
            )
        }
    }

    fun withBruksenheter(bruksenheter: List<Bruksenhet>): Bygning {
        return this.copy(
            bruksenheter = bruksenheter,
        )
    }
}

data class RegisterMetadata(val registreringstidspunkt: Instant)
data class Bruksareal(val data: Double, val metadata: RegisterMetadata)
data class Byggeaar(val data: Int, val metadata: RegisterMetadata)
data class Vannforsyning(val data: VannforsyningKode, val metadata: RegisterMetadata)
data class Avlop(val data: AvlopKode, val metadata: RegisterMetadata)
data class Energikilde(val data: EnergikildeKode, val metadata: RegisterMetadata)
data class Oppvarming(val data: OppvarmingKode, val metadata: RegisterMetadata)

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
    val bruksareal: Bruksareal? = null,
    val energikilder: List<Energikilde> = emptyList(),
    val oppvarminger: List<Oppvarming> = emptyList(),
) {
    fun withEgenregistrertData(egenregistreringer: List<Egenregistrering>): Bruksenhet {
        // Her så antar jeg at man bare har registrert bruksenheten én gang per registrering, som gir mening
        // Likevel har vi ikke noen logisk sjekk på dette ved registrering, så det bør vi nok ha
        val bruksenhetRegistreringer = egenregistreringer.mapNotNull { egenregistrering ->
            val bruksenhetRegistrering =
                egenregistrering.bygningRegistrering.bruksenhetRegistreringer.filter { it.bruksenhetId == this.bruksenhetId }
                    .firstOrNull()

            if (bruksenhetRegistrering != null) {
                egenregistrering.registreringstidspunkt to bruksenhetRegistrering
            } else {
                null
            }
        }

        // Jeg er litt usikker på om det er nødvendig å filtrere ut og lage et Pair, så folde på bare bruksenhetregistreringene
        // fremfor å gjøre det på hele bygningregistreringen dersom vi legger til logikk for å kun godkjenne én registrering
        return bruksenhetRegistreringer.fold(this) { bruksenhetAggregate, egenregistrering ->
            Bruksenhet(
                bruksenhetId = bruksenhetAggregate.bruksenhetId,
                bygningId = bruksenhetAggregate.bygningId,
                bruksareal = bruksenhetAggregate.bruksareal ?: if (egenregistrering.second.bruksarealRegistrering != null) {
                    Bruksareal(
                        // ser ikke ut som kotlin skjønner at bruksarealRegistrering ikke er null her
                        data = egenregistrering.second.bruksarealRegistrering!!.bruksareal,
                        metadata = RegisterMetadata(
                            registreringstidspunkt = egenregistrering.first,
                        ),
                    )
                } else null,
                energikilder = bruksenhetAggregate.energikilder.takeIf { it.isNotEmpty() }
                    ?: if (egenregistrering.second.energikildeRegistrering?.energikilder?.isNotEmpty() == true) {
                        egenregistrering.second.energikildeRegistrering!!.energikilder.map { registrertKilde ->
                            Energikilde(
                                data = registrertKilde,
                                metadata = RegisterMetadata(
                                    registreringstidspunkt = egenregistrering.first,
                                ),
                            )
                        }
                    } else emptyList(),
                oppvarminger = bruksenhetAggregate.oppvarminger.takeIf { it.isNotEmpty() }
                    ?: if (egenregistrering.second.oppvarmingRegistrering?.oppvarminger?.isNotEmpty() == true) {
                        egenregistrering.second.oppvarmingRegistrering!!.oppvarminger.map { registrertOppvarming ->
                            Oppvarming(
                                data = registrertOppvarming,
                                metadata = RegisterMetadata(
                                    registreringstidspunkt = egenregistrering.first,
                                ),
                            )
                        }
                    } else emptyList(),
            )
        }
    }
}
