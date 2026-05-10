# GreenScan

Android-приложение (Kotlin, Jetpack Compose, CameraX, ML Kit Barcode, Room): сканирование штрихкода, поиск в локальной SQLite и данные из [Open Food Facts](https://world.openfoodfacts.org/) для товаров с [green-dostavka.by](https://green-dostavka.by).

В репозитории: инструменты для той же схемы БД на Python (`schema.sql`, инициализация, парсер каталога Green).

## Структура

| Путь | Назначение |
|------|------------|
| `android/` | Приложение (Gradle, исходники Kotlin). |
| `src/schema.sql` | DDL SQLite (общий с Room). |
| `src/database.py` | Создание пустой БД по схеме. |
| `src/green_parser.py` | Парсинг сайта и заполнение БД. |
| `src/test/` | Тесты парсера. |
| `doc/architecture.txt` | Краткое описание потоков данных. |
| `requirements.txt` | Зависимости Python. |

БД для приложения: `android/app/src/main/assets/database/products.db` (см. `doc/architecture.txt`). Чтобы собрать файл заново: `database.py` → `green_parser.py` → скопировать `products.db` в этот каталог assets.

## Android

1. [Android Studio](https://developer.android.com/studio).
2. `android/local.properties.example` → `android/local.properties`, указать `sdk.dir`.
3. Открыть каталог `android/`, собрать и запустить на устройстве или эмуляторе.

## Python (схема и наполнение БД)

```bash
cd src
python -m venv .venv && source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r ../requirements.txt
python database.py
python green_parser.py --db products.db   # опционально: --limit 10 --no-progress
```

Тесты: `pip install pytest` и `pytest test/` из каталога `src/`.

## Схема

Таблицы `products`, `barcodes`, `nutrition_facts` — в `src/schema.sql`.
