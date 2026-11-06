"""Salt water and fresh water (3.45.0): blockstates for the salt water and brine fluids, the fluid tag is a static file
(data/minecraft/tags/fluid/water.json), the "flasks to bucket" recipe, strings and Field Guide paragraphs (end of the
Containers chapter), in 10 languages.

Java: com.periodicelements.water (Water registers the fluids, makes the sea salt after a chunk's features, digs brine
springs; SeaFluid is the fluid), mixins WaterFluidMixin, FlowableFluidMixin (infinite sources), FluidBlockMixin,
BiomeIceMixin, ChunkGeneratorMixin, NoiseConfigMixin (climate for rivers). The Seawater Still, its reactions and the
Desalination research are rows of data/machines.csv, reactions.csv and research.csv; tools/machines.py has its name,
recipe and look. generate.py calls generate_water and merges translations(code); guidebook.py appends GUIDE[code].
"""
import json

BLOCKS = {"salt_water": "Salt Water", "brine": "Brine"}

# English strings whose translations live here (machine, research, reactions, items).
STRINGS = [
    "Seawater Still",
    "A copper pot still over a fire: seawater boils and its steam condenses as pure fresh water; the salt stays in the pot",
    "Desalination",
    "Boil seawater in a copper still and condense the steam: pure fresh water at sea (Richard Hawkins 1594)",
    "Seawater boiled in a still: the steam condenses as pure fresh water, like distilled water (ships' stills, Hawkins 1594)",
    "Common salt scraped from the pot once the water has boiled off; the bitter salts are left as bittern",
    "Salt water from the sea, 3.5% salts: dry it in a Salt Pan, or boil it in a Seawater Still for fresh water",
    "Bucket of Brine",
    "Spring brine, 26% salts: a Salt Pan dries it to far more salt than seawater gives",
    "Salt Water",
    "Brine",
    "Still salt",
]

