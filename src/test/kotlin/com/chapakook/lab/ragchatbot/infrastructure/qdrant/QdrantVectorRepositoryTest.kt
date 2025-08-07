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
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@DisplayName("Qdart Vector Repository Tests")
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
         * - [x] topK 값이 0이하 경우 `QDRANT_BAD_REQUEST`를 반환한다.
         * - [x] 정상 요청시 관련된 `Chunk` 리스트를 반환한다.
         */

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
         * - [x] 정상적으로 `Qdrant`에 벡터 데이터를 저장한다.
         */

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
