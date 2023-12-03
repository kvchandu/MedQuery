package com.example.medquery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign


import com.example.medquery.ui.theme.MedQueryTheme
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


import okhttp3.Response;
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder


class RecordActivity : ComponentActivity() {


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

    data class AssistantResponse(
        val answer: String
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
                    askAssistant(matches[0])

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

    fun getJsonQuestion(question: String): RequestBody {
        val json = JSONObject().apply {
            put("question", question)
        }

        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
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

    private fun askAssistant(query: String) {

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "http://192.168.1.15:8000/qna?question=$encodedQuery"
        Log.d("TEST", url)

        val request = Request.Builder()
            .url(url)
            .get()
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                Log.d("TEST", "Request Failed ${e.message}")

            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
//                println(response.body()?.string())

                val responseBody = response.body()?.string()
                val gson = Gson()

                val assistantResponse = gson.fromJson(responseBody, AssistantResponse::class.java)
                val answer = assistantResponse.answer
//                val gptResponse = gson.fromJson(responseBody, GPTResponse::class.java)
//                val content = gptResponse.choices.firstOrNull()?.message?.content

                if (answer != null) {
                    Log.d("TEST", answer)
                }


                conversationMessages.add(GPTMessage(role = "assistant", content = answer.toString()))
            }
        })


    }


    private fun askGPT(query: String) {
        val url = "https://api.openai.com/v1/chat/completions"
        val jsonRequest = getJsonRequest(conversationMessages)

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ")
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
