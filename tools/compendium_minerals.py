"""Mineral Compendium (3.15.0, stage 3 of the compendium plan): rocks, sediments, ores and the rocks of other worlds.

Seen once, a rock is named; its composition shows only after it is analysed (look at it for 2 seconds with a
Magnifying Lens), and then only the elements the player knows. Natural rocks, ores and plants also hide their
composition in item tooltips until analysed. Research adds layers: De re metallica (L2, compendium:ores), Theory of
the Earth (L3, compendium:origin), Thin Sections (L4, compendium:petrography), X-ray Diffraction (L5,
compendium:crystals). In the Geological world, with Hutton, the corner panel names the formation underfoot.
Translations: tools/compendium_minerals_<code>.py. Facts written from general geological knowledge (2026-11).
"""

ITEMS = {
    "mineral_compendium": ("Mineral Compendium",
                           "In your hotbar it names the rocks you know; use it to read about them"),
}
SHAPELESS = {"mineral_compendium": (["minecraft:book", "minecraft:flint"], 1)}
FEATURES = {"ores", "origin", "petrography", "crystals"}

LANG = {
    "gui.periodic_elements.compendium.mineral_title": "Mineral Compendium",
    "gui.periodic_elements.compendium.unknown_rock": "Unknown rock",
    "gui.periodic_elements.compendium.new_mineral": "New rock in your Compendium: %s",
    "gui.periodic_elements.compendium.analysed": "Analysed: %s",
    "gui.periodic_elements.compendium.mineral_summary": "%s of %s rocks noted, %s analysed.",
    "gui.periodic_elements.compendium.mineral_help": "Keep this book in your hotbar. Look at a rock for a moment, or "
                                                     "mine it, to note it. Look at it for 2 seconds with a Magnifying "
                                                     "Lens to analyse it. Choose a group on the left.",
    "gui.periodic_elements.compendium.groups": "Groups",
    "gui.periodic_elements.compendium.rocks_here": "Rocks",
    "gui.periodic_elements.compendium.other_world": "A rock of another world: no real rock matches it.",
    "gui.periodic_elements.compendium.depth": "First noted at depth y %s.",
    "gui.periodic_elements.compendium.composition": "Composition (by mass):",
    "gui.periodic_elements.compendium.not_analysed": "Not analysed yet: look at it for 2 seconds with a Magnifying "
                                                     "Lens.",
    "gui.periodic_elements.compendium.no_composition": "No composition is known for it.",
    "gui.periodic_elements.compendium.other_elements": "%s other (elements you do not know yet)",
    "gui.periodic_elements.compendium.ore_mineral": "Mineral: %s.",
    "gui.periodic_elements.compendium.ore_depth": "Found between y %s and %s in this world.",
    "gui.periodic_elements.compendium.class": "Class: %s.",
    "gui.periodic_elements.compendium.minerals": "Made of: %s.",
    "gui.periodic_elements.compendium.formula": "Formula: %s.",
    "gui.periodic_elements.compendium.system": "Crystal system: %s.",
    "gui.periodic_elements.compendium.mineral_hint": "Research De re metallica (level 2) and Theory of the Earth "
                                                     "(level 3) to learn more about each rock.",
    "gui.periodic_elements.compendium.formation": "Formation: %s",
    "tooltip.periodic_elements.not_analysed": "Composition: not analysed (use a Magnifying Lens)",
    "hud.periodic_elements.lens.analysing": "Analysing... keep looking for 2 seconds",
}

