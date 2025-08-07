package com.chapakook.lab.ragchatbot.domain.embedding

interface EmbeddingClient {
    fun embed(apiKey: String, question: String): Embedding.Vector
}
