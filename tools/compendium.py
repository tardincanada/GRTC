"""Tree Compendium (3.1.0, stage 1 of the compendium plan): items, textures, recipe, runtime data and text.

Carried in the hotbar, the book shows the biome's name in the top-left corner and names the tree under the crosshair
once the player has noted it (looked at it for a second, or felled it). Used, it opens the compendium: biomes with the
trees noted there, each with real facts. Optional research adds layers: Species Plantarum (scientific name, family,
native range), Geography of Plants (climate of each biome), The Red List (conservation status).
Java: com.periodicelements.compendium. Facts: tools/compendium_trees.py; translations: tools/compendium_i18n.py.
"""
import csv
import json
from pathlib import Path

from PIL import Image

import compendium_trees as CT

ITEMS = {
    "tree_compendium": ("Tree Compendium",
                        "In your hotbar it shows the biome and names the trees you know; use it to read about them"),
    "compendium_notes": ("Compendium Notes", "Use it to add every tree, plant and biome noted in it to your own "
                                             "compendiums; it is used up"),
}
SHAPELESS = {"tree_compendium": (["minecraft:book", "#minecraft:saplings"], 1)}
# Unlocks "compendium:<feature>" (checked by generate.check_unlocks).
FEATURES = {"classification", "climate", "conservation", "evolution"}
IUCN = {"LC": "Least Concern", "NT": "Near Threatened", "VU": "Vulnerable", "EN": "Endangered",
        "CR": "Critically Endangered", "DD": "Data Deficient", "NE": "Not Evaluated"}

LANG = {
    "gui.periodic_elements.compendium.title": "Tree Compendium",
    "gui.periodic_elements.compendium.unknown_tree": "Unknown tree",
    "gui.periodic_elements.compendium.hud_climate": "about %s °C, %s mm of rain a year",
    "gui.periodic_elements.compendium.new_tree": "New tree in your Compendium: %s",
    "gui.periodic_elements.compendium.new_biome": "New biome in your Compendium: %s",
    "gui.periodic_elements.compendium.biomes": "Biomes",
    "gui.periodic_elements.compendium.summary": "%s of %s trees and %s of %s biomes noted.",
    "gui.periodic_elements.compendium.help": "Keep this book in your hotbar. Look at a tree for a moment, or fell it, "
                                             "to note it; walk into a biome to note the biome. Choose a biome on the "
                                             "left.",
    "gui.periodic_elements.compendium.not_visited": "You have not been here yet.",
    "gui.periodic_elements.compendium.trees_here": "Trees",
    "gui.periodic_elements.compendium.unseen": "???",
    "gui.periodic_elements.compendium.climate": "Climate: about %s °C a year, %s mm of rain.",
    "gui.periodic_elements.compendium.height": "Height: up to %s m.",
    "gui.periodic_elements.compendium.family": "Family: %s.",
    "gui.periodic_elements.compendium.range": "Native to: %s.",
    "gui.periodic_elements.compendium.status": "Red List: %s.",
    "gui.periodic_elements.compendium.grows": "Grows in: %s.",
    "gui.periodic_elements.compendium.gives": "Gives: %s.",
    "gui.periodic_elements.compendium.this_world": "A tree of this world only: no real species matches it.",
    "gui.periodic_elements.compendium.first_seen": "First noted at X %s, Z %s.",
    "gui.periodic_elements.compendium.hint": "Research Species Plantarum (level 3) to learn its scientific name, "
                                             "family and native range.",
    "gui.periodic_elements.compendium.back": "Back",
    "gui.periodic_elements.compendium.mark": "Mark on Atlas",
    "gui.periodic_elements.compendium.copy_book": "Hold a book in your other hand to copy your notes",
    "gui.periodic_elements.compendium.copied": "Compendium Notes made: %s entries",
    "gui.periodic_elements.compendium.merged": "Added to your compendiums: %s new entries",
    "gui.periodic_elements.compendium.blank": "These notes are empty",
}
for _c, _n in IUCN.items():
    LANG[f"gui.periodic_elements.compendium.iucn.{_c.lower()}"] = _n

