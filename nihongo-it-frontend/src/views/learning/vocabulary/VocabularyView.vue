<template>
  <v-container fluid>
    <div class="d-flex align-center mb-4">
      <h2 class="font-weight-bold" style="font-size: 1.3rem;">Từ vựng tiếng Nhật</h2>
      <v-spacer></v-spacer>
      <v-btn
        color="primary"
        variant="outlined"
        prepend-icon="mdi-bookmark-multiple"
        :to="{ name: 'savedVocabulary' }"
        class="ml-2"
      >
        Từ vựng đã lưu
      </v-btn>
    </div>

    <!-- Search and Filter Bar -->
    <v-card class="mb-6 filter-card elevation-1">
      <v-card-text>
        <v-row align="center">
          <!-- Search Box -->
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="filters.keyword"
              prepend-inner-icon="mdi-magnify"
              label="Tìm kiếm từ vựng"
              clearable
              hide-details
              density="comfortable"
              variant="outlined"
              @update:model-value="debounceSearch"
            ></v-text-field>
          </v-col>

          <!-- JLPT Level Filter -->
          <v-col cols="12" sm="3">
            <v-select
              v-model="filters.jlptLevel"
              label="Trình độ JLPT"
              :items="jlptLevels"
              clearable
              hide-details
              density="comfortable"
              variant="outlined"
              @update:model-value="fetchVocabulary"
            ></v-select>
          </v-col>

          <!-- Category Filter -->
          <v-col cols="12" sm="5">
            <v-select
              v-model="filters.category"
              label="Danh mục"
              :items="categories"
              clearable
              hide-details
              density="comfortable"
              variant="outlined"
              @update:model-value="fetchVocabulary"
            ></v-select>
          </v-col>
        </v-row>

        <!-- Active Filters -->
        <v-row class="mt-3" v-if="hasActiveFilters">
          <v-col cols="12">
            <div class="d-flex flex-wrap align-center">
              <span class="text-body-2 text-medium-emphasis mr-2">Bộ lọc đang dùng:</span>
              <v-chip
                v-if="filters.keyword"
                size="small"
                class="mr-2 mb-1"
                closable
                @click:close="clearFilter('keyword')"
              >
                Tìm kiếm: {{ filters.keyword }}
              </v-chip>
              <v-chip
                v-if="filters.jlptLevel"
                size="small"
                color="primary"
                class="mr-2 mb-1"
                closable
                @click:close="clearFilter('jlptLevel')"
              >
                Trình độ: {{ filters.jlptLevel }}
              </v-chip>
              <v-chip
                v-if="filters.category"
                size="small"
                color="success"
                class="mr-2 mb-1"
                closable
                @click:close="clearFilter('category')"
              >
                Danh mục: {{ filters.category }}
              </v-chip>
              <v-btn
                size="small"
                variant="text"
                density="comfortable"
                @click="clearAllFilters"
              >
                Xóa tất cả
              </v-btn>
            </div>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Loading State -->
    <div v-if="loading" class="d-flex justify-center my-8">
      <v-progress-circular
        indeterminate
        color="primary"
        size="64"
      ></v-progress-circular>
    </div>

    <!-- Custom Vocabulary Table -->
    <v-card v-else class="vocabulary-card elevation-2">
      <v-list class="custom-vocabulary-list pa-0">
        <div v-if="vocabulary.length === 0" class="text-center pa-6">
          <v-icon size="large" icon="mdi-book-search-outline" class="mb-4"></v-icon>
          <h3 class="text-h6">Không tìm thấy từ vựng</h3>
          <p class="text-body-1 text-medium-emphasis mb-4">
            Hãy điều chỉnh bộ lọc tìm kiếm hoặc thêm từ vựng mới.
          </p>
          <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddDialog">
            Thêm từ vựng
          </v-btn>
        </div>

        <template v-else>
          <!-- Table Header -->
          <div class="custom-header">
            <div class="d-flex px-4 py-3 text-subtitle-2 font-weight-bold w-100">
              <div class="header-cell column-border" style="width: 80px;">Trình độ</div>
              <div class="header-cell column-border" style="width: 450px;">Từ vựng</div>
              <div class="header-cell column-border" style="width: 200px;">Kanji</div>
              <div class="header-cell column-border" style="width: 150px;">Danh mục</div>
              <div class="header-cell column-border" style="flex-grow: 1;">Thao tác</div>
            </div>
          </div>

          <!-- Table Content -->
          <v-list-item
            v-for="item in vocabulary"
            :key="item.vocabId"
            class="vocabulary-item"
            :data-vocab-id="item.vocabId"
            @click="toggleExpand(item.vocabId)"
          >
            <!-- Main Row Content -->
            <div class="d-flex align-center w-100 vocabulary-row">
              <!-- JLPT Level -->
              <div class="content-cell text-center column-border" style="width: 80px;">
                <v-chip
                  :color="getJlptColor(item.jlptLevel)"
                  size="small"
                  class="font-weight-bold level-chip"
                >
                  {{ item.jlptLevel }}
                </v-chip>
              </div>

              <!-- Vocabulary (Hiragana/Katakana) with Meaning -->
              <div class="content-cell vocabulary-cell column-border" style="width: 450px;">
                <div class="d-flex align-center">
                  <span class="text-body-2 text-wrap hiragana-text">
                    {{ item.katakana ? item.katakana : item.hiragana }}
                  </span>
                  <span v-if="item.katakana && item.hiragana" class="text-caption ml-2 text-medium-emphasis text-wrap">({{ item.hiragana }})</span>
                </div>
                <div class="meaning-text text-caption mt-1 text-medium-emphasis text-wrap">
                  {{ item.meaning }}
                </div>
              </div>

              <!-- Kanji -->
              <div class="content-cell text-center column-border" style="width: 200px;">
                <span v-if="item.kanji" class="japanese-text kanji-text">{{ item.kanji }}</span>
                <span v-else class="text-medium-emphasis">—</span>
              </div>

              <!-- Category -->
              <div class="content-cell text-center column-border" style="width: 150px;">
                <v-chip
                  v-if="item.category"
                  size="x-small"
                  color="success"
                  variant="outlined"
                  class="category-chip"
                >
                  {{ item.category }}
                </v-chip>
                <span v-else class="text-medium-emphasis">—</span>
              </div>

              <!-- Actions -->
              <div class="content-cell text-center column-border action-cell" style="flex-grow: 1;" @click.stop>
                <div class="d-flex justify-center">
                  <v-btn
                    icon="mdi-volume-high"
                    size="small"
                    variant="text"
                    @click.stop="playAudio(item.audioPath || item.audioUrl || null, item)"
                    class="mr-2 action-btn"
                    color="blue"
                    title="Phát âm"
                    :loading="playingAudioId === item.vocabId"
                    :disabled="playingAudioId !== null && playingAudioId !== item.vocabId || playingExampleAudioId !== null"
                  ></v-btn>

                  <v-btn
                    :icon="item.isSaved ? 'mdi-bookmark' : 'mdi-bookmark-outline'"
                    size="small"
                    variant="text"
                    :color="item.isSaved ? 'warning' : undefined"
                    @click.stop="toggleSave(item)"
                    class="mr-2 action-btn"
                    :title="item.isSaved ? 'Bỏ lưu' : 'Lưu từ vựng'"
                  ></v-btn>

                  <v-btn
                    :icon="expandedItems.includes(item.vocabId) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                    size="small"
                    variant="text"
                    @click.stop="toggleExpand(item.vocabId)"
                    class="action-btn mr-2"
                    title="Hiện câu ví dụ"
                  ></v-btn>

                  <v-btn
                    size="small"
                    variant="outlined"
                    color="success"
                    @click.stop="toggleChatGPT(item.vocabId)"
                    class="chat-gpt-btn"
                    :disabled="loadingChatGPT === item.vocabId"
                    :loading="loadingChatGPT === item.vocabId"
                  >
                    <v-icon left size="small" class="mr-1">mdi-chat-processing</v-icon>
                    Hỏi ChatGPT
                  </v-btn>
                </div>
              </div>
            </div>

            <!-- Expanded Content (Example Sentences) -->
            <div v-if="expandedItems.includes(item.vocabId)" class="expanded-content py-2 px-4" @click.stop>
              <div v-if="item.exampleSentence" class="example-sentence my-2">
                <div class="d-flex align-items-center">
                  <v-icon class="mr-2" size="x-small" color="grey">mdi-format-quote-open</v-icon>
                  <div class="flex-grow-1 japanese-text">{{ item.exampleSentence }}</div>
                  <v-btn
                    icon="mdi-volume-high"
                    size="x-small"
                    variant="text"
                    @click.stop="playAudio(item.exampleAudioPath || null, item, true)"
                    color="blue"
                    title="Phát âm câu ví dụ"
                    class="action-btn"
                    :loading="playingExampleAudioId === item.vocabId"
                    :disabled="playingExampleAudioId !== null && playingExampleAudioId !== item.vocabId || playingAudioId !== null"
                  ></v-btn>
                </div>
                <div v-if="item.exampleSentenceTranslation" class="example-translation ml-6 mt-1 text-caption text-medium-emphasis">
                  {{ item.exampleSentenceTranslation }}
                </div>
              </div>

              <div class="additional-info d-flex flex-wrap mt-2">
                <div class="mr-4 mb-1">
                  <span class="text-caption text-medium-emphasis">Tạo bởi:</span>
                  <span class="text-caption ml-1">{{ item.createdBy || 'Admin' }}</span>
                </div>
                <div class="mr-4 mb-1">
                  <span class="text-caption text-medium-emphasis">Ngày tạo:</span>
                  <span class="text-caption ml-1">{{ formatDate(item.createdAt ?? '1999') }}</span>
                </div>
                <div class="ms-auto">
                  <v-btn
                    size="x-small"
                    variant="outlined"
                    color="primary"
                    @click.stop="viewDetails(item)"
                    class="text-none"
                  >
                    Xem chi tiết
                  </v-btn>
                </div>
              </div>
            </div>

            <!-- ChatGPT Content -->
            <div v-if="chatGPTItems.includes(item.vocabId)" class="chatgpt-content mt-0 py-2 px-4" @click.stop>
              <!-- Initial AI Explanation -->
              <v-card flat class="chatgpt-card pa-3 mb-3" v-if="item.aiExplanation">
                <div class="d-flex align-items-start mb-2">
                  <v-avatar size="32" color="green" class="mr-2">
                    <span class="text-caption text-white">AI</span>
                  </v-avatar>
                  <div>
                    <div class="text-subtitle-2 font-weight-medium">Trợ lý ChatGPT</div>
                    <div class="chatgpt-message text-body-2 mt-1">
                      <p v-html="displayedText[item.vocabId] || ''"></p>
                      <span v-if="typingInProgress === item.vocabId && typingExamples[item.vocabId] === undefined" class="typing-cursor">|</span>

                      <div v-if="item.aiExamples && item.aiExamples.length > 0 && typingExamples[item.vocabId] !== undefined" class="mt-3">
                        <p class="font-weight-medium">Câu ví dụ:</p>
                        <div v-for="(example, exIndex) in item.aiExamples" :key="exIndex" class="mt-2">
                          <template v-if="typingExamples[item.vocabId] > exIndex || typingExamples[item.vocabId] === -1">
                            <!-- Fully displayed examples -->
                            <p class="example-text">{{ example.japanese }}</p>
                            <p class="text-caption ml-3">{{ example.vietnamese }}</p>
                          </template>
                          <template v-else-if="typingExamples[item.vocabId] === exIndex">
                            <!-- Currently typing example -->
                            <p class="example-text">
                              {{ getExampleProperty(example, 'japaneseDisplayed') || '' }}
                              <span v-if="isJapaneseComplete(example) && !hasVietnameseStarted(example)" class="typing-cursor">|</span>
                            </p>
                            <p v-if="hasVietnameseStarted(example)" class="text-caption ml-3">
                              {{ getExampleProperty(example, 'vietnameseDisplayed') || '' }}
                              <span class="typing-cursor">|</span>
                            </p>
                          </template>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </v-card>

              <!-- Chat History -->
              <template v-if="item.chatHistory && item.chatHistory.length > 0">
                <div
                  v-for="(message, msgIndex) in (item.chatHistory as Array<{role: string, content: string}>)"
                  :key="msgIndex"
                  class="mb-3"
                >
                  <!-- User Message -->
                  <div v-if="message.role === 'user'" class="d-flex align-items-start">
                    <v-avatar size="32" color="blue" class="mr-2">
                      <span class="text-caption text-white">Bạn</span>
                    </v-avatar>
                    <div>
                      <div class="text-subtitle-2 font-weight-medium">Bạn</div>
                      <div class="chatgpt-message text-body-2 mt-1">
                        {{ message.content }}
                      </div>
                    </div>
                  </div>

                  <!-- AI Response -->
                  <v-card v-else flat class="chatgpt-card pa-3">
                    <div class="d-flex align-items-start">
                      <v-avatar size="32" color="green" class="mr-2">
                        <span class="text-caption text-white">AI</span>
                      </v-avatar>
                      <div>
                        <div class="text-subtitle-2 font-weight-medium">Trợ lý ChatGPT</div>
                        <div class="chatgpt-message text-body-2 mt-1">
                          <span v-html="message.content"></span>
                          <span v-if="typingInProgress === item.vocabId &&
                                      msgIndex === (item.chatHistory?.length - 1)"
                                class="typing-cursor">|</span>
                        </div>
                      </div>
                    </div>
                  </v-card>
                </div>
              </template>

              <!-- User Input -->
              <div class="chat-input-container d-flex mt-3">
                <v-text-field
                  v-model="chatInputs[item.vocabId]"
                  placeholder="Nhập tin nhắn của bạn..."
                  variant="outlined"
                  density="comfortable"
                  hide-details
                  @keyup.enter="sendChatMessage(item.vocabId)"
                  class="mr-2"
                ></v-text-field>
                <v-btn
                  color="primary"
                  :disabled="!chatInputs[item.vocabId]"
                  @click.stop="sendChatMessage(item.vocabId)"
                >
                  <v-icon>mdi-send</v-icon>
                </v-btn>
              </div>
            </div>

            <v-divider></v-divider>
          </v-list-item>
        </template>
      </v-list>

      <!-- Custom Pagination -->
      <v-card-actions class="d-flex align-center justify-space-between px-4 py-3 pagination-footer">
        <v-select
          v-model="filters.size"
          label="Số mục trên trang"
          :items="[10, 20, 50, 100]"
          density="compact"
          variant="outlined"
          style="max-width: 150px"
          @update:model-value="changeItemsPerPage"
          hide-details
        ></v-select>

        <v-pagination
          v-if="totalPages > 1"
          v-model="currentPage"
          :length="totalPages"
          :total-visible="7"
          rounded
          @update:model-value="changePage"
        ></v-pagination>

        <div v-else class="text-caption text-medium-emphasis">
          Hiển thị {{ vocabulary.length }} mục{{ vocabulary.length !== 1 ? '' : '' }}
        </div>

        <!-- Add Vocabulary Button -->
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="openAddDialog"
        >
          Thêm từ vựng
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { useToast } from 'vue-toast-notification'
import vocabularyService from '@/services/vocabulary.service'
import authService from '@/services/auth.service'
import type { VocabularyItem, VocabularyFilter, ExampleSentence, ChatMessage } from '@/services/vocabulary.service'
import axios from 'axios'

