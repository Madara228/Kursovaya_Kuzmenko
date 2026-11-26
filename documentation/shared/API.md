
# RealMatch API Documentation

## Общая информация
*   **Базовый URL (Dev - пока что на http):** `http://127.0.0.1:8000` (на ANDROD надо будет настроить)
*   **Префикс API:** `/api`
*   **Префикс статики (медиа):** `/media` (раздается без префикса `/api`)
*   **Формат данных:** `application/json` (за исключением эндпоинтов с загрузкой файлов, где используется `multipart/form-data`)
*   **Формат дат:** ISO 8601 (например, `2023-11-01T15:00:00Z`)
*   **Авторизация:** JWT (Bearer Token). Передается в заголовке: `Authorization: Bearer <access_token>`
*   **Сроки жизни токенов:** Пользователи — 7 дней (604800 секунд), Партнеры — 24 часа (86400 секунд).

### Формат пагинации (для GET-запросов списков)
Все эндпоинты, возвращающие списки, поддерживают query-параметры:
*   `page` (int, по умолчанию 1) — номер страницы.
*   `limit` (int, по умолчанию 20, макс 100) — записей на странице.
*   `sort` (string) — поле сортировки (например, `-created_at` для убывания).

**Пример ответа для списка (200 OK):**
```json
{
  "data": [],
  "meta": {
    "total_items": 0,
    "total_pages": 0,
    "current_page": 1,
    "per_page": 20
  }
}
```

### Формат ошибок
```json
{
  "error": "Conflict",
  "message": "Описание ошибки",
  "code": 409
}
```

---

## 1. Аутентификация

### 1.1. Вход через имитатор ЕСИА
`POST /auth/esia-login`
*   **Права доступа:** Публичный
*   **Описание:** Принимает логин и пароль от имитатора Госуслуг. Если пользователя нет в `users` (понимаем это по `esia_id`), создает его. Возвращает JWT.
*   **Запрос (Request):**
    ```json
    {
      "login": "ivanov_esia",
      "password": "secretpassword"
    }
    ```
*   **Ответ (Response 200 OK):**
    ```json
    {
      "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "token_type": "bearer",
      "expires_in": 604800,
      "user": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "first_name": "Иван",
        "last_name": "Иванов",
        "is_new_user": true
      }
    }
    ```

### 1.2. Вход для партнеров
`POST /auth/partner-login`
*   **Права доступа:** Публичный
*   **Описание:** Авторизация партнерских аккаунтов по логину и паролю. Возвращает JWT.
*   **Запрос (Request):**
    ```json
    {
      "login": "cafe_romashka",
      "password": "partner_password"
    }
    ```
*   **Ответ (Response 200 OK):**
    ```json
    {
      "access_token": "eyJhbGciOiJIUzI1Ni...",
      "token_type": "bearer",
      "expires_in": 86400,
      "partner_id": "880e8400-e29b-41d4-a716-446655440000"
    }
    ```

---

## 2. Профиль Пользователя (Users)

### 2.1. Получить свой профиль
`GET /users/me`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** ID пользователя берется из JWT.
*   **Ответ (Response 200 OK):**
    ```json
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "esia_id": "123e4567-e89b-12d3-a456-426614174000",
      "first_name": "Иван",
      "last_name": "Иванов",
      "birth_date": "2000-05-20",
      "gender": "M",
      "city": "Москва",
      "bio": "Люблю программировать и котиков",
      "photos": [
        {"id": "uuid1", "url": "/media/user_1/1.jpg", "is_main": true},
        {"id": "uuid2", "url": "/media/user_1/2.jpg", "is_main": false}
      ]
    }
    ```

### 2.2. Обновить свой профиль
`PATCH /users/me`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** Изменение только тех полей, которые пользователь вводит сам (город, о себе).
*   **Запрос (Request):**
    ```json
    {
      "city": "Санкт-Петербург",
      "bio": "Новый текст о себе"
    }
    ```
*   **Ответ (Response 200 OK):** Возвращает объект пользователя (аналогично 2.1).

