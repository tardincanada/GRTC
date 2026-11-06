"""Crop genetics stages 2 and 3 (3.37.0): the Crossing Notebook, silage and the Silo, inoculant, hybrids, balanced
feed, the Seed Irradiator, the Gamma Garden, the DNA Sequencer, the Thermocycler, the Gene Gun with its four gene
constructs, and the CRISPR Kit. Called from tools/crops.py; the research rows live in data/research.csv and the
cobalt-60 reaction in data/reactions.csv."""
import random
import zlib

from PIL import Image

# Items: id -> (English name, tooltip or None).
ITEMS = {
    "crossing_notebook": ("Crossing Notebook", "Mendel's Laws: use it on seeds, then on other seeds: it works out what "
                          "share of the seeds of that cross will show each trait"),
    "silage": ("Silage", None),
    "inoculant": ("Inoculant", "Nitrogen Fixation: use it on soybeans, beans or alfalfa: harvested ripe, they leave "
                  "their farmland strongly fertilized"),
    "balanced_feed": ("Balanced Feed", None),
    "cobalt_60_source": ("Cobalt-60 Source", None),
    "gene_gun": ("Gene Gun", "Transgenesis: hold a gene construct in your other hand and use the gun on a crop: half "
                 "the shots take"),
    "crispr_kit": ("CRISPR Kit", "CRISPR: use it on a crop to write the useful form of a gene into both its copies; "
                   "sneak-use it to choose the gene"),
    "bt_gene": ("Bt Gene", "Gene Gun: caterpillars never take the plant"),
    "epsps_gene": ("EPSPS Gene", "Gene Gun: Field Clear spares the plant"),
    "golden_gene": ("Golden Rice Gene", "Gene Gun, rice only: its grains carry vitamin A and can be eaten raw"),
    "dreb_gene": ("DREB Gene", "Gene Gun: the plant tolerates drought"),
}
CONSTRUCTS = ("bt_gene", "epsps_gene", "golden_gene", "dreb_gene")
# Blocks: id -> (English name, gate description, tool).
BLOCKS = {
    "silo": ("Silo", "Silage: put maize, sorghum or alfalfa in by hand or from a hopper; one silage every 5 seconds "
             "comes out below", "axe"),
    "seed_irradiator": ("Seed Irradiator", "X-ray Mutagenesis: X-rays seeds put in it (40 kJ a seed): 3 in 10 die, the "
                        "rest mutate 20 times as often", "pickaxe"),
    "gamma_garden": ("Gamma Garden", "Gamma Garden: crops ripening within 6 blocks give seeds that mutate 20 times as "
                     "often. Radioactive", "pickaxe"),
    "dna_sequencer": ("DNA Sequencer", "DNA Sequencing: use seeds on it to read their whole genome (20 kJ a seed)",
                      "pickaxe"),
    "thermocycler": ("Thermocycler", "PCR: sneak-use a seed on it to set the marker; seeds put in are sorted: those "
                     "with all the marker's useful traits go below, the rest to the sides (10 kJ a seed)", "pickaxe"),
}
RESEARCH_ITEMS = {"crossing_notebook", "silo", "inoculant", "balanced_feed", "seed_irradiator", "gamma_garden",
                  "dna_sequencer", "thermocycler", "gene_gun", "crispr_kit"} | set(CONSTRUCTS)
RESEARCH_CROP = {"hybrid", "dna"}

