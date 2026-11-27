import { api, setAuth, loginDemoUser } from '../api.js?v=4.0';
import { toast } from './toast.js?v=4.0';

export function openAuthModal(defaultMode = 'login', initialToken = '') {
    const modalContainer = document.getElementById('modal-container');
    if (!modalContainer) return;

    modalContainer.innerHTML = `
        <div id="auth-backdrop" class="fixed inset-0 bg-neutral-900/40 flex items-center justify-center p-4 z-50">
            <div class="bg-white w-full max-w-md rounded-2xl p-6 sm:p-8 shadow-lg relative border border-neutral-200">
                <!-- Close Button -->
                <button id="auth-close-btn" class="absolute top-4 right-4 text-neutral-400 hover:text-neutral-700 transition p-1" title="Закрити">
                    <i data-lucide="x" class="w-5 h-5"></i>
                </button>

                <!-- Header -->
                <div class="text-center mb-5">
                    <div id="auth-header-icon" class="w-10 h-10 rounded-lg bg-neutral-100 flex items-center justify-center mx-auto mb-3 text-neutral-800">
                        <i data-lucide="users" id="auth-main-icon" class="w-5 h-5"></i>
                    </div>
                    <h3 id="auth-title" class="text-xl font-bold text-neutral-900 tracking-tight">Вхід у FriendsHub</h3>
                    <p id="auth-subtitle" class="text-xs text-neutral-500 mt-1">Спільний розклад та зустрічі з друзями</p>
                </div>

                <!-- Back link (for reset / new-password mode) -->
                <div id="reset-back-wrapper" class="hidden mb-4">
                    <button type="button" id="btn-back-to-login" class="text-xs text-neutral-600 hover:text-neutral-900 transition flex items-center gap-1.5 font-medium">
                        <i data-lucide="arrow-left" class="w-3.5 h-3.5"></i>
                        <span>Повернутися до входу</span>
                    </button>
                </div>

                <!-- Google OAuth Button -->
                <div id="social-auth-section" class="mb-5">
                    <a href="/oauth2/authorization/google" id="google-auth-btn" class="w-full py-2.5 px-4 rounded-lg bg-white hover:bg-neutral-50 text-neutral-800 font-medium text-xs sm:text-sm transition flex items-center justify-center gap-2.5 border border-neutral-300">
                        <svg class="w-4 h-4" viewBox="0 0 24 24">
                            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
                            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
                        </svg>
                        <span>Продовжити через Google</span>
                    </a>
                    <div class="relative my-4 flex items-center justify-center">
                        <div class="border-t border-neutral-200 w-full"></div>
                        <span class="bg-white px-3 text-[11px] text-neutral-400 uppercase tracking-wider absolute">або через логін</span>
                    </div>
                </div>

                <!-- Tabs: Login / Register -->
                <div id="auth-mode-tabs" class="flex rounded-lg bg-neutral-100 p-1 mb-5 border border-neutral-200">
                    <button id="tab-btn-login" class="flex-1 py-1.5 text-xs font-medium rounded-md transition ${defaultMode === 'login' ? 'bg-white text-neutral-900 shadow-sm font-semibold' : 'text-neutral-500 hover:text-neutral-900'}">
                        Вхід
                    </button>
                    <button id="tab-btn-register" class="flex-1 py-1.5 text-xs font-medium rounded-md transition ${defaultMode === 'register' ? 'bg-white text-neutral-900 shadow-sm font-semibold' : 'text-neutral-500 hover:text-neutral-900'}">
                        Реєстрація
                    </button>
                </div>

                <!-- Main Login / Register Form -->
                <form id="auth-form" class="space-y-3.5">
                    <div id="field-name" class="${defaultMode === 'login' ? 'hidden' : ''}">
                        <label class="block text-xs font-medium text-neutral-700 mb-1">Ваше ім'я</label>
                        <input type="text" id="input-name" placeholder="Олександр Іваненко" class="w-full px-3.5 py-2 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 focus:outline-none focus:border-neutral-900 transition text-xs sm:text-sm">
                    </div>

                    <div>
                        <label id="label-login" class="block text-xs font-medium text-neutral-700 mb-1">
                            ${defaultMode === 'login' ? 'Логін або Email' : 'Юзернейм'}
                        </label>
                        <input type="text" id="input-username" required placeholder="alex_dev" class="w-full px-3.5 py-2 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 focus:outline-none focus:border-neutral-900 transition text-xs sm:text-sm">
                    </div>

                    <div id="field-email" class="${defaultMode === 'login' ? 'hidden' : ''}">
                        <label class="block text-xs font-medium text-neutral-700 mb-1">Електронна пошта</label>
                        <input type="email" id="input-email" placeholder="alex@gmail.com" class="w-full px-3.5 py-2 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 focus:outline-none focus:border-neutral-900 transition text-xs sm:text-sm">
                    </div>

                    <div>
                        <div class="flex items-center justify-between mb-1">
                            <label class="block text-xs font-medium text-neutral-700">Пароль</label>
                            <button type="button" id="btn-forgot-password" class="text-xs text-neutral-500 hover:text-neutral-900 transition ${defaultMode === 'login' ? '' : 'hidden'}">
                                Забули пароль?
                            </button>
                        </div>
                        <div class="relative">
                            <input type="password" id="input-password" required placeholder="••••••••" class="w-full px-3.5 py-2 pr-10 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 focus:outline-none focus:border-neutral-900 transition text-xs sm:text-sm">
                            <button type="button" id="toggle-password-btn" class="absolute right-2 top-1/2 -translate-y-1/2 text-neutral-400 hover:text-neutral-700 p-1.5 transition rounded-md hover:bg-neutral-100" title="Показати/приховати пароль">
                                <i data-lucide="eye" id="toggle-password-icon" class="w-4 h-4"></i>
                            </button>
                        </div>
                    </div>

                    <button type="submit" id="auth-submit-btn" class="w-full mt-2 py-2.5 px-4 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs sm:text-sm transition flex items-center justify-center gap-2">
                        <span id="btn-text">${defaultMode === 'login' ? 'Увійти' : 'Створити акаунт'}</span>
                        <i data-lucide="arrow-right" class="w-4 h-4"></i>
                    </button>
                </form>

                <!-- Password Reset Request Section (Email only) -->
                <div id="reset-request-section" class="hidden space-y-3.5">
                    <form id="reset-request-form" class="space-y-3.5">
                        <div>
                            <label class="block text-xs font-medium text-neutral-700 mb-1">Електронна пошта акаунту</label>
                            <input type="email" id="input-reset-email" required placeholder="alex@gmail.com" class="w-full px-3.5 py-2 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 focus:outline-none focus:border-neutral-900 transition text-xs sm:text-sm">
                        </div>
                        <p class="text-xs text-neutral-500 leading-relaxed">
                            Вкажіть пошту. Ми надішлемо вам лист із персональним посиланням для встановлення нового паролю.
                        </p>
                        <button type="submit" id="btn-send-reset-email" class="w-full py-2.5 px-4 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs sm:text-sm transition flex items-center justify-center gap-2">
                            <span>Надіслати посилання на пошту</span>
                            <i data-lucide="send" class="w-4 h-4"></i>
                        </button>
                    </form>

                    <!-- Success State after email sent -->
                    <div id="reset-success-box" class="hidden text-center py-4 space-y-3.5">
                        <div class="w-12 h-12 rounded-xl bg-neutral-100 border border-neutral-200 text-neutral-800 flex items-center justify-center mx-auto">
                            <i data-lucide="mail-check" class="w-6 h-6"></i>
                        </div>
                        <div>
                            <h4 class="text-base font-semibold text-neutral-900 mb-1">Лист надіслано</h4>
                            <p id="reset-success-email-text" class="text-xs text-neutral-600 leading-relaxed max-w-xs mx-auto">
                                Ми надіслали лист із посиланням на вашу пошту.
                            </p>
                        </div>
                        <div class="p-3 rounded-lg bg-neutral-50 border border-neutral-200 text-xs text-neutral-600 text-left leading-relaxed">
                            Відкрийте пошту та перейдіть за посиланням у листі для зміни паролю.
                        </div>
                        <button type="button" id="btn-reset-done" class="w-full py-2 px-4 rounded-lg bg-white hover:bg-neutral-50 text-neutral-800 font-medium text-xs border border-neutral-300 transition">
                            Повернутися до входу
                        </button>
                    </div>
                </div>

                <!-- Set New Password Section (Opened when following email link) -->
                <div id="set-new-password-section" class="hidden space-y-3.5">
                    <form id="set-new-password-form" class="space-y-3.5">
                        <div>
                            <label class="block text-xs font-medium text-neutral-700 mb-1">Новий пароль</label>
                            <div class="relative">
                                <input type="password" id="input-new-pwd" required placeholder="••••••••" class="w-full px-3.5 py-2 pr-10 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 focus:outline-none focus:border-neutral-900 transition text-xs sm:text-sm">
                                <button type="button" id="toggle-new-pwd-btn" class="absolute right-2 top-1/2 -translate-y-1/2 text-neutral-400 hover:text-neutral-700 p-1.5 transition rounded-md hover:bg-neutral-100" title="Показати/приховати пароль">
                                    <i data-lucide="eye" id="toggle-new-pwd-icon" class="w-4 h-4"></i>
                                </button>
                            </div>
                        </div>

                        <div>
                            <label class="block text-xs font-medium text-neutral-700 mb-1">Повторіть новий пароль</label>
                            <div class="relative">
                                <input type="password" id="input-confirm-new-pwd" required placeholder="••••••••" class="w-full px-3.5 py-2 pr-10 rounded-lg bg-white border border-neutral-300 text-neutral-900 placeholder-neutral-400 focus:outline-none focus:border-neutral-900 transition text-xs sm:text-sm">
                                <button type="button" id="toggle-confirm-pwd-btn" class="absolute right-2 top-1/2 -translate-y-1/2 text-neutral-400 hover:text-neutral-700 p-1.5 transition rounded-md hover:bg-neutral-100" title="Показати/приховати пароль">
                                    <i data-lucide="eye" id="toggle-confirm-pwd-icon" class="w-4 h-4"></i>
                                </button>
                            </div>
                        </div>

                        <button type="submit" id="btn-save-new-password" class="w-full py-2.5 px-4 rounded-lg bg-neutral-900 hover:bg-neutral-800 text-white font-medium text-xs sm:text-sm transition flex items-center justify-center gap-2">
                            <span>Зберегти новий пароль</span>
                            <i data-lucide="check" class="w-4 h-4"></i>
                        </button>
                    </form>
                </div>

                <!-- Demo quick entrance -->
                <div id="demo-section" class="mt-5 pt-4 border-t border-neutral-200 text-center">
                    <p class="text-xs text-neutral-500 mb-2">Швидке ознайомлення без реєстрації:</p>
                    <button id="quick-demo-btn" class="px-3.5 py-2 rounded-lg bg-neutral-100 hover:bg-neutral-200 text-xs font-medium text-neutral-800 border border-neutral-200 transition flex items-center justify-center gap-1.5 mx-auto">
                        <i data-lucide="play" class="w-3.5 h-3.5"></i>
                        <span>Увійти в Демо-режим</span>
                    </button>
                </div>
            </div>
        </div>
    `;

    if (window.lucide) window.lucide.createIcons();

    let currentMode = defaultMode;
    let activeResetToken = initialToken;

    const setupPasswordToggle = (btnId, inputId) => {
        const btn = document.getElementById(btnId);
        const input = document.getElementById(inputId);
        if (!btn || !input) return;

        btn.onclick = (e) => {
            e.preventDefault();
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            const icon = btn.querySelector('i');
            if (icon) {
                icon.setAttribute('data-lucide', isPassword ? 'eye-off' : 'eye');
                if (window.lucide) window.lucide.createIcons();
            }
        };
    };

    setupPasswordToggle('toggle-password-btn', 'input-password');
    setupPasswordToggle('toggle-new-pwd-btn', 'input-new-pwd');
    setupPasswordToggle('toggle-confirm-pwd-btn', 'input-confirm-new-pwd');

    const setMode = (mode) => {
        currentMode = mode;
        const isLogin = mode === 'login';
        const isRegister = mode === 'register';
        const isReset = mode === 'reset';
        const isSetNewPassword = mode === 'set-new-password';

        const tabs = document.getElementById('auth-mode-tabs');
        const socialSection = document.getElementById('social-auth-section');
        const authForm = document.getElementById('auth-form');
        const resetSection = document.getElementById('reset-request-section');
        const setNewPwdSection = document.getElementById('set-new-password-section');
        const demoSection = document.getElementById('demo-section');
        const resetBack = document.getElementById('reset-back-wrapper');
        const forgotPasswordBtn = document.getElementById('btn-forgot-password');
        const mainIcon = document.getElementById('auth-main-icon');

        if (isReset) {
            tabs.classList.add('hidden');
            socialSection.classList.add('hidden');
            authForm.classList.add('hidden');
            resetSection.classList.remove('hidden');
            setNewPwdSection.classList.add('hidden');
            demoSection.classList.add('hidden');
            resetBack.classList.remove('hidden');

            document.getElementById('auth-title').innerText = 'Відновлення паролю';
            document.getElementById('auth-subtitle').innerText = 'Посилання для зміни паролю буде надіслано на email';
            if (mainIcon) mainIcon.setAttribute('data-lucide', 'mail');

            document.getElementById('reset-request-form').classList.remove('hidden');
            document.getElementById('reset-success-box').classList.add('hidden');
        } else if (isSetNewPassword) {
            tabs.classList.add('hidden');
            socialSection.classList.add('hidden');
            authForm.classList.add('hidden');
            resetSection.classList.add('hidden');
            setNewPwdSection.classList.remove('hidden');
            demoSection.classList.add('hidden');
            resetBack.classList.remove('hidden');

            document.getElementById('auth-title').innerText = 'Встановлення нового пароля';
            document.getElementById('auth-subtitle').innerText = 'Введіть новий пароль для вашого акаунту';
            if (mainIcon) mainIcon.setAttribute('data-lucide', 'shield-check');
        } else {
            tabs.classList.remove('hidden');
            socialSection.classList.remove('hidden');
            authForm.classList.remove('hidden');
            resetSection.classList.add('hidden');
            setNewPwdSection.classList.add('hidden');
            demoSection.classList.remove('hidden');
            resetBack.classList.add('hidden');

            document.getElementById('tab-btn-login').className = `flex-1 py-1.5 text-xs font-medium rounded-md transition ${isLogin ? 'bg-white text-neutral-900 shadow-sm font-semibold' : 'text-neutral-500 hover:text-neutral-900'}`;
            document.getElementById('tab-btn-register').className = `flex-1 py-1.5 text-xs font-medium rounded-md transition ${isRegister ? 'bg-white text-neutral-900 shadow-sm font-semibold' : 'text-neutral-500 hover:text-neutral-900'}`;
            document.getElementById('auth-title').innerText = isLogin ? 'Вхід у FriendsHub' : 'Реєстрація у FriendsHub';
            document.getElementById('auth-subtitle').innerText = 'Спільний розклад та зустрічі з друзями';
            document.getElementById('label-login').innerText = isLogin ? 'Логін або Email' : 'Юзернейм';
            document.getElementById('btn-text').innerText = isLogin ? 'Увійти' : 'Створити акаунт';

            document.getElementById('field-name').classList.toggle('hidden', isLogin);
            document.getElementById('field-email').classList.toggle('hidden', isLogin);
            if (forgotPasswordBtn) forgotPasswordBtn.classList.toggle('hidden', !isLogin);

            if (mainIcon) mainIcon.setAttribute('data-lucide', 'users');
        }

        if (window.lucide) window.lucide.createIcons();
    };

    document.getElementById('tab-btn-login').onclick = () => setMode('login');
    document.getElementById('tab-btn-register').onclick = () => setMode('register');
    document.getElementById('btn-forgot-password').onclick = () => setMode('reset');
    document.getElementById('btn-back-to-login').onclick = () => setMode('login');
    document.getElementById('btn-reset-done').onclick = () => setMode('login');

    const closeModal = () => {
        modalContainer.innerHTML = '';
    };

    document.getElementById('auth-close-btn').onclick = closeModal;
    document.getElementById('auth-backdrop').onclick = (e) => {
        if (e.target.id === 'auth-backdrop') closeModal();
    };

    // Quick demo entrance
    document.getElementById('quick-demo-btn').onclick = () => {
        loginDemoUser();
        toast.success("Вхід у демонстраційний режим виконано");
        closeModal();
    };

    // Submit handler: Login & Register
    document.getElementById('auth-form').onsubmit = async (e) => {
        e.preventDefault();
        const submitBtn = document.getElementById('auth-submit-btn');
        submitBtn.disabled = true;
        submitBtn.classList.add('opacity-75');

        const username = document.getElementById('input-username').value.trim();
        const password = document.getElementById('input-password').value;

        try {
            if (currentMode === 'login') {
                const res = await api.auth.login({ login: username, password });
                if (res && res.token) {
                    setAuth(res.token, res.user);
                }
                toast.success(`Вітаємо, ${res.user?.name || res.user?.username || username}`);
            } else {
                const name = document.getElementById('input-name').value.trim() || username;
                const email = document.getElementById('input-email').value.trim();
                const res = await api.auth.register({ username, name, email, password });
                if (res && res.token) {
                    setAuth(res.token, res.user);
                }
                toast.success(`Акаунт створено. Ласкаво просимо, ${name}`);
            }
            closeModal();
            window.dispatchEvent(new CustomEvent('auth-changed'));
        } catch (err) {
            toast.error(err.message || 'Помилка під час авторизації');
        } finally {
            submitBtn.disabled = false;
            submitBtn.classList.remove('opacity-75');
        }
    };

    // Submit handler: Reset Password Request (send email link)
    document.getElementById('reset-request-form').onsubmit = async (e) => {
        e.preventDefault();
        const btn = document.getElementById('btn-send-reset-email');
        btn.disabled = true;
        btn.classList.add('opacity-75');

        const email = document.getElementById('input-reset-email').value.trim();

        try {
            await api.auth.requestPasswordReset({ email });
            toast.success("Посилання для відновлення надіслано на вашу пошту");
            document.getElementById('reset-request-form').classList.add('hidden');
            const successBox = document.getElementById('reset-success-box');
            document.getElementById('reset-success-email-text').innerHTML = `Ми надіслали лист із посиланням на адресу <span class="font-semibold text-neutral-900">${email}</span>.`;
            successBox.classList.remove('hidden');
            if (window.lucide) window.lucide.createIcons();
        } catch (err) {
            toast.error(err.message || 'Не вдалося надіслати запит на скидання паролю');
        } finally {
            btn.disabled = false;
            btn.classList.remove('opacity-75');
        }
    };

    // Submit handler: Set New Password (called after user clicks email link)
    document.getElementById('set-new-password-form').onsubmit = async (e) => {
        e.preventDefault();
        const btn = document.getElementById('btn-save-new-password');
        const pwd1 = document.getElementById('input-new-pwd').value;
        const pwd2 = document.getElementById('input-confirm-new-pwd').value;

        if (pwd1 !== pwd2) {
            toast.error("Введені паролі не співпадають");
            return;
        }

        if (pwd1.length < 6) {
            toast.error("Пароль повинен містити щонайменше 6 символів");
            return;
        }

        btn.disabled = true;
        btn.classList.add('opacity-75');

        try {
            await api.confirmPasswordReset({ token: activeResetToken, newPassword: pwd1 });
            toast.success("Пароль успішно змінено. Тепер ви можете увійти з новим паролем.");
            setMode('login');
            const pwdInput = document.getElementById('input-password');
            if (pwdInput) pwdInput.value = pwd1;
        } catch (err) {
            toast.error(err.message || 'Помилка скидання паролю. Посилання недійсне або застаріло.');
        } finally {
            btn.disabled = false;
            btn.classList.remove('opacity-75');
        }
    };

    if (defaultMode === 'set-new-password' || (defaultMode === 'reset' && initialToken)) {
        activeResetToken = initialToken;
        setMode('set-new-password');
    } else if (defaultMode === 'reset') {
        setMode('reset');
    }
}
