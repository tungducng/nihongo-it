package io.github.ndtung723.nihongoit.aiservice.controller

import org.slf4j.LoggerFactory
import org.springframework.ai.openai.OpenAiAudioSpeechModel
import org.springframework.ai.openai.OpenAiAudioSpeechOptions
import org.springframework.ai.openai.api.OpenAiAudioApi
import org.springframework.ai.openai.audio.speech.SpeechPrompt
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

@RestController
@RequestMapping("/api/v1/ai/tts")
class TTSController(
    private val openAiAudioSpeechModel: OpenAiAudioSpeechModel,
) {
    companion object {
        private const val SPEED_MIN = 0.25f
        private const val SPEED_MAX = 4.0f
        private const val SPEED_DEFAULT = 1.0f
        private const val MAX_TEXT_BYTES = 4096
    }

    private val logger = LoggerFactory.getLogger(TTSController::class.java)

    private val validContentTypes = setOf("vocabulary", "example", "conversation")
    private val defaultContentType = "vocabulary"

    @PostMapping("/generate")
    fun generateSpeech(
        @RequestBody text: String,
        @RequestHeader(value = "X-Speech-Speed", required = false, defaultValue = "1.0") speedStr: String,
        @RequestHeader(value = "X-Content-Type", required = false, defaultValue = "vocabulary") contentType: String,
        @RequestHeader(value = "X-Content-Language", required = false, defaultValue = "ja") language: String,
        @RequestHeader(value = "X-Save-Audio", required = false, defaultValue = "false") saveAudio: Boolean,
    ): ResponseEntity<ByteArray> {
        val validatedContentType = validateContentType(contentType)
        val speed = speedStr.toFloatOrNull()?.coerceIn(SPEED_MIN, SPEED_MAX) ?: SPEED_DEFAULT
        require(text.isNotBlank()) { "Text must not be blank" }
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_TEXT_BYTES) {
            "Text exceeds maximum length"
        }

        val options =
            OpenAiAudioSpeechOptions
                .builder()
                .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .model("gpt-4o-mini-tts")
                .speed(speed)
                .build()

        val response =
            try {
                openAiAudioSpeechModel.call(SpeechPrompt(text, options))
            } catch (e: Exception) {
                throw RuntimeException("TTS generation failed: ${e.message}", e)
            }

        require(response.result.output.isNotEmpty()) {
            "OpenAI returned empty audio data"
        }

        if (saveAudio) {
            saveGeneratedAudio(text, response.result.output, validatedContentType)
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType("audio", "mpeg"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=speech.mp3")
            .header("X-Content-Language", language)
            .header("X-Content-Type", validatedContentType)
            .body(response.result.output)
    }

    private fun saveGeneratedAudio(
        text: String,
        audioData: ByteArray,
        contentType: String,
    ) {
        try {
            val audioFile = resolveAudioPath(contentType, text) ?: return
            val directory = audioFile.parent
            if (!Files.exists(directory)) {
                Files.createDirectories(directory)
                logger.info("Created directory: {}", directory)
            }
            Files.write(audioFile, audioData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            logger.info("Saved generated audio to: {}", audioFile.toAbsolutePath())
        } catch (e: Exception) {
            logger.error("Failed to save generated audio", e)
        }
    }

    @GetMapping("/check")
    fun checkAudioExists(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "vocabulary") contentType: String,
    ): ResponseEntity<Map<String, Boolean>> {
        val audioFile = resolveAudioPath(validateContentType(contentType), text)
        val exists = audioFile != null && Files.exists(audioFile)
        return ResponseEntity.ok(mapOf("exists" to exists))
    }

    @GetMapping("/audio")
    fun getAudio(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "vocabulary") contentType: String,
    ): ResponseEntity<ByteArray> {
        val audioFile =
            resolveAudioPath(validateContentType(contentType), text)
                ?: return ResponseEntity.notFound().build()
        if (!Files.exists(audioFile)) return ResponseEntity.notFound().build()

        val audioData = Files.readAllBytes(audioFile)
        return ResponseEntity
            .ok()
            .contentType(MediaType("audio", "mpeg"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"audio.mp3\"")
            .body(audioData)
    }

    private fun validateContentType(contentType: String): String = if (contentType in validContentTypes) contentType else defaultContentType

    /**
     * Resolves the on-disk path for cached audio, defending against path traversal:
     *   - `text` is mapped through SHA-256 so user input never reaches the filesystem
     *   - resolved path must stay inside the configured base directory
     * Returns `null` if the resolved path would escape the base (defense in depth).
     */
    private fun resolveAudioPath(
        contentType: String,
        text: String,
    ): Path? {
        val safeContentType = validateContentType(contentType)
        val baseDir = Paths.get("src", "main", "resources", safeContentType).toAbsolutePath().normalize()
        val filename = sha256Hex(text) + ".mp3"
        val resolved = baseDir.resolve(filename).normalize()
        return if (resolved.startsWith(baseDir)) resolved else null
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
