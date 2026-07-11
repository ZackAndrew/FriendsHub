// ==================== User ====================
export interface User {
  id: number;
  username: string;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
  status: 'CREATED' | 'ACTIVATED';
  dateOfRegistration: string;
  telegramChatID: number | null;
}

export interface AuthRequest {
  username: string;
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  login: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

// ==================== Friendship ====================
export type FriendshipStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export interface FriendshipRequestResponse {
  id: number;
  requesterId: number;
  requesterUsername: string;
  requesterName: string;
  addresseeId: number;
  addresseeUsername: string;
  addresseeName: string;
  status: FriendshipStatus;
  createdAt: string;
}

export interface FriendshipDecisionResponse {
  id: number;
  requesterId: number;
  addresseeId: number;
  status: FriendshipStatus;
  decidedAt: string;
}

export interface Friend {
  friendshipId: number;
  userId: number;
  username: string;
  name: string;
  status: FriendshipStatus;
  since: string;
}

// ==================== Availability ====================
export interface AvailabilityRequest {
  startTime: string;
  endTime: string;
}

export interface AvailabilitySlot {
  id: number;
  userId: number;
  startTime: string;
  endTime: string;
}

export interface CommonSlot {
  startTime: string;
  endTime: string;
  friendUsername: string;
}

// ==================== Meeting ====================
export type MeetingStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELLED';

export interface MeetingRequest {
  participantUsername: string;
  startTime: string;
  endTime: string;
  title: string;
}

export interface MeetingResponse {
  id: number;
  organizerId: number;
  participantId: number;
  organizerUsername: string;
  participantUsername: string;
  startTime: string;
  endTime: string;
  status: MeetingStatus;
  title: string;
}

// ==================== Pagination ====================
export interface PageableResponse<T> {
  content: T[];
  totalElements: number;
  pageNumber: number;
  totalPages: number;
}
