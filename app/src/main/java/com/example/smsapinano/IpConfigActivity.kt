package com.example.smsapinano

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IpConfigActivity : AppCompatActivity() {

    private lateinit var etNewIp: EditText
    private lateinit var btnAddIp: Button
    private lateinit var rvAllowedIps: RecyclerView
    private lateinit var tvCurrentIps: TextView

    private val adapter = AllowedIpAdapter(mutableListOf()) { ip ->
        removeIp(ip)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ip_config)

        etNewIp = findViewById(R.id.et_new_ip)
        btnAddIp = findViewById(R.id.btn_add_ip)
        rvAllowedIps = findViewById(R.id.rv_allowed_ips)
        tvCurrentIps = findViewById(R.id.tv_current_ips)

        rvAllowedIps.layoutManager = LinearLayoutManager(this)
        rvAllowedIps.adapter = adapter

        loadAllowedIps()

        btnAddIp.setOnClickListener {
            val newIp = etNewIp.text.toString().trim()
            if (newIp.isNotEmpty()) {
                if (isValidIp(newIp)) {
                    addIp(newIp)
                    etNewIp.text.clear()
                    Toast.makeText(this, "IP agregada: $newIp", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Formato de IP inválido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Escribe una IP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAllowedIps() {
        val prefs = getSharedPreferences("IpConfig", Context.MODE_PRIVATE)
        val savedIps = prefs.getStringSet("allowed_ips", setOf("127.0.0.1", "::1")) ?: setOf()
        adapter.updateList(savedIps.toMutableList())
        updateSummary()
    }

    private fun addIp(ip: String) {
        val prefs = getSharedPreferences("IpConfig", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("allowed_ips", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        current.add(ip)
        prefs.edit().putStringSet("allowed_ips", current).apply()
        adapter.addIp(ip)
        updateSummary()
    }

    private fun removeIp(ip: String) {
        val prefs = getSharedPreferences("IpConfig", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("allowed_ips", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        current.remove(ip)
        prefs.edit().putStringSet("allowed_ips", current).apply()
        adapter.removeIp(ip)
        updateSummary()
        Toast.makeText(this, "IP eliminada: $ip", Toast.LENGTH_SHORT).show()
    }

    private fun updateSummary() {
        tvCurrentIps.text = "IPs permitidas (${adapter.itemCount}):"
    }

    private fun isValidIp(ip: String): Boolean {
        // Validación básica IPv4 + localhost
        return ip.matches(Regex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) ||
                ip == "127.0.0.1" || ip == "::1"
    }
}