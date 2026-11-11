package com.example.real_time_notes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateNoteActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NOTE_TITLE = "com.example.real_time_notes.NOTE_TITLE"
        const val EXTRA_NOTE_DESCRIPTION = "com.example.real_time_notes.NOTE_DESCRIPTION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_note)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val titleField = findViewById<EditText>(R.id.note_name_input)
        val descriptionField = findViewById<EditText>(R.id.note_description_input)
        findViewById<Button>(R.id.create_note_button).setOnClickListener {
            val title = titleField.text.toString().trim()
            if (title.isEmpty()) {
                titleField.error = getString(R.string.note_name_required)
                return@setOnClickListener
            }

            val result = Intent().apply {
                putExtra(EXTRA_NOTE_TITLE, title)
                putExtra(EXTRA_NOTE_DESCRIPTION, descriptionField.text.toString().trim())
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}
