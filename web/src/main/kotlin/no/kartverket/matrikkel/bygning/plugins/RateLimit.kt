package no.kartverket.matrikkel.bygning.plugins

import io.ktor.server.application.Application
import no.kartverket.matrikkel.bygning.config.loadConfiguration
import org.redisson.Redisson
import org.redisson.api.RRateLimiter
import org.redisson.api.RateIntervalUnit
import org.redisson.api.RateType
import org.redisson.api.RedissonClient
import org.redisson.config.Config

object RateLimiterProvider {
    lateinit var redisson: RedissonClient
    lateinit var rateLimiter: RRateLimiter

    fun init(application: Application) {
        val config = loadConfiguration(application.environment)
        val redsConfig = Config().apply {
            useSingleServer().address = config.property("redis.uri").getString()
        }
        redisson = Redisson.create(redsConfig)
        rateLimiter = redisson.getRateLimiter("globalRateLimiter")
        rateLimiter.trySetRate(RateType.OVERALL, 5, 10, RateIntervalUnit.SECONDS)
    }
}

fun Application.configureRateLimit() {
    RateLimiterProvider.init(this)
}
