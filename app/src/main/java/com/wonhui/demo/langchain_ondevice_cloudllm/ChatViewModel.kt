package com.wonhui.demo.langchain_ondevice_cloudllm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.langchain4j.chain.ConversationalChain
import dev.langchain4j.data.message.AiMessage.aiMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val gemma2ChatModel: Gemma2ChatModel
) : ViewModel() {

    private val openAiModel = OpenAiChatModel.builder()
        .apiKey(BuildConfig.openAiApiKey)
        .modelName("gpt-3.5-turbo")
        .build()

    private val models = mutableMapOf(
        Model.OpenAi to openAiModel,
        Model.Gemma2 to gemma2ChatModel
    )

    private val _uiState = MutableStateFlow(ChatUiState.Empty)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(inputText: String) {
        _uiState.update { it.copy(loadingState = LoadingState.Loading) }

        ChatUiState.addMessage(userMessage(inputText))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedModel = models[uiState.value.selectedModel]
                val memoryWindow = MessageWindowChatMemory.withMaxMessages(10)
                ChatUiState.messages.forEachIndexed { index, message ->
                    if (index != ChatUiState.messages.lastIndex)
                        memoryWindow.add(message)
                }
                val chain = ConversationalChain.builder()
                    .chatLanguageModel(selectedModel)
                    .chatMemory(memoryWindow)
                    .build()

                selectedModel?.let {
                    val response = chain.execute(inputText)
                    _uiState.update { it.copy(loadingState = LoadingState.Success(response)) }
                    ChatUiState.addMessage(aiMessage(response))
                }

                Log.d(TAG, memoryWindow.messages().toString())
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingState = LoadingState.Error(
                            e.localizedMessage ?: ""
                        )
                    )
                }
            }
        }
    }

    fun onModelSelected(model: Model) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    private fun switchModelByNetwork(isNetworkConnected: Boolean) {
        if (isNetworkConnected)
            _uiState.update { it.copy(selectedModel = Model.OpenAi) }
        else
            _uiState.update { it.copy(selectedModel = Model.Gemma2) }
    }

    init {
        // Subscribe a `networkStatus`
        viewModelScope.launch {
            MyApp.networkStatus.collectLatest { isConnected ->
                switchModelByNetwork(isConnected)
            }
        }
    }

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val inferenceModel = Gemma2ChatModel.getInstance(context)
                return ChatViewModel(inferenceModel) as T
            }
        }
    }
}