# 3.5.0: the Plant Compendium shares the system (tools/compendium_plants.py). The tree-only copies keep the order
# of each language module's UI list (compendium_i18n.ui_english).
import compendium_plants as CP  # noqa: E402
TREE_ITEMS, TREE_LANG = dict(ITEMS), dict(LANG)
ITEMS.update(CP.ITEMS)
SHAPELESS.update(CP.SHAPELESS)
LANG.update(CP.LANG)
# 3.15.0: the Mineral Compendium (tools/compendium_minerals.py).
import compendium_minerals as CM  # noqa: E402
ITEMS.update(CM.ITEMS)
SHAPELESS.update(CM.SHAPELESS)
LANG.update(CM.LANG)
FEATURES |= CM.FEATURES
# 3.50.0: the Animal Compendium (tools/fauna.py).
ITEMS["animal_compendium"] = ("Animal Compendium",
                              "In your hotbar it names the animals you know; use it to read about them")
SHAPELESS["animal_compendium"] = (["minecraft:book", "minecraft:feather"], 1)

GUIDE = [
    "# The Tree Compendium",
    "A book and any sapling make the Tree Compendium; no research is needed. Keep it anywhere in your hotbar and the "
    "top-left corner of the screen names the biome you stand in, and the tree you look at once you know it.",
    "To note a tree, look at its log, leaves or sapling for a moment from up to 8 blocks away, or fell it, while the "
    "book is in your hotbar. Walking into a biome notes the biome. Use the book to read: biomes on the left, the trees "
    "of each one on the right, with what each tree is, how tall it grows, where it grows and what it gives.",
    "Sneak and use the Compendium with a book in your other hand to make Compendium Notes; whoever reads them adds "
    "every tree and biome in them to their own Compendium.",
    "Species Plantarum (level 3): each tree shows its scientific name, its family and where it grows wild in the real "
    "world, after Linnaeus (1753).",
    "Geography of Plants (level 3): each biome shows its climate, and the corner of the screen shows it too, after "
    "Humboldt (1807).",
    "The Red List (level 6): each tree shows how threatened it is in the wild, after the IUCN (1964).",
]

LEATHER, LEATHER_D, PAPER, LEAF, LEAF_D = (46, 92, 52), (30, 62, 34), (236, 226, 196), (90, 160, 60), (50, 110, 40)


def _rect(img, x0, y0, x1, y1, c):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(c) + (255,)


def item_texture(iid):
    if iid == "plant_compendium":
        return CP.item_texture()
    if iid == "mineral_compendium":
        return CM.item_texture()
    if iid == "animal_compendium":
        return _animal_book()
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    if iid == "tree_compendium":
        # A green cloth-bound book with a pressed leaf on the cover.
        _rect(img, 2, 1, 13, 14, LEATHER_D)
        _rect(img, 3, 1, 13, 13, LEATHER)
        _rect(img, 3, 14, 13, 14, PAPER)
        for x, y in [(8, 4), (7, 5), (8, 5), (9, 5), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (7, 7), (8, 7), (9, 7),
                     (8, 8)]:
            px[x, y] = LEAF + (255,)
        for x, y in [(8, 9), (8, 10), (8, 5), (8, 6), (8, 7)]:
            px[x, y] = LEAF_D + (255,)
    else:
        # A folded sheet with a leaf sketch.
        _rect(img, 3, 3, 12, 12, PAPER)
        _rect(img, 3, 12, 12, 12, (196, 184, 150))
        for x, y in [(7, 6), (8, 6), (6, 7), (7, 7), (8, 7), (9, 7), (7, 8), (8, 8), (7, 9)]:
            px[x, y] = LEAF_D + (255,)
    return img


