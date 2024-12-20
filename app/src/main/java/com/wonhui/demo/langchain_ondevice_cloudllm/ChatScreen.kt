package com.wonhui.demo.langchain_ondevice_cloudllm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun ChatRoute(
    chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.getFactory(LocalContext.current.applicationContext)
    )
) {
    val chatUiState by chatViewModel.uiState.collectAsState()

    ChatScreen(
        uiState = chatUiState,
        onSendChatClicked = { inputText -> chatViewModel.sendMessage(inputText) },
        onModelSelected = chatViewModel::onModelSelected
    )
}

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onSendChatClicked: (String) -> Unit = {},
    onModelSelected: (Model) -> Unit
) {
    var userMessage by remember { mutableStateOf("") }
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
                value = userMessage,
                label = { Text(stringResource(R.string.chat_label)) },
                placeholder = { Text(stringResource(R.string.chat_hint)) },
                onValueChange = { userMessage = it },
                modifier = Modifier
                    .weight(8f)
            )
            TextButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendChatClicked(userMessage)
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
                    // Copy enabled
                    SelectionContainer {
                        Text(
                            text = loadingState.outputText,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
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
