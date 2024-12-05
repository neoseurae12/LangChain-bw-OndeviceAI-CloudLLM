package com.wonhui.demo.langchain_ondevice_cloudllm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import dev.langchain4j.chain.ConversationalChain
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration


class SummarizeViewModel(
    private val googleModel: GenerativeModel
) : ViewModel() {

//    private val mistralAiModel = MistralAiChatModel.builder()
//        .apiKey(AiToken.MistralAiToken)
//        .build()


//    private val PROJECT = "gen-lang-client-0139548919"
//    private val LOCATION = "us-central1"
//    private val MODEL_NAME = "gemini-pro"

//    private val geminiAiModel = VertexAiGeminiChatModel.builder()
//        .project(PROJECT)
//        .location(LOCATION)
//        .modelName(MODEL_NAME)
//        .build()

    private val openAiModel = OpenAiChatModel.builder()
        .apiKey(BuildConfig.openAiApiKey)
        .modelName("gpt-3.5-turbo")
        .build()

    private val ollamaModel = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("llama3.1")
        .timeout(Duration.ofSeconds(60))
        .build()

    private val models = mutableMapOf(
        Model.OpenAi to openAiModel,
        Model.Ollama to ollamaModel,
        //Model.MistralAi to mistralAiModel,
        //Model.Gemini to geminiAiModel,
    )

    private val _uiState = MutableStateFlow(SummaryState.Empty)
    val uiState: StateFlow<SummaryState> = _uiState.asStateFlow()
    private var previousQuestion = ""

    fun summarize(inputText: String) {
        _uiState.update { it.copy(loadingState = LoadingState.Loading) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedModel = models[uiState.value.selectedModel]
                val memoryWindow = MessageWindowChatMemory.withMaxMessages(10)
                val chain = ConversationalChain.builder()
                    .chatLanguageModel(selectedModel)
                    .chatMemory(memoryWindow)
                    .build()

                if (previousQuestion.isNotEmpty()) {
                    chain.execute(previousQuestion)
                }

                selectedModel?.let {
                    previousQuestion = inputText
                    val response = chain.execute(inputText)
                    _uiState.update { it.copy(loadingState = LoadingState.Success(response)) }
                }
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
}


//val generativeModel = GenerativeModel(
//    modelName = "gemini-pro",
//    apiKey = AiToken.GoogleAiToken
//)
//
//private val mistralModel = MistralAiChatModel.builder()
//    .apiKey(AiToken.MistralAiToken)
//    .build()
//
//private val openAiModel = OpenAiChatModel.builder()
//    .apiKey(AiToken.OpenAiToken)
//    .modelName("gpt-4-turbo-preview")
//    .build()
//
//private val geminiModel = VertexAiGeminiChatModel.builder()
//    .project("bubbly-dominion-285208")
//    .location("us-central8")
//    .modelName("gemini-pro")
//
//private val models = mutableMapOf(
//    //Model.Gemini to geminiModel,
//    Model.OpenAi to openAiModel,
//    Model.MistralAi to mistralModel
//)
//fun chatMemory() {
//    val model = models[uiState.value.selectedModel]
//    // val prompt = SystemMessage.from("I'm the king of the UK talk to me with respect")
//    val userMessage = UserMessage.from(inputText)
//
//    val chatMemory = MessageWindowChatMemory.withMaxMessages(10)
//    val chain = ConversationalChain.builder()
//        .chatLanguageModel(models[uiState.value.selectedModel])
//        .chatMemory(chatMemory)
//        .build()
//
//    if (previousQuestion.isNotEmpty()) {
//        chain.execute(previousQuestion)
//    }
//    val response = chain?.execute(/*prompt, */inputText)
//    //val response = model?.generate(/*prompt, */inputText)
//
//    response?.let {
//        previousQuestion = it
//        _uiState.update { stats -> stats.copy(loadingState = LoadingState.Success(it)) }
//    }
//}