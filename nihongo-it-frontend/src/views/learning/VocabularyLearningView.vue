<template>
  <div class="vocabulary-learning-container">
    <!-- Header Section -->
    <div class="header px-4 py-3 d-flex justify-space-between align-center">
      <div class="d-flex align-center">
        <v-btn icon class="mr-2" @click="goBack">
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
        <div class="text-h6 font-weight-bold text-dark">
          <span class="text-primary">IT</span>
          <span class="japanese-text">単語学習</span>
        </div>
      </div>
      <div class="d-flex align-center">
        <v-btn icon variant="text" @click="openFilterDialog">
          <v-icon>mdi-filter-variant</v-icon>
        </v-btn>
      </div>
    </div>

    <!-- Search Field -->
    <div class="search-container px-4 mb-4">
      <div class="search-field d-flex align-center pa-2 rounded-pill">
        <v-text-field
          v-model="search"
          placeholder="検索 / Tìm kiếm từ vựng"
          prepend-inner-icon="mdi-magnify"
          variant="plain"
          hide-details
          single-line
          density="compact"
          @update:model-value="filterVocabulary"
        ></v-text-field>
      </div>
    </div>

    <!-- Active Filters (if any) -->
    <div class="active-filters px-4 mb-3" v-if="hasActiveFilters">
      <div class="d-flex flex-wrap">
        <v-chip
          v-if="selectedJlptLevel"
          size="small"
          color="primary"
          class="mr-2 mb-1"
          closable
          @click:close="clearFilter('jlptLevel')"
        >
          {{ selectedJlptLevel }}
        </v-chip>
        <v-chip
          v-if="selectedTopic"
          size="small"
          color="success"
          class="mr-2 mb-1"
          closable
          @click:close="clearFilter('topic')"
        >
          {{ selectedTopic.meaning }}
        </v-chip>
        <v-chip
          v-if="selectedCategory"
          size="small"
          color="info"
          class="mr-2 mb-1"
          closable
          @click:close="clearFilter('category')"
        >
          {{ selectedCategory.meaning }}
        </v-chip>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <!-- Category View (when no category is selected) -->
      <div v-if="!selectedCategory" class="category-section">
        <div v-for="category in categories" :key="category.id" class="mb-6">
          <div class="px-4 mb-3">
            <div class="text-subtitle-1 text-grey text-center">カテゴリー</div>
            <div class="text-h4 font-weight-bold text-center japanese-text">{{ category.name }}</div>
            <div class="text-subtitle-1 text-center">{{ category.meaning }}</div>
          </div>

          <div class="category-card px-4 mb-4">
            <v-card
              class="category-lesson-card mb-4"
              variant="outlined"
              rounded="lg"
              @click="selectCategory(category.name)"
            >
              <v-img
                :src="getImagePath(category.name)"
                height="180"
                cover
                class="position-relative"
              >
                <div class="category-overlay d-flex flex-column justify-center align-center">
                  <div class="text-h5 font-weight-bold text-white text-center pb-2">
                    {{ category.meaning }}
                  </div>
                  <div class="text-body-1 text-white">
                    {{ getTopicsCount(category) }} topics
                  </div>
                </div>
              </v-img>
            </v-card>
          </div>
        </div>
      </div>

      <!-- Topic View (when a category is selected) -->
      <div v-else-if="selectedCategory && !selectedTopic" class="topic-section">
        <div class="px-4 mb-3">
          <div class="text-subtitle-1 text-grey text-center">カテゴリー</div>
          <div class="text-h4 font-weight-bold text-center japanese-text">{{ selectedCategory.name }}</div>
          <div class="text-subtitle-1 text-center">{{ selectedCategory.meaning }}</div>
        </div>

        <div class="topics-list px-4">
          <div v-for="topic in getTopicsByCategory(selectedCategory)" :key="topic.id" class="mb-4">
            <v-card
              class="topic-card"
              variant="outlined"
              rounded="lg"
              @click="handleTopicSelect(topic)"
            >
              <v-card-title class="d-flex align-center">
                <span class="japanese-text">{{ topic.name }}</span>
                <v-spacer></v-spacer>
                <v-chip size="small" color="success">{{ getVocabularyCount(topic) }} words</v-chip>
              </v-card-title>
              <v-card-text>
                <div class="text-body-1">{{ topic.meaning }}</div>
              </v-card-text>
            </v-card>
          </div>
        </div>
      </div>

      <!-- Vocabulary List (when a topic is selected) -->
      <div v-else-if="selectedTopic" class="vocabulary-section px-4">
        <div class="d-flex align-center mb-4">
          <v-btn icon class="mr-2" @click="backToTopics" variant="text">
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
          <div>
            <div class="text-h6 font-weight-bold japanese-text">{{ selectedTopic.name }}</div>
            <div class="text-subtitle-1">{{ selectedTopic.meaning }}</div>
          </div>
        </div>

        <div v-if="filteredVocabulary.length === 0" class="text-center py-8">
          <v-icon size="large" icon="mdi-book-search-outline" class="mb-4"></v-icon>
          <h3 class="text-h6">Không tìm thấy từ vựng</h3>
          <p class="text-body-1 text-medium-emphasis">
            Hãy điều chỉnh bộ lọc tìm kiếm hoặc chọn chủ đề khác.
          </p>
        </div>

        <div v-else>
          <v-card
            v-for="item in filteredVocabulary"
            :key="item.vocabId"
            class="vocabulary-item mb-4"
            variant="outlined"
            rounded="lg"
            @click="toggleExpand(item.vocabId)"
          >
            <div class="pa-4">
              <div class="d-flex align-center mb-2">
                <div>
                  <div class="d-flex align-center">
                    <div class="text-h6 japanese-text font-weight-bold">{{ item.term }}</div>
                    <v-chip
                      :color="getJlptColor(item.jlptLevel)"
                      size="small"
                      class="ml-3"
                    >
                      {{ item.jlptLevel }}
                    </v-chip>
                  </div>
                  <div v-if="item.pronunciation" class="text-subtitle-1 japanese-text text-medium-emphasis">
                    {{ item.pronunciation }}
                  </div>
                </div>
                <v-spacer></v-spacer>
                <div class="d-flex">
                  <v-btn
                    icon="mdi-volume-high"
                    size="small"
                    variant="text"
                    @click.stop="playAudio(item)"
                    class="mr-2"
                    color="blue"
                    title="Phát âm"
                  ></v-btn>
                  <v-btn
                    :icon="item.isSaved ? 'mdi-bookmark' : 'mdi-bookmark-outline'"
                    size="small"
                    variant="text"
                    :color="item.isSaved ? 'warning' : undefined"
                    @click.stop="toggleSave(item)"
                    class="mr-2"
                    title="Lưu từ vựng"
                  ></v-btn>
                  <v-btn
                    :icon="expandedItems.includes(item.vocabId) ? 'mdi-chevron-up' : 'mdi-chevron-down'"
                    size="small"
                    variant="text"
                    class="mr-2"
                    title="Xem thêm"
                  ></v-btn>
                </div>
              </div>

              <div class="text-body-1 meaning-text">{{ item.meaning }}</div>

              <!-- Expanded Content -->
              <div v-if="expandedItems.includes(item.vocabId)" class="mt-4">
                <v-divider class="mb-3"></v-divider>

                <!-- Example Section -->
                <div v-if="item.example" class="example-section mb-3 pa-3 rounded">
                  <div class="d-flex align-items-center">
                    <v-icon class="mr-2" size="x-small" color="grey">mdi-format-quote-open</v-icon>
                    <div class="flex-grow-1 japanese-text">{{ item.example }}</div>
                    <v-btn
                      icon="mdi-volume-high"
                      size="x-small"
                      variant="text"
                      @click.stop="playAudio(item, true)"
                      color="blue"
                      title="Phát âm câu ví dụ"
                    ></v-btn>
                  </div>
                  <div v-if="item.exampleMeaning" class="example-translation ml-6 mt-1 text-caption text-medium-emphasis">
                    {{ item.exampleMeaning }}
                  </div>
                </div>

                <!-- View Details Button -->
                <div class="text-center">
                  <v-btn
                    variant="outlined"
                    color="primary"
                    size="small"
                    @click.stop="navigateToDetail(item.vocabId)"
                  >
                    Xem chi tiết
                  </v-btn>
                </div>
              </div>
            </div>
          </v-card>
        </div>
      </div>
    </div>

    <!-- Filter Dialog -->
    <v-dialog v-model="showFilterDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h6 font-weight-bold">
          Lọc từ vựng
        </v-card-title>
        <v-card-text>
          <v-select
            v-model="selectedJlptLevel"
            label="Trình độ JLPT"
            :items="jlptLevels"
            clearable
            variant="outlined"
            class="mb-4"
          ></v-select>

          <v-select
            v-model="tempSelectedCategory"
            label="Danh mục"
            :items="categories"
            item-title="meaning"
            item-value="id"
            clearable
            variant="outlined"
            class="mb-4"
            @update:model-value="updateTopicsForCategory"
          ></v-select>

          <v-select
            v-model="tempSelectedTopic"
            label="Chủ đề"
            :items="availableTopics"
            item-title="meaning"
            item-value="id"
            clearable
            variant="outlined"
            :disabled="!tempSelectedCategory"
          ></v-select>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="clearAllFilters">Xóa tất cả</v-btn>
          <v-btn color="primary" @click="applyFilters">Áp dụng</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// State
