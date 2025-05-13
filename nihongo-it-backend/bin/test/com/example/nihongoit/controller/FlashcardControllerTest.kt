package com.example.nihongoit.controller

import com.example.nihongoit.dto.ResponseDto
import com.example.nihongoit.dto.ResponseType
import com.example.nihongoit.dto.flashcard.*
import com.example.nihongoit.dto.review.ReviewFlashcardResponseDto
import com.example.nihongoit.dto.review.ReviewRequest
import com.example.nihongoit.entity.FlashcardEntity
import com.example.nihongoit.security.JwtAuthenticationFilter
import com.example.nihongoit.security.JwtTokenUtil
import com.example.nihongoit.service.FSRSService
import com.example.nihongoit.service.FlashcardService
import com.example.nihongoit.util.UserAuthUtil
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(controllers = [FlashcardController::class])
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class FlashcardControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun flashcardController(flashcardService: FlashcardService, userAuthUtil: UserAuthUtil): FlashcardController {
            return FlashcardController(flashcardService, userAuthUtil)
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var flashcardService: FlashcardService

    @MockitoBean
    private lateinit var fsrsService: FSRSService

    @MockitoBean
    private lateinit var userAuthUtil: UserAuthUtil
    
    @MockitoBean
    private lateinit var jwtTokenUtil: JwtTokenUtil
    
    @MockitoBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    private val testUserId = UUID.randomUUID()
    private lateinit var testFlashcard: FlashcardEntity
    private lateinit var testFlashcardDTO: FlashcardDTO
    private lateinit var createFlashcardRequestDto: CreateFlashcardRequestDto
    private lateinit var updateFlashcardRequestDto: UpdateFlashcardRequestDto
    private val authToken = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOiI0ZGE4ZGYxZS0xZGRiLTQzZjEtYmExMy05NDA4NzA1ZmY3ZDUiLCJyb2xlIjoxLCJlbWFpbCI6ImFkbWluQGV4YW1wbGUuY29tIiwiZnVsbE5hbWUiOiJhZG1pbiIsInByb2ZpbGVQaWN0dXJlIjoiIiwiY3VycmVudExldmVsIjoiTjUiLCJqbHB0R29hbCI6Ik4zIiwibGFzdExvZ2luIjoiMjAyNS0wNC0xMFQwMTo0MDo0My4wODMyMTIiLCJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTc0NDIyNTk4OCwiZXhwIjoxNzQ0MzEyMzg4fQ.U1x9eACXZ1QCUg_dXi6hjCMvDbflpBa0SFf5F0p3vFraGCar9W_LFfCSeBjuhFxMyy6tcj4IpFRh6hRbibpCTg"

    @BeforeEach
    fun setup() {
        testFlashcard = FlashcardEntity(
            flashCardId = UUID.randomUUID(),
            userId = testUserId,
            frontText = "フロント",
            backText = "Front",
            notes = "Test note",
            tags = listOf("JLPT N5", "IT")
        )

        testFlashcardDTO = FlashcardDTO(
            id = testFlashcard.flashCardId,
            frontText = testFlashcard.frontText,
            backText = testFlashcard.backText,
            notes = testFlashcard.notes,
            tags = testFlashcard.tags
        )
        
        createFlashcardRequestDto = CreateFlashcardRequestDto(
            frontText = "新しいフロント",
            backText = "New Front",
            notes = "New test note",
            tags = listOf("JLPT N4", "Programming")
        )
        
        updateFlashcardRequestDto = UpdateFlashcardRequestDto(
            frontText = "更新フロント",
            backText = "Updated Front",
            notes = "Updated test note",
            tags = listOf("JLPT N3", "Software")
        )

        // Setup mocks using mockito-kotlin
        whenever(userAuthUtil.getCurrentUserId()).thenReturn(testUserId)
        whenever(jwtTokenUtil.validateToken(any(), any())).thenReturn(true)
    }

    @Test
    @WithMockUser
    fun `test get all flashcards`() {
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "All flashcards retrieved successfully"
        )
        
        val responseBody = GetFlashcardsResponseDto(
            result = responseDto,
            data = listOf(testFlashcardDTO)
        )
        
        whenever(flashcardService.getAllFlashcards()).thenReturn(responseBody)

        mockMvc.perform(get("/api/v1/flashcards")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.data[0].frontText").value("フロント"))
                .andExpect(jsonPath("$.data[0].backText").value("Front"))
    }
    
    @Test
    @WithMockUser
    fun `test get due flashcards`() {
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "Due cards retrieved successfully"
        )
        
        val responseBody = GetDueCardsResponseDto(
            result = responseDto,
            data = listOf(testFlashcardDTO)
        )
        
        whenever(flashcardService.getDueCards()).thenReturn(responseBody)

        mockMvc.perform(get("/api/v1/flashcards/due")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.data[0].frontText").value("フロント"))
    }

    @Test
    @WithMockUser
    fun `test get flashcard by id`() {
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard retrieved successfully"
        )
        
        val responseBody = GetFlashcardResponseDto(
            result = responseDto,
            data = testFlashcardDTO
        )
        
        whenever(flashcardService.getFlashcardById(any())).thenReturn(responseBody)

        mockMvc.perform(get("/api/v1/flashcards/${testFlashcard.flashCardId}")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.data.frontText").value("フロント"))
    }

    @Test
    @WithMockUser
    fun `test create flashcard`() {
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard created successfully"
        )
        
        val responseBody = CreateFlashcardResponseDto(
            result = responseDto,
            data = testFlashcardDTO
        )
        
        whenever(flashcardService.createFlashcard(any())).thenReturn(responseBody)

        mockMvc.perform(post("/api/v1/flashcards")
                .header("Authorization", "Bearer $authToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createFlashcardRequestDto)))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isCreated)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.data.frontText").value("フロント"))
    }
    
    @Test
    @WithMockUser
    fun `test update flashcard`() {
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard updated successfully"
        )
        
        val responseBody = UpdateFlashcardResponseDto(
            result = responseDto,
            data = testFlashcardDTO
        )
        
        whenever(flashcardService.updateFlashcard(any(), any())).thenReturn(responseBody)

        mockMvc.perform(put("/api/v1/flashcards/${testFlashcard.flashCardId}")
                .header("Authorization", "Bearer $authToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateFlashcardRequestDto)))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.data.frontText").value("フロント"))
    }
    
    @Test
    @WithMockUser
    fun `test delete flashcard`() {
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard deleted successfully"
        )
        
        val responseBody = DeleteFlashcardResponseDto(
            result = responseDto
        )
        
        whenever(flashcardService.deleteFlashcard(any())).thenReturn(responseBody)

        mockMvc.perform(delete("/api/v1/flashcards/${testFlashcard.flashCardId}")
                .header("Authorization", "Bearer $authToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.result.message").value("Flashcard deleted successfully"))
    }

    @Test
    @WithMockUser
    fun `test review flashcard`() {
        val reviewRequest = ReviewRequest(rating = 3)
        
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "Flashcard reviewed successfully"
        )
        
        val responseBody = ReviewFlashcardResponseDto(
            result = responseDto,
            data = testFlashcardDTO
        )
        
        whenever(flashcardService.processReview(any(), eq(3))).thenReturn(responseBody)

        mockMvc.perform(post("/api/v1/flashcards/${testFlashcard.flashCardId}/review")
                .header("Authorization", "Bearer $authToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.result.message").value("Flashcard reviewed successfully"))
    }

    @Test
    @WithMockUser
    fun `test get statistics`() {
        val statistics = mapOf(
            "totalCards" to 10L,
            "dueCards" to 3,
            "reviewsLast7Days" to 25
        )
        
        val responseDto = ResponseDto(
            status = ResponseType.OK,
            message = "Study statistics retrieved successfully"
        )
        
        val responseBody = GetStatisticsResponseDto(
            result = responseDto,
            data = statistics
        )
        
        whenever(flashcardService.getStudyStatistics()).thenReturn(responseBody)

        mockMvc.perform(get("/api/v1/flashcards/statistics")
                .header("Authorization", "Bearer $authToken")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Print results for debugging
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result.status").value("OK"))
                .andExpect(jsonPath("$.data.totalCards").value(10))
                .andExpect(jsonPath("$.data.dueCards").value(3))
    }
}