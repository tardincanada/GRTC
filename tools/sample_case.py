"""Sample Case and the player's settings screen (3.31.0): texture, model, recipe and strings.

Code in samplecase/ (item, screen handler), client/SampleCaseScreen, client/ClientConfig, client/ConfigScreen,
compat/modmenu and mixin/ItemEntityMixin. generate.py calls generate_sample_case() and merges translations(code).
"""
import json

from PIL import Image

ITEM = "sample_case"

LANG = {
    "item.periodic_elements.sample_case": "Sample Case",
    "item.periodic_elements.sample_case.tip": "Holds only elements and substances, 54 places. Use it to open; sneak and use to switch picking up",
    "item.periodic_elements.sample_case.used": "%s of %s places used",
    "item.periodic_elements.sample_case.pickup_on": "Picks up samples you walk over",
    "item.periodic_elements.sample_case.pickup_off": "Does not pick up (sneak and use to switch on)",
    "message.periodic_elements.sample_case.on": "Sample Case: picks up samples",
    "message.periodic_elements.sample_case.off": "Sample Case: does not pick up",
    "gui.periodic_elements.config.title": "Elementar: settings",
    "gui.periodic_elements.config.minimap_size": "Minimap size: %s",
    "gui.periodic_elements.config.minimap_corner": "Minimap corner",
    "gui.periodic_elements.config.corner.top_left": "Top left",
    "gui.periodic_elements.config.corner.top_right": "Top right",
    "gui.periodic_elements.config.corner.bottom_left": "Bottom left",
    "gui.periodic_elements.config.corner.bottom_right": "Bottom right",
    "gui.periodic_elements.config.lens_lines": "Magnifying Lens: %s elements named",
    "gui.periodic_elements.config.compendium_hud": "Compendium corner panel",
    "gui.periodic_elements.config.compendium_hud.tip": "The biome, climate and plant under the crosshair while a compendium is in the hotbar",
    "gui.periodic_elements.config.composition_tooltip": "Compositions in tooltips",
    "gui.periodic_elements.config.composition_tooltip.tip": "What a block or item is made of, under its name",
    "gui.periodic_elements.config.radiation_tooltip": "Radioactivity in tooltips",
    "gui.periodic_elements.config.radiation_tooltip.tip": "The decay rate of radioactive items, under their name",
}

_K = list(LANG)

