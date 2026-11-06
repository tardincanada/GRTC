"""Trees of the world (2.8.0) in the mod's other languages.

SPECIES: each tree's name per language, one string per species in LANGS order. pt/es/fr/it/ru give the name used after
"de"/"di"/the Russian genitive; de/nl/sv/da give "name|compound stem" (the stem takes the block suffix: Mahagoni +
stamm). ru gives "name|genitive". The block names are built from PATTERNS.
TEXT: every other new English string (products, substances, reactions, interface, guide) -> translation per language.
"""

LANGS = ["pt_br", "es_es", "fr_fr", "it_it", "de_de", "nl_nl", "sv_se", "da_dk", "ru_ru"]

SPECIES = {
    "mahogany": "Mogno / caoba / acajou / mogano / Mahagonibaum|Mahagoni / mahonieboom|mahonie / mahognyträd|mahogny / mahognitræ|mahogni / Махагони|махагони",
    "brazil_nut": "Castanheira-do-pará / castaño de Pará / noyer du Brésil / noce del Brasile / Paranussbaum|Paranussbaum / paranotenboom|paranotenboom / paranötsträd|paranötsträd / paranøddetræ|paranøddetræ / Бертолеция|бертолеции",
    "brazilwood": "Pau-brasil / palo brasil / pernambouc / pernambuco / Brasilholzbaum|Brasilholz / brazielhoutboom|brazielhout / brasilträd|brasilträ / brasiltræ|brasiltræ / Фернамбук|фернамбука",
    "sapodilla": "Sapotizeiro / chicozapote / sapotillier / sapodilla / Breiapfelbaum|Breiapfel / sapodilla|sapodilla / sapotillträd|sapotill / sapodilla|sapodilla / Саподилла|саподиллы",
    "andiroba": "Andiroba / andiroba / andiroba / andiroba / Andirobabaum|Andiroba / andiroba|andiroba / andiroba|andiroba / andiroba|andiroba / Андироба|андиробы",
    "acai": "Açaizeiro / palma de asaí / palmier açaï / palma açaí / Açaí-Palme|Açaí- / açaípalm|açaípalm / açaípalm|açaípalm / açaípalme|açaípalme / Эвтерпа|эвтерпы",
    "babassu": "Babaçu / babasú / babassu / babassu / Babassupalme|Babassupalmen / babassupalm|babassupalm / babassupalm|babassupalm / babassupalme|babassupalme / Бабассу|бабассу",
    "copaiba": "Copaíba / copaiba / copaïer / copaiba / Kopaivabaum|Kopaiva / copaiba|copaiba / kopaivaträd|kopaiva / kopaivatræ|kopaiva / Копаифера|копаиферы",
    "cinchona": "Quina / quino / quinquina / china / Chinarindenbaum|Chinarindenbaum / kinaboom|kina / kinaträd|kina / kinatræ|kina / Хинное дерево|хинного дерева",
    "oil_palm": "Dendezeiro / palma aceitera / palmier à huile / palma da olio / Ölpalme|Ölpalmen / oliepalm|oliepalm / oljepalm|oljepalm / oliepalme|oliepalme / Масличная пальма|масличной пальмы",
    "teak": "Teca / teca / teck / teak / Teakbaum|Teak / teakboom|teak / teakträd|teak / teaktræ|teak / Тик|тика",
    "cinnamon": "Caneleira / canelo / cannelier / cannella / Zimtbaum|Zimt / kaneelboom|kaneel / kanelträd|kanel / kaneltræ|kanel / Коричник|коричника",
    "camphor_laurel": "Canforeira / alcanforero / camphrier / canforo / Kampferbaum|Kampfer / kamferboom|kamfer / kamferträd|kamfer / kamfertræ|kamfer / Камфорное дерево|камфорного дерева",
    "sal": "Meranti / shorea / sal / sal / Salbaum|Salbaum / salboom|salboom / salträd|salträd / shoreatræ|shorea / Шорея|шореи",
    "nutmeg": "Noz-moscadeira / mirística / muscadier / noce moscata / Muskatnussbaum|Muskatnussbaum / nootmuskaatboom|nootmuskaatboom / muskotträd|muskot / muskattræ|muskat / Мускатник|мускатника",
    "baobab": "Baobá / baobab / baobab / baobab / Affenbrotbaum|Affenbrotbaum / baobab|baobab / baobab|baobab / baobab|baobab / Баобаб|баобаба",
    "gum_arabic_tree": "Acácia-de-goma / acacia de Senegal / acacia du Sénégal / acacia del Senegal / Gummiarabikumbaum|Gummiarabikumbaum / gom-arabicumboom|gom-arabicumboom / gummiakacia|gummiakacia / gummiakacie|gummiakacie / Акация сенегальская|акации сенегальской",
    "marula": "Marula / marula / marula / marula / Marulabaum|Marula / marula|marula / marula|marula / marula|marula / Марула|марулы",
    "shea": "Karité / karité / karité / karité / Sheabutterbaum|Sheabutterbaum / sheaboom|shea / sheaträd|shea / sheatræ|shea / Дерево ши|дерева ши",
    "eucalyptus": "Eucalipto / eucalipto / eucalyptus / eucalipto / Eukalyptus|Eukalyptus / eucalyptus|eucalyptus / eukalyptus|eukalyptus / eukalyptus|eukalyptus / Эвкалипт|эвкалипта",
    "pequi": "Pequizeiro / pequi / pequi / pequi / Pequibaum|Pequi / pequi|pequi / pequi|pequi / pequi|pequi / Пеки|пеки",
    "barbatimao": "Barbatimão / barbatimão / barbatimão / barbatimão / Barbatimão|Barbatimão- / barbatimão|barbatimão- / barbatimão|barbatimão- / barbatimão|barbatimão- / Барбатимао|барбатимао",
    "yellow_ipe": "Ipê-amarelo / lapacho amarillo / ipé jaune / ipè giallo / Gelber Ipê|Ipê- / gele ipé|ipé- / gul ipé|ipé- / gul ipé|ipé- / Жёлтый ипе|жёлтого ипе",
    "jatoba": "Jatobá / jatobá / courbaril / jatobá / Jatobá|Jatobá- / jatoba|jatoba / jatoba|jatoba / jatoba|jatoba / Курбарил|курбарила",
    "sandalwood": "Sândalo / sándalo / santal / sandalo / Sandelholzbaum|Sandelholz / sandelhoutboom|sandelhout / sandelträd|sandel / sandeltræ|sandel / Сантал|сантала",
    "dragon_tree": "Dragoeiro / drago de Socotra / dragonnier / dracena / Drachenbaum|Drachenbaum / drakenbloedboom|drakenbloedboom / drakblodsträd|drakblodsträd / dragetræ|dragetræ / Драконово дерево|драконова дерева",
    "frankincense": "Olíbano / boswellia / boswellie / boswellia / Weihrauchbaum|Weihrauchbaum / wierookboom|wierookboom / rökelseträd|rökelseträd / røgelsestræ|røgelsestræ / Босвеллия|босвеллии",
    "myrrh": "Mirra / mirra / commiphora / mirra / Myrrhenbaum|Myrrhenbaum / mirreboom|mirreboom / myrraträd|myrraträd / myrratræ|myrratræ / Коммифора|коммифоры",
    "date_palm": "Tamareira / palmera datilera / palmier dattier / palma da dattero / Dattelpalme|Dattelpalmen / dadelpalm|dadelpalm / dadelpalm|dadelpalm / daddelpalme|daddelpalme / Финиковая пальма|финиковой пальмы",
    "mesquite": "Algarobeira / mezquite / prosopis / mesquite / Mesquitebaum|Mesquite / mesquite|mesquite / mesquite|mesquite / mesquite|mesquite / Прозопис|прозописа",
    "joshua_tree": "Árvore-de-josué / árbol de Josué / arbre de Josué / albero di Giosuè / Josua-Palmlilie|Josua-Palmlilien / joshuaboom|joshuaboom / josuaträd|josuaträd / josvatræ|josvatræ / Юкка коротколистная|юкки коротколистной",
    "saxaul": "Saxaul / saxaúl / saxaoul / saxaul / Saxaul|Saxaul / saxaul|saxaul / saxaul|saxaul / saxaul|saxaul / Саксаул|саксаула",
    "argan": "Argânia / argán / arganier / argan / Arganbaum|Argan / arganboom|argan / arganträd|argan / argantræ|argan / Аргания|аргании",
    "palo_verde": "Palo-verde / palo verde / palo verde / palo verde / Palo Verde|Palo-Verde- / paloverde|paloverde / paloverde|paloverde / paloverde|paloverde / Паркинсония|паркинсонии",
    "pinyon_pine": "Pinheiro-piñon / piñonero de Colorado / pin pignon / pino edule / Colorado-Pinyon-Kiefer|Pinyonkiefern / pinyonden|pinyonden / pinyontall|pinyontall / pinyonfyr|pinyonfyr / Сосна съедобная|сосны съедобной",
    "utah_juniper": "Zimbro-de-utah / enebro de Utah / genévrier de l'Utah / ginepro dello Utah / Utah-Wacholder|Utah-Wacholder / Utah-jeneverbes|Utah-jeneverbes- / utahen|utahen / Utah-ene|Utah-ene- / Можжевельник ютский|можжевельника ютского",
    "desert_ironwood": "Pau-ferro-do-deserto / palo fierro / bois de fer du désert / legno di ferro del deserto / Wüsteneisenholz|Wüsteneisenholz / woestijnijzerhout|woestijnijzerhout / ökenjärnträd|ökenjärnträd / ørkenjerntræ|ørkenjerntræ / Ольнея|ольнеи",
    "tamarisk": "Tamargueira / taray / tamaris / tamerice / Tamariske|Tamarisken / tamarisk|tamarisk / tamarisk|tamarisk / tamarisk|tamarisk / Тамарикс|тамарикса",
    "ponderosa_pine": "Pinheiro-ponderosa / pino ponderosa / pin ponderosa / pino ponderosa / Gelbkiefer|Gelbkiefern / ponderosaden|ponderosaden / gultall|gultall / gulfyr|gulfyr / Сосна жёлтая|сосны жёлтой",
    "gambel_oak": "Carvalho-de-gambel / roble de Gambel / chêne de Gambel / quercia di Gambel / Gambel-Eiche|Gambel-Eichen / Gambeleik|Gambeleiken / gambelek|gambelek / gambeleg|gambeleg / Дуб Гамбела|дуба Гамбела",
    "mountain_mahogany": "Mogno-da-montanha / caoba de montaña / acajou de montagne / mogano di montagna / Bergmahagoni|Bergmahagoni / bergmahonie|bergmahonie / bergsmahogny|bergsmahogny / bjergmahogni|bjergmahogni / Церкокарпус|церкокарпуса",
    "elephant_tree": "Bursera / torote / bursera / bursera / Elefantenbaum|Elefantenbaum / olifantsboom|olifantsboom / elefantträd|elefantträd / elefanttræ|elefanttræ / Бурсера|бурсеры",
    "coconut_palm": "Coqueiro / cocotero / cocotier / palma da cocco / Kokospalme|Kokospalmen / kokospalm|kokospalm / kokospalm|kokospalm / kokospalme|kokospalme / Кокосовая пальма|кокосовой пальмы",
    "tropical_almond": "Amendoeira-da-praia / almendro de la India / badamier / mandorlo indiano / Katappenbaum|Katappenbaum / katapangboom|katapangboom / katappaträd|katappaträd / katappatræ|katappatræ / Терминалия катаппа|терминалии катаппа",
    "casuarina": "Casuarina / casuarina / filao / casuarina / Kasuarine|Kasuarinen / casuarina|casuarina / kasuarina|kasuarina / kasuarina|kasuarina / Казуарина|казуарины",
    "screw_pine": "Pandano / pándano / pandanus / pandano / Schraubenbaum|Schraubenbaum / schroefpalm|schroefpalm / skruvpalm|skruvpalm / skruepalme|skruepalme / Пандан|пандана",
    "sea_grape": "Uva-da-praia / uva de playa / raisinier bord de mer / uva di mare / Seetraube|Seetrauben / zeedruif|zeedruif / havsdruva|havsdruv / havdrue|havdrue / Кокколоба|кокколобы",
    "black_mangrove": "Mangue-preto / mangle negro / palétuvier noir / mangrovia nera / Schwarze Mangrove|Schwarzmangroven / zwarte mangrove|zwartemangrove / svart mangrove|svartmangrove / sort mangrove|sortmangrove / Авиценния|авиценнии",
    "white_mangrove": "Mangue-branco / mangle blanco / palétuvier blanc / mangrovia bianca / Weiße Mangrove|Weißmangroven / witte mangrove|wittemangrove / vit mangrove|vitmangrove / hvid mangrove|hvidmangrove / Лагункулярия|лагункулярии",
    "buttonwood": "Mangue-de-botão / mangle botoncillo / palétuvier gris / mangrovia bottone / Knopfmangrove|Knopfmangroven / knopmangrove|knopmangrove / knappmangrove|knappmangrove / knapmangrove|knapmangrove / Конокарпус|конокарпуса",
    "nipa_palm": "Palmeira-nipa / palma nipa / palmier nipa / palma nipa / Nipapalme|Nipapalmen / nipapalm|nipapalm / nipapalm|nipapalm / nipapalme|nipapalme / Нипа|нипы",
    "buriti": "Buriti / moriche / palmier bâche / moriche / Moriche-Palme|Moriche-Palmen / morichepalm|morichepalm / morichepalm|morichepalm / morichepalme|morichepalme / Маврикиева пальма|маврикиевой пальмы",
}

# Block names: (log, stripped log, planks, leaves, sapling). {n} = name, {s} = compound stem, {g} = genitive.
PATTERNS = {
    "pt_br": ("Tronco de {n}", "Tronco Descascado de {n}", "Tábuas de {n}", "Folhas de {n}", "Muda de {n}"),
    "es_es": ("Tronco de {n}", "Tronco de {n} sin corteza", "Tablones de {n}", "Hojas de {n}", "Brote de {n}"),
    "fr_fr": ("Bûche {de}{n}", "Bûche {de}{n} écorcée", "Planches {de}{n}", "Feuilles {de}{n}", "Pousse {de}{n}"),
    "it_it": ("Tronco di {n}", "Tronco di {n} scortecciato", "Assi di {n}", "Foglie di {n}", "Arbusto di {n}"),
    "de_de": ("{s}stamm", "Entrindeter {s}stamm", "{s}bretter", "{s}laub", "{s}setzling"),
    "nl_nl": ("{s}stam", "Ontschorste {s}stam", "{s}planken", "{s}bladeren", "{s}zaailing"),
    "sv_se": ("{s}stam", "Barkad {s}stam", "{s}plankor", "{s}löv", "{s}planta"),
    "da_dk": ("{s}stamme", "Afbarket {s}stamme", "{s}planker", "{s}løv", "{s}spire"),
    "ru_ru": ("Бревно {g}", "Окорённое бревно {g}", "Доски из {g}", "Листва {g}", "Саженец {g}"),
}


H_ASPIRE = {"hêtre", "houx"}


def _cap(s):
    return s[:1].upper() + s[1:]


