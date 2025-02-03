import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import no.kartverket.matrikkel.bygning.plugins.RateLimiterProvider
import org.redisson.api.RRateLimiter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class RateLimitService {
    private val userLimits = ConcurrentHashMap<String, RateLimitCounter>()
    private val MAX_REQUESTS = 5
    private val TIME_WINDOW = 10_000L

    private val rateLimiter: RRateLimiter = RateLimiterProvider.rateLimiter

    fun limitRequests(call: ApplicationCall): Boolean {
        val userId = extractUserId(call) ?: return false  // Hvis ingen bruker-ID, blokker forespørselen. Bypass på noe vis
        val currentTime = System.currentTimeMillis()

        // Bruk Redisson rate limiter basert på bruker-ID
        val redissonAllowed = rateLimiter.tryAcquire()

        // Bruk lokal rate limiter per bruker-ID
        val userAllowed = checkLimit(userLimits, userId, currentTime)

        return redissonAllowed && userAllowed
    }

    private fun extractUserId(call: ApplicationCall): String? {
        return call.principal<JWTPrincipal>()?.payload?.subject
    }

    private fun checkLimit(map: ConcurrentHashMap<String, RateLimitCounter>, key: String, currentTime: Long): Boolean {
        val counter = map.compute(key) { _, existing ->
            val limit = existing ?: RateLimitCounter(AtomicInteger(0), currentTime)

            if (currentTime - limit.timestamp > TIME_WINDOW) {
                RateLimitCounter(AtomicInteger(1), currentTime)
            } else {
                if (limit.counter.incrementAndGet() > MAX_REQUESTS) {
                    null
                } else {
                    limit
                }
            }
        }

        return counter != null
    }

    private data class RateLimitCounter(val counter: AtomicInteger, val timestamp: Long)
}
