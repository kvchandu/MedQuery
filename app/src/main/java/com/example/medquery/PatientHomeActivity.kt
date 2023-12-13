package com.example.medquery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medquery.ui.theme.MedQueryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class PatientHomeActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference
    lateinit var mAuth: FirebaseAuth

    data class PatientInfo(val name: String = "", val age: String = "", val conditions: String = "")

    private lateinit var info: PatientInfo
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        getPatientInfo()

        setContent {
            MedQueryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
//                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Patient HOME SCREEN", fontSize = 18.sp)
                        }


                        Spacer(modifier = Modifier.padding(10.dp))

                        PrescriptionList(mAuth.currentUser?.uid ?: "")


                        Button(onClick = { recordScreen() }) {
                            Text(text = "Talk to Assistant")
                        }
                    }

                }
            }
        }
    }

    private fun getPatientInfo() {
        val userId = mAuth.currentUser?.uid  // Getting the current user's UID

        if (userId != null) {
            // Reference to the user's data in the database
            val userRef = database.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        info = snapshot.getValue(PatientInfo::class.java)!!

                    } else {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        } else {

        }
    }


    @Composable
    fun PatientDescription() {
        Text("Hello $info.name")
    }

    @Composable
    fun PrescriptionList(patientFirebaseId: String) {
        var prescriptionList by remember { mutableStateOf(emptyList<Prescription>()) }

        Log.d("Prescription Patient ID", patientFirebaseId)

        // Use a Firebase database reference to fetch prescriptions for the patient
        val prescriptionsRef =
            FirebaseDatabase.getInstance().getReference("prescriptions")
                .child(patientFirebaseId)
        prescriptionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val prescriptions = mutableListOf<Prescription>()
                for (childSnapshot in snapshot.children) {
                    // Deserialize Prescription data and add it to the list
                    val prescription = childSnapshot.getValue(Prescription::class.java)
                    prescription?.let { prescriptions.add(it) }
                }
                prescriptionList = prescriptions
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            fun <DatabaseError> onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        // Display the prescription list using a Compose composable
        PrescriptionListContent(prescriptionList)
    }

    @Composable
    fun PrescriptionListContent(prescriptions: List<Prescription>) {
        // Customize this composable based on your UI requirements
        Column {
            Text("List of prescriptions assigned by the doctor")

            Spacer(modifier = Modifier.padding(10.dp))

            for (prescription in prescriptions) {
                Row() {
                    Text(text = "Medication: ")
                    Text(text = prescription.medicineName)

                }

                Row() {
                    Text(text = "Notes: ")
                    Text(text = prescription.notes)
                }

                Spacer(modifier = Modifier.padding(5.dp))
            }
        }
    }

    data class Prescription(
        val medicineName: String = "",
        val notes: String = ""
    )

    fun recordScreen() {
        val intent = Intent(this, RecordActivity::class.java)
        startActivity(intent)
    }
}