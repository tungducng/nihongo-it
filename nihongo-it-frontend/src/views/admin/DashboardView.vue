<template>
  <v-container>
    <div class="dashboard-header filters-container pa-3 rounded mb-4">
      <div class="d-flex flex-wrap align-center">
        <h1 class="text-h5 font-weight-bold mr-4">Dashboard quản trị</h1>
        <v-spacer></v-spacer>

        <v-btn
          color="primary"
          variant="tonal"
          prepend-icon="mdi-refresh"
          @click="fetchDashboardData"
          density="comfortable"
          class="refresh-btn"
          :loading="loading"
        >
          Làm mới dữ liệu
        </v-btn>
      </div>
    </div>

    <v-row v-if="loading && !stats.userCount">
      <v-col cols="12" md="6" lg="3" v-for="i in 4" :key="i">
        <v-skeleton-loader type="card" class="mb-4"></v-skeleton-loader>
      </v-col>
    </v-row>

    <div v-else class="dashboard-content">
      <v-row>
        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title primary-gradient text-white px-4 py-3">
              <v-icon size="large" color="white" class="mr-2">mdi-account-group</v-icon>
              Người dùng
            </v-card-title>
            <v-card-text class="pa-4 text-center">
              <div class="text-h3 mb-1">{{ stats.userCount }}</div>
              <div class="text-caption text-grey">Tổng số người dùng</div>
            </v-card-text>
            <v-card-actions class="px-4 pb-4">
              <v-btn block variant="outlined" color="primary" to="/admin/users" class="action-btn">
                <v-icon class="mr-1">mdi-account-cog</v-icon>
                Quản lý người dùng
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title success-gradient text-white px-4 py-3">
              <v-icon size="large" color="white" class="mr-2">mdi-book-open-variant</v-icon>
              Từ vựng
            </v-card-title>
            <v-card-text class="pa-4 text-center">
              <div class="text-h3 mb-1">{{ stats.vocabularyCount }}</div>
              <div class="text-caption text-grey">Tổng số từ vựng</div>
            </v-card-text>
            <v-card-actions class="px-4 pb-4">
              <v-btn block variant="outlined" color="success" to="/admin/vocabulary" class="action-btn">
                <v-icon class="mr-1">mdi-dictionary</v-icon>
                Quản lý từ vựng
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title info-gradient text-white px-4 py-3">
              <v-icon size="large" color="white" class="mr-2">mdi-folder-multiple</v-icon>
              Danh mục
            </v-card-title>
            <v-card-text class="pa-4 text-center">
              <div class="text-h3 mb-1">{{ stats.categoryCount }}</div>
              <div class="text-caption text-grey">Tổng số danh mục</div>
            </v-card-text>
            <v-card-actions class="px-4 pb-4">
              <v-btn block variant="outlined" color="info" to="/admin/categories" class="action-btn">
                <v-icon class="mr-1">mdi-folder-cog</v-icon>
                Quản lý danh mục
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title warning-gradient text-white px-4 py-3">
              <v-icon size="large" color="white" class="mr-2">mdi-tag-multiple</v-icon>
              Chủ đề
            </v-card-title>
            <v-card-text class="pa-4 text-center">
              <div class="text-h3 mb-1">{{ stats.topicCount }}</div>
              <div class="text-caption text-grey">Tổng số chủ đề</div>
            </v-card-text>
            <v-card-actions class="px-4 pb-4">
              <v-btn block variant="outlined" color="warning" to="/admin/topics" class="action-btn">
                <v-icon class="mr-1">mdi-tag-edit</v-icon>
                Quản lý chủ đề
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <v-card class="mb-4" elevation="2">
            <v-card-title class="stats-title px-4 py-3 success-gradient-light">
              <v-icon class="mr-2">mdi-chart-bar</v-icon>
              Thống kê học viên
            </v-card-title>
            <v-card-text class="pa-4 text-center">
              <p class="text-body-1 mb-4">
                Xem thống kê chi tiết về tiến độ học tập của học viên, bao gồm thẻ ghi nhớ, tỷ lệ ghi nhớ,
                phân bố theo cấp độ JLPT và nhiều thông tin khác.
              </p>
              <v-btn
                size="large"
                color="success"
                variant="elevated"
                class="mr-2"
                prepend-icon="mdi-chart-areaspline"
                to="/admin/statistics"
              >
                Xem thống kê tổng quan
              </v-btn>
              <v-btn
                size="large"
                color="primary"
                variant="tonal"
                prepend-icon="mdi-account-multiple-check"
                to="/admin/statistics/users"
              >
                Thống kê theo học viên
              </v-btn>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="6">
          <v-card class="mb-4 stat-card" elevation="2">
            <v-card-title class="stats-title px-4 py-3 primary-gradient-light">
              <v-icon class="mr-2">mdi-account-clock</v-icon>
              Hoạt động gần đây
            </v-card-title>
            <v-card-text class="pa-0">
              <v-list v-if="recentActivity.length" class="activity-list">
                <template v-for="(activity, index) in recentActivity" :key="index">
                  <v-list-item class="activity-item">
                    <template v-slot:prepend>
                      <v-avatar size="36" color="primary" variant="tonal" class="mr-3">
                        <v-icon color="primary">mdi-account</v-icon>
                      </v-avatar>
                    </template>
                    <v-list-item-title class="font-weight-medium">{{ activity.user }}</v-list-item-title>
                    <v-list-item-subtitle class="text-grey">{{ activity.action }}</v-list-item-subtitle>
                    <template v-slot:append>
                      <v-chip size="small" variant="tonal" color="grey" class="time-chip">
                        {{ formatDate(activity.timestamp) }}
                      </v-chip>
                    </template>
                  </v-list-item>
                  <v-divider v-if="index < recentActivity.length - 1"></v-divider>
                </template>
              </v-list>
              <div v-else class="text-center pa-4">
                <v-icon size="large" color="grey" class="mb-2">mdi-clock-outline</v-icon>
                <p class="text-grey">Không có hoạt động nào gần đây</p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="mb-4 stat-card" elevation="2">
            <v-card-title class="stats-title px-4 py-3 info-gradient-light">
              <v-icon class="mr-2">mdi-chart-line</v-icon>
              Thống kê hệ thống
            </v-card-title>
            <v-card-text class="pa-4">
              <div class="stat-item d-flex justify-space-between align-center mb-3 pb-2">
                <div class="d-flex align-center">
                  <v-icon color="blue" class="mr-2">mdi-account-plus</v-icon>
                  <span>Người dùng mới (7 ngày qua):</span>
                </div>
                <v-chip color="blue" variant="tonal" size="small">{{ stats.newUsers }}</v-chip>
              </div>
              <div class="stat-item d-flex justify-space-between align-center mb-3 pb-2">
                <div class="d-flex align-center">
                  <v-icon color="green" class="mr-2">mdi-account-check</v-icon>
                  <span>Người dùng hoạt động (30 ngày):</span>
                </div>
                <v-chip color="green" variant="tonal" size="small">{{ stats.activeUsers }}</v-chip>
              </div>
              <div class="stat-item d-flex justify-space-between align-center mb-3 pb-2">
                <div class="d-flex align-center">
                  <v-icon color="amber" class="mr-2">mdi-magnify</v-icon>
                  <span>Lượt tìm kiếm từ vựng (hôm nay):</span>
                </div>
                <v-chip color="amber" variant="tonal" size="small">{{ stats.searches }}</v-chip>
              </div>
              <div class="stat-item d-flex justify-space-between align-center mb-3 pb-2">
                <div class="d-flex align-center">
                  <v-icon color="deep-purple" class="mr-2">mdi-cards</v-icon>
                  <span>Flashcards đã tạo (hôm nay):</span>
                </div>
                <v-chip color="deep-purple" variant="tonal" size="small">{{ stats.flashcardsCreated }}</v-chip>
              </div>
              <div class="stat-item d-flex justify-space-between align-center">
                <div class="d-flex align-center">
                  <v-icon color="indigo" class="mr-2">mdi-clipboard-text-play</v-icon>
                  <span>Flashcards đã học (hôm nay):</span>
                </div>
                <v-chip color="indigo" variant="tonal" size="small">{{ stats.flashcardsStudied }}</v-chip>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-snackbar
        v-model="showError"
        color="error"
        timeout="3000"
      >
        {{ errorMessage }}
        <template v-slot:actions>
          <v-btn
            color="white"
            variant="text"
            @click="showError = false"
          >
            Đóng
          </v-btn>
        </template>
      </v-snackbar>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { format } from 'date-fns';
