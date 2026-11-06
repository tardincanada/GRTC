"""Jade plugin (3.26.0): its strings. Code in src/main/java/com/periodicelements/compat/jade.

generate.py merges LANG into en_us and translations(code) into each language.
"""

LANG = {
    "jade.periodic_elements.fuel": "Fuel: %s%% left",
    "jade.periodic_elements.analyse": "Composition: analyse it with the Magnifying Lens",
    "jade.periodic_elements.other": "%s unidentified",
    "config.jade.plugin_periodic_elements.machine": "Machines: status, temperature, reaction",
    "config.jade.plugin_periodic_elements.power": "Power blocks: stored energy and state",
    "config.jade.plugin_periodic_elements.storage_tank": "Storage Tank: contents",
    "config.jade.plugin_periodic_elements.composition": "Block composition (Magnifying Lens rules)",
}

_K = list(LANG)

# One list per language, in the order of LANG's keys.
_T = {
    "pt_br": ["Combustível: restam %s%%", "Composição: analise com a Lupa", "%s não identificado",
              "Máquinas: estado, temperatura, reação", "Blocos de energia: energia guardada e estado",
              "Tanque de Armazenamento: conteúdo", "Composição dos blocos (regras da Lupa)"],
    "es_es": ["Combustible: queda un %s%%", "Composición: analízalo con la Lupa", "%s sin identificar",
              "Máquinas: estado, temperatura, reacción", "Bloques de energía: energía almacenada y estado",
              "Tanque de almacenamiento: contenido", "Composición de los bloques (reglas de la Lupa)"],
    "fr_fr": ["Combustible : %s %% restant", "Composition : analysez-le avec la Loupe", "%s non identifié",
              "Machines : état, température, réaction", "Blocs d'énergie : énergie stockée et état",
              "Réservoir de stockage : contenu", "Composition des blocs (règles de la Loupe)"],
    "de_de": ["Brennstoff: noch %s %%", "Zusammensetzung: mit der Lupe analysieren", "%s nicht bestimmt",
              "Maschinen: Zustand, Temperatur, Reaktion", "Energieblöcke: gespeicherte Energie und Zustand",
              "Lagertank: Inhalt", "Zusammensetzung der Blöcke (Regeln der Lupe)"],
    "it_it": ["Combustibile: resta il %s%%", "Composizione: analizzalo con la Lente d'ingrandimento", "%s non identificato",
              "Macchine: stato, temperatura, reazione", "Blocchi di energia: energia accumulata e stato",
              "Serbatoio di stoccaggio: contenuto", "Composizione dei blocchi (regole della Lente d'ingrandimento)"],
    "nl_nl": ["Brandstof: nog %s%%", "Samenstelling: onderzoek het met het Vergrootglas", "%s niet bepaald",
              "Machines: toestand, temperatuur, reactie", "Energieblokken: opgeslagen energie en toestand",
              "Opslagtank: inhoud", "Samenstelling van blokken (regels van het Vergrootglas)"],
    "ru_ru": ["Топливо: осталось %s%%", "Состав: изучите его увеличительным стеклом", "%s не определено",
              "Машины: состояние, температура, реакция", "Энергоблоки: запас энергии и состояние",
              "Резервуар: содержимое", "Состав блоков (по правилам увеличительного стекла)"],
    "sv_se": ["Bränsle: %s %% kvar", "Sammansättning: analysera med förstoringsglaset", "%s ej bestämt",
              "Maskiner: tillstånd, temperatur, reaktion", "Energiblock: lagrad energi och tillstånd",
              "Lagringstank: innehåll", "Blockens sammansättning (förstoringsglasets regler)"],
    "da_dk": ["Brændsel: %s %% tilbage", "Sammensætning: analysér med forstørrelsesglasset", "%s ikke bestemt",
              "Maskiner: tilstand, temperatur, reaktion", "Energiblokke: lagret energi og tilstand",
              "Lagertank: indhold", "Blokkenes sammensætning (forstørrelsesglassets regler)"],
}


def translations(code):
    """English -> translation for the Jade strings in one language."""
    return {LANG[k]: t for k, t in zip(_K, _T.get(code, []))}
