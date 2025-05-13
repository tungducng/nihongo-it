package com.example.nihongoit.repository

import com.example.nihongoit.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<RoleEntity, Int> {
    fun findByRoleId(roleId: Int): RoleEntity?
} 
