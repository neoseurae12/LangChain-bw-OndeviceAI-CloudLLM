package com.wonhui.demo.langchain_ondevice_cloudllm

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface LoadingState {
    data object Initial : LoadingState
    data object Loading : LoadingState
    data class Success(val outputText: String) : LoadingState
    data class Error(val errorMessage: String) : LoadingState
}