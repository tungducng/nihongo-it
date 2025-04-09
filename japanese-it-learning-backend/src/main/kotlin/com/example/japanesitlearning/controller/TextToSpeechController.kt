package com.example.japanesitlearning.controller

import org.springframework.ai.openai.OpenAiAudioSpeechModel
import org.springframework.ai.openai.OpenAiAudioSpeechOptions
import org.springframework.ai.openai.api.OpenAiAudioApi
import org.springframework.ai.openai.audio.speech.SpeechPrompt
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/tts")
class TextToSpeechController(
    private val openAiAudioSpeechModel: OpenAiAudioSpeechModel,
) {

    /**
     * Convert text to speech and return audio as a byte array
     * @param text The text to convert to speech
     * @param voice The voice to use (alloy, echo, fable, onyx, nova, shimmer)
     * @param speed The speed of the speech (0.25 to 4.0)
     * @param format The audio format (mp3, opus, aac, flac, wav, pcm)
     * @return ResponseEntity containing the audio bytes
     */
    @PostMapping("/synthesize")
    fun synthesizeSpeech(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "alloy") voice: String,
        @RequestParam(required = false, defaultValue = "1.0") speed: Float,
        @RequestParam(required = false, defaultValue = "mp3") format: String,
    ): ResponseEntity<ByteArray> {
        try {
            // Validate parameters
            if (text.isBlank()) {
                return ResponseEntity.badRequest().build()
            }

            // Create speech options
            val speechOptions = OpenAiAudioSpeechOptions.builder()
                .model("tts-1")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.valueOf(voice.uppercase()))
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.valueOf(format.uppercase()))
                .speed(speed)
                .build()

            // Create speech prompt
            val speechPrompt = SpeechPrompt(text, speechOptions)

            // Call the model
            val response = openAiAudioSpeechModel.call(speechPrompt)

            // Determine the MediaType based on the format
            val mediaType = when (format.lowercase()) {
                "mp3" -> MediaType.parseMediaType("audio/mpeg")
                "opus" -> MediaType.parseMediaType("audio/opus")
                "aac" -> MediaType.parseMediaType("audio/aac")
                "flac" -> MediaType.parseMediaType("audio/flac")
                "wav" -> MediaType.parseMediaType("audio/wav")
                "pcm" -> MediaType.parseMediaType("audio/pcm")
                else -> MediaType.APPLICATION_OCTET_STREAM
            }

            // Return the audio
            return ResponseEntity.ok()
                .contentType(mediaType)
                .body(response.result.output)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Stream the text-to-speech conversion
     * Useful for longer texts where you want to start playing the audio before the entire conversion is complete
     * @param text The text to convert to speech
     * @param voice The voice to use (alloy, echo, fable, onyx, nova, shimmer)
     * @param speed The speed of the speech (0.25 to 4.0)
     * @param format The audio format (mp3, opus, aac, flac, wav, pcm)
     * @return Flux of SpeechResponse for streaming
     */
    @PostMapping("/stream", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun streamSpeech(
        @RequestParam text: String,
        @RequestParam(required = false, defaultValue = "alloy") voice: String,
        @RequestParam(required = false, defaultValue = "1.0") speed: Float,
        @RequestParam(required = false, defaultValue = "mp3") format: String,
    ): Flux<ByteArray> {
        try {
            // Create speech options
            val speechOptions = OpenAiAudioSpeechOptions.builder()
                .model("tts-1")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.valueOf(voice.uppercase()))
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.valueOf(format.uppercase()))
                .speed(speed)
                .build()

            // Create speech prompt
            val speechPrompt = SpeechPrompt(text, speechOptions)

            // Stream the response
            return openAiAudioSpeechModel.stream(speechPrompt)
                .map { response -> response.result.output }
        } catch (e: Exception) {
            return Flux.error(e)
        }
    }

    /**
     * Endpoint specifically designed for speaking Japanese vocabulary
     * @param word The Japanese word to pronounce
     * @param voice The voice to use (default: nova, which has good Japanese pronunciation)
     * @return ResponseEntity containing the audio bytes
     */
    @GetMapping("/speak-japanese")
    fun speakJapaneseWord(
        @RequestParam word: String,
        @RequestParam(required = false, defaultValue = "nova") voice: String,
        @RequestParam(required = false, defaultValue = "1.0") speed: Float,
    ): ResponseEntity<ByteArray> {
        try {
            // Create speech options with settings optimized for Japanese
            val speechOptions = OpenAiAudioSpeechOptions.builder()
                .model("tts-1")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.valueOf(voice.uppercase()))
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(speed)
                .build()

            // Create speech prompt
            val speechPrompt = SpeechPrompt(word, speechOptions)

            // Call the model
            val response = openAiAudioSpeechModel.call(speechPrompt)

            // Return the audio
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(response.result.output)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Endpoint to read a complete Japanese sentence with vocabulary explanation
     * @param sentence The Japanese sentence to read
     * @param vocabulary The vocabulary word to emphasize
     * @param translation The translation of the sentence
     * @return ResponseEntity containing the audio bytes
     */
    @PostMapping("/read-japanese-example")
    fun readJapaneseExample(
        @RequestParam sentence: String,
        @RequestParam vocabulary: String,
        @RequestParam translation: String,
        @RequestParam(required = false, defaultValue = "nova") voice: String,
    ): ResponseEntity<ByteArray> {
        try {
            val text = """
                単語: $vocabulary
                例文: $sentence
                意味: $translation
            """.trimIndent()

            // Create speech options
            val speechOptions = OpenAiAudioSpeechOptions.builder()
                .model("tts-1")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.valueOf(voice.uppercase()))
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(0.9f) // Slightly slower for better comprehension
                .build()

            // Create speech prompt
            val speechPrompt = SpeechPrompt(text, speechOptions)

            // Call the model
            val response = openAiAudioSpeechModel.call(speechPrompt)

            // Return the audio
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(response.result.output)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