LANG = {
    "gene.periodic_elements.S": "Stature",
    "gene.periodic_elements.S.good": "semi-dwarf",
    "gene.periodic_elements.S.plain": "tall",
    "gene.periodic_elements.bt": "Bt",
    "gene.periodic_elements.epsps": "glyphosate tolerant",
    "gene.periodic_elements.golden": "golden rice",
    "gene.periodic_elements.dreb": "DREB",
    "gui.periodic_elements.crop.hybrid": "F1 hybrid: vigorous, but its seeds do not keep it",
    "gui.periodic_elements.crop.pure_line": "Pure line: breeds true",
    "gui.periodic_elements.crop.mixed_line": "Mixed line: its seeds will vary",
    "gui.periodic_elements.crop.inoculated": "Inoculated: its roots fix nitrogen",
    "gui.periodic_elements.notebook.needs": "You do not know how traits are inherited yet (Mendel's Laws, level 4)",
    "gui.periodic_elements.notebook.nothing": "Use the notebook on seeds held in your other hand",
    "gui.periodic_elements.notebook.first": "First parent noted: %s. Now use it on the other parent's seeds",
    "gui.periodic_elements.notebook.species": "These are not the same kind of plant: they will not cross",
    "gui.periodic_elements.notebook.cross": "Cross with %s:",
    "gui.periodic_elements.notebook.gene": "%s: %s, %s%% %s",
    "gui.periodic_elements.inoculant.needs": "You do not know about root bacteria yet (Nitrogen Fixation, level 4)",
    "gui.periodic_elements.inoculant.not_legume": "Only soybeans, beans and alfalfa take rhizobia",
    "gui.periodic_elements.inoculant.done": "Inoculated: harvested ripe, it leaves the ground fertilized",
    "gui.periodic_elements.gene_gun.needs": "You do not know how to put a gene into a plant yet (Transgenesis, level 7)",
    "gui.periodic_elements.gene_gun.empty": "Hold a gene construct in your other hand",
    "gui.periodic_elements.gene_gun.rice_only": "The golden rice gene works only in rice",
    "gui.periodic_elements.gene_gun.took": "The %s took",
    "gui.periodic_elements.gene_gun.missed": "Missed: the gene did not take",
    "gui.periodic_elements.crispr.needs": "You do not know how to edit genes yet (CRISPR, level 7)",
    "gui.periodic_elements.crispr.no_stature": "Only wheat and rice have the Stature gene",
    "gui.periodic_elements.crispr.done": "%s edited: %s",
    "gui.periodic_elements.crispr.target": "Target gene: %s",
    "gui.periodic_elements.silo.status": "Silo: %s waiting, %s silage ready",
    "gui.periodic_elements.silo.wrong": "The Silo takes maize, sorghum or alfalfa",
    "gui.periodic_elements.seed_irradiator.status": "Seed Irradiator: %s seeds waiting, %s irradiated",
    "gui.periodic_elements.seed_irradiator.no_power": "Seed Irradiator: no power (40 kJ a seed); %s waiting, %s done",
    "gui.periodic_elements.seed_irradiator.wrong": "The Seed Irradiator takes seeds of one kind at a time",
    "gui.periodic_elements.thermocycler.status": "Thermocycler: %s seeds waiting, %s sorted",
    "gui.periodic_elements.thermocycler.no_power": "Thermocycler: no power (10 kJ a seed); %s waiting, %s sorted",
    "gui.periodic_elements.thermocycler.no_marker": "Thermocycler: no marker; sneak-use a seed on it to set one",
    "gui.periodic_elements.thermocycler.wrong": "The Thermocycler takes seeds of one kind at a time",
    "gui.periodic_elements.thermocycler.marker": "Marker set: %s",
    "gui.periodic_elements.dna_sequencer.no_power": "DNA Sequencer: needs %s kJ of power",
    "gui.periodic_elements.dna_sequencer.read": "%s: %s",
}
for _i, (_n, _tip) in ITEMS.items():
    LANG[f"item.periodic_elements.{_i}"] = _n
    if _tip:
        LANG[f"item.periodic_elements.{_i}.tip"] = _tip
for _b, (_n, _desc, _tool) in BLOCKS.items():
    LANG[f"block.periodic_elements.{_b}"] = _n
    LANG[f"block.periodic_elements.{_b}.desc"] = _desc


def _i(x):
    return {"item": x if ":" in x else f"periodic_elements:{x}"}


def _shapeless(out, count, *ins, category="misc"):
    return {"type": "minecraft:crafting_shapeless", "category": category, "ingredients": [_i(x) for x in ins],
            "result": {"id": f"periodic_elements:{out}", "count": count}}


