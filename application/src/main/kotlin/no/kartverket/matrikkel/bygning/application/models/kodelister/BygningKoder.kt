package no.kartverket.matrikkel.bygning.application.models.kodelister

enum class EtasjeplanKode(
    override val presentasjonsnavn: String, override val beskrivelse: String
) : IKode {
    Loftetasje(
        presentasjonsnavn = "Loftetasje",
        beskrivelse = "Loftetasje",
    ),

    Hovedetasje(
        presentasjonsnavn = "Hovedetasje",
        beskrivelse = "Hovedetasje",
    ),

    Kjelleretasje(
        presentasjonsnavn = "Kjelleretasje",
        beskrivelse = "Kjelleretasje",
    )
}
