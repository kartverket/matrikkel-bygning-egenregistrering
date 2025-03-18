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
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.ids.EgenregistreringId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object BruksenhetRegistreringMother {
    fun standardBruksenhetRegistrering(): Builder = Builder()
}

class Builder {
    private var bruksenhetBubbleId: BruksenhetBubbleId = BruksenhetBubbleId(1)
    private var bruksarealRegistrering: BruksarealRegistrering? = BruksarealRegistrering(
        totaltBruksareal = 50.0,
        etasjeRegistreringer = emptyList(),
        kildemateriale = KildematerialeKode.Selvrapportert,
    )
    private var byggeaarRegistrering: ByggeaarRegistrering? = null
    private var vannforsyningRegistrering: VannforsyningRegistrering? = null
    private var avlopRegistrering: AvlopRegistrering? = null
    private var energikildeRegistrering: List<EnergikildeRegistrering>? = null
    private var oppvarmingRegistrering: List<OppvarmingRegistrering>? = null

    fun withBruksenhetBubbleId(id: BruksenhetBubbleId) = apply {
        this.bruksenhetBubbleId = id
    }

    fun withBruksareal(
        bruksareal: Double,
        etasjeRegistreringer: List<EtasjeBruksarealRegistrering>?,
        kildemateriale: KildematerialeKode
    ) = apply {
        bruksarealRegistrering = BruksarealRegistrering(
            totaltBruksareal = bruksareal,
            etasjeRegistreringer = etasjeRegistreringer,
            kildemateriale = kildemateriale,
        )
    }

    fun withByggeaar(byggeaar: Int, kildemateriale: KildematerialeKode) = apply {
        byggeaarRegistrering = ByggeaarRegistrering(byggeaar = byggeaar, kildemateriale = kildemateriale)
    }

    fun withVannforsyningRegistrering(
        vannforsyning: VannforsyningRegistrering
    ) = apply {
        vannforsyningRegistrering = vannforsyning
    }

    fun withAvlopRegistrering(
        avlop: AvlopRegistrering
    ) = apply {
        avlopRegistrering = avlop
    }

    fun withEnergikilderRegistreringer(
        energikilder: List<EnergikildeRegistrering>,
    ) = apply {
        energikildeRegistrering = energikilder
    }

    fun withOppvarmingRegistreringer(
        oppvarming: List<OppvarmingRegistrering>,
    ) = apply {
        oppvarmingRegistrering = oppvarming
    }

    fun build(): BruksenhetRegistrering {
        return BruksenhetRegistrering(
            bruksenhetBubbleId,
            bruksarealRegistrering,
            byggeaarRegistrering,
            vannforsyningRegistrering,
            avlopRegistrering,
            energikildeRegistrering,
            oppvarmingRegistrering,
        )
    }
}

object BruksenhetMother {
    fun standardBruksenhet(): Builder = Builder()

    class Builder {
        private var id: BruksenhetId = BruksenhetId("00000000-0000-0000-0000-000000000002")
        private var bruksenhetBubbleId: BruksenhetBubbleId = BruksenhetBubbleId(1)
        private var etasjer: Multikilde<BruksenhetEtasjer> = Multikilde()
        private var byggeaar: Multikilde<Byggeaar> = Multikilde()
        private var totaltBruksareal: Multikilde<Bruksareal> = Multikilde()
        private var energikilder: Multikilde<List<Energikilde>> = Multikilde()
        private var oppvarming: Multikilde<List<Oppvarming>> = Multikilde()
        private var vannforsyning: Multikilde<Vannforsyning> = Multikilde()
        private var avlop: Multikilde<Avlop> = Multikilde()

        fun withId(id: BruksenhetId) = apply {
            this.id = id
        }

        fun withBruksenhetBubbleId(bruksenhetBubbleId: BruksenhetBubbleId) = apply {
            this.bruksenhetBubbleId = bruksenhetBubbleId
        }

        fun withEtasjer(etasjer: BruksenhetEtasjer) = apply {
            this.etasjer = Multikilde(autoritativ = etasjer, egenregistrert = null)
        }

        fun withByggeaar(byggeaar: Int) = apply {
            this.byggeaar = Multikilde(
                autoritativ = Byggeaar(
                    byggeaar,
                    metadata = autorativMetadata(),
                ),
                egenregistrert = null,
            )
        }

        fun withBruksareal(bruksareal: Double) = apply {
            this.totaltBruksareal = Multikilde(
                autoritativ = Bruksareal(
                    bruksareal,
                    metadata = autorativMetadata(),
                ),
                egenregistrert = null,
            )
        }

        fun withEnergikilder(kilder: List<EnergikildeKode>) = apply {
            this.energikilder = Multikilde(
                autoritativ = kilder.map {
                    Energikilde(
                        it,
                        metadata = autorativMetadata(),
                    )
                },
                egenregistrert = null,
            )
        }

        fun withOppvarming(koder: List<OppvarmingKode>) = apply {
            this.oppvarming = Multikilde(
                autoritativ = koder.map {
                    Oppvarming(
                        it,
                        metadata = autorativMetadata(),
                    )
                },
                egenregistrert = null,
            )
        }

