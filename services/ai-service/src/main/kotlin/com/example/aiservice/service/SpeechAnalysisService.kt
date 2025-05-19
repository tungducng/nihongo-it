package com.example.aiservice.service

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.nio.file.Files
import java.nio.file.Paths

@Service
class SpeechAnalysisService {

    private val logger = LoggerFactory.getLogger(SpeechAnalysisService::class.java)
    private val SAMPLES_DIR = "src/main/resources/samples/"
    private val webClient: WebClient

    init {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")
                .build()
    }

    fun analyze(audio: MultipartFile, sentence: String): Map<String, Any> {
        // Validate input
        // Remove empty file check to allow any file size
        // if (audio.isEmpty) {
        //     logger.error("Empty audio file received")
        //     throw IllegalArgumentException("File âm thanh rỗng")
        // }
        if (sentence.isBlank()) {
            logger.error("Empty sentence received")
            throw IllegalArgumentException("Câu không hợp lệ")
        }

        logger.info("Analyzing audio: size=${audio.size}, originalFilename=${audio.originalFilename}, contentType=${audio.contentType}")

        // Determine which filename to use - keep original extension
        val originalFilename = audio.originalFilename ?: "speech.webm"
        
        // Chuẩn bị file âm thanh người dùng
        val audioResource = object : ByteArrayResource(audio.bytes) {
            override fun getFilename(): String {
                // Pass the original filename to preserve the extension
                logger.info("Using audio filename: $originalFilename")
                return originalFilename
            }
        }

        // Kiểm tra âm thanh mẫu
        val sampleFileName = sentenceToFileName(sentence)
        val sampleFile = Paths.get(SAMPLES_DIR + sampleFileName + ".wav")
        
        val sampleResource = if (Files.exists(sampleFile)) {
            logger.info("Found sample file: $sampleFile")
            object : ByteArrayResource(Files.readAllBytes(sampleFile)) {
                override fun getFilename(): String {
                    return sampleFileName + ".wav"
                }
            }
        } else {
            logger.warn("Sample file not found for '$sentence' at path: $sampleFile")
            null
        }

        // Gọi API FastAPI
        logger.info("Preparing request to Python FastAPI...")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("audio", audioResource, MediaType.parseMediaType(audio.contentType ?: "audio/webm"))
        bodyBuilder.part("sentence", sentence)
        if (sampleResource != null) {
            bodyBuilder.part("sample", sampleResource, MediaType.parseMediaType("audio/wav"))
        }

        logger.info("Sending request to FastAPI endpoint...")
        try {
            val response = webClient.post()
                    .uri("/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .blockOptional()
                    .orElseThrow { RuntimeException("Lỗi khi gọi FastAPI: không có phản hồi") } as Map<String, Any>
            
            // Log the response for debugging
            logger.info("Received response from FastAPI: $response")
            
            // Process enhanced analysis results
            processAnalysisResults(response)
            
            // Return the processed response
            return response
        } catch (e: Exception) {
            logger.error("Error calling FastAPI", e)
            throw RuntimeException("Lỗi khi gọi FastAPI: ${e.message}")
        }
    }

    /**
     * Process and enhance the analysis results if needed
     */
    private fun processAnalysisResults(response: Map<String, Any>): Map<String, Any> {
        try {
            // Ensure all required fields are present
            val processedResponse = response.toMutableMap()
            
            // Ensure words array has expected properties
            @Suppress("UNCHECKED_CAST")
            val words = processedResponse["words"] as? List<Map<String, Any>> ?: emptyList()
            
            // Log summary of word analysis
            val correctWords = words.count { it["isCorrect"] as? Boolean ?: false }
            val incorrectWords = words.size - correctWords
            logger.info("Word analysis summary: ${words.size} total words, $correctWords correct, $incorrectWords incorrect")
            
            // Log personalized feedback if present
            val personalizedFeedback = processedResponse["personalizedFeedback"] as? String
            if (personalizedFeedback != null) {
                logger.info("Personalized feedback provided: ${personalizedFeedback.take(50)}...")
            } else {
                // Add default personalized feedback if not present
                val score = processedResponse["score"] as? Number ?: 0
                val defaultFeedback = if (score.toInt() > 80) {
                    "Phát âm tốt! Tiếp tục luyện tập để hoàn thiện hơn."
                } else {
                    "Cần cải thiện phát âm của một số từ. Hãy tập trung vào các từ được đánh dấu đỏ."
                }
                processedResponse["personalizedFeedback"] = defaultFeedback
                logger.info("Added default personalized feedback")
            }
            
            return processedResponse
        } catch (e: Exception) {
            logger.error("Error processing analysis results", e)
            return response // Return original response if processing fails
        }
    }

    /**
     * Enhanced audio analysis that forwards to Python service
     */
    fun analyzeEnhanced(audio: MultipartFile, referenceText: String, sampleId: String?): Map<String, Any> {
        // Validate input
        if (referenceText.isBlank()) {
            logger.error("Empty reference text received")
            throw IllegalArgumentException("Câu tham chiếu không hợp lệ")
        }

        logger.info("Enhanced analysis for: ref='$referenceText', sampleId='${sampleId ?: "none"}'")
        logger.info("Audio file: size=${audio.size}, name=${audio.originalFilename}, type=${audio.contentType}")

        // Prepare audio file
        val audioResource = object : ByteArrayResource(audio.bytes) {
            override fun getFilename(): String {
                return audio.originalFilename ?: "recording.wav"
            }
        }

        // Prepare multipart request body
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", audioResource, MediaType.parseMediaType(audio.contentType ?: "audio/wav"))
        bodyBuilder.part("reference_text", referenceText)
        if (!sampleId.isNullOrBlank()) {
            bodyBuilder.part("sample_id", sampleId)
        }

        // Call Python service
        logger.info("Sending enhanced analysis request to Python service...")
        try {
            val response = webClient.post()
                .uri("/analyze-audio-enhanced")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(Map::class.java)
                .blockOptional()
                .orElseThrow { RuntimeException("Không có phản hồi từ dịch vụ Python") } as Map<String, Any>

            logger.info("Received enhanced analysis response from Python service")
            
            // Process the results if needed
            return processAnalysisResults(response)
        } catch (e: Exception) {
            logger.error("Error in enhanced analysis: ${e.message}")
            throw RuntimeException("Lỗi khi thực hiện phân tích nâng cao: ${e.message}")
        }

    }

    fun analyzeSample(sampleId: String, sentence: String): Map<String, Any> {
        // Validate input
        if (sampleId.isBlank()) {
            logger.error("Empty sample ID received")
            throw IllegalArgumentException("ID mẫu không hợp lệ")
        }
        if (sentence.isBlank()) {
            logger.error("Empty sentence received")
            throw IllegalArgumentException("Câu không hợp lệ")
        }

        logger.info("Analyzing sample: $sampleId for sentence: $sentence")

        // Check if sample file exists - using WAV instead of MP3
        val sampleFile = Paths.get(SAMPLES_DIR + sampleId + ".wav")
        if (!Files.exists(sampleFile)) {
            logger.error("Sample file not found: $sampleFile")
            throw IllegalArgumentException("Không tìm thấy file mẫu: $sampleId")
        }

        // Use the direct sample analysis endpoint
        logger.info("Using direct analysis endpoint for sample: $sampleId")
        try {
            val response = webClient.get()
                    .uri("/analyze-direct-sample/$sampleId")
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .blockOptional()
                    .orElseThrow { RuntimeException("Lỗi khi gọi FastAPI: không có phản hồi") } as Map<String, Any>
            
            logger.info("Received response from FastAPI: $response")
            return response
        } catch (e: Exception) {
            logger.error("Error calling FastAPI", e)
            throw RuntimeException("Lỗi khi gọi FastAPI: ${e.message}")
        }
    }

    /**
     * Get sample audio file either directly from the file system or proxied through Python FastAPI
     */
    fun getSampleAudio(sampleId: String, format: String = "wav"): ByteArray {
        // Normalize format
        val audioFormat = if (format.equals("mp3", ignoreCase = true)) "mp3" else "wav"
        
        // Check if the file exists locally first
        val localPath = Paths.get(SAMPLES_DIR + sampleId + "." + audioFormat)
        if (Files.exists(localPath)) {
            logger.info("Serving sample audio from local file: $localPath")
            return Files.readAllBytes(localPath)
        }
        
        // If not found locally, try to get it from the Python service
        logger.info("Sample not found locally, requesting from Python service: $sampleId.$audioFormat")
        try {
            val response = webClient.get()
                    .uri("/sample-audio/$sampleId?format=$audioFormat")
                    .accept(MediaType.parseMediaType(if (audioFormat == "mp3") "audio/mpeg" else "audio/wav"))
                    .retrieve()
                    .bodyToMono(ByteArray::class.java)
                    .blockOptional()
                    .orElseThrow { RuntimeException("No response from Python service") }
            
            logger.info("Successfully retrieved sample audio from Python service: $sampleId.$audioFormat")
            return response
        } catch (e: Exception) {
            logger.error("Error retrieving sample audio from Python service", e)
            throw RuntimeException("Error retrieving sample audio: ${e.message}")
        }
    }

    /**
     * Check if Python service is healthy
     */
    fun checkPythonServiceHealth(): Map<String, Any> {
        logger.info("Checking Python service health")
        try {
            val response = webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(Map::class.java)
                .blockOptional()
                .orElseThrow { RuntimeException("No response from Python service") } as Map<String, Any>
            
            logger.info("Python service health check successful")
            return response
        } catch (e: Exception) {
            logger.error("Error checking Python service health", e)
            throw RuntimeException("Unable to reach Python service: ${e.message}")
        }
    }

    private fun sentenceToFileName(sentence: String): String {
        return when (sentence) {
            "今日、図書館で本を借りました。" -> "today_library"
            "こんにちは、友達！" -> "hello_friend"
            "今日の天気はとても良いですね。" -> "weather_good"
            "日本語を勉強することは楽しいです。" -> "japanese_study"
            // Add more mappings as needed
            else -> {
                // Generate a safe filename for any other sentence
                val safeName = sentence.take(10).replace(Regex("[^\\p{L}\\p{N}]"), "_").lowercase()
                logger.warn("No predefined mapping for sentence, using generated name: $safeName")
                safeName
            }
        }
    }
} 