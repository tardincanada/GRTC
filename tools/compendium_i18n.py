"""Tree Compendium (3.1.0) in the mod's other languages.

Each language has its own module tools/compendium_<code>.py with:
  TITLE   the book's name (also the Field Guide chapter title),
  GUIDE   the Field Guide chapter, same paragraphs and headings as compendium.GUIDE,
  UI      English -> translation for the interface, item names, research and vanilla tree names,
  TREES   species id -> (native range, about),
  BIOMES  biome id -> about.
text(code) turns all of it into English -> translation for generate.py.
"""
import importlib

import compendium as C
import compendium_trees as CT

LANGS = ["pt_br", "es_es", "fr_fr", "it_it", "de_de", "nl_nl", "sv_se", "da_dk", "ru_ru"]


def _mod(code):
    try:
        return importlib.import_module(f"compendium_{code}")
    except ModuleNotFoundError:
        return None


class _Lazy(dict):
    def __init__(self, attr, fallback):
        super().__init__()
        self.attr, self.fallback = attr, fallback

    def __missing__(self, code):
        m = _mod(code)
        return getattr(m, self.attr) if m and hasattr(m, self.attr) else self.fallback


TITLE = _Lazy("TITLE", "Tree Compendium")
GUIDE = _Lazy("GUIDE", C.GUIDE)


class _PlantLazy(dict):
    def __init__(self, attr, fallback):
        super().__init__()
        self.attr, self.fallback = attr, fallback

    def __missing__(self, code):
        m = _plants_mod(code)
        return getattr(m, self.attr) if m and hasattr(m, self.attr) else self.fallback


import compendium_plants as _CP  # noqa: E402
PLANT_TITLE = _PlantLazy("TITLE", "Plant Compendium")
PLANT_GUIDE = _PlantLazy("GUIDE", _CP.GUIDE)


def text(code):
    m = _mod(code)
    if m is None:
        return {}
    ui = getattr(m, "UI", [])
    en = ui_english()
    assert len(ui) == len(en), (code, len(ui), len(en))
    out = dict(zip(en, ui))
    for sid, (rng, about) in getattr(m, "TREES", {}).items():
        fam, height, iucn, rng_en, about_en = CT.DATA[sid]
        out[about_en] = about
        if rng_en:
            out[rng_en] = rng
    for b, about in getattr(m, "BIOMES", {}).items():
        out[CT.BIOMES[b][2]] = about
    for k, v in plant_text(code).items():
        out.setdefault(k, v)
    for k, v in mineral_text(code).items():
        out.setdefault(k, v)  # a string shared with a tree keeps the tree's translation
    return out


def plant_ui_english():
    """The Plant Compendium's interface strings, in the order of each compendium_plants_<code>.UI."""
    import compendium_plants as CP
    ui = list(CP.LANG.values())
    for name, tip in CP.ITEMS.values():
        ui += [name, tip]
    return ui


def _plants_mod(code):
    try:
        return importlib.import_module(f"compendium_plants_{code}")
    except ModuleNotFoundError:
        return None


def plant_text(code):
    import compendium_plants as CP
    m = _plants_mod(code)
    if m is None:
        return {}
    en = plant_ui_english()
    assert len(m.UI) == len(en), (code, "plants", len(m.UI), len(en))
    out = dict(zip(en, m.UI))
    assert set(m.PLANTS) == set(CP.PLANTS), (code, set(CP.PLANTS) ^ set(m.PLANTS))
    for pid, (name, rng, about) in m.PLANTS.items():
        p = CP.PLANTS[pid]
        out[p["name"]] = name
        out[p["about"]] = about
        if p["range"]:
            assert rng, (code, pid)
            out[p["range"]] = rng
    assert set(m.BIOMES) == set(CP.BIOMES), code
    for b, about in m.BIOMES.items():
        out[CP.BIOMES[b][2]] = about
    return out


def ui_english():
    """The interface strings in the order each language module's UI list gives them."""
    import csv
    from pathlib import Path
    ui = list(C.TREE_LANG.values())
    for name, tip in C.TREE_ITEMS.values():
        ui += [name, tip]
    with (Path(__file__).resolve().parent.parent / "data" / "research.csv").open(newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            if r["id"] in ("species_plantarum", "plant_geography", "red_list"):
                ui += [r["name"], r["description"]]
    ui += [v["name"] for v in CT.VANILLA.values()]
    out = []
    for x in ui:
        if x not in out:
            out.append(x)
    return out


# ---- 3.15.0 Mineral Compendium: tools/compendium_minerals_<code>.py ----
# TITLE, GUIDE (as compendium_minerals.GUIDE), UI (mineral_ui_english order), GROUPS id -> (name, about),
# FORMATIONS id -> name, SYSTEMS id -> name, MINERALS id -> (about, origin, made, mineral) (None where English has none).

def _minerals_mod(code):
    try:
        return importlib.import_module(f"compendium_minerals_{code}")
    except ModuleNotFoundError:
        return None


class _MineralLazy(dict):
    def __init__(self, attr, fallback):
        super().__init__()
        self.attr, self.fallback = attr, fallback

    def __missing__(self, code):
        m = _minerals_mod(code)
        return getattr(m, self.attr) if m and hasattr(m, self.attr) else self.fallback


import compendium_minerals as _CM  # noqa: E402
MINERAL_TITLE = _MineralLazy("TITLE", "Mineral Compendium")
MINERAL_GUIDE = _MineralLazy("GUIDE", _CM.GUIDE)


def mineral_ui_english():
    ui = list(_CM.LANG.values())
    for name, tip in _CM.ITEMS.values():
        ui += [name, tip]
    for r in _CM.RESEARCH:
        ui += [r[5], r[7]]
    return ui


def mineral_text(code):
    m = _minerals_mod(code)
    if m is None:
        return {}
    en = mineral_ui_english()
    assert len(m.UI) == len(en), (code, "minerals", len(m.UI), len(en))
    out = dict(zip(en, m.UI))
    assert set(m.GROUPS) == set(_CM.GROUPS), code
    for g, (name, about) in m.GROUPS.items():
        out[_CM.GROUPS[g][0]] = name
        out[_CM.GROUPS[g][1]] = about
    assert set(m.FORMATIONS) == set(_CM.FORMATIONS), code
    for f, name in m.FORMATIONS.items():
        out[_CM.FORMATIONS[f]] = name
    assert set(m.SYSTEMS) == set(_CM.SYSTEMS), code
    for k, name in m.SYSTEMS.items():
        out[_CM.SYSTEMS[k]] = name
    assert set(m.MINERALS) == set(_CM.MINERALS), (code, set(_CM.MINERALS) ^ set(m.MINERALS))
    for mid, texts in m.MINERALS.items():
        e = _CM.MINERALS[mid]
        for k, t in zip(("about", "origin", "made", "mineral"), texts):
            assert (t is None) == (e[k] is None), (code, mid, k)
            if t is not None:
                out.setdefault(e[k], t)
    return out
