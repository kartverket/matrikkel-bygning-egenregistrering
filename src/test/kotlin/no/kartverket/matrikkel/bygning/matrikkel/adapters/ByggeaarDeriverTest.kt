package no.kartverket.matrikkel.bygning.matrikkel.adapters

import assertk.assertThat
import assertk.assertions.isNull
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelBygningsstatusKode
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.bygning
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.localDateUtc
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikk
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.bygning.BygningsstatusHistorikkList
import kotlin.test.Test

class ByggeaarDeriverTest {
    @Test
    fun `bygning med riktig status som ikke er etter terskeldato skal ikke faa byggeaar`() {
        val bygningsStatusHistorikkList = BygningsstatusHistorikkList()

        bygningsStatusHistorikkList.item.add(
            BygningsstatusHistorikk().apply {
                bygningsstatusKodeId = MatrikkelBygningsstatusKode.MidlertidigBrukstillatelse()
                dato = localDateUtc(2009, 4, 25)
            },
        )

        val bygning = bygning {
            bygningsstatusHistorikker = bygningsStatusHistorikkList
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isNull()
    }

    @Test
    fun `bygning med riktig status som er etter terskeldato skal faa byggeaar`() {
        val bygningsStatusHistorikkList = BygningsstatusHistorikkList()

        bygningsStatusHistorikkList.item.add(
            BygningsstatusHistorikk().apply {
                bygningsstatusKodeId = MatrikkelBygningsstatusKode.MidlertidigBrukstillatelse()
                dato = localDateUtc(2009, 4, 26)
            },
        )

        val bygning = bygning {
            bygningsstatusHistorikker = bygningsStatusHistorikkList
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).equals(2009)
    }

    @Test
    fun `bygning med feil status som er etter terskeldato skal ikke faa byggeaar`() {
        val bygningsStatusHistorikkList = BygningsstatusHistorikkList()

        bygningsStatusHistorikkList.item.add(
            BygningsstatusHistorikk().apply {
                bygningsstatusKodeId = MatrikkelBygningsstatusKode.TattIBruk()
                dato = localDateUtc(2009, 4, 26)
            },
        )

        val bygning = bygning {
            bygningsstatusHistorikker = bygningsStatusHistorikkList
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isNull()
    }
}
