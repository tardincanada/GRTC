"""Isotopes (1.3.0): natural isotope abundances for the tooltip, and the ratios the game tracks per item.

NATURAL: element id -> [(mass number, atom %)], the stable or primordial isotopes above 0.1%, largest first
(IUPAC representative abundances, rounded). Radioactive elements keep their line from generate.ISOTOPES.
TRACKED: the elements whose items carry their own ratio of one isotope (a data component), because a process in
the game changes it: element -> (isotope, natural atom %, rounding step, what changes it).
REACTIONS: reaction id -> extra runtime fields:
  "stamp": {product: atom %}  the product comes out with this ratio, whatever went in.
  "separate": {"alpha": a, "fraction": f}  a calutron pass: the fraction f of the feed comes out with its
      isotope's odds multiplied by a; the rest comes out depleted by mass balance.
  "minRatio": {element: atom %}  the reaction only takes feed at least this enriched.
IRRADIATION: items the reactor makes and the ratio they get (plutonium from a short irradiation of uranium).
"""
import json

NATURAL = {
    "hydrogen": [(1, 99.98), (2, 0.016)], "helium": [(4, 99.9998)], "lithium": [(7, 92.4), (6, 7.6)],
    "beryllium": [(9, 100)], "boron": [(11, 80.1), (10, 19.9)], "carbon": [(12, 98.9), (13, 1.1)],
    "nitrogen": [(14, 99.6), (15, 0.4)], "oxygen": [(16, 99.76), (18, 0.2)], "fluorine": [(19, 100)],
    "neon": [(20, 90.5), (22, 9.25), (21, 0.27)], "sodium": [(23, 100)],
    "magnesium": [(24, 79.0), (26, 11.0), (25, 10.0)], "aluminium": [(27, 100)],
    "silicon": [(28, 92.2), (29, 4.7), (30, 3.1)], "phosphorus": [(31, 100)],
    "sulfur": [(32, 95.0), (34, 4.25), (33, 0.75)], "chlorine": [(35, 75.8), (37, 24.2)],
    "argon": [(40, 99.6), (36, 0.33)], "potassium": [(39, 93.3), (41, 6.7)],
    "calcium": [(40, 96.9), (44, 2.1), (42, 0.65), (48, 0.19)], "scandium": [(45, 100)],
    "titanium": [(48, 73.7), (46, 8.3), (47, 7.4), (49, 5.4), (50, 5.2)], "vanadium": [(51, 99.75), (50, 0.25)],
    "chromium": [(52, 83.8), (53, 9.5), (50, 4.3), (54, 2.4)], "manganese": [(55, 100)],
    "iron": [(56, 91.75), (54, 5.85), (57, 2.1), (58, 0.28)], "cobalt": [(59, 100)],
    "nickel": [(58, 68.1), (60, 26.2), (62, 3.6), (61, 1.1), (64, 0.9)], "copper": [(63, 69.2), (65, 30.8)],
    "zinc": [(64, 49.2), (66, 27.7), (68, 18.5), (67, 4.0), (70, 0.6)], "gallium": [(69, 60.1), (71, 39.9)],
    "germanium": [(74, 36.5), (72, 27.3), (70, 20.5), (73, 7.8), (76, 7.8)], "arsenic": [(75, 100)],
    "selenium": [(80, 49.6), (78, 23.8), (76, 9.4), (82, 8.7), (77, 7.6), (74, 0.9)],
    "bromine": [(79, 50.7), (81, 49.3)],
    "krypton": [(84, 57.0), (86, 17.3), (82, 11.6), (83, 11.5), (80, 2.3), (78, 0.35)],
    "rubidium": [(85, 72.2), (87, 27.8)], "strontium": [(88, 82.6), (86, 9.9), (87, 7.0), (84, 0.56)],
    "yttrium": [(89, 100)], "zirconium": [(90, 51.5), (94, 17.4), (92, 17.2), (91, 11.2), (96, 2.8)],
    "niobium": [(93, 100)],
    "molybdenum": [(98, 24.4), (96, 16.7), (95, 15.9), (92, 14.5), (100, 9.8), (97, 9.6), (94, 9.2)],
    "ruthenium": [(102, 31.6), (104, 18.6), (101, 17.1), (99, 12.8), (100, 12.6), (96, 5.5), (98, 1.9)],
    "rhodium": [(103, 100)],
    "palladium": [(106, 27.3), (108, 26.5), (105, 22.3), (110, 11.7), (104, 11.1), (102, 1.0)],
    "silver": [(107, 51.8), (109, 48.2)],
    "cadmium": [(114, 28.7), (112, 24.1), (111, 12.8), (110, 12.5), (113, 12.2), (116, 7.5), (106, 1.2), (108, 0.9)],
    "indium": [(115, 95.7), (113, 4.3)],
    "tin": [(120, 32.6), (118, 24.2), (116, 14.5), (119, 8.6), (117, 7.7), (124, 5.8), (122, 4.6), (112, 1.0),
            (114, 0.7), (115, 0.3)],
    "antimony": [(121, 57.2), (123, 42.8)],
    "tellurium": [(130, 34.1), (128, 31.7), (126, 18.8), (125, 7.1), (124, 4.7), (122, 2.6), (123, 0.9)],
    "iodine": [(127, 100)],
    "xenon": [(132, 26.9), (129, 26.4), (131, 21.2), (134, 10.4), (136, 8.9), (130, 4.1), (128, 1.9)],
    "caesium": [(133, 100)], "barium": [(138, 71.7), (137, 11.2), (136, 7.9), (135, 6.6), (134, 2.4)],
    "lanthanum": [(139, 99.9)], "cerium": [(140, 88.4), (142, 11.1), (138, 0.25), (136, 0.19)],
    "praseodymium": [(141, 100)],
    "neodymium": [(142, 27.2), (144, 23.8), (146, 17.2), (143, 12.2), (145, 8.3), (148, 5.8), (150, 5.6)],
    "samarium": [(152, 26.7), (154, 22.7), (147, 15.0), (149, 13.8), (148, 11.2), (150, 7.4), (144, 3.1)],
    "europium": [(153, 52.2), (151, 47.8)],
    "gadolinium": [(158, 24.8), (160, 21.9), (156, 20.5), (157, 15.7), (155, 14.8), (154, 2.2), (152, 0.2)],
    "terbium": [(159, 100)],
    "dysprosium": [(164, 28.3), (162, 25.5), (163, 24.9), (161, 18.9), (160, 2.3), (158, 0.1)],
    "holmium": [(165, 100)],
    "erbium": [(166, 33.5), (168, 27.0), (167, 22.9), (170, 14.9), (164, 1.6), (162, 0.14)],
    "thulium": [(169, 100)],
    "ytterbium": [(174, 32.0), (172, 21.7), (173, 16.1), (171, 14.1), (176, 12.8), (170, 3.0), (168, 0.1)],
    "lutetium": [(175, 97.4), (176, 2.6)],
    "hafnium": [(180, 35.1), (178, 27.3), (177, 18.6), (179, 13.6), (176, 5.3), (174, 0.16)],
    "tantalum": [(181, 99.99)], "tungsten": [(184, 30.6), (186, 28.4), (182, 26.5), (183, 14.3), (180, 0.12)],
    "rhenium": [(187, 62.6), (185, 37.4)],
    "osmium": [(192, 40.8), (190, 26.3), (189, 16.2), (188, 13.3), (187, 2.0), (186, 1.6)],
    "iridium": [(193, 62.7), (191, 37.3)],
    "platinum": [(195, 33.8), (194, 32.9), (196, 25.2), (198, 7.4), (192, 0.8)], "gold": [(197, 100)],
    "mercury": [(202, 29.9), (200, 23.1), (199, 16.9), (201, 13.2), (198, 10.0), (204, 6.9), (196, 0.15)],
    "thallium": [(205, 70.5), (203, 29.5)], "lead": [(208, 52.4), (206, 24.1), (207, 22.1), (204, 1.4)],
    "bismuth": [(209, 100)],
}