const search = ref('')
const selectedJlptLevel = ref(null)
const selectedCategory = ref(null)
const selectedTopic = ref(null)
const expandedItems = ref([])
const showFilterDialog = ref(false)
const tempSelectedCategory = ref(null)
const tempSelectedTopic = ref(null)

// Filter options
const jlptLevels = ['N1', 'N2', 'N3', 'N4', 'N5']

// Mock data for categories (from SQL)
const categories = [
  { id: '1', name: 'IT基礎', meaning: 'IT Basics', displayOrder: 1 },
  { id: '2', name: 'プログラミング', meaning: 'Programming', displayOrder: 2 },
  { id: '3', name: 'ウェブ開発', meaning: 'Web Development', displayOrder: 3 },
  { id: '4', name: 'データベース', meaning: 'Database', displayOrder: 4 },
  { id: '5', name: 'AI・データ', meaning: 'Artificial Intelligence / Data', displayOrder: 5 },
  { id: '6', name: 'コミュニケーション', meaning: 'Communication & Teamwork', displayOrder: 6 },
  { id: '7', name: 'キャリア実務', meaning: 'Project & Reality', displayOrder: 7 }
]

// Mock data for topics (from SQL)
const topics = [
  // IT Basics
  { id: '101', name: 'コンピューターの構造', meaning: 'Computer Architecture', categoryId: '1', displayOrder: 1 },
  { id: '102', name: 'オペレーティングシステム', meaning: 'Operating System', categoryId: '1', displayOrder: 2 },
  { id: '103', name: 'ハードウェア', meaning: 'Hardware', categoryId: '1', displayOrder: 3 },
  { id: '104', name: 'ソフトウェア', meaning: 'Software', categoryId: '1', displayOrder: 4 },
  { id: '105', name: 'コンピューターネットワーク', meaning: 'Computer Network', categoryId: '1', displayOrder: 5 },
  { id: '106', name: 'IT共通用語', meaning: 'General Terms in IT', categoryId: '1', displayOrder: 6 },

  // Programming
  { id: '201', name: 'プログラミング言語', meaning: 'Programming Language', categoryId: '2', displayOrder: 1 },
  { id: '202', name: 'アルゴリズムとデータ構造', meaning: 'Algorithms & Data Structures', categoryId: '2', displayOrder: 2 },
  { id: '203', name: 'デバッグとエラーハンドリング', meaning: 'Debugging and Error Handling', categoryId: '2', displayOrder: 3 },
  { id: '204', name: 'オブジェクト指向', meaning: 'Object Oriented', categoryId: '2', displayOrder: 4 },
  { id: '205', name: 'コマンドライン・ターミナル', meaning: 'Command Line & Terminal', categoryId: '2', displayOrder: 5 },

  // Web Development
  { id: '301', name: 'フロントエンド', meaning: 'Frontend', categoryId: '3', displayOrder: 1 },
  { id: '302', name: 'バックエンド', meaning: 'Backend', categoryId: '3', displayOrder: 2 },
  { id: '303', name: 'システムアーキテクチャ', meaning: 'System Architecture', categoryId: '3', displayOrder: 3 },
  { id: '304', name: 'DevOps・CI/CD', meaning: 'DevOps, CI/CD', categoryId: '3', displayOrder: 4 },

  // Database
  { id: '401', name: 'SQLの基本', meaning: 'Basic SQL', categoryId: '4', displayOrder: 1 },
  { id: '402', name: 'データベース設計', meaning: 'Database Design', categoryId: '4', displayOrder: 2 },
  { id: '403', name: 'NoSQL・新しい概念', meaning: 'NoSQL and other new concept', categoryId: '4', displayOrder: 3 },

  // AI/Data
  { id: '501', name: '機械学習', meaning: 'Machine Learning', categoryId: '5', displayOrder: 1 },
  { id: '502', name: 'データ分析', meaning: 'Data Analysis', categoryId: '5', displayOrder: 2 },
  { id: '503', name: '自然言語処理', meaning: 'Natural Language Processing', categoryId: '5', displayOrder: 3 }
]

