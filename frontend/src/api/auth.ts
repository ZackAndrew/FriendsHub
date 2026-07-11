import api from './axios';
import type { AuthRequest, LoginRequest, AuthResponse } from '../types';

export const authApi = {
  register: (data: AuthRequest) =>
    api.post<AuthResponse>('/api/auth/register', data),

  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/api/auth/login', data),

  verifyEmail: (token: string) =>
    api.get(`/api/auth/verify/${token}`),
};
