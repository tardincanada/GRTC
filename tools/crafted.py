"""Compositions of crafted vanilla blocks, derived from the game's own recipes (3.34.0).

data/compositions.csv lists natural blocks and a few items by hand. Most vanilla blocks are made from those by a
recipe (4 brick -> bricks, 3 stone -> 6 slabs, sand -> glass ...): this tool gives each of them the sum of what goes
in, divided by how many come out, so nothing can be made from nothing by crafting a block and taking it apart.

    python3 tools/crafted.py [minecraft jar]   # writes data/crafted_compositions.csv (needs a generated compositions.json)

The jar defaults to the one Loom keeps in ~/.gradle. generate.py reads the csv (composition_rows) and lets hand rows win.
Rules: crafting (shaped, shapeless), stonecutting and smelting recipes, the game's and the mod's; for a tag the cheapest known item stands in (the first listed among equals);
when several recipes make the same thing the cheapest per item is used; rows are written in terms of hand rows only.
Only blocks get rows (items such as sticks are used on the way but get none).
"""
import csv
import glob
import json
import os
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CSV = ROOT / "data" / "crafted_compositions.csv"
TYPES = {"crafting_shaped", "crafting_shapeless", "stonecutting", "smelting"}


def default_jar():
    pattern = os.path.expanduser("~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-*/minecraft-merged-1.21.1-*.jar")
    jars = sorted(glob.glob(pattern))
    assert jars, "no Minecraft 1.21.1 jar in the Loom cache: run ./gradlew build once, or pass the jar"
    return jars[0]


def derive(jar, base, extra_recipes=()):
    """base: id -> amount (hand rows). Returns id -> (amount, {base id: units}, recipe id) for crafted blocks."""
    z = zipfile.ZipFile(jar)
    tags, recipes, blocks = {}, [], set()
    for n in z.namelist():
        if n.startswith("data/minecraft/tags/item/") and n.endswith(".json"):
            tags["minecraft:" + n[len("data/minecraft/tags/item/"):-5]] = json.load(z.open(n))["values"]
        elif n.startswith("data/minecraft/recipe/") and n.endswith(".json"):
            recipes.append(("minecraft:" + n[len("data/minecraft/recipe/"):-5], json.load(z.open(n))))
        elif n.startswith("assets/minecraft/blockstates/") and n.endswith(".json"):
            blocks.add("minecraft:" + n[len("assets/minecraft/blockstates/"):-5])
    # The mod's recipes count only where they make a vanilla block (a cheaper way to it), not for items on the way:
    # otherwise string made from nylon would make wool nylon.
    vanilla_ids = {rid for rid, _ in recipes} | {rid for rid, _ in extra_recipes if rid.startswith("same:")}
    for rid, r in extra_recipes:
        res = r.get("result") or {}
        if (res.get("id") or res.get("item")) in blocks:
            recipes.append((rid, r))

    def options(ing):
        if isinstance(ing, list):
            return [o for i in ing for o in options(i)]
        if "item" in ing:
            return [ing["item"]]
        if "tag" in ing:
            tag = ing["tag"] if ":" in ing["tag"] else "minecraft:" + ing["tag"]
            return [o for v in tags.get(tag, []) for o in (options({"tag": v[1:]}) if v.startswith("#") else [v])]
        return []

    def inputs(r):
        t = r["type"].split(":")[-1]
        if t == "crafting_shaped":
            count = {}
            for row in r["pattern"]:
                for ch in row:
                    if ch != " ":
                        count[ch] = count.get(ch, 0) + 1
            return [(r["key"][k], n) for k, n in count.items()]
        if t == "crafting_shapeless":
            return [(i, 1) for i in r["ingredients"]]
        return [(r["ingredient"], 1)]

    # mix: id -> {base id: units} for ONE item; amount = sum of units.
    mix = {k: {k: a} for k, a in base.items()}
    source = {}
    vanilla = {}  # the cheapest vanilla recipe's makeup, per item
    vsource = {}
    for _ in range(60):
        changed = False
        for rid, r in recipes:
            if r.get("type", "").split(":")[-1] not in TYPES or "result" not in r:
                continue
            out = r["result"].get("id") or r["result"].get("item")
            if out in base:
                continue
            n_out = r["result"].get("count", 1)
            total = {}
            ok = True
            for ing, n in inputs(r):
                opts = options(ing)
                known = [o for o in opts if o in mix]
                if not known:
                    ok = False
                    break
                # Cheapest; among equals, the one the tag lists first (oak planks for #planks).
                pick = min(known, key=lambda o: (round(sum(mix[o].values()), 6), opts.index(o)))
                for k, u in mix[pick].items():
                    total[k] = total.get(k, 0) + u * n / n_out
            if not ok:
                continue
            amount = sum(total.values())
            if rid not in vanilla_ids and out in vanilla:
                # A mod recipe sets the amount; the vanilla recipe's makeup is kept (see below).
                v = vanilla[out]
                total = {b: u / sum(v.values()) * amount for b, u in v.items()}
            same_recipe_new_makeup = source.get(out) == rid and any(
                abs(total.get(b, 0) - mix[out].get(b, 0)) > 1e-6 for b in set(total) | set(mix[out]))
            if out not in mix or amount < sum(mix[out].values()) - 1e-6 or same_recipe_new_makeup:
                mix[out] = total
                source[out] = rid
                changed = True
            if rid in vanilla_ids:
                # The same recipe again, through vanilla recipes only (hand rows or vanilla-made items).
                vtotal = {}
                for ing, n in inputs(r):
                    opts = options(ing)
                    vk = [o for o in opts if o in base or o in vanilla]
                    if not vk:
                        vtotal = None
                        break
                    vm = lambda o: vanilla.get(o) or {o: base[o]}
                    pick = min(vk, key=lambda o: (round(sum(vm(o).values()), 6), opts.index(o)))
                    for k, u in vm(pick).items():
                        vtotal[k] = vtotal.get(k, 0) + u * n / n_out
                if vtotal and (out not in vanilla or sum(vtotal.values()) < sum(vanilla[out].values()) - 1e-6 or (
                        vsource.get(out) == rid and any(abs(vtotal.get(b, 0) - vanilla[out].get(b, 0)) > 1e-6
                                                        for b in set(vtotal) | set(vanilla[out])))):
                    vanilla[out] = vtotal
                    vsource[out] = rid
                    changed = True
        if not changed:
            break
    out = {}
    for k, m in mix.items():
        if k not in source or k not in blocks:
            continue
        amount = sum(m.values())
        if k in vanilla and source[k] not in vanilla_ids:
            # A mod recipe is cheaper (a PLA flower pot): keep that amount, so nothing comes from nothing, but the
            # makeup of the vanilla block (a brick pot), which is what nearly every such block in the world is.
            v = vanilla[k]
            m = {b: u / sum(v.values()) * amount for b, u in v.items()}
            source[k] = f"{source[k]} (amount); makeup of the vanilla recipe"
        if source[k].startswith("same:"):
            source[k] = "same as " + " + ".join(i["item"] for i in dict(extra_recipes)[source[k]]["ingredients"])
        out[k] = (amount, m, source[k])
    return out


