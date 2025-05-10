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
          <span class="text-subtitle-1 font-weight-medium">Luyện hội thoại</span>
        </div>

        <v-card class="" variant="flat">
          <div class="d-flex align-center px-2 py-1">
            <div class="conversation-info d-flex align-center">
              <h2 class="text-body-1 font-weight-bold mb-0 mr-2">{{ conversation.title }}</h2>
              <v-chip :color="getJlptColor(conversation.jlptLevel)" size="x-small" density="compact">
                {{ conversation.jlptLevel }}
              </v-chip>
              <div class="text-caption text-medium-emphasis ml-3 d-none d-sm-block">
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
            <v-chip color="info" label>
              <v-icon start>mdi-robot</v-icon>
              Nihongo IT
            </v-chip>
          </div>

          <!-- Chat Messages Container -->
          <div class="chat-container">
            <template v-for="(line, i) in conversation.dialogue" :key="i">
              <div
                v-if="visibleLineIndices.includes(i) || dimmedLineIndices.includes(i)"
                class="message-row mb-2"
                :class="[
                  line.speaker === 'user' ? 'justify-end' : 'justify-start',
                  dimmedLineIndices.includes(i) ? 'dimmed-line' : ''
                ]"
                :data-index="i"
              >
                <!-- Avatar for Nihongo IT -->
                <div v-if="line.speaker !== 'user'" class="avatar-container mr-2">
                  <v-avatar size="36" color="info">
                    <v-icon color="white">mdi-robot</v-icon>
                  </v-avatar>
                </div>

                <!-- Message Bubble -->
                <div
                  class="message-container"
                  :class="{
                    'user-message': line.speaker === 'user',
                    'bot-message': line.speaker !== 'user',
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
                    <v-img
                      v-if="userProfilePicture"
                      :src="userProfilePicture"
                      alt="Ảnh đại diện của bạn"
                    ></v-img>
                    <span v-else class="text-subtitle-2 text-white">{{ avatarInitials }}</span>
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
import { useAuthStore } from '@/stores'
import axios, { AxiosError } from 'axios'
import authService from '@/services/auth.service'

