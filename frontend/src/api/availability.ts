import api from './axios';
import type { AvailabilityRequest, AvailabilitySlot, CommonSlot } from '../types';

export const availabilityApi = {
  addSlot: (data: AvailabilityRequest) =>
    api.post<AvailabilitySlot>('/api/availability', data),

  getUserSlots: (userId: number) =>
    api.get<AvailabilitySlot[]>(`/api/availability/user/${userId}`),

  getCommonSlots: (from?: string, to?: string) =>
    api.get<CommonSlot[]>('/api/availability/common', {
      params: { from, to },
    }),
};
