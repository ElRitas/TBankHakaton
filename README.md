# Payment Link Generator API

Простое Ktor-приложение на Kotlin для генерации платёжных ссылок в зависимости от платформы пользователя (Android, iOS, Desktop).

## Запуск проекта

Для запуска сервера:

```
./gradlew run
```

## Эндпоинты

### POST /payment

Создание платёжной ссылки.

Тело запроса (JSON):

```
{
  "paymentId": "b1e29ec2-82d7-4b14-9509-f089f9110abc",
  "deviceOS": "Android",
  "webview": true
}
```

Поля запроса:

- paymentId (string) — UUID платежа. Должен быть валидным UUID.
- deviceOS (string) — Операционная система устройства: Android, iOS, Desktop.
- webview (boolean) — Использовать ли WebView (только для iOS).

Пример успешного ответа (HTTP 200 OK):

```
{
  "link": "tinkoffbank://Main/tpay/b1e29ec2-82d7-4b14-9509-f089f9110abc",
  "status": 200
}
```

Примеры ссылок:

| deviceOS | webview | Сгенерированная ссылка |
|----------|---------|------------------------|
| Android  | любой   | tinkoffbank://Main/tpay/{paymentId} |
| iOS      | true    | https://www.tinkoff.ru/tpay/{paymentId} |
| iOS      | false   | bank100000000004://Main/tpay/{paymentId} |
| Desktop  | любой   | https://www.tinkoff.ru/tpay/{paymentId} |

### Ошибки

Коды ошибок и их описание:

- 400: Invalid JSON format or wrong UUID — Неверный формат JSON или UUID
- 400: Unsupported deviceOS: {deviceOS} — Неподдерживаемая операционная система
- 400: Invalid UUID format: {paymentId} — Неверный формат UUID
- 500: Something went wrong — Внутренняя ошибка сервера

Пример ошибки (HTTP 400 Bad Request):

```
{
  "error": "Invalid UUID format: not-a-valid-uuid",
  "code": 400
}
```

## Технологии

- Ktor (server, routing, content negotiation)
- Kotlin Serialization (для JSON)
- Netty (как HTTP сервер)

## Как работает

1. Принимает POST-запрос с данными платежа.
2. Валидирует paymentId как UUID.
3. Проверяет значение deviceOS.
4. Генерирует платёжную ссылку в зависимости от устройства и наличия webview.
5. Возвращает ссылку клиенту.

## Требования

- JDK 17+
- Gradle
