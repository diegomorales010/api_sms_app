package com.example.smsapinano

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AllowedIpAdapter(
    private var ips: MutableList<String>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<AllowedIpAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIp: TextView = view.findViewById(R.id.tv_ip)
        val btnDelete: Button = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allowed_ip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ip = ips[position]
        holder.tvIp.text = ip
        holder.btnDelete.setOnClickListener { onDelete(ip) }
    }

    override fun getItemCount(): Int = ips.size

    fun addIp(ip: String) {
        if (!ips.contains(ip)) {
            ips.add(0, ip)
            notifyItemInserted(0)
        }
    }

    fun removeIp(ip: String) {
        val index = ips.indexOf(ip)
        if (index >= 0) {
            ips.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateList(newList: List<String>) {
        ips.clear()
        ips.addAll(newList)
        notifyDataSetChanged()
    }
}