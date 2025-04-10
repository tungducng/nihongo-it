<template>
  <div class="flashcard-container">
    <div v-if="flashcardsStore.loading">
      <v-progress-circular
        indeterminate
        color="primary"
        size="70"
        class="ma-auto d-block mb-4"
      ></v-progress-circular>
      <v-sheet class="text-center">Đang tải...</v-sheet>
    </div>

    <v-sheet v-else-if="!flashcardsStore.currentCard" class="text-center pa-6">
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
                {{ flashcardsStore.currentCard.front }}
              </v-card-text>
            </div>
            <div class="card-back">
              <v-card-text
                class="d-flex flex-column align-center justify-center"
                style="height: 100%"
              >
                <span class="text-h4 mb-2">{{ flashcardsStore.currentCard.back }}</span>
                <span class="text-body-1" v-if="flashcardsStore.currentCard.notes">{{
                  flashcardsStore.currentCard.notes
                }}</span>
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
          :model-value="(flashcardsStore.reviewHistory.length / totalCardsToday) * 100"
          color="primary"
          height="10"
          rounded
          striped
          class="mb-2"
        ></v-progress-linear>
        Đã ôn tập: {{ flashcardsStore.reviewHistory.length }} / {{ totalCardsToday }}
      </v-sheet>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useFlashcardsStore } from '@/stores'

const router = useRouter()
const flashcardsStore = useFlashcardsStore()
const isFlipped = ref(false)
const totalCardsToday = ref(0)

const flipCard = () => {
  isFlipped.value = !isFlipped.value
}

const rateCard = (rating: number) => {
  if (flashcardsStore.currentCard) {
    flashcardsStore.reviewCard({
      cardId: flashcardsStore.currentCard.id,
      rating,
    })
    isFlipped.value = false
  }
}

const createNewCard = () => {
  router.push('/flashcards/create')
}

// Watch for changes in dueCards
watch(
  () => flashcardsStore.dueCards,
  (newCards) => {
    if (newCards) {
      totalCardsToday.value = newCards.length + flashcardsStore.reviewHistory.length
    }
  },
  { immediate: true },
)

// Fetch cards on component mount
onMounted(() => {
  flashcardsStore.fetchDueCards()
})
</script>

<style scoped>
.flashcard-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.card {
  position: relative;
  perspective: 1000px;
}

.card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  text-align: center;
  transition: transform 0.6s;
  transform-style: preserve-3d;
}

.card.flipped .card-inner {
  transform: rotateY(180deg);
}

.card-front,
.card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
}

.card-back {
  transform: rotateY(180deg);
}
</style>