GROUPS = {
    "rocks": ("Rocks", "The solid rock of the world, from the stone underfoot to obsidian and glacier ice."),
    "sediments": ("Sediments and Soils", "Loose grains and soils: what weather, water and life make of rock."),
    "ores": ("Ores", "Rocks rich enough in one useful mineral to be worth mining."),
    "other_worlds": ("Other Worlds", "Rocks of the Nether and the End, unlike anything on Earth."),
    "igneous": ("Igneous Rocks", "Rocks frozen from molten magma or lava, deep down or at the surface."),
    "sedimentary": ("Sedimentary Rocks", "Rocks laid down in layers by water, wind and living things."),
    "metamorphic": ("Metamorphic Rocks", "Rocks changed by heat and pressure without melting."),
}
FORMATIONS = {
    "ocean_floor": "Ocean floor", "mountain_belt": "Mountain belt", "granite_dome": "Granite dome",
    "volcanic_neck": "Volcanic neck", "granite_shield": "Granite shield", "sedimentary_basin": "Sedimentary basin",
    "mesa": "Mesa", "basalt_plateau": "Basalt plateau",
}
SYSTEMS = {
    "cubic": "cubic", "tetragonal": "tetragonal", "orthorhombic": "orthorhombic", "hexagonal": "hexagonal",
    "trigonal": "trigonal", "monoclinic": "monoclinic", "triclinic": "triclinic",
    "amorphous": "none (amorphous, no crystal lattice)",
}

GUIDE = [
    "# The Mineral Compendium",
    "A book and a flint make the Mineral Compendium; no research is needed. Keep it in your hotbar and the top-left "
    "corner names the rock you look at once you know it. Look at a rock for a moment from up to 8 blocks away, or "
    "mine it, to note it. Use the book to read: groups on the left, the rocks of each one on the right.",
    "A rock's composition shows only once you have analysed it: hold a Magnifying Lens and look at the rock for 2 "
    "seconds. Even then the book names only the elements you know; the rest is summed as \"other\" and fills in as "
    "your research finds new elements. Until analysed, natural rocks, ores and plants hide their composition in "
    "their tooltips too; made things keep showing it.",
    "Compendium Notes copy all three books at once.",
    "De re metallica (level 2): each ore shows its mineral and the depths where it is found, after Agricola (1556).",
    "Theory of the Earth (level 3): each rock shows its class (igneous, sedimentary, metamorphic) and how it forms, "
    "and the book groups rocks by class, after Hutton (1788). In the Geological world the corner of the screen also "
    "names the formation you stand on.",
    "Thin Sections (level 4): each rock shows the minerals it is made of, after Sorby (1858).",
    "X-ray Diffraction (level 5): each mineral shows its formula and crystal system, after the Braggs (1913).",
]

RESEARCH = [
    # id, level, requires, samples, unlock, name, date, description
    ("de_re_metallica", 2, "lenses;writing", "minecraft:paper*8;minecraft:raw_iron*4;minecraft:raw_copper*4",
     "compendium:ores", "De re metallica", "1556",
     "Agricola describes every ore and how miners find it: the Mineral Compendium shows each ore's mineral and the "
     "depths where it is found"),
    ("theory_of_the_earth", 3, "de_re_metallica;geological_map",
     "minecraft:paper*8;minecraft:granite*4;minecraft:sandstone*4;minecraft:basalt*4", "compendium:origin",
     "Theory of the Earth", "1788",
     "Hutton reads at Siccar Point that rocks are made, worn away and made again: the Compendium sorts rocks into "
     "igneous, sedimentary and metamorphic and names the formation underfoot"),
    ("thin_sections", 4, "theory_of_the_earth;spectroscopy",
     "minecraft:glass*8;minecraft:paper*8;minecraft:diorite*4", "compendium:petrography", "Thin Sections", "1858",
     "Sorby grinds rock to slices thin enough to see through under a microscope: the Compendium shows the minerals "
     "each rock is made of"),
    ("xray_diffraction", 5, "thin_sections;geiger_counter",
     "minecraft:quartz*8;minecraft:amethyst_shard*4;minecraft:paper*8", "compendium:crystals", "X-ray Diffraction",
     "1913",
     "The Braggs bounce X-rays off crystals and read the spacing of their atoms: the Compendium shows each "
     "mineral's formula and crystal system"),
]

_TERRACOTTA = ["terracotta"] + [f"{c}_terracotta" for c in (
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
    "blue", "brown", "green", "red", "black")]


