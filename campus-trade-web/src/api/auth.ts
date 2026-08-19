import http from './http'
import type { AuthUser } from '../stores/auth'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface RegisterPayload {
  username: string
  email: string
  password: string
  emailCode: string
}

interface LoginPayload {
  username: string
  password: string
}

interface LoginData {
  token: string
  tokenType: string
  expiresIn: number
  user: AuthUser
}

export function sendEmailCode(email: string) {
  return http.post<ApiResponse<null>>('/auth/email-code', { email })
}

export function register(payload: RegisterPayload) {
  return http.post<ApiResponse<null>>('/auth/register', payload)
}

export function login(payload: LoginPayload) {
  return http.post<ApiResponse<LoginData>>('/auth/login', payload)
}

export function getCurrentUser(token?: string) {
  return http.get<ApiResponse<AuthUser>>('/auth/me', {
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  })
}

export function logout() {
  return http.post<ApiResponse<null>>('/auth/logout')
}
