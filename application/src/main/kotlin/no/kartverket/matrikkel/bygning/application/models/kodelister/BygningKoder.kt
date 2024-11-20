package no.kartverket.matrikkel.bygning.application.models.kodelister

enum class EtasjeplanKode(
    override val presentasjonsnavn: String, override val beskrivelse: String, val kortnavn: String,
) : IKode {
    // TODO Disse forklaringene er helt grusomme å lese for en lekmann, synes kanskje man kunne gjort sitt for å forbedre det
    Loftetasje(
        presentasjonsnavn = "Loftetasje",
        beskrivelse = "Et tilgjengelig rom over øverste alminnelige etasje som ikke oppfyller kravene til alminnelig etasje. Fri høyde må være større eller lik 1,9m i en bredde på minst 0,6 m. Loftsarealet måles til 0,6m utenfor høyde på minst 1,9m",
        kortnavn = "L",
    ),
    Hovedetasje(
        presentasjonsnavn = "Hovedetasje",
        beskrivelse = "Et plan der underkant dekke er høyere enn 1,5m over planert terrengs gjennonsnittsnivå rundt bygningen, og der den frie bredden i høyde 1,9m må minst være 1,9m",
        kortnavn = "H",
    ),
    Underetasje(
        presentasjonsnavn = "Underetasje",
        beskrivelse = "Et plan der underkant dekke eller himling er høyere enn 0,75m, men høyst 1,5m over planert gjennomsnittsnivå",
        kortnavn = "U",
    ),
    Kjelleretasje(
        presentasjonsnavn = "Kjelleretasje",
        beskrivelse = "Et plan der underkant dekke eller himling er høyst 0,75m over planert terreng gjennonsnittsnivå rundt bygningen",
        kortnavn = "K",
    ),
    IkkeOppgitt(
        presentasjonsnavn = "Ikke Oppgitt",
        beskrivelse = "Ikke oppgitt. Brukes på unummererte bruksenheter",
        kortnavn = "IO"
    );

    companion object {
        fun of(etasjeplanKode: String): EtasjeplanKode {
            return when (etasjeplanKode.uppercase()) {
                Hovedetasje.kortnavn -> Hovedetasje
                Underetasje.kortnavn -> Underetasje
                Loftetasje.kortnavn -> Loftetasje
                Kjelleretasje.kortnavn -> Kjelleretasje
                else -> throw IllegalArgumentException("Ugyldig etasjeplankode: $etasjeplanKode")
            }
        }
    }

    override fun toString(): String {
        return this.kortnavn
    }
}
