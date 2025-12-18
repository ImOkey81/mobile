package aeza.hostmaster.mobilehost.presentation.checks

import aeza.hostmaster.mobilehost.domain.model.DnsLookupResult
import aeza.hostmaster.mobilehost.domain.model.HttpCheckResult
import aeza.hostmaster.mobilehost.domain.model.MetricGroup
import aeza.hostmaster.mobilehost.domain.model.MetricItem
import aeza.hostmaster.mobilehost.domain.model.PingJob
import aeza.hostmaster.mobilehost.domain.model.PingMetrics
import aeza.hostmaster.mobilehost.domain.model.PingResult
import aeza.hostmaster.mobilehost.domain.model.TcpCheckResult
import aeza.hostmaster.mobilehost.domain.model.TracerouteCheckResult
import aeza.hostmaster.mobilehost.domain.model.TracerouteHop
import org.json.JSONObject

internal fun parseMetricGroups(body: String?): List<MetricGroup> {
    if (body.isNullOrBlank()) return emptyList()

    return runCatching {
        val json = JSONObject(body)
        val groups = mutableListOf<MetricGroup>()

        json.optJSONArray("results")?.let { results ->
            for (index in 0 until results.length()) {
                val result = results.optJSONObject(index) ?: continue
                val metrics = result.optJSONObject("metrics")?.toMetricItems().orEmpty()
                if (metrics.isNotEmpty()) {
                    val title = result.optString("checkType", null)?.takeIf { it.isNotBlank() }
                    groups += MetricGroup(title, metrics)
                }
            }
        }

        json.optJSONObject("metrics")?.toMetricItems()?.takeIf { it.isNotEmpty() }?.let { metrics ->
            val title = json.optString("checkType", null).takeIf { it.isNotBlank() }
            groups += MetricGroup(title, metrics)
        }

        groups
    }.getOrElse { emptyList() }
}

internal fun parsePingJob(body: String?): PingJob? {
    if (body.isNullOrBlank()) return null

    return runCatching {
        val json = JSONObject(body)
        val resultsArray = json.optJSONArray("result") ?: return@runCatching null

        val pingResults = buildList {
            for (index in 0 until resultsArray.length()) {
                val resultObject = resultsArray.optJSONObject(index) ?: continue
                val pingObject = resultObject.optJSONObject("ping") ?: continue

                add(
                    PingResult(
                        id = resultObject.optString("id", null),
                        type = resultObject.optString("type", null),
                        status = resultObject.optString("status", null),
                        durationMillis = resultObject.optLong("durationMillis", 0L).takeIf { it > 0 },
                        ping = PingMetrics(
                            location = pingObject.optString("location", null),
                            country = pingObject.optString("country", null),
                            ip = pingObject.optString("ip", null),
                            transmitted = pingObject.optInt("transmitted", -1).takeIf { it >= 0 },
                            received = pingObject.optInt("received", -1).takeIf { it >= 0 },
                            packetLoss = pingObject.optDouble("packetLoss", Double.NaN).takeUnless { it.isNaN() },
                            minRtt = pingObject.optDouble("minRtt", Double.NaN).takeUnless { it.isNaN() },
                            avgRtt = pingObject.optDouble("avgRtt", Double.NaN).takeUnless { it.isNaN() },
                            maxRtt = pingObject.optDouble("maxRtt", Double.NaN).takeUnless { it.isNaN() }
                        )
                    )
                )
            }
        }

        if (pingResults.isEmpty()) return@runCatching null

        PingJob(
            id = json.optString("id", null),
            target = json.optString("target", null),
            status = json.optString("status", null),
            executedAt = json.optString("executedAt", null),
            finishedAt = json.optString("finishedAt", null),
            totalDurationMillis = json.optLong("totalDurationMillis", 0L).takeIf { it > 0 },
            results = pingResults
        )
    }.getOrNull()
}

internal fun parseHttpResult(body: String?): HttpCheckResult? {
    if (body.isNullOrBlank()) return null

    return runCatching {
        val resultObject = JSONObject(body).optJSONArray("result")?.optJSONObject(0) ?: return@runCatching null
        val httpObject = resultObject.optJSONObject("http") ?: return@runCatching null

        HttpCheckResult(
            id = resultObject.optString("id", null).takeIf { it.isNotBlank() },
            status = resultObject.optString("status", null).takeIf { it.isNotBlank() },
            durationMillis = resultObject.optLong("durationMillis", 0L).takeIf { it > 0 },
            location = httpObject.optString("location", null).takeIf { it.isNotBlank() },
            country = httpObject.optString("country", null).takeIf { it.isNotBlank() },
            timeMillis = httpObject.optLong("timeMillis", 0L).takeIf { it > 0 },
            statusCode = httpObject.optInt("statusCode", -1).takeIf { it >= 0 },
            ip = httpObject.optString("ip", null).takeIf { it.isNotBlank() },
            result = httpObject.optString("result", null).takeIf { it.isNotBlank() }
        )
    }.getOrNull()
}

