# Швидкий запуск і перевірка

## Передумови

- Установлений JDK 8 або новіший, доступні `java` і `javac` у `PATH`.
- Команди виконуються з кореня репозиторію.
- Зовнішні залежності та база даних не потрібні.

## Компіляція й запуск у PowerShell

```powershell
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force out | Out-Null
javac -d out $sources
java -cp out callcenter.Main
```

Очікується автоматичний демонстраційний сценарій українською з фіксованими вигаданими даними. Перевірте, що стани виводяться у порядку `NewRequest`, `CallInProgress`, `Recorded`, `QualityEvaluated`, `SummarySent`, після чого явно повідомляється про завершення. Повідомлення описують симульовану телефонію та електронну пошту; реальних викликів немає.

## Окремі перевірки життєвого циклу

Після компіляції виконайте:

```powershell
java -cp out callcenter.CallLifecycleChecks
```

Перевірки мають підтвердити, що:

1. оцінювання без доступного запису не зберігає оцінку і не переводить дзвінок у `QualityEvaluated`;
2. незавершена обробка не створює `CallRecording` і не переводить дзвінок у `Recorded` або `QualityEvaluated`.

Перевірки невдалих сценаріїв виконуються окремо та не змінюють успішний стандартний запуск. Деталі сутностей і переходів наведено в [data-model.md](data-model.md); консольна поведінка визначена в [contracts/console.md](contracts/console.md).
