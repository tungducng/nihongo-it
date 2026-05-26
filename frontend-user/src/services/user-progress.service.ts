import api from '@/lib/api'

export interface UserProgress {
  userId: string
  streakCount: number
  lastStudyDate: string | null
  points: number
  dailyGoalMinutes: number
}

const userProgressService = {
  async getMyProgress(): Promise<UserProgress> {
    const res = await api.get<UserProgress>('/api/v1/learning/users/me/progress')
    return res.data
  },
}

export default userProgressService