def _shaped(out, pattern, key, category="misc"):
    return {"type": "minecraft:crafting_shaped", "category": category, "pattern": pattern,
            "key": {k: ({"tag": v[1:]} if v.startswith("#") else _i(v)) for k, v in key.items()},
            "result": {"id": f"periodic_elements:{out}", "count": 1}}


RECIPES = {
    "crossing_notebook": _shapeless("crossing_notebook", 1, "minecraft:book", "minecraft:feather", "minecraft:black_dye",
                                    category="equipment"),
    "silo": _shaped("silo", ["PIP", "P P", "PIP"], {"P": "#minecraft:planks", "I": "minecraft:iron_ingot"}),
    "inoculant": _shapeless("inoculant", 4, "soybeans", "minecraft:dirt", "glucose", "minecraft:glass_bottle"),
    "balanced_feed": _shapeless("balanced_feed", 4, "maize_kernels", "oats", "protein_meal", "minecraft:bone_meal",
                                "sodium_chloride"),
    "seed_irradiator": _shaped("seed_irradiator", ["LGL", "WIW", "LLL"], {
        "L": "lead", "G": "minecraft:glass", "W": "tungsten", "I": "minecraft:iron_ingot"}),
    "gamma_garden": _shaped("gamma_garden", ["LLL", "LCL", "LLL"], {"L": "lead", "C": "cobalt_60_source"}),
    "dna_sequencer": _shaped("dna_sequencer", ["GGG", "ICI", "III"], {
        "G": "minecraft:glass", "I": "minecraft:iron_ingot", "C": "integrated_circuit"}),
    "thermocycler": _shaped("thermocycler", ["III", "KCK", "III"], {
        "I": "minecraft:iron_ingot", "K": "minecraft:copper_ingot", "C": "integrated_circuit"}),
    "gene_gun": _shapeless("gene_gun", 1, "minecraft:iron_ingot", "minecraft:iron_ingot", "minecraft:gold_ingot",
                           "helium", category="equipment"),
    "crispr_kit": _shapeless("crispr_kit", 1, "glucose", "integrated_circuit", "minecraft:glass_bottle",
                             category="equipment"),
    "bt_gene": _shapeless("bt_gene", 1, "minecraft:glass_bottle", "glucose", "minecraft:dirt"),
    "epsps_gene": _shapeless("epsps_gene", 1, "minecraft:glass_bottle", "glucose", "glyphosate"),
    "golden_gene": _shapeless("golden_gene", 1, "minecraft:glass_bottle", "glucose", "minecraft:dandelion"),
    "dreb_gene": _shapeless("dreb_gene", 1, "minecraft:glass_bottle", "glucose", "sorghum"),
}

FEED = {"silage": ("cow_food", "sheep_food", "goat_food"),
        "balanced_feed": ("cow_food", "sheep_food", "goat_food", "pig_food", "chicken_food", "horse_food")}


def composition_rows():
    """Never more units than the recipe puts in (CraftedCompositionTests)."""
    p = "periodic_elements:"
    return [
        (p + "crossing_notebook", 100.0, "cellulose:70;protein:25;carbon:5"),
        (p + "silage", 1000.0 / 9.0, "water:65;cellulose:18;starch:8;protein:5;glucose:4"),
        (p + "inoculant", 100.0, "water:55;silicon_dioxide:25;protein:10;glucose:10"),
        (p + "balanced_feed", 1000.0 / 9.0, "starch:40;protein:30;tricalcium_phosphate:15;cellulose:7;sodium_chloride:5;water:3"),
        (p + "cobalt_60_source", 10.0, "cobalt:100"),
        (p + "gene_gun", 400.0, "iron:55;gold:25;helium:20"),
        (p + "crispr_kit", 200.0, "water:40;glucose:30;silicon:20;protein:10"),
        (p + "bt_gene", 50.0, "water:90;glucose:8;protein:2"),
        (p + "epsps_gene", 50.0, "water:90;glucose:8;protein:2"),
        (p + "golden_gene", 50.0, "water:90;glucose:8;protein:2"),
        (p + "dreb_gene", 50.0, "water:90;glucose:8;protein:2"),
        (p + "silo", 1500.0, "cellulose:55;lignin:20;water:10;iron:15"),
        (p + "seed_irradiator", 1500.0, "lead:40;silicon_dioxide:30;calcium_oxide:10;tungsten:12;iron:8"),
        (p + "gamma_garden", 800.0, "lead:98;cobalt:2"),
        (p + "dna_sequencer", 2000.0, "silicon_dioxide:55;iron:25;calcium_oxide:15;silicon:5"),
        (p + "thermocycler", 900.0, "iron:70;copper:20;silicon:10"),
    ]


