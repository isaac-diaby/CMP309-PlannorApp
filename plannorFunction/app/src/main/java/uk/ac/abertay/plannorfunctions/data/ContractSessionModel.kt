package uk.ac.abertay.plannorfunctions.data

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date


data class IContractSessionUiState(
    val contractSessions: Map<String, List<IContractSession>>

)

data class IContractSession(
    val startDate: Date,
    val endDate: Date,
    val durationWork: Int = 0,
    val durationBreak: Int = 0,
    val owner: String = "TESTUID",
    val contractId: String = "TESTCID",
    val sessionID: String = "TESTSESSIONID"
)

data class IContractSessionPayload(
    val startDate: Date,
    val endDate: Date,
    val durationWork: Int = 0,
    val durationBreak: Int = 0,
    val contractId: String = "TESTCID",
    var owner: String = "TESTUID",
)

class ContractSessionViewModel : ViewModel() {
    val tag = "ContractSessionModel" // DEBUG: TAG
    val db = FirebaseFirestore.getInstance()

    private val _contractSessionUiState = MutableStateFlow(IContractSessionUiState(emptyMap()))
    val contractSessionUiState: StateFlow<IContractSessionUiState> =
        _contractSessionUiState.asStateFlow()

    init {
        initialState()
    }

    private fun initialState() {
//        Open a thread to call the Database and get the init contracts before the View model is created
        viewModelScope.launch {
            _contractSessionUiState.value =
                IContractSessionUiState(contractSessions = getAllMyContractSessions())
        }
    }

    fun reloadState() {
//        Reload my contractors
        viewModelScope.launch {
            _contractSessionUiState.update { currentState -> currentState.copy(contractSessions = getAllMyContractSessions()) }

        }
    }

    private suspend fun getAllMyContractSessions(): Map<String, ArrayList<IContractSession>> {
//        call firebase and get all of the sessions relating to the currently authed user
        var contractsSessionData: HashMap<String, ArrayList<IContractSession>> =
            HashMap<String, ArrayList<IContractSession>>()
        try {
//          Get My Own Contacts sessions
            val contractsQ =
                db.collection("sessions").whereEqualTo("owner", UserViewModel().myUID).get()
                    .addOnFailureListener { exception ->
                        Log.w(tag, "Error getting documents.", exception)
                    }
//        AWAIT for the request to finish
            val docs = contractsQ.await().documents
            for (document in docs) {
                Log.d("$tag AWAITED", "${document.id} => ${document.data}")
                val contractSessionToAdd =
                    IContractSession(
                        owner = document["owner"] as String,
                        contractId = document["contractId"] as String,
                        startDate = (document["startDate"] as Timestamp).toDate(),
                        durationBreak = (document["durationBreak"] as Long).toInt(),
                        durationWork = (document["durationWork"] as Long).toInt(),
                        endDate = (document["endDate"] as Timestamp).toDate(),
                        sessionID = document.id
                    )
                if (contractsSessionData[contractSessionToAdd.contractId].isNullOrEmpty()) {
                    contractsSessionData[contractSessionToAdd.contractId] = ArrayList()
                }
                contractsSessionData[contractSessionToAdd.contractId]?.add(contractSessionToAdd)
//                    contractsSessionData[contractSessionToAdd.contractId]!! +
            }

            Log.d("$tag FINAL", contractsSessionData.toString())
        } catch (e: Exception) {
            Log.e("$tag FS ERROR", "OH NO: ${e.message}" )
        }
        return contractsSessionData
    }

    fun postASession(context: View, sessionMetaData: IContractSessionPayload) {

        sessionMetaData.owner = UserViewModel().myUID.toString()

        db.collection("sessions")
            .add(sessionMetaData)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    tag,
                    "DocumentSnapshot added with ID: ${documentReference.toString()}"
                )

                reloadState()
//          User Feedback that tells user that the session has been saved
                Snackbar.make(context, "New Session Added", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Error adding document", e)
                Snackbar.make(context, "Failed to add Contractor", Snackbar.LENGTH_SHORT)
                    .show()
            }
    }
    fun deleteSession(context: View, id: String) {
        db.collection("sessions")
            .document(id)
            .delete()
            .addOnSuccessListener { documentReference ->
                Log.d(
                    tag,
                    "DocumentSnapshot deleted with ID: ${documentReference.toString()}"
                )

                reloadState()

//          User Feedback that the session has been saved
                Snackbar.make(context, "Deleted Session", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Error adding document", e)
                Snackbar.make(context, "Failed to delete Contractor", Snackbar.LENGTH_SHORT)
                    .show()
            }
    }

}


