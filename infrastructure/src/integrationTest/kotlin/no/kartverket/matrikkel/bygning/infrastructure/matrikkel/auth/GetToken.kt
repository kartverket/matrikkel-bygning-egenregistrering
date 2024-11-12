package no.kartverket.matrikkel.bygning.infrastructure.matrikkel.auth

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Testprogram for å hente ut token gjennom resource owner password grant.
 */
fun main() {
    val console = System.console()
    val url = URI.create(console.readLine("Issuer"))
    val clientId = console.readLine("Client id")
    val clientSecret = console.readPassword("Client secret")
    val username = console.readLine("Username")
    val password = console.readPassword("Password")

    val httpClient = HttpClient.newHttpClient()

    val request = HttpRequest.newBuilder(url.resolve("token"))
        .POST(
            HttpRequest.BodyPublishers.ofString(
                "grant_type=password&username=$username&password=$password&client_id=$clientId&client_secret=$clientSecret",
            ),
        )
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build()
    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    println(response.body())
}
