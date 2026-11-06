"""Multiblock structures (3.3.0) in the mod's other languages: block names, screen lines, the guide's templates (T) and the
Structures chapter (same paragraphs and headings as multiblock.GUIDE_*)."""

CODES = ("pt_br", "es_es", "fr_fr", "de_de", "it_it", "nl_nl", "sv_se", "da_dk", "ru_ru")

BLOCKS = {
    "pt_br": {
        "refractory_brick": ("Tijolo Refratário", "Tijolo de argila refratária para a casca de um alto-forno; aguenta 1.600 °C"),
        "tuyere": ("Ventaneira", "Bocal de cobre refrigerado a água para a base do alto-forno: o sopro entra por aqui"),
        "structure_port": ("Porta de Estrutura", "Ponha na casca de uma estrutura: os funis chegam ao controlador por ela"),
        "silica_brick": ("Tijolo de Sílica", "Tijolo de areia de quartzo para a bateria de fornos de coque: firme mesmo em brasa"),
    },
    "es_es": {
        "refractory_brick": ("Ladrillo refractario", "Ladrillo de arcilla refractaria para el alto horno; aguanta 1600 °C"),
        "tuyere": ("Tobera", "Boquilla de cobre refrigerada por agua para la base del alto horno: por aquí entra el viento"),
        "structure_port": ("Puerto de estructura", "Ponlo en el casco de una estructura: las tolvas llegan al controlador por él"),
        "silica_brick": ("Ladrillo de sílice", "Ladrillo de arena de cuarzo para la batería de hornos de coque: firme al rojo"),
    },
    "fr_fr": {
        "refractory_brick": ("Brique réfractaire", "Brique d'argile réfractaire pour l'enveloppe d'un haut fourneau ; tient 1 600 °C"),
        "tuyere": ("Tuyère", "Buse en cuivre refroidie à l'eau pour le bas du haut fourneau : le vent entre par ici"),
        "structure_port": ("Port de structure", "À placer dans l'enveloppe d'une structure : les entonnoirs atteignent le contrôleur par lui"),
        "silica_brick": ("Brique de silice", "Brique de sable de quartz pour une batterie de fours à coke : solide même au rouge"),
    },
    "de_de": {
        "refractory_brick": ("Schamottestein", "Feuerfester Ziegel für den Mantel eines Hochofens; hält 1.600 °C aus"),
        "tuyere": ("Blasform", "Wassergekühlte Kupferdüse für den Fuß des Hochofens: hier geht der Wind hinein"),
        "structure_port": ("Strukturanschluss", "In die Hülle einer Struktur setzen: Trichter erreichen darüber den Regler"),
        "silica_brick": ("Silikastein", "Quarzsandziegel für eine Koksofenbatterie: bleibt fest, auch wenn er glüht"),
    },
    "it_it": {
        "refractory_brick": ("Mattone refrattario", "Mattone di argilla refrattaria per il mantello di un altoforno; regge 1600 °C"),
        "tuyere": ("Ugello", "Ugello di rame raffreddato ad acqua per la base dell'altoforno: da qui entra il vento"),
        "structure_port": ("Porta di struttura", "Mettila nel guscio di una struttura: le tramogge raggiungono il controllore da qui"),
        "silica_brick": ("Mattone di silice", "Mattone di sabbia di quarzo per una batteria di forni a coke: saldo anche al calor rosso"),
    },
    "nl_nl": {
        "refractory_brick": ("Vuurvaste steen", "Steen van vuurvaste klei voor de mantel van een hoogoven; kan 1600 °C aan"),
        "tuyere": ("Blaasvorm", "Watergekoelde koperen mond voor de voet van de hoogoven: hier gaat de wind in"),
        "structure_port": ("Structuurpoort", "Zet hem in de wand van een structuur: trechters bereiken de regelaar erdoor"),
        "silica_brick": ("Silicasteen", "Steen van kwartszand voor een cokesovenbatterij: blijft stevig, ook gloeiend"),
    },
    "sv_se": {
        "refractory_brick": ("Eldfast tegel", "Tegel av eldfast lera för masugnens mantel; tål 1 600 °C"),
        "tuyere": ("Form", "Vattenkyld kopparform för masugnens fot: här blåses luften in"),
        "structure_port": ("Strukturport", "Sätt den i en strukturs skal: trattar når styrenheten genom den"),
        "silica_brick": ("Silikategel", "Tegel av kvartssand för ett koksverk: håller även glödhet"),
    },
    "da_dk": {
        "refractory_brick": ("Ildfast sten", "Sten af ildfast ler til en højovns kappe; tåler 1.600 °C"),
        "tuyere": ("Blæseform", "Vandkølet kobberdyse til højovnens fod: her blæses luften ind"),
        "structure_port": ("Strukturport", "Sæt den i en strukturs skal: tragte når styreenheden gennem den"),
        "silica_brick": ("Silikasten", "Sten af kvartssand til et koksværk: holder sig fast, også gloende"),
    },
    "ru_ru": {
        "refractory_brick": ("Огнеупорный кирпич", "Шамотный кирпич для кожуха доменной печи; выдерживает 1600 °C"),
        "tuyere": ("Фурма", "Медное сопло с водяным охлаждением для низа доменной печи: сюда подаётся дутьё"),
        "structure_port": ("Порт конструкции", "Вставьте в оболочку конструкции: воронки достают через него до контроллера"),
        "silica_brick": ("Динасовый кирпич", "Кирпич из кварцевого песка для коксовой батареи: прочен даже раскалённым"),
    },
}

_KEYS = ["blast_furnace", "coke_battery", "coke_battery.gas", "missing", "missing.floor", "too_small.blast_furnace",
         "too_small.coke_battery", "no_tuyere", "decimal", "layer", "floor", "smallest", "largest", "drag"]
_LANG_ROWS = {
    "pt_br": ["Alto-forno %sx%sx%s: velocidade x%s, calor por item %s%%", "Bateria de %s fornos: velocidade x%s, calor por item %s%%",
              "Bateria de %s fornos: velocidade x%s, aquecida pelo próprio gás", "Estrutura: falta %s na camada %s",
              "Estrutura: falta %s no piso", "Alto-forno baixo demais: o interior deve ter de 3 a 10 de altura",
              "Bateria pequena demais", "O alto-forno precisa de uma Ventaneira na camada de baixo", ",",
              "Camada %s de %s", "Piso", "Menor", "Maior", "Arraste para girar"],
    "es_es": ["Alto horno %sx%sx%s: velocidad x%s, calor por pieza %s%%", "Batería de %s hornos: velocidad x%s, calor por pieza %s%%",
              "Batería de %s hornos: velocidad x%s, calentada con su propio gas", "Estructura: falta %s en la capa %s",
              "Estructura: falta %s en el suelo", "Alto horno demasiado bajo: el interior debe medir de 3 a 10 de alto",
              "Batería demasiado pequeña", "El alto horno necesita una Tobera en la capa de abajo", ",",
              "Capa %s de %s", "Suelo", "Mínima", "Máxima", "Arrastra para girar"],
    "fr_fr": ["Haut fourneau %sx%sx%s : vitesse x%s, chaleur par objet %s %%", "Batterie de %s fours : vitesse x%s, chaleur par objet %s %%",
              "Batterie de %s fours : vitesse x%s, chauffée par son propre gaz", "Structure : il manque %s à la couche %s",
              "Structure : il manque %s au sol", "Haut fourneau trop bas : l'intérieur doit faire de 3 à 10 de haut",
              "Batterie trop petite", "Le haut fourneau a besoin d'une Tuyère dans sa couche du bas", ",",
              "Couche %s sur %s", "Sol", "La plus petite", "La plus grande", "Faites glisser pour tourner"],
    "de_de": ["Hochofen %sx%sx%s: Tempo x%s, Wärme je Stück %s %%", "Batterie mit %s Öfen: Tempo x%s, Wärme je Stück %s %%",
              "Batterie mit %s Öfen: Tempo x%s, beheizt mit eigenem Gas", "Struktur: %s fehlt in Schicht %s",
              "Struktur: %s fehlt im Boden", "Hochofen zu niedrig: der Innenraum muss 3 bis 10 hoch sein",
              "Batterie zu klein", "Der Hochofen braucht eine Blasform in der untersten Schicht", ",",
              "Schicht %s von %s", "Boden", "Kleinste", "Größte", "Ziehen zum Drehen"],
    "it_it": ["Altoforno %sx%sx%s: velocità x%s, calore per pezzo %s%%", "Batteria di %s forni: velocità x%s, calore per pezzo %s%%",
              "Batteria di %s forni: velocità x%s, scaldata dal proprio gas", "Struttura: manca %s allo strato %s",
              "Struttura: manca %s nel pavimento", "Altoforno troppo basso: l'interno deve essere alto da 3 a 10",
              "Batteria troppo piccola", "L'altoforno ha bisogno di un Ugello nello strato più basso", ",",
              "Strato %s di %s", "Pavimento", "Minima", "Massima", "Trascina per ruotare"],
    "nl_nl": ["Hoogoven %sx%sx%s: snelheid x%s, warmte per stuk %s%%", "Batterij van %s ovens: snelheid x%s, warmte per stuk %s%%",
              "Batterij van %s ovens: snelheid x%s, gestookt met eigen gas", "Structuur: %s ontbreekt in laag %s",
              "Structuur: %s ontbreekt in de vloer", "Hoogoven te laag: de binnenkant moet 3 tot 10 hoog zijn",
              "Batterij te klein", "De hoogoven heeft een Blaasvorm in de onderste laag nodig", ",",
              "Laag %s van %s", "Vloer", "Kleinste", "Grootste", "Sleep om te draaien"],
    "sv_se": ["Masugn %sx%sx%s: fart x%s, värme per föremål %s %%", "Koksverk med %s ugnar: fart x%s, värme per föremål %s %%",
              "Koksverk med %s ugnar: fart x%s, eldas med egen gas", "Struktur: %s saknas i lager %s",
              "Struktur: %s saknas i golvet", "Masugnen är för låg: insidan ska vara 3 till 10 hög",
              "Koksverket är för litet", "Masugnen behöver en Form i det nedersta lagret", ",",
              "Lager %s av %s", "Golv", "Minsta", "Största", "Dra för att vrida"],
    "da_dk": ["Højovn %sx%sx%s: fart x%s, varme pr. genstand %s %%", "Koksværk med %s ovne: fart x%s, varme pr. genstand %s %%",
              "Koksværk med %s ovne: fart x%s, fyret med egen gas", "Struktur: %s mangler i lag %s",
              "Struktur: %s mangler i gulvet", "Højovnen er for lav: indersiden skal være 3 til 10 høj",
              "Koksværket er for lille", "Højovnen skal have en Blæseform i det nederste lag", ",",
              "Lag %s af %s", "Gulv", "Mindste", "Største", "Træk for at dreje"],
    "ru_ru": ["Домна %sx%sx%s: скорость x%s, тепло на предмет %s%%", "Батарея из %s печей: скорость x%s, тепло на предмет %s%%",
              "Батарея из %s печей: скорость x%s, греется своим газом", "Конструкция: нет блока «%s» в слое %s",
              "Конструкция: нет блока «%s» в полу", "Домна слишком низкая: внутри должно быть от 3 до 10 в высоту",
              "Батарея слишком мала", "Домне нужна Фурма в нижнем слое", ",",
              "Слой %s из %s", "Пол", "Наименьшая", "Наибольшая", "Тяните, чтобы вращать"],
}
_PREFIX = {"layer": "gui.periodic_elements.guide.", "floor": "gui.periodic_elements.guide.", "smallest": "gui.periodic_elements.guide.",
           "largest": "gui.periodic_elements.guide.", "drag": "gui.periodic_elements.guide."}
LANG = {code: {_PREFIX.get(k, "gui.periodic_elements.structure.") + k: v for k, v in zip(_KEYS, rows, strict=True)}
        for code, rows in _LANG_ROWS.items()}

