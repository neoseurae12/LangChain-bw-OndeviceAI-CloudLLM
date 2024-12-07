package com.wonhui.demo.langchain_ondevice_cloudllm

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wonhui.demo.langchain_ondevice_cloudllm.ui.theme.DemoAiTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: SummarizeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = SummarizeViewModel()

        setContent {
            DemoAiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SummarizeRoute(viewModel)
                }
            }
        }

        // Subscribe a `networkStatus`
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                MyApp.networkStatus.collectLatest { isConnected ->
                    viewModel.switchModelByNetwork(isConnected)
                    showNetworkStatus(isConnected)
                }
            }
        }
    }

    private fun showNetworkStatus(isConnected: Boolean) {
        val message = if (isConnected) getString(R.string.network_connected) else getString(R.string.network_disconnected)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "My Network Status: $message")
    }
}

@Composable
internal fun SummarizeRoute(
    summarizeViewModel: SummarizeViewModel = viewModel()
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()

    SummarizeScreen(
        uiState = summarizeUiState,
        onSummarizeClicked = { inputText -> summarizeViewModel.summarize(inputText) },
        onModelSelected = summarizeViewModel::onModelSelected
    )
}

@Composable
fun SummarizeScreen(
    uiState: SummaryState,
    onSummarizeClicked: (String) -> Unit = {},
    onModelSelected: (Model) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var isModelSelectorExpended by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isModelSelectorExpended = true
                },
            verticalAlignment = Alignment.CenterVertically,
            content = {
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = "Selected Model : ${uiState.selectedModel.llmName}"
                )
                AiModelSelector(
                    isExpended = isModelSelectorExpended,
                    onDismissRequest = { isModelSelectorExpended = false },
                    onModelSelected = {
                        isModelSelectorExpended = false
                        onModelSelected(it)
                    }
                )
            }
        )
        Row {
            OutlinedTextField(
                value = prompt,
                label = { Text(stringResource(R.string.summarize_label)) },
                placeholder = { Text(stringResource(R.string.summarize_hint)) },
                onValueChange = { prompt = it },
                modifier = Modifier
                    .weight(8f)
            )
            TextButton(
                onClick = {
                    if (prompt.isNotBlank()) {
                        onSummarizeClicked(prompt)
                    }
                },

                modifier = Modifier
                    .weight(2f)
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(stringResource(R.string.action_go))
            }
        }
        when (val loadingState = uiState.loadingState) {
            LoadingState.Initial -> {
                // Nothing is shown
            }

            LoadingState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadingState.Success -> {
                Row(modifier = Modifier.padding(all = 8.dp)) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Person Icon"
                    )
                    Text(
                        text = loadingState.outputText,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            is LoadingState.Error -> {
                Text(
                    text = loadingState.errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(all = 8.dp)
                )
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
