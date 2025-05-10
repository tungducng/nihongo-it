<template>
  <div class="conversation-practice-container">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
      <span class="mt-4 text-body-1">Đang tải dữ liệu hội thoại...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <v-alert type="error" class="mb-4">{{ error }}</v-alert>
      <v-btn @click="navigateBack" color="primary" variant="outlined">
        <v-icon start>mdi-arrow-left</v-icon>
        Quay lại danh sách hội thoại
      </v-btn>
    </div>

    <!-- Main Content -->
    <template v-else-if="conversation">
      <!-- Conversation Header -->
      <div class="conversation-header mb-1">
        <div class="d-flex align-center">
          <v-btn icon @click="navigateBack" class="mr-1" size="small" color="secondary" variant="text">
            <v-icon>mdi-arrow-left</v-icon>
          </v-btn>
          <span class="text-subtitle-2 font-weight-medium">Luyện hội thoại</span>
        </div>

        <v-card class="mt-1" variant="flat">
          <div class="d-flex flex-column flex-sm-row align-sm-center px-2 py-1">
            <div class="conversation-info">
              <div class="d-flex align-center">
                <h2 class="text-body-1 font-weight-bold">{{ conversation.title }}</h2>
                <v-chip :color="getJlptColor(conversation.jlptLevel)" size="x-small" density="compact" class="ml-2">
                  {{ conversation.jlptLevel }}
                </v-chip>
              </div>
              <div class="text-caption text-medium-emphasis">
                {{ conversation.description }}
              </div>
            </div>
            <v-spacer></v-spacer>
            <v-btn
              :icon="conversation.isSaved ? 'mdi-bookmark' : 'mdi-bookmark-outline'"
              size="small"
              variant="text"
              :color="conversation.isSaved ? 'warning' : undefined"
              @click="toggleSave"
              class="ml-1 action-btn"
              :title="conversation.isSaved ? 'Bỏ lưu' : 'Lưu hội thoại'"
            ></v-btn>
          </div>
        </v-card>
      </div>

      <!-- Conversation Card -->
      <v-card class="main-conversation-card mb-6" variant="elevated">
        <v-divider></v-divider>

        <!-- Conversation Practice Section -->
        <v-card-text class="py-4" style="position: relative; overflow: visible; min-height: 60vh;">
          <div class="d-flex mb-4">
            <v-chip color="primary" label class="mr-3">
              <v-icon start>mdi-account</v-icon>
              Bạn
            </v-chip>
            <v-chip color="info" label>
              <v-icon start>mdi-account-tie</v-icon>
              Người bản xứ
            </v-chip>
          </div>

          <!-- Chat Messages Container -->
          <div class="chat-container">
            <template v-for="(line, i) in conversation.dialogue" :key="i">
              <div
                v-if="visibleLineIndices.includes(i)"
                class="message-row mb-2"
                :class="line.speaker === 'user' ? 'justify-end' : 'justify-start'"
                :data-index="i"
              >
                <!-- Avatar for Native Speaker -->
                <div v-if="line.speaker !== 'user'" class="avatar-container mr-2">
                  <v-avatar size="36" color="info">
                    <v-icon color="white">mdi-account-tie</v-icon>
                  </v-avatar>
                </div>

                <!-- Message Bubble -->
                <div
                  class="message-container"
                  :class="{
                    'user-message': line.speaker === 'user',
                    'native-message': line.speaker !== 'user',
                    'completed-message': lineCompletionStatus[i] && line.speaker === 'user',
                    'score-excellent': lineCompletionStatus[i] && pronunciationScores[i] >= 90,
                    'score-very-good': lineCompletionStatus[i] && pronunciationScores[i] >= 80 && pronunciationScores[i] < 90,
                    'score-good': lineCompletionStatus[i] && pronunciationScores[i] >= 70 && pronunciationScores[i] < 80,
                    'score-fair': lineCompletionStatus[i] && pronunciationScores[i] >= 60 && pronunciationScores[i] < 70,
                    'score-poor': lineCompletionStatus[i] && pronunciationScores[i] > 0 && pronunciationScores[i] < 60
                  }"
                  :style="{
                    maxWidth: '80%',
                    minWidth: '270px'
                  }"
                >
                  <!-- Message Content -->
                  <div class="message-content pa-2">
                    <div class="d-flex justify-space-between align-center mb-0">
                      <div class="japanese-text text-subtitle-1 font-weight-medium">
                        <!-- Show typing effect for latest message (both user and native) -->
                        <span v-if="isTyping && i === visibleLineIndices[visibleLineIndices.length - 1]">
                          {{ typedText }}<span class="typing-cursor">|</span>
                        </span>
                        <span v-else>{{ line.japanese }}</span>
                      </div>
                      <!-- Audio Button for all messages -->
                      <v-btn
                        icon
                        density="comfortable"
                        size="x-small"
                        @click="playAudio(line)"
                        color="info"
                        variant="text"
                        title="Nghe phát âm mẫu"
                      >
                        <v-icon size="small">mdi-volume-high</v-icon>
                      </v-btn>
                    </div>
                    <div class="text-body-2 text-medium-emphasis">
                      <!-- Also apply typing effect to the meaning -->
                      <span v-if="isTyping && i === visibleLineIndices[visibleLineIndices.length - 1]">
                        {{ isTyping && typedText.length > 0 ? line.meaning : '' }}
                      </span>
                      <span v-else>{{ line.meaning }}</span>
                    </div>
                  </div>

                  <!-- User Controls section below message -->
                  <div v-if="isUserLine(line)" class="user-controls mt-0">
                    <!-- Row of controls -->
                    <div class="d-flex align-center flex-wrap">
                      <!-- Recording Controls (always visible) -->
                      <div class="d-flex gap-2">
                        <v-btn
                          size="small"
                          color="error"
                          variant="text"
                          :disabled="isRecording && activeLineIndex !== i"
                          @click="startRecording(i)"
                          :loading="isProcessing && activeLineIndex === i"
                        >
                          <v-icon size="small" class="mr-1">mdi-microphone</v-icon>
                          <span v-if="!(isRecording && activeLineIndex === i)" class="d-none d-sm-inline">Ghi âm</span>
                        </v-btn>

                        <v-btn
                          v-if="isRecording && activeLineIndex === i"
                          size="small"
                          color="primary"
                          variant="text"
                          @click="stopRecording"
                        >
                          <v-icon size="small" class="mr-1">mdi-stop</v-icon>
                          <span class="d-none d-sm-inline">Dừng</span>
                        </v-btn>

                        <v-btn
                          v-if="recordedAudioUrls[i] && !(isRecording && activeLineIndex === i)"
                          size="small"
                          color="secondary"
                          variant="text"
                          :disabled="isRecording"
                          @click="playRecordedAudio(i)"
                        >
                          <v-icon size="small" class="mr-1">mdi-play</v-icon>
                          <span class="d-none d-sm-inline">Nghe lại</span>
                        </v-btn>
                      </div>

                      <v-spacer></v-spacer>

                      <!-- Score display inline with controls -->
                      <div v-if="pronunciationScores[i] > 0" class="d-flex align-center">
                        <v-tooltip location="top">
                          <template v-slot:activator="{ props }">
                            <v-chip
                              v-bind="props"
                              :color="getScoreColor(pronunciationScores[i])"
                              size="small"
                              class="mr-2"
                            >
                              {{ pronunciationScores[i] }}
                            </v-chip>
                          </template>
                          {{ getVietnameseFeedback(pronunciationScores[i]) }}
                        </v-tooltip>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Avatar for User -->
                <div v-if="line.speaker === 'user'" class="avatar-container ml-2">
                  <v-avatar size="36" color="primary">
                    <v-icon color="white">mdi-account</v-icon>
                  </v-avatar>
                </div>
              </div>
            </template>
          </div>

          <!-- Completion Message -->
          <v-alert
            v-if="isConversationCompleted"
            type="success"
            variant="tonal"
            class="mt-4"
          >
            <div class="text-h6 mb-2">Chúc mừng bạn đã hoàn thành hội thoại!</div>
            <p>Hãy tiếp tục luyện tập để cải thiện kỹ năng giao tiếp tiếng Nhật của bạn.</p>
          </v-alert>
        </v-card-text>
      </v-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toast-notification'

