package com.chapakook.lab.ragchatbot.infrastructure.openai

import com.chapakook.lab.ragchatbot.domain.embedding.Embedding
import com.chapakook.lab.ragchatbot.domain.embedding.EmbeddingClient
import com.chapakook.lab.ragchatbot.support.error.CoreException
import com.chapakook.lab.ragchatbot.support.error.ErrorType
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class OpenAiEmbeddingClient(
    private val webClient: WebClient,
) : EmbeddingClient {
    override fun embed(apiKey: String, question: String): Embedding {
        val request = OpenAiRequest.Embedding.V1(input = question)
        try {
            val response = webClient.post()
                .uri("/v1/embeddings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiResponse.Embedding::class.java)
                .block() ?: throw IllegalStateException("OpenAI 응답이 null입니다")

            return response.data.first().embedding.let { Embedding(it) }
        } catch (ex: WebClientResponseException.Unauthorized) {
            throw CoreException(ErrorType.OPENAI_API_KEY_INVALID, "OpenAI API Key가 유효하지 않습니다.")
        } catch (ex: WebClientResponseException.Forbidden) {
            throw CoreException(ErrorType.OPENAI_API_KEY_FORBIDDEN, "OpenAI API Key가 권한이 없습니다.")
        }
    }
}