T = {
    "pt_br": {"structures": "Estruturas", "gain_furnace": "Interior {a}x{a}x{h}: {blocks} blocos, velocidade x{speed}, calor por item {loss}%",
              "gain_battery": "{n} fornos: {blocks} blocos, velocidade x{speed}, calor por item {loss}%",
              "gain_battery_gas": "{n} fornos: {blocks} blocos, velocidade x{speed}, sem combustível",
              "assembly": "Camada por camada (a menor; repita as camadas do meio para subir mais):",
              "assembly_battery": "Camada por camada (um forno; repita o forno e sua parede para pôr mais):",
              "view": "A estrutura em 3D:", "blocks_head": "Blocos"},
    "es_es": {"structures": "Estructuras", "gain_furnace": "Interior {a}x{a}x{h}: {blocks} bloques, velocidad x{speed}, calor por pieza {loss}%",
              "gain_battery": "{n} hornos: {blocks} bloques, velocidad x{speed}, calor por pieza {loss}%",
              "gain_battery_gas": "{n} hornos: {blocks} bloques, velocidad x{speed}, sin combustible",
              "assembly": "Capa a capa (la mínima; repite las capas del medio para subirla):",
              "assembly_battery": "Capa a capa (un horno; repite el horno y su pared para añadir más):",
              "view": "La estructura en 3D:", "blocks_head": "Bloques"},
    "fr_fr": {"structures": "Structures", "gain_furnace": "Intérieur {a}x{a}x{h} : {blocks} blocs, vitesse x{speed}, chaleur par objet {loss} %",
              "gain_battery": "{n} fours : {blocks} blocs, vitesse x{speed}, chaleur par objet {loss} %",
              "gain_battery_gas": "{n} fours : {blocks} blocs, vitesse x{speed}, sans combustible",
              "assembly": "Couche par couche (la plus petite ; répétez les couches du milieu pour monter plus haut) :",
              "assembly_battery": "Couche par couche (un four ; répétez le four et son mur pour en ajouter) :",
              "view": "La structure en 3D :", "blocks_head": "Blocs"},
    "de_de": {"structures": "Strukturen", "gain_furnace": "Innen {a}x{a}x{h}: {blocks} Blöcke, Tempo x{speed}, Wärme je Stück {loss} %",
              "gain_battery": "{n} Öfen: {blocks} Blöcke, Tempo x{speed}, Wärme je Stück {loss} %",
              "gain_battery_gas": "{n} Öfen: {blocks} Blöcke, Tempo x{speed}, kein Brennstoff",
              "assembly": "Schicht für Schicht (die kleinste; mittlere Schichten wiederholen, um höher zu bauen):",
              "assembly_battery": "Schicht für Schicht (ein Ofen; Ofen und Wand wiederholen für mehr):",
              "view": "Die Struktur in 3D:", "blocks_head": "Blöcke"},
    "it_it": {"structures": "Strutture", "gain_furnace": "Interno {a}x{a}x{h}: {blocks} blocchi, velocità x{speed}, calore per pezzo {loss}%",
              "gain_battery": "{n} forni: {blocks} blocchi, velocità x{speed}, calore per pezzo {loss}%",
              "gain_battery_gas": "{n} forni: {blocks} blocchi, velocità x{speed}, senza combustibile",
              "assembly": "Strato per strato (la più piccola; ripeti gli strati di mezzo per salire):",
              "assembly_battery": "Strato per strato (un forno; ripeti il forno e la sua parete per aggiungerne):",
              "view": "La struttura in 3D:", "blocks_head": "Blocchi"},
    "nl_nl": {"structures": "Structuren", "gain_furnace": "Binnen {a}x{a}x{h}: {blocks} blokken, snelheid x{speed}, warmte per stuk {loss}%",
              "gain_battery": "{n} ovens: {blocks} blokken, snelheid x{speed}, warmte per stuk {loss}%",
              "gain_battery_gas": "{n} ovens: {blocks} blokken, snelheid x{speed}, geen brandstof",
              "assembly": "Laag voor laag (de kleinste; herhaal de middelste lagen om hoger te bouwen):",
              "assembly_battery": "Laag voor laag (één oven; herhaal de oven en zijn wand voor meer):",
              "view": "De structuur in 3D:", "blocks_head": "Blokken"},
    "sv_se": {"structures": "Strukturer", "gain_furnace": "Insida {a}x{a}x{h}: {blocks} block, fart x{speed}, värme per föremål {loss} %",
              "gain_battery": "{n} ugnar: {blocks} block, fart x{speed}, värme per föremål {loss} %",
              "gain_battery_gas": "{n} ugnar: {blocks} block, fart x{speed}, inget bränsle",
              "assembly": "Lager för lager (den minsta; upprepa mittlagren för att bygga högre):",
              "assembly_battery": "Lager för lager (en ugn; upprepa ugnen och dess vägg för fler):",
              "view": "Strukturen i 3D:", "blocks_head": "Block"},
    "da_dk": {"structures": "Strukturer", "gain_furnace": "Inderside {a}x{a}x{h}: {blocks} blokke, fart x{speed}, varme pr. genstand {loss} %",
              "gain_battery": "{n} ovne: {blocks} blokke, fart x{speed}, varme pr. genstand {loss} %",
              "gain_battery_gas": "{n} ovne: {blocks} blokke, fart x{speed}, intet brændsel",
              "assembly": "Lag for lag (den mindste; gentag de midterste lag for at bygge højere):",
              "assembly_battery": "Lag for lag (én ovn; gentag ovnen og dens væg for flere):",
              "view": "Strukturen i 3D:", "blocks_head": "Blokke"},
    "ru_ru": {"structures": "Конструкции", "gain_furnace": "Внутри {a}x{a}x{h}: {blocks} блоков, скорость x{speed}, тепло на предмет {loss}%",
              "gain_battery": "{n} печей: {blocks} блоков, скорость x{speed}, тепло на предмет {loss}%",
              "gain_battery_gas": "{n} печей: {blocks} блоков, скорость x{speed}, без топлива",
              "assembly": "Слой за слоем (наименьшая; повторите средние слои, чтобы строить выше):",
              "assembly_battery": "Слой за слоем (одна печь; повторите печь и её стенку, чтобы добавить ещё):",
              "view": "Конструкция в 3D:", "blocks_head": "Блоки"},
}

