package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.bygning
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.bygningsstatusHistorikk
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.bygningsstatusHistorikkList
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.localDateUtc
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.timestampUtc
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelBygningsstatusKode
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
                bygningsstatusHistorikk {
                    bygningsstatusKodeId = MatrikkelBygningsstatusKode.FerdigAttest()
                    registrertDato = timestampUtc(2009, 4, 26)
                    dato = localDateUtc(2009, 4, 24)
                }
            )
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isEqualTo(2009)
    }

    @Test
    fun `bygning med riktig status som ikke er etter terskeldato skal ikke faa byggeaar`() {
        val bygning = bygning {
            bygningsstatusHistorikker = bygningsstatusHistorikkList(
                bygningsstatusHistorikk {
                    bygningsstatusKodeId = MatrikkelBygningsstatusKode.FerdigAttest()
                    registrertDato = timestampUtc(2009, 4, 25)
                    dato = localDateUtc(2009, 4, 24)
                }
            )
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isNull()
    }

    @Test
    fun `bygning med feil status som er etter terskeldato skal ikke faa byggeaar`() {
        val bygning = bygning {
            bygningsstatusHistorikker = bygningsstatusHistorikkList(
                bygningsstatusHistorikk {
                    bygningsstatusKodeId = MatrikkelBygningsstatusKode.TattIBruk()
                    registrertDato = timestampUtc(2009, 4, 26)
                    dato = localDateUtc(2009, 4, 24)
                }
            )
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isNull()
    }

    @Test
    fun `bygning med flere gyldige statuser skal bruke eldste status for byggeaar`() {
        val bygning = bygning {
            bygningsstatusHistorikker = bygningsstatusHistorikkList(
                derivableBygningsstatusHistorikk,
                bygningsstatusHistorikk {
                    bygningsstatusKodeId = MatrikkelBygningsstatusKode.TattIBruk()
                    registrertDato = timestampUtc(2010, 4, 26)
                    dato = localDateUtc(2010, 4, 24)
                }
            )
        }

        val derivedByggeaar = deriveByggeaarForBygning(bygning)

        assertThat(derivedByggeaar).isEqualTo(2009)
    }
}
