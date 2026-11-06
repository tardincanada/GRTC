"""Creative testing items (3.51.0) and the creative tab names.

Only in the Creative Testing tab: no recipe, no loot. Code in src/main/java/com/periodicelements/creative.
generate.py calls generate_creative() and merges translations(code).
"""
import json

from PIL import Image

BLOCKS = ["creative_generator", "creative_fluid_source", "creative_item_source", "creative_void", "creative_accelerator"]
ITEMS = ["protection_amulet", "growth_wand", "debug_magnifier", "structure_builder"]

M = "periodic_elements"
LANG = {
    f"block.{M}.creative_generator": "Creative Generator",
    f"block.{M}.creative_generator.tip": "Gives unlimited current to every machine touching it or joined to it by wire",
    f"block.{M}.creative_fluid_source": "Infinite Fluid Source",
    f"block.{M}.creative_fluid_source.tip":
        "Use a flask, cylinder or water bucket on it to choose the liquid or gas; pipes, pumps and machines draw it for ever",
    f"block.{M}.creative_item_source": "Infinite Item Source",
    f"block.{M}.creative_item_source.tip":
        "Use any item on it to choose what it gives; hoppers, belts and machines take that item from it for ever",
    f"block.{M}.creative_void": "Universal Void",
    f"block.{M}.creative_void.tip": "Destroys every item, liquid and gas sent into it",
    f"block.{M}.creative_accelerator": "Accelerator Clock",
    f"block.{M}.creative_accelerator.tip":
        "Machines, crops, saplings and young animals within 8 blocks run 10 times as fast",
    f"item.{M}.protection_amulet": "Protection Amulet",
    f"item.{M}.protection_amulet.tip":
        "Anywhere in your inventory: no radiation, poison, venom, infection, fire, heat, cold or drowning, and no harm from animals of the mod",
    f"item.{M}.growth_wand": "Growth Wand",
    f"item.{M}.growth_wand.tip":
        "Use on a crop, sapling or shrub to grow it at once (sneak: everything within 3 blocks); use on a young animal to make it an adult",
    f"item.{M}.debug_magnifier": "Debug Magnifier",
    f"item.{M}.debug_magnifier.tip":
        "Use on a block or animal to see in chat what is inside it: energy, heat, contents, composition, genes",
    f"item.{M}.structure_builder": "Structure Builder",
    f"item.{M}.structure_builder.tip":
        "Sneak and use in the air to choose a structure; use on a block to build it there, ready to work",
    f"tooltip.{M}.creative_only": "Creative testing only: no recipe",
    f"gui.{M}.creative_generator": "Creative Generator: up to %s kJ a tick to each machine",
    f"message.{M}.creative_item_source.set": "Infinite Item Source gives: %s",
    f"message.{M}.creative_item_source.empty": "Infinite Item Source: use an item on it to choose what it gives",
    f"message.{M}.creative_fluid_source.set": "Infinite Fluid Source gives: %s",
    f"message.{M}.creative_fluid_source.empty":
        "Infinite Fluid Source: use a flask, cylinder or water bucket on it to choose the fluid",
    f"message.{M}.creative_fluid_source.wrong_container": "Liquids go in a glass flask, gases in a steel cylinder",
    f"message.{M}.creative.cleared": "Cleared",
    f"message.{M}.creative_void.info": "Universal Void: destroys every item, liquid and gas sent into it",
    f"message.{M}.creative_accelerator.info": "Accelerator Clock: everything within %2$s blocks runs %1$s times as fast",
    f"message.{M}.growth_wand.grown": "Grown: %s",
    f"message.{M}.debug_magnifier.composition": "Composition (%s units): %s",
    f"message.{M}.debug_magnifier.nothing": "Nothing stored inside",
    f"message.{M}.structure_builder.chosen": "Structure: %s",
    f"message.{M}.structure_builder.built": "Built: %s",
    f"message.{M}.structure_builder.tank": "Tank (inside 3x3x3)",
    # 3.51.1: the /compendium command's answers were fixed English
    f"command.{M}.compendium.revealed": "Compendium: %s entries noted for %s",
    f"command.{M}.compendium.forgotten": "Compendium notes forgotten for %s",
}

TAB_NAMES = {  # in creative_tabs.TABS order
    "elements": "Elements", "solid_substances": "Solid Substances", "liquid_substances": "Liquid Substances",
    "gas_substances": "Gases", "materials": "Materials and Intermediates", "ores_rocks": "Ores, Rocks and Soils",
    "trees": "Trees and Wood", "shrubs": "Shrubs", "crops": "Crops and Seeds", "fauna": "Animals",
    "food_medicine": "Food and Medicine", "machines": "Machines", "power_logistics": "Power and Logistics",
    "science": "Science", "tools": "Tools", "combat": "Combat", "exploration": "Exploration",
    "creative": "Creative Testing",
}
for _t, _n in TAB_NAMES.items():
    LANG[f"itemGroup.{M}.{_t}"] = _n