// Define types
interface ConversationLine {
  speaker: 'user' | 'native';
  japanese: string;
  meaning: string;
  audioUrl?: string;
}

interface Conversation {
  id: string;
  title: string;
  description: string;
  jlptLevel?: string;
  isSaved?: boolean;
  dialogue: ConversationLine[];
}

// Router
const route = useRoute()
const router = useRouter()
const toast = useToast()

// State
const loading = ref(false)
const error = ref('')
const conversation = ref<Conversation | null>(null)
const isRecording = ref(false)
const isProcessing = ref(false)
const activeLineIndex = ref(-1)
const recordedAudioUrls = ref<(string | null)[]>([])
const recordedAudioBlobs = ref<(Blob | null)[]>([])
const pronunciationScores = ref<number[]>([])
const lineCompletionStatus = ref<boolean[]>([])
const mediaRecorder = ref<MediaRecorder | null>(null)
const audioChunks = ref<Blob[]>([])

// Audio stream
let audioStream: MediaStream | null = null

// Thêm refs và computed mới để quản lý hiệu ứng typing và hiển thị từng dòng
const visibleLineIndices = ref<number[]>([0]); // Ban đầu chỉ hiển thị dòng đầu tiên
const isTyping = ref(true); // Bắt đầu với hiệu ứng typing
const typedText = ref('');
const currentTypeIndex = ref(0);
const typeSpeed = 80; // ms per character - Đã tăng từ 50 lên 80ms

