package com.example.real_time_notes

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<Button>(R.id.sign_in_back_button).setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()

        val signInButton = findViewById<Button>(R.id.sign_in_button)
        val emailField = findViewById<EditText>(R.id.sign_in_email_input)
        val passwordField = findViewById<EditText>(R.id.sign_in_password_input)

        signInButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.error = getString(R.string.invalid_email)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordField.error = getString(R.string.password_required)
                return@setOnClickListener
            }

            signInButton.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    signInButton.isEnabled = true

                    if (task.isSuccessful) {

                        val userEmail = auth.currentUser?.email ?: email

                        Toast.makeText(
                            this,
                            getString(R.string.signed_in_as, userEmail),
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this, SecondActivity::class.java)
                            .putExtra(SecondActivity.EXTRA_GUEST_NAME, userEmail)
                        startActivity(intent)
                        finish()
                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.localizedMessage
                                ?: getString(R.string.sign_in_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
