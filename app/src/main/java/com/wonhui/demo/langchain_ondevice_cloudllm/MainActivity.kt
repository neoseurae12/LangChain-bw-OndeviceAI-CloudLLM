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
    //Gemini(llmName = "Gemini Ai"),
    //MistralAi(llmName = "Mistral Ai"),
    Ollama(llmName = "Ollama (On-device AI)")
}

@Composable
fun AiModelSelector(
    modifier: Modifier = Modifier,
    isExpended: Boolean,
    onDismissRequest: () -> Unit,
    onModelSelected: (Model) -> Unit
) {
    DropdownMenu(
        modifier = modifier,
        expanded = isExpended,
        onDismissRequest = onDismissRequest,
        content = {
            Model.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.llmName) },
                    onClick = {
                        onModelSelected(item)
                    }
                )
            }
        }
    )
}