GUIDE = {
    "pt_br": {
        "STRUCT_INTRO": [
            "# Como as estruturas funcionam",
            "@2 Algumas máquinas crescem e viram estruturas. Monte uma casca de blocos de invólucro em volta de um interior "
            "vazio e ponha a máquina na parede, virada para fora: ela vira o controlador e trabalha com o tamanho da "
            "estrutura inteira. Sozinha, funciona como antes. Quanto maior a estrutura, mais rápida ela fica e menos calor "
            "perde por item: velocidade = (volume interno / menor volume)^0,85, então dobrar o volume dá x1,8, não x2.",
            "@2 O controlador mede a estrutura uma vez por segundo e mostra o resultado na tela. Se faltar um bloco, a tela "
            "diz qual e em que camada, e faíscas vermelhas marcam o lugar enquanto você segura o Guia de Campo. Portas de "
            "Estrutura em qualquer lugar da casca deixam os funis entrar e sair como pelos lados da máquina: de cima para a "
            "entrada, por baixo saem os produtos, pelos lados entram combustível e o resto.",
        ],
        "STRUCT_FURNACE": [
            "# Alto-forno",
            "@2 Um Alto-Forno a Carvão Vegetal na parede de uma coluna de Tijolo Refratário. O interior tem de 1x1 a 3x3 "
            "blocos e de 3 a 10 de altura, aberto em cima; o piso e todos os blocos da parede, cantos também, são tijolo "
            "refratário, Ventaneira ou Porta de Estrutura. É preciso uma Ventaneira na camada de baixo: Foles ou uma "
            "Máquina a Vapor encostados nela sopram o forno inteiro. Com coque ele chega 100 °C mais quente que o forno sozinho.",
        ],
        "STRUCT_BATTERY": [
            "# Bateria de fornos de coque",
            "@3 Uma fila de fornos estreitos num bloco de Tijolo de Sílica, com um Forno de Coque na parede da frente de um "
            "deles. Cada forno tem 1 de largura, 3 de fundo e 2 de altura, com uma parede de aquecimento de um bloco entre "
            "eles; de 1 a 12 fornos. As paredes compartilhadas perdem menos calor, e a partir de 4 fornos a bateria queima "
            "o próprio gás de coqueria e dispensa combustível.",
        ],
    },
    "es_es": {
        "STRUCT_INTRO": [
            "# Cómo funcionan las estructuras",
            "@2 Algunas máquinas crecen hasta volverse estructuras. Levanta un casco de bloques de revestimiento alrededor de "
            "un interior vacío y pon la máquina en su pared, mirando hacia fuera: pasa a ser el controlador y trabaja con el "
            "tamaño de toda la estructura. Sola, funciona como antes. Cuanto mayor es la estructura, más rápido trabaja y "
            "menos calor pierde por pieza: velocidad = (volumen interior / volumen mínimo)^0,85; doblar el volumen da x1,8, no x2.",
            "@2 El controlador mide su estructura una vez por segundo y lo muestra en su pantalla. Si falta un bloque, la "
            "pantalla dice cuál y en qué capa, y chispas rojas marcan el sitio mientras sostienes la Guía de campo. Los "
            "Puertos de estructura, en cualquier punto del casco, dejan entrar y salir a las tolvas como los lados de la "
            "máquina: desde arriba a la entrada, por debajo salen los productos, por los lados entran el combustible y lo demás.",
        ],
        "STRUCT_FURNACE": [
            "# Alto horno",
            "@2 Un Alto horno de carbón vegetal en la pared de una columna de Ladrillo refractario. El interior mide de 1x1 a "
            "3x3 bloques y de 3 a 10 de alto, abierto arriba; el suelo y todos los bloques de la pared, esquinas incluidas, "
            "son ladrillo refractario, Tobera o Puerto de estructura. Hace falta una Tobera en la capa de abajo: unos Fuelles "
            "o una Máquina de vapor junto a ella soplan todo el horno. Con coque llega 100 °C más caliente que el horno solo.",
        ],
        "STRUCT_BATTERY": [
            "# Batería de hornos de coque",
            "@3 Una fila de hornos estrechos en un bloque de Ladrillo de sílice, con un Horno de coque en la pared frontal de "
            "uno de ellos. Cada horno mide 1 de ancho, 3 de fondo y 2 de alto, con una pared de calefacción de un bloque "
            "entre hornos; de 1 a 12 hornos. Las paredes compartidas pierden menos calor, y desde 4 hornos la batería quema "
            "su propio gas de coquería y no necesita combustible.",
        ],
    },
    "fr_fr": {
        "STRUCT_INTRO": [
            "# Comment marchent les structures",
            "@2 Certaines machines deviennent des structures. Construisez une enveloppe de blocs autour d'un intérieur vide et "
            "placez la machine dans sa paroi, tournée vers l'extérieur : elle devient le contrôleur et travaille à la taille "
            "de toute la structure. Seule, elle marche comme avant. Plus la structure est grande, plus elle va vite et moins "
            "elle perd de chaleur par objet : vitesse = (volume intérieur / plus petit volume)^0,85 ; doubler le volume donne x1,8, pas x2.",
            "@2 Le contrôleur mesure sa structure chaque seconde et l'affiche sur son écran. S'il manque un bloc, l'écran dit "
            "lequel et à quelle couche, et des étincelles rouges marquent l'endroit tant que vous tenez le Guide de terrain. "
            "Les Ports de structure, n'importe où dans l'enveloppe, laissent entrer et sortir les entonnoirs comme les côtés "
            "de la machine : par le haut vers l'entrée, par le bas sortent les produits, par les côtés le combustible et le reste.",
        ],
        "STRUCT_FURNACE": [
            "# Haut fourneau",
            "@2 Un Haut fourneau au charbon de bois dans la paroi d'une colonne de Brique réfractaire. L'intérieur fait de 1x1 "
            "à 3x3 blocs et de 3 à 10 de haut, ouvert en haut ; le sol et tous les blocs de paroi, coins compris, sont en "
            "brique réfractaire, Tuyère ou Port de structure. Il faut une Tuyère dans la couche du bas : des Soufflets ou une "
            "Machine à vapeur contre elle soufflent tout le fourneau. Au coke, il monte 100 °C plus haut que le fourneau seul.",
        ],
        "STRUCT_BATTERY": [
            "# Batterie de fours à coke",
            "@3 Une rangée de fours étroits dans un bloc de Brique de silice, un Four à coke dans le mur avant de l'un d'eux. "
            "Chaque four fait 1 de large, 3 de profondeur et 2 de haut, avec un mur de chauffe d'un bloc entre les fours ; de "
            "1 à 12 fours. Les murs partagés perdent moins de chaleur, et dès 4 fours la batterie brûle son propre gaz de "
            "cokerie et se passe de combustible.",
        ],
    },
    "de_de": {
        "STRUCT_INTRO": [
            "# Wie Strukturen funktionieren",
            "@2 Manche Maschinen wachsen zu Strukturen. Baue eine Hülle aus Mantelblöcken um einen leeren Innenraum und setze "
            "die Maschine nach außen gewandt in ihre Wand: Sie wird zum Regler und arbeitet mit der Größe der ganzen Struktur. "
            "Allein arbeitet sie wie bisher. Je größer die Struktur, desto schneller arbeitet sie und desto weniger Wärme "
            "verliert sie je Stück: Tempo = (Innenvolumen / kleinstes Volumen)^0,85; doppeltes Volumen gibt x1,8, nicht x2.",
            "@2 Der Regler misst seine Struktur einmal pro Sekunde und zeigt sie auf seinem Bildschirm. Fehlt ein Block, nennt "
            "der Bildschirm ihn und seine Schicht, und rote Funken zeigen die Stelle, solange du das Feldhandbuch hältst. "
            "Strukturanschlüsse irgendwo in der Hülle lassen Trichter hinein und hinaus wie die Seiten der Maschine: von oben "
            "in den Eingang, unten heraus die Produkte, von der Seite Brennstoff und der Rest.",
        ],
        "STRUCT_FURNACE": [
            "# Hochofen",
            "@2 Ein Holzkohle-Hochofen in der Wand einer Säule aus Schamottestein. Der Innenraum ist 1x1 bis 3x3 Blöcke groß "
            "und 3 bis 10 hoch, oben offen; Boden und alle Wandblöcke, auch die Ecken, sind Schamottestein, Blasform oder "
            "Strukturanschluss. Eine Blasform in der untersten Schicht ist Pflicht: Blasebälge oder eine Dampfmaschine daran "
            "blasen den ganzen Ofen. Mit Koks wird er 100 °C heißer als der Ofen allein.",
        ],
        "STRUCT_BATTERY": [
            "# Koksofenbatterie",
            "@3 Eine Reihe schmaler Öfen in einem Block aus Silikastein, ein Koksofen in der Vorderwand eines davon. Jeder "
            "Ofen ist 1 breit, 3 tief und 2 hoch, mit einer ein Block dicken Heizwand dazwischen; 1 bis 12 Öfen. Geteilte "
            "Wände verlieren weniger Wärme, und ab 4 Öfen verbrennt die Batterie ihr eigenes Koksofengas und braucht keinen Brennstoff.",
        ],
    },
    "it_it": {
        "STRUCT_INTRO": [
            "# Come funzionano le strutture",
            "@2 Alcune macchine crescono fino a diventare strutture. Costruisci un guscio di blocchi di rivestimento attorno a "
            "un interno vuoto e metti la macchina nella sua parete, rivolta verso l'esterno: diventa il controllore e lavora "
            "con la dimensione dell'intera struttura. Da sola funziona come prima. Più la struttura è grande, più è veloce e "
            "meno calore perde per pezzo: velocità = (volume interno / volume minimo)^0,85; raddoppiare il volume dà x1,8, non x2.",
            "@2 Il controllore misura la struttura una volta al secondo e la mostra sul suo schermo. Se manca un blocco, lo "
            "schermo dice quale e in che strato, e scintille rosse segnano il punto mentre tieni la Guida da campo. Le Porte "
            "di struttura, ovunque nel guscio, fanno entrare e uscire le tramogge come i lati della macchina: dall'alto "
            "nell'ingresso, dal basso escono i prodotti, dai lati entrano combustibile e il resto.",
        ],
        "STRUCT_FURNACE": [
            "# Altoforno",
            "@2 Un Altoforno a carbone di legna nella parete di una colonna di Mattone refrattario. L'interno va da 1x1 a 3x3 "
            "blocchi e da 3 a 10 di altezza, aperto in cima; il pavimento e tutti i blocchi della parete, angoli compresi, "
            "sono mattone refrattario, Ugello o Porta di struttura. Serve un Ugello nello strato più basso: Mantici o una "
            "Macchina a vapore accanto soffiano tutto il forno. A coke arriva 100 °C più caldo del forno da solo.",
        ],
        "STRUCT_BATTERY": [
            "# Batteria di forni a coke",
            "@3 Una fila di forni stretti in un blocco di Mattone di silice, con un Forno a coke nella parete frontale di uno di "
            "essi. Ogni forno è largo 1, profondo 3 e alto 2, con una parete riscaldante di un blocco fra i forni; da 1 a 12 "
            "forni. Le pareti in comune perdono meno calore, e da 4 forni la batteria brucia il proprio gas di cokeria e non "
            "ha bisogno di combustibile.",
        ],
    },
    "nl_nl": {
        "STRUCT_INTRO": [
            "# Hoe structuren werken",
            "@2 Sommige machines groeien uit tot structuren. Bouw een wand van mantelblokken rond een lege binnenruimte en zet "
            "de machine in die wand, naar buiten gericht: ze wordt de regelaar en werkt met de grootte van de hele structuur. "
            "Alleen werkt ze als voorheen. Hoe groter de structuur, hoe sneller ze werkt en hoe minder warmte ze per stuk "
            "verliest: snelheid = (binnenvolume / kleinste volume)^0,85; dubbel volume geeft x1,8, niet x2.",
            "@2 De regelaar meet zijn structuur elke seconde en toont die op zijn scherm. Ontbreekt er een blok, dan noemt het "
            "scherm welk en in welke laag, en rode vonken wijzen de plek aan zolang je de Veldgids vasthoudt. "
            "Structuurpoorten, waar ook in de wand, laten trechters in en uit zoals de zijkanten van de machine: van boven in "
            "de invoer, onderuit de producten, van opzij brandstof en de rest.",
        ],
        "STRUCT_FURNACE": [
            "# Hoogoven",
            "@2 Een houtskoolhoogoven in de wand van een kolom van Vuurvaste steen. De binnenkant is 1x1 tot 3x3 blokken en "
            "3 tot 10 hoog, boven open; de vloer en alle wandblokken, hoeken ook, zijn vuurvaste steen, Blaasvorm of "
            "Structuurpoort. Een Blaasvorm in de onderste laag is nodig: een Blaasbalg of Stoommachine ertegen blaast de hele "
            "oven. Met cokes wordt hij 100 °C heter dan de oven alleen.",
        ],
        "STRUCT_BATTERY": [
            "# Cokesovenbatterij",
            "@3 Een rij smalle ovens in een blok van Silicasteen, met een Cokesoven in de voorwand van een ervan. Elke oven is "
            "1 breed, 3 diep en 2 hoog, met een verwarmingswand van één blok tussen de ovens; 1 tot 12 ovens. Gedeelde wanden "
            "verliezen minder warmte, en vanaf 4 ovens stookt de batterij haar eigen cokesovengas en heeft ze geen brandstof nodig.",
        ],
    },
    "sv_se": {
        "STRUCT_INTRO": [
            "# Så fungerar strukturer",
            "@2 Vissa maskiner växer till strukturer. Bygg ett skal av mantelblock runt ett tomt inre och sätt maskinen i "
            "väggen, vänd utåt: den blir styrenheten och arbetar med hela strukturens storlek. Ensam fungerar den som förut. "
            "Ju större struktur, desto snabbare arbetar den och desto mindre värme förlorar den per föremål: fart = (inre "
            "volym / minsta volym)^0,85; dubbel volym ger x1,8, inte x2.",
            "@2 Styrenheten mäter sin struktur en gång i sekunden och visar den på sin skärm. Saknas ett block säger skärmen "
            "vilket och i vilket lager, och röda gnistor visar platsen medan du håller Fälthandboken. Strukturportar var som "
            "helst i skalet släpper in och ut trattar som maskinens sidor: uppifrån till inmatningen, nedåt ut produkterna, "
            "från sidan bränsle och resten.",
        ],
        "STRUCT_FURNACE": [
            "# Masugn",
            "@2 En träkolsmasugn i väggen av en pelare av Eldfast tegel. Insidan är 1x1 till 3x3 block och 3 till 10 hög, "
            "öppen upptill; golvet och alla väggblock, även hörnen, är eldfast tegel, Form eller Strukturport. En Form i det "
            "nedersta lagret krävs: Bälgar eller en Ångmaskin mot den blåser hela ugnen. Med koks blir den 100 °C varmare än ugnen ensam.",
        ],
        "STRUCT_BATTERY": [
            "# Koksverk",
            "@3 En rad smala ugnar i ett block av Silikategel, med en Koksugn i framväggen till en av dem. Varje ugn är 1 bred, "
            "3 djup och 2 hög, med en uppvärmningsvägg på ett block mellan ugnarna; 1 till 12 ugnar. Delade väggar förlorar "
            "mindre värme, och från 4 ugnar eldar koksverket med sin egen koksugnsgas och behöver inget bränsle.",
        ],
    },
    "da_dk": {
        "STRUCT_INTRO": [
            "# Sådan virker strukturer",
            "@2 Nogle maskiner vokser til strukturer. Byg en skal af kappeblokke rundt om et tomt indre og sæt maskinen i "
            "væggen, vendt udad: den bliver styreenheden og arbejder med hele strukturens størrelse. Alene virker den som før. "
            "Jo større strukturen er, jo hurtigere arbejder den og jo mindre varme taber den pr. genstand: fart = (indre "
            "rumfang / mindste rumfang)^0,85; dobbelt rumfang giver x1,8, ikke x2.",
            "@2 Styreenheden måler sin struktur hvert sekund og viser den på sin skærm. Mangler der en blok, siger skærmen "
            "hvilken og i hvilket lag, og røde gnister viser stedet, mens du holder Felthåndbogen. Strukturporte hvor som "
            "helst i skallen lader tragte komme ind og ud som maskinens sider: ovenfra til indgangen, nedad ud produkterne, "
            "fra siden brændsel og resten.",
        ],
        "STRUCT_FURNACE": [
            "# Højovn",
            "@2 En trækulshøjovn i væggen af en søjle af Ildfast sten. Indersiden er 1x1 til 3x3 blokke og 3 til 10 høj, åben "
            "foroven; gulvet og alle vægblokke, også hjørnerne, er ildfast sten, Blæseform eller Strukturport. Der skal være "
            "en Blæseform i det nederste lag: Blæsebælge eller en Dampmaskine op ad den blæser hele ovnen. Med koks bliver den "
            "100 °C varmere end ovnen alene.",
        ],
        "STRUCT_BATTERY": [
            "# Koksværk",
            "@3 En række smalle ovne i en blok af Silikasten, med en Koksovn i forvæggen til en af dem. Hver ovn er 1 bred, 3 "
            "dyb og 2 høj, med en varmevæg på én blok mellem ovnene; 1 til 12 ovne. Fælles vægge taber mindre varme, og fra "
            "4 ovne fyrer koksværket med sin egen koksovnsgas og behøver intet brændsel.",
        ],
    },
    "ru_ru": {
        "STRUCT_INTRO": [
            "# Как работают конструкции",
            "@2 Некоторые машины вырастают в конструкции. Постройте оболочку из облицовочных блоков вокруг пустого "
            "пространства и вставьте машину в её стену лицом наружу: она станет контроллером и будет работать с размером "
            "всей конструкции. Одна она работает как раньше. Чем больше конструкция, тем быстрее она работает и тем меньше "
            "тепла теряет на предмет: скорость = (внутренний объём / наименьший объём)^0,85; вдвое больший объём даёт x1,8, а не x2.",
            "@2 Контроллер раз в секунду измеряет конструкцию и показывает её на своём экране. Если не хватает блока, экран "
            "называет его и слой, а красные искры отмечают место, пока вы держите Полевой справочник. Порты конструкции в "
            "любом месте оболочки пропускают воронки так же, как стороны машины: сверху во вход, снизу выходят продукты, "
            "сбоку топливо и остальное.",
        ],
        "STRUCT_FURNACE": [
            "# Доменная печь",
            "@2 Древесноугольная домна в стене колонны из Огнеупорного кирпича. Внутри от 1x1 до 3x3 блоков и от 3 "
            "до 10 в высоту, сверху открыто; пол и все блоки стены, углы тоже, это огнеупорный кирпич, Фурма или Порт "
            "конструкции. В нижнем слое нужна Фурма: Мехи или Паровая машина рядом с ней дуют во всю печь. На коксе она "
            "горячее на 100 °C, чем печь одна.",
        ],
        "STRUCT_BATTERY": [
            "# Коксовая батарея",
            "@3 Ряд узких печей в блоке из Динасового кирпича, с Коксовой печью в передней стене одной из них. Каждая печь 1 "
            "в ширину, 3 в глубину и 2 в высоту, между печами греющая стенка в один блок; от 1 до 12 печей. Общие стенки "
            "теряют меньше тепла, а с 4 печей батарея жжёт свой коксовый газ и обходится без топлива.",
        ],
    },
}

