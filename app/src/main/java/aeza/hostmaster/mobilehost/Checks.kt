package aeza.hostmaster.mobilehost

import android.util.Base64
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val BASE_URL = "http://91.107.126.43:8080"
private const val CHECKS_ENDPOINT = "$BASE_URL/api/checks"
private const val USERNAME = "admin"
private const val PASSWORD = "admin"

data class ServerCheckResult(
    val statusCode: Int?,
    val jobId: String?,
    val body: String?,
    val error: String? = null
)

private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()
}

suspend fun runHttpCheck(target: String): ServerCheckResult = submitCheck(
    type = "HTTP",
    payload = mapOf("target" to target)
)

suspend fun runPingCheck(target: String): ServerCheckResult = submitCheck(
    type = "PING",
    payload = mapOf("target" to target)
)

suspend fun runTcpCheck(host: String, port: Int): ServerCheckResult = submitCheck(
    type = "TCP_PORT",
    payload = mapOf("target" to host, "port" to port)
)

suspend fun runTraceroute(target: String): ServerCheckResult = submitCheck(
    type = "TRACEROUTE",
    payload = mapOf("target" to target)
)

suspend fun runDnsLookup(target: String): ServerCheckResult = submitCheck(
    type = "DNS_LOOKUP",
    payload = mapOf("target" to target)
)

suspend fun fetchJob(jobId: String): ServerCheckResult = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url("$CHECKS_ENDPOINT/$jobId")
        .header("Authorization", buildBasicAuth())
        .get()
        .build()

    return@withContext executeRequest(request)
}

private suspend fun submitCheck(type: String, payload: Map<String, Any?>): ServerCheckResult {
    return withContext(Dispatchers.IO) {
        val body = buildRequestBody(
            payload + mapOf(
                "checkTypes" to listOf(type)
            )
        )

        val request = Request.Builder()
            .url(CHECKS_ENDPOINT)
            .header("Authorization", buildBasicAuth())
            .post(body)
            .build()

        executeRequest(request)
    }
}

private fun buildRequestBody(payload: Map<String, Any?>) =
    JSONObject(payload.toMap()).toString().toRequestBody(jsonMediaType)

private fun buildBasicAuth(): String {
    val creds = "$USERNAME:$PASSWORD"
    val token = Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)
    return "Basic $token"
}

private fun executeRequest(request: Request): ServerCheckResult {
    return try {
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string()
            val jobId = parseJobId(body)
            ServerCheckResult(
                statusCode = response.code,
                jobId = jobId,
                body = body,
                error = null
            )
        }
    } catch (ex: IOException) {
        ServerCheckResult(statusCode = null, jobId = null, body = null, error = ex.localizedMessage)
    }
}

private fun parseJobId(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return runCatching {
        val json = JSONObject(body)
        json.optString("id").takeIf { uuid ->
            uuid.isNotBlank() && runCatching { UUID.fromString(uuid) }.isSuccess
        }
    }.getOrNull()
}
