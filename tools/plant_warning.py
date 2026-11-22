"""Look-at warning for plants that will not grow where they stand (3.54.0, Guilherme).

Code: src/main/java/com/periodicelements/shrubs/PlantWarning.java, client/PlantWarningHud.java and the Jade plugin.
generate.py merges LANG into en_us and translations(code) into each language.
"""

M = "periodic_elements"

LANG = {
    f"gui.{M}.plant.not_here": "This plant doesn't grow here",
    f"gui.{M}.plant.wrong_environment": "This plant doesn't grow in this environment",
    f"config.jade.plugin_{M}.plant_growth": "Plants: warning when they will not grow there",
}

_K = list(LANG)

_T = {
    "pt_br": ["Esta planta não cresce aqui", "Esta planta não cresce nesse ambiente",
              "Plantas: aviso quando não crescem ali"],
    "es_es": ["Esta planta no crece aquí", "Esta planta no crece en este ambiente",
              "Plantas: aviso cuando no crecen ahí"],
    "fr_fr": ["Cette plante ne pousse pas ici", "Cette plante ne pousse pas dans cet environnement",
              "Plantes : avertissement quand elles ne poussent pas ici"],
    "de_de": ["Diese Pflanze wächst hier nicht", "Diese Pflanze wächst in dieser Umgebung nicht",
              "Pflanzen: Warnung, wenn sie dort nicht wachsen"],
    "it_it": ["Questa pianta non cresce qui", "Questa pianta non cresce in questo ambiente",
              "Piante: avviso quando non crescono lì"],
    "nl_nl": ["Deze plant groeit hier niet", "Deze plant groeit niet in deze omgeving",
              "Planten: waarschuwing als ze daar niet groeien"],
    "sv_se": ["Den här växten växer inte här", "Den här växten växer inte i den här miljön",
              "Växter: varning när de inte växer där"],
    "da_dk": ["Denne plante vokser ikke her", "Denne plante vokser ikke i dette miljø",
              "Planter: advarsel når de ikke vokser der"],
    "ru_ru": ["Это растение здесь не растёт", "Это растение не растёт в таких условиях",
              "Растения: предупреждение, если они там не растут"],
}


def translations(code):
    """English -> translation for these strings in one language."""
    vals = _T.get(code, [])
    assert not vals or len(vals) == len(_K), (code, len(vals), len(_K))
    return {LANG[k]: t for k, t in zip(_K, vals)}
