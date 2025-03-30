import com.example.japanesitlearning.entity.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ai_generated_content")
data class AiGeneratedContentEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "content_id", updatable = false, nullable = false)
    val contentId: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    val contentType: ContentType,

    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    val contentJson: String,

    @Column(name = "target_jlpt_level", nullable = false)
    val targetJlptLevel: Int, // 5: N5, 4: N4, 3: N3

    @Column(name = "generated_date", nullable = false)
    val generatedDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_approved", nullable = false)
    val isApproved: Boolean = false,

    @Column(name = "topic", nullable = false)
    val topic: String,

    @Column(name = "tags")
    val tags: String?, // Comma-separated tags for better content discovery

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    val approvedBy: UserEntity?,

    @Column(name = "approved_date")
    val approvedDate: LocalDateTime?,

    @Column(name = "effectiveness_score")
    val effectivenessScore: Float? // Calculated based on user performance
)

enum class ContentType {
    VOCABULARY,
    CONVERSATION,
    QUIZ,
    GRAMMAR_POINT
}