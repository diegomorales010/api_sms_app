package com.example.smsapinano  // Asegúrate que coincida con tu paquete

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvIp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvIp = findViewById(R.id.tv_ip_address)
        updateIpAddress()

        findViewById<Button>(R.id.btn_start)?.setOnClickListener {
            startService(Intent(this, SmsApiService::class.java))
            Toast.makeText(this, "Servicio iniciado", Toast.LENGTH_SHORT).show()
            updateIpAddress() // refrescar IP al iniciar
        }

        findViewById<Button>(R.id.btn_stop)?.setOnClickListener {
            stopService(Intent(this, SmsApiService::class.java))
            Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show()
        }

        // Botón para ver historial
        findViewById<Button>(R.id.btn_view_history)?.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<Button>(R.id.btn_config_ips)?.setOnClickListener {
            startActivity(Intent(this, IpConfigActivity::class.java))
        }
    }

    private fun updateIpAddress() {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            tvIp.text = "IP actual: $ipAddress:8080"
        } catch (e: Exception) {
            tvIp.text = "No se pudo obtener IP (WiFi activado?)"
        }
    }
}