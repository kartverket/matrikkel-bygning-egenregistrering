package no.kartverket.matrikkel.bygning.application.models.kodelister

// Kan det finnes flere presentasjonsnavn på én kode? For eksempel per ett på bokmål og ett på nynorsk?
interface IKode {
    val presentasjonsnavn: String
    val beskrivelse: String
}