// Extended interface for examples with typing animation
interface AnimatedExample {
  japanese: string;
  vietnamese: string;
  japaneseDisplayed?: string;
  vietnameseDisplayed?: string;
}

@Component({
  name: 'VocabularyView'
})
export default class VocabularyView extends Vue {
  // Data
  vocabulary: VocabularyItem[] = []
  loading = false
  currentPage = 1
  totalPages = 0
  totalItems = 0
  expandedItems: string[] = []
  chatGPTItems: string[] = []
  loadingChatGPT: string | null = null
  chatInputs: Record<string, string> = {}
  playingAudioId: string | null = null
  playingExampleAudioId: string | null = null
  typingInProgress: string | null = null
  displayedText: Record<string, string> = {}
  typingSpeed = 20 // ms between characters
  typingExamples: Record<string, number> = {} // Track which example is currently being animated

  filters = {
    keyword: null as string | null,
    jlptLevel: null as string | null,
    category: null as string | null,
    page: 0,
    size: 10
  } as VocabularyFilter

  // Filter options
  jlptLevels = ['N1', 'N2', 'N3', 'N4', 'N5']
  categories = [
    'Programming',
    'Web Development',
    'AI/Machine Learning',
    'Cyber Security',
    'Database',
    'Technology',
    'Computer Hardware',
    'Project Management',
    'Networking',
    'Software Testing'
  ]
  // Computed Properties
  get hasActiveFilters(): boolean {
    return !!(
      this.filters.keyword ||
      this.filters.jlptLevel ||
      this.filters.category
    )
  }

