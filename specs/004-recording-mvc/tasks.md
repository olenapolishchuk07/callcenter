# Tasks: Вебкерування записами дзвінків за MVC

**Input**: Документи проєктування з `specs/004-recording-mvc/`

**Prerequisites**: `plan.md` (обов'язковий), `spec.md` (обов'язковий для історій користувача), `research.md`, `data-model.md`, `contracts/http-api.md`, `quickstart.md`

**Tests**: Додано перевірки моделі CRUD, SQLite CRUD, HTTP-контрактів і відображення помилок відповідно до плану тестування.

**Organization**: Завдання групуються за історіями користувача для незалежного впровадження й перевірки кожної історії.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Завдання можна виконувати паралельно (різні файли, без залежностей від незавершених завдань)
- **[Story]**: Історія користувача: US1, US2 або US3
- Кожне завдання містить точний шлях до файла

## Path Conventions

- Maven-проєкт: `src/main/java/`, `src/main/resources/public/`, `src/test/java/`, `docs/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Додати вебзалежності й базову структуру ресурсів

- [ ] T001 Оновити залежності Javalin 6.x, Jackson `jackson-databind` і сумісний із Javalin `slf4j-simple` у `pom.xml`
- [ ] T002 Створити статичну вебсторінку `src/main/resources/public/index.html`
- [ ] T003 [P] Створити клієнтський сценарій читання та змін `src/main/resources/public/script.js`
- [ ] T004 [P] Створити адаптивний темний стиль `src/main/resources/public/style.css`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Реалізувати спільну MVC-межу та CRUD сховища до початку історій користувача

**⚠️ CRITICAL**: Жодна історія користувача не починається до завершення цієї фази

- [ ] T005 Замінити операції сховища в `src/main/java/callcenter/RecordingStore.java` на читання всіх, пошук за ID, окреме вставлення, оновлення та видалення
- [ ] T006 Реалізувати явні `insert`, `findById`, `update` і `delete` без upsert у `src/main/java/callcenter/SqliteRecordingStore.java`
- [ ] T007 Додати операції читання та CRUD над колекцією `recordings: List<CallRecording>` у `src/main/java/callcenter/Call.java`
- [ ] T008 Додати лише серверні перевірки `recordingId` після обрізання пробілів, `duration >= 0` і заданого `createdAt` у `src/main/java/callcenter/CallRecording.java`
- [ ] T009 Створити без HTTP-коду результат дії моделі з успіхом або українським повідомленням у `src/main/java/callcenter/CallCenterOperationResult.java`
- [ ] T010 Створити незалежний від Javalin контролер у `src/main/java/callcenter/CallCenterController.java`
- [ ] T011 Створити незалежний від Javalin HTTP-вигляд у `src/main/java/callcenter/CallCenterWebView.java`
- [ ] T012 Створити інтерфейс запуску з контролером і портом у `src/main/java/callcenter/WebServer.java`
- [ ] T013 Додати прив'язку `RecordingStore` → `SqliteRecordingStore`, прив'язку `WebServer` → майбутню реалізацію й constructor injection у `src/main/java/callcenter/CallCenterModule.java`

**Checkpoint**: Фундамент готовий - усі історії користувача можуть початися

---

## Phase 3: User Story 1 - Перегляд записів (Priority: P1) 🎯 MVP

**Goal**: Оператор переглядає всі наявні записи зі сховища через локальну вебсторінку

**Independent Test**: Запустити застосунок, відкрити `/`, порівняти рядки сторінки з `GET /api/CallCenter` і перевірити порожню таблицю без записів

### Tests for User Story 1

- [ ] T014 [P] [US1] Додати перевірку читання порожнього сховища й усіх атрибутів записів у `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [ ] T015 [P] [US1] Додати перевірку незалежного читання через `Call` і узгодженої колекції записів у `src/test/java/callcenter/CallTest.java`
- [ ] T016 [P] [US1] Додати перевірку контракту `GET /api/CallCenter` у `src/test/java/callcenter/JavalinWebServerTest.java`

### Implementation for User Story 1

- [ ] T017 [US1] Реалізувати читання всіх записів у контролері `src/main/java/callcenter/CallCenterController.java`
- [ ] T018 [US1] Реалізувати перетворення списку на JSON-відповідь зі статусом `200` у `src/main/java/callcenter/CallCenterWebView.java`
- [ ] T019 [US1] Реєструвати `GET /api/CallCenter` і віддавати статичні ресурси `/public` в `src/main/java/callcenter/JavalinWebServer.java`
- [ ] T020 [US1] Реалізувати завантаження таблиці, текстове показування HTML-символів і стан порожнього сховища в `src/main/resources/public/script.js`
- [ ] T021 [US1] Додати таблицю записів і адаптивну структуру темної сторінки в `src/main/resources/public/index.html`

**Checkpoint**: Історія US1 повністю працює: сторінка показує актуальний список читанням зі сховища

---

## Phase 4: User Story 2 - Додавання та виправлення запису (Priority: P1)

