package com.maxivpn.app

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Thin adapter around the official XTLS/libXray Android AAR.
 *
 * The AAR is fetched by GitHub Actions from the official XTLS/libXray release.
 * Reflection keeps this source compatible with the generated gomobile package
 * name used by different libXray builds.
 */
object XrayCore {
    private val candidates = listOf(
        "libXray.LibXray",
        "libxray.LibXray",
        "com.github.xtls.libxray.LibXray"
    )

    private fun invoke(request: String): String {
        var last: Throwable? = null
        for (name in candidates) {
            try {
                val c = Class.forName(name)
                val m = c.methods.firstOrNull {
                    it.name == "invoke" && it.parameterTypes.size == 1 &&
                    it.parameterTypes[0] == String::class.java
                } ?: continue
                return m.invoke(null, request) as String
            } catch (t: Throwable) { last = t }
        }
        throw IllegalStateException("libXray AAR/API was not found", last)
    }

    fun convertShareLinks(text: String): String {
        val req = JSONObject()
            .put("apiVersion", 1)
            .put("method", "convertShareLinksToXrayJson")
            .put("payload", JSONObject().put("text", text))
        return invoke(req.toString())
    }

    fun test(configJson: String): String {
        val req = JSONObject()
            .put("apiVersion", 1)
            .put("method", "testXray")
            .put("payload", JSONObject().put("xrayJson", configJson))
        return invoke(req.toString())
    }

    fun startFromFile(context: Context, configJson: String): String {
        val file = File(context.filesDir, "xray-config.json")
        file.writeText(configJson)
        val req = JSONObject()
            .put("apiVersion", 1)
            .put("method", "runXray")
            .put("payload", JSONObject().put("configPath", file.absolutePath))
        return invoke(req.toString())
    }

    fun stop(): String {
        val req = JSONObject()
            .put("apiVersion", 1)
            .put("method", "stopXray")
            .put("payload", JSONObject())
        return invoke(req.toString())
    }
}
