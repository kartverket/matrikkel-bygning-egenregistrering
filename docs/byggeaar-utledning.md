# Utledning av byggeår

## Bakgrunn

Per 31.10.2024 har ikke matrikkelen byggeår satt for verken bygninger eller bruksenheter. Det er flere andre
virksomheter i Norge som har både forsøkt å utlede byggeår fra eksisterende data, eller satt dette selv ut i fra
dokumentasjon/egenregistrering de selv innhenter. Dette gjelder blant annet Enova og SSB. Det har vært uttrykt ønske om
at byggeår settes som et nytt felt for bygninger, og kanskje bruksenheter, i selve eiendomsregistret, slik at man har én
kilde til sannhet for dette.

## Utledning for bygninger

Etter en del graving har det blitt vurdert at byggeår kan utledes ved å se på de historiske bygningsstatusene til en
bygning. For at man skal kunne utlede byggeåret til bygningen må et sett med regler for bygningsstatusene bygningen har
gjelde:

1. Vi vurderer kun bygningsstatuskode FerdigAttest og MidlertidigBrukstillatelse
2. Statusen må være registrert fra og med 25.04.2009*
3. Statusen skal være gjeldende, og ikke være en status markert som slettet
4. Vedtaksdato skal ikke være etter registreringsdato
5. Vedtaksdato skal ikke være langt tilbake i tid, vurdert til år 1000 (sjekke opp)
6. Vedtaksdato og registreringsdato skal ikke være langt frem i tid, vurdert til 100 år fra i dag

\* _Datoen er satt i forbindelse med at alle kommuner var over på et system hvor de kunne føre ferdigattest og
midlertidig brukstillatelse_  
