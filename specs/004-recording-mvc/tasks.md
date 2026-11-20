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

- [X] T001 Оновити залежності Javalin 6.x, Jackson `jackson-databind` і сумісний із Javalin `slf4j-simple` у `pom.xml`
- [X] T002 Створити статичну вебсторінку `src/main/resources/public/index.html`
- [X] T003 [P] Створити клієнтський сценарій читання та змін `src/main/resources/public/script.js`
- [X] T004 [P] Створити адаптивний темний стиль `src/main/resources/public/style.css`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Реалізувати спільну MVC-межу та CRUD сховища до початку історій користувача

**⚠️ CRITICAL**: Жодна історія користувача не починається до завершення цієї фази

- [X] T005 Замінити операції сховища в `src/main/java/callcenter/RecordingStore.java` на читання всіх, пошук за ID, окреме вставлення, оновлення та видалення
- [X] T006 Реалізувати явні `insert`, `findById`, `update` і `delete` без upsert у `src/main/java/callcenter/SqliteRecordingStore.java`
- [X] T007 Додати операції читання та CRUD над колекцією `recordings: List<CallRecording>` у `src/main/java/callcenter/Call.java`
- [X] T008 Додати лише серверні перевірки `recordingId` після обрізання пробілів, `duration >= 0` і заданого `createdAt` у `src/main/java/callcenter/CallRecording.java`
- [X] T009 Створити без HTTP-коду результат дії моделі з успіхом або українським повідомленням у `src/main/java/callcenter/CallCenterOperationResult.java`
- [X] T010 Створити незалежний від Javalin контролер у `src/main/java/callcenter/CallCenterController.java`
- [X] T011 Створити незалежний від Javalin HTTP-вигляд у `src/main/java/callcenter/CallCenterWebView.java`
- [X] T012 Створити інтерфейс запуску з контролером і портом у `src/main/java/callcenter/WebServer.java`
- [X] T013 Додати прив'язку `RecordingStore` → `SqliteRecordingStore`, прив'язку `WebServer` → майбутню реалізацію й constructor injection у `src/main/java/callcenter/CallCenterModule.java`

**Checkpoint**: Фундамент готовий - усі історії користувача можуть початися

---

## Phase 3: User Story 1 - Перегляд записів (Priority: P1) 🎯 MVP

**Goal**: Оператор переглядає всі наявні записи зі сховища через локальну вебсторінку

**Independent Test**: Запустити застосунок, відкрити `/`, порівняти рядки сторінки з `GET /api/CallCenter` і перевірити порожню таблицю без записів

### Tests for User Story 1

- [X] T014 [P] [US1] Додати перевірку читання порожнього сховища й усіх атрибутів записів у `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [X] T015 [P] [US1] Додати перевірку незалежного читання через `Call` і узгодженої колекції записів у `src/test/java/callcenter/CallTest.java`
- [X] T016 [P] [US1] Додати перевірку контракту `GET /api/CallCenter` у `src/test/java/callcenter/JavalinWebServerTest.java`

### Implementation for User Story 1

- [X] T017 [US1] Реалізувати читання всіх записів у контролері `src/main/java/callcenter/CallCenterController.java`
- [X] T018 [US1] Реалізувати перетворення списку на JSON-відповідь зі статусом `200` у `src/main/java/callcenter/CallCenterWebView.java`
- [X] T019 [US1] Реєструвати `GET /api/CallCenter` і віддавати статичні ресурси `/public` в `src/main/java/callcenter/JavalinWebServer.java`
- [X] T020 [US1] Реалізувати завантаження таблиці, текстове показування HTML-символів і стан порожнього сховища в `src/main/resources/public/script.js`
- [X] T021 [US1] Додати таблицю записів і адаптивну структуру темної сторінки в `src/main/resources/public/index.html`

**Checkpoint**: Історія US1 повністю працює: сторінка показує актуальний список читанням зі сховища

---

## Phase 4: User Story 2 - Додавання та виправлення запису (Priority: P1)

**Goal**: Оператор додає або редагує єдиним серверно-контрольованим записом і отримує актуальний список

**Independent Test**: Додати коректний запис, перечитати його через GET, редагувати й перевірити збереження після перезапуску; дубль і невідповідний ID мають відмовляти

### Tests for User Story 2

- [X] T022 [P] [US2] Додати перевірки додавання, оновлення, дублікату та невідомого запису в `src/test/java/callcenter/CallTest.java`
- [X] T023 [P] [US2] Додати перевірки явного insert/update без upsert і без зміни невідомого ID в `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [X] T024 [P] [US2] Додати перевірки контролера для створення `CallRecording` і передачі запитів моделі в `src/test/java/callcenter/CallCenterControllerTest.java`
- [X] T025 [P] [US2] Додати перевірки HTTP-контрактів `POST` і `PUT`, включно зі статусами `201`, `400`, `404` і `409`, в `src/test/java/callcenter/JavalinWebServerTest.java`

