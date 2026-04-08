# 🧱 Базовий бекенд-план (тільки модуль №1)

**Проєкт:** FriendHub — спільний сервіс для друзів  
**Технології:** Java 17+, Spring Boot 3, PostgreSQL, JPA, Security (JWT)

---

# ## 1. Архітектура проєкту

**Stack:**

- Java 17+
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Security (JWT)
- PostgreSQL
- MapStruct
- Validation API

**Пакетна структура:**
com.friendhub
├── config
├── controller
├── dto
│ ├── request
│ └── response
├── entity
├── exception
├── mapper
├── repository
└── service
├── impl
└── interfaces

markdown
Copy code

---

# ## 2. Модуль №1 — Users & Friends (база всього проєкту)

---

# ### 2.1 Реєстрація та логін (JWT)

**Ендпоінти:**

- `POST /auth/register` — створення акаунту
- `POST /auth/login` — отримання JWT

**Валідація:**

- Email — правильний формат
- Username — унікальний
- Password — мінімум 8 символів
- Пароль хешується через BCrypt

**User поля:**

- `id`
- `username`
- `email`
- `password`
- `createdAt`
- `status` (ACTIVE / BANNED / PENDING)

---

# ### 2.2 Система друзів (Friendship)

**Механіка:**

- користувач відправляє запит у друзі
- другий приймає або відхиляє
- після accept — дружба двостороння

**Friendship поля:**

- `id`
- `requesterId`
- `receiverId`
- `status` (PENDING / ACCEPTED / REJECTED)
- `createdAt`

**Ендпоінти:**

- `POST /friends/request/{userId}`
- `POST /friends/accept/{requestId}`
- `POST /friends/reject/{requestId}`
- `GET /friends` — список друзів
- `GET /friends/requests` — всі friend requests

---

# ### 2.3 User Search

**Ендпоінт:**

- `GET /users/search?query=`

**Повертає:**

- username
- email
- статус дружби (friend / pending outgoing / pending incoming / none)

---

# ## 3. Безпека

- JWT Access Token
- (пізніше) Refresh Token
- BCrypt для паролів
- Все, що стосується friends — тільки для авторизованих
- Роль одна: `USER`

---

# ## 4. База даних (мінімальна)

**Таблиці:**
users
friendships

Copy code

Плановані модулі для майбутнього:
tasks
events
votes
comments
media
notifications
groups

yaml
Copy code

---

# ## 5. Можливість розширення

Цей модуль — фундамент. На нього легко підключити:

- Task Manager (спільні таски друзів)
- Система подій
- Голосування
- Мем-галерея
- WebSocket-чати
- Статистика
- Система нагород

---

# ✔ Готовий рухатись далі

Можу згенерувати:

- структуру проєкту зі всіма пакетами
- повні `entity + repository + controller`
- JWT-конфіг
- Swagger конфіг
- або навіть повний "стартовий" Spring Boot код