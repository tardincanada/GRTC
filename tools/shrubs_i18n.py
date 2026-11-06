"""Translations for tools/shrubs.py: one module tools/shrubs_<code>.py per language with

NAMES {shrub: (name, block name, cutting name)}, PRODUCTS {product: name}, ABOUT {shrub: (about, range)},
SUBSTANCES {substance: (name, use)}, REACTIONS {reaction: description}, RESEARCH {project: (name, description)},
UI (list, in the order of ui_english()), GUIDE (list, like shrubs.GUIDE) and T (guide templates and chapter title).
"""
import csv
import importlib
from pathlib import Path

import shrubs as S

CODES = ("pt_br", "fr_fr", "es_es", "nl_nl", "it_it", "de_de", "sv_se", "da_dk", "ru_ru")
NEW_SUBSTANCES = ("jojoba_wax", "vegetable_wax", "bixin", "lawsone", "linalool", "caffeine", "capsaicin", "emetine",
                  "pilocarpine", "ndga")
NEW_REACTIONS = ("jojoba_pressing", "wax_boiling", "bixin_dyeing", "lawsone_dyeing", "linalool_distillation",
                 "caffeine_extraction", "capsaicin_extraction", "emetine_extraction", "pilocarpine_extraction",
                 "ndga_extraction")
NEW_RESEARCH = ("heated_greenhouse", "alkaloids", "food_antioxidants")
# 3.22.0, stages 2 and 3: in tools/shrubs23_<code>.py.
NEW_SUBSTANCES_23 = ("geraniol", "arbutin", "vitamin_c")
NEW_REACTIONS_23 = ("geraniol_distillation", "vitamin_c_extraction", "arbutin_extraction")
NEW_RESEARCH_23 = ("cold_greenhouse", "vitamins")
# Reaction names, in the order of CODES: pt_br, fr_fr, es_es, nl_nl, it_it, de_de, sv_se, da_dk, ru_ru.
REACTION_NAMES = {
    "jojoba_pressing": ("Prensagem da jojoba", "Pressage du jojoba", "Prensado de jojoba", "Jojoba persen", "Spremitura della jojoba",
                        "Jojobapressung", "Jojobapressning", "Jojobapresning", "Отжим жожобы"),
    "wax_boiling": ("Fervura da cera", "Bouillage de la cire", "Hervido de la cera", "Was koken", "Bollitura della cera",
                    "Wachskochen", "Vaxkokning", "Vokskogning", "Вываривание воска"),
    "bixin_dyeing": ("Tingimento com bixina", "Teinture à la bixine", "Tinte con bixina", "Verven met bixine",
                     "Tintura con bissina", "Färben mit Bixin", "Färgning med bixin", "Farvning med bixin", "Крашение биксином"),
    "lawsone_dyeing": ("Tingimento com lausona", "Teinture à la lawsone", "Tinte con lawsona", "Verven met lawson",
                       "Tintura con lawsone", "Färben mit Lawson", "Färgning med lawson", "Farvning med lawson",
                       "Крашение лавсоном"),
    "linalool_distillation": ("Destilação do linalol", "Distillation du linalol", "Destilación del linalool",
                              "Linalooldestillatie", "Distillazione del linalolo", "Linalooldestillation",
                              "Linalooldestillation", "Linalooldestillation", "Перегонка линалоола"),
    "caffeine_extraction": ("Extração da cafeína", "Extraction de la caféine", "Extracción de la cafeína", "Cafeïne-extractie",
                            "Estrazione della caffeina", "Koffeingewinnung", "Koffeinextraktion", "Koffeinudvinding",
                            "Извлечение кофеина"),
    "capsaicin_extraction": ("Extração da capsaicina", "Extraction de la capsaïcine", "Extracción de la capsaicina",
                             "Capsaïcine-extractie", "Estrazione della capsaicina", "Capsaicingewinnung",
                             "Capsaicinextraktion", "Capsaicinudvinding", "Извлечение капсаицина"),
    "emetine_extraction": ("Extração da emetina", "Extraction de l'émétine", "Extracción de la emetina", "Emetine-extractie",
                           "Estrazione dell'emetina", "Emetingewinnung", "Emetinextraktion", "Emetinudvinding",
                           "Извлечение эметина"),
    "pilocarpine_extraction": ("Extração da pilocarpina", "Extraction de la pilocarpine", "Extracción de la pilocarpina",
                               "Pilocarpine-extractie", "Estrazione della pilocarpina", "Pilocarpingewinnung",
                               "Pilokarpinextraktion", "Pilocarpinudvinding", "Извлечение пилокарпина"),
    "ndga_extraction": ("Extração do NDGA", "Extraction du NDGA", "Extracción del NDGA", "NDGA-extractie", "Estrazione dell'NDGA",
                        "NDGA-Gewinnung", "NDGA-extraktion", "NDGA-udvinding", "Извлечение НДГК"),
    # 3.22.0
    "geraniol_distillation": ("Destilação do geraniol", "Distillation du géraniol", "Destilación del geraniol",
                              "Geranioldestillatie", "Distillazione del geraniolo", "Geranioldestillation",
                              "Geranioldestillation", "Geranioldestillation", "Перегонка гераниола"),
    "vitamin_c_extraction": ("Extração da vitamina C", "Extraction de la vitamine C", "Extracción de la vitamina C",
                             "Vitamine C-extractie", "Estrazione della vitamina C", "Vitamin-C-Gewinnung",
                             "C-vitaminextraktion", "C-vitaminudvinding", "Извлечение витамина C"),
    "arbutin_extraction": ("Extração da arbutina", "Extraction de l'arbutine", "Extracción de la arbutina",
                           "Arbutine-extractie", "Estrazione dell'arbutina", "Arbutingewinnung", "Arbutinextraktion",
                           "Arbutinudvinding", "Извлечение арбутина"),
}
ROOT = Path(__file__).resolve().parent.parent


