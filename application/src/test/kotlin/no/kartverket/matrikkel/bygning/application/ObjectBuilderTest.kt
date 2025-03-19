import no.kartverket.matrikkel.bygning.application.models.BruksarealRegistrering
import no.kartverket.matrikkel.bygning.application.models.Bruksenhet
import no.kartverket.matrikkel.bygning.application.models.BygningEtasje
import no.kartverket.matrikkel.bygning.application.models.Felt.Bruksareal
import no.kartverket.matrikkel.bygning.application.models.Felt.BruksenhetEtasjer
import no.kartverket.matrikkel.bygning.application.models.Multikilde
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.RegistreringAktoer
import no.kartverket.matrikkel.bygning.application.models.ids.BruksenhetBubbleId
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode
import no.kartverket.matrikkel.bygning.application.models.kodelister.ProsessKode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectBuilderTest {

    @Test
    fun `should build Bygning with default values`() {
        val bygning = BygningBuilder().build()
        assertEquals(0L, bygning.bygningsnummer)
        assertEquals(emptyList<BygningEtasje>(), bygning.etasjer)
        assertEquals(emptyList<Bruksenhet>(), bygning.bruksenheter)
    }

    @Test
    fun `should build Bygning with custom values`() {
        val testBygning = BygningBuilder()
            .bygningsnummer(123456)
            .build()

        assertEquals(123456, testBygning.bygningsnummer)
    }

    @Test
    fun `should build Bruksenhet with default values`() {
        val bruksenhet = BruksenhetBuilder().build()
        assertEquals(Multikilde<BruksenhetEtasjer>(), bruksenhet.etasjer)
    }

    @Test
    fun `should build Bruksenhet with custom values`() {
        val testBruksenhet = BruksenhetBuilder()
            .totaltBruksareal(
                Multikilde(
                    Bruksareal(
                        50.0,
                        RegisterMetadata(
                            Instant.now(), RegistreringAktoer.Signatur("datakatalogen"),
                            kildemateriale = KildematerialeKode.Selvrapportert,
                            prosess = ProsessKode.Egenregistrering,
                        ),
                    ),
                ),
            )
            .build()

        assertEquals(50.0, testBruksenhet.totaltBruksareal.autoritativ?.data)
    }

    @Test
    fun `should build EgenregistreringRequest with default values`() {
        val egenregistrering = BruksenhetRegistreringBuilder1().build()
        assertEquals(0L, egenregistrering.bruksenhetBubbleId.value)
        assertEquals(null, egenregistrering.bruksarealRegistrering)
        assertEquals(null, egenregistrering.byggeaarRegistrering)
        assertEquals(null, egenregistrering.energikildeRegistrering)
        assertEquals(null, egenregistrering.oppvarmingRegistrering)
        assertEquals(null, egenregistrering.vannforsyningRegistrering)
        assertEquals(null, egenregistrering.avlopRegistrering)
    }

    @Test
    fun `should build EgenregistreringRequest with custom values`() {
        val egenregistrering = BruksenhetRegistreringBuilder1()
            .bruksenhetBubbleId(BruksenhetBubbleId(123))
            .bruksareal(
                BruksarealRegistrering(
                    100.0,
                    null,
                    KildematerialeKode.Selvrapportert))
            .build()

        assertEquals(123, egenregistrering.bruksenhetBubbleId.value)
        assertEquals(100.0, egenregistrering.bruksarealRegistrering?.totaltBruksareal)
        assertEquals(KildematerialeKode.Selvrapportert, egenregistrering.bruksarealRegistrering?.kildemateriale)
    }
}
