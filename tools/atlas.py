"""Field Atlas (2.7.0, stage 1 of the map plan): item names, textures, recipes, research unlocks and text.

The Atlas is one item that gains features as the player researches (levels 1 to 3 in stage 1). Java:
com.periodicelements.atlas. Recipes are added to machines.RECIPES / SHAPELESS so the guide lists them.
"""
from PIL import Image

ATLAS_ITEMS = {
    "field_atlas": ("Field Atlas", "Use it to open your map; it draws the land you pass while you carry it"),
    "atlas_copy": ("Atlas Copy", "Use it to add everything drawn in it to your own Field Atlas; it is used up"),
    "survey_balloon": ("Survey Balloon", "Use it to go up and draw 128 blocks all round on your Atlas; used up"),
    # 3.10.0, stage 2 (levels 4 and 5).
    "core_drill": ("Core Drill", "Use it on the ground to bring up a core: every rock and ore down to the bottom"),
    "prospecting_magnetometer": ("Prospecting Magnetometer",
                                 "Use it to find magnetic and dense ground in the chunks around you, not which ore"),
    # 3.14.0, stage 3 (levels 6 and 7).
    "handheld_radar": ("Handheld Radar", "In your pack: a minimap at the top left (key N) and creatures within 48 blocks"),
    "gps_receiver": ("GPS Receiver", "In your pack: the minimap anywhere, a marker where you die, routes to your markers"),
    "satellite": ("Satellite", "Use it on a Launch Pad under open sky to put it in orbit for your Tracking Stations"),
}
# Blocks of the Atlas (stage 2): name, tooltip.
ATLAS_BLOCKS = {
    "radio_transmitter": ("Radio Transmitter",
                          "Shares your Atlas markers with every player who knows radio within 512 blocks"),
    "radar_station": ("Radar Station", "With power: your Atlas shows creatures and players within 96 blocks of it"),
    "tracking_station": ("Tracking Station", "With power and a satellite up: maps 512 blocks all round onto your Atlas each day"),
    "launch_pad": ("Launch Pad", "Use a Satellite on it, under open sky, to launch it"),
}
# Unlocks that name an Atlas feature ("map:...") or an item gate that lives in Java ("item:vanilla_map").
MAP_FEATURES = {"compass", "latitude", "copy", "longitude", "contours", "geology",
                "ores", "radiation", "anomalies", "geiger_log", "radio",
                "radar", "minimap", "isotopes", "satellite", "landsat", "gps"}
ITEM_UNLOCKS = {"field_atlas", "survey_balloon", "vanilla_map", "vanilla_compass", "core_drill", "prospecting_magnetometer",
                "radio_transmitter", "radar_station", "handheld_radar", "satellite", "launch_pad", "tracking_station",
                "gps_receiver"}

