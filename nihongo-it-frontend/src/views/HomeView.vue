<template>
  <div class="dashboard">
    <v-container>
      <v-row>
        <v-col cols="12">
          <h1 class="welcome-header">
            こんにちは、{{ username }}さん！
            <small>Welcome to your Japanese learning journey</small>
          </h1>
        </v-col>
      </v-row>

      <v-row>
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

      <v-row class="mt-4">
        <v-col cols="12">
          <h2>Recommended for Today</h2>
        </v-col>

        <v-col v-for="lesson in suggestedLessons" :key="lesson.id" cols="12" sm="6" md="4">
          <v-card>
            <v-card-title>{{ lesson.title }}</v-card-title>
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
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'

interface Lesson {
  id: string
  title: string
  type: string
  level: string
  estimatedMinutes: number
  description: string
}

@Component({
  name: 'DashboardView'
})
export default class DashboardView extends Vue {
  // Mock data instead of store data
  username = 'Guest'
  currentLevel = 'N5'

  // Static progress data
  dailyProgress = {
    minutesStudied: 25,
  }

  studyPlan = {
    dailyGoalMinutes: 60,
  }

  dailyGoalProgress = 42 // Static percentage

  // Mock suggested lessons
  suggestedLessons: Lesson[] = [
    {
      id: '1',
      title: 'Basic Greetings',
      type: 'conversation',
      level: 'N5',
      estimatedMinutes: 15,
      description: 'Learn essential Japanese greetings for everyday conversations.',
    },
    {
      id: '2',
      title: 'Numbers 1-100',
      type: 'vocabulary',
      level: 'N5',
      estimatedMinutes: 20,
      description: 'Master counting in Japanese from 1 to 100.',
    },
    {
      id: '3',
      title: 'IT Terminology Basics',
      type: 'vocabulary',
      level: 'N5',
      estimatedMinutes: 25,
      description: 'Essential IT vocabulary for beginners in Japanese.',
    },
  ]
}
</script>

<style lang="sass" scoped>
.welcome-header
  margin-bottom: 2rem

  small
    display: block
    font-size: 1rem
    font-weight: normal
    color: rgba(0, 0, 0, 0.7)
    margin-top: 0.5rem

.daily-progress
  text-align: center

  .v-card-text
    display: flex
    flex-direction: column
    align-items: center

.progress-details
  text-align: center

  p
    margin: 0.5rem 0
</style>
