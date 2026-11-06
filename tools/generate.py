#!/usr/bin/env python3
"""Generate every per-element resource from data/elements.csv.

Run from the project root:  python3 tools/generate.py

Outputs (all overwritten each run):
  src/main/resources/periodic_elements/elements.json   runtime element table read by the mod
  src/main/resources/periodic_elements/substances.json runtime substance table (from data/substances.csv)
  src/main/resources/assets/periodic_elements/lang/en_us.json
  src/main/resources/assets/periodic_elements/models/item/<id>.json
  src/main/resources/assets/periodic_elements/textures/item/<id>.png
  src/main/resources/assets/periodic_elements/icon.png
"""
import csv
import json
import shutil
import sys
from pathlib import Path

from PIL import Image

from compositions import generate_compositions
from machines import generate_machines, GUI_LANG, ITEMS as MACHINE_ITEMS
from ores import generate_ores
from research import generate_research
from rotations import randomize_rotations, randomize_vanilla_rotations
from substances import generate_substances

MOD_ID = "periodic_elements"
ROOT = Path(__file__).resolve().parent.parent
CSV_PATH = ROOT / "data" / "elements.csv"
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / MOD_ID
# Languages besides English; each needs data/lang/<code>.json, data/lang/vanilla_<code>.json, tools/guide_<code>.py.
LANGUAGES = [c for c in ("pt_br", "fr_fr", "es_es", "nl_nl", "it_it", "de_de", "sv_se", "da_dk", "ru_ru")
             if (ROOT / "data" / "lang" / f"{c}.json").exists()]

STATES = {"solid", "liquid", "gas", "unknown"}
CATEGORIES = {
    "alkali_metal": "Alkali Metal",
    "alkaline_earth_metal": "Alkaline Earth Metal",
    "transition_metal": "Transition Metal",
    "post_transition_metal": "Post-Transition Metal",
    "metalloid": "Metalloid",
    "reactive_nonmetal": "Reactive Nonmetal",
    "noble_gas": "Noble Gas",
    "lanthanide": "Lanthanide",
    "actinide": "Actinide",
    "unknown": "Unknown Properties",
}

# Historical tech levels (see the design plan).
LEVELS = {
    0: "Stone Age",
    1: "Bronze and Iron Age",
    2: "Alchemy and early modern",
    3: "Chemical Revolution",
    4: "Industrial chemistry",
    5: "Early 20th century",
    6: "Nuclear age",
    7: "Accelerator age",
}

# 3x5 pixel font. '#' = ink.
FONT = {
    "0": ["###", "#.#", "#.#", "#.#", "###"],
    "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["##.", "..#", ".#.", "#..", "###"],
    "3": ["##.", "..#", ".#.", "..#", "##."],
    "4": ["#.#", "#.#", "###", "..#", "..#"],
    "5": ["###", "#..", "##.", "..#", "##."],
    "6": [".##", "#..", "###", "#.#", "###"],
    "7": ["###", "..#", ".#.", ".#.", ".#."],
    "8": ["###", "#.#", "###", "#.#", "###"],
    "9": ["###", "#.#", "###", "..#", "##."],
    "A": [".#.", "#.#", "###", "#.#", "#.#"],
    "B": ["##.", "#.#", "##.", "#.#", "##."],
    "C": [".##", "#..", "#..", "#..", ".##"],
    "D": ["##.", "#.#", "#.#", "#.#", "##."],
    "E": ["###", "#..", "##.", "#..", "###"],
    "F": ["###", "#..", "##.", "#..", "#.."],
    "G": [".##", "#..", "#.#", "#.#", ".##"],
    "H": ["#.#", "#.#", "###", "#.#", "#.#"],
    "I": ["###", ".#.", ".#.", ".#.", "###"],
    "K": ["#.#", "#.#", "##.", "#.#", "#.#"],
    "L": ["#..", "#..", "#..", "#..", "###"],
    "M": ["#.#", "###", "###", "#.#", "#.#"],
    "N": ["##.", "#.#", "#.#", "#.#", "#.#"],
    "O": [".#.", "#.#", "#.#", "#.#", ".#."],
    "P": ["##.", "#.#", "##.", "#..", "#.."],
    "R": ["##.", "#.#", "##.", "#.#", "#.#"],
    "S": [".##", "#..", ".#.", "..#", "##."],
    "T": ["###", ".#.", ".#.", ".#.", ".#."],
    "U": ["#.#", "#.#", "#.#", "#.#", "###"],
    "V": ["#.#", "#.#", "#.#", "#.#", ".#."],
    "W": ["#.#", "#.#", "###", "###", "#.#"],
    "X": ["#.#", "#.#", ".#.", "#.#", "#.#"],
    "Y": ["#.#", "#.#", ".#.", ".#.", ".#."],
    "Z": ["###", "..#", ".#.", "#..", "###"],
    "a": ["...", ".##", "#.#", "#.#", ".##"],
    "b": ["#..", "##.", "#.#", "#.#", "##."],
    "c": ["...", ".##", "#..", "#..", ".##"],
    "d": ["..#", ".##", "#.#", "#.#", ".##"],
    "e": ["...", ".#.", "###", "#..", ".##"],
    "f": [".##", "#..", "##.", "#..", "#.."],
    "g": ["...", ".##", "#.#", ".##", "##."],
    "h": ["#..", "##.", "#.#", "#.#", "#.#"],
    "i": [".#.", "...", ".#.", ".#.", ".#."],
    "k": ["#..", "#.#", "##.", "#.#", "#.#"],
    "l": ["##.", ".#.", ".#.", ".#.", "###"],
    "m": ["...", "##.", "###", "#.#", "#.#"],
    "n": ["...", "##.", "#.#", "#.#", "#.#"],
    "o": ["...", ".#.", "#.#", "#.#", ".#."],
    "p": ["...", "##.", "#.#", "##.", "#.."],
    "r": ["...", "#.#", "##.", "#..", "#.."],
    "s": ["...", ".##", ".#.", "..#", "##."],
    "t": [".#.", "###", ".#.", ".#.", ".##"],
    "u": ["...", "#.#", "#.#", "#.#", ".##"],
    "v": ["...", "#.#", "#.#", "#.#", ".#."],
    "y": ["...", "#.#", "#.#", ".##", "##."],
}

