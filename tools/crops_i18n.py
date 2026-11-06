"""Translations for tools/crops.py (3.23.0): every English string the crops add, in the order of CODES, and the
Field Guide chapter "Crops and Genetics" in each language (same paragraphs and headings as crops.GUIDE)."""
import crops as C

CODES = ("pt_br", "fr_fr", "es_es", "nl_nl", "it_it", "de_de", "sv_se", "da_dk", "ru_ru")

# English -> (pt_br, fr_fr, es_es, nl_nl, it_it, de_de, sv_se, da_dk, ru_ru)
S = {
    # substances, research, reactions
    "Nicotine": ("Nicotina", "Nicotine", "Nicotina", "Nicotine", "Nicotina", "Nikotin", "Nikotin", "Nikotin", "Никотин"),
    "The poison of tobacco: an old insecticide": (
        "O veneno do tabaco: um inseticida antigo", "Le poison du tabac : un vieil insecticide",
        "El veneno del tabaco: un insecticida antiguo", "Het gif van tabak: een oud insecticide",
        "Il veleno del tabacco: un vecchio insetticida", "Das Gift des Tabaks: ein altes Insektizid",
        "Tobakens gift: ett gammalt insektsmedel", "Tobakkens gift: et gammelt insektmiddel",
        "Яд табака: старинный инсектицид"),
    "Indigo": ("Anil", "Indigo", "Añil", "Indigo", "Indaco", "Indigo", "Indigo", "Indigo", "Индиго"),
    "The blue of jeans, from fermented indigo leaves": (
        "O azul do jeans, das folhas de anil fermentadas", "Le bleu du jean, tiré des feuilles d'indigotier fermentées",
        "El azul de los vaqueros, de hojas de añil fermentadas", "Het blauw van spijkerbroeken, uit gegiste indigobladeren",
        "Il blu dei jeans, dalle foglie d'indaco fermentate", "Das Blau der Jeans, aus vergorenen Indigoblättern",
        "Jeansens blå, ur jästa indigoblad", "Cowboybuksernes blå, af gærede indigoblade",
        "Синий цвет джинсов, из перебродивших листьев индигоферы"),
    "Domestication": ("Domesticação", "Domestication", "Domesticación", "Domesticatie", "Domesticazione",
                      "Domestizierung", "Domesticering", "Domesticering", "Одомашнивание"),
    "Keep the seeds of the best plants: the Seed Lens shows what traits a seed or a crop has": (
        "Guarde as sementes das melhores plantas: a Lupa de sementes mostra as características de uma semente ou lavoura",
        "Garder les graines des meilleures plantes : la Loupe à graines montre les caractères d'une graine ou d'une culture",
        "Guarda las semillas de las mejores plantas: la Lupa de semillas muestra los rasgos de una semilla o un cultivo",
        "Bewaar het zaad van de beste planten: de Zadenloep toont de eigenschappen van een zaad of gewas",
        "Conserva i semi delle piante migliori: la Lente per semi mostra i caratteri di un seme o di una coltura",
        "Behalte die Samen der besten Pflanzen: die Samenlupe zeigt die Merkmale eines Samens oder einer Feldfrucht",
        "Spara fröna från de bästa plantorna: Fröluppen visar vilka egenskaper ett frö eller en gröda har",
        "Gem frøene fra de bedste planter: Frøluppen viser hvilke egenskaber et frø eller en afgrøde har",
        "Сохраняй семена лучших растений: семенная лупа показывает признаки семени или посева"),
    "Animal Husbandry": ("Pastoreio", "Élevage", "Pastoreo", "Veeteelt", "Allevamento", "Viehzucht", "Djurhållning",
                         "Husdyrhold", "Скотоводство"),
    "Pen the herd and feed it: a Feeding Trough feeds the animals around it so they breed and grow": (
        "Cerque o rebanho e alimente-o: um Cocho alimenta os animais em volta para que procriem e cresçam",
        "Parquer le troupeau et le nourrir : une Mangeoire nourrit les animaux autour pour qu'ils se reproduisent et grandissent",
        "Encierra el rebaño y aliméntalo: un Comedero alimenta a los animales cercanos para que críen y crezcan",
        "Houd de kudde bijeen en voer haar: een Voerbak voert de dieren eromheen zodat ze kweken en groeien",
        "Recinta il gregge e nutrilo: una Mangiatoia sfama gli animali intorno perché si riproducano e crescano",
        "Halte die Herde zusammen und füttere sie: ein Futtertrog füttert die Tiere ringsum, damit sie sich vermehren und wachsen",
        "Håll hjorden inhägnad och utfodra den: ett Foderho matar djuren runt omkring så att de förökar sig och växer",
        "Hold flokken samlet og fodr den: en Foderkrybbe fodrer dyrene omkring, så de formerer sig og vokser",
        "Загони стадо и корми его: кормушка кормит животных вокруг, чтобы они размножались и росли"),
    "Crop Rotation": ("Rotação de culturas", "Rotation des cultures", "Rotación de cultivos", "Wisselbouw",
                      "Rotazione delle colture", "Fruchtwechsel", "Växtföljd", "Sædskifte", "Севооборот"),
    "Three-field rotation: soybeans, beans, indigo and alfalfa leave the ground fertilized when harvested": (
        "Rotação trienal: soja, feijão, anil e alfafa deixam a terra adubada quando colhidos",
        "Assolement triennal : soja, haricots, indigotier et luzerne laissent la terre fertilisée une fois récoltés",
        "Rotación trienal: soja, judías, añil y alfalfa dejan la tierra abonada al cosecharse",
        "Drieslagstelsel: sojabonen, bonen, indigo en luzerne laten de grond bemest achter na de oogst",
        "Rotazione triennale: soia, fagioli, indaco ed erba medica lasciano il terreno concimato quando raccolti",
        "Dreifelderwirtschaft: Sojabohnen, Bohnen, Indigo und Luzerne hinterlassen gedüngten Boden, wenn sie geerntet werden",
        "Tresäde: sojabönor, bönor, indigo och lusern lämnar jorden gödslad när de skördas",
        "Trevangsbrug: sojabønner, bønner, indigo og lucerne efterlader jorden gødet, når de høstes",
        "Трёхполье: соя, фасоль, индигофера и люцерна оставляют землю удобренной после уборки"),
    "Natural Dyes": ("Tinturaria natural", "Teintures naturelles", "Tintes naturales", "Natuurlijke kleurstoffen",
                     "Tinture naturali", "Naturfarbstoffe", "Naturliga färgämnen", "Naturlige farvestoffer",
                     "Природные красители"),
    "Blue from indigo, red from madder root: the Dye Vat dyes with plants": (
        "Azul do anil, vermelho da raiz de garança: a Dye Vat tinge com plantas",
        "Le bleu de l'indigotier, le rouge de la racine de garance : la cuve de teinture teint avec des plantes",
        "Azul del añil, rojo de la raíz de rubia: la cuba de tinte tiñe con plantas",
        "Blauw uit indigo, rood uit meekrapwortel: het verfvat verft met planten",
        "Blu dall'indaco, rosso dalla radice di robbia: il tino da tintura tinge con le piante",
        "Blau aus Indigo, Rot aus Krappwurzel: der Färbebottich färbt mit Pflanzen",
        "Blått ur indigo, rött ur krapprot: färgkaret färgar med växter",
        "Blåt af indigo, rødt af kraprod: farvekarret farver med planter",
        "Синий из индигоферы, красный из корня марены: красильный чан красит растениями"),
    "Plant Hybridization": ("Hibridação de plantas", "Hybridation des plantes", "Hibridación de plantas",
                            "Plantenkruising", "Ibridazione delle piante", "Pflanzenkreuzung", "Växtkorsning",
                            "Plantekrydsning", "Гибридизация растений"),
    "Kölreuter crossed tobacco plants by hand: the Pollination Brush chooses the father of a plant's seeds": (
        "Kölreuter cruzou pés de tabaco à mão: o Pincel de polinização escolhe o pai das sementes de uma planta",
        "Kölreuter croisa des plants de tabac à la main : le Pinceau de pollinisation choisit le père des graines d'une plante",
        "Kölreuter cruzó plantas de tabaco a mano: el Pincel de polinización elige el padre de las semillas de una planta",
        "Kölreuter kruiste tabaksplanten met de hand: het Bestuivingspenseel kiest de vader van de zaden van een plant",
        "Kölreuter incrociò a mano piante di tabacco: il Pennello impollinatore sceglie il padre dei semi di una pianta",
        "Kölreuter kreuzte Tabakpflanzen von Hand: der Bestäubungspinsel wählt den Vater der Samen einer Pflanze",
        "Kölreuter korsade tobaksplantor för hand: Pollineringspenseln väljer fadern till en plantas frön",
        "Kölreuter krydsede tobaksplanter i hånden: Bestøvningspenslen vælger faderen til en plantes frø",
        "Кёльрейтер скрещивал табак вручную: кисть для опыления выбирает отца семян растения"),
    "Tobacco Wash": ("Calda de fumo", "Décoction de tabac", "Caldo de tabaco", "Tabakswater", "Infuso di tabacco",
                     "Tabaksud", "Tobaksvatten", "Tobaksvand", "Табачный настой"),
    "Tobacco steeped in water kills caterpillars on crops: the first insecticide of the farm": (
        "Fumo de molho na água mata as lagartas da lavoura: o primeiro inseticida da roça",
        "Le tabac infusé dans l'eau tue les chenilles des cultures : le premier insecticide de la ferme",
        "El tabaco en remojo mata las orugas de los cultivos: el primer insecticida de la granja",
        "Tabak geweekt in water doodt rupsen op gewassen: het eerste insecticide van de boerderij",
        "Il tabacco in infusione uccide i bruchi sulle colture: il primo insetticida della fattoria",
        "In Wasser eingeweichter Tabak tötet Raupen auf Feldern: das erste Insektizid des Hofes",
        "Tobak som dragit i vatten dödar larver på grödor: gårdens första insektsmedel",
        "Tobak udblødt i vand dræber larver på afgrøder: gårdens første insektmiddel",
        "Табак, настоянный на воде, убивает гусениц на посевах: первый инсектицид фермы"),
    "Mendel's Laws": ("Leis de Mendel", "Lois de Mendel", "Leyes de Mendel", "Wetten van Mendel", "Leggi di Mendel",
                      "Mendelsche Regeln", "Mendels lagar", "Mendels love", "Законы Менделя"),
    "Mendel counted his peas: each trait is two factors, one from each parent. The Seed Lens shows the genes themselves": (
        "Mendel contou suas ervilhas: cada característica são dois fatores, um de cada pai. A Lupa de sementes mostra os próprios genes",
        "Mendel compta ses pois : chaque caractère tient à deux facteurs, un de chaque parent. La Loupe à graines montre les gènes eux-mêmes",
        "Mendel contó sus guisantes: cada rasgo son dos factores, uno de cada progenitor. La Lupa de semillas muestra los propios genes",
        "Mendel telde zijn erwten: elke eigenschap bestaat uit twee factoren, één van elke ouder. De Zadenloep toont de genen zelf",
        "Mendel contò i suoi piselli: ogni carattere è dato da due fattori, uno per genitore. La Lente per semi mostra i geni stessi",
        "Mendel zählte seine Erbsen: jedes Merkmal sind zwei Faktoren, einer von jedem Elternteil. Die Samenlupe zeigt die Gene selbst",
        "Mendel räknade sina ärtor: varje egenskap är två faktorer, en från varje förälder. Fröluppen visar själva generna",
        "Mendel talte sine ærter: hver egenskab er to faktorer, en fra hver forælder. Frøluppen viser selve generne",
        "Мендель считал свой горох: каждый признак это два фактора, по одному от каждого родителя. Семенная лупа показывает сами гены"),
    "Indigo dyeing": ("Tingimento com anil", "Teinture à l'indigo", "Tinte con añil", "Verven met indigo",
                      "Tintura con indaco", "Färben mit Indigo", "Färgning med indigo", "Farvning med indigo",
                      "Крашение индиго"),
    "Indigo leaves steeped and fermented in the vat; the cloth turns blue in the air: 32 blue dyes to a flask's worth": (
        "Folhas de anil de molho e fermentadas na tina; o pano fica azul ao ar: 32 corantes azuis por frasco",
        "Feuilles d'indigotier macérées et fermentées dans la cuve ; le tissu bleuit à l'air : 32 colorants bleus par flacon",
        "Hojas de añil maceradas y fermentadas en la cuba; la tela se vuelve azul al aire: 32 tintes azules por frasco",
        "Indigobladeren geweekt en gegist in het vat; de stof wordt blauw aan de lucht: 32 blauwe kleurstoffen per fles",
        "Foglie d'indaco macerate e fermentate nel tino; il tessuto diventa blu all'aria: 32 coloranti blu per fiasca",
        "Indigoblätter im Bottich eingeweicht und vergoren; das Tuch wird an der Luft blau: 32 blaue Farbstoffe je Flasche",
        "Indigoblad blötläggs och jäses i karet; tyget blir blått i luften: 32 blå färgämnen per flaska",
        "Indigoblade udblødes og gæres i karret; stoffet bliver blåt i luften: 32 blå farvestoffer pr. flaske",
        "Листья индигоферы замачивают и сбраживают в чане; ткань синеет на воздухе: 32 синих красителя на колбу"),
    "Madder dyeing": ("Tingimento com garança", "Teinture à la garance", "Tinte con rubia", "Verven met meekrap",
                      "Tintura con robbia", "Färben mit Krapp", "Färgning med krapp", "Farvning med krap",
                      "Крашение мареной"),
    "Madder root ground and boiled with alum: the old Turkey red: 32 red dyes to a flask's worth": (
        "Raiz de garança moída e fervida com alúmen: o velho vermelho-turco: 32 corantes vermelhos por frasco",
        "Racine de garance moulue et bouillie avec de l'alun : le vieux rouge turc : 32 colorants rouges par flacon",
        "Raíz de rubia molida y hervida con alumbre: el viejo rojo turco: 32 tintes rojos por frasco",
        "Meekrapwortel gemalen en gekookt met aluin: het oude Turks rood: 32 rode kleurstoffen per fles",
        "Radice di robbia macinata e bollita con allume: il vecchio rosso turco: 32 coloranti rossi per fiasca",
        "Krappwurzel gemahlen und mit Alaun gekocht: das alte Türkischrot: 32 rote Farbstoffe je Flasche",
        "Krapprot mald och kokt med alun: det gamla turkiskt rött: 32 röda färgämnen per flaska",
        "Kraprod malet og kogt med alun: det gamle tyrkisk rød: 32 røde farvestoffer pr. flaske",
        "Корень марены, молотый и сваренный с квасцами: старый турецкий красный: 32 красных красителя на колбу"),
    # crops
    "Rice Plants": ("Pés de arroz", "Plants de riz", "Plantas de arroz", "Rijstplanten", "Piante di riso", "Reispflanzen",
                    "Risplantor", "Risplanter", "Рис (посев)"),
    "Rice": ("Arroz", "Riz", "Arroz", "Rijst", "Riso", "Reis", "Ris", "Ris", "Рис"),
    "Maize Plants": ("Pés de milho", "Plants de maïs", "Plantas de maíz", "Maïsplanten", "Piante di mais",
                     "Maispflanzen", "Majsplantor", "Majsplanter", "Кукуруза (посев)"),
    "Maize": ("Milho", "Maïs", "Maíz", "Maïs", "Mais", "Mais", "Majs", "Majs", "Кукуруза"),
    "Maize Kernels": ("Grãos de milho", "Grains de maïs", "Granos de maíz", "Maïskorrels", "Chicchi di mais",
                      "Maiskörner", "Majskorn", "Majskerner", "Зёрна кукурузы"),
    "Soybean Plants": ("Pés de soja", "Plants de soja", "Plantas de soja", "Sojaplanten", "Piante di soia",
                       "Sojapflanzen", "Sojaplantor", "Sojaplanter", "Соя (посев)"),
    "Soybean": ("Soja", "Soja", "Soja", "Soja", "Soia", "Soja", "Soja", "Soja", "Соя"),
    "Soybeans": ("Grãos de soja", "Graines de soja", "Granos de soja", "Sojabonen", "Semi di soia", "Sojabohnen",
                 "Sojabönor", "Sojabønner", "Соевые бобы"),
    "Bean Plants": ("Pés de feijão", "Plants de haricot", "Plantas de judía", "Bonenplanten", "Piante di fagiolo",
                    "Bohnenpflanzen", "Bönplantor", "Bønneplanter", "Фасоль (посев)"),
    "Common Bean": ("Feijão", "Haricot", "Judía", "Boon", "Fagiolo", "Gartenbohne", "Böna", "Bønne", "Фасоль"),
    "Beans": ("Feijão", "Haricots", "Judías", "Bonen", "Fagioli", "Bohnen", "Bönor", "Bønner", "Фасоль"),
    "Cassava Plants": ("Pés de mandioca", "Plants de manioc", "Plantas de yuca", "Cassaveplanten", "Piante di manioca",
                       "Maniokpflanzen", "Kassavaplantor", "Kassavaplanter", "Маниок (посев)"),
    "Cassava": ("Mandioca", "Manioc", "Yuca", "Cassave", "Manioca", "Maniok", "Kassava", "Kassava", "Маниок"),
    "Cassava Stem": ("Rama de mandioca", "Bouture de manioc", "Estaca de yuca", "Cassavestek", "Talea di manioca",
                     "Maniokstängel", "Kassavastickling", "Kassavastikling", "Черенок маниока"),
    "Cassava Root": ("Raiz de mandioca", "Racine de manioc", "Raíz de yuca", "Cassavewortel", "Radice di manioca",
                     "Maniokwurzel", "Kassavarot", "Kassavarod", "Корень маниока"),
    "Barley Plants": ("Pés de cevada", "Plants d'orge", "Plantas de cebada", "Gerstplanten", "Piante d'orzo",
                      "Gerstenpflanzen", "Kornplantor", "Bygplanter", "Ячмень (посев)"),
    "Barley": ("Cevada", "Orge", "Cebada", "Gerst", "Orzo", "Gerste", "Korn", "Byg", "Ячмень"),
    "Tomato Plants": ("Pés de tomate", "Plants de tomate", "Plantas de tomate", "Tomatenplanten", "Piante di pomodoro",
                      "Tomatenpflanzen", "Tomatplantor", "Tomatplanter", "Томат (посев)"),
    "Tomato": ("Tomate", "Tomate", "Tomate", "Tomaat", "Pomodoro", "Tomate", "Tomat", "Tomat", "Помидор"),
    "Tomato Seeds": ("Sementes de tomate", "Graines de tomate", "Semillas de tomate", "Tomatenzaden", "Semi di pomodoro",
                     "Tomatensamen", "Tomatfrön", "Tomatfrø", "Семена томата"),
    "Coffee Bush": ("Pé de café", "Caféier", "Cafeto", "Koffiestruik", "Pianta di caffè", "Kaffeestrauch",
                    "Kaffebuske", "Kaffebusk", "Кофейный куст"),
    "Coffee": ("Café", "Café", "Café", "Koffie", "Caffè", "Kaffee", "Kaffe", "Kaffe", "Кофе"),
    "Coffee Beans": ("Grãos de café", "Grains de café", "Granos de café", "Koffiebonen", "Chicchi di caffè",
                     "Kaffeebohnen", "Kaffebönor", "Kaffebønner", "Кофейные зёрна"),
    "Cotton Plants": ("Pés de algodão", "Plants de coton", "Plantas de algodón", "Katoenplanten", "Piante di cotone",
                      "Baumwollpflanzen", "Bomullsplantor", "Bomuldsplanter", "Хлопчатник (посев)"),
    "Cotton": ("Algodão", "Coton", "Algodón", "Katoen", "Cotone", "Baumwolle", "Bomull", "Bomuld", "Хлопок"),
    "Cotton Seeds": ("Caroços de algodão", "Graines de coton", "Semillas de algodón", "Katoenzaden", "Semi di cotone",
                     "Baumwollsamen", "Bomullsfrön", "Bomuldsfrø", "Семена хлопчатника"),
    "Flax Plants": ("Pés de linho", "Plants de lin", "Plantas de lino", "Vlasplanten", "Piante di lino",
                    "Flachspflanzen", "Linplantor", "Horplanter", "Лён (посев)"),
    "Flax": ("Linho", "Lin", "Lino", "Vlas", "Lino", "Flachs", "Lin", "Hør", "Лён"),
    "Linseed": ("Linhaça", "Graines de lin", "Linaza", "Lijnzaad", "Semi di lino", "Leinsamen", "Linfrö", "Hørfrø",
                "Льняное семя"),
    "Hemp Plants": ("Pés de cânhamo", "Plants de chanvre", "Plantas de cáñamo", "Hennepplanten", "Piante di canapa",
                    "Hanfpflanzen", "Hampaplantor", "Hampeplanter", "Конопля (посев)"),
    "Hemp": ("Cânhamo", "Chanvre", "Cáñamo", "Hennep", "Canapa", "Hanf", "Hampa", "Hamp", "Конопля"),
    "Hemp Seeds": ("Sementes de cânhamo", "Chènevis", "Semillas de cáñamo", "Hennepzaad", "Semi di canapa", "Hanfsamen",
                   "Hampfrön", "Hampefrø", "Семена конопли"),
    "Rapeseed Plants": ("Pés de colza", "Plants de colza", "Plantas de colza", "Koolzaadplanten", "Piante di colza",
                        "Rapspflanzen", "Rapsplantor", "Rapsplanter", "Рапс (посев)"),
    "Rapeseed": ("Colza", "Colza", "Colza", "Koolzaad", "Colza", "Raps", "Raps", "Raps", "Рапс"),
    "Castor Plants": ("Pés de mamona", "Plants de ricin", "Plantas de ricino", "Wonderboomplanten", "Piante di ricino",
                      "Rizinuspflanzen", "Ricinplantor", "Ricinusplanter", "Клещевина (посев)"),
    "Castor": ("Mamona", "Ricin", "Ricino", "Wonderboom", "Ricino", "Rizinus", "Ricin", "Ricinus", "Клещевина"),
    "Castor Beans": ("Bagas de mamona", "Graines de ricin", "Semillas de ricino", "Wonderboonzaden", "Semi di ricino",
                     "Rizinussamen", "Ricinfrön", "Ricinusfrø", "Семена клещевины"),
    "Indigo Plants": ("Pés de anil", "Plants d'indigotier", "Plantas de añil", "Indigoplanten", "Piante d'indaco",
                      "Indigopflanzen", "Indigoplantor", "Indigoplanter", "Индигофера (посев)"),
    "Indigo Seeds": ("Sementes de anil", "Graines d'indigotier", "Semillas de añil", "Indigozaden", "Semi d'indaco",
                     "Indigosamen", "Indigofrön", "Indigofrø", "Семена индигоферы"),
    "Indigo Leaves": ("Folhas de anil", "Feuilles d'indigotier", "Hojas de añil", "Indigobladeren", "Foglie d'indaco",
                      "Indigoblätter", "Indigoblad", "Indigoblade", "Листья индигоферы"),
    "Madder Plants": ("Pés de garança", "Plants de garance", "Plantas de rubia", "Meekrapplanten", "Piante di robbia",
                      "Krapppflanzen", "Krapplantor", "Krapplanter", "Марена (посев)"),
    "Madder": ("Garança", "Garance", "Rubia", "Meekrap", "Robbia", "Krapp", "Krapp", "Krap", "Марена"),
    "Madder Seeds": ("Sementes de garança", "Graines de garance", "Semillas de rubia", "Meekrapzaden", "Semi di robbia",
                     "Krappsamen", "Krappfrön", "Krapfrø", "Семена марены"),
    "Madder Root": ("Raiz de garança", "Racine de garance", "Raíz de rubia", "Meekrapwortel", "Radice di robbia",
                    "Krappwurzel", "Krapprot", "Kraprod", "Корень марены"),
    "Tobacco Plants": ("Pés de fumo", "Plants de tabac", "Plantas de tabaco", "Tabaksplanten", "Piante di tabacco",
                       "Tabakpflanzen", "Tobaksplantor", "Tobaksplanter", "Табак (посев)"),
    "Tobacco": ("Tabaco", "Tabac", "Tabaco", "Tabak", "Tabacco", "Tabak", "Tobak", "Tobak", "Табак"),
    "Tobacco Seeds": ("Sementes de fumo", "Graines de tabac", "Semillas de tabaco", "Tabakszaden", "Semi di tabacco",
                      "Tabaksamen", "Tobaksfrön", "Tobaksfrø", "Семена табака"),
    "Tobacco Leaves": ("Folhas de fumo", "Feuilles de tabac", "Hojas de tabaco", "Tabaksbladeren", "Foglie di tabacco",
                       "Tabakblätter", "Tobaksblad", "Tobaksblade", "Листья табака"),
    "Alfalfa Plants": ("Pés de alfafa", "Plants de luzerne", "Plantas de alfalfa", "Luzerneplanten",
                       "Piante di erba medica", "Luzernepflanzen", "Lusernplantor", "Lucerneplanter", "Люцерна (посев)"),
    "Alfalfa": ("Alfafa", "Luzerne", "Alfalfa", "Luzerne", "Erba medica", "Luzerne", "Lusern", "Lucerne", "Люцерна"),
    "Alfalfa Seeds": ("Sementes de alfafa", "Graines de luzerne", "Semillas de alfalfa", "Luzernezaad",
                      "Semi di erba medica", "Luzernesamen", "Lusernfrön", "Lucernefrø", "Семена люцерны"),
    "Sorghum Plants": ("Pés de sorgo", "Plants de sorgho", "Plantas de sorgo", "Sorghumplanten", "Piante di sorgo",
                       "Hirsepflanzen", "Durraplantor", "Durraplanter", "Сорго (посев)"),
    "Sorghum": ("Sorgo", "Sorgho", "Sorgo", "Sorghum", "Sorgo", "Sorghumhirse", "Durra", "Durra", "Сорго"),
    "Oat Plants": ("Pés de aveia", "Plants d'avoine", "Plantas de avena", "Haverplanten", "Piante d'avena",
                   "Haferpflanzen", "Havreplantor", "Havreplanter", "Овёс (посев)"),
    "Oats": ("Aveia", "Avoine", "Avena", "Haver", "Avena", "Hafer", "Havre", "Havre", "Овёс"),
    "Turnip Plants": ("Pés de nabo", "Plants de navet", "Plantas de nabo", "Raapplanten", "Piante di rapa",
                      "Rübenpflanzen", "Rovplantor", "Roeplanter", "Турнепс (посев)"),
    "Fodder Turnip": ("Nabo forrageiro", "Navet fourrager", "Nabo forrajero", "Stoppelknol", "Rapa da foraggio",
                      "Futterrübe", "Foderrova", "Foderroe", "Кормовой турнепс"),
    "Turnip Seeds": ("Sementes de nabo", "Graines de navet", "Semillas de nabo", "Raapzaad", "Semi di rapa",
                     "Rübensamen", "Rovfrön", "Roefrø", "Семена турнепса"),
    "Turnip": ("Nabo", "Navet", "Nabo", "Raap", "Rapa", "Rübe", "Rova", "Roe", "Турнепс"),
    "Cooked Rice": ("Arroz cozido", "Riz cuit", "Arroz cocido", "Gekookte rijst", "Riso cotto", "Gekochter Reis",
                    "Kokt ris", "Kogte ris", "Варёный рис"),
    "Cooked Beans": ("Feijão cozido", "Haricots cuits", "Judías cocidas", "Gekookte bonen", "Fagioli cotti",
                     "Gekochte Bohnen", "Kokta bönor", "Kogte bønner", "Варёная фасоль"),
    "Cassava Flour": ("Farinha de mandioca", "Farine de manioc", "Harina de yuca", "Cassavemeel", "Farina di manioca",
                      "Maniokmehl", "Kassavamjöl", "Kassavamel", "Мука из маниока"),
    "Seed Lens": ("Lupa de sementes", "Loupe à graines", "Lupa de semillas", "Zadenloep", "Lente per semi", "Samenlupe",
                  "Frölupp", "Frølup", "Семенная лупа"),
    "Pollination Brush": ("Pincel de polinização", "Pinceau de pollinisation", "Pincel de polinización",
                          "Bestuivingspenseel", "Pennello impollinatore", "Bestäubungspinsel", "Pollineringspensel",
                          "Bestøvningspensel", "Кисть для опыления"),
    "Feeding Trough": ("Cocho", "Mangeoire", "Comedero", "Voerbak", "Mangiatoia", "Futtertrog", "Foderho",
                       "Foderkrybbe", "Кормушка"),
    # interface
    "Climate: suits it": ("Clima: adequado", "Climat : lui convient", "Clima: le conviene", "Klimaat: geschikt",
                          "Clima: adatto", "Klima: passt", "Klimat: passar", "Klima: passer", "Климат: подходит"),
    "Climate: wrong for it, grows at %s%% speed": (
        "Clima: inadequado, cresce a %s%% da velocidade", "Climat : ne lui convient pas, pousse à %s%% de sa vitesse",
        "Clima: no le conviene, crece al %s%% de velocidad", "Klimaat: ongeschikt, groeit op %s%% snelheid",
        "Clima: non adatto, cresce al %s%% della velocità", "Klima: unpassend, wächst mit %s%% Tempo",
        "Klimat: fel, växer i %s%% fart", "Klima: forkert, vokser med %s%% fart",
        "Климат: неподходящий, растёт на %s%% скорости"),
    "Rice: needs water next to its farmland to grow": (
        "Arroz: precisa de água ao lado da terra arada para crescer",
        "Riz : il lui faut de l'eau à côté de sa terre labourée pour pousser",
        "Arroz: necesita agua junto a su tierra de cultivo para crecer",
        "Rijst: heeft water naast zijn akkerland nodig om te groeien",
        "Riso: ha bisogno d'acqua accanto al terreno arato per crescere",
        "Reis: braucht Wasser neben seinem Ackerboden, um zu wachsen",
        "Ris: behöver vatten intill sin åkerjord för att växa",
        "Ris: skal have vand ved siden af sin landbrugsjord for at vokse",
        "Рис: чтобы расти, ему нужна вода рядом с пашней"),
    "Caterpillars! It has stopped growing": (
        "Lagartas! Parou de crescer", "Des chenilles ! Elle ne pousse plus", "¡Orugas! Ha dejado de crecer",
        "Rupsen! Hij groeit niet meer", "Bruchi! Ha smesso di crescere", "Raupen! Sie wächst nicht mehr",
        "Larver! Den har slutat växa", "Larver! Den er holdt op med at vokse", "Гусеницы! Растение перестало расти"),
    "Rust! It has stopped growing": (
        "Ferrugem! Parou de crescer", "De la rouille ! Elle ne pousse plus", "¡Roya! Ha dejado de crecer",
        "Roest! Hij groeit niet meer", "Ruggine! Ha smesso di crescere", "Rost! Sie wächst nicht mehr",
        "Rost! Den har slutat växa", "Rust! Den er holdt op med at vokse", "Ржавчина! Растение перестало расти"),
    "Healthy": ("Saudável", "Saine", "Sana", "Gezond", "Sana", "Gesund", "Frisk", "Sund", "Здорово"),
    "Pollinated by a neighbour": ("Polinizada por uma vizinha", "Pollinisée par une voisine", "Polinizada por una vecina",
                                  "Bestoven door een buur", "Impollinata da una vicina", "Von einer Nachbarin bestäubt",
                                  "Pollinerad av en granne", "Bestøvet af en nabo", "Опылено соседом"),
    "Pollinated by itself": ("Autopolinizada", "Autopollinisée", "Autopolinizada", "Door zichzelf bestoven",
                             "Autoimpollinata", "Selbst bestäubt", "Självpollinerad", "Selvbestøvet", "Самоопыление"),
    "Pollinated by hand": ("Polinizada à mão", "Pollinisée à la main", "Polinizada a mano", "Met de hand bestoven",
                           "Impollinata a mano", "Von Hand bestäubt", "Handpollinerad", "Håndbestøvet",
                           "Опылено вручную"),
    "Not yet pollinated": ("Ainda não polinizada", "Pas encore pollinisée", "Aún sin polinizar", "Nog niet bestoven",
                           "Non ancora impollinata", "Noch nicht bestäubt", "Inte pollinerad än", "Endnu ikke bestøvet",
                           "Ещё не опылено"),
    "Wild plant": ("Planta silvestre", "Plante sauvage", "Planta silvestre", "Wilde plant", "Pianta selvatica",
                   "Wildpflanze", "Vild växt", "Vild plante", "Дикое растение"),
    "Traits: %s": ("Características: %s", "Caractères : %s", "Rasgos: %s", "Eigenschappen: %s", "Caratteri: %s",
                   "Merkmale: %s", "Egenskaper: %s", "Egenskaber: %s", "Признаки: %s"),
    "Traits: nothing out of the ordinary": (
        "Características: nada fora do comum", "Caractères : rien d'inhabituel", "Rasgos: nada fuera de lo común",
        "Eigenschappen: niets bijzonders", "Caratteri: niente di insolito", "Merkmale: nichts Besonderes",
        "Egenskaper: inget ovanligt", "Egenskaber: intet usædvanligt", "Признаки: ничего необычного"),
    "Genotype: %s": ("Genótipo: %s", "Génotype : %s", "Genotipo: %s", "Genotype: %s", "Genotipo: %s", "Genotyp: %s",
                     "Genotyp: %s", "Genotype: %s", "Генотип: %s"),
    "Grown from a cutting: a copy of its parent": (
        "Plantada de rama: uma cópia da mãe", "Issue d'une bouture : une copie de son parent",
        "Plantada de estaca: una copia de su progenitor", "Uit een stek gekweekt: een kopie van zijn ouder",
        "Cresciuta da talea: una copia del genitore", "Aus einem Steckling: eine Kopie der Mutterpflanze",
        "Odlad från stickling: en kopia av föräldern", "Dyrket fra stikling: en kopi af forælderen",
        "Выращено из черенка: копия родителя"),
    "You do not know what to look for yet (Domestication, level 1)": (
        "Você ainda não sabe o que procurar (Domesticação, nível 1)",
        "Vous ne savez pas encore quoi chercher (Domestication, niveau 1)",
        "Aún no sabes qué buscar (Domesticación, nivel 1)",
        "Je weet nog niet waar je op moet letten (Domesticatie, niveau 1)",
        "Non sai ancora cosa cercare (Domesticazione, livello 1)",
        "Du weißt noch nicht, worauf du achten sollst (Domestizierung, Stufe 1)",
        "Du vet inte vad du ska leta efter än (Domesticering, nivå 1)",
        "Du ved endnu ikke, hvad du skal kigge efter (Domesticering, niveau 1)",
        "Ты пока не знаешь, на что смотреть (Одомашнивание, уровень 1)"),
    "Hold seeds in your other hand, or use the lens on a crop": (
        "Segure sementes na outra mão ou use a lupa numa lavoura",
        "Tenez des graines dans l'autre main, ou utilisez la loupe sur une culture",
        "Sostén semillas en la otra mano o usa la lupa en un cultivo",
        "Houd zaden in je andere hand, of gebruik de loep op een gewas",
        "Tieni dei semi nell'altra mano, o usa la lente su una coltura",
        "Halte Samen in der anderen Hand oder benutze die Lupe an einer Pflanze",
        "Håll frön i andra handen, eller använd luppen på en gröda",
        "Hold frø i den anden hånd, eller brug luppen på en afgrøde",
        "Держи семена в другой руке или примени лупу к посеву"),
    "Analysed %s seeds": ("%s sementes analisadas", "%s graines analysées", "%s semillas analizadas", "%s zaden onderzocht",
                          "%s semi analizzati", "%s Samen untersucht", "%s frön analyserade", "%s frø analyseret",
                          "Изучено семян: %s"),
    "You do not know how to cross plants by hand yet (Plant Hybridization, level 3)": (
        "Você ainda não sabe cruzar plantas à mão (Hibridação de plantas, nível 3)",
        "Vous ne savez pas encore croiser des plantes à la main (Hybridation des plantes, niveau 3)",
        "Aún no sabes cruzar plantas a mano (Hibridación de plantas, nivel 3)",
        "Je weet nog niet hoe je planten met de hand kruist (Plantenkruising, niveau 3)",
        "Non sai ancora incrociare le piante a mano (Ibridazione delle piante, livello 3)",
        "Du weißt noch nicht, wie man Pflanzen von Hand kreuzt (Pflanzenkreuzung, Stufe 3)",
        "Du vet inte hur man korsar växter för hand än (Växtkorsning, nivå 3)",
        "Du ved endnu ikke, hvordan man krydser planter i hånden (Plantekrydsning, niveau 3)",
        "Ты пока не умеешь скрещивать растения вручную (Гибридизация растений, уровень 3)"),
    "Pollen collected from %s": ("Pólen colhido de %s", "Pollen récolté sur %s", "Polen recogido de %s",
                                 "Stuifmeel verzameld van %s", "Polline raccolto da %s", "Pollen von %s gesammelt",
                                 "Pollen insamlat från %s", "Pollen indsamlet fra %s", "Пыльца собрана: %s"),
    "Pollinated by hand with pollen of %s": (
        "Polinizada à mão com pólen de %s", "Pollinisée à la main avec du pollen de %s",
        "Polinizada a mano con polen de %s", "Met de hand bestoven met stuifmeel van %s",
        "Impollinata a mano con polline di %s", "Von Hand mit Pollen von %s bestäubt",
        "Handpollinerad med pollen från %s", "Håndbestøvet med pollen fra %s", "Опылено вручную пыльцой: %s"),
    "Only a ripe plant gives pollen": (
        "Só uma planta madura dá pólen", "Seule une plante mûre donne du pollen", "Solo una planta madura da polen",
        "Alleen een rijpe plant geeft stuifmeel", "Solo una pianta matura dà polline", "Nur eine reife Pflanze gibt Pollen",
        "Bara en mogen växt ger pollen", "Kun en moden plante giver pollen", "Пыльцу даёт только зрелое растение"),
    "This pollen is from %s: it will not take on another kind of plant": (
        "Este pólen é de %s: não pega em outra espécie",
        "Ce pollen vient de %s : il ne prendra pas sur une autre plante",
        "Este polen es de %s: no prenderá en otra especie",
        "Dit stuifmeel is van %s: het slaat niet aan op een andere soort",
        "Questo polline è di %s: non attecchisce su un'altra specie",
        "Dieser Pollen stammt von %s: er wirkt nicht bei einer anderen Art",
        "Detta pollen kommer från %s: det tar inte på en annan sorts växt",
        "Dette pollen er fra %s: det tager ikke på en anden slags plante",
        "Это пыльца от: %s; на другом виде она не приживётся"),
    "You do not know this remedy yet (Tobacco Wash, level 3)": (
        "Você ainda não conhece este remédio (Calda de fumo, nível 3)",
        "Vous ne connaissez pas encore ce remède (Décoction de tabac, niveau 3)",
        "Aún no conoces este remedio (Caldo de tabaco, nivel 3)",
        "Je kent dit middel nog niet (Tabakswater, niveau 3)",
        "Non conosci ancora questo rimedio (Infuso di tabacco, livello 3)",
        "Du kennst dieses Mittel noch nicht (Tabaksud, Stufe 3)",
        "Du känner inte till det här medlet än (Tobaksvatten, nivå 3)",
        "Du kender endnu ikke dette middel (Tobaksvand, niveau 3)",
        "Ты пока не знаешь этого средства (Табачный настой, уровень 3)"),
    "Caterpillars washed off %s plants": (
        "Lagartas lavadas de %s plantas", "Chenilles lavées de %s plantes", "Orugas lavadas de %s plantas",
        "Rupsen van %s planten gewassen", "Bruchi lavati via da %s piante", "Raupen von %s Pflanzen abgewaschen",
        "Larver tvättade av %s plantor", "Larver vasket af %s planter", "Гусеницы смыты с растений: %s"),
    "Caterpillars killed on %s plants": (
        "Lagartas mortas em %s plantas", "Chenilles tuées sur %s plantes", "Orugas eliminadas en %s plantas",
        "Rupsen gedood op %s planten", "Bruchi uccisi su %s piante", "Raupen auf %s Pflanzen getötet",
        "Larver dödade på %s plantor", "Larver dræbt på %s planter", "Гусеницы уничтожены на растениях: %s"),
    "Feeding Trough: %s %s": ("Cocho: %s %s", "Mangeoire : %s %s", "Comedero: %s %s", "Voerbak: %s %s",
                              "Mangiatoia: %s %s", "Futtertrog: %s %s", "Foderho: %s %s", "Foderkrybbe: %s %s",
                              "Кормушка: %s %s"),
    "Feeding Trough: empty; use feed on it to fill it": (
        "Cocho: vazio; use ração nele para enchê-lo",
        "Mangeoire : vide ; utilisez du fourrage dessus pour la remplir",
        "Comedero: vacío; usa pienso en él para llenarlo",
        "Voerbak: leeg; gebruik voer erop om hem te vullen",
        "Mangiatoia: vuota; usa del mangime su di essa per riempirla",
        "Futtertrog: leer; benutze Futter daran, um ihn zu füllen",
        "Foderho: tomt; använd foder på det för att fylla det",
        "Foderkrybbe: tom; brug foder på den for at fylde den",
        "Кормушка: пусто; примени к ней корм, чтобы наполнить"),
    "The trough holds only one kind of feed": (
        "O cocho guarda um só tipo de ração", "La mangeoire ne contient qu'une sorte de fourrage",
        "El comedero solo admite un tipo de pienso", "De voerbak houdt maar één soort voer",
        "La mangiatoia contiene un solo tipo di mangime", "Der Trog fasst nur eine Futtersorte",
        "Hoet rymmer bara en sorts foder", "Krybben rummer kun én slags foder",
        "В кормушке может быть только один вид корма"),
    "Not analysed (Seed Lens)": ("Não analisadas (Lupa de sementes)", "Non analysées (Loupe à graines)",
                                 "Sin analizar (Lupa de semillas)", "Niet onderzocht (Zadenloep)",
                                 "Non analizzati (Lente per semi)", "Nicht untersucht (Samenlupe)",
                                 "Ej analyserade (Fröluppen)", "Ikke analyseret (Frøluppen)", "Не изучено (семенная лупа)"),
    "With Domestication: use it holding seeds in your other hand, or on a crop, to see their traits; with Mendel's Laws, their genes": (
        "Com Domesticação: use-a segurando sementes na outra mão, ou numa lavoura, para ver suas características; com Leis de Mendel, seus genes",
        "Avec Domestication : utilisez-la en tenant des graines dans l'autre main, ou sur une culture, pour voir leurs caractères ; avec Lois de Mendel, leurs gènes",
        "Con Domesticación: úsala sosteniendo semillas en la otra mano, o sobre un cultivo, para ver sus rasgos; con Leyes de Mendel, sus genes",
        "Met Domesticatie: gebruik hem met zaden in je andere hand, of op een gewas, om hun eigenschappen te zien; met Wetten van Mendel, hun genen",
        "Con Domesticazione: usala tenendo dei semi nell'altra mano, o su una coltura, per vederne i caratteri; con Leggi di Mendel, i geni",
        "Mit Domestizierung: benutze sie mit Samen in der anderen Hand oder an einer Pflanze, um ihre Merkmale zu sehen; mit Mendelschen Regeln ihre Gene",
        "Med Domesticering: använd den med frön i andra handen, eller på en gröda, för att se egenskaperna; med Mendels lagar, generna",
        "Med Domesticering: brug den med frø i den anden hånd, eller på en afgrøde, for at se egenskaberne; med Mendels love, generne",
        "С Одомашниванием: примени её, держа семена в другой руке, или к посеву, чтобы увидеть признаки; с Законами Менделя — гены"),
    "Plant Hybridization: take pollen from a ripe plant, then brush it onto a plant of the same kind to choose its father": (
        "Hibridação de plantas: tire pólen de uma planta madura e passe-o numa planta da mesma espécie para escolher o pai",
        "Hybridation des plantes : prenez le pollen d'une plante mûre, puis passez-le sur une plante de la même sorte pour choisir son père",
        "Hibridación de plantas: toma polen de una planta madura y pásalo a una planta de la misma especie para elegir su padre",
        "Plantenkruising: neem stuifmeel van een rijpe plant en strijk het op een plant van dezelfde soort om de vader te kiezen",
        "Ibridazione delle piante: prendi il polline da una pianta matura e spennellalo su una della stessa specie per sceglierne il padre",
        "Pflanzenkreuzung: nimm Pollen von einer reifen Pflanze und pinsle ihn auf eine Pflanze derselben Art, um ihren Vater zu wählen",
        "Växtkorsning: ta pollen från en mogen växt och pensla det på en växt av samma sort för att välja fadern",
        "Plantekrydsning: tag pollen fra en moden plante og pensl det på en plante af samme slags for at vælge faderen",
        "Гибридизация растений: возьми пыльцу со зрелого растения и нанеси её на растение того же вида, чтобы выбрать отца"),
    "Tobacco Wash: use it on a crop to wash the caterpillars off the plants around it": (
        "Calda de fumo: use-a numa lavoura para lavar as lagartas das plantas em volta",
        "Décoction de tabac : utilisez-la sur une culture pour laver les chenilles des plantes autour",
        "Caldo de tabaco: úsalo en un cultivo para lavar las orugas de las plantas de alrededor",
        "Tabakswater: gebruik het op een gewas om de rupsen van de planten eromheen te wassen",
        "Infuso di tabacco: usalo su una coltura per lavare via i bruchi dalle piante intorno",
        "Tabaksud: benutze ihn an einer Pflanze, um die Raupen von den Pflanzen ringsum abzuwaschen",
        "Tobaksvatten: använd det på en gröda för att tvätta bort larverna från plantorna runt omkring",
        "Tobaksvand: brug det på en afgrøde for at vaske larverne af planterne omkring",
        "Табачный настой: примени к посеву, чтобы смыть гусениц с растений вокруг"),
    "Animal Husbandry: fill it with feed; it feeds the animals within 6 blocks that eat it, so they breed and their young grow": (
        "Pastoreio: encha-o de ração; ele alimenta os animais a até 6 blocos que comem aquilo, para que procriem e os filhotes cresçam",
        "Élevage : remplissez-la de fourrage ; elle nourrit les animaux à 6 blocs qui le mangent, pour qu'ils se reproduisent et que les petits grandissent",
        "Pastoreo: llénalo de pienso; alimenta a los animales a 6 bloques que lo comen, para que críen y sus crías crezcan",
        "Veeteelt: vul hem met voer; hij voert de dieren binnen 6 blokken die het eten, zodat ze kweken en hun jongen groeien",
        "Allevamento: riempila di mangime; nutre gli animali entro 6 blocchi che lo mangiano, così si riproducono e i piccoli crescono",
        "Viehzucht: fülle ihn mit Futter; er füttert die Tiere im Umkreis von 6 Blöcken, die es fressen, damit sie sich vermehren und die Jungen wachsen",
        "Djurhållning: fyll det med foder; det matar djuren inom 6 block som äter det, så att de förökar sig och ungarna växer",
        "Husdyrhold: fyld den med foder; den fodrer dyrene inden for 6 blokke, der æder det, så de formerer sig og ungerne vokser",
        "Скотоводство: наполни её кормом; она кормит животных в 6 блоках, которые его едят, чтобы они размножались и молодняк рос"),
    "Crop pests and diseases": ("Pragas e doenças das lavouras", "Ravageurs et maladies des cultures",
                                "Plagas y enfermedades de cultivos", "Plagen en ziekten van gewassen",
                                "Parassiti e malattie delle colture", "Schädlinge und Krankheiten der Feldfrüchte",
                                "Skadedjur och sjukdomar på grödor", "Skadedyr og sygdomme på afgrøder",
                                "Вредители и болезни посевов"),
    # genes
    "Cycle": ("Ciclo", "Cycle", "Ciclo", "Cyclus", "Ciclo", "Zyklus", "Cykel", "Cyklus", "Цикл"),
    "early": ("precoce", "précoce", "precoz", "vroeg", "precoce", "früh", "tidig", "tidlig", "скороспелый"),
    "normal cycle": ("ciclo normal", "cycle normal", "ciclo normal", "normale cyclus", "ciclo normale", "normaler Zyklus",
                     "normal cykel", "normal cyklus", "обычный цикл"),
    "Yield": ("Produção", "Rendement", "Rendimiento", "Opbrengst", "Resa", "Ertrag", "Avkastning", "Udbytte",
              "Урожайность"),
    "abundant": ("farta", "abondante", "abundante", "overvloedig", "abbondante", "ertragreich", "riklig", "rigelig",
                 "урожайный"),
    "ordinary yield": ("produção comum", "rendement ordinaire", "rendimiento común", "gewone opbrengst", "resa comune",
                       "gewöhnlicher Ertrag", "vanlig avkastning", "almindeligt udbytte", "обычный урожай"),
    "Disease resistance": ("Resistência a doenças", "Résistance aux maladies", "Resistencia a enfermedades",
                           "Ziekteresistentie", "Resistenza alle malattie", "Krankheitsresistenz", "Sjukdomsresistens",
                           "Sygdomsresistens", "Устойчивость к болезням"),
    "resists rust": ("resiste à ferrugem", "résiste à la rouille", "resiste a la roya", "bestand tegen roest",
                     "resiste alla ruggine", "rostresistent", "motstår rost", "modstår rust", "устойчив к ржавчине"),
    "takes rust": ("pega ferrugem", "attrape la rouille", "coge roya", "krijgt roest", "prende la ruggine",
                   "rostanfällig", "får rost", "får rust", "болеет ржавчиной"),
    "Drought tolerance": ("Tolerância à seca", "Tolérance à la sécheresse", "Tolerancia a la sequía",
                          "Droogtetolerantie", "Tolleranza alla siccità", "Trockenheitstoleranz", "Torktålighet",
                          "Tørketolerance", "Засухоустойчивость"),
    "drought tolerant": ("tolera a seca", "tolère la sécheresse", "tolera la sequía", "droogtetolerant",
                         "tollera la siccità", "trockenheitstolerant", "torktålig", "tørketolerant", "засухоустойчивый"),
    "needs water": ("precisa de água", "a besoin d'eau", "necesita agua", "heeft water nodig", "ha bisogno d'acqua",
                    "braucht Wasser", "behöver vatten", "skal have vand", "нуждается в воде"),
    "Cold tolerance": ("Tolerância ao frio", "Tolérance au froid", "Tolerancia al frío", "Koudetolerantie",
                       "Tolleranza al freddo", "Kältetoleranz", "Köldtålighet", "Kuldetolerance", "Холодостойкость"),
    "cold tolerant": ("tolera o frio", "tolère le froid", "tolera el frío", "koudetolerant", "tollera il freddo",
                      "kältetolerant", "köldtålig", "kuldetolerant", "холодостойкий"),
    "feels the cold": ("sente o frio", "craint le froid", "sufre el frío", "voelt de kou", "soffre il freddo",
                       "kälteempfindlich", "känslig för kyla", "følsom for kulde", "боится холода"),
    "Pest resistance": ("Resistência a pragas", "Résistance aux ravageurs", "Resistencia a plagas", "Plaagresistentie",
                        "Resistenza ai parassiti", "Schädlingsresistenz", "Skadedjursresistens", "Skadedyrsresistens",
                        "Устойчивость к вредителям"),
    "resists caterpillars": ("resiste a lagartas", "résiste aux chenilles", "resiste a las orugas",
                             "bestand tegen rupsen", "resiste ai bruchi", "raupenresistent", "motstår larver",
                             "modstår larver", "устойчив к гусеницам"),
    "takes caterpillars": ("pega lagarta", "attrape les chenilles", "coge orugas", "krijgt rupsen", "prende i bruchi",
                           "raupenanfällig", "får larver", "får larver", "страдает от гусениц"),
}

