# RealMatch — как запустить всю систему

Порядок: **PostgreSQL → бэкенд → эмулятор/приложение**.

## 1. База данных (PostgreSQL)

Нужен установленный PostgreSQL (pgAdmin или psql) и база `realmatch`.

1. Создать базу `realmatch` (pgAdmin → Databases → Create; или `createdb realmatch`).
2. Выполнить схему: `documentation/DB/физическая.sql`.
3. Выполнить тестовые данные: `documentation/DB/тестовые данные(над ХЭШИ паролей поправить).sql`.
4. Проверить строку подключения: `documentation/DB/connection_string.txt`
   (она же используется в `backend/Backend/.env` → `DATABASE_URL`).

Если база уже настроена, этот шаг пропускается.

## 2. Бэкенд (FastAPI)

Терминал:

```powershell
cd D:\Course_project\backend\Backend
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Остановить — `Ctrl+C`. С автоперезапуском при правках добавь `--reload`.

Проверка:
- `http://127.0.0.1:8000/health` → `{"status":"healthy","database":"connected"}`
- `http://127.0.0.1:8000/docs` → Swagger

Если `.venv` нет:

```powershell
python -m venv .venv
.\.venv\Scripts\pip install -r requirements.txt
```

Фото раздаются из `backend/Backend/media` по URL `/media/...`.

## 3. Эмулятор и приложение

### Эмулятор

Android Studio → **Device Manager** → `Medium_Phone_API_37.0` → ▶
(или в терминале `D:\SDK\emulator\emulator.exe -avd Medium_Phone_API_37.0`).

AVD хранится в `D:\AndroidAvd`, ссылка на него — в `C:\Users\Redmi\.android\avd`.

### Приложение

Открыть в Android Studio папку `D:\Course_project\frontend`, нажать **Run 'app'** (Shift+F10).

Либо из терминала:

```powershell
cd D:\Course_project\frontend
.\gradlew.bat assembleDebug
D:\SDK\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
D:\SDK\platform-tools\adb.exe shell am start -n com.example.realmatch/.MainActivity
```

Для сборки из терминала нужна JDK 25 в `JAVA_HOME` (подойдёт JDK из Android Studio: `...\Android Studio\jbr`).

Адрес бэкенда по умолчанию — `http://10.0.2.2:8000` (это компьютер с точки зрения эмулятора).

### Реальный телефон

1. Телефон и компьютер в одной Wi-Fi сети, на телефоне включена отладка по USB.
2. Узнать IP компьютера: `ipconfig`.
3. В `frontend/local.properties` добавить:
   ```properties
   RM_HOST=http://<IP-компьютера>:8000
   ```
   (например, `http://192.168.1.189:8000`) и пересобрать приложение.
4. При первом запуске разрешить доступ к устройствам рядом (Android 17), иначе `10.0.2.2`/LAN будут в таймауте.
5. При необходимости разрешить порт 8000 в брандмауэре Windows.

## Тестовые аккаунты

Вход пользователя — через «Войти как Пользователь» (имитация Госуслуг):

| Логин | Пароль | Кто |
|---|---|---|
| `login_1` | `123` | Иван Иванов (много данных: лента, метчи, чаты, промокод) |
| `login_4` | `123` | Мария Кузнецова (3 фото) |
| `login_6` | `123` | Елена Васильева (2 фото) |
| `login_14` | `123` | Юлия (принятый инвайт с Иваном) |
| `new_user2` | `123` | пустой профиль для показа онбординга |

Остальные пользователи — `login_2` … `login_15`, пароль `123`.

Партнёр — «Вход для Партнеров»: `cafe_1` / `123` (Кафе «Ромашка»).

## Что проверить после запуска

1. Лента (свайпы, листание фото стрелками).
2. Метчи → карточка пользователя.
3. Чаты: переписка, эмодзи, опрос каждые 4 секунды.
4. Иконка подарка в чате → офферы (с городом и адресом) → «Предложить» → приглашение в чате.
5. Профиль: фото, город, «о себе», выход.
6. Выход → вход партнёра → кабинет: акции (создание/редактирование/завершение).

## Частые проблемы

- **«Нет соединения с сервером»** — не запущен бэкенд, либо для телефона неверный `RM_HOST`, либо не выдано разрешение «Устройства рядом».
- **Порт 8000 занят** — `Get-NetTCPConnection -LocalPort 8000` и завершить процесс, либо сменить порт и `RM_HOST`.
- **Сборка падает по памяти** — закрыть эмулятор перед сборкой (он занимает 3–4 ГБ).
- **Фото 404** — проверить, что файлы лежат в `backend/Backend/media`; соответствие «логин → файлы» — `documentation/DB/фото_профилей.md`.
- **401 / «Сессия истекла»** — войти заново (токен пользователя живёт 7 дней, партнёра — 24 часа).

## Документация

- `documentation/shared/API.md` — API.
- `documentation/shared/Общая логика работы.txt` — сценарии.
- `documentation/frontend/архитектура проекта.md`, `план разработки.md`.
- `documentation/frontend/UI_reference` — макеты Figma.
- `documentation/DB/фото_профилей.md` — фото тестовых профилей.