def _r(group, cls, blocks, about, origin=None, made=None, formula=None, system=None, icon=None, mineral=None,
       depth=None, gives=(), items=(), real=True):
    return {"group": group, "cls": cls, "blocks": list(blocks), "icon": icon or blocks[0], "about": about,
            "origin": origin, "made": made, "formula": formula, "system": system, "mineral": mineral,
            "depth": depth, "gives": list(gives), "items": list(items), "real": real}


# id -> entry. Blocks, icon, gives and items are vanilla ids without "minecraft:" or ids with a namespace.
MINERALS = {
    # ---- Rocks ----
    "stone": _r("rocks", "igneous", ["stone", "cobblestone"],
                "Plain grey stone, the bulk of the world under the soil. Its composition is the average of the upper "
                "continental crust.",
                "It stands for the crust as a whole: mostly granite-like rock that cooled from magma, much of it "
                "since changed by heat and pressure.",
                "quartz, feldspar, mica and amphibole"),
    "deepslate": _r("rocks", "metamorphic", ["deepslate", "cobbled_deepslate"],
                    "Dark, hard, layered rock of the depths that splits along flat planes, like slate.",
                    "Mud buried deep and squeezed until its clay turned to mica: slate forms where mountains are "
                    "built.",
                    "mica, chlorite and quartz"),
    "granite": _r("rocks", "igneous", ["granite"],
                  "Speckled pink and grey rock with crystals big enough to see. Kitchen worktops and old city kerbs "
                  "are granite.",
                  "Magma that cooled slowly kilometres underground, so its crystals had time to grow; it reaches the "
                  "surface only after the rock above wears away.",
                  "quartz, potassium feldspar, plagioclase and biotite"),
    "diorite": _r("rocks", "igneous", ["diorite"],
                  "White rock flecked with black, like salt and pepper. The Inca and the Egyptians carved it.",
                  "Magma poorer in silica than granite, cooled slowly underground, often where an ocean plate sinks "
                  "under a continent.",
                  "plagioclase and hornblende, with little quartz"),
    "andesite": _r("rocks", "igneous", ["andesite"],
                   "Fine-grained grey rock, the lava of the Andes that gave it its name.",
                   "Lava erupted by volcanoes above sinking ocean plates, cooled fast at the surface into small "
                   "crystals.",
                   "plagioclase and pyroxene in a fine glassy groundmass"),
    "tuff": _r("rocks", "igneous", ["tuff"],
               "Soft grey rock full of tiny fragments. The Romans built with it and made concrete from its ash.",
               "Volcanic ash thrown out in an eruption, fallen and welded into rock.",
               "shards of volcanic glass, pumice and broken crystals"),
    "basalt": _r("rocks", "igneous", ["basalt", "smooth_basalt", "polished_basalt"],
                 "Dark, heavy rock that often cracks into tall six-sided columns, as at the Giant's Causeway. It "
                 "floors all the oceans.",
                 "Runny lava from the mantle, erupted at mid-ocean ridges, hotspots like Hawaii and huge floods like "
                 "the Deccan Traps.",
                 "plagioclase, augite and olivine", formula=None),
    # ---- stage 4a (3.42.0): the Geological world's own rocks ----
    "shale": _r("rocks", "sedimentary", ["periodic_elements:shale", "periodic_elements:cobbled_shale", "periodic_elements:polished_shale"],
         "Dark grey rock that splits into thin sheets. Most of the world's sedimentary rock is shale.",
         'Mud and clay settled in quiet seas and lakes, then buried and pressed into rock; the dark kinds hold the remains of algae that turn into oil.',
         'clay minerals, fine quartz and a little organic matter'),
    "gneiss": _r("rocks", "metamorphic", ["periodic_elements:gneiss", "periodic_elements:cobbled_gneiss", "periodic_elements:polished_gneiss"],
         'Hard rock striped in light and dark bands, often folded like toffee. Some of the oldest rocks on Earth are gneiss.',
         'Granite or sediment cooked and squeezed deep under growing mountains until its minerals sorted into bands.',
         'quartz and feldspar in the light bands, biotite and hornblende in the dark ones'),
    "peridotite": _r("rocks", "igneous", ["periodic_elements:peridotite", "periodic_elements:cobbled_peridotite", "periodic_elements:polished_peridotite"],
         "Heavy dark green rock made almost entirely of olivine. The Earth's mantle is peridotite.",
         'Rock of the mantle, brought up where plates pull apart or pushed onto the continents in slivers when an ocean closes.',
         'olivine and pyroxene, with grains of chromite'),
    "kimberlite": _r("rocks", "igneous", ["periodic_elements:kimberlite", "periodic_elements:cobbled_kimberlite", "periodic_elements:polished_kimberlite"],
         'Blue-green rock full of broken fragments that fills narrow pipes; the only rock that brings diamonds up from the deep.',
         'Gas-rich magma from 150 kilometres down that shot to the surface in hours, tearing pieces of the mantle with it.',
         'altered olivine (serpentine), phlogopite mica and calcite, with fragments of mantle rock'),
    "laterite": _r("rocks", "sedimentary", ["periodic_elements:laterite", "periodic_elements:cobbled_laterite", "periodic_elements:polished_laterite"],
         'Rust-red, porous rock that is soft when dug and hardens in the air; in India it was cut into bricks, which gave it its name.',
         'Tropical rain washes everything soluble out of a rock over millions of years and leaves the iron and aluminium oxides behind.',
         'goethite, hematite, gibbsite and kaolinite'),
    "obsidian": _r("rocks", "igneous", ["obsidian"],
                   "Black volcanic glass with edges sharper than steel; people made knives and arrowheads of it for "
                   "thousands of years.",
                   "Silica-rich lava cooled so fast that no crystals could form.",
                   "volcanic glass, with almost no crystals", formula="SiO2", system="amorphous"),
    "calcite": _r("rocks", "sedimentary", ["calcite"],
                  "White rock of calcium carbonate that fizzes in acid. In the Geological world it stands for the "
                  "limestone of the basins, where caves form.",
                  "Shells and skeletons of sea creatures piled on the sea floor and hardened into limestone.",
                  "calcite", formula="CaCO3", system="trigonal"),
    "dripstone": _r("rocks", "sedimentary", ["dripstone_block", "pointed_dripstone"],
                    "Stalactites and stalagmites, grown one thin ring at a time; a centimetre can take a hundred "
                    "years.",
                    "Water that dissolved limestone drips in a cave, loses its carbon dioxide and leaves the calcite "
                    "behind.",
                    "calcite in growth layers", formula="CaCO3", system="trigonal"),
    "sandstone": _r("rocks", "sedimentary", ["sandstone"],
                    "Tan rock of sand grains you can see and feel. Petra and many cathedrals are carved from it.",
                    "Sand of old beaches, rivers and deserts, buried and cemented grain to grain.",
                    "quartz grains cemented by calcite or silica"),
    "red_sandstone": _r("rocks", "sedimentary", ["red_sandstone"],
                        "Rusty red sandstone, the rock of the canyons and mesas of the American Southwest.",
                        "Desert sand whose grains were coated with iron oxide before they were cemented.",
                        "quartz grains coated with hematite"),
    "terracotta": _r("rocks", "sedimentary", _TERRACOTTA,
                     "Hard bands of clay in red, orange, yellow and brown that stripe the badlands.",
                     "Clay and silt laid down in lakes and floodplains, hardened and stained by iron.",
                     "clay minerals, quartz and iron oxides"),
    "amethyst": _r("rocks", "igneous", ["amethyst_block", "budding_amethyst", "amethyst_cluster", "large_amethyst_bud",
                                        "medium_amethyst_bud", "small_amethyst_bud"],
                   "Purple quartz that lines the inside of geodes. Its colour comes from traces of iron and natural "
                   "radiation.",
                   "Hot water seeping through gas bubbles in old lava grew crystals inward, layer by layer.",
                   "quartz coloured by iron", formula="SiO2", system="trigonal", icon="amethyst_cluster"),
    "ice": _r("rocks", "metamorphic", ["ice", "packed_ice", "blue_ice"],
              "Ice is a mineral, and glacier ice is a rock. Old, dense ice turns blue because it absorbs red light.",
              "Snow buried under more snow is pressed into firn and then into solid ice, without melting.",
              "ice crystals and trapped air bubbles", formula="H2O", system="hexagonal"),
    # ---- Sediments and soils ----
    "sand": _r("sediments", "sedimentary", ["sand", "suspicious_sand"],
               "Pale grains of quartz, the mineral that survives when the rest of a rock has weathered away.",
               "Rock broken by weather, carried by rivers and wind and sorted until only hard quartz is left.",
               "quartz with a little feldspar"),
    "red_sand": _r("sediments", "sedimentary", ["red_sand"],
                   "Sand stained red by a coat of iron oxide, the colour of hot deserts.",
                   "Desert sand that rusted: iron in the grains oxidised in the dry heat.",
                   "quartz grains coated with iron oxide"),
    "gravel": _r("sediments", "sedimentary", ["gravel", "suspicious_gravel"],
                 "Loose pebbles of every kind of rock, rounded by rolling in rivers and surf.",
                 "Rock broken by frost and rivers, not yet cemented into conglomerate.",
                 "rock fragments of every kind"),
    "clay": _r("sediments", "sedimentary", ["clay"],
               "Smooth, sticky grey earth that holds water and can be shaped and fired into pottery and brick.",
               "Feldspar weathered by water into tiny flakes of clay minerals, settled in still water.",
               "kaolinite and other clay minerals"),
    "dirt": _r("sediments", "sedimentary", ["dirt", "coarse_dirt", "rooted_dirt", "grass_block", "podzol", "mycelium"],
               "Soil: weathered rock mixed with humus, water, air and countless living things. A handful holds "
               "billions of bacteria.",
               "Rock weathered in place and mixed with the remains of plants and animals over thousands of years.",
               "quartz, clay minerals and humus"),
    "terra_roxa": _r("sediments", "sedimentary", ["periodic_elements:terra_roxa"],
         "Dark red, deep and fertile soil of weathered basalt. Brazil's coffee frontier followed it across São Paulo and Paraná.",
         'Basalt weathered for a long time in a warm, wet climate: its iron turns to red oxides and its feldspar to clay.',
         'kaolinite, iron oxides (hematite, magnetite) and gibbsite'),
    "chalky_soil": _r("sediments", "sedimentary", ["periodic_elements:chalky_soil"],
         'Pale, crumbly soil full of lime over limestone; vines and beech woods like it.',
         'Thin soil weathered from limestone; the rain dissolves the calcite slowly, so lime stays in it.',
         'calcite, quartz silt and clay, with humus'),
    "mud": _r("sediments", "sedimentary", ["mud"],
              "Waterlogged soil of swamps and riverbanks. Buried and pressed, mud becomes shale.",
              "Fine silt and clay settled where water stands still.",
              "clay, silt and water"),
}

