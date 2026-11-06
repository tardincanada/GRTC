"""Animal models and skins (3.50.0): each body template builds cubes (Minecraft entity model space: pixels, y down,
the ground at y 24, the head toward -z) in named parts with an animation role; the skin is painted on the cubes' box UV.

The runtime file fauna_models.json lists, per species: texture size, render scale, and parts
{name, parent, pivot (relative to the parent), rot, role, cubes [{o, s, uv}]}. The client builds the model from it
(client/FaunaModel) and animates each part by its role.
"""
import math
import random

from PIL import Image


def _hex(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def _mix(a, b, t):
    return tuple(round(a[i] * (1 - t) + b[i] * t) for i in range(3))


def _shade(c, f):
    return tuple(max(0, min(255, round(v * f))) for v in c)


class Builder:
    def __init__(self):
        self.parts = []  # dicts with absolute pivot

    def part(self, name, pivot, role=None, rot=(0, 0, 0), parent=None):
        p = dict(name=name, pivot=list(pivot), rot=list(rot), role=role, parent=parent, cubes=[])
        self.parts.append(p)
        return p

    @staticmethod
    def cube(part, o, s, key, **kw):
        s = [max(1, round(v)) if v >= 1 else 1 for v in s]
        part["cubes"].append(dict(o=[float(v) for v in o], s=s, key=key, **kw))


# ------------------------------------------------------------------ templates

def quad(b, p):
    bl, bw, bh, ll, lw = p.get("bl", 16), p.get("bw", 8), p.get("bh", 8), p.get("ll", 10), p.get("lw", 3)
    top = 24 - ll - bh
    body = b.part("body", (0, top + bh / 2, 0), "body")
    b.cube(body, (-bw / 2, -bh / 2, -bl / 2), (bw, bh, bl), "body", belly=True)
    if p.get("shaggy"):
        b.cube(body, (-bw / 2 - 0.5, bh / 2 - 1, -bl / 2 + 1), (bw + 1, 3, bl - 2), "body", belly=False, fringe=True)
    if p.get("hump"):
        b.cube(body, (-bw / 2 + 1, -bh / 2 - 2, -bl / 2 + 1), (bw - 2, 2, bl // 3), "body")
    if p.get("rump"):
        pass
    fz, bz = -bl / 2 + lw / 2 + 1, bl / 2 - lw / 2 - 1
    lx = bw / 2 - lw / 2 - 0.5
    for name, x, z, role in (("leg_fl", -lx, fz, "leg_a"), ("leg_fr", lx, fz, "leg_b"), ("leg_bl", -lx, bz, "leg_b"),
                             ("leg_br", lx, bz, "leg_a")):
        leg = b.part(name, (x, 24 - ll, z), role)
        b.cube(leg, (-lw / 2, 0, -lw / 2), (lw, ll, lw), "leg")
        if p.get("claws") and z == fz:
            b.cube(leg, (-lw / 2, ll - 1, -lw / 2 - 2), (lw, 1, 2), "claw")
    nl, nw = p.get("nl", 0), p.get("nw", 4)
    hl, hw, hh = p.get("hl", 6), p.get("hw", 6), p.get("hh", 5)
    sl, sw, sh = p.get("sl", 0), p.get("sw", 3), p.get("sh", 3)
    head = b.part("head", (0, top + bh * 0.35, -bl / 2 + 1), "head")
    if nl > 0:
        b.cube(head, (-nw / 2, -nl, -nw / 2 - 1), (nw, nl + 2, nw), "neck", belly=True)
        if p.get("mane"):
            b.cube(head, (-0.5, -nl - 1, -nw / 2 + nw - 1), (1, nl + 2, 2), "mane")
    elif p.get("mane"):
        b.cube(body, (-1, -bh / 2 - 1, -bl / 2), (2, 1, bl - 3), "mane")
    hy = -nl - hh + (1 if nl > 0 else hh * 0.5)
    hz = -nw / 2 - 1 - hl if nl > 0 else -hl + 1
    b.cube(head, (-hw / 2, hy, hz), (hw, hh, hl), "head", eyes=True)
    if sl > 0:
        b.cube(head, (-sw / 2, hy + hh - sh, hz - sl), (sw, sh, sl), "snout", nose=True)
    ew, eh = p.get("ew", 1), p.get("eh", 2)
    if ew > 0:
        for sx in (-1, 1):
            ex = sx * (hw / 2 - ew / 2) - ew / 2
            b.cube(head, (ex + sx * 0.5, hy - eh, hz + hl - 2), (ew, eh, 1), "ear")
            if p.get("tufts"):
                b.cube(head, (ex + sx * 0.5, hy - eh - 2, hz + hl - 2), (1, 2, 1), "ear")
    horn, hs = p.get("horn"), p.get("hs", 6)
    if horn == "antler":
        for sx in (-1, 1):
            x0 = sx * (hw / 2 - 1) - 0.5
            b.cube(head, (x0, hy - hs, hz + hl - 3), (1, hs, 1), "horn")
            b.cube(head, (x0 + sx * 1, hy - hs, hz + hl - 3), (1, 1, 1), "horn")
            b.cube(head, (x0 + sx * 2, hy - hs - 2, hz + hl - 3), (1, 2, 1), "horn")
            b.cube(head, (x0, hy - hs * 0.6, hz + hl - 5), (1, 1, 2), "horn")
            b.cube(head, (x0, hy - hs * 0.35, hz + hl - 5), (1, 1, 2), "horn")
            b.cube(head, (x0 + sx * 1, hy - hs + 2, hz + hl - 2), (1, 1, 1), "horn")
    elif horn == "palmate":
        for sx in (-1, 1):
            b.cube(head, (sx * hw / 2 - (0 if sx > 0 else 3), hy, hz + hl - 3), (3, 1, 1), "horn")
            b.cube(head, (sx * (hw / 2 + 2) - (0 if sx > 0 else 6), hy - 2, hz + hl - 6), (6, 1, 6), "horn")
            for k in range(3):
                b.cube(head, (sx * (hw / 2 + 2 + 2 * k) - (0 if sx > 0 else 1), hy - 4, hz + hl - 6 + 2 * k), (1, 2, 1), "horn")
    elif horn == "horns":
        for sx in (-1, 1):
            b.cube(head, (sx * hw / 2 - (0 if sx > 0 else 3), hy + 1, hz + 2), (3, 1, 1), "horn")
            b.cube(head, (sx * (hw / 2 + 2) - (0 if sx > 0 else 1), hy - 2, hz + 2), (1, 3, 1), "horn")
    elif horn == "tusks":
        for sx in (-1, 1):
            b.cube(head, (sx * (sw / 2) - (0 if sx > 0 else 1), hy + hh - sh - 1, hz - sl + 1), (1, 2, 1), "horn")
    tl = p.get("tl", 6)
    if tl > 0:
        tw, th = p.get("tw", 1), p.get("th", 1)
        tail = b.part("tail", (0, top + 1, bl / 2 - 0.5), "tail", rot=(-p.get("tdrop", 0.6) if tl > 3 else -1.2, 0, 0))
        b.cube(tail, (-tw / 2, 0, 0), (tw, th, tl), "tail")
        if p.get("tiptail"):
            b.cube(tail, (-tw / 2 - 0.5, -0.5, tl - 1), (tw + 1, th + 1, 2), "tip")


def bird(b, p):
    bl, bw, bh, ll, lw = p.get("bl", 8), p.get("bw", 5), p.get("bh", 5), p.get("ll", 3), p.get("lw", 1)
    top = 24 - ll - bh
    body = b.part("body", (0, top + bh / 2, 0), "body")
    b.cube(body, (-bw / 2, -bh / 2, -bl / 2), (bw, bh, bl), "body", belly=True)
    for name, x, role in (("leg_l", -bw / 4 - 0.5, "leg_a"), ("leg_r", bw / 4 + 0.5, "leg_b")):
        leg = b.part(name, (x, 24 - ll, 0.5), role)
        b.cube(leg, (-lw / 2, 0, -lw / 2), (lw, ll, lw), "leg")
        b.cube(leg, (-1, ll - 1, -2), (2 if lw < 2 else lw + 1, 1, 3), "leg")
    wl, wh = p.get("wl", 6), p.get("wh", 4)
    for name, sx, role in (("wing_l", -1, "wing_l"), ("wing_r", 1, "wing_r")):
        w = b.part(name, (sx * bw / 2, top + 1, -bl / 2 + 1), role)
        b.cube(w, (-0.5 if sx > 0 else -0.5, 0, 0), (1, wh, wl), "wing")
    tl, tw, th = p.get("tl", 3), p.get("tw", 3), p.get("th", 1)
    tail = b.part("tail", (0, top + 1, bl / 2 - 1), "tail", rot=(0.35 if tl < 8 else 0.15, 0, 0))
    b.cube(tail, (-tw / 2, 0, 0), (tw, th, tl), "tail")
    nl, nw = p.get("nl", 0), p.get("nw", 2)
    hl, hw, hh = p.get("hl", 3), p.get("hw", 3), p.get("hh", 3)
    head = b.part("head", (0, top + 1, -bl / 2 + 1), "head")
    if nl > 0:
        b.cube(head, (-nw / 2, -nl, -nw / 2 - 1), (nw, nl + 1, nw), "neck")
        if p.get("collar", True):
            pass
    hy = -nl - hh + 1
    hz = -hl - 1 if nl > 0 else -hl - 1
    b.cube(head, (-hw / 2, hy, hz), (hw, hh, hl), "head", eyes=True)
    bkl, bkw, bkh = p.get("bkl", 2), p.get("bkw", 1), p.get("bkh", 1)
    b.cube(head, (-bkw / 2, hy + (hh - bkh) * 0.6, hz - bkl), (bkw, bkh, bkl), "beak", tipped=True)


def reptile(b, p):
    bl, bw, bh, ll, lw = p.get("bl", 16), p.get("bw", 7), p.get("bh", 4), p.get("ll", 3), p.get("lw", 2)
    top = 24 - ll - bh + 1
    body = b.part("body", (0, top + bh / 2, 0), "body")
    b.cube(body, (-bw / 2, -bh / 2, -bl / 2), (bw, bh, bl), "body", belly=True, scutes=True)
    fz, bz = -bl / 2 + 2, bl / 2 - 2
    for name, sx, z, role in (("leg_fl", -1, fz, "sleg_a"), ("leg_fr", 1, fz, "sleg_b"), ("leg_bl", -1, bz, "sleg_b"),
                              ("leg_br", 1, bz, "sleg_a")):
        leg = b.part(name, (sx * (bw / 2), 24 - ll, z), role)
        b.cube(leg, (0 if sx > 0 else -lw, 0, -lw / 2), (lw, ll, lw), "leg")
        b.cube(leg, (0 if sx > 0 else -lw - 1, ll - 1, -lw / 2 - 1), (lw + 1, 1, lw + 1), "leg")
    hl, hw, hh, sl = p.get("hl", 5), p.get("hw", 6), p.get("hh", 3), p.get("sl", 6)
    head = b.part("head", (0, top + 1, -bl / 2), "head")
    b.cube(head, (-hw / 2, -1, -hl), (hw, hh, hl), "head", eyes=True)
    if sl > 0:
        b.cube(head, (-hw / 2 + 1, -1 + hh - max(2, hh - 1), -hl - sl), (hw - 2, max(2, hh - 1), sl), "snout", teeth=True)
    tl, tw = p.get("tl", 16), p.get("tw", 4)
    t1 = b.part("tail", (0, top + 1, bl / 2), "tail")
    b.cube(t1, (-tw / 2, -1, 0), (tw, max(2, bh - 1), tl / 2), "body", scutes=True)
    t2 = b.part("tail2", (0, 0, tl / 2), "tail2", parent="tail")
    b.cube(t2, (-tw / 4, 0, 0), (max(1, tw / 2), max(1, bh - 2), tl / 2), "body", scutes=True)


def frog(b, p):
    bl, bw, bh = p.get("bl", 5), p.get("bw", 4), p.get("bh", 3)
    body = b.part("body", (0, 24 - bh / 2 - 1, 0), "body")
    b.cube(body, (-bw / 2, -bh / 2, -bl / 2), (bw, bh, bl), "body", belly=True)
    head = b.part("head", (0, 24 - bh - 1, -bl / 2 + 1), "head")
    b.cube(head, (-bw / 2, 0, -3), (bw, 2, 3), "body", belly=True)
    for sx in (-1, 1):
        b.cube(head, (sx * (bw / 2 - 1) - 0.5, -1, -2), (1, 1, 1), "eye")
    for name, sx, z, ln, role in (("leg_fl", -1, -bl / 2 + 1, 1, "sleg_a"), ("leg_fr", 1, -bl / 2 + 1, 1, "sleg_b"),
                                  ("leg_bl", -1, bl / 2 - 1, 2, "sleg_b"), ("leg_br", 1, bl / 2 - 1, 2, "sleg_a")):
        leg = b.part(name, (sx * bw / 2, 23, z), role)
        b.cube(leg, (0 if sx > 0 else -ln, 0, -ln / 2 - 0.5), (ln, 1, ln + 1), "leg")


def scorpion(b, p):
    bl, bw, bh = p.get("bl", 6), p.get("bw", 4), p.get("bh", 2)
    body = b.part("body", (0, 24 - bh - 1, 0), "body")
    b.cube(body, (-bw / 2, 0, -bl / 2), (bw, bh, bl), "body", belly=True)
    for i in range(4):
        z = -bl / 2 + 1 + i * (bl - 2) / 3
        for sx, role in ((-1, "sleg_a" if i % 2 else "sleg_b"), (1, "sleg_b" if i % 2 else "sleg_a")):
            leg = b.part(f"leg{i}{'l' if sx < 0 else 'r'}", (sx * bw / 2, 24 - bh, z), role, rot=(0, 0, sx * 0.5))
            b.cube(leg, (0 if sx > 0 else -3, 0, -0.5), (3, 1, 1), "leg")
    for sx, nm in ((-1, "claw_l"), (1, "claw_r")):
        c = b.part(nm, (sx * (bw / 2 - 1), 24 - bh - 0.5, -bl / 2), "arm", rot=(0, sx * -0.4, 0))
        b.cube(c, (-0.5, 0, -3), (1, 1, 3), "leg")
        b.cube(c, (-1, -0.5, -5), (2, 2, 2), "claw")
    prev, py = None, None
    segs = [(1.5, 2), (1.5, 2), (1.5, 2), (1.5, 2)]
    t = b.part("tail", (0, 24 - bh - 0.5, bl / 2), "stinger", rot=(0.9, 0, 0))
    b.cube(t, (-1, -1, 0), (2, 2, 2), "body")
    parent = "tail"
    for i in range(3):
        s = b.part(f"tail{i + 2}", (0, 0, 2), "stinger", rot=(0.75, 0, 0), parent=parent)
        b.cube(s, (-1, -1, 0), (2, 2, 2), "body")
        parent = f"tail{i + 2}"
    st = b.part("sting", (0, 0, 2), None, rot=(0.9, 0, 0), parent=parent)
    b.cube(st, (-0.5, -0.5, 0), (1, 1, 2), "sting")


def locust(b, p):
    bl = p.get("bl", 6)
    body = b.part("body", (0, 22, 0), "body")
    b.cube(body, (-1, -1, -bl / 2 + 1), (2, 2, bl), "body", belly=True)
    head = b.part("head", (0, 21.5, -bl / 2 + 1), "head")
    b.cube(head, (-1, -1, -2), (2, 2, 2), "body", eyes=True)
    b.cube(head, (-1, -3, -2), (1, 2, 1), "leg")
    b.cube(head, (0, -3, -2), (1, 2, 1), "leg")
    for sx, role in ((-1, "wing_l"), (1, "wing_r")):
        w = b.part("wing" + str(sx), (sx * 0.5, 20.5, -bl / 2 + 2), role)
        b.cube(w, (-1 if sx < 0 else 0, 0, 0), (1, 1, bl), "wing")
    for i, (z, ln) in enumerate(((-bl / 2 + 2, 2), (-bl / 2 + 3, 2), (0.5, 4))):
        for sx in (-1, 1):
            leg = b.part(f"leg{i}{sx}", (sx, 22.5, z), "sleg_a" if (i + (sx > 0)) % 2 else "sleg_b", rot=(0, 0, sx * 0.6))
            b.cube(leg, (0 if sx > 0 else -ln, 0, -0.5), (ln, 1, 1), "leg")


def crab(b, p):
    body = b.part("body", (0, 22, 0), "body")
    b.cube(body, (-3, -1, -2), (6, 2, 4), "shell", belly=True)
    for sx in (-1, 1):
        b.cube(body, (sx * 1.5 - 0.5, -2, -2), (1, 1, 1), "eye")
        c = b.part("claw" + str(sx), (sx * 3, 22, -1.5), "arm", rot=(0, sx * 0.5, 0))
        b.cube(c, (-1, -1, -3), (2, 2, 3), "claw")
        for i in range(3):
            leg = b.part(f"leg{i}{sx}", (sx * 3, 22.5, -0.5 + i), "sleg_a" if (i + (sx > 0)) % 2 else "sleg_b", rot=(0, 0, sx * 0.4))
            b.cube(leg, (0 if sx > 0 else -3, 0, -0.5), (3, 1, 1), "leg")


def horseshoe(b, p):
    body = b.part("body", (0, 23, 0), "body")
    b.cube(body, (-4, -1, -5), (8, 1, 6), "shell", belly=True)
    b.cube(body, (-3, -2, -4), (6, 1, 4), "shell")
    b.cube(body, (-3, -1, 1), (6, 1, 3), "shell")
    b.cube(body, (-2, -2, -4), (1, 1, 1), "eye")
    tail = b.part("tail", (0, 22.5, 4), "tail")
    b.cube(tail, (-0.5, -0.5, 0), (1, 1, 7), "tail")
    for i in range(3):
        for sx in (-1, 1):
            leg = b.part(f"leg{i}{sx}", (sx * 2, 23.5, -3 + 2 * i), "sleg_a" if (i + (sx > 0)) % 2 else "sleg_b")
            b.cube(leg, (-0.5, 0, -0.5), (1, 1, 1), "belly")


def fish(b, p):
    bl, bw, bh = p.get("bl", 8), p.get("bw", 2), p.get("bh", 3)
    y = 24 - bh / 2 - 0.5
    front = b.part("body", (0, y, 0), "body")
    fl = round(bl * 0.55)
    b.cube(front, (-bw / 2, -bh / 2, -fl), (bw, bh, fl), "body", belly=True, eyes_front=True, scutes=bool(p.get("scutes")))
    if p.get("snout"):
        b.cube(front, (-bw / 4, -bh / 2 + bh / 3, -fl - p["snout"]), (max(1, bw / 2), max(1, bh / 3), p["snout"]), "body")
    b.cube(front, (-0.5, -bh / 2 - max(1, bh // 3), -fl / 2), (1, max(1, bh // 3), max(2, fl // 3)), "fin")
    for sx in (-1, 1):
        b.cube(front, (sx * bw / 2 - (0 if sx > 0 else 1), bh / 4, -fl + max(1, fl // 4)), (1, 1, max(2, bl // 6)), "fin")
    tail = b.part("tail", (0, y, 0), "fin_tail")
    tl = bl - fl
    b.cube(tail, (-bw / 2 + (0.5 if bw > 2 else 0), -bh / 2 + 0.5, 0), (max(1, bw - 1), max(1, bh - 1), tl), "body", belly=True)
    if p.get("shark"):
        b.cube(tail, (-0.5, -bh, tl - 1), (1, bh, 3), "fin")
        b.cube(tail, (-0.5, 0, tl - 1), (1, bh // 2, 2), "fin")
    else:
        b.cube(tail, (-0.5, -bh / 2 - 1, tl), (1, bh + 2, max(2, bl // 6)), "fin")


def cephalopod(b, p):
    body = b.part("body", (0, 21, 0), "body")
    b.cube(body, (-3, -1.5, -3), (6, 3, 9), "body", belly=True)
    for sx in (-1, 1):
        f = b.part("fin" + str(sx), (sx * 3, 21, 1.5), "flipper_l" if sx < 0 else "flipper_r")
        b.cube(f, (0 if sx > 0 else -1, 0, -4.5), (1, 1, 9), "belly")
    head = b.part("head", (0, 21, -3), "head")
    b.cube(head, (-2.5, -1.5, -2), (5, 3, 2), "body", eyes=True)
    for i in range(8):
        a = b.part(f"arm{i}", (-1.75 + (i % 4), 20.5 + (i // 4), -5), "tentacle", rot=(0.1 * (i % 3), 0.15 * (i % 4 - 1.5), 0))
        b.cube(a, (-0.5, -0.5, -3 - (i % 2)), (1, 1, 3 + (i % 2)), "arm")


def jelly(b, p):
    bell = b.part("body", (0, 14, 0), "bell")
    b.cube(bell, (-4, -2, -4), (8, 4, 8), "body")
    b.cube(bell, (-3, -3, -3), (6, 1, 6), "body")
    b.cube(bell, (-4.5, 2, -4.5), (9, 1, 9), "ring")
    for i in range(12):
        ang = i / 12 * 2 * math.pi
        t = b.part(f"t{i}", (math.cos(ang) * 3.5, 16.5, math.sin(ang) * 3.5), "tentacle")
        b.cube(t, (-0.5, 0, -0.5), (1, 6 + (i % 3) * 2, 1), "tentacle")


def whale(b, p):
    # Built at 1/2.5 scale: a 44 px whale renders 6.9 blocks long.
    body = b.part("body", (0, 18, 0), "body")
    b.cube(body, (-5, -6, -6), (10, 11, 16), "body", belly=True, wrinkles=True)
    head = b.part("head", (0, 18, -6), "head")
    b.cube(head, (-5, -6, -16), (10, 11, 16), "body", eyes=True, wrinkles=True)
    b.cube(head, (-2, 5, -14), (4, 1, 13), "mouth")
    for sx in (-1, 1):
        f = b.part("flip" + str(sx), (sx * 5, 21, -3), "flipper_l" if sx < 0 else "flipper_r")
        b.cube(f, (0 if sx > 0 else -4, 0, -1), (4, 1, 3), "body")
    t1 = b.part("tail", (0, 18, 10), "fluke")
    b.cube(t1, (-3.5, -4, 0), (7, 8, 10), "body", belly=True)
    t2 = b.part("tail2", (0, 0, 10), "fluke", parent="tail")
    b.cube(t2, (-2, -2, 0), (4, 4, 6), "body")
    b.cube(t2, (-8, -0.5, 5), (16, 1, 5), "body")


def pinniped(b, p):
    bl, bw, bh = p.get("bl", 14), p.get("bw", 6), p.get("bh", 5)
    body = b.part("body", (0, 24 - bh / 2, 0), "body")
    b.cube(body, (-bw / 2, -bh / 2, -bl / 2), (bw, bh, bl * 0.6), "body", belly=True)
    b.cube(body, (-bw / 2 + 1, -bh / 2 + 1, -bl / 2 + bl * 0.6), (bw - 2, bh - 1, bl * 0.4), "body", belly=True)
    hl, hw, hh = p.get("hl", 5), p.get("hw", 5), p.get("hh", 4)
    head = b.part("head", (0, 24 - bh, -bl / 2 + 1), "head")
    b.cube(head, (-hw / 2, -hh + 2, -hl), (hw, hh, hl), "body", eyes=True, belly=True)
    if p.get("tusks"):
        b.cube(head, (-hw / 2 + 1, 0, -hl - 1), (hw - 2, 2, 2), "snout")
        for sx in (-1, 1):
            b.cube(head, (sx * 1.5 - 0.5, 2, -hl - 1), (1, 6, 1), "tusk")
    for sx in (-1, 1):
        f = b.part("flip" + str(sx), (sx * bw / 2, 23, -bl / 2 + 3), "flipper_l" if sx < 0 else "flipper_r")
        b.cube(f, (0 if sx > 0 else -3, 0, -1), (3, 1, 3), "body")
    t = b.part("tail", (0, 24 - bh / 2 + 1, bl / 2), "fluke")
    b.cube(t, (-bw / 2 + 0.5, -0.5, 0), (bw - 1, 1, 3), "body")


def snail(b, p):
    foot = b.part("body", (0, 23.5, 0), "body")
    b.cube(foot, (-1.5, -0.5, -2.5), (3, 1, 5), "foot")
    shell = b.part("shell", (0, 22, 0.5), None, rot=(-0.3, 0, 0))
    b.cube(shell, (-2, -1.5, -2), (4, 3, 4), "shell")
    b.cube(shell, (-1.5, -3, -1), (3, 2, 3), "shell")
    b.cube(shell, (-1, -4, 0), (2, 1, 2), "shell")
    for sx in (-1, 1):
        b.cube(shell, (sx * 2 - (0 if sx > 0 else 1), -1, -1), (1, 1, 1), "spine")
        b.cube(shell, (sx * 1.5 - (0 if sx > 0 else 1), -2.5, 0), (1, 1, 1), "spine")
    b.cube(shell, (-0.5, -0.5, -6), (1, 1, 4), "shell")


TEMPLATES = {"quad": quad, "bird": bird, "reptile": reptile, "frog": frog, "scorpion": scorpion, "locust": locust,
             "crab": crab, "horseshoe": horseshoe, "fish": fish, "cephalopod": cephalopod, "jelly": jelly,
             "whale": whale, "pinniped": pinniped, "snail": snail}

# ------------------------------------------------------------------ painting

FALLBACK = {"belly": "body", "head": "body", "neck": "head", "leg": "body", "snout": "head", "ear": "head",
            "tail": "body", "wing": "body", "mane": "body", "fin": "body", "shell": "body", "foot": "body", "claw": "leg",
            "arm": "body", "ring": "body", "tentacle": "body", "spine": "shell", "sting": "body", "tip": "tail",
            "tusk": "horn", "mouth": "belly"}
DEFAULTS = {"horn": "E8DCC0", "beak": "8A8478", "eye": "141414", "claw": "2A2420", "body": "808080"}


def color(pal, key):
    seen = set()
    k = key
    while k not in pal and k not in seen:
        seen.add(k)
        if k in FALLBACK:
            k = FALLBACK[k]
        else:
            break
    if k in pal:
        return _hex(pal[k])
    return _hex(DEFAULTS.get(key, DEFAULTS["body"]))


def pack(parts, width=64):
    cubes = [c for p in parts for c in p["cubes"]]
    order = sorted(cubes, key=lambda c: -(c["s"][2] + c["s"][1]))
    while True:
        x = y = row_h = 0
        ok = True
        for c in order:
            w, h, d = c["s"]
            cw, ch = 2 * d + 2 * w, d + h
            if cw > width:
                ok = False
                break
            if x + cw > width:
                x, y, row_h = 0, y + row_h, 0
            c["uv"] = [x, y]
            x += cw
            row_h = max(row_h, ch)
        if ok:
            height = y + row_h
            if height <= width * 2:
                break
        width *= 2
    th = 1
    while th < height:
        th *= 2
    return width, max(th, 16)


def paint(species, parts, size, winter=False):
    W, H = size
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()
    pal = dict(species["pal"])
    if winter:
        pal.update(body=pal["winter"], belly=pal["winter"], head=pal["winter"], leg=pal["winter"], tail=pal["winter"])
    pattern = species.get("pattern")
    rng = random.Random(species["id"] + ("w" if winter else ""))
    alpha = 150 if species["body"] == "jelly" else 255

    def put(x, y, c, a=alpha):
        if 0 <= x < W and 0 <= y < H:
            px[x, y] = tuple(c) + (a,)

    for p in parts:
        for c in p["cubes"]:
            w, h, d = c["s"]
            u, v = c["uv"]
            key = c["key"]
            base = color(pal, key)
            belly = color(pal, "belly") if c.get("belly") else base
            dorsal = _shade(base, 0.9)
            pat_keys = {"body", "head", "neck", "leg", "tail", "snout"}

            def pat(x, y, col, gx, gy):
                if pattern == "stripes" and key in pat_keys:
                    if (gx + (gy // 3) % 2) % 3 == 0:
                        return color(pal, "stripe")
                elif pattern == "spots" and key in pat_keys:
                    if rng.random() < 0.13:
                        return color(pal, "spot")
                elif pattern == "blotches" and key in pat_keys:
                    if ((gx // 2) * 7 + (gy // 2) * 3) % 5 in (0, 2) and rng.random() < 0.9:
                        return color(pal, "blotch")
                elif pattern == "spines" and key == "body":
                    if (gx + gy) % 2 == 0:
                        return color(pal, "tip")
                    return _shade(col, 0.8 + 0.3 * rng.random())
                elif pattern == "rings" and key == "body":
                    if (gx % 4, gy % 4) in ((1, 1), (2, 1), (1, 2)) and rng.random() < 0.6:
                        return color(pal, "ring")
                elif pattern == "algae" and key in ("body", "leg"):
                    if rng.random() < 0.25:
                        return _mix(col, (90, 130, 60), 0.5)
                elif pattern == "scales" and key in ("wing", "body") and not c.get("belly_px"):
                    if (gx + 2 * (gy % 2)) % 4 == 0:
                        return _shade(col, 1.25)
                elif pattern == "cuttle" and key == "body":
                    if (gx + gy // 2) % 4 == 0:
                        return color(pal, "stripe")
                elif pattern == "wrinkles" and key == "body":
                    if gy % 3 == 0 and rng.random() < 0.7:
                        return _shade(col, 0.85)
                return col

            def noisy(col):
                n = rng.randint(-7, 7)
                return tuple(max(0, min(255, v + n)) for v in col)

            # top (u+d, v, w x d)
            for x in range(w):
                for y in range(d):
                    put(u + d + x, v + y, noisy(pat(x, y, dorsal, x, y)))
            # bottom (u+d+w, v, w x d)
            for x in range(w):
                for y in range(d):
                    put(u + d + w + x, v + y, noisy(pat(x, y, belly, x, y) if not c.get("belly") else belly))
            # sides: right (u, v+d, d x h), front (u+d), left (u+d+w, d x h), back (u+2d+w, w x h)
            faces = ((u, d, "side_r"), (u + d, w, "front"), (u + d + w, d, "side_l"), (u + 2 * d + w, w, "back"))
            for fx, fw, fname in faces:
                for x in range(fw):
                    for y in range(h):
                        t = y / max(1, h - 1)
                        col = dorsal if t < 0.3 else base
                        if c.get("belly") and t > 0.66:
                            col = _mix(base, belly, 0.85)
                        col = pat(x, y, col, x + (0 if fname != "side_l" else 7), y)
                        if c.get("fringe") and y == h - 1 and x % 2 == 0:
                            put(fx + x, v + d + y, (0, 0, 0), 0)
                            continue
                        put(fx + x, v + d + y, noisy(col))
            eye = color(pal, "eye")
            if c.get("eyes"):
                ey = v + d + max(0, min(h - 1, h // 3))
                # sides: the front edge of the right side is its last column, of the left side its first
                put(u + d - 2 if d > 2 else u + d - 1, ey, eye)
                put(u + d + w + (1 if d > 2 else 0), ey, eye)
                if "eyering" in pal:
                    put(u + d - 3 if d > 3 else u, ey, color(pal, "eyering"))
                    put(u + d + w + 2 if d > 3 else u + d + w, ey, color(pal, "eyering"))
                if "throat" in pal:
                    for x in range(w):
                        for y in range(h // 2, h):
                            put(u + d + x, v + d + y, color(pal, "throat"))
                if "face" in pal:
                    for y in range(h // 3, min(h, h // 3 + 2)):
                        put(u + d - 1, v + d + y, color(pal, "face"))
                        put(u + d + w, v + d + y, color(pal, "face"))
                if "mask" in pal:
                    for x in range(max(0, d - 3), d):
                        put(u + x, ey, color(pal, "mask"))
                        put(u + d + w + d - 1 - x, ey, color(pal, "mask"))
            if c.get("eyes_front"):
                ey = v + d + max(0, h // 3)
                put(u + 1, ey, eye)
                put(u + 2 * d + w - 2, ey, eye)
                if "eye" in pal:
                    put(u + 1, ey, color(pal, "eye"))
            if c.get("nose"):
                for x in range(w):
                    put(u + d + x, v + d, _shade(base, 0.6))
                if w >= 3:
                    put(u + d + 0, v + d + 1, (25, 20, 18))
                    put(u + d + w - 1, v + d + 1, (25, 20, 18))
            if c.get("tipped") and "tip" in pal:
                for y in range(h):
                    put(u + d, v + d + y, color(pal, "tip"))
            if c.get("teeth"):
                for x in range(0, d, 2):
                    put(u + x, v + d + h - 1, (230, 226, 210))
                    put(u + d + w + x, v + d + h - 1, (230, 226, 210))
            if c.get("scutes") and "scute" in pal:
                for x in range(0, w, 2):
                    for y in range(0, d, 2):
                        put(u + d + x, v + y, color(pal, "scute"))
            if key == "eye":
                for x in range(w):
                    for y in range(h):
                        put(u + d + x, v + d + y, eye)
    if "collar" in pal or "rump" in pal or "speculum" in pal or "breast" in pal:
        # simple accents by part: collar ring on necks, chestnut breast, rump patch
        for p in parts:
            for c in p["cubes"]:
                w, h, d = c["s"]
                u, v = c["uv"]
                if c["key"] == "neck" and "collar" in pal:
                    for x in range(2 * d + 2 * w):
                        put(u + x, v + d + h - 1, color(pal, "collar"))
                if c["key"] == "body" and "breast" in pal and c.get("belly"):
                    for x in range(w):
                        for y in range(h):
                            put(u + d + x, v + d + y, color(pal, "breast"))
                if c["key"] == "body" and "rump" in pal and c.get("belly"):
                    for x in range(w):
                        for y in range(h):
                            put(u + 2 * d + w + x, v + d + y, color(pal, "rump"))
                if c["key"] == "wing" and "speculum" in pal:
                    for y in range(1, min(h, 3)):
                        for x in range(d // 2, d // 2 + 2):
                            put(u + x, v + d + y, color(pal, "speculum"))
                            put(u + d + w + x, v + d + y, color(pal, "speculum"))
    return img


def build(species):
    b = Builder()
    TEMPLATES[species["body"]](b, species["shape"])
    parts = b.parts
    size = pack(parts)
    # pivots relative to parents
    by = {p["name"]: p for p in parts}
    out = []
    for p in parts:
        piv = p["pivot"]
        if p["parent"]:
            pp = by[p["parent"]]
            # templates give child pivots already relative to the parent
            rel = piv
        else:
            rel = piv
        out.append(dict(name=p["name"], parent=p["parent"], pivot=[round(v, 3) for v in rel],
                        rot=[round(v, 4) for v in p["rot"]], role=p["role"],
                        cubes=[dict(o=[round(v, 3) for v in c["o"]], s=c["s"], uv=c["uv"]) for c in p["cubes"]]))
    return dict(tex=list(size), scale=species.get("scale", 1.0), parts=out), parts, size