// Computed
const isConversationCompleted = computed(() => {
  const userLines = conversation.value?.dialogue.filter(line => line.speaker === 'user') || [];
  return userLines.length > 0 &&
    userLines.every((_, index) => lineCompletionStatus.value[getUserLineIndex(index)]);
})

// Methods
const getJlptColor = (level: string | undefined): string => {
  if (!level) return 'grey';
  const colors: Record<string, string> = {
    'N1': 'red',
    'N2': 'orange',
    'N3': 'amber',
    'N4': 'light-green',
    'N5': 'green'
  }
  return colors[level] || 'grey'
}

const getScoreColor = (score: number): string => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'light-green'
  if (score >= 70) return 'lime'
  if (score >= 60) return 'warning'
  return 'error'
}

const getVietnameseFeedback = (score: number): string => {
  if (score >= 90) {
    return 'Phát âm xuất sắc!'
  } else if (score >= 80) {
    return 'Phát âm rất tốt'
  } else if (score >= 70) {
    return 'Phát âm tốt'
  } else if (score >= 60) {
    return 'Phát âm khá'
  } else {
    return 'Cần cải thiện'
  }
}

const toggleSave = () => {
  if (!conversation.value) return;

  // Toggle saved status (demo only)
  conversation.value.isSaved = !conversation.value.isSaved;

  toast.success(conversation.value.isSaved ? 'Đã lưu hội thoại' : 'Đã bỏ lưu hội thoại', {
    position: 'top',
    duration: 2000
  });
}

const playAudio = (line: ConversationLine) => {
  // Mock function, in a real app would play the audio from line.audioUrl
  toast.info(`Đang phát âm thanh mẫu...`, {
    position: 'top',
    duration: 2000
  });
}

const isUserLine = (line: ConversationLine): boolean => {
  return line.speaker === 'user';
}

const getUserLineIndex = (userIndex: number): number => {
  // Find the index in the dialogue array for the nth user line
  if (!conversation.value) return -1;

  let count = 0;
  for (let i = 0; i < conversation.value.dialogue.length; i++) {
    if (conversation.value.dialogue[i].speaker === 'user') {
      if (count === userIndex) return i;
      count++;
    }
  }
  return -1;
}

const canInteractWith = (index: number): boolean => {
  if (!conversation.value) return false;
  const line = conversation.value.dialogue[index];

  // Luôn cho phép tương tác với các dòng của người dùng
  return line.speaker === 'user';
}

