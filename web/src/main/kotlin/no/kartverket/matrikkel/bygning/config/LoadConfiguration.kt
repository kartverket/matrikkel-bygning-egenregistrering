package no.kartverket.matrikkel.bygning.config

import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.withFallback
import no.kartverket.matrikkel.bygning.config.Env.LOCAL
import no.kartverket.matrikkel.bygning.config.Env.SKIP
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log: Logger = LoggerFactory.getLogger(object {}::class.java)

fun loadConfiguration(environment: ApplicationEnvironment): ApplicationConfig =
    configLocation()
        .let {
            log.info("Loading configuration using file: {}", it)
            // Any properties set in tests or similar will be used first, then fall back to config from configLocation
            environment.config.withFallback(ApplicationConfig(it))
        }

private fun configLocation(): String =
    when (Env.current()) {
        SKIP -> "application.conf"
        LOCAL -> "application-local.conf"
    }
