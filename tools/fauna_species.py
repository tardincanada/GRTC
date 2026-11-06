"""Animals of the world (3.50.0): one row per species. tools/fauna.py turns these into entity types, spawn rules,
models, textures, loot tables and the Animal Compendium; data/fauna.csv is written from this table for reading.

Fields
  stage     plan stage (1 tropical + arid, 2 temperate + rivers, 3 cold + mountain, 4 sea + coast)
  name      English name; sci, family (family · order · class)
  body      model template (fauna_models.TEMPLATES); shape = template parameters; pal = colours; pattern
  kind      land, amphibious (walks and swims, breathes water), water (swims only), school (schooling fish)
  behaviour passive, neutral (fights back), night (attacks players at night), touch (poisons who touches it)
  biomes    where it spawns; group = (min, max); weight = spawn weight
  size      (width, height) of the hitbox in blocks; scale = model scale; length = real length in metres
  hp, speed, attack
  food      items it eats (breeds with, follows); drops = [(item, min, max)]
  traits    eggs:<item>, down, milk, shear:<item>, antlers, manure, venom:<item>, blood, arrows, guano, dam, honey,
            hunts_rabbits, cure_pests, eats_crops, strand, invulnerable, sting, leaves, winter_coat, bucket, flutter,
            glow, slow
  iucn      Red List status
"""

S = []


def sp(id, stage, name, sci, family, body, kind, behaviour, biomes, group, weight, size, length, hp, speed, attack=0,
       food=(), drops=(), traits=(), iucn="LC", shape=None, pal=None, pattern=None, scale=1.0, spawn=None):
    S.append(dict(id=id, stage=stage, name=name, sci=sci, family=family, body=body, kind=kind, behaviour=behaviour,
                  biomes=biomes.split(";"), group=group, weight=weight, size=size, length=length, hp=hp, speed=speed,
                  attack=attack, food=list(food), drops=list(drops), traits=list(traits), iucn=iucn, shape=shape or {},
                  pal=pal or {}, pattern=pattern, scale=scale, spawn=spawn))


M = "minecraft:"
P = "periodic_elements:"
FISH = [M + "cod", M + "salmon"]

# ---------------- stage 1: tropical ----------------
sp("capybara", 1, "Capybara", "Hydrochoerus hydrochaeris", "Caviidae · Rodentia · Mammalia", "quad", "amphibious", "passive",
   "mangrove_swamp;jungle;sparse_jungle", (2, 5), 8, (0.9, 0.8), 1.2, 12, 0.22,
   food=[M + "sugar_cane"], drops=[(P + "game_meat", 1, 2), (M + "leather", 0, 1)], traits=["manure"],
   shape=dict(bl=14, bw=9, bh=8, ll=4, lw=3, hl=6, hw=6, hh=6, sl=2, sw=5, sh=4, ew=1, eh=1, tl=0),
   pal=dict(body="8B5E3C", belly="A87B55", snout="5C3D28", leg="6E4A2E"))
sp("tapir", 1, "South American Tapir", "Tapirus terrestris", "Tapiridae · Perissodactyla · Mammalia", "quad", "amphibious",
   "passive", "jungle;bamboo_jungle", (1, 2), 5, (1.0, 1.1), 2.0, 20, 0.2,
   food=[M + "melon_slice", M + "apple"], drops=[(P + "game_meat", 1, 3), (M + "leather", 0, 2)], traits=["manure"],
   iucn="VU", shape=dict(bl=18, bw=9, bh=10, ll=7, lw=3, hl=7, hw=6, hh=6, sl=3, sw=3, sh=3, ew=2, eh=2, tl=1),
   pal=dict(body="3D3530", belly="4A403A", ear="D8D0C8", snout="2A2420"))
sp("toco_toucan", 1, "Toco Toucan", "Ramphastos toco", "Ramphastidae · Piciformes · Aves", "bird", "land", "passive",
   "jungle;sparse_jungle", (1, 3), 6, (0.4, 0.6), 0.6, 6, 0.25,
   food=[M + "melon_slice", M + "sweet_berries"], drops=[(M + "feather", 1, 2)], traits=["flutter"],
   shape=dict(bl=6, bw=4, bh=5, ll=2, nl=0, hl=3, hw=3, hh=3, bkl=6, bkw=2, bkh=3, wl=5, wh=4, tl=4, tw=3),
   pal=dict(body="1A1A1A", head="1A1A1A", throat="F4F2E8", beak="F28A1A", tip="1A1A1A", leg="5A6A8A", eyering="3AA0E0"))