  // Lifecycle Hooks
  async mounted() {
    this.fetchVocabulary();
  }

  // Methods
  toggleExpand(vocabId: string) {
    const index = this.expandedItems.indexOf(vocabId);

    if (index >= 0) {
      // If already expanded, collapse it
      this.expandedItems.splice(index, 1);
    } else {
      // If not expanded, expand it
      this.expandedItems.push(vocabId);
    }
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'Unknown';

    try {
      const date = new Date(dateString);
      return date.toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch (e) {
      return dateString;
    }
  }

  async fetchVocabulary() {
    this.loading = true;

    try {
      console.log('Fetching vocabulary with filters:', this.filters);
      const response = await vocabularyService.getVocabulary(this.filters);
      console.log('Vocabulary response:', response);

      if (response && Array.isArray(response.content)) {
        this.vocabulary = response.content;
        this.totalPages = response.totalPages;
        this.totalItems = response.totalElements;
        console.log('Processed vocabulary items:', this.vocabulary.length);
      } else {
        console.error('Invalid response format:', response);
        this.vocabulary = [];
        this.totalPages = 0;
        this.totalItems = 0;
        const toast = useToast();
        toast.error('Received invalid data format from server', {
          position: 'top',
          duration: 3000
        });
      }
    } catch (error) {
      console.error('Error fetching vocabulary:', error);
      this.vocabulary = [];
      this.totalPages = 0;
      this.totalItems = 0;
      const toast = useToast();
      toast.error('Failed to load vocabulary items', {
        position: 'top',
        duration: 3000
      });
    } finally {
      this.loading = false;
    }
  }

  debounceSearch = this.debounce(() => {
    this.fetchVocabulary()
  }, 500)

  debounce(fn: Function, delay: number) {
    let timeoutId: number | null = null
    return function(this: any, ...args: any[]) {
      if (timeoutId) {
        clearTimeout(timeoutId)
      }
      timeoutId = setTimeout(() => {
        fn.apply(this, args)
      }, delay)
    }
  }

  changePage(page: number) {
    this.currentPage = page;
    this.filters.page = page - 1; // API uses 0-based indexing
    this.expandedItems = []; // Close all expanded items when changing page
    this.fetchVocabulary();
  }

  changeItemsPerPage(size: number) {
    // Ensure size is a positive number
    const validSize = size > 0 ? size : 10;
    this.filters.size = validSize;
    this.filters.page = 0;
    this.currentPage = 1;
    this.expandedItems = []; // Close all expanded items when changing items per page
    this.fetchVocabulary();
  }

  clearFilter(filterName: 'keyword' | 'jlptLevel' | 'category') {
    this.filters[filterName] = null;
    this.fetchVocabulary();
  }

  clearAllFilters() {
    this.filters.keyword = null;
    this.filters.jlptLevel = null;
    this.filters.category = null;
    this.filters.page = 0;
    this.currentPage = 1;
    this.fetchVocabulary();
  }

  async toggleSave(item: VocabularyItem) {
    const toast = useToast();
    try {
      if (item.isSaved) {
        await vocabularyService.removeSavedVocabulary(item.vocabId);
        toast.success('Removed from saved items', {
          position: 'top',
          duration: 2000
        });
      } else {
        await vocabularyService.saveVocabulary(item.vocabId);
        toast.success('Added to saved items', {
          position: 'top',
          duration: 2000
        });
      }

      // Toggle the state locally
      item.isSaved = !item.isSaved;

    } catch (error) {
      console.error('Error toggling save status:', error);
      toast.error('Failed to update saved status', {
        position: 'top',
        duration: 3000
      });
    }
  }

  getJlptColor(level: string): string {
    switch (level) {
      case 'N1': return 'red';
      case 'N2': return 'orange';
      case 'N3': return 'amber';
      case 'N4': return 'light-green';
      case 'N5': return 'green';
      default: return 'grey';
    }
  }

  async playAudio(audioPath: string | null, vocabItem?: VocabularyItem, isExampleSentence: boolean = false): Promise<void> {
    const toast = useToast();

    if (!vocabItem) {
      toast.error('Could not identify vocabulary item');
      return;
    }

    // Set loading state based on whether it's a word or example
    if (isExampleSentence) {
      this.playingExampleAudioId = vocabItem.vocabId;
    } else {
      this.playingAudioId = vocabItem.vocabId;
    }

    try {
      if (audioPath) {
        // If there's an existing audio path, use it
        const audio = new Audio(audioPath);
        await audio.play();
      } else {
        // Verify authentication before proceeding
        const authToken = authService.getToken();
        if (!authToken) {
          toast.error('Please log in to use text-to-speech', {
            position: 'top',
            duration: 4000
          });
          // Redirect to login page after a short delay
          setTimeout(() => {
            this.$router.push({
              name: 'login',
              query: { redirect: this.$route.fullPath }
            });
          }, 1500);
          return;
        }

        // No audio path available, use TTS API
        // Get the item either from the parameter or by finding it in the vocabulary array
        const currentItem = vocabItem || this.getCurrentItemFromEvent();

        if (!currentItem) {
          toast.warning('Could not identify vocabulary item', {
            position: 'top',
            duration: 3000
          });
          return;
        }

        // Determine which text to use for speech
        let textToSpeak = '';

        if (isExampleSentence && currentItem.exampleSentence) {
          // Use example sentence if requested and available
          textToSpeak = currentItem.exampleSentence;
        } else {
          // Use vocabulary word based on priority: katakana -> kanji -> hiragana
          if (currentItem.katakana) {
            textToSpeak = currentItem.katakana;
          } else if (currentItem.kanji) {
            textToSpeak = currentItem.kanji;
          } else if (currentItem.hiragana) {
            textToSpeak = currentItem.hiragana;
          } else {
            toast.warning('No Japanese text available for this vocabulary', {
              position: 'top',
              duration: 3000
            });
            return;
          }
        }

        // Show loading indicator
        toast.info('Generating audio...', {
          position: 'top',
          duration: 2000
        });

        // Get the backend API URL
        const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

        // Set speed: 0.9 for vocabulary, 1.0 for example sentences
        const speed = isExampleSentence ? 1.0 : 0.9;

        // Call the TTS API with Authorization header
        const response = await axios.post(`${apiUrl}/api/v1/tts/generate`, textToSpeak, {
          headers: {
            'Content-Type': 'text/plain; charset=UTF-8',
            'Accept-Language': 'ja-JP',
            'X-Speech-Speed': speed.toString(),
            'X-Content-Language': 'ja',
            'X-Content-Is-Example': isExampleSentence.toString(),
            'Authorization': `Bearer ${authToken}`,
            'Accept': 'audio/mpeg'
          },
          responseType: 'arraybuffer'
        });

        // Convert response to blob and create audio URL
        const audioBlob = new Blob([response.data], { type: 'audio/mpeg' });
        const audioUrl = URL.createObjectURL(audioBlob);

        // Play the audio
        const audio = new Audio(audioUrl);
        audio.onended = () => {
          URL.revokeObjectURL(audioUrl);
          if (isExampleSentence) {
            this.playingExampleAudioId = null;
          } else {
            this.playingAudioId = null;
          }
        };

        await audio.play();
      }
    } catch (error) {
      console.error('Error generating or playing TTS audio:', error);

      // Special handling for 401 errors to prevent logout
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        toast.error('TTS service requires authentication. Please log in again when convenient.', {
          position: 'top',
          duration: 3000
        });
      } else {
        toast.error(error instanceof Error ? error.message : 'Failed to generate speech', {
          position: 'top',
          duration: 3000
        });
      }
    } finally {
      // Reset loading state if no audio was played (in case of error)
      // For successful audio playback, the onended event will handle this
      if (!audioPath) {
        setTimeout(() => {
          if (isExampleSentence) {
            this.playingExampleAudioId = null;
          } else {
            this.playingAudioId = null;
          }
        }, 3000);
      } else {
        if (isExampleSentence) {
          this.playingExampleAudioId = null;
        } else {
          this.playingAudioId = null;
        }
      }
    }
  }