# Tiny state markers (3x3), drawn in the top-right corner of the tile.
STATE_MARK = {
    "solid": ["###", "###", "###"],
    "liquid": [".#.", "###", ".#."],
    "gas": ["#.#", "...", "#.#"],
    "unknown": ["##.", "..#", ".#."],
}


def hex_rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def shade(rgb, f):
    return tuple(max(0, min(255, int(c * f))) for c in rgb)


def luminance(rgb):
    r, g, b = (c / 255 for c in rgb)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def draw_glyphs(px, text, x, y, color):
    for ch in text:
        glyph = FONT[ch]
        for gy, row in enumerate(glyph):
            for gx, cell in enumerate(row):
                if cell == "#":
                    px[x + gx, y + gy] = color
        x += 4


def text_width(text):
    return len(text) * 4 - 1


def make_texture(el):
    """A 16x16 periodic-table tile: number top-left, state mark top-right, symbol centered."""
    base = hex_rgb(el["color"])
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    border = shade(base, 0.55)
    highlight = shade(base, 1.0) if luminance(base) > 0.9 else tuple(min(255, c + 40) for c in base)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                px[x, y] = border + (255,)
            elif x == 1 or y == 1:
                px[x, y] = highlight + (255,)
            else:
                px[x, y] = base + (255,)
    # Clip the outer corners for a rounded look.
    for cx, cy in ((0, 0), (15, 0), (0, 15), (15, 15)):
        px[cx, cy] = (0, 0, 0, 0)

    ink = (20, 20, 24, 255) if luminance(base) > 0.45 else (245, 245, 245, 255)
    num = str(el["number"])
    if len(num) <= 2:
        draw_glyphs(px, num, 2, 2, ink)
    else:  # three digits fill the top row; skip the state mark
        draw_glyphs(px, num, 2, 2, ink)
    if len(num) <= 2:
        for my, row in enumerate(STATE_MARK[el["state"]]):
            for mx, cell in enumerate(row):
                if cell == "#":
                    px[11 + mx, 3 + my] = ink

    sym = el["symbol"]
    w = text_width(sym)
    draw_glyphs(px, sym, (16 - w) // 2, 9, ink)
    return img


def load_elements():
    with CSV_PATH.open(newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    elements = []
    for r in rows:
        el = {
            "number": int(r["number"]),
            "symbol": r["symbol"].strip(),
            "name": r["name"].strip(),
            "state": r["state"].strip(),
            "category": r["category"].strip(),
            "color": r["color"].strip().upper(),
        }
        el["discovered"] = r["discovered"].strip()
        el["level"] = int(r["level"])
        el["id"] = el["name"].lower()
        assert el["state"] in STATES, el
        assert el["category"] in CATEGORIES, el
        assert len(el["color"]) == 6, el
        assert el["level"] in LEVELS, el
        for ch in el["symbol"] + str(el["number"]):
            assert ch in FONT, f"no glyph for {ch!r} ({el['name']})"
        elements.append(el)
    numbers = [e["number"] for e in elements]
    assert numbers == list(range(1, len(elements) + 1)), "atomic numbers must be 1..N in order"
    assert len({e["symbol"] for e in elements}) == len(elements), "duplicate symbol"
    return elements


def main():
    elements = load_elements()

    for sub in ("models/item", "textures/item"):
        d = ASSETS / sub
        if d.exists():
            shutil.rmtree(d)
        d.mkdir(parents=True)
    (ASSETS / "lang").mkdir(parents=True, exist_ok=True)
    (RES / MOD_ID).mkdir(parents=True, exist_ok=True)

    runtime = [{k: e[k] for k in ("id", "number", "symbol", "name", "state", "category", "color", "level", "discovered")} for e in elements]
    (RES / MOD_ID / "elements.json").write_text(json.dumps(runtime, indent=2) + "\n", encoding="utf-8")

    lang = {"itemGroup.periodic_elements.elements": "Periodic Table"}
    for state in sorted(STATES):
        lang[f"state.periodic_elements.{state}"] = state.capitalize()
    for key, label in CATEGORIES.items():
        lang[f"category.periodic_elements.{key}"] = label
    lang["tooltip.periodic_elements.symbol"] = "Symbol: %s"
    lang["tooltip.periodic_elements.number"] = "Atomic number: %s"
    lang["tooltip.periodic_elements.state"] = "State at room temperature: %s"
    lang["tooltip.periodic_elements.level"] = "Tech level %s: %s"
    lang["tooltip.periodic_elements.discovered"] = "Isolated: %s"
    lang["tooltip.periodic_elements.discovered.ancient"] = "Known since antiquity"
    lang["tooltip.periodic_elements.composition"] = "Yields (%s units):"
    lang["item.periodic_elements.gangue"] = "Gangue (ordinary rock)"
    lang["tooltip.periodic_elements.composition.line"] = " %s %s"
    lang["tooltip.periodic_elements.composition.more"] = " ...and %s more (hold Shift)"
    lang["tooltip.periodic_elements.ghast_tear_anode"] = "Anode: in an electric machine's upgrade slot, electrolysis that makes chlorine uses a quarter less power"  # 3.46.0
    for lvl, label in LEVELS.items():
        lang[f"level.periodic_elements.{lvl}"] = label
    for e in elements:
        lang[f"item.{MOD_ID}.{e['id']}"] = e["name"]
        model = {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD_ID}:item/{e['id']}"}}
        (ASSETS / "models" / "item" / f"{e['id']}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
        make_texture(e).save(ASSETS / "textures" / "item" / f"{e['id']}.png")
    subs = generate_substances(ROOT / "data" / "substances.csv", elements, ASSETS, RES / MOD_ID, lang, MOD_ID,
                               ROOT / "preview-substances.png")
    from trees import composition_rows
    from shrubs import composition_rows as shrub_rows
    from geo_ores import composition_rows as geo_composition_rows
    from crops import composition_rows as crop_rows
    comps, factors = generate_compositions(ROOT / "data" / "compositions.csv", ROOT / "data" / "rarity.csv",
                                           elements, subs, RES / MOD_ID, composition_rows(ROOT) + shrub_rows() + geo_composition_rows(ROOT) + crop_rows()
                                           + __import__('vanilla_blocks').composition_rows() + __import__('geo_rocks').composition_rows() + __import__('fauna').composition_rows(),
                                           fallback_rows=__import__('crafted').composition_rows(ROOT))
    __import__('vanilla_blocks').write_none(RES / MOD_ID)
    from fluids import generate_fluids
    liquids, gases = generate_fluids(ASSETS, RES / MOD_ID, lang, MOD_ID, elements, subs, comps)
    print(f"  {len(liquids)} liquids and {len(gases)} gases in flasks; {len(gases)} gas cylinders")
    for el, f in sorted(factors.items(), key=lambda kv: kv[1]):
        print(f"  rarity factor {el}: {f:.3f}")
    ores, raw_items, ore_drops = generate_ores(ASSETS, RES / "data" / MOD_ID, RES / MOD_ID, lang, MOD_ID,
                                               {e["id"] for e in elements} | {x["id"] for x in subs})
    print(f"  {len(ores)} ores")
    from geo_rocks import generate_geo_rocks, PICKAXE as ROCK_PICKAXE
    print(f"  {len(generate_geo_rocks(ASSETS, RES / 'data' / MOD_ID, RES / MOD_ID, lang))} blocks of new rocks (Geological world)")
    import ores as _ores
    _ores.PICKAXE.extend(ROCK_PICKAXE)
    from geo_rocks import generate_soils
    print(f"  {len(generate_soils(ASSETS, RES / 'data' / MOD_ID, RES / MOD_ID, lang))} soils by rock (Geological world)")
    from geo_ores import generate_geo_ores
    print(f"  {len(generate_geo_ores(ASSETS, RES / 'data' / MOD_ID, RES / MOD_ID, lang))} ores in other rocks (Geological world)")
    research = generate_research(ROOT / "data" / "research.csv",
                                 {e["id"] for e in elements} | {x["id"] for x in subs} | set(raw_items)
                                 | set(MACHINE_ITEMS) | {"copper_wire"} | set(__import__("crops").items()),
                                 RES / MOD_ID, lang, MOD_ID)
    print(f"  {len(research)} research projects ({sum(p['key'] for p in research.values())} key)")
    reactions, machines = generate_machines(ROOT, ASSETS, RES / "data" / MOD_ID, RES / MOD_ID, lang, MOD_ID,
                                            subs, elements, comps, research)
    from gear import generate_gear
    gear = generate_gear(ASSETS, RES / "data" / MOD_ID, RES / MOD_ID, lang, MOD_ID, research)
    print(f"  {len(gear)} gear materials")
    from farm import generate_farm
    farm_blocks = generate_farm(ASSETS, RES / "data" / MOD_ID, RES / MOD_ID, lang, MOD_ID)
    from trees import generate_trees, write_item_textures
    tree_species = generate_trees(ROOT, ASSETS, RES / "data", RES / MOD_ID, lang, MOD_ID)
    write_item_textures(ASSETS)
    print(f"  {len(tree_species)} tree species")
    from shrubs import generate_shrubs
    print(f"  {len(generate_shrubs(ASSETS, RES / 'data', RES / MOD_ID, lang, MOD_ID))} shrub species")
    from crops import generate_crops
    print(f"  {len(generate_crops(ASSETS, RES / 'data', RES / MOD_ID, lang, MOD_ID))} crops")
    from compendium import write_runtime as write_compendium
    print(f"  {write_compendium(ROOT, RES / MOD_ID, lang, MOD_ID)} Tree Compendium entries")
    from industry import generate_industry
    generate_industry(ASSETS, RES / "data", lang, MOD_ID)
    from mining import generate_mining
    generate_mining(ASSETS, RES / "data", RES / MOD_ID, lang, MOD_ID)
    from atlas import atlas_block_assets
    atlas_block_assets(ASSETS, RES / "data", lang, MOD_ID)
    from multiblock import generate_multiblock
    generate_multiblock(ASSETS, RES / "data", lang, MOD_ID)
    from tanks import generate_tanks
    generate_tanks(ASSETS, RES / "data", lang, MOD_ID)
    from progress_book import generate_progress_book
    generate_progress_book(ASSETS, lang, MOD_ID)
    from emi import LANG as EMI_LANG
    lang.update(EMI_LANG)
    from jade import LANG as JADE_LANG
    lang.update(JADE_LANG)
    from search import LANG as SEARCH_LANG
    lang.update(SEARCH_LANG)
    from vent import LANG as VENT_LANG
    lang.update(VENT_LANG)
    from sample_case import generate_sample_case
    generate_sample_case(ASSETS, RES / "data", lang, MOD_ID)
    from logistics import generate_logistics
    generate_logistics(ASSETS, RES / "data", lang, MOD_ID)
    from water import generate_water
    generate_water(ASSETS, RES / "data", lang, MOD_ID)
    from exploration import generate_exploration
    generate_exploration(ASSETS, RES / "data", lang, MOD_ID)
    from fauna import generate_fauna
    print(f"  {len(generate_fauna(ASSETS, RES / 'data', RES / MOD_ID, lang, MOD_ID))} animal species")
    from lights import generate_lights
    print(f"  {generate_lights(ROOT, RES / MOD_ID, MOD_ID)} light sources in lights.json")
    from creative_items import generate_creative
    print(f"  {len(generate_creative(ASSETS, lang, MOD_ID))} creative testing blocks and items")
    # Last: natural blocks get random rotations (after every generator has written its blockstates).
    print(f"  {randomize_rotations(ASSETS, MOD_ID)} blocks with random rotation")
    print(f"  {randomize_vanilla_rotations(RES)} vanilla blocks with random rotation")
    lang.update(GUI_LANG)
    check_samples_obtainable(research, reactions, ore_drops, raw_items, machines)
    check_elements_obtainable(elements, reactions, ore_drops, comps)
    check_unlocks(research, reactions, machines, gear)
    check_textures()
    write_lens(elements, subs, reactions, comps)
    from isotopes import write_isotopes
    natural = write_isotopes(RES / MOD_ID, elements, reactions)
    write_radiation(elements, subs, comps, natural)
    print(f"  {len(reactions)} reactions in {len(machines)} machines")
    from guidebook import write_guidebook
    # Translations (pt_br 2.4.0, six more 2.5.0): data/lang/<code>.json maps each English string to its translation.
    translations = {}
    for code in LANGUAGES:
        tr = json.loads((ROOT / "data" / "lang" / f"{code}.json").read_text(encoding="utf-8"))
        from trees import translations as tree_translations
        from multiblock import translations as multiblock_translations
        tr = {**tr, **tree_translations(code), **multiblock_translations(code)}
        from compendium import translations as compendium_translations
        tr = {**tr, **compendium_translations(code)}
        from shrubs import translations as shrub_translations
        tr = {**tr, **shrub_translations(code)}
        from crops import translations as crop_translations
        tr = {**tr, **crop_translations(code)}
        from geo_rocks import translations as geo_rock_translations
        tr = {**tr, **geo_rock_translations(code)}
        from geo_ores import translations as geo_ore_translations
        tr = {**tr, **geo_ore_translations(code)}
        from progress_book import translations as progress_book_translations
        tr = {**tr, **progress_book_translations(code)}
        from emi import translations as emi_translations
        tr = {**tr, **emi_translations(code)}
        from jade import translations as jade_translations
        tr = {**tr, **jade_translations(code)}
        from search import translations as search_translations
        tr = {**tr, **search_translations(code)}
        from vent import translations as vent_translations
        tr = {**tr, **vent_translations(code)}
        from tanks import translations as tank_translations
        tr = {**tr, **tank_translations(code)}
        from sample_case import translations as sample_case_translations
        tr = {**tr, **sample_case_translations(code)}
        from logistics import translations as logistics_translations
        tr = {**tr, **logistics_translations(code)}
        from water import translations as water_translations
        tr = {**tr, **water_translations(code)}
        from exploration import translations as exploration_translations
        tr = {**tr, **exploration_translations(code)}
        from fauna import translations as fauna_translations
        tr = {**tr, **fauna_translations(code)}
        from creative_items import translations as creative_translations
        tr = {**tr, **creative_translations(code)}
        translations[code] = {k: tr[v] for k, v in lang.items() if v in tr}
        missing = sorted({v for v in lang.values() if v not in tr})
        if missing:
            print(f"  {code}: {len(missing)} strings untranslated, e.g. {missing[:3]}")
    from creative_tabs import write as write_tabs
    unplaced = write_tabs(RES / MOD_ID, ASSETS / "models" / "item")
    if unplaced:
        sys.exit(f"items with no creative tab: {unplaced}")
    print(f"  field guide: {write_guidebook(RES / MOD_ID, lang, MOD_ID, research, reactions, machines, {c: {**lang, **t} for c, t in translations.items()})} paragraphs")
    from emi import log_tags
    tag_dir = RES / "data" / MOD_ID / "tags" / "item"
    lang.update(log_tags(tag_dir, "en_us", lang))
    for code, t in translations.items():
        t.update(log_tags(tag_dir, code, {**lang, **t}))
    (ASSETS / "lang" / "en_us.json").write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    for code, t in translations.items():
        (ASSETS / "lang" / f"{code}.json").write_text(json.dumps(t, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    # Mod icon (3.51.1): the "El" tile drawn in tools/icon.png; images/icon-512.png is the Modrinth copy.
    Image.open(Path(__file__).parent / "icon.png").save(ASSETS / "icon.png")

    # Preview sheet laid out like the periodic table (not shipped in the jar).
    sheet = Image.new("RGBA", (18 * 17, 10 * 17 + 4), (40, 40, 46, 255))
    for e in elements:
        col, row = table_position(e["number"])
        sheet.alpha_composite(make_texture(e), (col * 17, row * 17 + (4 if row >= 8 else 0)))
    sheet.resize((sheet.width * 4, sheet.height * 4), Image.NEAREST).save(ROOT / "preview.png")
    print(f"Generated resources for {len(elements)} elements and {len(subs)} substances, compositions for {len(comps)} blocks and items.")


def check_samples_obtainable(research, reactions, ore_drops, raw_items, machines):
    """Every mod item a research project asks for must come from an ore or a machine."""
    from machines import RECIPES, SHAPELESS
    sources = set(ore_drops) | {f"{MOD_ID}:{r}" for r in raw_items} | {f"{MOD_ID}:{r}" for r in list(RECIPES) + list(SHAPELESS)}
    sources |= {f"{MOD_ID}:{m['residue']}" for m in machines.values()}
    from crops import items as crop_items
    sources |= {f"{MOD_ID}:{i}" for i in crop_items()}
    sources |= reactor_outputs()
    sources |= {f"{MOD_ID}:seawater_bucket", f"{MOD_ID}:brine_bucket"}  # 3.45.0: filled from the sea and brine springs
    for r in reactions:
        for p in r["products"]:
            if not p["vented"] and not p["slag"]:
                sources.add(p["id"][5:] if p["id"].startswith("item:") else f"{MOD_ID}:{p['id']}")
    for p in research.values():
        for s in p["samples"]:
            choices = s["item"].split("|")
            if all(c.startswith(MOD_ID + ":") for c in choices):
                assert any(c in sources for c in choices), f"research {p['id']} needs {s['item']}, which nothing produces"


# Standard atomic weights for the elements that appear in substance formulas.
ATOMIC_WEIGHT = {"H": 1.008, "Li": 6.94, "Be": 9.012, "B": 10.81, "C": 12.011, "N": 14.007, "O": 15.999,
                 "Na": 22.990, "Mg": 24.305, "Al": 26.982, "Si": 28.085, "P": 30.974, "S": 32.06, "Cl": 35.45,
                 "K": 39.098, "Ca": 40.078, "Ti": 47.867, "Mn": 54.938, "Fe": 55.845, "Co": 58.933, "Cu": 63.546,
                 "Zn": 65.38, "As": 74.922, "Br": 79.904, "Sr": 87.62, "Cd": 112.41, "Sn": 118.71, "Sb": 121.76,
                 "I": 126.90, "Ba": 137.33, "Hg": 200.59, "Pb": 207.2, "U": 238.03,
                 "F": 18.998, "V": 50.942, "Ni": 58.693, "Ga": 69.723, "Ge": 72.630, "Rb": 85.468, "Y": 88.906,
                 "Zr": 91.224, "In": 114.82, "Cs": 132.91, "Ce": 140.12, "W": 183.84, "Tl": 204.38, "Ra": 226.0,
                 "Th": 232.04, "Cr": 51.996, "Mo": 95.95, "Re": 186.21, "Nb": 92.906, "Ta": 180.95,
                 "Hf": 178.49, "La": 138.91, "Pr": 140.91, "Nd": 144.24, "Sc": 44.956, "Sm": 150.36, "Eu": 151.96,
                 "Gd": 157.25, "Tb": 158.93, "Dy": 162.50, "Ho": 164.93, "Er": 167.26, "Tm": 168.93, "Yb": 173.05, "Lu": 174.97,
                 # Man-made elements: the mass of the isotope the game makes.
                 "Np": 237.05, "Pu": 239.05, "Am": 241.06, "Cm": 244.06, "Pm": 146.92}


def write_lens(elements, subs, reactions, comps):
    """lens.json for the Magnifying Lens: each substance's elements by mass, and which research makes each element.

    An element is known to a player once they know a project whose reaction gives it (a reaction with no project,
    or a level 0 element, is known from the start)."""
    by_symbol = {e["symbol"]: e["id"] for e in elements}
    fractions = {}
    for sub in subs:
        masses = {by_symbol[sym]: ATOMIC_WEIGHT[sym] * n for sym, n in sub["composition"].items()}
        total = sum(masses.values())
        fractions[sub["id"]] = {el: round(m / total, 6) for el, m in masses.items()}
    el_ids = {e["id"] for e in elements}
    sources = {}
    always = {e["id"] for e in elements if e["level"] == 0}
    for r in reactions:
        for p in r["products"]:
            if p["vented"]:
                continue
            pid = p["id"]
            if pid.startswith("item:"):
                c = comps.get(pid[5:])
                el = c["parts"][0]["id"] if c and c["parts"] and c["parts"][0]["units"] > 0.95 * c["amount"] else None
                el = TRACE_CARRIERS.get(pid.split(":")[-1], el)
            else:
                el = pid
            if el in el_ids:
                if r["project"] is None:
                    always.add(el)
                else:
                    sources.setdefault(el, set()).add(r["project"])
    for el, project in IRRADIATION_LENS.items():
        sources.setdefault(el, set()).add(project)
    missing = [e["name"] for e in elements if e["level"] <= RELEASED_LEVEL and e["id"] not in always | set(sources)
               and e["id"] not in NUCLEAR_PENDING]
    assert not missing, f"lens: no research reveals {missing}"
    (RES / MOD_ID / "lens.json").write_text(json.dumps({
        "alwaysKnown": sorted(always), "sources": {k: sorted(v) for k, v in sorted(sources.items())},
        "substances": fractions}, indent=1) + "\n", encoding="utf-8")


# Gamma dose rate at 1 m, in Sv per hour per gram (1 unit) of the element. Radium and radon are counted with
# their short-lived daughters (the classic radium constant is 8.25 mSv/h per gram at 1 m); uranium and thorium
# as natural metal; actinium freshly separated (its daughters grow in with decay in 0.9.0). Values are rounded.
# The man-made elements count the isotope the game makes (see DECAY and ISOTOPES); californium-252 counts its
# neutrons too. Fission products are taken as spent fuel after ten years of cooling (mostly caesium-137).
GAMMA_SV_PER_H_PER_G = {"radium": 8.25e-3, "radon": 1.46e3, "polonium": 1.9e-4, "actinium": 7e-5,
                        "uranium": 2.5e-9, "thorium": 3e-9, "protactinium": 5e-5,
                        "neptunium": 1e-7, "plutonium": 2e-7, "americium": 4e-4, "curium": 2e-5, "promethium": 1e-6,
                        "astatine": 10.0, "berkelium": 1e-3, "californium": 2.3e3, "einsteinium": 0.1, "fermium": 0.1,
                        "mendelevium": 0.1, "fission_products": 0.01}
# Blocks whose contents don't count toward the dose around them: lead walls and water.
SHIELDED = ["periodic_elements:hot_cell", "periodic_elements:nuclear_reactor"]

# Items that decay on Minecraft's clock (one game day = 24 hours, so 72 times real time):
# item -> (nuclide, half-life in days, the item it becomes or None when nothing worth keeping is left).
DECAY = {
    "periodic_elements:polonium": ("Po-210", 138.4, "periodic_elements:lead"),
    "periodic_elements:radon_seed": ("Rn-222", 3.82, "periodic_elements:gold"),
    "periodic_elements:astatine_sample": ("At-211", 0.3, None),
    "periodic_elements:berkelium_sample": ("Bk-249", 330.0, "periodic_elements:californium_sample"),
    "periodic_elements:californium_sample": ("Cf-252", 965.7, None),
    "periodic_elements:einsteinium_sample": ("Es-253", 20.47, "periodic_elements:berkelium_sample"),
    "periodic_elements:fermium_sample": ("Fm-257", 100.5, "periodic_elements:californium_sample"),
    "periodic_elements:mendelevium_sample": ("Md-258", 51.5, "periodic_elements:einsteinium_sample"),
    "periodic_elements:tritium_vial": ("H-3", 4500.0, None),
}
# The isotope each radioactive element item stands for, shown in its tooltip.
ISOTOPES = {
    "technetium": "Tc-99, half-life 211,000 years", "promethium": "Pm-147, half-life 2.6 years",
    "polonium": "Po-210, half-life 138 days", "astatine": "At-211, half-life 7.2 hours",
    "radon": "Rn-222, half-life 3.8 days", "francium": "Fr-223, half-life 22 minutes",
    "radium": "Ra-226, half-life 1,600 years", "actinium": "Ac-227, half-life 22 years",
    "thorium": "Th-232, half-life 14 billion years", "protactinium": "Pa-231, half-life 32,800 years",
    "uranium": "99.3% U-238 (4.5 billion years), 0.72% U-235", "neptunium": "Np-237, half-life 2.1 million years",
    "plutonium": "Pu-239, half-life 24,100 years", "americium": "Am-241, half-life 432 years",
    "curium": "Cm-244, half-life 18 years", "berkelium": "Bk-249, half-life 330 days",
    "californium": "Cf-252, half-life 2.6 years", "einsteinium": "Es-253, half-life 20.5 days",
    "fermium": "Fm-257, half-life 100 days", "mendelevium": "Md-258, half-life 51 days",
}


# Cherenkov glow (2.3.0, client only). Brightness follows the dose rate of ONE item or block at 1 m (how concentrated
# the activity is, not how much there is): in air only from airThreshold up, in or beside water from waterThreshold up.
# Running machines glow as if they were an item of the given dose rate.
CHERENKOV = {
    "airThreshold": 0.1, "waterThreshold": 1e-4, "decades": 6.0,
    "machines": {"nuclear_reactor": 1e4, "heavy_ion_accelerator": 10.0, "cyclotron": 1.0,
                 "radioisotope_generator": 1e-3},
    "color": [70, 140, 255],
}


def write_radiation(elements, subs, comps, natural):
    ids = {e["id"] for e in elements} | {s["id"] for s in subs}
    assert set(GAMMA_SV_PER_H_PER_G) <= ids, set(GAMMA_SV_PER_H_PER_G) - ids
    assert set(ISOTOPES) <= ids
    decay = {}
    for item, (nuclide, days, daughter) in DECAY.items():
        assert item in comps and (daughter is None or daughter in comps), item
        decay[item] = {"nuclide": nuclide, "halfLifeTicks": round(days * 24000), "daughter": daughter or ""}
    (RES / MOD_ID / "decay.json").write_text(json.dumps({"items": decay, "isotopes": ISOTOPES,
                                                                         "natural": natural}, indent=1) + "\n",
                                             encoding="utf-8")
    (RES / MOD_ID / "radiation.json").write_text(json.dumps({
        "gamma": GAMMA_SV_PER_H_PER_G, "shielded": SHIELDED,
        "carriedDistance": 0.5, "blockRadius": 6, "intervalTicks": 40, "milderFactor": 0.1,
        "repairHalfLifeHours": 72.0, "cherenkov": CHERENKOV}, indent=1) + "\n", encoding="utf-8")


def check_textures():
    """Every texture a block or item model names must exist, or the game shows the purple and black square."""
    missing = []
    for kind in ("block", "item"):
        for f in sorted((ASSETS / "models" / kind).glob("*.json")):
            for ref in json.loads(f.read_text(encoding="utf-8")).get("textures", {}).values():
                if ref.startswith(MOD_ID + ":") and not (ASSETS / "textures" / (ref.split(":", 1)[1] + ".png")).exists():
                    missing.append(f"{kind}/{f.stem}: {ref}")
    if missing:
        sys.exit("missing textures:\n  " + "\n  ".join(missing))
    print("  every model texture exists")


def check_unlocks(research, reactions, machines, gear):
    """Every unlock of a released level must name something the mod has built: no placeholders."""
    from machines import ITEMS, RECIPES, SHAPELESS, EXTRA_SHAPELESS, EXTRA_SHAPED, POWER
    from atlas import ITEM_UNLOCKS as ATLAS_ITEM_UNLOCKS, MAP_FEATURES
    reaction_ids = {r["id"] for r in reactions}
    items = set(ITEMS) | {"hand_cannon", "flintlock_musket", "breech_loading_rifle", "copper_wire", "smoke_detector", "insulated_cable", "asphalt", "tritium_light", "storage_tank", "tallow_candle", "tree_tap", "tarmac", "mining_charge", "demolition_charge", "bakelite_blocks", "wetsuit", "shaped_charge", "anfo_charge", "insulation", "sledgehammers", "excavators", "blasting_charge", "charging_bench", "ore_seeking", "refractory_brick", "tuyere", "structure_port", "silica_brick", "lead_sheet", "gay_lussac_tower", "distillation_tray", "diffuser_stage", "graphite_block", "fuel_channel", "reactor_shielding", "greenhouse_heater", "greenhouse_cooler"} | set(__import__("tanks").BLOCKS) | ATLAS_ITEM_UNLOCKS | {f"{m['id']}_tools" for m in gear} | {f"{m['id']}_armor" for m in gear}
    recipes = set(RECIPES) | set(SHAPELESS) | set(EXTRA_SHAPELESS) | set(EXTRA_SHAPED)
    from compendium import FEATURES as COMPENDIUM_FEATURES
    from crops import RESEARCH_UNLOCKS as CROP_UNLOCKS
    items |= CROP_UNLOCKS["item"]
    from logistics import ITEM_UNLOCKS as LOGISTICS_UNLOCKS
    items |= LOGISTICS_UNLOCKS
    from exploration import ITEM_UNLOCKS as EXPLORATION_UNLOCKS
    items |= EXPLORATION_UNLOCKS
    from fauna import ITEM_UNLOCKS as FAUNA_UNLOCKS
    items |= FAUNA_UNLOCKS
    known = {"crop": CROP_UNLOCKS["crop"], "map": MAP_FEATURES, "compendium": COMPENDIUM_FEATURES, "machine": set(machines) | set(POWER), "reaction": reaction_ids, "item": items, "recipe": recipes,
             "upgrade": {"forced_air", "steam_blast", "compressor", "regenerator", "refractory_lining", "gas_recovery_hood", "mechanical_stirrer", "catalyst_bed", "output_pusher", "vacuum_pump", "conveyor_belt", "foam_lagging", "sterile_air_filter"}}
    missing = []
    for p in research.values():
        if p["level"] > RELEASED_LEVEL:
            continue
        for u in p["unlocks"]:
            kind, ident = u.split(":", 1)
            if ident not in known.get(kind, ()):
                missing.append(f"{p['id']} -> {u}")
    assert not missing, "unlocks with nothing behind them: " + ", ".join(missing)


# Items that carry an element only as a trace but are its real source (radon is only ever handled sealed in a seed).
TRACE_CARRIERS = {"radon_seed": "radon", "francium_precipitate": "francium", "astatine_sample": "astatine",
                  "berkelium_sample": "berkelium", "californium_sample": "californium", "einsteinium_sample": "einsteinium",
                  "fermium_sample": "fermium", "mendelevium_sample": "mendelevium"}
# Elements 102 to 118 live for seconds or less: the accelerator gives a detection record, not atoms you can keep.
TRACE_CARRIERS.update({f"{e}_record": e for e in (
    "nobelium", "lawrencium", "rutherfordium", "dubnium", "seaborgium", "bohrium", "hassium", "meitnerium", "darmstadtium",
    "roentgenium", "copernicium", "nihonium", "flerovium", "moscovium", "livermorium", "tennessine", "oganesson")})


def reactor_outputs():
    """What Nuclear Reactors make outside the reaction table: spent rods and irradiated targets."""
    from machines import POWER
    out = set()
    for _, _, _, _, cfg in POWER.values():
        if cfg.get("kind") == "reactor":
            out.add(cfg["spent"])
            out |= set(cfg["irradiation"].values())
    return out


# Elements made only by irradiation in the reactor: the research that reveals them to the lens.
IRRADIATION_LENS = {"einsteinium": "nuclear_reactor", "fermium": "nuclear_reactor"}


# The highest tech level the mod has built so far: every element at or below it must have a source.
RELEASED_LEVEL = 7
# Elements that wait for a later release (empty since 0.9.5 finished level 6).
NUCLEAR_PENDING = set()


def check_elements_obtainable(elements, reactions, ore_drops, comps):
    """An element is obtainable if a machine makes it, an ore drops it, or an item is (almost) pure it."""
    made = set()
    for r in reactions:
        for p in r["products"]:
            if not p["vented"]:
                made.add(p["id"].split(":")[-1] if p["id"].startswith("item:") else p["id"])
                made.add(TRACE_CARRIERS.get(p["id"].split(":")[-1], ""))
    made |= {d.split(":")[1] for d in ore_drops}
    made |= {TRACE_CARRIERS.get(i.split(":")[1], i.split(":")[1]) for i in reactor_outputs()}
    for key, c in comps.items():
        if not key.startswith(MOD_ID + ":") and c["parts"] and c["parts"][0]["units"] >= 0.9 * c["amount"]:
            made.add(c["parts"][0]["id"])   # charcoal is carbon, a gold ingot is gold
    missing = [e["name"] for e in elements
               if e["level"] <= RELEASED_LEVEL and e["id"] not in made and e["id"] not in NUCLEAR_PENDING]
    assert not missing, f"elements up to level {RELEASED_LEVEL} with no source: {', '.join(missing)}"
    print(f"  all {sum(e['level'] <= RELEASED_LEVEL and e['id'] not in NUCLEAR_PENDING for e in elements)} elements up to "
          f"level {RELEASED_LEVEL} have a source")


def table_position(n):
    """(column, row) in an 18-column table, with lanthanides/actinides on rows 8 and 9."""
    if 57 <= n <= 71:
        return 2 + (n - 57), 8
    if 89 <= n <= 103:
        return 2 + (n - 89), 9
    periods = [(1, 2), (3, 10), (11, 18), (19, 36), (37, 54), (55, 86), (87, 118)]
    for row, (lo, hi) in enumerate(periods):
        if lo <= n <= hi:
            if row == 0:
                return (0 if n == 1 else 17), 0
            if row in (1, 2):
                i = n - lo
                return (i if i < 2 else i + 10), row
            if row in (3, 4):
                return n - lo, row
            i = n - lo  # periods 6-7 with the f-block removed
            if i < 2:
                return i, row
            if i == 2:
                return 2, row  # La / Ac sits in group 3 on the main table
            return i - 14, row
    raise ValueError(n)


if __name__ == "__main__":
    main()
