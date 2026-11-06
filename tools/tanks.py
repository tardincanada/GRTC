"""Tanks and pipes (3.38.0): the multiblock tank, Fluid Pipe and Fluid Pump. Assets, recipes, strings and the Field
Guide paragraphs (end of the Containers chapter, 10 languages).

Java: com.periodicelements.fluid (FluidContent registers the blocks, TankStructure measures a tank, FluidNet carries
fluid along pipes; MachineBlockEntity sends outputs down pipes and draws inputs from them). generate.py calls
generate_tanks, merges translations(code); machines.py adds SHAPED to RECIPES; guidebook.py appends GUIDE[code] and
turns "=grid <id>" paragraphs into recipe grids.
"""
import json
import random

from PIL import Image

BLOCKS = {
    "tank_wall": ("Tank Plate", "Riveted iron plate for the shell of a tank: build a closed box round an empty inside"),
    "tank_controller": ("Tank Controller", "One in the shell of every tank: it holds what the tank holds, 16,000 units for each block inside"),
    "tank_valve": ("Tank Valve", "Set it in a tank's shell: pipes, hoppers and you fill and empty the tank through it"),
    "tank_gauge": ("Tank Gauge", "Optional dial for a tank's shell, or against a Storage Tank: shows how full it is, in percent"),
    "tank_compressor": ("Tank Compressor", "One in a tank's shell compresses gas: 50,000 units of gas for each block inside"),
    "fluid_pipe": ("Fluid Pipe", "Joins machines to tanks: a machine sends its liquids and gases into the tanks it reaches and draws what it needs"),
    "fluid_pump": ("Fluid Pump", "Moves 250 units a tick from the tanks behind it to the tanks in front; redstone stops it"),
}
# Which research unlocks each block (also in data/research.csv).
UNLOCKS = {b: "gas_mains" for b in BLOCKS}
UNLOCKS["tank_compressor"] = "gas_compression"

SHAPED = {
    "tank_wall": {"pattern": ["III", "ILI", "III"], "count": 8,
                  "key": {"I": "minecraft:iron_ingot", "L": "periodic_elements:lead"}},
    "tank_controller": {"pattern": ["WCW", "GTG", "WCW"],
                        "key": {"W": "periodic_elements:tank_wall", "C": "minecraft:copper_ingot", "G": "minecraft:glass",
                                "T": "periodic_elements:storage_tank"}},
    "tank_valve": {"pattern": ["WPW"], "key": {"W": "periodic_elements:tank_wall", "P": "periodic_elements:fluid_pipe"}},
    "tank_gauge": {"pattern": ["G", "K", "W"],
                   "key": {"G": "minecraft:glass_pane", "K": "minecraft:clock", "W": "periodic_elements:tank_wall"}},
    "tank_compressor": {"pattern": ["SCS", "SWS"],
                        "key": {"S": "periodic_elements:steel_ingot", "C": "periodic_elements:compressor",
                                "W": "periodic_elements:tank_wall"}},
    "fluid_pipe": {"pattern": ["ILI"], "count": 6, "key": {"I": "minecraft:iron_ingot", "L": "periodic_elements:lead"}},
    "fluid_pump": {"pattern": ["IPI", "F F", "III"],
                   "key": {"I": "minecraft:iron_ingot", "P": "minecraft:piston", "F": "periodic_elements:fluid_pipe"}},
}

LANG = {
    "gui.periodic_elements.big_tank.holds": "Tank %s: %s of %s units of %s (%s%%)",
    "gui.periodic_elements.big_tank.empty": "Tank %1$s, %2$s blocks inside: empty; holds %3$s units of liquid or gas",
    "gui.periodic_elements.big_tank.empty.compressed": "Tank %1$s, %2$s blocks inside, with a compressor: empty; holds %3$s units of liquid or %4$s of gas",
    "gui.periodic_elements.big_tank.open": "Tank: no closed, empty inside of at most %s blocks a side next to the controller (a gap in the shell?)",
    "gui.periodic_elements.big_tank.missing": "Tank: %s at %s %s %s is not a tank block",
    "gui.periodic_elements.big_tank.not_box": "Tank: the inside must be an empty box with nothing in it",
    "gui.periodic_elements.big_tank.two_controllers": "Tank: a tank has only one Tank Controller",
    "gui.periodic_elements.big_tank.broken": "%s; it still holds %s units of %s",
    "gui.periodic_elements.big_tank.no_controller": "Not part of a tank with a Tank Controller",
    "gui.periodic_elements.big_tank.gauge_alone": "This gauge reads nothing: set it in a tank's shell or against a Storage Tank",
    "gui.periodic_elements.big_tank.vent_empty": "The tank is empty: nothing to release",
    "gui.periodic_elements.big_tank.vent_liquid": "%s is a liquid: only gases can be released into the air",
    "gui.periodic_elements.big_tank.vented": "Released %s units of %s into the air",
}

RESEARCH = ("Gas Mains", "Winsor and Clegg lit London with coal gas sent through iron mains from gasholders (1812): Fluid "
            "Pipes join machines to tanks, and Tank Plates build a tank as big as you like")

# ---- Textures. ----