_K = list(LANG)

_TABS_T = {
    "pt_br": ["Elementos", "Substâncias sólidas", "Substâncias líquidas", "Gases", "Materiais e intermediários",
              "Minérios, rochas e solos", "Árvores e madeira", "Arbustos", "Plantas cultiváveis e sementes", "Animais",
              "Alimentos e remédios", "Máquinas", "Energia e logística", "Ciência", "Ferramentas", "Combate",
              "Exploração", "Testes no criativo"],
    "es_es": ["Elementos", "Sustancias sólidas", "Sustancias líquidas", "Gases", "Materiales e intermedios",
              "Minerales, rocas y suelos", "Árboles y madera", "Arbustos", "Cultivos y semillas", "Animales",
              "Alimentos y medicinas", "Máquinas", "Energía y logística", "Ciencia", "Herramientas", "Combate",
              "Exploración", "Pruebas en creativo"],
    "fr_fr": ["Éléments", "Substances solides", "Substances liquides", "Gaz", "Matériaux et intermédiaires",
              "Minerais, roches et sols", "Arbres et bois", "Arbustes", "Cultures et graines", "Animaux",
              "Aliments et remèdes", "Machines", "Énergie et logistique", "Science", "Outils", "Combat",
              "Exploration", "Tests en créatif"],
    "de_de": ["Elemente", "Feste Stoffe", "Flüssige Stoffe", "Gase", "Werkstoffe und Zwischenprodukte",
              "Erze, Gesteine und Böden", "Bäume und Holz", "Sträucher", "Nutzpflanzen und Samen", "Tiere",
              "Nahrung und Arznei", "Maschinen", "Energie und Logistik", "Wissenschaft", "Werkzeuge", "Kampf",
              "Erkundung", "Kreativ-Tests"],
    "it_it": ["Elementi", "Sostanze solide", "Sostanze liquide", "Gas", "Materiali e intermedi",
              "Minerali, rocce e suoli", "Alberi e legno", "Arbusti", "Colture e semi", "Animali",
              "Cibo e medicine", "Macchine", "Energia e logistica", "Scienza", "Strumenti", "Combattimento",
              "Esplorazione", "Test in creativa"],
    "nl_nl": ["Elementen", "Vaste stoffen", "Vloeibare stoffen", "Gassen", "Materialen en tussenproducten",
              "Ertsen, gesteenten en bodems", "Bomen en hout", "Struiken", "Gewassen en zaden", "Dieren",
              "Voedsel en medicijnen", "Machines", "Energie en logistiek", "Wetenschap", "Gereedschap", "Gevecht",
              "Verkenning", "Creatief testen"],
    "sv_se": ["Grundämnen", "Fasta ämnen", "Flytande ämnen", "Gaser", "Material och mellanprodukter",
              "Malmer, bergarter och jordar", "Träd och virke", "Buskar", "Grödor och frön", "Djur",
              "Mat och läkemedel", "Maskiner", "Energi och logistik", "Vetenskap", "Verktyg", "Strid",
              "Utforskning", "Kreativa tester"],
    "da_dk": ["Grundstoffer", "Faste stoffer", "Flydende stoffer", "Gasser", "Materialer og mellemprodukter",
              "Malme, bjergarter og jorde", "Træer og træ", "Buske", "Afgrøder og frø", "Dyr",
              "Mad og medicin", "Maskiner", "Energi og logistik", "Videnskab", "Værktøj", "Kamp",
              "Udforskning", "Kreative tests"],
    "ru_ru": ["Элементы", "Твёрдые вещества", "Жидкие вещества", "Газы", "Материалы и полупродукты",
              "Руды, породы и почвы", "Деревья и древесина", "Кустарники", "Культуры и семена", "Животные",
              "Еда и лекарства", "Машины", "Энергия и логистика", "Наука", "Инструменты", "Бой",
              "Исследование", "Творческие тесты"],
}

