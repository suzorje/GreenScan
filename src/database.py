"""
Инициализация БД по канонической схеме (см. schema.sql) — для синхронизации с Android Room.
"""
import sqlite3
from pathlib import Path

_SCHEMA = Path(__file__).resolve().parent / "schema.sql"


def init_db(db_path: str = "products.db") -> None:
    conn = sqlite3.connect(db_path)
    try:
        conn.executescript(_SCHEMA.read_text(encoding="utf-8"))
        conn.commit()
    finally:
        conn.close()
    print(f"Database initialized at {db_path}")


if __name__ == "__main__":
    init_db()
