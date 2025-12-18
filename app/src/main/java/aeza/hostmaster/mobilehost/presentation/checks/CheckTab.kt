package aeza.hostmaster.mobilehost.presentation.checks

import aeza.hostmaster.mobilehost.domain.model.CheckType

enum class CheckTab(val title: String, val type: CheckType) {
    HTTP("HTTP/HTTPS", CheckType.HTTP),
    PING("PING", CheckType.PING),
    TCP("TCP", CheckType.TCP),
    TRACE("Traceroute", CheckType.TRACEROUTE),
    DNS("DNS Lookup", CheckType.DNS_LOOKUP);
}
