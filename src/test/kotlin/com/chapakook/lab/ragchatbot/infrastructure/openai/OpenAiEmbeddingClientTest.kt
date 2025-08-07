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
import java.nio.charset.StandardCharsets

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
         * - [x] 유효하지 않은 API 키는 `OPENAI_API_KEY_INVALID` 반환한다.
         * - [x] 권한 없는 API 키는 `OPENAI_API_KEY_FORBIDDEN` 반환한다.
         * - [x] 요청이 너무 빠르면 `OPENAI_API_TOO_MANY_REQUESTS` 반환한다.
         * - [ ] 요청 길이가 너무 길면 `OPENAI_CONTEXT_LENGTH_EXCEEDED` 반환한다.
         * - [ ] 요청 사용량 초과 시 `OPENAI_QUOTA_EXCEEDED` 반환한다.
         * - [x] 서버 오류는 `OPENAI_INTERNAL_SERVER_ERROR` 반환한다.
         * - [ ] 모델 이름이 잘못되면 `OPENAI_MODEL_NOT_FOUND` 반환한다.
         * - [x] 정상 요청시 1536 길이의 벡터를 반환한다.
         */
        @Test
        fun `bad - 유효하지 않은 API 키는 "OPENAI_API_KEY_INVALID" 반환한다`() {
            // arrange
            val invalidApiKey = "invelid_api_key"
            val question = "RAG는 무엇인가요?"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $invalidApiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec

            val errorJson = """
                {
                  "error": {
                    "message": "Incorrect API key provided.",
                    "type": "invalid_request_error",
                    "param": null,
                    "code": "invalid_api_key"
                  }
                }
            """.trimIndent()

            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                HttpHeaders.EMPTY,
                errorJson.toByteArray(),
                StandardCharsets.UTF_8,
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
        fun `bad - 권한 없는 API 키는 "OPENAI_API_KEY_FORBIDDEN" 반환한다`() {
            // arrange
            val forbiddenApiKey = "forbidden_api_key"
            val question = "RAG는 무엇인가요?"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $forbiddenApiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec

            val errorJson = """
                {
                  "error": {
                    "message": "You lack the required permissions to use this API key.",
                    "type": "invalid_request_error",
                    "param": null,
                    "code": "organization_disabled"
                  }
                }

            """.trimIndent()
            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                HttpHeaders.EMPTY,
                errorJson.toByteArray(),
                StandardCharsets.UTF_8,
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
        fun `bad - 요청이 너무 빠르면 "OPENAI_API_TOO_MANY_REQUESTS" 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "RAG는 무엇인가요?"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec

            val errorJson = """
                {
                  "error": {
                    "message": "You are sending requests too quickly. Please slow down.",
                    "type": "rate_limit_error",
                    "param": null,
                    "code": "rate_limit_exceeded"
                  }
                }
            """.trimIndent()

            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                HttpHeaders.EMPTY,
                errorJson.toByteArray(),
                StandardCharsets.UTF_8,
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
        fun `bad - 요청 길이가 너무 길면 "OPENAI_CONTEXT_LENGTH_EXCEEDED" 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "too long question that exceeds the maximum allowed length for OpenAI embeddings request".repeat(10000)

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec

            val errorJson = """
                {
                  "error": {
                    "message": "The input is too long.",
                    "type": "invalid_request_error",
                    "param": "input",
                    "code": "context_length_exceeded"
                  }
                }
            """.trimIndent()

            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                HttpHeaders.EMPTY,
                errorJson.toByteArray(),
                StandardCharsets.UTF_8,
            )

            // act
            val exception = assertThrows<CoreException> { openAiEmbeddingClient.embed(apiKey, question) }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_CONTEXT_LENGTH_EXCEEDED) },
            )
        }

        @Test
        fun `bad - 요청 사용량 초과 시 "OPENAI_QUOTA_EXCEEDED" 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "test question"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec

            val errorJson = """
                {
                  "error": {
                    "message": "You exceeded your current quota.",
                    "type": "insufficient_quota",
                    "code": "quota_exceeded"
                  }
                }
            """.trimIndent()

            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                HttpHeaders.EMPTY,
                errorJson.toByteArray(),
                StandardCharsets.UTF_8,
            )

            // act
            val exception = assertThrows<CoreException> {
                openAiEmbeddingClient.embed(apiKey, question)
            }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_QUOTA_EXCEEDED) },
            )
        }

        @Test
        fun `bad - 서버 오류는 "OPENAI_INTERNAL_SERVER_ERROR" 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "test question"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec

            val errorJson = """
                {
                  "error": {
                    "message": "The server had an error while processing your request.",
                    "type": "server_error",
                    "param": null,
                    "code": null
                  }
                }
            """.trimIndent()

            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                HttpHeaders.EMPTY,
                errorJson.toByteArray(),
                StandardCharsets.UTF_8,
            )

            // act
            val exception = assertThrows<CoreException> {
                openAiEmbeddingClient.embed(apiKey, question)
            }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_API_INTERNAL_SERVER_ERROR) },
            )
        }

        @Test
        fun `bad - 모델 이름이 잘못되면 "OPENAI_MODEL_NOT_FOUND" 반환한다`() {
            // arrange
            val apiKey = "api_key"
            val question = "test question"

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/v1/embeddings") } returns requestBodySpec
            every { requestBodySpec.header("Authorization", "Bearer $apiKey") } returns requestBodySpec
            every { requestBodySpec.bodyValue(any<OpenAiRequest.Embedding.V1>()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec

            val errorJson = """
                {
                  "error": {
                    "message": "The model `text-embedding-xyz` does not exist.",
                    "type": "invalid_request_error",
                    "param": "model",
                    "code": "model_not_found"
                  }
                }
            """.trimIndent()

            every {
                responseSpec.bodyToMono(OpenAiResponse.Embedding::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                HttpHeaders.EMPTY,
                errorJson.toByteArray(),
                StandardCharsets.UTF_8,
            )

            // act
            val exception = assertThrows<CoreException> {
                openAiEmbeddingClient.embed(apiKey, question)
            }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.OPENAI_MODEL_NOT_FOUND) },
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
