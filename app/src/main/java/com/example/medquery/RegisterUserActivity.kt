package com.example.medquery

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medquery.ui.theme.MedQueryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterUserActivity : ComponentActivity() {

    data class PatientInfo (val name: String, val age: String, val conditions: String)
    private lateinit var database: DatabaseReference
    lateinit var mAuth: FirebaseAuth

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedQueryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
//                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {TopBar()}, bottomBar = {}) {
                        innerPadding ->
                        RegistrationForm(modifier = Modifier.padding(16.dp))
                    }

                }
            }
        }
        mAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
    }

    fun submitForm(info: PatientInfo){
        mAuth.uid?.let {
            database.child("users").child(mAuth.uid.toString()).setValue(info).addOnSuccessListener {
                val intent = Intent(this, PatientHomeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RegistrationForm(modifier: Modifier = Modifier) {

        var patientName by rememberSaveable { mutableStateOf("") }
        var age by rememberSaveable { mutableStateOf("") }
        var medicalCondition by rememberSaveable { mutableStateOf("") }


        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Name",
                    modifier = Modifier.weight(0.2f)
                )
                TextField(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxWidth(),
                    value = patientName,
                    onValueChange = { patientName = it },
                    placeholder = { Text(text = "Enter your name") },
                )
            }

            Spacer(modifier = Modifier.padding(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Age",
                    modifier = Modifier.weight(0.2f)
                )
                TextField(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxWidth(),
                    value = age,
                    onValueChange = { age = it },
                    placeholder = { Text(text = "Enter your age") },
                )
            }

            Text(
                text = "Prior Medical Conditions",
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = medicalCondition,
                onValueChange = { medicalCondition = it },
                placeholder = { Text(text = "Symptoms, etc") },
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Button(onClick = {submitForm(PatientInfo(name= patientName, age= age, conditions= medicalCondition))}){
                Text(text = "Register")
            }
        }
    }
}
