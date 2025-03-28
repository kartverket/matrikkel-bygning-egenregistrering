package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders

import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelAvlopKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelVannforsyningKode
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BruksenhetId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BruksenhetIdList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Bygning
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Etasje
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.EtasjeList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.Etasjedata
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EnergikildeKodeIdList
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.OppvarmingsKodeIdList

fun bygning(scope: Bygning.() -> Unit): Bygning =
    Bygning()
        .apply {
            // Fyll inn verdier som aldri vil være null i matrikkelen.
            etasjedata = Etasjedata()
            etasjedata.antallBoenheter = 0
            etasjedata.bruksarealTilBolig = 0.0
            etasjedata.bruksarealTilAnnet = 0.0
            etasjedata.bruksarealTotalt = 0.0
            etasjedata.alternativtAreal = 0.0
            etasjedata.alternativtAreal2 = 0.0
            etasjedata.bruttoarealTilBolig = 0.0
            etasjedata.bruttoarealTilAnnet = 0.0
            etasjedata.bruttoarealTotalt = 0.0
            bygningsstatusHistorikker =
                bygningsstatusHistorikkList(
                    bygningsstatusHistorikk {
                        registrertDato = timestampUtc(2000, 1, 1)
                    },
                )
        }.apply(scope)
        .apply {
            // Fyller inn tomme lister hvis det ikke har blitt fylt inn noe annet.
            if (bruksenhetIds == null) bruksenhetIds = BruksenhetIdList()
            if (energikildeKodeIds == null) energikildeKodeIds = EnergikildeKodeIdList()
            if (oppvarmingsKodeIds == null) oppvarmingsKodeIds = OppvarmingsKodeIdList()
            // Vet ikke om denne bør være fylt ut med noe, vet ikke hvordan matrikkelen dealer med tomme etasjer?
            if (etasjer == null) etasjer = EtasjeList()

            // Fyller inn standard kodeid hvis det ikke har blitt fylt inn noe annet
            if (vannforsyningsKodeId == null) vannforsyningsKodeId = MatrikkelVannforsyningKode.IkkeOppgitt()
            if (avlopsKodeId == null) avlopsKodeId = MatrikkelAvlopKode.IkkeOppgitt()
        }

fun Bygning.bruksenhetIds(vararg bruksenhetIds: BruksenhetId) {
    this.bruksenhetIds =
        BruksenhetIdList().apply {
            item.addAll(bruksenhetIds)
        }
}

fun Bygning.etasjer(vararg etasjer: Etasje) {
    this.etasjer =
        EtasjeList().apply {
            item.addAll(etasjer)
        }
}

fun etasje(scope: Etasje.() -> Unit): Etasje = Etasje().apply(scope)