def block_names(code, species_id):
    """The five block names of a species in one language, and the tree's own name."""
    part = SPECIES[species_id].split(" / ")[LANGS.index(code)]
    name, _, form = part.partition("|")
    pat = PATTERNS[code]
    if code in ("de_de", "nl_nl", "sv_se", "da_dk"):
        stem = form

        def fill(p):
            head, _, tail = p.partition("{s}")
            suffix = tail
            if stem.endswith("-"):
                suffix = _cap(tail)
            s = (head + stem + suffix) if head else _cap(stem) + suffix
            return s
        return [fill(p) for p in pat], _cap(name)
    if code == "ru_ru":
        return [p.format(g=form) for p in pat], name
    # elide before a vowel or a mute h, not before an aspirated h (hêtre, houx)
    de = "d'" if name[:1].lower() in "aeiouyéèêàâîôûh" and name.lower() not in H_ASPIRE else "de "
    return [_cap(p.format(n=name, de=de)) for p in pat], _cap(name)


# English -> translation, in LANGS order.
TEXT = {
    # Fruit, nuts and bark.
    "Brazil Nut": "Castanha-do-pará / Nuez de Brasil / Noix du Brésil / Noce del Brasile / Paranuss / Paranoot / Paranöt / Paranød / Бразильский орех",
    "Sapodilla": "Sapoti / Chicozapote / Sapotille / Sapodilla / Breiapfel / Sapodilla / Sapotill / Sapodilla / Саподилла",
    "Andiroba Seed": "Semente de Andiroba / Semilla de andiroba / Graine d'andiroba / Seme di andiroba / Andirobasamen / Andirobazaad / Andirobafrö / Andirobafrø / Семя андиробы",
    "Acai Berry": "Açaí / Asaí / Baie d'açaï / Bacca di açaí / Açaí-Beere / Açaíbes / Açaíbär / Açaíbær / Ягода асаи",
    "Babassu Nut": "Coco de Babaçu / Coco de babasú / Noix de babassu / Noce di babassu / Babassunuss / Babassunoot / Babassunöt / Babassunød / Орех бабассу",
    "Oil Palm Fruit": "Dendê / Fruto de palma aceitera / Fruit du palmier à huile / Frutto di palma da olio / Ölpalmfrucht / Oliepalmvrucht / Oljepalmsfrukt / Oliepalmefrugt / Плод масличной пальмы",
    "Nutmeg": "Noz-moscada / Nuez moscada / Noix de muscade / Noce moscata / Muskatnuss / Nootmuskaat / Muskotnöt / Muskatnød / Мускатный орех",
    "Baobab Fruit": "Fruto de Baobá / Fruto de baobab / Pain de singe / Frutto di baobab / Affenbrotfrucht / Baobabvrucht / Baobabfrukt / Baobabfrugt / Плод баобаба",
    "Marula Fruit": "Fruto de Marula / Fruto de marula / Fruit de marula / Frutto di marula / Marulafrucht / Marulavrucht / Marulafrukt / Marulafrugt / Плод марулы",
    "Shea Nut": "Noz de Karité / Nuez de karité / Noix de karité / Noce di karité / Sheanuss / Sheanoot / Sheanöt / Sheanød / Орех ши",
    "Pequi": "Pequi / Pequi / Pequi / Pequi / Pequi / Pequi / Pequi / Pequi / Пеки",
    "Date": "Tâmara / Dátil / Datte / Dattero / Dattel / Dadel / Dadel / Daddel / Финик",
    "Mesquite Pod": "Vagem de Algaroba / Vaina de mezquite / Gousse de prosopis / Baccello di mesquite / Mesquiteschote / Mesquitepeul / Mesquiteskida / Mesquitebælg / Боб прозописа",
    "Argan Nut": "Noz de Argânia / Nuez de argán / Noix d'argan / Noce di argan / Argannuss / Arganoot / Argannöt / Argannød / Орех аргании",
    "Palo Verde Seeds": "Sementes de Palo-verde / Semillas de palo verde / Graines de palo verde / Semi di palo verde / Palo-Verde-Samen / Paloverdezaden / Paloverdefrön / Paloverdefrø / Семена паркинсонии",
    "Pine Nut": "Pinhão / Piñón / Pignon / Pinolo / Pinienkern / Pijnboompit / Pinjenöt / Pinjekerne / Кедровый орех",
    "Juniper Berry": "Baga de Zimbro / Baya de enebro / Baie de genièvre / Bacca di ginepro / Wacholderbeere / Jeneverbes / Enbär / Enebær / Можжевеловая ягода",
    "Acorn": "Bolota / Bellota / Gland / Ghianda / Eichel / Eikel / Ekollon / Agern / Жёлудь",
    "Coconut": "Coco / Coco / Noix de coco / Noce di cocco / Kokosnuss / Kokosnoot / Kokosnöt / Kokosnød / Кокос",
    "Tropical Almond": "Castanhola / Almendra de la India / Amande des Indes / Mandorla indiana / Katappenmandel / Katapangamandel / Katappamandel / Katappamandel / Миндаль катаппа",
    "Pandanus Fruit": "Fruto de Pandano / Fruto de pándano / Fruit de pandanus / Frutto di pandano / Pandanusfrucht / Pandanusvrucht / Pandanusfrukt / Pandanusfrugt / Плод пандана",
    "Sea Grapes": "Uvas-da-praia / Uvas de playa / Raisins de mer / Uva di mare / Seetrauben / Zeedruiven / Havsdruvor / Havdruer / Морской виноград",
    "Nipa Fruit": "Fruto de Nipa / Fruto de nipa / Fruit de nipa / Frutto di nipa / Nipafrucht / Nipavrucht / Nipafrukt / Nipafrugt / Плод нипы",
    "Buriti Fruit": "Buriti / Moriche / Fruit de palmier bâche / Frutto di moriche / Morichefrucht / Morichevrucht / Morichefrukt / Morichefrugt / Плод маврикиевой пальмы",
    "Cinchona Bark": "Casca de Quina / Corteza de quina / Écorce de quinquina / Corteccia di china / Chinarinde / Kinabast / Kinabark / Kinabark / Хинная кора",
    "Cinnamon": "Canela / Canela / Cannelle / Cannella / Zimt / Kaneel / Kanel / Kanel / Корица",
    # Substances.
    "Pine Resin": "Resina de Pinheiro / Resina de pino / Résine de pin / Resina di pino / Kiefernharz / Dennenhars / Tallkåda / Fyrreharpiks / Сосновая смола",
    "Turpentine": "Terebintina / Trementina / Térébenthine / Trementina / Terpentinöl / Terpentijn / Terpentin / Terpentin / Скипидар",
    "Rosin": "Breu / Colofonia / Colophane / Colofonia / Kolophonium / Colofonium / Kolofonium / Kolofonium / Канифоль",
    "Copaiba Balsam": "Óleo de Copaíba / Bálsamo de copaiba / Baume de copahu / Balsamo di copaiba / Kopaivabalsam / Copaivabalsem / Kopaivabalsam / Kopaivabalsam / Копайский бальзам",
    "Palm Sap": "Seiva de Palmeira / Savia de palma / Sève de palmier / Linfa di palma / Palmsaft / Palmsap / Palmsav / Palmesaft / Пальмовый сок",
    "Chicle": "Chicle / Chicle / Chiclé / Chicle / Chicle / Chicle / Chicle / Chicle / Чикл",
    "Dammar": "Resina Damar / Resina dammar / Résine dammar / Resina dammar / Dammarharz / Dammarhars / Dammarharts / Dammarharpiks / Даммара",
    "Copal": "Copal / Copal / Copal / Coppale / Kopal / Kopal / Kopal / Kopal / Копал",
    "Gum Arabic": "Goma-arábica / Goma arábiga / Gomme arabique / Gomma arabica / Gummi arabicum / Arabische gom / Gummi arabicum / Gummi arabicum / Гуммиарабик",
    "Frankincense": "Olíbano / Incienso / Encens / Incenso / Weihrauch / Wierook / Rökelse / Røgelse / Ладан",
    "Myrrh": "Mirra / Mirra / Myrrhe / Mirra / Myrrhe / Mirre / Myrra / Myrra / Мирра",
    "Dragon's Blood": "Sangue-de-dragão / Sangre de drago / Sang-dragon / Sangue di drago / Drachenblut / Drakenbloed / Drakblod / Drageblod / Драконова кровь",
    "Quinine": "Quinina / Quinina / Quinine / Chinino / Chinin / Kinine / Kinin / Kinin / Хинин",
    "Cinnamaldehyde": "Cinamaldeído / Cinamaldehído / Cinnamaldéhyde / Cinnamaldeide / Zimtaldehyd / Kaneelaldehyde / Kanelaldehyd / Kanelaldehyd / Коричный альдегид",
    "Camphor": "Cânfora / Alcanfor / Camphre / Canfora / Kampfer / Kamfer / Kamfer / Kamfer / Камфора",
    "Eucalyptol": "Eucaliptol / Eucaliptol / Eucalyptol / Eucaliptolo / Eukalyptol / Eucalyptol / Eukalyptol / Eukalyptol / Эвкалиптол",
    "Santalol": "Santalol / Santalol / Santalol / Santalolo / Santalol / Santalol / Santalol / Santalol / Санталол",
    "Brazilin": "Brasilina / Brasilina / Brésiline / Brasilina / Brasilin / Braziline / Brasilin / Brasilin / Бразилин",
    "Tapped from pines: distils to turpentine and rosin": "Tirada dos pinheiros: destila em terebintina e breu / Sangrada de los pinos: da trementina y colofonia / Récoltée sur les pins : donne térébenthine et colophane / Spillata dai pini: dà trementina e colofonia / Aus Kiefern gezapft: gibt Terpentinöl und Kolophonium / Uit dennen getapt: geeft terpentijn en colofonium / Tappad ur tallar: ger terpentin och kolofonium / Tappet fra fyr: giver terpentin og kolofonium / Подсочка сосен: даёт скипидар и канифоль",
    "Bow rosin, soldering flux and paper size": "Breu de arco, fluxo de solda e cola de papel / Resina de arco, fundente y apresto de papel / Colophane d'archet, flux de soudure, encollage / Pece per archetti, flussante e collatura / Geigenharz, Lötflussmittel und Papierleim / Strijkhars, soldeermiddel en papierlijm / Stråkharts, lödflussmedel och papperslim / Buekolofonium, loddemiddel og papirlim / Канифоль для смычков, флюс, проклейка бумаги",
    "Oil-resin of copaiba: varnish and folk medicine": "Óleo-resina de copaíba: verniz e remédio popular / Oleorresina de copaiba: barniz y medicina popular / Oléorésine de copaïer : vernis et remède / Oleoresina di copaiba: vernice e rimedio popolare / Ölharz der Kopaiva: Firnis und Volksheilmittel / Oliehars van copaiba: vernis en volksmiddel / Oljeharts av kopaiva: fernissa och folkmedicin / Olieharpiks af kopaiva: fernis og folkemedicin / Маслосмола копаиферы: лак и народное лекарство",
    "Nipa palm sap: boil for sugar or ferment into wine": "Seiva da nipa: ferva para açúcar ou fermente em vinho / Savia de nipa: hiérvela para azúcar o fermenta vino / Sève de nipa : bouillie en sucre ou fermentée en vin / Linfa di nipa: bollita dà zucchero, fermentata vino / Nipasaft: zu Zucker kochen oder zu Wein vergären / Nipasap: kook tot suiker of laat gisten tot wijn / Nipasav: koka till socker eller jäs till vin / Nipasaft: kog til sukker eller gær til vin / Сок нипы: уварите в сахар или сбродите в вино",
    "Bitter cinchona alkaloid: the first malaria cure": "Alcaloide amargo da quina: o primeiro remédio da malária / Alcaloide amargo de la quina: el primer antipalúdico / Alcaloïde amer du quinquina : premier antipaludéen / Alcaloide amaro della china: il primo antimalarico / Bitteres Chinarindenalkaloid: erstes Malariamittel / Bitter kina-alkaloïde: het eerste malariamiddel / Bittert kinaalkaloid: det första malariamedlet / Bittert kinaalkaloid: det første malariamiddel / Горький алкалоид хины: первое средство от малярии",
    "From camphor wood: mothballs, and celluloid": "Da canforeira: naftalina e celuloide / Del alcanforero: antipolillas y celuloide / Du camphrier : antimites et celluloïd / Dal canforo: antitarme e celluloide / Aus Kampferholz: Mottenschutz und Zelluloid / Uit kamferhout: mottenballen en celluloid / Ur kamferträ: malkulor och celluloid / Fra kamfertræ: mølkugler og celluloid / Из камфорного дерева: от моли и для целлулоида",
    "Oil of eucalyptus: cough drops and antiseptics": "Óleo de eucalipto: pastilhas para tosse e antissépticos / Aceite de eucalipto: pastillas para la tos y antisépticos / Huile d'eucalyptus : pastilles et antiseptiques / Olio di eucalipto: pastiglie per la tosse e antisettici / Eukalyptusöl: Hustenbonbons und Antiseptika / Eucalyptusolie: hoestsnoepjes en ontsmetting / Eukalyptusolja: halstabletter och antiseptika / Eukalyptusolie: hostebolsjer og antiseptika / Эвкалиптовое масло: леденцы от кашля, антисептики",
    "Red dye of brazilwood, the tree that named Brazil": "Corante vermelho do pau-brasil, que deu nome ao Brasil / Tinte rojo del palo brasil, que dio nombre a Brasil / Teinture rouge du pernambouc, qui nomma le Brésil / Tintura rossa del pernambuco, che diede nome al Brasile / Roter Farbstoff des Brasilholzes, Namensgeber Brasiliens / Rode kleurstof van brazielhout, naamgever van Brazilië / Rött färgämne ur brasilträ, som gav Brasilien namn / Rødt farvestof fra brasiltræ, der gav Brasilien navn / Красный краситель фернамбука, давшего имя Бразилии",
    "Spirit of turpentine: thins paint and varnish": "Aguarrás: dilui tinta e verniz / Aguarrás: diluye pinturas y barnices / Essence de térébenthine : dilue peintures et vernis / Acquaragia: diluisce vernici e pitture / Terpentinöl verdünnt Farbe und Firnis / Terpentijn verdunt verf en vernis / Terpentin tunnar ut färg och fernissa / Terpentin fortynder maling og fernis / Скипидар разбавляет краски и лаки",
    "Sapodilla gum: the first chewing gum": "Goma do sapotizeiro: o primeiro chiclete / Goma del chicozapote: el primer chicle de mascar / Gomme du sapotillier : le premier chewing-gum / Gomma della sapodilla: la prima gomma da masticare / Gummi des Breiapfelbaums: der erste Kaugummi / Gom van de sapodilla: de eerste kauwgom / Gummi från sapotillträdet: det första tuggummit / Gummi fra sapodillaen: det første tyggegummi / Камедь саподиллы: первая жевательная резинка",
    "Clear resin of the sal tree: varnish and torches": "Resina clara do meranti: verniz e tochas / Resina clara de la shorea: barniz y antorchas / Résine claire du sal : vernis et torches / Resina chiara del sal: vernici e torce / Klares Harz des Salbaums: Firnis und Fackeln / Heldere hars van de salboom: vernis en fakkels / Klar kåda från salträdet: fernissa och facklor / Klar harpiks fra shoreatræet: fernis og fakler / Прозрачная смола шореи: лак и факелы",
    "Hard tree resin: incense and varnish": "Resina dura de árvore: incenso e verniz / Resina dura de árbol: incienso y barniz / Résine dure : encens et vernis / Resina dura d'albero: incenso e vernice / Hartes Baumharz: Räucherwerk und Firnis / Harde boomhars: wierook en vernis / Hård trädkåda: rökelse och fernissa / Hård træharpiks: røgelse og fernis / Твёрдая смола деревьев: благовоние и лак",
    "Acacia gum: binds ink and paint, thickens food": "Goma de acácia: liga tinta e pigmento, engrossa alimentos / Goma de acacia: aglutina tinta y pintura, espesa alimentos / Gomme d'acacia : lie encres et peintures, épaissit les aliments / Gomma d'acacia: lega inchiostri e colori, addensa i cibi / Akaziengummi bindet Tinte und Farbe, dickt Speisen an / Acaciagom: bindt inkt en verf, dikt voedsel in / Akaciagummi binder bläck och färg, tjockar mat / Akaciegummi binder blæk og maling, jævner mad / Камедь акации: связывает чернила и краски, загущает пищу",
    "Fragrant resin burned as incense since antiquity": "Resina perfumada queimada como incenso desde a Antiguidade / Resina aromática quemada como incienso desde la Antigüedad / Résine parfumée brûlée comme encens depuis l'Antiquité / Resina profumata bruciata come incenso fin dall'antichità / Duftendes Harz, seit der Antike als Räucherwerk verbrannt / Geurige hars, al sinds de oudheid als wierook gebrand / Doftande kåda som bränts som rökelse sedan antiken / Duftende harpiks brændt som røgelse siden oldtiden / Душистая смола, которую жгут как благовоние с древности",
    "Bitter scented gum resin: incense, perfume, embalming": "Goma-resina amarga e perfumada: incenso, perfume, embalsamamento / Gomorresina amarga y aromática: incienso, perfume, embalsamamiento / Gomme-résine amère et parfumée : encens, parfum, embaumement / Gommoresina amara e profumata: incenso, profumo, imbalsamazione / Bitteres duftendes Gummiharz: Räucherwerk, Parfüm, Einbalsamierung / Bittere geurige gomhars: wierook, parfum, balsemen / Bitter doftande gummiharts: rökelse, parfym, balsamering / Bitter duftende gummiharpiks: røgelse, parfume, balsamering / Горькая душистая камедесмола: благовоние, духи, бальзамирование",
    "Red resin of the dragon tree: pigment and varnish": "Resina vermelha do dragoeiro: pigmento e verniz / Resina roja del drago: pigmento y barniz / Résine rouge du dragonnier : pigment et vernis / Resina rossa della dracena: pigmento e vernice / Rotes Harz des Drachenbaums: Pigment und Firnis / Rode hars van de drakenbloedboom: pigment en vernis / Röd kåda från drakblodsträdet: pigment och fernissa / Rød harpiks fra dragetræet: pigment og fernis / Красная смола драконова дерева: пигмент и лак",
    "Oil of cinnamon: the taste of the spice": "Óleo de canela: o sabor da especiaria / Aceite de canela: el sabor de la especia / Huile de cannelle : le goût de l'épice / Olio di cannella: il sapore della spezia / Zimtöl: der Geschmack des Gewürzes / Kaneelolie: de smaak van de specerij / Kanelolja: kryddans smak / Kanelolie: krydderiets smag / Коричное масло: вкус пряности",
    "Oil of sandalwood: one of the oldest perfumes": "Óleo de sândalo: um dos perfumes mais antigos / Aceite de sándalo: uno de los perfumes más antiguos / Huile de santal : l'un des plus anciens parfums / Olio di sandalo: uno dei profumi più antichi / Sandelholzöl: einer der ältesten Duftstoffe / Sandelhoutolie: een van de oudste parfums / Sandelträolja: en av de äldsta parfymerna / Sandeltræolie: en af de ældste parfumer / Сандаловое масло: один из древнейших ароматов",
    # Reactions.
    "Pine resin distilled with steam: turpentine comes over, rosin stays in the pot": "Resina de pinheiro destilada com vapor: a terebintina passa, o breu fica no pote / Resina de pino destilada con vapor: pasa la trementina, la colofonia queda en la olla / Résine de pin distillée à la vapeur : la térébenthine passe, la colophane reste dans le pot / Resina di pino distillata in corrente di vapore: passa la trementina, la colofonia resta nel recipiente / Kiefernharz mit Dampf destilliert: Terpentinöl geht über, Kolophonium bleibt im Topf / Dennenhars met stoom gedestilleerd: terpentijn gaat over, colofonium blijft in de pot / Tallkåda destillerad med ånga: terpentinen går över, kolofoniet blir kvar i grytan / Fyrreharpiks destilleret med damp: terpentinen går over, kolofoniet bliver i gryden / Живицу перегоняют с паром: скипидар отгоняется, канифоль остаётся в кубе",
    "Cinchona bark steeped in dilute acid gives up its quinine (Pelletier and Caventou 1820)": "Casca de quina em ácido diluído solta a quinina (Pelletier e Caventou 1820) / La corteza de quina en ácido diluido suelta su quinina (Pelletier y Caventou 1820) / L'écorce de quinquina dans un acide dilué cède sa quinine (Pelletier et Caventou 1820) / La corteccia di china in acido diluito cede il chinino (Pelletier e Caventou 1820) / Chinarinde in verdünnter Säure gibt ihr Chinin ab (Pelletier und Caventou 1820) / Kinabast in verdund zuur geeft zijn kinine af (Pelletier en Caventou 1820) / Kinabark i utspädd syra släpper sitt kinin (Pelletier och Caventou 1820) / Kinabark i fortyndet syre afgiver sit kinin (Pelletier og Caventou 1820) / Хинная кора в разбавленной кислоте отдаёт хинин (Пеллетье и Кавенту, 1820)",
    "Cinnamon bark distilled with steam: the oil of cinnamon comes over": "Casca de canela destilada com vapor: passa o óleo de canela / Corteza de canela destilada con vapor: pasa el aceite de canela / Écorce de cannelle distillée à la vapeur : l'huile de cannelle passe / Corteccia di cannella distillata in corrente di vapore: passa l'olio di cannella / Zimtrinde mit Dampf destilliert: das Zimtöl geht über / Kaneelbast met stoom gedestilleerd: de kaneelolie gaat over / Kanelbark destillerad med ånga: kaneloljan går över / Kanelbark destilleret med damp: kanelolien går over / Коричную кору перегоняют с паром: отгоняется коричное масло",
    "Camphor wood distilled with steam: camphor crystallizes from the vapour": "Madeira de canforeira destilada com vapor: a cânfora cristaliza do vapor / Madera de alcanforero destilada con vapor: el alcanfor cristaliza del vapor / Bois de camphrier distillé à la vapeur : le camphre cristallise à partir des vapeurs / Legno di canforo distillato in corrente di vapore: la canfora cristallizza dal vapore / Kampferholz mit Dampf destilliert: Kampfer kristallisiert aus dem Dampf / Kamferhout met stoom gedestilleerd: kamfer kristalliseert uit de damp / Kamferträ destillerat med ånga: kamfer kristalliseras ur ångan / Kamfertræ destilleret med damp: kamfer krystalliserer ud af dampen / Камфорную древесину перегоняют с паром: камфора кристаллизуется из пара",
    "Eucalyptus leaves distilled with steam: the oil comes over": "Folhas de eucalipto destiladas com vapor: o óleo passa / Hojas de eucalipto destiladas con vapor: pasa el aceite / Feuilles d'eucalyptus distillées à la vapeur : l'huile passe / Foglie di eucalipto distillate in corrente di vapore: passa l'olio / Eukalyptusblätter mit Dampf destilliert: das Öl geht über / Eucalyptusbladeren met stoom gedestilleerd: de olie gaat over / Eukalyptusblad destillerade med ånga: oljan går över / Eukalyptusblade destilleret med damp: olien går over / Листья эвкалипта перегоняют с паром: отгоняется масло",
    "Sandalwood distilled with steam: sandalwood oil comes over": "Sândalo destilado com vapor: passa o óleo de sândalo / Sándalo destilado con vapor: pasa el aceite de sándalo / Santal distillé à la vapeur : l'huile de santal passe / Sandalo distillato in corrente di vapore: passa l'olio di sandalo / Sandelholz mit Dampf destilliert: Sandelholzöl geht über / Sandelhout met stoom gedestilleerd: sandelhoutolie gaat over / Sandelträ destillerat med ånga: sandelträolja går över / Sandeltræ destilleret med damp: sandeltræolie går over / Сандал перегоняют с паром: отгоняется сандаловое масло",
    "Brazilwood chips boiled and laked with alum: 32 red dyes to a flask's worth": "Lascas de pau-brasil fervidas e fixadas com alúmen: 32 corantes vermelhos por frasco / Astillas de palo brasil hervidas y fijadas con alumbre: 32 tintes rojos por frasco / Copeaux de pernambouc bouillis et laqués à l'alun : 32 teintures rouges par flacon / Trucioli di pernambuco bolliti e laccati con allume: 32 coloranti rossi per fiaschetta / Brasilholzspäne gekocht und mit Alaun verlackt: 32 rote Farbstoffe je Flasche / Brazielhoutsnippers gekookt en met aluin gelakt: 32 rode kleurstoffen per fles / Brasilträflis kokad och lackad med alun: 32 röda färgämnen per flaska / Brasiltræspåner kogt og lakeret med alun: 32 røde farvestoffer pr. flaske / Стружку фернамбука вываривают и осаждают квасцами: 32 красных красителя на колбу",
    "Resin distillation": "Destilação da resina / Destilación de la resina / Distillation de la résine / Distillazione della resina / Harzdestillation / Harsdestillatie / Kådadestillation / Harpiksdestillation / Перегонка живицы",
    "Quinine extraction": "Extração de quinina / Extracción de quinina / Extraction de la quinine / Estrazione del chinino / Chininextraktion / Kinine-extractie / Kininextraktion / Kininekstraktion / Извлечение хинина",
    "Cinnamon oil": "Óleo de canela / Aceite de canela / Huile de cannelle / Olio di cannella / Zimtöl / Kaneelolie / Kanelolja / Kanelolie / Коричное масло",
    "Camphor distillation": "Destilação da cânfora / Destilación del alcanfor / Distillation du camphre / Distillazione della canfora / Kampferdestillation / Kamferdestillatie / Kamferdestillation / Kamferdestillation / Перегонка камфоры",
    "Eucalyptol distillation": "Destilação do eucaliptol / Destilación del eucaliptol / Distillation de l'eucalyptol / Distillazione dell'eucaliptolo / Eukalyptoldestillation / Eucalyptoldestillatie / Eukalyptoldestillation / Eukalyptoldestillation / Перегонка эвкалиптола",
    "Santalol distillation": "Destilação do santalol / Destilación del santalol / Distillation du santalol / Distillazione del santalolo / Santaloldestillation / Santaloldestillatie / Santaloldestillation / Santaloldestillation / Перегонка санталола",
    "Brazilin dyeing": "Tingimento com brasilina / Teñido con brasilina / Teinture à la brésiline / Tintura con brasilina / Brasilinfärben / Verven met braziline / Brasilinfärgning / Brasilinfarvning / Крашение бразилином",
    # Interface.
    "Grows only in: %s": "Só cresce em: %s / Solo crece en: %s / Ne pousse que dans : %s / Cresce solo in: %s / Wächst nur in: %s / Groeit alleen in: %s / Växer bara i: %s / Vokser kun i: %s / Растёт только в биомах: %s",
    "This sapling grows only in: %s": "Esta muda só cresce em: %s / Este brote solo crece en: %s / Cette pousse ne pousse que dans : %s / Questo arbusto cresce solo in: %s / Dieser Setzling wächst nur in: %s / Deze zaailing groeit alleen in: %s / Den här plantan växer bara i: %s / Denne spire vokser kun i: %s / Этот саженец растёт только в биомах: %s",
    "Tree Tap: %s of %s ready (%s)": "Bica de Sangria: %s de %s prontos (%s) / Sangrador: %s de %s listos (%s) / Saignée : %s sur %s prêts (%s) / Spillatore: %s di %s pronti (%s) / Zapfhahn: %s von %s bereit (%s) / Tapkraan: %s van %s klaar (%s) / Tapprör: %s av %s klara (%s) / Tappehane: %s af %s klar (%s) / Подсочный кран: готово %s из %s (%s)",
    "Nothing flows: this tree gives it only where it grows wild": "Nada escorre: esta árvore só dá isso onde cresce nativa / No sale nada: este árbol solo lo da donde crece silvestre / Rien ne coule : cet arbre ne le donne que là où il pousse à l'état sauvage / Non esce nulla: quest'albero lo dà solo dove cresce spontaneo / Es fließt nichts: dieser Baum gibt es nur, wo er wild wächst / Er stroomt niets: deze boom geeft het alleen waar hij in het wild groeit / Inget rinner: trädet ger det bara där det växer vilt / Intet løber: træet giver det kun, hvor det vokser vildt / Ничего не течёт: дерево даёт это только там, где растёт в дикой природе",
    "A Tree Tap has to be fixed to the side of a rubber log or a tree that gives sap or resin": "A bica de sangria tem de ser fixada na lateral de um tronco de seringueira ou de uma árvore que dê seiva ou resina / El sangrador debe fijarse al costado de un tronco de caucho o de un árbol que dé savia o resina / La saignée doit être fixée sur le côté d'une bûche d'hévéa ou d'un arbre qui donne sève ou résine / Lo spillatore va fissato al fianco di un tronco di albero della gomma o di un albero che dia linfa o resina / Der Zapfhahn muss seitlich an einem Kautschukbaumstamm oder einem Baum mit Saft oder Harz sitzen / De tapkraan moet aan de zijkant van een rubberboomstam of een boom met sap of hars zitten / Tappröret måste sitta på sidan av en gummiträdsstam eller ett träd som ger sav eller kåda / Tappehanen skal sidde på siden af en gummitræsstamme eller et træ, der giver saft eller harpiks / Кран нужно закрепить сбоку на бревне гевеи или дерева, дающего сок или смолу",
    "Fix it to a rubber log in the jungle, or to a tree that gives sap or resin: it collects some every five minutes": "Fixe-a num tronco de seringueira na selva, ou numa árvore que dê seiva ou resina: ela coleta um pouco a cada cinco minutos / Fíjalo a un tronco de caucho en la jungla, o a un árbol que dé savia o resina: recoge algo cada cinco minutos / Fixez-le à une bûche d'hévéa dans la jungle, ou à un arbre qui donne sève ou résine : il en récolte toutes les cinq minutes / Fissalo a un tronco di albero della gomma nella giungla, o a un albero che dia linfa o resina: ne raccoglie un po' ogni cinque minuti / An einem Kautschukbaumstamm im Dschungel oder an einem Baum mit Saft oder Harz befestigen: sammelt alle fünf Minuten etwas / Bevestig hem aan een rubberboomstam in de jungle, of aan een boom met sap of hars: hij vangt elke vijf minuten wat op / Fäst den på en gummiträdsstam i djungeln, eller på ett träd som ger sav eller kåda: den samlar lite var femte minut / Fastgør den til en gummitræsstamme i junglen eller et træ, der giver saft eller harpiks: den samler lidt hvert femte minut / Закрепите на бревне гевеи в джунглях или на дереве, дающем сок или смолу: каждые пять минут собирает немного",
    "Grows only in the jungle, like the wild rubber trees": "Só cresce na selva, como as seringueiras nativas / Solo crece en la jungla, como los cauchos silvestres / Ne pousse que dans la jungle, comme les hévéas sauvages / Cresce solo nella giungla, come gli alberi della gomma selvatici / Wächst nur im Dschungel, wie die wilden Kautschukbäume / Groeit alleen in de jungle, net als de wilde rubberbomen / Växer bara i djungeln, som de vilda gummiträden / Vokser kun i junglen, som de vilde gummitræer / Растёт только в джунглях, как дикие гевеи",
    "Trees of the World": "Árvores do Mundo / Árboles del mundo / Arbres du monde / Alberi del mondo / Bäume der Welt / Bomen van de wereld / Världens träd / Verdens træer / Деревья мира",
}

