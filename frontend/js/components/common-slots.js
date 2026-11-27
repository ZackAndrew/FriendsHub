import { api, isAuthenticated } from '../api.js?v=4.0';
import { toast } from './toast.js?v=4.0';

export async function renderCommonSlots(container) {
    if (!isAuthenticated()) {
        container.innerHTML = `
            <div class="py-12 text-center text-neutral-500 text-sm">
                <p>Будь ласка, увійдіть, щоб скористатися підбором часу.</p>
            </div>
        `;
        return;
    }

    const formatDate = (date) => {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    };

    const addDays = (baseDate, days) => {
        return new Date(baseDate.getFullYear(), baseDate.getMonth(), baseDate.getDate() + days);
    };

    const getIsoDay = (date) => {
        const day = date.getDay();
        return day === 0 ? 7 : day;
    };

    const formatHumanDateRange = (sStr, eStr) => {
        if (!sStr || !eStr) return '';
        const [sy, sm, sd] = sStr.split('-');
        const [ey, em, ed] = eStr.split('-');
        return `${sd}.${sm}.${sy} — ${ed}.${em}.${ey}`;
    };

    const generate24HourOptions = (selectedTime = "09:00", isEnd = false) => {
        const options = [];
        for (let h = 0; h < 24; h++) {
            for (let m = 0; m < 60; m += 30) {
                const timeStr = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
                const isSel = timeStr === selectedTime;
                options.push(`<option value="${timeStr}" ${isSel ? 'selected' : ''}>${timeStr}</option>`);
            }
        }
        if (isEnd) {
            options.push(`<option value="23:59" ${selectedTime === '23:59' || selectedTime === '24:00' ? 'selected' : ''}>23:59 (кінець доби)</option>`);
        }
        return options.join('');
    };

    const today = new Date();
    const todayStr = formatDate(today);
    const nextWeekStr = formatDate(addDays(today, 7));

    container.innerHTML = `
        <div class="space-y-6">
            <!-- Header Banner -->
            <div class="bg-white p-6 rounded-xl border border-neutral-200 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h2 class="text-xl font-bold text-neutral-900 tracking-tight">Підбір часу для зустрічі</h2>
                    <p class="text-xs text-neutral-500 mt-1">
                        Оберіть друзів, період пошуку та бажаний час. Система знайде найкращі спільні вікна та дозволить створити опитування.
                    </p>
                </div>
            </div>

            <!-- Parameters Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <!-- Filter Form Card -->
                <div class="bg-white p-6 rounded-xl border border-neutral-200 lg:col-span-1 h-fit space-y-4">
                    <h3 class="text-sm font-semibold text-neutral-900 flex items-center gap-2 mb-2">
                        <i data-lucide="sliders" class="w-4 h-4 text-neutral-600"></i>
                        <span>Параметри пошуку</span>
                    </h3>

                    <!-- Participants Selector -->
                    <div>
                        <label class="block text-xs font-medium text-neutral-700 mb-2">Оберіть друзів</label>
                        <div id="matcher-friends-container" class="space-y-1.5 max-h-44 overflow-y-auto pr-1">
                            <div class="text-xs text-neutral-400 py-2">Завантаження друзів...</div>
                        </div>
                    </div>

                    <!-- Date Range with Quick Presets -->
                    <div>
                        <div class="flex items-center justify-between mb-1.5">
                            <label class="block text-xs font-medium text-neutral-700">Період пошуку</label>
                            <span id="active-preset-dates-summary" class="text-[11px] text-neutral-500 font-medium"></span>
                        </div>
                        
                        <!-- Quick Presets -->
                        <div class="flex flex-wrap gap-1.5 mb-2.5" id="date-presets-container">
                            <button type="button" data-preset="this-week" class="date-preset-btn px-2.5 py-1 rounded-md text-[11px] font-medium bg-neutral-100 hover:bg-neutral-200 text-neutral-700 border border-neutral-200 transition">
                                Цього тижня
                            </button>
                            <button type="button" data-preset="next-week" class="date-preset-btn px-2.5 py-1 rounded-md text-[11px] font-medium bg-neutral-100 hover:bg-neutral-200 text-neutral-700 border border-neutral-200 transition">
                                Наступного тижня
                            </button>
                            <button type="button" data-preset="weekend" class="date-preset-btn px-2.5 py-1 rounded-md text-[11px] font-medium bg-neutral-100 hover:bg-neutral-200 text-neutral-700 border border-neutral-200 transition">
                                На вихідних
                            </button>
                            <button type="button" data-preset="7-days" class="date-preset-btn px-2.5 py-1 rounded-md text-[11px] font-semibold bg-neutral-900 text-white border border-neutral-900 transition">
                                7 днів
                            </button>
                            <button type="button" data-preset="custom" class="date-preset-btn px-2.5 py-1 rounded-md text-[11px] font-medium bg-neutral-100 hover:bg-neutral-200 text-neutral-700 border border-neutral-200 transition">
                                Власний період
                            </button>
                        </div>

                        <!-- Custom Date Pickers (hidden when standard preset is active) -->
                        <div id="custom-date-container" class="hidden pt-1">
                            <div class="grid grid-cols-2 gap-2">
                                <div>
                                    <label class="block text-[11px] font-medium text-neutral-500 mb-1">Початкова дата</label>
                                    <input type="date" id="match-window-start" value="${todayStr}" class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs focus:outline-none focus:border-neutral-900 transition">
                                </div>
                                <div>
                                    <label class="block text-[11px] font-medium text-neutral-500 mb-1">Кінцева дата</label>
                                    <input type="date" id="match-window-end" value="${nextWeekStr}" class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs focus:outline-none focus:border-neutral-900 transition">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Days of Week Selector -->
                    <div>
                        <label class="block text-xs font-medium text-neutral-700 mb-2">Бажані дні тижня</label>
                        <div class="grid grid-cols-7 gap-1" id="match-days-selector">
                            <button type="button" data-day="1" class="match-day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Пн</button>
                            <button type="button" data-day="2" class="match-day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Вт</button>
                            <button type="button" data-day="3" class="match-day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Ср</button>
                            <button type="button" data-day="4" class="match-day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Чт</button>
                            <button type="button" data-day="5" class="match-day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Пт</button>
                            <button type="button" data-day="6" class="match-day-btn py-1.5 text-xs font-semibold rounded-md bg-neutral-900 text-white border border-neutral-900 transition">Сб</button>
                            <button type="button" data-day="7" class="match-day-btn py-1.5 text-xs font-semibold rounded-md bg-neutral-900 text-white border border-neutral-900 transition">Нд</button>
                        </div>
                    </div>

                    <!-- European 24h Time Window & Broad Duration -->
                    <div class="space-y-3">
                        <div>
                            <label class="block text-[11px] font-medium text-neutral-600 mb-1">Години доби (24h формат)</label>
                            <div class="grid grid-cols-2 gap-2">
                                <div>
                                    <span class="block text-[10px] text-neutral-400 mb-0.5">Від</span>
                                    <select id="match-daily-start" class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs focus:outline-none focus:border-neutral-900">
                                        ${generate24HourOptions("09:00", false)}
                                    </select>
                                </div>
                                <div>
                                    <span class="block text-[10px] text-neutral-400 mb-0.5">До</span>
                                    <select id="match-daily-end" class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs focus:outline-none focus:border-neutral-900">
                                        ${generate24HourOptions("22:00", true)}
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div>
                            <label class="block text-[11px] font-medium text-neutral-600 mb-1">Тривалість зустрічі</label>
                            <select id="match-duration" class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs focus:outline-none focus:border-neutral-900">
                                <option value="15">15 хв (коротка кава / онлайн-дзвінок)</option>
                                <option value="30">30 хв</option>
                                <option value="45">45 хв</option>
                                <option value="60" selected>1 година</option>
                                <option value="90">1.5 години</option>
                                <option value="120">2 години</option>
                                <option value="150">2.5 години</option>
                                <option value="180">3 години</option>
                                <option value="240">4 години (півдня)</option>
                                <option value="300">5 годин</option>
                                <option value="360">6 годин</option>
                                <option value="480">8 годин (весь день)</option>
                            </select>
                        </div>
                    </div>

                    <button id="calc-slots-btn" class="w-full py-2.5 px-4 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center justify-center gap-2">
                        <i data-lucide="search" class="w-4 h-4"></i>
                        <span>Знайти оптимальні слоти</span>
                    </button>
                </div>

                <!-- Results & Poll Creation Card -->
                <div class="bg-white p-6 rounded-xl border border-neutral-200 lg:col-span-2 flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-4">
                            <h3 class="text-sm font-semibold text-neutral-900 flex items-center gap-2">
                                <i data-lucide="list" class="w-4 h-4 text-neutral-600"></i>
                                <span>Знайдені варіанти</span>
                            </h3>
                            <span id="match-results-status" class="text-xs text-neutral-500">Оберіть параметри та натисніть пошук</span>
                        </div>

                        <div id="calculated-slots-container" class="space-y-2.5">
                            <div class="text-center py-12 text-neutral-400 text-xs">
                                Оберіть друзів та натисніть кнопку "Знайти оптимальні слоти" для розрахунку.
                            </div>
                        </div>
                    </div>

                    <!-- Create Poll Section (hidden initially) -->
                    <div id="create-poll-section" class="hidden mt-6 pt-5 border-t border-neutral-200">
                        <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5">
                            <input type="text" id="poll-title-input" placeholder="Тема зустрічі (наприклад: Обговорення проекту)" class="flex-1 px-3.5 py-2 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs placeholder-neutral-400 focus:outline-none focus:border-neutral-900">
                            <button id="submit-poll-btn" class="py-2 px-4 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center justify-center gap-2 whitespace-nowrap">
                                <i data-lucide="vote" class="w-4 h-4"></i>
                                <span>Створити опитування</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    if (window.lucide) window.lucide.createIcons();

    // Preferred days selection logic
    const preferredDays = new Set([6, 7]);
    const updatePreferredDaysUI = () => {
        document.querySelectorAll('.match-day-btn').forEach(b => {
            const d = parseInt(b.getAttribute('data-day'));
            const isSelected = preferredDays.has(d);
            b.className = `match-day-btn py-1.5 text-xs rounded-md transition border ${
                isSelected ? 'bg-neutral-900 text-white border-neutral-900 font-semibold' : 'bg-white text-neutral-700 hover:bg-neutral-50 border-neutral-300 font-medium'
            }`;
        });
    };

    document.querySelectorAll('.match-day-btn').forEach(btn => {
        btn.onclick = () => {
            const day = parseInt(btn.getAttribute('data-day'));
            if (preferredDays.has(day)) {
                if (preferredDays.size > 1) preferredDays.delete(day);
            } else {
                preferredDays.add(day);
            }
            updatePreferredDaysUI();
        };
    });

    // Date Preset Logic: Hide custom date inputs when standard preset is chosen
    const customContainer = document.getElementById('custom-date-container');
    const summarySpan = document.getElementById('active-preset-dates-summary');

    const updatePresetButtons = (activePreset = null) => {
        document.querySelectorAll('.date-preset-btn').forEach(btn => {
            const isCurrent = btn.getAttribute('data-preset') === activePreset;
            btn.className = `date-preset-btn px-2.5 py-1 rounded-md text-[11px] transition border ${
                isCurrent
                    ? 'bg-neutral-900 text-white border-neutral-900 font-semibold'
                    : 'bg-neutral-100 hover:bg-neutral-200 text-neutral-700 border-neutral-200 font-medium'
            }`;
        });

        // Hide custom date picker if a standard preset is selected
        if (customContainer) {
            if (activePreset === 'custom') {
                customContainer.classList.remove('hidden');
                if (summarySpan) summarySpan.innerText = '';
            } else {
                customContainer.classList.add('hidden');
                const s = document.getElementById('match-window-start')?.value;
                const e = document.getElementById('match-window-end')?.value;
                if (summarySpan && s && e) {
                    summarySpan.innerText = formatHumanDateRange(s, e);
                }
            }
        }
    };

    const applyDatePreset = (presetKey) => {
        const now = new Date();
        const isoDay = getIsoDay(now);
        let sDate, eDate;

        if (presetKey === 'this-week') {
            sDate = now;
            eDate = addDays(now, 7 - isoDay);
        } else if (presetKey === 'next-week') {
            const daysToNextMonday = 8 - isoDay;
            sDate = addDays(now, daysToNextMonday);
            eDate = addDays(sDate, 6);
        } else if (presetKey === 'weekend') {
            if (isoDay <= 5) {
                sDate = addDays(now, 6 - isoDay);
                eDate = addDays(now, 7 - isoDay);
            } else if (isoDay === 6) {
                sDate = now;
                eDate = addDays(now, 1);
            } else {
                // Sunday: target next weekend
                sDate = addDays(now, 6);
                eDate = addDays(now, 7);
            }
            // Auto-select Saturday and Sunday in preferred days
            preferredDays.clear();
            preferredDays.add(6);
            preferredDays.add(7);
            updatePreferredDaysUI();
        } else if (presetKey === '7-days') {
            sDate = now;
            eDate = addDays(now, 7);
        } else if (presetKey === 'custom') {
            updatePresetButtons('custom');
            return;
        }

        if (sDate && eDate) {
            document.getElementById('match-window-start').value = formatDate(sDate);
            document.getElementById('match-window-end').value = formatDate(eDate);
            updatePresetButtons(presetKey);
        }
    };

    document.querySelectorAll('.date-preset-btn').forEach(btn => {
        btn.onclick = () => {
            const presetKey = btn.getAttribute('data-preset');
            applyDatePreset(presetKey);
        };
    });

    ['match-window-start', 'match-window-end'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.oninput = () => {
                updatePresetButtons('custom');
            };
        }
    });

    // Default to 7 days preset initially
    applyDatePreset('7-days');

    // Load Friends into selector
    let friendsList = [];
    try {
        friendsList = await api.friends.getAll();
        const friendsContainer = document.getElementById('matcher-friends-container');
        if (friendsList.length === 0) {
            friendsContainer.innerHTML = '<div class="text-xs text-neutral-500 py-2">Немає доданих друзів. Додайте їх у вкладці "Друзі".</div>';
        } else {
            friendsContainer.innerHTML = friendsList.map((f, i) => `
                <label class="flex items-center gap-2.5 p-2 rounded-lg bg-neutral-50 hover:bg-neutral-100 border border-neutral-200 cursor-pointer transition">
                    <input type="checkbox" name="match-friend" value="${f.username}" ${i === 0 ? 'checked' : ''} class="rounded border-neutral-300 text-neutral-900 focus:ring-0">
                    <div class="text-xs">
                        <span class="font-medium text-neutral-900">${f.name || f.username}</span>
                        <span class="text-neutral-500">(@${f.username})</span>
                    </div>
                </label>
            `).join('');
        }
    } catch {
        const friendsContainer = document.getElementById('matcher-friends-container');
        friendsContainer.innerHTML = `
            <label class="flex items-center gap-2.5 p-2 rounded-lg bg-neutral-50 border border-neutral-200 cursor-pointer">
                <input type="checkbox" name="match-friend" value="oleksiy_k" checked class="rounded border-neutral-300 text-neutral-900">
                <span class="text-xs font-medium text-neutral-900">Олексій Коваленко (@oleksiy_k)</span>
            </label>
            <label class="flex items-center gap-2.5 p-2 rounded-lg bg-neutral-50 border border-neutral-200 cursor-pointer">
                <input type="checkbox" name="match-friend" value="maria_s" checked class="rounded border-neutral-300 text-neutral-900">
                <span class="text-xs font-medium text-neutral-900">Марія Сидоренко (@maria_s)</span>
            </label>
        `;
    }

    let calculatedSlots = [];

    // Calculate slots click handler
    document.getElementById('calc-slots-btn').onclick = async () => {
        const selectedFriends = Array.from(document.querySelectorAll('input[name="match-friend"]:checked')).map(cb => cb.value);
        if (selectedFriends.length === 0) {
            toast.warning("Оберіть хоча б одного друга для зустрічі");
            return;
        }

        const startVal = document.getElementById('match-window-start').value;
        const endVal = document.getElementById('match-window-end').value;

        // Ensure windowStart is in the future for backend @Future validation
        let windowStart;
        const curDateStr = formatDate(new Date());
        if (startVal === curDateStr) {
            const nowPlus = new Date(Date.now() + 5 * 60000);
            windowStart = nowPlus.toISOString().slice(0, 19);
        } else {
            windowStart = startVal + "T00:00:00";
        }
        const windowEnd = endVal + "T23:59:59";

        let dailyStartTime = document.getElementById('match-daily-start').value;
        let dailyEndTime = document.getElementById('match-daily-end').value;
        if (dailyStartTime.length === 5) dailyStartTime += ':00';
        if (dailyEndTime.length === 5) dailyEndTime += ':00';

        const durationMinutes = parseInt(document.getElementById('match-duration').value);

        const resultsContainer = document.getElementById('calculated-slots-container');
        resultsContainer.innerHTML = '<div class="text-center py-10 text-neutral-500 text-xs">Розрахунок доступних варіантів...</div>';

        try {
            calculatedSlots = await api.availability.calculate({
                participantNames: selectedFriends,
                windowStart,
                windowEnd,
                preferredDaysOfWeek: Array.from(preferredDays).sort(),
                dailyStartTime,
                dailyEndTime,
                durationMinutes
            });

            if (!calculatedSlots || calculatedSlots.length === 0) {
                resultsContainer.innerHTML = `
                    <div class="text-center py-10 text-neutral-500 text-xs">
                        Не знайдено спільних слотів за вказаними фільтрами.<br>
                        Спробуйте розширити години або обрати інші дні.
                    </div>
                `;
                document.getElementById('create-poll-section').classList.add('hidden');
                return;
            }

            resultsContainer.innerHTML = calculatedSlots.map((slot, idx) => {
                const s = new Date(slot.start);
                const e = new Date(slot.end);
                const dateStr = s.toLocaleDateString('uk-UA', { weekday: 'long', day: 'numeric', month: 'long' });
                const timeStr = `${s.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})} — ${e.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}`;

                let badgeClass = 'bg-emerald-50 text-emerald-700 border-emerald-200';
                let badgeText = 'Вільні всі';

                if (slot.score < 95 && slot.score >= 50) {
                    badgeClass = 'bg-amber-50 text-amber-800 border-amber-200';
                    badgeText = 'Найменш зайняті';
                } else if (slot.score < 50) {
                    badgeClass = 'bg-neutral-100 text-neutral-700 border-neutral-200';
                    badgeText = 'Частковий збіг';
                }

                return `
                    <div class="flex items-center justify-between p-3.5 rounded-lg border border-neutral-200 bg-neutral-50/50 hover:bg-neutral-50 transition">
                        <div class="flex items-center gap-3">
                            <div class="w-7 h-7 rounded-md bg-neutral-200 text-neutral-800 font-semibold text-xs flex items-center justify-center">
                                ${idx + 1}
                            </div>
                            <div>
                                <div class="text-xs font-semibold text-neutral-900 capitalize">${dateStr}</div>
                                <div class="text-[11px] text-neutral-500 flex items-center gap-2 mt-0.5">
                                    <span>${timeStr}</span>
                                    <span>&bull;</span>
                                    <span>Вільний час: ${slot.score}%</span>
                                </div>
                            </div>
                        </div>
                        <span class="text-[11px] px-2 py-0.5 rounded ${badgeClass} border font-medium">
                            ${badgeText}
                        </span>
                    </div>
                `;
            }).join('');

            document.getElementById('create-poll-section').classList.remove('hidden');
            if (window.lucide) window.lucide.createIcons();
            toast.success(`Знайдено ${calculatedSlots.length} варіанти`);
        } catch (err) {
            resultsContainer.innerHTML = `<div class="text-red-600 text-xs py-4 text-center">${err.message}</div>`;
            toast.error(err.message || "Помилка розрахунку слотів");
        }
    };

    // Create Poll click handler
    document.getElementById('submit-poll-btn').onclick = async () => {
        const titleInput = document.getElementById('poll-title-input');
        const title = titleInput.value.trim() || "Зустріч";
        const selectedFriends = Array.from(document.querySelectorAll('input[name="match-friend"]:checked')).map(cb => cb.value);

        if (calculatedSlots.length === 0) {
            toast.warning("Спочатку розрахуйте слоти");
            return;
        }

        const btn = document.getElementById('submit-poll-btn');
        btn.disabled = true;

        try {
            await api.meetings.propose({
                title,
                participantNames: selectedFriends,
                proposedOptions: calculatedSlots
            });
            toast.success("Опитування створено, запрошення надіслано");
            window.switchTab('meetings');
        } catch (err) {
            toast.error(err.message || "Помилка створення опитування");
        } finally {
            btn.disabled = false;
        }
    };
}
