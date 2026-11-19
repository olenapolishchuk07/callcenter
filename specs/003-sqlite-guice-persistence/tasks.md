# Tasks: Збереження даних кол-центру через SQLite і Guice

**Input**: Design documents from `/specs/003-sqlite-guice-persistence/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Специфікація вимагає автоматизованих перевірок збірки та поведінки збереження. Тестові завдання виконуються до відповідної реалізації.

**Organization**: Завдання згруповано за користувацькими історіями для незалежної реалізації та перевірки кожної історії.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Можна виконувати паралельно — інші файли та відсутність залежностей від незавершених завдань
- **[Story]**: Історія користувача, наприклад US1, US2 або US3
- Опис завдання містить точний шлях до файлу

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Ініціалізація Maven-проєкту та інфраструктури тестування

- [ ] T001 Створити Maven-проєкт `callcenter` зі стандартною структурою в `pom.xml`
- [ ] T002 Додати JDK 21, Google Guice 7, sqlite-jdbc та JUnit Jupiter у `pom.xml`
- [ ] T003 Налаштувати виконання Maven-програми та JUnit у `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Базова предметна модель і сховище, які блокують усі історії користувача

**⚠️ CRITICAL**: Жодну історію користувача не починати до завершення цієї фази

- [ ] T004 Перевірити та зафіксувати композицію `Call *-- "0..1" CallRecording` у `docs/class.mmd`
- [ ] T005 [P] Створити предметний інтерфейс `RecordingStore` із методами збереження та читання в `src/main/java/callcenter/RecordingStore.java`
- [ ] T006 [P] Додати перевірку обов’язкового унікального ідентифікатора та атрибутів `CallRecording` у `src/main/java/callcenter/CallRecording.java`
- [ ] T007 Реалізувати JDBC-схему та предметні операції SQLite у `src/main/java/callcenter/SqliteRecordingStore.java`
- [ ] T008 Реалізувати UPSERT за `recordingId` у `src/main/java/callcenter/SqliteRecordingStore.java`; повторне збереження ключа оновлює решту полів
- [ ] T009 Реалізувати читання `LocalDateTime` у форматі ISO-8601 та без перетворення часового поясу у `src/main/java/callcenter/SqliteRecordingStore.java`
- [ ] T010 Додати забезпечення полів SQLite: `recordingId TEXT PRIMARY KEY`, `duration INTEGER NOT NULL` і `createdAt TEXT NOT NULL` у `src/main/java/callcenter/SqliteRecordingStore.java`
- [ ] T011 Створити модуль Guice `CallCenterModule`, який успадковує `AbstractModule`, прив’язує `RecordingStore` до `SqliteRecordingStore` і надає з’єднання у `src/main/java/callcenter/CallCenterModule.java`

**Checkpoint**: База та модуль залежностей готові; історії можуть починатися паралельно

---

## Phase 3: User Story 1 - Запуск із збереженням даних (Priority: P1) 🎯 MVP

**Goal**: Успішний сценарій кол-центру зберігає `CallRecording` у SQLite і формує статичну HTML-таблицю

**Independent Test**: Виконати `mvn package` і `mvn exec:java -Dexec.mainClass=callcenter.Main`; перевірити незмінність консольних повідомлень, `target/callcenter.db`, таблицю у `target/callcenter-report.html` і оновлення запису без дублю

### Tests for User Story 1