TRACKED = {
    "uranium": ("U-235", 0.72, 0.01, "Calutron enrichment"),
    "plutonium": ("Pu-240", 24.0, 0.1, "how long it stayed in the reactor"),
    "lithium": ("Li-6", 7.6, 0.01, "Calutron enrichment"),
    "boron": ("B-10", 19.9, 0.1, "Calutron enrichment"),
    "calcium": ("Ca-48", 0.19, 0.01, "the Calutron taking calcium-48 out"),
    "hydrogen": ("H-2", 0.016, 0.001, "nothing: heavy water comes out as its own substance"),
}

REACTIONS = {
    # Reactor-grade plutonium from spent fuel: about 24% Pu-240 after three years in a power reactor.
    "plutonium_reduction": {"stamp": {"plutonium": 24.0}},
    # The game's Ca-48 cut (1%, boosted for play) takes more than the 0.19% there is: the rest has none left.
    "calcium_48_separation": {"stamp": {"item:periodic_elements:calcium": 0.0}},
    "uranium_235_separation": {"separate": {"alpha": 20.0, "fraction": 0.05}},
    "lithium_6_separation": {"separate": {"alpha": 200.0, "fraction": 0.07}},
    "boron_10_separation": {"separate": {"alpha": 50.0, "fraction": 0.2}},
    "enriched_metal_fuel": {"minRatio": {"uranium": 3.0}},
    "control_rod_pressing": {"minRatio": {"boron": 90.0}},
}

