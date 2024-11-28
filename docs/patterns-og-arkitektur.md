# Patterns, arkitektur og andre tekonologivalg

Følgende side har oversikt over hvilke teknologiske valg vi har gjort i Team Bygning, som kan være kjekt å vite om som
utvikler. Dette inkluderer overordnet arkitektur, code patterns og andre ting.

## Ports and Adapters

Ports and Adapters, Clean Architecture, Hexagonal Architecture, kjært barn har mange navn! Dette er en beslutning som er
tatt på litt overordnet nivå for et repoet, som har et par fordeler. Her er et diagram som forklarer konseptet.

![Ports and Adapters](https://www.arhohuttunen.com/media/post/hexagonal-architecture/hexagonal-architecture-external-dependencies.svg)

Kort fortalt så sikrer ports and adapters at applikasjonslogikken, eventuelt forretningslogikken i applikasjonen,
ikke har noe forhold til hvordan ting utenfor er implementert. For å ta et enkelt eksempel: Hvis du har en
kalkulatorapplikasjon som skal få inn tall og summere disse, kan du gjerne ha logikk for hvordan summere tall, men
akkurat hvor tallene kommer fra er egentlig ikke så nøye. Det eneste du egentlig trenger å vite er en liste med tall å
summere.

Sånn man løser dette er at forretningslogikken også definerer interfacer (eller ports) den behøver for å kunne
gjennomføre logikken sin, så er det opp til orkestreringen av applikasjonen hvilke implementasjoner (eller adapters) man
skal bruke i hvilke sammenhenger.

I dette repoet så foregår alt forretningslogikk i `application` modulen, mens orkestreringen foregår i `web` modulen.

## Feilhåndtering via Result

I dette repoet bruker vi et eksternt bibliotek kalt `kotlin-result` for feilhåndtering. Dette følger en mer funksjonell
approach til feilhåndtering enn det man vanligvis får via Exceptions. Result gjør at vi kan følge et pattern som kan
kalles for "Railway Oriented Programming".

![Railway Oriented Programming](https://miro.medium.com/v2/resize:fit:1400/0*9d1qrHFWZ8IoTH8W)

Et Result fra `kotlin-result` består av en wrapper, Result, som har to verdier, value/success og error/failure. Når man
returnerer et Result så kan man fortsette å operere på resultatet som om alt har gått bra. Dette forenkler logikken
ytterst i web-laget, hvor vi kan chaine operasjoner, uavhengig av resultatet av for eksempel en validering. Her ser man
et eksempel på dette som "tulle-kode":

```kotlin
val resultat = service.hentData()
    .andThen { data ->
        validerData(data)
    }
    .andThen { validertData ->
        sjekkNoeGreier(validertData)
    }
    .fold(
        success = sjekketOgValidertData.toResponse(),
        failure = error.toResponse(),
    )
```

Her returnerer `hentData`, `validerData` og `sjekkNoeGreier` alle en Result som blir mappet videre i `andThen` som om
alt har gått bra, og til slutt "folder" vi over resultatet og lager et sluttresultat avhengig av om alt har gått bra
eller om noe gikk feil i løpet av chainen.