GUIDE = [
    "# Modern breeding",
    "@4 Mendel's Laws also teaches the Crossing Notebook: use it on seeds held in your other hand, then on the other "
    "parent's seeds. It writes out, gene by gene, what share of the seeds of that cross will show each trait.",
    "@4 Silage (level 4): the Silo turns maize, sorghum and alfalfa, put in by hand or from a hopper, into silage, one "
    "every 5 seconds; a hopper below takes it out. A trough of silage lasts twice as long. Nitrogen Fixation (level "
    "4): Inoculant used on soybeans, beans or alfalfa gives their roots rhizobia: harvested ripe, they leave their "
    "farmland strongly fertilized.",
    "@5 Hybrid Maize (level 5): a seed of two different pure lines (lines with two equal copies of every gene) is an "
    "F1 hybrid: it grows and yields 20% more, but its own seeds lose it. The Seed Lens marks hybrids and pure lines. "
    "Balanced Feed (level 5): maize kernels, oats, protein meal, bone meal and salt make 4 feeds every farm animal "
    "eats: the young grow twice as fast and parents are ready to breed again sooner.",
    "@5 X-ray Mutagenesis (level 5): the Seed Irradiator X-rays seeds put in it (40 kJ a seed from piles or dynamos "
    "joined to it). 3 seeds in 10 die; each gene copy of the rest mutates 20 times as often as in nature. Sow them all "
    "and keep the rare useful ones.",
    "@6 DNA Double Helix (level 6): the Seed Lens shows every trait. Wheat and rice carry a seventh gene, S, Stature: "
    "ss plants are semi-dwarf and on strong fertilizer (NPK) yield half again as much, the Green Revolution. Wild "
    "wheat from the grass of cherry groves sometimes carries s.",
    "@6 Gamma Garden (level 6): the Cyclotron makes a cobalt-60 source from cobalt with heavy water in the reagent "
    "slot; eight lead round it make the Gamma Garden. Crops that ripen within 6 blocks of it give seeds that mutate 20 "
    "times as often. It is radioactive: keep your distance.",
    "@7 DNA Sequencing (level 7): the DNA Sequencer reads the whole genome of seeds used on it (20 kJ a seed). PCR "
    "(level 7): the Thermocycler tests each seed against a marker seed (sneak-use a seed on it to set the marker): "
    "seeds with every useful trait of the marker go to a hopper below, the rest to hoppers at the sides (10 kJ a seed).",
    "@7 Transgenesis (level 7): with a gene construct in your other hand, the Gene Gun used on a crop puts the gene in "
    "it; half the shots take. Bt: caterpillars never take it. EPSPS: Field Clear spares it. Golden Rice (rice only): "
    "its grains carry vitamin A and can be eaten raw. DREB: drought tolerant. Seeds inherit a transgene from either "
    "parent. CRISPR (level 7): the CRISPR Kit writes the useful form of one gene into both copies of a crop; sneak-use "
    "it to choose the gene.",
]


# ---------- textures ----------