_T = {
    "pt_br": [
        "Destilador de Água do Mar",
        "Um alambique de cobre sobre o fogo: a água do mar ferve e o vapor condensa como água doce pura; o sal fica no tacho",
        "Dessalinização",
        "Ferva água do mar num destilador de cobre e condense o vapor: água doce pura no mar (Richard Hawkins, 1594)",
        "Água do mar fervida num destilador: o vapor condensa como água doce pura, igual à destilada (destiladores de navio, Hawkins 1594)",
        "Sal comum raspado do tacho quando a água acaba de ferver; os sais amargos ficam como água-mãe",
        "Água salgada do mar, 3,5% de sais: seque-a numa Salina ou ferva-a num Destilador de Água do Mar para ter água doce",
        "Balde de Salmoura",
        "Salmoura de fonte, 26% de sais: uma Salina tira dela muito mais sal do que da água do mar",
        "Água Salgada",
        "Salmoura",
        "Sal do destilador",
    ],
    "es_es": [
        "Destilador de agua de mar",
        "Un alambique de cobre sobre el fuego: el agua de mar hierve y su vapor se condensa como agua dulce pura; la sal queda en la olla",
        "Desalinización",
        "Hierve agua de mar en un destilador de cobre y condensa el vapor: agua dulce pura en el mar (Richard Hawkins, 1594)",
        "Agua de mar hervida en un destilador: el vapor se condensa como agua dulce pura, igual que la destilada (destiladores de barco, Hawkins 1594)",
        "Sal común rascada de la olla cuando el agua ha hervido; las sales amargas quedan como aguas madres",
        "Agua salada del mar, 3,5% de sales: sécala en una Salina o hiérvela en un Destilador de agua de mar para tener agua dulce",
        "Cubo de salmuera",
        "Salmuera de manantial, 26% de sales: una Salina saca de ella mucha más sal que del agua de mar",
        "Agua salada",
        "Salmuera",
        "Sal del destilador",
    ],
    "fr_fr": [
        "Distillateur d'eau de mer",
        "Un alambic de cuivre sur le feu : l'eau de mer bout et sa vapeur se condense en eau douce pure ; le sel reste dans la cuve",
        "Dessalement",
        "Faites bouillir l'eau de mer dans un alambic de cuivre et condensez la vapeur : de l'eau douce pure en mer (Richard Hawkins, 1594)",
        "Eau de mer bouillie dans un alambic : la vapeur se condense en eau douce pure, comme l'eau distillée (alambics de bord, Hawkins 1594)",
        "Sel commun raclé dans la cuve une fois l'eau évaporée ; les sels amers restent en eaux-mères",
        "Eau salée de la mer, 3,5 % de sels : séchez-la dans un Marais salant ou faites-la bouillir dans un Distillateur d'eau de mer pour avoir de l'eau douce",
        "Seau de saumure",
        "Saumure de source, 26 % de sels : un Marais salant en tire bien plus de sel que de l'eau de mer",
        "Eau salée",
        "Saumure",
        "Sel du distillateur",
    ],
    "de_de": [
        "Meerwasser-Destille",
        "Eine kupferne Brennblase über dem Feuer: Meerwasser kocht, sein Dampf kondensiert zu reinem Süßwasser; das Salz bleibt im Kessel",
        "Entsalzung",
        "Meerwasser in einer kupfernen Destille kochen und den Dampf kondensieren: reines Süßwasser auf See (Richard Hawkins 1594)",
        "Meerwasser in einer Destille gekocht: der Dampf kondensiert zu reinem Süßwasser, wie destilliertes Wasser (Schiffsdestillen, Hawkins 1594)",
        "Kochsalz, aus dem Kessel gekratzt, wenn das Wasser verkocht ist; die bitteren Salze bleiben als Mutterlauge",
        "Salzwasser aus dem Meer, 3,5 % Salze: in einer Salzpfanne trocknen oder in einer Meerwasser-Destille zu Süßwasser kochen",
        "Soleeimer",
        "Quellsole, 26 % Salze: eine Salzpfanne gewinnt daraus viel mehr Salz als aus Meerwasser",
        "Salzwasser",
        "Sole",
        "Destillensalz",
    ],
    "it_it": [
        "Distillatore d'acqua di mare",
        "Un alambicco di rame sul fuoco: l'acqua di mare bolle e il vapore si condensa in acqua dolce pura; il sale resta nella caldaia",
        "Dissalazione",
        "Fai bollire l'acqua di mare in un distillatore di rame e condensa il vapore: acqua dolce pura in mare (Richard Hawkins, 1594)",
        "Acqua di mare bollita in un distillatore: il vapore si condensa in acqua dolce pura, come quella distillata (distillatori di bordo, Hawkins 1594)",
        "Sale comune raschiato dalla caldaia quando l'acqua è evaporata; i sali amari restano come acque madri",
        "Acqua salata del mare, 3,5% di sali: seccala in una Salina o falla bollire in un Distillatore d'acqua di mare per l'acqua dolce",
        "Secchio di salamoia",
        "Salamoia di sorgente, 26% di sali: una Salina ne ricava molto più sale che dall'acqua di mare",
        "Acqua salata",
        "Salamoia",
        "Sale del distillatore",
    ],
    "nl_nl": [
        "Zeewaterdestilleerketel",
        "Een koperen distilleerketel boven het vuur: zeewater kookt en de damp condenseert tot zuiver zoet water; het zout blijft in de ketel",
        "Ontzilting",
        "Kook zeewater in een koperen ketel en condenseer de damp: zuiver zoet water op zee (Richard Hawkins, 1594)",
        "Zeewater gekookt in een distilleerketel: de damp condenseert tot zuiver zoet water, net als gedestilleerd water (scheepsketels, Hawkins 1594)",
        "Keukenzout uit de ketel geschraapt als het water is weggekookt; de bittere zouten blijven over als moederloog",
        "Zout water uit zee, 3,5% zouten: droog het in een Zoutpan of kook het in een Zeewaterdestilleerketel voor zoet water",
        "Emmer pekel",
        "Bronpekel, 26% zouten: een Zoutpan haalt er veel meer zout uit dan uit zeewater",
        "Zout water",
        "Pekel",
        "Ketelzout",
    ],
    "ru_ru": [
        "Опреснитель морской воды",
        "Медный перегонный куб над огнём: морская вода кипит, пар конденсируется в чистую пресную воду; соль остаётся в котле",
        "Опреснение",
        "Кипятите морскую воду в медном кубе и конденсируйте пар: чистая пресная вода в море (Ричард Хокинс, 1594)",
        "Морская вода, прокипячённая в кубе: пар конденсируется в чистую пресную воду, как дистиллированная (судовые опреснители, Хокинс 1594)",
        "Поваренная соль, соскобленная со дна котла, когда вода выкипела; горькие соли остаются маточным рассолом",
        "Солёная вода из моря, 3,5% солей: высушите её в соляном бассейне или выкипятите в опреснителе морской воды ради пресной воды",
        "Ведро рассола",
        "Рассол из источника, 26% солей: соляной бассейн получает из него гораздо больше соли, чем из морской воды",
        "Солёная вода",
        "Рассол",
        "Соль из опреснителя",
    ],
    "sv_se": [
        "Havsvattendestillator",
        "En kopparpanna över elden: havsvatten kokar och ångan kondenserar till rent sötvatten; saltet stannar i pannan",
        "Avsaltning",
        "Koka havsvatten i en kopparpanna och kondensera ångan: rent sötvatten till sjöss (Richard Hawkins 1594)",
        "Havsvatten kokat i en destillator: ångan kondenserar till rent sötvatten, som destillerat vatten (skeppsdestillatorer, Hawkins 1594)",
        "Koksalt skrapat ur pannan när vattnet har kokat bort; de bittra salterna blir kvar som moderlut",
        "Saltvatten från havet, 3,5 % salter: torka det i en Saltpanna eller koka det i en Havsvattendestillator för sötvatten",
        "Hink med saltlake",
        "Källsaltlake, 26 % salter: en Saltpanna får ut mycket mer salt ur den än ur havsvatten",
        "Saltvatten",
        "Saltlake",
        "Destillatorsalt",
    ],
    "da_dk": [
        "Havvandsdestillator",
        "En kobberkedel over ilden: havvand koger, og dampen fortættes til rent ferskvand; saltet bliver i kedlen",
        "Afsaltning",
        "Kog havvand i en kobberkedel og fortæt dampen: rent ferskvand til søs (Richard Hawkins 1594)",
        "Havvand kogt i en destillator: dampen fortættes til rent ferskvand, ligesom destilleret vand (skibsdestillatorer, Hawkins 1594)",
        "Køkkensalt skrabet ud af kedlen, når vandet er kogt væk; de bitre salte bliver tilbage som moderlud",
        "Saltvand fra havet, 3,5 % salte: tør det i en Saltpande, eller kog det i en Havvandsdestillator for ferskvand",
        "Spand med saltlage",
        "Kildesaltlage, 26 % salte: en Saltpande får langt mere salt ud af den end af havvand",
        "Saltvand",
        "Saltlage",
        "Destillatorsalt",
    ],
}

