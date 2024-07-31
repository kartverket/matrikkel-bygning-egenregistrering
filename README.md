# API for Bygning

Dette repoet har ansvar for ny bygningsdel som del av Eiendomsregister 2.0. I første omgang er det ønskelig at APIet
skal kunne ta i mot egenregistrert data om bygninger og bruksenheter fra Team Egenregistrering.

## Lokalt oppsett

Prosjektet er satt opp med IntelliJ på MacOS. Dersom noe ikke fungerer, med andre IDEer eller operativsystemer, gjerne
fyll ut README med informasjon om dette.

Prosjektet er bygd og kjørt med `temurin-21` JRE og IntelliJ default Kotlin SDK.

### Kjøring av database

For å kjøre applikasjonen må du først starte en database. For lokal kjøring er det definert en postgres-database i 
[docker-compose.yaml](docker-compose.yaml). Her blir det definert et volume under `~/apps/postgres` slik at
databasedata persisteres og overlever omstart av databaseserveren. Dersom man ønsker å fjerne lokale data kan man 
fjerne denne katalogstrukturen.

Databasen kan startes med docker compose:

```sh
$ docker-compose up db -d
```
Flagget `-d` gjør at loggene fra containeren ikke skrives til stdout. Dersom du ønsker det kan du droppe flagget.

### Kjøring av applikasjon

Når databasen kjører, kan du kjøre opp applikasjonen enten lokalt, eller som en Docker container.

Hvis du vil kjøre appen som en Docker container kan du kjøre:

```sh
$ docker-compose up web -d
```

Ellers er det bare å kjøre opp applikasjonen som ønsket via IntelliJ eller kommandolinje. Ingen spesielle hensyn som er
nødvendig rundt miljøkonfigurasjon, det skal ha sane defaults.