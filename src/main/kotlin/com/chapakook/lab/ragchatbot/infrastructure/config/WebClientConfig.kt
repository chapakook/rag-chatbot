package com.chapakook.lab.ragchatbot.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean("openAiWebClient")
    fun openAiWebClient(): WebClient = WebClient.builder()
        .baseUrl("https://api.openai.com")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    @Bean("qdartWebClient")
    fun qdartWebclient(
        @Value("\${qdrant.base-url}") baseUrl: String,
    ): WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()
}
