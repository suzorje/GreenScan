"""
Парсер green-dostavka.by: список URL из sitemap, карточка из __NEXT_DATA__ (Redux initialState).
"""
from __future__ import annotations

import json
import re
import sqlite3
import time
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Any, Iterator, Optional
from urllib.parse import urljoin, urlparse

import requests
from tqdm.auto import tqdm

BASE = "https://green-dostavka.by"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 GreenCourseParser/1.0",
    "Accept-Language": "ru-RU,ru;q=0.9",
}


def fetch_text(url: str, timeout: float = 25.0) -> Optional[str]:
    try:
        r = requests.get(url, headers=HEADERS, timeout=timeout)
        r.raise_for_status()
        return r.text
    except requests.RequestException:
        return None


def parse_next_initial_state(html: str) -> Optional[dict[str, Any]]:
    m = re.search(
        r'<script id="__NEXT_DATA__" type="application/json">(.*?)</script>',
        html,
        re.DOTALL,
    )
    if not m:
        return None
    try:
        outer = json.loads(m.group(1))
        raw = outer.get("props", {}).get("initialState")
        if raw is None:
            return None
        if isinstance(raw, str):
            return json.loads(raw)
        if isinstance(raw, dict):
            return raw
    except (json.JSONDecodeError, TypeError, KeyError):
        return None
    return None


def _product_data_list(initial: dict[str, Any]) -> list[Any]:
    try:
        pd = initial["product"]["data"]
        return pd if isinstance(pd, list) else []
    except (KeyError, TypeError):
        return []


def _pair_map(data_list: list[Any]) -> dict[str, Any]:
    out: dict[str, Any] = {}
    for x in data_list:
        if isinstance(x, list) and len(x) == 2 and isinstance(x[0], str):
            out[x[0]] = x[1]
    return out


def parse_nutrition_from_energy_cost(text: Optional[str]) -> dict[str, Optional[float]]:
    """Из строки вида 'белки 8,5 г; жиры 1,5 г; ... ккал 250/ ...'."""
    if not text:
        return {
            "proteins_g": None,
            "fats_g": None,
            "carbohydrates_g": None,
            "calories_kcal": None,
        }
    t = text.lower().replace(",", ".")
    def one(pat: str) -> Optional[float]:
        m = re.search(pat, t, re.I)
        if not m:
            return None
        try:
            return float(m.group(1))
        except ValueError:
            return None
    return {
        "proteins_g": one(r"белк\w*\s*([\d.]+)"),
        "fats_g": one(r"жир\w*\s*([\d.]+)"),
        "carbohydrates_g": one(r"углевод\w*\s*([\d.]+)"),
        "calories_kcal": one(r"ккал\s*([\d.]+)"),
    }


def _breadcrumbs_category(bcr: Any) -> Optional[str]:
    if not isinstance(bcr, dict):
        return None
    data = bcr.get("data")
    if not isinstance(data, list):
        return None
    titles: list[str] = []
    for row in data:
        if isinstance(row, dict) and row.get("title"):
            titles.append(str(row["title"]).strip())
    if not titles:
        return None
    return " > ".join(titles)


def parse_product_from_initial_state(
    initial: dict[str, Any], url: str = ""
) -> Optional[dict[str, Any]]:
    """Разбор уже распарсенного Redux initialState (тесты и отладка)."""
    pmap = _pair_map(_product_data_list(initial))
    item = pmap.get("item")
    store = pmap.get("storeProduct")
    crumbs = pmap.get("breadcrumbs")
    if not isinstance(item, dict):
        return None

    slug = item.get("slug") or ""
    canonical = f"{BASE}/product/{slug}/" if slug else url

    price_rub = old_price_rub = None
    if isinstance(store, dict):
        # цены в копейках (минорных единицах) сайта
        pws = store.get("priceWithSale")
        prev = store.get("previousPrice") or store.get("price")
        if isinstance(pws, (int, float)):
            price_rub = float(pws) / 100.0
        if isinstance(prev, (int, float)):
            old_price_rub = float(prev) / 100.0

    gtin = item.get("gtin")
    codes: list[str] = []
    if isinstance(gtin, str) and gtin.strip().isdigit():
        codes.append(gtin.strip())
    for b in item.get("barcodes") or []:
        if isinstance(b, dict):
            c = b.get("code")
            if isinstance(c, str) and c.strip().isdigit() and c not in codes:
                codes.append(c.strip())

    nut = parse_nutrition_from_energy_cost(item.get("energyCost"))
    out = {
        "url": canonical,
        "green_internal_id": item.get("id"),
        "name": item.get("title") or "",
        "description": item.get("description"),
        "category": _breadcrumbs_category(crumbs),
        "price_rub": price_rub,
        "old_price_rub": old_price_rub,
        "producer": item.get("producer"),
        "barcodes": codes,
        "nutrition": {
            "proteins_g": nut["proteins_g"],
            "fats_g": nut["fats_g"],
            "carbohydrates_g": nut["carbohydrates_g"],
            "calories_kcal": nut["calories_kcal"],
            "energy_raw": item.get("energyCost"),
        },
    }
    return out


def parse_product_html(html: str, url: str) -> Optional[dict[str, Any]]:
    initial = parse_next_initial_state(html)
    if not initial:
        return None
    return parse_product_from_initial_state(initial, url)


