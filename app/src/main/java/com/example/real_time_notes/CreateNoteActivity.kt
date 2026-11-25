package com.example.real_time_notes

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class CreateNoteActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_note)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        findViewById<Button>(R.id.create_note_back_button).setOnClickListener {
            finish()
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
            val ownerId = auth.currentUser?.uid

            val note = hashMapOf(
                "title" to title,
                "description" to description,
                "allowOthers" to allowOthers,
                "ownerId" to ownerId,
                "createdAt" to FieldValue.serverTimestamp()
            )

            createButton.isEnabled = false
            db.collection("notes").add(note)
                .addOnSuccessListener { document ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener

                    startActivity(NoteActivity.createIntent(this, document.id, title))
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