// Define types
interface ConversationLine {
  speaker: 'user' | 'bot';
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
const authStore = useAuthStore()

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
const dimmedLineIndices = ref<number[]>([]) // Danh sách các dòng hiển thị mờ
const isTyping = ref(true); // Bắt đầu với hiệu ứng typing
const typedText = ref('');
const currentTypeIndex = ref(0);
const typeSpeed = ref(60); // ms per character - tốc độ typing có thể điều chỉnh
const typingVariation = ref(15); // Thêm biến đổi ngẫu nhiên cho tốc độ typing để tự nhiên hơn

// Computed
const isConversationCompleted = computed(() => {
  const userLines = conversation.value?.dialogue.filter(line => line.speaker === 'user') || [];
  return userLines.length > 0 &&
    userLines.every((_, index) => lineCompletionStatus.value[getUserLineIndex(index)]);
})

// Computed cho thông tin user
const userProfilePicture = computed(() => authStore.user?.profilePicture || null)
const avatarInitials = computed(() => {
  if (!authStore.user?.fullName) return 'U'
  return authStore.user.fullName
    .split(' ')
    .map(name => name.charAt(0))
    .join('')
    .toUpperCase()
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

const playAudio = async (line: ConversationLine) => {
  if (!line.japanese) return;

  try {
    // Verify authentication before proceeding
    const authToken = authService.getToken()
    if (!authToken) {
      toast.error('Vui lòng đăng nhập để sử dụng tính năng đọc văn bản', {
        position: 'top',
        duration: 4000
      })
      // Redirect to login page after a short delay
      setTimeout(() => {
        router.push({
          name: 'login',
          query: { redirect: router.currentRoute.value.fullPath }
        })
      }, 1500)
      return
    }

    // Text to speak
    const textToSpeak = line.japanese;

    // Get the backend API URL
    const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

    // First check if audio already exists
    try {
      const checkResponse = await axios.get(`${apiUrl}/api/v1/tts/check`, {
        params: {
          text: textToSpeak,
          contentType: "conversation"
        },
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });

      if (checkResponse.data.exists) {
        // Audio exists, use it
        console.log('Using existing conversation audio file');
        toast.info('Đang phát âm thanh...', {
          position: 'top',
          duration: 2000
        });

        // Get the audio file
        const response = await axios.get(`${apiUrl}/api/v1/tts/audio`, {
          params: {
            text: textToSpeak,
            contentType: "conversation"
          },
          headers: {
            'Authorization': `Bearer ${authToken}`
          },
          responseType: 'blob'
        });

        // Play the audio
        const audioBlob = new Blob([response.data], { type: 'audio/mpeg' });
        const audioUrl = URL.createObjectURL(audioBlob);
        const audio = new Audio(audioUrl);
        audio.onended = () => {
          URL.revokeObjectURL(audioUrl);
        };
        await audio.play();
        return;
      }
    } catch (error) {
      console.log('Error checking for existing audio:', error);
      // Continue with generating new audio if check fails
    }

    // No existing audio found, generate new audio
    // Show loading indicator
    toast.info('Đang tạo âm thanh...', {
      position: 'top',
      duration: 2000
    })

    // Set speed for conversation sentences
    const speed = 1.0;

    // Call the TTS API with Authorization header
    const response = await axios.post(`${apiUrl}/api/v1/tts/generate`, textToSpeak, {
      headers: {
        'Content-Type': 'text/plain; charset=UTF-8',
        'Accept-Language': 'ja-JP',
        'X-Speech-Speed': speed.toString(),
        'X-Content-Language': 'ja',
        'X-Content-Type': 'conversation',
        'X-Save-Audio': 'true', // Tell backend to save this audio
        'Authorization': `Bearer ${authToken}`,
        'Accept': 'audio/mpeg'
      },
      responseType: 'arraybuffer'
    })

    // Convert response to blob and create audio URL
    const audioBlob = new Blob([response.data], { type: 'audio/mpeg' })
    const audioUrl = URL.createObjectURL(audioBlob)

    // Play the audio
    const audio = new Audio(audioUrl)
    audio.onended = () => {
      URL.revokeObjectURL(audioUrl)
    }
    await audio.play()
  } catch (error) {
    console.error('Error generating or playing TTS audio:', error)

    // Special handling for 401 errors
    if (error instanceof AxiosError && error.response?.status === 401) {
      toast.error('Dịch vụ TTS yêu cầu xác thực. Vui lòng đăng nhập lại khi có thể.', {
        position: 'top',
        duration: 3000
      })
    } else {
      toast.error(error instanceof Error ? error.message : 'Không thể tạo giọng nói', {
        position: 'top',
        duration: 3000
      })
    }
  }
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

// Scroll tới tin nhắn mới nhất với hiệu ứng mượt hơn
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
      const targetPosition = window.scrollY + rect.top - 150;

      // Giảm thiểu giật lag bằng cách sử dụng biến delta nhỏ cho mỗi frame
      const startPosition = window.scrollY;
      const distance = targetPosition - startPosition;

      // Chỉ cuộn khi khoảng cách đủ lớn để tránh cuộn không cần thiết
      if (Math.abs(distance) < 20) return;

      const duration = Math.min(Math.abs(distance), 600); // Thời gian tối đa là 600ms, ngắn hơn cho khoảng cách nhỏ
      let startTime: number | null = null;

      // Hàm animation cuộn với easing function mượt mà hơn
      function animateScroll(timestamp: number) {
        if (!startTime) startTime = timestamp;
        const elapsed = timestamp - startTime;
        const progress = Math.min(elapsed / duration, 1);

        // Easing function cải tiến (easeOutQuint)
        const easedProgress = 1 - Math.pow(1 - progress, 3);

        window.scrollTo({
          top: startPosition + distance * easedProgress,
          behavior: 'auto' // Sử dụng 'auto' vì chúng ta đang tự xử lý animation
        });

        if (elapsed < duration) {
          requestAnimationFrame(animateScroll);
        }
      }

      // Bắt đầu animation với requestAnimationFrame để đồng bộ với refresh rate màn hình
      requestAnimationFrame(animateScroll);
    }
  }, 50); // Giảm thời gian chờ từ 100ms xuống 50ms
}

