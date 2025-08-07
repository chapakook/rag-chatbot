package com.chapakook.lab.ragchatbot.domain.embedding

interface VectorRepository {
    fun search(query: Embedding.Vector, topK: Int = 5): List<Embedding.Chunk>
    fun save(chunks: List<Embedding.Chunk>)
}