def iter_product_urls_from_sitemap() -> Iterator[str]:
    idx_xml = fetch_text(f"{BASE}/sitemap.xml")
    if not idx_xml:
        return
    try:
        root = ET.fromstring(idx_xml)
    except ET.ParseError:
        return
    ns = {"sm": "http://www.sitemaps.org/schemas/sitemap/0.9"}
    for loc in root.findall(".//sm:sitemap/sm:loc", ns):
        if loc.text and "sub-sitemaps" in loc.text:
            sub = fetch_text(loc.text.strip())
            if not sub:
                continue
            try:
                subroot = ET.fromstring(sub)
            except ET.ParseError:
                continue
            for u in subroot.findall(".//sm:url/sm:loc", ns):
                if u.text and "/product/" in u.text:
                    yield u.text.strip().rstrip("/") + "/"


def apply_schema(conn: sqlite3.Connection) -> None:
    schema_path = Path(__file__).resolve().parent / "schema.sql"
    sql = schema_path.read_text(encoding="utf-8")
    conn.executescript(sql)
    conn.commit()


def upsert_product(conn: sqlite3.Connection, row: dict[str, Any]) -> int:
    cur = conn.cursor()
    cur.execute(
        """
        INSERT INTO products (
            green_internal_id, name, description, category, url,
            price_rub, old_price_rub, producer, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
        ON CONFLICT(url) DO UPDATE SET
            green_internal_id = excluded.green_internal_id,
            name = excluded.name,
            description = excluded.description,
            category = excluded.category,
            price_rub = excluded.price_rub,
            old_price_rub = excluded.old_price_rub,
            producer = excluded.producer,
            updated_at = excluded.updated_at
        """,
        (
            row.get("green_internal_id"),
            row["name"],
            row.get("description"),
            row.get("category"),
            row["url"],
            row.get("price_rub"),
            row.get("old_price_rub"),
            row.get("producer"),
        ),
    )
    cur.execute("SELECT id FROM products WHERE url = ?", (row["url"],))
    r = cur.fetchone()
    return int(r[0]) if r else -1


def replace_barcodes_and_nutrition(
    conn: sqlite3.Connection, product_id: int, barcodes: list[str], nutrition: dict[str, Any]
) -> None:
    cur = conn.cursor()
    cur.execute("DELETE FROM barcodes WHERE product_id = ?", (product_id,))
    cur.execute("DELETE FROM nutrition_facts WHERE product_id = ?", (product_id,))
    for code in barcodes:
        cur.execute(
            "INSERT OR IGNORE INTO barcodes (product_id, code) VALUES (?, ?)",
            (product_id, code),
        )
    cur.execute(
        """
        INSERT INTO nutrition_facts (
            product_id, proteins_g, fats_g, carbohydrates_g, calories_kcal, energy_raw
        ) VALUES (?, ?, ?, ?, ?, ?)
        """,
        (
            product_id,
            nutrition.get("proteins_g"),
            nutrition.get("fats_g"),
            nutrition.get("carbohydrates_g"),
            nutrition.get("calories_kcal"),
            nutrition.get("energy_raw"),
        ),
    )
    conn.commit()


def _postfix_name(name: Optional[str], max_len: int = 36) -> str:
    if not name:
        return ""
    s = str(name).replace("\n", " ").strip()
    if len(s) <= max_len:
        return s
    return s[: max_len - 1] + "…"


def run(
    db_path: str,
    limit: Optional[int] = None,
    delay_s: float = 0.15,
    *,
    progress: bool = True,
) -> None:
    conn = sqlite3.connect(db_path)
    try:
        apply_schema(conn)
        if progress:
            print("Сбор ссылок на товары из sitemap…", flush=True)
        if progress:
            urls = list(
                tqdm(
                    iter_product_urls_from_sitemap(),
                    desc="Sitemap",
                    unit=" URL",
                )
            )
        else:
            urls = list(iter_product_urls_from_sitemap())
        if limit is not None:
            urls = urls[:limit]
            if progress:
                print(f"Ограничение: обрабатываем {len(urls)} URL из каталога.", flush=True)

        ok = skip_net = skip_parse = skip_db = 0
        pbar = tqdm(urls, desc="Карточки", unit="шт") if progress else None
        for url in pbar or urls:
            if pbar is not None:
                pbar.set_postfix_str("", refresh=False)
            html = fetch_text(url)
            if not html:
                skip_net += 1
                if pbar is not None:
                    pbar.set_postfix_str("нет ответа", refresh=True)
                continue
            parsed = parse_product_html(html, url)
            if not parsed or not parsed.get("name"):
                skip_parse += 1
                if pbar is not None:
                    pbar.set_postfix_str("нет JSON/имени", refresh=True)
                continue
            pid = upsert_product(conn, parsed)
            if pid < 0:
                skip_db += 1
                if pbar is not None:
                    pbar.set_postfix_str("ошибка БД", refresh=True)
                continue
            replace_barcodes_and_nutrition(
                conn, pid, parsed.get("barcodes") or [], parsed.get("nutrition") or {}
            )
            ok += 1
            if pbar is not None:
                pbar.set_postfix_str(_postfix_name(parsed.get("name")), refresh=True)
            if delay_s:
                time.sleep(delay_s)
        print(
            f"Готово: записано карточек {ok}, пропуск (сеть) {skip_net}, "
            f"(парсинг) {skip_parse}, (БД) {skip_db}. Файл: {db_path}",
            flush=True,
        )
    finally:
        conn.close()


if __name__ == "__main__":
    import argparse

    ap = argparse.ArgumentParser()
    ap.add_argument("--db", default="products.db")
    ap.add_argument("--limit", type=int, default=None, help="Ограничить число карточек (тест)")
    ap.add_argument("--delay", type=float, default=0.15)
    ap.add_argument("--no-progress", action="store_true", help="Отключить tqdm (логи/CI)")
    args = ap.parse_args()
    run(args.db, limit=args.limit, delay_s=args.delay, progress=not args.no_progress)
    print("done:", args.db)