# ---- 3.8.0: stage 2 (Lead Chamber, Gay-Lussac Tower, Distillation Column, outline) ----
_BLOCKS2 = {
    "pt_br": {
        "lead_sheet": ("Chapa de Chumbo", "Placa grossa de chumbo para as paredes de uma Câmara de Chumbo: o óleo de vitríolo não a corrói"),
        "gay_lussac_tower": ("Torre de Gay-Lussac", "Torre de chumbo cheia de coque: três ou mais junto a uma Câmara de Chumbo recolhem os vapores, 25% mais rápido"),
        "distillation_tray": ("Prato de Destilação", "Prato de aço com borbulhadores: empilhe sobre um Alambique de Refinaria para fazer uma coluna"),
    },
    "es_es": {
        "lead_sheet": ("Lámina de plomo", "Placa gruesa de plomo para las paredes de una Cámara de plomo: el aceite de vitriolo no la ataca"),
        "gay_lussac_tower": ("Torre de Gay-Lussac", "Torre de plomo rellena de coque: tres o más junto a una Cámara de plomo recogen los humos, 25% más rápido"),
        "distillation_tray": ("Plato de destilación", "Plato de acero con campanas: apílalos sobre un Alambique de refinería para hacer una columna"),
    },
    "fr_fr": {
        "lead_sheet": ("Feuille de plomb", "Plaque épaisse de plomb pour les parois d'une Chambre de plomb : l'huile de vitriol ne la ronge pas"),
        "gay_lussac_tower": ("Tour de Gay-Lussac", "Tour de plomb garnie de coke : trois ou plus contre une Chambre de plomb captent ses vapeurs, 25 % plus vite"),
        "distillation_tray": ("Plateau de distillation", "Plateau d'acier à calottes : empilez-les sur un Alambic de raffinerie pour faire une colonne"),
    },
    "de_de": {
        "lead_sheet": ("Bleiplatte", "Dicke Bleiplatte für die Wände einer Bleikammer: Vitriolöl greift sie nicht an"),
        "gay_lussac_tower": ("Gay-Lussac-Turm", "Bleiturm mit Koksfüllung: drei oder mehr an einer Bleikammer fangen ihre Dämpfe auf, 25 % schneller"),
        "distillation_tray": ("Destillationsboden", "Stahlboden mit Glocken: auf eine Raffineriedestille gestapelt ergeben sie eine Kolonne"),
    },
    "it_it": {
        "lead_sheet": ("Lastra di piombo", "Lastra spessa di piombo per le pareti di una Camera di piombo: l'olio di vetriolo non la intacca"),
        "gay_lussac_tower": ("Torre di Gay-Lussac", "Torre di piombo piena di coke: tre o più contro una Camera di piombo raccolgono i fumi, 25% più veloce"),
        "distillation_tray": ("Piatto di distillazione", "Piatto d'acciaio a campanelle: impilali su un Distillatore di raffineria per fare una colonna"),
    },
    "nl_nl": {
        "lead_sheet": ("Loodplaat", "Dikke loodplaat voor de wanden van een Loodkamer: vitriool tast hem niet aan"),
        "gay_lussac_tower": ("Gay-Lussac-toren", "Loden toren gevuld met cokes: drie of meer tegen een Loodkamer vangen de dampen op, 25% sneller"),
        "distillation_tray": ("Destillatieschotel", "Stalen schotel met klokjes: stapel ze op een Raffinaderijdistilleerder voor een kolom"),
    },
    "sv_se": {
        "lead_sheet": ("Blyplåt", "Tjock blyplåt för väggarna i en Blykammare: vitriololja fräter inte på den"),
        "gay_lussac_tower": ("Gay-Lussac-torn", "Blytorn fyllt med koks: tre eller fler mot en Blykammare fångar ångorna, 25 % snabbare"),
        "distillation_tray": ("Destillationsbotten", "Stålbotten med klockor: stapla dem på en Raffinaderidestillator för att bygga en kolonn"),
    },
    "da_dk": {
        "lead_sheet": ("Blyplade", "Tyk blyplade til væggene i et Blykammer: vitriololie ætser den ikke"),
        "gay_lussac_tower": ("Gay-Lussac-tårn", "Blytårn fyldt med koks: tre eller flere op ad et Blykammer fanger dampene, 25 % hurtigere"),
        "distillation_tray": ("Destillationsbund", "Stålbund med klokker: stabl dem på en Raffinaderidestillator for at bygge en kolonne"),
    },
    "ru_ru": {
        "lead_sheet": ("Свинцовый лист", "Толстый свинцовый лист для стен Свинцовой камеры: купоросное масло его не разъедает"),
        "gay_lussac_tower": ("Башня Гей-Люссака", "Свинцовая башня с коксом: три и более у Свинцовой камеры ловят её пары, на 25% быстрее"),
        "distillation_tray": ("Ректификационная тарелка", "Стальная тарелка с колпачками: поставьте их на Нефтеперегонный куб, чтобы получить колонну"),
    },
}
_KEYS2 = ["lead_chamber", "lead_chamber.tower", "distillation_column", "too_small.lead_chamber", "too_small.distillation_column"]
_LANG2 = {
    "pt_br": ["Câmara de chumbo %sx%sx%s: velocidade x%s, calor por item %s%%",
              "Câmara de chumbo %sx%sx%s com torre: velocidade x%s, calor por item %s%%",
              "Coluna %sx%s, %s pratos: velocidade x%s, %s%% do betume vira frações leves",
              "Câmara de chumbo pequena demais: o interior deve ter pelo menos 3x3x3",
              "Coluna baixa demais: precisa de pelo menos 4 pratos"],
    "es_es": ["Cámara de plomo %sx%sx%s: velocidad x%s, calor por pieza %s%%",
              "Cámara de plomo %sx%sx%s con torre: velocidad x%s, calor por pieza %s%%",
              "Columna %sx%s, %s platos: velocidad x%s, %s%% del betún a fracciones ligeras",
              "Cámara de plomo demasiado pequeña: el interior debe medir al menos 3x3x3",
              "Columna demasiado baja: necesita al menos 4 platos"],
    "fr_fr": ["Chambre de plomb %sx%sx%s : vitesse x%s, chaleur par objet %s %%",
              "Chambre de plomb %sx%sx%s et tour : vitesse x%s, chaleur par objet %s %%",
              "Colonne %sx%s, %s plateaux : vitesse x%s, %s %% du bitume en coupes légères",
              "Chambre de plomb trop petite : l'intérieur doit faire au moins 3x3x3",
              "Colonne trop basse : il faut au moins 4 plateaux"],
    "de_de": ["Bleikammer %sx%sx%s: Tempo x%s, Wärme je Stück %s %%",
              "Bleikammer %sx%sx%s mit Turm: Tempo x%s, Wärme je Stück %s %%",
              "Kolonne %sx%s, %s Böden: Tempo x%s, %s %% des Bitumens zu leichten Schnitten",
              "Bleikammer zu klein: innen mindestens 3x3x3",
              "Kolonne zu niedrig: sie braucht mindestens 4 Böden"],
    "it_it": ["Camera di piombo %sx%sx%s: velocità x%s, calore per pezzo %s%%",
              "Camera di piombo %sx%sx%s con torre: velocità x%s, calore per pezzo %s%%",
              "Colonna %sx%s, %s piatti: velocità x%s, %s%% del bitume in tagli leggeri",
              "Camera di piombo troppo piccola: l'interno deve essere almeno 3x3x3",
              "Colonna troppo bassa: servono almeno 4 piatti"],
    "nl_nl": ["Loodkamer %sx%sx%s: snelheid x%s, warmte per stuk %s%%",
              "Loodkamer %sx%sx%s met toren: snelheid x%s, warmte per stuk %s%%",
              "Kolom %sx%s, %s schotels: snelheid x%s, %s%% van het bitumen naar lichte fracties",
              "Loodkamer te klein: binnen minstens 3x3x3",
              "Kolom te laag: er zijn minstens 4 schotels nodig"],
    "sv_se": ["Blykammare %sx%sx%s: fart x%s, värme per föremål %s %%",
              "Blykammare %sx%sx%s med torn: fart x%s, värme per föremål %s %%",
              "Kolonn %sx%s, %s bottnar: fart x%s, %s %% av bitumenet till lätta fraktioner",
              "Blykammaren är för liten: insidan måste vara minst 3x3x3",
              "Kolonnen är för låg: den behöver minst 4 bottnar"],
    "da_dk": ["Blykammer %sx%sx%s: fart x%s, varme pr. genstand %s %%",
              "Blykammer %sx%sx%s med tårn: fart x%s, varme pr. genstand %s %%",
              "Kolonne %sx%s, %s bunde: fart x%s, %s %% af bitumenet til lette fraktioner",
              "Blykammeret er for lille: indersiden skal være mindst 3x3x3",
              "Kolonnen er for lav: den skal have mindst 4 bunde"],
    "ru_ru": ["Свинцовая камера %sx%sx%s: скорость x%s, тепло на предмет %s%%",
              "Свинцовая камера %sx%sx%s с башней: скорость x%s, тепло на предмет %s%%",
              "Колонна %sx%s, %s тарелок: скорость x%s, %s%% битума в лёгкие фракции",
              "Свинцовая камера слишком мала: внутри должно быть не меньше 3x3x3",
              "Колонна слишком низкая: нужно не меньше 4 тарелок"],
}
_T2 = {
    "pt_br": {"gain_chamber": "Interior {a}x{b}x{h}: {blocks} blocos, velocidade x{speed}, calor por item {loss}%",
              "gain_chamber_tower": "Interior {a}x{b}x{h} com a torre: velocidade x{speed}, calor por item {loss}%",
              "gain_column": "{w}x{w}, {h} pratos: velocidade x{speed}, calor por item {loss}%, {cut}% do betume vira frações leves",
              "assembly_chamber": "Camada por camada (a menor; uma sala maior só tem paredes mais compridas):",
              "assembly_column": "Camada por camada (a menor; ponha mais pratos em cima para subir):"},
    "es_es": {"gain_chamber": "Interior {a}x{b}x{h}: {blocks} bloques, velocidad x{speed}, calor por pieza {loss}%",
              "gain_chamber_tower": "Interior {a}x{b}x{h} con la torre: velocidad x{speed}, calor por pieza {loss}%",
              "gain_column": "{w}x{w}, {h} platos: velocidad x{speed}, calor por pieza {loss}%, {cut}% del betún a fracciones ligeras",
              "assembly_chamber": "Capa por capa (la más pequeña; una sala mayor solo tiene paredes más largas):",
              "assembly_column": "Capa por capa (la más pequeña; añade platos encima para subir):"},
    "fr_fr": {"gain_chamber": "Intérieur {a}x{b}x{h} : {blocks} blocs, vitesse x{speed}, chaleur par objet {loss} %",
              "gain_chamber_tower": "Intérieur {a}x{b}x{h} avec la tour : vitesse x{speed}, chaleur par objet {loss} %",
              "gain_column": "{w}x{w}, {h} plateaux : vitesse x{speed}, chaleur par objet {loss} %, {cut} % du bitume en coupes légères",
              "assembly_chamber": "Couche par couche (la plus petite ; une salle plus grande a juste des murs plus longs) :",
              "assembly_column": "Couche par couche (la plus petite ; ajoutez des plateaux dessus pour monter) :"},
    "de_de": {"gain_chamber": "Innen {a}x{b}x{h}: {blocks} Blöcke, Tempo x{speed}, Wärme je Stück {loss} %",
              "gain_chamber_tower": "Innen {a}x{b}x{h} mit Turm: Tempo x{speed}, Wärme je Stück {loss} %",
              "gain_column": "{w}x{w}, {h} Böden: Tempo x{speed}, Wärme je Stück {loss} %, {cut} % des Bitumens zu leichten Schnitten",
              "assembly_chamber": "Schicht für Schicht (die kleinste; ein größerer Raum hat nur längere Wände):",
              "assembly_column": "Schicht für Schicht (die kleinste; oben Böden ergänzen, um höher zu bauen):"},
    "it_it": {"gain_chamber": "Interno {a}x{b}x{h}: {blocks} blocchi, velocità x{speed}, calore per pezzo {loss}%",
              "gain_chamber_tower": "Interno {a}x{b}x{h} con la torre: velocità x{speed}, calore per pezzo {loss}%",
              "gain_column": "{w}x{w}, {h} piatti: velocità x{speed}, calore per pezzo {loss}%, {cut}% del bitume in tagli leggeri",
              "assembly_chamber": "Strato per strato (la più piccola; una sala più grande ha solo pareti più lunghe):",
              "assembly_column": "Strato per strato (la più piccola; aggiungi piatti sopra per salire):"},
    "nl_nl": {"gain_chamber": "Binnen {a}x{b}x{h}: {blocks} blokken, snelheid x{speed}, warmte per stuk {loss}%",
              "gain_chamber_tower": "Binnen {a}x{b}x{h} met de toren: snelheid x{speed}, warmte per stuk {loss}%",
              "gain_column": "{w}x{w}, {h} schotels: snelheid x{speed}, warmte per stuk {loss}%, {cut}% van het bitumen naar lichte fracties",
              "assembly_chamber": "Laag voor laag (de kleinste; een grotere kamer heeft alleen langere wanden):",
              "assembly_column": "Laag voor laag (de kleinste; zet er schotels bovenop om hoger te gaan):"},
    "sv_se": {"gain_chamber": "Insida {a}x{b}x{h}: {blocks} block, fart x{speed}, värme per föremål {loss} %",
              "gain_chamber_tower": "Insida {a}x{b}x{h} med tornet: fart x{speed}, värme per föremål {loss} %",
              "gain_column": "{w}x{w}, {h} bottnar: fart x{speed}, värme per föremål {loss} %, {cut} % av bitumenet till lätta fraktioner",
              "assembly_chamber": "Lager för lager (den minsta; ett större rum har bara längre väggar):",
              "assembly_column": "Lager för lager (den minsta; lägg fler bottnar ovanpå för att bygga högre):"},
    "da_dk": {"gain_chamber": "Inderside {a}x{b}x{h}: {blocks} blokke, fart x{speed}, varme pr. genstand {loss} %",
              "gain_chamber_tower": "Inderside {a}x{b}x{h} med tårnet: fart x{speed}, varme pr. genstand {loss} %",
              "gain_column": "{w}x{w}, {h} bunde: fart x{speed}, varme pr. genstand {loss} %, {cut} % af bitumenet til lette fraktioner",
              "assembly_chamber": "Lag for lag (den mindste; et større rum har bare længere vægge):",
              "assembly_column": "Lag for lag (den mindste; sæt flere bunde ovenpå for at bygge højere):"},
    "ru_ru": {"gain_chamber": "Внутри {a}x{b}x{h}: {blocks} блоков, скорость x{speed}, тепло на предмет {loss}%",
              "gain_chamber_tower": "Внутри {a}x{b}x{h} с башней: скорость x{speed}, тепло на предмет {loss}%",
              "gain_column": "{w}x{w}, {h} тарелок: скорость x{speed}, тепло на предмет {loss}%, {cut}% битума в лёгкие фракции",
              "assembly_chamber": "Слой за слоем (наименьшая; у комнаты побольше просто длиннее стены):",
              "assembly_column": "Слой за слоем (наименьшая; добавляйте тарелки сверху, чтобы выше):"},
}
_GUIDE2 = {
    "pt_br": {
        "STRUCT_OUTLINE": [
            "@2 Para montar uma, agache e use o controlador com o Guia de Campo na mão: a menor estrutura aparece em volta "
            "dele em blocos translúcidos (laranja a casca, cinza as portas, vermelho a Ventaneira, azul a torre) e cada "
            "bloco que você põe ocupa o seu lugar. Faça de novo para esconder."],
        "STRUCT_CHAMBER": [
            "# Câmara de chumbo",
            "@3 Uma Câmara de Chumbo na parede de uma sala fechada de Chapa de Chumbo: interior de 3x3x3 a 9x9x6, paredes, "
            "piso, teto e cantos todos de chumbo ou Portas de Estrutura. Uma Torre de Gay-Lussac, três ou mais blocos de "
            "Torre de Gay-Lussac empilhados por fora da casca, recolhe os vapores nitrosos e os devolve: a câmara trabalha 25% mais rápido."],
        "STRUCT_COLUMN": [
            "# Coluna de destilação",
            "@4 Pratos de Destilação empilhados sobre um Alambique de Refinaria, 1x1 ou 3x3 (o alambique sob o centro), de 4 "
            "a 16 de altura; uma Porta de Estrutura pode ficar no lugar de qualquer prato. A largura dá velocidade; a altura "
            "dá cortes melhores: a partir de 4 pratos, cada prato manda mais betume para cima como nafta e querosene, 40% dele com 16 pratos."],
    },
    "es_es": {
        "STRUCT_OUTLINE": [
            "@2 Para construir una, agáchate y usa su controlador con la Guía de campo en la mano: la estructura más pequeña "
            "se dibuja a su alrededor en bloques translúcidos (naranja el casco, gris los puertos, rojo la Tobera, azul la "
            "torre) y cada bloque que pongas ocupa su sitio. Repite para ocultarla."],
        "STRUCT_CHAMBER": [
            "# Cámara de plomo",
            "@3 Una Cámara de plomo en la pared de una sala cerrada de Lámina de plomo: interior de 3x3x3 a 9x9x6, paredes, "
            "suelo, techo y esquinas todo de plomo o Puertos de estructura. Una Torre de Gay-Lussac, tres o más bloques de "
            "Torre de Gay-Lussac apilados por fuera del casco, recoge los humos nitrosos y los devuelve: la cámara trabaja un 25% más rápido."],
        "STRUCT_COLUMN": [
            "# Columna de destilación",
            "@4 Platos de destilación apilados sobre un Alambique de refinería, 1x1 o 3x3 (el alambique bajo el centro), de 4 "
            "a 16 de alto; un Puerto de estructura puede ocupar el lugar de cualquier plato. El ancho da velocidad; la altura "
            "da mejores cortes: desde 4 platos, cada plato sube más betún como nafta y queroseno, el 40% con 16 platos."],
    },
    "fr_fr": {
        "STRUCT_OUTLINE": [
            "@2 Pour en construire une, accroupissez-vous et utilisez son contrôleur avec le Guide de terrain en main : la "
            "plus petite structure se dessine autour en blocs translucides (orange l'enveloppe, gris les ports, rouge la "
            "Tuyère, bleu la tour) et chaque bloc posé prend sa place. Recommencez pour la cacher."],
        "STRUCT_CHAMBER": [
            "# Chambre de plomb",
            "@3 Une Chambre de plomb dans la paroi d'une pièce close en Feuille de plomb : intérieur de 3x3x3 à 9x9x6, murs, "
            "sol, plafond et coins tout en plomb ou en Ports de structure. Une Tour de Gay-Lussac, trois blocs de Tour de "
            "Gay-Lussac ou plus empilés contre l'extérieur de l'enveloppe, capte les vapeurs nitreuses et les renvoie : la chambre travaille 25 % plus vite."],
        "STRUCT_COLUMN": [
            "# Colonne de distillation",
            "@4 Des Plateaux de distillation empilés sur un Alambic de raffinerie, 1x1 ou 3x3 (l'alambic sous le centre), de 4 "
            "à 16 de haut ; un Port de structure peut remplacer n'importe quel plateau. La largeur donne la vitesse, la "
            "hauteur de meilleures coupes : dès 4 plateaux, chaque plateau fait monter plus de bitume en naphta et kérosène, 40 % à 16 plateaux."],
    },
    "de_de": {
        "STRUCT_OUTLINE": [
            "@2 Zum Bauen schleichen und den Regler mit dem Feldhandbuch in der Hand benutzen: die kleinste Struktur wird in "
            "durchscheinenden Blöcken um ihn gezeichnet (orange die Hülle, grau die Anschlüsse, rot die Blasform, blau der "
            "Turm), und jeder gesetzte Block nimmt seinen Platz ein. Noch einmal, um sie auszublenden."],
        "STRUCT_CHAMBER": [
            "# Bleikammer",
            "@3 Eine Bleikammer in der Wand eines geschlossenen Raums aus Bleiplatten: innen 3x3x3 bis 9x9x6, Wände, Boden, "
            "Decke und Ecken ganz aus Blei oder Strukturanschlüssen. Ein Gay-Lussac-Turm, drei oder mehr Gay-Lussac-Turm-"
            "Blöcke außen an der Hülle gestapelt, fängt die nitrosen Dämpfe auf und führt sie zurück: die Kammer arbeitet 25 % schneller."],
        "STRUCT_COLUMN": [
            "# Destillationskolonne",
            "@4 Destillationsböden auf einer Raffineriedestille gestapelt, 1x1 oder 3x3 (die Destille unter der Mitte), 4 bis "
            "16 hoch; ein Strukturanschluss kann jeden Boden ersetzen. Breite bringt Tempo, Höhe bessere Schnitte: ab 4 "
            "Böden schickt jeder Boden mehr Bitumen als Naphtha und Kerosin nach oben, 40 % davon bei 16 Böden."],
    },
    "it_it": {
        "STRUCT_OUTLINE": [
            "@2 Per costruirne una, accovacciati e usa il controllore con la Guida da campo in mano: la struttura più piccola "
            "viene disegnata intorno in blocchi traslucidi (arancione il guscio, grigio le porte, rosso l'Ugello, blu la "
            "torre) e ogni blocco che metti prende il suo posto. Ripeti per nasconderla."],
        "STRUCT_CHAMBER": [
            "# Camera di piombo",
            "@3 Una Camera di piombo nella parete di una stanza chiusa di Lastra di piombo: interno da 3x3x3 a 9x9x6, pareti, "
            "pavimento, soffitto e angoli tutti di piombo o Porte di struttura. Una Torre di Gay-Lussac, tre o più blocchi di "
            "Torre di Gay-Lussac impilati fuori dal guscio, raccoglie i fumi nitrosi e li rimanda indietro: la camera lavora il 25% più veloce."],
        "STRUCT_COLUMN": [
            "# Colonna di distillazione",
            "@4 Piatti di distillazione impilati su un Distillatore di raffineria, 1x1 o 3x3 (il distillatore sotto il centro), "
            "da 4 a 16 di altezza; una Porta di struttura può prendere il posto di qualsiasi piatto. La larghezza dà velocità, "
            "l'altezza tagli migliori: da 4 piatti in su, ogni piatto manda più bitume in alto come nafta e cherosene, il 40% con 16 piatti."],
    },
    "nl_nl": {
        "STRUCT_OUTLINE": [
            "@2 Om er een te bouwen: sluip en gebruik de regelaar met de Veldgids in je hand: de kleinste structuur wordt er in "
            "doorschijnende blokken omheen getekend (oranje de wand, grijs de poorten, rood de Blaasvorm, blauw de toren) en "
            "elk blok dat je plaatst neemt zijn plek in. Nog eens om hem te verbergen."],
        "STRUCT_CHAMBER": [
            "# Loodkamer",
            "@3 Een Loodkamer in de wand van een gesloten ruimte van Loodplaat: binnen 3x3x3 tot 9x9x6, wanden, vloer, plafond "
            "en hoeken allemaal lood of Structuurpoorten. Een Gay-Lussac-toren, drie of meer Gay-Lussac-torenblokken tegen de "
            "buitenkant van de wand gestapeld, vangt de nitreuze dampen op en stuurt ze terug: de kamer werkt 25% sneller."],
        "STRUCT_COLUMN": [
            "# Destillatiekolom",
            "@4 Destillatieschotels gestapeld op een Raffinaderijdistilleerder, 1x1 of 3x3 (de distilleerder onder het midden), "
            "4 tot 16 hoog; een Structuurpoort mag elke schotel vervangen. Breedte geeft snelheid, hoogte betere fracties: "
            "vanaf 4 schotels stuurt elke schotel meer bitumen omhoog als nafta en kerosine, 40% ervan bij 16 schotels."],
    },
    "sv_se": {
        "STRUCT_OUTLINE": [
            "@2 För att bygga en: smyg och använd styrenheten med Fälthandboken i handen: den minsta strukturen ritas runt den "
            "i genomskinliga block (orange skalet, grått portarna, rött formen, blått tornet) och varje block du sätter tar "
            "sin plats. Gör det igen för att dölja den."],
        "STRUCT_CHAMBER": [
            "# Blykammare",
            "@3 En Blykammare i väggen till ett slutet rum av Blyplåt: insida 3x3x3 till 9x9x6, väggar, golv, tak och hörn allt "
            "bly eller Strukturportar. Ett Gay-Lussac-torn, tre eller fler Gay-Lussac-tornblock staplade mot skalets utsida, "
            "fångar de nitrösa ångorna och skickar tillbaka dem: kammaren arbetar 25 % snabbare."],
        "STRUCT_COLUMN": [
            "# Destillationskolonn",
            "@4 Destillationsbottnar staplade på en Raffinaderidestillator, 1x1 eller 3x3 (destillatorn under mitten), 4 till 16 "
            "höga; en Strukturport får ersätta vilken botten som helst. Bredd ger fart, höjd bättre fraktioner: från 4 "
            "bottnar skickar varje botten mer bitumen uppåt som nafta och fotogen, 40 % av det vid 16 bottnar."],
    },
    "da_dk": {
        "STRUCT_OUTLINE": [
            "@2 For at bygge en: snig dig og brug styreenheden med Felthåndbogen i hånden: den mindste struktur tegnes rundt om "
            "den i gennemsigtige blokke (orange skallen, gråt portene, rødt blæseformen, blåt tårnet), og hver blok du sætter "
            "tager sin plads. Gør det igen for at skjule den."],
        "STRUCT_CHAMBER": [
            "# Blykammer",
            "@3 Et Blykammer i væggen til et lukket rum af Blyplade: inderside 3x3x3 til 9x9x6, vægge, gulv, loft og hjørner alt "
            "bly eller Strukturporte. Et Gay-Lussac-tårn, tre eller flere Gay-Lussac-tårnblokke stablet op ad skallens "
            "yderside, fanger de nitrøse dampe og sender dem tilbage: kammeret arbejder 25 % hurtigere."],
        "STRUCT_COLUMN": [
            "# Destillationskolonne",
            "@4 Destillationsbunde stablet på en Raffinaderidestillator, 1x1 eller 3x3 (destillatoren under midten), 4 til 16 "
            "høje; en Strukturport må erstatte enhver bund. Bredde giver fart, højde bedre fraktioner: fra 4 bunde sender hver "
            "bund mere bitumen op som nafta og petroleum, 40 % af det ved 16 bunde."],
    },
    "ru_ru": {
        "STRUCT_OUTLINE": [
            "@2 Чтобы построить её, присядьте и используйте контроллер с Полевым справочником в руке: вокруг него появится "
            "наименьшая конструкция из полупрозрачных блоков (оранжевым оболочка, серым порты, красным Фурма, синим башня), "
            "и каждый поставленный блок занимает своё место. Повторите, чтобы скрыть."],
        "STRUCT_CHAMBER": [
            "# Свинцовая камера",
            "@3 Свинцовая камера в стене закрытой комнаты из Свинцового листа: внутри от 3x3x3 до 9x9x6, стены, пол, потолок и "
            "углы целиком из свинца или Портов конструкции. Башня Гей-Люссака, три и более блока Башни Гей-Люссака снаружи "
            "оболочки, ловит нитрозные пары и возвращает их: камера работает на 25% быстрее."],
        "STRUCT_COLUMN": [
            "# Ректификационная колонна",
            "@4 Ректификационные тарелки на Нефтеперегонном кубе, 1x1 или 3x3 (куб под серединой), от 4 до 16 в высоту; Порт "
            "конструкции может стоять вместо любой тарелки. Ширина даёт скорость, высота лучшие фракции: с 4 тарелок каждая "
            "тарелка поднимает больше битума как лигроин и керосин, 40% при 16 тарелках."],
    },
}
for _c in CODES:
    BLOCKS[_c].update(_BLOCKS2[_c])
    LANG[_c].update({"gui.periodic_elements.structure." + k: v for k, v in zip(_KEYS2, _LANG2[_c], strict=True)})
    T[_c].update(_T2[_c])
    GUIDE[_c].update(_GUIDE2[_c])

