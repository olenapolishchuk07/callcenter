# Модель даних: UML-документація кол-центру

Ця модель визначає назви та відповідальності, які потрібно узгоджено показати на діаграмах і CRC-картках. Вона описує навчальну предметну модель, а не схему бази даних.

## Класи та інтерфейс

| Елемент | Вид | Ключові дані | Відповідальність і зв’язки |
|---|---|---|---|
| `CallCenterParticipant` | Абстрактний клас | `participantId`, `displayName` | Спільні дані учасника; батьківський клас `Client`, `Operator`, `Supervisor`. Має конструктор, фінальний метод `getParticipantId()` і метод `getDisplayName()`, який можуть перевизначати підкласи. |
| `Client` | Клас | `email`, `phoneNumber` | Подає заявку та отримує підсумок; створює/пов’язаний із `Call` і взаємодіє з `EmailService`. Перевизначає `getDisplayName()`. Перевантажує `requestCallback()` варіантами з бажаним часом і без нього. |
| `Operator` | Клас | `operatorStatus` | Обробляє призначений `Call` за `ConversationScript`. Перевизначає `getDisplayName()`. |
| `Supervisor` | Клас | `supervisorCode` | Прослуховує `CallRecording` та створює `QualityEvaluation` для дзвінка. Перевизначає `getDisplayName()`. |
| `ConversationScript` | Інтерфейс | — | Оголошує операції виконання кроків сценарію; не має конструктора. Реалізується `StandardConversationScript`. |
| `StandardConversationScript` | Клас | `scriptName`, `steps` | Реалізує `ConversationScript` і надає кроки розмови оператору. |
| `Call` | Клас | `callId`, `state`, `telephonyService`, `recording`, `qualityEvaluation`, `summary` | Керує життєвим циклом дзвінка; отримує `TelephonyService` через конструктор; композиційно володіє `CallRecording`. |
| `TelephonyService` | Клас | `serviceName` | Симулює початок і завершення телефонної розмови; не виконує зовнішніх викликів. |
| `CallRecording` | Клас | `recordingId`, `duration`, `createdAt` | Представляє запис, створений для одного `Call`; доступний супервізору для прослуховування й оцінювання. |
| `QualityEvaluation` | Клас | `evaluationId`, `score`, `feedback` | Зберігає результат оцінювання запису супервізором і пов’язується з `Call`. |
| `ConversationSummary` | Клас | `summaryId`, `content`, `preparedAt` | Представляє підготовлений підсумок розмови, адресований клієнту. |
| `EmailService` | Клас | `serviceName` | Симулює надсилання `ConversationSummary` клієнтові на електронну пошту. |

Кожен клас на діаграмі має показувати щонайменше один атрибут, один метод і конструктор. Інтерфейс показує оголошення операцій без конструктора. `CallCenterParticipant.getParticipantId()` позначається як `final`; у трьох нащадках перевизначається `getDisplayName()`. Перевантаження демонструється двома відмінними сигнатурами `Client.requestCallback(...)`.

## Відношення

- `Client`, `Operator` і `Supervisor` успадковують `CallCenterParticipant`.
- `StandardConversationScript` реалізує `ConversationScript`; `Operator` використовує сценарій при обробці дзвінка.
- `Call` залежить від `TelephonyService`; сервіс передається як екземплярна залежність, не статично.
- `Call` композиційно володіє `CallRecording` (один запис цього дзвінка).
- `Supervisor` оцінює запис і пов’язує `QualityEvaluation` із відповідним `Call`.
- `Call` зберігає `ConversationSummary`; `Client` отримує його через симульований `EmailService`.
- CRC-картки потрібні для кожного з 11 класів. `ConversationScript` — інтерфейс, тому окремої картки класу для нього не потрібно.

## Стани та правила переходів

Успішний життєвий цикл: початок → `NewRequest` → `CallInProgress` → `Recorded` → `QualityEvaluated` → `SummarySent` → кінець.

- Неповні/некоректні контакти не створюють готову до обробки заявку.
- Недоступний оператор не переводить дзвінок у `CallInProgress`.
- Без завершення дзвінка й запису стан `Recorded` не встановлюється.
- Без доступного запису оцінювання не завершується; `QualityEvaluated` не встановлюється.
- Без підсумку та підтвердженого результату симульованого надсилання стан `SummarySent` не встановлюється.

Альтернативні гілки можуть пояснювати невдачі, але не повинні показувати їх як успішний перехід.
