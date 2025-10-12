# TaskManager API

##  Основные функции

-  Регистрация и аутентификация пользователей
-  CRUD операции для пользователей и задач
-  Автоматическое назначение задач доступным пользователям
-  Контроль рабочей нагрузки (максимум 3 активные задачи на пользователя)
-  Автоматическое изменение статуса пользователя (AVAILABLE/BUSY)
-  Управление статусом выполнения задач (done/undone)
-  Фильтрация задач по приоритету и исполнителю

##  Установка и настройка

### Предварительные требования

- Java 21+
- PostgreSQL 12+
- Maven 3.6+

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd TaskManager
```

### 2. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE taskmanager;
CREATE USER taskmanager_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO taskmanager_user;
```

### 3. Конфигурация приложения

Отредактируйте файл `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=taskmanager_user
spring.datasource.password=your_password

### 4. Сборка и запуск

```bash
# Сборка проекта
mvn clean install

# Запуск приложения
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

##  API Документация

### Аутентификация

#### Регистрация пользователя
```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "username",
    "email": "user@example.com",
    "password": "Password123!"
}
```

#### Вход в систему
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "username",
    "password": "Password123!"
}
```

**Ответ:**
```json
{
    "token": "jwt-token-here",
    "username": "username",
    "email": "user@example.com",
    "userId": 1
}
```

### Управление пользователями

#### Получить всех пользователей
```http
GET /api/users
Authorization: Bearer <token>
```

#### Получить пользователя по ID
```http
GET /api/users/{id}
Authorization: Bearer <token>
```

#### Обновить пользователя
```http
PUT /api/users/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
    "username": "new_username",
    "email": "new@example.com",
    "password": "NewPassword123!"
}
```

#### Удалить пользователя
```http
DELETE /api/users/{id}
Authorization: Bearer <token>
```

#### Получить доступных пользователей
```http
GET /api/users/available
Authorization: Bearer <token>
```

### Управление задачами

#### Создать задачу (автоназначение)
```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
    "title": "Название задачи",
    "description": "Описание задачи",
    "priorityLevel": "HIGH"
}
```

**Приоритеты:** `LOW`, `MEDIUM`, `HIGH`, `URGENT`

#### Получить все задачи
```http
GET /api/tasks
Authorization: Bearer <token>
```

#### Получить задачу по ID
```http
GET /api/tasks/{id}
Authorization: Bearer <token>
```

#### Обновить задачу (только название и описание)
```http
PUT /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
    "title": "Обновленное название",
    "description": "Обновленное описание"
}
```

#### Отметить задачу как выполненную/невыполненную
```http
PUT /api/tasks/{id}/done
Authorization: Bearer <token>
Content-Type: application/json

{
    "done": true
}
```

#### Удалить задачу
```http
DELETE /api/tasks/{id}
Authorization: Bearer <token>
```

#### Получить задачи по приоритету
```http
GET /api/tasks/priority/{priorityLevel}
Authorization: Bearer <token>
```

#### Получить задачи исполнителя
```http
GET /api/tasks/assignee/{userId}
Authorization: Bearer <token>
```

#### Переназначить задачу
```http
POST /api/tasks/{taskId}/assign
Authorization: Bearer <token>
```

##  Бизнес-логика

### Автоматическое назначение задач

1. При создании задачи система автоматически:
   - Ищет доступных пользователей (статус `AVAILABLE`)
   - Выбирает пользователя с менее чем 3 активными задачами
   - Назначает задачу этому пользователю
   - Если у пользователя становится 3 активные задачи, меняет его статус на `BUSY`

### Управление статусом пользователей

- **AVAILABLE** - пользователь может получать новые задачи (< 3 активных)
- **BUSY** - пользователь занят (≥ 3 активных задач)
- **OFFLINE** - пользователь недоступен

### Пересчет статуса

Статус пользователя автоматически пересчитывается при:
- Завершении задачи (done = true)
- Удалении задачи
- Переназначении задачи
