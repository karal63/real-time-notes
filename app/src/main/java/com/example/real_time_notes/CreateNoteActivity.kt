package com.example.real_time_notes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class CreateNoteActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NOTE_TITLE = "com.example.real_time_notes.NOTE_TITLE"
        const val EXTRA_NOTE_DESCRIPTION = "com.example.real_time_notes.NOTE_DESCRIPTION"
        const val EXTRA_NOTE_ALLOW_OTHERS = "com.example.real_time_notes.NOTE_ALLOW_OTHERS"
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
        val allowOthersField = findViewById<Switch>(R.id.allow_others_switch)
        val createButton = findViewById<Button>(R.id.create_note_button)

        createButton.setOnClickListener {
            val title = titleField.text.toString().trim()

            if (title.isEmpty()) {
                titleField.error = getString(R.string.note_name_required)
                return@setOnClickListener
            }

            val description = descriptionField.text.toString().trim()
            val allowOthers = allowOthersField.isChecked
            val note = hashMapOf(
                "title" to title,
                "description" to description,
                "allowOthers" to allowOthers,
                "createdAt" to FieldValue.serverTimestamp()
            )

            createButton.isEnabled = false
            FirebaseFirestore.getInstance().collection("notes").add(note)
                .addOnSuccessListener {
                    val result = Intent().apply {
                        putExtra(EXTRA_NOTE_TITLE, title)
                        putExtra(EXTRA_NOTE_DESCRIPTION, description)
                        putExtra(EXTRA_NOTE_ALLOW_OTHERS, allowOthers)
                    }
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
                .addOnFailureListener { error ->
                    createButton.isEnabled = true
                    Log.e("CreateNoteActivity", "Failed to save note", error)
                    val message = when ((error as? FirebaseFirestoreException)?.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> R.string.note_save_permission_denied
                        FirebaseFirestoreException.Code.UNAVAILABLE -> R.string.note_save_unavailable
                        else -> R.string.note_save_failed
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
        }
    }
}
