package com.example.real_time_notes

import android.content.Intent
import android.app.ActivityOptions
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mainButton = findViewById<Button>(R.id.main_button)

        mainButton.setOnClickListener {
            val options = ActivityOptions.makeSceneTransitionAnimation(this)
            startActivity(Intent(this, SecondActivity::class.java), options.toBundle())
        }
    }
}
