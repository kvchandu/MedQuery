package com.example.medquery

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    Row (modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("MedQuery", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
}



@Composable
fun MessageBox(message: RecordActivity.GPTMessage, modifier: Modifier = Modifier){
    Row {
        if (message.role == "user") {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null, // Provide a meaningful content description
                modifier = Modifier
                    .size(56.dp) // Adjust the size as needed
                    .weight(0.2f)
                    .fillMaxHeight()
            )
            Text(modifier = Modifier.weight(0.8f), text = "${message.content}")
        }
        else {

            Text(modifier = Modifier.weight(0.8f), text = "${message.content}")
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null, // Provide a meaningful content description
                modifier = Modifier
                    .size(56.dp) // Adjust the size as needed
                    .weight(0.2f)
                    .fillMaxHeight()
            )
        }
    }
}


