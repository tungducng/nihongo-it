package io.github.ndtung723.nihongoit.userservice.repository

import io.github.ndtung723.nihongoit.userservice.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<RoleEntity, Int> {
    fun findByRoleId(roleId: Int): RoleEntity?
}
