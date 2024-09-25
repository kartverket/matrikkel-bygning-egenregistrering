package no.kartverket.matrikkel.bygning.matrikkel.adapters

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.bygning
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.bygningsstatusHistorikk
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.bygningsstatusHistorikkList
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.copy
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.localDateUtc
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.timestampUtc
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.withBygningsstatusKodeId
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.withDato
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.withRegistrertDato
import no.kartverket.matrikkel.bygning.matrikkelapi.id.MatrikkelBygningsstatusKode
import kotlin.test.Test


class ByggeaarDeriverTest {
    val derivableBygningsstatusHistorikk = bygningsstatusHistorikk {
        bygningsstatusKodeId = MatrikkelBygningsstatusKode.MidlertidigBrukstillatelse()
        registrertDato = timestampUtc(2009, 4, 26)
        dato = localDateUtc(2009, 4, 24)
    }

    @Test
    fun `bygning med midlertidig brukstillatelse som er etter terskeldato skal faa byggeaar`() {
        val bygning = bygning {
            bygningsstatusHistorikker = bygningsstatusHistorikkList(derivableBygningsstatusHistorikk)
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isEqualTo(2009)
    }

    @Test
    fun `bygning med ferdigattest som er etter terskeldato skal faa byggeaar`() {
        val bygning = bygning {
            bygningsstatusHistorikker = bygningsstatusHistorikkList(
                derivableBygningsstatusHistorikk.withBygningsstatusKodeId(MatrikkelBygningsstatusKode.FerdigAttest()),
            )
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isEqualTo(2009)
    }

    @Test
    fun `bygning med riktig status som ikke er etter terskeldato skal ikke faa byggeaar`() {
        val bygning = bygning {
            bygningsstatusHistorikker = bygningsstatusHistorikkList(derivableBygningsstatusHistorikk.withRegistrertDato(2009, 4, 25))
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isNull()
    }

    @Test
    fun `bygning med feil status som er etter terskeldato skal ikke faa byggeaar`() {
        val bygning = bygning {
            bygningsstatusHistorikker =
                bygningsstatusHistorikkList(derivableBygningsstatusHistorikk.withBygningsstatusKodeId(MatrikkelBygningsstatusKode.TattIBruk()))
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isNull()
    }

    @Test
    fun `bygning med flere gyldige statuser skal bruke eldst status for byggeår`() {
        val bygning = bygning {
            bygningsstatusHistorikker = bygningsstatusHistorikkList(
                derivableBygningsstatusHistorikk,
                derivableBygningsstatusHistorikk.copy().withRegistrertDato(2010, 4, 26).withDato(2010, 4, 26),
            )
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isEqualTo(2009)
    }
}