### Implementation for User Story 2

- [X] T026 [US2] Реалізувати додавання у `src/main/java/callcenter/Call.java`, відхиляючи дублікат і перевіряючи доступність оператора/заборонені стани
- [X] T027 [US2] Реалізувати виправлення наявного запису у `src/main/java/callcenter/Call.java`, забороняючи зміну `recordingId`
- [X] T028 [US2] Додати координацію створення й виправлення запису в `src/main/java/callcenter/CallCenterController.java`
- [X] T029 [US2] Додати перетворення успіху й відмов додавання/виправлення на `201`/`200`, `400`, `404` і `409` у `src/main/java/callcenter/CallCenterWebView.java`
- [X] T030 [US2] Реєструвати маршрути `POST /api/CallCenter` і `PUT /api/CallCenter/{id}`, розбирати JSON лише в адаптері `src/main/java/callcenter/JavalinWebServer.java`
- [X] T031 [US2] Реалізувати одну форму створення/редагування, автоматичне заповнення вибраного запису й повторне GET-перечитування в `src/main/resources/public/index.html` і `src/main/resources/public/script.js`
- [X] T032 [US2] Додати оформлення області повідомлень успіху й відмови та форми в `src/main/resources/public/style.css`

**Checkpoint**: Історії US1 і US2 працюють: дані додаються й виправляються, а оновлений список перечитується

---

## Phase 5: User Story 3 - Видалення та зрозуміла відмова (Priority: P2)

**Goal**: Оператор видаляє лише після підтвердження та отримує зрозумілі українські відмови без хибного успіху

**Independent Test**: Підтвердити видалення й перевірити зникнення запису; скасувати підтвердження, змінити відсутній запис і перевірити відмови зі незмінним сховищем

### Tests for User Story 3

- [X] T033 [P] [US3] Додати перевірку видалення та відсутнього запису у `src/test/java/callcenter/CallTest.java`
- [X] T034 [P] [US3] Додати перевірку видалення існуючого й невідомого ID в `src/test/java/callcenter/SqliteRecordingStoreTest.java`
- [X] T035 [P] [US3] Додати перевірки розмежування `404` для відсутнього запису й `409` для забороненої зміни в `src/test/java/callcenter/CallCenterWebViewTest.java`
- [X] T036 [P] [US3] Додати HTTP-перевірки `DELETE`, некоректного JSON, помилки сховища `500` і українських повідомлень у `src/test/java/callcenter/JavalinWebServerTest.java`

### Implementation for User Story 3

- [X] T037 [US3] Реалізувати видалення наявного запису та відмову для невідомого ID у `src/main/java/callcenter/Call.java`
- [X] T038 [US3] Додати координацію видалення в `src/main/java/callcenter/CallCenterController.java`
- [X] T039 [US3] Реєструвати `DELETE /api/CallCenter/{id}` і обробляти помилки JSON/сховища в `src/main/java/callcenter/JavalinWebServer.java`
- [X] T040 [US3] Реалізувати підтвердження видалення, скасування запиту й повторне читання після успіху в `src/main/resources/public/script.js`
- [X] T041 [US3] Візуально відрізнити кнопку видалення, повідомлення успіху й повідомлення відмови в `src/main/resources/public/style.css`

