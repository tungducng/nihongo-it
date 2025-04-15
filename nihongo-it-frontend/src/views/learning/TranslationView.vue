<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <div class="d-flex align-center">
          <v-btn icon class="mr-3" @click="$router.go(-1)">
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
          <h1 class="text-h4 font-weight-bold">Công cụ dịch thuật</h1>
        </div>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12" md="12" class="mx-auto">
        <v-card class="translation-card mt-4">
          <v-card-title class="translation-card-title">
            <v-icon color="amber-darken-2" class="mr-2">mdi-translate</v-icon>
            <span>Dịch Tiếng Nhật - Tiếng Việt</span>
          </v-card-title>

          <v-card-text>
            <v-row>
              <v-col cols="12">
                <v-btn-toggle
                  v-model="translationDirection"
                  mandatory
                  color="primary"
                  class="mb-4"
                >
                  <v-btn value="vn-to-jp" class="translation-toggle-btn">
                    <v-icon class="mr-1">mdi-arrow-right</v-icon>
                    Tiếng Việt → Tiếng Nhật
                  </v-btn>
                  <v-btn value="jp-to-vn" class="translation-toggle-btn">
                    <v-icon class="mr-1">mdi-arrow-right</v-icon>
                    Tiếng Nhật → Tiếng Việt
                  </v-btn>
                </v-btn-toggle>

                <!-- Model Selection Switch -->
                <div class="d-flex align-center mt-2">
                  <v-switch
                    v-model="useGpt4"
                    :color="useGpt4 ? 'primary' : 'success'"
                    hide-details
                    density="compact"
                    class="mx-2"
                  ></v-switch>
                  <span class="text-body-2" :class="{'text-primary': useGpt4}">
                    Bật GPT-4o
                    <v-tooltip activator="parent" location="end">
                      {{ useGpt4 ? 'Độ chính xác cao hơn cho thuật ngữ chuyên ngành nhưng đắt hơn 20 lần' : 'Bật để có độ chính xác cao hơn' }}
                    </v-tooltip>
                  </span>
                </div>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="6">
                <div class="input-section">
                  <div class="text-subtitle-1 font-weight-medium mb-2">
                    {{ translationDirection === 'vn-to-jp' ? 'Tiếng Việt' : 'Tiếng Nhật' }}
                  </div>
                  <v-textarea
                    v-model="sourceText"
                    :label="translationDirection === 'vn-to-jp' ? 'Nhập văn bản tiếng Việt' : 'Nhập văn bản tiếng Nhật'"
                    :placeholder="translationDirection === 'vn-to-jp' ? 'Nhập văn bản tiếng Việt tại đây...' : 'Nhập văn bản tiếng Nhật tại đây...'"
                    variant="outlined"
                    rows="8"
                    hide-details
                    class="source-textarea"
                  ></v-textarea>
                  <div class="d-flex justify-space-between align-center mt-2">
                    <v-btn
                      size="small"
                      variant="text"
                      prepend-icon="mdi-content-copy"
                      @click="copyText(sourceText)"
                      :disabled="!sourceText"
                    >
                      Sao chép
                    </v-btn>
                    <v-btn
                      size="small"
                      variant="text"
                      prepend-icon="mdi-trash-can-outline"
                      @click="clearSourceText"
                      :disabled="!sourceText"
                    >
                      Xóa
                    </v-btn>
                  </div>
                </div>
              </v-col>

              <v-col cols="12" md="6">
                <div class="output-section">
                  <div class="text-subtitle-1 font-weight-medium mb-2">
                    {{ translationDirection === 'vn-to-jp' ? 'Tiếng Nhật' : 'Tiếng Việt' }}
                  </div>
                  <div
                    class="translation-output"
                    :class="{'japanese-text': translationDirection === 'vn-to-jp', 'vietnamese-text': translationDirection === 'jp-to-vn'}"
                  >
                    <div v-if="translating" class="translation-loading pa-4">
                      <v-progress-circular indeterminate color="primary"></v-progress-circular>
                      <span class="ml-2">Đang dịch...</span>
                    </div>
                    <div v-else-if="translationResult" class="translation-text pa-4">
                      {{ translationResult }}
                    </div>
                    <div v-else class="empty-translation pa-4">
                      Bản dịch sẽ hiển thị ở đây
                    </div>
                  </div>
                  <div class="d-flex justify-space-between align-center mt-2">
                    <v-btn
                      size="small"
                      variant="text"
                      prepend-icon="mdi-content-copy"
                      @click="copyText(translationResult)"
                      :disabled="!translationResult"
                    >
                      Sao chép
                    </v-btn>
                    <v-btn
                      size="small"
                      variant="tonal"
                      color="primary"
                      prepend-icon="mdi-volume-high"
                      @click="speakText(translationResult)"
                      :disabled="!translationResult"
                    >
                      Đọc
                    </v-btn>
                  </div>
                </div>
              </v-col>
            </v-row>

            <v-row class="mt-4">
              <v-col cols="12" class="text-center">
                <v-btn
                  color="primary"
                  size="large"
                  :loading="translating"
                  :disabled="!sourceText"
                  @click="translateText"
                >
                  <v-icon left>mdi-translate</v-icon>
                  Dịch
                </v-btn>
              </v-col>
            </v-row>

            <!-- Translation History -->
            <v-row v-if="translationHistory.length > 0" class="mt-6">
              <v-col cols="12">
                <div class="d-flex justify-space-between align-center mb-3">
                  <h3 class="text-h6">Lịch sử dịch</h3>
                  <v-btn
                    size="small"
                    variant="text"
                    color="error"
                    @click="clearHistory"
                  >
                    Xóa lịch sử
                  </v-btn>
                </div>
                <v-list>
                  <v-list-item
                    v-for="(item, index) in translationHistory"
                    :key="index"
                    @click="loadFromHistory(item)"
                    class="history-item mb-2"
                  >
                    <template v-slot:prepend>
                      <v-icon :color="item.direction === 'vn-to-jp' ? 'green' : 'blue'">
                        mdi-translate
                      </v-icon>
                    </template>
                    <v-list-item-title>
                      <small class="text-caption text-secondary">
                        {{ item.direction === 'vn-to-jp' ? 'VN → JP' : 'JP → VN' }}
                        <v-chip size="x-small" class="ml-2" :color="item.isEconomy ? 'success' : 'primary'" variant="outlined">
                          {{ item.isEconomy ? 'GPT-3.5 Turbo' : 'GPT-4o' }}
                        </v-chip>
                      </small>
                      <span class="ml-2 text-truncate history-text">{{ item.source }}</span>
                    </v-list-item-title>
                    <v-list-item-subtitle class="text-truncate history-text">
                      {{ item.result }}
                    </v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <!-- Related Vocabulary Suggestions -->
        <v-card class="mt-6 related-vocab-card">
          <v-card-title>
            <v-icon color="primary" class="mr-2">mdi-information-outline</v-icon>
            Từ vựng liên quan
          </v-card-title>
          <v-card-text>
            <p class="text-body-2 text-medium-emphasis mb-4">
              Dưới đây là một số thuật ngữ IT thường được sử dụng liên quan đến bản dịch của bạn:
            </p>
            <v-row>
              <v-col
                v-for="(term, index) in relatedVocabulary"
                :key="index"
                cols="12" sm="6" md="4"
              >
                <v-card variant="outlined" class="vocab-item-card">
                  <v-card-text>
                    <div class="d-flex flex-column">
                      <span class="japanese-text text-subtitle-1">{{ term.japanese }}</span>
                      <span class="text-caption text-medium-emphasis">{{ term.vietnamese }}</span>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import authService from '@/services/auth.service'

