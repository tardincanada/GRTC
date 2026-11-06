"""Search in the Field Guide and the compendiums (3.27.0): its strings. Code in client/SearchText.java,
FieldGuideScreen, GuideBookScreen and CompendiumScreen.

generate.py merges LANG into en_us and translations(code) into each language.
"""

LANG = {
    "gui.periodic_elements.search": "Search",
    "gui.periodic_elements.search.hint": "Search...",
    "gui.periodic_elements.guide.results": "%s chapters found (the best first)",
    "gui.periodic_elements.guide.no_results": "Nothing found in what you can read so far",
    "gui.periodic_elements.compendium.results": "Search: %s",
    "gui.periodic_elements.compendium.no_results": "Nothing found among the entries you have noted.",
}

_K = list(LANG)

# One list per language, in the order of LANG's keys.
_T = {
    "pt_br": ["Buscar", "Buscar...", "%s capítulos encontrados (os melhores primeiro)",
              "Nada encontrado no que você já pode ler", "Busca: %s",
              "Nada encontrado entre as entradas que você anotou."],
    "es_es": ["Buscar", "Buscar...", "%s capítulos encontrados (los mejores primero)",
              "No se encontró nada en lo que ya puedes leer", "Búsqueda: %s",
              "No se encontró nada entre las entradas que has anotado."],
    "fr_fr": ["Rechercher", "Rechercher...", "%s chapitres trouvés (les meilleurs d'abord)",
              "Rien trouvé dans ce que vous pouvez lire pour l'instant", "Recherche : %s",
              "Rien trouvé parmi les entrées que vous avez notées."],
    "de_de": ["Suchen", "Suchen...", "%s Kapitel gefunden (die besten zuerst)",
              "Nichts gefunden in dem, was du bisher lesen kannst", "Suche: %s",
              "Nichts gefunden unter den Einträgen, die du notiert hast."],
    "it_it": ["Cerca", "Cerca...", "%s capitoli trovati (i migliori prima)",
              "Niente trovato in ciò che puoi leggere finora", "Ricerca: %s",
              "Niente trovato tra le voci che hai annotato."],
    "nl_nl": ["Zoeken", "Zoeken...", "%s hoofdstukken gevonden (de beste eerst)",
              "Niets gevonden in wat je tot nu toe kunt lezen", "Zoeken: %s",
              "Niets gevonden in de vermeldingen die je hebt genoteerd."],
    "ru_ru": ["Поиск", "Поиск...", "Найдено глав: %s (сначала лучшие)",
              "Ничего не найдено в том, что вам уже доступно", "Поиск: %s",
              "Ничего не найдено среди записанных вами статей."],
    "sv_se": ["Sök", "Sök...", "%s kapitel hittade (de bästa först)",
              "Inget hittades i det du kan läsa hittills", "Sökning: %s",
              "Inget hittades bland posterna du har antecknat."],
    "da_dk": ["Søg", "Søg...", "%s kapitler fundet (de bedste først)",
              "Intet fundet i det, du kan læse indtil nu", "Søgning: %s",
              "Intet fundet blandt de opslag, du har noteret."],
}


def translations(code):
    """English -> translation for the search strings in one language."""
    return {LANG[k]: t for k, t in zip(_K, _T.get(code, []))}