sp("sloth", 1, "Brown-throated Sloth", "Bradypus variegatus", "Bradypodidae · Pilosa · Mammalia", "quad", "land", "passive",
   "jungle", (1, 1), 4, (0.6, 0.6), 0.6, 10, 0.04,
   food=[M + "jungle_leaves"], traits=["leaves", "slow"],
   shape=dict(bl=11, bw=7, bh=7, ll=6, lw=2, hl=4, hw=5, hh=4, sl=1, sw=3, sh=2, ew=0, eh=0, tl=0, shaggy=1, claws=1),
   pal=dict(body="8A7A5E", belly="9A8A6E", head="C8B898", snout="3A3028", mask="3A3028", claw="2A2420"), pattern="algae")
sp("golden_poison_frog", 1, "Golden Poison Frog", "Phyllobates terribilis", "Dendrobatidae · Anura · Amphibia", "frog",
   "amphibious", "touch", "jungle;bamboo_jungle", (1, 3), 6, (0.3, 0.25), 0.05, 4, 0.2,
   food=[M + "spider_eye"], traits=["arrows"], iucn="EN",
   shape=dict(bl=5, bw=4, bh=3), pal=dict(body="F2C21E", belly="F8D850", leg="E0A818"))
sp("caiman", 1, "Broad-snouted Caiman", "Caiman latirostris", "Alligatoridae · Crocodylia · Reptilia", "reptile",
   "amphibious", "neutral", "mangrove_swamp", (1, 2), 4, (1.0, 0.5), 2.5, 24, 0.18, 5,
   food=FISH, drops=[(M + "leather", 1, 3), (P + "game_meat", 1, 2)],
   shape=dict(bl=16, bw=7, bh=4, ll=3, lw=2, hl=5, hw=6, hh=3, sl=7, tl=18, tw=4),
   pal=dict(body="4A4A32", belly="B8B090", scute="2E2E20"))
# ---------------- stage 1: arid ----------------
sp("ostrich", 1, "Common Ostrich", "Struthio camelus", "Struthionidae · Struthioniformes · Aves", "bird", "land", "passive",
   "savanna;savanna_plateau", (3, 6), 8, (0.9, 2.2), 2.4, 20, 0.32,
   food=[M + "wheat", M + "wheat_seeds"], drops=[(M + "feather", 2, 4), (P + "game_meat", 1, 3)],
   traits=["eggs:" + P + "ostrich_egg", "manure"],
   shape=dict(bl=12, bw=9, bh=9, ll=14, lw=2, nl=14, nw=2, hl=3, hw=3, hh=2, bkl=2, bkw=2, bkh=1, wl=7, wh=5, tl=3, tw=6),
   pal=dict(body="1E1A18", wing="F0EEE6", tail="F0EEE6", head="C8A898", neck="C8A898", leg="C8A898", beak="D8B8A0"))
sp("zebra", 1, "Plains Zebra", "Equus quagga", "Equidae · Perissodactyla · Mammalia", "quad", "land", "passive",
   "savanna;savanna_plateau;windswept_savanna", (4, 8), 8, (1.2, 1.4), 2.3, 22, 0.3,
   food=[M + "wheat"], drops=[(M + "leather", 1, 2), (P + "game_meat", 1, 2)], traits=["manure"], iucn="NT",
   shape=dict(bl=18, bw=8, bh=9, ll=12, lw=3, nl=8, nw=4, hl=7, hw=4, hh=5, sl=0, ew=1, eh=3, tl=7, tw=1, mane=1),
   pal=dict(body="EEEEE8", belly="F4F4F0", stripe="1E1E1E", snout="1E1E1E", mane="1E1E1E", tail="1E1E1E"), pattern="stripes")
sp("deathstalker", 1, "Deathstalker Scorpion", "Leiurus quinquestriatus", "Buthidae · Scorpiones · Arachnida", "scorpion",
   "land", "night", "desert;badlands;eroded_badlands", (1, 2), 5, (0.5, 0.3), 0.08, 6, 0.28, 2,
   traits=["venom:" + P + "scorpion_venom"], iucn="NE",
   shape=dict(bl=6, bw=4, bh=2), pal=dict(body="D8C060", leg="E0CC78", sting="6A4A20"))
sp("desert_locust", 1, "Desert Locust", "Schistocerca gregaria", "Acrididae · Orthoptera · Insecta", "locust", "land",
   "passive", "desert;savanna", (6, 12), 6, (0.3, 0.2), 0.07, 2, 0.3,
   food=[M + "wheat"], drops=[(P + "locust", 0, 1)], traits=["eats_crops", "flutter"],
   shape=dict(bl=6, bw=2, bh=2), pal=dict(body="C8A830", wing="B8A880", leg="A88820", spot="2A2010"), pattern="spots")
