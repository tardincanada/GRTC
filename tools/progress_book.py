"""The Book of Progress (3.20.0): a creative-only testing book that sets the reader's tech level.

No recipe and no loot: it is only in the Workshop creative tab. Code in research/ProgressBook.java and
client/ProgressBookScreen.java. generate.py calls generate_progress_book() and merges translations(code).
"""
import json

from PIL import Image

ITEM = "progress_book"

LANG = {
    "item.periodic_elements.progress_book": "Book of Progress",
    "item.periodic_elements.progress_book.tip":
        "Testing aid, only in the creative inventory. Choose your tech level to test how progress changes what you can do",
    "gui.periodic_elements.progress_book.title": "Book of Progress",
    "gui.periodic_elements.progress_book.current": "Your tech level: %s (%s), %s projects",
    "gui.periodic_elements.progress_book.level": "Level %s: %s",
    "gui.periodic_elements.progress_book.optional.on": "Optional research: included",
    "gui.periodic_elements.progress_book.optional.off": "Optional research: left out (key projects only)",
    "gui.periodic_elements.progress_book.hint":
        "Picking a level sets your research to exactly that level: earlier research is added, later research is "
        "removed. Creative mode ignores research when placing blocks, so switch to survival to test the gates.",
    "message.periodic_elements.progress_book.set": "Tech level set to %s (%s): %s research projects",
}

_K = list(LANG)

# One list per language, in the order of LANG's keys.
_T = {
    "pt_br": [
        "Livro do Avanço",
        "Item de teste, só no inventário do criativo. Escolha seu nível tecnológico para testar como o avanço muda o "
        "que você pode fazer",
        "Livro do Avanço",
        "Seu nível tecnológico: %s (%s), %s projetos",
        "Nível %s: %s",
        "Pesquisas opcionais: incluídas",
        "Pesquisas opcionais: fora (só projetos-chave)",
        "Escolher um nível deixa sua pesquisa exatamente nesse nível: as pesquisas anteriores são somadas e as "
        "posteriores, removidas. O modo criativo ignora a pesquisa ao colocar blocos; mude para o sobrevivência para "
        "testar as restrições.",
        "Nível tecnológico definido: %s (%s), %s projetos de pesquisa",
    ],
    "es_es": [
        "Libro del Progreso",
        "Objeto de prueba, solo en el inventario creativo. Elige tu nivel tecnológico para probar cómo el progreso "
        "cambia lo que puedes hacer",
        "Libro del Progreso",
        "Tu nivel tecnológico: %s (%s), %s proyectos",
        "Nivel %s: %s",
        "Investigaciones opcionales: incluidas",
        "Investigaciones opcionales: fuera (solo proyectos clave)",
        "Elegir un nivel deja tu investigación exactamente en ese nivel: se añade la investigación anterior y se quita "
        "la posterior. El modo creativo ignora la investigación al colocar bloques; cambia a supervivencia para probar "
        "las restricciones.",
        "Nivel tecnológico fijado en %s (%s): %s proyectos de investigación",
    ],
    "fr_fr": [
        "Livre du Progrès",
        "Objet de test, seulement dans l'inventaire créatif. Choisissez votre niveau technologique pour tester comment "
        "le progrès change ce que vous pouvez faire",
        "Livre du Progrès",
        "Votre niveau technologique : %s (%s), %s projets",
        "Niveau %s : %s",
        "Recherches facultatives : incluses",
        "Recherches facultatives : exclues (projets clés seulement)",
        "Choisir un niveau met vos recherches exactement à ce niveau : les recherches antérieures sont ajoutées, les "
        "suivantes retirées. Le mode créatif ignore la recherche pour poser des blocs ; passez en survie pour tester "
        "les restrictions.",
        "Niveau technologique fixé à %s (%s) : %s projets de recherche",
    ],
    "it_it": [
        "Libro del Progresso",
        "Oggetto di prova, solo nell'inventario creativo. Scegli il tuo livello tecnologico per provare come il "
        "progresso cambia ciò che puoi fare",
        "Libro del Progresso",
        "Il tuo livello tecnologico: %s (%s), %s progetti",
        "Livello %s: %s",
        "Ricerche facoltative: incluse",
        "Ricerche facoltative: escluse (solo progetti chiave)",
        "Scegliere un livello porta la tua ricerca esattamente a quel livello: la ricerca precedente viene aggiunta, "
        "quella successiva tolta. La modalità creativa ignora la ricerca quando si piazzano blocchi; passa alla "
        "sopravvivenza per provare i vincoli.",
        "Livello tecnologico impostato a %s (%s): %s progetti di ricerca",
    ],
    "de_de": [
        "Buch des Fortschritts",
        "Testgegenstand, nur im Kreativinventar. Wähle deine Technologiestufe, um zu testen, wie der Fortschritt "
        "verändert, was du tun kannst",
        "Buch des Fortschritts",
        "Deine Technologiestufe: %s (%s), %s Projekte",
        "Stufe %s: %s",
        "Optionale Forschung: enthalten",
        "Optionale Forschung: ausgelassen (nur Schlüsselprojekte)",
        "Eine Stufe zu wählen setzt deine Forschung genau auf diese Stufe: frühere Forschung kommt hinzu, spätere wird "
        "entfernt. Der Kreativmodus ignoriert Forschung beim Platzieren von Blöcken; wechsle in den Überlebensmodus, "
        "um die Sperren zu testen.",
        "Technologiestufe auf %s gesetzt (%s): %s Forschungsprojekte",
    ],
    "nl_nl": [
        "Boek van de Vooruitgang",
        "Testvoorwerp, alleen in de creatieve inventaris. Kies je technologieniveau om te testen hoe vooruitgang "
        "verandert wat je kunt doen",
        "Boek van de Vooruitgang",
        "Je technologieniveau: %s (%s), %s projecten",
        "Niveau %s: %s",
        "Optioneel onderzoek: inbegrepen",
        "Optioneel onderzoek: weggelaten (alleen sleutelprojecten)",
        "Een niveau kiezen zet je onderzoek precies op dat niveau: eerder onderzoek wordt toegevoegd, later onderzoek "
        "verwijderd. De creatieve modus negeert onderzoek bij het plaatsen van blokken; schakel naar overleven om de "
        "beperkingen te testen.",
        "Technologieniveau ingesteld op %s (%s): %s onderzoeksprojecten",
    ],
    "sv_se": [
        "Framstegens bok",
        "Testföremål, bara i det kreativa förrådet. Välj din teknivå för att testa hur framsteg ändrar vad du kan göra",
        "Framstegens bok",
        "Din teknivå: %s (%s), %s projekt",
        "Nivå %s: %s",
        "Valfri forskning: med",
        "Valfri forskning: utelämnad (bara nyckelprojekt)",
        "Att välja en nivå sätter din forskning till exakt den nivån: tidigare forskning läggs till, senare tas bort. "
        "Kreativt läge struntar i forskning när block placeras; byt till överlevnad för att testa spärrarna.",
        "Teknivå satt till %s (%s): %s forskningsprojekt",
    ],
    "da_dk": [
        "Fremskridtets bog",
        "Testgenstand, kun i det kreative inventar. Vælg dit teknologiniveau for at teste, hvordan fremskridt ændrer, "
        "hvad du kan gøre",
        "Fremskridtets bog",
        "Dit teknologiniveau: %s (%s), %s projekter",
        "Niveau %s: %s",
        "Valgfri forskning: med",
        "Valgfri forskning: udeladt (kun nøgleprojekter)",
        "At vælge et niveau sætter din forskning til præcis det niveau: tidligere forskning tilføjes, senere fjernes. "
        "Kreativ tilstand ignorerer forskning, når der placeres blokke; skift til overlevelse for at teste "
        "begrænsningerne.",
        "Teknologiniveau sat til %s (%s): %s forskningsprojekter",
    ],
    "ru_ru": [
        "Книга прогресса",
        "Предмет для проверки, есть только в творческом инвентаре. Выберите свой технологический уровень, чтобы "
        "проверить, как прогресс меняет ваши возможности",
        "Книга прогресса",
        "Ваш технологический уровень: %s (%s), проектов: %s",
        "Уровень %s: %s",
        "Дополнительные исследования: включены",
        "Дополнительные исследования: исключены (только ключевые проекты)",
        "Выбор уровня ставит ваши исследования ровно на этот уровень: более ранние добавляются, более поздние "
        "удаляются. В творческом режиме исследования не мешают ставить блоки; перейдите в режим выживания, чтобы "
        "проверить ограничения.",
        "Технологический уровень установлен: %s (%s), исследовательских проектов: %s",
    ],
}


