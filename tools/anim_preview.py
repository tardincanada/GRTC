"""Preview sheets of the machine animations (3.33.0): an animated GIF per tech level, each machine drawn as an
isometric block, idle and then one block per process look, labelled with the processes it covers.

    python3 tools/anim_preview.py <out_dir> [lang]      (lang defaults to pt_br)

Reads the generated textures and animations.json, so run tools/generate.py first.
"""
import csv
import json
import sys
from math import gcd
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src" / "main" / "resources"
TEX = RES / "assets" / "periodic_elements" / "textures" / "block"
W = 48          # face size in the preview (16 px x 3)
TILE = 2 * W
LABEL_H = 34
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"


def _shade(img, k):
    px = img.load()
    out = img.copy()
    po = out.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            po[x, y] = (int(r * k), int(g * k), int(b * k), a)
    return out


def iso(top, front, side):
    """An isometric block from three 16x16 faces."""
    out = Image.new("RGBA", (2 * W, 2 * W), (0, 0, 0, 0))
    faces = [
        (top.resize((W, W), Image.NEAREST), (0.5, 1, -W / 2, -0.5, 1, W / 2)),
        (_shade(front.resize((W, W), Image.NEAREST), 0.85), (1, 0, 0, -0.5, 1, -W / 2)),
        (_shade(side.resize((W, W), Image.NEAREST), 0.62), (1, 0, -W, 0.5, 1, -1.5 * W)),
    ]
    for img, data in faces:
        t = img.transform((2 * W, 2 * W), Image.AFFINE, data, resample=Image.NEAREST)
        out.alpha_composite(t)
    return out