# ---- 3.13.0: stage 3 (Enrichment Cascade, Nuclear Reactor) ----
_BLOCKS3 = {
    "pt_br": {
        "diffuser_stage": ("Estágio Difusor", "Uma bomba e uma barreira porosa de níquel: um bloco deles atrás de uma Cascata de Enriquecimento faz dela uma cascata de verdade"),
        "graphite_block": ("Bloco de Grafite", "Grafite puro que freia os nêutrons sem capturá-los: o núcleo de um Reator Nuclear"),
        "fuel_channel": ("Canal de Combustível", "Um tubo de Zircaloy atravessando o núcleo de grafite, onde o combustível queima: cada um soma potência"),
        "reactor_shielding": ("Blindagem de Reator", "Aço e chumbo em volta do núcleo de um reator: segura os nêutrons dentro e a radiação fora"),
    },
    "es_es": {
        "diffuser_stage": ("Etapa difusora", "Una bomba y una barrera porosa de níquel: un bloque de ellas tras una Cascada de enriquecimiento la vuelve una cascada de verdad"),
        "graphite_block": ("Bloque de grafito", "Grafito puro que frena los neutrones sin atraparlos: el núcleo de un Reactor nuclear"),
        "fuel_channel": ("Canal de combustible", "Un tubo de zircaloy que atraviesa el núcleo de grafito, donde arde el combustible: cada uno suma potencia"),
        "reactor_shielding": ("Blindaje de reactor", "Acero y plomo alrededor del núcleo de un reactor: los neutrones dentro y la radiación fuera"),
    },
    "fr_fr": {
        "diffuser_stage": ("Étage de diffusion", "Une pompe et une barrière poreuse en nickel : un bloc d'étages derrière une Cascade d'enrichissement en fait une vraie cascade"),
        "graphite_block": ("Bloc de graphite", "Graphite pur qui ralentit les neutrons sans les capturer : le cœur d'un Réacteur nucléaire"),
        "fuel_channel": ("Canal de combustible", "Un tube de zircaloy à travers le cœur de graphite, où brûle le combustible : chacun ajoute de la puissance"),
        "reactor_shielding": ("Blindage de réacteur", "Acier et plomb autour du cœur d'un réacteur : les neutrons dedans, les rayonnements dehors"),
    },
    "de_de": {
        "diffuser_stage": ("Diffusionsstufe", "Eine Pumpe und eine poröse Nickelwand: ein Block davon hinter einer Anreicherungskaskade macht sie zur echten Kaskade"),
        "graphite_block": ("Graphitblock", "Reiner Graphit bremst Neutronen, ohne sie einzufangen: der Kern eines Kernreaktors"),
        "fuel_channel": ("Brennstoffkanal", "Ein Zircaloy-Rohr durch den Graphitkern, in dem der Brennstoff brennt: jeder bringt Leistung"),
        "reactor_shielding": ("Reaktorabschirmung", "Stahl und Blei um einen Reaktorkern: hält die Neutronen drinnen und die Strahlung draußen"),
    },
    "it_it": {
        "diffuser_stage": ("Stadio diffusore", "Una pompa e una barriera porosa di nichel: un blocco di stadi dietro una Cascata di arricchimento ne fa una vera cascata"),
        "graphite_block": ("Blocco di grafite", "Grafite pura che rallenta i neutroni senza catturarli: il nocciolo di un Reattore nucleare"),
        "fuel_channel": ("Canale del combustibile", "Un tubo di zircaloy attraverso il nocciolo di grafite, dove brucia il combustibile: ognuno aggiunge potenza"),
        "reactor_shielding": ("Schermatura del reattore", "Acciaio e piombo intorno al nocciolo di un reattore: i neutroni dentro, le radiazioni fuori"),
    },
    "nl_nl": {
        "diffuser_stage": ("Diffusietrap", "Een pomp en een poreuze nikkelwand: een blok ervan achter een Verrijkingscascade maakt er een echte cascade van"),
        "graphite_block": ("Grafietblok", "Zuiver grafiet remt neutronen af zonder ze te vangen: de kern van een Kernreactor"),
        "fuel_channel": ("Splijtstofkanaal", "Een zircaloybuis door de grafietkern waarin de splijtstof brandt: elk kanaal geeft vermogen"),
        "reactor_shielding": ("Reactorafscherming", "Staal en lood rond een reactorkern: houdt de neutronen binnen en de straling buiten"),
    },
    "sv_se": {
        "diffuser_stage": ("Diffusionssteg", "En pump och en porös nickelbarriär: ett block av dem bakom en Anrikningskaskad gör den till en riktig kaskad"),
        "graphite_block": ("Grafitblock", "Ren grafit som bromsar neutroner utan att fånga dem: kärnan i en Kärnreaktor"),
        "fuel_channel": ("Bränslekanal", "Ett zircaloyrör genom grafitkärnan där bränslet brinner: varje kanal ger effekt"),
        "reactor_shielding": ("Reaktorskärm", "Stål och bly runt en reaktorkärna: håller neutronerna inne och strålningen ute"),
    },
    "da_dk": {
        "diffuser_stage": ("Diffusionstrin", "En pumpe og en porøs nikkelbarriere: en blok af dem bag en Berigningskaskade gør den til en rigtig kaskade"),
        "graphite_block": ("Grafitblok", "Ren grafit, der bremser neutroner uden at fange dem: kernen i en Atomreaktor"),
        "fuel_channel": ("Brændselskanal", "Et zircaloyrør gennem grafitkernen, hvor brændslet brænder: hver kanal giver effekt"),
        "reactor_shielding": ("Reaktorafskærmning", "Stål og bly rundt om en reaktorkerne: holder neutronerne inde og strålingen ude"),
    },
    "ru_ru": {
        "diffuser_stage": ("Диффузионная ступень", "Насос и пористая никелевая перегородка: блок ступеней за Каскадом обогащения делает его настоящим каскадом"),
        "graphite_block": ("Графитовый блок", "Чистый графит замедляет нейтроны, не захватывая их: активная зона Ядерного реактора"),
        "fuel_channel": ("Топливный канал", "Труба из циркалоя сквозь графитовую кладку, где горит топливо: каждая добавляет мощность"),
        "reactor_shielding": ("Защита реактора", "Сталь и свинец вокруг активной зоны: нейтроны внутри, излучение снаружи"),
    },
}
_KEYS3 = ["gui.periodic_elements.structure.enrichment_cascade", "gui.periodic_elements.structure.too_small.enrichment_cascade",
          "gui.periodic_elements.structure.too_small.nuclear_reactor", "gui.periodic_elements.structure.no_channel",
          "gui.periodic_elements.power.reactor_core", "gui.periodic_elements.power.reactor.subcritical",
          "gui.periodic_elements.power.reactor.runaway"]