T = {
    "pt_br": {"crops": "Lavouras e genética"}, "fr_fr": {"crops": "Cultures et génétique"},
    "es_es": {"crops": "Cultivos y genética"}, "nl_nl": {"crops": "Gewassen en genetica"},
    "it_it": {"crops": "Colture e genetica"}, "de_de": {"crops": "Feldfrüchte und Genetik"},
    "sv_se": {"crops": "Grödor och genetik"}, "da_dk": {"crops": "Afgrøder og genetik"},
    "ru_ru": {"crops": "Посевы и генетика"},
}

GUIDE_TEXT = {
    "pt_br": [
        "# Lavouras",
        "Vinte lavouras nascem silvestres no mundo: uma planta madura sobre grama ou areia, que não cresce. Quebre-a "
        "para tirar as sementes (a mandioca dá ramas) e plante-as em terra arada como o trigo. O arroz precisa de água "
        "logo ao lado da terra arada. Café e alfafa ficam plantados: clique com o botão direito quando maduros para "
        "colher, e eles rebrotam.",
        "Cada lavoura gosta de alguns climas (quente, temperado ou frio). Fora deles cresce na metade da velocidade, "
        "nunca deixa de crescer. Em terra seca (deserto, savana, terras áridas) uma lavoura sem água bastante também "
        "cresce na metade da velocidade, a não ser que seja de terra seca (algodão, mamona, alfafa, sorgo). Uma estufa "
        "aquecida conta como quente.",
        "# Genes",
        "Toda planta, as vanilla também, carrega seis genes, duas cópias de cada, uma de cada pai. Eles agem desde o "
        "primeiro plantio; a pesquisa só deixa você vê-los. A forma útil de cada gene e como ela aparece:",
        "- C, Ciclo: plantas cc amadurecem cerca de 30% antes (recessivo: Cc parece normal).",
        "- P, Produção: um P basta para 30% de chance de mais um item na colheita (dominante).",
        "- R, Resistência a doenças: um R deixa a ferrugem quatro vezes mais rara (dominante).",
        "- D, Tolerância à seca: plantas dd crescem a toda velocidade em terra seca sem água (recessivo).",
        "- F, Tolerância ao frio: um F deixa uma lavoura de terra mais quente crescer a toda velocidade no frio (dominante).",
        "- I, Resistência a pragas: um I deixa a lagarta quatro vezes mais rara (dominante).",
        "Sementes de loja, de aldeia e as pilhas vanilla têm as formas comuns (CC pp rr DD ff ii). As plantas "
        "silvestres carregam as formas úteis, muitas vezes escondidas numa cópia só: é delas que vem a melhoria. "
        "Sementes de trigo tiradas da grama às vezes também são silvestres.",
        "# Cruzamento",
        "Quando uma planta amadurece, ela recebe pólen de uma planta madura da mesma espécie a até 2 blocos: milho, "
        "cânhamo, colza, alfafa, nabo, cenoura e beterraba fazem isso 80% das vezes, as outras só 5% (elas se "
        "autopolinizam). Cada semente recebe então uma cópia de cada gene da mãe e uma do pai, ao acaso. Um gene em "
        "500 sofre mutação. Mandioca e batata se plantam de pedaços da planta: copiam-na exatamente. Sementes com "
        "genes diferentes não se empilham.",
        "Domesticação (nível 1) ensina a Lupa de sementes: use-a segurando sementes na outra mão, ou numa lavoura, para "
        "ver suas características. Uma planta que tem a forma recessiva numa só cópia parece comum: só os filhos contam.",
        "Pastoreio (nível 1): o Cocho guarda uma pilha de ração e alimenta os animais a até 6 blocos que a comem: os "
        "adultos procriam e os filhotes crescem mais rápido. Vacas, ovelhas e cabras comem alfafa, aveia, cevada, sorgo "
        "e nabo; porcos milho, mandioca, soja e nabo; galinhas grãos; cavalos aveia e alfafa.",
        "Rotação de culturas (nível 2): soja, feijão, anil e alfafa colhidos maduros deixam a terra arada adubada. "
        "Tinturaria natural (nível 2): a Dye Vat faz corante azul das folhas de anil e vermelho da raiz de garança.",
        "Hibridação de plantas (nível 3): o Pincel de polinização tira pólen de uma planta madura; passado numa planta "
        "da mesma espécie, em qualquer lugar, faz da primeira o pai das sementes dela. Calda de fumo (nível 3): três "
        "folhas de fumo e uma garrafa de água lavam as lagartas das lavouras em volta de onde você a usar.",
        "Leis de Mendel (nível 4): a Lupa de sementes mostra as duas letras de cada gene, e Cc se distingue de CC. "
        "Cruze duas plantas Cc e um quarto das sementes sai cc.",
        "# Pragas e doenças",
        "Uma planta crescendo no meio de mais de 20 da mesma espécie num quadrado de 9 por 9 pode pegar lagarta ou "
        "ferrugem. Ela para de crescer, dá um item a menos e passa para as vizinhas. Lavouras misturadas não pegam "
        "nada. Arranque-a, ou use Calda de fumo ou Insect Spray contra lagartas (lavouras em terra com DDT nunca pegam) "
        "e calda bordalesa contra ferrugem. A regra de jogo periodicElementsCropPests desliga pragas e doenças.",
        "# Onde nascem silvestres",
    ],
    "fr_fr": [
        "# Cultures",
        "Vingt cultures poussent à l'état sauvage : une plante mûre sur l'herbe ou le sable, qui ne pousse plus. "
        "Cassez-la pour ses graines (le manioc donne des boutures) et semez-les sur de la terre labourée comme le blé. "
        "Le riz veut de l'eau juste à côté de sa terre. Le caféier et la luzerne restent en place : clic droit une fois "
        "mûrs pour récolter, et ils repoussent.",
        "Chaque culture aime certains climats (chaud, tempéré ou froid). Ailleurs elle pousse à moitié vitesse, jamais "
        "pas du tout. En terre sèche (désert, savane, badlands) un champ mal arrosé pousse aussi à moitié vitesse, sauf "
        "les cultures des terres sèches (coton, ricin, luzerne, sorgho). Une serre chauffée compte comme chaude.",
        "# Gènes",
        "Chaque plante, les cultures vanilla aussi, porte six gènes, en deux copies, une de chaque parent. Ils agissent "
        "dès le premier semis ; la recherche permet seulement de les voir. La forme utile de chaque gène :",
        "- C, Cycle : les plantes cc mûrissent environ 30 % plus tôt (récessif : Cc a l'air normal).",
        "- P, Rendement : un seul P donne 30 % de chances d'un produit de plus à la récolte (dominant).",
        "- R, Résistance aux maladies : un R rend la rouille quatre fois plus rare (dominant).",
        "- D, Tolérance à la sécheresse : les plantes dd poussent à pleine vitesse en terre sèche sans eau (récessif).",
        "- F, Tolérance au froid : un F laisse une culture des pays chauds pousser à pleine vitesse au froid (dominant).",
        "- I, Résistance aux ravageurs : un I rend les chenilles quatre fois plus rares (dominant).",
        "Les graines des marchands, des villages et des piles vanilla ont les formes ordinaires (CC pp rr DD ff ii). Les "
        "plantes sauvages portent les formes utiles, souvent cachées dans une seule copie : c'est d'elles que vient "
        "l'amélioration. Les graines de blé tirées de l'herbe sont parfois sauvages aussi.",
        "# Croisements",
        "Quand une plante mûrit, elle reçoit le pollen d'une plante mûre de son espèce à 2 blocs au plus : maïs, chanvre, "
        "colza, luzerne, navet, carotte et betterave le font 80 % du temps, les autres 5 % seulement (elles "
        "s'autopollinisent). Chaque graine reçoit alors une copie de chaque gène de la mère et une du père, au hasard. "
        "Un gène sur 500 mute. Le manioc et la pomme de terre se plantent par morceaux : ils copient la plante exactement. "
        "Les graines aux gènes différents ne s'empilent pas.",
        "Domestication (niveau 1) apprend la Loupe à graines : utilisez-la en tenant des graines dans l'autre main, ou "
        "sur une culture, pour voir leurs caractères. Une plante qui porte une forme récessive en une seule copie a "
        "l'air ordinaire : seule sa descendance le dit.",
        "Élevage (niveau 1) : la Mangeoire contient une pile de fourrage et nourrit les animaux à 6 blocs qui le "
        "mangent : les adultes se reproduisent et les petits grandissent plus vite. Vaches, moutons et chèvres mangent "
        "luzerne, avoine, orge, sorgho et navets ; les cochons maïs, manioc, soja et navets ; les poules du grain ; les "
        "chevaux avoine et luzerne.",
        "Rotation des cultures (niveau 2) : soja, haricots, indigotier et luzerne récoltés mûrs laissent leur terre "
        "fertilisée. Teintures naturelles (niveau 2) : la cuve de teinture fait du bleu avec l'indigotier et du rouge "
        "avec la racine de garance.",
        "Hybridation des plantes (niveau 3) : le Pinceau de pollinisation prend le pollen d'une plante mûre ; passé sur "
        "une plante de la même sorte, n'importe où, il fait de la première le père de ses graines. Décoction de tabac "
        "(niveau 3) : trois feuilles de tabac et une fiole d'eau lavent les chenilles des cultures autour de l'endroit visé.",
        "Lois de Mendel (niveau 4) : la Loupe à graines montre les deux lettres de chaque gène, et l'on distingue Cc de "
        "CC. Croisez deux plantes Cc et un quart des graines sont cc.",
        "# Ravageurs et maladies",
        "Une plante qui pousse parmi plus de 20 de son espèce dans un carré de 9 sur 9 peut attraper des chenilles ou la "
        "rouille. Elle cesse de pousser, donne un produit de moins et contamine ses voisines. Les champs mélangés "
        "n'attrapent rien. Cassez-la, ou utilisez la Décoction de tabac ou l'Insect Spray contre les chenilles (les "
        "cultures sur un sol au DDT n'en attrapent jamais) et la bouillie bordelaise contre la rouille. La règle de jeu "
        "periodicElementsCropPests désactive ravageurs et maladies.",
        "# Où elles poussent à l'état sauvage",
    ],
    "es_es": [
        "# Cultivos",
        "Veinte cultivos crecen silvestres en el mundo: una planta madura sobre hierba o arena que no crece. Rómpela para "
        "sacar sus semillas (la yuca da estacas) y siémbralas en tierra de cultivo como el trigo. El arroz necesita agua "
        "justo al lado de su tierra. El cafeto y la alfalfa se quedan plantados: haz clic derecho cuando estén maduros "
        "para cosechar, y vuelven a crecer.",
        "Cada cultivo prefiere algunos climas (cálido, templado o frío). Fuera de ellos crece a mitad de velocidad, "
        "nunca deja de crecer. En tierra seca (desierto, sabana, badlands) un campo sin suficiente agua también crece a "
        "mitad de velocidad, salvo los cultivos de secano (algodón, ricino, alfalfa, sorgo). Un invernadero calentado "
        "cuenta como cálido.",
        "# Genes",
        "Toda planta, también las vanilla, lleva seis genes, dos copias de cada uno, una de cada progenitor. Actúan desde "
        "la primera siembra; la investigación solo te deja verlos. La forma útil de cada gen y cómo se nota:",
        "- C, Ciclo: las plantas cc maduran un 30% antes (recesivo: Cc parece normal).",
        "- P, Rendimiento: un solo P da un 30% de probabilidad de un producto más al cosechar (dominante).",
        "- R, Resistencia a enfermedades: un R hace la roya cuatro veces más rara (dominante).",
        "- D, Tolerancia a la sequía: las plantas dd crecen a toda velocidad en tierra seca sin agua (recesivo).",
        "- F, Tolerancia al frío: un F deja crecer a toda velocidad en el frío un cultivo de tierras cálidas (dominante).",
        "- I, Resistencia a plagas: un I hace las orugas cuatro veces más raras (dominante).",
        "Las semillas de tiendas, aldeas y pilas vanilla tienen las formas comunes (CC pp rr DD ff ii). Las plantas "
        "silvestres llevan las formas útiles, a menudo escondidas en una sola copia: de ellas viene la mejora. Las "
        "semillas de trigo sacadas de la hierba a veces también son silvestres.",
        "# Cruces",
        "Cuando una planta madura, toma polen de una planta madura de su especie a 2 bloques como mucho: maíz, cáñamo, "
        "colza, alfalfa, nabo, zanahoria y remolacha lo hacen el 80% de las veces, las demás solo el 5% (se "
        "autopolinizan). Cada semilla recibe entonces una copia de cada gen de la madre y otra del padre, al azar. Un gen "
        "de cada 500 muta. La yuca y la patata se plantan de trozos de la planta: la copian exactamente. Las semillas con "
        "genes distintos no se apilan.",
        "Domesticación (nivel 1) enseña la Lupa de semillas: úsala sosteniendo semillas en la otra mano, o sobre un "
        "cultivo, para ver sus rasgos. Una planta con una forma recesiva en una sola copia parece común: solo su "
        "descendencia lo revela.",
        "Pastoreo (nivel 1): el Comedero guarda una pila de pienso y alimenta a los animales a 6 bloques que lo comen: "
        "los adultos crían y las crías crecen más rápido. Vacas, ovejas y cabras comen alfalfa, avena, cebada, sorgo y "
        "nabos; los cerdos maíz, yuca, soja y nabos; las gallinas grano; los caballos avena y alfalfa.",
        "Rotación de cultivos (nivel 2): soja, judías, añil y alfalfa cosechados maduros dejan su tierra abonada. Tintes "
        "naturales (nivel 2): la cuba de tinte hace tinte azul con hojas de añil y rojo con raíz de rubia.",
        "Hibridación de plantas (nivel 3): el Pincel de polinización toma polen de una planta madura; pasado a una planta "
        "de la misma especie, en cualquier sitio, hace de la primera el padre de sus semillas. Caldo de tabaco (nivel 3): "
        "tres hojas de tabaco y una botella de agua lavan las orugas de los cultivos alrededor de donde lo uses.",
        "Leyes de Mendel (nivel 4): la Lupa de semillas muestra las dos letras de cada gen, y Cc se distingue de CC. "
        "Cruza dos plantas Cc y una cuarta parte de las semillas sale cc.",
        "# Plagas y enfermedades",
        "Una planta que crece entre más de 20 de su especie en un cuadrado de 9 por 9 puede coger orugas o roya. Deja de "
        "crecer, da un producto menos y contagia a sus vecinas. Los campos mezclados no cogen nada. Rómpela, o usa Caldo "
        "de tabaco o Insect Spray contra las orugas (los cultivos en suelo con DDT nunca las cogen) y caldo bordelés "
        "contra la roya. La regla de juego periodicElementsCropPests desactiva plagas y enfermedades.",
        "# Dónde crecen silvestres",
    ],
    "nl_nl": [
        "# Gewassen",
        "Twintig gewassen groeien wild in de wereld: een rijpe plant op gras of zand die niet meer groeit. Breek hem voor "
        "zijn zaden (cassave geeft stekken) en zaai ze op akkerland zoals tarwe. Rijst heeft water direct naast zijn "
        "akkerland nodig. Koffie en luzerne blijven staan: klik rechts als ze rijp zijn om te oogsten, en ze groeien "
        "terug.",
        "Elk gewas houdt van bepaalde klimaten (warm, gematigd of koud). Elders groeit het op halve snelheid, nooit "
        "helemaal niet. Op droog land (woestijn, savanne, badlands) groeit een veld zonder genoeg water ook op halve "
        "snelheid, behalve droge-landgewassen (katoen, wonderboom, luzerne, sorghum). Een verwarmde kas telt als warm.",
        "# Genen",
        "Elke plant, ook de vanilla-gewassen, draagt zes genen, twee kopieën van elk, één van elke ouder. Ze werken vanaf "
        "de eerste zaaiing; onderzoek laat je ze alleen zien. De nuttige vorm van elk gen en hoe die zich toont:",
        "- C, Cyclus: cc-planten zijn ongeveer 30% eerder rijp (recessief: Cc ziet er normaal uit).",
        "- P, Opbrengst: één P geeft 30% kans op één oogst meer (dominant).",
        "- R, Ziekteresistentie: één R maakt roest vier keer zeldzamer (dominant).",
        "- D, Droogtetolerantie: dd-planten groeien op volle snelheid op droog land zonder water (recessief).",
        "- F, Koudetolerantie: één F laat een gewas uit warmere streken op volle snelheid groeien in de kou (dominant).",
        "- I, Plaagresistentie: één I maakt rupsen vier keer zeldzamer (dominant).",
        "Zaden uit winkels, dorpen en vanilla-stapels hebben de gewone vormen (CC pp rr DD ff ii). Wilde planten dragen "
        "de nuttige vormen, vaak verborgen in één kopie: daar komt verbetering vandaan. Tarwezaad uit gras is soms ook "
        "wild.",
        "# Kruisen",
        "Als een plant rijpt, krijgt hij stuifmeel van een rijpe plant van zijn soort binnen 2 blokken: maïs, hennep, "
        "koolzaad, luzerne, raap, wortel en biet doen dat 80% van de tijd, de rest maar 5% (ze bestuiven zichzelf). Elk "
        "zaad krijgt dan van elk gen één kopie van de moeder en één van de vader, willekeurig. Eén gen op 500 muteert. "
        "Cassave en aardappel worden geplant uit stukken van de plant: ze kopiëren hem precies. Zaden met verschillende "
        "genen stapelen niet.",
        "Domesticatie (niveau 1) leert de Zadenloep: gebruik hem met zaden in je andere hand, of op een gewas, om hun "
        "eigenschappen te zien. Een plant met een recessieve vorm in één kopie ziet er gewoon uit: alleen zijn "
        "nakomelingen verraden het.",
        "Veeteelt (niveau 1): de Voerbak houdt een stapel voer en voert de dieren binnen 6 blokken die het eten: "
        "volwassen dieren kweken en jongen groeien sneller op. Koeien, schapen en geiten eten luzerne, haver, gerst, "
        "sorghum en rapen; varkens maïs, cassave, sojabonen en rapen; kippen graan; paarden haver en luzerne.",
        "Wisselbouw (niveau 2): sojabonen, bonen, indigo en luzerne die rijp geoogst worden laten hun akkerland bemest "
        "achter. Natuurlijke kleurstoffen (niveau 2): het verfvat maakt blauwe kleurstof uit indigobladeren en rode uit "
        "meekrapwortel.",
        "Plantenkruising (niveau 3): het Bestuivingspenseel neemt stuifmeel van een rijpe plant; op een plant van "
        "dezelfde soort gestreken, waar dan ook, maakt het de eerste plant de vader van haar zaden. Tabakswater "
        "(niveau 3): drie tabaksbladeren en een fles water wassen de rupsen van de gewassen rond de plek waar je het "
        "gebruikt.",
        "Wetten van Mendel (niveau 4): de Zadenloep toont de twee letters van elk gen, zodat Cc van CC te onderscheiden "
        "is. Kruis twee Cc-planten en een kwart van de zaden is cc.",
        "# Plagen en ziekten",
        "Een plant die groeit tussen meer dan 20 van zijn soort in een vierkant van 9 bij 9 kan rupsen of roest krijgen. "
        "Hij stopt met groeien, geeft één oogst minder en steekt zijn buren aan. Gemengde velden krijgen niets. Breek "
        "hem, of gebruik Tabakswater of Insect Spray tegen rupsen (gewassen op DDT-grond krijgen ze nooit) en Bordeaux-pap "
        "tegen roest. De spelregel periodicElementsCropPests zet plagen en ziekten uit.",
        "# Waar ze wild groeien",
    ],
    "it_it": [
        "# Colture",
        "Venti colture crescono selvatiche nel mondo: una pianta matura su erba o sabbia che non cresce. Rompila per i "
        "semi (la manioca dà talee) e seminali su terreno arato come il grano. Il riso vuole acqua proprio accanto al suo "
        "terreno. Caffè ed erba medica restano piantati: clic destro quando sono maturi per raccogliere, e ricrescono.",
        "Ogni coltura ama alcuni climi (caldo, temperato o freddo). Altrove cresce a metà velocità, mai per niente. In "
        "terra secca (deserto, savana, badlands) un campo senza acqua a sufficienza cresce anch'esso a metà velocità, "
        "tranne le colture da terra secca (cotone, ricino, erba medica, sorgo). Una serra riscaldata conta come calda.",
        "# Geni",
        "Ogni pianta, anche le colture vanilla, porta sei geni, due copie ciascuno, una per genitore. Agiscono fin dalla "
        "prima semina; la ricerca serve solo a vederli. La forma utile di ogni gene e come si vede:",
        "- C, Ciclo: le piante cc maturano circa il 30% prima (recessivo: Cc sembra normale).",
        "- P, Resa: basta un P per il 30% di probabilità di un prodotto in più al raccolto (dominante).",
        "- R, Resistenza alle malattie: un R rende la ruggine quattro volte più rara (dominante).",
        "- D, Tolleranza alla siccità: le piante dd crescono a piena velocità in terra secca senza acqua (recessivo).",
        "- F, Tolleranza al freddo: un F fa crescere a piena velocità al freddo una coltura di terre più calde (dominante).",
        "- I, Resistenza ai parassiti: un I rende i bruchi quattro volte più rari (dominante).",
        "I semi dei negozi, dei villaggi e delle pile vanilla hanno le forme comuni (CC pp rr DD ff ii). Le piante "
        "selvatiche portano le forme utili, spesso nascoste in una sola copia: è da lì che viene il miglioramento. I semi "
        "di grano presi dall'erba a volte sono selvatici anch'essi.",
        "# Incroci",
        "Quando una pianta matura, riceve il polline di una pianta matura della sua specie entro 2 blocchi: mais, canapa, "
        "colza, erba medica, rapa, carota e barbabietola lo fanno l'80% delle volte, le altre solo il 5% (si "
        "autoimpollinano). Ogni seme riceve poi una copia di ogni gene dalla madre e una dal padre, a caso. Un gene su 500 "
        "muta. Manioca e patata si piantano da pezzi della pianta: la copiano esattamente. I semi con geni diversi non si "
        "impilano.",
        "Domesticazione (livello 1) insegna la Lente per semi: usala tenendo dei semi nell'altra mano, o su una coltura, "
        "per vederne i caratteri. Una pianta con una forma recessiva in una sola copia sembra comune: solo la prole lo "
        "rivela.",
        "Allevamento (livello 1): la Mangiatoia contiene una pila di mangime e nutre gli animali entro 6 blocchi che lo "
        "mangiano: gli adulti si riproducono e i piccoli crescono più in fretta. Mucche, pecore e capre mangiano erba "
        "medica, avena, orzo, sorgo e rape; i maiali mais, manioca, soia e rape; le galline cereali; i cavalli avena ed "
        "erba medica.",
        "Rotazione delle colture (livello 2): soia, fagioli, indaco ed erba medica raccolti maturi lasciano il terreno "
        "concimato. Tinture naturali (livello 2): il tino da tintura fa il blu dalle foglie d'indaco e il rosso dalla "
        "radice di robbia.",
        "Ibridazione delle piante (livello 3): il Pennello impollinatore prende il polline da una pianta matura; passato "
        "su una pianta della stessa specie, ovunque sia, rende la prima padre dei suoi semi. Infuso di tabacco "
        "(livello 3): tre foglie di tabacco e una bottiglia d'acqua lavano via i bruchi dalle colture intorno al punto "
        "in cui lo usi.",
        "Leggi di Mendel (livello 4): la Lente per semi mostra le due lettere di ogni gene, così Cc si distingue da CC. "
        "Incrocia due piante Cc e un quarto dei semi è cc.",
        "# Parassiti e malattie",
        "Una pianta che cresce tra più di 20 della sua specie in un quadrato di 9 per 9 può prendere i bruchi o la "
        "ruggine. Smette di crescere, dà un prodotto in meno e contagia le vicine. I campi misti non prendono nulla. "
        "Rompila, oppure usa l'Infuso di tabacco o l'Insect Spray contro i bruchi (le colture su terreno al DDT non li "
        "prendono mai) e la poltiglia bordolese contro la ruggine. La regola di gioco periodicElementsCropPests "
        "disattiva parassiti e malattie.",
        "# Dove crescono selvatiche",
    ],
    "de_de": [
        "# Feldfrüchte",
        "Zwanzig Feldfrüchte wachsen wild in der Welt: eine reife Pflanze auf Gras oder Sand, die nicht wächst. Baue sie "
        "für ihre Samen ab (Maniok gibt Stecklinge) und säe sie auf Ackerboden wie Weizen. Reis braucht Wasser direkt "
        "neben seinem Ackerboden. Kaffee und Luzerne bleiben stehen: rechtsklicke sie reif, um zu ernten, und sie "
        "wachsen nach.",
        "Jede Feldfrucht mag bestimmte Klimas (warm, gemäßigt oder kalt). Anderswo wächst sie mit halber Geschwindigkeit, "
        "nie gar nicht. Auf trockenem Land (Wüste, Savanne, Tafelberge) wächst ein nicht voll bewässertes Feld ebenfalls "
        "halb so schnell, außer bei Trockenlandpflanzen (Baumwolle, Rizinus, Luzerne, Sorghumhirse). Ein beheiztes "
        "Gewächshaus zählt als warm.",
        "# Gene",
        "Jede Pflanze, auch die Vanilla-Feldfrüchte, trägt sechs Gene, je zwei Kopien, eine von jedem Elternteil. Sie "
        "wirken ab der ersten Aussaat; die Forschung macht sie nur sichtbar. Die nützliche Form jedes Gens und wie sie "
        "sich zeigt:",
        "- C, Zyklus: cc-Pflanzen reifen etwa 30% früher (rezessiv: Cc sieht normal aus).",
        "- P, Ertrag: ein P reicht für 30% Chance auf eine Frucht mehr bei der Ernte (dominant).",
        "- R, Krankheitsresistenz: ein R macht Rost viermal seltener (dominant).",
        "- D, Trockenheitstoleranz: dd-Pflanzen wachsen auf trockenem Land ohne Wasser mit voller Geschwindigkeit (rezessiv).",
        "- F, Kältetoleranz: ein F lässt eine Pflanze wärmerer Länder in der Kälte mit voller Geschwindigkeit wachsen (dominant).",
        "- I, Schädlingsresistenz: ein I macht Raupen viermal seltener (dominant).",
        "Samen von Händlern, aus Dörfern und Vanilla-Stapeln haben die gewöhnlichen Formen (CC pp rr DD ff ii). "
        "Wildpflanzen tragen die nützlichen Formen, oft versteckt in nur einer Kopie: daher kommt die Verbesserung. "
        "Weizensamen aus Gras sind manchmal auch wild.",
        "# Kreuzung",
        "Wenn eine Pflanze reift, bekommt sie Pollen von einer reifen Pflanze ihrer Art in höchstens 2 Blöcken: Mais, "
        "Hanf, Raps, Luzerne, Rübe, Karotte und Rote Bete tun das zu 80%, die anderen nur zu 5% (sie bestäuben sich "
        "selbst). Jeder Samen bekommt dann von jedem Gen eine Kopie von der Mutter und eine vom Vater, zufällig. Eins von "
        "500 Genen mutiert. Maniok und Kartoffel werden aus Pflanzenstücken gesetzt: sie kopieren die Pflanze genau. "
        "Samen mit verschiedenen Genen stapeln sich nicht.",
        "Domestizierung (Stufe 1) lehrt die Samenlupe: benutze sie mit Samen in der anderen Hand oder an einer Pflanze, "
        "um ihre Merkmale zu sehen. Eine Pflanze mit einer rezessiven Form in nur einer Kopie sieht gewöhnlich aus: nur "
        "ihre Nachkommen verraten es.",
        "Viehzucht (Stufe 1): der Futtertrog fasst einen Stapel Futter und füttert die Tiere im Umkreis von 6 Blöcken, "
        "die es fressen: Erwachsene vermehren sich und Junge wachsen schneller. Kühe, Schafe und Ziegen fressen Luzerne, "
        "Hafer, Gerste, Sorghumhirse und Rüben; Schweine Mais, Maniok, Sojabohnen und Rüben; Hühner Körner; Pferde Hafer "
        "und Luzerne.",
        "Fruchtwechsel (Stufe 2): reif geerntete Sojabohnen, Bohnen, Indigo und Luzerne hinterlassen gedüngten Ackerboden. "
        "Naturfarbstoffe (Stufe 2): der Färbebottich macht blauen Farbstoff aus Indigoblättern und roten aus Krappwurzel.",
        "Pflanzenkreuzung (Stufe 3): der Bestäubungspinsel nimmt Pollen von einer reifen Pflanze; auf eine Pflanze "
        "derselben Art gepinselt, egal wo, macht er die erste zum Vater ihrer Samen. Tabaksud (Stufe 3): drei "
        "Tabakblätter und eine Flasche Wasser waschen die Raupen von den Pflanzen rund um die Stelle, wo du ihn benutzt.",
        "Mendelsche Regeln (Stufe 4): die Samenlupe zeigt die zwei Buchstaben jedes Gens, sodass sich Cc von CC "
        "unterscheiden lässt. Kreuze zwei Cc-Pflanzen und ein Viertel der Samen ist cc.",
        "# Schädlinge und Krankheiten",
        "Eine Pflanze, die zwischen mehr als 20 ihrer Art in einem Quadrat von 9 mal 9 wächst, kann Raupen oder Rost "
        "bekommen. Sie wächst nicht mehr, gibt eine Frucht weniger und steckt ihre Nachbarn an. Gemischte Felder bekommen "
        "nichts. Baue sie ab, oder nimm Tabaksud oder Insect Spray gegen Raupen (Pflanzen auf DDT-Boden bekommen nie "
        "welche) und Bordeauxbrühe gegen Rost. Die Spielregel periodicElementsCropPests schaltet Schädlinge und "
        "Krankheiten ab.",
        "# Wo sie wild wachsen",
    ],
    "sv_se": [
        "# Grödor",
        "Tjugo grödor växer vilt i världen: en mogen planta på gräs eller sand som inte växer. Bryt den för fröna "
        "(kassava ger sticklingar) och så dem på åkerjord som vete. Ris behöver vatten precis intill sin åkerjord. Kaffe "
        "och lusern står kvar: högerklicka dem mogna för att skörda, och de växer tillbaka.",
        "Varje gröda trivs i vissa klimat (varmt, tempererat eller kallt). Annars växer den i halv fart, aldrig inte "
        "alls. På torr mark (öken, savann, badlands) växer ett fält utan tillräckligt vatten också i halv fart, utom "
        "torrmarksgrödor (bomull, ricin, lusern, durra). Ett uppvärmt växthus räknas som varmt.",
        "# Gener",
        "Varje planta, även vanilla-grödorna, bär sex gener, två kopior av varje, en från varje förälder. De verkar från "
        "första sådden; forskningen låter dig bara se dem. Den nyttiga formen av varje gen och hur den syns:",
        "- C, Cykel: cc-plantor mognar ungefär 30% tidigare (recessiv: Cc ser normal ut).",
        "- P, Avkastning: ett P räcker för 30% chans till en skörd till (dominant).",
        "- R, Sjukdomsresistens: ett R gör rost fyra gånger ovanligare (dominant).",
        "- D, Torktålighet: dd-plantor växer i full fart på torr mark utan vatten (recessiv).",
        "- F, Köldtålighet: ett F låter en gröda från varmare trakter växa i full fart i kyla (dominant).",
        "- I, Skadedjursresistens: ett I gör larver fyra gånger ovanligare (dominant).",
        "Frön från handlare, byar och vanilla-högar har de vanliga formerna (CC pp rr DD ff ii). Vilda plantor bär de "
        "nyttiga formerna, ofta gömda i en enda kopia: det är därifrån förbättringen kommer. Vetefrön från gräs är ibland "
        "också vilda.",
        "# Korsning",
        "När en planta mognar får den pollen från en mogen planta av samma sort inom 2 block: majs, hampa, raps, lusern, "
        "rova, morot och rödbeta gör det 80% av gångerna, de andra bara 5% (de pollinerar sig själva). Varje frö får "
        "sedan en kopia av varje gen från modern och en från fadern, slumpvis. En gen av 500 muterar. Kassava och potatis "
        "sätts av bitar av plantan: de kopierar den exakt. Frön med olika gener staplas inte.",
        "Domesticering (nivå 1) lär ut Fröluppen: använd den med frön i andra handen, eller på en gröda, för att se deras "
        "egenskaper. En planta med en recessiv form i en enda kopia ser vanlig ut: bara avkomman avslöjar det.",
        "Djurhållning (nivå 1): Foderhoet rymmer en hög foder och matar djuren inom 6 block som äter det: vuxna förökar "
        "sig och ungarna växer fortare. Kor, får och getter äter lusern, havre, korn, durra och rovor; grisar majs, "
        "kassava, sojabönor och rovor; höns säd; hästar havre och lusern.",
        "Växtföljd (nivå 2): sojabönor, bönor, indigo och lusern som skördas mogna lämnar åkerjorden gödslad. Naturliga "
        "färgämnen (nivå 2): färgkaret gör blått färgämne av indigoblad och rött av krapprot.",
        "Växtkorsning (nivå 3): Pollineringspenseln tar pollen från en mogen planta; penslat på en planta av samma sort, "
        "var som helst, gör det den första till far till dess frön. Tobaksvatten (nivå 3): tre tobaksblad och en flaska "
        "vatten tvättar bort larverna från grödorna runt där du använder det.",
        "Mendels lagar (nivå 4): Fröluppen visar varje gens två bokstäver, så att Cc skiljs från CC. Korsa två Cc-plantor "
        "och en fjärdedel av fröna blir cc.",
        "# Skadedjur och sjukdomar",
        "En planta som växer bland fler än 20 av sin sort i en kvadrat på 9 gånger 9 kan få larver eller rost. Den slutar "
        "växa, ger en skörd mindre och smittar grannarna. Blandade fält får ingenting. Bryt den, eller använd "
        "Tobaksvatten eller Insect Spray mot larver (grödor på DDT-mark får dem aldrig) och bordeauxvätska mot rost. "
        "Spelregeln periodicElementsCropPests stänger av skadedjur och sjukdomar.",
        "# Var de växer vilt",
    ],
    "da_dk": [
        "# Afgrøder",
        "Tyve afgrøder vokser vildt i verden: en moden plante på græs eller sand, som ikke vokser. Bræk den for frøene "
        "(kassava giver stiklinger) og så dem på landbrugsjord som hvede. Ris skal have vand lige ved siden af sin jord. "
        "Kaffe og lucerne bliver stående: højreklik dem modne for at høste, og de gror igen.",
        "Hver afgrøde trives i visse klimaer (varmt, tempereret eller koldt). Andre steder vokser den med halv fart, "
        "aldrig slet ikke. På tør jord (ørken, savanne, badlands) vokser en mark uden nok vand også med halv fart, "
        "undtagen tørjordsafgrøder (bomuld, ricinus, lucerne, durra). Et opvarmet drivhus tæller som varmt.",
        "# Gener",
        "Hver plante, også vanilla-afgrøderne, bærer seks gener, to kopier af hvert, en fra hver forælder. De virker fra "
        "første såning; forskningen lader dig kun se dem. Den nyttige form af hvert gen og hvordan den viser sig:",
        "- C, Cyklus: cc-planter modner omkring 30% tidligere (recessiv: Cc ser normal ud).",
        "- P, Udbytte: et P er nok til 30% chance for en høst mere (dominant).",
        "- R, Sygdomsresistens: et R gør rust fire gange sjældnere (dominant).",
        "- D, Tørketolerance: dd-planter vokser med fuld fart på tør jord uden vand (recessiv).",
        "- F, Kuldetolerance: et F lader en afgrøde fra varmere egne vokse med fuld fart i kulde (dominant).",
        "- I, Skadedyrsresistens: et I gør larver fire gange sjældnere (dominant).",
        "Frø fra handlende, landsbyer og vanilla-stakke har de almindelige former (CC pp rr DD ff ii). Vilde planter bærer "
        "de nyttige former, ofte gemt i en enkelt kopi: derfra kommer forbedringen. Hvedefrø fra græs er nogle gange også "
        "vilde.",
        "# Krydsning",
        "Når en plante modner, får den pollen fra en moden plante af samme slags inden for 2 blokke: majs, hamp, raps, "
        "lucerne, roe, gulerod og rødbede gør det 80% af gangene, de andre kun 5% (de bestøver sig selv). Hvert frø får "
        "så en kopi af hvert gen fra moderen og en fra faderen, tilfældigt. Et gen ud af 500 muterer. Kassava og kartoffel "
        "sættes af stykker af planten: de kopierer den præcist. Frø med forskellige gener stables ikke.",
        "Domesticering (niveau 1) lærer dig Frøluppen: brug den med frø i den anden hånd, eller på en afgrøde, for at se "
        "deres egenskaber. En plante med en recessiv form i én kopi ser almindelig ud: kun afkommet afslører det.",
        "Husdyrhold (niveau 1): Foderkrybben rummer en stak foder og fodrer dyrene inden for 6 blokke, der æder det: "
        "voksne formerer sig, og ungerne vokser hurtigere. Køer, får og geder æder lucerne, havre, byg, durra og roer; "
        "grise majs, kassava, sojabønner og roer; høns korn; heste havre og lucerne.",
        "Sædskifte (niveau 2): sojabønner, bønner, indigo og lucerne høstet modne efterlader jorden gødet. Naturlige "
        "farvestoffer (niveau 2): farvekarret laver blåt farvestof af indigoblade og rødt af kraprod.",
        "Plantekrydsning (niveau 3): Bestøvningspenslen tager pollen fra en moden plante; penslet på en plante af samme "
        "slags, hvor som helst, gør den den første til far til dens frø. Tobaksvand (niveau 3): tre tobaksblade og en "
        "flaske vand vasker larverne af afgrøderne omkring, hvor du bruger det.",
        "Mendels love (niveau 4): Frøluppen viser hvert gens to bogstaver, så Cc kan skelnes fra CC. Kryds to Cc-planter, "
        "og en fjerdedel af frøene bliver cc.",
        "# Skadedyr og sygdomme",
        "En plante, der vokser blandt flere end 20 af sin slags i et kvadrat på 9 gange 9, kan få larver eller rust. Den "
        "holder op med at vokse, giver en høst mindre og smitter naboerne. Blandede marker får intet. Bræk den, eller "
        "brug Tobaksvand eller Insect Spray mod larver (afgrøder på DDT-jord får dem aldrig) og bordeauxvæske mod rust. "
        "Spilreglen periodicElementsCropPests slår skadedyr og sygdomme fra.",
        "# Hvor de vokser vildt",
    ],
    "ru_ru": [
        "# Посевы",
        "В мире дико растут двадцать культур: зрелое растение на траве или песке, которое не растёт. Сломай его ради "
        "семян (маниок даёт черенки) и посей их на пашню, как пшеницу. Рису нужна вода прямо рядом с пашней. Кофе и "
        "люцерна остаются на месте: собирай их правым щелчком, когда созреют, и они отрастают снова.",
        "Каждая культура любит определённый климат (тёплый, умеренный или холодный). В другом она растёт вдвое медленнее, "
        "но растёт. На сухой земле (пустыня, саванна, бесплодные земли) поле без достатка воды тоже растёт вдвое "
        "медленнее, кроме засушливых культур (хлопок, клещевина, люцерна, сорго). Отапливаемая теплица считается тёплой.",
        "# Гены",
        "Каждое растение, и ванильные культуры тоже, несёт шесть генов, по две копии, по одной от каждого родителя. Они "
        "действуют с первого посева; исследования лишь позволяют их увидеть. Полезная форма каждого гена и как она "
        "проявляется:",
        "- C, Цикл: растения cc созревают примерно на 30% раньше (рецессивный: Cc выглядит обычно).",
        "- P, Урожайность: одного P хватает для 30% шанса на ещё один плод при сборе (доминантный).",
        "- R, Устойчивость к болезням: один R делает ржавчину вчетверо реже (доминантный).",
        "- D, Засухоустойчивость: растения dd растут в полную силу на сухой земле без воды (рецессивный).",
        "- F, Холодостойкость: один F позволяет культуре тёплых краёв расти в полную силу на холоде (доминантный).",
        "- I, Устойчивость к вредителям: один I делает гусениц вчетверо реже (доминантный).",
        "Семена из лавок, деревень и ванильных стопок несут обычные формы (CC pp rr DD ff ii). Дикие растения несут "
        "полезные формы, часто спрятанные в одной копии: отсюда и берётся улучшение. Семена пшеницы из травы иногда тоже "
        "дикие.",
        "# Скрещивание",
        "Созревая, растение получает пыльцу от зрелого растения своего вида не дальше 2 блоков: кукуруза, конопля, рапс, "
        "люцерна, турнепс, морковь и свёкла делают это в 80% случаев, остальные лишь в 5% (они самоопыляются). Каждое "
        "семя получает по одной копии каждого гена от матери и от отца, наугад. Один ген из 500 мутирует. Маниок и "
        "картофель сажают частями растения: они копируют его в точности. Семена с разными генами не складываются в стопку.",
        "Одомашнивание (уровень 1) даёт семенную лупу: примени её, держа семена в другой руке, или к посеву, чтобы "
        "увидеть их признаки. Растение с рецессивной формой в одной копии выглядит обычным: правду скажет лишь потомство.",
        "Скотоводство (уровень 1): кормушка вмещает стопку корма и кормит животных в 6 блоках, которые его едят: "
        "взрослые размножаются, а молодняк растёт быстрее. Коровы, овцы и козы едят люцерну, овёс, ячмень, сорго и "
        "турнепс; свиньи кукурузу, маниок, сою и турнепс; куры зерно; лошади овёс и люцерну.",
        "Севооборот (уровень 2): соя, фасоль, индигофера и люцерна, убранные зрелыми, оставляют пашню удобренной. "
        "Природные красители (уровень 2): красильный чан делает синий краситель из листьев индигоферы и красный из корня "
        "марены.",
        "Гибридизация растений (уровень 3): кисть для опыления берёт пыльцу со зрелого растения; нанесённая на растение "
        "того же вида, где угодно, она делает первое отцом его семян. Табачный настой (уровень 3): три листа табака и "
        "бутылка воды смывают гусениц с посевов вокруг места применения.",
        "Законы Менделя (уровень 4): семенная лупа показывает две буквы каждого гена, и Cc отличается от CC. Скрести два "
        "растения Cc, и четверть семян будет cc.",
        "# Вредители и болезни",
        "Растение среди более чем 20 растений своего вида в квадрате 9 на 9 может подхватить гусениц или ржавчину. Оно "
        "перестаёт расти, даёт на один плод меньше и заражает соседей. Смешанные поля ничего не подхватывают. Сломай "
        "его или применяй табачный настой или Insect Spray против гусениц (посевы на земле с ДДТ их никогда не "
        "подхватывают) и бордосскую жидкость против ржавчины. Игровое правило periodicElementsCropPests отключает "
        "вредителей и болезни.",
        "# Где растут дико",
    ],
}
# 3.37.0: crop genetics stages 2 and 3.
import genetics_i18n as _G  # noqa: E402

S.update(_G.S)
for _code in CODES:
    GUIDE_TEXT[_code][-1:-1] = _G.GUIDE[_code]
GUIDE = {code: {"CROPS": GUIDE_TEXT[code]} for code in CODES}


def text(code):
    i = CODES.index(code)
    out = {en: tr[i] for en, tr in S.items()}
    guide = GUIDE_TEXT[code]
    assert len(guide) == len(C.GUIDE), (code, len(guide), len(C.GUIDE))
    assert [p.startswith("# ") for p in guide] == [p.startswith("# ") for p in C.GUIDE], (code, "headings")
    return out