def _strip(name):
    p = TEX / f"{name}.png"
    if not p.exists():
        return None, 1
    img = Image.open(p).convert("RGBA")
    frames = [img.crop((0, i * 16, 16, i * 16 + 16)) for i in range(img.height // 16)]
    meta = TEX / f"{name}.png.mcmeta"
    ft = json.loads(meta.read_text())["animation"].get("frametime", 1) if meta.exists() else 1
    return frames, ft


def _wrap(text, font, width, draw):
    words, lines, cur = text.split(), [], ""
    for w in words:
        t = (cur + " " + w).strip()
        if draw.textlength(t, font=font) <= width:
            cur = t
        else:
            if cur:
                lines.append(cur)
            cur = w
    if cur:
        lines.append(cur)
    return lines[:2]


def sheet(level, machines, modes, lang, out_dir):
    font = ImageFont.truetype(FONT, 10)
    bold = ImageFont.truetype(FONT, 13)
    rows = []
    for mid in machines:
        side = Image.open(TEX / f"{mid}_side.png").convert("RGBA")
        top = Image.open(TEX / f"{mid}_top.png").convert("RGBA")
        front = Image.open(TEX / f"{mid}_front.png").convert("RGBA")
        by_mode = {}
        for rid, m in sorted(modes.get(mid, {}).items(), key=lambda kv: ORDER.get(kv[0], 0)):
            by_mode.setdefault(m, []).append(rid)
        n = max(by_mode) + 1 if by_mode else 1
        tiles = [("parada", None, None)]
        for m in range(n):
            sfx = "" if m == 0 else f"_{m}"
            fr, ft = _strip(f"{mid}_front_active{sfx}")
            tp, tt = _strip(f"{mid}_top_active{sfx}")
            names = [lang.get(f"reaction.periodic_elements.{r}", r) for r in by_mode.get(m, [])]
            label = names[0] + (f" (+{len(names) - 1})" if len(names) > 1 else "") if names else "em uso"
            tiles.append((label, (fr, ft), (tp, tt)))
        rows.append((lang.get(f"block.periodic_elements.{mid}", mid), side, top, front, tiles))
    cols = max(len(r[4]) for r in rows)
    width = 16 + cols * (TILE + 12)
    row_h = 22 + TILE + LABEL_H
    height = 16 + len(rows) * row_h
    # One GIF frame per 2 game ticks, long enough for every strip to loop.
    fts = {1}
    for r in rows:
        for _, f, t in r[4][1:]:
            fts.update({f[1] if f else 1, t[1] if t else 1})
    loop = 8
    for ft in fts:
        loop = loop * ft * 8 // gcd(loop, ft * 8)
    loop = min(loop, 192)
    frames = []
    for tick in range(0, loop, 2):
        img = Image.new("RGB", (width, height), (36, 38, 44))
        d = ImageDraw.Draw(img)
        for ri, (name, side, top, front, tiles) in enumerate(rows):
            y = 8 + ri * row_h
            d.text((10, y), name, font=bold, fill=(240, 240, 240))
            for ci, (label, f, t) in enumerate(tiles):
                x = 10 + ci * (TILE + 12)
                if f is None:
                    block = iso(top, front, side)
                else:
                    fi = f[0][(tick // f[1]) % len(f[0])] if f[0] else front
                    ti = t[0][(tick // t[1]) % len(t[0])] if t[0] else top
                    block = iso(ti, fi, side)
                img.paste(block, (x, y + 20), block)
                for li, line in enumerate(_wrap(label, font, TILE + 8, d)):
                    d.text((x, y + 22 + TILE + li * 12), line, font=font,
                           fill=(170, 170, 170) if f is None else (230, 230, 200))
        frames.append(img)
    path = out_dir / f"animacoes_nivel_{level}.gif"
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=100, loop=0, optimize=True)
    return path


ORDER = {}

PARTS = ["refractory_brick", "tuyere", "silica_brick", "lead_sheet", "gay_lussac_tower", "distillation_tray",
         "diffuser_stage", "graphite_block", "fuel_channel", "reactor_shielding", "structure_port"]


POWER = ["voltaic_pile", "steam_engine", "dynamo", "diesel_generator", "solar_panel", "nuclear_reactor",
         "radioisotope_generator", "wind_turbine", "helium_collector"]


def power_sheet(lang, out_dir):
    """The power blocks (3.33.1): each one stopped, then running."""
    font = ImageFont.truetype(FONT, 11)
    cols = 3
    cell = (TILE * 2 + 40, TILE + 50)
    rows = (len(POWER) + cols - 1) // cols
    data = {}
    for b in POWER:
        side = Image.open(TEX / f"{b}_side.png").convert("RGBA")
        data[b] = (side, Image.open(TEX / f"{b}_top.png").convert("RGBA"), Image.open(TEX / f"{b}_front.png").convert("RGBA"),
                   _strip(f"{b}_front_on"), _strip(f"{b}_top_on"))
    frames = []
    for tick in range(0, 48, 2):
        img = Image.new("RGB", (16 + cols * cell[0], 16 + rows * cell[1]), (36, 38, 44))
        d = ImageDraw.Draw(img)
        for k, b in enumerate(POWER):
            x, y = 10 + (k % cols) * cell[0], 8 + (k // cols) * cell[1]
            side, top, front, (fr, ft), (tp, tt) = data[b]
            d.text((x, y), lang.get(f"block.periodic_elements.{b}", b), font=font, fill=(240, 240, 240))
            blk = iso(top, front, side)
            img.paste(blk, (x, y + 18), blk)
            blk = iso(tp[(tick // tt) % len(tp)], fr[(tick // ft) % len(fr)], side)
            img.paste(blk, (x + TILE + 12, y + 18), blk)
        frames.append(img)
    path = out_dir / "animacoes_energia.gif"
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=100, loop=0, optimize=True)
    return path


def battery_sheet(lang, out_dir):
    """3.41.0: the Lithium-Ion Battery charging from empty to full and running down again."""
    import animations
    font = ImageFont.truetype(FONT, 14)
    big = ImageFont.truetype(FONT, 22)
    side = Image.open(TEX / "lithium_ion_battery_side.png").convert("RGBA")
    names = ["vazia (0%)", "vermelho (abaixo de 25%)", "amarelo (25% a 49%)", "verde (50% a 99%)", "azul (100%)"]
    steps = list(range(0, 101, 2)) + [100] * 8 + list(range(100, -1, -2)) + [0] * 8
    frames = []
    for pct in steps:
        f = pct / 100
        level, band = _battery_state(f)
        front = Image.open(TEX / f"lithium_ion_battery_front_{level}_{band}.png").convert("RGBA")
        top = Image.open(TEX / f"lithium_ion_battery_top_{band}.png").convert("RGBA")
        img = Image.new("RGB", (2 * TILE + 260, 2 * TILE + 40), (36, 38, 44))
        d = ImageDraw.Draw(img)
        blk = iso(top, front, side).resize((2 * TILE, 2 * TILE), Image.NEAREST)
        img.paste(blk, (16, 20), blk)
        d.text((2 * TILE + 40, 50), f"{pct}%", font=big, fill=(240, 240, 240))
        d.text((2 * TILE + 40, 90), f"barra: {level} de {animations.BATTERY_ROWS}", font=font, fill=(200, 200, 200))
        d.text((2 * TILE + 40, 115), "cor: " + names[band], font=font, fill=animations.BATTERY_COLOURS[band] if band else (160, 160, 160))
        frames.append(img)
    path = out_dir / "bateria_simulacao.gif"
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=120, loop=0, optimize=True)
    return path


def _battery_state(f):
    """The same rules as BatteryBlock.level and band."""
    import animations
    rows = animations.BATTERY_ROWS
    if f <= 1e-9:
        return 0, 0
    if f >= 1 - 1e-9:
        return rows, 4
    level = max(1, min(rows - 1, int(f * rows + 0.5)))
    return level, 1 if f < 0.25 else 2 if f < 0.5 else 3


def parts_sheet(lang, out_dir):
    """The multiblock blocks: each one still, then working."""
    font = ImageFont.truetype(FONT, 11)
    cols = 4
    cell = (TILE * 2 + 30, TILE + 50)
    rows = (len(PARTS) + cols - 1) // cols
    strips = {b: _strip(f"{b}_active") for b in PARTS}
    frames = []
    for tick in range(0, 48, 2):
        img = Image.new("RGB", (16 + cols * cell[0], 16 + rows * cell[1]), (36, 38, 44))
        d = ImageDraw.Draw(img)
        for k, b in enumerate(PARTS):
            x, y = 10 + (k % cols) * cell[0], 8 + (k // cols) * cell[1]
            still = Image.open(TEX / f"{b}.png").convert("RGBA")
            fr, ft = strips[b]
            act = fr[(tick // ft) % len(fr)]
            d.text((x, y), lang.get(f"block.periodic_elements.{b}", b), font=font, fill=(240, 240, 240))
            blk = iso(still, still, still)
            img.paste(blk, (x, y + 18), blk)
            blk = iso(act, act, act)
            img.paste(blk, (x + TILE + 12, y + 18), blk)
        frames.append(img)
    path = out_dir / "animacoes_multiblocos.gif"
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=100, loop=0, optimize=True)
    return path


def main():
    with open(ROOT / "data" / "reactions.csv", encoding="utf-8") as f:
        ORDER.update({r["id"]: i for i, r in enumerate(csv.DictReader(f))})
    out_dir = Path(sys.argv[1])
    out_dir.mkdir(parents=True, exist_ok=True)
    code = sys.argv[2] if len(sys.argv) > 2 else "pt_br"
    lang = json.loads((RES / "assets" / "periodic_elements" / "lang" / f"{code}.json").read_text(encoding="utf-8"))
    modes = json.loads((RES / "periodic_elements" / "animations.json").read_text(encoding="utf-8"))
    by_level = {}
    with open(ROOT / "data" / "machines.csv", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            by_level.setdefault(max(1, int(r["level"])), []).append(r["id"])  # level 0 (the pit) joins level 1
    only = set(sys.argv[3].split(",")) if len(sys.argv) > 3 else None
    if not only or "battery" in only:
        print(battery_sheet(lang, out_dir))
    if not only or "power" in only:
        print(power_sheet(lang, out_dir))
    if not only or "multi" in only:
        print(parts_sheet(lang, out_dir))
    for level, ms in sorted(by_level.items()):
        if only and str(level) not in only:
            continue
        print(sheet(level, ms, modes, lang, out_dir))


if __name__ == "__main__":
    main()