# Biome names for the Field Guide (the game names them itself in tooltips).
BIOMES = {
    "jungle": "Jungle / Selva / Jungla / Jungle / Giungla / Dschungel / Oerwoud / Djungel / Jungle / Джунгли",
    "bamboo_jungle": "Bamboo Jungle / Selva de bambu / Jungla de bambú / Jungle de bambous / Giungla di bambù / Bambusdschungel / Bamboe-oerwoud / Bambudjungel / Bambusjungle / Бамбуковые заросли",
    "mangrove_swamp": "Mangrove Swamp / Manguezal / Manglar / Marais à mangroves / Palude di mangrovie / Mangrovensumpf / Mangrovemoeras / Mangroveträsk / Mangrovesump / Мангровое болото",
    "old_growth_pine_taiga": "Old Growth Pine Taiga / Taiga de pinheiros antigos / Taiga de pinos ancestral / Taïga ancienne de pins / Taiga di pini secolari / Kiefern-Urtaiga / Oernaaldbomentaiga / Urskog med tallar / Gammel fyrretajga / Реликтовая сосновая тайга",
    "sparse_jungle": "Sparse Jungle / Selva esparsa / Jungla dispersa / Jungle clairsemée / Giungla rada / Lichter Dschungel / Dunbegroeid oerwoud / Gles djungel / Tynd jungle / Редкие джунгли",
    "savanna": "Savanna / Savana / Sabana / Savane / Savana / Savanne / Savanne / Savann / Savanne / Саванна",
    "savanna_plateau": "Savanna Plateau / Planalto de savana / Meseta de sabana / Plateau de savane / Altopiano della savana / Savannenhochebene / Savanneplateau / Savannplatå / Savanneplateau / Саванна (плато)",
    "windswept_savanna": "Windswept Savanna / Savana das ventanias / Sabana ventiscosa / Savane venteuse / Savana ventosa / Zerzauste Savanne / Onstuimige savanne / Vindpinad savann / Vindblæst savanne / Выветренная саванна",
    "wooded_badlands": "Wooded Badlands / Terras áridas florestadas / Tierras baldías frondosas / Badlands boisées / Calanchi boscosi / Bewaldete Tafelberge / Beboste badlands / Trädbevuxen stenöken / Skovklædt stenørken / Лесистые бесплодные земли",
    "beach": "Beach / Praia / Playa / Plage / Spiaggia / Strand / Strand / Strand / Strand / Пляж",
    "desert": "Desert / Deserto / Desierto / Désert / Deserto / Wüste / Woestijn / Öken / Ørken / Пустыня",
    "badlands": "Badlands / Terras áridas / Tierras baldías / Badlands / Calanchi / Tafelberge / Badlands / Stenöken / Stenørken / Бесплодные земли",
    "eroded_badlands": "Eroded Badlands / Terras áridas erodidas / Tierras baldías erosionadas / Badlands érodées / Calanchi erosi / Abgetragene Tafelberge / Geërodeerde badlands / Eroderad stenöken / Eroderet stenørken / Выветренные бесплодные земли",
    "plains": "Plains / Planície / Llanura / Plaines / Pianura / Ebene / Velden / Slätter / Eng / Равнины",
    "sunflower_plains": "Sunflower Plains / Planície de girassóis / Llanura de girasoles / Plaines de tournesols / Pianura di girasoli / Sonnenblumenebene / Zonnebloemvelden / Solrosslätter / Solsikkeeng / Подсолнуховые поля",
    "forest": "Forest / Floresta / Bosque / Forêt / Foresta / Wald / Bos / Skog / Skov / Лес",
    "flower_forest": "Flower Forest / Floresta de flores / Bosque floral / Forêt fleurie / Foresta floreale / Blumenwald / Bloemenbos / Blomskog / Blomsterskov / Цветочный лес",
    "birch_forest": "Birch Forest / Floresta de bétulas / Abedular / Forêt de bouleaux / Foresta di betulle / Birkenwald / Berkenbos / Björkskog / Birkeskov / Березняк",
    "old_growth_birch_forest": "Old Growth Birch Forest / Floresta de bétulas antigas / Abedular ancestral / Forêt ancienne de bouleaux / Foresta di betulle secolari / Birken-Urwald / Oerberkenbos / Urskog med björkar / Gammel birkeskov / Реликтовый березняк",
    "dark_forest": "Dark Forest / Floresta escura / Bosque oscuro / Forêt sombre / Selva oscura / Dunkler Wald / Dicht bos / Mörk skog / Mørkskov / Тёмный лес",
    "swamp": "Swamp / Pântano / Pantano / Marais / Palude / Sumpf / Moeras / Träsk / Sump / Болото",
    "cherry_grove": "Cherry Grove / Cerejal / Cerezal / Bosquet de cerisiers / Boschetto di ciliegi / Kirschberghain / Kersenboomgaard / Körsbärslund / Kirsebærlund / Вишнёвая роща",
    "windswept_hills": "Windswept Hills / Colinas das ventanias / Colinas ventiscosas / Collines venteuses / Colline ventose / Zerzauste Hügel / Onstuimige heuvels / Vindpinade kullar / Vindblæste bakker / Выветренные холмы",
    "stony_shore": "Stony Shore / Costa rochosa / Costa rocosa / Côte rocheuse / Costa rocciosa / Steinige Küste / Steenkust / Stenig kust / Stenkyst / Каменистый берег",
    "taiga": "Taiga / Taiga / Taiga / Taïga / Taiga / Taiga / Taiga / Tajga / Tajga / Тайга",
    "windswept_forest": "Windswept Forest / Floresta das ventanias / Bosque ventiscoso / Forêt venteuse / Foresta ventosa / Zerzauster Wald / Onstuimig bos / Vindpinad skog / Vindblæst skov / Выветренный лес",
    "snowy_taiga": "Snowy Taiga / Taiga nevada / Taiga nevada / Taïga enneigée / Taiga innevata / Verschneite Taiga / Besneeuwde taiga / Snöig tajga / Snetajga / Заснеженная тайга",
    "snowy_plains": "Snowy Plains / Planície nevada / Llanura nevada / Plaines enneigées / Pianura innevata / Verschneite Ebene / Besneeuwde velden / Snöiga slätter / Snesletter / Заснеженные равнины",
    "snowy_beach": "Snowy Beach / Praia nevada / Playa nevada / Plage enneigée / Spiaggia innevata / Verschneiter Strand / Besneeuwd strand / Snöig strand / Snestrand / Заснеженный пляж",
    "meadow": "Meadow / Pradaria / Prado / Prairie / Prateria / Alm / Weide / Äng / Eng / Луг",
    "grove": "Grove / Bosque / Abetal / Bosquet / Boschetto / Berghain / Bosje / Skogsdunge / Lund / Роща",
    "stony_peaks": "Stony Peaks / Picos rochosos / Cumbres rocosas / Pics rocheux / Vette rocciose / Steinige Gipfel / Steenpieken / Steniga bergstoppar / Klippetoppe / Каменные вершины",
    "windswept_gravelly_hills": "Windswept Gravelly Hills / Colinas de cascalho das ventanias / Colinas pedregosas ventiscosas / Collines graveleuses venteuses / Colline di ghiaia ventose / Zerzauste Geröllhügel / Onstuimige grindheuvels / Vindpinade grusiga kullar / Vindblæste grusbakker / Выветренные гравийные холмы",
    "old_growth_spruce_taiga": "Old Growth Spruce Taiga / Taiga de abetos antigos / Taiga de abetos ancestral / Taïga ancienne de sapins / Taiga di abeti secolari / Fichten-Urtaiga / Oersparrentaiga / Urskog med granar / Gammel grantajga / Реликтовая еловая тайга",
    "ice_spikes": "Ice Spikes / Picos de gelo / Picos de hielo / Stalagmites de glace / Spuntoni di ghiaccio / Eiszapfentundra / IJspijlers / Isspikar / Ispigge / Ледяные пики",
    "snowy_slopes": "Snowy Slopes / Encostas nevadas / Ladera nevada / Pentes enneigées / Pendii innevati / Verschneite Hänge / Besneeuwde pistes / Snöiga sluttningar / Snebakker / Заснеженные склоны",
    "frozen_peaks": "Frozen Peaks / Picos congelados / Cumbres heladas / Pics gelés / Vette ghiacciate / Vereiste Gipfel / Bevroren pieken / Isiga bergstoppar / Frosne bjergtoppe / Замёрзшие вершины",
    "jagged_peaks": "Jagged Peaks / Picos pontiagudos / Cumbres escarpadas / Pics dentelés / Vette frastagliate / Zerklüftete Gipfel / Scherpe pieken / Spetsiga bergstoppar / Takkede bjergtoppe / Зубчатые вершины",
}

