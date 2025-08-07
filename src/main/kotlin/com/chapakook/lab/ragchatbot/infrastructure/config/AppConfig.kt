package com.chapakook.lab.ragchatbot.infrastructure.config

import com.chapakook.lab.ragchatbot.infrastructure.openai.OpenAiProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAiProperties::class)
class AppConfig
