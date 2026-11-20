# Швидкий запуск і перевірка

## Передумови

- JDK 21 і Maven 3.9+ у `PATH`.
- Команди виконуються з кореня репозиторію в PowerShell.

## Зібрати та запустити

```powershell
mvn test
mvn package
mvn exec:java -Dexec.mainClass=callcenter.Main
```

Очікувано: тести та збірка завершуються успішно; застосунок запускає вебсервер на порту `8080` і залишається запущеним. Відкрийте `http://localhost:8080/`.

## Перевірка API у PowerShell

В іншому вікні PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/api/CallCenter

$recording = @{
    recordingId = 'REC-QUICKSTART'
    duration = 42
    createdAt = '2026-11-20T14:30:00'
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/CallCenter `
    -ContentType 'application/json' -Body $recording
Invoke-RestMethod http://localhost:8080/api/CallCenter
```

Очікувано: GET повертає JSON-масив, POST — українське повідомлення про успіх зі статусом `201`, наступний GET містить `REC-QUICKSTART`. Для ізольованої перевірки виправлення та видалення використовуйте відповідно `PUT /api/CallCenter/REC-QUICKSTART` і `DELETE /api/CallCenter/REC-QUICKSTART`.

## Перевірка відмов і вебінтерфейсу

- Повторіть POST з тим самим ID: `409`, українське повідомлення, початкові дані незмінні.
- Надішліть PUT з ID тіла, що не збігається зі шляхом: `400`, дані незмінні.
- Змініть або видаліть невідомий ID: `404` з українським повідомленням.
- Спробуйте від'ємний `duration` чи відсутній/некоректний `createdAt`: `400`, сховище незмінне.
- На сторінці перевірте створення, автоматичне заповнення форми для редагування, повторне завантаження списку після зміни, підтвердження й скасування видалення, а також візуальну різницю повідомлень і текстове відображення HTML-символів.
- Перезапустіть сервер і виконайте GET: успішно збережений запис лишається, а перший запуск чистої бази не створює тестових записів.

Повні поля й коди відповідей наведені у [контракті HTTP API](contracts/http-api.md); правила сутностей — у [моделі даних](data-model.md).
