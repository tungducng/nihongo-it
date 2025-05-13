package com.example.nihongoit.feign

import com.example.nihongoit.config.FeignConfig
import com.example.nihongoit.feign.models.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@FeignClient(
    name = "ai-service", 
    url = "\${app.ai-service-url}",
    configuration = [FeignConfig::class]
)
interface AiServiceClient {
    
    // Text-to-speech endpoints
    @PostMapping("/ai-service-api/v1/tts/generate")
    fun generateSpeech(
        @RequestBody text: String,
        @RequestHeader(value = "X-Speech-Speed", required = false, defaultValue = "1.0") speedStr: String,
        @RequestHeader(value = "X-Content-Type", required = false, defaultValue = "vocabulary") contentType: String,
        @RequestHeader(value = "X-Content-Language", required = false, defaultValue = "ja") language: String,
        @RequestHeader(value = "X-Save-Audio", required = false, defaultValue = "false") saveAudio: Boolean
    ): ResponseEntity<ByteArray>
    
    @GetMapping("/ai-service-api/v1/tts/check")
    fun checkAudioExists(
        @RequestParam("text") text: String,
        @RequestParam(value = "contentType", required = false, defaultValue = "vocabulary") contentType: String
    ): ResponseEntity<Map<String, Boolean>>
    
    @GetMapping("/ai-service-api/v1/tts/audio")
    fun getAudio(
        @RequestParam("text") text: String,
        @RequestParam(value = "contentType", required = false, defaultValue = "vocabulary") contentType: String
    ): ResponseEntity<ByteArray>
    
    // Speech analysis endpoints
    @PostMapping(value = ["/ai-service-api/v1/speech/analyze"], consumes = ["multipart/form-data"])
    fun analyzeSpeech(
        @RequestPart("audio") audio: MultipartFile,
        @RequestPart("sentence") text: String,
        @RequestPart("userId", required = false) userId: String?
    ): ResponseEntity<AnalyzeSpeechResponse>
    
    @PostMapping("/ai-service-api/v1/speech/analyze-sample")
    fun analyzeSample(
        @RequestParam("sentence") sentence: String,
        @RequestParam("sampleId") sampleId: String
    ): ResponseEntity<AnalyzeSpeechResponse>
    
    @GetMapping("/ai-service-api/v1/speech/sample-audio/{sampleId}")
    fun getSampleAudio(
        @PathVariable sampleId: String,
        @RequestParam(required = false, defaultValue = "wav") format: String
    ): ResponseEntity<ByteArray>
    
    @PostMapping(value = ["/ai-service-api/v1/speech/analyze-audio-enhanced"], consumes = ["multipart/form-data"])
    fun analyzeAudioEnhanced(
        @RequestPart("file") audio: MultipartFile,
        @RequestPart("reference_text") referenceText: String,
        @RequestPart("sample_id", required = false) sampleId: String?
    ): ResponseEntity<Any>
    
    @GetMapping("/ai-service-api/v1/speech/health")
    fun healthCheck(): ResponseEntity<Map<String, Any>>
    
    // Chat endpoints
    @GetMapping("/ai-service-api/v1/chat/ask-ai")
    fun askAI(@RequestParam("message") prompt: String): String
    
    @GetMapping("/ai-service-api/v1/chat/ask-ai-options")
    fun getResponseOptions(@RequestParam("message") prompt: String): String
    
    // Translation endpoints
    @PostMapping("/ai-service-api/v1/chat/translate")
    fun translate(
        @RequestBody text: String,
        @RequestParam("direction") direction: String
    ): TranslationResponse
    
    @PostMapping("/ai-service-api/v1/chat/translate/economy")
    fun translateEconomy(
        @RequestBody text: String,
        @RequestParam("direction") direction: String
    ): TranslationResponse
    
    // Vocabulary endpoints
    @PostMapping("/ai-service-api/v1/chat/vocabulary/list")
    fun getVocabularyList(
        @RequestParam("category") category: String,
        @RequestParam("level") level: String = "N5"
    ): List<VocabularyInfo>
    
    @PostMapping("/ai-service-api/v1/chat/vocabulary/explain")
    fun explainVocabulary(
        @RequestParam("term") term: String,
        @RequestParam("pronunciation") pronunciation: String?,
        @RequestParam("meaning") meaning: String,
        @RequestParam("topicName") topicName: String?,
        @RequestParam("example") example: String?
    ): String
    
    @PostMapping("/ai-service-api/v1/chat/vocabulary/chat")
    fun vocabularyChat(
        @RequestParam("vocabWord") vocabWord: String,
        @RequestParam("userMessage") userMessage: String
    ): String
    
    @PostMapping("/ai-service-api/v1/chat/list-output")
    fun getListResponse(
        @RequestParam("category") category: String,
        @RequestParam("year") year: String
    ): MutableList<String>?
    
    @PostMapping("/ai-service-api/v1/chat/advisor")
    fun getResponseAdvisor(@RequestParam("message") message: String): String?
    
    @PostMapping("/ai-service-api/v1/chat/map-output")
    fun getMapResponse(
        @RequestParam("category") category: String, 
        @RequestParam("year") year: String
    ): Map<String, Any>?
} 