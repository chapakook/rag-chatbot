package com.chapakook.lab.ragchatbot.infrastructure.qdrant

import com.chapakook.lab.ragchatbot.domain.embedding.Embedding
import com.chapakook.lab.ragchatbot.support.error.CoreException
import com.chapakook.lab.ragchatbot.support.error.ErrorType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@DisplayName("Qdart Vector Repository Unit Tests")
class QdrantVectorRepositoryTest {
    private val webClient: WebClient = mockk()
    private val requestBodyUriSpec: WebClient.RequestBodyUriSpec = mockk()
    private val requestBodySpec: WebClient.RequestBodySpec = mockk()
    private val requestHeadersSpec: WebClient.RequestHeadersSpec<*> = mockk()
    private val responseSpec: WebClient.ResponseSpec = mockk()
    private val collectionName = "test-collection"
    private val qdrantVectorRepository: QdrantVectorRepository = QdrantVectorRepository(webClient, collectionName)

    @DisplayName("Search Tests")
    @Nested
    inner class Search {
        /**
         * - [ ] Qdrant 응답이 비정상 JSON이면 `QDRANT_UNKNOWN_ERROR` 반환한다.
         * - [ ] Qdrant 검색 중 타임아웃 발생 시 `QDRANT_TIMEOUT` 반환한다.
         * - [ ] Qdrant 서버 오류로 인해 저장이 불가능 하면 `QDRANT_INTERNAL_SERVER_ERROR`을 반환한다.
         * - [x] topK 값이 0이하 경우 `QDRANT_BAD_REQUEST`를 반환한다.
         * - [ ] Qdrant 응답이 null이면 빈 리스트를 반환한다.
         * - [x] 정상 요청시 관련된 `Chunk` 리스트를 반환한다.
         */

        @Test
        fun `bad - Qdrant 응답이 비정상 JSON이면 "QDRANT_UNKNOWN_ERROR" 반환한다`() {
            // arrange
            val query = Embedding.Vector(List(5) { it.toFloat() })
            val topK = 5

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/collections/$collectionName/points/search") } returns requestBodySpec
            every { requestBodySpec.contentType(any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(QdrantResponse.Search::class.java)
            } throws RuntimeException("JSON Parse Error")

            // act
            val exception = assertThrows<CoreException> {
                qdrantVectorRepository.search(query, topK)
            }

            // assert
            assertThat(exception.errorType).isEqualTo(ErrorType.QDRANT_UNKNOWN_ERROR)
        }

        @Test
        fun `bad - Qdrant 검색 중 타임아웃 발생 시 "QDRANT_TIMEOUT" 반환한다`() {
            // arrange
            val query = Embedding.Vector(List(5) { it.toFloat() })
            val topK = 5

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/collections/$collectionName/points/search") } returns requestBodySpec
            every { requestBodySpec.contentType(any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(QdrantResponse.Search::class.java)
            } throws java.net.SocketTimeoutException("timeout")

            // act
            val exception = assertThrows<CoreException> {
                qdrantVectorRepository.search(query, topK)
            }

            // assert
            assertThat(exception.errorType).isEqualTo(ErrorType.QDRANT_TIMEOUT)
        }

        @Test
        fun `bad - Qdrant 서버 오류로 인해 저장이 불가능 하면 "QDRANT_INTERNAL_SERVER_ERROR"을 반환한다`() {
            // arrange
            val query = Embedding.Vector(List(5) { it.toFloat() })
            val topK = 1

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/collections/$collectionName/points/search") } returns requestBodySpec
            every { requestBodySpec.contentType(any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(QdrantResponse.Search::class.java)
            } throws WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "".toByteArray(),
                null,
            )

            // act
            val exception = assertThrows<CoreException> {
                qdrantVectorRepository.search(query, topK)
            }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.QDRANT_INTERNAL_SERVER_ERROR) },
            )
        }

        @Test
        fun `bad - topK 값이 0이하 경우 "QDRANT_BAD_REQUEST"를 반환한다`() {
            // arrange
            val query = Embedding.Vector(List(5) { it.toFloat() })
            val topK = 0

            // act
            val exception = assertThrows<CoreException> {
                qdrantVectorRepository.search(query, topK)
            }

            // assert
            assertAll(
                { assertThat(exception).isInstanceOf(CoreException::class.java) },
                { assertThat(exception.errorType).isEqualTo(ErrorType.QDRANT_BAD_REQUEST) },
            )
        }

