package aeza.hostmaster.mobilehost.domain.model

enum class CheckType { HTTP, PING, TCP, TRACEROUTE, DNS_LOOKUP }

data class CheckRequest(
    val type: CheckType,
    val target: String
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

data class HttpCheckResult(
    val id: String?,
    val status: String?,
    val durationMillis: Long?,
    val location: String?,
    val country: String?,
    val timeMillis: Long?,
    val statusCode: Int?,
    val ip: String?,
    val result: String?
)

data class TcpCheckResult(
    val id: String?,
    val status: String?,
    val durationMillis: Long?,
    val location: String?,
    val country: String?,
    val connectTimeMillis: Long?,
    val connectionStatus: String?,
    val ip: String?
)

data class TracerouteHop(val hop: Int?, val ip: String?, val time: String?)

data class TracerouteCheckResult(
    val id: String?,
    val status: String?,
    val durationMillis: Long?,
    val message: String?,
    val hops: List<TracerouteHop>
)

data class DnsLookupResult(
    val id: String?,
    val status: String?,
    val durationMillis: Long?,
    val location: String?,
    val country: String?,
    val records: List<String>,
    val ttl: String?
)
