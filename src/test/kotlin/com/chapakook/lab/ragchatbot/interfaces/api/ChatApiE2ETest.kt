package com.chapakook.lab.ragchatbot.interfaces.api

import com.chapakook.lab.ragchatbot.interfaces.api.chat.ChatRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ChatApiE2ETest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @DisplayName("POST Chat API E2E Test /api/v1/chat")
    @Nested
    inner class Chat {
        /**
         * - [x] `X-OPENAI-KEY` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.
         * - [x] 질문이 비어있으면 `400 Bad Request` 응답을 받는다.
         * - [x] 질문을 보내면 정상적으로 답변이 반환된다.
         */

        @Test
        fun `bad - "X-OPENAI-KEY" 헤더가 없을 경우, "400 Bad Request" 응답을 반환한다`() {
            // arrange
            val baseUri = "/api/v1/chat"
            val headers = HttpHeaders().apply { set("Content-Type", MediaType.APPLICATION_JSON_VALUE) }
            val requestBody = ChatRequest.Chat.V1("RAG는 무엇인가요?")

            // act
            val response = webTestClient.post()
                .uri(baseUri)
                .headers { it.addAll(headers) }
                .bodyValue(requestBody)
                .exchange()
                .expectBody(String::class.java)
                .returnResult()

            // assert
            assertAll(
                { assert(response.status.is4xxClientError) },
                { assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST) },
            )
        }

        @Test
        fun `bad - 질문이 비어있으면 "400 Bad Request" 응답을 받는다`() {
            // arrange
            val baseUri = "/api/v1/chat"
            val headers = HttpHeaders().apply {
                set("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                set("X-OPENAI-KEY", "test-openai-key")
            }
            val requestBody = ChatRequest.Chat.V1("")

            // act
            val response = webTestClient.post()
                .uri(baseUri)
                .headers { it.addAll(headers) }
                .bodyValue(requestBody)
                .exchange()
                .expectBody(String::class.java)
                .returnResult()

            // assert
            assertAll(
                { assert(response.status.is4xxClientError) },
                { assertThat(response.status).isEqualTo(HttpStatus.BAD_REQUEST) },
            )
        }

        @Test
        fun `good - 질문을 보내면 정상적으로 답변이 반환된다`() {
            // arrange
            val baseUri = "/api/v1/chat"
            val headers = HttpHeaders().apply {
                set("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                set("X-OPENAI-KEY", "test-openai-key")
            }
            val requestBody = ChatRequest.Chat.V1("RAG는 무엇인가요?")

            // act
            val response = webTestClient.post()
                .uri(baseUri)
                .headers { it.addAll(headers) }
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java)
                .returnResult()

            // assert
            assertAll(
                { assert(response.status.is2xxSuccessful) },
                { assertThat(response.responseBody).isNotEmpty() },
            )
        }
    }
}