**Goal**: Оператор додає або редагує єдиним серверно-контрольованим записом і отримує актуальний список

**Independent Test**: Додати коректний запис, перечитати його через GET, редагувати й перевірити збереження після перезапуску; дубль і невідповідний ID мають відмовляти

### Tests for User Story 2

- [ ] T022 [P] [US2] Додати перевірки додавання, оновлення, дублікату та невідомого запису в `src/test/java/callcenter/CallTest.java`
- [ ] T023 [P] [US2] Додати перевірки явного insert/update без upsert і без зміни невідомого ID в `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [ ] T024 [P] [US2] Додати перевірки контролера для створення `CallRecording` і передачі запитів моделі в `src/test/java/callcenter/CallCenterControllerTest.java`
- [ ] T025 [P] [US2] Додати перевірки HTTP-контрактів `POST` і `PUT`, включно зі статусами `201`, `400`, `404` і `409`, в `src/test/java/callcenter/JavalinWebServerTest.java`

### Implementation for User Story 2

- [ ] T026 [US2] Реалізувати додавання у `src/main/java/callcenter/Call.java`, відхиляючи дублікат і перевіряючи доступність оператора/заборонені стани
- [ ] T027 [US2] Реалізувати виправлення наявного запису у `src/main/java/callcenter/Call.java`, забороняючи зміну `recordingId`
- [ ] T028 [US2] Додати координацію створення й виправлення запису в `src/main/java/callcenter/CallCenterController.java`
- [ ] T029 [US2] Додати перетворення успіху й відмов додавання/виправлення на `201`/`200`, `400`, `404` і `409` у `src/main/java/callcenter/CallCenterWebView.java`
- [ ] T030 [US2] Реєструвати маршрути `POST /api/CallCenter` і `PUT /api/CallCenter/{id}`, розбирати JSON лише в адаптері `src/main/java/callcenter/JavalinWebServer.java`
- [ ] T031 [US2] Реалізувати одну форму створення/редагування, автоматичне заповнення вибраного запису й повторне GET-перечитування в `src/main/resources/public/index.html` і `src/main/resources/public/script.js`
- [ ] T032 [US2] Додати оформлення області повідомлень успіху й відмови та форми в `src/main/resources/public/style.css`

**Checkpoint**: Історії US1 і US2 працюють: дані додаються й виправляються, а оновлений список перечитується

---

## Phase 5: User Story 3 - Видалення та зрозуміла відмова (Priority: P2)

**Goal**: Оператор видаляє лише після підтвердження та отримує зрозумілі українські відмови без хибного успіху

**Independent Test**: Підтвердити видалення й перевірити зникнення запису; скасувати підтвердження, змінити відсутній запис і перевірити відмови зі незмінним сховищем

### Tests for User Story 3

- [ ] T033 [P] [US3] Додати перевірку видалення та відсутнього запису у `src/test/java/callcenter/CallTest.java`
- [ ] T034 [P] [US3] Додати перевірку видалення існуючого й невідомого ID в `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [ ] T035 [P] [US3] Додати перевірки розмежування `404` для відсутнього запису й `409` для забороненої зміни в `src/test/java/callcenter/CallCenterWebViewTest.java`
- [ ] T036 [P] [US3] Додати HTTP-перевірки `DELETE`, некоректного JSON, помилки сховища `500` і українських повідомлень у `src/test/java/callcenter/JavalinWebServerTest.java`

### Implementation for User Story 3

- [ ] T037 [US3] Реалізувати видалення наявного запису та відмову для невідомого ID у `src/main/java/callcenter/Call.java`
- [ ] T038 [US3] Додати координацію видалення в `src/main/java/callcenter/CallCenterController.java`
- [ ] T039 [US3] Реєструвати `DELETE /api/CallCenter/{id}` і обробляти помилки JSON/сховища в `src/main/java/callcenter/JavalinWebServer.java`
- [ ] T040 [US3] Реалізувати підтвердження видалення, скасування запиту й повторне читання після успіху в `src/main/resources/public/script.js`
- [ ] T041 [US3] Візуально відрізнити кнопку видалення, повідомлення успіху й повідомлення відмови в `src/main/resources/public/style.css`

**Checkpoint**: Усі три історії працюють незалежно; видалення підтверджується, а відмови не маскуються як успіх

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Завершити інфраструктуру запуску, документацію, діаграми та наскрізну перевірку

