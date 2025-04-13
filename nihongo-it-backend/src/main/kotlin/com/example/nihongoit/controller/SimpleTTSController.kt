package com.example.nihongoit.controller

import org.springframework.ai.openai.OpenAiAudioSpeechModel
import org.springframework.ai.openai.OpenAiAudioSpeechOptions
import org.springframework.ai.openai.api.OpenAiAudioApi
import org.springframework.ai.openai.audio.speech.SpeechPrompt
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/tts")
class TTSController(
    private val openAiAudioSpeechModel: OpenAiAudioSpeechModel,
) {

    @PostMapping("/generate")
    fun generateSpeech(
        @RequestBody text: String,
        @RequestHeader(value = "X-Speech-Speed", required = false, defaultValue = "1.0") speedStr: String,
        @RequestHeader(value = "X-Content-Is-Example", required = false, defaultValue = "false") isExample: Boolean
    ): ResponseEntity<ByteArray> {
        // Parse speed parameter
        val speed = try {
            speedStr.toFloat().coerceIn(0.25f, 4.0f) // Ensure speed is within OpenAI's allowed range
        } catch (e: Exception) {
            1.0f // Default to 1.0 if parsing fails
        }

        // Note: OpenAiAudioTranscriptionOptions is for Speech-to-Text (not Text-to-Speech)
        // It cannot be directly used with the SpeechPrompt or OpenAiAudioSpeechModel
        // Instead, we'll incorporate Japanese language handling directly in the prompt
        
        // Add Japanese language hints based on content type
//        val japaneseText = if (isExample) {
//            """
//            # 日本語の例文を発音してください。
//            # 自然な抑揚で、明確に発音してください。
//
//            $text
//            """.trimIndent()
//        } else {
//            """
//            # 日本語の単語を発音してください。
//            # ゆっくりと、明確に発音してください。
//
//            $text
//            """.trimIndent()
//        }
        
        val options = OpenAiAudioSpeechOptions.builder()
            .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA) // NOVA has the best Japanese pronunciation
            .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
            .model("gpt-4o-mini-tts")
            .speed(speed)
            .build()

        val response = try {
            openAiAudioSpeechModel.call(SpeechPrompt(text, options))
        } catch (e: Exception) {
            throw RuntimeException("TTS generation failed: ${e.message}")
        }

        // Validate audio output
        require(response.result.output.isNotEmpty()) {
            "OpenAI returned empty audio data"
        }

        return ResponseEntity.ok()
            .contentType(MediaType("audio", "mpeg"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=speech.mp3")
            .body(response.result.output)
    }
}