const startRecording = async (index: number) => {
  try {
    // Reset recording state
    audioChunks.value = [];
    activeLineIndex.value = index;

    audioStream = await navigator.mediaDevices.getUserMedia({
      audio: {
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true
      }
    });

    if (audioStream) {
      mediaRecorder.value = new MediaRecorder(audioStream);

      mediaRecorder.value.ondataavailable = (event: BlobEvent) => {
        if (event.data.size > 0) {
          audioChunks.value.push(event.data);
        }
      };

      mediaRecorder.value.onstop = () => {
        const audioBlob = new Blob(audioChunks.value, { type: 'audio/wav' });
        recordedAudioBlobs.value[index] = audioBlob;
        recordedAudioUrls.value[index] = URL.createObjectURL(audioBlob);
        processRecording(index);
      };

      mediaRecorder.value.start();
      isRecording.value = true;
      toast.info('Bắt đầu ghi âm - Hãy phát âm câu hội thoại', {
        position: 'top',
        duration: 2000
      });
    }
  } catch (err) {
    console.error('Error starting recording:', err);
    toast.error('Không thể truy cập microphone', {
      position: 'top',
      duration: 3000
    });
  }
}

const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop();
    isRecording.value = false;

    // Stop all audio tracks
    if (audioStream) {
      audioStream.getTracks().forEach(track => track.stop());
    }
  }
}

const playRecordedAudio = (index: number) => {
  if (!recordedAudioUrls.value[index]) return;

  try {
    const audio = new Audio(recordedAudioUrls.value[index]);
    audio.onended = () => {};
    audio.onerror = () => {
      console.error('Error playing recorded audio');
      toast.error('Không thể phát bản ghi âm', {
        position: 'top',
        duration: 3000
      });
    };

    audio.play().catch(err => {
      console.error('Error playing recorded audio:', err);
      toast.error('Không thể phát bản ghi âm', {
        position: 'top',
        duration: 3000
      });
    });
  } catch (err) {
    console.error('Error playing recorded audio:', err);
    toast.error('Không thể phát bản ghi âm', {
      position: 'top',
      duration: 3000
    });
  }
}

const processRecording = async (index: number) => {
  if (activeLineIndex.value !== index) return;

  isProcessing.value = true;

  try {
    // Demo: Simulate API call to analyze pronunciation
    await new Promise(resolve => setTimeout(resolve, 1500));

    // For demo purposes, generate a random score between 50 and 100
    const randomScore = Math.floor(Math.random() * 51) + 50;
    pronunciationScores.value[index] = randomScore;

    // Chỉ đánh dấu hoàn thành nếu là dòng cuối cùng đang hiển thị
    const lastVisibleIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
    console.log("Processing recording:", {
      index,
      lastVisibleIndex,
      isLastVisible: index === lastVisibleIndex,
      visibleIndices: [...visibleLineIndices.value]
    });

    if (index === lastVisibleIndex) {
      markAsComplete(index);
    } else {
      // Nếu không phải dòng cuối cùng, chỉ cập nhật trạng thái hoàn thành
      lineCompletionStatus.value[index] = true;
    }

    toast.success('Đã phân tích phát âm', {
      position: 'top',
      duration: 2000
    });
  } catch (err) {
    console.error('Error processing recording:', err);
    toast.error('Không thể phân tích bản ghi âm', {
      position: 'top',
      duration: 3000
    });
  } finally {
    isProcessing.value = false;
  }
}

// Hàm kiểm tra xem dòng có được hiển thị không
const isLineVisible = (index: number) => {
  return visibleLineIndices.value.includes(index);
}

// Scroll tới tin nhắn mới nhất
const scrollToLatestMessage = () => {
  // Đợi DOM cập nhật
  setTimeout(() => {
    // Lấy dòng hội thoại mới nhất
    const lastVisibleIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
    if (lastVisibleIndex === undefined) return;

    const lastMessageElement = document.querySelector(`.message-row[data-index="${lastVisibleIndex}"]`);
    if (lastMessageElement) {
      // Lấy vị trí của phần tử so với cửa sổ
      const rect = lastMessageElement.getBoundingClientRect();
      // Cuộn đến vị trí của phần tử mới nhất
      window.scrollTo({
        top: window.scrollY + rect.top - 200, // Hiển thị phần tử với khoảng cách 200px từ đầu trang
        behavior: 'smooth'
      });
      console.log("Scrolled to latest message:", lastVisibleIndex);
    }
  }, 200);
}

