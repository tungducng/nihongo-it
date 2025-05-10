<template>
  <div class="admin-dashboard">
    <h1 class="text-h4 mb-5">Admin Dashboard</h1>

    <div class="dashboard-content">
      <v-row>
        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title">
              <v-icon size="large" color="primary" class="mr-2">mdi-account-group</v-icon>
              Users
            </v-card-title>
            <v-card-text>
              <div class="text-h3 text-center">{{ stats.userCount }}</div>
              <div class="text-center text-caption">Total users</div>
            </v-card-text>
            <v-card-actions>
              <v-btn block variant="tonal" color="primary" to="/admin/users">
                Manage Users
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title">
              <v-icon size="large" color="success" class="mr-2">mdi-book-open-variant</v-icon>
              Vocabulary
            </v-card-title>
            <v-card-text>
              <div class="text-h3 text-center">{{ stats.vocabularyCount }}</div>
              <div class="text-center text-caption">Total vocabulary entries</div>
            </v-card-text>
            <v-card-actions>
              <v-btn block variant="tonal" color="success" to="/admin/vocabulary">
                Manage Vocabulary
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title">
              <v-icon size="large" color="info" class="mr-2">mdi-folder-multiple</v-icon>
              Categories
            </v-card-title>
            <v-card-text>
              <div class="text-h3 text-center">{{ stats.categoryCount }}</div>
              <div class="text-center text-caption">Total categories</div>
            </v-card-text>
            <v-card-actions>
              <v-btn block variant="tonal" color="info" to="/admin/categories">
                Manage Categories
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6" lg="3">
          <v-card class="mb-4 dashboard-card" elevation="2">
            <v-card-title class="card-title">
              <v-icon size="large" color="warning" class="mr-2">mdi-tag-multiple</v-icon>
              Topics
            </v-card-title>
            <v-card-text>
              <div class="text-h3 text-center">{{ stats.topicCount }}</div>
              <div class="text-center text-caption">Total topics</div>
            </v-card-text>
            <v-card-actions>
              <v-btn block variant="tonal" color="warning" to="/admin/topics">
                Manage Topics
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="6">
          <v-card class="mb-4" elevation="2">
            <v-card-title>
              <v-icon class="mr-2">mdi-account-clock</v-icon>
              Recent User Activity
            </v-card-title>
            <v-card-text v-if="recentActivity.length">
              <v-list lines="two">
                <v-list-item v-for="(activity, index) in recentActivity" :key="index">
                  <v-list-item-title>{{ activity.user }}</v-list-item-title>
                  <v-list-item-subtitle>{{ activity.action }}</v-list-item-subtitle>
                  <template v-slot:append>
                    <div class="text-caption">{{ formatDate(activity.timestamp) }}</div>
                  </template>
                </v-list-item>
              </v-list>
            </v-card-text>
            <v-card-text v-else>
              <p class="text-center pa-4">No recent activity found</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="mb-4" elevation="2">
            <v-card-title>
              <v-icon class="mr-2">mdi-chart-line</v-icon>
              System Statistics
            </v-card-title>
            <v-card-text>
              <div class="d-flex justify-space-between mb-2">
                <div>New users (last 7 days):</div>
                <div>{{ stats.newUsers }}</div>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <div>Active users (last 30 days):</div>
                <div>{{ stats.activeUsers }}</div>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <div>Vocabulary searches (today):</div>
                <div>{{ stats.searches }}</div>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <div>Flashcards created (today):</div>
                <div>{{ stats.flashcardsCreated }}</div>
              </div>
              <div class="d-flex justify-space-between mb-2">
                <div>Flashcards studied (today):</div>
                <div>{{ stats.flashcardsStudied }}</div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { format } from 'date-fns';

// Mock data for development - would be replaced with API calls
const stats = ref({
  userCount: 152,
  vocabularyCount: 1895,
  categoryCount: 7,
  topicCount: 35,
  newUsers: 12,
  activeUsers: 98,
  searches: 254,
  flashcardsCreated: 76,
  flashcardsStudied: 189
});

const recentActivity = ref([
  {
    user: 'john.doe@example.com',
    action: 'Logged in',
    timestamp: new Date(Date.now() - 5 * 60000)
  },
  {
    user: 'alice.smith@example.com',
    action: 'Created 5 new flashcards',
    timestamp: new Date(Date.now() - 25 * 60000)
  },
  {
    user: 'bob.johnson@example.com',
    action: 'Completed N4 vocabulary study session',
    timestamp: new Date(Date.now() - 2 * 3600000)
  },
  {
    user: 'sarah.williams@example.com',
    action: 'Account created',
    timestamp: new Date(Date.now() - 5 * 3600000)
  }
]);

const formatDate = (date: Date) => {
  return format(date, 'MMM dd, HH:mm');
};

onMounted(() => {
  // Fetch dashboard statistics from API
  // Replace with actual API calls when backend is ready
  console.log('Dashboard mounted, fetching stats...');
});
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
}

.dashboard-card {
  transition: transform 0.2s;
}

.dashboard-card:hover {
  transform: translateY(-5px);
}

.card-title {
  display: flex;
  align-items: center;
}
</style>
