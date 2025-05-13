package com.example.nihongoit.chat

import com.example.nihongoit.chat.SpeechAnalysisService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/speech")
class SpeechController {

    private val logger = LoggerFactory.getLogger(SpeechController::class.java)

    @Autowired
    private lateinit var speechAnalysisService: SpeechAnalysisService

    @PostMapping("/analyze")
    fun analyzeSpeech(
        @RequestParam("audio") audio: MultipartFile,
        @RequestParam("sentence") sentence: String,
        @RequestParam("userId", required = false) userId: String?
    ): ResponseEntity<Any> {
        logger.info("Received audio file: ${audio.originalFilename}, size: ${audio.size} bytes, contentType: ${audio.contentType}")
        logger.info("Audio file empty: ${audio.isEmpty}, Sentence: $sentence, UserId: ${userId ?: "not provided"}")

        // Log WAV file details if content type is audio/wav
        if (audio.contentType?.contains("wav", ignoreCase = true) == true ||
            audio.originalFilename?.endsWith(".wav", ignoreCase = true) == true) {
            logger.info("Processing WAV file")
        } else {
            logger.info("Non-WAV file detected: ${audio.contentType}")
        }

        return try {
            val analysis = speechAnalysisService.analyze(audio, sentence)
            logger.info("Analysis completed successfully")
            ResponseEntity.ok(analysis)
        } catch (e: Exception) {
            logger.error("Error processing request", e)
            ResponseEntity.badRequest().body(mapOf(
                "error" to e.message,
                "message" to "Lỗi khi phân tích: ${e.message}"
            ))
        }
    }

    @PostMapping("/analyze-sample")
    fun analyzeSample(
        @RequestParam("sentence") sentence: String,
        @RequestParam("sampleId") sampleId: String
    ): ResponseEntity<Any> {
        logger.info("Received analyze-sample request: sampleId=$sampleId, sentence=$sentence")

        return try {
            val analysis = speechAnalysisService.analyzeSample(sampleId, sentence)
            logger.info("Sample analysis completed successfully")
            ResponseEntity.ok(analysis)
        } catch (e: Exception) {
            logger.error("Error processing sample request", e)
            ResponseEntity.badRequest().body(mapOf(
                "error" to e.message,
                "message" to "Lỗi khi phân tích mẫu: ${e.message}"
            ))
        }
    }

    @GetMapping("/sample-audio/{sampleId}")
    fun getSampleAudio(
        @PathVariable sampleId: String,
        @RequestParam(required = false, defaultValue = "wav") format: String
    ): ResponseEntity<ByteArray> {
        logger.info("Received request for sample audio: $sampleId, format: $format")

        return try {
            val audio = speechAnalysisService.getSampleAudio(sampleId, format)

            // Determine content type
            val contentType = if (format.equals("mp3", ignoreCase = true)) {
                "audio/mpeg"
            } else {
                "audio/wav" // Default to WAV
            }

            // Return the audio file with appropriate headers
            ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "inline; filename=\"$sampleId.$format\"")
                    .body(audio)

        } catch (e: Exception) {
            logger.error("Error getting sample audio", e)
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/analyze-audio-enhanced")
    fun analyzeAudioEnhanced(
        @RequestParam("file") audio: MultipartFile,
        @RequestParam("reference_text") referenceText: String,
        @RequestParam("sample_id", required = false) sampleId: String?
    ): ResponseEntity<Any> {
        logger.info("Received enhanced audio analysis request - file: ${audio.originalFilename}, size: ${audio.size}, reference_text: $referenceText, sample_id: $sampleId")

        return try {
            val analysis = speechAnalysisService.analyzeEnhanced(audio, referenceText, sampleId)
            logger.info("Enhanced analysis completed successfully")
            ResponseEntity.ok(analysis)
        } catch (e: Exception) {
            logger.error("Error processing enhanced analysis request", e)
            ResponseEntity.badRequest().body(mapOf(
                "error" to e.message,
                "message" to "Lỗi khi phân tích nâng cao: ${e.message}"
            ))
        }
    }

    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        logger.info("Health check requested")

        val pythonServiceStatus = try {
            val response = speechAnalysisService.checkPythonServiceHealth()
            mapOf(
                "available" to true,
                "status" to (response["status"] ?: "unknown"),
                "details" to response
            )
        } catch (e: Exception) {
            logger.error("Error checking Python service health", e)
            mapOf(
                "available" to false,
                "error" to e.message
            )
        }

        val result = mapOf(
            "status" to "healthy",
            "timestamp" to System.currentTimeMillis(),
            "javaService" to mapOf(
                "status" to "healthy",
                "version" to "1.0"
            ),
            "pythonService" to pythonServiceStatus
        )

        return ResponseEntity.ok(result)
    }
}