def translations(code):
    """English -> translation for the book's strings in one language."""
    return {LANG[k]: t for k, t in zip(_K, _T.get(code, []))}


def texture():
    """A violet book with gold clasps and a gold rising arrow on the cover."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    def rect(x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                px[x, y] = c + (255,)

    rect(3, 2, 12, 14, (60, 30, 90))       # cover edge
    rect(4, 3, 11, 13, (120, 60, 170))     # cover
    rect(12, 3, 13, 13, (236, 230, 210))   # pages
    rect(3, 5, 4, 5, (230, 190, 70))       # clasps
    rect(3, 11, 4, 11, (230, 190, 70))
    # a gold staircase arrow climbing to the upper right: progress
    for i, (x, y) in enumerate([(5, 11), (6, 11), (6, 10), (7, 10), (7, 9), (8, 9), (8, 8), (9, 8), (9, 7)]):
        px[x, y] = (250, 215, 90, 255)
    rect(9, 5, 10, 5, (250, 215, 90))
    rect(10, 5, 10, 6, (250, 215, 90))
    return img


def generate_progress_book(assets, lang, mod_id):
    texture().save(assets / "textures" / "item" / f"{ITEM}.png")
    (assets / "models" / "item" / f"{ITEM}.json").write_text(json.dumps(
        {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{ITEM}"}}, indent=2) + "\n",
        encoding="utf-8")
    lang.update(LANG)