sp("gila_monster", 1, "Gila Monster", "Heloderma suspectum", "Helodermatidae · Squamata · Reptilia", "reptile", "land",
   "neutral", "badlands;eroded_badlands;wooded_badlands;desert", (1, 1), 4, (0.6, 0.35), 0.5, 10, 0.16, 3,
   food=[M + "egg"], traits=["venom:" + P + "gila_venom", "bite"], iucn="NT",
   shape=dict(bl=9, bw=5, bh=3, ll=2, lw=2, hl=4, hw=4, hh=3, sl=1, tl=6, tw=3),
   pal=dict(body="1E1A18", belly="2A2420", blotch="E07A5A"), pattern="blotches")

# ---------------- stage 2: temperate and rivers ----------------
sp("red_deer", 2, "Red Deer", "Cervus elaphus", "Cervidae · Artiodactyla · Mammalia", "quad", "land", "passive",
   "forest;dark_forest;birch_forest;old_growth_birch_forest", (2, 5), 8, (0.9, 1.4), 2.0, 18, 0.32,
   food=[M + "apple", M + "wheat"], drops=[(P + "game_meat", 1, 3), (M + "leather", 1, 2)], traits=["antlers", "manure"],
   shape=dict(bl=16, bw=7, bh=8, ll=13, lw=2, nl=7, nw=3, hl=6, hw=4, hh=4, sl=2, sw=3, sh=3, ew=1, eh=3, tl=2, horn="antler", hs=8),
   pal=dict(body="8A5A33", belly="C9A27A", rump="D9C29A", horn="E0D0B0", snout="3A2A1A"))
sp("wild_boar", 2, "Wild Boar", "Sus scrofa", "Suidae · Artiodactyla · Mammalia", "quad", "land", "neutral",
   "forest;dark_forest", (2, 4), 8, (0.9, 0.9), 1.5, 16, 0.25, 4,
   food=[M + "carrot", M + "potato", M + "beetroot"], drops=[(M + "porkchop", 1, 3), (P + "boar_bristle", 0, 2)],
   traits=["manure"], shape=dict(bl=16, bw=8, bh=9, ll=6, lw=3, hl=7, hw=6, hh=6, sl=3, sw=4, sh=3, ew=1, eh=2, tl=3, horn="tusks", mane=1),
   pal=dict(body="4A3B30", belly="5A4A3E", mane="2A221C", snout="6B5040", horn="F0E8D8"))
sp("eurasian_beaver", 2, "Eurasian Beaver", "Castor fiber", "Castoridae · Rodentia · Mammalia", "quad", "amphibious",
   "passive", "river;swamp", (1, 2), 5, (0.6, 0.5), 1.0, 12, 0.2,
   food=[P + "willow_bark", M + "stick"], drops=[(P + "castoreum", 0, 1), (M + "leather", 0, 1)], traits=["dam"],
   shape=dict(bl=12, bw=8, bh=7, ll=3, lw=2, hl=5, hw=5, hh=4, sl=1, sw=3, sh=3, ew=1, eh=1, tl=7, tw=5, th=1, tdrop=0.05),
   pal=dict(body="5C3A22", belly="6A4630", tail="3A3330", snout="2A1A10", tooth="E08A2A"))
sp("pheasant", 2, "Common Pheasant", "Phasianus colchicus", "Phasianidae · Galliformes · Aves", "bird", "land", "passive",
   "plains;sunflower_plains;meadow;flower_forest", (2, 4), 8, (0.5, 0.6), 0.8, 8, 0.28,
   food=[M + "wheat_seeds", M + "melon_seeds", M + "pumpkin_seeds"], drops=[(M + "feather", 1, 3), (M + "chicken", 1, 1)],
   traits=["eggs:" + P + "pheasant_egg", "flutter"],
   shape=dict(bl=7, bw=5, bh=5, ll=4, lw=1, nl=2, nw=2, hl=3, hw=3, hh=3, bkl=1, bkw=1, bkh=1, wl=5, wh=4, tl=10, tw=2, th=1),
   pal=dict(body="B05A24", head="1E4A30", neck="1E4A30", face="C8241E", collar="F0F0EA", tail="8A5A30", wing="9A7A50", leg="8A7A6A", beak="D8C8A0"))
sp("mallard", 2, "Mallard", "Anas platyrhynchos", "Anatidae · Anseriformes · Aves", "bird", "amphibious", "passive",
   "river;swamp", (2, 4), 8, (0.5, 0.6), 0.6, 8, 0.25,
   food=[M + "wheat_seeds", M + "bread"], drops=[(M + "feather", 1, 2), (M + "chicken", 1, 1)],
   traits=["eggs:" + P + "duck_egg", "down", "flutter"],
   shape=dict(bl=8, bw=5, bh=4, ll=2, lw=1, nl=3, nw=2, hl=3, hw=3, hh=3, bkl=3, bkw=2, bkh=1, wl=6, wh=3, tl=2, tw=3),
   pal=dict(body="8A8478", breast="6A3A2A", head="1E5A3A", neck="1E5A3A", collar="F0F0EA", beak="E0C030", leg="E08A2A", wing="7A7468", speculum="3A4AA0"))
