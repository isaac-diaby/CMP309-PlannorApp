package uk.ac.abertay.plannorfunctions.ui.theme.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.ac.abertay.plannorfunctions.data.ContractsViewModel
import uk.ac.abertay.plannorfunctions.ui.theme.PlannorFunctionsTheme

@ExperimentalMaterial3Api
@Composable
fun CreateNewContractDialog(dialogStateCtl: ((show: Boolean) -> Unit), addNewContractorCtl: ((contractorTitle: String) -> Unit)){

    val charLimit = 25
    val charMin = 2
    var newContractTitle by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("", TextRange(charMin, charLimit)))
    }
    var isError by rememberSaveable { mutableStateOf(false) }

// Validate Contractor title to meet basic length check and isn't empty.
    fun validate(text: String) {
        isError = ((text.length > charLimit) || (text.length < charMin))
    }
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onDismissRequest.
                Log.d("Dialog", "Closed")
//                openDialog = false
                dialogStateCtl?.let { it(false) }
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,

                ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "New Contract",
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)

                    )
                    Text(
                        text = "Here you can add a new contract workspace so you can scope your work hours",
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
//                    Input Field
                    OutlinedTextField(

                        value = newContractTitle,
                        singleLine = true,
                        onValueChange = { newContractTitle = it },
                        isError = isError,
                        label = { Text("Contract Title") },
                        supportingText = { Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            text = if (isError) "Title needs to be $charMin-$charLimit: ${newContractTitle.text.length}" else ""
                        )},
                        modifier = Modifier.semantics {
                            // Provide localized description of the error
                            if (isError) error("Title needs to be 2-25 char")
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        TextButton(
                            onClick = {
                                Log.d("Dialog", "Dismiss")
                                dialogStateCtl?.let { it(false) }
                            },
//
                        ) {
                            Text("Dismiss")
                        }
                        TextButton(
                            onClick = {
                                Log.d("Dialog", "Saved")
                                validate(newContractTitle.text)
                                if (isError.not()) {
//                                    Post New Contractor
//                                    ContractsViewModel().AddNewContracts
                                    addNewContractorCtl?.invoke(newContractTitle.text)

                                    dialogStateCtl?.let { it(false) }
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
//            Text(text = "New Contract")
        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
 fun DefaultPreviewNewContractForm() {
    val openDialog = remember { mutableStateOf(true) }
    PlannorFunctionsTheme {
        if (openDialog.value) {
            CreateNewContractDialog(
                { show ->   openDialog.value = (show)},
                {contractorTitle -> contractorTitle}
            )
//            {  openDialog.value = (false)}
        }
    }
}