package no.kartverket.matrikkel.bygning.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import no.kartverket.matrikkel.bygning.meterRegistry

fun Application.configureMonitoring() {
    install(MicrometerMetrics) {
        registry = meterRegistry
    }
}
