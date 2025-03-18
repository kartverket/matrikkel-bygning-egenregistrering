import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningEtasje
import no.kartverket.matrikkel.bygning.application.models.Egenregistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Felt.Byggeaar
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer.Foedselsnummer
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningBubbleId
import no.kartverket.matrikkel.bygning.application.models.ids.BygningId
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import java.util.*

class BygningBuilder {
    private var id: BygningId = BygningId(UUID.randomUUID())
    private var bygningBubbleId: BygningBubbleId = BygningBubbleId(0L)
    private var bygningsnummer: Long = 0
    private var etasjer: List<BygningEtasje> = emptyList()
    private var bruksenheter: List<Bruksenhet> = emptyList()
    private var byggeaar: Multikilde<Byggeaar> = Multikilde()
    private var bruksareal: Multikilde<Bruksareal> = Multikilde()
    private var energikilder: Multikilde<Energikilde> = Multikilde()
    private var oppvarminger: Multikilde<Oppvarming> = Multikilde()
    private var vannforsyning: Multikilde<Vannforsyning> = Multikilde()
    private var avlop: Multikilde<Avlop> = Multikilde()

    fun id(id: BygningId) = apply { this.id = id }
    fun bygningBubbleId(bygningBubbleId: BygningBubbleId) = apply { this.bygningBubbleId = bygningBubbleId }
    fun bygningsnummer(bygningsnummer: Long) = apply { this.bygningsnummer = bygningsnummer }
    fun etasjer(etasjer: List<BygningEtasje>) = apply { this.etasjer = etasjer }
    fun bruksenheter(bruksenheter: List<Bruksenhet>) = apply { this.bruksenheter = bruksenheter }
    fun byggeaar(byggeaar: Multikilde<Byggeaar>) = apply { this.byggeaar = byggeaar }
    fun bruksareal(bruksareal: Multikilde<Bruksareal>) = apply { this.bruksareal = bruksareal }
    fun energikilder(energikilder: Multikilde<Energikilde>) = apply { this.energikilder = energikilder }
    fun oppvarminger(oppvarminger: Multikilde<Oppvarming>) = apply { this.oppvarminger = oppvarminger }
    fun vannforsyning(vannforsyning: Multikilde<Vannforsyning>) = apply { this.vannforsyning = vannforsyning }
    fun avlop(avlop: Multikilde<Avlop>) = apply { this.avlop = avlop }

    fun build() = Bygning(
        id = id,
        bygningBubbleId = bygningBubbleId,
        bygningsnummer = bygningsnummer,
        etasjer = etasjer,
        bruksenheter = bruksenheter,
        byggeaar = byggeaar,
        bruksareal = bruksareal,
        energikilder = energikilder,
        oppvarminger = oppvarminger,
        vannforsyning = vannforsyning,
        avlop = avlop,
    )
}

class BruksenhetBuilder {
    private var id: BruksenhetId = BruksenhetId(UUID.randomUUID())
    private var bruksenhetBubbleId: BruksenhetBubbleId = BruksenhetBubbleId(0L)
    private var etasjer: Multikilde<BruksenhetEtasjer> = Multikilde()
    private var byggeaar: Multikilde<Byggeaar> = Multikilde()
    private var totaltBruksareal: Multikilde<Bruksareal> = Multikilde()
    private var energikilder: Multikilde<Energikilde> = Multikilde()
    private var oppvarminger: Multikilde<Oppvarming> = Multikilde()
    private var vannforsyning: Multikilde<Vannforsyning> = Multikilde()
    private var avlop: Multikilde<Avlop> = Multikilde()

    fun id(id: BruksenhetId) = apply { this.id = id }
    fun bruksenhetBubbleId(bruksenhetBubbleId: BruksenhetBubbleId) = apply { this.bruksenhetBubbleId = bruksenhetBubbleId }
    fun etasjer(etasjer: Multikilde<BruksenhetEtasjer>) = apply { this.etasjer = etasjer }
    fun byggeaar(byggeaar: Multikilde<Byggeaar>) = apply { this.byggeaar = byggeaar }
    fun totaltBruksareal(totaltBruksareal: Multikilde<Bruksareal>) = apply { this.totaltBruksareal = totaltBruksareal }
    fun energikilder(energikilder: Multikilde<Energikilde>) = apply { this.energikilder = energikilder }
    fun oppvarminger(oppvarminger: Multikilde<Oppvarming>) = apply { this.oppvarminger = oppvarminger }
    fun vannforsyning(vannforsyning: Multikilde<Vannforsyning>) = apply { this.vannforsyning = vannforsyning }
    fun avlop(avlop: Multikilde<Avlop>) = apply { this.avlop = avlop }

