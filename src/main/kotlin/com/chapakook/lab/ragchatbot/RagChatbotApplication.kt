package com.chapakook.lab.ragchatbot

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.TimeZone

@SpringBootApplication
class RagChatbotApplication {
     @PostConstruct
     fun started() {
         // set timezone
         TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
     }
}

fun main(args: Array<String>) {
    runApplication<RagChatbotApplication>(*args)
}
