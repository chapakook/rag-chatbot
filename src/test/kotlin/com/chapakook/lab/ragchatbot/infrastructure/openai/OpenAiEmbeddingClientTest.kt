package com.chapakook.lab.ragchatbot.infrastructure.openai

import com.chapakook.lab.ragchatbot.support.error.CoreException
import com.chapakook.lab.ragchatbot.support.error.ErrorType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

class OpenAiEmbeddingClientTest {
    private val webClient: WebClient = mockk()
    private val requestBodyUriSpec: WebClient.RequestBodyUriSpec = mockk()
    private val requestBodySpec: WebClient.RequestBodySpec = mockk()
    private val requestHeadersSpec: WebClient.RequestHeadersSpec<*> = mockk()
    private val responseSpec: WebClient.ResponseSpec = mockk()

    private val openAiEmbeddingClient: OpenAiEmbeddingClient = OpenAiEmbeddingClient(webClient)

    @DisplayName("OpenAI Embedding Client Tests")
    @Nested
    inner class Embed {
        /**
         * - [ ] OpenAI Key가 유요하지 않을 경우, `OPENAI_API_KEY_INVALID`을 반환한다.
         * - [ ] OpenAI Key가 권한이 없는 경우 `OPENAI_API_KEY_FORBIDDEN`을 반환한다.
         * - [ ] 요청 간격이 너무 빠른 경우 `OPENAI_API_RATE_LIMIT`을 반환한다.
         * - [ ] 요청 길이 초과시 input 길이가 너무 길면 `OPENAI_API_REQUEST_TOO_LONG`을 반환한다.
         * - [ ] 정상 요청시 1536 길이의 벡터를 반환한다.
         */
        @Test
        fun `bad - OpenAI Key가 유효하지 않을 경우, "OPENAI_API_KEY_INVALID"을 반환한다`() {
            // arrange
            val invalidApiKey = "invelid_api_key"
            val question = "RAG는 무엇인가요?"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $invalidApiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                HttpHeaders.EMPTY,
                "API Key invalid".toByteArray(),
                null,
            )

            // act
            val exception = assertThrows<CoreException> {
                openAiEmbeddingClient.embed(invalidApiKey, question)
            }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_API_KEY_INVALID) },
            )
        }
    }
}
