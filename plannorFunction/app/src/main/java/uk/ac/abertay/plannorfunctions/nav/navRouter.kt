package uk.ac.abertay.plannorfunctions.nav

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.abertay.plannorfunctions.data.ContractSessionViewModel
import uk.ac.abertay.plannorfunctions.data.ContractsViewModel
import uk.ac.abertay.plannorfunctions.data.UserViewModel
import uk.ac.abertay.plannorfunctions.screens.ContractorScreen
import uk.ac.abertay.plannorfunctions.screens.HomeOverviewScreen
import uk.ac.abertay.plannorfunctions.services.ContractSessionService

// Authed App Nav Controller
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun navRouter(
    contractSessionService: ContractSessionService,
    contractsViewModel: ContractsViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    contractSessionViewModel: ContractSessionViewModel = viewModel()
) {
    val navController = rememberNavController()
    val userUIState by userViewModel.userUiState.collectAsState()
    val contractsUiState by contractsViewModel.contractUiState.collectAsState()
    val contractSessionUIState by contractSessionViewModel.contractSessionUiState.collectAsState()

    NavHost(navController = navController, startDestination = AppRoutes.Home.route) {
        composable(AppRoutes.Home.route) {
            contractsViewModel.computeAllTimePay(contractsUiState.contracts, contractSessionUIState.contractSessions )
            HomeOverviewScreen(
                navController = navController,
                contractsViewModel,
                userViewModel,
                contractSessionViewModel
            )
        }
//        composable(AppRoutes.Home.route) { HomeOverviewScreen(navController = navController, contractsViewModel, userViewModel) }
        composable(AppRoutes.Contractor.route + "/{id}") { navBackStack ->
//            Get the ID of the contract that the user has selected
            val contractIndex = navBackStack.arguments?.getString("id")
            Log.d("ContractCard", "RouterID: $contractIndex")
            ContractorScreen(
                navController = navController,
                contractSessionService = contractSessionService,
                index = contractIndex?.toInt()!!,
                contractsViewModel,
                contractSessionViewModel
            )
        }
    }
}