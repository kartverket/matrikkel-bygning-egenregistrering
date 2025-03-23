package no.kartverket.matrikkel.bygning.application.models.aggregering

import no.kartverket.matrikkel.bygning.application.models.Gyldighetsperiode
import no.kartverket.matrikkel.bygning.application.models.RegisterMetadata
import no.kartverket.matrikkel.bygning.application.models.kodelister.KildematerialeKode

internal fun RegisterMetadata.withKildemateriale(kildemateriale: KildematerialeKode?): RegisterMetadata =
    this.copy(kildemateriale = kildemateriale)

internal fun RegisterMetadata.withGyldighetsperiode(gyldighetsperiode: Gyldighetsperiode): RegisterMetadata =
    this.copy(gyldighetsperiode = gyldighetsperiode)