// Mock data for vocabulary
const vocabulary = [
  {
    vocabId: '1001',
    term: 'コンピュータ',
    pronunciation: 'こんぴゅーた',
    meaning: 'Máy tính',
    example: 'このコンピュータは新しいです。',
    exampleMeaning: 'Máy tính này mới.',
    jlptLevel: 'N5',
    topicId: '101',
    isSaved: false
  },
  {
    vocabId: '1002',
    term: 'プログラム',
    pronunciation: 'ぷろぐらむ',
    meaning: 'Chương trình',
    example: '彼は新しいプログラムを作りました。',
    exampleMeaning: 'Anh ấy đã tạo một chương trình mới.',
    jlptLevel: 'N4',
    topicId: '201',
    isSaved: true
  },
  {
    vocabId: '1003',
    term: 'ウェブサイト',
    pronunciation: 'うぇぶさいと',
    meaning: 'Trang web',
    example: '私たちは新しいウェブサイトを開発しています。',
    exampleMeaning: 'Chúng tôi đang phát triển một trang web mới.',
    jlptLevel: 'N4',
    topicId: '301',
    isSaved: false
  },
  {
    vocabId: '1004',
    term: 'データベース',
    pronunciation: 'でーたべーす',
    meaning: 'Cơ sở dữ liệu',
    example: 'データベースからデータを取得します。',
    exampleMeaning: 'Lấy dữ liệu từ cơ sở dữ liệu.',
    jlptLevel: 'N3',
    topicId: '401',
    isSaved: false
  },
  {
    vocabId: '1005',
    term: '人工知能',
    pronunciation: 'じんこうちのう',
    meaning: 'Trí tuệ nhân tạo (AI)',
    example: '人工知能は私たちの生活を変えました。',
    exampleMeaning: 'Trí tuệ nhân tạo đã thay đổi cuộc sống của chúng ta.',
    jlptLevel: 'N2',
    topicId: '501',
    isSaved: true
  }
]

