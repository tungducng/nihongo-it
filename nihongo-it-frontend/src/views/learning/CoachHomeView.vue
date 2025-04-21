<template>
  <div class="coach-container">
    <!-- Coach Header -->
    <div class="header px-4 py-3 d-flex justify-space-between align-center">
      <div class="d-flex align-center">
        <v-btn icon class="mr-2" @click="goBack">
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
        <img src="https://via.placeholder.com/40/1abc9c/ffffff?text=TC" alt="The Coach" class="coach-logo mr-2" />
        <div class="text-h6 font-weight-bold text-dark">the<span class="primary--text">coach</span></div>
      </div>
      <div class="d-flex align-center">
        <v-btn
          color="warning"
          variant="flat"
          class="upgrade-btn mr-2"
          rounded="pill"
        >
          Nâng cấp Pro <v-icon small class="ml-1">mdi-crown</v-icon>
        </v-btn>
        <v-btn icon variant="text">
          <v-icon>mdi-bell-outline</v-icon>
        </v-btn>
      </div>
    </div>

    <!-- Search Field -->
    <div class="search-container px-4 mb-4">
      <div class="search-field d-flex align-center pa-2 rounded-pill">
        <span class="px-4 text-subtitle-1 text-grey">LỘ TRÌNH GIAO TIẾP</span>
        <v-icon class="mx-2">mdi-refresh</v-icon>
        <v-spacer></v-spacer>
        <v-btn icon class="search-btn">
          <v-icon size="32">mdi-magnify</v-icon>
        </v-btn>
      </div>
    </div>

    <!-- Intro Lesson Card -->
    <div class="px-4 mb-5">
      <v-card class="intro-lesson-card" color="#2B5E6E" dark rounded="lg" @click="navigateToLesson('intro')">
        <v-card-text class="px-4 py-5 position-relative">
          <div class="intro-text">
            <div class="text-subtitle-1 text-uppercase mb-2">BÀI HỌC MỞ ĐẦU</div>
            <div class="text-h5 font-weight-bold mb-4">Chia sẻ về việc học tiếng Anh.</div>
            <v-btn
              color="#1CD4AE"
              rounded="pill"
              class="continue-btn"
              variant="flat"
            >
              Tiếp tục
            </v-btn>
          </div>
          <div class="avatar-container">
            <img src="https://via.placeholder.com/160/ff9955/ffffff?text=Avatar" alt="Avatar" class="avatar-img" />
          </div>
        </v-card-text>
      </v-card>
    </div>

    <!-- Theme Section 1 -->
    <div class="theme-section mb-6">
      <div class="px-4 mb-3">
        <div class="text-subtitle-1 text-grey text-center">CHỦ ĐỀ 1</div>
        <div class="text-h4 font-weight-bold text-center">Tình huống học thử</div>
      </div>

      <!-- Theme Lessons -->
      <div class="theme-lessons px-4 mb-4">
        <v-card class="theme-lesson-card mb-4" variant="outlined" rounded="lg" @click="navigateToLessonDetail('interview')">
          <v-img
            src="https://via.placeholder.com/600/3f51b5/ffffff?text=Interview"
            height="180"
            cover
            class="position-relative"
          >
            <div class="level-badge">
              <div class="text-uppercase font-weight-medium">DỄ</div>
            </div>
          </v-img>
          <v-card-text class="px-4 py-4">
            <div class="text-h6 font-weight-bold">Vượt qua bài phỏng vấn tuyển dụng</div>
            <div class="text-body-2 mt-2">16 Bài học</div>
          </v-card-text>
        </v-card>

        <v-card class="theme-lesson-card mb-4" variant="outlined" rounded="lg" @click="navigateToLessonDetail('foreign')">
          <v-img
            src="https://via.placeholder.com/600/673ab7/ffffff?text=Foreign"
            height="180"
            cover
            class="position-relative"
          >
            <div class="level-badge">
              <div class="text-uppercase font-weight-medium">DỄ</div>
            </div>
          </v-img>
          <v-card-text class="px-4 py-4">
            <div class="text-h6 font-weight-bold">Chỉ đường cho người nước ngoài</div>
            <div class="text-body-2 mt-2">10 Bài học</div>
          </v-card-text>
        </v-card>
      </div>
    </div>

    <!-- Theme Section 2 -->
    <div class="theme-section mb-6">
      <div class="px-4 mb-3">
        <div class="text-subtitle-1 text-grey text-center">CHỦ ĐỀ 2</div>
        <div class="text-h4 font-weight-bold text-center">Công việc hàng ngày</div>
      </div>

      <!-- Theme Lessons -->
      <div class="theme-lessons px-4 mb-4">
        <v-card class="theme-lesson-card mb-4" variant="outlined" rounded="lg" @click="navigateToLessonDetail('workday')">
          <v-img
            src="https://via.placeholder.com/600/2196f3/ffffff?text=Work"
            height="180"
            cover
            class="position-relative"
          >
            <div class="level-badge">
              <div class="text-uppercase font-weight-medium">DỄ</div>
            </div>
            <div class="premium-badge">
              <v-icon color="warning">mdi-crown</v-icon>
            </div>
          </v-img>
          <v-card-text class="px-4 py-4">
            <div class="text-h6 font-weight-bold">Kể về một ngày làm việc của bạn</div>
            <div class="text-body-2 mt-2">9 Bài học</div>
          </v-card-text>
        </v-card>

        <v-card class="theme-lesson-card mb-4" variant="outlined" rounded="lg" @click="navigateToLessonDetail('viewpoint')">
          <v-img
            src="https://via.placeholder.com/600/3f51b5/ffffff?text=Viewpoint"
            height="180"
            cover
            class="position-relative"
          >
            <div class="level-badge">
              <div class="text-uppercase font-weight-medium">VỪA</div>
            </div>
            <div class="premium-badge">
              <v-icon color="warning">mdi-crown</v-icon>
            </div>
          </v-img>
          <v-card-text class="px-4 py-4">
            <div class="text-h6 font-weight-bold">Chia sẻ quan điểm làm việc</div>
            <div class="text-body-2 mt-2">14 Bài học</div>
          </v-card-text>
        </v-card>

        <v-card class="theme-lesson-card mb-4" variant="outlined" rounded="lg" @click="navigateToLessonDetail('schedule')">
          <v-img
            src="https://via.placeholder.com/600/00bcd4/ffffff?text=Schedule"
            height="180"
            cover
            class="position-relative"
          >
            <div class="level-badge">
              <div class="text-uppercase font-weight-medium">VỪA</div>
            </div>
            <div class="premium-badge">
              <v-icon color="warning">mdi-crown</v-icon>
            </div>
          </v-img>
          <v-card-text class="px-4 py-4">
            <div class="text-h6 font-weight-bold">Lên lịch hẹn với khách hàng nước ngoài</div>
            <div class="text-body-2 mt-2">10 Bài học</div>
          </v-card-text>
        </v-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentTime = ref('21:21')
