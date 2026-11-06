"""Substances (compounds) generated from data/substances.csv. Called by generate.py."""
import csv
import json
import re
import zlib
from pathlib import Path

from PIL import Image

TIERS = {
    1: "Simple molecules",
    2: "Simple inorganic compounds",
    3: "Salts and minerals",
    4: "Organic compounds",
    5: "Complex organics",
    6: "Polymers",
}
SECTORS = {"industry": "Industry", "agriculture": "Agriculture", "both": "Industry and agriculture"}
STATES = {"solid", "liquid", "gas"}
SUBSCRIPT = str.maketrans("0123456789n", "₀₁₂₃₄₅₆₇₈₉ₙ")
TOKEN = re.compile(r"([A-Z][a-z]?|\d+|\(|\)|·|n)")


def parse_formula(formula, symbols):
    """Return {symbol: count} for one formula unit (polymer repeat unit for '(...)n')."""
    total = {}
    for part_index, part in enumerate(formula.split("·")):
        coeff = 1
        m = re.match(r"^(\d+)(.*)$", part)
        if part_index > 0 and m:
            coeff, part = int(m.group(1)), m.group(2)
        tokens = TOKEN.findall(part)
        if "".join(tokens) != part:
            raise ValueError(f"cannot parse formula {formula!r}")
        stack = [{}]
        i = 0
        while i < len(tokens):
            t = tokens[i]
            i += 1
            mult = 1
            if i < len(tokens) and tokens[i].isdigit():
                mult = int(tokens[i])
                i += 1
            if t == "(":
                stack.append({})
                if mult != 1:
                    raise ValueError(formula)
                continue
            if t == ")":
                group = stack.pop()
                for k, v in group.items():
                    stack[-1][k] = stack[-1].get(k, 0) + v * mult
                continue
            if t == "n":  # polymer marker: count one repeat unit
                continue
            if t == "D":  # deuterium counts as hydrogen
                t = "H"
            if t not in symbols:
                raise ValueError(f"unknown element {t!r} in {formula!r}")
            stack[-1][t] = stack[-1].get(t, 0) + mult
        if len(stack) != 1:
            raise ValueError(f"unbalanced parentheses in {formula!r}")
        for k, v in stack[0].items():
            total[k] = total.get(k, 0) + v * coeff
    return total


def display_formula(formula):
    """H2SO4 -> H₂SO₄, keeping hydrate coefficients (·2H2O -> ·2H₂O) full size."""
    out = []
    for part_index, part in enumerate(formula.split("·")):
        m = re.match(r"^(\d+)(.*)$", part) if part_index > 0 else None
        lead, body = (m.group(1), m.group(2)) if m else ("", part)
        out.append(lead + re.sub(r"(?<=[A-Za-z)])\d+|(?<=\))n", lambda g: g.group(0).translate(SUBSCRIPT), body))
    return "·".join(out)


