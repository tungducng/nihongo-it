<template>
  <v-container>
    <h1 class="text-h4 mb-6 text-center">Ôn tập Flashcards</h1>
    <FlashcardReview />
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import FlashcardReview from '@/components/learning/FlashcardReview.vue'

@Component({
  name: 'FlashcardsView',
  components: {
    FlashcardReview,
  },
})
export default class FlashcardsView extends Vue {}
</script>
