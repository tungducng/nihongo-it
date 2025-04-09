package com.example.japanesitlearning.chat

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.converter.ListOutputConverter
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/ai")
class ChatController(
    private val chatService: ChatService,
    private val chatClient: ChatClient,
) {

    @GetMapping("/ask-ai")
    fun generate(@RequestParam(value = "message") prompt: String): String {
        return chatService.getResponse(prompt)
    }

    @GetMapping("/ask-ai-options")
    fun getResponseOptions(@RequestParam(value = "message") prompt: String): String {
        return chatService.getResponseOptions(prompt)
    }

    @PostMapping("/vocabulary/list")
    fun getListVocabulary(
        @RequestParam category: String,
        @RequestParam(required = false, defaultValue = "N5") level: String,
    ): List<VocabularyInfo>? {
        val vocabularyList = chatClient.prompt()
            .user { u ->
                u.text("Vui lòng cung cấp 2 từ vựng tiếng Nhật thuộc chủ đề {category} ở cấp độ JLPT {level}. Bao gồm từ, cách đọc, ý nghĩa và câu ví dụ.")
                    .param("category", category)
                    .param("level", level)
            }
            .call()
            .entity(object : ParameterizedTypeReference<List<VocabularyInfo>>() {})

        return vocabularyList
    }

    // using ListOutputConverter
    @PostMapping("/list-output")
    fun getListResponse(@RequestParam category: String, year: String): MutableList<String>? {
        return chatClient.prompt()
            .user { u ->
                u.text("Please provide the names of 3 best books for the given {category} and the {year}")
                    .param("category", category)
                    .param("year", year)
            }
            .call()
            .entity(ListOutputConverter(DefaultConversionService()))
    }

    @PostMapping("/advisor")
    fun getResponseAdvisor(@RequestParam message: String): String? {
        return chatClient.prompt()
            .user(message)
            .call()
            .content()
    }

    // using MapOutputConverter
    @PostMapping("/map-output")
    fun getMapResponse(@RequestParam category: String, year: String): Map<String, Any>? {
        return chatClient.prompt()
            .user { u ->
                u.text(
                    """
                Please provide me best book for the given {category} and the {year}.
                Please do provide a summary of the book as well, the information should be 
                limited and not much in depth. The response should be in the JSON format 
                containing this information:
                category, book, year, review, author, summary
                Please remove ```json from the final output
                    """.trimIndent(),
                )
                    .param("category", category)
                    .param("year", year)
            }
            .call()
            .entity(object : ParameterizedTypeReference<Map<String, Any>>() {})
    }
}
