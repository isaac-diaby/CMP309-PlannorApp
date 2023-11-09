package uk.ac.abertay.plannorfunctions.ui.theme.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.ac.abertay.plannorfunctions.data.IContract
import uk.ac.abertay.plannorfunctions.ui.theme.PlannorFunctionsTheme


// Contract card Display
@ExperimentalMaterial3Api
@Composable
fun ContractCard(
    index: Int = 0,
    contractMetaData: IContract,
    isEditMode: Boolean,
    dialogStateCtl: () -> Unit = {},
    selectedContractCtl: (selectedIndex: Int) -> Unit = {},
    deleteContractCtl: (selectedIndex: Int) -> Unit = {}
) {

    fun contractorSelect() {
        if (contractMetaData.title == "") {
            dialogStateCtl()
        } else {
            Log.d("ContractCard", "Index Selected: $index")
            selectedContractCtl(index)
        }
    }

    ElevatedCard(
        onClick = { contractorSelect() },
        modifier = Modifier
            .size(width = 140.dp, height = 80.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            if (contractMetaData.title == "") {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                Icon(
                    Icons.Filled.Add,
                    modifier = Modifier
//                        .align(Alignment.Center)
                        .size(36.dp),
                    contentDescription = "Add a New Contract"
                )
//                Text("Add", Modifier.align(Alignment.Center))
            } else {
                Text(
                    contractMetaData.title,
//                    Modifier.align(Alignment.Center)
                )
                if (isEditMode) {
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Button(onClick = {
                        /*Delete this Contractor*/
                        deleteContractCtl(index)
                    }) {
                        Text(text = "Delete")
                    }
                }
            }
        }
    }
}


// Simple development preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PlannorFunctionsTheme {
        ContractCard(
            contractMetaData = IContract(title = "ACME LTD", id = ""),
            isEditMode = true
        )
    }
}