_LANG3 = {
    "pt_br": ["Cascata de %s estágios: velocidade x%s, calor por item %s%%, %s%% da carga enriquecida",
              "Cascata pequena demais: precisa de pelo menos 4 Estágios Difusores",
              "Núcleo do reator pequeno demais: deve ter pelo menos 3x3x3",
              "O núcleo do reator precisa de pelo menos um Canal de Combustível",
              "Núcleo %sx%sx%s, %s canais de combustível: %s de %s kJ guardados; %s; k %s; %s",
              "subcrítico: o núcleo é pequeno demais para este combustível",
              "SCRAM: disparou sem Barra de Controle de Boro nos espaços"],
    "es_es": ["Cascada de %s etapas: velocidad x%s, calor por pieza %s%%, %s%% de la carga enriquecida",
              "Cascada demasiado pequeña: necesita al menos 4 Etapas difusoras",
              "Núcleo del reactor demasiado pequeño: debe medir al menos 3x3x3",
              "El núcleo del reactor necesita al menos un Canal de combustible",
              "Núcleo %sx%sx%s, %s canales de combustible: %s de %s kJ guardados; %s; k %s; %s",
              "subcrítico: el núcleo es demasiado pequeño para este combustible",
              "SCRAM: se disparó sin Barra de control de boro en sus huecos"],
    "fr_fr": ["Cascade de %s étages : vitesse x%s, chaleur par objet %s %%, %s %% de la charge enrichie",
              "Cascade trop petite : il faut au moins 4 Étages de diffusion",
              "Cœur du réacteur trop petit : il doit faire au moins 3x3x3",
              "Le cœur du réacteur a besoin d'au moins un Canal de combustible",
              "Cœur %sx%sx%s, %s canaux de combustible : %s sur %s kJ stockés ; %s ; k %s ; %s",
              "sous-critique : le cœur est trop petit pour ce combustible",
              "SCRAM : il s'est emballé sans Barre de contrôle en bore dans ses cases"],
    "de_de": ["Kaskade mit %s Stufen: Tempo x%s, Wärme je Stück %s %%, %s %% des Einsatzes angereichert",
              "Kaskade zu klein: sie braucht mindestens 4 Diffusionsstufen",
              "Reaktorkern zu klein: mindestens 3x3x3",
              "Der Reaktorkern braucht mindestens einen Brennstoffkanal",
              "Kern %sx%sx%s, %s Brennstoffkanäle: %s von %s kJ gespeichert; %s; k %s; %s",
              "unterkritisch: der Kern ist für diesen Brennstoff zu klein",
              "SCRAM: er ist ohne Bor-Steuerstab in den Fächern durchgegangen"],
    "it_it": ["Cascata di %s stadi: velocità x%s, calore per pezzo %s%%, %s%% della carica arricchita",
              "Cascata troppo piccola: servono almeno 4 Stadi diffusori",
              "Nocciolo del reattore troppo piccolo: deve essere almeno 3x3x3",
              "Il nocciolo del reattore ha bisogno di almeno un Canale del combustibile",
              "Nocciolo %sx%sx%s, %s canali del combustibile: %s di %s kJ accumulati; %s; k %s; %s",
              "sottocritico: il nocciolo è troppo piccolo per questo combustibile",
              "SCRAM: è andato fuori controllo senza Barra di controllo al boro negli slot"],
    "nl_nl": ["Cascade van %s trappen: snelheid x%s, warmte per stuk %s%%, %s%% van de voeding verrijkt",
              "Cascade te klein: er zijn minstens 4 Diffusietrappen nodig",
              "Reactorkern te klein: minstens 3x3x3",
              "De reactorkern heeft minstens één Splijtstofkanaal nodig",
              "Kern %sx%sx%s, %s splijtstofkanalen: %s van %s kJ opgeslagen; %s; k %s; %s",
              "subkritisch: de kern is te klein voor deze splijtstof",
              "SCRAM: hij sloeg op hol zonder Regelstaaf van boor in zijn vakken"],
    "sv_se": ["Kaskad med %s steg: fart x%s, värme per föremål %s %%, %s %% av matningen anrikad",
              "Kaskaden är för liten: den behöver minst 4 Diffusionssteg",
              "Reaktorkärnan är för liten: minst 3x3x3",
              "Reaktorkärnan behöver minst en Bränslekanal",
              "Kärna %sx%sx%s, %s bränslekanaler: %s av %s kJ lagrat; %s; k %s; %s",
              "underkritisk: kärnan är för liten för det här bränslet",
              "SCRAM: den skenade utan Borstyrstav i facken"],
    "da_dk": ["Kaskade med %s trin: fart x%s, varme pr. genstand %s %%, %s %% af fødningen beriget",
              "Kaskaden er for lille: den skal have mindst 4 Diffusionstrin",
              "Reaktorkernen er for lille: mindst 3x3x3",
              "Reaktorkernen skal have mindst én Brændselskanal",
              "Kerne %sx%sx%s, %s brændselskanaler: %s af %s kJ lagret; %s; k %s; %s",
              "underkritisk: kernen er for lille til dette brændsel",
              "SCRAM: den løb løbsk uden Borstyrestav i rummene"],
    "ru_ru": ["Каскад из %s ступеней: скорость x%s, тепло на предмет %s%%, %s%% сырья обогащено",
              "Каскад слишком мал: нужно не меньше 4 Диффузионных ступеней",
              "Активная зона слишком мала: не меньше 3x3x3",
              "Активной зоне нужен хотя бы один Топливный канал",
              "Зона %sx%sx%s, %s топливных каналов: %s из %s кДж; %s; k %s; %s",
              "подкритичен: зона мала для этого топлива",
              "АЗ: разгон без Борного управляющего стержня в ячейках"],
}
_T3 = {
    "pt_br": {"gain_cascade": "{n} estágios: velocidade x{speed}, calor por item {loss}%, {enriched} enriquecido por 100",
              "gain_reactor": "Núcleo {a}x{b}x{h}, {ch} canais: {kj} kJ por tick, barras duram x{life}, k {ke} enriquecido, {kn} natural",
              "assembly_cascade": "Camada por camada (a menor; faça o bloco mais largo, alto ou comprido para mais estágios):",
              "assembly_reactor": "Camada por camada (a menor; canais em colunas alternadas, grafite entre eles):"},
    "es_es": {"gain_cascade": "{n} etapas: velocidad x{speed}, calor por pieza {loss}%, {enriched} enriquecido por 100",
              "gain_reactor": "Núcleo {a}x{b}x{h}, {ch} canales: {kj} kJ por tick, las barras duran x{life}, k {ke} enriquecido, {kn} natural",
              "assembly_cascade": "Capa por capa (la más pequeña; haz el bloque más ancho, alto o largo para más etapas):",
              "assembly_reactor": "Capa por capa (la más pequeña; canales en columnas alternas, grafito entre ellos):"},
    "fr_fr": {"gain_cascade": "{n} étages : vitesse x{speed}, chaleur par objet {loss} %, {enriched} enrichi pour 100",
              "gain_reactor": "Cœur {a}x{b}x{h}, {ch} canaux : {kj} kJ par tick, barres x{life} plus longues, k {ke} enrichi, {kn} naturel",
              "assembly_cascade": "Couche par couche (la plus petite ; élargissez, montez ou allongez le bloc pour plus d'étages) :",
              "assembly_reactor": "Couche par couche (le plus petit ; canaux une colonne sur deux, graphite entre eux) :"},
    "de_de": {"gain_cascade": "{n} Stufen: Tempo x{speed}, Wärme je Stück {loss} %, {enriched} angereichert je 100",
              "gain_reactor": "Kern {a}x{b}x{h}, {ch} Kanäle: {kj} kJ je Tick, Stäbe halten x{life}, k {ke} angereichert, {kn} natürlich",
              "assembly_cascade": "Schicht für Schicht (die kleinste; den Block breiter, höher oder länger bauen für mehr Stufen):",
              "assembly_reactor": "Schicht für Schicht (der kleinste; Kanäle in jeder zweiten Säule, Graphit dazwischen):"},
    "it_it": {"gain_cascade": "{n} stadi: velocità x{speed}, calore per pezzo {loss}%, {enriched} arricchito su 100",
              "gain_reactor": "Nocciolo {a}x{b}x{h}, {ch} canali: {kj} kJ per tick, barre durano x{life}, k {ke} arricchito, {kn} naturale",
              "assembly_cascade": "Strato per strato (la più piccola; fai il blocco più largo, alto o lungo per più stadi):",
              "assembly_reactor": "Strato per strato (il più piccolo; canali a colonne alterne, grafite in mezzo):"},
    "nl_nl": {"gain_cascade": "{n} trappen: snelheid x{speed}, warmte per stuk {loss}%, {enriched} verrijkt per 100",
              "gain_reactor": "Kern {a}x{b}x{h}, {ch} kanalen: {kj} kJ per tick, staven gaan x{life} mee, k {ke} verrijkt, {kn} natuurlijk",
              "assembly_cascade": "Laag voor laag (de kleinste; maak het blok breder, hoger of langer voor meer trappen):",
              "assembly_reactor": "Laag voor laag (de kleinste; kanalen in om en om kolommen, grafiet ertussen):"},
    "sv_se": {"gain_cascade": "{n} steg: fart x{speed}, värme per föremål {loss} %, {enriched} anrikat per 100",
              "gain_reactor": "Kärna {a}x{b}x{h}, {ch} kanaler: {kj} kJ per tick, stavar räcker x{life}, k {ke} anrikat, {kn} naturligt",
              "assembly_cascade": "Lager för lager (den minsta; gör blocket bredare, högre eller längre för fler steg):",
              "assembly_reactor": "Lager för lager (den minsta; kanaler i varannan pelare, grafit emellan):"},
    "da_dk": {"gain_cascade": "{n} trin: fart x{speed}, varme pr. genstand {loss} %, {enriched} beriget pr. 100",
              "gain_reactor": "Kerne {a}x{b}x{h}, {ch} kanaler: {kj} kJ pr. tick, stave holder x{life}, k {ke} beriget, {kn} naturligt",
              "assembly_cascade": "Lag for lag (den mindste; gør blokken bredere, højere eller længere for flere trin):",
              "assembly_reactor": "Lag for lag (den mindste; kanaler i hver anden søjle, grafit imellem):"},
    "ru_ru": {"gain_cascade": "{n} ступеней: скорость x{speed}, тепло на предмет {loss}%, {enriched} обогащённого на 100",
              "gain_reactor": "Зона {a}x{b}x{h}, {ch} каналов: {kj} кДж за тик, стержни служат x{life}, k {ke} обогащённый, {kn} природный",
              "assembly_cascade": "Слой за слоем (наименьший; делайте блок шире, выше или длиннее для новых ступеней):",
              "assembly_reactor": "Слой за слоем (наименьший; каналы через колонну, графит между ними):"},
}
_GUIDE3 = {
    "pt_br": {
        "STRUCT_CASCADE": ["# Cascata de enriquecimento",
            "@6 Uma Cascata de Enriquecimento com um bloco maciço de Estágios Difusores atrás dela: de 1 a 4 de largura, 1 a 4 "
            "de altura e até 16 de comprimento, de 4 a 64 estágios ao todo; uma Porta de Estrutura pode ficar no lugar de "
            "qualquer estágio. O gás passa por todos os estágios, então cada um soma velocidade e esgota mais o rejeito: "
            "sozinha a máquina transforma 100 de natural em 14,5 de enriquecido; com 64 estágios, em 18."],
        "STRUCT_REACTOR": ["# Reator nuclear",
            "@6 Um Reator Nuclear numa parede de Blindagem de Reator em volta de um núcleo de Blocos de Grafite e Canais de "
            "Combustível: núcleo de 3x3x3 a 9x9x7, sem buracos, com água encostada na blindagem por fora. Cada três Canais "
            "de Combustível queimam barras e geram corrente como um reator sozinho; um núcleo maior perde menos nêutrons "
            "pela borda, então cada barra dura mais.",
            "@6 O núcleo precisa ser crítico (k 1 ou mais). Barras enriquecidas ficam críticas em qualquer núcleo. Barras "
            "naturais, com água pesada nos espaços, só num núcleo grande: 7x7x7, 8x8x6, 9x9x5 ou maior, como nos reatores "
            "CANDU. Acima de k 1,3 a reação em cadeia dispara, a menos que haja uma Barra de Controle de Boro nos espaços. "
            "Se disparar, ou se a água sumir, o reator faz SCRAM: a barra se perde e vaza radiação em volta por meio dia. "
            "Ele nunca explode."],
    },
    "es_es": {
        "STRUCT_CASCADE": ["# Cascada de enriquecimiento",
            "@6 Una Cascada de enriquecimiento con un bloque macizo de Etapas difusoras detrás: de 1 a 4 de ancho, 1 a 4 de "
            "alto y hasta 16 de largo, de 4 a 64 etapas en total; un Puerto de estructura puede ocupar el lugar de cualquier "
            "etapa. El gas pasa por todas las etapas, así que cada una suma velocidad y agota más las colas: sola, la "
            "máquina convierte 100 de natural en 14,5 de enriquecido; con 64 etapas, en 18."],
        "STRUCT_REACTOR": ["# Reactor nuclear",
            "@6 Un Reactor nuclear en una pared de Blindaje de reactor alrededor de un núcleo de Bloques de grafito y Canales "
            "de combustible: núcleo de 3x3x3 a 9x9x7, sin huecos, con agua tocando el blindaje por fuera. Cada tres Canales "
            "de combustible queman barras y dan corriente como un reactor solo; un núcleo mayor pierde menos neutrones por "
            "el borde, así que cada barra dura más.",
            "@6 El núcleo debe ser crítico (k 1 o más). Las barras enriquecidas son críticas en cualquier núcleo. Las "
            "naturales, con agua pesada en los huecos, solo en uno grande: 7x7x7, 8x8x6, 9x9x5 o mayor, como los reactores "
            "CANDU. Por encima de k 1,3 la reacción en cadena se dispara, salvo que haya una Barra de control de boro en los "
            "huecos. Si se dispara, o falta el agua, el reactor hace SCRAM: la barra se pierde y escapa radiación alrededor "
            "durante medio día. Nunca explota."],
    },
    "fr_fr": {
        "STRUCT_CASCADE": ["# Cascade d'enrichissement",
            "@6 Une Cascade d'enrichissement avec un bloc plein d'Étages de diffusion derrière elle : 1 à 4 de large, 1 à 4 "
            "de haut et jusqu'à 16 de long, 4 à 64 étages en tout ; un Port de structure peut remplacer n'importe quel "
            "étage. Le gaz traverse tous les étages, donc chacun ajoute de la vitesse et appauvrit davantage les rejets : "
            "seule, la machine change 100 de naturel en 14,5 d'enrichi ; avec 64 étages, en 18."],
        "STRUCT_REACTOR": ["# Réacteur nucléaire",
            "@6 Un Réacteur nucléaire dans une paroi de Blindage de réacteur autour d'un cœur de Blocs de graphite et de "
            "Canaux de combustible : cœur de 3x3x3 à 9x9x7, sans trou, de l'eau contre le blindage à l'extérieur. Chaque "
            "trio de Canaux de combustible brûle des barres et produit du courant comme un réacteur seul ; un cœur plus grand "
            "perd moins de neutrons par ses bords, donc chaque barre dure plus longtemps.",
            "@6 Le cœur doit être critique (k 1 ou plus). Les barres enrichies sont critiques dans tout cœur. Les barres "
            "naturelles, avec de l'eau lourde dans les cases, seulement dans un grand : 7x7x7, 8x8x6, 9x9x5 ou plus, comme "
            "les réacteurs CANDU. Au-dessus de k 1,3 la réaction en chaîne s'emballe, sauf si une Barre de contrôle en bore "
            "est dans les cases. S'il s'emballe, ou si l'eau manque, le réacteur fait un SCRAM : la barre est perdue et des "
            "rayonnements fuient autour pendant une demi-journée. Il n'explose jamais."],
    },
    "de_de": {
        "STRUCT_CASCADE": ["# Anreicherungskaskade",
            "@6 Eine Anreicherungskaskade mit einem massiven Block aus Diffusionsstufen dahinter: 1 bis 4 breit, 1 bis 4 hoch "
            "und bis zu 16 lang, insgesamt 4 bis 64 Stufen; ein Strukturanschluss kann jede Stufe ersetzen. Das Gas läuft "
            "durch alle Stufen, also bringt jede Tempo und reichert die Abfälle weiter ab: allein macht die Maschine aus 100 "
            "natürlichem 14,5 angereichertes, mit 64 Stufen 18."],
        "STRUCT_REACTOR": ["# Kernreaktor",
            "@6 Ein Kernreaktor in einer Wand aus Reaktorabschirmung um einen Kern aus Graphitblöcken und Brennstoffkanälen: "
            "Kern 3x3x3 bis 9x9x7, ohne Lücken, außen Wasser an der Abschirmung. Je drei Brennstoffkanäle verbrennen Stäbe "
            "und liefern Strom wie ein einzelner Reaktor; ein größerer Kern verliert am Rand weniger Neutronen, also hält "
            "jeder Stab länger.",
            "@6 Der Kern muss kritisch sein (k 1 oder mehr). Angereicherte Stäbe sind in jedem Kern kritisch. Natürliche, "
            "mit schwerem Wasser in den Fächern, nur in einem großen: 7x7x7, 8x8x6, 9x9x5 und größer, wie die CANDU-"
            "Reaktoren. Über k 1,3 geht die Kettenreaktion durch, wenn kein Bor-Steuerstab in den Fächern liegt. Geht er "
            "durch oder fehlt das Wasser, macht der Reaktor einen SCRAM: der Stab ist verloren, und einen halben Tag lang "
            "tritt Strahlung aus. Er explodiert nie."],
    },
    "it_it": {
        "STRUCT_CASCADE": ["# Cascata di arricchimento",
            "@6 Una Cascata di arricchimento con un blocco pieno di Stadi diffusori dietro: da 1 a 4 di larghezza, 1 a 4 di "
            "altezza e fino a 16 di lunghezza, da 4 a 64 stadi in tutto; una Porta di struttura può prendere il posto di "
            "qualsiasi stadio. Il gas passa per tutti gli stadi, quindi ognuno aggiunge velocità e impoverisce di più le "
            "code: da sola la macchina fa di 100 naturale 14,5 arricchito; con 64 stadi, 18."],
        "STRUCT_REACTOR": ["# Reattore nucleare",
            "@6 Un Reattore nucleare in una parete di Schermatura del reattore intorno a un nocciolo di Blocchi di grafite e "
            "Canali del combustibile: nocciolo da 3x3x3 a 9x9x7, senza buchi, con acqua a contatto della schermatura fuori. "
            "Ogni tre Canali del combustibile bruciano barre e danno corrente come un reattore da solo; un nocciolo più "
            "grande perde meno neutroni dal bordo, quindi ogni barra dura di più.",
            "@6 Il nocciolo deve essere critico (k 1 o più). Le barre arricchite sono critiche in qualsiasi nocciolo. Quelle "
            "naturali, con acqua pesante negli slot, solo in uno grande: 7x7x7, 8x8x6, 9x9x5 o più, come i reattori CANDU. "
            "Sopra k 1,3 la reazione a catena va fuori controllo, a meno che negli slot ci sia una Barra di controllo al "
            "boro. Se va fuori controllo, o manca l'acqua, il reattore fa SCRAM: la barra è persa e per mezza giornata "
            "fuoriescono radiazioni intorno. Non esplode mai."],
    },
    "nl_nl": {
        "STRUCT_CASCADE": ["# Verrijkingscascade",
            "@6 Een Verrijkingscascade met een massief blok Diffusietrappen erachter: 1 tot 4 breed, 1 tot 4 hoog en tot 16 "
            "lang, 4 tot 64 trappen in totaal; een Structuurpoort mag elke trap vervangen. Het gas gaat door alle trappen, "
            "dus elke trap geeft snelheid en put de restanten verder uit: alleen maakt de machine van 100 natuurlijk 14,5 "
            "verrijkt, met 64 trappen 18."],
        "STRUCT_REACTOR": ["# Kernreactor",
            "@6 Een Kernreactor in een wand van Reactorafscherming rond een kern van Grafietblokken en Splijtstofkanalen: "
            "kern 3x3x3 tot 9x9x7, zonder gaten, met water tegen de buitenkant van de afscherming. Elke drie "
            "Splijtstofkanalen verbranden staven en geven stroom als één losse reactor; een grotere kern verliest minder "
            "neutronen aan de rand, dus elke staaf gaat langer mee.",
            "@6 De kern moet kritisch zijn (k 1 of meer). Verrijkte staven zijn in elke kern kritisch. Natuurlijke, met zwaar "
            "water in de vakken, alleen in een grote: 7x7x7, 8x8x6, 9x9x5 en groter, zoals de CANDU-reactoren. Boven k 1,3 "
            "slaat de kettingreactie op hol, tenzij er een Regelstaaf van boor in de vakken ligt. Slaat hij op hol of "
            "verdwijnt het water, dan maakt de reactor een SCRAM: de staaf is verloren en er lekt een halve dag straling "
            "rondom. Hij ontploft nooit."],
    },
    "sv_se": {
        "STRUCT_CASCADE": ["# Anrikningskaskad",
            "@6 En Anrikningskaskad med ett massivt block av Diffusionssteg bakom sig: 1 till 4 bred, 1 till 4 hög och upp till "
            "16 lång, 4 till 64 steg i allt; en Strukturport får ersätta vilket steg som helst. Gasen går genom alla steg, "
            "så varje steg ger fart och utarmar avfallet mer: ensam gör maskinen 100 naturligt till 14,5 anrikat, med 64 "
            "steg till 18."],
        "STRUCT_REACTOR": ["# Kärnreaktor",
            "@6 En Kärnreaktor i en vägg av Reaktorskärm runt en kärna av Grafitblock och Bränslekanaler: kärna 3x3x3 till "
            "9x9x7, utan hål, med vatten mot skärmens utsida. Var tredje Bränslekanal bränner stavar och ger ström som en "
            "ensam reaktor; en större kärna tappar färre neutroner vid kanten, så varje stav räcker längre.",
            "@6 Kärnan måste vara kritisk (k 1 eller mer). Anrikade stavar är kritiska i vilken kärna som helst. Naturliga, "
            "med tungt vatten i facken, bara i en stor: 7x7x7, 8x8x6, 9x9x5 och större, som CANDU-reaktorerna. Över k 1,3 "
            "skenar kedjereaktionen om ingen Borstyrstav ligger i facken. Skenar den, eller försvinner vattnet, gör "
            "reaktorn SCRAM: staven går förlorad och strålning läcker ut runt den i en halv dag. Den exploderar aldrig."],
    },
    "da_dk": {
        "STRUCT_CASCADE": ["# Berigningskaskade",
            "@6 En Berigningskaskade med en massiv blok af Diffusionstrin bag sig: 1 til 4 bred, 1 til 4 høj og op til 16 "
            "lang, 4 til 64 trin i alt; en Strukturport må erstatte ethvert trin. Gassen går gennem alle trin, så hvert trin "
            "giver fart og udtømmer affaldet mere: alene gør maskinen 100 naturligt til 14,5 beriget, med 64 trin til 18."],
        "STRUCT_REACTOR": ["# Atomreaktor",
            "@6 En Atomreaktor i en væg af Reaktorafskærmning rundt om en kerne af Grafitblokke og Brændselskanaler: kerne "
            "3x3x3 til 9x9x7, uden huller, med vand op ad afskærmningens yderside. Hver tredje Brændselskanal brænder stave "
            "og giver strøm som en enkelt reaktor; en større kerne mister færre neutroner ved kanten, så hver stav holder "
            "længere.",
            "@6 Kernen skal være kritisk (k 1 eller mere). Berigede stave er kritiske i enhver kerne. Naturlige, med tungt "
            "vand i rummene, kun i en stor: 7x7x7, 8x8x6, 9x9x5 og større, som CANDU-reaktorerne. Over k 1,3 løber "
            "kædereaktionen løbsk, medmindre der ligger en Borstyrestav i rummene. Løber den løbsk, eller forsvinder "
            "vandet, laver reaktoren SCRAM: staven er tabt, og stråling siver ud omkring den en halv dag. Den eksploderer "
            "aldrig."],
    },
    "ru_ru": {
        "STRUCT_CASCADE": ["# Каскад обогащения",
            "@6 Каскад обогащения со сплошным блоком Диффузионных ступеней позади: от 1 до 4 в ширину, от 1 до 4 в высоту и "
            "до 16 в длину, всего от 4 до 64 ступеней; Порт конструкции может стоять вместо любой ступени. Газ проходит все "
            "ступени, поэтому каждая добавляет скорость и глубже обедняет отвал: одна машина делает из 100 природного 14,5 "
            "обогащённого, 64 ступени — 18."],
        "STRUCT_REACTOR": ["# Ядерный реактор",
            "@6 Ядерный реактор в стене из Защиты реактора вокруг активной зоны из Графитовых блоков и Топливных каналов: "
            "зона от 3x3x3 до 9x9x7, без пустот, снаружи к защите прилегает вода. Каждые три Топливных канала жгут стержни "
            "и дают ток, как один реактор; большая зона теряет меньше нейтронов на краю, так что стержень служит дольше.",
            "@6 Зона должна быть критичной (k 1 и больше). Обогащённые стержни критичны в любой зоне. Природные, с тяжёлой "
            "водой в ячейках, только в большой: 7x7x7, 8x8x6, 9x9x5 и больше, как реакторы CANDU. Выше k 1,3 цепная "
            "реакция идёт вразнос, если в ячейках нет Борного управляющего стержня. При разгоне или без воды срабатывает "
            "аварийная защита: стержень потерян, и полдня вокруг утекает излучение. Реактор никогда не взрывается."],
    },
}
for _c in CODES:
    BLOCKS[_c].update(_BLOCKS3[_c])
    LANG[_c].update(dict(zip(_KEYS3, _LANG3[_c], strict=True)))
    T[_c].update(_T3[_c])
    GUIDE[_c].update(_GUIDE3[_c])