# One list per language, in the order of LANG's keys.
_T = {
    "pt_br": ["Maleta de Amostras", "Guarda só elementos e substâncias, 54 espaços. Use para abrir; agache e use para ligar ou desligar a coleta",
              "%s de %s espaços ocupados", "Recolhe as amostras por onde você passa", "Não recolhe (agache e use para ligar)",
              "Maleta de Amostras: recolhe amostras", "Maleta de Amostras: não recolhe",
              "Elementar: configurações", "Tamanho do minimapa: %s", "Canto do minimapa",
              "Superior esquerdo", "Superior direito", "Inferior esquerdo", "Inferior direito",
              "Lupa: %s elementos nomeados", "Painel do compêndio",
              "O bioma, o clima e a planta sob a mira enquanto um compêndio está na barra",
              "Composição nas dicas", "Do que um bloco ou item é feito, abaixo do nome",
              "Radioatividade nas dicas", "A taxa de decaimento dos itens radioativos, abaixo do nome"],
    "es_es": ["Maletín de Muestras", "Guarda solo elementos y sustancias, 54 huecos. Úsalo para abrirlo; agáchate y úsalo para activar o desactivar la recogida",
              "%s de %s huecos ocupados", "Recoge las muestras por donde pasas", "No recoge (agáchate y úsalo para activarlo)",
              "Maletín de Muestras: recoge muestras", "Maletín de Muestras: no recoge",
              "Elementar: ajustes", "Tamaño del minimapa: %s", "Esquina del minimapa",
              "Arriba a la izquierda", "Arriba a la derecha", "Abajo a la izquierda", "Abajo a la derecha",
              "Lupa: %s elementos nombrados", "Panel del compendio",
              "El bioma, el clima y la planta bajo la mira mientras un compendio está en la barra",
              "Composición en las descripciones", "De qué está hecho un bloque u objeto, bajo su nombre",
              "Radiactividad en las descripciones", "La tasa de desintegración de los objetos radiactivos, bajo su nombre"],
    "fr_fr": ["Mallette d'échantillons", "Ne contient que des éléments et des substances, 54 places. Utilisez-la pour l'ouvrir ; accroupi, pour activer ou couper le ramassage",
              "%s places sur %s occupées", "Ramasse les échantillons sur votre passage", "Ne ramasse pas (accroupi + utiliser pour activer)",
              "Mallette d'échantillons : ramasse", "Mallette d'échantillons : ne ramasse pas",
              "Elementar : réglages", "Taille de la mini-carte : %s", "Coin de la mini-carte",
              "En haut à gauche", "En haut à droite", "En bas à gauche", "En bas à droite",
              "Loupe : %s éléments nommés", "Panneau du compendium",
              "Le biome, le climat et la plante visée quand un compendium est dans la barre",
              "Composition dans les infobulles", "De quoi est fait un bloc ou un objet, sous son nom",
              "Radioactivité dans les infobulles", "Le taux de désintégration des objets radioactifs, sous leur nom"],
    "de_de": ["Probenkoffer", "Fasst nur Elemente und Stoffe, 54 Plätze. Benutzen zum Öffnen; schleichend benutzen schaltet das Aufsammeln",
              "%s von %s Plätzen belegt", "Sammelt Proben auf, über die du läufst", "Sammelt nicht auf (schleichend benutzen zum Einschalten)",
              "Probenkoffer: sammelt Proben auf", "Probenkoffer: sammelt nicht auf",
              "Elementar: Einstellungen", "Größe der Minikarte: %s", "Ecke der Minikarte",
              "Oben links", "Oben rechts", "Unten links", "Unten rechts",
              "Lupe: %s Elemente genannt", "Kompendium-Eckfeld",
              "Biom, Klima und die Pflanze im Fadenkreuz, solange ein Kompendium in der Schnellleiste ist",
              "Zusammensetzung in Tooltips", "Woraus ein Block oder Gegenstand besteht, unter seinem Namen",
              "Radioaktivität in Tooltips", "Die Zerfallsrate radioaktiver Gegenstände, unter ihrem Namen"],
    "it_it": ["Valigetta dei campioni", "Contiene solo elementi e sostanze, 54 posti. Usala per aprirla; da accovacciato, per attivare o disattivare la raccolta",
              "%s posti su %s occupati", "Raccoglie i campioni su cui passi", "Non raccoglie (accovacciati e usala per attivare)",
              "Valigetta dei campioni: raccoglie", "Valigetta dei campioni: non raccoglie",
              "Elementar: impostazioni", "Dimensione della minimappa: %s", "Angolo della minimappa",
              "In alto a sinistra", "In alto a destra", "In basso a sinistra", "In basso a destra",
              "Lente d'ingrandimento: %s elementi nominati", "Riquadro del compendio",
              "Il bioma, il clima e la pianta nel mirino mentre un compendio è nella barra",
              "Composizione nei suggerimenti", "Di cosa è fatto un blocco o un oggetto, sotto il nome",
              "Radioattività nei suggerimenti", "Il tasso di decadimento degli oggetti radioattivi, sotto il nome"],
    "nl_nl": ["Monsterkoffer", "Bevat alleen elementen en stoffen, 54 plaatsen. Gebruik om te openen; sluipend gebruiken schakelt het oprapen",
              "%s van %s plaatsen bezet", "Raapt monsters op waar je overheen loopt", "Raapt niet op (sluipend gebruiken om aan te zetten)",
              "Monsterkoffer: raapt monsters op", "Monsterkoffer: raapt niet op",
              "Elementar: instellingen", "Grootte van de minikaart: %s", "Hoek van de minikaart",
              "Linksboven", "Rechtsboven", "Linksonder", "Rechtsonder",
              "Vergrootglas: %s elementen genoemd", "Compendiumpaneel",
              "Het bioom, het klimaat en de plant in het vizier zolang een compendium in de hotbar zit",
              "Samenstelling in tooltips", "Waar een blok of voorwerp van gemaakt is, onder de naam",
              "Radioactiviteit in tooltips", "De vervalsnelheid van radioactieve voorwerpen, onder de naam"],
    "ru_ru": ["Чемодан для образцов", "Хранит только элементы и вещества, 54 места. Используйте, чтобы открыть; крадучись — включить или выключить подбор",
              "Занято мест: %s из %s", "Подбирает образцы, по которым вы проходите", "Не подбирает (крадучись используйте, чтобы включить)",
              "Чемодан для образцов: подбирает", "Чемодан для образцов: не подбирает",
              "Elementar: настройки", "Размер миникарты: %s", "Угол миникарты",
              "Слева сверху", "Справа сверху", "Слева снизу", "Справа снизу",
              "Увеличительное стекло: названо элементов: %s", "Панель справочника",
              "Биом, климат и растение под прицелом, пока справочник на панели быстрого доступа",
              "Состав в подсказках", "Из чего сделан блок или предмет, под его названием",
              "Радиоактивность в подсказках", "Скорость распада радиоактивных предметов, под их названием"],
    "sv_se": ["Provväska", "Rymmer bara grundämnen och ämnen, 54 platser. Använd för att öppna; smygande användning slår på eller av upplockning",
              "%s av %s platser använda", "Plockar upp prover du går över", "Plockar inte upp (smyg och använd för att slå på)",
              "Provväska: plockar upp prover", "Provväska: plockar inte upp",
              "Elementar: inställningar", "Minikartans storlek: %s", "Minikartans hörn",
              "Uppe till vänster", "Uppe till höger", "Nere till vänster", "Nere till höger",
              "Förstoringsglas: %s grundämnen namngivna", "Kompendiets hörnpanel",
              "Biomet, klimatet och växten i siktet medan ett kompendium ligger i snabbfältet",
              "Sammansättning i verktygstips", "Vad ett block eller föremål är gjort av, under namnet",
              "Radioaktivitet i verktygstips", "Sönderfallshastigheten för radioaktiva föremål, under namnet"],
    "da_dk": ["Prøvekuffert", "Rummer kun grundstoffer og stoffer, 54 pladser. Brug for at åbne; snig og brug for at slå opsamling til eller fra",
              "%s af %s pladser brugt", "Samler prøver op, du går over", "Samler ikke op (snig og brug for at slå til)",
              "Prøvekuffert: samler prøver op", "Prøvekuffert: samler ikke op",
              "Elementar: indstillinger", "Minikortets størrelse: %s", "Minikortets hjørne",
              "Øverst til venstre", "Øverst til højre", "Nederst til venstre", "Nederst til højre",
              "Forstørrelsesglas: %s grundstoffer navngivet", "Kompendiets hjørnepanel",
              "Biomet, klimaet og planten i sigtekornet, mens et kompendium er i hotbaren",
              "Sammensætning i værktøjstip", "Hvad en blok eller genstand er lavet af, under navnet",
              "Radioaktivitet i værktøjstip", "Henfaldshastigheden for radioaktive genstande, under navnet"],
}


