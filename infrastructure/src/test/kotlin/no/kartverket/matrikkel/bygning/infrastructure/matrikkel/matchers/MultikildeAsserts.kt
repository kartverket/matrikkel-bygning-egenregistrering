package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.matchers

import assertk.Assert
import assertk.all
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import no.kartverket.matrikkel.bygning.application.models.Multikilde

fun <T : Any> Assert<Multikilde<T>>.isEmpty() {
    prop(Multikilde<T>::autoritativ).isNull()
    prop(Multikilde<T>::egenregistrert).isNull()
}

fun <T : Any> Assert<Multikilde<T>>.erAutoritativIkkeEgenregistrert(body: Assert<T>.() -> Unit) = all {
    prop(Multikilde<T>::autoritativ).isNotNull().all(body)
    prop(Multikilde<T>::egenregistrert).isNull()
}