const dataRate = ref('23.0 KB/s')

// Methods
function updateTime() {
  const date = new Date()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

function updateDataRate() {
  const rates = ['0.16 KB/s', '2.36 KB/s', '23.0 KB/s']
  dataRate.value = rates[Math.floor(Math.random() * rates.length)]
}

function goBack() {
  router.back()
}

function navigateToLesson(lessonId: string) {
  router.push(`/learning/lesson/${lessonId}`)
}

function navigateToLessonDetail(courseId: string) {
  router.push(`/learning/course/${courseId}`)
}

// Lifecycle hooks
onMounted(() => {
  updateTime()
  updateDataRate()

  // Update time every minute
  setInterval(updateTime, 60000)

  // Update data rate occasionally (for demonstration only)
  setInterval(updateDataRate, 10000)
})
</script>

<style scoped lang="scss">
.coach-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 64px;
}

.status-bar {
  margin-bottom: 4px;
}

.time-display {
  font-weight: 500;
}

.data-rate {
  font-size: 0.7rem;
}

.status-icons {
  display: flex;
  align-items: center;
}

.battery-status {
  display: flex;
  align-items: center;
}

.coach-logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
}

.upgrade-btn {
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
}

.search-field {
  background-color: #f0f0f0;
  height: 48px;
}

.search-btn {
  background-color: white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.intro-lesson-card {
  overflow: hidden;
  position: relative;
}

.intro-text {
  width: 65%;
  z-index: 2;
  position: relative;
}

.avatar-container {
  position: absolute;
  right: -20px;
  bottom: -10px;
  z-index: 1;
}

.avatar-img {
  width: 160px;
  height: 160px;
  object-fit: cover;
  border-radius: 50%;
}

.continue-btn {
  text-transform: none;
  font-weight: 600;
  letter-spacing: 0;
}

.theme-lesson-card {
  overflow: hidden;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-4px);
  }
}

.level-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 0.75rem;
}

.premium-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.5);
  color: gold;
  padding: 4px;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-navigation {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 10;
}
</style>