# Field Guide chapter, the hand-written paragraphs (English in trees.GUIDE): same order, same headings.
GUIDE = {
    "pt_br": [
        "Cada bioma terrestre tem cinco árvores próprias, espécies reais que crescem nativas ali. A muda só cresce nos biomas de onde vem a espécie: em outro lugar ela fica parada e a farinha de osso não adianta. A dica da muda diz quais são esses biomas.",
        "Frutos e castanhas caem das folhas de vez em quando. Descasque o tronco com um machado para tirar a casca quando a casca é o que vale. Fixe uma Bica de Sangria numa árvore que dê seiva ou resina: os líquidos pedem um frasco vazio, as resinas duras caem direto na bica.",
        "Nas máquinas: a resina de pinheiro no Alambique dá terebintina e breu; casca de canela, madeira de canforeira, folhas de eucalipto e sândalo dão seus óleos no Alambique; a casca de quina no Tanque de Digestão dá quinina; o pau-brasil no Tanque de Tingimento dá corante vermelho. Frutos oleosos vão para a Prensa de Óleo e os doces para o Barril de Fermentação.",
    ],
    "es_es": [
        "Cada bioma terrestre tiene cinco árboles propios, especies reales que crecen silvestres allí. El brote solo crece en los biomas de donde viene la especie: en cualquier otro sitio se queda quieto y el polvo de hueso no sirve. Su descripción dice cuáles son esos biomas.",
        "Frutos y nueces caen de las hojas de vez en cuando. Descorteza un tronco con un hacha para quitarle la corteza cuando la corteza es lo útil. Fija un Sangrador a un árbol que dé savia o resina: los líquidos piden un frasco vacío, las resinas duras caen directamente en el sangrador.",
        "En las máquinas: la resina de pino en el Alambique da trementina y colofonia; la corteza de canela, la madera de alcanforero, las hojas de eucalipto y el sándalo dan sus aceites en el Alambique; la corteza de quina en la Cuba de digestión da quinina; el palo brasil en la Cuba de tinte da tinte rojo. Los frutos oleosos van a la Prensa de aceite y los dulces al Barril de fermentación.",
    ],
    "fr_fr": [
        "Chaque biome terrestre a cinq arbres à lui, des espèces réelles qui y poussent à l'état sauvage. Une pousse ne grandit que dans les biomes d'où vient son espèce : ailleurs elle reste une pousse et la poudre d'os ne sert à rien. Son infobulle nomme ces biomes.",
        "Fruits et noix tombent parfois des feuilles. Écorcez une bûche à la hache pour récolter l'écorce quand c'est elle qui sert. Fixez une Saignée à un arbre qui donne sève ou résine : les liquides demandent un flacon vide, les résines dures tombent directement dans la saignée.",
        "Dans les machines : la résine de pin dans l'Alambic donne térébenthine et colophane ; l'écorce de cannelle, le bois de camphrier, les feuilles d'eucalyptus et le santal donnent leurs huiles dans l'Alambic ; l'écorce de quinquina dans la Cuve de digestion donne la quinine ; le pernambouc dans la Cuve de teinture donne une teinture rouge. Les fruits huileux vont à la Presse à huile, les sucrés au Tonneau de fermentation.",
    ],
    "it_it": [
        "Ogni bioma di terra ha cinque alberi suoi, specie reali che vi crescono spontanee. Un arbusto cresce solo nei biomi da cui viene la sua specie: altrove resta un arbusto e la farina d'ossa non serve. Il suo suggerimento elenca quei biomi.",
        "Frutti e noci cadono ogni tanto dalle foglie. Scorteccia un tronco con l'ascia per staccare la corteccia quando è lei la parte utile. Fissa uno Spillatore a un albero che dia linfa o resina: i liquidi vogliono una fiaschetta vuota, le resine dure cadono direttamente nello spillatore.",
        "Nelle macchine: la resina di pino nell'Alambicco dà trementina e colofonia; corteccia di cannella, legno di canforo, foglie di eucalipto e sandalo danno i loro oli nell'Alambicco; la corteccia di china nella Vasca di digestione dà il chinino; il pernambuco nel Tino di tintura dà colorante rosso. I frutti oleosi vanno al Torchio da olio, quelli dolci alla Botte di fermentazione.",
    ],
    "de_de": [
        "Jedes Landbiom hat fünf eigene Bäume, echte Arten, die dort wild wachsen. Ein Setzling wächst nur in den Biomen, aus denen seine Art stammt: anderswo bleibt er ein Setzling, und Knochenmehl hilft nicht. Sein Tooltip nennt diese Biome.",
        "Früchte und Nüsse fallen ab und zu aus dem Laub. Entrinde einen Stamm mit der Axt, um die Rinde zu ernten, wo die Rinde das Wertvolle ist. Befestige einen Zapfhahn an einem Baum mit Saft oder Harz: Flüssigkeiten brauchen eine leere Flasche, harte Harze fallen direkt in den Zapfhahn.",
        "In den Maschinen: Kiefernharz im Alembik gibt Terpentinöl und Kolophonium; Zimtrinde, Kampferholz, Eukalyptuslaub und Sandelholz geben ihre Öle im Alembik; Chinarinde im Aufschlussbottich gibt Chinin; Brasilholz im Färbebottich gibt roten Farbstoff. Ölige Früchte kommen in die Ölpresse, süße ins Gärfass.",
    ],
    "nl_nl": [
        "Elk landbioom heeft vijf eigen bomen, echte soorten die er in het wild groeien. Een zaailing groeit alleen in de biomen waar zijn soort vandaan komt: elders blijft hij een zaailing en helpt beendermeel niet. Zijn tooltip noemt die biomen.",
        "Vruchten en noten vallen soms uit de bladeren. Ontschors een stam met een bijl om de schors te oogsten als die het nuttige deel is. Bevestig een Tapkraan aan een boom die sap of hars geeft: vloeistoffen vragen een lege fles, harde harsen vallen direct in de tapkraan.",
        "In de machines: dennenhars in de Alambiek geeft terpentijn en colofonium; kaneelbast, kamferhout, eucalyptusbladeren en sandelhout geven hun oliën in de Alambiek; kinabast in het Ontsluitingsvat geeft kinine; brazielhout in de Verfkuip geeft rode kleurstof. Olierijke vruchten gaan naar de Oliepers, zoete naar het Gistingsvat.",
    ],
    "sv_se": [
        "Varje landbiom har fem egna träd, verkliga arter som växer vilt där. En planta växer bara i de biom dess art kommer från: på andra ställen förblir den en planta och benmjöl hjälper inte. Dess verktygstips nämner de biomen.",
        "Frukt och nötter faller ibland ur lövverket. Barka en stam med yxa för att ta barken där barken är det nyttiga. Fäst ett Tapprör på ett träd som ger sav eller kåda: vätskor kräver en tom flaska, hårda kådor faller direkt ner i tappröret.",
        "I maskinerna: tallkåda i Alembiken ger terpentin och kolofonium; kanelbark, kamferträ, eukalyptusblad och sandelträ ger sina oljor i Alembiken; kinabark i Uppslutningskaret ger kinin; brasilträ i Färgkaret ger rött färgämne. Oljiga frukter går till Oljepressen, söta till Jäsfatet.",
    ],
    "da_dk": [
        "Hvert landbiom har fem egne træer, virkelige arter, der vokser vildt dér. En spire vokser kun i de biomer, dens art kommer fra: andre steder forbliver den en spire, og benmel hjælper ikke. Dens værktøjstip nævner de biomer.",
        "Frugter og nødder falder af og til fra løvet. Afbark en stamme med en økse for at få barken, hvor barken er det nyttige. Fastgør en Tappehane på et træ, der giver saft eller harpiks: væsker kræver en tom flaske, hårde harpikser falder direkte ned i tappehanen.",
        "I maskinerne: fyrreharpiks i Alembikken giver terpentin og kolofonium; kanelbark, kamfertræ, eukalyptusblade og sandeltræ giver deres olier i Alembikken; kinabark i Oplukningskarret giver kinin; brasiltræ i Farvekarret giver rødt farvestof. Fedtede frugter går til Oliepressen, søde til Gæringstønden.",
    ],
    "ru_ru": [
        "В каждом сухопутном биоме растут пять своих деревьев, настоящие виды, дикорастущие там. Саженец растёт только в биомах, откуда родом его вид: в других местах он так и остаётся саженцем, и костная мука не помогает. Подсказка саженца называет эти биомы.",
        "Плоды и орехи иногда падают с листвы. Окорите бревно топором, чтобы снять кору, если ценна именно она. Закрепите подсочный кран на дереве, дающем сок или смолу: для жидкостей нужна пустая колба, твёрдые смолы падают прямо в кран.",
        "В машинах: живица в аламбике даёт скипидар и канифоль; коричная кора, камфорная древесина, листья эвкалипта и сандал отдают свои масла в аламбике; хинная кора в чане для вскрытия даёт хинин; фернамбук в красильном чане даёт красный краситель. Масличные плоды идут в маслобойный пресс, сладкие — в бродильную бочку.",
    ],
}

