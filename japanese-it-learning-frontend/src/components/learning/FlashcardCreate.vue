<template>
  <div class="flashcard-create-container">
    <v-card class="mx-auto pa-6" max-width="600" elevation="3">
      <v-card-title class="text-center text-h5 mb-4">
        <v-icon icon="mdi-card-plus-outline" color="primary" class="me-2"></v-icon>
        Tạo Flashcard mới
      </v-card-title>

      <v-form @submit.prevent="createFlashcard" ref="form" v-model="isFormValid">
        <v-text-field
          v-model="front"
          :rules="[(v) => !!v || 'Vui lòng nhập nội dung mặt trước']"
          label="Mặt trước (Tiếng Nhật)"
          required
          variant="outlined"
          full-width
          class="mb-4"
          clearable
          prepend-inner-icon="mdi-translate"
        />

        <v-text-field
          v-model="back"
          :rules="[(v) => !!v || 'Vui lòng nhập nội dung mặt sau']"
          label="Mặt sau (Nghĩa tiếng Việt)"
          required
          variant="outlined"
          full-width
          class="mb-4"
          clearable
          prepend-inner-icon="mdi-comment-text-outline"
        />

        <v-textarea
          v-model="notes"
          label="Ghi chú (Tùy chọn)"
          variant="outlined"
          full-width
          class="mb-4"
          rows="3"
          auto-grow
          counter
          prepend-inner-icon="mdi-note-text-outline"
          clearable
        />

        <v-card class="mb-6 pa-4" variant="outlined" flat>
          <v-card-title class="text-subtitle-1">Xem trước:</v-card-title>
          <v-tabs v-model="previewTab" bg-color="transparent">
            <v-tab value="front">Mặt trước</v-tab>
            <v-tab value="back">Mặt sau</v-tab>
          </v-tabs>

          <v-window v-model="previewTab" class="mt-2">
            <v-window-item value="front">
              <v-card-text
                class="d-flex justify-center align-center"
                :class="front ? 'text-h5' : 'text-subtitle-2 text-disabled'"
                style="min-height: 100px"
              >
                {{ front || 'Nội dung mặt trước sẽ hiển thị ở đây' }}
              </v-card-text>
            </v-window-item>
            <v-window-item value="back">
              <v-card-text
                class="d-flex flex-column justify-center align-center"
                style="min-height: 100px"
              >
                <span :class="back ? 'text-h5' : 'text-subtitle-2 text-disabled'">
                  {{ back || 'Nội dung mặt sau sẽ hiển thị ở đây' }}
                </span>
                <span v-if="notes" class="text-body-2 mt-2">{{ notes }}</span>
              </v-card-text>
            </v-window-item>
          </v-window>
        </v-card>

        <div class="d-flex gap-4">
          <v-btn
            type="submit"
            color="primary"
            block
            :loading="flashcardsStore.loading"
            :disabled="!isFormValid"
            prepend-icon="mdi-check"
          >
            Tạo Flashcard
          </v-btn>

          <v-btn
            variant="tonal"
            color="secondary"
            block
            @click="resetForm"
            :disabled="flashcardsStore.loading"
            prepend-icon="mdi-refresh"
          >
            Làm mới
          </v-btn>
        </div>

        <div class="text-center mt-4">
          <v-btn
            variant="text"
            color="primary"
            @click="router.back()"
            prepend-icon="mdi-arrow-left"
          >
            Quay lại
          </v-btn>
        </div>
      </v-form>

      <v-snackbar v-model="showSnackbar" color="success" timeout="3000" location="top">
        Flashcard đã được tạo thành công!

        <template v-slot:actions>
          <v-btn variant="text" @click="showSnackbar = false"> Đóng </v-btn>
        </template>
      </v-snackbar>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useFlashcardsStore } from '@/stores'

const router = useRouter()
const flashcardsStore = useFlashcardsStore()
const form = ref(null)
const isFormValid = ref(false)
const showSnackbar = ref(false)
const previewTab = ref('front')

const front = ref('')
const back = ref('')
const notes = ref('')

const createFlashcard = async () => {
  await flashcardsStore.createFlashcard({
    front: front.value,
    back: back.value,
    notes: notes.value,
  })

  // Show success message
  showSnackbar.value = true

  // Clear the form
  resetForm()
}

const resetForm = () => {
  front.value = ''
  back.value = ''
  notes.value = ''
  if (form.value) {
    // Reset validation
    form.value.reset()
  }
}
</script>

<style scoped>
.flashcard-create-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}
</style>