    fun build() = Bruksenhet(
        id = id,
        bruksenhetBubbleId = bruksenhetBubbleId,
        etasjer = etasjer,
        byggeaar = byggeaar,
        totaltBruksareal = totaltBruksareal,
        energikilder = energikilder,
        oppvarminger = oppvarminger,
        vannforsyning = vannforsyning,
        avlop = avlop,
    )
}

class BruksenhetRegistreringBuilder {
    private var bruksenhetBubbleId: BruksenhetBubbleId = BruksenhetBubbleId(0L)
    private var bruksarealRegistrering: BruksarealRegistrering? = null
    private var byggeaarRegistrering: ByggeaarRegistrering? = null
    private var vannforsyningRegistrering: VannforsyningRegistrering? = null
    private var avlopRegistrering: AvlopRegistrering? = null
    private var energikildeRegistrering: EnergikildeRegistrering? = null
    private var oppvarmingRegistrering: OppvarmingRegistrering? = null

    fun bruksenhetBubbleId(bruksenhetBubbleId: BruksenhetBubbleId) = apply { this.bruksenhetBubbleId = bruksenhetBubbleId }
    fun bruksarealRegistrering(bruksarealRegistrering: BruksarealRegistrering) =
        apply { this.bruksarealRegistrering = bruksarealRegistrering }

    fun byggeaarRegistrering(byggeaarRegistrering: ByggeaarRegistrering?) = apply { this.byggeaarRegistrering = byggeaarRegistrering }
    fun vannforsyningRegistrering(vannforsyningRegistrering: VannforsyningRegistrering?) =
        apply { this.vannforsyningRegistrering = vannforsyningRegistrering }

    fun avlopRegistrering(avlopRegistrering: AvlopRegistrering?) = apply { this.avlopRegistrering = avlopRegistrering }
    fun energikildeRegistrering(energikildeRegistrering: EnergikildeRegistrering?) =
        apply { this.energikildeRegistrering = energikildeRegistrering }

    fun oppvarmingRegistrering(oppvarmingRegistrering: OppvarmingRegistrering?) =
        apply { this.oppvarmingRegistrering = oppvarmingRegistrering }

    fun build(): BruksenhetRegistrering {
        return BruksenhetRegistrering(
            bruksenhetBubbleId = bruksenhetBubbleId,
            bruksarealRegistrering = bruksarealRegistrering,
            byggeaarRegistrering = byggeaarRegistrering,
            vannforsyningRegistrering = vannforsyningRegistrering,
            avlopRegistrering = avlopRegistrering,
            energikildeRegistrering = energikildeRegistrering,
            oppvarmingRegistrering = oppvarmingRegistrering,
        )
    }
}

class EgenregistreringBuilder {
    private var id: UUID = UUID.randomUUID()
    private var eier: Foedselsnummer? = null
    private var registreringstidspunkt: Instant = Instant.now()
    private var prosess: ProsessKode = ProsessKode.Egenregistrering
    private var bruksenhetRegistrering: BruksenhetRegistrering ?  = null

    fun id(id: UUID) = apply { this.id = id }
    fun eier(eier: Foedselsnummer) = apply { this.eier = eier }
    fun registreringstidspunkt(registreringstidspunkt: Instant) = apply { this.registreringstidspunkt = registreringstidspunkt }
    fun prosess(prosess: ProsessKode) = apply { this.prosess = prosess }
    fun bruksenhetRegistrering(bruksenhetRegistrering: BruksenhetRegistrering) =
        apply { this.bruksenhetRegistrering = bruksenhetRegistrering }

    fun build(): Egenregistrering {
        return Egenregistrering(
            id = id,
            eier = eier!!,
            registreringstidspunkt = registreringstidspunkt,
            prosess = prosess,
            bruksenhetRegistrering = bruksenhetRegistrering!!,
        )
    }
}