# Field Guide paragraphs, end of the Containers chapter.
GUIDE = {
    "en_us": [
        "@1 # Salt water and fresh water",
        "@1 The sea is salt water: the water of every ocean, beach and stony shore, and of lakes in ocean biomes. Rivers "
        "and lakes inland are fresh water, as pure as distilled water. They look and behave the same; only a bucket tells "
        "them apart (Bucket of Water or Bucket of Seawater), and a glass bottle fills from fresh water only. In the "
        "Geological world no river rises in a desert or badlands, though one from wetter land can run through them.",
        "@1 Two fresh water sources make an endless fresh source between them, and two salt sources a salt one. A fresh "
        "source and a salt source always make salt water, and the fresh source turns salt too. Fresh water poured into "
        "the sea becomes sea water.",
        "@1 Rare brine springs on dry land, ringed with a white salt crust, hold brine with 26% salts: a Salt Pan dries a "
        "bucket of it to about eight times the salt of a bucket of seawater. Brine makes no endless source.",
        "@2 Desalination (level 2) unlocks the Seawater Still. Put in seawater, fuel and empty flasks: it boils the water "
        "off into flasks of fresh water and leaves common salt and bittern. Eight flasks of water around an empty bucket "
        "make a Bucket of Water.",
        "@2 =grid desalination_still",
    ],
    "pt_br": [
        "@1 # Água salgada e água doce",
        "@1 O mar é de água salgada: a água de todo oceano, praia e costa rochosa, e dos lagos dos biomas oceânicos. Rios "
        "e lagos do interior são de água doce, tão pura quanto a destilada. As duas têm a mesma aparência e o mesmo "
        "comportamento; só o balde as distingue (Balde de Água ou Balde de Água do Mar), e o frasco de vidro das poções só "
        "enche com água doce. No mundo Geológico nenhum rio nasce em deserto ou badlands, mas um rio de terras mais úmidas pode "
        "atravessá-los.",
        "@1 Duas fontes de água doce fazem entre elas uma fonte infinita de água doce, e duas de água salgada, uma de água "
        "salgada. Uma fonte doce com uma salgada faz sempre água salgada, e a fonte doce também vira salgada. Água doce "
        "despejada no mar vira água do mar.",
        "@1 Raros poços de salmoura em terra seca, cercados por uma crosta branca de sal, guardam salmoura com 26% de sais: "
        "uma Salina tira de um balde dela cerca de oito vezes o sal de um balde de água do mar. A salmoura não faz fonte "
        "infinita.",
        "@2 Dessalinização (nível 2) libera o Destilador de Água do Mar. Ponha água do mar, combustível e frascos vazios: "
        "ele ferve a água e a recolhe em frascos de água doce, deixando sal comum e água-mãe. Oito frascos de água em volta "
        "de um balde vazio fazem um Balde de Água.",
        "@2 =grid desalination_still",
    ],
    "es_es": [
        "@1 # Agua salada y agua dulce",
        "@1 El mar es agua salada: el agua de todo océano, playa y costa rocosa, y de los lagos de los biomas oceánicos. "
        "Los ríos y lagos del interior son agua dulce, tan pura como la destilada. Las dos se ven y se comportan igual; "
        "solo el cubo las distingue (Cubo de agua o Cubo de agua de mar), y el frasco de cristal solo se llena con agua "
        "dulce. En el mundo Geológico ningún río nace en un desierto o en unas badlands, pero uno de tierras más húmedas "
        "puede cruzarlos.",
        "@1 Dos fuentes de agua dulce hacen entre ellas una fuente infinita de agua dulce, y dos de agua salada, una de "
        "agua salada. Una fuente dulce con una salada siempre da agua salada, y la fuente dulce también se vuelve salada. "
        "El agua dulce vertida en el mar se vuelve agua de mar.",
        "@1 Raros pozos de salmuera en tierra seca, rodeados de una costra blanca de sal, guardan salmuera con un 26% de "
        "sales: una Salina saca de un cubo de ella unas ocho veces la sal de un cubo de agua de mar. La salmuera no hace "
        "fuentes infinitas.",
        "@2 Desalinización (nivel 2) desbloquea el Destilador de agua de mar. Pon agua de mar, combustible y matraces "
        "vacíos: hierve el agua y la recoge en matraces de agua dulce, y deja sal común y aguas madres. Ocho matraces de "
        "agua alrededor de un cubo vacío hacen un Cubo de agua.",
        "@2 =grid desalination_still",
    ],
    "fr_fr": [
        "@1 # Eau salée et eau douce",
        "@1 La mer est d'eau salée : l'eau de chaque océan, plage et côte rocheuse, et des lacs des biomes océaniques. Les "
        "rivières et les lacs de l'intérieur sont d'eau douce, aussi pure que l'eau distillée. Les deux ont le même aspect "
        "et le même comportement ; seul le seau les distingue (Seau d'eau ou Seau d'eau de mer), et la fiole en verre ne "
        "se remplit que d'eau douce. Dans le monde Géologique, aucune rivière ne prend sa source dans un désert ou des "
        "badlands, mais une rivière venue de terres plus humides peut les traverser.",
        "@1 Deux sources d'eau douce font entre elles une source infinie d'eau douce, et deux sources salées une source "
        "salée. Une source douce et une source salée donnent toujours de l'eau salée, et la source douce devient salée "
        "elle aussi. L'eau douce versée dans la mer devient de l'eau de mer.",
        "@1 De rares puits de saumure en terre sèche, entourés d'une croûte de sel blanche, contiennent une saumure à 26 % "
        "de sels : un Marais salant tire d'un seau de saumure environ huit fois le sel d'un seau d'eau de mer. La saumure "
        "ne fait pas de source infinie.",
        "@2 Dessalement (niveau 2) débloque le Distillateur d'eau de mer. Mettez de l'eau de mer, du combustible et des "
        "fioles vides : il fait bouillir l'eau et la recueille en fioles d'eau douce, en laissant du sel commun et des "
        "eaux-mères. Huit fioles d'eau autour d'un seau vide donnent un Seau d'eau.",
        "@2 =grid desalination_still",
    ],
    "de_de": [
        "@1 # Salzwasser und Süßwasser",
        "@1 Das Meer ist Salzwasser: das Wasser jedes Ozeans, Strands und jeder Steilküste und der Seen in Ozeanbiomen. "
        "Flüsse und Seen im Landesinneren sind Süßwasser, so rein wie destilliertes Wasser. Beide sehen gleich aus und "
        "verhalten sich gleich; nur ein Eimer unterscheidet sie (Wassereimer oder Meerwassereimer), und eine Glasflasche "
        "füllt sich nur mit Süßwasser. In der geologischen Welt entspringt kein Fluss in einer Wüste oder in Tafelbergen, "
        "aber einer aus feuchterem Land kann sie durchqueren.",
        "@1 Zwei Süßwasserquellen machen zwischen sich eine unendliche Süßwasserquelle, zwei Salzwasserquellen eine "
        "salzige. Eine süße und eine salzige Quelle machen immer Salzwasser, und die süße Quelle wird ebenfalls salzig. "
        "Ins Meer gegossenes Süßwasser wird zu Meerwasser.",
        "@1 Seltene Solebrunnen auf trockenem Land, von einer weißen Salzkruste umgeben, enthalten Sole mit 26 % Salzen: "
        "eine Salzpfanne gewinnt aus einem Eimer davon etwa achtmal so viel Salz wie aus einem Eimer Meerwasser. Sole "
        "bildet keine unendliche Quelle.",
        "@2 Entsalzung (Stufe 2) schaltet die Meerwasser-Destille frei. Gib Meerwasser, Brennstoff und leere Kolben "
        "hinein: sie kocht das Wasser ab und fängt es in Kolben mit Süßwasser auf; Kochsalz und Mutterlauge bleiben "
        "zurück. Acht Kolben Wasser um einen leeren Eimer ergeben einen Wassereimer.",
        "@2 =grid desalination_still",
    ],
    "it_it": [
        "@1 # Acqua salata e acqua dolce",
        "@1 Il mare è d'acqua salata: l'acqua di ogni oceano, spiaggia e costa rocciosa, e dei laghi dei biomi oceanici. "
        "Fiumi e laghi dell'entroterra sono d'acqua dolce, pura come quella distillata. Le due hanno lo stesso aspetto e "
        "lo stesso comportamento; solo il secchio le distingue (Secchio d'acqua o Secchio di acqua di mare), e la "
        "bottiglia di vetro si riempie solo d'acqua dolce. Nel mondo Geologico nessun fiume nasce in un deserto o nelle "
        "badlands, ma uno che viene da terre più umide può attraversarli.",
        "@1 Due sorgenti d'acqua dolce fanno tra loro una sorgente infinita d'acqua dolce, e due salate una salata. Una "
        "sorgente dolce e una salata danno sempre acqua salata, e anche la sorgente dolce diventa salata. L'acqua dolce "
        "versata in mare diventa acqua di mare.",
        "@1 Rari pozzi di salamoia in terra asciutta, cinti da una crosta bianca di sale, contengono salamoia al 26% di "
        "sali: una Salina ne ricava da un secchio circa otto volte il sale di un secchio di acqua di mare. La salamoia "
        "non fa sorgenti infinite.",
        "@2 Dissalazione (livello 2) sblocca il Distillatore d'acqua di mare. Metti acqua di mare, combustibile e beute "
        "vuote: fa bollire l'acqua e la raccoglie in beute d'acqua dolce, lasciando sale comune e acque madri. Otto beute "
        "d'acqua attorno a un secchio vuoto danno un Secchio d'acqua.",
        "@2 =grid desalination_still",
    ],
    "nl_nl": [
        "@1 # Zout water en zoet water",
        "@1 De zee is zout water: het water van elke oceaan, elk strand en elke rotskust, en van meren in oceaanbiomen. "
        "Rivieren en meren in het binnenland zijn zoet water, zo zuiver als gedestilleerd water. Ze zien er hetzelfde uit "
        "en gedragen zich hetzelfde; alleen een emmer onderscheidt ze (Emmer water of Emmer zeewater), en een glazen fles "
        "vult zich alleen met zoet water. In de geologische wereld ontspringt geen rivier in een woestijn of in badlands, "
        "maar een rivier uit vochtiger land kan er doorheen stromen.",
        "@1 Twee bronnen van zoet water maken ertussen een eindeloze zoetwaterbron, en twee zoutwaterbronnen een zoute. "
        "Een zoete en een zoute bron maken altijd zout water, en de zoete bron wordt ook zout. Zoet water dat je in zee "
        "giet, wordt zeewater.",
        "@1 Zeldzame pekelbronnen op droog land, omringd door een witte zoutkorst, houden pekel met 26% zouten: een "
        "Zoutpan haalt uit een emmer ervan ongeveer acht keer zoveel zout als uit een emmer zeewater. Pekel maakt geen "
        "eindeloze bron.",
        "@2 Ontzilting (niveau 2) ontgrendelt de Zeewaterdestilleerketel. Doe er zeewater, brandstof en lege kolven in: "
        "hij kookt het water af en vangt het op in kolven met zoet water; keukenzout en moederloog blijven achter. Acht "
        "kolven water rond een lege emmer maken een Emmer water.",
        "@2 =grid desalination_still",
    ],
    "ru_ru": [
        "@1 # Солёная и пресная вода",
        "@1 Море из солёной воды: вода каждого океана, пляжа и каменистого берега, а также озёр в океанских биомах. Реки "
        "и озёра в глубине суши пресные, чистые, как дистиллированная вода. Выглядят и ведут себя они одинаково; различает "
        "их только ведро (Ведро воды или Ведро морской воды), а стеклянный пузырёк наполняется только пресной водой. В "
        "геологическом мире ни одна река не берёт начало в пустыне или бесплодных землях, но река с более влажных земель "
        "может их пересечь.",
        "@1 Два источника пресной воды дают между собой бесконечный пресный источник, а два солёных — солёный. Пресный и "
        "солёный источник всегда дают солёную воду, и пресный источник тоже становится солёным. Пресная вода, вылитая в "
        "море, становится морской.",
        "@1 Редкие рассольные колодцы на суше, окружённые белой соляной коркой, содержат рассол с 26% солей: соляной "
        "бассейн получает из ведра рассола примерно в восемь раз больше соли, чем из ведра морской воды. Рассол не даёт "
        "бесконечного источника.",
        "@2 Опреснение (уровень 2) открывает опреснитель морской воды. Положите морскую воду, топливо и пустые колбы: он "
        "выпаривает воду и собирает её в колбы с пресной водой, оставляя поваренную соль и маточный рассол. Восемь колб "
        "воды вокруг пустого ведра дают Ведро воды.",
        "@2 =grid desalination_still",
    ],
    "sv_se": [
        "@1 # Saltvatten och sötvatten",
        "@1 Havet är saltvatten: vattnet i varje ocean, strand och klippkust, och i sjöar i havsbiomer. Floder och sjöar "
        "inåt landet är sötvatten, lika rent som destillerat vatten. De ser likadana ut och beter sig likadant; bara en "
        "hink skiljer dem åt (Vattenhink eller Hink med havsvatten), och en glasflaska fylls bara med sötvatten. I den "
        "geologiska världen rinner ingen flod upp i en öken eller i badlands, men en flod från fuktigare land kan rinna "
        "genom dem.",
        "@1 Två källor med sötvatten gör en oändlig sötvattenkälla mellan sig, och två saltvattenkällor en salt. En söt "
        "och en salt källa ger alltid saltvatten, och den söta källan blir också salt. Sötvatten som hälls i havet blir "
        "havsvatten.",
        "@1 Sällsynta saltlakebrunnar på torra land, omgivna av en vit saltskorpa, håller saltlake med 26 % salter: en "
        "Saltpanna får ur en hink av den ungefär åtta gånger saltet i en hink havsvatten. Saltlake ger ingen oändlig "
        "källa.",
        "@2 Avsaltning (nivå 2) låser upp Havsvattendestillatorn. Lägg i havsvatten, bränsle och tomma kolvar: den kokar "
        "bort vattnet och samlar det i kolvar med sötvatten och lämnar koksalt och moderlut. Åtta kolvar vatten runt en "
        "tom hink blir en Vattenhink.",
        "@2 =grid desalination_still",
    ],
    "da_dk": [
        "@1 # Saltvand og ferskvand",
        "@1 Havet er saltvand: vandet i hvert ocean, hver strand og klippekyst og i søer i havbiomer. Floder og søer inde "
        "i landet er ferskvand, lige så rent som destilleret vand. De ser ens ud og opfører sig ens; kun en spand skelner "
        "dem (Vandspand eller Spand med havvand), og en glasflaske fyldes kun med ferskvand. I den geologiske verden "
        "udspringer ingen flod i en ørken eller i badlands, men en flod fra fugtigere land kan løbe igennem dem.",
        "@1 To ferskvandskilder laver en uendelig ferskvandskilde mellem sig, og to saltvandskilder en salt. En fersk og "
        "en salt kilde giver altid saltvand, og den ferske kilde bliver også salt. Ferskvand, der hældes i havet, bliver "
        "havvand.",
        "@1 Sjældne saltlagebrønde på tørt land, omgivet af en hvid saltskorpe, rummer saltlage med 26 % salte: en "
        "Saltpande får af en spand af den omkring otte gange saltet i en spand havvand. Saltlage giver ingen uendelig "
        "kilde.",
        "@2 Afsaltning (niveau 2) låser op for Havvandsdestillatoren. Kom havvand, brændsel og tomme kolber i: den koger "
        "vandet væk og samler det i kolber med ferskvand og efterlader køkkensalt og moderlud. Otte kolber vand omkring "
        "en tom spand giver en Vandspand.",
        "@2 =grid desalination_still",
    ],
}


