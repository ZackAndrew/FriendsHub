import api from './axios';
import type { User, PageableResponse } from '../types';

export const usersApi = {
  getById: (id: number) =>
    api.get<User>(`/api/users/${id}`),

  getByUsername: (username: string) =>
    api.get<User>(`/api/users/by-username/${username}`),

  getAll: (page = 0, size = 20) =>
    api.get<PageableResponse<User>>('/api/users', { params: { page, size } }),

  updateStatus: (userId: number, status: string) =>
    api.patch<User>(`/api/users/${userId}/status`, { status }),

  delete: (userId: number) =>
    api.delete(`/api/users/${userId}`),
};
