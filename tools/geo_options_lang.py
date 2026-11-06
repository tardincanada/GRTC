"""3.49.0: strings of the Geological world's "Customize" screen (worldgen/GeoOptions, client/GeoOptionsScreen)."""
P = "gui.periodic_elements.geo_options"
GEO_OPTIONS_LANG = {
    f"{P}.title": "Geological World",
    f"{P}.biomes": "Biome size",
    f"{P}.biomes.small": "Small",
    f"{P}.biomes.normal": "Normal",
    f"{P}.biomes.large": "Large",
    f"{P}.biomes.huge": "Huge",
    f"{P}.biomes.tip": "How far one climate (temperature and humidity) stretches before the next. Large is about what worlds before 3.49 had.",
    f"{P}.provinces": "Rock province size",
    f"{P}.provinces.small": "Small",
    f"{P}.provinces.normal": "Normal",
    f"{P}.provinces.large": "Large",
    f"{P}.provinces.tip": "Width of each geological province (granite shield, sedimentary basin, volcanic field): about 600, 1100 or 1800 blocks.",
    f"{P}.volcanoes": "Volcanoes",
    f"{P}.volcanoes.few": "Rare",
    f"{P}.volcanoes.normal": "Normal",
    f"{P}.volcanoes.many": "Frequent",
    f"{P}.volcanoes.tip": "How many volcanic cones rise in volcanic provinces and beside mountain belts.",
    f"{P}.ocean": "Ocean",
    f"{P}.ocean.few": "Less",
    f"{P}.ocean.normal": "Normal",
    f"{P}.ocean.many": "More",
    f"{P}.ocean.tip": "How much of the world is sea.",
    f"{P}.rivers": "Rivers",
    f"{P}.rivers.few": "Fewer",
    f"{P}.rivers.normal": "Normal",
    f"{P}.rivers.many": "More",
    f"{P}.rivers.tip": "How small a stream may be and still become a river.",
    f"{P}.note": "Saved with the world. Worlds made before 3.49 keep their old look.",
}