# ---- Ores ----
_VANILLA_ORES = {
    # id: (blocks, mineral, formula, system, depth, gives, items, about)
    "coal": (["coal_ore", "deepslate_coal_ore"], "coal", "C", "amorphous", (0, 320), ["coal"], [],
             "Black seams of coal in grey stone: the remains of swamp forests buried 300 million years ago."),
    "iron": (["iron_ore", "deepslate_iron_ore"], "hematite and magnetite", "Fe2O3", "trigonal", (-64, 320),
             ["raw_iron"], ["raw_iron"],
             "Rusty flecks of iron oxide. Most iron ore was laid down on the sea floor when the first oxygen rusted "
             "the oceans' iron."),
    "copper": (["copper_ore", "deepslate_copper_ore"], "chalcopyrite", "CuFeS2", "tetragonal", (-16, 112),
               ["raw_copper"], ["raw_copper"],
               "Brassy copper sulfide with green stains of malachite, the first ore people smelted."),
    "gold": (["gold_ore", "deepslate_gold_ore"], "native gold", "Au", "cubic", (-64, 256), ["raw_gold"],
             ["raw_gold"],
             "Grains of native gold in quartz veins. Gold does not rust, so it is found as metal."),
    "redstone": (["redstone_ore", "deepslate_redstone_ore"], "cinnabar", "HgS", "trigonal", (-64, 15),
                 ["redstone"], [],
                 "In this mod redstone is cinnabar, the bright red ore of mercury, mined at Almadén for 2,000 years."),
    "lapis": (["lapis_ore", "deepslate_lapis_ore"], "lazurite", "(Na,Ca)8Al6Si6O24(S,SO4)", "cubic", (-64, 64),
              ["lapis_lazuli"], [],
              "Deep blue lapis lazuli, ground for centuries into ultramarine, the costliest paint of the Renaissance."),
    "diamond": (["diamond_ore", "deepslate_diamond_ore"], "diamond", "C", "cubic", (-64, 16), ["diamond"], [],
                "Carbon crushed 150 km down and shot up in kimberlite pipes; the hardest natural mineral."),
    "emerald": (["emerald_ore", "deepslate_emerald_ore"], "beryl (emerald)", "Be3Al2Si6O18", "hexagonal", (-16, 320),
                ["emerald"], [],
                "Green beryl coloured by chromium, found in mountain veins; Colombia's mines are the most famous."),
    "nether_quartz": (["nether_quartz_ore"], "quartz", "SiO2", "trigonal", (10, 117), ["quartz"], [],
                      "White quartz crystals in netherrack."),
    "nether_gold": (["nether_gold_ore"], "native gold", "Au", "cubic", (10, 117), ["gold_nugget"], [],
                    "Specks of gold in netherrack."),
}
for _id, (_b, _m, _f, _s, _d, _g, _i, _a) in _VANILLA_ORES.items():
    MINERALS[_id] = _r("ores", None, _b, _a, mineral=_m, formula=_f, system=_s, depth=_d, gives=_g, items=_i,
                       icon=_b[0])

