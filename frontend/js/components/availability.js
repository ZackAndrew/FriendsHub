import { api, isAuthenticated } from '../api.js?v=4.0';
import { toast } from './toast.js?v=4.0';

export async function renderAvailability(container) {
    if (!isAuthenticated()) {
        container.innerHTML = `
            <div class="py-12 text-center text-neutral-500 text-sm">
                <p>Будь ласка, увійдіть, щоб налаштувати власний графік.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="space-y-6">
            <!-- Header Banner -->
            <div class="bg-white p-6 rounded-xl border border-neutral-200 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h2 class="text-xl font-bold text-neutral-900 tracking-tight">Мій розклад зайнятості</h2>
                    <p class="text-xs text-neutral-500 mt-1">
                        Вкажіть щотижневі повторювані заняття (робота, навчання, спорт). Система автоматично врахує їх при підборі вільного часу.
                    </p>
                </div>

                <div class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-neutral-100 border border-neutral-200 text-xs text-neutral-700">
                    <i data-lucide="calendar" class="w-4 h-4 text-neutral-600"></i>
                    <span>Синхронізація з календарем</span>
                </div>
            </div>

            <!-- Routine Management Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <!-- Add Routine Form Card -->
                <div class="bg-white p-6 rounded-xl border border-neutral-200 lg:col-span-1 h-fit">
                    <h3 class="text-sm font-semibold text-neutral-900 mb-2 flex items-center gap-2">
                        <i data-lucide="plus" class="w-4 h-4 text-neutral-600"></i>
                        <span>Додати заняття</span>
                    </h3>
                    <p class="text-xs text-neutral-500 mb-4">Вкажіть назву, дні тижня та години.</p>

                    <form id="add-routine-form" class="space-y-3.5">
                        <div>
                            <label class="block text-xs font-medium text-neutral-700 mb-1">Назва</label>
                            <input type="text" id="routine-name" required placeholder="Наприклад: Робота або Тренування" class="w-full px-3 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 text-xs focus:outline-none focus:border-neutral-900 transition">
                        </div>

                        <!-- Days of week selector -->
                        <div>
                            <label class="block text-xs font-medium text-neutral-700 mb-1.5">Дні тижня</label>
                            <div class="grid grid-cols-7 gap-1" id="days-selector">
                                <button type="button" data-day="1" class="day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Пн</button>
                                <button type="button" data-day="2" class="day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Вт</button>
                                <button type="button" data-day="3" class="day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Ср</button>
                                <button type="button" data-day="4" class="day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Чт</button>
                                <button type="button" data-day="5" class="day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Пт</button>
                                <button type="button" data-day="6" class="day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Сб</button>
                                <button type="button" data-day="7" class="day-btn py-1.5 text-xs font-medium rounded-md bg-white text-neutral-700 border border-neutral-300 transition">Нд</button>
                            </div>
                        </div>

                        <!-- Hours range -->
                        <div class="grid grid-cols-2 gap-2">
                            <div>
                                <label class="block text-[11px] font-medium text-neutral-600 mb-1">Початок</label>
                                <input type="time" id="routine-start" required value="09:00" class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs focus:outline-none focus:border-neutral-900 transition">
                            </div>
                            <div>
                                <label class="block text-[11px] font-medium text-neutral-600 mb-1">Кінець</label>
                                <input type="time" id="routine-end" required value="18:00" class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 text-xs focus:outline-none focus:border-neutral-900 transition">
                            </div>
                        </div>

                        <button type="submit" class="w-full py-2.5 px-4 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center justify-center gap-1.5">
                            <i data-lucide="plus" class="w-4 h-4"></i>
                            <span>Додати до списку</span>
                        </button>
                    </form>
                </div>

                <!-- Active Routines List -->
                <div class="bg-white p-6 rounded-xl border border-neutral-200 lg:col-span-2 flex flex-col justify-between">
                    <div>
                        <div class="flex items-center justify-between mb-4">
                            <h3 class="text-sm font-semibold text-neutral-900 flex items-center gap-2">
                                <i data-lucide="clock" class="w-4 h-4 text-neutral-600"></i>
                                <span>Постійний графік</span>
                            </h3>
                            <button id="save-routines-btn" class="px-3.5 py-1.5 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center gap-1.5">
                                <i data-lucide="check" class="w-3.5 h-3.5"></i>
                                <span>Зберегти зміни</span>
                            </button>
                        </div>

                        <div id="routines-list" class="space-y-2.5">
                            <div class="py-8 text-center text-neutral-400 text-xs">Завантаження розкладу...</div>
                        </div>
                    </div>

                    <div class="mt-6 pt-4 border-t border-neutral-200 text-xs text-neutral-500">
                        Підказка: Вільний час визначається між вашими постійними заняттями.
                    </div>
                </div>
            </div>
        </div>
    `;

    if (window.lucide) window.lucide.createIcons();

    let routines = [];
    const dayNames = { 1: "Пн", 2: "Вт", 3: "Ср", 4: "Чт", 5: "Пт", 6: "Сб", 7: "Нд" };

    // Toggle days logic
    const selectedDays = new Set([1, 2, 3, 4, 5]);
    const updateDayButtons = () => {
        document.querySelectorAll('.day-btn').forEach(btn => {
            const day = parseInt(btn.getAttribute('data-day'));
            const isSelected = selectedDays.has(day);
            btn.className = `day-btn py-1.5 text-xs rounded-md transition border ${
                isSelected ? 'bg-neutral-900 text-white border-neutral-900 font-semibold' : 'bg-white text-neutral-700 hover:bg-neutral-50 border-neutral-300 font-medium'
            }`;
        });
    };

    document.querySelectorAll('.day-btn').forEach(btn => {
        btn.onclick = () => {
            const day = parseInt(btn.getAttribute('data-day'));
            if (selectedDays.has(day)) {
                if (selectedDays.size > 1) selectedDays.delete(day);
            } else {
                selectedDays.add(day);
            }
            updateDayButtons();
        };
    });
    updateDayButtons();

    const renderRoutinesList = () => {
        const listContainer = document.getElementById('routines-list');
        if (!listContainer) return;

        if (routines.length === 0) {
            listContainer.innerHTML = `
                <div class="text-center py-8 text-neutral-400 text-xs">
                    Графік ще не додано.<br>
                    Додайте ваші постійні години зайнятості у формі зліва.
                </div>
            `;
            return;
        }

        listContainer.innerHTML = routines.map((r, idx) => {
            const daysLabel = (r.daysOfWeek || []).sort().map(d => dayNames[d] || d).join(', ');
            return `
                <div class="flex items-center justify-between p-3.5 rounded-lg border border-neutral-200 bg-neutral-50/50 hover:bg-neutral-50 transition">
                    <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded-md bg-white border border-neutral-200 text-neutral-700 flex items-center justify-center font-medium text-xs">
                            <i data-lucide="calendar" class="w-4 h-4"></i>
                        </div>
                        <div>
                            <div class="text-xs font-semibold text-neutral-900">${r.name}</div>
                            <div class="text-[11px] text-neutral-500 mt-0.5 flex items-center gap-2">
                                <span class="px-1.5 py-0.2 rounded bg-neutral-200 text-neutral-800 font-medium">${daysLabel}</span>
                                <span>${r.startTime.slice(0, 5)} — ${r.endTime.slice(0, 5)}</span>
                            </div>
                        </div>
                    </div>
                    <button onclick="window.removeRoutine(${idx})" class="p-1.5 text-neutral-400 hover:text-red-600 transition" title="Видалити">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                    </button>
                </div>
            `;
        }).join('');

        if (window.lucide) window.lucide.createIcons();
    };

    window.removeRoutine = (idx) => {
        routines.splice(idx, 1);
        renderRoutinesList();
    };

    // Load current routines
    try {
        const loaded = await api.routines.get();
        routines = Array.isArray(loaded) ? loaded : [];
        renderRoutinesList();
    } catch {
        routines = [
            { name: "Робота", daysOfWeek: [1, 2, 3, 4, 5], startTime: "09:00:00", endTime: "18:00:00" },
            { name: "Спорт", daysOfWeek: [2, 4], startTime: "19:00:00", endTime: "20:30:00" }
        ];
        renderRoutinesList();
    }

    // Add new routine item
    document.getElementById('add-routine-form').onsubmit = (e) => {
        e.preventDefault();
        const name = document.getElementById('routine-name').value.trim();
        const start = document.getElementById('routine-start').value;
        const end = document.getElementById('routine-end').value;

        if (start >= end) {
            toast.error("Час початку має бути раніше за час завершення");
            return;
        }

        routines.push({
            name,
            daysOfWeek: Array.from(selectedDays).sort(),
            startTime: start.length === 5 ? `${start}:00` : start,
            endTime: end.length === 5 ? `${end}:00` : end
        });

        toast.info(`"${name}" додано. Натисніть "Зберегти зміни", щоб оновити на сервері.`);
        document.getElementById('routine-name').value = '';
        renderRoutinesList();
    };

    // Save all routines to backend
    document.getElementById('save-routines-btn').onclick = async () => {
        const btn = document.getElementById('save-routines-btn');
        btn.disabled = true;
        try {
            await api.routines.save(routines);
            toast.success("Графік збережено");
        } catch (err) {
            toast.error(err.message || "Помилка збереження графіка");
        } finally {
            btn.disabled = false;
        }
    };
}
