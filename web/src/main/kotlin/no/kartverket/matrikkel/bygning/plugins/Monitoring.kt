package no.kartverket.matrikkel.bygning.plugins

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import no.kartverket.matrikkel.bygning.meterRegistry

fun Application.configureMonitoring() {
    install(MicrometerMetrics) {
        registry = meterRegistry
    }
}