### 2.3. Получить профиль другого пользователя
`GET /users/{user_id}`
*   **Права доступа:** Любой пользователь (JWT)
*   **Описание:** Возвращает профиль пользователя для просмотра. Конфиденциальные данные (esia_id) скрыты.
*   **Ответ (Response 200 OK):**
    ```json
    {
      "id": "uuid-пользователя",
      "first_name": "Мария",
      "last_name": "Петрова",
      "birth_date": "2001-02-15",
      "gender": "F",
      "city": "Москва",
      "bio": "Люблю котиков",
      "photos": [
        {"id": "uuid", "url": "/media/user_2/1.jpg", "is_main": true}
      ]
    }
    ```

---

## 3. Фотографии пользователя (Photos)

### 3.1. Загрузить фотографию
`POST /photos`
*   **Права доступа:** Пользователь (JWT)
*   **Формат:** `multipart/form-data`
*   **Описание:** Максимум 6 фото. Если первое фото, `is_main` ставится `TRUE` автоматически.
*   **Поля запроса (Form-Data):**
    *   `file` (File, обязательный): Изображение (jpg/png).
    *   `is_main` (Boolean, опциональный): Сделать ли фото главным (по умолчанию `false`).
*   **Ответ (Response 201 Created):**
    ```json
    {
      "id": "uuid-new-photo",
      "url": "/media/user_1/photo_3.jpg",
      "is_main": false
    }
    ```

### 3.2. Сделать фото главным
`PATCH /photos/{photo_id}/main`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** Снимает флаг `is_main` со старого фото и ставит на текущее.
*   **Ответ (Response 200 OK):**
    ```json
    {
      "message": "Main photo updated"
    }
    ```

### 3.3. Удалить фотографию
`DELETE /photos/{photo_id}`
*   **Права доступа:** Пользователь (JWT)
*   **Ответ (Response 200 OK):**
    ```json
    {
      "message": "Photo deleted successfully"
    }
    ```

---

## 4. Лента и Свайпы (Feed & Contacts)

### 4.1. Получить ленту анкет
`GET /feed`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** Возвращает список анкет, которых пользователь еще не лайкал/дизлайкал. С пагинацией.
*   **Ответ (Response 200 OK):**
    ```json
    {
      "data": [
        {
          "id": "uuid-пользователя-1",
          "first_name": "Анна",
          "birth_date": "2002-08-10",
          "city": "Москва",
          "bio": "Привет!",
          "photos": [{"id": "uuid", "url": "/media/...", "is_main": true}]
        }
      ],
      "meta": { "total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20 }
    }
    ```

### 4.2. Поставить лайк/дизлайк (Свайп)
`POST /contacts/swipe`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** Если `LIKE` и есть встречный лайк — статус `MATCHED`. Если просто `LIKE` — `PENDING`. Если `DISLIKE` — `REJECTED`.
*   **Запрос (Request):**
    ```json
    {
      "target_user_id": "uuid-пользователя-кого-лайкнули",
      "action": "LIKE"
    }
    ```
*   **Ответ (Response 200 OK):**
    ```json
    {
      "status": "MATCHED",
      "is_match": true
    }
    ```

---

## 5. Мэтчи и Чаты (Matches & Chats)

### 5.1. Получить список мэтчей
`GET /matches`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** Список пользователей со взаимной симпатией (status = MATCHED).
*   **Ответ (Response 200 OK):**
    ```json
    {
      "data": [
        {
          "id": "uuid-мэтча",
          "user": {
            "id": "uuid-пользователя",
            "first_name": "Мария",
            "photos": [{"id": "uuid", "url": "/media/...", "is_main": true}]
          }
        }
      ],
      "meta": { "total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20 }
    }
    ```

