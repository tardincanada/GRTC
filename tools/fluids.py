"""Containers for liquids and gases (1.5.0).

Every liquid or gas item (an element or substance whose state is "liquid" or "gas") is held in a Glass Flask:
111.1 units, and the empty flask comes back when the item is used. Gases can also be compressed into Steel
Cylinders of 1000 units (nine flasks' worth): one "<gas>_cylinder" item per gas, made only by a machine with a
Compressor upgrade. This module writes the cylinder items (composition, texture, model, name) and fluids.json,
which tells the Java side which items need which container.
"""
import json

from substances import _hex, cylinder_texture

FLASK = "glass_flask"
CYLINDER = "steel_cylinder"
CYLINDER_UNITS = 1000.0
# A Storage Tank holds sixteen blocks' worth of one liquid or gas.
TANK_UNITS = 16000.0


def fluid_ids(elements, subs):
    liquids = [e["id"] for e in elements if e["state"] == "liquid"] + [s["id"] for s in subs if s["state"] == "liquid"]
    gases = [e["id"] for e in elements if e["state"] == "gas"] + [s["id"] for s in subs if s["state"] == "gas"]
    return liquids, gases


def generate_fluids(assets, runtime_dir, lang, mod_id, elements, subs, comps):
    liquids, gases = fluid_ids(elements, subs)
    colors = {e["id"]: e["color"] for e in elements}
    colors.update({s["id"]: s["color"] for s in subs})
    for g in gases:
        cid = f"{g}_cylinder"
        key = f"{mod_id}:{cid}"
        assert key not in comps, key
        comps[key] = {"amount": CYLINDER_UNITS, "parts": [{"id": g, "units": CYLINDER_UNITS}]}
        lang[f"item.{mod_id}.{cid}"] = f"{lang[f'item.{mod_id}.{g}']} Cylinder"
        cylinder_texture(_hex(colors[g])).save(assets / "textures" / "item" / f"{cid}.png")
        (assets / "models" / "item" / f"{cid}.json").write_text(json.dumps(
            {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{cid}"}}, indent=2) + "\n",
            encoding="utf-8")
    (runtime_dir / "compositions.json").write_text(json.dumps(comps, indent=1) + "\n", encoding="utf-8")
    (runtime_dir / "fluids.json").write_text(json.dumps({
        "flask": FLASK, "cylinder": CYLINDER, "cylinderUnits": CYLINDER_UNITS, "tankUnits": TANK_UNITS,
        "liquids": liquids, "gases": gases}, indent=1) + "\n", encoding="utf-8")
    lang[f"tooltip.{mod_id}.in_flask"] = "In a glass flask: the empty comes back when it is used"
    lang[f"tooltip.{mod_id}.in_cylinder"] = "Compressed: %s units, nine flasks' worth; the cylinder comes back when it is used"
    return liquids, gases
