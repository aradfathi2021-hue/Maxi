package com.maxivpn.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import java.util.concurrent.Executors\nimport org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var store: ConfigStore
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private var configs = mutableListOf<VpnConfig>()
    private var selectedIndex = -1

    private val vpnPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK && selectedIndex >= 0) {
            val cfg = configs[selectedIndex]
            startService(Intent(this, MaxiVpnService::class.java).putExtra("xrayJson", cfg.xrayJson))
            status.text = "وضعیت: در حال اتصال"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ConfigStore(this)
        configs = store.load()
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 24)
            setBackgroundColor(0xFF0B0D12.toInt())
        }

        val title = TextView(this).apply {
            text = "MAXI VPN"
            textSize = 30f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 8)
        }
        root.addView(title)

        status = TextView(this).apply {
            text = "وضعیت: قطع"
            textSize = 17f
            setTextColor(0xFFB9B9C7.toInt())
        }
        root.addView(status)

        val connect = Button(this).apply {
            text = "اتصال / قطع اتصال"
            setOnClickListener { toggleVpn() }
        }
        root.addView(connect)

        val add = Button(this).apply {
            text = "＋ افزودن کانفیگ"
            setOnClickListener { showAddDialog() }
        }
        root.addView(add)

        val sub = Button(this).apply {
            text = "＋ افزودن لینک اشتراک"
            setOnClickListener { showSubscriptionDialog() }
        }
        root.addView(sub)

        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list)

        setContentView(root)
        render()
    }

    private fun render() {
        list.removeAllViews()
        configs.forEachIndexed { index, cfg ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)
            }
            val name = TextView(this).apply {
                text = cfg.name
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            }
            row.addView(name)
            val use = Button(this).apply {
                text = "انتخاب"
                setOnClickListener {
                    status.text = "سرور انتخاب‌شده: ${cfg.name}"
                    selectedIndex = index
                }
            }
            row.addView(use)
            val del = Button(this).apply {
                text = "حذف"
                setOnClickListener {
                    configs.removeAt(index); store.save(configs); render()
                }
            }
            row.addView(del)
            list.addView(row)
        }
    }

    private fun showAddDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(30,10,30,0) }
        val name = EditText(this).apply { hint = "نام سرور" }
        val uri = EditText(this).apply { hint = "vless://..." }
        box.addView(name); box.addView(uri)
        AlertDialog.Builder(this).setTitle("افزودن کانفیگ").setView(box)
            .setPositiveButton("ذخیره") { _, _ ->
                if (uri.text.isNotBlank()) {
                    val raw = uri.text.toString()
                    val displayName = name.text.toString().ifBlank { "Server ${configs.size+1}" }
                    configs.add(VpnConfig(displayName, raw))
                    store.save(configs); render()
                    Executors.newSingleThreadExecutor().execute {
                        try {
                            val result = XrayCore.convertShareLinks(raw)
                            val json = JSONObject(result)
                            val data = json.optJSONObject("data")
                            val xray = data?.optString("xrayJson")
                            if (!xray.isNullOrBlank()) {
                                runOnUiThread {
                                    val idx = configs.lastIndex
                                    configs[idx] = configs[idx].copy(xrayJson = xray)
                                    store.save(configs)
                                    Toast.makeText(this, "کانفیگ توسط Xray آماده شد", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }.setNegativeButton("لغو", null).show()
    }

    private fun showSubscriptionDialog() {
        val input = EditText(this).apply { hint = "https://example.com/sub"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("لینک اشتراک").setView(input)
            .setPositiveButton("دریافت") { _, _ ->
                val url = input.text.toString()
                Executors.newSingleThreadExecutor().execute {
                    try {
                        val uris = SubscriptionParser.fetch(url)
                        runOnUiThread {
                            uris.forEachIndexed { i, u ->
                                try {
                                    val result = XrayCore.convertShareLinks(u)
                                    val x = JSONObject(result).optJSONObject("data")?.optString("xrayJson")
                                    configs.add(VpnConfig("Subscription ${configs.size+i+1}", u, x))
                                } catch (_: Exception) {
                                    configs.add(VpnConfig("Subscription ${configs.size+i+1}", u))
                                }
                            }
                            store.save(configs); render()
                            Toast.makeText(this, "${uris.size} کانفیگ اضافه شد", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread { Toast.makeText(this, "خطا در دریافت اشتراک", Toast.LENGTH_LONG).show() }
                    }
                }
            }.setNegativeButton("لغو", null).show()
    }

    private fun toggleVpn() {
        if (selectedIndex < 0 || selectedIndex >= configs.size) {
            Toast.makeText(this, "اول یک سرور را انتخاب کن", Toast.LENGTH_SHORT).show()
            return
        }
        val cfg = configs[selectedIndex]
        val xrayJson = cfg.xrayJson
        if (xrayJson.isNullOrBlank()) {
            Toast.makeText(this, "این کانفیگ هنوز به Xray JSON تبدیل نشده", Toast.LENGTH_LONG).show()
            return
        }
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermission.launch(intent)
        } else {
            val svc = Intent(this, MaxiVpnService::class.java)
                .putExtra("xrayJson", xrayJson)
            startService(svc)
            status.text = "وضعیت: در حال اتصال"
        }
    }
}
