package no.kartverket.matrikkel.bygning.models

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import no.kartverket.matrikkel.bygning.models.RegistreringAktoer.*
import kotlin.test.Test

class RegistreringAktoerTest {
    @Test
    fun `fnr som er for kort skal feile validering`() {
        assertFailure { Foedselsnummer("123") }.messageContains("er ikke gyldig")
    }

    @Test
    fun `fnr med tekst men riktig lengde skal feile validering`() {
        assertFailure { Foedselsnummer("1234567890a") }.messageContains("er ikke gyldig")
    }

    @Test
    fun `fnr med riktig lengde men ikke riktig oppbyggning skal feile validering`() {
        assertFailure { Foedselsnummer("12345678901") }.messageContains("er ikke gyldig")
    }

    @Test
    fun `fnr med riktig lengde og oppbyggning skal valideres`() {
        assertThat(Foedselsnummer("31129956715").value).isEqualTo("31129956715")
    }
}