def _animal_book():
    """A tan cloth-bound book with a paw print on the cover."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    _rect(img, 2, 1, 13, 14, (110, 72, 40))
    _rect(img, 3, 1, 13, 13, (164, 112, 64))
    _rect(img, 3, 14, 13, 14, PAPER)
    ink = (70, 42, 22, 255)
    for x, y in [(7, 7), (8, 7), (9, 7), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (7, 9), (8, 9), (9, 9),
                 (5, 5), (7, 4), (9, 4), (11, 5)]:
        px[x, y] = ink
    return img


def _species(root):
    with (root / "data" / "trees.csv").open(newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def write_runtime(root, runtime_dir, lang, mod):
    """runtime compendium.json plus the English lang lines; returns the number of tree entries."""
    trees, vanilla_names = [], {}
    for sid, v in CT.VANILLA.items():
        fam, height, iucn, rng, about = CT.DATA[sid]
        trees.append({"id": sid, "vanilla": True, "icon": v["icon"], "scientific": v["scientific"], "family": fam,
                      "height": height, "iucn": iucn, "biomes": v["biomes"],
                      "blocks": [f"minecraft:{b}" for b in v["blocks"]],
                      "products": ["minecraft:cocoa_beans"] if sid == "jungle" else []})
        lang[f"tree.{mod}.{sid}"] = v["name"]
        vanilla_names[sid] = v["name"]
    for r in _species(root):
        sid = r["id"]
        assert sid in CT.DATA, f"no compendium entry for tree {sid}"
        fam, height, iucn, rng, about = CT.DATA[sid]
        products = []
        for p in (r["fruit"], r["bark_item"], r["tap"]):
            if p:
                products.append(p if ":" in p else f"{mod}:{p}")
        trees.append({"id": sid, "vanilla": False, "icon": f"{mod}:{sid}_sapling", "scientific": r["scientific"],
                      "family": fam, "height": height, "iucn": iucn, "biomes": r["biomes"].split(";"),
                      "blocks": [f"{mod}:{b}" for b in (f"{sid}_log", f"stripped_{sid}_log", f"{sid}_leaves",
                                                         f"{sid}_sapling")],
                      "products": products})
    for t in trees:
        fam, height, iucn, rng, about = CT.DATA[t["id"]]
        assert iucn in IUCN or iucn is None, (t["id"], iucn)
        lang[f"compendium.{mod}.tree.{t['id']}.about"] = about
        if rng:
            lang[f"compendium.{mod}.tree.{t['id']}.range"] = rng
        for b in t["biomes"]:
            assert b in CT.BIOMES, (t["id"], b)
    plants = CP.runtime_entries()
    import shrubs as SH
    for t in SH.compendium_entries(mod):
        sp = SH.SHRUBS[t["id"]]
        lang[f"plant.{mod}.{t['id']}"] = sp["name"]
        lang[f"compendium.{mod}.plant.{t['id']}.about"] = sp["about"]
        lang[f"compendium.{mod}.plant.{t['id']}.range"] = sp["range"]
        plants.append(t)
    for t in plants:
        if t["id"] in SH.SHRUBS:
            continue
        p = CP.PLANTS[t["id"]]
        lang[f"plant.{mod}.{t['id']}"] = p["name"]
        lang[f"compendium.{mod}.plant.{t['id']}.about"] = p["about"]
        if p["range"]:
            lang[f"compendium.{mod}.plant.{t['id']}.range"] = p["range"]
        assert p["iucn"] in IUCN or p["iucn"] is None, (t["id"], p["iucn"])
        for b in t["biomes"]:
            assert b in CT.BIOMES or b in CP.BIOMES, (t["id"], b)
    minerals = CM.runtime_entries()
    # 3.24.1: the ores in other rocks of the Geological world (granite_galena_ore ...) count as their mineral's ore
    import geo_ores as GO
    by_block = {b: e for e in minerals for b in e["blocks"]}
    geo_minerals = GO.minerals()
    for vid, _rock, m in GO.variants():
        entry = next((by_block[b] for b in geo_minerals[m][1].values() if b in by_block), None)
        assert entry is not None, f"no Mineral Compendium entry for {vid}"
        entry["blocks"].append(f"{mod}:{vid}")
    CM.write_lang(lang, mod)
    # 3.50.0: the Animal Compendium (tools/fauna.py)
    import fauna as FA
    import fauna_text as FT
    animals = FA.compendium_entries(mod)
    FA.compendium_lang(lang, mod)
    biomes = []
    for b, (temp, rain, about) in list(CT.BIOMES.items()) + list(CP.BIOMES.items()) + list(FT.BIOMES.items()):
        biomes.append({"id": b, "temperature": temp, "rain": rain})
        lang[f"compendium.{mod}.biome.{b}.about"] = about
    (runtime_dir / "compendium.json").write_text(json.dumps({"trees": trees, "plants": plants, "minerals": minerals, "animals": animals, "biomes": biomes}, indent=1,
                                                            ensure_ascii=False) + "\n", encoding="utf-8")
    return len(trees) + len(plants) + len(minerals) + len(animals)


def translations(code):
    """English -> translation of everything the compendium adds, in one language."""
    import compendium_i18n as I
    return I.text(code)
