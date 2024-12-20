package com.wonhui.demo.langchain_ondevice_cloudllm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wonhui.demo.langchain_ondevice_cloudllm.ui.theme.DemoAiTheme

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DemoAiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ChatRoute()
                }
            }
        }
    }
}

enum class Model(val llmName: String) {
    OpenAi(llmName = "Open AI (Cloud-based LLM)"),
    Gemma2(llmName = "Gemma2 2b (On-device AI)")
}
