import BruksenhetMother.standardBruksenhet
import BruksenhetRegistreringMother.standardBruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningEtasje
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.EtasjeBruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.EnergikildeOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.OppvarmingOpplysning
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.util.UUID

object BruksenhetRegistreringMother {
    fun standardBruksenhetRegistrering(): Builder = Builder()
}

class Builder {
    private var bruksenhetBubbleId: BruksenhetBubbleId = BruksenhetBubbleId(1)
    private var bruksarealRegistrering: BruksarealRegistrering? =
        BruksarealRegistrering(
            totaltBruksareal = 50.0,
            etasjeRegistreringer = emptyList(),
            kildemateriale = KildematerialeKode.Selvrapportert,
        )
    private var byggeaarRegistrering: ByggeaarRegistrering? = null
    private var vannforsyningRegistrering: VannforsyningRegistrering? = null
    private var avlopRegistrering: AvlopRegistrering? = null
    private var energikildeRegistrering: EnergikildeRegistrering? = null
    private var oppvarmingRegistrering: OppvarmingRegistrering? = null

    fun withBruksenhetBubbleId(id: BruksenhetBubbleId) =
        apply {
            this.bruksenhetBubbleId = id
        }

    fun withBruksarealRegistrering(
        bruksareal: Double,
        etasjeRegistreringer: List<EtasjeBruksarealRegistrering>?,
        kildemateriale: KildematerialeKode,
    ) = apply {
        bruksarealRegistrering =
            BruksarealRegistrering(
                totaltBruksareal = bruksareal,
                etasjeRegistreringer = etasjeRegistreringer,
                kildemateriale = kildemateriale,
            )
    }

    fun withByggeaarRegistrering(
        byggeaar: Int,
        kildemateriale: KildematerialeKode,
    ) = apply {
        byggeaarRegistrering = ByggeaarRegistrering(byggeaar = byggeaar, kildemateriale = kildemateriale)
    }

    fun withVannforsyningRegistrering(vannforsyning: VannforsyningRegistrering) =
        apply {
            vannforsyningRegistrering = vannforsyning
        }

    fun withAvlopRegistrering(avlop: AvlopRegistrering) =
        apply {
            avlopRegistrering = avlop
        }

    fun withEnergikilderRegistreringer(energikilder: EnergikildeRegistrering) =
        apply {
            energikildeRegistrering = energikilder
        }

    fun withOppvarmingRegistreringer(oppvarming: OppvarmingRegistrering) =
        apply {
            oppvarmingRegistrering = oppvarming
        }

    fun build(): BruksenhetRegistrering =
        BruksenhetRegistrering(
            bruksenhetBubbleId,
            bruksarealRegistrering,
            byggeaarRegistrering,
            vannforsyningRegistrering,
            avlopRegistrering,
            energikildeRegistrering,
            oppvarmingRegistrering,
        )
}

object BruksenhetMother {
    fun standardBruksenhet(): Builder = Builder()

    class Builder {
        private var id: BruksenhetId = BruksenhetId("00000000-0000-0000-0000-000000000002")
        private var bruksenhetBubbleId: BruksenhetBubbleId = BruksenhetBubbleId(1)
        private var etasjer: Multikilde<BruksenhetEtasjer> = Multikilde()
        private var byggeaar: Multikilde<Byggeaar> = Multikilde()
        private var totaltBruksareal: Multikilde<Bruksareal> = Multikilde()
        private var energikilder: Multikilde<EnergikildeOpplysning> = Multikilde()
        private var oppvarming: Multikilde<OppvarmingOpplysning> = Multikilde()
        private var vannforsyning: Multikilde<Vannforsyning> = Multikilde()
        private var avlop: Multikilde<Avlop> = Multikilde()

        fun withId(id: BruksenhetId) =
            apply {
                this.id = id
            }

        fun withBruksenhetBubbleId(bruksenhetBubbleId: BruksenhetBubbleId) =
            apply {
                this.bruksenhetBubbleId = bruksenhetBubbleId
            }

        fun withEtasjer(etasjer: BruksenhetEtasjer) =
            apply {
                this.etasjer = Multikilde(egenregistrert = etasjer)
            }

        fun withByggeaar(byggeaar: Byggeaar) =
            apply {
                this.byggeaar =
                    Multikilde(
                        egenregistrert = byggeaar,
                    )
            }

        fun withBruksareal(bruksareal: Bruksareal) =
            apply {
                this.totaltBruksareal =
                    Multikilde(
                        egenregistrert = bruksareal,
                    )
            }

        fun withEnergikilder(energikilder: EnergikildeOpplysning) =
            apply {
                this.energikilder =
                    Multikilde(
                        egenregistrert = energikilder,
                    )
            }

        fun withOppvarming(oppvarming: OppvarmingOpplysning) =
            apply {
                this.oppvarming =
                    Multikilde(
                        egenregistrert = oppvarming,
                    )
            }

