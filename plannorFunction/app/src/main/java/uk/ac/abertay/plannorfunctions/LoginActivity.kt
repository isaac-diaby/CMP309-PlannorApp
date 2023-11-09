package uk.ac.abertay.plannorfunctions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import uk.ac.abertay.plannorfunctions.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        //        Find the inputs and error text
        val emailText = binding.accountEmailField
        val passwordText = binding.accountPasswordField
        val errorText = binding.authErrorMsg

        //        Login to the app
        val loginBtn = binding.LoginBtn
        loginBtn.setOnClickListener {
            // Login to the app
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                errorText.text = getString(R.string.auth_login_error_msg)
            } else {
                login(email, password)
            }
        }

        val signUpBtn = binding.SignupBtn
        signUpBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginActivity", "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.

                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    //   Update the UI based on the user's login status
    @OptIn(ExperimentalAnimationApi::class)
    private fun updateUI(user:  FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val errorText = findViewById<TextView>(R.id.authErrorMsg)
            errorText.text = getString(R.string.auth_login_error_msg2)
        }

    }
}