def mod_recipes():
    """The mod's own recipes (some make vanilla blocks, such as PLA flower pots), as (id, json)."""
    out = []
    for p in sorted((ROOT / "src/main/resources/data").glob("*/recipe/*.json")):
        out.append((f"{p.parent.parent.name}:{p.stem}", json.loads(p.read_text(encoding="utf-8"))))
    return out


def write(rows):
    with CSV.open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["id", "amount", "composition", "note"])
        for key in sorted(rows):
            amount, m, rid = rows[key]
            parts = sorted(m.items(), key=lambda kv: -kv[1])
            comp = ";".join(f"block:{k}:{u / amount * 100:.4f}" for k, u in parts)
            w.writerow([key, f"{amount:.4f}", comp, f"recipe {rid}"])


def composition_rows(root=ROOT):
    """(id, amount, composition) for generate.py; hand rows with the same id win."""
    path = Path(root) / "data" / "crafted_compositions.csv"
    if not path.exists():
        return []
    with path.open(newline="", encoding="utf-8") as f:
        return [(r["id"], r["amount"], r["composition"]) for r in csv.DictReader(f)]


if __name__ == "__main__":
    jar = sys.argv[1] if len(sys.argv) > 1 else default_jar()
    comps = json.loads((ROOT / "src/main/resources/periodic_elements/compositions.json").read_text(encoding="utf-8"))
    with (ROOT / "data" / "compositions.csv").open(newline="", encoding="utf-8") as f:
        hand = {r["id"].strip() for r in csv.DictReader(f)}
    import vanilla_blocks
    hand |= {r[0] for r in vanilla_blocks.composition_rows()}
    crafted = {r[0] for r in composition_rows()} - hand
    base = {k: v["amount"] for k, v in comps.items() if k not in crafted}
    rows = derive(jar, base, mod_recipes() + vanilla_blocks.same_recipes())
    write(rows)
    print(f"{len(rows)} crafted blocks written to {CSV.relative_to(ROOT)}")