internal fun parseTcpResult(body: String?): TcpCheckResult? {
    if (body.isNullOrBlank()) return null

    return runCatching {
        val resultObject = JSONObject(body).optJSONArray("result")?.optJSONObject(0) ?: return@runCatching null
        val tcpObject = resultObject.optJSONObject("tcp") ?: return@runCatching null

        TcpCheckResult(
            id = resultObject.optString("id", null).takeIf { it.isNotBlank() },
            status = resultObject.optString("status", null).takeIf { it.isNotBlank() },
            durationMillis = resultObject.optLong("durationMillis", 0L).takeIf { it > 0 },
            location = tcpObject.optString("location", null).takeIf { it.isNotBlank() },
            country = tcpObject.optString("country", null).takeIf { it.isNotBlank() },
            connectTimeMillis = tcpObject.optLong("connectTimeMillis", 0L).takeIf { it > 0 },
            connectionStatus = tcpObject.optString("status", null).takeIf { it.isNotBlank() },
            ip = tcpObject.optString("ip", null).takeIf { it.isNotBlank() }
        )
    }.getOrNull()
}

internal fun parseTracerouteResult(body: String?): TracerouteCheckResult? {
    if (body.isNullOrBlank()) return null

    return runCatching {
        val resultObject = JSONObject(body).optJSONArray("result")?.optJSONObject(0) ?: return@runCatching null
        val tracerouteObject = resultObject.optJSONObject("traceroute") ?: return@runCatching null

        val hops = buildList {
            tracerouteObject.optJSONArray("hops")?.let { hopsArray ->
                for (index in 0 until hopsArray.length()) {
                    val hopObject = hopsArray.optJSONObject(index) ?: continue
                    add(
                        TracerouteHop(
                            hop = hopObject.optInt("hop", -1).takeIf { it >= 0 },
                            ip = hopObject.optString("ip", null).takeIf { it.isNotBlank() },
                            time = hopObject.optString("time", null).takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
        }

        if (hops.isEmpty()) return@runCatching null

        TracerouteCheckResult(
            id = resultObject.optString("id", null).takeIf { it.isNotBlank() },
            status = resultObject.optString("status", null).takeIf { it.isNotBlank() },
            durationMillis = resultObject.optLong("durationMillis", 0L).takeIf { it > 0 },
            message = resultObject.optString("message", null).takeIf { it.isNotBlank() },
            hops = hops
        )
    }.getOrNull()
}

internal fun parseDnsLookupResult(body: String?): DnsLookupResult? {
    if (body.isNullOrBlank()) return null

    return runCatching {
        val resultObject = JSONObject(body).optJSONArray("result")?.optJSONObject(0) ?: return@runCatching null
        val dnsObject = resultObject.optJSONObject("dns") ?: return@runCatching null

        val records = buildList {
            dnsObject.optJSONArray("records")?.let { recordsArray ->
                for (index in 0 until recordsArray.length()) {
                    recordsArray.optString(index, null)?.takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }
        }

        if (records.isEmpty()) return@runCatching null

        DnsLookupResult(
            id = resultObject.optString("id", null).takeIf { it.isNotBlank() },
            status = resultObject.optString("status", null).takeIf { it.isNotBlank() },
            durationMillis = resultObject.optLong("durationMillis", 0L).takeIf { it > 0 },
            location = dnsObject.optString("location", null).takeIf { it.isNotBlank() },
            country = dnsObject.optString("country", null).takeIf { it.isNotBlank() },
            records = records,
            ttl = dnsObject.optString("ttl", null).takeIf { it.isNotBlank() }
        )
    }.getOrNull()
}

private fun JSONObject.toMetricItems(): List<MetricItem> {
    val metrics = mutableListOf<MetricItem>()
    keys().forEach { key ->
        when (val value = opt(key)) {
            is JSONObject -> {
                value.keys().forEach { nestedKey ->
                    metrics += MetricItem(
                        label = formatMetricLabel("$key.$nestedKey"),
                        value = value.opt(nestedKey).toString()
                    )
                }
            }
            null -> Unit
            else -> metrics += MetricItem(formatMetricLabel(key), value.toString())
        }
    }
    return metrics
}

private fun formatMetricLabel(raw: String): String {
    return raw
        .replace("_", " ")
        .replace(".", " ")
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.lowercase().replaceFirstChar { char ->
                char.titlecase()
            }
        }
}
