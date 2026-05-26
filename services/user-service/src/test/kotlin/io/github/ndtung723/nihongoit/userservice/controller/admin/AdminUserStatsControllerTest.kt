package io.github.ndtung723.nihongoit.userservice.controller.admin

import io.github.ndtung723.nihongoit.userservice.entity.JlptLevel
import io.github.ndtung723.nihongoit.userservice.entity.RoleEntity
import io.github.ndtung723.nihongoit.userservice.entity.UserEntity
import io.github.ndtung723.nihongoit.userservice.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdminUserStatsControllerTest {
    private lateinit var userRepository: UserRepository
    private lateinit var controller: AdminUserStatsController

    private val now = LocalDateTime.now()

    private fun makeUser(
        email: String = "u@test.com",
        lastLogin: LocalDateTime? = now,
        createdAt: LocalDateTime? = now.minusDays(2),
    ): UserEntity {
        val u =
            UserEntity(
                userId = UUID.randomUUID(),
                email = email,
                password = "pw",
                fullName = "User",
                profilePicture = null,
                currentLevel = null,
                jlptGoal = null,
                lastLogin = lastLogin,
                role = RoleEntity(1, "ROLE_USER"),
            )
        u.createdAt = createdAt
        return u
    }

    @BeforeEach
    fun setup() {
        userRepository = mock()
        controller = AdminUserStatsController(userRepository)
    }

    @Test
    @DisplayName("count() returns total user count")
    fun count_returnsTotal() {
        whenever(userRepository.count()).thenReturn(42L)
        assertEquals(42L, controller.count()["count"])
    }

    @Test
    @DisplayName("newSince() parses ISO timestamp and delegates to repo")
    fun newSince_parsesAndDelegates() {
        val iso = "2026-05-21T00:00:00"
        whenever(userRepository.countByCreatedAtAfter(LocalDateTime.parse(iso))).thenReturn(3L)
        assertEquals(3L, controller.newSince(iso)["count"])
    }

    @Test
    @DisplayName("activeSince() delegates to repo")
    fun activeSince_delegates() {
        val iso = "2026-05-21T00:00:00"
        whenever(userRepository.countByLastLoginAfter(LocalDateTime.parse(iso))).thenReturn(7L)
        assertEquals(7L, controller.activeSince(iso)["count"])
    }

    @Test
    @DisplayName("byLevel() maps null enum to 'Not specified'")
    fun byLevel_handlesNulls() {
        whenever(userRepository.countGroupByCurrentLevel()).thenReturn(
            listOf(
                arrayOf<Any?>(JlptLevel.N3, 5L),
                arrayOf<Any?>(null, 2L),
            ),
        )
        val result = controller.byLevel()
        assertEquals(5, result["N3"])
        assertEquals(2, result["Not specified"])
    }

    @Test
    @DisplayName("byJlptGoal() maps null enum to 'Not specified'")
    fun byJlptGoal_handlesNulls() {
        whenever(userRepository.countGroupByJlptGoal()).thenReturn(
            listOf(
                arrayOf<Any?>(JlptLevel.N2, 10L),
                arrayOf<Any?>(null, 1L),
            ),
        )
        val result = controller.byJlptGoal()
        assertEquals(10, result["N2"])
        assertEquals(1, result["Not specified"])
    }

    @Test
    @DisplayName("recentActivities() — recent login → 'Đã đăng nhập'")
    fun recentActivities_recentLogin() {
        val u = makeUser(email = "x@test.com", lastLogin = now.minusMinutes(5))
        whenever(userRepository.findTop10ByOrderByLastLoginDesc()).thenReturn(listOf(u))
        val result = controller.recentActivities(10)
        assertEquals(1, result.size)
        assertEquals("x@test.com", result[0]["user"])
        assertEquals("Đã đăng nhập", result[0]["action"])
    }

    @Test
    @DisplayName("recentActivities() — new account → 'Đã tạo tài khoản mới'")
    fun recentActivities_newAccount() {
        val u =
            makeUser(
                email = "new@test.com",
                lastLogin = now.minusHours(3),
                createdAt = now.minusHours(2),
            )
        whenever(userRepository.findTop10ByOrderByLastLoginDesc()).thenReturn(listOf(u))
        val result = controller.recentActivities(10)
        assertEquals("Đã tạo tài khoản mới", result[0]["action"])
    }

    @Test
    @DisplayName("recentActivities() — old account, old login → 'Đã truy cập hệ thống'")
    fun recentActivities_legacyAccess() {
        val u =
            makeUser(
                email = "old@test.com",
                lastLogin = now.minusDays(5),
                createdAt = now.minusDays(60),
            )
        whenever(userRepository.findTop10ByOrderByLastLoginDesc()).thenReturn(listOf(u))
        val result = controller.recentActivities(10)
        assertEquals("Đã truy cập hệ thống", result[0]["action"])
    }

    @Test
    @DisplayName("recentActivities() — user with null lastLogin is skipped")
    fun recentActivities_skipsNullLastLogin() {
        val u = makeUser(lastLogin = null)
        whenever(userRepository.findTop10ByOrderByLastLoginDesc()).thenReturn(listOf(u))
        assertTrue(controller.recentActivities(10).isEmpty())
    }

    @Test
    @DisplayName("recentActivities() — caps limit to MAX_LIMIT")
    fun recentActivities_capsLimit() {
        val users = (1..5).map { makeUser(email = "u$it@test.com") }
        whenever(userRepository.findTop10ByOrderByLastLoginDesc()).thenReturn(users)
        val result = controller.recentActivities(3)
        assertEquals(3, result.size)
    }
}
