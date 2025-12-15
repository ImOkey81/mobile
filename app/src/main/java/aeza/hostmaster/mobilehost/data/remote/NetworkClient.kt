package aeza.hostmaster.mobilehost.data.remote

import aeza.hostmaster.mobilehost.domain.model.CheckRequest
import aeza.hostmaster.mobilehost.domain.model.CheckType
import aeza.hostmaster.mobilehost.domain.model.Credentials
import aeza.hostmaster.mobilehost.domain.model.ServerCheckResult
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
private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

class NetworkClient {
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()
    }

    suspend fun submitCheck(credentials: Credentials, request: CheckRequest): ServerCheckResult {
        return withContext(Dispatchers.IO) {
            val payload = buildPayload(request)
            val body = JSONObject(payload).toString().toRequestBody(jsonMediaType)

            val httpRequest = Request.Builder()
                .url(CHECKS_ENDPOINT)
                .header("Authorization", buildBasicAuth(credentials))
                .post(body)
                .build()

            executeRequest(httpRequest)
        }
    }

    suspend fun fetchJob(credentials: Credentials, jobId: String): ServerCheckResult {
        val httpRequest = Request.Builder()
            .url("$CHECKS_ENDPOINT/$jobId")
            .header("Authorization", buildBasicAuth(credentials))
            .get()
            .build()

        return withContext(Dispatchers.IO) { executeRequest(httpRequest) }
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

    private fun buildPayload(request: CheckRequest): Map<String, Any?> {
        val basePayload = mapOf("checkTypes" to listOf(request.type.name))
        val extra = when (request.type) {
            CheckType.HTTP, CheckType.PING, CheckType.TRACEROUTE, CheckType.DNS_LOOKUP -> mapOf("target" to request.target)
            CheckType.TCP_PORT -> mapOf("target" to request.target, "port" to request.port)
        }
        return basePayload + extra
    }

    private fun buildBasicAuth(credentials: Credentials): String {
        val creds = "${credentials.username}:${credentials.password}"
        val token = android.util.Base64.encodeToString(creds.toByteArray(), android.util.Base64.NO_WRAP)
        return "Basic $token"
    }

    private fun parseJobId(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(body)
            json.optString("id", null)?.takeIf { uuid ->
                runCatching { UUID.fromString(uuid) }.isSuccess
            }
        }.getOrNull()
    }
}
