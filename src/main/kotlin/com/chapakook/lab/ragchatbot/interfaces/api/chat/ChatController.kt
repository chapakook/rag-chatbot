package com.chapakook.lab.ragchatbot.interfaces.api.chat

import com.chapakook.lab.ragchatbot.interfaces.api.ApiResponse
import com.chapakook.lab.ragchatbot.support.error.CoreException
import com.chapakook.lab.ragchatbot.support.error.ErrorType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/chat")
class ChatController : ChatApiSpec.V1 {
    @PostMapping
    override fun chat(
        @RequestHeader("X-OPENAI-KEY") apikey: String,
        @RequestBody requestBody: ChatRequest.Chat.V1,
    ): ApiResponse<ChatResponse.Chat.V1> {
        if (requestBody.question.isBlank()) throw CoreException(ErrorType.BAD_REQUEST, "질문은 필수 입력값입니다.")
        return ChatResponse.Chat.V1("응답입니다").let { ApiResponse.success(it) }
    }
}
