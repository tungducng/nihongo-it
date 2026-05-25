package com.example.learningservice.entity

import com.example.common.entity.AbstractAuditEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "categories")
data class CategoryEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "category_id", updatable = false, nullable = false)
    val categoryId: UUID? = null,
    @Column(name = "name", nullable = false, unique = true)
    val name: String, // japanese name
    @Column(name = "meaning")
    val meaning: String? = null, // vietnamese meaning
    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = true)
    val topics: MutableList<TopicEntity> = mutableListOf(),
) : AbstractAuditEntity()
