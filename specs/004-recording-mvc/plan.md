# Implementation Plan: Вебкерування записами дзвінків за MVC

**Branch**: `004-recording-mvc` | **Date**: 2026-11-20 | **Spec**: [spec.md](spec.md)

**Input**: `/specs/004-recording-mvc/spec.md`

## Summary

Замінити консольний сценарій і статичний HTML-звіт локальним MVC-вебзастосунком для перегляду та керування записами дзвінків. Модель `Call` працюватиме з колекцією `CallRecording` через наявний `RecordingStore`; тільки `SqliteRecordingStore` виконуватиме SQL. Незалежний від фреймворку контролер і вигляд будуть викликані єдиним Javalin-адаптером, який також обробляє JSON і статичні ресурси. Сторінка використовуватиме vanilla HTML/CSS/JavaScript у темному стилі.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Maven; Google Guice 7; Javalin 6; Jackson (`jackson-databind`); `slf4j-simple`; наявні SQLite JDBC та JUnit 5

**Storage**: Наявна SQLite-база `target/callcenter.db`; таблиця `call_recordings` і її поля лишаються без змін

**Testing**: Наявні JUnit 5 тести; додати перевірки моделі CRUD, SQLite CRUD, контрактів HTTP і відображення помилок; ручна наскрізна перевірка через сторінку

**Target Platform**: Локальний Java-застосунок, `http://localhost:8080/`

**Project Type**: Maven web application із вбудованим вебсервером і статичним клієнтом

**Performance Goals**: Локальний однокористувацький інтерфейс; усі записи повертаються одним запитом, без додаткової вимоги до пропускної здатності

**Constraints**: Не додавати SQLite-таблиць чи сервісів збереження; SQL лишається у `SqliteRecordingStore`; Javalin і JSON API ізолюються в одному класі інфраструктури; бізнес-валідація виконується лише сервером; файли клієнта розміщуються у `src/main/resources/public`; нових тестових записів не створювати

## Constitution Check

| Принцип / вимога | Перевірка |
|---|---|
| Мінімальна складність і SRP | Використовуються зазначені MVC-компоненти й наявне SQLite-сховище; кожен клас має одну визначену роль |
| Локальний вебсервер | Javalin 6 запускається на порту `8080`, без зовнішнього сервера даних |
| Межі MVC | `Call` перевіряє правила; контролер координує модель; `CallCenterWebView` формує HTTP-незалежний результат; вебфреймворк ізольовано в адаптері |
| Інверсія залежностей | Guice зберігає constructor injection для `Call` і зв’язує абстракцію вебсервера з реалізацією |
| Зміни даних на сервері | JavaScript лише надсилає запити, показує результат і оновлює список; не перевіряє правила предметної області |
| SQL та сховище | CRUD-операції додаються до `RecordingStore`; SQL залишається в `SqliteRecordingStore`; схему не розширювати |
| Стилізація | Окремий `style.css`, темне оформлення, без CSS/JS бібліотеки |
| Мова й документація | Пояснення та повідомлення користувачеві українською; ідентифікатори коду англійською; UML у `docs` |

**Gate до дослідження**: PASS. Відомих відхилень від конституції немає. Вибір `Call` → `CallRecording` `0..*` зафіксовано за уточненням користувача: це узгоджує модель із CRUD усіх збережених записів.

## Project Structure

### Documentation (this feature)

```text
specs/004-recording-mvc/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── http-api.md
```

### Source Code

```text
src/
├── main/
│   ├── java/callcenter/
│   │   ├── Call.java
│   │   ├── CallRecording.java
│   │   ├── RecordingStore.java
│   │   ├── SqliteRecordingStore.java
│   │   ├── CallCenterController.java
│   │   ├── CallCenterWebView.java
│   │   ├── WebServer.java
│   │   ├── JavalinWebServer.java
│   │   ├── CallCenterModule.java
│   │   └── Main.java
│   └── resources/public/
│       ├── index.html
│       ├── script.js
│       └── style.css
└── test/java/callcenter/
    ├── CallTest.java
    ├── SqliteRecordingStoreTest.java
    ├── CallCenterControllerTest.java
    ├── CallCenterWebViewTest.java
    └── JavalinWebServerTest.java

docs/
├── class.mmd
├── sequence.mmd
└── component.puml
```

**Structure Decision**: Розширити наявний Maven-проєкт і package `callcenter`; не створювати окремі модулі чи фронтенд-збірку. Межа між MVC і Javalin проходить через інтерфейс `WebServer`; конкретний адаптер реєструє маршрути, десеріалізує JSON, передає значення контролеру, серіалізує відповідь і запускає статичні ресурси.

## Complexity Tracking

Порушень конституції, що потребують виправдання, немає.
