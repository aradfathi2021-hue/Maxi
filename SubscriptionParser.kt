package com.maxivpn.app

import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL

object SubscriptionParser {
    fun decodeText(text: String): List<String> {
        val cleaned = text.trim()
        val decoded = try {
            String(Base64.decode(cleaned, Base64.DEFAULT))
        } catch (_: Exception) { cleaned }
        return decoded.lines().map { it.trim() }
            .filter { it.startsWith("vless://") || it.startsWith("vmess://") ||
                      it.startsWith("trojan://") || it.startsWith("ss://") }
    }

    fun fetch(urlString: String): List<String> {
        val c = URL(urlString).openConnection() as HttpURLConnection
        c.connectTimeout = 10000
        c.readTimeout = 15000
        c.requestMethod = "GET"
        return try {
            decodeText(c.inputStream.bufferedReader().use { it.readText() })
        } finally { c.disconnect() }
    }
}