def generate_water(assets, data_root, lang, mod_id):
    """Blockstates for the two fluid blocks (they use vanilla's water model), block names, the bucket recipe."""
    bs = assets / "blockstates"
    bs.mkdir(parents=True, exist_ok=True)
    for bid, name in BLOCKS.items():
        (bs / f"{bid}.json").write_text(json.dumps({"variants": {"": {"model": "minecraft:block/water"}}}, indent=2) + "\n")
        lang[f"block.{mod_id}.{bid}"] = name
    recipe_dir = data_root / mod_id / "recipe"
    recipe_dir.mkdir(parents=True, exist_ok=True)
    # Eight flasks of fresh water (888.8 units) around an empty bucket fill it; the flasks come back empty.
    (recipe_dir / "water_bucket_from_flasks.json").write_text(json.dumps({
        "type": "minecraft:crafting_shaped", "category": "misc",
        "pattern": ["FFF", "FBF", "FFF"],
        "key": {"F": {"item": f"{mod_id}:water"}, "B": {"item": "minecraft:bucket"}},
        "result": {"id": "minecraft:water_bucket", "count": 1},
    }, indent=2) + "\n")


def translations(code):
    """English -> translation for the water strings in one language."""
    if code not in _T:
        return {}
    return dict(zip(STRINGS, _T[code], strict=True))
