package uk.ac.abertay.plannorfunctions.data

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks.await
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Def the  Contracts UI State
data class IContractUiState(
    val contracts: List<IContract>, //= listOf(IContract(owner = "NEW", id = "TESTCID"))
    val createNewContractFormShow: Boolean = false,
    val editMode: Boolean = false,
    val computed: IComputedStats
)

data class IContract(
    val owner: String = "",
    val title: String = "",
    val id: String,
    val rate: Number = 10.5f
)

data class IContractPayload(
    val owner: String = "",
    val title: String = "",
    val rate: Float = 10.5f
)

data class IComputedStats(
    val allTimePay: Float = 0.00f,
    val allTimeHoursWorked: Float = 0.00f
//    val averageHourlyPay: Float = 10.5f,
//    val AverageHoursPs: Float = 7.5f

)

// This will manage the state for all of the contracts that the user is part of.
class ContractsViewModel : ViewModel() {
    val tag = "ContractModel" // DEBUG: TAG
    val db = FirebaseFirestore.getInstance()

    // only the ContractsViewModel can change this data
    private val _contractUiState =
        MutableStateFlow(IContractUiState(contracts = emptyList(), computed = IComputedStats()))
//    private val _contractUiState = MutableLiveData(IContractUiState())


    // public read-only for best practice.
    val contractUiState: StateFlow<IContractUiState> = _contractUiState.asStateFlow()
//    val contractUiState: IContractUiState? = _contractUiState.value


//        Business LOGIC State Vars
//    private lateinit var myContracts: ArrayList<IContract>
//
//        UI State Vars
//    private var myContractsMS: MutableList<IContract> = mutableListOf()

    init {
        initialState()
    }

    private fun initialState() {
//        myContractsMS.clear()
//        Open a thread to call the Database and get the init contracts before the View model is created
        viewModelScope.launch {
//            Best practice
            val myCntractors = getMyContracts()
            _contractUiState.value =
                IContractUiState(contracts = myCntractors, computed = IComputedStats())
        }
    }

    private fun reloadState() {
//        Reload my contractors
        viewModelScope.launch {
            _contractUiState.update { currentState -> currentState.copy(contracts = getMyContracts()) }
        }
    }

    private fun reloadComputedState(computedStats: IComputedStats) {
        viewModelScope.launch {
            _contractUiState.update { currentState -> currentState.copy(computed = computedStats) }
        }

    }


    //  Business logic
    suspend fun getMyContracts(): List<IContract> {
        //    call cloud firestore database for the currently authed users contract by the uid

        var contractsData: List<IContract> = listOf(IContract(owner = "NEW", id = ""))
        try {
//          Get My Own Contarcts
            val contractsQ =
                db.collection("contractors").whereEqualTo("owner", UserViewModel().myUID).get()
                    .addOnFailureListener { exception ->
                        Log.w(tag, "Error getting documents.", exception)
                    }
//        AWAIT for the request to finish
            val docs = contractsQ.await().documents
            for (document in docs) {
                Log.d(tag + " AWAITED", "${document.id} => ${document.data}")
//                    myContracts.add(document.data as ContractUiState)
                val contractToAdd =
                    IContract(
                        owner = document["owner"] as String,
                        title = document["title"] as String,
                        id = document.id,
                        rate = document["rate"] as Number, // Defualt £rate/h

                    )

                contractsData += contractToAdd
//                Log.d(tag+" IN LOOP", contractsData.toString())
            }

            Log.d(tag + " FINAL", contractsData.toString())
        } catch (e: Exception) {
            Log.e(tag + " FS ERROR", "OH NO: ${e.cause}", e.cause)
        }
        return contractsData

    }

    //  Update if the Add New Contract Dialog should show or not
    fun setAddNewContractDialogState(show: Boolean = false) {
//        Update inplace with update() and copy() function
        _contractUiState.update { currentState -> currentState.copy(createNewContractFormShow = show) }
    }

    fun toggleEditModeState() {
//        Update inplace with update() and copy() function
        _contractUiState.update { currentState -> currentState.copy(editMode = !currentState.editMode) }
    }

    //  a function to add a new contract under the current user UID
    fun AddNewContracts(context: View, title: String) {
        val NewContractor = IContractPayload(title = title, owner = UserViewModel().myUID!!)
        db.collection("contractors")
            .add(NewContractor)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    tag,
                    "DocumentSnapshot added with ID: ${documentReference.toString()}"
                )

                reloadState()
//          User Feedback tha that the contractor has been created
                Snackbar.make(context, "New Contractor Added", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Error adding document", e)
                Snackbar.make(context, "Failed to add Contractor", Snackbar.LENGTH_SHORT)
                    .show()
            }
    }

    fun deleteContract(context: View, index: Int) {
        val contractID = _contractUiState.value.contracts[index].id
        db.collection("contractors").document(contractID)
            .delete()
            .addOnSuccessListener { documentReference ->
                Log.d(
                    tag,
                    "DocumentSnapshot Deleted with ID: ${documentReference.toString()}"
                )
//                delete all sessions belonging to this contractor too
                db.collection("sessions").whereEqualTo("contractId", contractID).get()
                    .addOnSuccessListener { documents ->
                        for (doc in documents) {
                            doc.reference.delete()
                        }


                        // User Feedback tha that the contractor has been deleted
                        Snackbar.make(
                            context,
                            "Deleted ${_contractUiState.value.contracts[index].title} Contractor",
                            Snackbar.LENGTH_SHORT
                        )
                            .show()

                        reloadState()
                        toggleEditModeState()
                    }
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Error deleting document", e)
                Snackbar.make(context, "Failed to delete Contractor", Snackbar.LENGTH_SHORT)
                    .show()
            }
    }


    //Computes the hours worked and the pay
    fun computeAllTimePay(
        contractors: List<IContract>,
        contractSessions: Map<String, List<IContractSession>>
    ): IComputedStats {
//        val contractors = contractUiState.value.contracts
//        val contractSessions = ContractSessionViewModel().contractSessionUiState.value.contractSessions
        Log.d(tag, "ct1: ${contractors}. ${contractSessions.entries}")
//        loop over my contracts and get thier ID's so we can get the hourly rate
        var allTimePay = 0f
        var allTimeHoursWorked = 0f
        try {
            for (contractor in contractors) {
                Log.d(tag, "ct: ${contractor.id}. ${contractSessions.entries}")
                if (contractSessions[contractor.id].isNullOrEmpty() == false) {
                    for (session in contractSessions.get(contractor.id.toString())!!) {
                        val hourlySession: Float = (session.durationWork.toFloat() / 3600) // 1H
                        val pay: Float = hourlySession * contractor.rate.toFloat()
                        // Add to Sum
                        allTimePay += pay
                        allTimeHoursWorked += hourlySession
                        Log.d(tag, "Sessions Pay: $pay , Hours: $hourlySession")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag + "Sessions", "OH NO: ", e.cause)
        }
        val computed =
            IComputedStats(allTimePay = allTimePay, allTimeHoursWorked = allTimeHoursWorked)
        reloadComputedState(computed)
        return computed
    }
}




