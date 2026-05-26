package io.github.ndtung723.nihongoit.userservice.entity

import jakarta.persistence.*

@Entity
@Table(name = "roles")
data class RoleEntity(
    @Id
    @Column(name = "role_id", nullable = false)
    val roleId: Int,
    @Column(name = "role_name", length = 20, nullable = false)
    val roleName: String,
) {
    companion object {
        const val ROLE_ADMIN = 1
        const val ROLE_USER = 2
    }
}
