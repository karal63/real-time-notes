package com.example.real_time_notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class NoteActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_NOTE_ID = "noteId"
        private const val EXTRA_NOTE_TITLE = "noteTitle"
        private const val STATE_DRAFT = "draft"
        private const val BODY_FIELD = "editorBody"

        fun createIntent(context: Context, noteId: String, title: String): Intent =
            Intent(context, NoteActivity::class.java)
                .putExtra(EXTRA_NOTE_ID, noteId)
                .putExtra(EXTRA_NOTE_TITLE, title)
    }

    private lateinit var document: DocumentReference
    private lateinit var editor: EditText
    private lateinit var titleView: TextView
    private lateinit var saveButton: MaterialButton
    private lateinit var loading: LinearProgressIndicator
    private var savedBody: String? = null
    private var pendingBody: String? = null
    private var restoredDraft: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        if (noteId.isNullOrBlank()) {
            finish()
            return
        }

        enableEdgeToEdge()
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
        saveButton = findViewById(R.id.note_save_button)
        loading = findViewById(R.id.note_editor_loading)
        setTitleText(intent.getStringExtra(EXTRA_NOTE_TITLE))

        saveButton.setOnClickListener { saveNote() }
        editor.doAfterTextChanged { updateSaveButton() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closeEditor()
        })
        findViewById<View>(R.id.note_back_button).setOnClickListener { closeEditor() }

        restoredDraft = savedInstanceState?.getCharSequence(STATE_DRAFT)?.toString()
        loadNote()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val body = currentBody()
        val draft = if (savedBody != null && body != savedBody) {
            body
        } else {
            restoredDraft
        }
        outState.putString(STATE_DRAFT, draft)
    }

    private fun setTitleText(title: String?) {
        titleView.text = title?.takeIf { it.isNotBlank() } ?: getString(R.string.untitled_note)
        setTitle(titleView.text)
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
                    .setNegativeButton(R.string.back_button) { _, _ -> closeEditor() }
                    .setOnCancelListener { closeEditor() }
                    .show()
                return@addOnCompleteListener
            }

            setTitleText(snapshot.getString("title"))
            val body = (snapshot.get("$BODY_FIELD.text") as? String).orEmpty()
            savedBody = body
            editor.setText(restoredDraft ?: body)
            restoredDraft = null
            editor.isEnabled = true
            updateSaveButton()
        }
    }

    private fun currentBody() = editor.text.toString()

    private fun updateSaveButton() {
        saveButton.isEnabled = savedBody != null && pendingBody == null && currentBody() != savedBody
    }

    private fun saveNote() {
        val body = currentBody()
        pendingBody = body
        updateSaveButton()
        document.update(BODY_FIELD, mapOf("text" to body)).addOnCompleteListener { task ->
            if (isFinishing || isDestroyed) return@addOnCompleteListener
            if (task.isSuccessful) savedBody = body
            pendingBody = null
            updateSaveButton()
            val message = if (task.isSuccessful) R.string.note_editor_saved else R.string.note_save_failed
            Snackbar.make(editor, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun closeEditor() {
        val lastSubmittedBody = pendingBody ?: savedBody
        val hasUnsavedChanges = restoredDraft != null ||
            (lastSubmittedBody != null && currentBody() != lastSubmittedBody)
        if (!hasUnsavedChanges) {
            finish()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.note_editor_unsaved)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.note_editor_discard) { _, _ -> finish() }
            .show()
    }
}
