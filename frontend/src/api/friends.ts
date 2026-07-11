import api from './axios';
import type { Friend, FriendshipRequestResponse, FriendshipDecisionResponse } from '../types';

export const friendsApi = {
  sendRequest: (userId: number) =>
    api.post<FriendshipRequestResponse>(`/api/friends/request/${userId}`),

  acceptRequest: (requestId: number) =>
    api.patch<FriendshipDecisionResponse>(`/api/friends/requests/${requestId}/accept`),

  declineRequest: (requestId: number) =>
    api.patch<FriendshipDecisionResponse>(`/api/friends/requests/${requestId}/decline`),

  getAllFriends: () =>
    api.get<Friend[]>('/api/friends'),

  getPendingRequests: () =>
    api.get<FriendshipRequestResponse[]>('/api/friends/pending'),

  removeFriend: (requestId: number) =>
    api.patch<FriendshipRequestResponse>(`/api/friends/request/${requestId}/removeFriend`),
};
