package com.chapakook.lab.ragchatbot.infrastructure.qdrant

import com.chapakook.lab.ragchatbot.domain.embedding.Embedding
import com.chapakook.lab.ragchatbot.domain.embedding.VectorRepository
import com.chapakook.lab.ragchatbot.support.error.CoreException
import com.chapakook.lab.ragchatbot.support.error.ErrorType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class QdrantVectorRepository(
    @Qualifier("qdrantWebClient") private val webClient: WebClient,
    @Value("\${qdrant.collection-name}") private val collectionName: String,
) : VectorRepository {
    override fun search(query: Embedding.Vector, topK: Int): List<Embedding.Chunk> {
        if (topK <= 0) throw CoreException(ErrorType.QDRANT_BAD_REQUEST)
        return webClient.post()
            .uri("/collections/$collectionName/points/search")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                QdrantRequest.Search(
                    vector = query.data,
                    top = topK,
                    withPayload = true,
                ),
            )
            .retrieve()
            .bodyToMono(QdrantResponse.Search::class.java)
            .block()
            ?.results
            ?.map {
                it.payload
                    .let { payload ->
                        Embedding.Chunk(
                            id = it.id,
                            text = payload.text,
                            documentId = payload.documentId,
                            url = payload.url,
                            vector = query,
                        )
                    }
            }
            ?: emptyList()
    }

    override fun save(chunks: List<Embedding.Chunk>) {
        webClient.put()
            .uri("/collections/$collectionName/points?wait=true")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                    QdrantRequest.Save(
                    points = chunks.map {
                        Qdrant.Point(
                            id = it.id,
                            vector = it.vector.data,
                            payload = Qdrant.Payload(
                                text = it.text,
                                documentId = it.documentId,
                                url = it.url,
                            ),
                        )
                    },
                ),
            )
            .retrieve()
            .toBodilessEntity()
            .block()
    }
}