# Field Guide templates for the generated paragraphs.
GUIDE_T = {
    "pt_br": {"trees_leaves": "{x} das folhas", "trees_bark": "{x} da casca", "trees_tap": "{x} pela torneira",
              "trees_wood": "madeira com {x}", "trees_leafy": "folhas com {x}", "trees_fuel": "lenha densa", "trees_none": "madeira"},
    "es_es": {"trees_leaves": "{x} de las hojas", "trees_bark": "{x} de la corteza", "trees_tap": "{x} con el grifo",
              "trees_wood": "madera con {x}", "trees_leafy": "hojas con {x}", "trees_fuel": "leña densa", "trees_none": "madera"},
    "fr_fr": {"trees_leaves": "{x} des feuilles", "trees_bark": "{x} de l'écorce", "trees_tap": "{x} au robinet",
              "trees_wood": "bois contenant {x}", "trees_leafy": "feuilles contenant {x}", "trees_fuel": "bois de chauffe dense", "trees_none": "bois"},
    "it_it": {"trees_leaves": "{x} dalle foglie", "trees_bark": "{x} dalla corteccia", "trees_tap": "{x} col rubinetto",
              "trees_wood": "legno con {x}", "trees_leafy": "foglie con {x}", "trees_fuel": "legna densa", "trees_none": "legno"},
    "de_de": {"trees_leaves": "{x} aus dem Laub", "trees_bark": "{x} aus der Rinde", "trees_tap": "{x} per Zapfhahn",
              "trees_wood": "Holz mit {x}", "trees_leafy": "Laub mit {x}", "trees_fuel": "dichtes Brennholz", "trees_none": "Holz"},
    "nl_nl": {"trees_leaves": "{x} uit de bladeren", "trees_bark": "{x} uit de schors", "trees_tap": "{x} via de tap",
              "trees_wood": "hout met {x}", "trees_leafy": "bladeren met {x}", "trees_fuel": "dicht brandhout", "trees_none": "hout"},
    "sv_se": {"trees_leaves": "{x} ur lövverket", "trees_bark": "{x} ur barken", "trees_tap": "{x} med kranen",
              "trees_wood": "trä med {x}", "trees_leafy": "löv med {x}", "trees_fuel": "tät ved", "trees_none": "trä"},
    "da_dk": {"trees_leaves": "{x} fra løvet", "trees_bark": "{x} fra barken", "trees_tap": "{x} med hanen",
              "trees_wood": "træ med {x}", "trees_leafy": "løv med {x}", "trees_fuel": "tæt brænde", "trees_none": "træ"},
    "ru_ru": {"trees_leaves": "{x} с листвы", "trees_bark": "{x} из коры", "trees_tap": "{x} через кран",
              "trees_wood": "древесина с веществом: {x}", "trees_leafy": "листья с веществом: {x}", "trees_fuel": "плотные дрова", "trees_none": "древесина"},
}


def text(code):
    """English -> translation for every new string in this language, block names included."""
    i = LANGS.index(code)
    out = {}
    for en, tr in {**TEXT}.items():
        parts = tr.split(" / ")
        assert len(parts) == len(LANGS), en
        out[en] = parts[i]
    return out


def biome_name(code, biome):
    parts = BIOMES[biome].split(" / ")
    return parts[0] if code == "en_us" else parts[1 + LANGS.index(code)]


# ---- 2.9.0, stage 2: temperate trees. ----
SPECIES.update({
    "eastern_cottonwood": "Choupo-americano / álamo de Virginia / peuplier deltoïde / pioppo deltoide / Kanadische Pappel|Kanadapappel / Amerikaanse populier|canadapopulier / kanadapoppel|kanadapoppel / canadisk poppel|canadapoppel / Тополь дельтовидный|тополя дельтовидного",
    "american_elm": "Olmo-americano / olmo americano / orme d'Amérique / olmo americano / Amerikanische Ulme|Ulmen / Amerikaanse iep|iepen / amerikansk alm|alm / hvid elm|elme / Вяз американский|вяза американского",
    "ash": "Freixo / fresno / frêne / frassino / Gemeine Esche|Eschen / es|essen / ask|ask / ask|aske / Ясень обыкновенный|ясеня",
    "walnut": "Nogueira / nogal / noyer / noce / Walnussbaum|Walnuss / walnoot|walnoten / valnötsträd|valnöts / valnøddetræ|valnødde / Орех грецкий|грецкого ореха",
    "white_mulberry": "Amoreira-branca / morera blanca / mûrier blanc / gelso bianco / Weiße Maulbeere|Maulbeer / witte moerbei|moerbei / vitt mullbärsträd|mullbärs / hvid morbær|morbær / Шелковица белая|шелковицы белой",
    "linden": "Tília / tilo / tilleul à petites feuilles / tiglio selvatico / Winterlinde|Linden / winterlinde|linden / skogslind|lind / småbladet lind|linde / Липа мелколистная|липы мелколистной",
    "sweet_chestnut": "Castanheiro / castaño / châtaignier / castagno / Edelkastanie|Kastanien / tamme kastanje|kastanje / äkta kastanj|kastanj / ægte kastanje|kastanje / Каштан посевной|каштана посевного",
    "pear": "Pereira / peral / poirier / pero / Birnbaum|Birnbaum / perenboom|perenboom / päronträd|päronträds / pæretræ|pære / Груша обыкновенная|груши",
    "elder": "Sabugueiro / saúco / sureau noir / sambuco / Schwarzer Holunder|Holunder / vlier|vlier / fläder|fläder / hyld|hylde / Бузина чёрная|бузины чёрной",
    "beech": "Faia / haya / hêtre / faggio / Rotbuche|Buchen / beuk|beuken / bok|bok / bøg|bøge / Бук лесной|бука лесного",
    "sugar_maple": "Bordo-açucareiro / arce de azúcar / érable à sucre / acero da zucchero / Zuckerahorn|Zuckerahorn / suikeresdoorn|suikeresdoorn / sockerlönn|sockerlönns / sukkerløn|sukkerløn / Клён сахарный|клёна сахарного",
    "hornbeam": "Carpino / carpe / charme / carpino bianco / Hainbuche|Hainbuchen / haagbeuk|haagbeuken / avenbok|avenbok / avnbøg|avnbøge / Граб обыкновенный|граба",
    "southern_magnolia": "Magnólia / magnolio / magnolia à grandes fleurs / magnolia / Immergrüne Magnolie|Magnolien / magnolia|magnolia / magnolia|magnolia / magnolie|magnolie / Магнолия крупноцветковая|магнолии крупноцветковой",
    "tulip_tree": "Tulipeiro / tulípero / tulipier de Virginie / albero dei tulipani / Tulpenbaum|Tulpenbaum / tulpenboom|tulpenboom / tulpanträd|tulpanträds / tulipantræ|tulipantræ / Тюльпанное дерево|тюльпанного дерева",
    "paulownia": "Paulównia / paulownia / paulownia / paulonia / Blauglockenbaum|Blauglockenbaum / anna-paulownaboom|paulownia / kejsarträd|kejsarträds / kejsertræ|kejsertræ / Павловния войлочная|павловнии",
    "judas_tree": "Árvore-de-judas / árbol del amor / arbre de Judée / albero di Giuda / Judasbaum|Judasbaum / judasboom|judasboom / judasträd|judasträds / judastræ|judastræ / Иудино дерево|иудина дерева",
    "quaking_aspen": "Choupo-tremedor / álamo temblón americano / peuplier faux-tremble / pioppo tremulo americano / Amerikanische Zitterpappel|Zitterpappel / Amerikaanse ratelpopulier|ratelpopulier / amerikansk asp|asp / amerikansk bævreasp|asp / Осина осинообразная|осины",
    "alder": "Amieiro / aliso / aulne glutineux / ontano nero / Schwarzerle|Erlen / zwarte els|elzen / klibbal|al / rødel|elle / Ольха чёрная|ольхи чёрной",
    "rowan": "Sorveira / serbal de cazadores / sorbier des oiseleurs / sorbo degli uccellatori / Eberesche|Ebereschen / wilde lijsterbes|lijsterbes / rönn|rönn / røn|røn / Рябина обыкновенная|рябины",
    "black_cherry": "Cerejeira-negra / cerezo negro / cerisier tardif / ciliegio tardivo / Späte Traubenkirsche|Traubenkirschen / Amerikaanse vogelkers|vogelkers / glanshägg|häggs / glansbladet hæg|hæg / Черёмуха поздняя|черёмухи поздней",
    "yew": "Teixo / tejo / if / tasso / Eibe|Eiben / taxus|taxus / idegran|idegrans / taks|taks / Тис ягодный|тиса",
    "black_walnut": "Nogueira-preta / nogal negro / noyer noir / noce nero / Schwarznuss|Schwarznuss / zwarte walnoot|zwarte-walnoot / svart valnöt|svartvalnöts / sort valnød|sortvalnødde / Орех чёрный|чёрного ореха",
    "holly": "Azevinho / acebo / houx / agrifoglio / Stechpalme|Stechpalmen / hulst|hulst / järnek|järneks / kristtorn|kristtorn / Падуб остролистный|падуба",
    "bald_cypress": "Cipreste-calvo / ciprés calvo / cyprès chauve / cipresso calvo / Echte Sumpfzypresse|Sumpfzypressen / moerascipres|moerascipres / sumpcypress|sumpcypress / sumpcypres|sumpcypres / Болотный кипарис|болотного кипариса",
    "water_tupelo": "Tupelo / tupelo acuático / tupélo aquatique / tupelo acquatico / Wasser-Tupelobaum|Tupelo / watertupelo|tupelo / vattentupelo|tupelo / vandtupelo|tupelo / Нисса водная|ниссы водной",
    "white_willow": "Salgueiro-branco / sauce blanco / saule blanc / salice bianco / Silberweide|Weiden / schietwilg|wilgen / vitpil|pil / hvidpil|pile / Ива белая|ивы белой",
    "red_maple": "Bordo-vermelho / arce rojo / érable rouge / acero rosso / Rotahorn|Rotahorn / rode esdoorn|rode-esdoorn / rödlönn|rödlönns / rødløn|rødløn / Клён красный|клёна красного",
    "ginkgo": "Ginkgo / ginkgo / ginkgo / ginkgo / Ginkgo|Ginkgo / ginkgo|ginkgo / ginkgo|ginkgo / ginkgo|ginkgo / Гинкго|гинкго",
    "japanese_maple": "Bordo-japonês / arce japonés / érable du Japon / acero giapponese / Fächerahorn|Fächerahorn / Japanse esdoorn|Japanse-esdoorn / japansk lönn|japanlönns / japansk løn|japanløn / Клён дланевидный|клёна дланевидного",
    "sugi": "Sugi / cedro japonés / cryptomère du Japon / criptomeria / Sicheltanne|Sicheltannen / Japanse ceder|cryptomeria / japansk cypress|sugi / japansk cypres|sugi / Криптомерия японская|криптомерии",
    "lacquer_tree": "Árvore-da-laca / árbol de la laca / arbre à laque / albero della lacca / Lackbaum|Lackbaum / lakboom|lakboom / lackträd|lackträds / laktræ|laktræ / Лаковое дерево|лакового дерева",
    "cork_oak": "Sobreiro / alcornoque / chêne-liège / sughera / Korkeiche|Korkeichen / kurkeik|kurkeiken / korkek|korkek / korkeg|korkege / Дуб пробковый|пробкового дуба",
    "olive": "Oliveira / olivo / olivier / olivo / Olivenbaum|Olivenbaum / olijfboom|olijfboom / olivträd|olivträds / oliventræ|oliventræ / Олива европейская|оливы",
    "cedar_of_lebanon": "Cedro-do-líbano / cedro del Líbano / cèdre du Liban / cedro del Libano / Libanon-Zeder|Zedern / libanonceder|ceder / libanonceder|ceder / libanonceder|ceder / Кедр ливанский|ливанского кедра",
    "black_pine": "Pinheiro-negro / pino negral / pin noir / pino nero / Schwarzkiefer|Schwarzkiefern / Oostenrijkse den|zwarte-den / svarttall|svarttalls / østrigsk fyr|sortfyr / Сосна чёрная|сосны чёрной",
    "carob": "Alfarrobeira / algarrobo / caroubier / carrubo / Johannisbrotbaum|Johannisbrotbaum / johannesbroodboom|johannesbroodboom / johannesbrödträd|johannesbrödträds / johannesbrødtræ|johannesbrødtræ / Рожковое дерево|рожкового дерева",
    "stone_pine": "Pinheiro-manso / pino piñonero / pin parasol / pino domestico / Pinie|Pinien / parasolden|parasolden / pinje|pinje / pinje|pinje / Сосна итальянская|сосны итальянской",
    "aleppo_pine": "Pinheiro-de-alepo / pino carrasco / pin d'Alep / pino d'Aleppo / Aleppo-Kiefer|Aleppokiefern / aleppoden|aleppoden / aleppotall|aleppotalls / aleppofyr|aleppofyr / Сосна алеппская|сосны алеппской",
    "apple": "Macieira / manzano / pommier / melo / Apfelbaum|Apfelbaum / appelboom|appelboom / äppelträd|äppelträds / æbletræ|æbletræ / Яблоня домашняя|яблони",
    "monterey_cypress": "Cipreste-de-monterey / ciprés de Monterrey / cyprès de Lambert / cipresso di Monterey / Monterey-Zypresse|Montereyzypressen / montereycipres|montereycipres / montereycypress|montereycypress / montereycypres|montereycypres / Кипарис крупноплодный|кипариса крупноплодного",
})

