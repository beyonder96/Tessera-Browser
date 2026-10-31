package com.tessera.browser.sync

import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

data class DesktopDeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int,
    val isOnline: Boolean = false,
    val lastSeenTimestamp: Long = 0L
)

data class RemoteDesktopTab(
    val id: String,
    val title: String,
    val url: String,
    val spaceId: String? = null,
    val isPinned: Boolean = false
)

class TesseraDesktopSyncManager(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("tessera_sync_prefs", Context.MODE_PRIVATE)

    private val _desktopDevice = MutableStateFlow<DesktopDeviceInfo?>(null)
    val desktopDevice: StateFlow<DesktopDeviceInfo?> = _desktopDevice.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    init {
        loadSavedDevice()
        startDiscoveryListener()
    }

    private fun loadSavedDevice() {
        val ip = prefs.getString("saved_desktop_ip", null)
        val port = prefs.getInt("saved_desktop_port", 41828)
        val name = prefs.getString("saved_desktop_name", "Tessera Windows") ?: "Tessera Windows"
        val id = prefs.getString("saved_desktop_id", "") ?: ""

        if (!ip.isNullOrBlank()) {
            val device = DesktopDeviceInfo(
                deviceId = id,
                deviceName = name,
                ipAddress = ip,
                port = port,
                isOnline = false
            )
            _desktopDevice.value = device
            scope.launch { checkDesktopStatus() }
        }
    }

    private fun saveDevice(device: DesktopDeviceInfo) {
        prefs.edit()
            .putString("saved_desktop_ip", device.ipAddress)
            .putInt("saved_desktop_port", device.port)
            .putString("saved_desktop_name", device.deviceName)
            .putString("saved_desktop_id", device.deviceId)
            .apply()
    }

    suspend fun checkDesktopStatus(): Boolean = withContext(Dispatchers.IO) {
        val target = _desktopDevice.value ?: return@withContext false
        try {
            val url = URL("http://${target.ipAddress}:${target.port}/api/v2/info")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 2500
                readTimeout = 2500
                requestMethod = "GET"
            }
            val ok = conn.responseCode in 200..299
            if (ok) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(resp)
                val serverName = json.optString("deviceName", target.deviceName)
                val updated = target.copy(
                    isOnline = true,
                    deviceName = serverName,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
                _desktopDevice.value = updated
                saveDevice(updated)
            } else {
                _desktopDevice.value = target.copy(isOnline = false)
            }
            ok
        } catch (e: Exception) {
            _desktopDevice.value = target.copy(isOnline = false)
            false
        }
    }

    suspend fun connectByIpOrBridgeUrl(input: String): Result<DesktopDeviceInfo> = withContext(Dispatchers.IO) {
        try {
            val clean = input.trim()
            var ip = clean
            var port = 41828

            if (clean.startsWith("http://") || clean.startsWith("https://")) {
                val parsed = URL(clean)
                ip = parsed.host
                if (clean.contains("syncPort=")) {
                    val sp = clean.substringAfter("syncPort=").substringBefore("&").toIntOrNull()
                    if (sp != null && sp > 0) port = sp
                } else if (parsed.port > 0) {
                    port = parsed.port
                }
            } else if (clean.contains(":")) {
                val parts = clean.split(":")
                ip = parts[0].trim()
                port = parts.getOrNull(1)?.toIntOrNull() ?: 41828
            }

            val testUrl = URL("http://$ip:$port/api/v2/info")
            val conn = (testUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }

            if (conn.responseCode in 200..299) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(resp)
                val deviceId = json.optString("deviceId", "win_pc")
                val deviceName = json.optString("deviceName", "Tessera Windows")

                val detected = DesktopDeviceInfo(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    ipAddress = ip,
                    port = port,
                    isOnline = true,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
                _desktopDevice.value = detected
                saveDevice(detected)
                Result.success(detected)
            } else {
                Result.failure(Exception("Servidor respondeu com código ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun startDiscoveryListener() {
        if (_isDiscovering.value) return
        _isDiscovering.value = true

        scope.launch {
            var socket: DatagramSocket? = null
            var lock: WifiManager.MulticastLock? = null
            try {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                lock = try {
                    wm?.createMulticastLock("tessera_sync_udp")?.apply {
                        setReferenceCounted(true)
                        acquire()
                    }
                } catch (e: Exception) { null }

                socket = DatagramSocket(41820).apply {
                    broadcast = true
                    soTimeout = 12000
                }
                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)

                while (_isDiscovering.value) {
                    try {
                        socket.receive(packet)
                        val text = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
                        val senderIp = packet.address.hostAddress ?: continue

                        val json = JSONObject(text)
                        val deviceId = json.optString("deviceId", "win_pc")
                        val deviceName = json.optString("deviceName", "PC Windows")
                        val port = json.optInt("port", 41828)

                        val detected = DesktopDeviceInfo(
                            deviceId = deviceId,
                            deviceName = deviceName,
                            ipAddress = senderIp,
                            port = port,
                            isOnline = true,
                            lastSeenTimestamp = System.currentTimeMillis()
                        )

                        _desktopDevice.value = detected
                        saveDevice(detected)
                    } catch (e: Exception) {
                        // Socket receive timeout or intermittent error, continue listening
                    }
                }
            } catch (e: Exception) {
                Log.w("TesseraSync", "UDP discovery listener note: ${e.message}")
            } finally {
                socket?.close()
                try {
                    if (lock?.isHeld == true) lock.release()
                } catch (e: Exception) {}
                _isDiscovering.value = false
            }
        }
    }

    suspend fun sendDiscoveryQuery(): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = DatagramSocket().apply { broadcast = true }
            val query = "TESSERA_DISCOVERY_QUERY".toByteArray(Charsets.UTF_8)
            val packet = DatagramPacket(query, query.size, InetAddress.getByName("255.255.255.255"), 41820)
            socket.send(packet)
            socket.close()
            checkDesktopStatus()
            true
        } catch (e: Exception) {
            checkDesktopStatus()
            false
        }
    }

    suspend fun fetchDesktopTabs(): List<RemoteDesktopTab> = withContext(Dispatchers.IO) {
        val target = _desktopDevice.value ?: return@withContext emptyList()
        try {
            val url = URL("http://${target.ipAddress}:${target.port}/api/v2/tabs")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }
            if (conn.responseCode in 200..299) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(resp)
                val arr = json.optJSONArray("tabs") ?: return@withContext emptyList()
                val list = mutableListOf<RemoteDesktopTab>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        RemoteDesktopTab(
                            id = obj.optString("id", i.toString()),
                            title = obj.optString("title", "Sem título"),
                            url = obj.optString("url", ""),
                            spaceId = obj.optString("spaceId", "").ifBlank { null },
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun pollMobileInbox(): List<RemoteDesktopTab> = withContext(Dispatchers.IO) {
        val target = _desktopDevice.value ?: return@withContext emptyList()
        if (!target.isOnline) return@withContext emptyList()
        try {
            val url = URL("http://${target.ipAddress}:${target.port}/api/v2/mobile-inbox")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 2500
                readTimeout = 2500
                requestMethod = "GET"
            }
            if (conn.responseCode in 200..299) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(resp)
                val arr = json.optJSONArray("tabs") ?: return@withContext emptyList()
                val list = mutableListOf<RemoteDesktopTab>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        RemoteDesktopTab(
                            id = obj.optString("id", i.toString()),
                            title = obj.optString("title", "Sem título"),
                            url = obj.optString("url", ""),
                            spaceId = obj.optString("spaceId", "").ifBlank { null }
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun pairWithDesktop(ipAddress: String, port: Int, pairingSecret: String): Result<DesktopDeviceInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$ipAddress:$port/api/v2/pair")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 5000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            val reqBody = JSONObject().apply {
                put("deviceId", "android_${Build.MANUFACTURER}_${Build.MODEL}".replace(" ", "_"))
                put("deviceName", "${Build.MODEL} (Tessera Android)")
                put("platform", "android")
                put("pairingSecret", pairingSecret.trim())
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(reqBody) }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val respText = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                val respJson = JSONObject(respText)

                val paired = DesktopDeviceInfo(
                    deviceId = respJson.optString("serverDeviceId", "win_pc"),
                    deviceName = respJson.optString("serverDeviceName", "Tessera Windows"),
                    ipAddress = ipAddress,
                    port = port,
                    isOnline = true,
                    lastSeenTimestamp = System.currentTimeMillis()
                )

                _desktopDevice.value = paired
                saveDevice(paired)
                Result.success(paired)
            } else {
                Result.failure(Exception("Falha de pareamento: Código HTTP $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendTabToDesktop(tabUrl: String, tabTitle: String): Boolean = withContext(Dispatchers.IO) {
        val target = _desktopDevice.value ?: return@withContext false
        try {
            val url = URL("http://${target.ipAddress}:${target.port}/api/v2/send-tab")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 4000
                readTimeout = 4000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            val payload = JSONObject().apply {
                put("url", tabUrl)
                put("title", tabTitle)
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            Log.e("TesseraSync", "Falha ao enviar aba para o PC", e)
            false
        }
    }

    suspend fun syncWithDesktop(backupJsonPayload: String): Result<String> = withContext(Dispatchers.IO) {
        val target = _desktopDevice.value ?: return@withContext Result.failure(Exception("Nenhum PC conectado"))
        try {
            val url = URL("http://${target.ipAddress}:${target.port}/api/v2/sync/delta")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 12000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(backupJsonPayload) }

            if (conn.responseCode in 200..299) {
                val respText = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                Result.success(respText)
            } else {
                Result.failure(Exception("Erro HTTP ${conn.responseCode} ao sincronizar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
