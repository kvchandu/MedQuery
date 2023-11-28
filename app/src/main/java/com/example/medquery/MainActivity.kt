package com.example.medquery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.heightIn


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf


import com.example.medquery.ui.theme.MedQueryTheme


class MainActivity : ComponentActivity() {


    private lateinit var speechRecognizer: SpeechRecognizer
    private val chatBoxText = mutableStateOf("")
    private val conversationMessages = mutableStateListOf<GPTMessage>()
    data class GPTMessage(val role: String, val message: String)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    chatBoxText.value = matches[0]
                    conversationMessages.add(GPTMessage("client", matches[0]))
                    Log.d("TEST", conversationMessages.size.toString())
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        setContent {
            MedQueryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
//                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Scaffold(topBar = {}, bottomBar = {BottomBar()}){
                            innerPadding ->
                        LazyColumn (modifier = Modifier
                                .padding(innerPadding)){
                            items(conversationMessages.size) {
                                index -> MessageBox(message = conversationMessages[index])
                            }
                        }

                    }
                }
            }
        }
    }



    @Composable
    private fun ConversationArea(modifier: Modifier = Modifier){
        Text(text = "Hi")
    }
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }


    @Composable
    fun MessageBox(message: GPTMessage, modifier: Modifier = Modifier){
        Text("${message.role} : ${message.message}" )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatBox(
        modifier: Modifier = Modifier
    ) {
        TextField(
            value = chatBoxText.value,
            onValueChange = {},
            modifier = modifier
                .heightIn(min = 56.dp)
        )
    }

    @Composable
    fun ChatButton(modifier: Modifier = Modifier) {
        Button(onClick = { startSpeechToText()}){
            Text("Filled")
        }
    }

    @Composable
    fun BottomBar(modifier: Modifier = Modifier){
        Row () {
            ChatBox(modifier = Modifier.weight(0.75f))
            ChatButton(modifier = Modifier.weight(0.25f))
        }
    }
}
