# API for Bygning

Dette repoet har ansvar for ny bygningsdel som del av Eiendomsregister 2.0. I første omgang er det ønskelig at APIet
skal kunne ta i mot egenregistrert data om bygninger og bruksenheter fra Team Egenregistrering.

## Lokalt oppsett

Prosjektet er satt opp med IntelliJ på MacOS. Dersom noe ikke fungerer, med andre IDEer eller operativsystemer, gjerne
fyll ut README med informasjon om dette.

Prosjektet er bygd og kjørt med `temurin-21` JRE og IntelliJ default Kotlin SDK.

For å kjøre prosjektet må du først sette opp database. Dette kan enkelt gjøres med docker compose:

```sh
$ docker-compose -up db -d
```

Flagget `-d` gjør at loggene fra containeren ikke skrives til stdout. Dersom du ønsker det kan du droppe flagget.

Etter dette kan du kjøre opp applikasjonen enten lokalt, eller som en Docker container.

Hvis du vil kjøre appen som en Docker container kan du kjøre:

```sh
$ docker-compose -up web -d
```

Ellers er det bare å kjøre opp applikasjonen som ønsket via IntelliJ eller kommandolinje. Ingen spesielle hensyn som er
nødvendig rundt miljøkonfigurasjon, det skal ha sane defaults.

## TODOs

* Utvide det faktiske APIet. Akkurat nå er det veldig, veldig enkelt, og er bare til for å lage en slags struktur med
  Java-standard db -> repo -> service -> route
* Sette opp menneske-vennlige logger for lokal utvikling, da disse er ganske kjipe å lese i JSON format
* Error Boundaries på ruting og databasekall