  // Helper method to get current item from event context
  getCurrentItemFromEvent(): VocabularyItem | null {
    // This relies on the event coming from a button inside a list item
    // The list item has the vocab ID
    const button = event?.target as HTMLElement;
    const listItem = button?.closest('.vocabulary-item');
    const vocabId = listItem?.getAttribute('data-vocab-id');

    if (vocabId) {
      return this.vocabulary.find(item => item.vocabId === vocabId) || null;
    }

    return null;
  }

  viewDetails(item: VocabularyItem) {
    // To be implemented - show detailed view or navigate to detail page
    this.$router.push(`/vocabulary/${item.vocabId}`);
  }

  openAddDialog() {
    // To be implemented - open dialog to add new vocabulary
    // This is a placeholder for future functionality
  }

  toggleChatGPT(vocabId: string) {
    const index = this.chatGPTItems.indexOf(vocabId);

    if (index >= 0) {
      // If already open, close it
      this.chatGPTItems.splice(index, 1);
    } else {
      // Start loading state
      this.loadingChatGPT = vocabId;

      // Find the vocabulary item by ID
      const vocabItem = this.vocabulary.find(item => item.vocabId === vocabId);

      if (!vocabItem) {
        console.error('Vocabulary item not found');
        this.loadingChatGPT = null;
        return;
      }

      // Initialize chat input for this item if not exists
      if (!this.chatInputs[vocabId]) {
        this.chatInputs[vocabId] = '';
      }

      // Get the backend API URL
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

      // Query the AI for vocabulary explanation
      axios.post(`${apiUrl}/api/v1/ai/vocabulary/explain`, null, {
        params: {
          kanji: vocabItem.kanji || '',
          hiragana: vocabItem.hiragana || '',
          katakana: vocabItem.katakana || '',
          meaning: vocabItem.meaning || '',
          category: vocabItem.category || '',
          exampleSentence: vocabItem.exampleSentence || ''
        }
      })
      .then(response => {
        // Store the AI response for this vocabulary
        const vocabIndex = this.vocabulary.findIndex(item => item.vocabId === vocabId);
        if (vocabIndex !== -1) {
          let data = response.data;
          // If data is a string, try to parse it as JSON
          if (typeof data === 'string') {
            try {
              // Clean string by removing markdown code blocks if present
              const cleanedString = data
                .replace(/```json/g, '')
                .replace(/```/g, '')
                .trim();

              data = JSON.parse(cleanedString);
            } catch (error) {
              console.error('Failed to parse response as JSON:', error);
              // If parsing fails, create a fallback object
              data = {
                explanation: data, // Use the raw string as explanation
                examples: []
              };
            }
          }

          // Add AI response data to the vocabulary item
          this.vocabulary[vocabIndex].aiExplanation = data.explanation;
          this.vocabulary[vocabIndex].aiExamples = data.examples || [];

          // Initialize the displayed text as empty
          this.displayedText[vocabId] = '';

          // Add to visible chatGPT items
          this.chatGPTItems.push(vocabId);

          // Start the typing animation
          this.animateTyping(vocabId, data.explanation);
        }
      })
      .catch(error => {
        console.error('Error fetching AI explanation:', error);
        const toast = useToast();
        toast.error('Failed to fetch AI explanation', {
          position: 'top',
          duration: 3000
        });
      })
      .finally(() => {
        this.loadingChatGPT = null;
      });
    }
  }

