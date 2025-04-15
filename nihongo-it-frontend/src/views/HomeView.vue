<template>
  <div class="dashboard">
    <!-- Search Bar -->
    <div class="search-bar py-3">
      <v-container>
        <v-row align="center">
          <v-col cols="12" sm="8" md="6" lg="5" class="mx-auto">
            <v-card variant="outlined" class="search-card">
              <v-text-field
                v-model="searchQuery"
                prepend-inner-icon="mdi-magnify"
                placeholder="日本語 or English で検索"
                hide-details
                variant="plain"
                density="comfortable"
                class="search-input"
                @keyup.enter="searchVocabulary"
              ></v-text-field>
              <v-btn color="primary" @click="searchVocabulary" class="search-btn">検索</v-btn>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </div>

    <v-container class="main-content">
      <!-- Welcome Section -->
      <v-row class="mt-6">
        <v-col cols="12">
          <h1 class="welcome-header">
            こんにちは、{{ username }}さん！
            <small>Welcome to your Japanese IT learning journey</small>
          </h1>
        </v-col>
      </v-row>

      <v-row>
        <!-- Progress & Level Section -->
        <v-col cols="12" md="4">
          <v-card class="daily-progress">
            <v-card-title>Today's Progress</v-card-title>
            <v-card-text>
              <v-progress-circular
                :model-value="dailyGoalProgress"
                :size="100"
                :width="10"
                color="success"
              >
                {{ dailyGoalProgress }}%
              </v-progress-circular>

              <div class="progress-details mt-4">
                <p>
                  <strong>{{ dailyProgress.minutesStudied }} minutes</strong>
                  studied today
                </p>
                <p>
                  Daily goal:
                  <strong>{{ studyPlan.dailyGoalMinutes }} minutes</strong>
                </p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="8">
          <v-card class="current-level">
            <v-card-title>Your Level: {{ currentLevel }}</v-card-title>
            <v-card-text>
              <p>Start your journey to master Japanese IT terminology!</p>
              <v-btn color="primary" to="/learning-path" class="mt-4">View Learning Path</v-btn>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Two Column Layout -->
      <v-row class="mt-6">
        <!-- Left Column - Trending Searches -->
        <v-col cols="12" md="4">
          <!-- Add Translation Box -->
          <v-card class="translation-card mb-6">
            <v-card-title class="translation-title">
              <div class="d-flex align-center">
                <v-icon color="amber-darken-2" class="me-2">mdi-translate</v-icon>
                ベトナム翻訳
              </div>
            </v-card-title>
            <v-card-text class="pa-0">
              <div class="translation-container">
                <div class="translation-options pa-2">
                  <div class="d-flex">
                    <v-btn-toggle
                      v-model="translationDirection"
                      mandatory
                      density="comfortable"
                      color="primary"
                      class="mb-2"
                    >
                      <v-btn value="vn-to-jp" size="small">
                        <v-icon size="small">mdi-arrow-right</v-icon>
                        ベトナム語→日本語
                      </v-btn>
                      <v-btn value="jp-to-vn" size="small">
                        <v-icon size="small">mdi-arrow-right</v-icon>
                        日本語→ベトナム語
                      </v-btn>
                    </v-btn-toggle>
                  </div>
                </div>

                <v-textarea
                  v-model="translationText"
                  :label="translationDirection === 'vn-to-jp' ? 'ベトナム語のテキスト' : '日本語のテキスト'"
                  placeholder="ここに翻訳したい文章を入力してください"
                  variant="outlined"
                  rows="5"
                  class="translation-input px-3"
                  hide-details
                ></v-textarea>

                <div class="translation-actions px-3 py-2 d-flex justify-end">
                  <v-btn
                    color="primary"
                    @click="goToTranslationPage"
                  >
                    翻訳する
                  </v-btn>
                </div>
              </div>
            </v-card-text>
          </v-card>

          <v-card class="search-ranking-card">
            <v-card-title class="search-ranking-title">
              <v-icon color="primary" size="small" class="me-2">mdi-magnify</v-icon>
              みんなの検索ランキング
            </v-card-title>
            <v-card-text class="pa-0">
              <v-list class="ranking-list">
                <v-list-item
                  v-for="(term, index) in trendingSearches"
                  :key="index"
                  :to="`/vocabulary/search?keyword=${term.keyword}`"
                  class="ranking-item"
                >
                  <template v-slot:prepend>
                    <div class="ranking-number" :class="index < 3 ? 'top-rank' : ''">{{ index + 1 }}</div>
                  </template>
                  <v-list-item-title>{{ term.keyword }}</v-list-item-title>
                </v-list-item>
              </v-list>
              <div class="text-center pa-3">
                <v-btn size="small" variant="text" color="primary" to="/vocabulary/trending">
                  もっとランキングを見る
                </v-btn>
              </div>
            </v-card-text>
          </v-card>

          <!-- Feature Card -->
          <v-card class="feature-card mt-6" color="primary" variant="outlined">
            <v-card-title>Try Our New Furigana Generator!</v-card-title>
            <v-card-text>
              <p>Generate furigana for any Japanese text. Perfect for studying kanji pronunciation and creating learning materials.</p>
            </v-card-text>
            <v-card-actions>
              <v-btn to="/furigana" color="primary" variant="flat">
                Try it now
                <v-icon end>mdi-arrow-right</v-icon>
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <!-- Right Column - Recommended Lessons -->
        <v-col cols="12" md="8">
          <!-- Translation Results Section (conditionally shown) -->
          <v-card v-if="translationResult" class="mb-6 translation-result-card">
            <v-card-title class="d-flex justify-space-between align-center">
              <span>翻訳結果</span>
              <v-btn icon size="small" @click="clearTranslation">
                <v-icon>mdi-close</v-icon>
              </v-btn>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <div class="result-container">
                <div class="original-text mb-4">
                  <div class="text-caption text-medium-emphasis mb-1">
                    {{ translationDirection === 'vn-to-jp' ? 'ベトナム語:' : '日本語:' }}
                  </div>
                  <div class="original-content pa-3 bg-grey-lighten-4 rounded">
                    {{ translationText }}
                  </div>
                </div>

                <div class="translated-text">
                  <div class="text-caption text-medium-emphasis mb-1">
                    {{ translationDirection === 'vn-to-jp' ? '日本語:' : 'ベトナム語:' }}
                  </div>
                  <div class="translated-content pa-3 bg-indigo-lighten-5 rounded japanese-text">
                    {{ translationResult }}
                  </div>
                </div>

                <div class="translation-footer d-flex justify-end mt-3">
                  <v-btn
                    size="small"
                    variant="text"
                    prepend-icon="mdi-content-copy"
                    @click="copyTranslation"
                  >
                    コピー
                  </v-btn>
                  <v-btn
                    size="small"
                    variant="text"
                    color="primary"
                    prepend-icon="mdi-share-variant"
                    :to="`/translations?text=${encodeURIComponent(translationText)}&dir=${translationDirection}&result=${encodeURIComponent(translationResult)}`"
                  >
                    詳細を見る
                  </v-btn>
                </div>
              </div>
            </v-card-text>
          </v-card>

          <h2 class="section-title mb-4">Recommended for Today</h2>

          <v-row>
            <v-col v-for="lesson in suggestedLessons" :key="lesson.id" cols="12" sm="6">
              <v-card class="lesson-card">
                <v-card-title class="lesson-title">{{ lesson.title }}</v-card-title>
                <v-card-subtitle>
                  <v-chip
                    size="small"
                    :color="
                      lesson.type === 'vocabulary'
                        ? 'info'
                        : lesson.type === 'conversation'
                          ? 'success'
                          : 'warning'
                    "
                  >
                    {{ lesson.type }}
                  </v-chip>
                  <v-chip size="small" class="ml-2">{{ lesson.level }}</v-chip>
                  <span class="ml-2">{{ lesson.estimatedMinutes }} min</span>
                </v-card-subtitle>
                <v-card-text>
                  {{ lesson.description }}
                </v-card-text>
                <v-card-actions>
                  <v-btn :to="`/${lesson.type}?lessonId=${lesson.id}`" color="primary" variant="tonal">
                    Start Lesson
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
          </v-row>

          <!-- Recent Vocabulary Section -->
          <h2 class="section-title mb-4 mt-6">Recently Added Vocabulary</h2>
          <v-card variant="outlined" class="recent-vocab-card">
            <v-list class="recent-vocab-list">
              <v-list-item
                v-for="(vocab, index) in recentVocabulary"
                :key="vocab.id"
                :to="`/vocabulary/${vocab.id}`"
                class="vocab-item"
              >
                <div class="d-flex align-center w-100">
                  <div>
                    <div class="d-flex align-center">
                      <span class="vocab-text">{{ vocab.kanji || vocab.hiragana }}</span>
                      <v-chip size="x-small" color="primary" variant="outlined" class="ms-2">
                        {{ vocab.level }}
                      </v-chip>
                    </div>
                    <div class="vocab-meaning">{{ vocab.meaning }}</div>
                  </div>
                  <v-spacer></v-spacer>
                  <v-btn icon size="small" variant="text" color="primary">
                    <v-icon>mdi-arrow-right</v-icon>
                  </v-btn>
                </div>
              </v-list-item>
            </v-list>
            <div class="text-center pa-3">
              <v-btn color="primary" variant="text" to="/vocabulary">
                View All Vocabulary
              </v-btn>
            </div>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { useAuthStore } from '@/stores'

