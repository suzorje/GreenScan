-- Canonical schema: Python parser + Android Room (same DDL).
-- products: основные атрибуты; barcodes: идентификация по штрихкоду;
-- nutrition_facts: расчётные показатели (БЖУ/ккал).

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS products (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  green_internal_id INTEGER,
  name TEXT NOT NULL,
  description TEXT,
  category TEXT,
  url TEXT NOT NULL UNIQUE,
  price_rub REAL,
  old_price_rub REAL,
  producer TEXT,
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS barcodes (
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  product_id INTEGER NOT NULL,
  code TEXT NOT NULL UNIQUE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS nutrition_facts (
  product_id INTEGER NOT NULL PRIMARY KEY,
  proteins_g REAL,
  fats_g REAL,
  carbohydrates_g REAL,
  calories_kcal REAL,
  energy_raw TEXT,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_barcodes_product ON barcodes(product_id);