### 5.2. Получить список чатов
`GET /chats`
*   **Права доступа:** Пользователь (JWT)
*   **Ответ (Response 200 OK):**
    ```json
    {
      "data": [
        {
          "contact_initiator_id": "uuid-1",
          "contact_responder_id": "uuid-2",
          "other_user": {
            "id": "uuid-2", 
            "first_name": "Мария", 
            "main_photo": "/media/user_2/1.jpg"
          },
          "last_message": "Привет! Как дела?",
          "last_message_time": "2023-11-01T15:00:00Z",
          "unread_count": 2
        }
      ],
      "meta": { "total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20 }
    }
    ```

### 5.3. Получить историю сообщений
`GET /chats/{contact_initiator_id}/{contact_responder_id}/messages`
*   **Права доступа:** Пользователь (JWT)
*   **Ответ (Response 200 OK):**
    ```json
    {
      "data": [
        {
          "author_id": "uuid-1",
          "text": "Привет!",
          "is_read": true,
          "created_at": "2023-11-01T14:55:00Z"
        }
      ],
      "meta": { "total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20 }
    }
    ```

### 5.4. Отправить сообщение
`POST /chats/{contact_initiator_id}/{contact_responder_id}/messages`
*   **Права доступа:** Пользователь (JWT)
*   **Запрос (Request):**
    ```json
    {
      "text": "Привет! Предлагаю сходить в кафе."
    }
    ```
*   **Ответ (Response 201 Created):**
    ```json
    {
      "author_id": "uuid-текущего-пользователя",
      "text": "Привет! Предлагаю сходить в кафе.",
      "is_read": false,
      "created_at": "2023-11-01T15:00:00Z"
    }
    ```

### 5.5. Отметить сообщения как прочитанные
`PATCH /chats/{contact_initiator_id}/{contact_responder_id}/read`
*   **Права доступа:** Пользователь (JWT)
*   **Ответ (Response 200 OK):**
    ```json
    {
      "status": "success",
      "updated_count": 5
    }
    ```

---

## 6. Инвайты и Промокоды (Invites)

### 6.1. Создать инвайт (Ссылку)
`POST /invites`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** Генерирует JWT-токен (7 дней), создает сообщение в чате с текстом и ссылкой.
*   **Запрос (Request):**
    ```json
    {
      "offer_id": "uuid-спецпредложения",
      "contact_initiator_id": "uuid-1",
      "contact_responder_id": "uuid-2"
    }
    ```
*   **Ответ (Response 201 Created):**
    ```json
    {
      "invite_link": "/api/invites/eyJhbGciOi...",
      "message_id": "uuid-сообщения"
    }
    ```

### 6.2. Проверить ссылку-приглашение
`GET /invites/{token}`
*   **Права доступа:** Публичный
*   **Описание:** Возвращает инфу о предложении, чтобы пользователь Б видел, куда его зовут.
*   **Ответ (Response 200 OK):**
    ```json
    {
      "offer_title": "Скидка 20% на кофе",
      "partner_name": "Кафе Ромашка",
      "initiator_name": "Иван",
      "is_valid": true
    }
    ```

### 6.3. Принять приглашение
`POST /invites/{token}/accept`
*   **Права доступа:** Пользователь (JWT)
*   **Описание:** Генерирует промокод, делит пополам, шлет 2 сообщения в чат.
*   **Ответ (Response 200 OK):**
    ```json
    {
      "status": "SUCCESS",
      "promocode_id": "uuid-промокода"
    }
    ```

---

## 7. Партнеры (Partners)

### 7.1. Получить предложения для дейтинга
`GET /offers`
*   **Права доступа:** Пользователь (JWT)
*   **Ответ (Response 200 OK):**
    ```json
    {
      "data": [
        {
          "id": "uuid-оффера",
          "partner_id": "uuid-партнера",
          "title": "Скидка 20% на кофе",
          "discount_info": "Покажите промокод бариста",
          "valid_until": "2024-12-31",
          "photo_url": "/media/offers/uuid/main.jpg",
          "partner": {
            "name": "Кафе Ромашка",
            "city": "Москва",
            "address": "ул. Ленина, 1"
          }
        }
      ],
      "meta": { "total_items": 1, "total_pages": 1, "current_page": 1, "per_page": 20 }
    }
    ```

