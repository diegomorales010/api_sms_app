package com.example.smsapinano

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsRequest(
    val timestamp: Long = System.currentTimeMillis(),
    val ip: String,
    val to: String,
    val text: String,
    val success: Boolean,
    val errorMessage: String? = null
) {
    val formattedTime: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

object RequestLog {
    private val requests = mutableListOf<SmsRequest>()

    fun add(request: SmsRequest) {
        requests.add(0, request) // lo más reciente arriba

        // Limitar a 200 elementos (borra el más antiguo)
        if (requests.size > 200) {
            requests.removeAt(requests.lastIndex)
        }
    }

    fun getAll(): List<SmsRequest> = requests.toList()

    // Opcional: método para limpiar todo
    fun clear() {
        requests.clear()
    }
}