package aeza.hostmaster.mobilehost.domain.model

enum class CheckType { HTTP, PING, TCP_PORT, TRACEROUTE, DNS_LOOKUP }

data class CheckRequest(
    val type: CheckType,
    val target: String,
    val port: Int? = null
)

data class Credentials(
    val username: String,
    val password: String
)

data class ServerCheckResult(
    val statusCode: Int?,
    val jobId: String?,
    val body: String?,
    val error: String? = null
)

data class MetricGroup(val title: String?, val metrics: List<MetricItem>)

data class MetricItem(val label: String, val value: String)

data class HeaderItem(val name: String, val value: String)

data class HttpMetrics(
    val location: String?,
    val country: String?,
    val ip: String?,
    val statusCode: Int?,
    val timeMillis: Long?,
    val result: String?,
    val headers: List<HeaderItem>?
)

data class PingJob(
    val id: String?,
    val target: String?,
    val status: String?,
    val executedAt: String?,
    val finishedAt: String?,
    val totalDurationMillis: Long?,
    val results: List<PingResult>
)

data class PingResult(
    val id: String?,
    val type: String?,
    val status: String?,
    val durationMillis: Long?,
    val ping: PingMetrics?
)

data class PingMetrics(
    val location: String?,
    val country: String?,
    val ip: String?,
    val transmitted: Int?,
    val received: Int?,
    val packetLoss: Double?,
    val minRtt: Double?,
    val avgRtt: Double?,
    val maxRtt: Double?
)
