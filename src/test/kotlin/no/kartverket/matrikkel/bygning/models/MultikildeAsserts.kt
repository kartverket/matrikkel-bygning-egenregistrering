package no.kartverket.matrikkel.bygning.models

import assertk.Assert
import assertk.all
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop

fun <T : Any> Assert<Multikilde<T>>.isEmpty() {
    prop(Multikilde<T>::autoritativ).isNull()
    prop(Multikilde<T>::egenregistrert).isNull()
}

fun <T : Any> Assert<Multikilde<T>>.erAutoritativIkkeEgenregistrert(body: Assert<T>.() -> Unit) = all {
    prop(Multikilde<T>::autoritativ).isNotNull().all(body)
    prop(Multikilde<T>::egenregistrert).isNull()
}
