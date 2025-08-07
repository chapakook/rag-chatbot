package com.chapakook.lab.ragchatbot.domain.embedding

class Embedding {
    @JvmInline
    value class Vector(val data: List<Float>)
    data class Chunk(
        val id: String,
        val text: String,
        val documentId: String,
        val url: String? = null,
        val vector: Vector,
    )
}
