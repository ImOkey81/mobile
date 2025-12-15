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
import kotlinx.coroutines.launch
import org.json.JSONObject

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
    val metricGroups = parseMetricGroups(result.body)
    if (metricGroups.isNotEmpty()) {
        MetricsSection(metricGroups)
    } else {
        result.body?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
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

private data class MetricGroup(val title: String?, val metrics: List<MetricItem>)

private data class MetricItem(val label: String, val value: String)

private val metadataKeys = setOf(
    "id",
    "jobId",
    "checkType",
    "checkTypes",
    "createdAt",
    "createdBy",
    "target",
    "host",
    "location",
    "result",
    "results",
    "log",
    "logs",
    "message",
    "error",
    "errors"
)

private fun parseMetricGroups(body: String?): List<MetricGroup> {
    if (body.isNullOrBlank()) return emptyList()

    return runCatching {
        val json = JSONObject(body)
        val groups = mutableListOf<MetricGroup>()

        json.optJSONArray("results")?.let { results ->
            for (index in 0 until results.length()) {
                val result = results.optJSONObject(index) ?: continue
                collectMetrics(result)?.let { metrics ->
                    val title = result.optString("checkType", null)?.takeIf { it.isNotBlank() }
                    groups += MetricGroup(title, metrics)
                }
            }
        }

        json.optJSONObject("result")?.let { result ->
            collectMetrics(result)?.let { metrics ->
                val title = json.optString("checkType", null).takeIf { it.isNotBlank() }
                groups += MetricGroup(title, metrics)
            }
        }

        json.optJSONObject("metrics")?.extractNumericMetrics()?.takeIf { it.isNotEmpty() }?.let { metrics ->
            val title = json.optString("checkType", null).takeIf { it.isNotBlank() }
            groups += MetricGroup(title, metrics)
        }

        if (groups.isEmpty()) {
            collectMetrics(json)?.let { metrics ->
                val title = json.optString("checkType", null).takeIf { it.isNotBlank() }
                groups += MetricGroup(title, metrics)
            }
        }

        groups
    }.getOrElse { emptyList() }
}

private fun collectMetrics(container: JSONObject): List<MetricItem>? {
    val mergedMetrics = linkedMapOf<String, MetricItem>()

    container.extractNumericMetrics(metadataKeys).forEach { metric ->
        mergedMetrics[metric.label] = metric
    }

    container.optJSONObject("metrics")?.extractNumericMetrics()?.forEach { metric ->
        mergedMetrics[metric.label] = metric
    }

    return mergedMetrics.values.takeIf { it.isNotEmpty() }?.toList()
}

private fun JSONObject.extractNumericMetrics(
    ignoredKeys: Set<String> = emptySet(),
    prefix: String = ""
): List<MetricItem> {
    val metrics = mutableListOf<MetricItem>()
    keys().forEach { key ->
        if (ignoredKeys.contains(key)) return@forEach

        val value = opt(key)
        val label = if (prefix.isNotBlank()) "$prefix.$key" else key
        when (value) {
            is Number, is Boolean -> metrics += MetricItem(
                label = formatMetricLabel(label),
                value = value.toString()
            )
            is JSONObject -> metrics += value.extractNumericMetrics(ignoredKeys, label)
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
