## Retningslinjer for commit-meldinger

Commit-meldinger skal dokumentere endringer som er gjort i koden, slik at det er enkelt å lese loggen i ettertid (git blame) for å forstå hvorfor en endringer gjort.

En commit-melding skal være så utfyllende at det er unødvendig å forklare endringen i en pull-request eller JIRA-sak.

### **Eksempel på commit-melding:**

```
Kort oppsummering av endringen (helst 50 tegn eller mindre)

Beskriv problemet som endringen skal løse. 
Det er viktig med en blank linje etter tittelen for at ulike 
git-verktøy skal vise commit-melding korrekt i etterkant.

Fokuser på hvorfor endringene har blitt gjort, og ikke så mye på hvordan 
(koden forklarer i utgangspunktet dette).
Alt ettersom hvilken endring det er snakk om,
kan man enten ha en lang eller kort tekst her.

- Punktliste er også ok.
- Bruk bindestrek, etterfulgt av ett mellomrom for å lage punktliste.

Hvis du ønsker kan man ha referanser til issue tracker (f.eks. JIRA), 
eller andre PR-er på slutten.

JIRA: XX-123
Se også: #234
```

### **Format**

Commit-meldingen bør fokusere på *hvorfor* noe har blitt gjort og *hvordan* det fungerer. Det bør ikke bare være en opplisting av *hva* som er blitt gjort. En hjelpsom commit-melding forklarer altså ikke hva som er gjort, men hvorfor endringen er gjort.

### **Språk**

Commit-meldinger skal være på norsk. Dette gjelder også navn på brancher.

Husk at alle kan lese commit-meldingene. Pass derfor på språkbruken i commit-meldingene da upassende commit-meldinger kan stille både Kartverket og deg personlig i et dårlig lys.

Husk også at både beskrivelser og diskusjoner i Pull Requests er åpent tilgjengelig. Pass derfor på hvordan man ordlegger seg, og hva man deler av informasjon.

### **JIRA-saksnummer**

I utgangspunktet bør commit-meldingen kunne stå på egne bein og at man ikke alltid trenger å referere til interne issue-tracking systemer som JIRA.
Eksterne brukere har heller ikke tilgang til JIRA, og har derfor ikke forutsetninger for å forstå en endring.

Det kan likevel være nyttig å kunne referere til JIRA-saksnummer eller andre PR-er.
Dette skal i så fall legges nederst i commit-meldingen slik:

```
Hvis du ønsker kan man ha referanser til issue tracker (f.eks. JIRA), 
eller andre PR-er på slutten.

JIRA: XX-123
Se også: #234
```

### **Commits på brancher**

Disse retningslinjene er først og fremst tiltenkt hvordan commit-historikken bør se ut på hovedbranchen (`main`)
Når man jobber på en branch er det ingen krav til hvordan commitene ser ut underveis, men man bør sikre at commit-ene er etter retningslinjene før man merger.
Dette kan f.eks. gjøres ved å skrive om git-historien på brancher før de merges til `main` for å unngå slike commit-meldinger på hovedbranchen:

```
Fikser feilen
Fikser feilen (nå bør det funke!)
Glemte en fil
```

Husk også at det i flere tilfeller gir mening å dele opp en PR i flere commits, og la alle disse commit-ene bli med på hovedbransjen uten å squashe commits.

### Ressurser

- https://github.com/navikt/offentlig/blob/main/guider/commit-meldinger.md
- https://github.com/erlang/otp/wiki/writing-good-commit-messages
- https://gist.github.com/robertpainsi/b632364184e70900af4ab688decf6f53
- [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/)
