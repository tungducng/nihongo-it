package com.example.japanesitlearning.controller

import org.springframework.ai.openai.OpenAiAudioSpeechModel
import org.springframework.ai.openai.OpenAiAudioSpeechOptions
import org.springframework.ai.openai.api.OpenAiAudioApi
import org.springframework.ai.openai.audio.speech.SpeechPrompt
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tts")
class TTSController(
    private val openAiAudioSpeechModel: OpenAiAudioSpeechModel
) {

    @PostMapping("/generate")
    fun generateSpeech(@RequestBody text: String): ResponseEntity<ByteArray> {
        val options = OpenAiAudioSpeechOptions.builder()
            .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA)
            .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
            .model("tts-1")
            .speed(1.0f)
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