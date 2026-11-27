/**
 * FriendsHub Application Entry Point
 */
import { renderNavbar, updateNavBadges } from './components/navbar.js?v=4.0';
import { renderDashboard } from './components/dashboard.js?v=4.0';
import { renderCommonSlots } from './components/common-slots.js?v=6.0';
import { renderMeetings } from './components/meetings.js?v=4.0';
import { renderAvailability } from './components/availability.js?v=4.0';
import { renderFriends } from './components/friends.js?v=4.0';
import { api, isAuthenticated, setAuth, setDemoMode, parseJwt } from './api.js?v=4.0';
import { openAuthModal } from './components/auth-modal.js?v=4.0';
import { toast } from './components/toast.js?v=4.0';

let activeTab = 'dashboard';

const tabRenderers = {
    'dashboard': renderDashboard,
    'common-slots': renderCommonSlots,
    'meetings': renderMeetings,
    'availability': renderAvailability,
    'friends': renderFriends
};

export function switchTab(tabName) {
    if (!tabRenderers[tabName]) return;
    activeTab = tabName;

    // Update Desktop Nav styles
    document.querySelectorAll('.nav-btn').forEach(btn => {
        const matches = btn.getAttribute('data-tab') === tabName;
        btn.classList.toggle('active', matches);
    });

    // Update Mobile Nav styles
    document.querySelectorAll('.mobile-nav-btn').forEach(btn => {
        const matches = btn.getAttribute('data-tab') === tabName;
        btn.classList.toggle('active', matches);
    });

    // Hide all tab panes, show active
    document.querySelectorAll('.tab-pane').forEach(pane => {
        pane.classList.add('hidden');
    });

    const activePane = document.getElementById(`tab-${tabName}`);
    if (activePane) {
        activePane.classList.remove('hidden');
        tabRenderers[tabName](activePane);
    }

    // Refresh icons
    if (window.lucide) window.lucide.createIcons();
}

window.switchTab = switchTab;

async function refreshBadges() {
    if (!isAuthenticated()) {
        updateNavBadges(0, 0);
        return;
    }
    try {
        const [pendingMeetings, pendingFriends] = await Promise.all([
            api.meetings.getPending().catch(() => []),
            api.friends.getPendingRequests().catch(() => [])
        ]);
        updateNavBadges(pendingMeetings.length, pendingFriends.length);
    } catch {
        // silent
    }
}

function initEvents() {
    // Nav Click handlers (Desktop)
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.onclick = () => {
            const tab = btn.getAttribute('data-tab');
            switchTab(tab);
        };
    });

    // Nav Click handlers (Mobile)
    document.querySelectorAll('.mobile-nav-btn').forEach(btn => {
        btn.onclick = () => {
            const tab = btn.getAttribute('data-tab');
            switchTab(tab);
        };
    });

    // Auth state changed listener
    window.addEventListener('auth-changed', () => {
        renderNavbar();
        switchTab(activeTab);
        refreshBadges();
    });

    // Backend mode changed listener
    window.addEventListener('backend-mode-changed', () => {
        renderNavbar();
        switchTab(activeTab);
        refreshBadges();
    });
}

async function handleUrlAuth() {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const isReset = window.location.pathname.includes('reset-password') || urlParams.get('reset') === 'true' || urlParams.has('resetToken');

    if (isReset) {
        const resetToken = urlParams.get('resetToken') || token || '';
        if (resetToken) {
            openAuthModal('set-new-password', resetToken);
        } else {
            openAuthModal('reset');
        }
        window.history.replaceState({}, document.title, '/');
        return;
    }

    if (token) {
        try {
            const payload = parseJwt(token);
            if (payload && payload.sub) {
                let user = null;
                try {
                    user = await api.users.getByUsername(payload.sub);
                } catch {
                    user = { username: payload.sub, name: payload.sub, role: 'USER' };
                }
                setDemoMode(false);
                setAuth(token, user);
                toast.success(`Вітаємо, ${user.name || user.username}! Успішний вхід через Google.`);
                window.dispatchEvent(new CustomEvent('auth-changed'));
            }
        } catch (e) {
            console.error("OAuth token process error:", e);
            toast.error("Помилка авторизації через Google");
        } finally {
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    }
}

// App Bootstrap
function bootstrap() {
    renderNavbar();
    initEvents();
    switchTab('dashboard');
    refreshBadges();
    handleUrlAuth();

    // Periodic badge check every 30s
    setInterval(refreshBadges, 30000);
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bootstrap);
} else {
    bootstrap();
}
