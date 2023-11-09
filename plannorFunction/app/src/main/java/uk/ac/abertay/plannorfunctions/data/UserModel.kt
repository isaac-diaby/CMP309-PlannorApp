package uk.ac.abertay.plannorfunctions.data

import android.service.autofill.UserData
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


data class IUserUiState(
    val name: String = "NAME",
    val email: String = "EMAIL",
    val uid: String = "TEST",
    val contractor: Boolean = true
)

class UserViewModel: ViewModel() {
    val tag = "UserModel" // DEBUG: TAG
    val auth = FirebaseAuth.getInstance() // Auth client
    val db = FirebaseFirestore.getInstance()


    private val _userUiState = MutableStateFlow(IUserUiState())
    val userUiState: StateFlow<IUserUiState> = _userUiState.asStateFlow()

    //        Business LOGIC State Vars
    private lateinit var myUserDataDocRef: DocumentReference

    //    Get my user ID
    private var _myUID = auth.currentUser?.uid
    val myUID: String?
        get() = _myUID

    init {
        initialState()
    }

    private fun initialState() {
        viewModelScope.launch {
//            Best practice
            _userUiState.value = getMyUserData()
        }
    }

    suspend fun getMyUserData(): IUserUiState {
        var userData: IUserUiState = IUserUiState()
        try {
            val myUID = auth.currentUser?.uid


            val userDataQ =
                db.collection("accounts").limit(1).whereEqualTo("uid", myUID).get()
                    .addOnFailureListener { exception ->
                        Log.w(tag, "Error getting User Document.", exception)
                    }
//        AWAIT for the request to finish
            val userDoc = userDataQ.await().documents[0]
            myUserDataDocRef = userDoc.reference
            val data = userDoc.data

//            // No User Doc Found fall back to defaults
            if (data.isNullOrEmpty()) return userData

            userData = IUserUiState(
                name = data["name"] as String,
                email = data["email"] as String,
                uid = data["uid"] as String,
                contractor = data["contractor"] as Boolean
            )

        } catch (e: Exception) {
            Log.e(tag + " FS ERROR", "OH NO:", e.cause)
        }
        return userData
    }
}