package no.kartverket.matrikkel.bygning.matrikkel

// TODO Må få gjort disse klassene til det som faktisk brukes i BygningService o.l.
// Per nå ligger alle egenregistreringer og sånt på en egen Bygningstype, og det burde nok justeres
data class Bygning(
    val bygningId: Long,
    val bygningNummer: Long,
    val bruksenheter: List<Bruksenhet>,
)

data class Bruksenhet(
    val bruksenhetId: Long,
    val bygningId: Long,
)