### 7.2. Получить свой профиль (Партнер)
`GET /partners/me`
*   **Права доступа:** Партнер (JWT)
*   **Ответ (Response 200 OK):**
    ```json
    {
      "id": "uuid-партнера",
      "login": "cafe_romashka",
      "contact_person": "Иванов Иван",
      "phone": "+79991234567",
      "name": "Кафе Ромашка",
      "city": "Москва",
      "address": "ул. Ленина, 1",
      "contact_info": "vk.com/romashka"
    }
    ```

### 7.3. Создать акцию (Партнер)
`POST /partners/me/offers`
*   **Права доступа:** Партнер (JWT)
*   **Формат:** `multipart/form-data`
*   **Поля запроса (Form-Data):**
    *   `title` (String, обязательный)
    *   `discount_info` (String, обязательный)
    *   `valid_until` (String, обязательный): Формат YYYY-MM-DD.
    *   `photo` (File, обязательный): jpg/png.
*   **Ответ (Response 201 Created):**
    ```json
    {
      "id": "uuid-новой-акции",
      "partner_id": "uuid-партнера",
      "title": "Скидка 20% на кофе",
      "discount_info": "Покажите промокод бариста",
      "valid_until": "2024-12-31",
      "photo_url": "/media/offers/uuid-новой-акции/main.jpg"
    }
    ```

### 7.4. Получить свои акции
`GET /partners/me/offers`
*   **Права доступа:** Партнер (JWT)
*   **Ответ (Response 200 OK):** Массив объектов (структура как в 7.3).

### 7.5. Обновить акцию (Партнер)
`PATCH /partners/me/offers/{offer_id}`
*   **Права доступа:** Партнер (JWT)
*   **Формат:** `multipart/form-data`
*   **Поля запроса (Form-Data):**
    *   `title` (String, опционально)
    *   `discount_info` (String, опционально)
    *   `valid_until` (String, опционально)
    *   `photo` (File, опционально): Если передан, старое фото перезаписывается.
*   **Ответ (Response 200 OK):** Обновленный объект акции.

### 7.6. Деактивировать акцию
`DELETE /partners/me/offers/{offer_id}`
*   **Права доступа:** Партнер (JWT)
*   **Описание:** Устанавливает `valid_until = CURRENT_DATE`.
*   **Ответ (Response 200 OK):**
    ```json
    {
      "message": "Offer deactivated successfully"
    }
    ```

---

## HTTP Статус-коды и Ошибки

| HTTP Код | Название | Описание |
| :--- | :--- | :--- |
| **400** | Bad Request | Ошибка валидации (Pydantic не пропустил). |
| **401** | Unauthorized | Нет токена, или он просрочен. |
| **403** | Forbidden | Нет прав (чужое фото, чужой оффер). |
| **404** | Not Found | Ресурс не найден. |
| **409** | Conflict | Конфликт данных (самолайк, дубль контакта). |
| **422** | Unprocessable Entity | Семантическая ошибка в теле запроса. |

**Специфичные ошибки бизнес-логики (возвращаются с соответствующим HTTP кодом):**
*   `INVALID_ESIA_CREDENTIALS`: Неверный логин/пароль ЕСИА.
*   `USER_UNDER_18`: Пользователю нет 18 лет.
*   `USER_BLOCKED`: Аккаунт заблокирован.
*   `SELF_SWIPE_NOT_ALLOWED`: Попытка свайпнуть себя.
*   `CONTACT_ALREADY_EXISTS`: Уже есть контакт с этим пользователем.
*   `PHOTO_LIMIT_REACHED`: Больше 6 фото.
*   `INVITE_EXPIRED`: Ссылка-приглашение просрочена.
*   `INVITE_ALREADY_ACCEPTED`: Промокод уже сгенерирован.
*   `WRONG_RESPONDER`: Попытка принять чужой инвайт.
*   `PARTNER_ACCESS_REQUIRED`: Попытка клиента зайти в ручки партнера.

