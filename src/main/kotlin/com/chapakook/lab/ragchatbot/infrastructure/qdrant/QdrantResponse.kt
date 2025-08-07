package com.chapakook.lab.ragchatbot.infrastructure.qdrant

class QdrantResponse {
    data class Search(val results: List<Result>) {
        data class Result(
            val id: String,
            val score: Float,
            val payload: Qdrant.Payload,
        )
    }
}
