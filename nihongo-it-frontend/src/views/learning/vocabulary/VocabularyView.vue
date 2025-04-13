<template>
  <v-container fluid>
    <h2 class="font-weight-bold mb-1" style="font-size: 1.3rem;">Nihongo Vocabulary</h2>

    <!-- Search and Filter Bar -->
    <v-card class="mb-6 filter-card elevation-1">
      <v-card-text>
        <v-row align="center">
          <!-- Search Box -->
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="filters.keyword"
              prepend-inner-icon="mdi-magnify"
              label="Search vocabulary"
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
              label="JLPT Level"
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
              label="Category"
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
              <span class="text-body-2 text-medium-emphasis mr-2">Active filters:</span>
              <v-chip
                v-if="filters.keyword"
                size="small"
                class="mr-2 mb-1"
                closable
                @click:close="clearFilter('keyword')"
              >
                Search: {{ filters.keyword }}
              </v-chip>
              <v-chip
                v-if="filters.jlptLevel"
                size="small"
                color="primary"
                class="mr-2 mb-1"
                closable
                @click:close="clearFilter('jlptLevel')"
              >
                Level: {{ filters.jlptLevel }}
              </v-chip>
              <v-chip
                v-if="filters.category"
                size="small"
                color="success"
                class="mr-2 mb-1"
                closable
                @click:close="clearFilter('category')"
              >
                Category: {{ filters.category }}
              </v-chip>
              <v-btn
                size="small"
                variant="text"
                density="comfortable"
                @click="clearAllFilters"
              >
                Clear all
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
          <h3 class="text-h6">No vocabulary found</h3>
          <p class="text-body-1 text-medium-emphasis mb-4">
            Try adjusting your search filters or adding new vocabulary.
          </p>
          <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddDialog">
            Add vocabulary
          </v-btn>
        </div>

        <template v-else>
          <!-- Table Header -->
          <div class="custom-header">
            <div class="d-flex px-4 py-3 text-subtitle-2 font-weight-bold w-100">
              <div class="header-cell column-border" style="width: 80px;">Level</div>
              <div class="header-cell column-border" style="width: 450px;">Vocabulary</div>
              <div class="header-cell column-border" style="width: 200px;">Kanji</div>
              <div class="header-cell column-border" style="width: 150px;">Category</div>
              <div class="header-cell column-border" style="flex-grow: 1;">Actions</div>
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
                  <span class="text-body-2 text-wrap hiragana-text">{{ item.hiragana }}</span>
                  <span v-if="item.katakana" class="text-caption ml-2 text-medium-emphasis text-wrap">({{ item.katakana }})</span>
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
                    title="Play pronunciation"
                  ></v-btn>

                  <v-btn
                    :icon="item.isSaved ? 'mdi-bookmark' : 'mdi-bookmark-outline'"
                    size="small"
                    variant="text"
                    :color="item.isSaved ? 'warning' : undefined"
                    @click.stop="toggleSave(item)"
                    class="mr-2 action-btn"
                  ></v-btn>

                  <v-btn
                    :icon="expandedItems.includes(item.vocabId) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                    size="small"
                    variant="text"
                    @click.stop="toggleExpand(item.vocabId)"
                    class="action-btn mr-2"
                    title="Show example sentences"
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
                  <div class="flex-grow-1 japanese-text" ref="exampleText" v-html="getInitialFurigana(item.exampleSentence)" :data-text="item.exampleSentence"></div>
                  <v-btn
                    icon="mdi-volume-high"
                    size="x-small"
                    variant="text"
                    @click.stop="playAudio(item.exampleAudioPath || null, item, true)"
                    color="blue"
                    title="Play example audio"
                    class="action-btn"
                  ></v-btn>
                </div>
                <div v-if="item.exampleSentenceTranslation" class="example-translation ml-6 mt-1 text-caption text-medium-emphasis">
                  {{ item.exampleSentenceTranslation }}
                </div>
              </div>

              <div class="additional-info d-flex flex-wrap mt-2">
                <div class="mr-4 mb-1">
                  <span class="text-caption text-medium-emphasis">Created by:</span>
                  <span class="text-caption ml-1">{{ item.createdBy }}</span>
                </div>
                <div class="mr-4 mb-1">
                  <span class="text-caption text-medium-emphasis">Created on:</span>
                  <span class="text-caption ml-1">{{ formatDate(item.createdAt) }}</span>
                </div>
                <div class="ms-auto">
                  <v-btn
                    size="x-small"
                    variant="outlined"
                    color="primary"
                    @click.stop="viewDetails(item)"
                    class="text-none"
                  >
                    View Details
                  </v-btn>
                </div>
              </div>
            </div>

            <!-- ChatGPT Content -->
            <div v-if="chatGPTItems.includes(item.vocabId)" class="chatgpt-content mt-0 py-2 px-4" @click.stop>
              <v-card flat class="chatgpt-card pa-3 mb-3">
                <div class="d-flex align-items-start mb-2">
                  <v-avatar size="32" color="green" class="mr-2">
                    <span class="text-caption text-white">AI</span>
                  </v-avatar>
                  <div>
                    <div class="text-subtitle-2 font-weight-medium">ChatGPT Assistant</div>
                    <div class="chatgpt-message text-body-2 mt-1">
                      <p>「{{ item.hiragana }}{{ item.kanji ? ` (${item.kanji})` : '' }}」について説明します：</p>
                      <p class="mt-2">これは「{{ item.meaning }}」という意味の日本語の単語です。</p>
                      <p class="mt-2">例文：</p>
                      <p class="mt-1 example-text">{{ item.exampleSentence || '彼はいつも新しい技術について' + item.hiragana + 'を持っています。' }}</p>
                      <p class="mt-1 example-text">この{{ item.hiragana }}は日本のIT業界でよく使われています。</p>
                      <p class="mt-2">他の例文：</p>
                      <p class="mt-1 example-text">プログラミングを学ぶには、{{ item.hiragana }}と忍耐力が必要です。</p>
                    </div>
                  </div>
                </div>
              </v-card>

              <div class="d-flex align-items-start mb-3">
                <v-avatar size="32" color="blue" class="mr-2">
                  <span class="text-caption text-white">You</span>
                </v-avatar>
                <div>
                  <div class="text-subtitle-2 font-weight-medium">You</div>
                  <div class="chatgpt-message text-body-2 mt-1">
                    「{{ item.hiragana }}」を使った他の例文をくださいませんか？
                  </div>
                </div>
              </div>

              <v-card flat class="chatgpt-card pa-3 mb-3">
                <div class="d-flex align-items-start">
                  <v-avatar size="32" color="green" class="mr-2">
                    <span class="text-caption text-white">AI</span>
                  </v-avatar>
                  <div>
                    <div class="text-subtitle-2 font-weight-medium">ChatGPT Assistant</div>
                    <div class="chatgpt-message text-body-2 mt-1">
                      <p>はい、「{{ item.hiragana }}」を使った例文をいくつか紹介します：</p>
                      <p class="mt-2 example-text">1. 彼は新しいプロジェクトに取り組む{{ item.hiragana }}を持っています。</p>
                      <p class="mt-1 example-text">2. この会社の成功は社員の{{ item.hiragana }}と努力のおかげです。</p>
                      <p class="mt-1 example-text">3. 技術者として成功するには、常に学ぶ{{ item.hiragana }}が必要です。</p>
                      <p class="mt-1 example-text">4. 彼女は常に前向きな{{ item.hiragana }}で仕事に取り組んでいます。</p>
                      <p class="mt-1 example-text">5. 新しいソフトウェアを開発する{{ item.hiragana }}が生まれました。</p>
                    </div>
                  </div>
                </div>
              </v-card>

              <!-- User Input -->
              <div class="chat-input-container d-flex mt-3">
                <v-text-field
                  v-model="chatInputs[item.vocabId]"
                  placeholder="メッセージを入力してください..."
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
          label="Items per page"
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
          Showing {{ vocabulary.length }} item{{ vocabulary.length !== 1 ? 's' : '' }}
        </div>

        <!-- Add Vocabulary Button -->
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="openAddDialog"
        >
          Add Vocabulary
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
import type { VocabularyItem, VocabularyFilter } from '@/services/vocabulary.service'
import axios from 'axios'
import { addFurigana, addFuriganaAsync, addReading } from '@/utils/furiganaHelper'

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
  furiganaCache: Record<string, string> = {} // Cache for processed sentences

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
    this.preloadCommonReadings();
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

    if (audioPath) {
      // If there's an existing audio path, use it
      try {
        const audio = new Audio(audioPath);
        await audio.play();
      } catch (error) {
        console.error('Error playing audio:', error);
        toast.error('Failed to play audio', {
          position: 'top',
          duration: 3000
        });
      }
      return;
    }

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
    try {
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
        // Use vocabulary word based on new priority: katakana -> kanji -> hiragana
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
      await audio.play();

      // Clean up the object URL after playing
      audio.onended = () => {
        URL.revokeObjectURL(audioUrl);
      };

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
      // Simulate loading
      this.loadingChatGPT = vocabId;

      // Simulate API call delay
      setTimeout(() => {
        this.chatGPTItems.push(vocabId);
        this.loadingChatGPT = null;

        // Initialize chat input for this item if not exists
        if (!this.chatInputs[vocabId]) {
          this.chatInputs[vocabId] = '';
        }
      }, 800);
    }
  }

  sendChatMessage(vocabId: string) {
    if (this.chatInputs[vocabId]) {
      console.log('Sending message for vocab ID:', vocabId, 'Message:', this.chatInputs[vocabId]);
      // In a real implementation, this would send the message to an API
      // For demo purposes, we just clear the input
      this.chatInputs[vocabId] = '';
    }
  }

  // Method for immediate display of text while async processing happens
  getInitialFurigana(text: string): string {
    if (!text) return '';

    // Check cache first
    if (this.furiganaCache[text]) {
      return this.furiganaCache[text];
    }

    // Get initial display with basic processing
    const initialResult = addFurigana(text);

    // Trigger async processing
    this.processFuriganaAsync(text);

    return initialResult;
  }

  // Process furigana asynchronously and update display
  async processFuriganaAsync(text: string): Promise<void> {
    try {
      const result = await addFuriganaAsync(text);

      // Update cache
      this.furiganaCache[text] = result;

      // Force update if needed - find elements with this text and update them
      this.$nextTick(() => {
        const elements = this.$refs.exampleText;
        if (elements) {
          // Could be an array or single element
          const targets = Array.isArray(elements) ? elements : [elements];

          for (const el of targets) {
            if (el && el.getAttribute('data-text') === text) {
              el.innerHTML = result;
            }
          }
        }
      });
    } catch (error) {
      console.error('Error with async furigana processing:', error);
    }
  }

  // Preload common readings for IT-related Japanese words
  preloadCommonReadings(): void {
    // IT terms
    addReading('技術', 'ぎじゅつ');
    addReading('情報', 'じょうほう');
    addReading('通信', 'つうしん');
    addReading('開発', 'かいはつ');
    addReading('言語', 'げんご');
    addReading('計算機', 'けいさんき');
    addReading('安全', 'あんぜん');
    addReading('化', 'か');
    addReading('専門', 'せんもん');
    addReading('知能', 'ちのう');
    addReading('画像', 'がぞう');
    addReading('処理', 'しょり');
    addReading('設計', 'せっけい');
    addReading('管理', 'かんり');
    addReading('用語', 'ようご');
    addReading('業務', 'ぎょうむ');
    addReading('品質', 'ひんしつ');

    // Japanese language terms
    addReading('日本語', 'にほんご');
    addReading('漢字', 'かんじ');
    addReading('ふりがな', 'ふりがな');
    addReading('単語', 'たんご');
    addReading('文法', 'ぶんぽう');
    addReading('語彙', 'ごい');
    addReading('発音', 'はつおん');
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

:deep(ruby)
  ruby-align: center

  rt
    font-size: 0.5em
    line-height: 1
    color: rgba(0, 0, 0, 0.6)
    text-align: center
    margin-bottom: -0.1em
</style>
