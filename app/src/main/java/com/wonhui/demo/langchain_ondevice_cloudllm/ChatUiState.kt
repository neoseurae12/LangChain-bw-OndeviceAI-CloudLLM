package com.wonhui.demo.langchain_ondevice_cloudllm

import androidx.compose.runtime.mutableStateListOf
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.UserMessage

data class ChatUiState(
    val selectedModel: Model = Model.OpenAi,
    val previousPrompt: String = "",
    val loadingState: LoadingState = LoadingState.Initial,
) {
    companion object {
        val Empty = ChatUiState()

        private const val START_TURN = "<start_of_turn>"
        private const val END_TURN = "<end_of_turn>"
        private const val USER_PREFIX = "user"
        private const val MODEL_PREFIX = "model"

        private val _messages: MutableList<ChatMessage> = mutableStateListOf()
        val messages: List<ChatMessage> = _messages

        val fullPrompt: String
            get() = _messages.takeLast(4).joinToString(separator = "\n") {
                when (it) {
                    is UserMessage -> {
                        "$START_TURN$USER_PREFIX\n${it.text()}$END_TURN"
                    }
                    is AiMessage -> {
                        "$START_TURN$MODEL_PREFIX\n${it.text()}$END_TURN"
                    }
                    else -> ""
                }
            }

        fun addMessage(chatMessage: ChatMessage) {
            _messages.add(chatMessage)
        }
    }
}
