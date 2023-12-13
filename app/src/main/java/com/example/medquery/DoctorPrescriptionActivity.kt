package com.example.medquery

import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medquery.ui.theme.MedQueryTheme
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class DoctorPrescriptionActivity : ComponentActivity() {

    data class Prescription(val patientName: String, val medicationName: String, val notes: String, val link: String)

    private lateinit var database: DatabaseReference
    private val isUploading = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database.reference
        setContent {
            MedQueryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = { }, bottomBar = ({})) { innerPadding ->
                        PrescriptionForm()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PrescriptionForm(modifier: Modifier = Modifier) {

        var patientName by rememberSaveable { mutableStateOf("") }
        var medicationName by rememberSaveable { mutableStateOf("") }
        var additionalNotes by rememberSaveable { mutableStateOf("") }
        var referenceLink by rememberSaveable {
            mutableStateOf("")
        }

        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("DOCTOR HOME SCREEN", fontSize = 18.sp)
            }


            Spacer(modifier = Modifier.padding(10.dp))

            Text(text = "Assign prescription to patient")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Patient Name",
                    modifier = Modifier.weight(0.2f)
                )
                TextField(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxWidth(),
                    value = patientName,
                    onValueChange = { patientName = it },
                    placeholder = { Text(text = "e.g. test") },
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Medication Name",
                    modifier = Modifier.weight(0.2f)
                )
                TextField(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxWidth(),
                    value = medicationName,
                    onValueChange = { medicationName = it },
                    placeholder = { Text(text = "e.g. Hexamine") },
                )
            }

            Text(
                text = "Additional Notes",
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                placeholder = { Text(text = "") },
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Text(
                text = "Reference Webpage.",
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = referenceLink,
                onValueChange = { referenceLink = it },
                placeholder = { Text(text = "") },
            )

            Spacer(modifier = Modifier.padding(4.dp))
            Row (horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                if (isUploading.value) {
                    Button(onClick = {
                        sendPrescription(
                            Prescription(
                                patientName = patientName,
                                medicationName = medicationName,
                                notes = additionalNotes,
                                link = referenceLink
                            )
                        )
                    }) {
                        Text(text = "Uploading Data")
                    }
                }

                else {
                    Button(onClick = {
                        sendPrescription(
                            Prescription(
                                patientName = patientName,
                                medicationName = medicationName,
                                notes = additionalNotes,
                                link = referenceLink
                            )
                        )
                    }) {
                        Text(text = "Send Prescription")
                    }
                }
            }
        }
    }

    private fun findPatient(patientName: String, onComplete: (String?) -> Unit) {
        val usersRef = database.child("users")
        val query: Query = usersRef.orderByChild("name").equalTo(patientName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Iterate through the matching results (there could be multiple, but for simplicity, we'll just take the first one)
                    for (userSnapshot in dataSnapshot.children) {
                        val userId = userSnapshot.key
                        onComplete(userId)
                        return
                    }
                } else {
                    // No match found
                    onComplete(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                onComplete(null)
            }
        })

    }

    private fun sendPrescription(prescription: Prescription) {
        isUploading.value = true
        val patientNameToSearch = prescription.patientName

        findPatient(patientNameToSearch) { userId ->
            if (userId != null) {
                println("User ID for $patientNameToSearch: $userId")

                val prescriptionsRef = database.child("prescriptions").child(userId)

                // Read existing prescriptions for the user
                prescriptionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val existingPrescriptions: MutableList<Map<String, Any>> =
                            if (dataSnapshot.exists()) {
                                dataSnapshot.children.mapNotNull { it.getValue() as? Map<String, Any> }
                                    .toMutableList()
                            } else {
                                mutableListOf()
                            }

                        // Create the new prescription data
                        val newPrescriptionData = mapOf(
                            "medicineName" to prescription.medicationName,
                            "notes" to prescription.notes
                            // Add other properties as needed
                        )

                        // Add the new prescription data to the existing list
                        existingPrescriptions.add(newPrescriptionData)

                        // Update the prescriptions node with the updated list
                        prescriptionsRef.setValue(existingPrescriptions)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    println("Prescription added successfully.")
                                    Log.d("firebase", "Prescription added successfully.")

                                    // Add to Vector Store
//                                    "/add/webpage"

                                    val url = "http://192.168.1.15:8000/add/webpage"
                                    val jsonRequest = getJsonRequest(prescription.link)

                                    val client = OkHttpClient.Builder()
                                        .connectTimeout(100, TimeUnit.SECONDS) // Set connection timeout to 10 seconds
                                        .readTimeout(100, TimeUnit.SECONDS)    // Set read timeout to 30 seconds
                                        .writeTimeout(100, TimeUnit.SECONDS)   // Set write timeout to 30 seconds
                                        .build()

                                    val request = Request.Builder().url(url).post(jsonRequest).build()

                                    client.newCall(request).enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            // Handle failure
                                            Log.d("TEST", "Request Failed ${e.message}")

                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            // Handle response

                                            Log.d("Upload", "Uploaded")
                                            isUploading.value = false

                                        }
                                    })



                                } else {
                                    println("Failed to add prescription.")
                                    Log.d("firebase", "Failed to add prescription.")
                                }
                            }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        println("Error reading existing prescriptions: ${databaseError.message}")
                        Log.d(
                            "firebase",
                            "Error reading existing prescriptions: ${databaseError.message}"
                        )
                    }
                })

            } else {
                println("No user found with the name $patientNameToSearch")
                Log.d("firebase", "Did not find patient")
            }
        }


    }

    fun getJsonRequest(link: String): RequestBody {
        val mutableStringBuilder = StringBuilder("{")
        mutableStringBuilder.append("\"links\": [")
//        for (message in lst) {
//            mutableStringBuilder.append("{\"role\":\"${message.role}\", \"content\":\"${message.content}\"},")
//        }

//        mutableStringBuilder.deleteCharAt(mutableStringBuilder.length - 1) // Remove the trailing comma
        mutableStringBuilder.append("\"$link\"")
        mutableStringBuilder.append("] ")
//        mutableStringBuilder.append("\"max_tokens\": 150")
        mutableStringBuilder.append("}")

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mutableStringBuilder.toString())

        Log.d("Link Json", mutableStringBuilder.toString())

        return body
    }
}


@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    MedQueryTheme {
        Greeting2("Android")
    }
}