  animateTyping(vocabId: string, text: string) {
    this.typingInProgress = vocabId;
    let currentIndex = 0;
    this.displayedText[vocabId] = '';

    const typeNextChar = () => {
      if (currentIndex < text.length) {
        this.displayedText[vocabId] += text.charAt(currentIndex);
        currentIndex++;
        setTimeout(typeNextChar, this.typingSpeed);
      } else {
        // Start animating examples after main text is complete
        const vocabItem = this.vocabulary.find(item => item.vocabId === vocabId);
        if (vocabItem?.aiExamples && vocabItem.aiExamples.length > 0) {
          this.typingExamples[vocabId] = 0;
          this.animateExamples(vocabId, vocabItem.aiExamples);
        } else {
          this.typingInProgress = null;
        }
      }
    };

    typeNextChar();
  }

  animateExamples(vocabId: string, examples: any[]) {
    if (!examples || examples.length === 0 || this.typingExamples[vocabId] >= examples.length) {
      this.typingExamples[vocabId] = -1; // All examples are displayed
      this.typingInProgress = null;
      return;
    }

    const currentExampleIndex = this.typingExamples[vocabId];
    const example = examples[currentExampleIndex] as AnimatedExample;

    // Animate Japanese text first
    this.animateExampleText(vocabId, 'japanese', example.japanese, () => {
      // Then animate Vietnamese text
      this.animateExampleText(vocabId, 'vietnamese', example.vietnamese, () => {
        // Move to next example
        this.typingExamples[vocabId]++;
        setTimeout(() => {
          this.animateExamples(vocabId, examples);
        }, 300); // Pause between examples
      });
    });
  }