# Mod ores (tools/ores.py): id -> (mineral, formula, crystal system, about).
_MOD_ORES = {
    "cassiterite": ("cassiterite", "SnO2", "tetragonal",
                    "Heavy brown-black tin oxide from granite veins; Cornwall's tin drew traders from the Bronze Age "
                    "on."),
    "galena": ("galena", "PbS", "cubic", "Silvery cubes of lead sulfide that break into smaller cubes; it often holds "
                                         "silver."),
    "sphalerite": ("sphalerite", "ZnS", "cubic", "Resinous brown zinc sulfide, the main ore of zinc; miners called it "
                                                 "blende, the deceiver."),
    "sulfur": ("native sulfur", "S", "orthorhombic", "Yellow crystals of pure sulfur grown around volcanic vents."),
    "saltpetre": ("nitre", "KNO3", "orthorhombic", "A white crust of nitre in dry rock, once scraped from cellars to "
                                                   "make gunpowder."),
    "cobaltite": ("cobaltite", "CoAsS", "orthorhombic", "Silver-white cobalt arsenic sulfide; kobolds, goblins of the "
                                                        "mines, were blamed for its poison."),
    "chromite": ("chromite", "FeCr2O4", "cubic", "Black, slightly metallic iron chromium oxide in layers and pods at "
                                                "the base of great basalt and peridotite bodies; nearly all chromium "
                                                "comes from it."),
    "pentlandite": ("pentlandite", "(Fe,Ni)9S8", "cubic", "Bronze-yellow iron nickel sulfide that sinks to the floor "
                                                         "of cooling magma; the main ore of nickel."),
    "stibnite": ("stibnite", "Sb2S3", "orthorhombic", "Steel-grey needles of antimony sulfide; ancient Egyptians used "
                                                      "it as black eye paint."),
    "arsenopyrite": ("arsenopyrite", "FeAsS", "monoclinic", "Silver-white iron arsenic sulfide that smells of garlic "
                                                            "when struck."),
    "barite": ("barite", "BaSO4", "orthorhombic", "Heavy spar, so dense a small piece feels like metal; drilling mud "
                                                  "uses most of it."),
    "spodumene": ("spodumene", "LiAlSi2O6", "monoclinic", "Pale lilac laths in pegmatite, a hard-rock source of the "
                                                          "lithium in batteries."),
    "borax": ("borax", "Na2B4O7·10H2O", "monoclinic", "White borax left by dried-up desert lakes, hauled out of Death "
                                                      "Valley by twenty-mule teams."),
    "pitchblende": ("uraninite", "UO2", "cubic", "Pitch-black uranium oxide; Marie and Pierre Curie found polonium "
                                                 "and radium in it."),
    "bauxite": ("gibbsite and boehmite", "Al(OH)3", "monoclinic", "Red clay left when tropical rain washes everything "
                                                                  "but aluminium and iron out of rock."),
    "pyrolusite": ("pyrolusite", "MnO2", "tetragonal", "Sooty black manganese oxide; cave painters used it as black "
                                                       "pigment."),
    "wolframite": ("wolframite", "(Fe,Mn)WO4", "monoclinic", "Heavy black tungsten ore in quartz veins; tin smelters "
                                                             "said it ate tin like a wolf."),
    "vanadinite": ("vanadinite", "Pb5(VO4)3Cl", "hexagonal", "Bright red hexagonal crystals of lead vanadate from "
                                                             "the dry oxidised zones of lead deposits."),
    "fluorite": ("fluorite", "CaF2", "cubic", "Purple and green cubes that glow under ultraviolet light, the "
                                              "glow named after it."),
    "heavy_mineral_sand": ("ilmenite, zircon and monazite", "FeTiO3", "trigonal",
                           "Black beach sand of heavy minerals, sorted by the waves; the source of titanium and "
                           "zirconium."),
    "molybdenite": ("molybdenite", "MoS2", "hexagonal", "Soft grey flakes like graphite, but heavier and with a blue "
                                                        "tint."),
    "coltan": ("columbite-tantalite", "(Fe,Mn)(Nb,Ta)2O6", "orthorhombic",
               "Heavy black ore of niobium and tantalum, the tantalum in every phone's capacitors."),
    "oil_shale": ("kerogen in shale", None, "amorphous", "Dark shale full of kerogen, the waxy remains of algae; "
                                                         "heated, it gives oil."),
}


