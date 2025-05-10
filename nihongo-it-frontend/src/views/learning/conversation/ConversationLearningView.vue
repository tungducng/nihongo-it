<template>
  <div class="conversation-learning-container">
    <div class="py-4">
      <h1 class="text-h4 font-weight-bold mb-6">Luyện hội thoại tiếng Nhật</h1>

      <v-alert type="info" variant="tonal" class="mb-6">
        <div class="text-h6 mb-2">Chào mừng đến với tính năng luyện hội thoại!</div>
        <p>Tính năng này giúp bạn thực hành các cuộc hội thoại hằng ngày bằng tiếng Nhật. Chọn một cuộc hội thoại để bắt đầu luyện tập.</p>
      </v-alert>

      <div class="conversation-list">
        <v-card
          v-for="conversation in conversations"
          :key="conversation.id"
          class="mb-4 conversation-card"
          variant="outlined"
          @click="navigateToConversation(conversation.id)"
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
              <span class="text-body-2">{{ conversation.lineCount }} dòng hội thoại</span>

              <v-spacer></v-spacer>

              <v-chip label size="small" :color="conversation.difficulty === 'Dễ' ? 'success' : conversation.difficulty === 'Trung bình' ? 'warning' : 'error'">
                {{ conversation.difficulty }}
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

// Router
const router = useRouter()

// Mock data
interface ConversationPreview {
  id: string;
  title: string;
  description: string;
  jlptLevel: string;
  lineCount: number;
  difficulty: 'Dễ' | 'Trung bình' | 'Khó';
}

// Mock conversations
const conversations = ref<ConversationPreview[]>([
  {
    id: '1',
    title: 'Chào hỏi và giới thiệu bản thân',
    description: 'Hội thoại cơ bản về giới thiệu bản thân trong tiếng Nhật',
    jlptLevel: 'N5',
    lineCount: 8,
    difficulty: 'Dễ'
  },
  {
    id: '2',
    title: 'Tại nhà hàng',
    description: 'Hội thoại khi đi ăn tại nhà hàng Nhật Bản',
    jlptLevel: 'N5',
    lineCount: 12,
    difficulty: 'Dễ'
  },
  {
    id: '3',
    title: 'Hỏi đường',
    description: 'Hội thoại khi cần hỏi đường trong thành phố',
    jlptLevel: 'N4',
    lineCount: 10,
    difficulty: 'Trung bình'
  },
  {
    id: '4',
    title: 'Phỏng vấn xin việc',
    description: 'Hội thoại khi tham gia phỏng vấn tuyển dụng bằng tiếng Nhật',
    jlptLevel: 'N3',
    lineCount: 14,
    difficulty: 'Khó'
  },
  {
    id: '5',
    title: 'Đi mua sắm',
    description: 'Hội thoại khi đi mua sắm tại cửa hàng quần áo',
    jlptLevel: 'N4',
    lineCount: 8,
    difficulty: 'Trung bình'
  }
])

// Methods
const getJlptColor = (level: string): string => {
  const colors: Record<string, string> = {
    'N1': 'red',
    'N2': 'orange',
    'N3': 'amber',
    'N4': 'light-green',
    'N5': 'green'
  }
  return colors[level] || 'grey'
}

const navigateToConversation = (id: string) => {
  router.push({
    name: 'conversationPractice',
    params: { id }
  })
}
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
