import api from './axios';
import type { MeetingRequest, MeetingResponse } from '../types';

export const meetingsApi = {
  create: (data: MeetingRequest) =>
    api.post<MeetingResponse>('/api/meeting', data),
};
