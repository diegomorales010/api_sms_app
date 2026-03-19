package com.example.smsapinano

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_requests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = RequestAdapter(RequestLog.getAll())
        recyclerView.adapter = adapter
    }
}