def _noise(base, var, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = rnd.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def _rect(img, x0, y0, x1, y1, color):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(color) + (255,)


IRON = (122, 126, 134)
EDGE = (84, 88, 96)
RIVET = (196, 200, 206)


def _plate(seed):
    img = _noise(IRON, 6, seed)
    _rect(img, 0, 0, 15, 0, EDGE)
    _rect(img, 0, 15, 15, 15, EDGE)
    _rect(img, 0, 0, 0, 15, EDGE)
    _rect(img, 15, 0, 15, 15, EDGE)
    for x in (2, 7, 13):
        _rect(img, x, 2, x, 2, RIVET)
        _rect(img, x, 13, x, 13, RIVET)
    for y in (7,):
        _rect(img, 2, y, 2, y, RIVET)
        _rect(img, 13, y, 13, y, RIVET)
    return img


def textures():
    out = {"tank_wall": _plate(3801)}
    ctrl = _plate(3802)
    _rect(ctrl, 3, 3, 6, 12, (40, 44, 52))          # level glass
    _rect(ctrl, 4, 7, 5, 11, (90, 170, 220))         # liquid in it
    _rect(ctrl, 4, 4, 5, 6, (150, 190, 210))
    for x, y in ((10, 6), (9, 7), (11, 7), (10, 8), (10, 7)):  # hand wheel
        _rect(ctrl, x, y, x, y, (190, 50, 40))
    for x, y in ((9, 5), (11, 5), (8, 7), (12, 7), (9, 9), (11, 9)):
        _rect(ctrl, x, y, x, y, (150, 36, 30))
    _rect(ctrl, 8, 11, 12, 12, (210, 180, 60))       # brass name plate
    out["tank_controller"] = ctrl
    valve = _plate(3803)
    _rect(valve, 4, 4, 11, 11, (96, 100, 108))       # flange
    _rect(valve, 5, 5, 10, 10, (60, 62, 70))
    _rect(valve, 6, 6, 9, 9, (24, 24, 28))           # the bore
    for x, y in ((4, 4), (11, 4), (4, 11), (11, 11)):
        _rect(valve, x, y, x, y, RIVET)
    out["tank_valve"] = valve
    gauge = _plate(3804)
    _rect(gauge, 1, 2, 14, 13, (52, 56, 62))         # bezel
    _rect(gauge, 2, 3, 13, 12, (14, 26, 16))         # dark glass the renderer writes on
    out["tank_gauge_front"] = gauge
    comp = _noise((150, 156, 166), 5, 3805)          # steel
    _rect(comp, 0, 0, 15, 0, (100, 104, 114))
    _rect(comp, 0, 15, 15, 15, (100, 104, 114))
    for y in range(3, 13, 2):                         # cooling fins
        _rect(comp, 2, y, 13, y, (110, 114, 124))
    _rect(comp, 6, 2, 9, 13, (70, 74, 84))           # cylinder
    _rect(comp, 7, 3, 8, 12, (176, 182, 192))        # piston rod
    _rect(comp, 12, 1, 13, 2, (220, 60, 50))         # red pressure mark
    out["tank_compressor"] = comp
    pipe = _noise((92, 96, 104), 5, 3806)
    _rect(pipe, 0, 0, 15, 1, (70, 74, 82))
    _rect(pipe, 0, 14, 15, 15, (70, 74, 82))
    _rect(pipe, 0, 5, 15, 6, (130, 134, 142))        # highlight
    _rect(pipe, 7, 0, 8, 15, (110, 112, 118))        # lead joint
    out["fluid_pipe"] = pipe
    end = _noise((92, 96, 104), 5, 3807)
    _rect(end, 0, 0, 15, 15, (70, 74, 82))
    _rect(end, 3, 3, 12, 12, (110, 114, 122))
    _rect(end, 5, 5, 10, 10, (30, 30, 34))
    out["fluid_pipe_end"] = end
    side = _plate(3808)
    _rect(side, 4, 7, 11, 8, (220, 200, 70))         # yellow arrow pointing up = out
    _rect(side, 6, 5, 9, 6, (220, 200, 70))
    _rect(side, 7, 4, 8, 4, (220, 200, 70))
    out["fluid_pump_side"] = side
    front = _plate(3809)
    _rect(front, 4, 4, 11, 11, (96, 100, 108))
    _rect(front, 5, 5, 10, 10, (30, 30, 34))
    _rect(front, 7, 7, 8, 8, (220, 200, 70))         # outlet, marked yellow
    out["fluid_pump_front"] = front
    back = _plate(3810)
    _rect(back, 4, 4, 11, 11, (96, 100, 108))
    _rect(back, 5, 5, 10, 10, (30, 30, 34))
    out["fluid_pump_back"] = back
    return out


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _cuboid(f, t, tex, ends=None):
    faces = {}
    for d in ("north", "south", "east", "west", "up", "down"):
        faces[d] = {"texture": "#" + (ends if ends and d in ("north", "south") else tex)}
    return {"from": f, "to": t, "faces": faces}


def _pipe_models(assets, m):
    core = {"parent": "minecraft:block/block", "textures": {"particle": m("fluid_pipe"), "pipe": m("fluid_pipe")},
            "elements": [_cuboid([5, 5, 5], [11, 11, 11], "pipe")]}
    _dump(assets / "models" / "block" / "fluid_pipe_core.json", core)
    arms = {"north": ([5, 5, 0], [11, 11, 5]), "south": ([5, 5, 11], [11, 11, 16]), "west": ([0, 5, 5], [5, 11, 11]),
            "east": ([11, 5, 5], [16, 11, 11]), "down": ([5, 0, 5], [11, 5, 11]), "up": ([5, 11, 5], [11, 16, 11])}
    for d, (f, t) in arms.items():
        el = {"from": f, "to": t, "faces": {k: {"texture": "#pipe"} for k in ("north", "south", "east", "west", "up", "down")}}
        el["faces"][d] = {"texture": "#end"}   # the flange where it meets the next block
        opp = {"north": "south", "south": "north", "east": "west", "west": "east", "up": "down", "down": "up"}[d]
        del el["faces"][opp]                    # hidden inside the core
        _dump(assets / "models" / "block" / f"fluid_pipe_{d}.json",
              {"parent": "minecraft:block/block", "textures": {"particle": m("fluid_pipe"), "pipe": m("fluid_pipe"),
                                                               "end": m("fluid_pipe_end")}, "elements": [el]})
    parts = [{"apply": {"model": m("fluid_pipe_core")}}]
    for d in arms:
        parts.append({"when": {d: "true"}, "apply": {"model": m(f"fluid_pipe_{d}")}})
    _dump(assets / "blockstates" / "fluid_pipe.json", {"multipart": parts})
    item = {"parent": "minecraft:block/block", "textures": {"particle": m("fluid_pipe"), "pipe": m("fluid_pipe"),
                                                           "end": m("fluid_pipe_end")},
            "elements": [_cuboid([5, 5, 0], [11, 11, 16], "pipe", ends="end")],
            "display": {"gui": {"rotation": [30, 45, 0], "scale": [0.8, 0.8, 0.8]}}}
    _dump(assets / "models" / "item" / "fluid_pipe.json", item)


def generate_tanks(assets, data_root, lang, mod_id):
    data_dir = data_root / mod_id
    m = lambda n: f"{mod_id}:block/{n}"
    for tid, img in textures().items():
        img.save(assets / "textures" / "block" / f"{tid}.png")
    for bid in ("tank_wall", "tank_controller", "tank_valve", "tank_compressor"):
        _dump(assets / "models" / "block" / f"{bid}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": m(bid)}})
        _dump(assets / "blockstates" / f"{bid}.json", {"variants": {"": {"model": m(bid)}}})
    _dump(assets / "models" / "block" / "tank_gauge.json", {"parent": "minecraft:block/orientable", "textures": {
        "front": m("tank_gauge_front"), "side": m("tank_wall"), "top": m("tank_wall")}})
    _dump(assets / "blockstates" / "tank_gauge.json", {"variants": {
        f"facing={f}": dict({"model": m("tank_gauge")}, **({"y": y} if y else {}))
        for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}})
    _dump(assets / "models" / "block" / "fluid_pump.json", {"parent": "minecraft:block/cube", "textures": {
        "particle": m("fluid_pump_side"), "north": m("fluid_pump_front"), "south": m("fluid_pump_back"),
        "east": m("fluid_pump_side"), "west": m("fluid_pump_side"), "up": m("fluid_pump_side"), "down": m("fluid_pump_side")}})
    rot = {"north": {}, "south": {"y": 180}, "east": {"y": 90}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
    _dump(assets / "blockstates" / "fluid_pump.json", {"variants": {
        f"facing={f},powered={p}": dict({"model": m("fluid_pump")}, **r) for f, r in rot.items() for p in ("false", "true")}})
    _pipe_models(assets, m)
    for bid in BLOCKS:
        if bid != "fluid_pipe":
            _dump(assets / "models" / "item" / f"{bid}.json", {"parent": m(bid)})
        _dump(data_dir / "loot_table" / "blocks" / f"{bid}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": f"{mod_id}:{bid}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    path = data_root / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
    values += [f"{mod_id}:{b}" for b in BLOCKS if f"{mod_id}:{b}" not in values]
    _dump(path, {"replace": False, "values": values})
    for bid, (name, desc) in BLOCKS.items():
        lang[f"block.{mod_id}.{bid}"] = name
        lang[f"block.{mod_id}.{bid}.desc"] = desc
    lang.update(LANG)


# ---- Field Guide: paragraphs at the end of the Containers chapter. "=grid <id>" becomes the recipe grid. ----

GUIDE = {"en_us": [
    "@3 # Tanks",
    "@3 Gas Mains (level 3) unlocks a tank you build as big as you like. Make a closed box of Tank Plates, edges and "
    "corners too, round an empty inside from 1x1x1 to 14x14x14 blocks, and set one Tank Controller anywhere in its walls. "
    "Every block of the inside holds 16,000 units of one liquid or gas: a 3x3x3 inside holds 432,000. The controller "
    "measures the tank once a second; use it with an empty hand to read its size and contents, or what is wrong.",
    "@3 Tank Valves in the shell are its doors. Use a valve or the controller with a filled flask or cylinder to pour it "
    "in, or with an empty flask to draw one out; hoppers put containers in from the top or sides and take them from the "
    "bottom, and pipes join it. A comparator reads how full it is. A Tank Gauge in the shell, optional, shows the percent "
    "in use on its face; set against a Storage Tank, it reads that tank instead.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Gas Compression (level 4) unlocks the Tank Compressor. With one in the shell the tank holds 50,000 units of gas "
    "for each block inside (liquids still 16,000), and empty Steel Cylinders come out of it filled. Take the compressor "
    "away and the gas the tank can no longer hold escapes.",
    "@4 =grid tank_compressor",
    "@3 Every tank has a relief valve: sneak and use its controller, a valve or a Storage Tank with an empty hand and "
    "the gas in it goes into the air. Liquids cannot be let out. Breaking the controller loses what the tank held; a gap "
    "in the shell only stops the tank until you close it.",
    "@3 # Pipes and pumps",
    "@3 Fluid Pipes join machines to tanks: Storage Tanks, Tank Controllers and Tank Valves (a machine touching a tank "
    "works the same). A machine sends every liquid and gas it makes down its pipes into the tanks they reach, before it "
    "fills flasks, so it needs none; and when its input or reagent slot is empty it draws from them the liquids and gases "
    "its reactions use, a flask's worth at a time, with no flask. Pipes hold nothing; a run may be 1,024 pipes long.",
    "@3 A Fluid Pump moves 250 units a tick from the tanks behind it, directly or through pipes, to the tanks in front of "
    "it; it faces the way you look when you place it, and a redstone signal stops it.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]}

_G = {}
_G["pt_br"] = [
    "@3 # Tanques",
    "@3 Redes de Gás (nível 3) libera um tanque que você constrói do tamanho que quiser. Faça uma caixa fechada de Chapas "
    "de Tanque, arestas e cantos também, em volta de um interior vazio de 1x1x1 a 14x14x14 blocos, e coloque um "
    "Controlador de Tanque em qualquer ponto das paredes. Cada bloco do interior guarda 16.000 unidades de um líquido ou "
    "gás: um interior de 3x3x3 guarda 432.000. O controlador mede o tanque uma vez por segundo; use-o com a mão vazia "
    "para ver o tamanho e o conteúdo, ou o que está errado.",
    "@3 As Válvulas de Tanque na casca são as portas dele. Use uma válvula ou o controlador com um frasco ou cilindro "
    "cheio para despejá-lo, ou com um frasco vazio para retirar um; funis colocam recipientes por cima ou pelos lados e "
    "os tiram por baixo, e os tubos se ligam a ela. Um comparador lê quanto está cheio. Um Mostrador de Tanque na casca, "
    "opcional, mostra no seu visor o percentual usado; encostado num Tanque de Armazenamento, mostra o desse tanque.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Compressão de Gás (nível 4) libera o Compressor de Tanque. Com um na casca, o tanque guarda 50.000 unidades de "
    "gás por bloco do interior (líquidos continuam 16.000), e cilindros de aço vazios saem dele cheios. Tire o compressor "
    "e o gás que o tanque não consegue mais guardar escapa.",
    "@4 =grid tank_compressor",
    "@3 Todo tanque tem válvula de alívio: agache e use o controlador, uma válvula ou um Tanque de Armazenamento com a mão "
    "vazia, e o gás dele vai para o ar. Líquidos não podem ser liberados. Quebrar o controlador perde o que o tanque "
    "guardava; uma falha na casca só para o tanque até você fechá-la.",
    "@3 # Tubos e bombas",
    "@3 Os Tubos de Fluido ligam máquinas a tanques: Tanques de Armazenamento, Controladores e Válvulas de Tanque (uma "
    "máquina encostada num tanque funciona igual). A máquina manda todo líquido e gás que produz pelos tubos para os "
    "tanques que eles alcançam, antes de encher frascos, e por isso não precisa deles; e quando o espaço de entrada ou de "
    "reagente está vazio, ela tira deles os líquidos e gases que suas reações usam, um frasco por vez, sem frasco. Os "
    "tubos não guardam nada; uma linha pode ter até 1.024 tubos.",
    "@3 Uma Bomba de Fluido move 250 unidades por tique dos tanques atrás dela, direto ou por tubos, para os tanques à "
    "frente; ela aponta para onde você olha ao colocá-la, e um sinal de redstone a para.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["es_es"] = [
    "@3 # Tanques",
    "@3 Redes de Gas (nivel 3) desbloquea un tanque que construyes tan grande como quieras. Haz una caja cerrada de Chapas "
    "de Tanque, aristas y esquinas también, alrededor de un interior vacío de 1x1x1 a 14x14x14 bloques, y pon un "
    "Controlador de Tanque en cualquier punto de sus paredes. Cada bloque del interior guarda 16.000 unidades de un "
    "líquido o gas: un interior de 3x3x3 guarda 432.000. El controlador mide el tanque una vez por segundo; úsalo con la "
    "mano vacía para ver su tamaño y contenido, o lo que falla.",
    "@3 Las Válvulas de Tanque de la carcasa son sus puertas. Usa una válvula o el controlador con un frasco o cilindro "
    "lleno para verterlo, o con un frasco vacío para sacar uno; las tolvas meten recipientes por arriba o por los lados "
    "y los sacan por abajo, y las tuberías se conectan a ella. Un comparador lee cuánto está lleno. Un Indicador de "
    "Tanque en la carcasa, opcional, muestra en su esfera el porcentaje usado; junto a un Tanque de almacenamiento, "
    "muestra el de ese tanque.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Compresión de Gas (nivel 4) desbloquea el Compresor de Tanque. Con uno en la carcasa, el tanque guarda 50.000 "
    "unidades de gas por bloque del interior (los líquidos siguen en 16.000), y los cilindros de acero vacíos salen "
    "llenos. Quita el compresor y el gas que el tanque ya no puede guardar escapa.",
    "@4 =grid tank_compressor",
    "@3 Todo tanque tiene válvula de alivio: agáchate y usa su controlador, una válvula o un Tanque de almacenamiento con "
    "la mano vacía, y su gas sale al aire. Los líquidos no se pueden liberar. Romper el controlador pierde lo que "
    "guardaba el tanque; un hueco en la carcasa solo lo detiene hasta que lo cierres.",
    "@3 # Tuberías y bombas",
    "@3 Las Tuberías de Fluido unen máquinas con tanques: Tanques de almacenamiento, Controladores y Válvulas de Tanque "
    "(una máquina pegada a un tanque funciona igual). La máquina manda cada líquido y gas que produce por sus tuberías a "
    "los tanques que alcanzan, antes de llenar frascos, así que no los necesita; y cuando su ranura de entrada o de "
    "reactivo está vacía, saca de ellos los líquidos y gases que usan sus reacciones, un frasco cada vez, sin frasco. "
    "Las tuberías no guardan nada; un tramo puede tener hasta 1.024 tuberías.",
    "@3 Una Bomba de Fluido mueve 250 unidades por tick de los tanques que tiene detrás, directamente o por tuberías, a "
    "los que tiene delante; mira hacia donde miras al colocarla, y una señal de redstone la detiene.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["fr_fr"] = [
    "@3 # Réservoirs",
    "@3 Conduites de Gaz (niveau 3) débloque un réservoir aussi grand que vous voulez. Construisez une boîte fermée de "
    "Tôles de Réservoir, arêtes et coins compris, autour d'un intérieur vide de 1x1x1 à 14x14x14 blocs, et placez un "
    "Contrôleur de Réservoir n'importe où dans ses parois. Chaque bloc de l'intérieur contient 16 000 unités d'un liquide "
    "ou d'un gaz : un intérieur de 3x3x3 en contient 432 000. Le contrôleur mesure le réservoir une fois par seconde ; "
    "utilisez-le la main vide pour lire sa taille et son contenu, ou ce qui ne va pas.",
    "@3 Les Vannes de Réservoir de la coque sont ses portes. Utilisez une vanne ou le contrôleur avec une fiole ou une "
    "bouteille pleine pour la vider dedans, ou avec une fiole vide pour en remplir une ; les entonnoirs mettent les "
    "récipients par le haut ou les côtés et les reprennent par le bas, et les tuyaux s'y raccordent. Un comparateur lit "
    "le remplissage. Une Jauge de Réservoir dans la coque, facultative, affiche le pourcentage utilisé sur son cadran ; "
    "posée contre un Réservoir de stockage, elle lit celui-ci.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Compression des Gaz (niveau 4) débloque le Compresseur de Réservoir. Avec un dans la coque, le réservoir contient "
    "50 000 unités de gaz par bloc intérieur (les liquides restent à 16 000), et les bouteilles d'acier vides en "
    "ressortent pleines. Retirez le compresseur et le gaz que le réservoir ne peut plus contenir s'échappe.",
    "@4 =grid tank_compressor",
    "@3 Chaque réservoir a une soupape : accroupissez-vous et utilisez son contrôleur, une vanne ou un Réservoir de "
    "stockage la main vide, et son gaz part dans l'air. Les liquides ne peuvent pas être relâchés. Casser le contrôleur "
    "perd ce que le réservoir contenait ; un trou dans la coque l'arrête seulement jusqu'à ce que vous le bouchiez.",
    "@3 # Tuyaux et pompes",
    "@3 Les Tuyaux de Fluide relient les machines aux réservoirs : Réservoirs de stockage, Contrôleurs et Vannes de "
    "Réservoir (une machine collée à un réservoir fait de même). Une machine envoie chaque liquide et gaz qu'elle produit "
    "par ses tuyaux dans les réservoirs qu'ils atteignent, avant de remplir des fioles, et n'en a donc pas besoin ; et "
    "quand son emplacement d'entrée ou de réactif est vide, elle y puise les liquides et gaz que ses réactions utilisent, "
    "une fiole à la fois, sans fiole. Les tuyaux ne contiennent rien ; une ligne peut compter 1 024 tuyaux.",
    "@3 Une Pompe à Fluide déplace 250 unités par tick des réservoirs derrière elle, directement ou par des tuyaux, vers "
    "ceux de devant ; elle est tournée là où vous regardez en la posant, et un signal de redstone l'arrête.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["de_de"] = [
    "@3 # Tanks",
    "@3 Gasleitungen (Stufe 3) schaltet einen Tank frei, den du so groß baust, wie du willst. Baue einen geschlossenen "
    "Kasten aus Tankblechen, Kanten und Ecken auch, um einen leeren Innenraum von 1x1x1 bis 14x14x14 Blöcken, und setze "
    "irgendwo in die Wände eine Tanksteuerung. Jeder Block des Innenraums fasst 16.000 Einheiten einer Flüssigkeit oder "
    "eines Gases: ein Innenraum von 3x3x3 fasst 432.000. Die Steuerung misst den Tank einmal pro Sekunde; benutze sie mit "
    "leerer Hand, um Größe und Inhalt zu sehen, oder was nicht stimmt.",
    "@3 Tankventile in der Hülle sind seine Türen. Benutze ein Ventil oder die Steuerung mit einem vollen Kolben oder "
    "einer vollen Flasche, um sie einzufüllen, oder mit einem leeren Kolben, um einen zu füllen; Trichter geben Behälter "
    "von oben oder von der Seite hinein und nehmen sie unten heraus, und Rohre schließen daran an. Ein Komparator liest "
    "den Füllstand. Eine Tankanzeige in der Hülle, freiwillig, zeigt auf ihrem Zifferblatt den belegten Prozentsatz; an "
    "einen Lagertank gesetzt, zeigt sie dessen Stand.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Gasverdichtung (Stufe 4) schaltet den Tankverdichter frei. Mit einem in der Hülle fasst der Tank 50.000 Einheiten "
    "Gas pro Block Innenraum (Flüssigkeiten weiter 16.000), und leere Stahlflaschen kommen gefüllt heraus. Nimm den "
    "Verdichter weg, und das Gas, das der Tank nicht mehr fassen kann, entweicht.",
    "@4 =grid tank_compressor",
    "@3 Jeder Tank hat ein Überdruckventil: schleiche und benutze seine Steuerung, ein Ventil oder einen Lagertank mit "
    "leerer Hand, und sein Gas entweicht in die Luft. Flüssigkeiten lassen sich nicht ablassen. Wer die Steuerung "
    "abbaut, verliert den Inhalt; eine Lücke in der Hülle hält den Tank nur an, bis du sie schließt.",
    "@3 # Rohre und Pumpen",
    "@3 Fluidrohre verbinden Maschinen mit Tanks: Lagertanks, Tanksteuerungen und Tankventilen (eine Maschine, die einen "
    "Tank berührt, ebenso). Eine Maschine schickt jede Flüssigkeit und jedes Gas, das sie herstellt, durch ihre Rohre in "
    "die erreichten Tanks, bevor sie Kolben füllt, und braucht daher keine; und ist ihr Eingangs- oder Reagenzplatz leer, "
    "zieht sie daraus die Flüssigkeiten und Gase, die ihre Reaktionen brauchen, einen Kolben auf einmal, ohne Kolben. "
    "Rohre fassen nichts; eine Leitung darf 1.024 Rohre lang sein.",
    "@3 Eine Fluidpumpe bewegt 250 Einheiten pro Tick aus den Tanks hinter ihr, direkt oder durch Rohre, in die Tanks vor "
    "ihr; sie zeigt beim Setzen dorthin, wohin du schaust, und ein Redstone-Signal hält sie an.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["it_it"] = [
    "@3 # Serbatoi",
    "@3 Condotte del Gas (livello 3) sblocca un serbatoio grande quanto vuoi. Costruisci una scatola chiusa di Lamiere da "
    "Serbatoio, spigoli e angoli compresi, attorno a un interno vuoto da 1x1x1 a 14x14x14 blocchi, e metti un Controllore "
    "di Serbatoio in un punto qualsiasi delle pareti. Ogni blocco dell'interno contiene 16.000 unità di un liquido o di un "
    "gas: un interno di 3x3x3 ne contiene 432.000. Il controllore misura il serbatoio una volta al secondo; usalo a mano "
    "vuota per leggerne dimensioni e contenuto, o cosa non va.",
    "@3 Le Valvole da Serbatoio nel guscio sono le sue porte. Usa una valvola o il controllore con una fiaschetta o una "
    "bombola piena per versarla, o con una fiaschetta vuota per riempirne una; le tramogge inseriscono recipienti "
    "dall'alto o dai lati e li tolgono dal basso, e i tubi vi si collegano. Un comparatore legge quanto è pieno. Un "
    "Indicatore da Serbatoio nel guscio, facoltativo, mostra sul quadrante la percentuale usata; appoggiato a un "
    "Serbatoio di stoccaggio, legge quello.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Compressione dei Gas (livello 4) sblocca il Compressore da Serbatoio. Con uno nel guscio il serbatoio contiene "
    "50.000 unità di gas per blocco interno (i liquidi restano 16.000), e le bombole d'acciaio vuote escono piene. Togli "
    "il compressore e il gas che il serbatoio non può più contenere sfugge.",
    "@4 =grid tank_compressor",
    "@3 Ogni serbatoio ha una valvola di sfogo: accovacciati e usa il controllore, una valvola o un Serbatoio di "
    "stoccaggio a mano vuota, e il suo gas va nell'aria. I liquidi non si possono liberare. Rompere il controllore perde "
    "ciò che il serbatoio conteneva; un buco nel guscio ferma il serbatoio solo finché non lo chiudi.",
    "@3 # Tubi e pompe",
    "@3 I Tubi per Fluidi collegano le macchine ai serbatoi: Serbatoi di stoccaggio, Controllori e Valvole da Serbatoio "
    "(una macchina che tocca un serbatoio fa lo stesso). Una macchina manda ogni liquido e gas che produce lungo i tubi "
    "nei serbatoi che raggiungono, prima di riempire fiaschette, quindi non ne ha bisogno; e quando lo slot d'ingresso o "
    "del reagente è vuoto, ne preleva i liquidi e i gas che usano le sue reazioni, una fiaschetta alla volta, senza "
    "fiaschetta. I tubi non contengono nulla; una linea può avere fino a 1.024 tubi.",
    "@3 Una Pompa per Fluidi sposta 250 unità per tick dai serbatoi dietro di lei, direttamente o tramite tubi, a quelli "
    "davanti; guarda dove guardi tu quando la piazzi, e un segnale di redstone la ferma.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["nl_nl"] = [
    "@3 # Tanks",
    "@3 Gasleidingen (niveau 3) ontgrendelt een tank die je zo groot bouwt als je wilt. Maak een gesloten doos van "
    "Tankplaten, ribben en hoeken ook, rond een lege binnenruimte van 1x1x1 tot 14x14x14 blokken, en zet ergens in de "
    "wanden één Tankregelaar. Elk blok van de binnenruimte bevat 16.000 eenheden van één vloeistof of gas: een "
    "binnenruimte van 3x3x3 bevat er 432.000. De regelaar meet de tank eens per seconde; gebruik hem met lege hand om "
    "grootte en inhoud te zien, of wat er mis is.",
    "@3 Tankkranen in de wand zijn de deuren. Gebruik een kraan of de regelaar met een volle kolf of cilinder om die "
    "erin te gieten, of met een lege kolf om er een te vullen; trechters stoppen houders erin van boven of opzij en halen "
    "ze er onderaan uit, en buizen sluiten erop aan. Een comparator leest hoe vol hij is. Een Tankmeter in de wand, naar "
    "keuze, toont op zijn wijzerplaat het gebruikte percentage; tegen een Opslagtank gezet, leest hij die.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Gascompressie (niveau 4) ontgrendelt de Tankcompressor. Met een in de wand bevat de tank 50.000 eenheden gas per "
    "blok binnenruimte (vloeistoffen blijven 16.000), en lege stalen cilinders komen er gevuld uit. Haal de compressor "
    "weg en het gas dat de tank niet meer kan bevatten ontsnapt.",
    "@4 =grid tank_compressor",
    "@3 Elke tank heeft een overdrukventiel: sluip en gebruik zijn regelaar, een kraan of een Opslagtank met lege hand, "
    "en het gas gaat de lucht in. Vloeistoffen kunnen er niet uit. Wie de regelaar breekt, verliest de inhoud; een gat in "
    "de wand houdt de tank alleen stil tot je het dicht.",
    "@3 # Buizen en pompen",
    "@3 Vloeistofbuizen verbinden machines met tanks: Opslagtanks, Tankregelaars en Tankkranen (een machine die een tank "
    "raakt doet hetzelfde). Een machine stuurt elke vloeistof en elk gas dat ze maakt door haar buizen naar de tanks die "
    "ze bereiken, vóór ze kolven vult, dus ze heeft er geen nodig; en als haar invoer- of reagensvak leeg is, haalt ze "
    "eruit wat haar reacties gebruiken, een kolf tegelijk, zonder kolf. Buizen bevatten niets; een leiding mag 1.024 "
    "buizen lang zijn.",
    "@3 Een Vloeistofpomp verplaatst 250 eenheden per tick van de tanks achter haar, direct of via buizen, naar de tanks "
    "ervoor; ze kijkt de kant op waar jij kijkt als je haar plaatst, en een redstonesignaal stopt haar.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["ru_ru"] = [
    "@3 # Резервуары",
    "@3 Газовые магистрали (уровень 3) открывают резервуар любого размера. Постройте закрытую коробку из листов "
    "резервуара, включая рёбра и углы, вокруг пустой полости от 1x1x1 до 14x14x14 блоков и поставьте в стену один "
    "контроллер резервуара. Каждый блок полости вмещает 16 000 единиц одной жидкости или газа: полость 3x3x3 вмещает "
    "432 000. Контроллер измеряет резервуар раз в секунду; используйте его пустой рукой, чтобы узнать размер и "
    "содержимое или что не так.",
    "@3 Вентили резервуара в оболочке — его двери. Используйте вентиль или контроллер с полной колбой или баллоном, "
    "чтобы вылить их, или с пустой колбой, чтобы набрать; воронки кладут сосуды сверху или сбоку и забирают снизу, а "
    "трубы подключаются к вентилю. Компаратор показывает заполненность. Указатель резервуара в оболочке, по желанию, "
    "показывает на циферблате процент заполнения; приставленный к накопительному резервуару, показывает его.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Сжатие газов (уровень 4) открывает компрессор резервуара. С ним в оболочке резервуар вмещает 50 000 единиц газа "
    "на блок полости (жидкости по-прежнему 16 000), а пустые стальные баллоны выходят из него полными. Уберите "
    "компрессор, и газ, который резервуар больше не вмещает, уйдёт.",
    "@4 =grid tank_compressor",
    "@3 У каждого резервуара есть предохранительный клапан: присядьте и используйте его контроллер, вентиль или "
    "накопительный резервуар пустой рукой, и газ уйдёт в воздух. Жидкости выпустить нельзя. Сломанный контроллер "
    "теряет содержимое; дыра в оболочке лишь останавливает резервуар, пока вы её не закроете.",
    "@3 # Трубы и насосы",
    "@3 Трубы для жидкостей соединяют машины с резервуарами: накопительными резервуарами, контроллерами и вентилями "
    "(машина, касающаяся резервуара, работает так же). Машина отправляет каждую жидкость и газ, которые делает, по "
    "трубам в достижимые резервуары, прежде чем наполнять колбы, и поэтому они ей не нужны; а когда её слот входа или "
    "реагента пуст, она берёт из них жидкости и газы для своих реакций, по колбе за раз, без колбы. Трубы ничего не "
    "хранят; линия может быть длиной до 1 024 труб.",
    "@3 Насос для жидкостей перекачивает 250 единиц за тик из резервуаров позади него, напрямую или по трубам, в "
    "резервуары впереди; он смотрит туда, куда смотрите вы при установке, а сигнал редстоуна его останавливает.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["sv_se"] = [
    "@3 # Tankar",
    "@3 Gasledningar (nivå 3) låser upp en tank du bygger så stor du vill. Gör en sluten låda av tankplåtar, kanter och "
    "hörn också, runt ett tomt inre från 1x1x1 till 14x14x14 block, och sätt en tankstyrning var som helst i väggarna. "
    "Varje block av det inre rymmer 16 000 enheter av en vätska eller gas: ett inre på 3x3x3 rymmer 432 000. Styrningen "
    "mäter tanken en gång i sekunden; använd den med tom hand för att se storlek och innehåll, eller vad som är fel.",
    "@3 Tankventiler i skalet är dess dörrar. Använd en ventil eller styrningen med en full flaska eller cylinder för att "
    "hälla i den, eller med en tom flaska för att fylla en; trattar lägger in behållare uppifrån eller från sidan och tar "
    "ut dem nedtill, och rör ansluts till den. En komparator läser hur full den är. En tankmätare i skalet, valfri, visar "
    "använd procent på sin tavla; satt mot en lagringstank läser den den tanken.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Gaskomprimering (nivå 4) låser upp tankkompressorn. Med en i skalet rymmer tanken 50 000 enheter gas per block "
    "inre (vätskor fortfarande 16 000), och tomma stålcylindrar kommer ut fyllda. Ta bort kompressorn och den gas tanken "
    "inte längre rymmer pyser ut.",
    "@4 =grid tank_compressor",
    "@3 Varje tank har en säkerhetsventil: smyg och använd dess styrning, en ventil eller en lagringstank med tom hand, "
    "så går gasen ut i luften. Vätskor kan inte släppas ut. Bryter du styrningen förloras innehållet; ett hål i skalet "
    "stoppar bara tanken tills du täpper till det.",
    "@3 # Rör och pumpar",
    "@3 Vätskerör förbinder maskiner med tankar: lagringstankar, tankstyrningar och tankventiler (en maskin som rör vid en "
    "tank fungerar likadant). En maskin skickar varje vätska och gas den gör genom sina rör till tankarna de når, innan "
    "den fyller flaskor, så den behöver inga; och när dess inmatnings- eller reagensplats är tom tar den ur dem de vätskor "
    "och gaser dess reaktioner använder, en flaska i taget, utan flaska. Rör rymmer inget; en ledning får vara 1 024 rör "
    "lång.",
    "@3 En vätskepump flyttar 250 enheter per tick från tankarna bakom den, direkt eller genom rör, till tankarna framför; "
    "den pekar dit du tittar när du sätter den, och en redstonesignal stoppar den.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
_G["da_dk"] = [
    "@3 # Tanke",
    "@3 Gasledninger (niveau 3) låser op for en tank, du bygger så stor du vil. Lav en lukket kasse af tankplader, kanter "
    "og hjørner også, rundt om et tomt indre fra 1x1x1 til 14x14x14 blokke, og sæt én tankstyring et sted i væggene. Hver "
    "blok af det indre rummer 16.000 enheder af én væske eller gas: et indre på 3x3x3 rummer 432.000. Styringen måler "
    "tanken én gang i sekundet; brug den med tom hånd for at se størrelse og indhold, eller hvad der er galt.",
    "@3 Tankventiler i skallen er dens døre. Brug en ventil eller styringen med en fuld kolbe eller cylinder for at hælde "
    "den i, eller med en tom kolbe for at fylde en; tragte lægger beholdere i fra oven eller siden og tager dem ud "
    "forneden, og rør tilsluttes den. En komparator læser, hvor fuld den er. En tankmåler i skallen, valgfri, viser den "
    "brugte procent på sin skive; sat mod en lagertank læser den den tank.",
    "@3 =grid tank_wall", "@3 =grid tank_controller", "@3 =grid tank_valve", "@3 =grid tank_gauge",
    "@4 Gaskomprimering (niveau 4) låser op for tankkompressoren. Med én i skallen rummer tanken 50.000 enheder gas per "
    "blok indre (væsker stadig 16.000), og tomme stålcylindre kommer fyldte ud. Fjern kompressoren, og den gas, tanken "
    "ikke længere kan rumme, slipper ud.",
    "@4 =grid tank_compressor",
    "@3 Hver tank har en sikkerhedsventil: snig og brug dens styring, en ventil eller en lagertank med tom hånd, og "
    "gassen går ud i luften. Væsker kan ikke lukkes ud. Ødelægger du styringen, mistes indholdet; et hul i skallen "
    "standser kun tanken, til du lukker det.",
    "@3 # Rør og pumper",
    "@3 Væskerør forbinder maskiner med tanke: lagertanke, tankstyringer og tankventiler (en maskine, der rører en tank, "
    "virker på samme måde). En maskine sender hver væske og gas, den laver, gennem sine rør til de tanke, de når, før den "
    "fylder kolber, så den behøver ingen; og når dens indgangs- eller reagensplads er tom, tager den de væsker og gasser, "
    "dens reaktioner bruger, en kolbe ad gangen, uden kolbe. Rør rummer intet; en ledning må være 1.024 rør lang.",
    "@3 En væskepumpe flytter 250 enheder per tick fra tankene bag den, direkte eller gennem rør, til tankene foran; den "
    "vender, som du kigger, når du sætter den, og et redstonesignal standser den.",
    "@3 =grid fluid_pipe", "@3 =grid fluid_pump",
]
GUIDE.update(_G)

# ---- Translations: block (name, description), LANG in key order, research (name, description). ----

_B = {
    "pt_br": {
        "tank_wall": ("Chapa de Tanque", "Chapa de ferro rebitada para a casca de um tanque: feche uma caixa em volta de um interior vazio"),
        "tank_controller": ("Controlador de Tanque", "Um na casca de todo tanque: guarda o que o tanque guarda, 16.000 unidades por bloco do interior"),
        "tank_valve": ("Válvula de Tanque", "Coloque na casca de um tanque: tubos, funis e você enchem e esvaziam o tanque por ela"),
        "tank_gauge": ("Mostrador de Tanque", "Visor opcional para a casca de um tanque, ou encostado num Tanque de Armazenamento: mostra o percentual usado"),
        "tank_compressor": ("Compressor de Tanque", "Um na casca de um tanque comprime o gás: 50.000 unidades de gás por bloco do interior"),
        "fluid_pipe": ("Tubo de Fluido", "Liga máquinas a tanques: a máquina manda seus líquidos e gases aos tanques que alcança e tira deles o que precisa"),
        "fluid_pump": ("Bomba de Fluido", "Move 250 unidades por tique dos tanques atrás dela para os da frente; redstone a para"),
    },
    "es_es": {
        "tank_wall": ("Chapa de Tanque", "Chapa de hierro remachada para la carcasa de un tanque: cierra una caja alrededor de un interior vacío"),
        "tank_controller": ("Controlador de Tanque", "Uno en la carcasa de cada tanque: guarda lo que guarda el tanque, 16.000 unidades por bloque interior"),
        "tank_valve": ("Válvula de Tanque", "Ponla en la carcasa de un tanque: tuberías, tolvas y tú llenáis y vaciáis el tanque por ella"),
        "tank_gauge": ("Indicador de Tanque", "Esfera opcional para la carcasa de un tanque, o junto a un Tanque de almacenamiento: muestra el porcentaje usado"),
        "tank_compressor": ("Compresor de Tanque", "Uno en la carcasa de un tanque comprime el gas: 50.000 unidades de gas por bloque interior"),
        "fluid_pipe": ("Tubería de Fluido", "Une máquinas con tanques: la máquina manda sus líquidos y gases a los tanques que alcanza y saca lo que necesita"),
        "fluid_pump": ("Bomba de Fluido", "Mueve 250 unidades por tick de los tanques de detrás a los de delante; la redstone la detiene"),
    },
    "fr_fr": {
        "tank_wall": ("Tôle de Réservoir", "Tôle de fer rivetée pour la coque d'un réservoir : fermez une boîte autour d'un intérieur vide"),
        "tank_controller": ("Contrôleur de Réservoir", "Un dans la coque de chaque réservoir : il contient ce que contient le réservoir, 16 000 unités par bloc intérieur"),
        "tank_valve": ("Vanne de Réservoir", "Posez-la dans la coque : tuyaux, entonnoirs et vous remplissez et videz le réservoir par elle"),
        "tank_gauge": ("Jauge de Réservoir", "Cadran facultatif pour la coque d'un réservoir, ou contre un Réservoir de stockage : affiche le pourcentage utilisé"),
        "tank_compressor": ("Compresseur de Réservoir", "Un dans la coque comprime le gaz : 50 000 unités de gaz par bloc intérieur"),
        "fluid_pipe": ("Tuyau de Fluide", "Relie les machines aux réservoirs : une machine envoie ses liquides et gaz aux réservoirs atteints et y puise ce qu'il lui faut"),
        "fluid_pump": ("Pompe à Fluide", "Déplace 250 unités par tick des réservoirs derrière elle vers ceux de devant ; la redstone l'arrête"),
    },
    "de_de": {
        "tank_wall": ("Tankblech", "Genietetes Eisenblech für die Hülle eines Tanks: baue einen geschlossenen Kasten um einen leeren Innenraum"),
        "tank_controller": ("Tanksteuerung", "Eine in der Hülle jedes Tanks: sie fasst, was der Tank fasst, 16.000 Einheiten je Block Innenraum"),
        "tank_valve": ("Tankventil", "In die Hülle eines Tanks setzen: Rohre, Trichter und du füllen und leeren den Tank dadurch"),
        "tank_gauge": ("Tankanzeige", "Freiwilliges Zifferblatt für die Hülle eines Tanks oder an einem Lagertank: zeigt den belegten Prozentsatz"),
        "tank_compressor": ("Tankverdichter", "Einer in der Hülle verdichtet Gas: 50.000 Einheiten Gas je Block Innenraum"),
        "fluid_pipe": ("Fluidrohr", "Verbindet Maschinen mit Tanks: eine Maschine schickt ihre Flüssigkeiten und Gase in erreichte Tanks und holt, was sie braucht"),
        "fluid_pump": ("Fluidpumpe", "Bewegt 250 Einheiten pro Tick aus den Tanks hinter ihr in die davor; Redstone hält sie an"),
    },
    "it_it": {
        "tank_wall": ("Lamiera da Serbatoio", "Lamiera di ferro rivettata per il guscio di un serbatoio: chiudi una scatola attorno a un interno vuoto"),
        "tank_controller": ("Controllore di Serbatoio", "Uno nel guscio di ogni serbatoio: contiene ciò che contiene il serbatoio, 16.000 unità per blocco interno"),
        "tank_valve": ("Valvola da Serbatoio", "Mettila nel guscio: tubi, tramogge e tu riempite e svuotate il serbatoio da lì"),
        "tank_gauge": ("Indicatore da Serbatoio", "Quadrante facoltativo per il guscio di un serbatoio, o contro un Serbatoio di stoccaggio: mostra la percentuale usata"),
        "tank_compressor": ("Compressore da Serbatoio", "Uno nel guscio comprime il gas: 50.000 unità di gas per blocco interno"),
        "fluid_pipe": ("Tubo per Fluidi", "Collega le macchine ai serbatoi: una macchina manda liquidi e gas ai serbatoi che raggiunge e preleva ciò che le serve"),
        "fluid_pump": ("Pompa per Fluidi", "Sposta 250 unità per tick dai serbatoi dietro a quelli davanti; la redstone la ferma"),
    },
    "nl_nl": {
        "tank_wall": ("Tankplaat", "Geklonken ijzeren plaat voor de wand van een tank: bouw een gesloten doos rond een lege binnenruimte"),
        "tank_controller": ("Tankregelaar", "Eén in de wand van elke tank: bevat wat de tank bevat, 16.000 eenheden per blok binnenruimte"),
        "tank_valve": ("Tankkraan", "Zet hem in de wand: buizen, trechters en jij vullen en legen de tank erdoor"),
        "tank_gauge": ("Tankmeter", "Wijzerplaat naar keuze voor de wand van een tank, of tegen een Opslagtank: toont het gebruikte percentage"),
        "tank_compressor": ("Tankcompressor", "Eén in de wand comprimeert gas: 50.000 eenheden gas per blok binnenruimte"),
        "fluid_pipe": ("Vloeistofbuis", "Verbindt machines met tanks: een machine stuurt haar vloeistoffen en gassen naar bereikte tanks en haalt wat ze nodig heeft"),
        "fluid_pump": ("Vloeistofpomp", "Verplaatst 250 eenheden per tick van de tanks erachter naar die ervoor; redstone stopt haar"),
    },
    "ru_ru": {
        "tank_wall": ("Лист резервуара", "Клёпаный железный лист для оболочки резервуара: закройте коробку вокруг пустой полости"),
        "tank_controller": ("Контроллер резервуара", "Один в оболочке каждого резервуара: хранит его содержимое, 16 000 единиц на блок полости"),
        "tank_valve": ("Вентиль резервуара", "Поставьте в оболочку: трубы, воронки и вы наполняете и опорожняете резервуар через него"),
        "tank_gauge": ("Указатель резервуара", "Необязательный циферблат для оболочки резервуара или накопительного резервуара: показывает процент заполнения"),
        "tank_compressor": ("Компрессор резервуара", "Один в оболочке сжимает газ: 50 000 единиц газа на блок полости"),
        "fluid_pipe": ("Труба для жидкостей", "Соединяет машины с резервуарами: машина шлёт жидкости и газы в достижимые резервуары и берёт нужное"),
        "fluid_pump": ("Насос для жидкостей", "Перекачивает 250 единиц за тик из резервуаров позади в резервуары впереди; редстоун его останавливает"),
    },
    "sv_se": {
        "tank_wall": ("Tankplåt", "Nitad järnplåt till skalet på en tank: bygg en sluten låda runt ett tomt inre"),
        "tank_controller": ("Tankstyrning", "En i skalet på varje tank: rymmer det tanken rymmer, 16 000 enheter per block inre"),
        "tank_valve": ("Tankventil", "Sätt den i skalet: rör, trattar och du fyller och tömmer tanken genom den"),
        "tank_gauge": ("Tankmätare", "Valfri tavla för skalet på en tank, eller mot en lagringstank: visar använd procent"),
        "tank_compressor": ("Tankkompressor", "En i skalet komprimerar gas: 50 000 enheter gas per block inre"),
        "fluid_pipe": ("Vätskerör", "Förbinder maskiner med tankar: en maskin skickar sina vätskor och gaser till nådda tankar och tar det den behöver"),
        "fluid_pump": ("Vätskepump", "Flyttar 250 enheter per tick från tankarna bakom till de framför; redstone stoppar den"),
    },
    "da_dk": {
        "tank_wall": ("Tankplade", "Nittet jernplade til skallen på en tank: byg en lukket kasse rundt om et tomt indre"),
        "tank_controller": ("Tankstyring", "Én i skallen på hver tank: rummer det, tanken rummer, 16.000 enheder per blok indre"),
        "tank_valve": ("Tankventil", "Sæt den i skallen: rør, tragte og du fylder og tømmer tanken gennem den"),
        "tank_gauge": ("Tankmåler", "Valgfri skive til skallen på en tank, eller mod en lagertank: viser den brugte procent"),
        "tank_compressor": ("Tankkompressor", "Én i skallen komprimerer gas: 50.000 enheder gas per blok indre"),
        "fluid_pipe": ("Væskerør", "Forbinder maskiner med tanke: en maskine sender sine væsker og gasser til nåede tanke og tager det, den skal bruge"),
        "fluid_pump": ("Væskepumpe", "Flytter 250 enheder per tick fra tankene bagved til dem foran; redstone standser den"),
    },
}

_L = {
    "pt_br": ["Tanque %s: %s de %s unidades de %s (%s%%)",
              "Tanque %1$s, %2$s blocos no interior: vazio; guarda %3$s unidades de líquido ou gás",
              "Tanque %1$s, %2$s blocos no interior, com compressor: vazio; guarda %3$s unidades de líquido ou %4$s de gás",
              "Tanque: nenhum interior fechado e vazio de até %s blocos de lado junto ao controlador (falha na casca?)",
              "Tanque: %s em %s %s %s não é um bloco de tanque",
              "Tanque: o interior deve ser uma caixa vazia, sem nada dentro",
              "Tanque: cada tanque tem um só Controlador de Tanque",
              "%s; ainda guarda %s unidades de %s",
              "Não faz parte de um tanque com Controlador de Tanque",
              "Este mostrador não lê nada: coloque-o na casca de um tanque ou encostado num Tanque de Armazenamento",
              "O tanque está vazio: nada a liberar",
              "%s é líquido: só gases podem ser liberados no ar",
              "%s unidades de %s liberadas no ar"],
    "es_es": ["Tanque %s: %s de %s unidades de %s (%s%%)",
              "Tanque %1$s, %2$s bloques dentro: vacío; guarda %3$s unidades de líquido o gas",
              "Tanque %1$s, %2$s bloques dentro, con compresor: vacío; guarda %3$s unidades de líquido o %4$s de gas",
              "Tanque: no hay interior cerrado y vacío de hasta %s bloques de lado junto al controlador (¿hueco en la carcasa?)",
              "Tanque: %s en %s %s %s no es un bloque de tanque",
              "Tanque: el interior debe ser una caja vacía, sin nada dentro",
              "Tanque: cada tanque tiene un solo Controlador de Tanque",
              "%s; aún guarda %s unidades de %s",
              "No forma parte de un tanque con Controlador de Tanque",
              "Este indicador no lee nada: ponlo en la carcasa de un tanque o junto a un Tanque de almacenamiento",
              "El tanque está vacío: nada que liberar",
              "%s es un líquido: solo los gases pueden liberarse al aire",
              "%s unidades de %s liberadas al aire"],
    "fr_fr": ["Réservoir %s : %s sur %s unités de %s (%s %%)",
              "Réservoir %1$s, %2$s blocs à l'intérieur : vide ; contient %3$s unités de liquide ou de gaz",
              "Réservoir %1$s, %2$s blocs à l'intérieur, avec compresseur : vide ; contient %3$s unités de liquide ou %4$s de gaz",
              "Réservoir : aucun intérieur fermé et vide d'au plus %s blocs de côté près du contrôleur (un trou dans la coque ?)",
              "Réservoir : %s en %s %s %s n'est pas un bloc de réservoir",
              "Réservoir : l'intérieur doit être une boîte vide, sans rien dedans",
              "Réservoir : un réservoir n'a qu'un seul Contrôleur de Réservoir",
              "%s ; il contient encore %s unités de %s",
              "Ne fait pas partie d'un réservoir avec un Contrôleur de Réservoir",
              "Cette jauge ne lit rien : posez-la dans la coque d'un réservoir ou contre un Réservoir de stockage",
              "Le réservoir est vide : rien à relâcher",
              "%s est un liquide : seuls les gaz peuvent être relâchés dans l'air",
              "%s unités de %s relâchées dans l'air"],
    "de_de": ["Tank %s: %s von %s Einheiten %s (%s %%)",
              "Tank %1$s, %2$s Blöcke innen: leer; fasst %3$s Einheiten Flüssigkeit oder Gas",
              "Tank %1$s, %2$s Blöcke innen, mit Verdichter: leer; fasst %3$s Einheiten Flüssigkeit oder %4$s Gas",
              "Tank: kein geschlossener, leerer Innenraum von höchstens %s Blöcken Seitenlänge an der Steuerung (Lücke in der Hülle?)",
              "Tank: %s bei %s %s %s ist kein Tankblock",
              "Tank: der Innenraum muss ein leerer Kasten sein, ohne etwas darin",
              "Tank: ein Tank hat nur eine Tanksteuerung",
              "%s; er fasst noch %s Einheiten %s",
              "Gehört zu keinem Tank mit Tanksteuerung",
              "Diese Anzeige liest nichts: setze sie in die Hülle eines Tanks oder an einen Lagertank",
              "Der Tank ist leer: nichts abzulassen",
              "%s ist eine Flüssigkeit: nur Gase lassen sich in die Luft ablassen",
              "%s Einheiten %s in die Luft abgelassen"],
    "it_it": ["Serbatoio %s: %s di %s unità di %s (%s%%)",
              "Serbatoio %1$s, %2$s blocchi interni: vuoto; contiene %3$s unità di liquido o gas",
              "Serbatoio %1$s, %2$s blocchi interni, con compressore: vuoto; contiene %3$s unità di liquido o %4$s di gas",
              "Serbatoio: nessun interno chiuso e vuoto di al massimo %s blocchi di lato accanto al controllore (un buco nel guscio?)",
              "Serbatoio: %s in %s %s %s non è un blocco da serbatoio",
              "Serbatoio: l'interno deve essere una scatola vuota, senza nulla dentro",
              "Serbatoio: un serbatoio ha un solo Controllore di Serbatoio",
              "%s; contiene ancora %s unità di %s",
              "Non fa parte di un serbatoio con Controllore di Serbatoio",
              "Questo indicatore non legge nulla: mettilo nel guscio di un serbatoio o contro un Serbatoio di stoccaggio",
              "Il serbatoio è vuoto: niente da liberare",
              "%s è un liquido: solo i gas si possono liberare nell'aria",
              "%s unità di %s liberate nell'aria"],
    "nl_nl": ["Tank %s: %s van %s eenheden %s (%s%%)",
              "Tank %1$s, %2$s blokken binnen: leeg; bevat %3$s eenheden vloeistof of gas",
              "Tank %1$s, %2$s blokken binnen, met compressor: leeg; bevat %3$s eenheden vloeistof of %4$s gas",
              "Tank: geen gesloten, lege binnenruimte van hoogstens %s blokken per zijde bij de regelaar (een gat in de wand?)",
              "Tank: %s op %s %s %s is geen tankblok",
              "Tank: de binnenruimte moet een lege doos zijn, zonder iets erin",
              "Tank: een tank heeft maar één Tankregelaar",
              "%s; hij bevat nog %s eenheden %s",
              "Hoort niet bij een tank met een Tankregelaar",
              "Deze meter leest niets: zet hem in de wand van een tank of tegen een Opslagtank",
              "De tank is leeg: niets om af te blazen",
              "%s is een vloeistof: alleen gassen kunnen de lucht in",
              "%s eenheden %s afgeblazen"],
    "ru_ru": ["Резервуар %s: %s из %s единиц: %s (%s%%)",
              "Резервуар %1$s, %2$s блоков внутри: пуст; вмещает %3$s единиц жидкости или газа",
              "Резервуар %1$s, %2$s блоков внутри, с компрессором: пуст; вмещает %3$s единиц жидкости или %4$s газа",
              "Резервуар: рядом с контроллером нет закрытой пустой полости не больше %s блоков по стороне (дыра в оболочке?)",
              "Резервуар: %s в %s %s %s не блок резервуара",
              "Резервуар: полость должна быть пустой коробкой, без ничего внутри",
              "Резервуар: у резервуара только один контроллер",
              "%s; в нём ещё %s единиц: %s",
              "Не часть резервуара с контроллером",
              "Этот указатель ничего не показывает: поставьте его в оболочку резервуара или к накопительному резервуару",
              "Резервуар пуст: выпускать нечего",
              "%s — жидкость: в воздух можно выпускать только газы",
              "Выпущено в воздух %s единиц: %s"],
    "sv_se": ["Tank %s: %s av %s enheter %s (%s %%)",
              "Tank %1$s, %2$s block inuti: tom; rymmer %3$s enheter vätska eller gas",
              "Tank %1$s, %2$s block inuti, med kompressor: tom; rymmer %3$s enheter vätska eller %4$s gas",
              "Tank: inget slutet, tomt inre på högst %s block per sida vid styrningen (ett hål i skalet?)",
              "Tank: %s vid %s %s %s är inget tankblock",
              "Tank: det inre måste vara en tom låda utan något i",
              "Tank: en tank har bara en tankstyrning",
              "%s; den rymmer fortfarande %s enheter %s",
              "Hör inte till en tank med tankstyrning",
              "Den här mätaren läser inget: sätt den i skalet på en tank eller mot en lagringstank",
              "Tanken är tom: inget att släppa ut",
              "%s är en vätska: bara gaser kan släppas ut i luften",
              "%s enheter %s släpptes ut i luften"],
    "da_dk": ["Tank %s: %s af %s enheder %s (%s %%)",
              "Tank %1$s, %2$s blokke inden i: tom; rummer %3$s enheder væske eller gas",
              "Tank %1$s, %2$s blokke inden i, med kompressor: tom; rummer %3$s enheder væske eller %4$s gas",
              "Tank: intet lukket, tomt indre på højst %s blokke per side ved styringen (et hul i skallen?)",
              "Tank: %s ved %s %s %s er ikke en tankblok",
              "Tank: det indre skal være en tom kasse uden noget i",
              "Tank: en tank har kun én tankstyring",
              "%s; den rummer stadig %s enheder %s",
              "Hører ikke til en tank med tankstyring",
              "Denne måler læser intet: sæt den i skallen på en tank eller mod en lagertank",
              "Tanken er tom: intet at lukke ud",
              "%s er en væske: kun gasser kan lukkes ud i luften",
              "%s enheder %s lukket ud i luften"],
}

_R = {
    "pt_br": ("Redes de Gás", "Winsor e Clegg iluminaram Londres com gás de carvão levado por canos de ferro desde os gasômetros (1812): Tubos de Fluido ligam máquinas a tanques, e Chapas de Tanque fazem um tanque do tamanho que você quiser"),
    "es_es": ("Redes de Gas", "Winsor y Clegg iluminaron Londres con gas de hulla llevado por tuberías de hierro desde los gasómetros (1812): las Tuberías de Fluido unen máquinas con tanques, y las Chapas de Tanque hacen un tanque tan grande como quieras"),
    "fr_fr": ("Conduites de Gaz", "Winsor et Clegg éclairèrent Londres au gaz de houille amené par des conduites de fer depuis les gazomètres (1812) : les Tuyaux de Fluide relient les machines aux réservoirs, et les Tôles de Réservoir font un réservoir aussi grand que vous voulez"),
    "de_de": ("Gasleitungen", "Winsor und Clegg beleuchteten London mit Kohlegas aus Gasometern durch eiserne Leitungen (1812): Fluidrohre verbinden Maschinen mit Tanks, und Tankbleche bauen einen Tank so groß, wie du willst"),
    "it_it": ("Condotte del Gas", "Winsor e Clegg illuminarono Londra con gas di carbone portato da condotte di ferro dai gasometri (1812): i Tubi per Fluidi collegano le macchine ai serbatoi, e le Lamiere da Serbatoio fanno un serbatoio grande quanto vuoi"),
    "nl_nl": ("Gasleidingen", "Winsor en Clegg verlichtten Londen met steenkoolgas door ijzeren leidingen uit gashouders (1812): Vloeistofbuizen verbinden machines met tanks, en Tankplaten bouwen een tank zo groot als je wilt"),
    "ru_ru": ("Газовые магистрали", "Уинзор и Клегг осветили Лондон каменноугольным газом, подаваемым по железным трубам из газгольдеров (1812): трубы для жидкостей соединяют машины с резервуарами, а листы резервуара позволяют построить резервуар любого размера"),
    "sv_se": ("Gasledningar", "Winsor och Clegg lyste upp London med kolgas genom järnledningar från gasklockor (1812): vätskerör förbinder maskiner med tankar, och tankplåtar bygger en tank så stor du vill"),
    "da_dk": ("Gasledninger", "Winsor og Clegg oplyste London med kulgas gennem jernledninger fra gasbeholdere (1812): væskerør forbinder maskiner med tanke, og tankplader bygger en tank så stor, du vil"),
}


def translations(code):
    """English -> translation for the tank blocks, screen lines and the Gas Mains research, in one language."""
    out = {}
    if code not in _B:
        return out
    for bid, (name, desc) in BLOCKS.items():
        out[name], out[desc] = _B[code][bid]
    for key, t in zip(LANG, _L[code]):
        out[LANG[key]] = t
    out[RESEARCH[0]], out[RESEARCH[1]] = _R[code]
    return out

# ---- 3.38.0 (second pass): the gauge goes onto a plate of a finished tank, one per tank. ----

_GAUGE_LANG = {
    "gui.periodic_elements.big_tank.gauge_needs_tank": "Use the gauge on a plate of a finished tank",
    "gui.periodic_elements.big_tank.one_gauge": "This tank already has its gauge: one per tank",
    "gui.periodic_elements.big_tank.two_gauges": "Tank: only one Tank Gauge per tank",
}
_GAUGE_T = {
    "pt_br": ["Use o mostrador numa chapa de um tanque pronto", "Este tanque já tem mostrador: um por tanque",
              "Tanque: só um Mostrador de Tanque por tanque"],
    "es_es": ["Usa el indicador en una chapa de un tanque terminado", "Este tanque ya tiene indicador: uno por tanque",
              "Tanque: solo un Indicador de Tanque por tanque"],
    "fr_fr": ["Utilisez la jauge sur une tôle d'un réservoir terminé", "Ce réservoir a déjà sa jauge : une par réservoir",
              "Réservoir : une seule Jauge de Réservoir par réservoir"],
    "de_de": ["Benutze die Anzeige an einem Blech eines fertigen Tanks", "Dieser Tank hat schon seine Anzeige: eine pro Tank",
              "Tank: nur eine Tankanzeige pro Tank"],
    "it_it": ["Usa l'indicatore su una lamiera di un serbatoio finito", "Questo serbatoio ha già il suo indicatore: uno per serbatoio",
              "Serbatoio: un solo Indicatore da Serbatoio per serbatoio"],
    "nl_nl": ["Gebruik de meter op een plaat van een afgebouwde tank", "Deze tank heeft al een meter: één per tank",
              "Tank: maar één Tankmeter per tank"],
    "ru_ru": ["Используйте указатель на листе готового резервуара", "У этого резервуара уже есть указатель: один на резервуар",
              "Резервуар: только один указатель на резервуар"],
    "sv_se": ["Använd mätaren på en plåt i en färdig tank", "Den här tanken har redan sin mätare: en per tank",
              "Tank: bara en tankmätare per tank"],
    "da_dk": ["Brug måleren på en plade i en færdig tank", "Denne tank har allerede sin måler: én per tank",
              "Tank: kun én tankmåler per tank"],
}
for _k, _v in _GAUGE_LANG.items():
    LANG[_k] = _v
for _c, _t in _GAUGE_T.items():
    _L[_c] = _L[_c] + _t

_GAUGE_GUIDE = {
    "en_us": "@3 A tank has at most one gauge. To add one to a finished tank, use the Tank Gauge on one of its plates: "
             "the gauge takes the plate's place, facing you, the plate comes back to you and the tank keeps all it holds.",
    "pt_br": "@3 Um tanque tem no máximo um mostrador. Para pôr um num tanque pronto, use o Mostrador de Tanque numa das "
             "chapas: o mostrador toma o lugar da chapa, virado para você, a chapa volta para você e o tanque guarda tudo "
             "o que tinha.",
    "es_es": "@3 Un tanque tiene como mucho un indicador. Para añadirlo a un tanque terminado, usa el Indicador de Tanque "
             "en una de sus chapas: el indicador ocupa el lugar de la chapa, mirando hacia ti, la chapa vuelve a ti y el "
             "tanque conserva todo lo que guarda.",
    "fr_fr": "@3 Un réservoir a au plus une jauge. Pour en ajouter une à un réservoir terminé, utilisez la Jauge de "
             "Réservoir sur l'une de ses tôles : la jauge prend la place de la tôle, tournée vers vous, la tôle vous "
             "revient et le réservoir garde tout ce qu'il contient.",
    "de_de": "@3 Ein Tank hat höchstens eine Anzeige. Um einem fertigen Tank eine zu geben, benutze die Tankanzeige an "
             "einem seiner Bleche: die Anzeige nimmt den Platz des Blechs ein, zu dir gewandt, das Blech bekommst du "
             "zurück, und der Tank behält alles, was er fasst.",
    "it_it": "@3 Un serbatoio ha al massimo un indicatore. Per aggiungerlo a un serbatoio finito, usa l'Indicatore da "
             "Serbatoio su una delle sue lamiere: l'indicatore prende il posto della lamiera, rivolto verso di te, la "
             "lamiera torna a te e il serbatoio conserva tutto ciò che contiene.",
    "nl_nl": "@3 Een tank heeft hoogstens één meter. Om er een aan een afgebouwde tank toe te voegen, gebruik je de "
             "Tankmeter op een van zijn platen: de meter neemt de plaats van de plaat in, naar jou toe, je krijgt de "
             "plaat terug en de tank houdt alles wat hij bevat.",
    "ru_ru": "@3 У резервуара не больше одного указателя. Чтобы добавить его к готовому резервуару, используйте "
             "указатель на одном из листов: указатель встанет на место листа, лицом к вам, лист вернётся к вам, а "
             "резервуар сохранит всё содержимое.",
    "sv_se": "@3 En tank har högst en mätare. För att sätta en på en färdig tank, använd tankmätaren på en av dess plåtar: "
             "mätaren tar plåtens plats, vänd mot dig, plåten kommer tillbaka till dig och tanken behåller allt den rymmer.",
    "da_dk": "@3 En tank har højst én måler. For at sætte en på en færdig tank, brug tankmåleren på en af dens plader: "
             "måleren tager pladens plads, vendt mod dig, pladen kommer tilbage til dig, og tanken beholder alt, den rummer.",
}
for _c, _p in _GAUGE_GUIDE.items():
    GUIDE[_c].insert(3, _p)
