package com.chapakook.lab.ragchatbot.infrastructure.openai

class OpenAiResponse {
    data class Embedding(val data: List<Data>){
        data class Data(val embedding: List<Float>, val index: Int)
    }
}
