package io.github.ndtung723.nihongoit.learningservice.entity

import io.github.ndtung723.nihongoit.common.entity.AbstractAuditEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "topics")
data class TopicEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "topic_id", updatable = false, nullable = false)
    val topicId: UUID? = null,
    @Column(name = "name", nullable = false, unique = true)
    val name: String, // japanese name
    @Column(name = "meaning", nullable = false)
    val meaning: String, // vietnamese meaning
    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: CategoryEntity,
    @OneToMany(mappedBy = "topic", cascade = [CascadeType.ALL], orphanRemoval = true)
    val vocabularyItems: MutableList<VocabularyEntity> = mutableListOf(),
) : AbstractAuditEntity()
