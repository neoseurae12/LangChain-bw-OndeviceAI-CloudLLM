package com.wonhui.demo.langchain_ondevice_cloudllm

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.output.Response
import java.io.File

class Gemma2ChatModel private constructor(context: Context) : ChatLanguageModel {
    private var llmInference: LlmInference

    private val modelExists: Boolean
        get() = File(MODEL_PATH).exists()

    init {
        if (!modelExists) {
            throw IllegalArgumentException("Model not found at path: $MODEL_PATH")
        }

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(MODEL_PATH)
            .setMaxTokens(1024)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    override fun generate(messages: MutableList<ChatMessage>): Response<AiMessage>
    {
        val prompt = ChatUiState.fullPrompt
        val gemmaPrompt = "$prompt\n<start_of_turn>model\n"

        Log.d(TAG, gemmaPrompt)

        val response = llmInference.generateResponse(gemmaPrompt)

        return Response.from(AiMessage.from(response))
    }

    companion object {

        private const val MODEL_PATH = "/data/local/tmp/llm/gemma-2b-it-gpu-int4.bin"
        private var instance: Gemma2ChatModel? = null

        fun getInstance(context: Context): Gemma2ChatModel {
            return if (instance != null) {
                instance!!
            } else {
                Gemma2ChatModel(context).also { instance = it }
            }
        }
    }
}