package aeza.hostmaster.mobilehost.presentation.checks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aeza.hostmaster.mobilehost.domain.model.MetricGroup
import aeza.hostmaster.mobilehost.domain.model.MetricItem
import aeza.hostmaster.mobilehost.domain.model.PingJob
import aeza.hostmaster.mobilehost.domain.model.PingMetrics
import aeza.hostmaster.mobilehost.domain.model.ServerCheckResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecksScreen(
    state: CheckUiState,
    onTabSelected: (CheckTab) -> Unit,
    onHttpTargetChange: (String) -> Unit,
    onPingTargetChange: (String) -> Unit,
    onTcpHostChange: (String) -> Unit,
    onTcpPortChange: (String) -> Unit,
    onTraceTargetChange: (String) -> Unit,
    onDnsTargetChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRefreshJob: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message -> snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Network Health Toolkit") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                CheckTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.selectedTab.ordinal == index,
                        onClick = { onTabSelected(tab) },
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

            CheckContent(
                state = state,
                onHttpTargetChange = onHttpTargetChange,
                onPingTargetChange = onPingTargetChange,
                onTcpHostChange = onTcpHostChange,
                onTcpPortChange = onTcpPortChange,
                onTraceTargetChange = onTraceTargetChange,
                onDnsTargetChange = onDnsTargetChange,
                onSubmit = onSubmit,
                onRefreshJob = onRefreshJob
            )
        }
    }
}

@Composable
private fun CheckContent(
    state: CheckUiState,
    onHttpTargetChange: (String) -> Unit,
    onPingTargetChange: (String) -> Unit,
    onTcpHostChange: (String) -> Unit,
    onTcpPortChange: (String) -> Unit,
    onTraceTargetChange: (String) -> Unit,
    onDnsTargetChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRefreshJob: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = state.selectedTab.title, style = MaterialTheme.typography.titleLarge)

        when (state.selectedTab) {
            CheckTab.HTTP -> {
                OutlinedTextField(
                    value = state.httpTarget,
                    onValueChange = onHttpTargetChange,
                    label = { Text("URL сайта") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CheckTab.PING -> {
                OutlinedTextField(
                    value = state.pingTarget,
                    onValueChange = onPingTargetChange,
                    label = { Text("Сайт или IP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CheckTab.TCP -> {
                OutlinedTextField(
                    value = state.tcpHost,
                    onValueChange = onTcpHostChange,
                    label = { Text("Хост") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.tcpPort,
                    onValueChange = onTcpPortChange,
                    label = { Text("Порт") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CheckTab.TRACE -> {
                OutlinedTextField(
                    value = state.traceTarget,
                    onValueChange = onTraceTargetChange,
                    label = { Text("Сайт или IP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            CheckTab.DNS -> {
                OutlinedTextField(
                    value = state.dnsTarget,
                    onValueChange = onDnsTargetChange,
                    label = { Text("Домен") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onSubmit, enabled = !state.loading) {
                Text("Отправить на сервер")
            }
        }

        ResultCard(
            loading = state.loading,
            result = state.result,
            onRefreshJob = onRefreshJob
        )
    }
}

@Composable
private fun ResultCard(
    loading: Boolean,
    result: ServerCheckResult?,
    onRefreshJob: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            result?.let { res ->
                Text("Код ответа: ${res.statusCode ?: "нет"}")
                res.jobId?.let { Text("ID задачи: $it") }

                val pingJob = parsePingJob(res.body)
                val metricGroups = parseMetricGroups(res.body)
                when {
                    pingJob != null -> PingResultSection(pingJob)
                    metricGroups.isNotEmpty() -> MetricsSection(metricGroups)
                    else -> {
                        res.body?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                maxLines = 10,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                res.jobId?.let { jobId ->
                    Button(onClick = { onRefreshJob(jobId) }, enabled = !loading) {
                        Text("Обновить статус задачи")
                    }
                }

                res.error?.let { Text("Ошибка: $it", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
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

@Composable
private fun LabeledRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatLatency(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format("%.2f", value)
    }
}
