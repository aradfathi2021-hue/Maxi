package com.maxivpn.app

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.IBinder

class MaxiVpnService : VpnService() {
    private var tun: android.os.ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = Notification.Builder(this, "maxi")
            .setContentTitle("Maxi VPN")
            .setContentText("Maxi VPN is running")
            .setSmallIcon(com.maxivpn.app.R.drawable.ic_maxi_vpn)
            .build()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("maxi", "Maxi VPN", NotificationManager.IMPORTANCE_LOW)
        )
        startForeground(1001, notification)

        // The selected profile's Xray JSON is passed through the intent.
        val json = intent?.getStringExtra("xrayJson")
        try {
            tun?.close()
            tun = Builder()
                .setSession("Maxi VPN")
                .addAddress("10.10.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .setMtu(1500)
                .establish()

            if (!json.isNullOrBlank()) {
                XrayCore.startFromFile(this, json)
            }
        } catch (_: Exception) {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try { XrayCore.stop() } catch (_: Exception) {}
        tun?.close()
        tun = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)
}
