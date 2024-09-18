@file:Suppress("unused")

package no.kartverket.matrikkel.bygning.matrikkelapi.builders

import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.AvlopsKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EnergikildeKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.EtasjeplanKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.KjokkentilgangKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.OppvarmingsKodeId
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.koder.VannforsyningsKodeId

enum class MatrikkelEtasjeplanKode(private val idValue: Long) {
    IkkeOppgitt(0),
    Hovedetasje(1),
    Kjelleretasje(2),
    Loft(3),
    Underetasje(4);

    operator fun invoke() = EtasjeplanKodeId().apply { value = idValue }
}

enum class MatrikkelVannforsyningKode(private val idValue: Long) {
    IkkeOppgitt(0),
    TilknyttetOffVannverk(1),
    TilknyttetPrivatVannverk(2),
    AnnenPrivatInnlagt(3),
    AnnenPrivatIkkeInnlagt(4);

    operator fun invoke() = VannforsyningsKodeId().apply { value = idValue }
}

enum class MatrikkelAvlopKode(private val idValue: Long) {
    IkkeOppgitt(0),
    OffentligKloakk(1),
    PrivatKloakk(2),
    IngenKloakk(3);

    operator fun invoke() = AvlopsKodeId().apply { value = idValue }
}

enum class MatrikkelEnergikildeKode(private val idValue: Long) {
    Elektrisitet(0),
    OljeParafin(1),
    Biobrensel(2),
    Solenergi(3),
    Varmepumpe(4),
    Gass(5),
    Fjernvarme(6),
    AnnenEnergiKilde(7);

    operator fun invoke() = EnergikildeKodeId().apply { value = idValue }
}

enum class MatrikkelOppvarmingKode(private val idValue: Long) {
    Elektrisk(0),
    Sentralvarme(1),
    AnnenOppvarming(2);

    operator fun invoke() = OppvarmingsKodeId().apply { value = idValue }
}

enum class MatrikkelKjokkentilgangKode(private val idValue: Long) {
    IkkeOppgitt(0),
    Kjokken(1),
    IkkeKjokken(2),
    FellesKjokken(3),
    Ukjent(4);

    operator fun invoke() = KjokkentilgangKodeId().apply { value = idValue }
}