SHAPED = {
    "survey_balloon": {"pattern": ["WWW", "WCW", "SPS"],
                       "key": {"W": "minecraft:white_wool", "C": "minecraft:charcoal", "S": "minecraft:string",
                               "P": "minecraft:oak_planks"}},
    "core_drill": {"pattern": ["SSS", " T ", " D "],
                   "key": {"S": "periodic_elements:steel_ingot", "T": "minecraft:stick", "D": "minecraft:diamond"}},
    "prospecting_magnetometer": {"pattern": ["BGB", "WCW", "BBB"],
                                 "key": {"B": "periodic_elements:brass_ingot", "G": "minecraft:glass_pane",
                                         "W": "periodic_elements:copper_wire", "C": "minecraft:compass"}},
    "radio_transmitter": {"pattern": ["WLW", "CRC", "SSS"],
                          "key": {"W": "periodic_elements:copper_wire", "L": "minecraft:lightning_rod",
                                  "C": "minecraft:copper_ingot", "R": "minecraft:redstone_block",
                                  "S": "periodic_elements:steel_ingot"}},
}
SHAPED.update({
    "radar_station": {"pattern": ["BBB", "WGW", "SRS"],
                      "key": {"B": "minecraft:iron_bars", "W": "periodic_elements:copper_wire",
                              "G": "periodic_elements:germanium_crystal", "R": "minecraft:redstone_block",
                              "S": "periodic_elements:steel_ingot"}},
    "handheld_radar": {"pattern": ["SPS", "WGW", "SRS"],
                       "key": {"S": "periodic_elements:steel_ingot", "P": "minecraft:glass_pane",
                               "W": "periodic_elements:copper_wire", "G": "periodic_elements:germanium_crystal",
                               "R": "minecraft:redstone"}},
    "satellite": {"pattern": ["PTP", "WRW", "TKT"],
                  "key": {"P": "periodic_elements:solar_panel", "T": "periodic_elements:titanium_alloy_ingot",
                          "W": "periodic_elements:copper_wire", "R": "minecraft:redstone_block",
                          "K": "periodic_elements:kerosene"}},
    "launch_pad": {"pattern": ["S S", "SOS", "BBB"],
                   "key": {"S": "periodic_elements:steel_ingot", "O": "periodic_elements:oxygen_cylinder",
                           "B": "minecraft:stone_bricks"}},
    "tracking_station": {"pattern": ["BBB", "WGW", "SCS"],
                         "key": {"B": "minecraft:iron_bars", "W": "periodic_elements:copper_wire",
                                 "G": "periodic_elements:germanium_crystal", "C": "periodic_elements:silicon_wafer",
                                 "S": "periodic_elements:steel_ingot"}},
    "gps_receiver": {"pattern": ["APA", "ACA", "ALA"],
                     "key": {"A": "periodic_elements:aluminium", "P": "minecraft:glass_pane",
                             "C": "periodic_elements:integrated_circuit", "L": "periodic_elements:lithium_ion_battery"}},
})
SHAPELESS = {
    "field_atlas": (["minecraft:leather", "minecraft:paper", "minecraft:paper", "minecraft:paper",
                     "periodic_elements:carbon_ink"], 1),
}

