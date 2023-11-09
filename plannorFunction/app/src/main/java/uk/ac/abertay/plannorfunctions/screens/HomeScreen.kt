package uk.ac.abertay.plannorfunctions.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import uk.ac.abertay.plannorfunctions.OnboardingActivity
import uk.ac.abertay.plannorfunctions.data.ContractSessionViewModel
import uk.ac.abertay.plannorfunctions.data.ContractsViewModel
import uk.ac.abertay.plannorfunctions.data.IContract
import uk.ac.abertay.plannorfunctions.data.UserViewModel
import uk.ac.abertay.plannorfunctions.nav.AppRoutes
import uk.ac.abertay.plannorfunctions.ui.theme.PlannorFunctionsTheme
import uk.ac.abertay.plannorfunctions.ui.theme.components.ContractCard
import uk.ac.abertay.plannorfunctions.ui.theme.components.CreateNewContractDialog
import uk.ac.abertay.plannorfunctions.util.roundFloat

// The main home screen view
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeOverviewScreen(
    navController: NavHostController?,
    contractsViewModel: ContractsViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    contractSessionViewModel: ContractSessionViewModel = viewModel()

) {
    val contractsUiState by contractsViewModel.contractUiState.collectAsState()
    val userUIState by userViewModel.userUiState.collectAsState()
    val contractSessionsUiState by contractSessionViewModel.contractSessionUiState.collectAsState()

//    contractsViewModel.computeAllTimePay(
//        contractsUiState.contracts,
//        contractSessionsUiState.contractSessions
//    )
    val view = LocalView.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { HomeScreenBottomNavBar {contractsViewModel.toggleEditModeState()} }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            UserContracts(
                contracts = contractsUiState.contracts,
                dialogStateCtl = { contractsViewModel.setAddNewContractDialogState(true) },
                selectedContractCtl = { selectedIndex ->
                    Log.d("ContractCard", "HomeContractID: $selectedIndex")
                    navController?.navigate(AppRoutes.Contractor.route + "/$selectedIndex")
                },
                deleteContractCtl = {selectedIndex ->
                    Log.d("ContractCard", "Deleting Contract: $selectedIndex")
                    contractsViewModel.deleteContract(view, selectedIndex)
                    contractSessionViewModel.reloadState()
                    contractsViewModel.computeAllTimePay(contractsUiState.contracts, contractSessionsUiState.contractSessions )

                },
                isEditMode = contractsUiState.editMode
            )
            if (contractsUiState.createNewContractFormShow) {
                //                Dynamic Toggle of dialog form
                CreateNewContractDialog(
                    dialogStateCtl = { show ->
                        contractsViewModel.setAddNewContractDialogState(
                            show
                        )
                    },
                    addNewContractorCtl = { contractorTitle ->
                        contractsViewModel.AddNewContracts(view, contractorTitle)
                    }

                )
            }
            Spacer(modifier = Modifier.padding(vertical = 32.dp))
            Text(
                text = "Hello User: ${userUIState.name}",
                fontSize = 16.sp
            )


            // Display the users all time hours / states and hourly rate.
            allTimeOverview(
                contractsUiState.computed.allTimePay,
                contractsUiState.computed.allTimeHoursWorked
            )

        }

    }
}

// the bottom navigation bar for quick actions

@Composable
fun HomeScreenBottomNavBar(toggleContactorEditMode: () -> Unit) {
    // Fetching the Local Context
    val mContext = LocalContext.current
    fun signOut() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        mContext.startActivity(Intent(mContext, OnboardingActivity::class.java))
//        finish()
    }
    BottomAppBar(
        {
            IconButton(onClick = {
            /*Enable edit mode to remove my contacts*/
                toggleContactorEditMode()
            }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { signOut() }) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = "Logout")
            }
        }
    )

}

// Displays the contracts that the user is part of allows the user to add / join a new contract
@ExperimentalMaterial3Api
@Composable
fun UserContracts(
    contracts: List<IContract>?,
    dialogStateCtl: () -> Unit,
    selectedContractCtl: (selectedIndex: Int) -> Unit,
    isEditMode: Boolean,
    deleteContractCtl: (selectedIndex: Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "My Contracts",
            Modifier.padding(vertical = 16.dp),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        LazyRow {
            if (contracts != null) {
                items(count = contracts.size) {
//                ContractCard(contracts[it].title)
//                    ContractCard()
                    Log.d("HOMESContractor", "${contracts[it]}")
                    ContractCard(
                        it,
                        isEditMode = isEditMode,
                        contractMetaData = contracts[it],
                        dialogStateCtl = dialogStateCtl,
                        selectedContractCtl = selectedContractCtl,
                        deleteContractCtl = deleteContractCtl
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun allTimeOverview(totalPay: Float, totalHours: Float) {
    Column(
    ) {
        Text(
            text = "All Sessions Overview",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Card(
                modifier = Modifier
                    .size(width = 160.dp, height = 100.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Pay",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "£${roundFloat(totalPay)}",
                        fontSize = 18.sp
                    )
                }
            }
            Card(
                modifier = Modifier
                    .size(width = 160.dp, height = 100.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Hours",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${roundFloat(totalHours)} h",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}


// Simple development preview
@Preview(showBackground = true)
@Composable
fun HomeScreenDefaultPreview() {
//    FirebaseApp.initializeApp()
    PlannorFunctionsTheme {
        HomeOverviewScreen(null)
    }
}