def translations(code):
    """English -> translation for these strings in one language."""
    return {LANG[k]: t for k, t in zip(_K, _T.get(code, []))}


def texture():
    """A brown leather case with a darker lid band, brass clasps, a handle and a little periodic-table label."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    def rect(x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                px[x, y] = c + (255,)

    rect(6, 2, 9, 2, (70, 42, 24))        # handle
    rect(5, 3, 5, 4, (70, 42, 24))
    rect(10, 3, 10, 4, (70, 42, 24))
    rect(1, 5, 14, 14, (74, 44, 24))      # outline
    rect(2, 6, 13, 13, (139, 90, 52))     # leather
    rect(2, 6, 13, 7, (112, 70, 38))      # lid band
    rect(2, 9, 13, 9, (112, 70, 38))      # seam
    rect(4, 8, 4, 8, (222, 180, 80))      # clasps
    rect(11, 8, 11, 8, (222, 180, 80))
    rect(5, 10, 10, 12, (236, 226, 196))  # label
    for x, y, c in [(6, 11, (60, 120, 200)), (7, 11, (200, 70, 60)), (8, 11, (80, 160, 90)), (9, 11, (230, 190, 70))]:
        px[x, y] = c + (255,)
    rect(2, 13, 13, 13, (100, 62, 34))    # bottom shade
    return img


RECIPE = {
    "type": "minecraft:crafting_shaped",
    "category": "misc",
    "pattern": ["LSL", "LCL", "LLL"],
    "key": {"L": {"item": "minecraft:leather"}, "S": {"item": "minecraft:string"}, "C": {"item": "minecraft:chest"}},
    "result": {"id": "periodic_elements:sample_case", "count": 1},
}


def generate_sample_case(assets, data_dir, lang, mod_id):
    texture().save(assets / "textures" / "item" / f"{ITEM}.png")
    (assets / "models" / "item" / f"{ITEM}.json").write_text(json.dumps(
        {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{ITEM}"}}, indent=2) + "\n",
        encoding="utf-8")
    (data_dir / mod_id / "recipe" / f"{ITEM}.json").write_text(json.dumps(RECIPE, indent=2) + "\n", encoding="utf-8")
    lang.update(LANG)
