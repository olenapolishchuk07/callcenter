# Модель даних: Імітаційний прототип кол-центру

Модель повторює погоджені сутності й зв'язки з `docs/class.mmd`, ролі та відповідальності з `docs/crc.xlsx`, а стани — з `docs/state.mmd`. Об'єкти створюються в пам'яті для одного запуску; постійне зберігання не передбачене.

## Сутності та компоненти

### CallCenterParticipant

- Абстрактний спільний учасник.
- Поля: `participantId: String`, `displayName: String`.
- `getParticipantId()` є незмінним для підкласів; `getDisplayName()` визначається спеціалізаціями.
- Спеціалізації: `Client`, `Operator`, `Supervisor`.

### Client

- Поля: `email: String`, `phoneNumber: String`; ідентифікатор і ім'я успадковуються.
- Відповідальність: подати заявку `requestCallback()` або `requestCallback(preferredTime)` та отримати підсумок.
- Зв'язок: один клієнт може запросити багато дзвінків.
- Перевірка даних заявки має відхиляти відсутні/порожні контактні дані; точні правила валідації формату специфікацією не встановлені. Дані демонстрації мають бути вигаданими.

### Operator

- Поле: `operatorStatus: String`; ідентифікатор та ім'я успадковуються.
- Відповідальність: обробити `Call` за інтерфейсом `ConversationScript` через `handleCall(call, script)`.
- Зв'язок: один оператор може обробляти багато дзвінків.

### Supervisor

- Поле: `supervisorCode: String`; ідентифікатор та ім'я успадковуються.
- Відповідальність: прослухати `CallRecording` і створити `QualityEvaluation` для дзвінка через `evaluate(recording, call)`.
- Передумова успішної оцінки: доступний запис; без нього оцінювання не зберігається й не встановлюється `QualityEvaluated`.

### ConversationScript та StandardConversationScript

- `ConversationScript` — інтерфейс кроків `executeStep(stepNumber)` і `getStepCount()`.
- `StandardConversationScript` реалізує інтерфейс; поля: `scriptName: String`, `steps: List<String>`.
- Кроки виконуються послідовно за індексом/номером, дозволеним кількістю кроків. Обробка не має бути позначена успішною, якщо розмову не завершено.

### Call

- Поля за діаграмою: `callId: String`, `state: String`, `telephonyService: TelephonyService`, `recording: CallRecording?`, `qualityEvaluation: QualityEvaluation?`, `summary: ConversationSummary?`; асоціації з `Client` та `Operator`.
- Відповідальність: тримати життєвий цикл і пов'язані результати; використовувати переданий через конструктор екземпляр `TelephonyService`.
- Операції за діаграмою: `start()`, `completeAndRecord()`, `saveEvaluation(evaluation)`, `saveSummary(summary)`.
- Перехід до `Recorded` дозволений лише після завершення розмови та створення запису. `QualityEvaluated` дозволений лише після доступного запису та збереження оцінки. `SummarySent` дозволений лише після наявності підсумку й підтвердження симульованого надсилання.
- Стани і переходи:

| Поточний стан | Умова/подія | Наступний стан |
|---|---|---|
| Початковий | Валідна заявка | `NewRequest` |
| Початковий | Некоректні контактні дані | `RequestRejected` |
| `NewRequest` | Оператора неможливо призначити | `OperatorUnavailable` |
| `NewRequest` | Оператор доступний і телефонію успішно розпочато | `CallInProgress` |
| `CallInProgress` | Дзвінок перервано або завершення не підтверджене | `CallFailed` |
| `CallInProgress` | Розмову завершено й запис створено | `Recorded` |
| `Recorded` | Запис недоступний для прослуховування | `EvaluationFailed` |
| `Recorded` | Запис прослухано й оцінку збережено | `QualityEvaluated` |
| `QualityEvaluated` | Підсумку немає або надсилання не підтверджене | `SummaryFailed` |
| `QualityEvaluated` | Підсумок підготовлено й надсилання підтверджено | `SummarySent` |

`RequestRejected`, `OperatorUnavailable`, `CallFailed`, `EvaluationFailed`, `SummaryFailed` та `SummarySent` є кінцевими станами процесу. Успішний стандартний запуск містить лише п'ять станів основного шляху.

### TelephonyService

- Поле: `serviceName: String`.
- Операції: `startCall(callId)` та `endCall(callId)` повертають результат симуляції.
- Відповідальність: вивести українське повідомлення про очікувану дію й повернути результат без реального телефонного виклику.

### CallRecording

- Поля: `recordingId: String`, `duration: int`, `createdAt: DateTime`.
- Відповідальність: представляти запис завершеної розмови й надавати його для прослуховування через `listen()`.
- Створюється тільки після успішного завершення дзвінка; тривалість не може бути від'ємною.

### QualityEvaluation

- Поля: `evaluationId: String`, `score: int`, `feedback: String`.
- Відповідальність: зберігати результат супервізорської оцінки та надавати його через `getResult()`.
- Створюється супервізором на основі запису. Межі шкали оцінювання специфікацією не визначені й тут не вводяться.

### ConversationSummary

- Поля: `summaryId: String`, `content: String`, `preparedAt: DateTime`.
- Відповідальність: представляти підготовлений підсумок та надавати його вміст.
- Підсумок потрібен до симуляції успішного надсилання.

### EmailService

- Поле: `serviceName: String`.
- Операція: `sendSummary(client, summary)` повертає результат симуляції.
- Відповідальність: вивести українське повідомлення про надсилання та результат; не надсилати реальний лист.

## Зв'язки

- `CallCenterParticipant` узагальнюється в `Client`, `Operator` і `Supervisor`.
- `StandardConversationScript` реалізує `ConversationScript`; `Operator` використовує цей інтерфейс.
- `Call` залежить від переданого екземпляра `TelephonyService` та композиційно володіє нульовим або одним `CallRecording`.
- `Call` зберігає нульову або одну `QualityEvaluation` і `ConversationSummary`.
- `Supervisor` використовує запис і створює оцінку; `EmailService` доставляє підсумок клієнту як симуляцію.

## Узгодження з реалізацією

`Main` і `CallLifecycleChecks` — технічні точки запуску/перевірки, не предметні сутності CRC. Будь-яке відхилення реалізованої моделі від перелічених класів, атрибутів, викликів, зв'язків або переходів має бути узгоджено в коді та всіх залежних діаграмах/CRC-картках перед завершенням функції.