def _put(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.load()[x, y] = tuple(c[:3]) + (255,)


def _shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def _noise(base, seed, spread=0.12):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            _put(img, x, y, _shade(base, 1 - spread + 2 * spread * rnd.random()))
    return img


def _frame(img, c):
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            _put(img, x, y, c)
    return img


def block_textures():
    """id -> {"side", "top", "front"?} for the five machines."""
    out = {}
    wood, iron, lead, steel = (150, 110, 65), (190, 190, 195), (95, 100, 110), (215, 218, 222)
    side = _noise(wood, 1)
    for y in (3, 12):
        for x in range(16):
            _put(side, x, y, (110, 110, 115))
    for x in (0, 5, 10, 15):
        for y in range(16):
            _put(side, x, y, _shade(wood, 0.7))
    top = _frame(_noise((120, 150, 60), 2), _shade(wood, 0.7))
    out["silo"] = {"side": side, "top": top}

    side = _frame(_noise(lead, 3), (60, 64, 70))
    for x in range(5, 11):
        for y in range(5, 11):
            _put(side, x, y, (40, 40, 45))
    for x, y in ((7, 7), (8, 8), (8, 7), (7, 8)):
        _put(side, x, y, (120, 200, 255))
    _put(side, 3, 12, (255, 200, 40))
    _put(side, 12, 12, (255, 200, 40))
    top = _frame(_noise(lead, 4), (60, 64, 70))
    for x in range(4, 12):
        for y in range(4, 12):
            _put(top, x, y, (170, 200, 210) if (x + y) % 3 else (140, 170, 180))
    out["seed_irradiator"] = {"side": side, "top": top}

    side = _frame(_noise(lead, 5), (60, 64, 70))
    for x in range(5, 11):
        _put(side, x, 6, (255, 210, 0))
    for x, y in ((7, 8), (8, 8), (6, 9), (9, 9), (5, 10), (10, 10)):
        _put(side, x, y, (255, 210, 0))
    top = _frame(_noise(lead, 6), (60, 64, 70))
    for x in range(6, 10):
        for y in range(6, 10):
            _put(top, x, y, (90, 170, 255))
    out["gamma_garden"] = {"side": side, "top": top}

    side = _frame(_noise(steel, 7, 0.05), (150, 150, 155))
    for y in range(3, 13):
        for x in range(3, 13):
            _put(side, x, y, (30, 40, 50))
    rnd = random.Random(8)
    for y in range(4, 12, 2):
        for x in range(4, 12):
            _put(side, x, y, ((60, 200, 90), (230, 70, 60), (70, 120, 240), (240, 200, 60))[rnd.randrange(4)])
    top = _frame(_noise(steel, 9, 0.05), (150, 150, 155))
    out["dna_sequencer"] = {"side": side, "top": top}

    side = _frame(_noise(iron, 10, 0.06), (130, 130, 135))
    for x in range(3, 13):
        _put(side, x, 4, (180, 120, 70))
    for x in range(4, 12):
        for y in range(7, 12):
            _put(side, x, y, (20, 30, 25))
    for i, x in enumerate(range(4, 12)):
        _put(side, x, 11 - (i % 4 + i // 4) % 5, (80, 240, 120))
    top = _frame(_noise(iron, 11, 0.06), (130, 130, 135))
    for x in range(3, 13, 2):
        for y in range(3, 13, 2):
            _put(top, x, y, (60, 60, 65))
    out["thermocycler"] = {"side": side, "top": top}
    return out


def item_textures():
    out = {}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(3, 13):
        for y in range(2, 15):
            _put(img, x, y, (70, 90, 60) if x == 3 else (235, 228, 205))
    for y in range(4, 13, 2):
        for x in range(5, 12):
            if (x + y) % 3:
                _put(img, x, y, (60, 60, 70))
    out["crossing_notebook"] = img

    rnd = random.Random(12)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for _ in range(110):
        x, y = int(rnd.gauss(7.5, 2.6)), int(rnd.gauss(9, 2.2))
        _put(img, x, y, _shade((150, 150, 60), 0.75 + 0.4 * rnd.random()))
    out["silage"] = img

    def bottle(liquid):
        b = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for y in range(5, 15):
            for x in range(4, 12):
                edge = x in (4, 11) or y == 14
                _put(b, x, y, (200, 220, 230) if edge or y < 8 else liquid)
        for y in (2, 3, 4):
            _put(b, 7, y, (200, 220, 230))
            _put(b, 8, y, (200, 220, 230))
        _put(b, 7, 1, (140, 100, 60))
        _put(b, 8, 1, (140, 100, 60))
        return b

    out["inoculant"] = bottle((140, 110, 70))
    for cid, col in zip(CONSTRUCTS, ((80, 170, 90), (90, 120, 220), (240, 190, 40), (200, 90, 60))):
        b = bottle(col)
        for y in range(9, 14):
            _put(b, 6 + (y % 2), y, (255, 255, 255))
            _put(b, 9 - (y % 2), y, (255, 255, 255))
        out[cid] = b

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for _ in range(90):
        x, y = int(rnd.gauss(7.5, 2.5)), int(rnd.gauss(9, 2.2))
        _put(img, x, y, ((200, 170, 60), (180, 120, 70), (230, 220, 200), (150, 160, 70))[rnd.randrange(4)])
    out["balanced_feed"] = img

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(3, 14):
        for x in range(5, 11):
            _put(img, x, y, (120, 125, 135) if x in (5, 10) or y in (3, 13) else (160, 165, 175))
    for x, y in ((7, 7), (8, 7), (7, 8), (8, 8), (7, 9), (8, 9)):
        _put(img, x, y, (90, 170, 255))
    out["cobalt_60_source"] = img

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(2, 13):
        for y in range(5, 8):
            _put(img, x, y, (150, 150, 160) if y != 5 else (190, 190, 200))
    for y in range(8, 14):
        _put(img, 9, y, (110, 80, 50))
        _put(img, 10, y, (110, 80, 50))
    for x, y in ((12, 4), (13, 4), (12, 3), (13, 3)):
        _put(img, x, y, (220, 180, 50))
    out["gene_gun"] = img

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(2, 15):
        _put(img, 7, y, (230, 235, 240))
        _put(img, 8, y, (200, 210, 220))
    for y in range(6, 12):
        _put(img, 7, y, (90, 200, 220))
        _put(img, 8, y, (70, 170, 200))
    for x in range(4, 12):
        _put(img, x, 2, (60, 60, 70))
    _put(img, 7, 15, (160, 160, 170))
    out["crispr_kit"] = img
    return out


def _dump(path, obj):
    import json
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def generate(assets, data_dir, lang, mod, tag):
    """Textures, models, blockstates, loot, tags, recipes and English names; tag(data_dir, path, values) adds to a tag."""
    tex_b, tex_i = assets / "textures" / "block", assets / "textures" / "item"
    models, states = assets / "models", assets / "blockstates"
    for iid, img in item_textures().items():
        img.save(tex_i / f"{iid}.png")
        parent = "minecraft:item/handheld" if iid in ("gene_gun", "crispr_kit") else "minecraft:item/generated"
        _dump(models / "item" / f"{iid}.json", {"parent": parent, "textures": {"layer0": f"{mod}:item/{iid}"}})
    for bid, faces in block_textures().items():
        for face, img in faces.items():
            img.save(tex_b / f"{bid}_{face}.png")
        _dump(models / "block" / f"{bid}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
            "side": f"{mod}:block/{bid}_side", "top": f"{mod}:block/{bid}_top", "bottom": f"{mod}:block/{bid}_top"}})
        _dump(states / f"{bid}.json", {"variants": {"": {"model": f"{mod}:block/{bid}"}}})
        _dump(models / "item" / f"{bid}.json", {"parent": f"{mod}:block/{bid}"})
        _dump(data_dir / mod / "loot_table" / "blocks" / f"{bid}.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod}:{bid}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
        tag(data_dir, f"minecraft/tags/block/mineable/{BLOCKS[bid][2]}.json", [f"{mod}:{bid}"])
    for rid, r in RECIPES.items():
        _dump(data_dir / mod / "recipe" / f"{rid}.json", r)
    for iid, tags in FEED.items():
        for t in tags:
            tag(data_dir, f"minecraft/tags/item/{t}.json", [f"{mod}:{iid}"])
    lang.update(LANG)


def seed(name):
    return zlib.crc32(name.encode())