        fun withVannforsyning(kode: VannforsyningKode) = apply {
            this.vannforsyning = Multikilde(
                autoritativ = Vannforsyning(
                    kode,
                    metadata = autorativMetadata(),
                ),
                egenregistrert = null,
            )
        }

        fun withAvlop(kode: AvlopKode) = apply {
            this.avlop = Multikilde(
                autoritativ = Avlop(
                    kode,
                    metadata = autorativMetadata(),
                ),
                egenregistrert = null,
            )
        }

        fun autorativMetadata() = RegisterMetadata(
            gyldighetsperiode = Gyldighetsperiode.of(0, 0),
            registrertAv = RegistreringAktoer.Signatur("TestTest"),
            kildemateriale = null,
            prosess = null,
            registreringstidspunkt = LocalDateTime.of(2021, 1, 1, 0, 0).toInstant(ZoneOffset.UTC),
        )

        fun build(): Bruksenhet {
            return Bruksenhet(
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
}

object BygningMother {
    fun standardBygning(): Builder = Builder()

    class Builder {
        private var id: BygningId = BygningId("00000000-0000-0000-0000-000000000001")
        private var bygningBubbleId: BygningBubbleId = BygningBubbleId(1)
        private var bygningsnummer: Long = 100
        private var etasjer: List<BygningEtasje> = emptyList()
        private var bruksenheter: List<Bruksenhet> = listOf(standardBruksenhet().build())
        private var byggeaar: Multikilde<Byggeaar> = Multikilde()
        private var bruksareal: Multikilde<Bruksareal> = Multikilde()
        private var energikilder: Multikilde<List<Energikilde>> = Multikilde()
        private var oppvarming: Multikilde<List<Oppvarming>> = Multikilde()
        private var vannforsyning: Multikilde<Vannforsyning> = Multikilde()
        private var avlop: Multikilde<Avlop> = Multikilde()

        fun withBygningId(id: BygningId) = apply { this.id = id }
        fun withBygningBubbleId(id: BygningBubbleId) = apply { this.bygningBubbleId = id }
        fun withBygningsnummer(nr: Long) = apply { this.bygningsnummer = nr }
        fun withEtasjer(etasjer: List<BygningEtasje>) = apply { this.etasjer = etasjer }
        fun withBruksenheter(bruksenheter: List<Bruksenhet>) = apply { this.bruksenheter = bruksenheter }
        fun withByggeaar(aar: Int, metadata: RegisterMetadata) = apply {
            this.byggeaar = Multikilde(Byggeaar(aar, metadata))
        }

        fun withBruksareal(areal: Double, metadata: RegisterMetadata) = apply {
            this.bruksareal = Multikilde(Bruksareal(areal, metadata))
        }

        fun withEnergikilder(kilder: List<Energikilde>, metadata: RegisterMetadata) = apply {
            this.energikilder = Multikilde(kilder, egenregistrert = null)
        }

        fun withOppvarming(kilder: List<Oppvarming>, metadata: RegisterMetadata) = apply {
            this.oppvarming = Multikilde(kilder, egenregistrert = null)
        }

        fun withVannforsyning(kode: VannforsyningKode, metadata: RegisterMetadata) = apply {
            this.vannforsyning = Multikilde(Vannforsyning(kode, metadata))
        }

        fun withAvlop(kode: AvlopKode, metadata: RegisterMetadata) = apply {
            this.avlop = Multikilde(Avlop(kode, metadata))
        }

        fun build(): Bygning {
            return Bygning(
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
}

object EgenregistreringMother {

    fun standardEgenregistrering(): Builder = Builder()

    class Builder {
        private var id: EgenregistreringId = EgenregistreringId(UUID.randomUUID())
        private var eier: Foedselsnummer = Foedselsnummer("66860475309")
        private var registreringstidspunkt: Instant = Instant.parse("2024-01-01T12:00:00.00Z")
        private var prosess: ProsessKode = ProsessKode.Egenregistrering
        private var bruksenhetRegistrering: BruksenhetRegistrering = standardBruksenhetRegistrering().build()

        fun withId(id: EgenregistreringId) = apply { this.id = id }
        fun withEier(eier: Foedselsnummer) = apply { this.eier = eier }
        fun withRegistreringstidspunkt(registreringstidspunkt: Instant) = apply { this.registreringstidspunkt = registreringstidspunkt }
        fun withProsess(prosess: ProsessKode) = apply { this.prosess = prosess }
        fun withBruksenhetRegistrering(bruksenhetRegistrering: BruksenhetRegistrering) =
            apply { this.bruksenhetRegistrering = bruksenhetRegistrering }

        fun build(): Egenregistrering {
            return Egenregistrering(
                id = id,
                eier = eier,
                registreringstidspunkt = registreringstidspunkt,
                prosess = prosess,
                bruksenhetRegistrering = bruksenhetRegistrering,
            )
        }
    }
}
