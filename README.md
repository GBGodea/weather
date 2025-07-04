📋Инструкция:

ВАЖНО!
**Перед началом работы скачайте или включите docker desktop, чтобы docker смог собрать контейнер**

1. Копируем проект с github
2. открываем проект в корне(чтобы были видны dockerfile и docker-compose)
![image](https://github.com/user-attachments/assets/26ff7a14-41e9-44aa-bc9e-e35740275a93)
3. Далее открывает консоль при помощи cmd и пишем команду **docker-compose up --build**
![image](https://github.com/user-attachments/assets/caecd54b-896f-4a1c-a608-762d6e70483e)
4. После этого у вас соберётся docker контейнер с настроенным redis и weather приложением
5. Можно отправлять запросы, например по адресу **http://localhost:8080/weather?city=moscow**