interface Lesson {
  id: string
  title: string
  type: string
  level: string
  estimatedMinutes: number
  description: string
}

interface VocabularyItem {
  id: string
  kanji: string | null
  hiragana: string
  meaning: string
  level: string
}

interface TrendingSearch {
  keyword: string
  count: number
}

@Component({
  name: 'HomeView'
})
export default class HomeView extends Vue {
  // User data
  searchQuery = ''

  // Translation data
  translationDirection = 'vn-to-jp'
  translationText = ''
  translationResult = ''
  translating = false

  // Get user info from auth store
  private authStore = useAuthStore()

  get username(): string {
    return this.authStore.user?.fullName || 'Guest'
  }

  get currentLevel(): string {
    return this.authStore.user?.currentLevel || 'N5'
  }

  // Static progress data
  dailyProgress = {
    minutesStudied: 25,
  }

  studyPlan = {
    dailyGoalMinutes: 60,
  }

  dailyGoalProgress = 42 // Static percentage

  // Trending searches
  trendingSearches: TrendingSearch[] = [
    { keyword: 'プログラミング', count: 243 },
    { keyword: 'データベース', count: 198 },
    { keyword: 'アプリ開発', count: 175 },
    { keyword: 'クラウド', count: 156 },
    { keyword: 'セキュリティ', count: 143 },
    { keyword: '人工知能', count: 132 },
    { keyword: 'ネットワーク', count: 121 },
    { keyword: 'バグ修正', count: 112 },
    { keyword: 'サーバー', count: 104 },
    { keyword: 'コーディング', count: 98 }
  ]