def _mod_ore_blocks(mid):
    import ores
    blocks = [k for k in ores.ORES if k.replace("deepslate_", "").replace("nether_", "").replace("_ore", "")
              .replace("_crust", "") == mid]
    assert blocks, mid
    return blocks


def _depth(blocks):
    import ores
    lo, hi = None, None
    for fid, (targets, size, count, height, biomes) in ores.FEATURES.items():
        if any(o in blocks for _, o in targets):
            ys = [v for k, v in _anchors(height)]
            lo = min(ys) if lo is None else min(lo, min(ys))
            hi = max(ys) if hi is None else max(hi, max(ys))
    return lo, hi


def _anchors(height):
    out = []
    for k in ("min_inclusive", "max_inclusive"):
        a = height.get(k, {})
        if "absolute" in a:
            out.append((k, a["absolute"]))
    return out


def _mod_ores():
    import ores
    for mid, (mineral, formula, system, about) in _MOD_ORES.items():
        blocks = _mod_ore_blocks(mid)
        drop = ores.ORES[blocks[0]][3]
        MINERALS[mid] = _r("ores", None, [f"periodic_elements:{b}" for b in blocks], about, mineral=mineral,
                           formula=formula, system=system, depth=_depth(blocks), gives=[drop],
                           items=[drop] if ":raw_" in drop else [], icon=f"periodic_elements:{blocks[0]}")