sp("american_alligator", 2, "American Alligator", "Alligator mississippiensis", "Alligatoridae · Crocodylia · Reptilia",
   "reptile", "amphibious", "neutral", "swamp", (1, 2), 4, (1.4, 0.6), 4.0, 30, 0.18, 6,
   food=FISH, drops=[(M + "leather", 2, 3), (P + "game_meat", 1, 3)],
   shape=dict(bl=22, bw=9, bh=5, ll=4, lw=3, hl=6, hw=8, hh=4, sl=9, tl=24, tw=5),
   pal=dict(body="2A2E24", belly="C8C0A0", scute="1A1E16"))
sp("hedgehog", 2, "European Hedgehog", "Erinaceus europaeus", "Erinaceidae · Eulipotyphla · Mammalia", "quad", "land",
   "passive", "flower_forest;plains;cherry_grove;forest", (1, 2), 5, (0.4, 0.35), 0.25, 6, 0.18,
   food=[P + "earthworm", M + "spider_eye"], traits=["cure_pests"],
   shape=dict(bl=7, bw=6, bh=5, ll=1, lw=1, hl=3, hw=3, hh=3, sl=2, sw=2, sh=1, ew=1, eh=1, tl=0),
   pal=dict(body="6E5A46", tip="E8DCC8", head="B8A080", snout="3A2A1A", leg="8A7058"), pattern="spines")
sp("sturgeon", 2, "European Sturgeon", "Acipenser sturio", "Acipenseridae · Acipenseriformes · Actinopterygii", "fish",
   "water", "passive", "river;frozen_river", (1, 2), 3, (0.7, 0.4), 2.0, 14, 0.6,
   drops=[(P + "caviar", 0, 1), (P + "isinglass", 0, 1), (M + "cod", 1, 1)], iucn="CR",
   shape=dict(bl=24, bw=4, bh=4, snout=4, scutes=1), pal=dict(body="6A6A5A", belly="D8D0C0", scute="E8E0D0", fin="5A5A4A"))
sp("peruvian_booby", 2, "Peruvian Booby", "Sula variegata", "Sulidae · Suliformes · Aves", "bird", "land", "passive",
   "stony_shore;beach", (4, 8), 6, (0.5, 0.6), 0.75, 8, 0.25,
   food=FISH, drops=[(M + "feather", 1, 2)], traits=["guano", "flutter"],
   shape=dict(bl=8, bw=5, bh=5, ll=2, lw=1, nl=2, nw=2, hl=3, hw=3, hh=3, bkl=4, bkw=1, bkh=2, wl=8, wh=4, tl=3, tw=2),
   pal=dict(body="6A5A4A", belly="F0F0EC", head="F0F0EC", neck="F0F0EC", wing="5A4A3A", beak="7A8A9A", leg="6A7A9A"), pattern="scales")

# ---------------- stage 3: cold and mountains ----------------
sp("reindeer", 3, "Reindeer", "Rangifer tarandus", "Cervidae · Artiodactyla · Mammalia", "quad", "land", "passive",
   "snowy_plains;snowy_taiga;ice_spikes", (3, 6), 8, (0.9, 1.4), 2.0, 18, 0.3,
   food=[P + "reindeer_lichen", M + "wheat"], drops=[(P + "game_meat", 1, 3), (M + "leather", 1, 2)],
   traits=["milk", "antlers", "manure"], iucn="VU",
   shape=dict(bl=16, bw=7, bh=8, ll=12, lw=2, nl=6, nw=3, hl=6, hw=4, hh=4, sl=2, sw=3, sh=3, ew=1, eh=2, tl=2, horn="antler", hs=10, shaggy=1),
   pal=dict(body="7A6A58", belly="E6DED0", neck="DCD3C3", horn="D8C8A8", snout="3A3028"))
sp("moose", 3, "Moose", "Alces alces", "Cervidae · Artiodactyla · Mammalia", "quad", "land", "neutral",
   "taiga;old_growth_pine_taiga;old_growth_spruce_taiga", (1, 2), 4, (1.3, 2.0), 3.0, 30, 0.28, 6,
   food=[P + "willow_bark", M + "sweet_berries"], drops=[(P + "game_meat", 2, 4), (M + "leather", 1, 3)], traits=["manure"],
   shape=dict(bl=20, bw=9, bh=11, ll=16, lw=3, nl=4, nw=4, hl=9, hw=5, hh=5, sl=3, sw=4, sh=4, ew=2, eh=3, tl=1, horn="palmate", hump=1),
   pal=dict(body="3B2C22", leg="8A7B68", horn="C8B898", snout="2A2018"))