**Checkpoint**: Усі три історії працюють незалежно; видалення підтверджується, а відмови не маскуються як успіх

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Завершити інфраструктуру запуску, документацію, діаграми та наскрізну перевірку

- [X] T042 [P] Замінити консольну демо-виконання у `src/main/java/callcenter/Main.java` отриманням `Call` і `WebServer` з Guice, початковими об'єктами FR-019 і запуском порту `8080`
- [X] T043 [P] Видалити з `src/main/java/callcenter/Main.java` консольні сценарії введення, генерацію статичного звіту та виклики демонстрації даних
- [X] T044 [P] Додати singleton прив'язку інфраструктурного вебсервера і виконувати запуск із `CallCenterController` у `src/main/java/callcenter/CallCenterModule.java`
- [X] T045 [P] Створити реалізацію адаптера Javalin із маршрутизацією, Jackson-десеріалізацією і статичними ресурсами в `src/main/java/callcenter/JavalinWebServer.java`
- [X] T046 [P] Оновити кореневий `README.md` описом MVC, моделлю, виглядом, контролером, Maven-командами, портом `8080`, маршрутами й темним стилем
- [X] T047 [P] Оновити `docs/class.mmd` блоками `namespace`/`box` для `model`, `view`, `controller`, `infrastructure` і множинністю `0..*`
- [X] T048 [P] Оновити `docs/sequence.mmd` шляхами читання та змін через сторінку, контролер, модель, сховище й SQLite
- [X] T049 [P] Оновити `docs/component.puml` користувачем, вузлом вебсервера, базою даних, MVC-частинами та інтерфейсами інфраструктури
- [X] T050 [P] Зберегти фінальний конкретизований текст запиту у `specs/004-recording-mvc/specify-prompt.md`
- [X] T051 [P] Перевірити ручний сценарій із `specs/004-recording-mvc/quickstart.md`, включно зі збереженням після перезапуску та відсутністю тестових записів у чистій базі
- [X] T052 [P] Запустити повну перевірку `mvn test` і зібрати застосунок `mvn package`
- [X] T053 [P] Запустити `mvn exec:java -Dexec.mainClass=callcenter.Main` і перевірити відповідь сторінки та API на `http://localhost:8080/`

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

---

## Phase 6: Convergence

- [X] T054 HIGH Перед перевіркою забороненого стану перевіряти існування запису в `src/main/java/callcenter/Call.java`, щоб відсутній запис для виправлення чи видалення повертав `404` за FR-008 та US3/AC3
- [X] T055 Додати в `src/test/java/callcenter/CallTest.java` перевірки заборонених додавання, виправлення й видалення для всіх визначених станів і недоступного оператора, перевіряючи незмінність сховища за FR-007 та US3/AC4
- [X] T056 Додати в `src/test/java/callcenter/SqliteRecordingStoreTest.java` перевірки відхилення дубля insert, оновлення невідомого ID та видалення існуючого й невідомого ID за T023 та T034
- [X] T057 Додати в `src/test/java/callcenter/JavalinWebServerTest.java` HTTP-перевірки POST/PUT/DELETE, статусів `201/200/400/404/409/500`, некоректного JSON та українських повідомлень за T025, T036 та SC-003
- [X] T058 Додати в `src/test/java/callcenter/SqliteRecordingStoreTest.java` окрему перевірку delete існуючого запису й повернення `false` для невідомого ID за T034

## Phase 7: Convergence

- [X] T059 Remove native browser validation constraints from `src/main/resources/public/index.html` and keep record validation exclusively on the server per FR-003 and FR-005
- [X] T060 Add in `src/test/java/callcenter/CallTest.java` mutation tests for `CallFailed`, `QualityEvaluated`, and `SummarySent`, verifying forbidden add/update/delete leave storage unchanged per FR-007 and US3/AC4
- [X] T061 Align `docs/class.mmd` with the implemented architecture by removing obsolete `HtmlReportGenerator` and `ConsoleEncoding` entries and locating `SqliteRecordingStore` correctly per FR-018 and FR-022
- [X] T062 Update `README.md` duplicate `recordingId` status from `400` to `409` to match FR-021, the HTTP contract, and implementation

## Phase 8: Convergence

