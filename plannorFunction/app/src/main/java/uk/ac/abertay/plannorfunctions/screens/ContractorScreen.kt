package uk.ac.abertay.plannorfunctions.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.material.snackbar.Snackbar
import uk.ac.abertay.plannorfunctions.data.ContractSessionViewModel
import uk.ac.abertay.plannorfunctions.data.ContractsViewModel
import uk.ac.abertay.plannorfunctions.data.IContractSession
import uk.ac.abertay.plannorfunctions.data.IContractSessionPayload
import uk.ac.abertay.plannorfunctions.helper.ContractSessionHelper
import uk.ac.abertay.plannorfunctions.services.ContractSessionService
import uk.ac.abertay.plannorfunctions.services.ContractSessionState
import uk.ac.abertay.plannorfunctions.ui.theme.PlannorFunctionsTheme
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.ACTION_SERVICE_CANCEL
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.ACTION_SERVICE_START
import uk.ac.abertay.plannorfunctions.util.ContractSessionConstants.ACTION_SERVICE_STOP
import uk.ac.abertay.plannorfunctions.util.roundFloat
import java.time.format.DateTimeFormatter
import java.util.Date


@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContractorScreen(
    navController: NavHostController?,
    contractSessionService: ContractSessionService,
    index: Int,
    contractsViewModel: ContractsViewModel = viewModel(),
    contractSessionViewModel: ContractSessionViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val view = LocalView.current
    val contractsUiState by contractsViewModel.contractUiState.collectAsState()
    val contractSessionsUiState by contractSessionViewModel.contractSessionUiState.collectAsState()

    val contractId = contractsUiState.contracts[index].id

    val hours by contractSessionService.hours
    val minutes by contractSessionService.minutes
    val seconds by contractSessionService.seconds
    val currentState by contractSessionService.currentState
    if (contractSessionService != null) {
    } else {
        Snackbar.make(view, "Could Not get contractSessionService", Snackbar.LENGTH_SHORT)
            .show()
    }
    fun navBackToHomeScreen() {
        navController?.popBackStack()
    }

    fun saveCurrentSession() {
//        Save the session to firebase
        val durationWork = ((seconds.toInt()) + (minutes.toInt() * 60) + (hours.toInt() * 60 * 60))

        val ContractSessionMetaData = IContractSessionPayload(
            startDate = contractSessionService.startDateTime,
            endDate = Date(),
            durationWork = durationWork,
            durationBreak = 0,
            contractId = contractId,
            owner = ""
        )
        contractSessionViewModel.postASession(view, ContractSessionMetaData)
    }



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { ContractorScreenBottomNavBar(ctx, currentState) { navBackToHomeScreen() } }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Selected Contract: ${contractsUiState.contracts[index].title}",
                Modifier.padding(vertical = 16.dp),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
//            The Stopwatch
            timerSection(hours, minutes, seconds)

//            Save Session Actions
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .weight(1f, false)
                    .align(alignment = Alignment.CenterHorizontally),

                onClick = {
                    ContractSessionHelper.triggerForegroundService(
                        context = ctx, action = ACTION_SERVICE_CANCEL
                    )
                    // Save to firebase
                    saveCurrentSession()

                },
                enabled = seconds != "00" && currentState != ContractSessionState.Started,
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(text = "Save Session")
            }
            PastSessions(contractSessionsUiState.contractSessions[contractsUiState.contracts[index].id],
                contractsUiState.contracts[index].rate,
                deleteContractCtl = { refId ->
                    contractSessionViewModel.deleteSession(
                        view,
                        refId
                    )
                })

        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun timerSection(hours: String, minutes: String, seconds: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        AnimatedContent(targetState = hours, transitionSpec = { transitionAnimation() }) {
            Text(
                text = "$hours H",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (hours == "00") Color.DarkGray else MaterialTheme.colorScheme.secondary
                )
            )
        }
        AnimatedContent(targetState = minutes, transitionSpec = { transitionAnimation() }) {
            Text(
                text = "$minutes M", style = TextStyle(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (minutes == "00") Color.DarkGray else MaterialTheme.colorScheme.secondary
                )
            )
        }
        AnimatedContent(targetState = seconds, transitionSpec = { transitionAnimation() }) {
            Text(
                text = "$seconds S", style = TextStyle(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (seconds == "00") Color.DarkGray else MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@Composable
fun PastSessions(
    iContractSessions: List<IContractSession>?,
    rate: Number,
    deleteContractCtl: (refId: String) -> Unit
) {
    //            TODO: add weekly break downs summary
//    modifier = Modifier.padding(16.dp)
    Column() {
        Text(
            text = "Past Sessions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        if (iContractSessions.isNullOrEmpty()) {
            Text(
                text = "You have no past sessions. create one by hitting the play button on the bottom right corner",
                fontSize = 14.sp,
            )
        } else {
            LazyColumn(
                Modifier.fillMaxWidth()
//                .padding(vertical = 24.dp),
//                verticalArrangement = Arrangement.spacedBy(4.dp),
//                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(iContractSessions.size) {
                    sessionItem(iContractSessions[it], rate, deleteContractCtl = deleteContractCtl)
                }
            }

        }
    }
}

@Composable
fun sessionItem(
    iContractSession: IContractSession,
    rate: Number,
    deleteContractCtl: (refId: String) -> Unit = {},
) {

    val hourlySession: Float = (iContractSession.durationWork.toFloat() / 3600) // 1H
    val minuteSession: Float = ((iContractSession.durationWork.toFloat() %  3600) / 60) // 1M
    val secondSession: Float = (iContractSession.durationWork.toFloat() % 60) // 1M
    val pay: Float = hourlySession * rate.toFloat()


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.wrapContentSize(),
//            text = "${roundFloat(hourlySession)} h"
        text = "${hourlySession.toInt()}:${minuteSession.toInt()}:${secondSession.toInt()}"
        )
//        Text(
////            modifier = Modifier.wrapContentSize(),
//            text = iContractSession.startDate.toString()
////                .format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy"))
//        )

//        Text(
//            modifier = Modifier.wrapContentSize(),
//            text = "${iContractSession.startDate.time}"
//        )
//        Text(
//            modifier = Modifier.wrapContentSize(),
//            text = "${iContractSession.endDate.time}"
//        )

        Text(
            modifier = Modifier.wrapContentSize(),
            text = "£${roundFloat(pay)}"
        )

        IconButton(onClick = {
        /* delete this session  */
            deleteContractCtl(iContractSession.sessionID)
        }) {
            Icon(Icons.Filled.DeleteForever, contentDescription = "Delete")
        }


    }
}


