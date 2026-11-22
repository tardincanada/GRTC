#!/usr/bin/env python3
"""Editable PNG pack of every block and item texture, and the way back into the mod.

Export (run from the project root):
  python3 tools/texture_pack.py export <out_dir> [--vanilla <dir with assets/minecraft/textures>]

  <out_dir>/texturas/<block|item|mob_effect|models/armor>/<name>.png
        every texture at its original resolution; the path is the mod's own texture path
  <out_dir>/icones-3d/<item_id>.png
        64x64 inventory icon of every item (blocks drawn in 3D, as in the inventory at GUI scale 4); view only
  <out_dir>/folhas/folha-texturas.png            all textures in one sheet, 1 px = 1 texel (editable)
  <out_dir>/folhas/folha-texturas-legenda.png    the same grid, enlarged, with every texture's name
  <out_dir>/folhas/folha-icones-3d.png           all inventory icons with names (view only)
  <out_dir>/folhas/folha-texturas.json           where each texture sits in folha-texturas.png
  <out_dir>/folhas/indice.csv                    row, column, file, name of every cell

Import (after the files were edited):
  python3 tools/texture_pack.py import <returned_dir> [--dry-run]

  Reads <returned_dir>/texturas/** and, if present, <returned_dir>/folhas/folha-texturas.png.
  Every texture whose pixels changed is written to art/texture_overrides/<path> (kept for good)
  and to src/main/resources/assets/periodic_elements/textures/<path> (used by the build).
  tools/generate.py re-applies art/texture_overrides after regenerating, so edits survive it.
"""
import argparse
import csv
import json
import math
import shutil
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

MOD_ID = "periodic_elements"
ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / MOD_ID
TEXTURES = ASSETS / "textures"
OVERRIDES = ROOT / "art" / "texture_overrides"
SECTIONS = [("block", "Blocos (textures/block)"), ("item", "Itens (textures/item)"),
            ("mob_effect", "Efeitos (textures/mob_effect)"), ("models/armor", "Armaduras vestidas (textures/models/armor)"),
            ("entity/fauna", "Animais (textures/entity/fauna)")]
ICON = 64
FONT_DIRS = ["/usr/share/fonts/truetype/dejavu/", "C:/Windows/Fonts/"]


def font(size, bold=False):
    for d in FONT_DIRS:
        for name in (["DejaVuSans-Bold.ttf", "arialbd.ttf"] if bold else ["DejaVuSans.ttf", "arial.ttf"]):
            try:
                return ImageFont.truetype(d + name, size)
            except OSError:
                pass
    return ImageFont.load_default()


def norm(img):
    """RGBA array with fully transparent pixels zeroed, so editors that drop hidden colour don't count as edits."""
    a = np.array(img.convert("RGBA"))
    a[a[..., 3] == 0] = 0
    return a


def same(a, b):
    return a.size == b.size and np.array_equal(norm(a), norm(b))


def texture_list():
    out = []
    for sec, _ in SECTIONS:
        d = TEXTURES / sec
        for f in sorted(d.glob("*.png")):
            out.append(f"{sec}/{f.name}")
    return out


def names():
    lang = {}
    for code in ("en_us", "pt_br"):
        p = ASSETS / "lang" / f"{code}.json"
        if p.exists():
            lang.update(json.loads(p.read_text(encoding="utf-8")))
    return {k.split(".", 2)[2]: v for k, v in lang.items()
            if k.startswith((f"item.{MOD_ID}.", f"block.{MOD_ID}.", f"entity.{MOD_ID}.")) and k.count(".") == 2}


# ---------------------------------------------------------------- 3D icons
FULL = [[0, 0, 0], [16, 16, 16]]
TEMPLATES = {
    "block/cube": {"down": "#down", "up": "#up", "north": "#north", "south": "#south", "west": "#west", "east": "#east"},
    "block/cube_all": dict.fromkeys(["down", "up", "north", "south", "west", "east"], "#all"),
    "block/leaves": dict.fromkeys(["down", "up", "north", "south", "west", "east"], "#all"),
    "block/cube_column": {"down": "#end", "up": "#end", **dict.fromkeys(["north", "south", "west", "east"], "#side")},
    "block/cube_column_horizontal": {"north": "#end", "south": "#end", **dict.fromkeys(["down", "up", "west", "east"], "#side")},
    "block/cube_bottom_top": {"down": "#bottom", "up": "#top", **dict.fromkeys(["north", "south", "west", "east"], "#side")},
    "block/orientable": {"down": "#top", "up": "#top", "north": "#front", **dict.fromkeys(["south", "west", "east"], "#side")},
    "block/orientable_with_bottom": {"down": "#bottom", "up": "#top", "north": "#front", **dict.fromkeys(["south", "west", "east"], "#side")},
}
FLAT_PARENTS = {"item/generated", "item/handheld", "block/cross", "block/tinted_cross"}