// Computed
const availableTopics = computed(() => {
  if (!tempSelectedCategory.value) return []
  return topics.filter(t => t.categoryId === tempSelectedCategory.value)
})

const hasActiveFilters = computed(() => {
  return selectedJlptLevel.value || selectedCategory.value || selectedTopic.value || search.value
})

const filteredVocabulary = computed(() => {
  let result = [...vocabulary]

  if (selectedJlptLevel.value) {
    result = result.filter(v => v.jlptLevel === selectedJlptLevel.value)
  }

  if (selectedTopic.value) {
    result = result.filter(v => v.topicId === selectedTopic.value?.id)
  } else if (selectedCategory.value) {
    const topicIds = topics
      .filter(t => t.categoryId === selectedCategory.value?.id)
      .map(t => t.id)
    result = result.filter(v => topicIds.includes(v.topicId))
  }

  if (search.value) {
    const searchLower = search.value.toLowerCase()
    result = result.filter(v =>
      v.term.toLowerCase().includes(searchLower) ||
      v.pronunciation?.toLowerCase().includes(searchLower) ||
      v.meaning.toLowerCase().includes(searchLower)
    )
  }

  return result
})

// Methods
function goBack() {
  router.back()
}

function getImagePath(categoryName: string): string {
  const categoryImages: Record<string, string> = {
    'IT基礎': 'it-basics.png',
    'プログラミング': 'programming.png',
    'ウェブ開発': 'web-dev.png',
    'データベース': 'database.png',
    'AI・データ': 'ai-data.png',
    'コミュニケーション': 'communication.png',
    'キャリア実務': 'career.png',
  }

  return categoryImages[categoryName] || 'default.png'
}