- [X] T063 HIGH Узгодити потік «контролер — вигляд» у `src/main/java/callcenter/CallCenterController.java` та `src/main/java/callcenter/JavalinWebServer.java`, щоб результат моделі передавався вигляду для формування відповіді за FR-011 і FR-012 (partial)
- [X] T064 Зібрати вигляд і контролер під час запуску в `src/main/java/callcenter/Main.java`, передаючи узгоджені MVC-об'єкти вебсерверу відповідно до FR-018 (partial)
- [X] T065 HIGH Не перетворювати порожню тривалість на `0` у `src/main/resources/public/script.js`; передавати відсутню або нечислову тривалість для серверного відхилення з `400` за FR-005 та US2/AC1 (partial)

## Phase 9: Convergence

- [X] T066 HIGH Реалізувати передавання результату моделі до `CallCenterWebView` через `CallCenterController` у `src/main/java/callcenter/CallCenterController.java` та `src/main/java/callcenter/JavalinWebServer.java` за FR-011 (partial)
- [X] T067 MEDIUM Прибрати Singleton-прив’язки `CallCenterController` і `CallCenterWebView` у `src/main/java/callcenter/CallCenterModule.java`, оскільки вони не є інфраструктурними одиничними об’єктами за FR-017 (contradicts)

## Phase 10: Convergence

- [X] T068 CRITICAL Прибрати Singleton-прив’язки `CallCenterController` і `CallCenterWebView` у `src/main/java/callcenter/CallCenterModule.java`; зберегти Singleton лише для інфраструктурних одиничних об’єктів за FR-017 і Конституцією V (contradicts)

## Phase 11: Convergence

- [X] T069 CRITICAL Прибрати Singleton-прив’язки `CallCenterController` і `CallCenterWebView` у `src/main/java/callcenter/CallCenterModule.java`; залишити Singleton лише для інфраструктурних одиничних об’єктів за FR-017 і Конституцією V (contradicts)
- [X] T070 CRITICAL Усунути предметну валідацію та parsing-перевірки запису з `src/main/java/callcenter/CallCenterController.java`; дозволити моделі перевіряти `recordingId`, `duration` і `createdAt`, зберігши контрольне створення `CallRecording` та передачу результату моделі за FR-011 і Конституцією V (contradicts)

## Phase 12: Convergence

- [X] T071 HIGH Виправити порядок перевірки у `src/main/java/callcenter/Call.java`: для виправлення спочатку перевіряти існування запису за `{id}` і лише після цього перевіряти збіг ідентифікатора у тілі та заборонений стан, щоб відсутній запис завжди повертав `404` за FR-008 та US2/AC4-US3/AC3

## Phase 13: Convergence

- [X] T072 HIGH Створювати `CallRecording` у `src/main/java/callcenter/CallCenterController.java` для створення та виправлення, передаючи готовий об’єкт моделі для предметної перевірки та координації CRUD за FR-011 і plan: MVC boundary
- [X] T073 HIGH Отримати `Call` і `WebServer` явно через Guice в `src/main/java/callcenter/Main.java`, а потім зібрати контролер і вигляд узгодженим MVC-потоком запуску за FR-018

## Phase 14: Convergence

- [X] T074 MEDIUM Доповнити `docs/class.mmd` оголошеннями класів `CallCenterController`, `CallCenterWebView` і `JavalinWebServer`, а також CRUD-операціями та зв'язками MVC/інфраструктури за FR-022

## Phase 15: Convergence

- [X] T075 HIGH Створювати `CallRecording` у `src/main/java/callcenter/CallCenterController.java` для операцій створення та виправлення та передавати готовий об’єкт методам моделі замість сирих значень за FR-011
- [X] T076 HIGH Отримати `Call` і `WebServer` явно через Guice в `src/main/java/callcenter/Main.java`, після чого узгодити створення контролера, вигляду та запуск сервера за FR-018
- [X] T077 MEDIUM Доповнити `docs/class.mmd` повними оголошеннями операцій і зв’язків для `Call`, `CallRecording`, `CallCenterController`, `CallCenterWebView`, `JavalinWebServer`, `RecordingStore` і MVC-шляхів CRUD за FR-022
