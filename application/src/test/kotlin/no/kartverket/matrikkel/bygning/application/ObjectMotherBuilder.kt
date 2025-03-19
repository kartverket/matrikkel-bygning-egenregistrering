import no.kartverket.matrikkel.bygning.application.models.AvlopRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.BruksenhetRegistrering
import no.kartverket.matrikkel.bygning.application.models.ByggeaarRegistrering
import no.kartverket.matrikkel.bygning.application.models.EnergikildeRegistrering
import no.kartverket.matrikkel.bygning.application.models.OppvarmingRegistrering
import no.kartverket.matrikkel.bygning.application.models.VannforsyningRegistrering
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import java.util.*

object BruksenhetMother {
    fun bruksenhet(): Builder {
        return Builder()
    }

    class Builder {
        private  var bruksenhetBubbleId: BruksenhetBubbleId = BruksenhetBubbleId(1)

        private var bruksarealRegistrering: BruksarealRegistrering? = BruksarealRegistrering(0.0, null, KildematerialeKode.Selvrapportert)
        private var byggeaarRegistrering: ByggeaarRegistrering = ByggeaarRegistrering(0, KildematerialeKode.Selvrapportert)
        private var vannforsyningRegistrering: VannforsyningRegistrering? = null
        private var avlopRegistrering: AvlopRegistrering? = null
        private var energikildeRegistrering: EnergikildeRegistrering? = null
        private var oppvarmingRegistrering: OppvarmingRegistrering? = null

        fun withBruksenhetBubbleId(id: BruksenhetBubbleId) = apply {
            this.bruksenhetBubbleId = id
        }

        fun withBruksareal(bruksareal: Double, kildemateriale: KildematerialeKode ) = apply {
            bruksarealRegistrering = bruksarealRegistrering?.copy(totaltBruksareal = bruksareal, kildemateriale = kildemateriale)
        }

        fun withByggeaar(byggeaar: Int, kildemateriale: KildematerialeKode) = apply {
            byggeaarRegistrering = byggeaarRegistrering.copy(byggeaar = byggeaar, kildemateriale = kildemateriale)
        }

        fun withVannforsyning(kode: VannforsyningKode, kildemateriale: KildematerialeKode) = apply {
            vannforsyningRegistrering = VannforsyningRegistrering(kode, kildemateriale = kildemateriale)
        }

        fun withAvlop(kode: AvlopKode, kildemateriale: KildematerialeKode) = apply {
            avlopRegistrering = AvlopRegistrering(kode, kildemateriale = kildemateriale)
        }

//        fun withEnergikilder(kilder: List<EnergikildeKode>,) = apply {
//            energikildeRegistrering = EnergikildeRegistrering(kilder, KildematerialeKode.Selvrapportert)
//        }
//
//        fun withOppvarming(koder: List<OppvarmingKode>) = apply {
//            oppvarmingRegistrering = OppvarmingRegistrering(koder, KildematerialeKode.Selvrapportert)
//        }

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

    //predefinert testobkect basert på  objectmother
    val standardBruksenhet = bruksenhet()
        .withBruksenhetBubbleId(BruksenhetBubbleId(1))
        .withBruksareal(
            50.0,
            KildematerialeKode.Selvrapportert
        )
        .withByggeaar(
            2000,
            kildemateriale = KildematerialeKode.Selvrapportert
        )
        .build()
}