class Icons:
    def __init__(self, vanilla):
        self.vanilla = Path(vanilla) if vanilla else None
        self.cache = {}
        self.missing = set()
        t, p = np.radians(135), np.radians(30)
        ry = np.array([[np.cos(t), 0, np.sin(t)], [0, 1, 0], [-np.sin(t), 0, np.cos(t)]])
        rx = np.array([[1, 0, 0], [0, np.cos(p), -np.sin(p)], [0, np.sin(p), np.cos(p)]])
        self.R = rx @ ry
        self.scale = ICON / 16 * 0.625
        ys, xs = np.mgrid[0:ICON, 0:ICON] + 0.5
        cam = np.stack([(xs - ICON / 2) / self.scale, -(ys - ICON / 2) / self.scale, np.zeros_like(xs)], -1)
        self.A = cam @ self.R  # R^T applied to row vectors
        self.B = self.R[2]     # R^T (0,0,1)

    def texture(self, ref):
        if ref in self.cache:
            return self.cache[ref]
        ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
        f = (TEXTURES / f"{path}.png") if ns == MOD_ID else (self.vanilla / "assets" / ns / "textures" / f"{path}.png" if self.vanilla else None)
        if f is not None and f.exists():
            img = np.array(Image.open(f).convert("RGBA"))
            if img.shape[0] > img.shape[1]:  # animated strip: first frame
                img = img[:img.shape[1]]
        else:
            self.missing.add(ref)
            img = np.full((16, 16, 4), (128, 128, 128, 255), np.uint8)
        self.cache[ref] = img
        return img

    def model(self, ref):
        ns, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
        if ns != MOD_ID:
            return {"vanilla": path}
        f = ASSETS / "models" / f"{path}.json"
        return json.loads(f.read_text(encoding="utf-8")) if f.exists() else {"vanilla": "missing"}

    def resolve(self, ref):
        textures, elements, kind = {}, None, None
        while ref:
            m = self.model(ref)
            if "vanilla" in m:
                v = m["vanilla"]
                if v in FLAT_PARENTS:
                    kind = kind or ("flat" if v.startswith("item/") else "cross")
                elif v in TEMPLATES and elements is None:
                    elements = [{"from": FULL[0], "to": FULL[1], "faces": {k: {"texture": t} for k, t in TEMPLATES[v].items()}}]
                break
            for k, v in m.get("textures", {}).items():
                textures.setdefault(k, v)
            if elements is None and "elements" in m:
                elements = m["elements"]
            ref = m.get("parent")
            if kind is None and ref and ref.endswith(("item/generated", "item/handheld")):
                kind = "flat"

        def tex(v, depth=0):
            while isinstance(v, str) and v.startswith("#") and depth < 10:
                v, depth = textures.get(v[1:]), depth + 1
            return v
        return kind, {k: tex(v) for k, v in textures.items()}, elements, tex

    def flat(self, layers):
        out = Image.new("RGBA", (16, 16))
        for ref in layers:
            t = Image.fromarray(self.texture(ref)).resize((16, 16), Image.NEAREST)
            out.alpha_composite(t)
        return out.resize((ICON, ICON), Image.NEAREST)

    def render(self, item_id):
        kind, textures, elements, tex = self.resolve(f"{MOD_ID}:item/{item_id}")
        if kind == "flat" or (kind == "cross" and not elements) or not elements:
            layers = [textures[k] for k in sorted(textures) if k.startswith("layer") and textures[k]]
            if not layers:
                any_ref = textures.get("cross") or textures.get("particle") or next((v for v in textures.values() if v), None)
                layers = [any_ref] if any_ref else []
            return self.flat(layers)
        img = np.zeros((ICON, ICON, 4), np.float32)
        depth = np.full((ICON, ICON), -1e9)
        shade = {"up": 1.0, "north": 0.8, "west": 0.6, "south": 0.8, "east": 0.6, "down": 0.5}
        axis = {"up": (1, 1), "down": (1, 0), "north": (2, 0), "south": (2, 1), "west": (0, 0), "east": (0, 1)}
        normal = {"up": (0, 1, 0), "down": (0, -1, 0), "north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0)}
        for el in elements:
            lo, hi = np.array(el["from"], float), np.array(el["to"], float)
            for face, spec in el.get("faces", {}).items():
                if (self.R @ np.array(normal[face]))[2] <= 1e-6:
                    continue
                ref = tex(spec.get("texture"))
                if not ref:
                    continue
                a, side = axis[face]
                c = (hi if side else lo)[a]
                d = (c - 8 - self.A[..., a]) / self.B[a]
                p = self.A + d[..., None] * self.B + 8
                inside = np.ones(d.shape, bool)
                for k in range(3):
                    if k != a:
                        inside &= (p[..., k] >= lo[k] - 1e-6) & (p[..., k] <= hi[k] + 1e-6)
                inside &= d >= depth - 1e-4  # later elements win on the same plane (ore overlays)
                if not inside.any():
                    continue
                x, y, z = p[..., 0], p[..., 1], p[..., 2]
                u, v, ar = {"up": (x, z, (lo[0], lo[2], hi[0], hi[2])), "down": (x, 16 - z, (lo[0], 16 - hi[2], hi[0], 16 - lo[2])),
                            "north": (16 - x, 16 - y, (16 - hi[0], 16 - hi[1], 16 - lo[0], 16 - lo[1])),
                            "south": (x, 16 - y, (lo[0], 16 - hi[1], hi[0], 16 - lo[1])),
                            "west": (z, 16 - y, (lo[2], 16 - hi[1], hi[2], 16 - lo[1])),
                            "east": (16 - z, 16 - y, (16 - hi[2], 16 - hi[1], 16 - lo[2], 16 - lo[1]))}[face]
                if "uv" in spec:
                    U = spec["uv"]
                    su = (u - ar[0]) / max(ar[2] - ar[0], 1e-6)
                    sv = (v - ar[1]) / max(ar[3] - ar[1], 1e-6)
                    u, v = U[0] + su * (U[2] - U[0]), U[1] + sv * (U[3] - U[1])
                t = self.texture(ref)
                h, w = t.shape[:2]
                ti = np.clip((v / 16 * h).astype(int), 0, h - 1)
                tj = np.clip((u / 16 * w).astype(int), 0, w - 1)
                px = t[ti, tj].astype(np.float32)
                inside &= px[..., 3] > 0
                px[..., :3] *= shade[face]
                img[inside] = px[inside]
                depth[inside] = d[inside]
        return Image.fromarray(img.clip(0, 255).astype(np.uint8))


# ---------------------------------------------------------------- sheets
def layout(entries, cols):
    """entries: list of (section_title, [(key, Image)]). Returns cells with grid row/col and pixel rect at 1:1."""
    cells, row, y = [], 0, 0
    for title, items in entries:
        if not items:
            continue
        cw = max(i.width for _, i in items)
        ch = max(i.height for _, i in items)
        ncol = max(1, cols * 16 // cw)
        for n, (key, img) in enumerate(items):
            r, c = divmod(n, ncol)
            cells.append({"key": key, "section": title, "row": row + r + 1, "col": c + 1,
                          "x": 1 + c * (cw + 2), "y": y + 1 + r * (ch + 2), "w": img.width, "h": img.height,
                          "cw": cw, "ch": ch, "img": img})
        nrows = (len(items) + ncol - 1) // ncol
        row += nrows + 1
        y += nrows * (ch + 2) + ch + 2
    return cells


def raw_sheet(cells):
    W = max(c["x"] + c["cw"] + 1 for c in cells)
    H = max(c["y"] + c["ch"] + 1 for c in cells)
    sheet = Image.new("RGBA", (W, H))
    for c in cells:
        sheet.paste(c["img"], (c["x"], c["y"]))
    return sheet


def legend_sheet(cells, label, title, zoom_px=64, cell_w=150, cell_h=104):
    f_name, f_key, f_head, f_title = font(12, True), font(10), font(18, True), font(26, True)
    margin_l, top = 46, 70
    sections, order = {}, []
    for c in cells:
        if c["section"] not in sections:
            sections[c["section"]] = []
            order.append(c["section"])
        sections[c["section"]].append(c)
    max_col = max(c["col"] for c in cells)
    W = margin_l + max_col * cell_w + 10
    y = top
    pos = {}
    for s in order:
        y += 34
        rows = sorted({c["row"] for c in sections[s]})
        for r in rows:
            pos[r] = y
            y += cell_h
    H = y + 20
    img = Image.new("RGB", (W, H), (245, 243, 238))
    d = ImageDraw.Draw(img)
    d.text((12, 14), title, fill=(30, 30, 30), font=f_title)
    for c in range(1, max_col + 1):
        d.text((margin_l + (c - 1) * cell_w + cell_w // 2 - 10, top - 18), f"C{c}", fill=(120, 120, 120), font=f_key)
    checker = Image.new("RGB", (zoom_px, zoom_px), (255, 255, 255))
    cd = ImageDraw.Draw(checker)
    for i in range(0, zoom_px, 8):
        for j in range(0, zoom_px, 8):
            if (i // 8 + j // 8) % 2:
                cd.rectangle([i, j, i + 7, j + 7], fill=(225, 225, 225))
    seen_rows = set()
    for s in order:
        first = min(c["row"] for c in sections[s])
        d.text((12, pos[first] - 30), s, fill=(140, 60, 20), font=f_head)
        for c in sections[s]:
            x0, y0 = margin_l + (c["col"] - 1) * cell_w, pos[c["row"]]
            if c["row"] not in seen_rows:
                seen_rows.add(c["row"])
                d.text((6, y0 + 26), f"L{c['row']}", fill=(120, 120, 120), font=f_key)
            d.rectangle([x0 + 2, y0 + 2, x0 + cell_w - 3, y0 + cell_h - 3], outline=(210, 205, 195))
            im = c["img"]
            k = max(1, min((cell_w - 8) // im.width, zoom_px // im.height))
            big = im.resize((im.width * k, im.height * k), Image.NEAREST) if k > 1 else im
            bx = x0 + (cell_w - big.width) // 2
            img.paste(checker.resize(big.size, Image.NEAREST), (bx, y0 + 5))
            img.paste(big, (bx, y0 + 5), big)
            for i, (text, f, col) in enumerate(label(c)):
                if d.textlength(text, font=f) > cell_w - 8:
                    while d.textlength(text + "…", font=f) > cell_w - 8 and len(text) > 3:
                        text = text[:-1]
                    text += "…"
                d.text((x0 + cell_w // 2, y0 + 5 + zoom_px + 4 + i * 15), text, fill=col, font=f, anchor="mt")
    return img


# ---------------------------------------------------------------- themes and batches (lotes)
CREATIVE_TABS = ROOT / "src" / "main" / "resources" / MOD_ID / "creative_tabs.json"
THEMES = [("elements", "elementos"), ("solid_substances", "substancias-solidas"), ("liquid_substances", "substancias-liquidas"),
          ("gas_substances", "gases"), ("materials", "materiais"), ("ores_rocks", "minerios-rochas-solos"),
          ("trees", "arvores-madeira"), ("shrubs", "arbustos"), ("crops", "plantas-cultivaveis"), ("fauna", "animais"),
          ("food_medicine", "alimentos-remedios-efeitos"), ("machines", "maquinas"), ("power_logistics", "energia-logistica"),
          ("science", "ciencia"), ("tools", "ferramentas"), ("combat", "combate-armaduras"), ("exploration", "exploracao"),
          ("creative", "testes-criativo"), (None, "outros")]
LOT_MIN, LOT_MAX = 5, 10
LOT_SIDE = 512      # enlarged copies in a batch: longest side close to this (integer zoom, pixels stay sharp)
BOARD_CELL = 256    # one cell of a batch board (prancha)
BOARD_GAP = 32
BOARD_BG = (127, 127, 127, 255)
MAGENTA = (255, 0, 255)
KEY_DIST = 110      # colours this close to magenta become transparent on import (blurry edges included)


def on_magenta(img):
    """The texture over a solid magenta background: image AIs keep a flat colour better than real transparency."""
    bg = Image.new("RGBA", img.size, MAGENTA + (255,))
    bg.alpha_composite(img)
    return bg


def is_animated(rel):
    f = TEXTURES / rel
    if not Path(str(f) + ".mcmeta").exists():
        return False
    w, h = Image.open(f).size
    return h > w and h % w == 0


def theme_of(rel, tabs, ids_by_len):
    sec, stem = rel.rsplit("/", 1)[0], rel.rsplit("/", 1)[1][:-4]
    if sec == "entity/fauna":
        return "fauna"
    if sec == "mob_effect":
        return "food_medicine"
    if sec == "models/armor":
        return "combat"
    if stem in tabs:
        return tabs[stem]
    for i in ids_by_len:
        if stem.startswith(i + "_"):
            return tabs[i]
    if "_bush_stage" in stem:
        return "shrubs"
    if "_crop_stage" in stem:
        return "crops"
    if stem.startswith("ore_overlay_"):
        return "ores_rocks"
    return None


def game_name(stem, nm, keys=None):
    """In-game name of a texture: the item with the same name, or the item whose name starts it (\"forno (front_on)\")."""
    if stem in nm:
        return nm[stem]
    for k in keys or sorted(nm, key=len, reverse=True):
        if stem.startswith(k + "_"):
            return f"{nm[k]} ({stem[len(k) + 1:]})"
    return ""


def common_prefix(a, b):
    n = 0
    while n < min(len(a), len(b)) and a[n] == b[n]:
        n += 1
    return n


def chunk(stems):
    """Split a sorted list into runs of LOT_MIN..LOT_MAX, cutting where neighbouring names differ most."""
    out, i = [], 0
    while i < len(stems):
        rem = len(stems) - i
        if rem <= LOT_MAX:
            out.append(stems[i:])
            break
        lo, hi = max(LOT_MIN, rem - LOT_MAX) if rem < LOT_MAX + LOT_MIN else LOT_MIN, min(LOT_MAX, rem - LOT_MIN)
        best = max(range(lo, hi + 1), key=lambda c: (-common_prefix(stems[i + c - 1], stems[i + c]), c))
        out.append(stems[i:i + best])
        i += best
    return out


def lot_names(rels):
    """File name used in the batches: the texture's own name, with its folder only if two folders share a name."""
    stems = {}
    for rel in rels:
        stems.setdefault(rel.rsplit("/", 1)[1], []).append(rel)
    out = {}
    for name, rs in stems.items():
        for rel in rs:
            out[rel] = name if len(rs) == 1 else rel.replace("/", "__")
    return out


def batches(rels):
    data = json.loads(CREATIVE_TABS.read_text(encoding="utf-8")) if CREATIVE_TABS.exists() else {"items": {}}
    tabs = data["items"]
    ids_by_len = sorted(tabs, key=len, reverse=True)
    by_theme = {}
    for rel in rels:
        if is_animated(rel):
            continue
        by_theme.setdefault(theme_of(rel, tabs, ids_by_len), []).append(rel)
    out = []
    for n, (tid, folder) in enumerate(THEMES, 1):
        items = sorted(by_theme.get(tid, []), key=lambda r: (r.rsplit("/", 1)[1], r))
        uv = [r for r in items if r.startswith(("entity/", "models/"))]  # unfolded 3D models get their own batches
        parts = chunk([r for r in items if r not in uv]) + chunk(uv)
        for k, part in enumerate(parts, 1):
            out.append((f"{n:02d}-{folder}", f"lote-{k:03d}_{part[0].rsplit('/', 1)[1][:-4][:28]}", part))
    return out


def zoom_for(img, side):
    return max(1, side // max(img.size))


def board(imgs, magenta=False):
    """One picture with every texture of a batch enlarged on a grid, for sending a whole batch as one image."""
    n = len(imgs)
    cols = math.ceil(math.sqrt(n))
    rows = math.ceil(n / cols)
    W, H = cols * BOARD_CELL + (cols + 1) * BOARD_GAP, rows * BOARD_CELL + (rows + 1) * BOARD_GAP
    out = Image.new("RGBA", (W, H), BOARD_BG)
    cells = []
    for i, (rel, img) in enumerate(imgs):
        r, c = divmod(i, cols)
        k = zoom_for(img, BOARD_CELL)
        big = img.resize((img.width * k, img.height * k), Image.NEAREST)
        x = BOARD_GAP + c * (BOARD_CELL + BOARD_GAP) + (BOARD_CELL - big.width) // 2
        y = BOARD_GAP + r * (BOARD_CELL + BOARD_GAP) + (BOARD_CELL - big.height) // 2
        out.paste(on_magenta(big) if magenta else Image.new("RGBA", big.size), (x, y))
        if not magenta:
            out.paste(big, (x, y))  # transparent where the texture is transparent
        cells.append({"key": rel, "x": x, "y": y, "w": big.width, "h": big.height})
    return out, {"about": "Onde cada textura está em prancha.png (pixels). Não editar.", "width": W, "height": H, "cells": cells}


# ---------------------------------------------------------------- export / import
def export(out, vanilla):
    out = Path(out)
    if out.exists():
        shutil.rmtree(out)
    nm = names()
    rels = texture_list()
    for rel in rels:
        (out / "texturas" / rel).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(TEXTURES / rel, out / "texturas" / rel)
        mc = Path(str(TEXTURES / rel) + ".mcmeta")
        if mc.exists():
            shutil.copy2(mc, Path(str(out / "texturas" / rel) + ".mcmeta"))
    print(f"  {len(rels)} texturas")

    icons = Icons(vanilla)
    (out / "icones-3d").mkdir(parents=True)
    item_ids = sorted(f.stem for f in (ASSETS / "models" / "item").glob("*.json"))
    icon_imgs = []
    for i in item_ids:
        im = icons.render(i)
        im.save(out / "icones-3d" / f"{i}.png")
        icon_imgs.append((i, im))
    print(f"  {len(item_ids)} ícones 3D" + (f" ({len(icons.missing)} texturas vanilla não encontradas)" if icons.missing else ""))

    fd = out / "folhas"
    fd.mkdir()
    entries = [(title, [(rel, Image.open(TEXTURES / rel).convert("RGBA")) for rel in rels if rel.rsplit("/", 1)[0] == sec])
               for sec, title in SECTIONS]
    cells = layout(entries, 40)
    sheet = raw_sheet(cells)
    sheet.save(fd / "folha-texturas.png")
    manifest = {"about": "Posição de cada textura em folha-texturas.png (x, y, largura, altura em pixels). Não editar.",
                "width": sheet.width, "height": sheet.height,
                "cells": [{k: c[k] for k in ("key", "row", "col", "x", "y", "w", "h")} for c in cells]}
    (fd / "folha-texturas.json").write_text(json.dumps(manifest, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")

    def tex_label(c):
        stem = c["key"].rsplit("/", 1)[1][:-4]
        lines = [(f"L{c['row']} C{c['col']}  {stem}", font(10), (60, 60, 60))]
        if stem in nm:
            lines.append((nm[stem], font(11, True), (20, 20, 20)))
        return lines
    legend_sheet(cells, tex_label, "Elementar: todas as texturas (L = linha, C = coluna de folha-texturas.png)").save(fd / "folha-texturas-legenda.png")

    icells = layout([("Ícones de inventário (somente para ver; edite as texturas)", icon_imgs)], 40 * ICON // 16)
    legend_sheet(icells, lambda c: [(c["key"], font(10), (60, 60, 60))] + ([(nm[c["key"]], font(11, True), (20, 20, 20))] if c["key"] in nm else []),
                 "Elementar: ícones de inventário em 3D").save(fd / "folha-icones-3d.png")

    with open(fd / "indice.csv", "w", newline="", encoding="utf-8-sig") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["linha", "coluna", "arquivo", "nome no jogo", "largura", "altura"])
        for c in cells:
            w.writerow([c["row"], c["col"], "texturas/" + c["key"], nm.get(c["key"].rsplit("/", 1)[1][:-4], ""), c["w"], c["h"]])

    # batches of 5-10 enlarged textures by theme, on magenta, ready to send to an image AI
    ln = lot_names(rels)
    lots = batches(rels)
    nkeys = sorted(nm, key=len, reverse=True)
    ld = out / "lotes"
    with _open_csv(ld / "indice-lotes.csv") as (f, w):
        w.writerow(["tema", "lote", "arquivo no lote", "nome no jogo", "tamanho original", "ampliação", "textura no mod"])
        for theme, lot, part in lots:
            d = ld / theme / lot
            (d / "prancha").mkdir(parents=True, exist_ok=True)
            imgs, lines, kinds = [], [], set()
            for rel in part:
                img = Image.open(TEXTURES / rel).convert("RGBA")
                k = zoom_for(img, LOT_SIDE)
                on_magenta(img.resize((img.width * k, img.height * k), Image.NEAREST)).save(d / ln[rel])
                imgs.append((rel, img))
                game = game_name(rel.rsplit("/", 1)[1][:-4], nm, nkeys)
                w.writerow([theme, lot, ln[rel], game, f"{img.width}x{img.height}", f"{k}x", "texturas/" + rel])
                lines.append(f"{ln[rel]:<44} {img.width}x{img.height} (ampliada {k}x)  {game}")
                kinds.add(kind_of(rel, img))
            b, bman = board(imgs, magenta=True)
            b.save(d / "prancha" / "prancha.png")
            (d / "prancha" / "prancha.json").write_text(json.dumps(bman, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
            (d / "lista.txt").write_text(f"{theme} / {lot}\n\n" + "\n".join(lines)
                                         + "\n\nprancha/prancha.png: as mesmas texturas numa imagem só, na ordem acima (da esquerda para a direita, de cima para baixo).\n",
                                         encoding="utf-8")
            (d / "pedido-chatgpt.txt").write_text(prompt(imgs, nm, nkeys, kinds, bman), encoding="utf-8")
    anim = sum(1 for r in rels if is_animated(r))
    print(f"  {len(lots)} lotes em {len({t for t, _, _ in lots})} temas ({anim} animações ficam fora: seguem as texturas paradas)")
    return len(rels), len(item_ids)


def kind_of(rel, img):
    sec = rel.rsplit("/", 1)[0]
    if sec in ("entity/fauna", "models/armor"):
        return "uv"
    if sec == "block" and np.array(img)[..., 3].min() == 255:
        return "tile"
    return "sprite"


KIND_TEXT = {
    "tile": "As texturas opacas são faces de bloco: devem encaixar lado a lado sem emenda visível (seamless) e ocupar a casa inteira, sem fundo magenta.",
    "sprite": "Os itens e plantas são figuras recortadas: o fundo em volta deles é magenta liso #FF00FF (é a transparência). Pode mudar o formato da figura.",
    "uv": "Atenção: texturas de animal e de armadura são mapas desdobrados de um modelo 3D. Cada região é uma face do modelo: mude cores e detalhes, mas não mova as regiões nem mude seus contornos.",
}


def prompt(imgs, nm, nkeys, kinds, bman):
    names_ = [game_name(rel.rsplit("/", 1)[1][:-4], nm, nkeys) or rel.rsplit("/", 1)[1][:-4] for rel, _ in imgs]
    sizes = sorted({f"{im.width}x{im.height}" for _, im in imgs})
    cols = round((bman["width"] - BOARD_GAP) / (BOARD_CELL + BOARD_GAP))
    rows = round((bman["height"] - BOARD_GAP) / (BOARD_CELL + BOARD_GAP))
    txt = [f"Esta prancha tem {len(imgs)} texturas de um mod de Minecraft, ampliadas de {' / '.join(sizes)} pixels, numa grade de {cols}x{rows}.",
           "Da esquerda para a direita, de cima para baixo: " + "; ".join(names_) + ".",
           "Redesenhe cada uma com identidade própria (formato, cores, detalhes típicos do que ela é), no estilo clássico do Minecraft, "
           "em pixel art na resolução original (cada pixel da arte = um quadrado da grade ampliada).",
           *[KIND_TEXT[k] for k in ("tile", "sprite", "uv") if k in kinds],
           "Regras: mantenha a mesma grade, o mesmo tamanho de imagem e as mesmas posições; as faixas cinza entre as casas continuam cinza; "
           "sem antialiasing, sem sombra projetada, sem texto, sem moldura."]
    return "\n\n".join(txt) + "\n"


def pilot(out, stems, magenta=True):
    """One batch of chosen textures (by file name), for a first test with an image AI."""
    out = Path(out)
    rels = texture_list()
    by = {r.rsplit("/", 1)[1][:-4]: r for r in rels}
    nm = names()
    (out / "prancha").mkdir(parents=True, exist_ok=True)
    imgs, lines = [], []
    for st in stems:
        rel = by[st]
        img = Image.open(TEXTURES / rel).convert("RGBA")
        k = zoom_for(img, LOT_SIDE)
        big = img.resize((img.width * k, img.height * k), Image.NEAREST)
        (on_magenta(big) if magenta else big).save(out / f"{st}.png")
        imgs.append((rel, img))
        lines.append(f"{st + '.png':<34} {img.width}x{img.height}  {game_name(st, nm)}")
    b, man = board(imgs, magenta)
    b.save(out / "prancha" / "prancha.png")
    (out / "prancha" / "prancha.json").write_text(json.dumps(man, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    (out / "lista.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")


class _open_csv:
    def __init__(self, path):
        path.parent.mkdir(parents=True, exist_ok=True)
        self.f = open(path, "w", newline="", encoding="utf-8-sig")

    def __enter__(self):
        return self.f, csv.writer(self.f, delimiter=";")

    def __exit__(self, *a):
        self.f.close()


def fit(img, orig, notes, where, keep_alpha=False, magenta=False):
    """Bring a returned picture back to the texture's own size and transparency.

    Larger or blurry pictures are reduced with nearest neighbour (each texel takes the colour at the centre of its
    cell). Transparency: a picture that came back fully opaque, or keep_alpha, takes the original's alpha; when the
    original only has fully clear / fully solid pixels, the returned alpha is snapped to 0 or 255."""
    img = img.convert("RGBA")
    ow, oh = orig.size
    if img.size != (ow, oh):
        if abs(img.width / img.height - ow / oh) > 0.03 * ow / oh:
            notes.append(f"proporção diferente ({img.width}x{img.height} para {ow}x{oh}), foi esticada: {where}")
        img = img.resize((ow, oh), Image.NEAREST)
    a = np.array(img)
    o = np.array(orig.convert("RGBA"))
    untouched = np.all(a == np.array(on_magenta(orig.convert("RGBA"))), axis=-1) if magenta else np.zeros(a.shape[:2], bool)
    rgb = a[..., :3].astype(int)
    key = ((rgb - np.array(MAGENTA)) ** 2).sum(-1) < KEY_DIST ** 2
    if magenta and o[..., 3].min() < 255 and key.any():  # solid magenta background = transparent
        a[key] = 0
        # edge pixels tinted by the magenta background (purple/pink fringe): drop them where they touch the background
        mg = np.minimum(rgb[..., 0], rgb[..., 2]) - rgb[..., 1]
        orgb, vis = o[..., :3].astype(int), o[..., 3] > 0
        omg = np.minimum(orgb[..., 0], orgb[..., 2]) - orgb[..., 1]
        # a texture whose own art is pink or lilac (spodumene, effect badges) keeps those colours: no fringe cleanup
        own_strong = ((omg > 50) & (np.abs(orgb[..., 0] - orgb[..., 2]) < 50) & vis).any()
        own_tint = ((omg > 20) & (np.minimum(orgb[..., 0], orgb[..., 2]) > 110) & vis).any()
        strong = (mg > 80) & (np.abs(rgb[..., 0] - rgb[..., 2]) < 50) & ~key  # clearly magenta, even inside (gaps in leaves)
        if not own_strong:
            a[strong] = 0
        # only light pinks/purples: dark purple is real art (acai berries), a fringe of the bright background is light
        tint = (mg > 20) & (np.minimum(rgb[..., 0], rgb[..., 2]) > 110) & (a[..., 3] > 0) & (not own_tint)
        for _ in range(3):
            clear = a[..., 3] == 0
            near = np.zeros_like(clear)
            near[1:] |= clear[:-1]; near[:-1] |= clear[1:]; near[:, 1:] |= clear[:, :-1]; near[:, :-1] |= clear[:, 1:]
            drop = tint & near
            if not drop.any():
                break
            a[drop] = 0
            tint &= ~drop
    oa = np.array(orig.convert("RGBA"))[..., 3]
    if keep_alpha or (a[..., 3].min() == 255 and oa.min() < 255):
        a[..., 3] = oa
    elif set(np.unique(oa).tolist()) <= {0, 255}:
        a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
    a[untouched] = o[untouched]  # pixels the AI left as they were keep their exact colour and transparency
    return Image.fromarray(a)


def stem_index(rels):
    ln = lot_names(rels)
    idx = {name.lower(): rel for rel, name in ln.items()}
    for rel in rels:  # also plain names when they are unique, and the full path with "__"
        idx.setdefault(rel.replace("/", "__").lower(), rel)
    return idx


def clean_name(name):
    """'copper_ore (1).png', 'copper_ore-editado.png' -> 'copper_ore.png' (copies made by Windows or by hand)."""
    import re
    stem = name
    while stem.lower().endswith(".png"):  # "x.png.png" from a download that added the extension again
        stem = stem[:-4]
    stem = re.sub(r"\s*\(\d+\)$", "", stem)
    stem = re.sub(r"[-_ ](editad[oa]|edit|novo|nova|new)$", "", stem, flags=re.I)
    return stem + ".png"


def detect_cells(img):
    """Boxes of the filled cells of a board picture, found from the grey bands between them, in reading order.
    Lets a board come back with another grid (2x5 instead of 3x3) and still be cut right."""
    a = np.array(img.convert("RGB")).astype(int)
    grey = (np.abs(a - np.array(BOARD_BG[:3])).max(-1) < 18)

    def spans(frac):
        inside = frac < 0.85
        out, start = [], None
        for i, v in enumerate(list(inside) + [False]):
            if v and start is None:
                start = i
            elif not v and start is not None:
                if i - start > 8:
                    out.append((start, i))
                start = None
        return out
    rows, cols = spans(grey.mean(1)), spans(grey.mean(0))
    boxes = []
    for y0, y1 in rows:
        for x0, x1 in cols:
            if grey[y0:y1, x0:x1].mean() < 0.85:
                boxes.append((x0, y0, x1, y1))
    return boxes, len(rows), len(cols)


def lot_boards(lots_dir):
    """Batch name -> its prancha.json, so a picture named after its batch can be cut without its folder."""
    out = {}
    if lots_dir:
        for j in Path(lots_dir).glob("*/lote-*/prancha/prancha.json"):
            lot, theme = j.parent.parent.name, j.parent.parent.parent.name
            out[f"{theme}__{lot}".lower()] = j
            out.setdefault(lot.lower(), j)
    return out


def collect_edits(src, keep_alpha=False, lots_dir=None):
    src = Path(src)
    boards = lot_boards(lots_dir)
    rels = texture_list()
    current = {rel: Image.open(TEXTURES / rel) for rel in rels}
    idx = stem_index(rels)
    found, notes = {}, []  # rel -> (priority, img, origin); lower priority wins

    def offer(rel, img, origin, prio):
        img = fit(img, current[rel], notes, origin, keep_alpha, magenta=prio in (2, 3))
        if same(img, current[rel]):
            return
        if rel in found:
            p, other, o2 = found[rel]
            if not same(img, other):
                winner = origin if prio <= p else o2
                notes.append(f"editada em dois lugares ({o2} e {origin}), vale {winner}: {rel}")
            if prio > p:  # same priority: the newer file (read later) wins
                return
        found[rel] = (prio, img, origin)

    for f in sorted(src.rglob("*.png"), key=lambda f: (f.stat().st_mtime, str(f))):
        parts = f.relative_to(src).parts
        if "icones-3d" in parts or "folhas" in parts:
            continue
        bj = f.parent / "prancha.json"
        if not bj.exists():
            bj = boards.get(clean_name(f.name)[:-4].lower())
        if bj is not None and bj.exists():
            man = json.loads(bj.read_text(encoding="utf-8"))
            s = Image.open(f).convert("RGBA")
            sx, sy = s.width / man["width"], s.height / man["height"]
            boxes, nr, nc = detect_cells(s)
            exp_cols = round((man["width"] - BOARD_GAP) / (BOARD_CELL + BOARD_GAP))
            square = all(0.6 < (x1 - x0) / max(1, y1 - y0) < 1.6 for x0, y0, x1, y1 in boxes)
            regrid = len(boxes) == len(man["cells"]) and (nc != exp_cols or (square and abs(sx - sy) > 0.03 * sx))
            if abs(sx - sy) > 0.03 * sx and not regrid:
                notes.append(f"prancha com proporção diferente ({s.width}x{s.height}), foi esticada: {f.relative_to(src)}")
            if regrid and nc != exp_cols:
                notes.append(f"prancha voltou com outra grade ({nc}x{nr}); cortada pela ordem das casas: {f.relative_to(src).as_posix()}")
            elif len(boxes) not in (0, len(man["cells"])) and nc != exp_cols:
                notes.append(f"prancha com {len(boxes)} casas em vez de {len(man['cells'])}: confira o resultado de {f.relative_to(src).as_posix()}")
            for i, c in enumerate(man["cells"]):
                if c["key"] not in current:
                    continue
                if regrid:  # the texture's place inside its own cell, scaled to the detected cell
                    cx0 = c["x"] - (BOARD_CELL - c["w"]) // 2
                    cy0 = c["y"] - (BOARD_CELL - c["h"]) // 2
                    x0, y0, x1, y1 = boxes[i]
                    kx, ky = (x1 - x0) / BOARD_CELL, (y1 - y0) / BOARD_CELL
                    box = (round(x0 + (c["x"] - cx0) * kx), round(y0 + (c["y"] - cy0) * ky),
                           round(x0 + (c["x"] - cx0 + c["w"]) * kx), round(y0 + (c["y"] - cy0 + c["h"]) * ky))
                else:
                    box = (round(c["x"] * sx), round(c["y"] * sy), round((c["x"] + c["w"]) * sx), round((c["y"] + c["h"]) * sy))
                offer(c["key"], s.crop(box), f"prancha {f.relative_to(src).as_posix()}", 3)
            continue
        if "texturas" in parts:
            rel = "/".join(parts[parts.index("texturas") + 1:])
            prio = 1
        else:
            rel = idx.get(clean_name(f.name).lower())
            prio = 2
        if rel not in current:
            notes.append(f"ignorado (não é textura do mod): {f.relative_to(src).as_posix()}")
            continue
        offer(rel, Image.open(f), f.relative_to(src).as_posix(), prio)

    sheet, man = src / "folhas" / "folha-texturas.png", src / "folhas" / "folha-texturas.json"
    if sheet.exists():
        cells = json.loads(man.read_text(encoding="utf-8")) if man.exists() else None
        if cells is None:
            notes.append("folha-texturas.json ausente: a folha não pôde ser lida")
        else:
            s = Image.open(sheet).convert("RGBA")
            W, H = cells.get("width"), cells.get("height")
            if W and (s.width, s.height) != (W, H):
                s = s.resize((W, H), Image.NEAREST)
            for c in cells["cells"]:
                if c["key"] in current:
                    offer(c["key"], s.crop((c["x"], c["y"], c["x"] + c["w"], c["y"] + c["h"])), f"folha L{c['row']} C{c['col']}", 4)

    edits = {rel: (img, origin) for rel, (p, img, origin) in found.items()}
    edits.update(propagate_animations(edits, current, rels))
    return edits, notes


def propagate_animations(edits, current, rels):
    """A running machine's animation is drawn over its still texture. When the still texture changed, every frame
    takes the new pixels wherever it still showed the old still texture; the moving part stays as it was."""
    statics = {r.rsplit("/", 1)[1][:-4]: r for r in rels if r in edits and not is_animated(r)}
    out = {}
    for rel in rels:
        if rel in edits or not is_animated(rel):
            continue
        sec, stem = rel.rsplit("/", 1)[0], rel.rsplit("/", 1)[1][:-4]
        base = max((s for s, r in statics.items() if r.rsplit("/", 1)[0] == sec and stem.startswith(s + "_")), key=len, default=None)
        if base is None:
            continue
        old = norm(current[statics[base]])
        new = np.array(edits[statics[base]][0].convert("RGBA"))
        strip = norm(current[rel])
        w = strip.shape[1]
        if old.shape != (w, w, 4):
            continue
        frames = strip.reshape(-1, w, w, 4)
        keep = np.all(frames == old[None], axis=-1)
        if keep[0].mean() < 0.4:
            continue
        frames = np.where(keep[..., None], new[None], frames)
        out[rel] = (Image.fromarray(frames.reshape(strip.shape).astype(np.uint8)), f"animação refeita a partir de {base}.png")
    return out


def apply_overrides(assets=ASSETS, overrides=OVERRIDES):
    """Copy art/texture_overrides/** over the generated textures. Called at the end of tools/generate.py."""
    n = 0
    if overrides.is_dir():
        for f in overrides.rglob("*.png"):
            dst = assets / "textures" / f.relative_to(overrides)
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(f, dst)
            n += 1
    return n


def compare(edits, path):
    """Before / after picture of every imported texture (8x), to check the result."""
    if not edits:
        return
    rows = sorted(edits.items())
    tile, pad, lab = 128, 10, 16
    W = 2 * tile + 3 * pad + 260
    H = len(rows) * (tile + pad) + pad + 30
    img = Image.new("RGB", (W, H), (245, 243, 238))
    d = ImageDraw.Draw(img)
    d.text((pad, 8), "antes  |  depois", fill=(30, 30, 30), font=font(14, True))
    for i, (rel, (new, origin)) in enumerate(rows):
        y = 30 + i * (tile + pad)
        for j, im in enumerate((Image.open(TEXTURES / rel).convert("RGBA"), new.convert("RGBA"))):
            fr = im.crop((0, 0, im.width, min(im.height, im.width))) if im.height > im.width else im
            k = max(1, tile // max(fr.size))
            big = fr.resize((fr.width * k, fr.height * k), Image.NEAREST)
            x = pad + j * (tile + pad)
            img.paste((200, 200, 200), (x, y, x + big.width, y + big.height))
            img.paste(big, (x, y), big)
        d.text((2 * tile + 3 * pad, y + 4), rel, fill=(30, 30, 30), font=font(11, True))
        d.text((2 * tile + 3 * pad, y + 4 + lab), origin[:44], fill=(90, 90, 90), font=font(10))
    img.save(path)


def do_import(src, dry, keep_alpha=False, compare_png=None, lots_dir=None):
    edits, notes = collect_edits(src, keep_alpha, lots_dir)
    lines = [f"{len(edits)} texturas alteradas:"]
    if compare_png:
        compare(edits, compare_png)
    for rel, (img, origin) in sorted(edits.items()):
        lines.append(f"  {rel}  ({img.width}x{img.height}, de {origin})")
        if not dry:
            for base in (OVERRIDES, TEXTURES):
                (base / rel).parent.mkdir(parents=True, exist_ok=True)
                img.save(base / rel)
    lines += notes
    print("\n".join(lines))
    return edits


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    e = sub.add_parser("export")
    e.add_argument("out")
    e.add_argument("--vanilla", help="folder holding assets/minecraft/textures (from the Minecraft client jar)")
    i = sub.add_parser("import")
    i.add_argument("src")
    i.add_argument("--dry-run", action="store_true")
    i.add_argument("--keep-alpha", action="store_true", help="always use the original transparency")
    i.add_argument("--compare", help="write a before/after picture here")
    i.add_argument("--lotes", help="the pack's lotes/ folder: pictures named <tema>__<lote>.png are cut with that batch's grid")
    pl = sub.add_parser("pilot")
    pl.add_argument("out")
    pl.add_argument("stems", nargs="+")
    a = ap.parse_args()
    if a.cmd == "export":
        export(a.out, a.vanilla)
    elif a.cmd == "pilot":
        pilot(a.out, a.stems)
    else:
        do_import(a.src, a.dry_run, a.keep_alpha, a.compare, a.lotes)


if __name__ == "__main__":
    sys.exit(main())
