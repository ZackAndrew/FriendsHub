/**
 * FriendsHub API Client
 * Supports smart slot calculation, weekly routine rhythms, polls, and voting.
 */

const API_BASE_URL = '';
let authToken = localStorage.getItem('fh_token') || null;
let currentUser = JSON.parse(localStorage.getItem('fh_user') || 'null');
let isDemoMode = localStorage.getItem('fh_demo_mode') === 'true';

// Mock database matching the new main branch schema
const mockDb = {
    user: {
        id: 1,
        username: "andrii_z",
        name: "Андрій Закордонський",
        email: "andrii@friendshub.com",
        role: "USER",
        status: "ACTIVATED",
        dateOfRegistration: "2026-01-10T12:00:00",
        telegramChatID: 849201948
    },
    friends: [
        { friendshipId: 101, userId: 2, username: "oleksiy_k", name: "Олексій Коваленко", status: "ACCEPTED", since: "2026-02-15T14:30:00" },
        { friendshipId: 102, userId: 3, username: "maria_s", name: "Марія Сидоренко", status: "ACCEPTED", since: "2026-03-01T09:15:00" },
        { friendshipId: 103, userId: 4, username: "dmytro_b", name: "Дмитро Бойко", status: "ACCEPTED", since: "2026-03-10T18:00:00" }
    ],
    friendRequests: [
        { id: 201, requesterId: 5, requesterUsername: "tetyana_v", requesterName: "Тетяна Василенко", addresseeId: 1, addresseeUsername: "andrii_z", addresseeName: "Андрій Закордонський", status: "PENDING", createdAt: "2026-11-18T10:00:00" }
    ],
    routines: [
        { name: "Робочий графік", daysOfWeek: [1, 2, 3, 4, 5], startTime: "09:00:00", endTime: "18:00:00" },
        { name: "Тренування", daysOfWeek: [2, 4], startTime: "19:00:00", endTime: "20:30:00" }
    ],
    meetings: [
        {
            id: 401,
            title: "Вибір часу для прогулянки та кави",
            organizerId: 2,
            organizerName: "Олексій Коваленко",
            startTime: null,
            endTime: null,
            participants: [
                { userId: 2, username: "oleksiy_k", status: "ACCEPTED", role: "ORGANIZER" },
                { userId: 1, username: "andrii_z", status: "PENDING", role: "PARTICIPANT" },
                { userId: 3, username: "maria_s", status: "PENDING", role: "PARTICIPANT" }
            ],
            options: [
                {
                    id: 501,
                    startTime: new Date(Date.now() + 86400000).toISOString().slice(0, 10) + "T18:30:00",
                    endTime: new Date(Date.now() + 86400000).toISOString().slice(0, 10) + "T20:00:00",
                    votesCount: 2
                },
                {
                    id: 502,
                    startTime: new Date(Date.now() + 172800000).toISOString().slice(0, 10) + "T19:00:00",
                    endTime: new Date(Date.now() + 172800000).toISOString().slice(0, 10) + "T20:30:00",
                    votesCount: 1
                },
                {
                    id: 503,
                    startTime: new Date(Date.now() + 259200000).toISOString().slice(0, 10) + "T14:00:00",
                    endTime: new Date(Date.now() + 259200000).toISOString().slice(0, 10) + "T15:30:00",
                    votesCount: 3
                }
            ]
        },
        {
            id: 402,
            title: "Стратегічне планування проєкту",
            organizerId: 1,
            organizerName: "Андрій Закордонський",
            startTime: new Date(Date.now() + 86400000).toISOString().slice(0, 10) + "T16:00:00",
            endTime: new Date(Date.now() + 86400000).toISOString().slice(0, 10) + "T17:30:00",
            participants: [
                { userId: 1, username: "andrii_z", status: "ACCEPTED", role: "ORGANIZER" },
                { userId: 4, username: "dmytro_b", status: "ACCEPTED", role: "PARTICIPANT" }
            ],
            options: []
        }
    ]
};