sp("brown_bear", 3, "Brown Bear", "Ursus arctos", "Ursidae · Carnivora · Mammalia", "quad", "land", "neutral",
   "taiga;old_growth_pine_taiga;old_growth_spruce_taiga;grove", (1, 1), 3, (1.4, 1.4), 2.5, 34, 0.25, 7,
   food=[M + "sweet_berries", M + "salmon", M + "honey_bottle"], drops=[(P + "tallow", 1, 2), (M + "leather", 1, 2)],
   traits=["honey"], shape=dict(bl=18, bw=11, bh=11, ll=8, lw=4, nl=1, hl=7, hw=7, hh=6, sl=3, sw=4, sh=3, ew=2, eh=2, tl=1, hump=1),
   pal=dict(body="5A3D24", belly="4A3220", snout="8A6A4A"))
sp("yak", 3, "Yak", "Bos grunniens", "Bovidae · Artiodactyla · Mammalia", "quad", "land", "passive",
   "grove;snowy_slopes;frozen_peaks;jagged_peaks;stony_peaks", (2, 5), 8, (1.3, 1.5), 3.0, 24, 0.22,
   food=[M + "wheat"], drops=[(M + "beef", 1, 3), (M + "leather", 0, 2)],
   traits=["milk", "shear:" + P + "yak_wool", "manure"], iucn="VU",
   shape=dict(bl=18, bw=10, bh=11, ll=9, lw=3, hl=7, hw=6, hh=6, sl=2, sw=4, sh=3, ew=2, eh=1, tl=5, tw=2, horn="horns", shaggy=1, hump=1),
   pal=dict(body="2E2620", belly="2A221C", horn="D8D0C0", snout="3A3028", tail="2A221C"))
sp("alpaca", 3, "Alpaca", "Vicugna pacos", "Camelidae · Artiodactyla · Mammalia", "quad", "land", "passive",
   "windswept_hills;windswept_gravelly_hills;stony_peaks", (3, 6), 8, (0.9, 1.6), 1.5, 16, 0.25,
   food=[M + "wheat", P + "alfalfa"], drops=[(P + "alpaca_wool", 1, 2)], traits=["shear:" + P + "alpaca_wool", "manure"],
   iucn=None, shape=dict(bl=13, bw=7, bh=8, ll=10, lw=3, nl=10, nw=4, hl=5, hw=4, hh=4, sl=2, sw=3, sh=3, ew=1, eh=3, tl=2, tw=2),
   pal=dict(body="E8DCC6", belly="F0E6D4", snout="8A7058"))
sp("alpine_marmot", 3, "Alpine Marmot", "Marmota marmota", "Sciuridae · Rodentia · Mammalia", "quad", "land", "passive",
   "meadow;windswept_hills", (2, 4), 6, (0.5, 0.5), 0.5, 8, 0.25,
   food=[M + "wheat_seeds", M + "dandelion"], drops=[(P + "tallow", 0, 1)],
   shape=dict(bl=10, bw=7, bh=6, ll=3, lw=2, hl=4, hw=5, hh=4, sl=1, sw=3, sh=2, ew=1, eh=1, tl=5, tw=2),
   pal=dict(body="7A6448", belly="A8946E", snout="3A2A1A", tail="4A3A2A"))
sp("stoat", 3, "Stoat", "Mustela erminea", "Mustelidae · Carnivora · Mammalia", "quad", "land", "passive",
   "snowy_plains;snowy_taiga;taiga", (1, 1), 4, (0.4, 0.3), 0.3, 6, 0.33,
   food=[M + "chicken", M + "rabbit"], drops=[(M + "rabbit_hide", 0, 1)], traits=["winter_coat"],
   shape=dict(bl=11, bw=3, bh=3, ll=3, lw=1, nl=1, nw=2, hl=3, hw=3, hh=3, sl=1, sw=2, sh=2, ew=1, eh=1, tl=5, tw=1, tiptail=1),
   pal=dict(body="8A5A30", belly="F2EAD8", tip="1A1A1A", winter="F4F4F0"))
sp("eurasian_lynx", 3, "Eurasian Lynx", "Lynx lynx", "Felidae · Carnivora · Mammalia", "quad", "land", "neutral",
   "taiga;grove;old_growth_spruce_taiga", (1, 1), 3, (0.7, 0.8), 1.1, 18, 0.3, 4,
   food=[M + "rabbit"], traits=["hunts_rabbits"],
   shape=dict(bl=14, bw=6, bh=7, ll=10, lw=2, hl=5, hw=5, hh=5, sl=2, sw=3, sh=2, ew=1, eh=3, tl=2, tufts=1),
   pal=dict(body="B08A5A", belly="E6D6BC", spot="6A4A2A", ear="2A2018"), pattern="spots")

