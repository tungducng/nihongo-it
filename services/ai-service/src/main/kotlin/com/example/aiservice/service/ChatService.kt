package com.example.aiservice.service

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatModel: ChatModel,
) {
    @Value("\${ai-service.openai.default-model:gpt-4o}")
    private lateinit var defaultModel: String

    @Value("\${ai-service.openai.economy-model:gpt-3.5-turbo}")
    private lateinit var economyModel: String

    fun getResponse(prompt: String): String {
        return chatModel.call(prompt)
    }

    fun getResponseOptions(prompt: String): String {
        val response: ChatResponse = chatModel.call(
            Prompt(
                prompt,
                OpenAiChatOptions.builder()
                    .model(defaultModel)
                    .temperature(0.4)
                    .build(),
            ),
        )
        return response.result.output.text
    }
    
    fun getEconomyResponse(prompt: String): String {
        val response: ChatResponse = chatModel.call(
            Prompt(
                prompt,
                OpenAiChatOptions.builder()
                    .model(economyModel)
                    .temperature(0.4)
                    .build(),
            ),
        )
        return response.result.output.text
    }
} 