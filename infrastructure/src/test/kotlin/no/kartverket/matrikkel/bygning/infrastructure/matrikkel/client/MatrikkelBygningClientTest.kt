package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.client

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.exactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import assertk.assertions.single
import io.mockk.checkUnnecessaryStub
import io.mockk.every
import io.mockk.mockk
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BruksenhetId
import no.kartverket.matrikkel.bygning.application.models.Bygning
import no.kartverket.matrikkel.bygning.application.models.BygningId
import no.kartverket.matrikkel.bygning.application.models.Felt.Avlop
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.Energikilde
import no.kartverket.matrikkel.bygning.application.models.Felt.Oppvarming
import no.kartverket.matrikkel.bygning.application.models.Felt.Vannforsyning
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.kodelister.AvlopKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.EnergikildeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.OppvarmingKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.VannforsyningKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.MatrikkelApi
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.bruksenhet
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.bruksenhetIds
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.bygning
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.energikildeKodeIdList
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.etasje
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.etasjer
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.matrikkelBubbleObjectList
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.oppvarmingsKodeIdList
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.builders.timestampUtc
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelAvlopKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelEnergikildeKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelEtasjeplanKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelOppvarmingKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.MatrikkelVannforsyningKode
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.bruksenhetId
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.id.bygningId
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.matchers.erAutoritativIkkeEgenregistrert
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.matchers.isEmpty
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.matchers.matchId
import no.kartverket.matrikkel.bygning.infrastructure.matrikkel.matchers.matchIds
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelContext
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.StoreService
import java.time.Instant
import kotlin.test.Test

class MatrikkelBygningClientTest {
    @Test
    fun `mapping takler minimalt utfylt bygning`() {
        val mockStoreService = mockk<StoreService> {
            val bygningId = bygningId(1L)
            val bruksenhetId = bruksenhetId(2L)
            every { getObject(matchId(bygningId), any()) } returns bygning {
                id = bygningId
                bygningsnummer = 1000L
                oppdateringsdato = timestampUtc(2024, 9, 13)
                oppdatertAv = "TestAnsatt"
                bruksenhetIds(bruksenhetId)
            }
            every { getObjects(matchIds(bruksenhetId), any()) } returns matrikkelBubbleObjectList(
                bruksenhet {
                    id = bruksenhetId
                    byggId = bygningId
                    oppdateringsdato = timestampUtc(2024, 9, 12)
                    oppdatertAv = "TestAnsatt"
                },
            )
        }
        val mockApi = mockk<MatrikkelApi.WithAuth> {
            every { matrikkelContext } returns MatrikkelContext()
            every { storeService() } returns mockStoreService
        }

        val client = MatrikkelBygningClient(mockApi)
        val bygning = client.getBygningById(1L)

        val isMatrikkelfoertBygningstidspunkt = createIsMatrikkelfoertAssert(Instant.parse("2024-09-13T00:00:00.00Z"))
        val isMatrikkelfoertBruksenhetstidspunkt = createIsMatrikkelfoertAssert(Instant.parse("2024-09-12T00:00:00.00Z"))

        assertThat(bygning.value, "bygning").all {
            prop(Bygning::bygningId).isEqualTo(BygningId(1L))
            prop(Bygning::bygningsnummer).isEqualTo(1000L)
            prop(Bygning::bruksareal).erAutoritativIkkeEgenregistrert {
                // TODO: Dette skal egentlig være "vet ikke", som kanskje ikke skal representeres slik
                prop(Bruksareal::data).isEqualTo(0.0)
                prop(Bruksareal::metadata).isMatrikkelfoertBygningstidspunkt()
            }
            prop(Bygning::avlop).isEmpty()
            prop(Bygning::vannforsyning).isEmpty()
            prop(Bygning::energikilder).isEmpty()
            prop(Bygning::oppvarminger).isEmpty()
            prop(Bygning::bruksenheter).single().all {
                prop(Bruksenhet::bruksenhetId).isEqualTo(BruksenhetId(2L))
                prop(Bruksenhet::bygningId).isEqualTo(BygningId(1L))
                prop(Bruksenhet::totaltBruksareal).erAutoritativIkkeEgenregistrert {
                    // TODO: Dette skal egentlig være "vet ikke", som kanskje ikke skal representeres slik
                    prop(Bruksareal::data).isEqualTo(0.0)
                    prop(Bruksareal::metadata).isMatrikkelfoertBruksenhetstidspunkt()
                }
            }
        }

        checkUnnecessaryStub(mockStoreService, mockApi)
    }