import { useToast } from 'vue-toast-notification';
import adminService from '@/services/admin.service';
import type { DashboardStats } from '@/services/admin.service';

const $toast = useToast();
const loading = ref(false);
const showError = ref(false);
const errorMessage = ref('');

// Khởi tạo dữ liệu thống kê mặc định
const stats = ref({
  userCount: 0,
  vocabularyCount: 0,
  categoryCount: 0,
  topicCount: 0,
  newUsers: 0,
  activeUsers: 0,
  searches: 0,
  flashcardsCreated: 0,
  flashcardsStudied: 0
});

// Khởi tạo dữ liệu hoạt động mặc định
interface ActivityItem {
  user: string;
  action: string;
  timestamp: Date;
}

const recentActivity = ref<ActivityItem[]>([]);

const formatDate = (date: Date) => {
  return format(new Date(date), 'HH:mm dd/MM');
};

async function fetchDashboardData() {
  loading.value = true;
  try {
    const data = await adminService.getDashboardStats();

    // Cập nhật thống kê
    stats.value = {
      userCount: data.userCount || 0,
      vocabularyCount: data.vocabularyCount || 0,
      categoryCount: data.categoryCount || 0,
      topicCount: data.topicCount || 0,
      newUsers: data.newUsers || 0,
      activeUsers: data.activeUsers || 0,
      searches: data.searchesToday || 0,
      flashcardsCreated: data.flashcardsCreatedToday || 0,
      flashcardsStudied: data.flashcardsStudiedToday || 0
    };

    // Cập nhật hoạt động gần đây
    if (data.recentActivities && Array.isArray(data.recentActivities)) {
      recentActivity.value = data.recentActivities.map((activity) => ({
        user: activity.user,
        action: activity.action,
        timestamp: new Date(activity.timestamp)
      }));
    }

    $toast.success('Dữ liệu dashboard đã được cập nhật');
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu dashboard:', error);
    errorMessage.value = 'Không thể tải dữ liệu dashboard. Vui lòng thử lại sau.';
    showError.value = true;

    // Nếu API chưa được triển khai, tải dữ liệu mẫu
    loadSampleData();
    $toast.warning('Đang sử dụng dữ liệu mẫu do API chưa sẵn sàng');
  } finally {
    loading.value = false;
  }
}