# One list per language, in the order of LANG's keys before the tab names.
_T = {
    "pt_br": [
        "Gerador Criativo", "Fornece energia ilimitada a todas as máquinas encostadas nele ou ligadas a ele por fio",
        "Fonte Infinita de Fluido",
        "Use um frasco, cilindro ou balde de água nele para escolher o líquido ou gás; canos, bombas e máquinas o tiram para sempre",
        "Fonte Infinita de Itens",
        "Use qualquer item nela para escolher o que ela dá; funis, esteiras e máquinas tiram esse item dela para sempre",
        "Ralo Universal", "Destrói todo item, líquido e gás enviado para ele",
        "Relógio Acelerador", "Máquinas, plantações, mudas e filhotes num raio de 8 blocos funcionam 10 vezes mais rápido",
        "Amuleto de Proteção",
        "Em qualquer lugar do inventário: sem radiação, veneno, peçonha, infecção, fogo, calor, frio ou afogamento, e sem dano dos animais do mod",
        "Varinha de Crescimento",
        "Use numa plantação, muda ou arbusto para fazê-lo crescer na hora (agachado: tudo num raio de 3 blocos); use num filhote para torná-lo adulto",
        "Lupa de Depuração",
        "Use num bloco ou animal para ver no chat o que há dentro: energia, calor, conteúdo, composição, genes",
        "Construtor de Estruturas",
        "Agache e use no ar para escolher uma estrutura; use num bloco para construí-la ali, pronta para funcionar",
        "Só para testes no criativo: sem receita",
        "Gerador Criativo: até %s kJ por tick para cada máquina",
        "A Fonte Infinita de Itens dá: %s",
        "Fonte Infinita de Itens: use um item nela para escolher o que ela dá",
        "A Fonte Infinita de Fluido dá: %s",
        "Fonte Infinita de Fluido: use um frasco, cilindro ou balde de água nela para escolher o fluido",
        "Líquidos vão em frasco de vidro, gases em cilindro de aço",
        "Esvaziado",
        "Ralo Universal: destrói todo item, líquido e gás enviado para ele",
        "Relógio Acelerador: tudo num raio de %2$s blocos funciona %1$s vezes mais rápido",
        "Cresceram: %s",
        "Composição (%s unidades): %s",
        "Nada guardado dentro",
        "Estrutura: %s",
        "Construído: %s",
        "Tanque (interior 3x3x3)",
        "Compêndio: %s verbetes anotados para %s",
        "Anotações do compêndio esquecidas para %s",
    ],
    "es_es": [
        "Generador creativo", "Da corriente ilimitada a todas las máquinas que lo tocan o están unidas a él por cable",
        "Fuente infinita de fluido",
        "Usa un matraz, cilindro o cubo de agua en ella para elegir el líquido o gas; tuberías, bombas y máquinas lo toman para siempre",
        "Fuente infinita de objetos",
        "Usa cualquier objeto en ella para elegir lo que da; tolvas, cintas y máquinas toman ese objeto de ella para siempre",
        "Sumidero universal", "Destruye todo objeto, líquido y gas que se le envía",
        "Reloj acelerador", "Máquinas, cultivos, brotes y crías en 8 bloques a la redonda van 10 veces más rápido",
        "Amuleto de protección",
        "En cualquier parte del inventario: sin radiación, veneno, ponzoña, infección, fuego, calor, frío ni ahogamiento, y sin daño de los animales del mod",
        "Varita de crecimiento",
        "Úsala en un cultivo, brote o arbusto para que crezca al instante (agachado: todo en 3 bloques); úsala en una cría para hacerla adulta",
        "Lupa de depuración",
        "Úsala en un bloque o animal para ver en el chat lo que hay dentro: energía, calor, contenido, composición, genes",
        "Constructor de estructuras",
        "Agáchate y úsalo en el aire para elegir una estructura; úsalo en un bloque para construirla allí, lista para funcionar",
        "Solo para pruebas en creativo: sin receta",
        "Generador creativo: hasta %s kJ por tick para cada máquina",
        "La fuente infinita de objetos da: %s",
        "Fuente infinita de objetos: usa un objeto en ella para elegir lo que da",
        "La fuente infinita de fluido da: %s",
        "Fuente infinita de fluido: usa un matraz, cilindro o cubo de agua en ella para elegir el fluido",
        "Los líquidos van en matraz de vidrio, los gases en cilindro de acero",
        "Vaciado",
        "Sumidero universal: destruye todo objeto, líquido y gas que se le envía",
        "Reloj acelerador: todo en %2$s bloques a la redonda va %1$s veces más rápido",
        "Han crecido: %s",
        "Composición (%s unidades): %s",
        "No guarda nada dentro",
        "Estructura: %s",
        "Construido: %s",
        "Tanque (interior 3x3x3)",
        "Compendio: %s entradas anotadas para %s",
        "Notas del compendio olvidadas para %s",
    ],
    "fr_fr": [
        "Générateur créatif", "Donne un courant illimité à toutes les machines qui le touchent ou y sont reliées par fil",
        "Source infinie de fluide",
        "Utilisez une fiole, une bouteille ou un seau d'eau dessus pour choisir le liquide ou le gaz ; tuyaux, pompes et machines y puisent sans fin",
        "Source infinie d'objets",
        "Utilisez un objet dessus pour choisir ce qu'elle donne ; entonnoirs, tapis et machines y prennent cet objet sans fin",
        "Néant universel", "Détruit tout objet, liquide et gaz qu'on y envoie",
        "Horloge accélératrice", "Machines, cultures, pousses et petits dans un rayon de 8 blocs vont 10 fois plus vite",
        "Amulette de protection",
        "N'importe où dans l'inventaire : ni radiation, poison, venin, infection, feu, chaleur, froid ou noyade, ni dégâts des animaux du mod",
        "Baguette de croissance",
        "Utilisez-la sur une culture, une pousse ou un arbuste pour le faire pousser aussitôt (accroupi : tout dans 3 blocs) ; sur un petit pour en faire un adulte",
        "Loupe de débogage",
        "Utilisez-la sur un bloc ou un animal pour voir dans le chat ce qu'il contient : énergie, chaleur, contenu, composition, gènes",
        "Constructeur de structures",
        "Accroupi, utilisez-le dans le vide pour choisir une structure ; sur un bloc pour la construire là, prête à fonctionner",
        "Seulement pour les tests en créatif : sans recette",
        "Générateur créatif : jusqu'à %s kJ par tick pour chaque machine",
        "La source infinie d'objets donne : %s",
        "Source infinie d'objets : utilisez un objet dessus pour choisir ce qu'elle donne",
        "La source infinie de fluide donne : %s",
        "Source infinie de fluide : utilisez une fiole, une bouteille ou un seau d'eau dessus pour choisir le fluide",
        "Les liquides vont dans une fiole en verre, les gaz dans une bouteille en acier",
        "Vidé",
        "Néant universel : détruit tout objet, liquide et gaz qu'on y envoie",
        "Horloge accélératrice : tout dans un rayon de %2$s blocs va %1$s fois plus vite",
        "Ont poussé : %s",
        "Composition (%s unités) : %s",
        "Rien à l'intérieur",
        "Structure : %s",
        "Construit : %s",
        "Réservoir (intérieur 3x3x3)",
        "Compendium : %s entrées notées pour %s",
        "Notes du compendium oubliées pour %s",
    ],
    "de_de": [
        "Kreativ-Generator", "Gibt unbegrenzt Strom an alle Maschinen, die ihn berühren oder per Draht mit ihm verbunden sind",
        "Unendliche Fluidquelle",
        "Benutze einen Kolben, eine Flasche oder einen Wassereimer daran, um Flüssigkeit oder Gas zu wählen; Rohre, Pumpen und Maschinen entnehmen es endlos",
        "Unendliche Gegenstandsquelle",
        "Benutze einen beliebigen Gegenstand daran, um zu wählen, was sie gibt; Trichter, Bänder und Maschinen entnehmen ihn endlos",
        "Universelle Leere", "Vernichtet jeden Gegenstand, jede Flüssigkeit und jedes Gas, das hineingeschickt wird",
        "Beschleunigeruhr", "Maschinen, Pflanzen, Setzlinge und Jungtiere im Umkreis von 8 Blöcken laufen 10-mal so schnell",
        "Schutzamulett",
        "Irgendwo im Inventar: keine Strahlung, kein Gift, keine Infektion, kein Feuer, keine Hitze, Kälte oder Ertrinken, kein Schaden durch Tiere des Mods",
        "Wachstumsstab",
        "An einer Pflanze, einem Setzling oder Strauch benutzen, damit er sofort wächst (schleichend: alles im Umkreis von 3 Blöcken); an einem Jungtier, damit es erwachsen wird",
        "Debug-Lupe",
        "An einem Block oder Tier benutzen, um im Chat zu sehen, was darin ist: Energie, Wärme, Inhalt, Zusammensetzung, Gene",
        "Strukturbauer",
        "Schleichend in die Luft benutzen, um eine Struktur zu wählen; an einem Block benutzen, um sie dort betriebsbereit zu bauen",
        "Nur für Tests im Kreativmodus: kein Rezept",
        "Kreativ-Generator: bis zu %s kJ pro Tick für jede Maschine",
        "Die unendliche Gegenstandsquelle gibt: %s",
        "Unendliche Gegenstandsquelle: benutze einen Gegenstand daran, um zu wählen, was sie gibt",
        "Die unendliche Fluidquelle gibt: %s",
        "Unendliche Fluidquelle: benutze einen Kolben, eine Flasche oder einen Wassereimer daran, um das Fluid zu wählen",
        "Flüssigkeiten kommen in einen Glaskolben, Gase in eine Stahlflasche",
        "Geleert",
        "Universelle Leere: vernichtet jeden Gegenstand, jede Flüssigkeit und jedes Gas, das hineingeschickt wird",
        "Beschleunigeruhr: alles im Umkreis von %2$s Blöcken läuft %1$s-mal so schnell",
        "Gewachsen: %s",
        "Zusammensetzung (%s Einheiten): %s",
        "Nichts darin gespeichert",
        "Struktur: %s",
        "Gebaut: %s",
        "Tank (innen 3x3x3)",
        "Kompendium: %s Einträge für %s notiert",
        "Kompendium-Notizen von %s vergessen",
    ],
    "it_it": [
        "Generatore creativo", "Dà corrente illimitata a tutte le macchine che lo toccano o vi sono collegate con filo",
        "Sorgente infinita di fluido",
        "Usa un'ampolla, una bombola o un secchio d'acqua su di essa per scegliere il liquido o gas; tubi, pompe e macchine lo prelevano per sempre",
        "Sorgente infinita di oggetti",
        "Usa un oggetto qualsiasi su di essa per scegliere cosa dà; tramogge, nastri e macchine ne prelevano quell'oggetto per sempre",
        "Vuoto universale", "Distrugge ogni oggetto, liquido e gas che vi viene inviato",
        "Orologio acceleratore", "Macchine, colture, germogli e cuccioli entro 8 blocchi vanno 10 volte più veloci",
        "Amuleto di protezione",
        "Ovunque nell'inventario: niente radiazioni, veleno, infezione, fuoco, caldo, freddo o annegamento, e nessun danno dagli animali del mod",
        "Bacchetta della crescita",
        "Usala su una coltura, un germoglio o un arbusto per farlo crescere subito (accovacciato: tutto entro 3 blocchi); su un cucciolo per renderlo adulto",
        "Lente di debug",
        "Usala su un blocco o animale per vedere in chat cosa c'è dentro: energia, calore, contenuto, composizione, geni",
        "Costruttore di strutture",
        "Accovacciati e usalo in aria per scegliere una struttura; usalo su un blocco per costruirla lì, pronta a funzionare",
        "Solo per test in creativa: senza ricetta",
        "Generatore creativo: fino a %s kJ per tick a ogni macchina",
        "La sorgente infinita di oggetti dà: %s",
        "Sorgente infinita di oggetti: usa un oggetto su di essa per scegliere cosa dà",
        "La sorgente infinita di fluido dà: %s",
        "Sorgente infinita di fluido: usa un'ampolla, una bombola o un secchio d'acqua su di essa per scegliere il fluido",
        "I liquidi vanno in un'ampolla di vetro, i gas in una bombola d'acciaio",
        "Svuotato",
        "Vuoto universale: distrugge ogni oggetto, liquido e gas che vi viene inviato",
        "Orologio acceleratore: tutto entro %2$s blocchi va %1$s volte più veloce",
        "Cresciuti: %s",
        "Composizione (%s unità): %s",
        "Niente dentro",
        "Struttura: %s",
        "Costruito: %s",
        "Serbatoio (interno 3x3x3)",
        "Compendio: %s voci annotate per %s",
        "Note del compendio dimenticate per %s",
    ],
    "nl_nl": [
        "Creatieve generator", "Geeft onbeperkt stroom aan elke machine die hem raakt of er met draad aan vastzit",
        "Oneindige vloeistofbron",
        "Gebruik er een kolf, cilinder of emmer water op om de vloeistof of het gas te kiezen; buizen, pompen en machines halen het er eindeloos uit",
        "Oneindige voorwerpbron",
        "Gebruik er een voorwerp op om te kiezen wat hij geeft; trechters, banden en machines halen dat voorwerp er eindeloos uit",
        "Universele leegte", "Vernietigt elk voorwerp, elke vloeistof en elk gas dat erin gaat",
        "Versnellingsklok", "Machines, gewassen, zaailingen en jonge dieren binnen 8 blokken gaan 10 keer zo snel",
        "Beschermingsamulet",
        "Ergens in je inventaris: geen straling, gif, infectie, vuur, hitte, kou of verdrinking, en geen schade van dieren van de mod",
        "Groeistaf",
        "Gebruik op een gewas, zaailing of struik om het meteen te laten groeien (sluipend: alles binnen 3 blokken); op een jong dier om het volwassen te maken",
        "Debug-loep",
        "Gebruik op een blok of dier om in de chat te zien wat erin zit: energie, warmte, inhoud, samenstelling, genen",
        "Structuurbouwer",
        "Sluip en gebruik in de lucht om een structuur te kiezen; gebruik op een blok om hem daar klaar voor gebruik te bouwen",
        "Alleen voor creatief testen: geen recept",
        "Creatieve generator: tot %s kJ per tick voor elke machine",
        "De oneindige voorwerpbron geeft: %s",
        "Oneindige voorwerpbron: gebruik er een voorwerp op om te kiezen wat hij geeft",
        "De oneindige vloeistofbron geeft: %s",
        "Oneindige vloeistofbron: gebruik er een kolf, cilinder of emmer water op om de vloeistof te kiezen",
        "Vloeistoffen gaan in een glazen kolf, gassen in een stalen cilinder",
        "Leeggemaakt",
        "Universele leegte: vernietigt elk voorwerp, elke vloeistof en elk gas dat erin gaat",
        "Versnellingsklok: alles binnen %2$s blokken gaat %1$s keer zo snel",
        "Gegroeid: %s",
        "Samenstelling (%s eenheden): %s",
        "Niets opgeslagen",
        "Structuur: %s",
        "Gebouwd: %s",
        "Tank (binnenkant 3x3x3)",
        "Compendium: %s items genoteerd voor %s",
        "Compendiumnotities vergeten voor %s",
    ],
    "sv_se": [
        "Kreativ generator", "Ger obegränsad ström till varje maskin som rör den eller är kopplad till den med tråd",
        "Oändlig fluidkälla",
        "Använd en kolv, cylinder eller vattenhink på den för att välja vätska eller gas; rör, pumpar och maskiner tar den i all evighet",
        "Oändlig föremålskälla",
        "Använd valfritt föremål på den för att välja vad den ger; trattar, band och maskiner tar det föremålet i all evighet",
        "Universellt tomrum", "Förstör varje föremål, vätska och gas som skickas in",
        "Accelerationsklocka", "Maskiner, grödor, plantor och ungdjur inom 8 block går 10 gånger så fort",
        "Skyddsamulett",
        "Var som helst i förrådet: ingen strålning, gift, infektion, eld, hetta, kyla eller drunkning, och ingen skada från moddens djur",
        "Tillväxtstav",
        "Använd på en gröda, planta eller buske för att den ska växa direkt (smygande: allt inom 3 block); på ett ungdjur för att göra det vuxet",
        "Felsökningslupp",
        "Använd på ett block eller djur för att se i chatten vad som finns inuti: energi, värme, innehåll, sammansättning, gener",
        "Strukturbyggare",
        "Smyg och använd i luften för att välja struktur; använd på ett block för att bygga den där, redo att arbeta",
        "Bara för kreativa tester: inget recept",
        "Kreativ generator: upp till %s kJ per tick till varje maskin",
        "Den oändliga föremålskällan ger: %s",
        "Oändlig föremålskälla: använd ett föremål på den för att välja vad den ger",
        "Den oändliga fluidkällan ger: %s",
        "Oändlig fluidkälla: använd en kolv, cylinder eller vattenhink på den för att välja fluid",
        "Vätskor går i en glaskolv, gaser i en stålcylinder",
        "Tömd",
        "Universellt tomrum: förstör varje föremål, vätska och gas som skickas in",
        "Accelerationsklocka: allt inom %2$s block går %1$s gånger så fort",
        "Vuxit: %s",
        "Sammansättning (%s enheter): %s",
        "Inget lagrat inuti",
        "Struktur: %s",
        "Byggd: %s",
        "Tank (insida 3x3x3)",
        "Kompendium: %s poster antecknade för %s",
        "Kompendiets anteckningar glömda för %s",
    ],
    "da_dk": [
        "Kreativ generator", "Giver ubegrænset strøm til hver maskine, der rører den eller er forbundet til den med ledning",
        "Uendelig fluidkilde",
        "Brug en kolbe, cylinder eller vandspand på den for at vælge væske eller gas; rør, pumper og maskiner tager den i al evighed",
        "Uendelig genstandskilde",
        "Brug en vilkårlig genstand på den for at vælge, hvad den giver; tragte, bånd og maskiner tager den genstand i al evighed",
        "Universelt tomrum", "Ødelægger hver genstand, væske og gas, der sendes ind",
        "Accelerationsur", "Maskiner, afgrøder, skud og unge dyr inden for 8 blokke kører 10 gange så hurtigt",
        "Beskyttelsesamulet",
        "Hvor som helst i inventaret: ingen stråling, gift, infektion, ild, varme, kulde eller drukning, og ingen skade fra moddets dyr",
        "Vækststav",
        "Brug på en afgrøde, et skud eller en busk for at få den til at vokse med det samme (snigende: alt inden for 3 blokke); på et ungt dyr for at gøre det voksent",
        "Fejlfindingslup",
        "Brug på en blok eller et dyr for at se i chatten, hvad der er indeni: energi, varme, indhold, sammensætning, gener",
        "Strukturbygger",
        "Snig og brug i luften for at vælge en struktur; brug på en blok for at bygge den der, klar til brug",
        "Kun til kreative tests: ingen opskrift",
        "Kreativ generator: op til %s kJ pr. tick til hver maskine",
        "Den uendelige genstandskilde giver: %s",
        "Uendelig genstandskilde: brug en genstand på den for at vælge, hvad den giver",
        "Den uendelige fluidkilde giver: %s",
        "Uendelig fluidkilde: brug en kolbe, cylinder eller vandspand på den for at vælge fluid",
        "Væsker kommer i en glaskolbe, gasser i en stålcylinder",
        "Tømt",
        "Universelt tomrum: ødelægger hver genstand, væske og gas, der sendes ind",
        "Accelerationsur: alt inden for %2$s blokke kører %1$s gange så hurtigt",
        "Vokset: %s",
        "Sammensætning (%s enheder): %s",
        "Intet gemt indeni",
        "Struktur: %s",
        "Bygget: %s",
        "Tank (inderside 3x3x3)",
        "Kompendium: %s opslag noteret for %s",
        "Kompendiets noter glemt for %s",
    ],
    "ru_ru": [
        "Творческий генератор", "Даёт неограниченный ток всем машинам, которые касаются его или соединены с ним проводом",
        "Бесконечный источник жидкости",
        "Примените к нему колбу, баллон или ведро воды, чтобы выбрать жидкость или газ; трубы, насосы и машины берут их бесконечно",
        "Бесконечный источник предметов",
        "Примените к нему любой предмет, чтобы выбрать, что он даёт; воронки, ленты и машины берут этот предмет бесконечно",
        "Всепоглощающая пустота", "Уничтожает каждый предмет, жидкость и газ, отправленные в неё",
        "Часы-ускоритель", "Машины, посевы, саженцы и детёныши в радиусе 8 блоков работают в 10 раз быстрее",
        "Защитный амулет",
        "Где угодно в инвентаре: никакой радиации, яда, инфекции, огня, жары, холода или утопления и никакого урона от животных мода",
        "Жезл роста",
        "Примените к посеву, саженцу или кусту, чтобы он сразу вырос (крадучись: всё в радиусе 3 блоков); к детёнышу, чтобы сделать его взрослым",
        "Отладочная лупа",
        "Примените к блоку или животному, чтобы увидеть в чате, что внутри: энергию, тепло, содержимое, состав, гены",
        "Строитель структур",
        "Крадучись примените в воздух, чтобы выбрать структуру; примените к блоку, чтобы построить её там готовой к работе",
        "Только для творческих тестов: без рецепта",
        "Творческий генератор: до %s кДж за тик каждой машине",
        "Бесконечный источник предметов даёт: %s",
        "Бесконечный источник предметов: примените к нему предмет, чтобы выбрать, что он даёт",
        "Бесконечный источник жидкости даёт: %s",
        "Бесконечный источник жидкости: примените к нему колбу, баллон или ведро воды, чтобы выбрать жидкость",
        "Жидкости наливают в стеклянную колбу, газы в стальной баллон",
        "Очищено",
        "Всепоглощающая пустота: уничтожает каждый предмет, жидкость и газ, отправленные в неё",
        "Часы-ускоритель: всё в радиусе %2$s блоков работает в %1$s раз быстрее",
        "Выросло: %s",
        "Состав (%s единиц): %s",
        "Внутри ничего нет",
        "Структура: %s",
        "Построено: %s",
        "Резервуар (внутри 3x3x3)",
        "Справочник: %s записей отмечено для %s",
        "Записи справочника забыты для %s",
    ],
}


