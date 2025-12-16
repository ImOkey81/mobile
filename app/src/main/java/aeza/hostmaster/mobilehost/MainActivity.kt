package aeza.hostmaster.mobilehost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aeza.hostmaster.mobilehost.ui.theme.MobileHostTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileHostTheme {
                MainScreen()
            }
        }
    }
}

private enum class CheckTab(val title: String) {
    HTTP("HTTP/HTTPS"),
    PING("PING"),
    TCP("TCP Port"),
    TRACE("Traceroute"),
    DNS("DNS Lookup")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(CheckTab.HTTP) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Network Health Toolkit") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                CheckTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab.ordinal == index,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) },
                        icon = {
                            when (tab) {
                                CheckTab.HTTP -> Icon(Icons.Default.Speed, contentDescription = null)
                                CheckTab.PING -> Icon(Icons.Default.NetworkCheck, contentDescription = null)
                                CheckTab.TCP -> Icon(Icons.Default.SettingsEthernet, contentDescription = null)
                                CheckTab.TRACE -> Icon(Icons.Default.Route, contentDescription = null)
                                CheckTab.DNS -> Icon(Icons.Default.Language, contentDescription = null)
                            }
                        }
                    )
                }
            }

            Text(
                text = "Запросы уходят на 91.107.126.43:8080 (Basic Auth)",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (selectedTab) {
                CheckTab.HTTP -> HttpCheckScreen()
                CheckTab.PING -> PingCheckScreen()
                CheckTab.TCP -> TcpCheckScreen()
                CheckTab.TRACE -> TraceCheckScreen()
                CheckTab.DNS -> DnsLookupScreen()
            }
        }
    }
}

@Composable
private fun HttpCheckScreen() {
    var target by rememberSaveable { mutableStateOf("https://example.com") }
    var result by remember { mutableStateOf<ServerCheckResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CheckScreenContainer(title = "HTTP/HTTPS доступность") {
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("URL сайта") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ActionButton(loading = loading, label = "Отправить на сервер") {
            scope.launch {
                loading = true
                result = runHttpCheck(target)
                loading = false
            }
        }

        ResultCard(loading = loading) {
            result?.let { res ->
                CheckResultSummary(res)
                res.jobId?.let { jobId ->
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            result = fetchJob(jobId)
                            loading = false
                        }
                    }, enabled = !loading) {
                        Text("Обновить статус задачи")
                    }
                }
            }
        }
    }
}

@Composable
private fun PingCheckScreen() {
    var target by rememberSaveable { mutableStateOf("1.1.1.1") }
    var result by remember { mutableStateOf<ServerCheckResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CheckScreenContainer(title = "PING ICMP") {
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Сайт или IP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ActionButton(loading = loading, label = "Отправить на сервер") {
            scope.launch {
                loading = true
                result = runPingCheck(target)
                loading = false
            }
        }

        ResultCard(loading = loading) {
            result?.let { res ->
                CheckResultSummary(res)
                res.jobId?.let { jobId ->
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            result = fetchJob(jobId)
                            loading = false
                        }
                    }, enabled = !loading) {
                        Text("Обновить статус задачи")
                    }
                }
            }
        }
    }
}

@Composable
private fun TcpCheckScreen() {
    var host by rememberSaveable { mutableStateOf("scanme.nmap.org") }
    var port by rememberSaveable { mutableStateOf("80") }
    var result by remember { mutableStateOf<ServerCheckResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CheckScreenContainer(title = "Порт TCP") {
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Хост") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Порт") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ActionButton(loading = loading, label = "Отправить на сервер") {
            scope.launch {
                loading = true
                result = runTcpCheck(host, port.toIntOrNull() ?: 0)
                loading = false
            }
        }

        ResultCard(loading = loading) {
            result?.let { res ->
                CheckResultSummary(res)
                res.jobId?.let { jobId ->
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            result = fetchJob(jobId)
                            loading = false
                        }
                    }, enabled = !loading) {
                        Text("Обновить статус задачи")
                    }
                }
            }
        }
    }
}