        @Test
        fun `happy - Qdrant 응답이 null이면 빈 리스트를 반환한다`() {
            // arrange
            val query = Embedding.Vector(List(5) { it.toFloat() })
            val topK = 1

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/collections/$collectionName/points/search") } returns requestBodySpec
            every { requestBodySpec.contentType(any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.bodyToMono(QdrantResponse.Search::class.java)
            } returns Mono.empty()

            // act
            val result = qdrantVectorRepository.search(query, topK)

            // assert
            assertThat(result).isEmpty()
        }

        @Test
        fun `happy - 정상 요청시 관련된 "Chunk" 리스트를 반환한다`() {
            // arrange
            val query = Embedding.Vector(List(5) { it.toFloat() })
            val topK = 1

            val qdrantResponse = QdrantResponse.Search(
                results = listOf(
                    QdrantResponse.Search.Result(
                        id = "chunk-1",
                        score = 0.99f,
                        payload = Qdrant.Payload(
                            text = "hello",
                            documentId = "doc-1",
                            url = "http://example.com",
                        ),
                    ),
                ),
            )

            every { webClient.post() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri("/collections/$collectionName/points/search") } returns requestBodySpec
            every { requestBodySpec.contentType(MediaType.APPLICATION_JSON) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono(QdrantResponse.Search::class.java) } returns Mono.just(qdrantResponse)

            // act
            val results = qdrantVectorRepository.search(query, topK)

            // assert
            assertAll(
                { assertThat(results).hasSize(1) },
                {
                    assertThat(results.first()).satisfies({
                        assertThat(it.id).isEqualTo("chunk-1")
                        assertThat(it.text).isEqualTo("hello")
                        assertThat(it.documentId).isEqualTo("doc-1")
                        assertThat(it.url).isEqualTo("http://example.com")
                        assertThat(it.vector).isEqualTo(query)
                    })
                },
            )
        }
    }

    @DisplayName("Save Tests")
    @Nested
    inner class Save {
        /**
         * - [ ] Qdrant 서버 오류로 인해 저장이 불가능 하면 `QDRANT_INTERNAL_SERVER_ERROR`을 반환한다.
         * - [x] 정상적으로 `Qdrant`에 벡터 데이터를 저장한다.
         */

        @Test
        fun `bad - Qdrant 서버 오류로 인해 저장이 불가능 하면 "QDRANT_INTERNAL_SERVER_ERROR"을 반환한다`() {
            // arrange
            val chunk = Embedding.Chunk(
                id = "sample-chunk-id",
                text = "This is a sample chunk for testing.",
                documentId = "sample-doc-id",
                url = "http://example.com",
                vector = Embedding.Vector(List(5) { it.toFloat() }),
            )
            every { webClient.put() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri(eq("/collections/$collectionName/points?wait=true")) } returns requestBodySpec
            every { requestBodySpec.contentType(any()) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every {
                responseSpec.toBodilessEntity()
            } throws WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "".toByteArray(),
                null,
            )

            // act
            val exception = assertThrows<CoreException> {
                qdrantVectorRepository.save(listOf(chunk))
            }

            // assert
            assertAll(
                { assertThat(exception.errorType).isEqualTo(ErrorType.QDRANT_INTERNAL_SERVER_ERROR) },
            )
        }

        @Test
        fun `happy - 정상적으로 "Qdrant"에 벡터 데이터를 저장한다`() {
            // arrange
            val chunks = listOf(
                Embedding.Chunk(
                    id = "chunk-1",
                    text = "hello world",
                    documentId = "doc-1",
                    url = "http://example.com",
                    vector = Embedding.Vector(List(5) { it.toFloat() }),
                ),
            )

            every { webClient.put() } returns requestBodyUriSpec
            every { requestBodyUriSpec.uri(eq("/collections/$collectionName/points?wait=true")) } returns requestBodySpec
            every { requestBodySpec.contentType(MediaType.APPLICATION_JSON) } returns requestBodySpec
            every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.toBodilessEntity() } returns Mono.empty()

            // act
            qdrantVectorRepository.save(chunks)

            // assert
            verify(exactly = 1) { webClient.put() }
            verify { requestBodyUriSpec.uri(eq("/collections/$collectionName/points?wait=true")) }
        }
    }
}
