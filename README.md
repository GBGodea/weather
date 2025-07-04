# 🌦️ Weather API + Redis (Docker)

## 📋 Инструкция по запуску

### ⚠️ Важно!
Перед началом работы убедитесь, что установлен и запущен **Docker Desktop**  
(скачать можно с [официального сайта](https://www.docker.com/products/docker-desktop/))

---

### 🚀 Шаги для запуска

1. 🔽 **Клонируйте проект с GitHub**

   ```bash
   git clone https://github.com/your-user/weather-app.git
   cd weather-app
   ```

2. 📁 Убедитесь, что вы находитесь в **корневой директории проекта**  
   Здесь должны быть видны файлы: `Dockerfile` и `docker-compose.yml`

   ![image](https://github.com/user-attachments/assets/7c643090-e926-4fcd-8847-9b601b8be3de)

3. 🔧 Откройте консоль (можно через `cmd` в адресной строке проводника)

   ![image](https://github.com/user-attachments/assets/38beeee5-17ed-4a0e-bf17-41dee1673d50)

4. ▶️ Выполните команду:

   ```bash
   docker-compose up --build
   ```

5. ✅ После сборки и запуска:
   - приложение будет доступно на: `http://localhost:8080/weather?city=moscow`
   - Redis будет использоваться для кэширования погоды

---

## 🔗 Примеры запросов

- Получение погоды:
  ```
  GET http://localhost:8080/weather?city=moscow
  ```

- Ответ (пример):
  ```json
  {
    "city": "Moscow",
    "temperature": "21°C",
    "description": "Cloudy",
    "cached": false
  }
  ```

---

## ⚙️ Компоненты

- 🧠 Java Web-приложение (Servlet API)
- 📦 Redis (для кэширования)
- 🐳 Docker Compose

---

## 💡 Примечание

- При первом запуске данные берутся из API, затем кэшируются в Redis.
- Повторные запросы по тому же городу обрабатываются мгновенно.

