package com.example.real_time_notes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<Button>(R.id.guest_button).setOnClickListener {
            // This guest name lasts only for the current visit.
            val guestName = getString(R.string.guest_name, Random.nextInt(1000, 10000))
            val intent = Intent(this, SecondActivity::class.java).apply {
                putExtra(SecondActivity.EXTRA_GUEST_NAME, guestName)
            }
            startActivity(intent)
        }

        val comingLater = { _: android.view.View ->
            Toast.makeText(this, R.string.coming_later, Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.login_button).setOnClickListener(comingLater)
        findViewById<Button>(R.id.create_account_button).setOnClickListener(comingLater)
    }
}
