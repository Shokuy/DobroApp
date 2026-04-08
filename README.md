# ДоброРядом (DobroApp)

`ДоброРядом` — Android-приложение, где:
- пенсионеры создают заявки на бытовую помощь;
- волонтеры принимают и выполняют заявки;
- за выполнение волонтер получает `ДоброКоины`.

Проект включает:
- Android клиент (Jetpack Compose, Clean + MVVM);
- Backend API (FastAPI + SQLAlchemy + JWT);
- PostgreSQL через Docker (для production/dev) или SQLite (быстрый локальный запуск).

---

## Основные сценарии

### Пенсионер
- вход по роли + имени;
- создание заявки (тип помощи, адрес, район, время, комментарий);
- указание награды в ДоброКоинах за задачу;
- просмотр статуса своих заявок.

### Волонтер
- вход по роли + имени;
- лента открытых заявок с фильтрацией по району;
- принятие/начало/завершение заявки;
- кошелек с балансом и транзакциями;
- рейтинг, награды, профиль.

---

## Технологии

### Android
- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- Koin (DI)
- Ktor Client
- Clean + MVVM

### Backend
- Python 3.12+
- FastAPI
- SQLAlchemy 2.x
- Psycopg 3
- JWT (PyJWT)
- Docker Compose + PostgreSQL

---

## Структура проекта

```text
DobroApp/
  app/                      # Android-приложение
  backend/                  # FastAPI backend
    app/
      api/
      models/
      main.py
      db.py
    docker-compose.yml
    requirements.txt
```

---

## Быстрый запуск

## 1) Запуск backend

Есть 2 варианта.

### Вариант A: Docker + PostgreSQL (рекомендуется)

```bash
cd backend
docker compose up --build
```

API будет доступно на:
- `http://localhost:8000`
- health-check: `http://localhost:8000/health`

### Вариант B: Локально через SQLite (без Docker)

```bash
cd backend
python -m pip install -r requirements.txt
```

PowerShell (Windows):

```powershell
$env:DATABASE_URL='sqlite+pysqlite:///./dev.db'
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

---

## 2) Запуск Android-приложения

1. Открой проект `DobroApp` в Android Studio.
2. Убедись, что выбран корректный Gradle JDK (рекомендуется 17 или 21).
3. Выполни:
   - `Sync Project with Gradle Files`
   - `Build -> Rebuild Project`
4. Запусти `app` на эмуляторе/устройстве.

### Важно для эмулятора
Android-клиент обращается к backend по адресу:
- `http://10.0.2.2:8000/api/`

`10.0.2.2` — это доступ к `localhost` хоста из Android-эмулятора.

---

## Проверка, что все работает

1. Зайди как `Пенсионер` (введи имя).
2. Создай заявку и укажи награду в ДоброКоинах.
3. Выйди на главный экран.
4. Зайди как `Волонтер` (введи другое имя).
5. Прими и начни выполнение заявки.
6. Заверши заявку.
7. Открой `Кошелек` волонтера — баланс должен увеличиться.

---

## Тесты

### Android
- Unit tests: `app/src/test`
- UI tests: `app/src/androidTest`

### Backend

```bash
cd backend
python -m pytest tests -q
```

---

## Частые проблемы

### 1) Кнопки не реагируют на входе
- Проверь, что backend запущен.
- Проверь endpoint `http://localhost:8000/health`.
- Проверь, что приложение запущено на эмуляторе (не на реальном устройстве без корректного URL).

### 2) `Баланс: 0` в кошельке
- Убедись, что заявка была именно `завершена`.
- Убедись, что завершаешь в роли волонтера.
- После завершения обнови экран кошелька (выйти/зайти).

### 3) Docker не стартует
- Запусти Docker Desktop.
- Повтори `docker compose up --build`.

---

## Roadmap

- полноценная регистрация и авторизация;
- уведомления о смене статуса заявки;
- геолокация и карта;
- партнерские офферы/промокоды;
- web-admin для модерации.

---

## Лицензия

Пока не задана. При необходимости добавьте `LICENSE`.
