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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign


import com.example.medquery.ui.theme.MedQueryTheme
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


import okhttp3.Response;
import java.io.IOException


class MainActivity : ComponentActivity() {


    private lateinit var speechRecognizer: SpeechRecognizer
    private val chatBoxText = mutableStateOf("")
    private val conversationMessages = mutableStateListOf<GPTMessage>()
    private val client = OkHttpClient()
    data class GPTMessage(val role: String, val content: String)
    data class GPTRequest(val model: String, val messages: List<GPTMessage> , val maxTokens: Int)
    data class GPTResponse(
        val id: String,
        val `object`: String,
        val created: Long,
        val model: String,
        val choices: List<Choice>,
        val usage: Usage
    )

    data class Choice(
        val index: Int,
        val message: Message,
        val finish_reason: String
    )

    data class Message(
        val role: String,
        val content: String
    )

    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )

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
//                    chatBoxText.value = matches[0]
                    conversationMessages.add(GPTMessage("user", matches[0]))
                    Log.d("TEST", conversationMessages.size.toString())
                    val reply = askGPT(matches[0])
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

                    Scaffold(topBar = { TopBar() }, bottomBar = {BottomBar()}){
                            innerPadding ->
                        LazyColumn (verticalArrangement = Arrangement.spacedBy(16.dp),modifier = Modifier
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

    fun getJsonRequest(lst: List<GPTMessage>): RequestBody {
        val mutableStringBuilder = StringBuilder("{")
        mutableStringBuilder.append("\"model\": \"gpt-3.5-turbo-0613\", \"messages\": [")
        for (message in lst) {
            mutableStringBuilder.append("{\"role\":\"${message.role}\", \"content\":\"${message.content}\"},")
        }
        mutableStringBuilder.deleteCharAt(mutableStringBuilder.length - 1) // Remove the trailing comma
        mutableStringBuilder.append("], ")
        mutableStringBuilder.append("\"max_tokens\": 150")
        mutableStringBuilder.append("}")

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), mutableStringBuilder.toString())

        return body
    }

    private fun askGPT(query: String) {
        val url = "https://api.openai.com/v1/chat/completions"
        val jsonRequest = getJsonRequest(conversationMessages)

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer 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")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
//                println(response.body()?.string())
                val responseBody = response.body()?.string()
                val gson = Gson()
                val gptResponse = gson.fromJson(responseBody, GPTResponse::class.java)
                val content = gptResponse.choices.firstOrNull()?.message?.content

                conversationMessages.add(GPTMessage(role = "assistant", content = content.toString()))
            }
        })
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

    @Composable
    fun TopBar(modifier: Modifier = Modifier) {
        Row (modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("MedQuery", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
}