@Composable
fun ContractorScreenBottomNavBar(
    ctx: Context,
    currentState: ContractSessionState,
    backToHomeScreen: () -> Unit
) {
    fun sessionCtl() {
        ContractSessionHelper.triggerForegroundService(
            context = ctx,
            action = if (currentState == ContractSessionState.Started) ACTION_SERVICE_STOP
            else ACTION_SERVICE_START
        )
    }
    BottomAppBar(
        {
            IconButton(onClick = {
                /* Go back to the home screen */
                backToHomeScreen()
            }) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            }
            IconButton(onClick = { /*TODO: Enable edit mode to remove my sessions for the selected contract*/ }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { sessionCtl() },
                containerColor = if (currentState == ContractSessionState.Started) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = if (currentState == ContractSessionState.Started) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "Add New Time Session"
                )
            }
        })

}

// Transition Animation
@ExperimentalAnimationApi
fun transitionAnimation(duration: Int = 600): ContentTransform {
    return (slideInVertically(animationSpec = tween(durationMillis = duration)) { height -> height } + fadeIn(
        animationSpec = tween(durationMillis = duration)
    )).togetherWith(slideOutVertically(animationSpec = tween(durationMillis = duration)) { height -> height } + fadeOut(
        animationSpec = tween(durationMillis = duration)
    ))
}

@OptIn(ExperimentalAnimationApi::class)
@Preview(showBackground = true)
@Composable
fun ContractorScreenDefaultPreview() {
    PlannorFunctionsTheme {
//        ContractorScreen(null, ,9890)
    }
}