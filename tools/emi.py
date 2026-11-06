"""EMI recipe viewer plugin (3.25.0): its strings. Code in src/main/java/com/periodicelements/compat/emi.

generate.py merges LANG into en_us and translations(code) into each language.
"""

LANG = {
    "emi.category.periodic_elements.composition": "Composition",
    "emi.periodic_elements.feed": "Any item that holds %s",
    "emi.periodic_elements.units": "%s units",
    "emi.periodic_elements.research": "Research: %s (level %s)",
    "emi.periodic_elements.research.none": "No research needed",
    "emi.periodic_elements.locked": "A process of tech level %s: research further to see it",
    "emi.periodic_elements.also_made": "Also made:",
    "emi.periodic_elements.gangue": "Gangue (separates like stone)",
    "emi.periodic_elements.total": "%s units in all (1000 make a block)",
    "emi.periodic_elements.hidden": "%s units not identified yet",
    "emi.periodic_elements.unknown": "Not identified yet: research the elements in it",
    "tag.item.periodic_elements.fertilizer": "Fertilizers",
    "tag.item.periodic_elements.soap": "Soaps",
    "tag.item.periodic_elements.ink": "Inks",
    "tag.item.periodic_elements.cable_insulation": "Cable Insulation",
}

_K = list(LANG)

# One list per language, in the order of LANG's keys.
_T = {
    "pt_br": ["Composição", "Qualquer item que contenha %s", "%s unidades", "Pesquisa: %s (nível %s)",
              "Não precisa de pesquisa", "Um processo do nível tecnológico %s: avance na pesquisa para vê-lo",
              "Também produz:", "Ganga (separa-se como pedra)", "%s unidades no total (1000 formam um bloco)",
              "%s unidades ainda não identificadas", "Ainda não identificado: pesquise os elementos que contém",
              "Fertilizantes", "Sabões", "Tintas", "Isolamento de cabo"],
    "es_es": ["Composición", "Cualquier objeto que contenga %s", "%s unidades", "Investigación: %s (nivel %s)",
              "No necesita investigación", "Un proceso del nivel tecnológico %s: avanza en la investigación para verlo",
              "También produce:", "Ganga (se separa como piedra)", "%s unidades en total (1000 forman un bloque)",
              "%s unidades aún sin identificar", "Aún sin identificar: investiga los elementos que contiene",
              "Fertilizantes", "Jabones", "Tintas", "Aislamiento de cable"],
    "fr_fr": ["Composition", "Tout objet qui contient %s", "%s unités", "Recherche : %s (niveau %s)",
              "Aucune recherche nécessaire", "Un procédé du niveau technologique %s : poursuivez la recherche pour le voir",
              "Produit aussi :", "Gangue (se sépare comme la pierre)", "%s unités au total (1000 font un bloc)",
              "%s unités pas encore identifiées", "Pas encore identifié : étudiez les éléments qu'il contient",
              "Engrais", "Savons", "Encres", "Isolant de câble"],
    "de_de": ["Zusammensetzung", "Jeder Gegenstand, der %s enthält", "%s Einheiten", "Forschung: %s (Stufe %s)",
              "Keine Forschung nötig", "Ein Verfahren der Technologiestufe %s: forsche weiter, um es zu sehen",
              "Entsteht auch:", "Gangart (trennt sich wie Stein)", "%s Einheiten insgesamt (1000 ergeben einen Block)",
              "%s Einheiten noch nicht bestimmt", "Noch nicht bestimmt: erforsche die Elemente darin",
              "Dünger", "Seifen", "Tinten", "Kabelisolierung"],
    "it_it": ["Composizione", "Qualsiasi oggetto che contiene %s", "%s unità", "Ricerca: %s (livello %s)",
              "Nessuna ricerca necessaria", "Un processo del livello tecnologico %s: prosegui la ricerca per vederlo",
              "Produce anche:", "Ganga (si separa come la pietra)", "%s unità in tutto (1000 fanno un blocco)",
              "%s unità non ancora identificate", "Non ancora identificato: studia gli elementi che contiene",
              "Fertilizzanti", "Saponi", "Inchiostri", "Isolante per cavi"],
    "nl_nl": ["Samenstelling", "Elk voorwerp dat %s bevat", "%s eenheden", "Onderzoek: %s (niveau %s)",
              "Geen onderzoek nodig", "Een proces van technologieniveau %s: onderzoek verder om het te zien",
              "Maakt ook:", "Ganggesteente (scheidt zich als steen)", "%s eenheden in totaal (1000 maken een blok)",
              "%s eenheden nog niet bepaald", "Nog niet bepaald: onderzoek de elementen erin",
              "Meststoffen", "Zepen", "Inkten", "Kabelisolatie"],
    "ru_ru": ["Состав", "Любой предмет, содержащий %s", "%s ед.", "Исследование: %s (уровень %s)",
              "Исследование не нужно", "Процесс технологического уровня %s: продолжайте исследования, чтобы увидеть его",
              "Также даёт:", "Пустая порода (разделяется как камень)", "Всего %s ед. (1000 составляют блок)",
              "%s ед. ещё не определено", "Ещё не определено: исследуйте элементы, из которых оно состоит",
              "Удобрения", "Мыло", "Чернила", "Изоляция кабеля"],
    "sv_se": ["Sammansättning", "Alla föremål som innehåller %s", "%s enheter", "Forskning: %s (nivå %s)",
              "Ingen forskning behövs", "En process på teknologinivå %s: forska vidare för att se den",
              "Ger också:", "Gångart (skiljs som sten)", "%s enheter totalt (1000 blir ett block)",
              "%s enheter ännu inte bestämda", "Ännu inte bestämt: forska på grundämnena i det",
              "Gödselmedel", "Tvålar", "Bläck", "Kabelisolering"],
    "da_dk": ["Sammensætning", "Enhver genstand, der indeholder %s", "%s enheder", "Forskning: %s (niveau %s)",
              "Ingen forskning nødvendig", "En proces på teknologiniveau %s: forsk videre for at se den",
              "Giver også:", "Gangart (skilles som sten)", "%s enheder i alt (1000 giver en blok)",
              "%s enheder endnu ikke bestemt", "Endnu ikke bestemt: forsk i grundstofferne i det",
              "Gødning", "Sæber", "Blæk", "Kabelisolering"],
}


# A tree's log tag ("#periodic_elements:acai_logs", as EMI names a slot that takes any of a tree's logs), from the
# tree's translated name. English first, then each language.
LOG_TAG = {
    "en_us": "%s Logs", "pt_br": "Troncos de %s", "es_es": "Troncos de %s", "fr_fr": "Bûches de %s",
    "de_de": "%s-Stämme", "it_it": "Tronchi di %s", "nl_nl": "%s-stammen", "ru_ru": "Брёвна: %s",
    "sv_se": "%s-stockar", "da_dk": "%s-stammer",
}


def log_tags(tags_dir, code, names):
    """tag.item keys for every <tree>_logs item tag, named from the tree's name in {@code names} (one language)."""
    out = {}
    for f in sorted(tags_dir.glob("*_logs.json")):
        tree = f.stem[:-len("_logs")]
        name = names.get(f"tree.periodic_elements.{tree}")
        if name:
            out[f"tag.item.periodic_elements.{f.stem}"] = LOG_TAG[code] % name
    return out


def translations(code):
    """English -> translation for the EMI strings in one language."""
    return {LANG[k]: t for k, t in zip(_K, _T.get(code, []))}
