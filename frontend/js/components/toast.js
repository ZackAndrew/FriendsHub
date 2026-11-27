/**
 * FriendsHub — Minimalist Calm Toast Notification Component
 */
export const toast = {
    show(message, type = 'info', duration = 3500) {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const el = document.createElement('div');
        const borderColor = 
            type === 'success' ? 'border-emerald-500' :
            type === 'error' ? 'border-red-500' :
            type === 'warning' ? 'border-amber-500' : 'border-neutral-400';

        const iconColor = 
            type === 'success' ? 'text-emerald-600' :
            type === 'error' ? 'text-red-600' :
            type === 'warning' ? 'text-amber-600' : 'text-neutral-600';

        const iconName = 
            type === 'success' ? 'check' :
            type === 'error' ? 'alert-triangle' :
            type === 'warning' ? 'alert-circle' : 'info';

        el.className = `pointer-events-auto flex items-center gap-3 px-4 py-2.5 rounded-lg bg-white border ${borderColor} text-neutral-900 shadow-sm text-xs font-medium transition-all transform duration-200 translate-y-1 opacity-0`;

        el.innerHTML = `
            <i data-lucide="${iconName}" class="w-4 h-4 flex-shrink-0 ${iconColor}"></i>
            <span class="flex-1 leading-normal">${message}</span>
            <button class="text-neutral-400 hover:text-neutral-800 transition text-sm leading-none">&times;</button>
        `;

        container.appendChild(el);
        if (window.lucide) window.lucide.createIcons({ root: el });

        // Trigger animation
        requestAnimationFrame(() => {
            el.classList.remove('translate-y-1', 'opacity-0');
        });

        const close = () => {
            el.classList.add('opacity-0', 'scale-95');
            setTimeout(() => el.remove(), 200);
        };

        el.querySelector('button').addEventListener('click', close);
        if (duration) setTimeout(close, duration);
    },
    success(msg) { this.show(msg, 'success'); },
    error(msg) { this.show(msg, 'error'); },
    warning(msg) { this.show(msg, 'warning'); },
    info(msg) { this.show(msg, 'info'); }
};
