import { api, isAuthenticated, getCurrentUser, loginDemoUser } from '../api.js?v=4.0';
import { openAuthModal } from './auth-modal.js?v=4.0';

export async function renderDashboard(container) {
    const isAuth = isAuthenticated();
    const user = getCurrentUser();

    if (!isAuth) {
        container.innerHTML = `
            <div class="py-12 sm:py-16 max-w-3xl mx-auto">
                <div class="text-center mb-10">
                    <h1 class="text-3xl sm:text-5xl font-bold tracking-tight text-neutral-900 mb-4 leading-tight">
                        Плануйте зустрічі з друзями без зайвих переписок
                    </h1>
                    <p class="text-neutral-600 text-base max-w-xl mx-auto leading-relaxed">
                        FriendsHub зіставляє ваш вільний час із розкладом друзів і знаходить точні вікна для спільних прогулянок чи зустрічей.
                    </p>
                    <div class="flex flex-col sm:flex-row items-center justify-center gap-3 mt-8">
                        <button id="hero-get-started" class="w-full sm:w-auto px-6 py-2.5 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-sm transition">
                            Створити акаунт
                        </button>
                        <button id="hero-demo" class="w-full sm:w-auto px-6 py-2.5 rounded-lg bg-white hover:bg-neutral-50 text-neutral-800 border border-neutral-300 font-medium text-sm transition">
                            Спробувати демо-режим
                        </button>
                    </div>
                </div>

                <!-- Feature Pillars -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-left">
                    <div class="bg-white p-5 rounded-xl border border-neutral-200">
                        <div class="w-8 h-8 rounded-lg bg-neutral-100 text-neutral-800 flex items-center justify-center mb-3">
                            <i data-lucide="calendar" class="w-4 h-4"></i>
                        </div>
                        <h4 class="font-semibold text-neutral-900 text-sm mb-1">Власний графік</h4>
                        <p class="text-xs text-neutral-500 leading-relaxed">Вказуйте регулярні години доступності або підключайте свій Google календар.</p>
                    </div>
                    <div class="bg-white p-5 rounded-xl border border-neutral-200">
                        <div class="w-8 h-8 rounded-lg bg-neutral-100 text-neutral-800 flex items-center justify-center mb-3">
                            <i data-lucide="clock" class="w-4 h-4"></i>
                        </div>
                        <h4 class="font-semibold text-neutral-900 text-sm mb-1">Підбір вільного часу</h4>
                        <p class="text-xs text-neutral-500 leading-relaxed">Система автоматично обчислює перетин вільних годин між обраними друзями.</p>
                    </div>
                    <div class="bg-white p-5 rounded-xl border border-neutral-200">
                        <div class="w-8 h-8 rounded-lg bg-neutral-100 text-neutral-800 flex items-center justify-center mb-3">
                            <i data-lucide="check-square" class="w-4 h-4"></i>
                        </div>
                        <h4 class="font-semibold text-neutral-900 text-sm mb-1">Зустрічі та голосування</h4>
                        <p class="text-xs text-neutral-500 leading-relaxed">Створюйте опитування для вибору найкращого часу або фіксуйте зустрічі одразу.</p>
                    </div>
                </div>
            </div>
        `;

        document.getElementById('hero-get-started').onclick = () => openAuthModal('register');
        document.getElementById('hero-demo').onclick = () => loginDemoUser();
        if (window.lucide) window.lucide.createIcons();
        return;
    }

    // Authenticated View
    container.innerHTML = `
        <div class="space-y-6">
            <!-- Header section -->
            <div class="bg-white rounded-xl p-6 border border-neutral-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h2 class="text-xl font-bold text-neutral-900 tracking-tight">
                        Привіт, ${user.name || user.username}
                    </h2>
                    <p class="text-xs text-neutral-500 mt-1">
                        Огляд вашої активності, найближчих зустрічей та перетинів графіків.
                    </p>
                </div>
                <div class="flex items-center gap-2.5">
                    <button id="dash-btn-find-slot" class="px-3.5 py-2 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center gap-2">
                        <i data-lucide="clock" class="w-3.5 h-3.5"></i>
                        <span>Підібрати час</span>
                    </button>
                    <button id="dash-btn-add-avail" class="px-3.5 py-2 rounded-lg bg-white hover:bg-neutral-50 text-neutral-700 border border-neutral-300 font-medium text-xs transition flex items-center gap-2">
                        <i data-lucide="calendar-range" class="w-3.5 h-3.5"></i>
                        <span>Мій графік</span>
                    </button>
                </div>
            </div>

            <!-- Stats Grid -->
            <div id="dash-stats-grid" class="grid grid-cols-2 lg:grid-cols-4 gap-3.5">
                <div class="bg-white p-4 rounded-xl border border-neutral-200">
                    <div class="flex items-center justify-between mb-1">
                        <span class="text-xs font-medium text-neutral-500">Мої друзі</span>
                        <i data-lucide="users" class="w-3.5 h-3.5 text-neutral-400"></i>
                    </div>
                    <div id="stat-friends-count" class="text-2xl font-bold text-neutral-900">...</div>
                </div>
                <div class="bg-white p-4 rounded-xl border border-neutral-200">
                    <div class="flex items-center justify-between mb-1">
                        <span class="text-xs font-medium text-neutral-500">Активні зустрічі</span>
                        <i data-lucide="calendar" class="w-3.5 h-3.5 text-neutral-400"></i>
                    </div>
                    <div id="stat-meetings-count" class="text-2xl font-bold text-neutral-900">...</div>
                </div>
                <div class="bg-white p-4 rounded-xl border border-neutral-200">
                    <div class="flex items-center justify-between mb-1">
                        <span class="text-xs font-medium text-neutral-500">Задані графіки</span>
                        <i data-lucide="calendar-range" class="w-3.5 h-3.5 text-neutral-400"></i>
                    </div>
                    <div id="stat-avail-count" class="text-2xl font-bold text-neutral-900">...</div>
                </div>
                <div class="bg-white p-4 rounded-xl border border-neutral-200">
                    <div class="flex items-center justify-between mb-1">
                        <span class="text-xs font-medium text-neutral-500">Очікують відповіді</span>
                        <i data-lucide="clock" class="w-3.5 h-3.5 text-neutral-400"></i>
                    </div>
                    <div id="stat-pending-count" class="text-2xl font-bold text-neutral-900">...</div>
                </div>
            </div>

            <!-- Two Columns Section -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
                <!-- Top Common Slots Widget -->
                <div class="bg-white rounded-xl p-5 border border-neutral-200 flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-4">
                            <h3 class="font-semibold text-neutral-900 text-sm">Рекомендовані слоти для зустрічі</h3>
                            <button id="widget-view-all-slots" class="text-xs font-medium text-neutral-600 hover:text-neutral-900">Усі варіанти &rarr;</button>
                        </div>
                        <div id="dash-common-slots-list" class="space-y-2.5">
                            <div class="text-xs text-neutral-400 py-6 text-center">Завантаження перетинів графіків...</div>
                        </div>
                    </div>
                </div>

                <!-- Next Upcoming Meeting -->
                <div class="bg-white rounded-xl p-5 border border-neutral-200 flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-4">
                            <h3 class="font-semibold text-neutral-900 text-sm">Найближча зустріч</h3>
                            <button id="widget-view-meetings" class="text-xs font-medium text-neutral-600 hover:text-neutral-900">Всі зустрічі &rarr;</button>
                        </div>
                        <div id="dash-next-meeting">
                            <div class="text-xs text-neutral-400 py-6 text-center">Пошук запланованих зустрічей...</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    if (window.lucide) window.lucide.createIcons();

    // Attach click handlers
    document.getElementById('dash-btn-find-slot').onclick = () => window.switchTab('common-slots');
    document.getElementById('dash-btn-add-avail').onclick = () => window.switchTab('availability');
    document.getElementById('widget-view-all-slots').onclick = () => window.switchTab('common-slots');
    document.getElementById('widget-view-meetings').onclick = () => window.switchTab('meetings');

    // Load dynamic data for dashboard
    loadDashboardData();
}

async function loadDashboardData() {
    try {
        const now = new Date(Date.now() + 5 * 60000);
        const windowStart = now.toISOString().slice(0, 19);
        const nextWeek = new Date(Date.now() + 7 * 86400000);
        const windowEnd = nextWeek.toISOString().slice(0, 10) + 'T23:59:59';

        const [friends, meetings, routines] = await Promise.all([
            api.friends.getAll().catch(() => []),
            api.meetings.getAll().catch(() => []),
            api.routines.get().catch(() => [])
        ]);

        let topSlots = [];
        const participantUsernames = (friends || []).map(f => f.username).filter(Boolean).slice(0, 3);
        if (participantUsernames.length > 0) {
            topSlots = await api.availability.calculate({
                participantNames: participantUsernames,
                windowStart: windowStart,
                windowEnd: windowEnd,
                preferredDaysOfWeek: [1, 2, 3, 4, 5, 6, 7],
                dailyStartTime: "17:00:00",
                dailyEndTime: "22:00:00",
                durationMinutes: 60
            }).catch(() => []);
        }

        const activePolls = meetings.filter(m => m.options && m.options.length > 0 && !m.startTime);
        const finalizedMeetings = meetings.filter(m => m.startTime);

        // Update stats
        const statFriends = document.getElementById('stat-friends-count');
        const statMeetings = document.getElementById('stat-meetings-count');
        const statAvail = document.getElementById('stat-avail-count');
        const statPending = document.getElementById('stat-pending-count');

        if (statFriends) statFriends.innerText = friends.length;
        if (statMeetings) statMeetings.innerText = finalizedMeetings.length;
        if (statAvail) statAvail.innerText = routines.length;
        if (statPending) statPending.innerText = activePolls.length;

        // Render Top Common Slots
        const slotsContainer = document.getElementById('dash-common-slots-list');
        if (slotsContainer) {
            if (!topSlots || topSlots.length === 0) {
                slotsContainer.innerHTML = `
                    <div class="text-center py-6 text-neutral-400 text-xs">
                        Немає розрахованих слотів.<br>
                        Перейдіть у вкладку "Підбір часу", щоб знайти варіанти для зустрічі.
                    </div>
                `;
            } else {
                slotsContainer.innerHTML = topSlots.slice(0, 3).map((slot, idx) => {
                    const s = new Date(slot.start);
                    const e = new Date(slot.end);
                    const dateStr = s.toLocaleDateString('uk-UA', { weekday: 'short', day: 'numeric', month: 'short' });
                    const timeStr = `${s.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - ${e.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}`;
                    
                    return `
                        <div class="flex items-center justify-between p-3 rounded-lg border border-neutral-200 bg-neutral-50/50 hover:bg-neutral-50 transition">
                            <div>
                                <div class="text-xs font-semibold text-neutral-900 capitalize">${dateStr}, ${timeStr}</div>
                                <div class="text-[11px] text-neutral-500 mt-0.5">Збіг: ${slot.score}%</div>
                            </div>
                            <button onclick="window.switchTab('common-slots')" class="px-2.5 py-1.5 rounded-md bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition">
                                Обрати
                            </button>
                        </div>
                    `;
                }).join('');
            }
        }

        // Render Next Upcoming Meeting or Poll
        const nextMeetingContainer = document.getElementById('dash-next-meeting');
        if (nextMeetingContainer) {
            if (finalizedMeetings.length > 0) {
                const nextM = finalizedMeetings[0];
                const s = new Date(nextM.startTime);
                const e = new Date(nextM.endTime);
                nextMeetingContainer.innerHTML = `
                    <div class="p-4 rounded-lg border border-neutral-200 bg-neutral-50/50">
                        <div class="flex items-center justify-between mb-1.5">
                            <span class="text-[11px] px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 border border-emerald-200 font-medium">Підтверджено</span>
                            <span class="text-xs text-neutral-500">${s.toLocaleDateString('uk-UA', { weekday: 'short', day: 'numeric', month: 'short' })}</span>
                        </div>
                        <h4 class="text-sm font-semibold text-neutral-900 mb-1">${nextM.title}</h4>
                        <div class="flex items-center gap-4 text-xs text-neutral-500 mt-2">
                            <span class="flex items-center gap-1">
                                <i data-lucide="clock" class="w-3.5 h-3.5"></i>
                                ${s.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - ${e.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                            </span>
                            <span class="flex items-center gap-1">
                                <i data-lucide="users" class="w-3.5 h-3.5"></i>
                                ${(nextM.participants || []).length} учасників
                            </span>
                        </div>
                    </div>
                `;
            } else if (activePolls.length > 0) {
                const poll = activePolls[0];
                nextMeetingContainer.innerHTML = `
                    <div class="p-4 rounded-lg border border-neutral-200 bg-neutral-50/50">
                        <div class="flex items-center justify-between mb-1.5">
                            <span class="text-[11px] px-2 py-0.5 rounded-md bg-amber-50 text-amber-700 border border-amber-200 font-medium">Голосування</span>
                        </div>
                        <h4 class="text-sm font-semibold text-neutral-900 mb-1">${poll.title}</h4>
                        <p class="text-xs text-neutral-500 mb-3">${poll.options.length} запропонованих варіантів часу</p>
                        <button onclick="window.switchTab('meetings')" class="px-3 py-1.5 rounded-md bg-neutral-900 hover:bg-neutral-800 text-white text-xs font-medium transition">
                            Переглянути
                        </button>
                    </div>
                `;
            } else {
                nextMeetingContainer.innerHTML = `
                    <div class="text-center py-6 text-neutral-400 text-xs">
                        Немає запланованих зустрічей.<br>
                        Підберіть вільний час і запропонуйте зустріч друзям.
                    </div>
                `;
            }
        }

        if (window.lucide) window.lucide.createIcons();
    } catch (e) {
        console.error("Dashboard data load error:", e);
    }
}