TEXT.update({
    # Fruit, nuts and bark.
    "Apple": "Maçã / Manzana / Pomme / Mela / Apfel / Appel / Äpple / Æble / Яблоко",
    "Walnut": "Noz / Nuez / Noix / Noce / Walnuss / Walnoot / Valnöt / Valnød / Грецкий орех",
    "Mulberry": "Amora-branca / Mora blanca / Mûre blanche / Mora di gelso / Maulbeere / Moerbei / Mullbär / Morbær / Шелковица",
    "Linden Flower": "Flor de Tília / Flor de tilo / Fleur de tilleul / Fiore di tiglio / Lindenblüte / Lindebloesem / Lindblomma / Lindeblomst / Липовый цвет",
    "Chestnut": "Castanha / Castaña / Châtaigne / Castagna / Esskastanie / Tamme kastanje / Ätlig kastanj / Spiselig kastanje / Каштан",
    "Pear": "Pera / Pera / Poire / Pera / Birne / Peer / Päron / Pære / Груша",
    "Elderberry": "Baga de Sabugueiro / Baya de saúco / Baie de sureau / Bacca di sambuco / Holunderbeere / Vlierbes / Fläderbär / Hyldebær / Ягода бузины",
    "Rowan Berry": "Baga de Sorveira / Baya de serbal / Baie de sorbier / Bacca di sorbo / Vogelbeere / Lijsterbes / Rönnbär / Rønnebær / Ягода рябины",
    "Black Walnut": "Noz-preta / Nuez negra / Noix noire / Noce nera / Schwarznuss / Zwarte walnoot / Svart valnöt / Sort valnød / Чёрный орех",
    "Ginkgo Nut": "Semente de Ginkgo / Semilla de ginkgo / Noix de ginkgo / Seme di ginkgo / Ginkgonuss / Ginkgonoot / Ginkgonöt / Ginkgonød / Орех гинкго",
    "Olive": "Azeitona / Aceituna / Olive / Oliva / Olive / Olijf / Oliv / Oliven / Оливка",
    "Carob Pod": "Vagem de Alfarroba / Algarroba / Caroube / Carruba / Johannisbrot / Johannesbrood / Johannesbröd / Johannesbrød / Плод рожкового дерева",
    "Cork": "Cortiça / Corcho / Liège / Sughero / Kork / Kurk / Kork / Kork / Пробка",
    "Willow Bark": "Casca de Salgueiro / Corteza de sauce / Écorce de saule / Corteccia di salice / Weidenrinde / Wilgenbast / Pilbark / Pilebark / Ивовая кора",
    # Substances.
    "Maple Sap": "Seiva de Bordo / Savia de arce / Sève d'érable / Linfa d'acero / Ahornsaft / Esdoornsap / Lönnsav / Ahornsaft / Кленовый сок",
    "Salicin": "Salicina / Salicina / Salicine / Salicina / Salicin / Salicine / Salicin / Salicin / Салицин",
    "Suberin": "Suberina / Suberina / Subérine / Suberina / Suberin / Suberine / Suberin / Suberin / Суберин",
    "Urushi Lacquer": "Laca Urushi / Laca urushi / Laque urushi / Lacca urushi / Urushi-Lack / Urushilak / Urushilack / Urushilak / Лак уруси",
    "Urushiol": "Urushiol / Urushiol / Urushiol / Urushiolo / Urushiol / Urushiol / Urushiol / Urushiol / Урушиол",
    "Taxol": "Taxol / Taxol / Taxol / Taxolo / Taxol / Taxol / Taxol / Taxol / Таксол",
    "Cedrol": "Cedrol / Cedrol / Cédrol / Cedrolo / Cedrol / Cedrol / Cedrol / Cedrol / Цедрол",
    "Sugar maple sap: boil it down for maple sugar": "Seiva do bordo-açucareiro: ferva para ter açúcar / Savia del arce de azúcar: hiérvela para azúcar / Sève d'érable à sucre : à bouillir pour le sucre / Linfa d'acero da zucchero: si bolle per lo zucchero / Zuckerahornsaft: einkochen ergibt Ahornzucker / Sap van de suikeresdoorn: inkoken tot suiker / Sockerlönnsav: kok in till lönnsocker / Sukkerlønsaft: kog den ind til ahornsukker / Сок сахарного клёна: уварите до сахара",
    "Willow bark glucoside: the herbal forerunner of aspirin": "Glicosídeo da casca de salgueiro: avô da aspirina / Glucósido de la corteza de sauce: abuelo de la aspirina / Glucoside de l'écorce de saule, ancêtre de l'aspirine / Glucoside della corteccia di salice, avo dell'aspirina / Glucosid der Weidenrinde: Vorläufer des Aspirins / Glucoside uit wilgenbast: voorloper van aspirine / Glukosid ur pilbark: aspirinets föregångare / Glukosid fra pilebark: aspirinens forløber / Глюкозид ивовой коры, предшественник аспирина",
    "Waxy polyester of cork: keeps out water and heat": "Poliéster ceroso da cortiça: barra água e calor / Poliéster céreo del corcho: aísla agua y calor / Polyester cireux du liège : arrête l'eau et la chaleur / Poliestere cereo del sughero: ferma acqua e calore / Wachsiger Polyester des Korks: hält Wasser und Hitze ab / Wasachtige polyester van kurk: weert water en warmte / Korkens vaxartade polyester: stänger ute vatten och värme / Korkens voksagtige polyester: holder vand og varme ude / Воскоподобный полиэфир пробки: не пускает воду и тепло",
    "Lacquer tree sap: a hard glossy varnish once dry": "Seiva da árvore-da-laca: verniz duro e brilhante / Savia del árbol de la laca: barniz duro y brillante / Sève de l'arbre à laque : un vernis dur et brillant / Linfa dell'albero della lacca: vernice dura e lucida / Saft des Lackbaums: trocken ein harter Glanzlack / Sap van de lakboom: droog een harde glanslak / Lackträdets sav: torkar till en hård blank lack / Laktræets saft: tørrer til en hård blank lak / Сок лакового дерева: высыхая, даёт твёрдый лак",
    "The oil in lacquer that hardens; it blisters skin": "O óleo da laca que endurece; queima a pele / El aceite de la laca que endurece; ampolla la piel / L'huile de la laque qui durcit ; elle brûle la peau / L'olio della lacca che indurisce; irrita la pelle / Das härtende Öl im Lack; es verätzt die Haut / De olie in lak die uithardt; geeft blaren / Oljan i lacken som härdar; den ger blåsor / Olien i lakken, der hærder; den giver vabler / Твердеющее масло лака; вызывает ожоги кожи",
    "Yew alkaloid that stops cancer cells dividing": "Alcaloide do teixo que impede o câncer de se dividir / Alcaloide del tejo que frena la división del cáncer / Alcaloïde de l'if qui bloque la division du cancer / Alcaloide del tasso che blocca la divisione del cancro / Eibenalkaloid, das die Teilung von Krebszellen stoppt / Taxusalkaloïde dat kankercellen stopt met delen / Idegransalkaloid som hindrar cancerceller att dela sig / Taksalkaloid, der stopper kræftcellers deling / Алкалоид тиса, останавливающий деление раковых клеток",
    "Cedarwood oil: perfume and moth repellent": "Óleo de cedro: perfume e repelente de traças / Aceite de cedro: perfume y antipolillas / Huile de cèdre : parfum et antimites / Olio di cedro: profumo e antitarme / Zedernholzöl: Duft und Mottenschutz / Cederhoutolie: parfum en tegen motten / Cederträolja: parfym och malmedel / Cedertræsolie: parfume og mod møl / Кедровое масло: духи и средство от моли",
    # Reactions.
    "Sugar crystallizing": "Cristalização do açúcar / Cristalización del azúcar / Cristallisation du sucre / Cristallizzazione dello zucchero / Zuckerkristallisation / Suikerkristallisatie / Sockerkristallisering / Sukkerkrystallisering / Кристаллизация сахара",
    "Salicin extraction": "Extração de salicina / Extracción de salicina / Extraction de la salicine / Estrazione della salicina / Salicinextraktion / Salicine-extractie / Salicinextraktion / Salicinekstraktion / Извлечение салицина",
    "Salicin oxidation": "Oxidação da salicina / Oxidación de la salicina / Oxydation de la salicine / Ossidazione della salicina / Salicinoxidation / Salicine-oxidatie / Salicinoxidation / Salicinoxidation / Окисление салицина",
    "Cedrol distillation": "Destilação do cedrol / Destilación del cedrol / Distillation du cédrol / Distillazione del cedrolo / Cedroldestillation / Cedroldestillatie / Cedroldestillation / Cedroldestillation / Перегонка цедрола",
    "Taxol extraction": "Extração de taxol / Extracción de taxol / Extraction du taxol / Estrazione del taxolo / Taxolextraktion / Taxolextractie / Taxolextraktion / Taxolekstraktion / Извлечение таксола",
    "Maple or palm sap boiled down: the sugar stays when the water has gone": "Seiva de bordo ou de palmeira fervida: o açúcar fica quando a água se vai / Savia de arce o de palma hervida: el azúcar queda cuando se va el agua / Sève d'érable ou de palmier réduite : le sucre reste quand l'eau est partie / Linfa d'acero o di palma bollita: lo zucchero resta quando l'acqua se ne va / Ahorn- oder Palmsaft eingekocht: der Zucker bleibt, wenn das Wasser fort ist / Esdoorn- of palmsap ingekookt: de suiker blijft als het water weg is / Lönn- eller palmsav inkokt: sockret blir kvar när vattnet är borta / Ahorn- eller palmesaft kogt ind: sukkeret bliver tilbage, når vandet er væk / Кленовый или пальмовый сок уварен: сахар остаётся, когда уходит вода",
    "Willow bark steeped in hot water gives up its salicin (Leroux 1829)": "Casca de salgueiro em água quente solta a salicina (Leroux 1829) / La corteza de sauce en agua caliente suelta su salicina (Leroux 1829) / L'écorce de saule infusée à chaud libère sa salicine (Leroux 1829) / La corteccia di salice in acqua calda cede la salicina (Leroux 1829) / Weidenrinde gibt in heißem Wasser ihr Salicin ab (Leroux 1829) / Wilgenbast in heet water geeft zijn salicine af (Leroux 1829) / Pilbark i hett vatten släpper sitt salicin (Leroux 1829) / Pilebark i varmt vand afgiver sin salicin (Leroux 1829) / Ивовая кора в горячей воде отдаёт салицин (Леру, 1829)",
    "Salicin split and oxidised: salicylic acid from willow bark (Piria 1838); put oxygen in the reagent slot": "Salicina quebrada e oxidada: ácido salicílico da casca de salgueiro (Piria 1838); ponha oxigênio no espaço de reagente / Salicina escindida y oxidada: ácido salicílico de la corteza de sauce (Piria 1838); pon oxígeno en la ranura de reactivo / Salicine scindée et oxydée : acide salicylique tiré du saule (Piria 1838) ; mettez de l'oxygène dans l'emplacement du réactif / Salicina scissa e ossidata: acido salicilico dal salice (Piria 1838); metti ossigeno nello slot del reagente / Salicin gespalten und oxidiert: Salicylsäure aus Weidenrinde (Piria 1838); Sauerstoff in den Reagenzplatz legen / Salicine gesplitst en geoxideerd: salicylzuur uit wilgenbast (Piria 1838); leg zuurstof in het reagensvak / Salicin spjälkat och oxiderat: salicylsyra ur pilbark (Piria 1838); lägg syre i reagensplatsen / Salicin spaltet og oxideret: salicylsyre fra pilebark (Piria 1838); læg ilt i reagenspladsen / Салицин расщеплён и окислен: салициловая кислота из ивовой коры (Пириа, 1838); положите кислород в ячейку реагента",
    "Cedar wood distilled with steam: cedarwood oil comes over": "Madeira de cedro destilada com vapor: sai o óleo de cedro / Madera de cedro destilada con vapor: pasa el aceite de cedro / Bois de cèdre distillé à la vapeur : l'huile de cèdre passe / Legno di cedro distillato in corrente di vapore: passa l'olio di cedro / Zedernholz mit Dampf destilliert: das Zedernholzöl geht über / Cederhout met stoom gedestilleerd: de cederolie komt over / Cederträ destillerat med ånga: cederoljan går över / Cedertræ destilleret med damp: cedertræsolien går over / Кедровая древесина перегнана с паром: отходит кедровое масло",
    "Yew needles extracted with solvent and purified: a few milligrams of taxol from a whole tree (Wall and Wani 1971)": "Agulhas de teixo extraídas com solvente e purificadas: poucos miligramas de taxol de uma árvore inteira (Wall e Wani 1971) / Agujas de tejo extraídas con disolvente y purificadas: unos miligramos de taxol de un árbol entero (Wall y Wani 1971) / Aiguilles d'if extraites au solvant et purifiées : quelques milligrammes de taxol par arbre (Wall et Wani 1971) / Aghi di tasso estratti con solvente e purificati: pochi milligrammi di taxolo da un albero intero (Wall e Wani 1971) / Eibennadeln mit Lösungsmittel extrahiert und gereinigt: wenige Milligramm Taxol aus einem ganzen Baum (Wall und Wani 1971) / Taxusnaalden met oplosmiddel geëxtraheerd en gezuiverd: een paar milligram taxol uit een hele boom (Wall en Wani 1971) / Idegransbarr extraherade med lösningsmedel och renade: några milligram taxol ur ett helt träd (Wall och Wani 1971) / Taksnåle ekstraheret med opløsningsmiddel og renset: få milligram taxol fra et helt træ (Wall og Wani 1971) / Хвоя тиса, экстрагированная растворителем и очищенная: несколько миллиграммов таксола из целого дерева (Уолл и Вани, 1971)",
})