# English string -> translation, merged into data/lang/<code>.json by hand (kept here as the source).
T = {
 "pt_br": ["Mundo Geológico", "Tamanho dos biomas", "Pequeno", "Normal", "Grande", "Enorme", "Distância que um clima (temperatura e umidade) ocupa antes do próximo. Grande é mais ou menos o que os mundos de antes da 3.49 tinham.", "Tamanho das províncias de rocha", "Largura de cada província geológica (escudo de granito, bacia sedimentar, campo vulcânico): cerca de 600, 1100 ou 1800 blocos.", "Vulcões", "Raros", "Frequentes", "Quantos cones vulcânicos se erguem nas províncias vulcânicas e ao lado das cordilheiras.", "Oceano", "Menos", "Mais", "Quanto do mundo é mar.", "Rios", "Menos", "Quão pequeno um córrego pode ser e ainda virar rio.", "Salvo com o mundo. Mundos feitos antes da 3.49 mantêm a aparência antiga."],
 "es_es": ["Mundo geológico", "Tamaño de los biomas", "Pequeño", "Normal", "Grande", "Enorme", "Hasta dónde se extiende un clima (temperatura y humedad) antes del siguiente. Grande es más o menos lo que tenían los mundos anteriores a la 3.49.", "Tamaño de las provincias de roca", "Anchura de cada provincia geológica (escudo de granito, cuenca sedimentaria, campo volcánico): unos 600, 1100 o 1800 bloques.", "Volcanes", "Raros", "Frecuentes", "Cuántos conos volcánicos se alzan en las provincias volcánicas y junto a las cordilleras.", "Océano", "Menos", "Más", "Cuánto del mundo es mar.", "Ríos", "Menos", "Lo pequeño que puede ser un arroyo y aun así volverse río.", "Se guarda con el mundo. Los mundos creados antes de la 3.49 conservan su aspecto antiguo."],
 "fr_fr": ["Monde géologique", "Taille des biomes", "Petite", "Normale", "Grande", "Énorme", "Jusqu'où s'étend un climat (température et humidité) avant le suivant. Grande correspond à peu près aux mondes d'avant la 3.49.", "Taille des provinces rocheuses", "Largeur de chaque province géologique (bouclier de granite, bassin sédimentaire, champ volcanique) : environ 600, 1100 ou 1800 blocs.", "Volcans", "Rares", "Fréquents", "Combien de cônes volcaniques s'élèvent dans les provinces volcaniques et le long des chaînes de montagnes.", "Océan", "Moins", "Plus", "La part du monde couverte par la mer.", "Rivières", "Moins", "La taille minimale d'un ruisseau pour devenir une rivière.", "Enregistré avec le monde. Les mondes créés avant la 3.49 gardent leur ancien aspect."],
 "de_de": ["Geologische Welt", "Biomgröße", "Klein", "Normal", "Groß", "Riesig", "Wie weit sich ein Klima (Temperatur und Feuchtigkeit) erstreckt, bevor das nächste kommt. Groß entspricht etwa den Welten vor 3.49.", "Größe der Gesteinsprovinzen", "Breite jeder geologischen Provinz (Granitschild, Sedimentbecken, Vulkanfeld): etwa 600, 1100 oder 1800 Blöcke.", "Vulkane", "Selten", "Häufig", "Wie viele Vulkankegel sich in vulkanischen Provinzen und neben Gebirgsgürteln erheben.", "Ozean", "Weniger", "Mehr", "Wie viel der Welt Meer ist.", "Flüsse", "Weniger", "Wie klein ein Bach sein darf, um noch zum Fluss zu werden.", "Wird mit der Welt gespeichert. Welten von vor 3.49 behalten ihr altes Aussehen."],
 "it_it": ["Mondo geologico", "Dimensione dei biomi", "Piccola", "Normale", "Grande", "Enorme", "Quanto si estende un clima (temperatura e umidità) prima del successivo. Grande è circa ciò che avevano i mondi prima della 3.49.", "Dimensione delle province rocciose", "Larghezza di ogni provincia geologica (scudo di granito, bacino sedimentario, campo vulcanico): circa 600, 1100 o 1800 blocchi.", "Vulcani", "Rari", "Frequenti", "Quanti coni vulcanici sorgono nelle province vulcaniche e accanto alle catene montuose.", "Oceano", "Meno", "Più", "Quanta parte del mondo è mare.", "Fiumi", "Meno", "Quanto può essere piccolo un ruscello per diventare comunque un fiume.", "Salvato con il mondo. I mondi creati prima della 3.49 mantengono il vecchio aspetto."],
 "nl_nl": ["Geologische wereld", "Biomegrootte", "Klein", "Normaal", "Groot", "Enorm", "Hoe ver één klimaat (temperatuur en vochtigheid) reikt voor het volgende. Groot is ongeveer wat werelden van vóór 3.49 hadden.", "Grootte van gesteenteprovincies", "Breedte van elke geologische provincie (granietschild, sedimentair bekken, vulkanisch veld): ongeveer 600, 1100 of 1800 blokken.", "Vulkanen", "Zeldzaam", "Vaak", "Hoeveel vulkaankegels er oprijzen in vulkanische provincies en naast bergketens.", "Oceaan", "Minder", "Meer", "Hoeveel van de wereld zee is.", "Rivieren", "Minder", "Hoe klein een beek mag zijn om toch een rivier te worden.", "Wordt met de wereld opgeslagen. Werelden van vóór 3.49 houden hun oude uiterlijk."],
 "da_dk": ["Geologisk verden", "Biomstørrelse", "Lille", "Normal", "Stor", "Kæmpe", "Hvor langt ét klima (temperatur og fugtighed) strækker sig før det næste. Stor svarer omtrent til verdener fra før 3.49.", "Størrelse på bjergartsprovinser", "Bredden af hver geologisk provins (granitskjold, sedimentært bassin, vulkansk felt): omkring 600, 1100 eller 1800 blokke.", "Vulkaner", "Sjældne", "Hyppige", "Hvor mange vulkankegler der rejser sig i vulkanske provinser og ved bjergkæder.", "Hav", "Mindre", "Mere", "Hvor meget af verden der er hav.", "Floder", "Færre", "Hvor lille en bæk må være og stadig blive en flod.", "Gemmes med verdenen. Verdener lavet før 3.49 beholder deres gamle udseende."],
 "sv_se": ["Geologisk värld", "Biomstorlek", "Liten", "Normal", "Stor", "Enorm", "Hur långt ett klimat (temperatur och fuktighet) sträcker sig innan nästa. Stor motsvarar ungefär världar från före 3.49.", "Storlek på bergartsprovinser", "Bredden på varje geologisk provins (granitsköld, sedimentbassäng, vulkaniskt fält): ungefär 600, 1100 eller 1800 block.", "Vulkaner", "Sällsynta", "Vanliga", "Hur många vulkankoner som reser sig i vulkaniska provinser och vid bergskedjor.", "Hav", "Mindre", "Mer", "Hur mycket av världen som är hav.", "Floder", "Färre", "Hur liten en bäck får vara och ändå bli en flod.", "Sparas med världen. Världar skapade före 3.49 behåller sitt gamla utseende."],
 "ru_ru": ["Геологический мир", "Размер биомов", "Маленький", "Обычный", "Большой", "Огромный", "Как далеко тянется один климат (температура и влажность) до следующего. «Большой» примерно соответствует мирам до 3.49.", "Размер горных провинций", "Ширина каждой геологической провинции (гранитный щит, осадочный бассейн, вулканическое поле): около 600, 1100 или 1800 блоков.", "Вулканы", "Редко", "Часто", "Сколько вулканических конусов поднимается в вулканических провинциях и у горных поясов.", "Океан", "Меньше", "Больше", "Какая часть мира занята морем.", "Реки", "Меньше", "Насколько маленьким может быть ручей, чтобы всё же стать рекой.", "Сохраняется вместе с миром. Миры, созданные до 3.49, сохраняют прежний вид."],
}
# T order: title, biomes, small, normal, large, huge, biomes.tip, provinces, provinces.tip, volcanoes, rare, frequent,
# volcanoes.tip, ocean, less, more, ocean.tip, rivers, fewer, rivers.tip, note
ORDER = [f"{P}.title", f"{P}.biomes", f"{P}.biomes.small", f"{P}.biomes.normal", f"{P}.biomes.large", f"{P}.biomes.huge",
         f"{P}.biomes.tip", f"{P}.provinces", f"{P}.provinces.tip", f"{P}.volcanoes", f"{P}.volcanoes.few", f"{P}.volcanoes.many",
         f"{P}.volcanoes.tip", f"{P}.ocean", f"{P}.ocean.few", f"{P}.ocean.many", f"{P}.ocean.tip", f"{P}.rivers",
         f"{P}.rivers.few", f"{P}.rivers.tip", f"{P}.note"]
