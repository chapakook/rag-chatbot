package com.chapakook.lab.ragchatbot.infrastructure.openai

import com.chapakook.lab.ragchatbot.support.error.CoreException
import com.chapakook.lab.ragchatbot.support.error.ErrorType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

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
         * - [x] OpenAI Key가 유요하지 않을 경우, `OPENAI_API_KEY_INVALID`을 반환한다.
         * - [x] OpenAI Key가 권한이 없는 경우 `OPENAI_API_KEY_FORBIDDEN`을 반환한다.
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
            val exception = assertThrows<CoreException> { openAiEmbeddingClient.embed(invalidApiKey, question) }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_API_KEY_INVALID) },
            )
        }

        @Test
        fun `bad - OpenAI Key가 권한이 없는 경우, "OPENAI_API_KEY_FORBIDDEN"을 반환한다`() {
            // arrange
            val forbiddenApiKey = "forbidden_api_key"
            val question = "RAG는 무엇인가요?"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $forbiddenApiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                HttpHeaders.EMPTY,
                "API Key forbidden".toByteArray(),
                null,
            )

            // act
            val exception = assertThrows<CoreException> { openAiEmbeddingClient.embed(forbiddenApiKey, question) }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_API_KEY_FORBIDDEN) },
            )
        }

        @Test
        fun `bad - 요청 간격이 너무 빠른 겨우 "OPENAI_API_RATE_LIMIT"을 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "RAG는 무엇인가요?"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                HttpHeaders.EMPTY,
                "API Key too many requests".toByteArray(),
                null,
            )

            // act
            val exception = assertThrows<CoreException> { openAiEmbeddingClient.embed(apiKey, question) }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_API_TOO_MANY_REQUESTS) },
            )
        }

        @Test
        fun `bad - 요청 길이 초과시 input 길이가 너무 길면 "OPENAI_API_REQUEST_TOO_LONG"을 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "too long question that exceeds the maximum allowed length for OpenAI embeddings request"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.REQUEST_URI_TOO_LONG.value(),
                "Request URI Too Long",
                HttpHeaders.EMPTY,
                "API Key too many requests".toByteArray(),
                null,
            )

            // act
            val exception = assertThrows<CoreException> { openAiEmbeddingClient.embed(apiKey, question) }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_API_REQUEST_TOO_LONG) },
            )
        }

        @Test
        fun `good - 정상 요청시 1536 길이의 벡터를 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "RAG는 무엇인가요?"
            val data = List(1536) { it.toFloat() }
            val response = OpenAiResponse.Embedding(
                data = listOf(
                    OpenAiResponse.Embedding.Data(
                        embedding = data,
                        index = 0,
                    ),
                ),
            )

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } returns Mono.just(response)

            // act
            val result = openAiEmbeddingClient.embed(apiKey, question)

            // assert
            assertAll(
                { assertThat(result).isNotNull },
                { assertThat(result.data).hasSize(1536) },
                { assertThat(result.data).isEqualTo(data) },
            )
        }
    }
}
