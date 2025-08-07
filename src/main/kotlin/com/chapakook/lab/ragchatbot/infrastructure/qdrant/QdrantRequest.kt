package com.chapakook.lab.ragchatbot.infrastructure.qdrant

class QdrantRequest {
    data class Search(
        val vector: List<Float>,
        val top: Int = 5,
        val withPayload: Boolean = true,
    )
    data class Save(val points: List<Qdrant.Point>)
}