  // Recent vocabulary
  recentVocabulary: VocabularyItem[] = [
    { id: '1', kanji: 'プログラム', hiragana: 'ぷろぐらむ', meaning: 'Program', level: 'N3' },
    { id: '2', kanji: 'データ', hiragana: 'でーた', meaning: 'Data', level: 'N4' },
    { id: '3', kanji: '更新', hiragana: 'こうしん', meaning: 'Update', level: 'N3' },
    { id: '4', kanji: '設定', hiragana: 'せってい', meaning: 'Settings/Configuration', level: 'N4' },
    { id: '5', kanji: 'アップロード', hiragana: 'あっぷろーど', meaning: 'Upload', level: 'N3' }
  ]

  // Mock suggested lessons
  suggestedLessons: Lesson[] = [
    {
      id: '1',
      title: 'Basic IT Greetings',
      type: 'conversation',
      level: 'N5',
      estimatedMinutes: 15,
      description: 'Learn essential Japanese greetings for IT workplace.',
    },
    {
      id: '2',
      title: 'Programming Terms',
      type: 'vocabulary',
      level: 'N4',
      estimatedMinutes: 20,
      description: 'Master basic programming terminology in Japanese.',
    },
    {
      id: '3',
      title: 'IT Workplace Phrases',
      type: 'conversation',
      level: 'N4',
      estimatedMinutes: 25,
      description: 'Common phrases used in Japanese IT companies.',
    },
    {
      id: '4',
      title: 'Web Development Terms',
      type: 'vocabulary',
      level: 'N3',
      estimatedMinutes: 30,
      description: 'Essential vocabulary for web developers.',
    }
  ]

