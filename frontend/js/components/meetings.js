import { api, isAuthenticated, getCurrentUser } from '../api.js?v=4.0';
import { toast } from './toast.js?v=4.0';

export async function renderMeetings(container) {
    if (!isAuthenticated()) {
        container.innerHTML = `
            <div class="py-12 text-center text-neutral-500 text-sm">
                <p>Будь ласка, увійдіть, щоб переглядати зустрічі.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="space-y-6">
            <!-- Header -->
            <div class="bg-white p-6 rounded-xl border border-neutral-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h2 class="text-xl font-bold text-neutral-900 tracking-tight">Зустрічі та опитування</h2>
                    <p class="text-xs text-neutral-500 mt-1">
                        Голосуйте за запропоновані часові слоти або переглядайте заплановані події.
                    </p>
                </div>
                <button onclick="window.switchTab('common-slots')" class="px-3.5 py-2 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center gap-1.5 self-start sm:self-auto">
                    <i data-lucide="clock" class="w-3.5 h-3.5"></i>
                    <span>Підібрати час</span>
                </button>
            </div>

            <!-- Subtabs Navigation -->
            <div class="flex border-b border-neutral-200 space-x-6 text-xs font-medium">
                <button id="subtab-all" class="pb-2.5 border-b-2 border-neutral-900 text-neutral-900 font-semibold flex items-center gap-1.5">
                    <i data-lucide="calendar" class="w-3.5 h-3.5"></i>
                    <span>Усі події</span>
                </button>
                <button id="subtab-polls" class="pb-2.5 border-b-2 border-transparent text-neutral-500 hover:text-neutral-900 flex items-center gap-1.5">
                    <i data-lucide="vote" class="w-3.5 h-3.5"></i>
                    <span>Опитування</span>
                </button>
                <button id="subtab-finalized" class="pb-2.5 border-b-2 border-transparent text-neutral-500 hover:text-neutral-900 flex items-center gap-1.5">
                    <i data-lucide="check-circle-2" class="w-3.5 h-3.5"></i>
                    <span>Підтверджені</span>
                </button>
            </div>

            <!-- Content Area -->
            <div id="meetings-list-container" class="grid grid-cols-1 lg:grid-cols-2 gap-4">
                <div class="col-span-full py-12 text-center text-neutral-400 text-xs">
                    Завантаження зустрічей...
                </div>
            </div>
        </div>
    `;

    if (window.lucide) window.lucide.createIcons();

    let activeSubtab = 'all';

    const setSubtab = (tab) => {
        activeSubtab = tab;
        ['all', 'polls', 'finalized'].forEach(t => {
            const btn = document.getElementById(`subtab-${t}`);
            if (btn) {
                const isActive = t === tab;
                btn.className = `pb-2.5 border-b-2 flex items-center gap-1.5 transition ${
                    isActive ? 'border-neutral-900 text-neutral-900 font-semibold' : 'border-transparent text-neutral-500 hover:text-neutral-900'
                }`;
            }
        });
        loadMeetingsData();
    };

    document.getElementById('subtab-all').onclick = () => setSubtab('all');
    document.getElementById('subtab-polls').onclick = () => setSubtab('polls');
    document.getElementById('subtab-finalized').onclick = () => setSubtab('finalized');

    const loadMeetingsData = async () => {
        const listContainer = document.getElementById('meetings-list-container');
        listContainer.innerHTML = '<div class="col-span-full py-12 text-center text-neutral-400 text-xs">Завантаження...</div>';

        try {
            const allMeetings = await api.meetings.getAll();
            let filtered = allMeetings;

            if (activeSubtab === 'polls') {
                filtered = allMeetings.filter(m => m.options && m.options.length > 0 && !m.startTime);
            } else if (activeSubtab === 'finalized') {
                filtered = allMeetings.filter(m => m.startTime);
            }

            if (!filtered || filtered.length === 0) {
                listContainer.innerHTML = `
                    <div class="col-span-full bg-white p-8 rounded-xl border border-neutral-200 text-center max-w-sm mx-auto">
                        <div class="w-10 h-10 rounded-lg bg-neutral-100 text-neutral-700 flex items-center justify-center mx-auto mb-3">
                            <i data-lucide="calendar" class="w-5 h-5"></i>
                        </div>
                        <h4 class="text-sm font-semibold text-neutral-900 mb-1">Немає зустрічей у цій категорії</h4>
                        <p class="text-xs text-neutral-500 mb-4">Скористайтеся підбором часу, щоб створити нове опитування.</p>
                        <button onclick="window.switchTab('common-slots')" class="px-3.5 py-2 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-xs font-medium text-white transition">
                            Підібрати час
                        </button>
                    </div>
                `;
                if (window.lucide) window.lucide.createIcons();
                return;
            }

            const currentUser = getCurrentUser();

            listContainer.innerHTML = filtered.map(m => {
                const isOrganizer = currentUser && m.organizerId === currentUser.id;
                const isPoll = m.options && m.options.length > 0 && !m.startTime;
                const participantsList = m.participants || [];

                return `
                    <div class="bg-white p-5 rounded-xl border border-neutral-200 flex flex-col justify-between">
                        <div>
                            <!-- Header Status & Title -->
                            <div class="flex items-center justify-between mb-3">
                                <span class="text-[11px] px-2 py-0.5 rounded font-medium border ${
                                    isPoll ? 'bg-amber-50 text-amber-800 border-amber-200' : 'bg-emerald-50 text-emerald-800 border-emerald-200'
                                }">
                                    ${isPoll ? 'Опитування' : 'Підтверджено'}
                                </span>
                                <span class="text-xs text-neutral-500">
                                    Організатор: <span class="text-neutral-800 font-medium">${isOrganizer ? 'Ви' : m.organizerName}</span>
                                </span>
                            </div>

                            <h3 class="text-base font-semibold text-neutral-900 mb-2">${m.title}</h3>

                            <!-- Participants list -->
                            <div class="mb-4">
                                <span class="text-[11px] font-medium text-neutral-500 uppercase tracking-wider block mb-1">Учасники</span>
                                <div class="flex flex-wrap gap-1.5">
                                    ${participantsList.map(p => `
                                        <span class="px-2 py-0.5 rounded bg-neutral-100 border border-neutral-200 text-xs text-neutral-700">
                                            @${p.username}
                                        </span>
                                    `).join('')}
                                </div>
                            </div>

                            <!-- If Finalized: show exact date/time -->
                            ${!isPoll && m.startTime ? `
                                <div class="p-3 rounded-lg bg-neutral-50 border border-neutral-200 text-xs text-neutral-800 mb-4 flex items-center gap-3">
                                    <div class="w-8 h-8 rounded-md bg-white border border-neutral-200 text-neutral-700 flex items-center justify-center font-semibold">
                                        <i data-lucide="calendar" class="w-4 h-4"></i>
                                    </div>
                                    <div>
                                        <div class="font-medium text-neutral-900 capitalize">${new Date(m.startTime).toLocaleDateString('uk-UA', { weekday: 'long', day: 'numeric', month: 'long' })}</div>
                                        <div class="text-neutral-500 text-[11px]">${new Date(m.startTime).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})} — ${new Date(m.endTime).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</div>
                                    </div>
                                </div>
                            ` : ''}

                            <!-- If Poll: show voting options -->
                            ${isPoll ? `
                                <div class="space-y-2 mb-4">
                                    <span class="text-[11px] font-medium text-neutral-600 uppercase tracking-wider block">Оберіть зручні варіанти</span>
                                    ${m.options.map(opt => {
                                        const s = new Date(opt.startTime);
                                        const e = new Date(opt.endTime);
                                        const dateStr = s.toLocaleDateString('uk-UA', { weekday: 'short', day: 'numeric', month: 'short' });
                                        const timeStr = `${s.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})} - ${e.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}`;

                                        return `
                                            <div class="flex items-center justify-between p-2.5 rounded-lg bg-neutral-50 border border-neutral-200 text-xs">
                                                <label class="flex items-center gap-2 cursor-pointer flex-1">
                                                    <input type="checkbox" name="vote-opt-${m.id}" value="${opt.id}" class="rounded border-neutral-300 text-neutral-900 focus:ring-0">
                                                    <div>
                                                        <span class="font-medium text-neutral-900 capitalize">${dateStr}</span>, 
                                                        <span class="text-neutral-600">${timeStr}</span>
                                                    </div>
                                                </label>
                                                <div class="flex items-center gap-2">
                                                    <span class="px-2 py-0.5 rounded bg-white border border-neutral-200 text-neutral-600 text-[11px]">
                                                        Голосів: ${opt.votesCount}
                                                    </span>
                                                    ${isOrganizer ? `
                                                        <button onclick="window.finalizeMeeting(${m.id}, ${opt.id})" class="px-2.5 py-1 rounded bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition" title="Затвердити цей час">
                                                            Обрати
                                                        </button>
                                                    ` : ''}
                                                </div>
                                            </div>
                                        `;
                                    }).join('')}
                                </div>
                            ` : ''}
                        </div>

                        <!-- Action Bar -->
                        <div class="pt-3.5 border-t border-neutral-200 flex items-center justify-between gap-2.5">
                            ${isPoll ? `
                                <button onclick="window.submitVotes(${m.id})" class="flex-1 py-1.5 px-3 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center justify-center gap-1.5">
                                    <i data-lucide="vote" class="w-3.5 h-3.5"></i>
                                    <span>Проголосувати</span>
                                </button>
                            ` : ''}

                            ${isOrganizer ? `
                                <button onclick="window.cancelMeeting(${m.id})" class="p-1.5 rounded-md text-neutral-400 hover:text-red-600 hover:bg-neutral-100 transition text-xs flex items-center gap-1" title="Скасувати зустріч">
                                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                                    <span class="hidden sm:inline">Скасувати</span>
                                </button>
                            ` : `
                                <button onclick="window.leaveMeeting(${m.id})" class="p-1.5 rounded-md text-neutral-400 hover:text-red-600 hover:bg-neutral-100 transition text-xs flex items-center gap-1">
                                    <i data-lucide="x-circle" class="w-4 h-4"></i>
                                    <span>Відмовитися</span>
                                </button>
                            `}
                        </div>
                    </div>
                `;
            }).join('');

            if (window.lucide) window.lucide.createIcons();
        } catch (err) {
            listContainer.innerHTML = `<div class="text-red-600 text-xs py-4 text-center">${err.message}</div>`;
        }
    };

    // Global action handlers
    window.submitVotes = async (meetingId) => {
        const checkboxes = document.querySelectorAll(`input[name="vote-opt-${meetingId}"]:checked`);
        const selectedIds = Array.from(checkboxes).map(cb => parseInt(cb.value));

        if (selectedIds.length === 0) {
            toast.warning("Оберіть хоча б один варіант для голосування");
            return;
        }

        try {
            await api.meetings.vote(meetingId, selectedIds);
            toast.success("Ваш голос враховано");
            loadMeetingsData();
        } catch (err) {
            toast.error(err.message || "Помилка збереження голосу");
        }
    };

    window.finalizeMeeting = async (meetingId, winningOptionId) => {
        if (!confirm("Затвердити цей час для остаточної зустрічі? Усім учасникам буде зафіксовано дату.")) return;
        try {
            await api.meetings.finalize(meetingId, winningOptionId);
            toast.success("Час зустрічі затверджено");
            loadMeetingsData();
        } catch (err) {
            toast.error(err.message || "Помилка затвердження зустрічі");
        }
    };

    window.cancelMeeting = async (meetingId) => {
        if (!confirm("Скасувати цю подію?")) return;
        try {
            await api.meetings.cancel(meetingId);
            toast.info("Зустріч скасовано");
            loadMeetingsData();
        } catch (err) {
            toast.error(err.message || "Помилка скасування");
        }
    };

    window.leaveMeeting = async (meetingId) => {
        if (!confirm("Відхилити участь у цій зустрічі?")) return;
        try {
            await api.meetings.decline(meetingId);
            toast.info("Ви відмовилися від участі");
            loadMeetingsData();
        } catch (err) {
            toast.error(err.message || "Помилка відхилення");
        }
    };

    loadMeetingsData();
}
