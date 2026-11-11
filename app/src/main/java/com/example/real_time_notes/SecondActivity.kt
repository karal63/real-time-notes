package com.example.real_time_notes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_GUEST_NAME = "com.example.real_time_notes.GUEST_NAME"
        private const val STATE_TITLES = "note_titles"
        private const val STATE_DESCRIPTIONS = "note_descriptions"
    }

    private val notes = mutableListOf<Note>()

    private val createNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val title = result.data?.getStringExtra(CreateNoteActivity.EXTRA_NOTE_TITLE).orEmpty()
        val description = result.data?.getStringExtra(CreateNoteActivity.EXTRA_NOTE_DESCRIPTION).orEmpty()
        if (title.isNotBlank()) {
            notes.add(0, Note(title, description))
            showNotes()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val guestName = intent.getStringExtra(EXTRA_GUEST_NAME) ?: getString(R.string.default_guest_name)
        findViewById<TextView>(R.id.welcome_text).text = getString(R.string.welcome_guest, guestName)

        if (savedInstanceState == null) {
            notes.add(Note(getString(R.string.sample_note_one), getString(R.string.sample_description_one)))
            notes.add(Note(getString(R.string.sample_note_two), getString(R.string.sample_description_two)))
        } else {
            val titles = savedInstanceState.getStringArrayList(STATE_TITLES).orEmpty()
            val descriptions = savedInstanceState.getStringArrayList(STATE_DESCRIPTIONS).orEmpty()
            notes.addAll(titles.zip(descriptions).map { (title, description) -> Note(title, description) })
        }
        showNotes()

        findViewById<Button>(R.id.add_note_button).setOnClickListener {
            createNoteLauncher.launch(Intent(this, CreateNoteActivity::class.java))
        }
        findViewById<Button>(R.id.join_button).setOnClickListener {
            Toast.makeText(this, R.string.coming_later, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList(STATE_TITLES, ArrayList(notes.map { it.title }))
        outState.putStringArrayList(STATE_DESCRIPTIONS, ArrayList(notes.map { it.description }))
        super.onSaveInstanceState(outState)
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
        }
    }
}