LANG = {
    "gui.periodic_elements.atlas.layer.ores": "Ores",
    "gui.periodic_elements.atlas.layer.radiation": "Radiation",
    "gui.periodic_elements.atlas.layer.anomalies": "Anomalies",
    "gui.periodic_elements.atlas.ores.none": "No ores found here",
    "gui.periodic_elements.atlas.rad.unknown": "No reading here",
    "gui.periodic_elements.atlas.anomaly": "Magnetic %s · dense %s",
    "gui.periodic_elements.atlas.core": "Core to Y %s: %s",
    "gui.periodic_elements.atlas.core.ores": "Ores in the core: %s",
    "gui.periodic_elements.atlas.core.needs": "Research Diamond Core Drill to take a core",
    "gui.periodic_elements.atlas.magnetometer": "Survey of %s chunks: strongest magnetic %s, strongest dense %s",
    "gui.periodic_elements.atlas.magnetometer.needs": "Research Magnetic and Seismic Prospecting to read the magnetometer",
    "gui.periodic_elements.atlas.radio.placed": "Transmitter on the air: players with radio within 512 blocks see your markers",
    "gui.periodic_elements.atlas.radio.needs": "Research Radio Direction Finding to set up a transmitter",
    "gui.periodic_elements.atlas.shared": "%s (from %s)",
    "gui.periodic_elements.atlas.title": "Field Atlas",
    "gui.periodic_elements.atlas.needs": "Research Surveying to draw with the Field Atlas",
    "gui.periodic_elements.atlas.empty": "Nothing drawn yet: carry the Atlas and walk",
    "gui.periodic_elements.atlas.x": "X %s",
    "gui.periodic_elements.atlas.y": "Y %s",
    "gui.periodic_elements.atlas.z": "Z %s",
    "gui.periodic_elements.atlas.scale": "%s blocks",
    "gui.periodic_elements.atlas.layer.terrain": "Terrain",
    "gui.periodic_elements.atlas.layer.geology": "Rocks",
    "gui.periodic_elements.atlas.markers": "Markers %s of %s",
    "gui.periodic_elements.atlas.marker.name": "Marker name",
    "gui.periodic_elements.atlas.marker.full": "No room for more markers",
    "gui.periodic_elements.atlas.help": "Drag to move, scroll to zoom, right-click to add or remove a marker",
    "gui.periodic_elements.atlas.copied": "Atlas Copy made: %s areas",
    "gui.periodic_elements.atlas.copy.needs": "Research Portolan Charts to copy the Atlas",
    "gui.periodic_elements.atlas.copy.paper": "Hold paper in your other hand to make a copy",
    "gui.periodic_elements.atlas.merged": "Added to your Atlas: %s new columns",
    "gui.periodic_elements.atlas.copy.blank": "This copy is blank",
    "gui.periodic_elements.atlas.balloon": "From the balloon you drew %s columns",
    "gui.periodic_elements.atlas.balloon.needs": "Research Hot-Air Balloon to fly the Survey Balloon",
    "gui.periodic_elements.atlas.vanilla_locked": "Research %s to make this",
    "gui.periodic_elements.atlas.rock.unknown": "Rock not yet seen",
    "gui.periodic_elements.atlas.rock.soil": "Deep soil",
    "gui.periodic_elements.atlas.dir.n": "N",
    "gui.periodic_elements.atlas.dir.e": "E",
    "gui.periodic_elements.atlas.dir.s": "S",
    "gui.periodic_elements.atlas.dir.w": "W",
    # 3.14.0, stage 3.
    "gui.periodic_elements.atlas.layer.isotopes": "Isotopes",
    "gui.periodic_elements.atlas.death": "Where you died",
    "gui.periodic_elements.atlas.route": "%s: %s blocks",
    "gui.periodic_elements.atlas.help.gps": "Drag, scroll to zoom, right-click: marker, middle-click a marker: route",
    "gui.periodic_elements.atlas.radar.placed": "Radar Station set up: give it power to see creatures within 96 blocks",
    "gui.periodic_elements.atlas.radar.on": "Radar on: creatures within %s blocks show on your Atlas",
    "gui.periodic_elements.atlas.tracking.placed": "Tracking Station set up: give it power and launch a satellite",
    "gui.periodic_elements.atlas.tracking.none": "No satellite in orbit: launch one from a Launch Pad",
    "gui.periodic_elements.atlas.tracking.on": "Tracking %s satellites: mapped %s blocks out",
    "gui.periodic_elements.atlas.station.no_power": "No power: connect it to the wire network",
    "gui.periodic_elements.atlas.satellite.needs": "Research Satellite and Tracking Station to launch it",
    "gui.periodic_elements.atlas.satellite.sky": "The Launch Pad needs open sky above it",
    "gui.periodic_elements.atlas.satellite.launched": "Satellite in orbit: you have %s up",
    "key.periodic_elements.minimap": "Show or hide the minimap",
    "key.categories.periodic_elements": "Elementar",
}

PAPER, PAPER_D, LEATHER, LEATHER_D, INK = (236, 226, 196), (196, 184, 150), (126, 78, 44), (92, 54, 30), (40, 40, 48)


def _rect(img, x0, y0, x1, y1, color):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(color) + (255,)


