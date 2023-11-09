package uk.ac.abertay.plannorfunctions.helper

import android.util.Patterns

class AuthHelper {
//    TODO: make companion aka static
    // Helper function for email verification
    fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    // Helper function for Password verification  6 characters or more
    fun isPasswordValid(password: CharSequence): Boolean {
        return (password.length < 6)
    }
}