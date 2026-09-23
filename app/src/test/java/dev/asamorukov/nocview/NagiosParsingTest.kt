package dev.asamorukov.nocview

import dev.asamorukov.nocview.data.remote.ApiProvider
import dev.asamorukov.nocview.data.remote.dto.CountData
import dev.asamorukov.nocview.data.remote.dto.Envelope
import dev.asamorukov.nocview.data.remote.dto.HostData
import dev.asamorukov.nocview.data.remote.dto.HostListData
import dev.asamorukov.nocview.data.remote.dto.ProgramStatusData
import dev.asamorukov.nocview.data.remote.dto.ServiceListData
import dev.asamorukov.nocview.data.remote.toDomain
import dev.asamorukov.nocview.data.remote.toHostCounts
import dev.asamorukov.nocview.data.remote.toServiceCounts
import dev.asamorukov.nocview.domain.model.HostStatus
import dev.asamorukov.nocview.domain.model.ServiceStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NagiosParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) {
            "Missing fixture $name"
        }.bufferedReader().use { it.readText() }

    @Test
    fun `host count parses`() {
        val envelope = json.decodeFromString<Envelope<CountData>>(fixture("hostcount.json"))
        assertEquals(0, envelope.result.typeCode)
        val counts = envelope.data.toHostCounts()
        assertEquals(19, counts.total)
        assertEquals(19, counts.up)
        assertEquals(0, counts.problems)
    }

    @Test
    fun `service count parses`() {
        val envelope = json.decodeFromString<Envelope<CountData>>(fixture("servicecount.json"))
        val counts = envelope.data.toServiceCounts()
        assertEquals(55, counts.total)
        assertEquals(55, counts.ok)
        assertEquals(0, counts.problems)
    }

    @Test
    fun `host list parses and maps to domain`() {
        val envelope = json.decodeFromString<Envelope<HostListData>>(fixture("hostlist.json"))
        val hosts = envelope.data.hostlist.map { (name, dto) -> dto.toDomain(name) }
        assertEquals(19, hosts.size)
        val host18 = hosts.first { it.name == "host18" }
        assertEquals(HostStatus.UP, host18.status)
        assertTrue(host18.pluginOutput.startsWith("PING OK"))
        assertTrue(host18.lastCheck != null && host18.lastCheck!! > 0)
        assertEquals("hard", host18.stateType)
    }

    @Test
    fun `service list parses and maps to domain`() {
        val envelope = json.decodeFromString<Envelope<ServiceListData>>(fixture("servicelist.json"))
        val services = envelope.data.servicelist.flatMap { (host, svcs) ->
            svcs.map { (description, dto) -> dto.toDomain(host, description) }
        }
        assertEquals(55, services.size)
        assertTrue(services.all { it.status == ServiceStatus.OK })
        assertTrue(services.any { it.hostName == "host18" && it.description == "Current Load" })
    }

    @Test
    fun `single host parses`() {
        val envelope = json.decodeFromString<Envelope<HostData>>(fixture("host18.json"))
        val host = envelope.data.host.toDomain()
        assertEquals("host18", host.name)
        assertEquals(HostStatus.UP, host.status)
    }

    @Test
    fun `program status parses`() {
        val envelope =
            json.decodeFromString<Envelope<ProgramStatusData>>(fixture("programstatus.json"))
        assertEquals("4.5.12", envelope.data.programstatus.version)
    }

    @Test
    fun `status string mapping is robust`() {
        assertEquals(HostStatus.UP, HostStatus.fromApi("up"))
        assertEquals(HostStatus.UNREACHABLE, HostStatus.fromApi("UNREACHABLE"))
        assertEquals(HostStatus.UNKNOWN, HostStatus.fromApi("garbage"))
        assertEquals(HostStatus.UNKNOWN, HostStatus.fromApi(null))
        assertEquals(ServiceStatus.CRITICAL, ServiceStatus.fromApi("critical"))
        assertEquals(ServiceStatus.PENDING, ServiceStatus.fromApi("pending"))
        assertEquals(ServiceStatus.UNKNOWN, ServiceStatus.fromApi("nonsense"))
    }

    @Test
    fun `base url normalization`() {
        assertEquals("http://host/nagios/", ApiProvider.normalizeBaseUrl("http://host/nagios"))
        assertEquals("https://host/", ApiProvider.normalizeBaseUrl("https://host"))
        assertEquals("http://host/", ApiProvider.normalizeBaseUrl("host"))
        assertEquals("http://localhost/", ApiProvider.normalizeBaseUrl("   "))
    }
}