def atlas_item_texture(iid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    if iid == "field_atlas":
        # A leather-bound book lying open: a sketched coast and a red route.
        _rect(img, 1, 3, 14, 13, LEATHER_D)
        _rect(img, 2, 3, 7, 12, PAPER)
        _rect(img, 8, 3, 13, 12, PAPER)
        _rect(img, 7, 3, 8, 12, PAPER_D)
        for x, y in [(3, 9), (4, 8), (4, 7), (5, 6), (6, 6), (9, 5), (10, 6), (11, 6), (12, 7), (12, 8), (11, 9)]:
            px[x, y] = (70, 110, 150, 255)
        for x, y in [(3, 5), (4, 5), (5, 4), (10, 9), (11, 10)]:
            px[x, y] = (190, 50, 40, 255)
    elif iid == "atlas_copy":
        # A rolled sheet tied with string.
        _rect(img, 3, 4, 12, 11, PAPER)
        _rect(img, 3, 4, 12, 4, PAPER_D)
        _rect(img, 3, 11, 12, 11, PAPER_D)
        _rect(img, 7, 4, 8, 11, (180, 150, 110))
        for x, y in [(4, 7), (5, 6), (6, 7), (9, 8), (10, 7), (11, 8)]:
            px[x, y] = (70, 110, 150, 255)
    elif iid == "core_drill":
        # A steel T-handle over a hollow bit with a diamond crown.
        _rect(img, 3, 2, 12, 3, (150, 156, 166))
        _rect(img, 7, 4, 8, 11, (120, 124, 134))
        _rect(img, 6, 12, 9, 13, (100, 104, 112))
        for x, y in [(6, 14), (8, 14), (7, 15), (9, 15)]:
            px[x, y] = (170, 235, 240, 255)
    elif iid == "prospecting_magnetometer":
        # A brass box with a dial and a swinging needle.
        _rect(img, 2, 3, 13, 13, (180, 140, 60))
        _rect(img, 4, 5, 11, 11, (236, 230, 210))
        for x, y in [(6, 10), (7, 9), (8, 8), (9, 7)]:
            px[x, y] = (200, 40, 30, 255)
        _rect(img, 7, 10, 8, 10, (40, 40, 48))
        _rect(img, 13, 6, 14, 7, (200, 120, 60))
    elif iid == "handheld_radar":
        # A grey box with a round green scope and a folding aerial.
        _rect(img, 2, 4, 13, 14, (90, 94, 100))
        _rect(img, 4, 6, 11, 12, (20, 40, 24))
        for x, y in [(7, 9), (8, 9), (7, 8), (8, 8)]:
            px[x, y] = (120, 240, 120, 255)
        for x, y in [(9, 7), (10, 6), (6, 11)]:
            px[x, y] = (80, 200, 90, 255)
        _rect(img, 12, 1, 12, 3, (160, 164, 170))
        _rect(img, 4, 13, 5, 13, (220, 60, 40))
    elif iid == "gps_receiver":
        # A small handset with a screen showing a map and a dot.
        _rect(img, 4, 2, 11, 14, (50, 52, 58))
        _rect(img, 5, 3, 10, 9, (120, 170, 110))
        _rect(img, 5, 6, 10, 6, (70, 110, 160))
        px[8, 5] = (230, 40, 30, 255)
        for x in (5, 7, 9):
            _rect(img, x, 11, x + 1, 12, (150, 150, 156))
        _rect(img, 10, 0, 10, 1, (120, 120, 126))
    elif iid == "satellite":
        # A sphere with two solar wings and aerials.
        _rect(img, 6, 6, 9, 9, (200, 204, 210))
        _rect(img, 7, 5, 8, 10, (200, 204, 210))
        _rect(img, 5, 7, 10, 8, (200, 204, 210))
        _rect(img, 0, 6, 3, 9, (40, 60, 140))
        _rect(img, 12, 6, 15, 9, (40, 60, 140))
        _rect(img, 4, 7, 4, 8, (150, 150, 156))
        _rect(img, 11, 7, 11, 8, (150, 150, 156))
        for x, y in [(10, 11), (11, 12), (12, 13), (5, 11), (4, 12), (3, 13)]:
            px[x, y] = (170, 170, 176, 255)
    elif iid == "survey_balloon":
        # A striped envelope over a wicker basket.
        for y in range(1, 10):
            half = [2, 4, 5, 5, 6, 6, 5, 4, 3][y - 1]
            for x in range(8 - half, 8 + half):
                px[x, y] = (210, 60, 50, 255) if (x // 2) % 2 == 0 else (240, 210, 120, 255)
        for x0 in (5, 10):
            _rect(img, x0, 10, x0, 11, (80, 70, 60))
        _rect(img, 5, 12, 10, 14, (150, 110, 60))
        _rect(img, 5, 12, 10, 12, (110, 80, 40))
    return img


# Field Guide chapter (English; each tools/guide_<code>.py has ATLAS with the same paragraphs and headings).
GUIDE = [
    "# The Field Atlas",
    "Surveying (level 1): leather, three paper and carbon ink make the Field Atlas. Carry it anywhere in your pack and "
    "it draws the land within 24 blocks of where you walk, in rough four-block strokes. Use it to open the map: drag "
    "to move, scroll to zoom, right-click to add or remove a marker (8 at first). Without a compass you only know "
    "roughly where you are. Surveying also unlocks the vanilla map recipe.",
    "# Compass and coordinates",
    "Magnetic Compass (level 2): the map shows north and an arrow at your exact place; while you hold the Atlas a "
    "compass bar runs along the top of the screen. It also unlocks the vanilla compass recipe.",
    "Quadrant and Astrolabe (level 2): the Atlas shows Z (how far north or south you are), a scale bar, and draws 48 "
    "blocks around you. Portolan Charts (level 2): sneak and use the Atlas with paper in your other hand to make an "
    "Atlas Copy; whoever uses the copy adds all of it, markers too, to their own Atlas.",
    "Marine Chronometer (level 3): the Atlas shows X and Y as well, on the map and on the compass bar.",
    "# Surveying from the air and the rock beneath",
    "Hot-Air Balloon (level 3): a Survey Balloon (wool, charcoal, string and planks) draws 128 blocks all round at "
    "once, and is used up.",
    "Triangulation and Theodolite (level 3): the Atlas draws block by block, with relief shading and a contour line "
    "every 8 blocks of height, and holds 32 markers.",
    "Geological Map (level 3): the Atlas looks up to 24 blocks under the soil of every column you pass and notes the "
    "first rock. Switch the Rocks layer on with the button at the top right; the bar names the rock under the cursor.",
    "# Prospecting, radiation and radio",
    "Diamond Core Drill (level 4): steel, a stick and a diamond make the Core Drill. Use it on the ground: the chat "
    "lists every rock and ore down to the bottom, and the Ores layer of the Atlas marks the chunk. Ores you mine are "
    "marked too.",
    "Mapping Radioactivity (level 4): every electroscope reading goes on the Radiation layer, from green (background) "
    "to red. Geiger Survey (level 5): with a Geiger counter in your pack, the radiation where you walk is logged by "
    "itself.",
    "Magnetic and Seismic Prospecting (level 5): the Prospecting Magnetometer reads the 7x7 chunks around you. The "
    "Anomalies layer shows magnetic ground (iron, nickel, cobalt, chromium) in blue and dense ground (other ores) in "
    "orange, against the survey's own background; it does not name the ore.",
    "Radio Direction Finding (level 5): a Radio Transmitter shares your markers and its own place with every player "
    "within 512 blocks who knows radio; they show in blue on that player's Atlas.",
    "# Radar, satellites and GPS",
    "Radar (level 6): a Radar Station on the wire network shows you, on the Atlas, every creature and player within 96 "
    "blocks of it: players white, hostile creatures red, the rest green. With a Handheld Radar in your pack a minimap "
    "sits at the top left of the screen, with what the set sees within 48 blocks; the N key hides it.",
    "Airborne Gamma Spectrometry (level 6): radiation readings are split by isotope (U-238, Ra-226, Th-232, Cs-137 and "
    "more) on the Isotopes layer, coloured by the main source. Fly with a Geiger counter, or stand 16 blocks above the "
    "ground, and the 3x3 chunks below are read at once.",
    "Satellite and Tracking Station (level 7): use a Satellite on a Launch Pad under open sky to put it in orbit. A "
    "Tracking Station with power then maps the land round itself onto your Atlas, from the station outwards, 512 blocks "
    "each way in a day, even where nobody has been: land never loaded is drawn from its height and kind of country. "
    "Each satellite (up to four) makes it faster.",
    "Multispectral Scanner (level 7): the satellites also note the rock of the land they map, and ores within 8 blocks "
    "of the surface, where the land is loaded.",
    "Global Positioning System (level 7): a GPS Receiver in your pack shows the minimap anywhere, with your "
    "coordinates. Where you die goes on the Atlas as a marker, and a middle-click on a marker sets a route: its "
    "distance under the minimap and its bearing on the compass bar.",
]


def _block_textures(b):
    """(side, top) 16 x 16 textures of an Atlas block."""
    side = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    top = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    if b == "radio_transmitter":
        _rect(side, 0, 0, 15, 15, (70, 72, 78))
        _rect(side, 1, 1, 14, 14, (110, 104, 92))
        _rect(side, 3, 3, 12, 8, (40, 40, 44))
        for x in range(4, 12, 2):
            _rect(side, x, 4, x, 7, (90, 90, 96))
        _rect(side, 3, 11, 5, 13, (220, 60, 40))
        _rect(side, 10, 11, 12, 13, (200, 170, 90))
        _rect(top, 0, 0, 15, 15, (70, 72, 78))
        _rect(top, 6, 6, 9, 9, (200, 120, 60))
    elif b == "radar_station":
        # A steel cabinet with a round green scope; on top a dish.
        _rect(side, 0, 0, 15, 15, (80, 84, 90))
        _rect(side, 1, 1, 14, 14, (120, 124, 130))
        _rect(side, 4, 3, 11, 10, (20, 40, 24))
        for x, y in [(7, 6), (8, 6), (7, 7), (8, 7), (9, 5), (10, 4), (5, 9)]:
            side.load()[x, y] = (110, 240, 110, 255)
        _rect(side, 3, 12, 5, 13, (220, 60, 40))
        _rect(side, 10, 12, 12, 13, (60, 200, 90))
        _rect(top, 0, 0, 15, 15, (80, 84, 90))
        for y in range(2, 14):
            half = [3, 5, 6, 6, 7, 7, 7, 7, 6, 6, 5, 3][y - 2]
            _rect(top, 8 - half, y, 7 + half, y, (190, 194, 200))
        _rect(top, 7, 7, 8, 8, (60, 62, 68))
    elif b == "tracking_station":
        # A white cabinet with a row of dials; on top a big dish with a feed.
        _rect(side, 0, 0, 15, 15, (90, 92, 98))
        _rect(side, 1, 1, 14, 14, (220, 222, 226))
        for x in (3, 7, 11):
            _rect(side, x, 3, x + 2, 5, (30, 30, 36))
            side.load()[x + 1, 4] = (240, 200, 60, 255)
        _rect(side, 2, 8, 13, 10, (40, 60, 140))
        _rect(side, 3, 12, 5, 13, (60, 200, 90))
        _rect(top, 0, 0, 15, 15, (90, 92, 98))
        for y in range(1, 15):
            half = [3, 5, 6, 7, 7, 8, 8, 8, 8, 7, 7, 6, 5, 3][y - 1]
            _rect(top, 8 - half, y, 7 + half, y, (230, 232, 236))
        _rect(top, 7, 7, 8, 8, (200, 60, 40))
    elif b == "launch_pad":
        # Scorched concrete with yellow and black hazard edges.
        _rect(side, 0, 0, 15, 15, (150, 150, 146))
        for x in range(0, 16, 4):
            _rect(side, x, 0, x + 1, 2, (230, 190, 40))
            _rect(side, x + 2, 0, x + 3, 2, (30, 30, 30))
        _rect(top, 0, 0, 15, 15, (160, 160, 156))
        for i in range(16):
            for x, y in [(i, 0), (i, 15), (0, i), (15, i)]:
                top.load()[x, y] = (230, 190, 40, 255) if (i // 2) % 2 == 0 else (30, 30, 30, 255)
        _rect(top, 5, 5, 10, 10, (70, 66, 62))
        _rect(top, 7, 7, 8, 8, (40, 36, 34))
    return side, top


def atlas_block_assets(assets, data_root, lang, mod_id):
    """The Atlas's blocks: textures, model, blockstate, loot, tool tag and name."""
    import json
    tex = assets / "textures" / "block"
    tex.mkdir(parents=True, exist_ok=True)

    def dump(path, obj):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")
    path = data_root / "minecraft" / "tags" / "block" / "mineable" / "pickaxe.json"
    values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
    for b in ATLAS_BLOCKS:
        side, top = _block_textures(b)
        side.save(tex / f"{b}_side.png")
        top.save(tex / f"{b}_top.png")
        m = f"{mod_id}:block/{b}"
        bottom = m + ("_side" if b == "launch_pad" else "_top")
        dump(assets / "models" / "block" / f"{b}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
            "side": m + "_side", "top": m + "_top", "bottom": bottom}})
        dump(assets / "blockstates" / f"{b}.json", {"variants": {"": {"model": m}}})
        dump(assets / "models" / "item" / f"{b}.json", {"parent": m})
        dump(data_root / mod_id / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod_id}:{b}"}],
             "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
        if f"{mod_id}:{b}" not in values:
            values.append(f"{mod_id}:{b}")
    dump(path, {"replace": False, "values": values})
    for b, (name, desc) in ATLAS_BLOCKS.items():
        lang[f"block.{mod_id}.{b}"] = name
        lang[f"block.{mod_id}.{b}.desc"] = desc
