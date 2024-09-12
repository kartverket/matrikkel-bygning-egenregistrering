package no.kartverket.matrikkel.bygning.matrikkel.adapters

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import assertk.assertions.single
import io.mockk.checkUnnecessaryStub
import io.mockk.every
import io.mockk.mockk
import no.kartverket.matrikkel.bygning.matrikkelapi.MatrikkelApi
import no.kartverket.matrikkel.bygning.matrikkelapi.bruksenhetId
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.bruksenhet
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.bruksenhetIds
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.bygning
import no.kartverket.matrikkel.bygning.matrikkelapi.builders.matrikkelBubbleObjectList
import no.kartverket.matrikkel.bygning.matrikkelapi.bygningId
import no.kartverket.matrikkel.bygning.matrikkelapi.matchers.matchId
import no.kartverket.matrikkel.bygning.matrikkelapi.matchers.matchIds
import no.kartverket.matrikkel.bygning.models.Bruksenhet
import no.kartverket.matrikkel.bygning.models.Bygning
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.domain.MatrikkelContext
import no.statkart.matrikkel.matrikkelapi.wsapi.v1.service.store.StoreService
import kotlin.test.Test

class MatrikkelBygningClientTest {
    @Test
    fun bygningUnummerertBruksenhetUtenEtasje() {
        val mockStoreService = mockk<StoreService> {
            val bygningId = bygningId(1L)
            val bruksenhetId = bruksenhetId(2L)
            every { getObject(matchId(bygningId), any()) } returns bygning {
                id = bygningId
                bygningsnummer = 1000L
                bruksenhetIds(bruksenhetId)
            }
            every { getObjects(matchIds(bruksenhetId), any()) } returns matrikkelBubbleObjectList(
                bruksenhet {
                    id = bruksenhetId
                    byggId = bygningId
                },
            )
        }
        val mockApi = mockk<MatrikkelApi.WithAuth> {
            every { matrikkelContext } returns MatrikkelContext()
            every { storeService() } returns mockStoreService
        }

        val client = MatrikkelBygningClient(mockApi)
        val bygning = client.getBygningById(1L)

        assertThat(bygning, "bygning").isNotNull().all {
            prop(Bygning::bygningId).isEqualTo(1L)
            prop(Bygning::bygningNummer).isEqualTo(1000L)
            prop(Bygning::bruksareal).isNull()
            prop(Bygning::bruksenheter).single().all {
                prop(Bruksenhet::bruksenhetId).isEqualTo(2L)
                prop(Bruksenhet::bygningId).isEqualTo(1L)
                prop(Bruksenhet::bruksareal).isNull()
            }
        }

        checkUnnecessaryStub(mockStoreService, mockApi)
    }
}