async function request(endpoint, options = {}) {
    if (isDemoMode) {
        return handleMockRequest(endpoint, options);
    }

    const token = authToken || localStorage.getItem('fh_token');
    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        ...options.headers
    };

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers
        });

        if (response.status === 401) {
            if (!endpoint.startsWith('/api/auth/') && token) {
                console.warn('Received 401 from', endpoint, 'clearing session');
                setAuth(null, null);
                window.dispatchEvent(new CustomEvent('auth-changed'));
            }
            throw new Error('Помилка авторизації (401). Будь ласка, увійдіть знову.');
        }

        if (response.status === 503 || response.status === 502) {
            console.warn("Backend unavailable, falling back to demo mode");
            setDemoMode(true);
            return handleMockRequest(endpoint, options);
        }

        if (!response.ok) {
            let errorMsg = `Помилка сервера: ${response.status}`;
            const contentType = response.headers.get('content-type') || '';
            if (contentType.includes('application/json')) {
                const errData = await response.json().catch(() => ({}));
                errorMsg = errData.error || errData.message || errorMsg;
            } else {
                const text = await response.text().catch(() => '');
                if (text) errorMsg = text;
            }
            throw new Error(errorMsg);
        }

        if (response.status === 204) return null;

        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            return await response.json();
        }
        const text = await response.text();
        return text ? { message: text } : null;
    } catch (err) {
        if (err.message.includes('Failed to fetch') || err.message.includes('NetworkError')) {
            console.warn("Network error to backend, switching to demo mode");
            setDemoMode(true);
            return handleMockRequest(endpoint, options);
        }
        throw err;
    }
}

