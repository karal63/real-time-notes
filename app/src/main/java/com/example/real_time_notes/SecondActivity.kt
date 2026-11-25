package com.example.real_time_notes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SecondActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var notesListener: ListenerRegistration? = null

    companion object {
        const val EXTRA_GUEST_NAME = "com.example.real_time_notes.GUEST_NAME"
    }

    private val notes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableNotesEdgeToEdge()
        setContentView(R.layout.activity_second)
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val guestName = intent.getStringExtra(EXTRA_GUEST_NAME) ?: getString(R.string.default_guest_name)
        findViewById<TextView>(R.id.welcome_text).text = getString(R.string.welcome_guest, guestName)

        findViewById<FloatingActionButton>(R.id.add_note_button).setOnClickListener {
            val intent = Intent(this, CreateNoteActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.join_button).setOnClickListener {
            showJoinDialog()
        }
    }

    private fun showJoinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_join_note, null)
        val codeField = dialogView.findViewById<TextInputEditText>(R.id.note_code_input)
        val codeLayout = dialogView.findViewById<TextInputLayout>(R.id.note_code_layout)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.join_note_title)
            .setMessage(R.string.join_note_message)
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.join, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val code = codeField.text?.toString()?.trim().orEmpty()
                if (code.isEmpty()) {
                    codeLayout.error = getString(R.string.note_code_required)
                } else {
                    dialog.dismiss()
                    Toast.makeText(this, R.string.coming_later, Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    override fun onStart() {
        super.onStart()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, R.string.sign_in_required, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        notesListener = db.collection("notes")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SecondActivity", "Failed to load notes", error)
                    Toast.makeText(this, R.string.notes_load_failed, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                notes.clear()
                notes.addAll(snapshot.documents.map { document ->
                    Note(
                        id = document.id,
                        title = document.getString("title").orEmpty(),
                        description = document.getString("description").orEmpty(),
                        allowOthers = document.getBoolean("allowOthers") ?: false,
                        ownerId = document.getString("ownerId").orEmpty()
                    )
                })
                showNotes()
            }
    }

    override fun onStop() {
        notesListener?.remove()
        notesListener = null
        super.onStop()
    }

    private fun showNotes() {
        val container = findViewById<LinearLayout>(R.id.notes_container)
        container.removeAllViews()

        for (note in notes) {
            val card = layoutInflater.inflate(R.layout.item_note, container, false)
            card.findViewById<TextView>(R.id.note_title).text = note.title
            card.findViewById<TextView>(R.id.note_description).apply {
                text = note.description
                visibility = if (note.description.isBlank()) View.GONE else View.VISIBLE
            }
            container.addView(card)

            card.setOnClickListener {
                startActivity(NoteActivity.createIntent(this, note.id, note.title))
            }
        }
    }
}
