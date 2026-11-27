import { isAuthenticated, getCurrentUser, api, setDemoMode, getDemoMode } from '../api.js?v=4.0';
import { openAuthModal } from './auth-modal.js?v=4.0';
import { toast } from './toast.js?v=4.0';

export function renderNavbar() {
    const authContainer = document.getElementById('auth-actions');
    const backendStatus = document.getElementById('backend-status');
    const backendIndicator = document.getElementById('backend-indicator');
    const backendLabel = document.getElementById('backend-label');

    if (!authContainer) return;

    const isAuth = isAuthenticated();
    const user = getCurrentUser();
    const isDemo = getDemoMode();

    if (backendStatus) {
        backendIndicator.className = isDemo ? 'w-1.5 h-1.5 rounded-full bg-amber-600' : 'w-1.5 h-1.5 rounded-full bg-emerald-600';
        backendLabel.innerText = isDemo ? 'Демо-режим' : 'Live API';
        backendStatus.classList.remove('hidden');
        backendStatus.title = isDemo ? "Натисніть для перемикання на Live API" : "Працює через Spring Boot API";
        backendStatus.onclick = () => {
            const nextMode = !isDemo;
            setDemoMode(nextMode);
            toast.info(nextMode ? "Увімкнено демо-режим" : "Увімкнено Live API");
            renderNavbar();
            window.dispatchEvent(new CustomEvent('auth-changed'));
        };
    }

    if (isAuth && user) {
        authContainer.innerHTML = `
            <div class="flex items-center gap-2.5">
                <div class="hidden sm:flex flex-col text-right leading-tight">
                    <span class="text-xs font-semibold text-neutral-900">${user.name || user.username}</span>
                    <span class="text-[11px] text-neutral-500">@${user.username}</span>
                </div>
                <div class="w-8 h-8 rounded-full bg-neutral-100 text-neutral-800 border border-neutral-300 flex items-center justify-center font-semibold text-xs">
                    ${(user.name || user.username || 'U')[0].toUpperCase()}
                </div>
                <button id="logout-btn" title="Вийти з акаунту" class="p-1.5 rounded-md text-neutral-500 hover:text-red-600 hover:bg-neutral-100 transition">
                    <i data-lucide="log-out" class="w-4 h-4"></i>
                </button>
            </div>
        `;

        document.getElementById('logout-btn').onclick = () => {
            api.auth.logout();
            toast.info("Ви вийшли з акаунту");
        };
    } else {
        authContainer.innerHTML = `
            <button id="nav-login-btn" class="px-3 py-1.5 rounded-md text-xs font-medium text-neutral-700 hover:bg-neutral-100 border border-neutral-300 transition">
                Увійти
            </button>
            <button id="nav-register-btn" class="px-3 py-1.5 rounded-md text-xs font-medium text-white bg-neutral-900 hover:bg-neutral-800 transition">
                Реєстрація
            </button>
        `;

        document.getElementById('nav-login-btn').onclick = () => openAuthModal('login');
        document.getElementById('nav-register-btn').onclick = () => openAuthModal('register');
    }

    if (window.lucide) window.lucide.createIcons();
}

export function updateNavBadges(pendingMeetings = 0, friendRequests = 0) {
    const badgeMeetings = document.getElementById('badge-pending-meetings');
    const badgeFriends = document.getElementById('badge-friend-requests');

    if (badgeMeetings) {
        if (pendingMeetings > 0) {
            badgeMeetings.innerText = pendingMeetings;
            badgeMeetings.classList.remove('hidden');
        } else {
            badgeMeetings.classList.add('hidden');
        }
    }

    if (badgeFriends) {
        if (friendRequests > 0) {
            badgeFriends.innerText = friendRequests;
            badgeFriends.classList.remove('hidden');
        } else {
            badgeFriends.classList.add('hidden');
        }
    }
}