// Fallback để tải dữ liệu mẫu trong trường hợp API chưa được triển khai
function loadSampleData() {
  stats.value = {
    userCount: 152,
    vocabularyCount: 1895,
    categoryCount: 7,
    topicCount: 35,
    newUsers: 12,
    activeUsers: 98,
    searches: 254,
    flashcardsCreated: 76,
    flashcardsStudied: 189
  };

  recentActivity.value = [
    {
      user: 'john.doe@example.com',
      action: 'Đã đăng nhập',
      timestamp: new Date(Date.now() - 5 * 60000)
    },
    {
      user: 'alice.smith@example.com',
      action: 'Đã tạo 5 flashcard mới',
      timestamp: new Date(Date.now() - 25 * 60000)
    },
    {
      user: 'bob.johnson@example.com',
      action: 'Đã hoàn thành bài học từ vựng N4',
      timestamp: new Date(Date.now() - 2 * 3600000)
    },
    {
      user: 'sarah.williams@example.com',
      action: 'Đã tạo tài khoản mới',
      timestamp: new Date(Date.now() - 5 * 3600000)
    }
  ];
}

onMounted(async () => {
  try {
    await fetchDashboardData();
  } catch (error) {
    console.error('Không thể kết nối tới API dashboard, đang tải dữ liệu mẫu:', error);
    loadSampleData();
    $toast.warning('Đang sử dụng dữ liệu mẫu');
  }
});
</script>

<style scoped>
.dashboard-header {
  margin-bottom: 24px;
}

.filters-container {
  background: white;
  border: 1px solid #eeeeee;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.dashboard-card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.dashboard-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1) !important;
}

.primary-gradient {
  background: linear-gradient(to right, #1976d2, #2196f3);
}

.primary-gradient-light {
  background: linear-gradient(to right, rgba(25, 118, 210, 0.9), rgba(33, 150, 243, 0.9));
}

.success-gradient {
  background: linear-gradient(to right, #2e7d32, #4caf50);
}

.info-gradient {
  background: linear-gradient(to right, #0288d1, #03a9f4);
}

.info-gradient-light {
  background: linear-gradient(to right, rgba(2, 136, 209, 0.9), rgba(3, 169, 244, 0.9));
}

.warning-gradient {
  background: linear-gradient(to right, #ed6c02, #ff9800);
}

.card-title {
  font-weight: 500;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1) !important;
}

.stats-title {
  font-weight: 500;
  letter-spacing: 0.5px;
}

.stat-item {
  border-bottom: 1px dashed #e0e0e0;
}

.stat-item:last-child {
  border-bottom: none;
}

.activity-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  transition: background-color 0.2s;
}

.activity-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.time-chip {
  font-size: 0.7rem;
}

.action-btn {
  text-transform: none;
  letter-spacing: 0.5px;
  height: 40px;
  font-weight: 500;
}

.refresh-btn {
  text-transform: none;
  letter-spacing: 0.5px;
  font-weight: 500;
}
</style>
