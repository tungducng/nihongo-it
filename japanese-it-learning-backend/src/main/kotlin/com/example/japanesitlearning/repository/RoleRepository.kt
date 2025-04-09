package com.example.japanesitlearning.repository

import com.example.japanesitlearning.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<RoleEntity, Int> {
    fun findByRoleId(roleId: Int): RoleEntity?
} 