// Hiệu ứng typing cải tiến cho văn bản
const typeTextEffect = (text: string) => {
  // Sử dụng requestAnimationFrame để đồng bộ với refresh rate màn hình
  if (!text || text.length === 0) {
    isTyping.value = false;
    return;
  }

  if (currentTypeIndex.value < text.length) {
    // Sử dụng RAF để đồng bộ với refresh rate của màn hình
    requestAnimationFrame(() => {
      typedText.value = text.substring(0, currentTypeIndex.value + 1);
      currentTypeIndex.value++;

      // Xác định thời gian chờ cho ký tự tiếp theo
      const currentChar = text[currentTypeIndex.value - 1];
      const pauseChars = ['。', '、', '!', '?', '！', '？', '…', '.', ','];

      // Tăng độ biến thiên cho tốc độ typing để tự nhiên hơn
      const randomVariation = Math.random() * typingVariation.value * 2 - typingVariation.value;
      let delay = Math.max(20, typeSpeed.value + randomVariation);

      // Thêm thời gian dừng cho dấu câu hoặc khoảng trắng
      if (pauseChars.includes(currentChar)) {
        delay += 250; // Dừng lâu hơn sau dấu câu
      } else if (currentChar === ' ') {
        delay += 40; // Dừng nhẹ cho khoảng trắng
      }

      // Giảm thời gian trễ nếu là ký tự cuối để tránh chờ quá lâu
      if (currentTypeIndex.value === text.length - 1) {
        delay = Math.min(delay, 60);
      }

      setTimeout(() => typeTextEffect(text), delay);
    });
  } else {
    isTyping.value = false;

    // Lấy dòng hiện tại đang typing
    const lastVisibleIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
    if (!conversation.value || lastVisibleIndex >= conversation.value.dialogue.length) return;

    const currentLine = conversation.value.dialogue[lastVisibleIndex];

    // Chỉ hiển thị mờ dòng tiếp theo nếu là dòng của Nihongo IT
    const nextIndex = lastVisibleIndex + 1;
    if (conversation.value && nextIndex < conversation.value.dialogue.length) {
      const nextLine = conversation.value.dialogue[nextIndex];

      // Hiển thị mờ dòng tiếp theo nếu là của Nihongo IT
      if (nextLine.speaker !== 'user' && !dimmedLineIndices.value.includes(nextIndex) && !visibleLineIndices.value.includes(nextIndex)) {
        requestAnimationFrame(() => {
          dimmedLineIndices.value.push(nextIndex);
        });
      }
    }

    // Chỉ tự động hiển thị dòng tiếp theo nếu là dòng đầu tiên của Nihongo IT
    if (currentLine.speaker !== 'user' && lastVisibleIndex === 0) {
      setTimeout(() => {
        startTypingNextLine();
      }, 800); // Giảm xuống 800ms thay vì 1000ms
    }
  }
}

// Bắt đầu hiệu ứng typing cho dòng tiếp theo
const startTypingNextLine = () => {
  if (!conversation.value) return;

  const lastVisibleIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
  const nextIndex = lastVisibleIndex + 1;

  // Kiểm tra xem có dòng tiếp theo không
  if (nextIndex < conversation.value.dialogue.length) {
    const nextLine = conversation.value.dialogue[nextIndex];

    // Nếu dòng tiếp theo đang hiển thị mờ, xóa khỏi danh sách hiển thị mờ
    requestAnimationFrame(() => {
      if (dimmedLineIndices.value.includes(nextIndex)) {
        dimmedLineIndices.value = dimmedLineIndices.value.filter(i => i !== nextIndex);
      }

      // Thêm dòng vào danh sách hiển thị
      visibleLineIndices.value.push(nextIndex);

      // Áp dụng hiệu ứng typing
      isTyping.value = true;
      typedText.value = '';
      currentTypeIndex.value = 0;

      // Đảm bảo tất cả thay đổi DOM đã được áp dụng trước khi bắt đầu typing
      setTimeout(() => {
        typeTextEffect(nextLine.japanese);
        // Cuộn sau khi bắt đầu hiệu ứng typing nhưng không chờ hoàn thành
        scrollToLatestMessage();
      }, 50);
    });
  }
}