  animateExampleText(vocabId: string, field: string, text: string, callback: () => void) {
    let currentIndex = 0;
    const exampleIndex = this.typingExamples[vocabId];

    // Initialize the example text if needed
    const vocabIndex = this.vocabulary.findIndex(item => item.vocabId === vocabId);
    if (vocabIndex !== -1 && this.vocabulary[vocabIndex].aiExamples) {
      const example = this.vocabulary[vocabIndex].aiExamples[exampleIndex] as any;
      if (!example[field + 'Displayed']) {
        example[field + 'Displayed'] = '';
      }
    }

    const typeNextChar = () => {
      if (currentIndex < text.length) {
        const vocabIndex = this.vocabulary.findIndex(item => item.vocabId === vocabId);
        if (vocabIndex !== -1 && this.vocabulary[vocabIndex].aiExamples) {
          const example = this.vocabulary[vocabIndex].aiExamples[exampleIndex] as any;
          example[field + 'Displayed'] = text.substring(0, currentIndex + 1);
        }
        currentIndex++;
        setTimeout(typeNextChar, this.typingSpeed);
      } else {
        callback();
      }
    };

    typeNextChar();
  }

  async sendChatMessage(vocabId: string) {
    if (!this.chatInputs[vocabId]) return;

    const message = this.chatInputs[vocabId];
    this.chatInputs[vocabId] = ''; // Clear input immediately for better UX

    const vocabItem = this.vocabulary.find(item => item.vocabId === vocabId);
    if (!vocabItem) {
      console.error('Vocabulary item not found');
      return;
    }

    // Create a local copy to maintain reactivity
    const localMessageHistory = vocabItem.chatHistory || [];

    // Add user message to history
    localMessageHistory.push({
      role: 'user',
      content: message
    });

    // Update the vocabulary item with the new message
    const vocabIndex = this.vocabulary.findIndex(item => item.vocabId === vocabId);
    if (vocabIndex !== -1) {
      // Ensure chatHistory exists and is an array
      if (!this.vocabulary[vocabIndex].chatHistory) {
        this.vocabulary[vocabIndex].chatHistory = [];
      }
      this.vocabulary[vocabIndex].chatHistory = [...localMessageHistory];
    }

    // Fetch AI response for the user message
    try {
      // Get the backend API URL
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

      const vocabWord = vocabItem.hiragana || '';

      const response = await axios.post(`${apiUrl}/api/v1/ai/vocabulary/chat`, null, {
        params: {
          vocabWord: vocabWord,
          userMessage: message
        }
      });

      // Process the response data
      let data = response.data;
      // If data is a string, try to parse it as JSON
      if (typeof data === 'string') {
        try {
          // Clean string by removing markdown code blocks if present
          const cleanedString = data
            .replace(/```json/g, '')
            .replace(/```/g, '')
            .trim();

          data = JSON.parse(cleanedString);
        } catch (error) {
          console.error('Failed to parse chat response as JSON:', error);
          // If parsing fails, create a fallback object with raw string as message
          data = {
            message: data
          };
        }
      }

      // Add AI response to chat history
      if (vocabIndex !== -1) {
        // Ensure chatHistory exists and is an array
        if (!this.vocabulary[vocabIndex].chatHistory) {
          this.vocabulary[vocabIndex].chatHistory = [];
        }

        // Use the message from the parsed data, or a fallback message
        const responseMessage = data && data.message
          ? data.message
          : "I'm sorry, I don't have a specific response for that. Could you try asking something else?";

        // Add empty response message first (for typing animation)
        const aiMessageIndex = this.vocabulary[vocabIndex].chatHistory!.length;
        this.vocabulary[vocabIndex].chatHistory!.push({
          role: 'assistant',
          content: '' // Empty content initially
        });

        // Start typing animation for the response
        this.animateTypingChatResponse(vocabId, responseMessage, vocabIndex, aiMessageIndex);
      }
    } catch (error) {
      console.error('Error sending chat message:', error);

      const toast = useToast();
      toast.error('Failed to get AI response', {
        position: 'top',
        duration: 3000
      });

      // Add error message to chat history
      if (vocabIndex !== -1) {
        // Ensure chatHistory exists and is an array
        if (!this.vocabulary[vocabIndex].chatHistory) {
          this.vocabulary[vocabIndex].chatHistory = [];
        }
        this.vocabulary[vocabIndex].chatHistory!.push({
          role: 'assistant',
          content: 'Sorry, I encountered an error processing your request. Please try again.'
        });
      }
    }
  }