        fun withVannforsyning(vannforsyning: Vannforsyning) =
            apply {
                this.vannforsyning =
                    Multikilde(
                        autoritativ = vannforsyning,
                    )
            }

        fun withAvlop(avlop: Avlop) =
            apply {
                this.avlop =
                    Multikilde(
                        autoritativ = avlop,
                    )
            }

        fun build(): Bruksenhet =
            Bruksenhet(
                id = id,
                bruksenhetBubbleId = bruksenhetBubbleId,
                etasjer = etasjer,
                byggeaar = byggeaar,
                totaltBruksareal = totaltBruksareal,
                energikilder = energikilder,
                oppvarming = oppvarming,
                vannforsyning = vannforsyning,
                avlop = avlop,
            )
    }
}

object BygningMother {
    fun standardBygning(): Builder = Builder()

    class Builder {
        private var id: BygningId = BygningId("00000000-0000-0000-0000-000000000001")
        private var bygningBubbleId: BygningBubbleId = BygningBubbleId(1)
        private var bygningsnummer: Long = 100
        private var etasjer: List<BygningEtasje> = emptyList()
        private var bruksenheter: List<Bruksenhet> =
            listOf(
                standardBruksenhet().build(),
            )
        private var byggeaar: Multikilde<Byggeaar> = Multikilde()
        private var bruksareal: Multikilde<Bruksareal> = Multikilde()
        private var energikilder: Multikilde<List<Energikilde>> = Multikilde()
        private var oppvarming: Multikilde<List<Oppvarming>> = Multikilde()
        private var vannforsyning: Multikilde<Vannforsyning> = Multikilde()
        private var avlop: Multikilde<Avlop> = Multikilde()

        fun withBygningId(id: BygningId) =
            apply {
                this.id = id
            }

        fun withBygningBubbleId(id: BygningBubbleId) =
            apply {
                this.bygningBubbleId = id
            }

        fun withBygningsnummer(nr: Long) =
            apply {
                this.bygningsnummer = nr
            }

        fun withEtasjer(etasjer: List<BygningEtasje>) =
            apply {
                this.etasjer = etasjer
            }

        fun withBruksenheter(bruksenheter: List<Bruksenhet>) =
            apply {
                this.bruksenheter = bruksenheter
            }

        fun withByggeaar(byggeaar: Byggeaar) =
            apply {
                this.byggeaar = Multikilde(byggeaar)
            }

        fun withBruksareal(bruksareal: Bruksareal) =
            apply {
                this.bruksareal = Multikilde(bruksareal)
            }

        fun withEnergikilder(energikilder: List<Energikilde>) =
            apply {
                this.energikilder = Multikilde(energikilder)
            }

        fun withOppvarming(oppvarming: List<Oppvarming>) =
            apply {
                this.oppvarming = Multikilde(oppvarming)
            }

        fun withVannforsyning(vannforsyning: Vannforsyning) =
            apply {
                this.vannforsyning = Multikilde(vannforsyning)
            }

        fun withAvlop(avlop: Avlop) =
            apply {
                this.avlop = Multikilde(avlop)
            }

        fun build(): Bygning =
            Bygning(
                id,
                bygningBubbleId,
                bygningsnummer,
                etasjer,
                bruksenheter,
                byggeaar,
                bruksareal,
                energikilder,
                oppvarming,
                vannforsyning,
                avlop,
            )
    }
}

object EgenregistreringMother {
    fun standardEgenregistrering(): Builder = Builder()

    class Builder {
        private var id: EgenregistreringId = EgenregistreringId(UUID.randomUUID())
        private var eier: Foedselsnummer = Foedselsnummer("66860475309")
        private var registreringstidspunkt: Instant = Instant.parse("2024-01-01T12:00:00.00Z")
        private var prosess: ProsessKode = ProsessKode.Egenregistrering
        private var bruksenhetRegistrering: BruksenhetRegistrering = standardBruksenhetRegistrering().build()

        fun withId(id: EgenregistreringId) =
            apply {
                this.id = id
            }

        fun withEier(eier: Foedselsnummer) =
            apply {
                this.eier = eier
            }

        fun withRegistreringstidspunkt(registreringstidspunkt: Instant) =
            apply {
                this.registreringstidspunkt = registreringstidspunkt
            }

        fun withProsess(prosess: ProsessKode) =
            apply {
                this.prosess = prosess
            }

        fun withBruksenhetRegistrering(bruksenhetRegistrering: BruksenhetRegistrering) =
            apply {
                this.bruksenhetRegistrering = bruksenhetRegistrering
            }

        fun build(): Egenregistrering =
            Egenregistrering(
                id = id,
                eier = eier,
                registreringstidspunkt = registreringstidspunkt,
                prosess = prosess,
                bruksenhetRegistrering = bruksenhetRegistrering,
            )
    }
}
