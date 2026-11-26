package com.example.real_time_notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class NoteActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_NOTE_ID = "noteId"
        private const val EXTRA_NOTE_TITLE = "noteTitle"
        private const val BODY_TEXT_FIELD = "editorBody.text"
        private const val AUTOSAVE_DELAY_MS = 750L

        fun createIntent(context: Context, noteId: String, title: String): Intent =
            Intent(context, NoteActivity::class.java)
                .putExtra(EXTRA_NOTE_ID, noteId)
                .putExtra(EXTRA_NOTE_TITLE, title)
    }

    private lateinit var document: DocumentReference
    private lateinit var editor: EditText
    private lateinit var titleView: TextView
    private lateinit var shareButton: MaterialButton
    private lateinit var loading: LinearProgressIndicator
    private val saveHandler = Handler(Looper.getMainLooper())
    private val pendingSave = Runnable { saveChanges() }
    private var lastSubmittedBody: String? = null
    private var lastSave: Task<Void>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        if (noteId.isNullOrBlank()) {
            finish()
            return
        }

        enableNotesEdgeToEdge()
        setContentView(R.layout.activity_note)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val padding = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(padding.left, padding.top, padding.right, padding.bottom)
            insets
        }

        document = FirebaseFirestore.getInstance().collection("notes").document(noteId)
        editor = findViewById(R.id.note_editor_input)
        titleView = findViewById(R.id.note_editor_title)
        shareButton = findViewById(R.id.note_share_button)
        loading = findViewById(R.id.note_editor_loading)
        setTitleText(intent.getStringExtra(EXTRA_NOTE_TITLE))

        shareButton.setOnClickListener { showShareDialog() }
        editor.doAfterTextChanged {
            if (editor.isEnabled) scheduleSave()
        }
        findViewById<View>(R.id.note_back_button).setOnClickListener { finish() }

        loadNote()
    }

    override fun onStop() {
        super.onStop()
        saveHandler.removeCallbacks(pendingSave)
        saveChanges()
    }

    private fun setTitleText(title: String?) {
        titleView.text = title?.takeIf { it.isNotBlank() } ?: getString(R.string.untitled_note)
        setTitle(titleView.text)
    }

    private fun showShareDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.note_share_title)
            .setMessage(getString(R.string.note_share_message, titleView.text))
            .setView(R.layout.dialog_share_note)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun loadNote() {
        loading.isVisible = true
        document.get().addOnCompleteListener { task ->
            if (isFinishing || isDestroyed) return@addOnCompleteListener
            loading.isVisible = false
            val snapshot = if (task.isSuccessful) task.result else null
            if (snapshot == null || !snapshot.exists()) {
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.note_editor_load_failed)
                    .setPositiveButton(R.string.note_editor_retry) { _, _ -> loadNote() }
                    .setNegativeButton(R.string.back_button) { _, _ -> finish() }
                    .setOnCancelListener { finish() }
                    .show()
                return@addOnCompleteListener
            }

            setTitleText(snapshot.getString("title"))
            val body = (snapshot.get(BODY_TEXT_FIELD) as? String).orEmpty()
            lastSubmittedBody = body
            editor.setText(body)
            editor.isEnabled = true
            shareButton.isVisible = snapshot.getBoolean("isPublic") == true
        }
    }

    private fun scheduleSave() {
        saveHandler.removeCallbacks(pendingSave)
        saveHandler.postDelayed(pendingSave, AUTOSAVE_DELAY_MS)
    }

    private fun saveChanges() {
        val previousBody = lastSubmittedBody ?: return
        val body = editor.text.toString()
        if (body == previousBody && lastSave?.exception == null) return

        // Firestore queues newer edits even while earlier writes are waiting for a connection.
        lastSubmittedBody = body
        val context = applicationContext
        lastSave = document.update(BODY_TEXT_FIELD, body).addOnFailureListener { error ->
            Log.e("NoteActivity", "Failed to autosave note", error)
            Toast.makeText(context, R.string.note_save_failed, Toast.LENGTH_LONG).show()
        }
    }
}
