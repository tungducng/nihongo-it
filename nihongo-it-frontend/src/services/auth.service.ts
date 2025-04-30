import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Create axios instance with authorization header interceptor
const apiClient = axios.create({
  baseURL: API_URL
});

// Add a request interceptor to include the token in all requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('Adding token to request:', `Bearer ${token.substring(0, 20)}...`);
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  fullName: string;
  profilePicture?: string;
  currentLevel?: string;
  jlptGoal?: string;
}

export interface LoginResponse {
  result: 'OK' | 'NG';
  token?: string;
  message?: string;
}

export interface SignupResponse {
  status: 'OK' | 'NG';
}

export interface UserInfo {
  userId: number;
  email: string;
  fullName: string;
  roleId: number;
  profilePicture?: string;
  currentLevel?: string;
  jlptGoal?: string;
  lastLogin?: string;
}

export interface GetCurrentUserResponse {
  status: 'OK' | 'NG';
  userInfo?: UserInfo;
}

export interface GoogleLoginRequest {
  tokenId: string;
}

class AuthService {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await apiClient.post('/api/v1/auth/login', credentials);
      const result = response.data;

      if (result.result === 'OK' && result.token) {
        this.saveToken(result.token);
        console.log('Token saved after login');
      }

      return result;
    } catch (error) {
      console.error('Login error:', error);
      return { result: 'NG' };
    }
  }

  async signup(userData: SignupRequest): Promise<SignupResponse> {
    try {
      const response = await apiClient.post('/api/v1/auth/signup', userData);
      return response.data;
    } catch (error) {
      console.error('Signup error:', error);
      throw error;
    }
  }

  async getCurrentUser(): Promise<GetCurrentUserResponse> {
    try {
      console.log('Getting current user, token exists:', !!this.getToken());
      const response = await apiClient.get('/api/v1/auth/current');
      return response.data;
    } catch (error) {
      console.error('Get current user error:', error);
      return { status: 'NG' };
    }
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  saveToken(token: string): void {
    localStorage.setItem('auth_token', token);
  }

  removeToken(): void {
    localStorage.removeItem('auth_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  async isTokenValid(): Promise<boolean> {
    const token = this.getToken();
    if (!token) return false;

    try {
      // Use the getCurrentUser endpoint to verify token validity
      // This endpoint should already exist in your backend
      const response = await apiClient.get('/api/v1/auth/current', {
        timeout: 3000 // Set timeout to detect server unavailability quickly
      });

      // If we get a successful response with user data, the token is valid
      return response.data && response.data.status === 'OK';
    } catch (error) {
      console.error('Token validation error:', error);
      // If server is down or token is invalid, return false
      return false;
    }
  }

  async loginWithGoogle(tokenId: string): Promise<LoginResponse> {
    try {
      console.log('Attempting Google login with token')
      const response = await apiClient.post('/api/v1/auth/google-login', { tokenId })
      const result = response.data

      if (result.result === 'OK' && result.token) {
        this.saveToken(result.token)
        console.log('Token saved after Google login')
      }

      return result
    } catch (error) {
      console.error('Google login error:', error)
      return { result: 'NG' }
    }
  }
}

export default new AuthService();