# A short stay in the reactor makes plutonium with little Pu-240 (weapons grade is under 7%).
IRRADIATION = {"periodic_elements:plutonium": 6.0}
# Reactor targets that only change when enriched: item -> (element, minimum atom %, product).
# Li-6 + n -> He-4 + T: lithium-6 turns into tritium; lithium-7 hardly reacts.
IRRADIATION_IF = {"periodic_elements:lithium": ("lithium", 90.0, "periodic_elements:tritium_vial")}


def write_isotopes(runtime_dir, elements, reactions):
    """isotopes.json for Java; also adds the REACTIONS fields to reactions.json. Returns the tooltip lines."""
    by_id = {e["id"]: e for e in elements}
    assert set(NATURAL) <= set(by_id), set(NATURAL) - set(by_id)
    lines = {}
    for el, iso in NATURAL.items():
        total = sum(p for _, p in iso)
        assert 99.0 <= total <= 101.0, (el, total)
        assert iso == sorted(iso, key=lambda x: -x[1]), el
        sym = by_id[el]["symbol"]
        shown = iso[:4]
        text = ", ".join(f"{p:g}% {sym}-{a}" for a, p in shown)
        if len(iso) > 4:
            text += f" and {len(iso) - 4} more"
        lines[el] = f"100% {sym}-{iso[0][0]}" if len(iso) == 1 else text
    ids = {r["id"]: r for r in reactions}
    for rid, extra in REACTIONS.items():
        assert rid in ids, f"isotopes: unknown reaction {rid}"
        ids[rid].update(extra)
    (runtime_dir / "reactions.json").write_text(json.dumps(reactions, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    tracked = {el: {"isotope": i, "natural": n, "step": s, "changedBy": c} for el, (i, n, s, c) in TRACKED.items()}
    (runtime_dir / "isotopes.json").write_text(json.dumps({"tracked": tracked, "irradiation": IRRADIATION,
                                                          "irradiationIf": {k: {"element": e, "min": m, "product": p}
                                                                            for k, (e, m, p) in IRRADIATION_IF.items()}},
                                                          indent=1) + "\n", encoding="utf-8")
    return lines
