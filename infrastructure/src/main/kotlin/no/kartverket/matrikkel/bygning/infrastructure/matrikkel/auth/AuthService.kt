package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth

interface AuthService {
    suspend fun harMatrikkeltilgang(token: String): String?
}
