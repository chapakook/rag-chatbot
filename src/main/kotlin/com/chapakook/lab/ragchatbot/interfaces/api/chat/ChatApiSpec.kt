package com.chapakook.lab.ragchatbot.interfaces.api.chat

import com.chapakook.lab.ragchatbot.interfaces.api.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Chat V1 API", description = "RAG Chatbot Chat API 입니다.")
interface ChatApiSpec {
    interface V1 {
        @Operation(summary = "RAG Chatbot Chat", description = "RAG Chatbot Chat을 요청합니다.")
        fun chat(
            @Schema(name = "X-OPENAI-KEY", description = "검색하는 사용자 OpenAI accessKey") apikey: String,
            @Schema(name = "검색할 내용", description = "검색할 내용 text") requestBody: ChatRequest.Chat.V1,
        ): ApiResponse<ChatResponse.Chat.V1>
    }
}