    @Test
    fun `mapping mapper alle felter i fullt utfylt bygning`() {
        // Bygningen er ikke fullt utfylt for det som enda ikke blir brukt
        val mockStoreService = mockk<StoreService> {
            val bygningId = bygningId(1L)
            val bruksenhetId = bruksenhetId(2L)
            every { getObject(matchId(bygningId), any()) } returns bygning {
                id = bygningId
                bygningsnummer = 1000L
                oppdateringsdato = timestampUtc(2024, 9, 12)
                oppdatertAv = "TestAnsatt"
                vannforsyningsKodeId = MatrikkelVannforsyningKode.TilknyttetOffVannverk()
                avlopsKodeId = MatrikkelAvlopKode.OffentligKloakk()
                energikildeKodeIds = energikildeKodeIdList(
                    MatrikkelEnergikildeKode.Elektrisitet(),
                    MatrikkelEnergikildeKode.Varmepumpe(),
                )
                oppvarmingsKodeIds = oppvarmingsKodeIdList(
                    MatrikkelOppvarmingKode.Elektrisk(),
                )
                etasjedata.bruksarealTotalt = 150.0
                etasjer(
                    etasje {
                        id = 1L
                        etasjeplanKodeId = MatrikkelEtasjeplanKode.Hovedetasje()
                        etasjenummer = 1
                        bruksarealTotalt = 150.0
                    },
                )
                bruksenhetIds(bruksenhetId)
            }
            every { getObjects(matchIds(bruksenhetId), any()) } returns matrikkelBubbleObjectList(
                bruksenhet {
                    id = bruksenhetId
                    byggId = bygningId
                    oppdateringsdato = timestampUtc(2024, 9, 13)
                    oppdatertAv = "TestAnsatt"
                    bruksareal = 140.0
                },
            )
        }
        val mockApi = mockk<MatrikkelApi.WithAuth> {
            every { matrikkelContext } returns MatrikkelContext()
            every { storeService() } returns mockStoreService
        }

        val client = MatrikkelBygningClient(mockApi)
        val bygning = client.getBygningById(1L)

        val isMatrikkelfoertBygningstidspunkt = createIsMatrikkelfoertAssert(Instant.parse("2024-09-12T00:00:00.00Z"))
        val isMatrikkelfoertBruksenhetstidspunkt = createIsMatrikkelfoertAssert(Instant.parse("2024-09-13T00:00:00.00Z"))

        assertThat(bygning.value, "bygning").isNotNull().all {
            prop(Bygning::bygningId).isEqualTo(BygningId(1L))
            prop(Bygning::bygningsnummer).isEqualTo(1000L)
            prop(Bygning::bruksareal).erAutoritativIkkeEgenregistrert {
                prop(Bruksareal::data).isEqualTo(150.0)
                prop(Bruksareal::metadata).isMatrikkelfoertBygningstidspunkt()
            }
            prop(Bygning::avlop).erAutoritativIkkeEgenregistrert {
                prop(Avlop::data).isEqualTo(AvlopKode.OffentligKloakk)
                prop(Avlop::metadata).isMatrikkelfoertBygningstidspunkt()
            }
            prop(Bygning::vannforsyning).erAutoritativIkkeEgenregistrert {
                prop(Vannforsyning::data).isEqualTo(VannforsyningKode.OffentligVannverk)
                prop(Vannforsyning::metadata).isMatrikkelfoertBygningstidspunkt()
            }
            prop(Bygning::energikilder).erAutoritativIkkeEgenregistrert {
                hasSize(2)
                exactly(1) {
                    it.prop(Energikilde::data).isEqualTo(EnergikildeKode.Elektrisitet)
                }
                exactly(1) {
                    it.prop(Energikilde::data).isEqualTo(EnergikildeKode.Varmepumpe)
                }
                each {
                    it.prop(Energikilde::metadata).isMatrikkelfoertBygningstidspunkt()
                }
            }
            prop(Bygning::oppvarminger).erAutoritativIkkeEgenregistrert {
                single().all {
                    prop(Oppvarming::data).isEqualTo(OppvarmingKode.Elektrisk)
                    prop(Oppvarming::metadata).isMatrikkelfoertBygningstidspunkt()
                }
            }
            prop(Bygning::bruksenheter).single().all {
                prop(Bruksenhet::bruksenhetId).isEqualTo(BruksenhetId(2L))
                prop(Bruksenhet::bygningId).isEqualTo(BygningId(1L))
                prop(Bruksenhet::totaltBruksareal).erAutoritativIkkeEgenregistrert {
                    prop(Bruksareal::data).isEqualTo(140.0)
                    prop(Bruksareal::metadata).isMatrikkelfoertBruksenhetstidspunkt()
                }
            }
        }

        checkUnnecessaryStub(mockStoreService, mockApi)
    }

    private fun createIsMatrikkelfoertAssert(expectedRegistreringstidspunkt: Instant): Assert<RegisterMetadata>.() -> Unit {
        return {
            prop(RegisterMetadata::registreringstidspunkt).isEqualTo(expectedRegistreringstidspunkt)
        }
    }
}
