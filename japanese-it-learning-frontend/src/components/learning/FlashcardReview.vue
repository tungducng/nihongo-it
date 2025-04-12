<template>
  <div class="flashcard-container">
    <div v-if="loading">
      <v-progress-circular
        indeterminate
        color="primary"
        size="70"
        class="ma-auto d-block mb-4"
      ></v-progress-circular>
      <v-sheet class="text-center">Đang tải...</v-sheet>
    </div>

    <v-sheet v-else-if="!currentCard" class="text-center pa-6">
      <v-icon icon="mdi-check-circle" color="success" size="64" class="mb-4"></v-icon>
      <h2 class="text-h4 mb-2">Không có thẻ nào cần ôn tập</h2>
      <p class="text-body-1 mb-6">Bạn đã hoàn thành tất cả flashcard cho hôm nay!</p>
      <v-btn color="primary" @click="createNewCard" prepend-icon="mdi-plus">Tạo thẻ mới</v-btn>
    </v-sheet>

    <div v-else class="flashcard">
      <v-hover v-slot="{ isHovering, props }">
        <v-card
          v-bind="props"
          :elevation="isHovering ? 8 : 2"
          class="card mb-6"
          :class="{ flipped: isFlipped }"
          @click="flipCard"
          height="300"
        >
          <div class="card-inner">
            <div class="card-front">
              <v-card-text class="d-flex align-center justify-center text-h4" style="height: 100%">
                {{ currentCard.front }}
              </v-card-text>
            </div>
            <div class="card-back">
              <v-card-text
                class="d-flex flex-column align-center justify-center"
                style="height: 100%"
              >
                <span class="text-h4 mb-2">{{ currentCard.back }}</span>
                <span class="text-body-1" v-if="currentCard.notes">{{ currentCard.notes }}</span>
              </v-card-text>
            </div>
          </div>
        </v-card>
      </v-hover>

      <v-expand-transition>
        <div v-if="isFlipped" class="mb-4">
          <v-row>
            <v-col cols="3">
              <v-btn color="error" block @click="rateCard(1)" variant="tonal">Quên hoàn toàn</v-btn>
            </v-col>
            <v-col cols="3">
              <v-btn color="warning" block @click="rateCard(2)" variant="tonal">Nhớ khó khăn</v-btn>
            </v-col>
            <v-col cols="3">
              <v-btn color="success" block @click="rateCard(3)" variant="tonal">Nhớ được</v-btn>
            </v-col>
            <v-col cols="3">
              <v-btn color="info" block @click="rateCard(4)" variant="tonal">Nhớ rõ ràng</v-btn>
            </v-col>
          </v-row>
        </div>
      </v-expand-transition>

      <v-sheet class="text-center text-subtitle-1">
        <v-progress-linear
          :model-value="(reviewHistory.length / totalCardsToday) * 100"
          color="primary"
          height="10"
          rounded
          striped
          class="mb-2"
        ></v-progress-linear>
        Đã ôn tập: {{ reviewHistory.length }} / {{ totalCardsToday }}
      </v-sheet>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { useFlashcardsStore } from '@/stores'
import { useRouter } from 'vue-router'
import type { Flashcard, Review } from '@/stores/modules/flashcards'
import { Watch } from 'vue-facing-decorator'

@Component({
  name: 'FlashcardReview',
})
export default class FlashcardReview extends Vue {
  private flashcardsStore = useFlashcardsStore()
  private router = useRouter()

  isFlipped = false
  totalCardsToday = 0

  get loading(): boolean {
    return this.flashcardsStore.loading
  }

  get currentCard(): Flashcard | null {
    return this.flashcardsStore.currentCard
  }

  get dueCards(): Flashcard[] {
    return this.flashcardsStore.dueCards
  }

  get reviewHistory(): Review[] {
    return this.flashcardsStore.reviewHistory
  }

  flipCard(): void {
    this.isFlipped = !this.isFlipped
  }

  rateCard(rating: number): void {
    if (this.currentCard) {
      this.flashcardsStore.reviewCard({
        cardId: this.currentCard.id,
        rating,
      })
      this.isFlipped = false
    }
  }

  createNewCard(): void {
    this.router.push('/flashcards/create')
  }

  @Watch('dueCards')
  onDueCardsChange(newCards: Flashcard[]): void {
    if (newCards) {
      this.totalCardsToday = newCards.length + this.reviewHistory.length
    }
  }

  mounted(): void {
    this.flashcardsStore.fetchDueCards()

    // Initialize totalCardsToday immediately
    if (this.dueCards) {
      this.totalCardsToday = this.dueCards.length + this.reviewHistory.length
    }
  }
}
</script>

<style lang="sass" scoped>
// Simplified styles with direct values
.flashcard-container
  max-width: 700px
  margin: 0 auto
  padding: 1rem

  @media (min-width: 768px)
    max-width: 800px

  @media (min-width: 992px)
    max-width: 900px

.card
  position: relative
  perspective: 1000px
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05)
  transition: transform 0.1s

  &:hover
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)

.card-inner
  position: relative
  width: 100%
  height: 100%
  text-align: center
  transition: transform 0.6s
  transform-style: preserve-3d

.card.flipped .card-inner
  transform: rotateY(180deg)

.card-front,
.card-back
  position: absolute
  width: 100%
  height: 100%
  backface-visibility: hidden

  &-content
    display: flex
    align-items: center
    justify-content: center
    height: 100%
    padding: 1rem

    h2
      font-size: 1.875rem
      font-weight: 500
      margin-bottom: 0.5rem

    p
      font-size: 1rem
      font-weight: 400
      color: #6c757d

// Card front/back colors
.card-front
  background-color: #e6ecff
  color: #3f37c9

.card-back
  background-color: #ffffff
  color: #4361ee
  transform: rotateY(180deg)

// Rating buttons
.rating-buttons
  .btn
    &.rating-1
      background-color: #f72585
      color: white

      &:hover
        background-color: #d41c70

    &.rating-2
      background-color: #f8961e
      color: #212529

      &:hover
        background-color: #d47d1a

    &.rating-3
      background-color: #4895ef
      color: white

      &:hover
        background-color: #2e7edb

    &.rating-4
      background-color: #4cc9f0
      color: white

      &:hover
        background-color: #26b6df

// Progress bar
.progress-indicator
  margin-top: 1rem
  font-size: 0.875rem
  color: #3f37c9

  .bar
    height: 8px
    border-radius: 9999px
    background-color: #e6ecff
    margin-bottom: 0.25rem

    .fill
      height: 100%
      border-radius: 9999px
      background-color: #4361ee
      transition: width 0.1s
</style>