  animateTypingChatResponse(vocabId: string, text: string, vocabIndex: number, messageIndex: number) {
    this.typingInProgress = vocabId;
    let currentIndex = 0;

    const typeNextChar = () => {
      if (currentIndex < text.length) {
        if (this.vocabulary[vocabIndex]?.chatHistory &&
            this.vocabulary[vocabIndex].chatHistory![messageIndex]) {
          this.vocabulary[vocabIndex].chatHistory![messageIndex].content =
            text.substring(0, currentIndex + 1);
        }
        currentIndex++;
        setTimeout(typeNextChar, this.typingSpeed);
      } else {
        this.typingInProgress = null;
      }
    };

    typeNextChar();
  }

  // Helper method to safely access example display properties
  getExampleProperty(example: any, property: string): string {
    return example && example[property] ? example[property] : '';
  }

  // Check if example has complete Japanese text
  isJapaneseComplete(example: any): boolean {
    return example &&
           example.japaneseDisplayed &&
           example.japanese &&
           example.japaneseDisplayed.length === example.japanese.length;
  }

  // Check if Vietnamese display has started
  hasVietnameseStarted(example: any): boolean {
    return example && example.vietnameseDisplayed ? true : false;
  }
}
</script>

<style lang="sass" scoped>
.filter-card
  border-radius: 8px
  background-color: #f9fafc
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05)

