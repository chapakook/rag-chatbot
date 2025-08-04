package com.chapakook.lab.ragchatbot.infrastructure.openai

import com.chapakook.lab.ragchatbot.domain.embedding.Embedding
import com.chapakook.lab.ragchatbot.domain.embedding.EmbeddingClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class OpenAiEmbeddingClient(
    private val webClient: WebClient
): EmbeddingClient {
    override fun embed(apiKey: String, question: String): Embedding {
        val request = OpenAiRequest.Embedding.V1( input = question)

        val webClient = webClient.mutate()
            .baseUrl("https://api.openai.com/v1/embeddings")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .build()

        val response = webClient.post()
            .bodyValue(request)
            .retrieve()
            .bodyToMono(OpenAiResponse.Embedding::class.java)
            .block() ?: throw IllegalStateException("OpenAI 응답이 null입니다")

        return response.data.first().embedding.let { Embedding(it) }
    }
}