function getTopicsCount(category) {
  return topics.filter(t => t.categoryId === category.id).length
}

function getTopicsByCategory(category) {
  return topics.filter(t => t.categoryId === category.id)
    .sort((a, b) => a.displayOrder - b.displayOrder)
}

function getVocabularyCount(topic) {
  return vocabulary.filter(v => v.topicId === topic.id).length
}

function selectCategory(categoryName: string) {
  const category = categories.find(c => c.name === categoryName)
  if (category) {
    tempSelectedCategory.value = category.id
    selectedCategory.value = category
    selectedTopic.value = null
  }
}

function selectTopic(category: Category) {
  selectedCategory.value = category
  selectedTopic.value = null
}

function backToTopics() {
  selectedTopic.value = null
}

function toggleExpand(vocabId) {
  const index = expandedItems.value.indexOf(vocabId)
  if (index === -1) {
    expandedItems.value.push(vocabId)
  } else {
    expandedItems.value.splice(index, 1)
  }
}

function openFilterDialog() {
  tempSelectedCategory.value = selectedCategory.value ? selectedCategory.value.id : null
  tempSelectedTopic.value = selectedTopic.value ? selectedTopic.value.id : null
  showFilterDialog.value = true
}

function updateTopicsForCategory() {
  tempSelectedTopic.value = null
}

function applyFilters() {
  // Apply JLPT filter directly

  // Apply category & topic filters
  if (tempSelectedCategory.value) {
    selectedCategory.value = categories.find(c => c.id === tempSelectedCategory.value)

    if (tempSelectedTopic.value) {
      selectedTopic.value = topics.find(t => t.id === tempSelectedTopic.value)
    } else {
      selectedTopic.value = null
    }
  } else {
    selectedCategory.value = null
    selectedTopic.value = null
  }

  showFilterDialog.value = false
}

function clearFilter(type) {
  if (type === 'jlptLevel') {
    selectedJlptLevel.value = null
  } else if (type === 'category') {
    selectedCategory.value = null
    selectedTopic.value = null
  } else if (type === 'topic') {
    selectedTopic.value = null
  }
}

function clearAllFilters() {
  selectedJlptLevel.value = null
  selectedCategory.value = null
  selectedTopic.value = null
  tempSelectedCategory.value = null
  tempSelectedTopic.value = null
  search.value = ''
}

function filterVocabulary() {
  // Search is handled by the computed property
}

function playAudio(item, isExample = false) {
  console.log(`Playing audio for ${isExample ? 'example' : 'term'}: ${isExample ? item.example : item.term}`)
  // In a real implementation, this would use the audio API or TTS service
}

function toggleSave(item) {
  item.isSaved = !item.isSaved
  console.log(`${item.isSaved ? 'Saved' : 'Unsaved'} vocabulary: ${item.term}`)
}

function getJlptColor(level) {
  switch (level) {
    case 'N1': return 'red'
    case 'N2': return 'orange'
    case 'N3': return 'amber'
    case 'N4': return 'light-green'
    case 'N5': return 'green'
    default: return 'grey'
  }
}

function handleTopicSelect(topic: Topic) {
  selectedTopic.value = topic
}

function navigateToDetail(vocabId: string) {
  router.push({ name: 'vocabulary-detail', params: { id: vocabId } })
}

// Lifecycle hooks
onMounted(() => {
  // In a real implementation, we would fetch categories, topics, and vocabulary
})
</script>

<style scoped lang="scss">
.vocabulary-learning-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 64px;
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
}

.search-field {
  background-color: #f0f0f0;
  height: 48px;
  border-radius: 24px;
}

.category-lesson-card {
  overflow: hidden;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1) !important;
  }
}

.category-overlay {
  background: linear-gradient(0deg, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.3) 100%);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 20px;
}

.topic-card {
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  }
}

.vocabulary-item {
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  }
}

.meaning-text {
  color: rgba(0, 0, 0, 0.6);
}

.example-section {
  background-color: #f5f5f5;
  border-left: 3px solid rgba(0, 0, 0, 0.1);
}

.example-translation {
  color: rgba(0, 0, 0, 0.6);
  font-style: italic;
}
</style>