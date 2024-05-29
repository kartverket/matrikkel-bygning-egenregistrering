# API for Bygning

Dette repoet har ansvar for ny bygningsdel som del av Eiendomsregister 2.0. I første omgang er det ønskelig at APIet skal kunne ta i mot egenregistrert data om bygninger og bruksenheter fra Team Egenregistering.


## Lokalt oppsett

Prosjektet er satt opp med IntelliJ på MacOS. Dersom noe ikke fungerer, med andre IDEer eller operativsystemer, gjerne fyll ut README med informasjon om dette.

Prosjektet er bygd og kjørt med `temurin-21` JRE og IntelliJ default Kotlin SDK.

For å kjøre prosjektet må du først sette opp database. Dette kan enkelt gjøres med docker compose:
```
$ docker-compose -up
```

Etter det er det bare å kjøre opp applikasjonen som vanlig, da denne håndterer databasemigreringer selv.


## TODOs

* Utvide det faktiske APIet. Akkurat nå er det veldig, veldig enkelt, og er bare til for å lage en slags struktur med Java-standard db -> repo -> service -> route
* Håndtere en god måte å samle opp default application config med environment variabler slik at man kan sette env vars i SKIP og få disse overskrevet fra defaultene i `application.conf`
* Sette opp menneske-vennlige logger for lokal utvikling, da disse er ganske kjipe å lese i JSON format