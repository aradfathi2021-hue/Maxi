package com.maxivpn.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ConfigStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("maxi_configs", Context.MODE_PRIVATE)

    fun load(): MutableList<VpnConfig> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<VpnConfig>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += VpnConfig(
                o.optString("name"),
                o.optString("uri"),
                o.optString("xrayJson").ifBlank { null }
            )
        }
        return out
    }

    fun save(items: List<VpnConfig>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(JSONObject().apply {
                put("name", it.name)
                put("uri", it.uri)
                if (it.xrayJson != null) put("xrayJson", it.xrayJson)
            })
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }
}
