package uk.ac.abertay.plannorfunctions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import uk.ac.abertay.plannorfunctions.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import uk.ac.abertay.plannorfunctions.helper.AuthHelper

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Firebase Auth
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //       Find the inputs and error text
        val nameText = binding.accountNameField
        val emailText = binding.accountEmailField
        val passwordText = binding.accountPasswordField
        val isContractor = binding.isContractor
        val errorText = binding.authErrorMsg

        val signUpBtn = binding.SignupBtn
        signUpBtn.setOnClickListener {
            // Login to the app
            val name = nameText.text.toString()
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val isContractor = isContractor.isChecked

//            Validate the inputs
            val myValidator = AuthHelper()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorText.text = getString(R.string.auth_login_error_msg)
            } else if (!myValidator.isEmailValid(email)) {
                errorText.text = "Invalid Email"

            } else if (myValidator.isPasswordValid(password)) {
                errorText.text = "Invalid Password - length 6+"
            } else {
                signUp(name, email, password, isContractor)
            }
        }


        //        Login to the app screeen
        val loginBtn = binding.LoginBtn
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    private fun signUp(name: String, email: String, password: String, contractor: Boolean) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignUpActivity", "createUserWithEmail:success")
                    val user = auth.currentUser
                    // add user to database with their name, email, and contractor status
                    val newUserData = hashMapOf(
                        "name" to name,
                        "contractor" to contractor,
                        "email" to email,
                        "uid" to user?.uid
                    )
                    db.collection("accounts").document(newUserData.get("uid") as String)
                        .set(newUserData)
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                "SignUpActivity",
                                "DocumentSnapshot added with ID: ${documentReference.toString()}"
                            )
                            // go to home Authed screen
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("SignUpActivity", "Error adding document", e)
                        }
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignUpActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.d("SignUpActivity", user.toString())

    }

}