_GUIDE4 = {
    "pt_br": "Nos bosques temperados: sangre o bordo-açucareiro e o bordo-vermelho para ter seiva de bordo e ferva-a na Salina para ter açúcar. Tire cortiça do sobreiro e casca do salgueiro-branco; o Tanque de Digestão extrai a salicina da casca e, mais adiante, a transforma em ácido salicílico. O cedro-do-líbano dá óleo de cedro no Alambique. A árvore-da-laca dá laca urushi. As folhas do teixo guardam um traço de taxol, que só o último nível consegue extrair.",
    "es_es": "En los bosques templados: sangra el arce de azúcar y el arce rojo para obtener savia de arce y hiérvela en la Salina para sacar azúcar. Saca corcho del alcornoque y corteza del sauce blanco; la Cuba de digestión extrae la salicina de la corteza y, más adelante, la convierte en ácido salicílico. El cedro del Líbano da aceite de cedro en el Alambique. El árbol de la laca da laca urushi. Las hojas del tejo guardan un rastro de taxol, que solo el último nivel puede extraer.",
    "fr_fr": "Dans les bois tempérés : saignez l'érable à sucre et l'érable rouge pour leur sève et faites-la réduire au Marais salant pour le sucre. Récoltez le liège du chêne-liège et l'écorce du saule blanc ; la Cuve de digestion tire la salicine de l'écorce, puis la change plus tard en acide salicylique. Le cèdre du Liban donne l'huile de cèdre dans l'Alambic. L'arbre à laque donne la laque urushi. Les feuilles de l'if gardent une trace de taxol, que seul le dernier niveau sait extraire.",
    "it_it": "Nei boschi temperati: spilla l'acero da zucchero e l'acero rosso per la linfa d'acero e falla bollire nella Salina per lo zucchero. Togli il sughero dalla sughera e la corteccia dal salice bianco; la Vasca di digestione estrae la salicina dalla corteccia e più avanti la trasforma in acido salicilico. Il cedro del Libano dà olio di cedro nell'Alambicco. L'albero della lacca dà la lacca urushi. Le foglie del tasso contengono una traccia di taxolo, che solo l'ultimo livello sa estrarre.",
    "de_de": "In den gemäßigten Wäldern: Zapfe Zuckerahorn und Rotahorn für Ahornsaft an und koche ihn in der Salzpfanne zu Zucker ein. Schäle Kork von der Korkeiche und Rinde von der Silberweide; der Aufschlussbottich zieht das Salicin aus der Rinde und macht später Salicylsäure daraus. Die Libanon-Zeder gibt im Alembik Zedernholzöl. Der Lackbaum gibt Urushi-Lack. Eibenlaub enthält eine Spur Taxol, die erst die letzte Stufe gewinnen kann.",
    "nl_nl": "In de gematigde bossen: tap suikeresdoorn en rode esdoorn af voor esdoornsap en kook het in in de Zoutpan tot suiker. Haal kurk van de kurkeik en bast van de schietwilg; het Ontsluitingsvat haalt de salicine uit de bast en maakt er later salicylzuur van. De libanonceder geeft cederolie in de Alambiek. De lakboom geeft urushilak. Taxusbladeren bevatten een spoor taxol, dat pas het laatste niveau kan winnen.",
    "sv_se": "I de tempererade skogarna: tappa sockerlönn och rödlönn på lönnsav och koka in den i Saltpannan till socker. Skala kork från korkeken och bark från vitpilen; Uppslutningskaret drar ut salicinet ur barken och gör senare salicylsyra av det. Libanoncedern ger cederolja i Alembiken. Lackträdet ger urushilack. Idegranens barr har ett spår av taxol, som bara den sista nivån kan utvinna.",
    "da_dk": "I de tempererede skove: tap sukkerløn og rødløn for ahornsaft og kog den ind i Saltpanden til sukker. Skræl kork af korkegen og bark af hvidpilen; Oplukningskarret trækker salicinen ud af barken og gør den senere til salicylsyre. Libanoncederen giver cedertræsolie i Alembikken. Laktræet giver urushilak. Takstræets nåle rummer et spor af taxol, som kun det sidste niveau kan udvinde.",
    "ru_ru": "В лесах умеренного пояса: подсачивайте сахарный и красный клён ради кленового сока и уваривайте его в соляном бассейне до сахара. Снимайте пробку с пробкового дуба и кору с белой ивы; чан для вскрытия извлекает из коры салицин, а позже превращает его в салициловую кислоту. Ливанский кедр даёт в аламбике кедровое масло. Лаковое дерево даёт лак уруси. В хвое тиса есть след таксола, извлечь который может только последний уровень.",
}
for _code, _p in _GUIDE4.items():
    GUIDE[_code].append(_p)


# ---- 3.0.0, stage 3: cold and mountain trees. ----
SPECIES.update({
    "balsam_fir": "Abeto-balsâmico / abeto balsámico / sapin baumier / abete balsamico / Balsamtanne|Balsamtannen / balsemzilverspar|balsemspar / balsamgran|balsamgrans / balsamgran|balsamgran / Пихта бальзамическая|пихты бальзамической",
    "silver_fir": "Abeto-branco / abeto blanco / sapin pectiné / abete bianco / Weißtanne|Weißtannen / gewone zilverspar|zilverspar / silvergran|silvergrans / ædelgran|ædelgran / Пихта белая|пихты белой",
    "douglas_fir": "Abeto-de-douglas / abeto de Douglas / douglas vert / abete di Douglas / Douglasie|Douglasien / douglasspar|douglasspar / douglasgran|douglasgrans / douglasgran|douglasgran / Лжетсуга Мензиса|лжетсуги",
    "siberian_fir": "Abeto-siberiano / abeto siberiano / sapin de Sibérie / abete siberiano / Sibirische Tanne|Sibirische-Tannen- / Siberische zilverspar|siberische-spar / sibirisk ädelgran|sibirisk-gran / sibirisk ædelgran|sibirisk-gran / Пихта сибирская|пихты сибирской",
    "green_alder": "Amieiro-verde / aliso verde / aulne vert / ontano verde / Grünerle|Grünerlen / groene els|groene-els / grönal|grönal / grønel|grønelle / Ольховник зелёный|ольховника",
    "sycamore_maple": "Bordo-sicômoro / arce blanco / érable sycomore / acero di monte / Bergahorn|Bergahorn / gewone esdoorn|esdoorn / tysklönn|tysklönns / ahorn|ahorn / Клён белый|клёна белого",
    "atlas_cedar": "Cedro-do-atlas / cedro del Atlas / cèdre de l'Atlas / cedro dell'Atlante / Atlas-Zeder|Atlaszedern / atlasceder|atlasceder / atlasceder|atlasceder / atlasceder|atlasceder / Кедр атласский|атласского кедра",
    "siberian_pine": "Cedro-siberiano / pino siberiano / pin de Sibérie / pino siberiano / Sibirische Zirbelkiefer|Sibirische-Zirbel- / Siberische den|siberische-den / sibirisk tall|sibirisk-tall / sibirisk fyr|sibirisk-fyr / Кедр сибирский|кедра сибирского",
    "western_red_cedar": "Cedro-vermelho / tuya gigante / thuya géant / tuia gigante / Riesen-Lebensbaum|Riesenlebensbaum / reuzenlevensboom|reuzenlevensboom / jättetuja|jättetuja / kæmpethuja|kæmpethuja / Туя складчатая|туи складчатой",
    "sea_buckthorn": "Espinheiro-marítimo / espino amarillo / argousier / olivello spinoso / Sanddorn|Sanddorn / duindoorn|duindoorn / havtorn|havtorns / havtorn|havtorn / Облепиха крушиновидная|облепихи",
    "european_larch": "Larício-europeu / alerce europeo / mélèze d'Europe / larice europeo / Europäische Lärche|Lärchen / Europese lork|lorken / europeisk lärk|lärk / europæisk lærk|lærke / Лиственница европейская|лиственницы европейской",
    "dahurian_larch": "Lariço-dahúrico / alerce de Dahuria / mélèze de Dahurie / larice della Dauria / Dahurische Lärche|Dahurische-Lärchen- / Dahurische lork|dahurische-lork / dahurisk lärk|dahurisk-lärk / dahurisk lærk|dahurisk-lærke / Лиственница Гмелина|лиственницы Гмелина",
    "siberian_dwarf_pine": "Pinheiro-anão-siberiano / pino enano siberiano / pin nain de Sibérie / pino nano siberiano / Zwergkiefer|Zwergkiefern / Siberische dwergden|dwergden / sibirisk dvärgtall|dvärgtalls / sibirisk dværgfyr|dværgfyr / Кедровый стланик|кедрового стланика",
    "eastern_white_pine": "Pinheiro-branco / pino blanco / pin blanc / pino strobo / Weymouth-Kiefer|Weymouthkiefern / Weymouthden|weymouthden / weymouthtall|weymouthtalls / weymouthfyr|weymouthfyr / Сосна веймутова|сосны веймутовой",
    "bristlecone_pine": "Pinheiro-bristlecone / pino longevo / pin de Bristlecone / pino longevo / Langlebige Kiefer|Grannenkiefern / langlevende den|grannenden / borstkottetall|borstkottetalls / børstekoglefyr|børstekoglefyr / Сосна остистая|сосны остистой",
    "swiss_pine": "Pinheiro-cembro / pino cembro / pin cembro / pino cembro / Zirbelkiefer|Zirbel / alpenden|alpenden / cembratall|cembratalls / cembrafyr|cembrafyr / Кедр европейский|кедра европейского",
    "whitebark_pine": "Pinheiro-de-casca-branca / pino de corteza blanca / pin à écorce blanche / pino dalla corteccia bianca / Weißstämmige Kiefer|Weißstammkiefern / witstamden|witstamden / vitbarkstall|vitbarkstalls / hvidbarksfyr|hvidbarksfyr / Сосна белокорая|сосны белокорой",
    "lodgepole_pine": "Pinheiro-lodgepole / pino contorta / pin tordu / pino contorto / Küsten-Kiefer|Drehkiefern / draaiden|draaiden / contortatall|contortatalls / klitfyr|klitfyr / Сосна скрученная|сосны скрученной",
    "mountain_pine": "Pinheiro-mugo / pino negro de montaña / pin de montagne / pino mugo / Bergkiefer|Latschen / bergden|bergden / bergtall|bergtalls / bjergfyr|bjergfyr / Сосна горная|сосны горной",
    "scots_pine": "Pinheiro-silvestre / pino silvestre / pin sylvestre / pino silvestre / Waldkiefer|Waldkiefern / grove den|grove-den / tall|tall / skovfyr|skovfyr / Сосна обыкновенная|сосны обыкновенной",
    "arctic_willow": "Salgueiro-ártico / sauce ártico / saule arctique / salice artico / Arktische Weide|Polarweiden / poolwilg|poolwilgen / fjällvide|fjällvide / arktisk pil|polarpile / Ива арктическая|ивы арктической",
    "coast_redwood": "Sequoia-costeira / secuoya roja / séquoia à feuilles d'if / sequoia sempreverde / Küstenmammutbaum|Küstenmammutbaum / kustmammoetboom|kustmammoetboom / kustsequoia|kustsequoia / kystredwood|kystredwood / Секвойя вечнозелёная|секвойи",
    "giant_sequoia": "Sequoia-gigante / secuoya gigante / séquoia géant / sequoia gigante / Riesenmammutbaum|Riesenmammutbaum / mammoetboom|mammoetboom / jättesequoia|jättesequoia / kæmpesequoia|kæmpesequoia / Секвойядендрон гигантский|секвойядендрона",
    "western_hemlock": "Tsuga / tsuga del Pacífico / pruche de l'Ouest / tsuga occidentale / Westamerikanische Hemlocktanne|Hemlocktannen / westelijke hemlockspar|hemlockspar / västamerikansk hemlock|hemlock / vestamerikansk hemlock|hemlock / Тсуга западная|тсуги западной",
    "common_juniper": "Zimbro-comum / enebro común / genévrier commun / ginepro comune / Gemeiner Wacholder|Wacholder / jeneverbes|jeneverbes / en|en / ene|ene / Можжевельник обыкновенный|можжевельника",
})

TEXT.update({
    "Sea Buckthorn Berry": "Baga de Espinheiro-marítimo / Baya de espino amarillo / Baie d'argousier / Bacca di olivello spinoso / Sanddornbeere / Duindoornbes / Havtornsbär / Havtornbær / Ягода облепихи",
    "Canada Balsam": "Bálsamo do Canadá / Bálsamo de Canadá / Baume du Canada / Balsamo del Canada / Kanadabalsam / Canadabalsem / Kanadabalsam / Canadabalsam / Канадский бальзам",
    "Balsam fir resin: the clear cement of lens makers": "Resina do abeto-balsâmico: a cola clara das lentes / Resina del abeto balsámico: pegamento de lentes / Résine du sapin baumier : la colle des lentilles / Resina dell'abete balsamico: colla delle lenti / Harz der Balsamtanne: klarer Kitt für Linsen / Hars van de balsemspar: heldere lenzenlijm / Balsamgranens kåda: klart kitt för linser / Balsamgranens harpiks: klar kit til linser / Смола пихты: прозрачный клей для линз",
})

_GUIDE5 = {
    "pt_br": "No frio e nas montanhas: lariços, abetos e pinheiros enchem as taigas; a sequoia-costeira e a sequoia-gigante, as árvores mais altas e mais volumosas da Terra, só crescem na taiga antiga de abetos. Acima do limite das árvores, pinheiros-anões, zimbros e o salgueiro-ártico crescem rentes ao chão e pegam na pedra, no cascalho e na neve. Sangre o abeto-balsâmico para ter bálsamo do Canadá, a cola transparente dos fabricantes de lentes, e o pinheiro-silvestre e o lariço para ter resina de pinheiro.",
    "es_es": "En el frío y en las montañas: alerces, abetos y pinos llenan las taigas; la secuoya roja y la secuoya gigante, los árboles más altos y más grandes de la Tierra, solo crecen en la taiga de abetos ancestral. Por encima del límite del bosque, pinos enanos, enebros y el sauce ártico crecen pegados al suelo y arraigan en piedra, grava y nieve. Sangra el abeto balsámico para obtener bálsamo de Canadá, el pegamento transparente de los ópticos, y el pino silvestre y el alerce para obtener resina de pino.",
    "fr_fr": "Dans le froid et en montagne : mélèzes, sapins et pins remplissent les taïgas ; le séquoia à feuilles d'if et le séquoia géant, les arbres les plus hauts et les plus massifs du monde, ne poussent que dans la taïga ancienne d'épicéas. Au-dessus de la limite des arbres, pins nains, genévriers et saule arctique rampent au ras du sol et s'enracinent dans la pierre, le gravier et la neige. Saignez le sapin baumier pour le baume du Canada, la colle transparente des opticiens, et le pin sylvestre et le mélèze pour la résine de pin.",
    "it_it": "Nel freddo e in montagna: larici, abeti e pini riempiono le taighe; la sequoia sempreverde e la sequoia gigante, gli alberi più alti e più grandi della Terra, crescono solo nella taiga di abeti secolari. Oltre il limite degli alberi, pini nani, ginepri e il salice artico strisciano bassi e attecchiscono su pietra, ghiaia e neve. Spilla l'abete balsamico per il balsamo del Canada, la colla trasparente degli ottici, e il pino silvestre e il larice per la resina di pino.",
    "de_de": "In der Kälte und im Gebirge: Lärchen, Tannen und Kiefern füllen die Taigas; Küstenmammutbaum und Riesenmammutbaum, die höchsten und größten Bäume der Erde, wachsen nur in der Fichten-Urtaiga. Über der Baumgrenze kriechen Zwergkiefern, Wacholder und die Arktische Weide am Boden und wurzeln in Stein, Kies und Schnee. Zapfe die Balsamtanne für Kanadabalsam, den klaren Kitt der Linsenmacher, und Waldkiefer und Lärche für Kiefernharz.",
    "nl_nl": "In de kou en in de bergen: lorken, sparren en dennen vullen de taiga's; de kustmammoetboom en de mammoetboom, de hoogste en grootste bomen op aarde, groeien alleen in de oude sparrentaiga. Boven de boomgrens kruipen dwergdennen, jeneverbessen en de poolwilg laag over de grond en wortelen in steen, grind en sneeuw. Tap de balsemspar af voor canadabalsem, de heldere lijm van lenzenmakers, en de grove den en de lork voor dennenhars.",
    "sv_se": "I kylan och i bergen: lärkar, granar och tallar fyller tajgorna; kustsequoian och jättesequoian, jordens högsta och största träd, växer bara i urskogstajgan med gran. Ovanför trädgränsen kryper dvärgtallar, enar och fjällviden längs marken och rotar sig i sten, grus och snö. Tappa balsamgranen på kanadabalsam, linsslipares klara kitt, och tallen och lärken på tallkåda.",
    "da_dk": "I kulden og i bjergene: lærk, ædelgran og fyr fylder tajgaerne; kystredwood og kæmpesequoia, Jordens højeste og største træer, vokser kun i den gamle grantajga. Over trægrænsen kryber dværgfyr, ene og polarpil langs jorden og slår rod i sten, grus og sne. Tap balsamgranen for canadabalsam, linsesliberes klare kit, og skovfyr og lærk for fyrreharpiks.",
    "ru_ru": "В холоде и в горах: лиственницы, пихты и сосны заполняют тайгу; секвойя и секвойядендрон, самые высокие и самые большие деревья Земли, растут только в старовозрастной еловой тайге. Выше границы леса кедровый стланик, можжевельник и арктическая ива стелются по земле и укореняются в камне, гравии и снегу. Подсачивайте пихту бальзамическую ради канадского бальзама, прозрачного клея оптиков, а сосну обыкновенную и лиственницу ради живицы.",
}
for _code, _p in _GUIDE5.items():
    GUIDE[_code].append(_p)