- [ ] T012 [P] [US1] Створити JUnit-тест UPSERT і відсутності дублів у `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [ ] T013 [P] [US1] Створити JUnit-тест читання усіх атрибутів і порожнього сховища у `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [ ] T014 [P] [US1] Створити JUnit-тест таблиці, порожньої таблиці та екранування `<`, `>`, `&`, лапок і апострофа у `src/test/java/callcenter/HtmlReportGeneratorTest.java`
- [ ] T015 [P] [US1] Створити JUnit-тест від’ємної тривалості: `duration >= 0` у `src/test/java/callcenter/CallRecordingTest.java`
- [ ] T016 [P] [US1] Додати перевірку JUnit для українського повідомлення про помилку SQLite у `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [ ] T016a [P] [US1] Додати JUnit-перевірки українських повідомлень і припинення роботи для помилок читання SQLite та генерації HTML у `src/test/java/callcenter/SqliteRecordingStoreTest.java` та `src/test/java/callcenter/HtmlReportGeneratorTest.java`

### Implementation for User Story 1

- [ ] T017 [US1] Додати впровадження `RecordingStore` через `@Inject`-конструктор у `src/main/java/callcenter/Call.java`
- [ ] T018 [US1] Викликати предметне збереження запису під час успішного завершення сценарію в `src/main/java/callcenter/Call.java`
- [ ] T019 [US1] Додати предметне створення HTML-таблиці на основі переданого генератору списку записів у `src/main/java/callcenter/HtmlReportGenerator.java`; читання сховища не входить до відповідальності генератора
- [ ] T020 [US1] Екранувати всі текстові значення для HTML як дані, не змінюючи збережені значення, у `src/main/java/callcenter/HtmlReportGenerator.java`
- [ ] T021 [US1] Зберегти порожній звіт із заголовками `recordingId`, `duration`, `createdAt` і без рядків у `target/callcenter-report.html`
- [ ] T022 [US1] Змінити точки створення `Call` у `src/main/java/callcenter/Client.java`, щоб приймати одержаний через Guice екземпляр без втрати сумісних сигнатур
- [ ] T023 [US1] Створити Injector і отримати `Call` через `injector.getInstance(Call.class)` у `src/main/java/callcenter/Main.java`
- [ ] T024 [US1] Після успішного сценарію прочитати сховище через `RecordingStore`, передати отримані записи в `HtmlReportGenerator` і створити звіт у `src/main/java/callcenter/Main.java`
- [ ] T025 [US1] Повідомляти українською про помилки з’єднання, запису, читання та генерації й припиняти запуск у `src/main/java/callcenter/Main.java`

**Checkpoint**: Історія 1 повністю реалізована та самостійно перевіряється

---

## Phase 4: User Story 2 - Вибір зв’язку для збереження (Priority: P2)

**Goal**: Наявна композиція `Call`—`CallRecording` вибрана й реалізована через інтерфейс сервісу та constructor injection

**Independent Test**: Переглянути `docs/class.mmd`, перевірити список композицій/агрегацій і переконатися, що `Call` залежить лише від `RecordingStore`, а SQL відсутній поза `SqliteRecordingStore`

- [ ] T026 [P] [US2] Зафіксувати в `spec.md` факт єдиного придатного зв’язку композиції `Call *-- "0..1" CallRecording` із `docs/class.mmd`
- [ ] T027 [US2] Використати лише обрану композицію `Call *-- "0..1" CallRecording` і не прив’язувати збереження до іншої сутності у `src/main/java/callcenter/Call.java`
- [ ] T028 [US2] Передати `RecordingStore` у конструктор `Call`, не додаючи статичного доступу до сховища у `src/main/java/callcenter/Call.java`
- [ ] T029 [US2] Додати в `CallCenterModule` зв’язування `RecordingStore` до `SqliteRecordingStore`, надання JDBC-з’єднання та конфігурацію `Call` для constructor injection у `src/main/java/callcenter/CallCenterModule.java`
- [ ] T030 [US2] Додати JUnit-перевірку зв’язування Guice й уникнення прямого `new Call(...)` у створювачі застосунку у `src/test/java/callcenter/CallCenterModuleTest.java`

**Checkpoint**: Історія 2 перевіряє інверсію залежностей і вибір композиції незалежно від звіту

---

## Phase 5: User Story 3 - Збірка, запуск і узгодження документації (Priority: P3)

**Goal**: Проєкт збирається й запускається командами Maven, а README та UML відповідають реальній реалізації

**Independent Test**: Виконати команди з `README.md`, перевірити стандартну структуру Maven і зірити `docs/class.mmd` та `docs/sequence.mmd` із модулем, Injector і constructor injection

- [ ] T031 [P] [US3] Оновити Maven-команди збирання та запуску українською в кореневому `README.md`
- [ ] T032 [P] [US3] Описати базу `target/callcenter.db` і точний звіт `target/callcenter-report.html` у `README.md`
- [ ] T033 [P] [US3] Додати до `README.md` правила про порожню базу, повторний запуск і відкриття HTML-файла вручну
- [ ] T034 [US3] Оновити класову діаграму модулем, прив’язкою інтерфейсу, SQLite-сервісом і зв’язком `Call`—`CallRecording` у `docs/class.mmd`
- [ ] T035 [US3] Оновити послідовну діаграму етапами модуля, прив’язки, створення Injector, отримання `Call` і впровадження конструктора у `docs/sequence.mmd`
- [ ] T036 [US3] Перевірити всі SQL-виклики локалізовані лише в `src/main/java/callcenter/SqliteRecordingStore.java`
- [ ] T037 [US3] Зберегти назви пакетів і наявні класи, не перейменовуючи їх під час оновлення Maven-структури
- [ ] T038 [US3] Запустити повну перевірку `mvn package` і документовану команду запуску згідно з `specs/003-sqlite-guice-persistence/quickstart.md`

**Checkpoint**: Усі три історії готові до інтегрованої перевірки

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Фінальна узгодженість, документація та перевірка помилок

- [ ] T039 [P] Оновити кореневий `README.md` вихідними повідомленнями про помилки та забороною успіху без повної операції
- [ ] T040 [P] Перевірити екранування `<`, `>`, `&`, лапок і апострофа в генерованому HTML у `src/test/java/callcenter/HtmlReportGeneratorTest.java`
- [ ] T041 [P] Зберегти наявну перевірку життєвого циклу в `src/test/java/callcenter/CallLifecycleChecks.java` і підтвердити її під час Maven-збірки
- [ ] T042 Перевірити повторний запуск без очищення `target` і відсутність видалення раніше збережених записів
- [ ] T043 Виконати кінцеву перевірку всіх вимог і зафіксувати команди/результати у `specs/003-sqlite-guice-persistence/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Без залежностей — починається одразу
- **Foundational (Phase 2)**: Залежить від Phase 1 — БЛОКУЄ всі історії користувача
- **User Stories (Phase 3+)**: Усі залежать від завершення Foundational
  - Паралельно за наявності окремих виконавців
  - Або послідовно за P1 → P2 → P3