def load_substances(path, elements):
    symbols = {e["symbol"] for e in elements}
    element_ids = {e["id"] for e in elements}
    with Path(path).open(newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    out = []
    for order, r in enumerate(rows):
        s = {
            "id": r["id"].strip(),
            "name": r["name"].strip(),
            "formula": r["formula"].strip(),
            "state": r["state"].strip(),
            "sector": r["sector"].strip(),
            "tier": int(r["tier"]),
            "level": int(r["level"]),
            "color": r["color"].strip().upper(),
            "use": r["use"].strip(),
        }
        assert re.fullmatch(r"[a-z0-9_]+", s["id"]), s
        assert s["id"] not in element_ids, f"{s['id']} clashes with an element"
        assert s["state"] in STATES, s
        assert s["sector"] in SECTORS, s
        assert s["tier"] in TIERS, s
        assert 0 <= s["level"] <= 7, s
        assert len(s["use"]) <= 55, f"use text too long for a tooltip line: {s['id']}"
        s["composition"] = parse_formula(s["formula"], symbols)
        s["atoms"] = sum(s["composition"].values())
        s["_order"] = order
        out.append(s)
    assert len({s["id"] for s in out}) == len(out), "duplicate substance id"
    # Simple to complex: by tier, then by atoms per formula unit, then file order.
    out.sort(key=lambda s: (s["tier"], s["atoms"], s["_order"]))
    return out


def _hex(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def _shade(rgb, f):
    return tuple(max(0, min(255, int(c * f))) for c in rgb)


def _lift(rgb, amount):
    return tuple(min(255, c + amount) for c in rgb)


def _gas_flask(px, color):
    glass_edge = (70, 90, 110, 255)
    cork = (150, 105, 60, 255)
    for y in range(0, 2):
        for x in range(6, 10):
            px[x, y] = cork
    for y in range(2, 6):
        px[6, y] = glass_edge
        px[9, y] = glass_edge
        for x in range(7, 9):
            px[x, y] = color + (120,)
    for y in range(5, 16):
        for x in range(1, 15):
            d = ((x - 7.5) ** 2 + (y - 10) ** 2) ** 0.5
            if d <= 5.6:
                edge = d > 4.7
                if edge:
                    px[x, y] = glass_edge
                elif x <= 5 and y <= 8:
                    px[x, y] = (240, 250, 255, 210)
                else:
                    px[x, y] = color + (150 if (x + y) % 3 else 115,)


def cylinder_texture(color):
    """A steel gas cylinder with a band of the gas's colour (compressed gas)."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    steel, steel_dark, steel_light = (130, 134, 140), (70, 72, 78), (190, 194, 200)
    for y in range(0, 3):
        for x in range(6, 10):
            px[x, y] = (steel_dark if (x in (6, 9) or y == 0) else steel) + (255,)
    for x in range(5, 11):
        px[x, 2] = steel_dark + (255,)
    for y in range(3, 16):
        inset = 1 if y in (3, 15) else 0
        for x in range(4 + inset, 12 - inset):
            if x in (4 + inset, 11 - inset) or y in (3, 15):
                px[x, y] = steel_dark + (255,)
            elif y in (4, 5, 6):  # the shoulder is painted in the gas's colour
                px[x, y] = (_lift(color, 30) if x == 5 + inset else color) + (255,)
            elif x == 5 + inset:
                px[x, y] = steel_light + (255,)
            elif x == 10 - inset:
                px[x, y] = _shade(steel, 0.8) + (255,)
            else:
                px[x, y] = steel + (255,)
    for x in range(5, 11):  # label band
        px[x, 10] = _shade(color, 0.7) + (255,)
    return img


def _texture(s):
    color = _hex(s["color"])
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    seed = zlib.crc32(s["id"].encode())

    def noise(x, y):
        return (seed ^ (x * 73856093) ^ (y * 19349663)) % 7

    if s["state"] == "liquid":  # Erlenmeyer flask
        glass_edge = (70, 90, 110, 255)
        glass = (200, 225, 240, 110)
        cork = (150, 105, 60, 255)
        for y in range(16):
            if y <= 5:
                l, r = 6, 9
            else:
                l, r = max(1, 6 - (y - 5)), min(14, 9 + (y - 5))
            if y >= 15:
                break
            for x in range(l, r + 1):
                if y <= 1:
                    px[x, y] = cork
                    continue
                edge = x in (l, r) or y == 14
                if edge:
                    px[x, y] = glass_edge
                elif y >= 9:
                    c = _lift(color, 35) if y == 9 else (color if noise(x, y) else _shade(color, 0.9))
                    px[x, y] = c + (255,)
                elif x == l + 1:
                    px[x, y] = (240, 250, 255, 200)
                else:
                    px[x, y] = glass
    elif s["state"] == "gas":  # stoppered round flask of faintly coloured gas
        _gas_flask(px, color)
    elif s["tier"] == 6:  # polymer pellets
        edge = _shade(color, 0.55) + (255,)
        for cx, cy in ((5, 6), (10, 5), (4, 11), (9, 10), (12, 12), (7, 14)):
            for dy in range(-2, 2):
                for dx in range(-2, 2):
                    if abs(dx + 0.5) + abs(dy + 0.5) > 2.5:
                        continue
                    x, y = cx + dx, cy + dy
                    if not (0 <= x < 16 and 0 <= y < 16):
                        continue
                    border = abs(dx + 0.5) + abs(dy + 0.5) > 1.5
                    if border:
                        px[x, y] = edge
                    elif dx == -1 and dy == -1:
                        px[x, y] = _lift(color, 50) + (255,)
                    else:
                        px[x, y] = color + (255,)
    else:  # powder pile
        edge = _shade(color, 0.55) + (255,)
        for y in range(5, 15):
            half = int(((y - 4) / 10) ** 0.5 * 7.5)
            for x in range(8 - half, 8 + half):
                if y == 14 or x == 8 - half or x == 8 + half - 1:
                    px[x, y] = edge
                else:
                    n = noise(x, y)
                    if n == 0:
                        c = _shade(color, 0.8)
                    elif n == 1:
                        c = _lift(color, 40)
                    else:
                        c = color
                    px[x, y] = c + (255,)
        top_half = int((1 / 10) ** 0.5 * 7.5)
        for x in range(8 - top_half, 8 + top_half):
            px[x, 4] = edge
    return img


def generate_substances(csv_path, elements, assets, runtime_dir, lang, mod_id, preview_path):
    subs = load_substances(csv_path, elements)

    runtime = [{
        "id": s["id"], "name": s["name"], "formula": display_formula(s["formula"]), "state": s["state"],
        "sector": s["sector"], "tier": s["tier"], "level": s["level"], "color": s["color"], "composition": s["composition"],
    } for s in subs]
    (runtime_dir / "substances.json").write_text(json.dumps(runtime, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    lang[f"itemGroup.{mod_id}.substances"] = "Substances"
    lang[f"tooltip.{mod_id}.formula"] = "Formula: %s"
    lang[f"tooltip.{mod_id}.tier"] = "Complexity tier %s: %s"
    for t, label in TIERS.items():
        lang[f"tier.{mod_id}.{t}"] = label
    for key, label in SECTORS.items():
        lang[f"sector.{mod_id}.{key}"] = label
    for s in subs:
        lang[f"item.{mod_id}.{s['id']}"] = s["name"]
        lang[f"item.{mod_id}.{s['id']}.use"] = s["use"]
        model = {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{s['id']}"}}
        (assets / "models" / "item" / f"{s['id']}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
        _texture(s).save(assets / "textures" / "item" / f"{s['id']}.png")

    cols = 14
    rows = (len(subs) + cols - 1) // cols
    sheet = Image.new("RGBA", (cols * 18, rows * 18), (40, 40, 46, 255))
    for i, s in enumerate(subs):
        sheet.alpha_composite(_texture(s), ((i % cols) * 18 + 1, (i // cols) * 18 + 1))
    sheet.resize((sheet.width * 4, sheet.height * 4), Image.NEAREST).save(preview_path)
    return subs