_mod_ores()

# ---- Other worlds ----
MINERALS.update({
    "netherrack": _r("other_worlds", None, ["netherrack"],
                     "The red rock of the Nether, light and crumbly, that burns for ever once lit. This mod reads it as "
                     "a scoria rich in iron and sulfur.", real=False),
    "blackstone": _r("other_worlds", None, ["blackstone", "gilded_blackstone"],
                     "Dark Nether rock like a sulfur-rich basalt, sometimes veined with gold.", real=False),
    "soul_sand": _r("other_worlds", None, ["soul_sand", "soul_soil"],
                    "Grey-brown sand that slows whatever walks on it; the mod reads it as sand with bone and organic "
                    "matter.", real=False),
    "glowstone": _r("other_worlds", None, ["glowstone"],
                    "Glowing crystal clusters hanging from the Nether's roof; the mod reads them as a zinc sulfide "
                    "phosphor, like old watch dials.", real=False),
    "end_stone": _r("other_worlds", None, ["end_stone"],
                    "Pale stone of the End's floating islands; its proposed composition is close to Moon rock.",
                    real=False),
})


def _ns(x):
    return x if ":" in x else f"minecraft:{x}"


def runtime_entries():
    out = []
    for mid, m in MINERALS.items():
        out.append({"id": mid, "group": m["group"], "cls": m["cls"], "icon": _ns(m["icon"]),
                    "blocks": [_ns(b) for b in m["blocks"]], "items": [_ns(i) for i in m["items"]],
                    "gives": [_ns(g) for g in m["gives"]], "formula": m["formula"], "system": m["system"],
                    "depth": list(m["depth"]) if m["depth"] else None, "real": m["real"],
                    "origin": bool(m["origin"]), "made": bool(m["made"]), "mineral": bool(m["mineral"])})
    return out


