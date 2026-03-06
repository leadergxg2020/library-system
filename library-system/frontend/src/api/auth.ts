import request from './request'
import type { ApiResponse } from './request'

export interface AdminVO {
  id: number
  username: string
}

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  confirmPassword: string
}

export const authApi = {
  login: (data: LoginDTO): Promise<ApiResponse<AdminVO>> =>
    request.post('/auth/login', data),

  logout: (): Promise<ApiResponse<null>> =>
    request.post('/auth/logout'),

  register: (data: RegisterDTO): Promise<ApiResponse<AdminVO>> =>
    request.post('/auth/register', data),

  me: (): Promise<ApiResponse<AdminVO>> =>
    request.get('/auth/me')
}