// Bắt đầu hiệu ứng typing cho dòng tiếp theo
const startTypingNextLine = () => {
  if (!conversation.value) return;

  const lastVisibleIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
  const nextIndex = lastVisibleIndex + 1;

  // Kiểm tra xem có dòng tiếp theo không
  if (nextIndex < conversation.value.dialogue.length) {
    const nextLine = conversation.value.dialogue[nextIndex];

    // Thêm dòng vào danh sách hiển thị
    visibleLineIndices.value.push(nextIndex);

    // Áp dụng hiệu ứng typing cho tất cả các dòng (cả native và user)
    isTyping.value = true;
    typedText.value = '';
    currentTypeIndex.value = 0;

    // Bắt đầu hiệu ứng typing
    typeTextEffect(nextLine.japanese);

    // Scroll xuống dòng mới nhất
    scrollToLatestMessage();
  }
}

// Hiệu ứng typing cho văn bản
const typeTextEffect = (text: string) => {
  console.log("Typing effect running", currentTypeIndex.value, text);
  if (currentTypeIndex.value < text.length) {
    typedText.value = text.substring(0, currentTypeIndex.value + 1);
    currentTypeIndex.value++;
    setTimeout(() => typeTextEffect(text), typeSpeed);
  } else {
    console.log("Typing effect completed");
    isTyping.value = false;

    // Chỉ tự động hiển thị dòng tiếp theo nếu dòng hiện tại là người bản xứ
    const lastIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
    if (conversation.value && lastIndex < conversation.value.dialogue.length) {
      const currentLine = conversation.value.dialogue[lastIndex];
      if (currentLine.speaker !== 'user') {
        // Nếu dòng hiện tại là người bản xứ, hiển thị dòng tiếp theo
        setTimeout(() => {
          startTypingNextLine();
        }, 1000);
      }
      // Nếu là dòng của người dùng, không tự động chuyển sang dòng tiếp theo
      // Người dùng phải ghi âm để tiếp tục
    }
  }
}

// Cập nhật hàm markAsComplete để hiển thị dòng tiếp theo
const markAsComplete = (index: number) => {
  lineCompletionStatus.value[index] = true;

  // Chỉ hiển thị dòng tiếp theo nếu đây là dòng cuối cùng đang hiển thị
  const lastVisibleIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
  if (index === lastVisibleIndex) {
    // Hiển thị dòng tiếp theo sau khi người dùng hoàn thành
    setTimeout(() => {
      startTypingNextLine();
      // Đảm bảo cuộn xuống sau khi hiển thị dòng mới
      scrollToLatestMessage();
    }, 1000);
  }

  // Check if the conversation is now completed
  if (isConversationCompleted.value) {
    toast.success('Chúc mừng! Bạn đã hoàn thành hội thoại', {
      position: 'top',
      duration: 3000
    });
  }
}

const navigateBack = () => {
  router.push({
    name: 'conversationLearning'
  });
}

