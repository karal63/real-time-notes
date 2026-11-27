package com.example.real_time_notes

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableNotesEdgeToEdge()
        setContentView(R.layout.activity_signup)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<Button>(R.id.sign_up_back_button).setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()

        val signUpButton = findViewById<Button>(R.id.sign_up_button)
        val emailField = findViewById<EditText>(R.id.sign_up_email_input)
        val passwordField = findViewById<EditText>(R.id.sign_up_password_input)
        val confirmPasswordField = findViewById<EditText>(R.id.sign_up_confirm_password_input)

        signUpButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.error = getString(R.string.invalid_email)
                return@setOnClickListener
            }

            if (password.length < 6) {
                passwordField.error = getString(R.string.password_too_short)
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordField.error = getString(R.string.passwords_do_not_match)
                return@setOnClickListener
            }

            signUpButton.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    signUpButton.isEnabled = true

                    if (task.isSuccessful) {

                        val userEmail = auth.currentUser?.email ?: email

                        Toast.makeText(
                            this,
                            getString(R.string.account_created, userEmail),
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
                                ?: getString(R.string.registration_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

        }
    }
}