def translations(code):
    """English -> translation for these strings in one language."""
    vals = _T.get(code, []) + _TABS_T.get(code, [])
    assert not vals or len(vals) == len(_K), (code, len(vals), len(_K))
    return {LANG[k]: t for k, t in zip(_K, vals)}


# ---------- textures ----------

def _img():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def _rect(px, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(c) + (255,) if len(c) == 3 else tuple(c)


def _frame(face):
    """The creative frame every test block shares: dark violet with a magenta checker border."""
    img = _img()
    px = img.load()
    _rect(px, 0, 0, 15, 15, (36, 18, 54))
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            px[x, y] = (200, 60, 200, 255) if (x + y) % 2 == 0 else (120, 30, 140, 255)
    _rect(px, 1, 1, 14, 1, (70, 34, 96))
    _rect(px, 1, 14, 14, 14, (24, 12, 36))
    face(px)
    return img


def _generator(px):
    bolt = [(9, 2), (8, 3), (8, 4), (7, 5), (7, 6), (6, 7), (6, 8), (7, 8), (8, 8), (9, 8), (8, 9), (8, 10), (7, 11), (7, 12),
            (6, 13)]
    for (x, y) in bolt:
        px[x, y] = (255, 220, 60, 255)
        if x + 1 < 15:
            px[x + 1, y] = (255, 180, 30, 255)
    for (x, y) in ((3, 3), (12, 12), (12, 3), (3, 12)):
        px[x, y] = (255, 120, 255, 255)


def _fluid(px):
    for y in range(4, 13):
        for x in range(4, 12):
            d = (x - 7.5) ** 2 / 14 + (y - 9) ** 2 / 12
            if d < 1 and not (y < 7 and abs(x - 7.5) > (y - 3)):
                px[x, y] = (40, 120 + 8 * (y - 4), 230, 255)
    px[6, 8] = px[6, 9] = (200, 240, 255, 255)


def _items(px):
    _rect(px, 3, 5, 12, 12, (150, 100, 50))
    _rect(px, 3, 5, 12, 6, (110, 70, 30))
    _rect(px, 7, 7, 8, 9, (230, 220, 120))
    for x in (4, 5, 10, 11):
        px[x, 3] = (255, 120, 255, 255)
    px[6, 2] = px[9, 2] = (255, 120, 255, 255)


def _void(px):
    import math
    for y in range(2, 14):
        for x in range(2, 14):
            r = math.hypot(x - 7.5, y - 7.5)
            if r < 6:
                a = math.atan2(y - 7.5, x - 7.5) + r * 0.7
                v = 0.5 + 0.5 * math.sin(3 * a)
                px[x, y] = (int(20 + 120 * v * (1 - r / 6)), int(5 + 20 * v), int(30 + 140 * v * (1 - r / 6)), 255)
    _rect(px, 7, 7, 8, 8, (0, 0, 0))


def _clock(px):
    import math
    for y in range(2, 14):
        for x in range(2, 14):
            r = math.hypot(x - 7.5, y - 7.5)
            if r < 5.8:
                px[x, y] = (230, 200, 90, 255) if r > 4.8 else (245, 240, 220, 255)
    for (x, y) in ((7, 3), (7, 4), (7, 5), (7, 6), (7, 7), (8, 7), (9, 7), (10, 7)):
        px[x, y] = (40, 30, 60, 255)
    for (x, y) in ((7, 3), (12, 7), (7, 12), (3, 7)):
        px[x, y] = (200, 60, 200, 255)


FACES = {"creative_generator": _generator, "creative_fluid_source": _fluid, "creative_item_source": _items,
         "creative_void": _void, "creative_accelerator": _clock}


def _amulet():
    img = _img()
    px = img.load()
    for i, (x, y) in enumerate([(3, 1), (4, 2), (4, 3), (5, 4), (5, 5), (6, 6), (12, 1), (11, 2), (11, 3), (10, 4), (10, 5),
                                (9, 6)]):
        px[x, y] = (230, 190, 70, 255)
    _rect(px, 5, 7, 10, 13, (200, 150, 40))
    _rect(px, 6, 8, 9, 12, (230, 70, 200))
    px[7, 9] = (255, 200, 255, 255)
    return img


def _wand():
    img = _img()
    px = img.load()
    for i in range(10):
        px[2 + i, 13 - i] = (120, 80, 40, 255)
        px[3 + i, 13 - i] = (90, 60, 30, 255)
    for (x, y) in ((12, 2), (13, 3), (11, 2), (13, 1), (12, 1), (14, 2), (12, 3)):
        px[x, y] = (90, 220, 90, 255)
    px[13, 2] = (230, 255, 160, 255)
    for (x, y) in ((10, 1), (14, 4), (9, 3)):
        px[x, y] = (255, 230, 120, 255)
    return img


def _magnifier():
    import math
    img = _img()
    px = img.load()
    for y in range(1, 11):
        for x in range(4, 14):
            r = math.hypot(x - 8.5, y - 5.5)
            if r < 4.6:
                px[x, y] = (110, 40, 130, 255) if r > 3.6 else (200, 170, 240, 200)
    px[7, 4] = (255, 255, 255, 255)
    for i in range(5):
        px[5 - i, 10 + i] = (60, 40, 30, 255)
        px[6 - i, 10 + i] = (90, 60, 40, 255)
    return img


def _builder():
    img = _img()
    px = img.load()
    _rect(px, 2, 3, 13, 12, (40, 90, 170))
    for x in range(2, 14, 3):
        _rect(px, x, 3, x, 12, (70, 120, 200))
    for y in range(3, 13, 3):
        _rect(px, 2, y, 13, y, (70, 120, 200))
    _rect(px, 5, 6, 10, 9, (230, 240, 255))
    _rect(px, 6, 7, 9, 8, (40, 90, 170))
    _rect(px, 2, 2, 13, 2, (200, 60, 200))
    _rect(px, 2, 13, 13, 13, (200, 60, 200))
    return img


ITEM_TEX = {"protection_amulet": _amulet, "growth_wand": _wand, "debug_magnifier": _magnifier, "structure_builder": _builder}


def generate_creative(assets, lang, mod):
    """Blockstates, models, textures and lang for the creative test blocks and items."""
    for b in BLOCKS:
        _frame(FACES[b]).save(assets / "textures" / "block" / f"{b}.png")
        (assets / "blockstates" / f"{b}.json").write_text(json.dumps({"variants": {"": {"model": f"{mod}:block/{b}"}}}, indent=2) + "\n")
        (assets / "models" / "block" / f"{b}.json").write_text(json.dumps(
            {"parent": "minecraft:block/cube_all", "textures": {"all": f"{mod}:block/{b}"}}, indent=2) + "\n")
        (assets / "models" / "item" / f"{b}.json").write_text(json.dumps({"parent": f"{mod}:block/{b}"}, indent=2) + "\n")
    for i in ITEMS:
        ITEM_TEX[i]().save(assets / "textures" / "item" / f"{i}.png")
        (assets / "models" / "item" / f"{i}.json").write_text(json.dumps(
            {"parent": "minecraft:item/handheld" if i == "growth_wand" else "minecraft:item/generated",
             "textures": {"layer0": f"{mod}:item/{i}"}}, indent=2) + "\n")
    lang.update(LANG)
    return BLOCKS + ITEMS