function handleMockRequest(endpoint, options) {
    const method = options.method || 'GET';
    const body = options.body ? JSON.parse(options.body) : null;

    return new Promise((resolve) => {
        setTimeout(() => {
            // AUTH
            if (endpoint === '/api/auth/login') {
                const user = mockDb.user;
                const token = 'mock-jwt-token-' + Date.now();
                setAuth(token, user);
                resolve({ token, user });
                return;
            }
            if (endpoint === '/api/auth/register') {
                const newUser = {
                    id: Math.floor(Math.random() * 1000) + 10,
                    username: body.username,
                    name: body.name || body.username,
                    email: body.email,
                    role: "USER",
                    status: "ACTIVATED",
                    dateOfRegistration: new Date().toISOString(),
                    telegramChatID: null
                };
                const token = 'mock-jwt-token-' + Date.now();
                setAuth(token, newUser);
                resolve({ token, user: newUser });
                return;
            }
            if (endpoint === '/api/auth/password-reset/request') {
                resolve({ message: "Лист для відновлення паролю надіслано" });
                return;
            }
            if (endpoint === '/api/auth/password-reset/confirm') {
                resolve({ message: "Пароль успішно змінено" });
                return;
            }

            // ROUTINES
            if (endpoint === '/api/routines' && method === 'PUT') {
                mockDb.routines = body;
                resolve({ success: true });
                return;
            }
            if (endpoint === '/api/routines' && method === 'GET') {
                resolve([...mockDb.routines]);
                return;
            }

            // AVAILABILITY CALCULATE (SLIDING WINDOW)
            if (endpoint === '/api/availability/calculate' && method === 'POST') {
                const duration = body.durationMinutes || 60;
                const baseDate = new Date(body.windowStart || Date.now());
                const slots = [];

                for (let i = 1; i <= 3; i++) {
                    const slotStart = new Date(baseDate.getTime() + i * 86400000);
                    const [h, m] = (body.dailyStartTime || "18:00").split(':').map(Number);
                    slotStart.setHours(h + (i - 1), m, 0, 0);
                    const slotEnd = new Date(slotStart.getTime() + duration * 60000);

                    slots.push({
                        start: slotStart.toISOString().slice(0, 19),
                        end: slotEnd.toISOString().slice(0, 19),
                        availableUserIds: [1, 2, 3],
                        score: 100 - (i - 1) * 10
                    });
                }
                resolve(slots);
                return;
            }

            // FRIENDS
            if (endpoint === '/api/friends' && method === 'GET') {
                resolve([...mockDb.friends]);
                return;
            }
            if (endpoint === '/api/friends/pending' && method === 'GET') {
                resolve([...mockDb.friendRequests]);
                return;
            }
            if (endpoint.startsWith('/api/friends/request/') && method === 'POST') {
                const userId = parseInt(endpoint.split('/').pop());
                const newReq = {
                    id: Date.now(),
                    requesterId: currentUser?.id || 1,
                    requesterUsername: currentUser?.username || "me",
                    requesterName: currentUser?.name || "Користувач",
                    addresseeId: userId,
                    addresseeUsername: "friend_" + userId,
                    addresseeName: "Друг " + userId,
                    status: "PENDING",
                    createdAt: new Date().toISOString()
                };
                resolve(newReq);
                return;
            }
            if (endpoint.includes('/accept') && endpoint.includes('/friends/requests/')) {
                const reqId = parseInt(endpoint.split('/')[4]);
                const reqIdx = mockDb.friendRequests.findIndex(r => r.id === reqId);
                if (reqIdx !== -1) {
                    const req = mockDb.friendRequests.splice(reqIdx, 1)[0];
                    mockDb.friends.push({
                        friendshipId: Date.now(),
                        userId: req.requesterId,
                        username: req.requesterUsername,
                        name: req.requesterName,
                        status: "ACCEPTED",
                        since: new Date().toISOString()
                    });
                }
                resolve({ id: reqId, status: "ACCEPTED", acceptedAt: new Date().toISOString() });
                return;
            }
            if (endpoint.includes('/decline') && endpoint.includes('/friends/requests/')) {
                const reqId = parseInt(endpoint.split('/')[4]);
                mockDb.friendRequests = mockDb.friendRequests.filter(r => r.id !== reqId);
                resolve({ id: reqId, status: "DECLINED" });
                return;
            }
            if (endpoint.includes('/removeFriend')) {
                const reqId = parseInt(endpoint.split('/')[3]);
                mockDb.friends = mockDb.friends.filter(f => f.friendshipId !== reqId);
                resolve({ status: "REMOVED" });
                return;
            }

            // MEETINGS
            if (endpoint === '/api/meeting/my' || endpoint === '/api/meeting/my-meetings') {
                resolve([...mockDb.meetings]);
                return;
            }
            if (endpoint === '/api/meeting/pending') {
                resolve(mockDb.meetings.filter(m => m.organizerId !== (currentUser?.id || 1)));
                return;
            }
            if (endpoint === '/api/meeting/outgoing') {
                resolve(mockDb.meetings.filter(m => m.organizerId === (currentUser?.id || 1)));
                return;
            }
            // Propose Smart Meeting with Options
            if (endpoint === '/api/meeting/propose' && method === 'POST') {
                const newMeeting = {
                    id: Date.now(),
                    title: body.title,
                    organizerId: currentUser?.id || 1,
                    organizerName: currentUser?.name || "Андрій Закордонський",
                    startTime: null,
                    endTime: null,
                    participants: [
                        { userId: currentUser?.id || 1, username: currentUser?.username || "andrii_z", status: "ACCEPTED", role: "ORGANIZER" },
                        ...body.participantNames.map((u, idx) => ({
                            userId: idx + 2,
                            username: u,
                            status: "PENDING",
                            role: "PARTICIPANT"
                        }))
                    ],
                    options: (body.proposedOptions || []).map((opt, i) => ({
                        id: 5000 + i,
                        startTime: opt.start,
                        endTime: opt.end,
                        votesCount: 0
                    }))
                };
                mockDb.meetings.unshift(newMeeting);
                resolve(newMeeting);
                return;
            }
            // Vote for options
            if (endpoint.includes('/vote') && method === 'POST') {
                const mId = parseInt(endpoint.split('/')[3]);
                const meeting = mockDb.meetings.find(m => m.id === mId);
                if (meeting && meeting.options) {
                    body.selectedOptionIds.forEach(optId => {
                        const opt = meeting.options.find(o => o.id === optId);
                        if (opt) opt.votesCount += 1;
                    });
                }
                resolve(meeting || { success: true });
                return;
            }
            // Finalize meeting
            if (endpoint.includes('/finalize') && method === 'POST') {
                const mId = parseInt(endpoint.split('/')[3]);
                const urlParams = new URLSearchParams(endpoint.split('?')[1]);
                const winningId = parseInt(urlParams.get('winningOptionId'));
                const meeting = mockDb.meetings.find(m => m.id === mId);
                if (meeting) {
                    const winningOpt = meeting.options.find(o => o.id === winningId) || meeting.options[0];
                    if (winningOpt) {
                        meeting.startTime = winningOpt.startTime;
                        meeting.endTime = winningOpt.endTime;
                    }
                }
                resolve(meeting || { success: true });
                return;
            }
            // Cancel meeting
            if (endpoint.includes('/cancel') && (method === 'DELETE' || method === 'PATCH')) {
                const mId = parseInt(endpoint.split('/')[3]);
                mockDb.meetings = mockDb.meetings.filter(m => m.id !== mId);
                resolve({ success: true });
                return;
            }
            // Accept/Decline meeting
            if (endpoint.includes('/accept')) {
                resolve({ success: true });
                return;
            }
            if (endpoint.includes('/decline')) {
                resolve({ success: true });
                return;
            }

            resolve({ success: true });
        }, 120);
    });
}

