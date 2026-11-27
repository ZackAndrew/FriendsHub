import { api, isAuthenticated } from '../api.js?v=4.0';
import { toast } from './toast.js?v=4.0';

export async function renderFriends(container) {
    if (!isAuthenticated()) {
        container.innerHTML = `
            <div class="py-12 text-center text-neutral-500 text-sm">
                <p>Будь ласка, увійдіть, щоб переглядати друзів.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = `
        <div class="space-y-6">
            <!-- Header Banner -->
            <div class="bg-white p-6 rounded-xl border border-neutral-200 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h2 class="text-xl font-bold text-neutral-900 tracking-tight">Друзі</h2>
                    <p class="text-xs text-neutral-500 mt-1">Додавайте друзів за юзернеймом або ID для пошуку спільного вільного часу.</p>
                </div>

                <!-- Add Friend by ID / Username Form -->
                <form id="add-friend-form" class="flex items-center gap-2">
                    <input type="text" id="friend-search-input" required placeholder="Юзернейм або ID..." class="px-3 py-1.5 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 text-xs focus:outline-none focus:border-neutral-900 transition w-56">
                    <button type="submit" class="px-3.5 py-1.5 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center gap-1.5 whitespace-nowrap">
                        <i data-lucide="user-plus" class="w-3.5 h-3.5"></i>
                        <span>Додати</span>
                    </button>
                </form>
            </div>

            <!-- Incoming Friend Requests (if any) -->
            <div id="incoming-requests-section" class="hidden bg-white p-5 rounded-xl border border-neutral-200">
                <div class="flex items-center gap-2 mb-3">
                    <i data-lucide="bell" class="w-4 h-4 text-neutral-700"></i>
                    <h3 class="text-xs font-semibold text-neutral-900 uppercase tracking-wider">Вхідні запити на дружбу</h3>
                </div>
                <div id="incoming-requests-list" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3"></div>
            </div>

            <!-- Friends Grid -->
            <div>
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-sm font-semibold text-neutral-900 flex items-center gap-2">
                        <i data-lucide="users" class="w-4 h-4 text-neutral-600"></i>
                        <span>Список друзів</span>
                    </h3>
                    <span id="friends-count-badge" class="text-xs bg-neutral-100 text-neutral-700 px-2 py-0.5 rounded font-medium">...</span>
                </div>
                <div id="friends-grid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div class="col-span-full py-12 text-center text-neutral-400 text-xs">
                        Завантаження списку друзів...
                    </div>
                </div>
            </div>
        </div>
    `;

    if (window.lucide) window.lucide.createIcons();

    const loadFriendsData = async () => {
        try {
            const [friends, pendingRequests] = await Promise.all([
                api.friends.getAll().catch(() => []),
                api.friends.getPendingRequests().catch(() => [])
            ]);

            // Badge count
            const countBadge = document.getElementById('friends-count-badge');
            if (countBadge) countBadge.innerText = `${friends.length} друзів`;

            // Pending requests
            const reqSection = document.getElementById('incoming-requests-section');
            const reqList = document.getElementById('incoming-requests-list');

            if (pendingRequests && pendingRequests.length > 0) {
                reqSection.classList.remove('hidden');
                reqList.innerHTML = pendingRequests.map(r => `
                    <div class="flex items-center justify-between p-3 rounded-lg bg-neutral-50 border border-neutral-200">
                        <div class="flex items-center gap-2.5">
                            <div class="w-8 h-8 rounded-full bg-neutral-200 text-neutral-800 flex items-center justify-center font-medium text-xs">
                                ${(r.requesterName || r.requesterUsername || 'U')[0].toUpperCase()}
                            </div>
                            <div>
                                <div class="text-xs font-semibold text-neutral-900">${r.requesterName || r.requesterUsername}</div>
                                <div class="text-[11px] text-neutral-500">@${r.requesterUsername}</div>
                            </div>
                        </div>
                        <div class="flex items-center gap-1.5">
                            <button onclick="window.friendRequestAction(${r.id}, 'accept')" class="p-1.5 rounded-md bg-neutral-900 hover:bg-neutral-800 text-white transition" title="Прийняти">
                                <i data-lucide="check" class="w-3.5 h-3.5"></i>
                            </button>
                            <button onclick="window.friendRequestAction(${r.id}, 'decline')" class="p-1.5 rounded-md text-neutral-500 hover:text-red-600 hover:bg-neutral-100 transition" title="Відхилити">
                                <i data-lucide="x" class="w-3.5 h-3.5"></i>
                            </button>
                        </div>
                    </div>
                `).join('');
            } else if (reqSection) {
                reqSection.classList.add('hidden');
            }

            // Friends Grid
            const friendsGrid = document.getElementById('friends-grid');
            if (!friendsGrid) return;

            if (friends.length === 0) {
                friendsGrid.innerHTML = `
                    <div class="col-span-full bg-white p-8 rounded-xl border border-neutral-200 text-center max-w-sm mx-auto">
                        <div class="w-10 h-10 rounded-lg bg-neutral-100 text-neutral-700 flex items-center justify-center mx-auto mb-3">
                            <i data-lucide="users" class="w-5 h-5"></i>
                        </div>
                        <h4 class="text-sm font-semibold text-neutral-900 mb-1">У вас ще немає доданих друзів</h4>
                        <p class="text-xs text-neutral-500">Введіть юзернейм або ID вище, щоб надіслати запит на дружбу.</p>
                    </div>
                `;
            } else {
                friendsGrid.innerHTML = friends.map(f => `
                    <div class="bg-white p-5 rounded-xl border border-neutral-200 flex flex-col justify-between hover:border-neutral-300 transition">
                        <div>
                            <div class="flex items-center justify-between mb-4">
                                <div class="flex items-center gap-3">
                                    <div class="w-10 h-10 rounded-full bg-neutral-100 border border-neutral-200 flex items-center justify-center text-neutral-900 font-semibold text-sm">
                                        ${(f.name || f.username || 'F')[0].toUpperCase()}
                                    </div>
                                    <div>
                                        <h4 class="text-sm font-semibold text-neutral-900">${f.name || f.username}</h4>
                                        <span class="text-xs text-neutral-500">@${f.username}</span>
                                    </div>
                                </div>
                                <button onclick="window.removeFriendAction(${f.friendshipId})" class="text-neutral-400 hover:text-red-600 transition p-1" title="Видалити з друзів">
                                    <i data-lucide="user-minus" class="w-4 h-4"></i>
                                </button>
                            </div>

                            <div class="text-[11px] text-neutral-500 bg-neutral-50 p-2 rounded-lg border border-neutral-200 mb-4 flex items-center justify-between">
                                <span>У друзях з:</span>
                                <span class="text-neutral-700 font-medium">${new Date(f.since || Date.now()).toLocaleDateString('uk-UA')}</span>
                            </div>
                        </div>

                        <!-- Actions -->
                        <div class="flex items-center gap-2 pt-3 border-t border-neutral-200">
                            <button onclick="window.quickMeetFriend('${f.username}')" class="flex-1 py-1.5 px-3 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs transition flex items-center justify-center gap-1.5">
                                <i data-lucide="calendar" class="w-3.5 h-3.5"></i>
                                <span>Зустрітися</span>
                            </button>
                            <button onclick="window.switchTab('common-slots')" class="py-1.5 px-3 rounded-lg bg-white hover:bg-neutral-50 text-neutral-700 text-xs font-medium border border-neutral-300 transition" title="Підібрати час">
                                <i data-lucide="clock" class="w-3.5 h-3.5"></i>
                            </button>
                        </div>
                    </div>
                `).join('');
            }

            if (window.lucide) window.lucide.createIcons();
        } catch (err) {
            toast.error(err.message || "Помилка завантаження друзів");
        }
    };

    // Friend Request actions
    window.friendRequestAction = async (id, action) => {
        try {
            if (action === 'accept') {
                await api.friends.acceptRequest(id);
                toast.success("Запит на дружбу прийнято");
            } else {
                await api.friends.declineRequest(id);
                toast.info("Запит відхилено");
            }
            loadFriendsData();
        } catch (err) {
            toast.error(err.message || "Дію не вдалося виконати");
        }
    };

    window.removeFriendAction = async (friendshipId) => {
        if (!confirm("Видалити цього користувача з друзів?")) return;
        try {
            await api.friends.removeFriend(friendshipId);
            toast.info("Користувача видалено з друзів");
            loadFriendsData();
        } catch (err) {
            toast.error(err.message || "Помилка видалення");
        }
    };

    window.quickMeetFriend = (username) => {
        window.switchTab('common-slots');
        toast.info(`Підберіть спільний час для зустрічі з @${username}`);
    };

    document.getElementById('add-friend-form').onsubmit = async (e) => {
        e.preventDefault();
        const input = document.getElementById('friend-search-input');
        const query = input.value.trim();

        try {
            let targetUserId = null;
            if (/^\d+$/.test(query)) {
                targetUserId = parseInt(query);
            } else {
                const userObj = await api.users.getByUsername(query);
                targetUserId = userObj.id;
            }

            await api.friends.sendRequest(targetUserId);
            toast.success(`Запит на дружбу надіслано користувачу "${query}"`);
            input.value = '';
            loadFriendsData();
        } catch (err) {
            toast.error(err.message || `Не вдалося знайти користувача "${query}"`);
        }
    };

    loadFriendsData();
}
