package com.example.nihongoit.controller

import com.example.nihongoit.dto.ResponseDto
import com.example.nihongoit.dto.ResponseType
import com.example.nihongoit.dto.admin.UserListResponse
import com.example.nihongoit.dto.user.UserCreateRequest
import com.example.nihongoit.dto.user.UserDto
import com.example.nihongoit.dto.user.UserUpdateRequest
import com.example.nihongoit.security.PreAuthFilter
import com.example.nihongoit.service.AdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "API endpoints for admin to manage user accounts")
@PreAuthFilter(hasRole = "admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get all users with pagination",
        description = "Retrieves a paginated list of all users in the system",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "email") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<UserListResponse> {
        val direction = if (sortDir.equals("desc", ignoreCase = true)) 
            Sort.Direction.DESC else Sort.Direction.ASC
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        val result = adminService.getAllUsers(pageable, search)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{userId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a single user by their unique identifier",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getUserById(@PathVariable userId: UUID): ResponseEntity<UserDto> {
        val user = adminService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Create new user",
        description = "Creates a new user account in the system",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun createUser(@RequestBody request: UserCreateRequest): ResponseEntity<UserDto> {
        val createdUser = adminService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PutMapping("/{userId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Update user",
        description = "Updates an existing user's information",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun updateUser(
        @PathVariable userId: UUID,
        @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserDto> {
        val updatedUser = adminService.updateUser(userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{userId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Delete user",
        description = "Deactivates a user account (soft delete)",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun deleteUser(@PathVariable userId: UUID): ResponseEntity<ResponseDto> {
        adminService.deactivateUser(userId)
        return ResponseEntity.ok(
            ResponseDto(
                status = ResponseType.OK,
                message = "User deactivated successfully"
            )
        )
    }

    @PutMapping("/{userId}/activate", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Activate user",
        description = "Activates a deactivated user account",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun activateUser(@PathVariable userId: UUID): ResponseEntity<ResponseDto> {
        adminService.activateUser(userId)
        return ResponseEntity.ok(
            ResponseDto(
                status = ResponseType.OK,
                message = "User activated successfully"
            )
        )
    }

    @PutMapping("/{userId}/change-role", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Change user role",
        description = "Changes a user's role (admin/user)",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun changeUserRole(
        @PathVariable userId: UUID,
        @RequestParam roleId: Int
    ): ResponseEntity<ResponseDto> {
        adminService.changeUserRole(userId, roleId)
        return ResponseEntity.ok(
            ResponseDto(
                status = ResponseType.OK,
                message = "User role updated successfully"
            )
        )
    }
} 