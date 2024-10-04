package no.kartverket.matrikkel.bygning.models.valuetype

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class FoedselsnummerTest {
    @Test
    fun `fnr som er for kort skal feile validering`() {
        val fnr = Foedselsnummer("123")

        val isValid = fnr.validate()

        assertThat(isValid).isFalse()
    }

    @Test
    fun `fnr med tekst men riktig lengde skal feile validering`() {
        val fnr = Foedselsnummer("1234567890a")

        val isValid = fnr.validate()

        assertThat(isValid).isFalse()
    }

    @Test
    fun `fnr med riktig lengde men ikke riktig oppbyggning skal feile validering`() {
        val fnr = Foedselsnummer("12345678901")

        val isValid = fnr.validate()

        assertThat(isValid).isFalse()
    }

    @Test
    fun `fnr med riktig lengde og oppbyggning skal valideres`() {
        val fnr = Foedselsnummer("31129956715")

        val isValid = fnr.validate()

        assertThat(isValid).isTrue()
    }
}