@Composable
private fun TraceCheckScreen() {
    var target by rememberSaveable { mutableStateOf("google.com") }
    var result by remember { mutableStateOf<ServerCheckResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CheckScreenContainer(title = "Traceroute") {
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Сайт или IP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ActionButton(loading = loading, label = "Отправить на сервер") {
            scope.launch {
                loading = true
                result = runTraceroute(target)
                loading = false
            }
        }

        ResultCard(loading = loading) {
            result?.let { res ->
                CheckResultSummary(res)
                res.jobId?.let { jobId ->
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            result = fetchJob(jobId)
                            loading = false
                        }
                    }, enabled = !loading) {
                        Text("Обновить статус задачи")
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsLookupScreen() {
    var target by rememberSaveable { mutableStateOf("example.com") }
    var result by remember { mutableStateOf<ServerCheckResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CheckScreenContainer(title = "DNS Lookup") {
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Домен") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ActionButton(loading = loading, label = "Отправить на сервер") {
            scope.launch {
                loading = true
                result = runDnsLookup(target)
                loading = false
            }
        }

        ResultCard(loading = loading) {
            result?.let { res ->
                CheckResultSummary(res)
                res.jobId?.let { jobId ->
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            result = fetchJob(jobId)
                            loading = false
                        }
                    }, enabled = !loading) {
                        Text("Обновить статус задачи")
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckScreenContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        content()
    }
}

@Composable
private fun ActionButton(
    loading: Boolean,
    label: String,
    action: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = action,
            enabled = !loading
        ) {
            Text(label)
        }
    }
}


@Composable
private fun ResultCard(loading: Boolean, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            content()
        }
    }
}

@Composable
private fun CheckResultSummary(result: ServerCheckResult) {
    Text("Код ответа: ${result.statusCode ?: "нет"}")
    result.jobId?.let { Text("ID задачи: $it") }
    val pingJob = parsePingJob(result.body)
    val httpResult = parseHttpResult(result.body)
    val tracerouteResult = parseTracerouteResult(result.body)
    val metricGroups = parseMetricGroups(result.body)
    when {
        pingJob != null -> PingResultSection(pingJob)
        httpResult != null -> HttpResultSection(httpResult)
        tracerouteResult != null -> TracerouteResultSection(tracerouteResult)
        metricGroups.isNotEmpty() -> MetricsSection(metricGroups)
        else -> {
            result.body?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    result.error?.let { Text("Ошибка: $it", color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun MetricsSection(metricGroups: List<MetricGroup>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Метрики", style = MaterialTheme.typography.titleMedium)

        metricGroups.forEach { group ->
            group.title?.let { title ->
                Text(title, style = MaterialTheme.typography.labelLarge)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                group.metrics.forEach { metric ->
                    MetricRow(metric)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(metric: MetricItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(metric.label, style = MaterialTheme.typography.bodyMedium)
        Text(metric.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HttpResultSection(result: HttpCheckResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("HTTP результат", style = MaterialTheme.typography.titleMedium)
        LabeledRow("Локация", result.location)
        LabeledRow("Страна", result.country)
        result.timeMillis?.let { LabeledRow("Время, мс", it.toString()) }
        result.statusCode?.let { LabeledRow("HTTP код", it.toString()) }
        LabeledRow("IP", result.ip)
        LabeledRow("Результат", result.result)
        result.headers?.takeIf { it.isNotBlank() }?.let {
            Text("Заголовки:")
            Text(it)
        }
    }
}

@Composable
private fun PingResultSection(job: PingJob) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("PING результат", style = MaterialTheme.typography.titleMedium)
        LabeledRow("ID", job.id)
        LabeledRow("Цель", job.target)
        LabeledRow("Статус", job.status)
        LabeledRow("Запуск", job.executedAt)
        LabeledRow("Завершено", job.finishedAt ?: "—")
        job.totalDurationMillis?.let { LabeledRow("Длительность, мс", it.toString()) }

        job.results.forEachIndexed { index, result ->
            Text("Измерение ${index + 1}", style = MaterialTheme.typography.labelLarge)
            LabeledRow("ID результата", result.id)
            LabeledRow("Статус", result.status)
            result.durationMillis?.let { LabeledRow("Длительность, мс", it.toString()) }
            result.ping?.let { ping ->
                PingLatencySummary(ping)
            }
        }
    }
}

@Composable
private fun TracerouteResultSection(result: TracerouteResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Traceroute результат", style = MaterialTheme.typography.titleMedium)

        LabeledRow("ID", result.id)
        LabeledRow("Статус", result.status)
        result.durationMillis?.let { LabeledRow("Длительность, мс", it.toString()) }
        result.message?.takeIf { it.isNotBlank() }?.let {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Сообщение", style = MaterialTheme.typography.bodyMedium)
                Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }

        Text("Хопы", style = MaterialTheme.typography.labelLarge)
        if (result.hops.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    result.hops.forEach { hop ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val hopLabel = hop.hop?.let { "$it" } ?: "—"
                            Text("Хоп $hopLabel", style = MaterialTheme.typography.bodyMedium)
                            Column(horizontalAlignment = Alignment.End) {
                                hop.ip?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                                hop.time?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (hop.ip.isNullOrBlank() && hop.time.isNullOrBlank()) {
                                    Text("нет данных", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("Хопы не получены", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PingLatencySummary(ping: PingMetrics) {
    val latencyMetrics = listOfNotNull(
        ping.minRtt?.let { "Мин" to it },
        ping.avgRtt?.let { "Средн" to it },
        ping.maxRtt?.let { "Макс" to it }
    )

    if (latencyMetrics.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            latencyMetrics.forEach { (label, value) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$label RTT",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatLatency(value)} мс",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatLatency(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}

@Composable
private fun LabeledRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private data class MetricGroup(val title: String?, val metrics: List<MetricItem>)

private data class MetricItem(val label: String, val value: String)

private data class HttpCheckResult(
    val location: String?,
    val country: String?,
    val timeMillis: Long?,
    val statusCode: Int?,
    val ip: String?,
    val result: String?,
    val headers: String?
)

private data class TracerouteResult(
    val id: String?,
    val status: String?,
    val durationMillis: Long?,
    val message: String?,
    val hops: List<TracerouteHop>
)

private data class TracerouteHop(
    val hop: Int?,
    val ip: String?,
    val time: String?
)

private data class PingJob(
    val id: String?,
    val target: String?,
    val status: String?,
    val executedAt: String?,
    val finishedAt: String?,
    val totalDurationMillis: Long?,
    val results: List<PingResult>
)

private data class PingResult(
    val id: String?,
    val type: String?,
    val status: String?,
    val durationMillis: Long?,
    val ping: PingMetrics?
)

private data class PingMetrics(
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

private fun parseMetricGroups(body: String?): List<MetricGroup> {
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

private fun parseHttpResult(body: String?): HttpCheckResult? {
    if (body.isNullOrBlank()) return null

    return runCatching {
        val json = JSONObject(body)

        val httpObject = json.optJSONObject("http")
            ?: json.optJSONObject("result")?.optJSONObject("http")
            ?: json.optJSONObject("results")?.optJSONObject("http")
            ?: json.optJSONArray("result")?.let { results ->
                (0 until results.length()).firstNotNullOfOrNull { index ->
                    results.optJSONObject(index)?.optJSONObject("http")
                }
            }
            ?: json.optJSONArray("results")?.let { results ->
                (0 until results.length()).firstNotNullOfOrNull { index ->
                    results.optJSONObject(index)?.optJSONObject("http")
                }
            }

        httpObject ?: return@runCatching null

        HttpCheckResult(
            location = httpObject.optString("location", null),
            country = httpObject.optString("country", null),
            timeMillis = httpObject.optLong("timeMillis", 0L).takeIf { it > 0 },
            statusCode = httpObject.optInt("statusCode", 0).takeIf { it > 0 },
            ip = httpObject.optString("ip", null),
            result = httpObject.optString("result", null),
            headers = httpObject.optJSONObject("headers")?.toString(2)
                ?: httpObject.optString("headers", null)
        )
    }.getOrNull()
}

private fun parseTracerouteResult(body: String?): TracerouteResult? {
    if (body.isNullOrBlank()) return null

    return runCatching {
        val json = JSONObject(body)
        val resultNode = json.opt("result")
            ?: json.opt("results")
            ?: return@runCatching null

        val resultsArray = when (resultNode) {
            is JSONArray -> resultNode
            is JSONObject -> JSONArray().apply { put(resultNode) }
            else -> return@runCatching null
        }

        val targetResult = (0 until resultsArray.length()).firstNotNullOfOrNull { index ->
            val resultObject = resultsArray.optJSONObject(index) ?: return@firstNotNullOfOrNull null
            val tracerouteObject = resultObject.optJSONObject("traceroute") ?: return@firstNotNullOfOrNull null
            resultObject to tracerouteObject
        } ?: return@runCatching null

        val resultObject = targetResult.first
        val tracerouteObject = targetResult.second

        val hops = tracerouteObject.optJSONArray("hops")?.let { hopsArray ->
            buildList {
                for (index in 0 until hopsArray.length()) {
                    val hopObject = hopsArray.optJSONObject(index) ?: continue
                    add(
                        TracerouteHop(
                            hop = hopObject.optInt("hop", -1).takeIf { it >= 0 },
                            ip = hopObject.optString("ip", null),
                            time = hopObject.optString("time", null)
                        )
                    )
                }
            }
        } ?: emptyList()

        TracerouteResult(
            id = resultObject.optString("id", null),
            status = resultObject.optString("status", null),
            durationMillis = resultObject.optLong("durationMillis", 0L).takeIf { it > 0 },
            message = resultObject.optString("message", null),
            hops = hops
        )
    }.getOrNull()
}

private fun parsePingJob(body: String?): PingJob? {
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

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    MobileHostTheme {
        MainScreen()
    }
}
