<template>
  <div class="conversation-learning-container">
    <div class="py-4">
      <h1 class="text-h4 font-weight-bold mb-6">Luyện hội thoại tiếng Nhật</h1>

      <v-alert type="info" variant="tonal" class="mb-6">
        <div class="text-h6 mb-2">Chào mừng đến với tính năng luyện hội thoại!</div>
        <p>Tính năng này giúp bạn thực hành các cuộc hội thoại hằng ngày bằng tiếng Nhật. Chọn một cuộc hội thoại để bắt đầu luyện tập.</p>
      </v-alert>

      <!-- Loading Skeleton -->
      <div v-if="loading">
        <v-skeleton-loader
          v-for="i in 3"
          :key="i"
          type="card, divider, list-item-avatar-two-line"
          class="mb-4"
        ></v-skeleton-loader>
      </div>

      <!-- Error Alert -->
      <v-alert
        v-else-if="error"
        type="error"
        variant="tonal"
        class="mb-6"
      >
        {{ errorMessage }}
        <v-btn class="mt-2" color="error" variant="text" @click="fetchConversations">
          Thử lại
        </v-btn>
      </v-alert>

      <!-- No Conversations Found -->
      <v-alert
        v-else-if="conversations.length === 0"
        type="info"
        variant="tonal"
        class="mb-6"
      >
        Không tìm thấy cuộc hội thoại nào. Vui lòng quay lại sau.
      </v-alert>

      <!-- Conversation List -->
      <div v-else class="conversation-list">
        <v-card
          v-for="conversation in conversations"
          :key="conversation.conversationId"
          class="mb-4 conversation-card"
          variant="outlined"
          @click="navigateToConversation(conversation.conversationId)"
        >
          <v-card-item>
            <template v-slot:prepend>
              <v-icon size="large" color="primary" class="mr-4">mdi-chat</v-icon>
            </template>

            <v-card-title>
              {{ conversation.title }}
              <v-chip :color="getJlptColor(conversation.jlptLevel)" size="small" class="ml-2">
                {{ conversation.jlptLevel }}
              </v-chip>
            </v-card-title>

            <v-card-subtitle class="mt-1">
              {{ conversation.description }}
            </v-card-subtitle>
          </v-card-item>

          <v-card-text>
            <div class="d-flex align-center">
              <v-icon size="small" color="info" class="mr-2">mdi-message-text</v-icon>
              <span class="text-body-2">{{ getLineCount(conversation) }} dòng hội thoại</span>

              <v-spacer></v-spacer>

              <v-chip label size="small" :color="getDifficultyColor(conversation.jlptLevel)">
                {{ getDifficultyText(conversation.jlptLevel) }}
              </v-chip>
            </div>
          </v-card-text>

          <v-divider></v-divider>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" variant="text">
              Bắt đầu học
              <v-icon end>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </div>

      <!-- Pagination -->
      <div v-if="!loading && !error && totalPages > 1" class="d-flex justify-center mt-6">
        <v-pagination
          v-model="currentPage"
          :length="totalPages"
          @update:model-value="handlePageChange"
          rounded
        ></v-pagination>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import conversationService from '@/services/conversation.service'
import type { Conversation, PagedResponse } from '@/services/conversation.service'
import { useToast } from 'vue-toast-notification'

// Router and toast
const router = useRouter()
const toast = useToast()

// State variables
const conversations = ref<Conversation[]>([])
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('Đã xảy ra lỗi khi tải dữ liệu hội thoại.')
const currentPage = ref(1) // 1-based for v-pagination
const totalPages = ref(0)
const pageSize = ref(10)

// Methods
const getJlptColor = (level: string | undefined): string => {
  if (!level) return 'grey'

  const colors: Record<string, string> = {
    'N1': 'red',
    'N2': 'orange',
    'N3': 'amber',
    'N4': 'light-green',
    'N5': 'green'
  }
  return colors[level] || 'grey'
}

const getDifficultyText = (jlptLevel: string | undefined): string => {
  if (!jlptLevel) return 'Không xác định'

  const difficulties: Record<string, string> = {
    'N1': 'Khó',
    'N2': 'Khó',
    'N3': 'Trung bình',
    'N4': 'Trung bình',
    'N5': 'Dễ'
  }
  return difficulties[jlptLevel] || 'Không xác định'
}

const getDifficultyColor = (jlptLevel: string | undefined): string => {
  if (!jlptLevel) return 'grey'

  const difficultyText = getDifficultyText(jlptLevel)
  if (difficultyText === 'Dễ') return 'success'
  if (difficultyText === 'Trung bình') return 'warning'
  if (difficultyText === 'Khó') return 'error'
  return 'grey'
}

const getLineCount = (conversation: Conversation): number => {
  return conversation.lines?.length || 0
}

const navigateToConversation = (id: string | undefined) => {
  if (!id) {
    toast.error('ID cuộc hội thoại không hợp lệ', {
      position: 'top',
      duration: 3000
    })
    return
  }

  router.push({
    name: 'conversationPractice',
    params: { id }
  })
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchConversations()
}

// Fetch conversations from API
const fetchConversations = async () => {
  loading.value = true
  error.value = false

  try {
    // Chuyển đổi currentPage từ 1-based sang 0-based cho API
    const apiPage = currentPage.value - 1

    // Sử dụng API public thay vì API admin vì API public đã được triển khai
    const response = await conversationService.getConversations(
      apiPage,
      pageSize.value,
      '', // search
      'title', // sortBy
      'asc' // sortDir
    )

    // Lấy dữ liệu từ response
    const data: PagedResponse<Conversation> = response.data

    conversations.value = data.content
    totalPages.value = data.totalPages

    if (conversations.value.length === 0 && currentPage.value > 1) {
      // Nếu trang hiện tại không có dữ liệu và không phải trang đầu tiên, quay lại trang trước
      currentPage.value--
      await fetchConversations()
    }
  } catch (err) {
    console.error('Error fetching conversations:', err)
    error.value = true
    errorMessage.value = 'Đã xảy ra lỗi khi tải dữ liệu hội thoại. Vui lòng thử lại sau.'

    // Show error toast
    toast.error('Không thể tải danh sách hội thoại', {
      position: 'top',
      duration: 3000
    })
  } finally {
    loading.value = false
  }
}

// Initial data fetch
onMounted(() => {
  fetchConversations()
})
</script>

<style scoped lang="scss">
.conversation-learning-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;

  .conversation-card {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 10px rgba(0,0,0,0.1);
    }
  }
}
</style>
