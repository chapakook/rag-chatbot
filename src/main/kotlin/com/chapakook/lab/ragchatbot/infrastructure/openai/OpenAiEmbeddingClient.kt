package com.chapakook.lab.ragchatbot.infrastructure.openai

import com.chapakook.lab.ragchatbot.domain.embedding.Embedding
import com.chapakook.lab.ragchatbot.domain.embedding.EmbeddingClient
import com.chapakook.lab.ragchatbot.support.error.CoreException
import com.chapakook.lab.ragchatbot.support.error.ErrorType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class OpenAiEmbeddingClient(
    @Qualifier("openAiWebClient")
    private val webClient: WebClient,
) : EmbeddingClient {
    override fun embed(apiKey: String, question: String): Embedding.Vector = try {
        webClient.post()
            .uri("/v1/embeddings")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .bodyValue(OpenAiRequest.Embedding.V1(input = question))
            .retrieve()
            .bodyToMono(OpenAiResponse.Embedding::class.java)
            .block()
            ?.data
            ?.firstOrNull()
            ?.embedding
            ?.let { Embedding.Vector(it) }
            ?: throw IllegalStateException("OpenAI 응답이 null입니다")
    } catch (ex: WebClientResponseException) {
        throw handleOpenAiException(ex)
    }

    private fun extractErrorCode(json: String): String? = try {
        jacksonObjectMapper().readTree(json)
            ?.get("error")
            ?.get("code")
            ?.asText()
    } catch (e: Exception) {
        null
    }

    private fun handleOpenAiException(ex: WebClientResponseException): CoreException = when (ex) {
        is WebClientResponseException.Unauthorized -> CoreException(ErrorType.OPENAI_API_KEY_INVALID)
        is WebClientResponseException.Forbidden -> CoreException(ErrorType.OPENAI_API_KEY_FORBIDDEN)
        is WebClientResponseException.TooManyRequests -> {
            when (extractErrorCode(ex.responseBodyAsString)) {
                "rate_limit_exceeded" -> CoreException(ErrorType.OPENAI_API_TOO_MANY_REQUESTS)
                "quota_exceeded" -> CoreException(ErrorType.OPENAI_QUOTA_EXCEEDED)
                else -> CoreException(ErrorType.OPENAI_API_UNKNOWN_ERROR)
            }
        }
        is WebClientResponseException.BadRequest -> {
            when (extractErrorCode(ex.responseBodyAsString)) {
                "context_length_exceeded" -> CoreException(ErrorType.OPENAI_CONTEXT_LENGTH_EXCEEDED)
                else -> CoreException(ErrorType.OPENAI_API_UNKNOWN_ERROR)
            }
        }
        is WebClientResponseException.NotFound -> CoreException(ErrorType.OPENAI_MODEL_NOT_FOUND)
        is WebClientResponseException.InternalServerError -> CoreException(ErrorType.OPENAI_API_INTERNAL_SERVER_ERROR)
        else -> CoreException(ErrorType.OPENAI_API_UNKNOWN_ERROR)
    }
}
