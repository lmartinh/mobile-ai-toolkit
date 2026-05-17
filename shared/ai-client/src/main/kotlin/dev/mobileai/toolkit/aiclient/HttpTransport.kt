package dev.mobileai.toolkit.aiclient

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

interface HttpTransport {
    fun postJson(url: String, headers: Map<String, String>, body: String, timeoutSeconds: Long = 60): HttpResult
}

data class HttpResult(
    val statusCode: Int,
    val body: String
)

class JdkHttpTransport(
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build()
) : HttpTransport {
    override fun postJson(url: String, headers: Map<String, String>, body: String, timeoutSeconds: Long): HttpResult {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .POST(HttpRequest.BodyPublishers.ofString(body))

        headers.forEach { (name, value) -> builder.header(name, value) }

        val response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        return HttpResult(response.statusCode(), response.body())
    }
}