.vocabulary-card
  border-radius: 8px
  overflow: hidden

.custom-vocabulary-list
  padding: 0

.custom-header
  background-color: #f0f4f8
  border-bottom: 1px solid rgba(0, 0, 0, 0.12)

.header-cell
  text-align: center
  padding: 0 8px
  color: rgba(0, 0, 0, 0.7)
  font-weight: 600
  display: flex
  align-items: center
  justify-content: center
  text-transform: uppercase
  font-size: 0.8rem
  letter-spacing: 0.5px

.column-border
  border-right: 1px solid rgba(0, 0, 0, 0.08)
  &:last-of-type
    border-right: none

.content-cell
  padding: 2px 8px
  overflow: hidden

.vocabulary-cell
  padding: 2px 12px
  min-height: 60px
  display: flex
  flex-direction: column
  justify-content: center

.text-wrap
  word-break: break-word
  white-space: normal
  overflow-wrap: break-word
  max-width: 100%

.vocabulary-item
  padding: 0
  cursor: pointer
  border-bottom: 1px solid rgba(0, 0, 0, 0.05)
  transition: background-color 0.2s ease

  &:hover
    background-color: rgba(0, 0, 0, 0.02)

.vocabulary-row
  padding: 8px 16px
  min-height: 54px
  width: 100%

.japanese-text
  font-family: 'Noto Sans JP', sans-serif

.hiragana-text
  font-weight: 500
  color: rgba(0, 0, 0, 0.8)

.kanji-text
  color: rgba(0, 0, 0, 0.85)
  font-weight: 600

.meaning-text
  font-style: italic
  color: rgba(0, 0, 0, 0.6)

.expanded-content
  background-color: #f8fafd
  border-top: 1px dashed rgba(0, 0, 0, 0.08)
  padding: 6px 16px

.example-sentence
  padding: 8px 10px
  background-color: rgba(0, 0, 0, 0.02)
  border-radius: 6px
  border-left: 3px solid rgba(0, 0, 0, 0.1)
  font-style: italic
  margin: 6px 0

.example-translation
  color: rgba(0, 0, 0, 0.6)
  font-style: italic

.level-chip
  min-width: 40px
  font-size: 0.75rem

.category-chip
  font-size: 0.65rem

.action-btn
  opacity: 0.8
  transition: all 0.2s ease

  &:hover
    opacity: 1
    transform: scale(1.1)

.pagination-footer
  background-color: #f9fafc
  border-top: 1px solid rgba(0, 0, 0, 0.05)

.action-cell
  display: flex
  justify-content: center

.chatgpt-content
  background-color: #f0f7ff
  border-top: 1px dashed rgba(0, 150, 0, 0.15)
  border-bottom: 1px dashed rgba(0, 150, 0, 0.15)

.chatgpt-card
  background-color: rgba(255, 255, 255, 0.7)
  border-radius: 12px

.chatgpt-message
  line-height: 1.5
  color: rgba(0, 0, 0, 0.7)

.example-text
  color: rgba(0, 0, 0, 0.6)
  font-style: italic
  padding-left: 12px
  border-left: 2px solid rgba(0, 150, 0, 0.3)

.chat-input-container
  background-color: rgba(255, 255, 255, 0.8)
  padding: 8px
  border-radius: 8px

.chat-gpt-btn
  font-size: 0.75rem
  height: 28px
  opacity: 0.9
  transition: all 0.2s ease

  &:hover
    opacity: 1
    transform: scale(1.05)

.additional-info
  display: flex
  flex-wrap: wrap
  margin-top: 6px

  .text-caption
    font-size: 0.7rem

// Japanese text styling similar to Jisho
.japanese-text
  font-size: 1.2rem
  line-height: 1.6

// Example sentence styling
.example-sentence
  background-color: #f9f9f9
  border-radius: 8px
  padding: 10px

  .japanese-text
    font-size: 1.1rem
    line-height: 1.6

  .example-translation
    margin-top: 5px
    color: #666
    font-style: italic

.typing-cursor
  display: inline-block
  width: 2px
  height: 16px
  background: #333
  margin-left: 2px
  animation: blink 0.7s infinite

@keyframes blink
  0%, 100%
    opacity: 1
  50%
    opacity: 0
</style>