- **Polish (Final Phase)**: Залежить від усіх обраних історій

### User Story Dependencies

- **User Story 1 (P1)**: Може початися після Foundational; не потребує US2/US3
- **User Story 2 (P2)**: Може початися після Foundational; перевіряє той самий зв’язок і може використовувати модель US1
- **User Story 3 (P3)**: Може початися після Foundational; перевіряє виконуваний результат US1 і зв’язки US2

### Within Each User Story

- Тести пишуться до реалізації й спочатку мають падати
- Моделі перед сервісами
- Сервіс перед генератором звіту та точкою входу
- Інтерфейс і модуль перед впровадженням у доменний клас
- Часова перевірка історії перед переходом до наступного пріоритету

---

## Parallel Opportunities

- Завдання T005–T006 і тестові завдання T012–T016 можуть виконуватися паралельно у межах різних файлів
- Після завершення Foundational US1, US2 і US3 можуть розроблятися паралельно
- Документаційні завдання T031–T035 також можуть виконуватися паралельно
- Polish-завдання з тестами й документацією позначені `[P]`

## Parallel Example: User Story 1

```text
T012 — JUnit-тест UPSERT в src/test/java/callcenter/SqliteRecordingStoreTest.java
T013 — JUnit-тест читання в src/test/java/callcenter/SqliteRecordingStoreTest.java
T014 — JUnit-тест екранування HTML в src/test/java/callcenter/HtmlReportGeneratorTest.java
T015 — JUnit-тест від’ємної тривалості в src/test/java/callcenter/CallRecordingTest.java
T016 — JUnit-тест помилки SQLite в src/test/java/callcenter/SqliteRecordingStoreTest.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Завершити Phase 1: Setup
2. Завершити Phase 2: Foundational
3. Завершити Phase 3: User Story 1
4. Перевірити MVP окремо за незалежним тестом US1
5. Переконатися, що база й звіт створені у `target`

### Incremental Delivery

1. Завершити Setup + Foundational — базове сховище готове
2. Додати US1 — перевірити MVP і продемонструвати повний звіт
3. Додати US2 — перевірити вибір зв’язку й constructor injection
4. Додати US3 — перевірити Maven-команди й узгоджені UML-діаграми
5. Завершити Polish — перевірити помилки, повторний запуск і документоване виконання

### Parallel Team Strategy

1. Команда разом завершує Setup і Foundational
2. Після Foundational:
   - Developer A: US1
   - Developer B: US2
   - Developer C: US3
3. Історії перевіряються й інтегруються незалежно

---

## Notes

- [P] означає завдання для інших файлів без залежності від незавершених завдань
- [Story] у задачах фази користувацької історії забезпечує трасованість до spec.md
- Кожна користувацька історія завершується незалежно перевірюваним інкрементом
- Для тестів спочатку виконати їх і зафіксувати очікувану невдачу
- Не використовувати `mvn clean` у перевірці збереження між запусками