def write_lang(lang, mod):
    for mid, m in MINERALS.items():
        p = f"compendium.{mod}.mineral.{mid}"
        lang[p + ".about"] = m["about"]
        for k in ("origin", "made", "mineral"):
            if m[k]:
                lang[f"{p}.{k}"] = m[k]
    for g, (name, about) in GROUPS.items():
        lang[f"compendium.{mod}.group.{g}"] = name
        lang[f"compendium.{mod}.group.{g}.about"] = about
    for f, name in FORMATIONS.items():
        lang[f"compendium.{mod}.formation.{f}"] = name
    for s, name in SYSTEMS.items():
        lang[f"compendium.{mod}.system.{s}"] = name


def item_texture():
    """A grey cloth-bound book with a crystal on the cover."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cloth, cloth_d, paper = (96, 92, 88), (62, 58, 56), (236, 226, 196)
    for y in range(1, 15):
        for x in range(2, 14):
            px[x, y] = cloth_d + (255,)
    for y in range(1, 14):
        for x in range(3, 14):
            px[x, y] = cloth + (255,)
    for x in range(3, 14):
        px[x, 14] = paper + (255,)
    light, mid, dark = (200, 160, 230), (150, 100, 200), (100, 60, 150)
    for x, y in [(8, 3), (7, 4), (8, 4), (9, 4), (7, 5), (8, 5), (9, 5), (7, 6), (8, 6), (9, 6), (7, 7), (8, 7),
                 (9, 7), (8, 8)]:
        px[x, y] = mid + (255,)
    for x, y in [(8, 3), (7, 4), (7, 5), (7, 6)]:
        px[x, y] = light + (255,)
    for x, y in [(9, 5), (9, 6), (9, 7), (8, 8)]:
        px[x, y] = dark + (255,)
    for x, y in [(6, 9), (7, 9), (8, 9), (9, 9), (10, 9)]:
        px[x, y] = (120, 112, 104, 255)
    return img
