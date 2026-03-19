package com.example.smsapinano  // ajusta si tu paquete es diferente

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RequestAdapter(private val requests: List<SmsRequest>) :
    RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    // Clase interna para el ViewHolder
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvIp: TextView = view.findViewById(R.id.tv_ip)
        val tvTo: TextView = view.findViewById(R.id.tv_to)
        val tvText: TextView = view.findViewById(R.id.tv_text)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
    }

    // Crear nueva vista (inflar el layout de item)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return ViewHolder(view)
    }

    // Vincular datos a la vista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = requests[position]

        holder.tvTime.text = req.formattedTime
        holder.tvIp.text = "IP: ${req.ip}"
        holder.tvTo.text = "Para: ${req.to}"
        holder.tvText.text = req.text
        holder.tvStatus.text = if (req.success) "ÉXITO" else "FALLÓ: ${req.errorMessage ?: "Desconocido"}"

        // Colores para éxito/fallo
        holder.tvStatus.setTextColor(
            if (req.success) Color.GREEN else Color.RED
        )
    }

    // Cantidad de items
    override fun getItemCount(): Int = requests.size
}