- [ ] T042 [P] Замінити консольну демо-виконання у `src/main/java/callcenter/Main.java` отриманням `Call` і `WebServer` з Guice, початковими об'єктами FR-019 і запуском порту `8080`
- [ ] T043 [P] Видалити з `src/main/java/callcenter/Main.java` консольні сценарії введення, генерацію статичного звіту та виклики демонстрації даних
- [ ] T044 [P] Додати singleton прив'язку інфраструктурного вебсервера і виконувати запуск із `CallCenterController` у `src/main/java/callcenter/CallCenterModule.java`
- [ ] T045 [P] Створити реалізацію адаптера Javalin із маршрутизацією, Jackson-десеріалізацією і статичними ресурсами в `src/main/java/callcenter/JavalinWebServer.java`
- [ ] T046 [P] Оновити кореневий `README.md` описом MVC, моделлю, виглядом, контролером, Maven-командами, портом `8080`, маршрутами й темним стилем
- [ ] T047 [P] Оновити `docs/class.mmd` блоками `namespace`/`box` для `model`, `view`, `controller`, `infrastructure` і множинністю `0..*`
- [ ] T048 [P] Оновити `docs/sequence.mmd` шляхами читання та змін через сторінку, контролер, модель, сховище й SQLite
- [ ] T049 [P] Оновити `docs/component.puml` користувачем, вузлом вебсервера, базою даних, MVC-частинами та інтерфейсами інфраструктури
- [ ] T050 [P] Зберегти фінальний конкретизований текст запиту у `specs/004-recording-mvc/specify-prompt.md`
- [ ] T051 [P] Перевірити ручний сценарій із `specs/004-recording-mvc/quickstart.md`, включно зі збереженням після перезапуску та відсутністю тестових записів у чистій базі
- [ ] T052 [P] Запустити повну перевірку `mvn test` і зібрати застосунок `mvn package`
- [ ] T053 [P] Запустити `mvn exec:java -Dexec.mainClass=callcenter.Main` і перевірити відповідь сторінки та API на `http://localhost:8080/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Залежностей немає; можна почати одразу
- **Foundational (Phase 2)**: Залежить від Setup і БЛОКУЄ всі історії користувача
- **User Stories (Phase 3+)**: Усі залежать від завершення Foundational
  - US1 і US2 мають однаковий P1-пріоритет і можуть виконуватися паралельно після foundation
  - US3 P2 починається після foundation і може використовувати спільні компоненти US1/US2
- **Polish (Final Phase)**: Залежить від усіх потрібних історій користувача

### User Story Dependencies

- **User Story 1 (P1)**: Запускається після Phase 2 без залежності від US2 чи US3
- **User Story 2 (P1)**: Запускається після Phase 2 і незалежно перевіряється через список US1
- **User Story 3 (P2)**: Запускається після Phase 2 і може інтегруватися зі спільною формою й таблицею

### Within Each User Story

- Тестові завдання записуються й перевіряються до реалізації відповідної історії
- Моделі та сховище перед контролером і виглядом
- Контролер і вигляд перед HTTP-реєстрацією адаптера
- Реалізація історії завершується перед переходом до наступного показника якості

---

## Parallel Opportunities

- У Phase 1 завдання T003 і T004 можна виконувати паралельно з T002
- У Foundation T005-T006 і T007-T008 можна виконувати паралельно після узгодження API
- У кожній історії тестові задачі T014-T016, T022-T025 і T033-T036 можна виконувати паралельно
- Після завершення foundation US1, US2 і US3 можуть розділятися між різними виконавцями
- У Polish документація та UML T046-T051 паралельні до фінальної збірки T052-T053

---

## Parallel Example: User Story 1

```text
T014 Перевірки порожнього й заповненого сховища в SqliteRecordingStoreTest
T015 Перевірка читання колекції через Call
T016 Контрактна перевірка GET /api/CallCenter
```

Після перевірок паралельно можна почати T019 і T020, а потім завершити T017-T018 і інтегрувати сторінку.

---

## Parallel Example: User Story 2

```text
T022 Перевірки CRUD моделі
T023 Перевірки insert/update/delete сховища
T024 Перевірки контролера
T025 Перевірки POST/PUT контракту
```

---

## Parallel Example: User Story 3

```text
T033 Перевірки видалення моделі
T034 Перевірки видалення сховища
T035 Перевірки статусів вигляду
T036 HTTP-перевірки DELETE і помилок
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Завершити Phase 1: Setup
2. Завершити Phase 2: Foundational
3. Завершити Phase 3: User Story 1
4. **STOP and VALIDATE**: Перевірити читання й порожню таблицю без US2/US3
5. Розгорнути або продемонструвати MVP читання записів

### Incremental Delivery

1. Завершити Setup + Foundational → спільна основа готова
2. Додати US1 → перевірити читання → продемонструвати MVP
3. Додати US2 → перевірити CRUD-додавання й виправлення
4. Додати US3 → перевірити видалення й відмови
5. Завершити Polish → перевірити повну збірку й документацію

### Parallel Team Strategy

1. Команда разом завершує Setup і Foundational
2. Після foundation:
   - Виконавець A: US1
   - Виконавець B: US2
   - Виконавець C: US3
3. Завершені історії перевіряються незалежно перед Phase N

---

## Notes

- `[P]` означає інші файли й відсутність залежності від незавершеного завдання
- `[Story]` визначає належність завдання конкретній історії користувача
- Кожне завдання має точний шлях до файла й виправне технічне дію
- Не створювати тестові записи у базі під час першого запуску
- SQL залишати лише в `SqliteRecordingStore`
- Помилки сховища не маскувати як успіх
