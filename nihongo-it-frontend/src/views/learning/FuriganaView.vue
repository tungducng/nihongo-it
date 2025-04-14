<template>
  <div class="furigana-container">
    <h1 class="text-h4 font-weight-bold mb-6">日本語ふりがな生成器</h1>
    <h2 class="text-h6 text-medium-emphasis mb-8">Japanese Furigana Generator</h2>

    <v-card class="mb-6 elevation-1 pa-4">
      <v-card-text>
        <v-textarea
          v-model="japaneseText"
          label="Enter Japanese Text"
          placeholder="例: 日本語を勉強しています。"
          rows="3"
          auto-grow
          class="mb-4"
          outlined
          hide-details
        ></v-textarea>

        <div class="d-flex align-items-center">
          <v-switch
            v-model="showFurigana"
            label="Show Furigana"
            color="primary"
            hide-details
            density="comfortable"
            class="mt-0 mr-4"
          ></v-switch>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            size="large"
            :loading="loading"
            @click="generateFurigana"
          >
            Generate Furigana
            <v-icon right>mdi-translate</v-icon>
          </v-btn>
        </div>
      </v-card-text>
    </v-card>

    <v-card v-if="tokens.length > 0" class="result-card mb-6 elevation-2 pa-6">
      <h3 class="text-h6 mb-4">Result with Furigana</h3>

      <div class="furigana-text">
        <ruby v-for="(token, index) in tokens" :key="index" class="ruby-text">
          {{ token.text }}
          <rt v-if="token.reading && token.isKanji && showFurigana">{{ token.reading }}</rt>
        </ruby>
      </div>

      <v-divider class="my-4"></v-divider>

      <div class="d-flex">
        <v-spacer></v-spacer>
        <v-btn
          color="secondary"
          variant="outlined"
          @click="copyToClipboard"
          class="mr-2"
        >
          Copy HTML
          <v-icon right>mdi-content-copy</v-icon>
        </v-btn>
        <v-btn
          color="success"
          variant="outlined"
          @click="copyPlainText"
        >
          Copy Text
          <v-icon right>mdi-clipboard-text</v-icon>
        </v-btn>
      </div>
    </v-card>

    <v-card v-if="tokens.length > 0" class="debug-card elevation-1 pa-4">
      <v-card-title class="text-subtitle-1">
        Debug Information
        <v-spacer></v-spacer>
        <v-btn
          size="small"
          variant="text"
          color="primary"
          @click="showDebug = !showDebug"
        >
          {{ showDebug ? 'Hide' : 'Show' }} Debug
        </v-btn>
      </v-card-title>

      <v-expand-transition>
        <div v-if="showDebug">
          <v-card-text>
            <pre class="debug-json">{{ JSON.stringify(tokens, null, 2) }}</pre>
          </v-card-text>
        </div>
      </v-expand-transition>
    </v-card>

    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      timeout="3000"
    >
      {{ snackbar.text }}
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

// State
const japaneseText = ref('日本語を勉強しています。')
const tokens = ref([])
const loading = ref(false)
const showDebug = ref(false)
const showFurigana = ref(true)
const snackbar = ref({
  show: false,
  text: '',
  color: 'success'
})

// Methods
async function generateFurigana() {
  if (!japaneseText.value.trim()) {
    showSnackbar('Please enter some Japanese text', 'error')
    return
  }

  loading.value = true

  try {
    const response = await axios.get('/api/v1/furigana', {
      params: { text: japaneseText.value }
    })

    tokens.value = response.data
    showSnackbar('Furigana generated successfully', 'success')
  } catch (error) {
    console.error('Error generating furigana:', error)
    showSnackbar('Failed to generate furigana. Please try again.', 'error')
  } finally {
    loading.value = false
  }
}

function copyToClipboard() {
  if (tokens.value.length === 0) return

  let html = ''
  tokens.value.forEach(token => {
    if (token.isKanji && token.reading) {
      html += `<ruby>${token.text}<rt>${token.reading}</rt></ruby>`
    } else {
      html += token.text
    }
  })

  navigator.clipboard.writeText(html)
    .then(() => showSnackbar('HTML copied to clipboard', 'success'))
    .catch(() => showSnackbar('Failed to copy HTML', 'error'))
}

function copyPlainText() {
  if (tokens.value.length === 0) return

  const text = tokens.value.map(token => token.text).join('')

  navigator.clipboard.writeText(text)
    .then(() => showSnackbar('Text copied to clipboard', 'success'))
    .catch(() => showSnackbar('Failed to copy text', 'error'))
}

function showSnackbar(text, color = 'success') {
  snackbar.value = {
    show: true,
    text,
    color
  }
}

// Initialize with example text
generateFurigana()
</script>

<style scoped>
.furigana-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.result-card {
  background-color: #fafafa;
  border-radius: 8px;
}

.furigana-text {
  font-size: 1.5rem;
  line-height: 2.2;
  margin: 20px 0;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  word-wrap: break-word;
}

.ruby-text {
  /* display: inline-block; */
  margin: 0 1px;
  text-align: center;
  position: relative;
}

ruby {
  position: relative;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
}

rt {
  position: absolute;
  top: 0.2em;
  font-size: 0.6rem;
  color: #2196F3;
  font-weight: 400;
  text-align: center;
  line-height: 1;
  white-space: nowrap;
  letter-spacing: 0.05em;
}

.debug-json {
  font-family: monospace;
  font-size: 0.85rem;
  line-height: 1.4;
  overflow-x: auto;
  padding: 8px;
  background-color: #f5f5f5;
  border-radius: 4px;
}
</style>
