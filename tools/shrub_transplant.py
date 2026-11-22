"""Moving and multiplying shrubs (3.56.0, Guilherme): shovel root ball, cutting on break, one or two cuttings.

Code: src/main/java/com/periodicelements/shrubs/ShrubBlock.java, ShrubCuttingItem.java and Shrubs.java (shovel).
generate.py merges LANG into en_us and translations(code) into each language.
"""

M = "periodic_elements"

LANG = {
    f"gui.{M}.shrub.too_young": "Too young for a cutting: let it grow a little",
    f"item.{M}.shrub_root_ball": "%s (with root ball)",
    f"gui.{M}.shrub.root_ball": "Whole bush: replanted at the same size",
}

_K = list(LANG)

_T = {
    "pt_br": ["Jovem demais para estaca: deixe crescer um pouco", "%s (com torrão)",
              "Arbusto inteiro: volta no mesmo tamanho ao replantar"],
    "es_es": ["Demasiado joven para un esqueje: déjalo crecer un poco", "%s (con cepellón)",
              "Arbusto entero: se replanta con el mismo tamaño"],
    "fr_fr": ["Trop jeune pour une bouture : laissez-le pousser un peu", "%s (avec motte)",
              "Arbuste entier : replanté à la même taille"],
    "de_de": ["Zu jung für einen Steckling: lass ihn etwas wachsen", "%s (mit Wurzelballen)",
              "Ganzer Strauch: wird in gleicher Größe eingepflanzt"],
    "it_it": ["Troppo giovane per una talea: lascialo crescere un po'", "%s (con zolla)",
              "Arbusto intero: ripiantato della stessa grandezza"],
    "nl_nl": ["Te jong voor een stek: laat hem wat groeien", "%s (met wortelkluit)",
              "Hele struik: wordt even groot herplant"],
    "sv_se": ["För ung för en stickling: låt den växa lite", "%s (med rotklump)",
              "Hel buske: planteras om i samma storlek"],
    "da_dk": ["For ung til en stikling: lad den vokse lidt", "%s (med rodklump)",
              "Hel busk: plantes igen i samme størrelse"],
    "ru_ru": ["Слишком молодой для черенка: дайте ему подрасти", "%s (с комом земли)",
              "Целый куст: после посадки сохранит размер"],
}


def translations(code):
    """English -> translation for these strings in one language."""
    vals = _T.get(code, [])
    assert not vals or len(vals) == len(_K), (code, len(vals), len(_K))
    return {LANG[k]: t for k, t in zip(_K, vals)}