interface TranslationHistoryItem {
  direction: string;
  source: string;
  result: string;
  isEconomy: boolean;
  timestamp: number;
}

interface VocabularyTerm {
  japanese: string;
  vietnamese: string;
}

export default defineComponent({
  name: 'TranslationView',

  setup() {
    const route = useRoute();
    const router = useRouter();

    const sourceText = ref('');
    const translationResult = ref('');
    const translating = ref(false);
    const translationDirection = ref('vn-to-jp'); // Default: Vietnamese to Japanese
    const translationHistory = ref<TranslationHistoryItem[]>([]);
    const useGpt4 = ref(false); // Default: Use GPT-3.5 Turbo (false = don't use GPT-4)

    // Mock related vocabulary (could be fetched based on translation content)
    const relatedVocabulary = ref<VocabularyTerm[]>([
      { japanese: 'プログラム', vietnamese: 'Chương trình' },
      { japanese: 'データベース', vietnamese: 'Cơ sở dữ liệu' },
      { japanese: 'ネットワーク', vietnamese: 'Mạng lưới' },
      { japanese: 'ソフトウェア', vietnamese: 'Phần mềm' },
      { japanese: 'セキュリティ', vietnamese: 'Bảo mật' },
      { japanese: 'バグ修正', vietnamese: 'Sửa lỗi' },
    ]);

    // Load data from URL if available
    onMounted(() => {
      // Check if there are query parameters
      if (route.query.text) {
        sourceText.value = route.query.text as string;

        if (route.query.dir) {
          translationDirection.value = route.query.dir as string;
        }

        // If there's a result parameter, just display it
        if (route.query.result) {
          translationResult.value = route.query.result as string;
        }
        // If there's text but no result, automatically translate
        else if (route.query.text) {
          // Trigger translation automatically
          translateText();
        }
      }

      // Load translation history from localStorage
      const savedHistory = localStorage.getItem('translationHistory');
      if (savedHistory) {
        try {
          translationHistory.value = JSON.parse(savedHistory);
        } catch (error) {
          console.error('Failed to parse translation history:', error);
        }
      }
    });

    // Save translation history when it changes
    watch(translationHistory, (newValue) => {
      localStorage.setItem('translationHistory', JSON.stringify(newValue));
    }, { deep: true });

    // Methods
    const translateText = async () => {
      if (!sourceText.value.trim()) return;

      translating.value = true;

      try {
        // Get the backend API URL
        const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

        // Get auth token (if available)
        const authToken = authService.getToken();

        // Create headers with authentication
        const headers: Record<string, string> = {
          'Content-Type': 'text/plain',
          'Accept': 'application/json'
        };

        // Add authentication token if available
        if (authToken) {
          headers['Authorization'] = `Bearer ${authToken}`;
        }

        // Choose endpoint based on GPT-4 selection
        const endpoint = useGpt4.value ? 'translate' : 'translate/economy';

        // Call the translation API with auth headers
        const response = await fetch(`${apiUrl}/api/v1/ai/${endpoint}?direction=${translationDirection.value}`, {
          method: 'POST',
          headers: headers,
          body: sourceText.value
        });

        if (!response.ok) {
          // Handle authentication errors specifically
          if (response.status === 401 || response.status === 403) {
            throw new Error('Bạn cần đăng nhập để sử dụng dịch vụ dịch thuật.');
          }
          throw new Error(`Dịch thuật thất bại: ${response.statusText}`);
        }

        const data = await response.json();
        translationResult.value = data.translation;

        // Add to translation history with economy flag
        addToHistory();
      } catch (error) {
        console.error('Translation error:', error);
        alert(error instanceof Error ? error.message : 'Dịch thuật thất bại. Vui lòng thử lại sau.');
      } finally {
        translating.value = false;
      }
    };

    const clearSourceText = () => {
      sourceText.value = '';
      translationResult.value = '';
    };

    const copyText = (text: string) => {
      if (!text) return;

      navigator.clipboard.writeText(text)
        .then(() => {
          // Optional: Show a success message
          alert('Đã sao chép vào clipboard!');
        })
        .catch(err => {
          console.error('Failed to copy text: ', err);
        });
    };

    const speakText = (text: string) => {
      if (!text) return;

      const utterance = new SpeechSynthesisUtterance(text);

      // Set the language based on the translation direction
      if (translationDirection.value === 'vn-to-jp') {
        utterance.lang = 'ja-JP'; // Japanese
      } else {
        utterance.lang = 'vi-VN'; // Vietnamese
      }

      window.speechSynthesis.speak(utterance);
    };

    const addToHistory = () => {
      // Add to the beginning of the array (most recent first)
      translationHistory.value.unshift({
        direction: translationDirection.value,
        source: sourceText.value,
        result: translationResult.value,
        isEconomy: !useGpt4.value, // false means GPT-4, true means economy
        timestamp: Date.now()
      });

      // Limit history size
      if (translationHistory.value.length > 10) {
        translationHistory.value = translationHistory.value.slice(0, 10);
      }
    };

    const loadFromHistory = (item: TranslationHistoryItem) => {
      translationDirection.value = item.direction;
      sourceText.value = item.source;
      translationResult.value = item.result;
      useGpt4.value = !item.isEconomy; // Convert from isEconomy to useGpt4
    };

    const clearHistory = () => {
      if (confirm('Bạn có chắc chắn muốn xóa lịch sử dịch?')) {
        translationHistory.value = [];
      }
    };

    return {
      sourceText,
      translationResult,
      translating,
      translationDirection,
      useGpt4,
      relatedVocabulary,
      translationHistory,
      translateText,
      clearSourceText,
      copyText,
      speakText,
      loadFromHistory,
      clearHistory
    };
  }
});
</script>

<style scoped lang="scss">
.translation-card {
  border-radius: 12px;
  overflow: hidden;
}

.translation-card-title {
  background-color: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
  font-size: 1.3rem;
  font-weight: 600;
}

.input-section, .output-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.source-textarea {
  flex-grow: 1;
}

.translation-output {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  min-height: 198px; // Match textarea height
  background-color: #f9f9f9;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-grow: 1;
}

.translation-text {
  width: 100%;
  height: 100%;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 198px;
}

.empty-translation {
  color: #9e9e9e;
  font-style: italic;
}

.translation-toggle-btn {
  min-width: 150px;
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
  line-height: 1.6;
}

.vietnamese-text {
  line-height: 1.6;
}

.history-item {
  border-radius: 8px;
  margin-bottom: 4px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f5f5f5;
  }
}

.history-text {
  max-width: 300px;
}

.related-vocab-card {
  border-radius: 12px;
}

.vocab-item-card {
  border-radius: 8px;
  height: 100%;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  }
}
</style>