# ---------------- stage 4: sea and coast ----------------
sp("ringed_seal", 4, "Ringed Seal", "Pusa hispida", "Phocidae · Carnivora · Mammalia", "pinniped", "amphibious", "passive",
   "frozen_ocean;deep_frozen_ocean;snowy_beach;frozen_river", (1, 3), 6, (0.9, 0.6), 1.4, 14, 0.2,
   food=FISH, drops=[(P + "blubber", 1, 2), (M + "leather", 0, 1)],
   shape=dict(bl=14, bw=6, bh=5, hl=5, hw=5, hh=4), pal=dict(body="7A7A72", belly="A8A8A0", ring="B8B8B0"), pattern="rings")
sp("walrus", 4, "Walrus", "Odobenus rosmarus", "Odobenidae · Carnivora · Mammalia", "pinniped", "amphibious", "neutral",
   "frozen_ocean;cold_ocean;stony_shore;snowy_beach", (2, 4), 4, (1.6, 1.0), 3.2, 30, 0.15, 5,
   food=FISH, drops=[(P + "blubber", 2, 3), (P + "ivory", 0, 2)], iucn="VU",
   shape=dict(bl=22, bw=12, bh=10, hl=7, hw=9, hh=8, tusks=1), pal=dict(body="A06A4A", belly="B07A5A", tusk="F0E8D8", snout="C8987A"))
sp("sperm_whale", 4, "Sperm Whale", "Physeter macrocephalus", "Physeteridae · Cetacea · Mammalia", "whale", "water", "passive",
   "deep_ocean;deep_cold_ocean;deep_lukewarm_ocean", (1, 2), 1, (3.0, 2.4), 16.0, 200, 0.9,
   drops=[(P + "spermaceti", 4, 8), (P + "blubber", 6, 10)], traits=["invulnerable", "strand"], iucn="VU", scale=2.5,
   shape=dict(), pal=dict(body="4A4A50", belly="5A5A62", mouth="E8E4DC"), pattern="wrinkles")
sp("greenland_shark", 4, "Greenland Shark", "Somniosus microcephalus", "Somniosidae · Squaliformes · Chondrichthyes",
   "fish", "water", "neutral", "cold_ocean;deep_cold_ocean;frozen_ocean;deep_frozen_ocean", (1, 1), 2, (1.4, 0.9), 5.0, 40,
   0.5, 5, drops=[(P + "shark_liver", 1, 1), (P + "greenland_shark_meat", 1, 3)], iucn="VU", scale=1.3,
   shape=dict(bl=30, bw=7, bh=7, shark=1), pal=dict(body="5A5A60", belly="6A6A70", fin="4A4A50", eye="C8D8D0"))
sp("atlantic_herring", 4, "Atlantic Herring", "Clupea harengus", "Clupeidae · Clupeiformes · Actinopterygii", "fish",
   "school", "passive", "ocean;cold_ocean;deep_cold_ocean;deep_ocean", (8, 12), 10, (0.4, 0.3), 0.3, 3, 0.9,
   drops=[(P + "herring", 1, 1)], traits=["bucket"], shape=dict(bl=8, bw=2, bh=3),
   pal=dict(body="3A5A6A", belly="D8DEE4", fin="8A9AA0"))
sp("murex", 4, "Spiny Dye-murex", "Bolinus brandaris", "Muricidae · Neogastropoda · Gastropoda", "snail", "water",
   "passive", "warm_ocean;lukewarm_ocean;deep_lukewarm_ocean", (2, 4), 6, (0.3, 0.3), 0.08, 4, 0.05,
   drops=[(P + "murex_snail", 1, 1)], iucn="NE", shape=dict(),
   pal=dict(shell="D8C8A8", spine="B89878", foot="E8D0B8"))
sp("cuttlefish", 4, "Common Cuttlefish", "Sepia officinalis", "Sepiidae · Sepiida · Cephalopoda", "cephalopod", "water",
   "passive", "lukewarm_ocean;deep_lukewarm_ocean;warm_ocean", (2, 3), 6, (0.6, 0.4), 0.45, 8, 0.6,
   drops=[(P + "sepia_ink", 1, 1), (P + "cuttlebone", 0, 1)], traits=["bucket"], iucn="LC",
   shape=dict(), pal=dict(body="8A6A5A", belly="D8C8B8", stripe="E0D0C0", arm="A08070"), pattern="cuttle")
sp("horseshoe_crab", 4, "Atlantic Horseshoe Crab", "Limulus polyphemus", "Limulidae · Xiphosura · Merostomata", "horseshoe",
   "amphibious", "passive", "beach;warm_ocean;lukewarm_ocean", (2, 5), 6, (0.6, 0.3), 0.6, 10, 0.12,
   traits=["blood"], iucn="VU", shape=dict(), pal=dict(shell="6A5A3A", tail="4A3A28", belly="8A7A5A"))