def _rows(name, key="id"):
    with (ROOT / "data" / name).open(newline="", encoding="utf-8") as f:
        return {r[key]: r for r in csv.DictReader(f)}


def ui_english():
    return [v for k, v in S.LANG.items() if k not in S.LANG23] + [S.HEATER_NAME, S.HEATER_DESC]


def ui23_english():
    """3.22.0 (stages 2 and 3): the new interface lines, then the cooler."""
    return list(S.LANG23.values()) + [S.COOLER_NAME, S.COOLER_DESC]


def _m1(code):
    return importlib.import_module(f"shrubs_{code}")


def _m23(code):
    return importlib.import_module(f"shrubs23_{code}")


class _Merged:
    """Stage 1 module and stages 2-3 module of one language, seen as one."""

    def __init__(self, code):
        self.a, self.b = _m1(code), _m23(code)
        for k in ("NAMES", "PRODUCTS", "ABOUT", "SUBSTANCES", "REACTIONS", "RESEARCH", "T"):
            setattr(self, k, {**getattr(self.a, k), **getattr(self.b, k)})
        self.GUIDE = self.a.GUIDE + self.b.GUIDE
        self.UI = self.a.UI
        self.UI23 = self.b.UI


def _m(code):
    return _Merged(code)


class _Lazy(dict):
    def __init__(self, attr):
        super().__init__()
        self.attr = attr

    def __missing__(self, code):
        v = getattr(_m(code), self.attr)
        self[code] = v
        return v


GUIDE = type("G", (dict,), {"__missing__": lambda self, code: self.setdefault(code, {"SHRUBS": _m(code).GUIDE})})()
T = _Lazy("T")


def text(code):
    m = _m(code)
    out = {}
    ui = ui_english()
    assert len(m.UI) == len(ui), (code, len(m.UI), len(ui))
    out.update(zip(ui, m.UI))
    sp = S.species()
    assert set(m.NAMES) >= set(sp) and set(m.ABOUT) >= set(sp), code
    for sid, s in sp.items():
        name, block, cutting = m.NAMES[sid]
        out[s["name"]] = name
        out[s["block"]] = block
        out[f"{s['name']} Cutting"] = cutting
        about, rng = m.ABOUT[sid]
        out[s["about"]] = about
        out[s["range"]] = rng
        if s["product"]:
            out[S.PRODUCTS[s["product"]][0]] = m.PRODUCTS[s["product"]]
    subs = _rows("substances.csv")
    assert len(m.UI23) == len(ui23_english()), (code, len(m.UI23), len(ui23_english()))
    out.update(zip(ui23_english(), m.UI23))
    for tid, t in S.TEAS.items():
        out[t[0]] = m.PRODUCTS[tid]
    for sid in NEW_SUBSTANCES + NEW_SUBSTANCES_23:
        name, use = m.SUBSTANCES[sid]
        out[subs[sid]["name"]] = name
        out[subs[sid]["use"]] = use
    reactions = _rows("reactions.csv")
    for rid in NEW_REACTIONS + NEW_REACTIONS_23:
        out[reactions[rid]["description"]] = m.REACTIONS[rid]
    research = _rows("research.csv")
    for pid in NEW_RESEARCH + NEW_RESEARCH_23:
        name, desc = m.RESEARCH[pid]
        out[research[pid]["name"]] = name
        out[research[pid]["description"]] = desc
    for rid in NEW_REACTIONS + NEW_REACTIONS_23:
        out[rid.replace("_", " ").capitalize().replace("Ndga", "NDGA").replace("Vitamin c", "Vitamin C")] = \
            REACTION_NAMES[rid][CODES.index(code)]
    assert len(m.GUIDE) == len(S.GUIDE), code
    return out
