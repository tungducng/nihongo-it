package com.example.japanesitlearning.chat

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatModel: ChatModel,
) {
    fun getResponse(prompt: String): String {
        return chatModel.call(prompt)
    }

    fun getResponseOptions(prompt: String): String {
        val response: ChatResponse = chatModel.call(
            Prompt(
                prompt,
                OpenAiChatOptions.builder()
                    .model("gpt-4o")
                    .temperature(0.4)
                    .build(),
            ),
        )
        return response.result.output.text
    }
}