# ---- 3.1.0: important species added after the plan. ----
SPECIES.update({
    "araucaria": "Araucária / araucaria / pin du Paraná / araucaria brasiliana / Brasilianische Araukarie|Araukarien / Braziliaanse araucaria|araucaria / brasiliansk araukaria|araukaria / brasiliansk araukarie|araukarie / Араукария узколистная|араукарии",
    "brazilian_rosewood": "Jacarandá-da-bahia / palisandro de Brasil / palissandre du Brésil / palissandro brasiliano / Rio-Palisander|Palisander / Braziliaans palissander|palissander / brasiliansk palisander|palisander / brasiliansk palisander|palisander / Дальбергия чёрная|дальбергии",
    "mango": "Mangueira / mango / manguier / mango / Mangobaum|Mangobaum / mangoboom|mangoboom / mangoträd|mangoträds / mangotræ|mangotræ / Манго индийское|манго",
    "avocado": "Abacateiro / aguacate / avocatier / avocado / Avocadobaum|Avocadobaum / avocadoboom|avocadoboom / avokadoträd|avokadoträds / avocadotræ|avocadotræ / Авокадо|авокадо",
    "cashew": "Cajueiro / anacardo / anacardier / anacardio / Cashewbaum|Cashewbaum / cashewboom|cashewboom / cashewträd|cashewträds / cashewtræ|cashewtræ / Анакардиум|анакардиума",
    "orange": "Laranjeira / naranjo / oranger / arancio / Orangenbaum|Orangenbaum / sinaasappelboom|sinaasappelboom / apelsinträd|apelsinträds / appelsintræ|appelsintræ / Апельсин|апельсина",
    "lemon": "Limoeiro / limonero / citronnier / limone / Zitronenbaum|Zitronenbaum / citroenboom|citroenboom / citronträd|citronträds / citrontræ|citrontræ / Лимон|лимона",
    "papaya": "Mamoeiro / papayo / papayer / papaya / Papayabaum|Papaya / papajaboom|papaja / papayaträd|papaya / papayatræ|papaya / Папайя|папайи",
    "kapok": "Sumaúma / ceiba / fromager / kapok / Kapokbaum|Kapokbaum / kapokboom|kapokboom / kapokträd|kapokträds / kapoktræ|kapoktræ / Сейба|сейбы",
    "sweet_cherry": "Cerejeira-doce / cerezo dulce / merisier / ciliegio dolce / Süßkirsche|Kirsch / zoete kers|kersen / sötkörsbär|körsbärs / sødkirsebær|kirsebær / Черешня|черешни",
    "plum": "Ameixeira / ciruelo / prunier / susino / Pflaumenbaum|Pflaumenbaum / pruimenboom|pruimenboom / plommonträd|plommonträds / blommetræ|blommetræ / Слива домашняя|сливы",
    "peach": "Pessegueiro / melocotonero / pêcher / pesco / Pfirsichbaum|Pfirsichbaum / perzikboom|perzikboom / persikoträd|persikoträds / ferskentræ|ferskentræ / Персик|персика",
    "apricot": "Damasqueiro / albaricoquero / abricotier / albicocco / Aprikosenbaum|Aprikosenbaum / abrikozenboom|abrikozenboom / aprikosträd|aprikosträds / abrikostræ|abrikostræ / Абрикос|абрикоса",
    "fig": "Figueira / higuera / figuier / fico / Feigenbaum|Feigenbaum / vijgenboom|vijgenboom / fikonträd|fikonträds / figentræ|figentræ / Инжир|инжира",
    "pomegranate": "Romãzeira / granado / grenadier / melograno / Granatapfelbaum|Granatapfelbaum / granaatappelboom|granaatappelboom / granatäppelträd|granatäppelträds / granatæbletræ|granatæbletræ / Гранат|граната",
    "almond": "Amendoeira / almendro / amandier / mandorlo / Mandelbaum|Mandelbaum / amandelboom|amandelboom / mandelträd|mandelträds / mandeltræ|mandeltræ / Миндаль|миндаля",
    "pistachio": "Pistacheira / pistachero / pistachier / pistacchio / Pistazienbaum|Pistazienbaum / pistacheboom|pistacheboom / pistaschträd|pistaschträds / pistacietræ|pistacietræ / Фисташка|фисташки",
    "hazel": "Aveleira / avellano / noisetier / nocciolo / Haselnuss|Hasel / hazelaar|hazelaar / hassel|hassel / hassel|hassel / Лещина|лещины",
    "pecan": "Nogueira-pecã / pacano / pacanier / pecan / Pekannussbaum|Pekannuss / pecannotenboom|pecannoten / pekannötsträd|pekannöts / pekannøddetræ|pekannødde / Пекан|пекана",
    "plane_tree": "Plátano-oriental / plátano oriental / platane d'Orient / platano orientale / Morgenländische Platane|Platanen / oosterse plataan|plataan / orientalisk platan|platan / orientalsk platan|platan / Платан восточный|платана",
    "horse_chestnut": "Castanheiro-da-índia / castaño de Indias / marronnier / ippocastano / Rosskastanie|Rosskastanien / paardenkastanje|paardenkastanje / hästkastanj|hästkastanj / hestekastanje|hestekastanje / Конский каштан|конского каштана",
})

TEXT.update({
    "Parana Pine Seed": "Pinhão / Piñón de araucaria / Pignon d'araucaria / Pinolo di araucaria / Araukariensamen / Araucariazaad / Araukariafrö / Araukariefrø / Семя араукарии",
    "Mango": "Manga / Mango / Mangue / Mango / Mango / Mango / Mango / Mango / Манго",
    "Avocado": "Abacate / Aguacate / Avocat / Avocado / Avocado / Avocado / Avokado / Avocado / Авокадо",
    "Cashew": "Castanha-de-caju / Anacardo / Noix de cajou / Anacardo / Cashewnuss / Cashewnoot / Cashewnöt / Cashewnød / Орех кешью",
    "Orange": "Laranja / Naranja / Orange / Arancia / Orange / Sinaasappel / Apelsin / Appelsin / Апельсин",
    "Lemon": "Limão / Limón / Citron / Limone / Zitrone / Citroen / Citron / Citron / Лимон",
    "Papaya": "Mamão / Papaya / Papaye / Papaya / Papaya / Papaja / Papaya / Papaya / Папайя",
    "Kapok Fiber": "Paina / Fibra de ceiba / Kapok / Kapok / Kapokfaser / Kapokvezel / Kapokfiber / Kapokfiber / Капок",
    "Cherry": "Cereja / Cereza / Cerise / Ciliegia / Kirsche / Kers / Körsbär / Kirsebær / Черешня",
    "Plum": "Ameixa / Ciruela / Prune / Susina / Pflaume / Pruim / Plommon / Blomme / Слива",
    "Peach": "Pêssego / Melocotón / Pêche / Pesca / Pfirsich / Perzik / Persika / Fersken / Персик",
    "Apricot": "Damasco / Albaricoque / Abricot / Albicocca / Aprikose / Abrikoos / Aprikos / Abrikos / Абрикос",
    "Fig": "Figo / Higo / Figue / Fico / Feige / Vijg / Fikon / Figen / Инжир",
    "Pomegranate": "Romã / Granada / Grenade / Melagrana / Granatapfel / Granaatappel / Granatäpple / Granatæble / Гранат",
    "Almond": "Amêndoa / Almendra / Amande / Mandorla / Mandel / Amandel / Mandel / Mandel / Миндаль",
    "Pistachio": "Pistache / Pistacho / Pistache / Pistacchio / Pistazie / Pistache / Pistasch / Pistacie / Фисташка",
    "Hazelnut": "Avelã / Avellana / Noisette / Nocciola / Haselnuss / Hazelnoot / Hasselnöt / Hasselnød / Лесной орех",
    "Pecan": "Noz-pecã / Nuez pecana / Noix de pécan / Noce pecan / Pekannuss / Pecannoot / Pekannöt / Pekannød / Орех пекан",
    "Horse Chestnut": "Castanha-da-índia / Castaña de Indias / Marron d'Inde / Ippocastano / Rosskastanie / Paardenkastanje / Hästkastanj / Hestekastanje / Конский каштан",
    "Citric Acid": "Ácido Cítrico / Ácido cítrico / Acide citrique / Acido citrico / Zitronensäure / Citroenzuur / Citronsyra / Citronsyre / Лимонная кислота",
    "Sour acid of lemons: food, cleaning and dyeing": "Ácido do limão: comida, limpeza e tingimento / Ácido del limón: comida, limpieza y tinte / Acide du citron : cuisine, nettoyage, teinture / Acido del limone: cibo, pulizia e tintura / Säure der Zitrone: Essen, Putzen, Färben / Zuur van citroen: eten, schoonmaak, verven / Citronens syra: mat, rengöring, färgning / Citronens syre: mad, rengøring, farvning / Кислота лимона: еда, чистка, крашение",
    "Citric acid crystallizing": "Cristalização do ácido cítrico / Cristalización del ácido cítrico / Cristallisation de l'acide citrique / Cristallizzazione dell'acido citrico / Zitronensäurekristallisation / Citroenzuurkristallisatie / Citronsyrakristallisering / Citronsyrekrystallisering / Кристаллизация лимонной кислоты",
    "Lemon juice limed and set free with sulfuric acid: citric acid crystallizes (Scheele 1784)": "Suco de limão com cal, liberado com ácido sulfúrico: o ácido cítrico cristaliza (Scheele 1784) / Zumo de limón con cal, liberado con ácido sulfúrico: cristaliza el ácido cítrico (Scheele 1784) / Jus de citron chaulé puis libéré à l'acide sulfurique : l'acide citrique cristallise (Scheele 1784) / Succo di limone con calce, liberato con acido solforico: l'acido citrico cristallizza (Scheele 1784) / Zitronensaft mit Kalk gefällt und mit Schwefelsäure freigesetzt: Zitronensäure kristallisiert (Scheele 1784) / Citroensap met kalk gebonden en met zwavelzuur vrijgemaakt: citroenzuur kristalliseert (Scheele 1784) / Citronsaft fälld med kalk och frigjord med svavelsyra: citronsyra kristalliserar (Scheele 1784) / Citronsaft fældet med kalk og frigjort med svovlsyre: citronsyre krystalliserer (Scheele 1784) / Лимонный сок с известью, освобождённый серной кислотой: кристаллизуется лимонная кислота (Шееле, 1784)",
})

_GUIDE6 = {
    "pt_br": "Árvores frutíferas: mangueira, laranjeira, limoeiro, mamoeiro, abacateiro e cajueiro nos trópicos; cerejeira-doce, ameixeira, pessegueiro, damasqueiro, figueira, romãzeira, amendoeira, pistacheira, aveleira e nogueira-pecã nas terras temperadas. A araucária do sul do Brasil dá pinhão e resina. O limão traz ácido cítrico, que o Tanque de Digestão cristaliza. A castanha-da-índia não se come.",
    "es_es": "Árboles frutales: mango, naranjo, limonero, papayo, aguacate y anacardo en los trópicos; cerezo dulce, ciruelo, melocotonero, albaricoquero, higuera, granado, almendro, pistachero, avellano y pacano en las tierras templadas. La araucaria del sur de Brasil da piñones y resina. El limón lleva ácido cítrico, que la Cuba de digestión cristaliza. La castaña de Indias no se come.",
    "fr_fr": "Arbres fruitiers : manguier, oranger, citronnier, papayer, avocatier et anacardier sous les tropiques ; merisier, prunier, pêcher, abricotier, figuier, grenadier, amandier, pistachier, noisetier et pacanier dans les terres tempérées. Le pin du Paraná, au sud du Brésil, donne des graines et de la résine. Le citron contient de l'acide citrique, que la Cuve de digestion cristallise. Le marron d'Inde ne se mange pas.",
    "it_it": "Alberi da frutto: mango, arancio, limone, papaya, avocado e anacardio ai tropici; ciliegio dolce, susino, pesco, albicocco, fico, melograno, mandorlo, pistacchio, nocciolo e pecan nelle terre temperate. L'araucaria del Brasile meridionale dà semi e resina. Il limone contiene acido citrico, che la Vasca di digestione cristallizza. L'ippocastano non si mangia.",
    "de_de": "Obstbäume: Mango, Orange, Zitrone, Papaya, Avocado und Cashew in den Tropen; Süßkirsche, Pflaume, Pfirsich, Aprikose, Feige, Granatapfel, Mandel, Pistazie, Hasel und Pekannuss in den gemäßigten Ländern. Die Brasilianische Araukarie gibt essbare Samen und Harz. Zitronen enthalten Zitronensäure, die der Aufschlussbottich auskristallisiert. Rosskastanien isst man nicht.",
    "nl_nl": "Fruitbomen: mango, sinaasappel, citroen, papaja, avocado en cashew in de tropen; zoete kers, pruim, perzik, abrikoos, vijg, granaatappel, amandel, pistache, hazelaar en pecan in de gematigde streken. De Braziliaanse araucaria geeft eetbare zaden en hars. Citroenen bevatten citroenzuur, dat het Ontsluitingsvat laat kristalliseren. Paardenkastanjes eet je niet.",
    "sv_se": "Fruktträd: mango, apelsin, citron, papaya, avokado och cashew i tropikerna; sötkörsbär, plommon, persika, aprikos, fikon, granatäpple, mandel, pistasch, hassel och pekannöt i de tempererade länderna. Den brasilianska araukarian ger ätbara frön och kåda. Citroner bär citronsyra, som Uppslutningskaret kristalliserar fram. Hästkastanjer äter man inte.",
    "da_dk": "Frugttræer: mango, appelsin, citron, papaya, avocado og cashew i troperne; sødkirsebær, blomme, fersken, abrikos, figen, granatæble, mandel, pistacie, hassel og pekannød i de tempererede lande. Den brasilianske araukarie giver spiselige frø og harpiks. Citroner rummer citronsyre, som Oplukningskarret krystalliserer ud. Hestekastanjer spiser man ikke.",
    "ru_ru": "Плодовые деревья: манго, апельсин, лимон, папайя, авокадо и кешью в тропиках; черешня, слива, персик, абрикос, инжир, гранат, миндаль, фисташка, лещина и пекан в умеренных краях. Араукария юга Бразилии даёт съедобные семена и смолу. В лимоне есть лимонная кислота, которую кристаллизует чан для вскрытия. Конский каштан несъедобен.",
}
for _code, _p in _GUIDE6.items():
    GUIDE[_code].append(_p)