export function setAuth(token, user) {
    authToken = token;
    currentUser = user;
    if (token) {
        localStorage.setItem('fh_token', token);
        localStorage.setItem('fh_user', JSON.stringify(user));
    } else {
        localStorage.removeItem('fh_token');
        localStorage.removeItem('fh_user');
    }
}

export function setDemoMode(val) {
    isDemoMode = val;
    localStorage.setItem('fh_demo_mode', val ? 'true' : 'false');
    if (val && !currentUser) {
        setAuth('demo-jwt-token', mockDb.user);
    }
    window.dispatchEvent(new CustomEvent('backend-mode-changed', { detail: { isDemo: val } }));
}

export function loginDemoUser() {
    setDemoMode(true);
    setAuth('demo-jwt-token', mockDb.user);
    window.dispatchEvent(new CustomEvent('auth-changed'));
}

export function getDemoMode() {
    return isDemoMode;
}

export function getCurrentUser() {
    return currentUser;
}

export function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch {
        return null;
    }
}

export function isAuthenticated() {
    return !!authToken && !!currentUser;
}

// High-level API methods
export const api = {
    auth: {
        login: async (credentials) => {
            // When user explicitly logs in, disable demo mode and use live backend
            setDemoMode(false);
            const res = await request('/api/auth/login', { method: 'POST', body: JSON.stringify(credentials) });
            if (res && res.token) {
                setAuth(res.token, res.user);
            }
            return res;
        },
        register: async (userData) => {
            setDemoMode(false);
            const res = await request('/api/auth/register', { method: 'POST', body: JSON.stringify(userData) });
            if (res && res.token) {
                setAuth(res.token, res.user);
            }
            return res;
        },
        logout: () => {
            setAuth(null, null);
            window.dispatchEvent(new CustomEvent('auth-changed'));
        },
        requestPasswordReset: (data) => request('/api/auth/password-reset/request', { method: 'POST', body: JSON.stringify(data) }),
        confirmPasswordReset: (data) => request('/api/auth/password-reset/confirm', { method: 'POST', body: JSON.stringify(data) })
    },
    routines: {
        save: (routinesList) => request('/api/routines', { method: 'PUT', body: JSON.stringify(routinesList) }),
        get: () => request('/api/routines')
    },
    availability: {
        calculate: (calcRequest) => request('/api/availability/calculate', { method: 'POST', body: JSON.stringify(calcRequest) })
    },
    meetings: {
        propose: (proposalDto) => request('/api/meeting/propose', { method: 'POST', body: JSON.stringify(proposalDto) }),
        vote: (meetingId, optionIds) => request(`/api/meeting/${meetingId}/vote`, { method: 'POST', body: JSON.stringify({ selectedOptionIds: optionIds }) }),
        finalize: (meetingId, winningOptionId) => request(`/api/meeting/${meetingId}/finalize?winningOptionId=${winningOptionId}`, { method: 'POST' }),
        createDirect: (meeting) => request('/api/meeting', { method: 'POST', body: JSON.stringify(meeting) }),
        getAll: () => request('/api/meeting/my'),
        getPending: () => request('/api/meeting/pending'),
        getOutgoing: () => request('/api/meeting/outgoing'),
        getById: (id) => request(`/api/meeting/${id}`),
        accept: (id) => request(`/api/meeting/${id}/accept`, { method: 'PATCH' }),
        decline: (id) => request(`/api/meeting/${id}/decline`, { method: 'PATCH' }),
        cancel: (id) => request(`/api/meeting/${id}/cancel`, { method: 'DELETE' })
    },
    friends: {
        getAll: () => request('/api/friends'),
        getPendingRequests: () => request('/api/friends/pending'),
        sendRequest: (userId) => request(`/api/friends/request/${userId}`, { method: 'POST' }),
        acceptRequest: (requestId) => request(`/api/friends/requests/${requestId}/accept`, { method: 'PATCH' }),
        declineRequest: (requestId) => request(`/api/friends/requests/${requestId}/decline`, { method: 'PATCH' }),
        removeFriend: (requestId) => request(`/api/friends/request/${requestId}/removeFriend`, { method: 'PATCH' })
    },
    users: {
        getById: (id) => request(`/api/users/${id}`),
        getByUsername: (username) => request(`/api/users/by-username/${username}`),
        getAll: () => request('/api/users')
    }
};
