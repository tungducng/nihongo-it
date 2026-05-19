package com.example.learningservice.entity

import com.example.common.entity.AbstractAuditEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "conv_id", updatable = false, nullable = false)
    val convId: UUID? = null,
    @Column(nullable = false)
    var title: String,
    @Column(nullable = true, columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "jlpt_level", nullable = true)
    var jlptLevel: String? = null,
    @Column(nullable = true)
    var unit: Int? = null,
    @OneToMany(mappedBy = "conversation", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var lines: MutableList<ConversationLineEntity> = mutableListOf(),
) : AbstractAuditEntity()
