<template>
  <div class="user-statistics-view">
    <div class="d-flex justify-space-between align-center">
      <h1 class="text-h4">Thống kê học viên</h1>
      <v-btn color="primary" prepend-icon="mdi-arrow-left" @click="goToOverview">
        Tổng quan
      </v-btn>
    </div>

    <!-- Filter and Search -->
    <v-card class="my-4">
      <v-card-text>
        <v-row>
          <v-col cols="12" sm="6" md="4">
            <v-text-field
              v-model="searchQuery"
              label="Tìm kiếm theo tên hoặc email"
              prepend-inner-icon="mdi-magnify"
              density="compact"
              hide-details
              class="mb-2"
              @keyup.enter="fetchUserStatistics"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-select
              v-model="sortBy"
              :items="[
                { title: 'Hoạt động gần đây', value: 'lastActive' },
                { title: 'Tên', value: 'userName' },
                { title: 'Tiến độ', value: 'progress' },
                { title: 'Số thẻ', value: 'totalCards' }
              ]"
              label="Sắp xếp theo"
              density="compact"
              hide-details
              @update:model-value="fetchUserStatistics"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-select
              v-model="sortDirection"
              :items="[
                { title: 'Giảm dần', value: 'desc' },
                { title: 'Tăng dần', value: 'asc' }
              ]"
              label="Thứ tự"
              density="compact"
              hide-details
              @update:model-value="fetchUserStatistics"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="6" md="2">
            <v-btn
              color="primary"
              block
              @click="fetchUserStatistics"
              :loading="loading"
            >
              Lọc
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- Loading State -->
    <v-row v-if="loading">
      <v-col cols="12" class="d-flex justify-center align-center" style="min-height: 300px;">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
      </v-col>
    </v-row>

    <!-- Error State -->
    <v-row v-else-if="error">
      <v-col cols="12">
        <v-alert type="error" variant="tonal">
          Không thể tải dữ liệu thống kê. Vui lòng thử lại sau.
          <v-btn color="error" variant="text" class="mt-2" @click="fetchUserStatistics">Thử lại</v-btn>
        </v-alert>
      </v-col>
    </v-row>

    <!-- Data display -->
    <template v-else>
      <!-- User Statistics Table -->
      <v-card>
        <v-data-table
          :headers="headers"
          :items="userStats"
          :items-per-page="10"
          :server-items-length="totalItems"
          :loading="loading"
          :page="page"
          @update:page="handlePageChange"
          @click:row="handleRowClick"
          class="elevation-1"
        >
          <!-- User name column with avatar -->
          <template v-slot:item.userName="{ item }">
            <div class="d-flex align-center">
              <v-avatar color="primary" class="mr-3">
                <span class="text-white">{{ item.userName?.charAt(0) || 'U' }}</span>
              </v-avatar>
              <div>
                <div>{{ item.userName }}</div>
                <div class="text-caption text-grey">{{ item.email }}</div>
              </div>
            </div>
          </template>

          <!-- Summary column -->
          <template v-slot:item.summary="{ item }">
            <div>
              <div class="d-flex align-center text-subtitle-2">
                <v-icon icon="mdi-cards-outline" size="small" class="mr-1"></v-icon>
                {{ item.summary?.totalCards || 0 }} thẻ
              </div>
              <div class="d-flex align-center text-caption">
                <v-icon icon="mdi-calendar-clock" size="small" class="mr-1"></v-icon>
                {{ item.summary?.dueCardsNow || 0 }} thẻ đến hạn
              </div>
            </div>
          </template>

          <!-- Progress column -->
          <template v-slot:item.progress="{ item }">
            <v-progress-linear
              :model-value="item.progress || 0"
              height="15"
              color="primary"
              striped
            >
              <template v-slot:default="{ value }">
                <span class="text-caption font-weight-bold">{{ Math.round(value) }}%</span>
              </template>
            </v-progress-linear>
          </template>

          <!-- Retention rate column -->
          <template v-slot:item.retentionRate="{ item }">
            <v-chip
              :color="getRetentionRateColor(item.summary?.overallRetentionRate)"
              size="small"
            >
              {{ formatPercent(item.summary?.overallRetentionRate) }}
            </v-chip>
          </template>

          <!-- Streak column -->
          <template v-slot:item.streak="{ item }">
            <div class="d-flex align-center">
              <v-icon icon="mdi-fire" color="orange" class="mr-1"></v-icon>
              {{ item.summary?.currentStreak || 0 }}
            </div>
          </template>

          <!-- Last active column -->
          <template v-slot:item.lastActive="{ item }">
            {{ formatDate(item.lastActive) }}
          </template>

          <!-- Actions column -->
          <template v-slot:item.actions="{ item }">
            <v-btn
              icon
              color="primary"
              size="small"
              @click.stop="viewUserDetails(item.userId)"
              variant="text"
            >
              <v-icon>mdi-eye</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import statisticsService from '@/services/statistics.service';
import type { UserStatistics } from '@/services/statistics.service';

const router = useRouter();

// State
const loading = ref(false);
const error = ref(false);
const userStats = ref<UserStatistics[]>([]);
const totalItems = ref(0);
const totalPages = ref(0);
const page = ref(1);

// Filters and sorting
const searchQuery = ref('');
const sortBy = ref('lastActive');
const sortDirection = ref('desc');

// Table headers
const headers = [
  { title: 'Học viên', key: 'userName', sortable: true },
  { title: 'Thống kê', key: 'summary', sortable: false },
  { title: 'Tiến độ', key: 'progress', sortable: true },
  { title: 'Tỷ lệ ghi nhớ', key: 'retentionRate', sortable: true },
  { title: 'Chuỗi ngày', key: 'streak', sortable: true },
  { title: 'Hoạt động gần đây', key: 'lastActive', sortable: true },
  { title: '', key: 'actions', sortable: false }
];

// Format helpers
const formatPercent = (value: number | undefined) => {
  if (value === undefined) return '0%';
  return `${Math.round(value)}%`;
};

const formatDate = (dateString: string | undefined) => {
  if (!dateString) return 'Không rõ';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getRetentionRateColor = (rate: number | undefined) => {
  if (rate === undefined) return 'grey';
  if (rate >= 80) return 'success';
  if (rate >= 60) return 'info';
  if (rate >= 40) return 'warning';
  return 'error';
};

// Navigation functions
const goToOverview = () => {
  router.push('/admin/statistics');
};

const viewUserDetails = (userId: string) => {
  router.push(`/admin/statistics/users/${userId}`);
};

const handleRowClick = (event: any, { item }: { item: UserStatistics }) => {
  viewUserDetails(item.userId);
};

// Pagination handler
const handlePageChange = (newPage: number) => {
  page.value = newPage;
  fetchUserStatistics();
};

// Fetch user statistics
const fetchUserStatistics = async () => {
  loading.value = true;
  error.value = false;

  try {
    // Convert page from 1-based to 0-based for backend
    const pageIndex = page.value - 1;

    const response = await statisticsService.getAllUserStatistics(
      pageIndex,
      10, // Page size
      sortBy.value === 'totalCards' ? 'summary.totalCards' : sortBy.value,
      sortDirection.value
    );

    userStats.value = response.users;
    totalItems.value = response.totalItems;
    totalPages.value = response.totalPages;

    console.log('User statistics:', response);
  } catch (err) {
    console.error('Error fetching user statistics:', err);
    error.value = true;
  } finally {
    loading.value = false;
  }
};

// Lifecycle hooks
onMounted(() => {
  fetchUserStatistics();
});
</script>

<style scoped lang="scss">
.v-data-table {
  cursor: pointer;
}
</style>
