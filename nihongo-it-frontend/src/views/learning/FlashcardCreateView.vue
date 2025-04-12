<template>
  <v-container>
    <h1 class="text-h4 mb-6 text-center">Tạo Flashcard mới</h1>
    <FlashcardCreate />
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import FlashcardCreate from '@/components/learning/FlashcardCreate.vue'

@Component({
  name: 'FlashcardCreateView',
  components: {
    FlashcardCreate,
  },
})
export default class FlashcardCreateView extends Vue {}
</script>
