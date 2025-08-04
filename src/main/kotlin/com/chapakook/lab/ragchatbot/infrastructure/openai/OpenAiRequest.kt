package com.chapakook.lab.ragchatbot.infrastructure.openai

class OpenAiRequest {
    class Embedding{
        data class V1(val model: String = "text-embedding-ada-002", val input: String)
    }
}