// Cập nhật hàm markAsComplete để hiển thị dòng tiếp theo
const markAsComplete = (index: number) => {
  lineCompletionStatus.value[index] = true;

  // Chỉ hiển thị dòng tiếp theo nếu đây là dòng cuối cùng đang hiển thị
  const lastVisibleIndex = visibleLineIndices.value[visibleLineIndices.value.length - 1];
  if (index === lastVisibleIndex) {
    // Nếu dòng tiếp theo đã hiển thị mờ, cần bỏ khỏi danh sách dimmedLineIndices và thêm vào visible
    const nextIndex = index + 1;

    // Sử dụng requestAnimationFrame để tránh blocking main thread
    requestAnimationFrame(() => {
      if (dimmedLineIndices.value.includes(nextIndex)) {
        // Xóa khỏi danh sách mờ và thêm vào danh sách hiển thị đầy đủ
        dimmedLineIndices.value = dimmedLineIndices.value.filter(i => i !== nextIndex);
        visibleLineIndices.value.push(nextIndex);

        // Kiểm tra dòng tiếp theo sau dòng của Nihongo IT
        if (conversation.value && nextIndex + 1 < conversation.value.dialogue.length) {
          const lineAfterNext = conversation.value.dialogue[nextIndex + 1];
          // Nếu dòng sau đó là của Nihongo IT, thêm vào danh sách hiển thị mờ
          if (lineAfterNext.speaker !== 'user') {
            dimmedLineIndices.value.push(nextIndex + 1);
          }
        }

        // Cuộn xuống dòng mới nhất - không cần đợi animation kết thúc
        scrollToLatestMessage();

        // Sau khi hiển thị dòng Nihongo IT, tiếp tục hiển thị dòng người dùng kế tiếp (nếu có)
        const nextUserIndex = nextIndex + 1;
        if (conversation.value && nextUserIndex < conversation.value.dialogue.length &&
            conversation.value.dialogue[nextUserIndex].speaker === 'user') {

          // Giảm thời gian chờ để cải thiện trải nghiệm
          setTimeout(() => {
            requestAnimationFrame(() => {
              // Bỏ dòng người dùng khỏi danh sách dimmed (nếu có) và thêm vào visible
              if (dimmedLineIndices.value.includes(nextUserIndex)) {
                dimmedLineIndices.value = dimmedLineIndices.value.filter(i => i !== nextUserIndex);
              }
              visibleLineIndices.value.push(nextUserIndex);

              // Áp dụng hiệu ứng typing cho dòng người dùng
              isTyping.value = true;
              typedText.value = '';
              currentTypeIndex.value = 0;

              // Kiểm tra lại conversation.value trước khi sử dụng
              if (conversation.value) {
                typeTextEffect(conversation.value.dialogue[nextUserIndex].japanese);
              }

              // Cuộn xuống dòng mới nhất sau một khoảng thời gian nhỏ
              setTimeout(() => scrollToLatestMessage(), 50);
            });
          }, 700); // Giảm từ 1000ms xuống 700ms
        }
      } else {
        // Nếu không có dòng mờ kế tiếp, tiếp tục hiển thị dòng tiếp theo như thông thường
        setTimeout(() => {
          startTypingNextLine();
          // Đảm bảo cuộn xuống sau khi hiển thị dòng mới
          scrollToLatestMessage();
        }, 700); // Giảm từ 1000ms xuống 700ms
      }
    });
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
          speaker: 'bot',
          japanese: 'こんにちは、はじめまして。',
          meaning: 'Xin chào, rất vui được gặp bạn.'
        },
        {
          speaker: 'user',
          japanese: 'こんにちは、はじめまして。',
          meaning: 'Xin chào, rất vui được gặp bạn.'
        },
        {
          speaker: 'bot',
          japanese: 'お名前は何ですか？',
          meaning: 'Tên bạn là gì?'
        },
        {
          speaker: 'user',
          japanese: 'わたしの名前は＿＿＿です。',
          meaning: 'Tên tôi là ___.'
        },
        {
          speaker: 'bot',
          japanese: 'どうぞよろしくお願いします。',
          meaning: 'Rất hân hạnh được gặp bạn.'
        },
        {
          speaker: 'user',
          japanese: 'どうぞよろしくお願いします。',
          meaning: 'Rất hân hạnh được gặp bạn.'
        },
        {
          speaker: 'bot',
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
    dimmedLineIndices.value = [];
    isTyping.value = true;
    typedText.value = '';
    currentTypeIndex.value = 0;

    // Kiểm tra xem dòng thứ 2 có phải của Nihongo IT không để hiển thị mờ
    if (conversation.value.dialogue.length > 1 && conversation.value.dialogue[1].speaker !== 'user') {
      dimmedLineIndices.value.push(1);
      console.log("Initially showing dimmed next line: 1");
    }

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
    will-change: contents;
    transform: translateZ(0);
  }

  .typing-cursor {
    display: inline-block;
    vertical-align: middle;
    transform-origin: bottom;
    will-change: opacity, transform;
    animation: cursorBlink 0.7s step-end infinite,
               cursorBounce 3s ease-in-out infinite;
  }

  @keyframes cursorBlink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
  }

  @keyframes cursorBounce {
    0%, 20%, 40%, 60%, 80%, 100% { transform: translateY(0) translateZ(0); }
    50% { transform: translateY(-2px) translateZ(0); }
  }

  .chat-container {
    display: flex;
    flex-direction: column;
    padding: 4px 0;
    min-height: 50vh;
    position: relative;
    overflow: visible;
    margin-bottom: 16px;
    contain: layout style;
  }

  .message-row {
    display: flex;
    align-items: flex-start;
    width: 100%;
    margin-bottom: 8px;
    animation: fadeInUp 0.4s cubic-bezier(0.25, 0.1, 0.25, 1) forwards;
    opacity: 0;
    will-change: transform, opacity;
    transform: translateZ(0);

    &.dimmed-line {
      opacity: 0.6;
      filter: blur(0.5px);
      pointer-events: none;
      animation: fadeInDimmed 0.5s cubic-bezier(0.25, 0.1, 0.25, 1) forwards;

      .message-container {
        opacity: 0.6;
        border-left: 3px dashed rgba(0,0,0,0.1);
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
        transform: scale(0.98);
      }
    }
  }

  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translate3d(0, 20px, 0);
    }
    to {
      opacity: 1;
      transform: translate3d(0, 0, 0);
    }
  }

  @keyframes fadeInDimmed {
    from {
      opacity: 0;
      transform: translate3d(0, 15px, 0);
    }
    to {
      opacity: 0.6;
      transform: translate3d(0, 0, 0);
    }
  }

  .message-container {
    position: relative;
    border-radius: 18px;
    transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    min-width: 270px;
    transform-origin: bottom center;
    animation: messageScale 0.25s cubic-bezier(0.25, 0.1, 0.25, 1) forwards;
    will-change: transform, opacity, box-shadow;
    backface-visibility: hidden;

    &:hover {
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px) translateZ(0);
    }

    &.user-message {
      background-color: #E3F2FD; // Màu xanh nhạt cho tin nhắn người dùng
      border-top-right-radius: 4px;
      align-self: flex-end;
    }

    &.bot-message {
      background-color: #E8F5E9; // Màu xanh lá nhạt cho tin nhắn của Nihongo IT
      border-top-left-radius: 4px;
      align-self: flex-start;
    }

    &.completed-message {
      border-left-width: 3px;
      border-left-style: solid;
      animation: pulse 0.8s cubic-bezier(0.25, 0.1, 0.25, 1);
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

    @keyframes messageScale {
      from {
        transform: scale3d(0.98, 0.98, 1);
        opacity: 0.8;
      }
      to {
        transform: scale3d(1, 1, 1);
        opacity: 1;
      }
    }

    @keyframes pulse {
      0% {
        box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.4);
      }
      70% {
        box-shadow: 0 0 0 6px rgba(76, 175, 80, 0);
      }
      100% {
        box-shadow: 0 0 0 0 rgba(76, 175, 80, 0);
      }
    }
  }

  .message-content {
    position: relative;
    z-index: 0;
    will-change: contents;
    contain: content;
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