  // Search method
  searchVocabulary() {
    if (this.searchQuery.trim()) {
      this.$router.push({
        path: '/vocabulary/search',
        query: { keyword: this.searchQuery }
      });
    }
  }

  // Translation methods
  goToTranslationPage() {
    if (!this.translationText.trim()) return;

    // Navigate to the translations page with the text and direction as query parameters
    this.$router.push({
      path: '/translations',
      query: {
        text: this.translationText,
        dir: this.translationDirection
      }
    });
  }

  async translateText() {
    if (!this.translationText.trim()) return;

    this.translating = true;

    try {
      // Get the backend API URL
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

      // Call the translation API
      const response = await fetch(`${apiUrl}/api/v1/ai/translate?direction=${this.translationDirection}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'text/plain',
          'Accept': 'application/json'
        },
        body: this.translationText
      });

      if (!response.ok) {
        throw new Error(`Translation failed: ${response.statusText}`);
      }

      const data = await response.json();
      this.translationResult = data.translation;
    } catch (error) {
      console.error('Translation error:', error);
      // Create a user-friendly error message
      this.translationResult = 'Sorry, an error occurred during translation. Please try again later.';
    } finally {
      this.translating = false;
    }
  }

  clearTranslation() {
    this.translationResult = '';
  }

  copyTranslation() {
    navigator.clipboard.writeText(this.translationResult)
      .then(() => {
        // Optional: Show a success message
        alert('Copied to clipboard!');
      })
      .catch(err => {
        console.error('Failed to copy text: ', err);
      });
  }
}
</script>

<style lang="scss" scoped>
.search-bar {
  background-color: #f5f5f5;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-card {
  display: flex;
  border-radius: 8px;
}

.search-input {
  flex-grow: 1;
}

.search-btn {
  border-radius: 0 8px 8px 0;
  height: 100%;
}

.welcome-header {
  margin-bottom: 1.5rem;
  font-weight: bold;
  color: #333;

  small {
    display: block;
    font-size: 1rem;
    font-weight: normal;
    color: rgba(0, 0, 0, 0.7);
    margin-top: 0.5rem;
  }
}

.daily-progress {
  text-align: center;
  border-radius: 10px;

  .v-card-text {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
}

.progress-details {
  text-align: center;

  p {
    margin: 0.5rem 0;
  }
}

.current-level {
  border-radius: 10px;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-top: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f0f0f0;
}

.search-ranking-card {
  border-radius: 10px;
  overflow: hidden;
}

.search-ranking-title {
  font-size: 1.1rem;
  background-color: #f8f8f8;
  padding: 10px 16px;
}

.ranking-list {
  padding: 0;
}

.ranking-item {
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f9f9f9;
  }
}

.ranking-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
  color: #616161;
  margin-right: 12px;

  &.top-rank {
    background-color: #3949ab;
    color: white;
  }
}

.lesson-card {
  border-radius: 10px;
  height: 100%;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
  }
}

.lesson-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.recent-vocab-card {
  border-radius: 10px;
  overflow: hidden;
}

.recent-vocab-list {
  padding: 0;
}

.vocab-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 16px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f9f9f9;
  }
}

.vocab-text {
  font-family: 'Noto Sans JP', sans-serif;
  font-size: 1.1rem;
  font-weight: 500;
}

.vocab-meaning {
  color: #666;
  font-size: 0.85rem;
  margin-top: 2px;
}

.feature-card {
  border-radius: 10px;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-3px);
  }
}

// Translation box styles
.translation-card {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
}

.translation-title {
  font-weight: 600;
  background-color: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
  padding: 12px 16px;
  font-size: 1.1rem;
}

.translation-options {
  border-bottom: 1px solid #f0f0f0;
}

.translation-input {
  border-radius: 0;
}

.translation-result-card {
  border-radius: 10px;
  border: 1px solid #e0e0e0;
}

.original-content,
.translated-content {
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 60px;
}

.japanese-text {
  font-family: 'Noto Sans JP', sans-serif;
  line-height: 1.6;
}
</style>