sp("green_crab", 4, "European Green Crab", "Carcinus maenas", "Carcinidae · Decapoda · Malacostraca", "crab",
   "amphibious", "passive", "beach;stony_shore", (2, 4), 6, (0.4, 0.3), 0.08, 6, 0.2,
   food=FISH, drops=[(P + "crab_shell", 1, 1)], shape=dict(), pal=dict(shell="4A6A2A", claw="6A7A3A", leg="5A6A30", eye="1A1A1A"))
sp("crystal_jelly", 4, "Crystal Jelly", "Aequorea victoria", "Aequoreidae · Leptothecata · Hydrozoa", "jelly", "water",
   "passive", "cold_ocean;lukewarm_ocean;ocean", (2, 4), 4, (0.5, 0.6), 0.15, 4, 0.1,
   drops=[(P + "aequorea_tissue", 1, 1)], traits=["bucket", "sting", "glow"], iucn="NE",
   shape=dict(), pal=dict(body="C8E0F0", ring="70F0A0", tentacle="E0F0F8"))

# Small animals that live as blocks and items (stage 1), and the pearl oyster (stage 4): only in the compendium.
SMALL = {
    "cochineal": dict(stage=1, name="Cochineal", sci="Dactylopius coccus", family="Dactylopiidae · Hemiptera · Insecta",
                      biomes=["desert", "badlands", "eroded_badlands"], blocks=[P + "cochineal_colony"], items=[P + "cochineal"], length=0.005, iucn=None),
    "lac_insect": dict(stage=1, name="Lac Insect", sci="Kerria lacca", family="Kerriidae · Hemiptera · Insecta",
                       biomes=["jungle", "bamboo_jungle", "sparse_jungle"], blocks=[P + "lac_colony"], items=[P + "sticklac"], length=0.005, iucn=None),
    "silkworm": dict(stage=1, name="Silkworm", sci="Bombyx mori", family="Bombycidae · Lepidoptera · Insecta",
                     biomes=["plains", "sunflower_plains"], blocks=[P + "sericulture_tray"], items=[P + "silkworm"], length=0.07, iucn=None),
    "earthworm": dict(stage=1, name="Common Earthworm", sci="Lumbricus terrestris", family="Lumbricidae · Opisthopora · Clitellata",
                      biomes=["plains", "forest", "flower_forest", "birch_forest", "meadow"], blocks=[P + "worm_bin"], items=[P + "earthworm"], length=0.2, iucn="LC"),
    "medicinal_leech": dict(stage=1, name="Medicinal Leech", sci="Hirudo medicinalis", family="Hirudinidae · Arhynchobdellida · Clitellata",
                            biomes=["swamp", "mangrove_swamp"], blocks=[], items=[P + "leech"], length=0.12, iucn="NT"),
    "pearl_oyster": dict(stage=4, name="Black-lip Pearl Oyster", sci="Pinctada margaritifera", family="Pteriidae · Ostreida · Bivalvia",
                         biomes=["warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean"], blocks=[P + "pearl_oyster"], items=[P + "pearl"], length=0.2, iucn="DD"),
}