// Lifecycle hooks
onMounted(() => {
  loading.value = true;

  // Demo data loading
  setTimeout(() => {
    // Mock conversation data
    conversation.value = {
      id: '1',
      title: 'Chào hỏi và giới thiệu bản thân',
      description: 'Hội thoại cơ bản về giới thiệu bản thân trong tiếng Nhật',
      jlptLevel: 'N5',
      isSaved: false,
      dialogue: [
        {
          speaker: 'native',
          japanese: 'こんにちは、はじめまして。',
          meaning: 'Xin chào, rất vui được gặp bạn.'
        },
        {
          speaker: 'user',
          japanese: 'こんにちは、はじめまして。',
          meaning: 'Xin chào, rất vui được gặp bạn.'
        },
        {
          speaker: 'native',
          japanese: 'お名前は何ですか？',
          meaning: 'Tên bạn là gì?'
        },
        {
          speaker: 'user',
          japanese: 'わたしの名前は＿＿＿です。',
          meaning: 'Tên tôi là ___.'
        },
        {
          speaker: 'native',
          japanese: 'どうぞよろしくお願いします。',
          meaning: 'Rất hân hạnh được gặp bạn.'
        },
        {
          speaker: 'user',
          japanese: 'どうぞよろしくお願いします。',
          meaning: 'Rất hân hạnh được gặp bạn.'
        },
        {
          speaker: 'native',
          japanese: 'ご出身はどちらですか？',
          meaning: 'Bạn đến từ đâu?'
        },
        {
          speaker: 'user',
          japanese: 'ベトナムから来ました。',
          meaning: 'Tôi đến từ Việt Nam.'
        }
      ]
    };

    // Initialize arrays based on dialogue length
    recordedAudioUrls.value = new Array(conversation.value.dialogue.length).fill(null);
    recordedAudioBlobs.value = new Array(conversation.value.dialogue.length).fill(null);
    pronunciationScores.value = new Array(conversation.value.dialogue.length).fill(0);
    lineCompletionStatus.value = new Array(conversation.value.dialogue.length).fill(false);

    // Đặt lại visibleLineIndices
    visibleLineIndices.value = [0];
    isTyping.value = true;
    typedText.value = '';
    currentTypeIndex.value = 0;

    // Bắt đầu hiệu ứng typing cho tin nhắn đầu tiên
    if (conversation.value && conversation.value.dialogue.length > 0) {
      console.log("Starting typing effect for first message");
      typeTextEffect(conversation.value.dialogue[0].japanese);
    }

    loading.value = false;
  }, 1000);

  // Thêm watcher để tự động cuộn xuống khi có dòng mới
  watch(visibleLineIndices, () => {
    console.log("Visible lines changed, scrolling to bottom");
    scrollToLatestMessage();
  }, { deep: true });
})

onUnmounted(() => {
  // Clean up resources when component is destroyed
  if (isRecording.value) {
    stopRecording();
  }

  recordedAudioUrls.value.forEach(url => {
    if (url) URL.revokeObjectURL(url);
  });
})
</script>

<style scoped lang="scss">
.conversation-practice-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 16px;

  .loading-container,
  .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 60vh;
  }

  .japanese-text {
    font-family: 'Noto Sans JP', sans-serif;
  }

  .typing-cursor {
    animation: blink 1s infinite;
    font-weight: bold;
  }

  @keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
  }

  .chat-container {
    display: flex;
    flex-direction: column;
    padding: 4px 0;
    min-height: 50vh;
    position: relative;
    overflow: visible;
    margin-bottom: 16px;
  }

  .message-row {
    display: flex;
    align-items: flex-start;
    width: 100%;
    margin-bottom: 8px;
  }

  .message-container {
    position: relative;
    border-radius: 18px;
    transition: all 0.3s ease;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    min-width: 270px;

    &.user-message {
      background-color: #E3F2FD; // Màu xanh nhạt cho tin nhắn người dùng
      border-top-right-radius: 4px;
      align-self: flex-end;
    }

    &.native-message {
      background-color: #F5F5F5; // Màu xám nhạt cho tin nhắn người bản xứ
      border-top-left-radius: 4px;
      align-self: flex-start;
    }

    &.completed-message {
      border-left-width: 3px;
      border-left-style: solid;
    }

    &.score-excellent {
      border-left-color: #4CAF50; // success
    }

    &.score-very-good {
      border-left-color: #8BC34A; // light-green
    }

    &.score-good {
      border-left-color: #CDDC39; // lime
    }

    &.score-fair {
      border-left-color: #FFC107; // warning
    }

    &.score-poor {
      border-left-color: #F44336; // error
    }
  }

  .message-content {
    position: relative;
    z-index: 0;
  }

  .user-controls {
    background-color: transparent;
    padding: 2px 12px;
    margin: 0;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
  }

  .avatar-container {
    margin-top: 0;
  }
}
</style>
