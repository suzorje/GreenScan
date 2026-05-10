import json
import unittest
from pathlib import Path

from green_parser import (
    parse_nutrition_from_energy_cost,
    parse_product_from_initial_state,
)

_FIX = Path(__file__).resolve().parent / "fixtures" / "green_product_state.json"


class TestGreenParser(unittest.TestCase):
    def test_nutrition_regex(self) -> None:
        s = "белки 8,5 г; жиры 1,5 г; углеводы 50,0 г; энергетическая ценность ккал 250/ кДж 1060."
        n = parse_nutrition_from_energy_cost(s)
        self.assertAlmostEqual(n["proteins_g"], 8.5, places=2)
        self.assertAlmostEqual(n["fats_g"], 1.5, places=2)
        self.assertAlmostEqual(n["carbohydrates_g"], 50.0, places=2)
        self.assertAlmostEqual(n["calories_kcal"], 250.0, places=2)

    def test_fixture_baget(self) -> None:
        raw = json.loads(_FIX.read_text(encoding="utf-8"))
        initial = {"product": raw["product"]}
        d = parse_product_from_initial_state(initial, "")
        self.assertIsNotNone(d)
        assert d is not None
        self.assertIn("Багет", d["name"])
        self.assertEqual(d["barcodes"], ["4816413223846"])
        self.assertAlmostEqual(d["price_rub"], 1.29, places=2)
        self.assertAlmostEqual(d["old_price_rub"], 1.49, places=2)
        self.assertIsNotNone(d["nutrition"]["proteins_g"])


if __name__ == "__main__":
    unittest.main()