# Vanilla animals in the Animal Compendium: id -> (scientific, family · order · class, length m, IUCN, products).
VANILLA = {
    "cow": ("Bos taurus", "Bovidae · Artiodactyla · Mammalia", 2.5, None, [M + "beef", M + "leather", M + "milk_bucket"]),
    "sheep": ("Ovis aries", "Bovidae · Artiodactyla · Mammalia", 1.5, None, [M + "white_wool", M + "mutton"]),
    "pig": ("Sus domesticus", "Suidae · Artiodactyla · Mammalia", 1.6, None, [M + "porkchop"]),
    "chicken": ("Gallus gallus domesticus", "Phasianidae · Galliformes · Aves", 0.6, None, [M + "egg", M + "feather", M + "chicken"]),
    "rabbit": ("Oryctolagus cuniculus", "Leporidae · Lagomorpha · Mammalia", 0.45, "EN", [M + "rabbit", M + "rabbit_hide", M + "rabbit_foot"]),
    "horse": ("Equus ferus caballus", "Equidae · Perissodactyla · Mammalia", 2.4, None, [M + "leather"]),
    "donkey": ("Equus asinus", "Equidae · Perissodactyla · Mammalia", 2.0, None, [M + "leather"]),
    "llama": ("Lama glama", "Camelidae · Artiodactyla · Mammalia", 2.0, None, [M + "leather"]),
    "camel": ("Camelus dromedarius", "Camelidae · Artiodactyla · Mammalia", 3.0, None, []),
    "goat": ("Oreamnos americanus", "Bovidae · Artiodactyla · Mammalia", 1.6, "LC", [M + "goat_horn", M + "milk_bucket"]),
    "wolf": ("Canis lupus", "Canidae · Carnivora · Mammalia", 1.4, "LC", []),
    "fox": ("Vulpes vulpes", "Canidae · Carnivora · Mammalia", 0.8, "LC", []),
    "cat": ("Felis catus", "Felidae · Carnivora · Mammalia", 0.5, None, [M + "string"]),
    "ocelot": ("Leopardus pardalis", "Felidae · Carnivora · Mammalia", 1.0, "LC", []),
    "panda": ("Ailuropoda melanoleuca", "Ursidae · Carnivora · Mammalia", 1.6, "VU", [M + "bamboo"]),
    "polar_bear": ("Ursus maritimus", "Ursidae · Carnivora · Mammalia", 2.5, "VU", [M + "cod", M + "salmon"]),
    "parrot": ("Ara macao", "Psittacidae · Psittaciformes · Aves", 0.85, "LC", [M + "feather"]),
    "bee": ("Apis mellifera", "Apidae · Hymenoptera · Insecta", 0.015, "DD", [M + "honeycomb", M + "honey_bottle"]),
    "frog": ("Rana temporaria", "Ranidae · Anura · Amphibia", 0.09, "LC", [M + "slime_ball"]),
    "axolotl": ("Ambystoma mexicanum", "Ambystomatidae · Caudata · Amphibia", 0.25, "CR", []),
    "turtle": ("Chelonia mydas", "Cheloniidae · Testudines · Reptilia", 1.1, "EN", [M + "turtle_scute", M + "seagrass"]),
    "armadillo": ("Dasypus novemcinctus", "Dasypodidae · Cingulata · Mammalia", 0.75, "LC", [M + "armadillo_scute"]),
    "bat": ("Myotis myotis", "Vespertilionidae · Chiroptera · Mammalia", 0.08, "LC", [P + "guano"]),
    "dolphin": ("Tursiops truncatus", "Delphinidae · Cetacea · Mammalia", 3.0, "LC", []),
    "squid": ("Loligo vulgaris", "Loliginidae · Myopsida · Cephalopoda", 0.4, "DD", [M + "ink_sac"]),
    "cod": ("Gadus morhua", "Gadidae · Gadiformes · Actinopterygii", 1.0, "VU", [M + "cod"]),
    "salmon": ("Salmo salar", "Salmonidae · Salmoniformes · Actinopterygii", 0.9, "LC", [M + "salmon"]),
    "pufferfish": ("Takifugu rubripes", "Tetraodontidae · Tetraodontiformes · Actinopterygii", 0.7, "NT", [M + "pufferfish"]),
    "tropical_fish": ("Amphiprion ocellaris", "Pomacentridae · Perciformes · Actinopterygii", 0.11, "LC", [M + "tropical_fish"]),
    "sea_pickle": ("Pyrosoma atlanticum", "Pyrosomatidae · Pyrosomida · Thaliacea", 0.6, None, [M + "lime_dye"]),
}
VANILLA_BIOMES = {
    "cow": "plains;sunflower_plains;meadow;forest", "sheep": "plains;sunflower_plains;meadow;windswept_hills",
    "pig": "plains;forest;birch_forest", "chicken": "plains;forest;jungle", "rabbit": "desert;flower_forest;snowy_plains;taiga;meadow",
    "horse": "plains;savanna", "donkey": "plains;savanna;meadow", "llama": "savanna;windswept_hills",
    "camel": "desert", "goat": "snowy_slopes;jagged_peaks;frozen_peaks", "wolf": "forest;taiga;grove;snowy_taiga",
    "fox": "taiga;snowy_taiga;old_growth_spruce_taiga", "cat": "plains;desert;savanna;taiga;snowy_plains", "ocelot": "jungle;bamboo_jungle",
    "panda": "bamboo_jungle;jungle", "polar_bear": "snowy_plains;ice_spikes;frozen_ocean", "parrot": "jungle;sparse_jungle",
    "bee": "plains;sunflower_plains;flower_forest;meadow;cherry_grove", "frog": "swamp;mangrove_swamp",
    "axolotl": "lush_caves", "turtle": "beach", "armadillo": "savanna;badlands;wooded_badlands", "bat": "dripstone_caves;lush_caves",
    "dolphin": "ocean;warm_ocean;lukewarm_ocean", "squid": "ocean;cold_ocean;deep_ocean", "cod": "ocean;cold_ocean;lukewarm_ocean",
    "salmon": "river;cold_ocean;frozen_ocean", "pufferfish": "warm_ocean;lukewarm_ocean", "tropical_fish": "warm_ocean;lush_caves",
    "sea_pickle": "warm_ocean",
}
VANILLA_BLOCKS = {"sea_pickle": [M + "sea_pickle"]}
