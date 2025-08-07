package com.chapakook.lab.ragchatbot.infrastructure.qdrant

class Qdrant {
    data class Payload(
        val text: String,
        val documentId: String,
        val url: String? = null,
    )
    data class Point(
        val id: String,
        val vector: List<Float>,
        val payload: Payload,
    )
}
