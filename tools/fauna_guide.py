"""Field Guide chapter "Animals of the World" (3.50.0, fauna stage 1) in English and nine languages.

"@N " sets a paragraph's level (a heading takes the next paragraph's). The translations carry no prefix; the
English one is copied onto them, so headings and levels line up."""
from pathlib import Path

GUIDE = [l for l in (Path(__file__).with_name("fauna_guide_en.txt").read_text(encoding="utf-8").splitlines()) if l.strip()]

T = {"animals": "Animals of the World"}
T_I18N = {
    "pt_br": {"animals": "Animais do Mundo"}, "es_es": {"animals": "Animales del mundo"},
    "fr_fr": {"animals": "Animaux du monde"}, "it_it": {"animals": "Animali del mondo"},
    "de_de": {"animals": "Tiere der Welt"}, "nl_nl": {"animals": "Dieren van de wereld"},
    "sv_se": {"animals": "Världens djur"}, "da_dk": {"animals": "Verdens dyr"},
    "ru_ru": {"animals": "Животные мира"},
}

GUIDE_I18N = {}
for _code in T_I18N:
    _paras = [l for l in Path(__file__).with_name(f"fauna_guide_{_code}.txt").read_text(encoding="utf-8").splitlines() if l.strip()]
    assert len(_paras) == len(GUIDE), (_code, len(_paras), len(GUIDE))
    assert [p.startswith("# ") for p in _paras] == [p.split(" ", 1)[1].startswith("# ") for p in GUIDE], (_code, "headings")
    GUIDE_I18N[_code] = [en.split(" ", 1)[0] + " " + t for en, t in zip(GUIDE, _paras)]
