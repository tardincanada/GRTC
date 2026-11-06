"""Writes the Geological world type's data files (2.6.0): world preset, noise settings, density functions.

The noise settings are vanilla overworld's with the terrain and the biome climate taken from the
periodic_elements:geology density function (Java, worldgen/GeoDensity). Needs the vanilla overworld
noise settings and normal preset, extracted from the Minecraft jar: python3 tools/geoworld.py <dir with data/minecraft>.
"""
import json
import sys
from pathlib import Path

# Snowy mountains leave the temperate band in Java (GeoWorld.snowFree, MultiNoiseBiomeSourceMixin).

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src" / "main" / "resources" / "data"
NS = "periodic_elements"


def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def main(vanilla):
    vanilla = Path(vanilla) / "data" / "minecraft" / "worldgen"
    wg = DATA / NS / "worldgen"
    write(wg / "noise" / "geology_seed.json", {"firstOctave": -7, "amplitudes": [1.0]})
    for out in ("sloped", "height", "continents", "erosion", "ridges", "depth", "shallow", "caves", "cave_fill", "cave_flood"):
        f = {"type": f"{NS}:geology", "output": out, "seed": f"{NS}:geology_seed"}
        if out in ("height", "continents", "erosion", "ridges"):
            f = {"type": "minecraft:flat_cache", "argument": {"type": "minecraft:cache_2d", "argument": f}}
        write(wg / "density_function" / "geological" / f"{out}.json", f)

    settings = json.loads((vanilla / "noise_settings" / "overworld.json").read_text())
    router = settings["noise_router"]
    text = json.dumps(router["final_density"])
    assert text.count('"minecraft:overworld/sloped_cheese"') == 3
    router["final_density"] = json.loads(text.replace('"minecraft:overworld/sloped_cheese"', f'"{NS}:geological/sloped"'))
    # Caves by rock (stage 2): vanilla's caves are filled in basin sediments and basalt (which get karst and lava tubes) and in most granite.
    router["final_density"] = {"type": "minecraft:min",
                               "argument1": {"type": "minecraft:max", "argument1": router["final_density"],
                                             "argument2": f"{NS}:geological/cave_fill"},
                               "argument2": f"{NS}:geological/caves"}
    router["initial_density_without_jaggedness"] = {"type": "minecraft:add", "argument1": 0.390625,
                                                    "argument2": f"{NS}:geological/sloped"}
    for k in ("continents", "erosion", "ridges", "depth"):
        router[k] = f"{NS}:geological/{k}"
    # Just under shallow seas and coasts, aquifers always take the sea's level: a hollow with a level of its own
    # was walled off by barrier blocks that stood as thin lines of land at sea level once the sea filled it.
    # 3.49.0: temperature and humidity scale with the biome size chosen for each world (worldgen/GeoClimate):
    # vanilla's noise with xz_scale = 0.25 / size. Worlds from before 3.49 keep temperature at twice the size.
    for k, kind in (("temperature", "temperature"), ("vegetation", "humidity")):
        v = router[k]
        assert v["type"] == "minecraft:shifted_noise" and v["xz_scale"] == 0.25 and v["y_scale"] == 0.0
        router[k] = {"type": f"{NS}:climate", "kind": kind, "xz_scale": 0.25, "shift_x": v["shift_x"],
                     "shift_z": v["shift_z"], "noise": v["noise"]}
    router["fluid_level_floodedness"] = {"type": "minecraft:max", "argument1": router["fluid_level_floodedness"],
                                         "argument2": {"type": "minecraft:max", "argument1": f"{NS}:geological/shallow",
                                                       "argument2": f"{NS}:geological/cave_flood"}}
    write(wg / "noise_settings" / "geological.json", settings)

    preset = json.loads((vanilla / "world_preset" / "normal.json").read_text())
    preset["dimensions"]["minecraft:overworld"]["generator"]["settings"] = f"{NS}:geological"
    write(wg / "world_preset" / "geological.json", preset)
    write(DATA / "minecraft" / "tags" / "worldgen" / "world_preset" / "normal.json",
          {"replace": False, "values": [f"{NS}:geological"]})


if __name__ == "__main__":
    main(sys.argv[1])
