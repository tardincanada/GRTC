"""Buskar, etapp 2 och 3 (3.22.0), på svenska. Se shrubs.py och shrubs_i18n.py."""

NAMES = {
    "sweet_briar": ("Äppelros", "Äppelros", "Stickling av äppelros"),
    "blackthorn": ("Slån", "Slånbuske", "Stickling av slån"),
    "cornelian_cherry": ("Körsbärskornell", "Körsbärskornell", "Stickling av körsbärskornell"),
    "raspberry": ("Hallon", "Hallonbuske", "Stickling av hallon"),
    "blackberry": ("Björnbär", "Björnbärsbuske", "Stickling av björnbär"),
    "blackcurrant": ("Svarta vinbär", "Svartvinbärsbuske", "Stickling av svarta vinbär"),
    "bilberry": ("Blåbär", "Blåbärsris", "Stickling av blåbär"),
    "damask_rose": ("Damascenerros", "Damascenerros", "Stickling av damascenerros"),
    "mock_orange": ("Doftschersmin", "Doftschersmin", "Stickling av schersmin"),
    "rhododendron": ("Pontisk rododendron", "Rododendron", "Stickling av rododendron"),
    "box": ("Buxbom", "Buxbom", "Stickling av buxbom"),
    "butchers_broom": ("Stickmyrten", "Stickmyrten", "Stickling av stickmyrten"),
    "wax_myrtle": ("Vaxpors", "Vaxpors", "Stickling av vaxpors"),
    "cranberry": ("Storfruktigt tranbär", "Tranbär", "Stickling av tranbär"),
    "buttonbush": ("Knappbuske", "Knappbuske", "Stickling av knappbuske"),
    "japanese_camellia": ("Japansk kamelia", "Japansk kamelia", "Stickling av kamelia"),
    "japanese_quince": ("Liten rosenkvitten", "Rosenkvitten", "Stickling av rosenkvitten"),
    "hydrangea": ("Hortensia", "Hortensia", "Stickling av hortensia"),
    "rosemary": ("Rosmarin", "Rosmarin", "Stickling av rosmarin"),
    "lavender": ("Lavendel", "Lavendel", "Stickling av lavendel"),
    "myrtle": ("Myrten", "Myrten", "Stickling av myrten"),
    "caper": ("Kapris", "Kaprisbuske", "Stickling av kapris"),
    "rockrose": ("Ladanumcistros", "Cistros", "Stickling av cistros"),
    "sumac": ("Garvarsumak", "Sumak", "Stickling av sumak"),
    "heather": ("Ljung", "Ljung", "Stickling av ljung"),
    "gorse": ("Ärttörne", "Ärttörne", "Stickling av ärttörne"),
    "broom": ("Harris", "Harris", "Stickling av harris"),
    "rugosa_rose": ("Vresros", "Vresros", "Stickling av vresros"),
    "beach_plum": ("Strandplommon", "Strandplommon", "Stickling av strandplommon"),
    "bog_bilberry": ("Odon", "Odon", "Stickling av odon"),
    "labrador_tea": ("Labradorte", "Skvattram", "Stickling av skvattram"),
    "crowberry": ("Kråkbär", "Kråkris", "Stickling av kråkbär"),
    "cloudberry": ("Hjortron", "Hjortron", "Stickling av hjortron"),
    "dwarf_birch": ("Dvärgbjörk", "Dvärgbjörk", "Stickling av dvärgbjörk"),
    "bearberry": ("Mjölon", "Mjölon", "Stickling av mjölon"),
    "alpenrose": ("Alpros", "Alpros", "Stickling av alpros"),
    "mountain_avens": ("Fjällsippa", "Fjällsippa", "Stickling av fjällsippa"),
    "dwarf_willow": ("Dvärgvide", "Dvärgvide", "Stickling av dvärgvide"),
    "arctic_bell_heather": ("Kantljung", "Kantljung", "Stickling av kantljung"),
    "gooseberry": ("Krusbär", "Krusbärsbuske", "Stickling av krusbär"),
    "red_elderberry": ("Druvfläder", "Druvfläder", "Stickling av druvfläder"),
}

PRODUCTS = {
    "rose_hips": "Nypon", "sloes": "Slånbär", "cornelian_cherries": "Kornellkörsbär", "raspberries": "Hallon",
    "blackberries": "Björnbär", "blackcurrants": "Svarta vinbär", "bilberries": "Blåbär",
    "rose_petals": "Damascenerrosblad", "rhododendron_flowers": "Rododendronblommor",
    "butchers_broom_berries": "Stickmyrtenbär", "bayberries": "Vaxporsbär", "cranberries": "Tranbär",
    "camellia_seeds": "Kameliafrön", "japanese_quinces": "Rosenkvitten", "rosemary_leaf": "Rosmarinkvistar",
    "lavender_flowers": "Lavendelblommor", "myrtle_berries": "Myrtenbär", "capers": "Kapris",
    "labdanum": "Labdanum", "sumac_leaf": "Sumakblad", "heather_flowers": "Ljungblommor",
    "broom_twigs": "Harriskvistar", "beach_plums": "Strandplommon", "bog_bilberries": "Odon",
    "labrador_tea_leaf": "Skvattramblad", "crowberries": "Kråkbär", "cloudberries": "Hjortron",
    "bearberry_leaf": "Mjölonblad", "dwarf_willow_bark": "Dvärgvidebark", "gooseberries": "Krusbär",
    "red_elderberries": "Druvflädebär",
    "black_tea": "Svart te", "rooibos_tea": "Rooiboste", "hibiscus_tea": "Hibiskuste",
    "rosehip_tea": "Nyponte", "lavender_tea": "Lavendelte", "rosemary_tea": "Rosmarinte",
    "heather_tea": "Ljungte", "labrador_tea": "Skvattramte",
}

ABOUT = {
    "sweet_briar": ("En taggig vildros vars blad doftar äpple efter regn. Dess scharlakansröda nypon har mer C-vitamin än "
                    "apelsiner; i krigets Storbritannien plockade barn dem till sirap.", "Europa och västra Asien"),
    "blackthorn": ("En tät häckbuske med långa törnen och vita blommor före bladen. Dess blåsvarta slånbär är för sträva före "
                   "frosten och smaksätter likör.", "Europa och västra Asien"),
    "cornelian_cherry": ("En kornell som blommar gult i slutet av vintern och bär blanka, sura röda frukter. Veden är så tät "
                         "att den sjunker; grekerna gjorde spjutskaft av den.", "Sydeuropa och västra Asien"),
    "raspberry": ("Dess skott lever två år: första året växer de, andra bär de frukt och dör. Hallonet lossnar ihåligt från "
                  "fästet, till skillnad från björnbäret.", "Europa och norra Asien"),
    "blackberry": ("Björnbärssnåret: taggiga bågskott som slår rot där spetsen når marken och tar över varje glänta. Bären har "
                   "födt människor sedan stenåldern.", "Europa"),
    "blackcurrant": ("Dess starkt doftande bär är rika på C-vitamin; under andra världskriget gav Storbritannien barn "
                     "svartvinbärssirap gratis.", "Nordeuropa och Asien"),
    "bilberry": ("Ett knähögt ris på sur skogsmark. Till skillnad från odlade amerikanska blåbär är bäret blått rakt igenom "
                 "och färgar mun och fingrar.", "Europa och norra Asien"),
    "damask_rose": ("Parfymens ros: i Bulgariens Rosendal destilleras omkring 3 000 kilo kronblad till ett kilo rosenolja, "
                    "rik på geraniol.", "Trädgårdsros från Syrien och Persien"),
    "mock_orange": ("Tidigt på sommaren täcks den av vita blommor som doftar apelsinblom. Dess ihåliga stjälkar blev pipskaft.",
                    "Sydeuropa och Kaukasus"),
    "rhododendron": ("Allt i den är giftigt, även honungen från blommorna: 'galen honung' fällde Xenofons soldater år 401 f.Kr. "
                     "Dess lila blommor ger ett färgämne.", "Iberiska halvön, Kaukasus och Svarta havets kust"),
    "box": ("En långsam vintergrön buske, klippt till häckar sedan romarnas trädgårdar. Dess gula ved är Europas hårdaste, för "
            "schackpjäser, linjaler och träsnitt.", "Europa, Nordafrika och västra Asien"),
    "butchers_broom": ("Dess stickiga 'blad' är plattade stjälkar med en blomma mitt på var och en. Slaktare band de styva "
                       "skotten till kvastar att skura huggkubbarna med.", "Europa, Nordafrika och Mellanöstern"),
    "wax_myrtle": ("Dess grå bär är täckta av vax. Amerikanska nybyggare kokade dem och skummade av vaxet till ljus med ren "
                   "doft.", "Östra Nordamerika och Karibien"),
    "cranberry": ("Ett krypande ris i myrar. Bären har luftfickor, så odlarna dränker fälten och bären flyter upp till "
                  "skörden.", "Myrar i nordöstra Nordamerika"),
    "buttonbush": ("Den står med fötterna i vatten och bär runda vita blomhuvuden som nåldynor, älskade av bin och fjärilar.",
                   "Östra Nordamerika"),
    "japanese_camellia": ("Tebuskens kusin, med blanka blad och röda blommor som faller hela. Japanska kvinnor vårdade håret "
                          "med tsubakiolja pressad ur fröna.", "Japan, Korea och östra Kina"),
    "japanese_quince": ("En låg, taggig buske med orangeröda blommor på kala grenar. Dess små hårda frukter är för sura råa men "
                        "doftar genom ett helt rum.", "Japan"),
    "hydrangea": ("Blommorna är rosa på kalkjord och blå på sur jord, där aluminium löses och färgar dem. Vattna med "
                  "aluminiumsulfat för blått eller med kalk för rosa.", "Japan"),
    "rosemary": ("'Havets dagg': en kådig buske på klippor vid havet. Grekiska elever bar den när de pluggade, för minnet. Dess "
                 "olja är mest eukalyptol och kamfer.", "Medelhavets kuster"),
    "lavender": ("Namnet kommer av latinets ord för att tvätta: romarna doftsatte sina bad med den. Oljan ur blommorna är mest "
                 "linalool.", "Bergen kring västra Medelhavet"),
    "myrtle": ("Afrodites vintergröna, buren i brudkransar. På Sardinien läggs de mörka bären i sprit till myrtenlikören mirto.",
               "Medelhavsområdet"),
    "caper": ("En utbredd taggig buske som växer ur gamla murar och klippor. Kapris är dess blomknoppar, plockade innan de slår "
              "ut och inlagda i salt.", "Medelhavet till Centralasien"),
    "rockrose": ("Dess klibbiga blad svettas ut en doftande kåda, labdanum. Kretensiska herdar kammade den ur getternas skägg; "
                 "parfymörer använder den ännu.", "Iberiska halvön och nordvästra Afrika"),
    "sumac": ("Bladen är till en fjärdedel garvämne: det fina marockanska lädret garvades med dem. De sura röda bären är "
              "Mellanösterns krydda.", "Medelhavet till Iran"),
    "heather": ("Hedarnas ljung, bränd fläckvis för att hållas ung åt orrar och får. Den gav kvastar, tak och strö, färgade ull "
                "gul och blev öl och te.", "Europa"),
    "gorse": ("Törnen i stället för blad och gula blommor som doftar kokos nästan hela året. Den oljiga veden brinner hett: "
              "bagarugnarnas gamla bränsle.", "Västeuropa"),
    "broom": ("Gröna, smidiga kvistar som blev kvastar. Plantagenet-kungarna fick sitt namn av den, planta genista; fibrerna "
              "spanns till grovt tyg.", "Väst- och Centraleuropa"),
    "rugosa_rose": ("En borstig ros på sanddyner som tål saltstänk och frost. Dess stora tomatröda nypon är de C-vitaminrikaste "
                    "av alla rosor.", "Kusterna av Japan, Korea, nordöstra Kina och ryska Fjärran östern"),
    "beach_plum": ("En buske på New Englands dyner vars små mörka plommon plockas varje höst till sylt.",
                   "Nordamerikas atlantkust"),
    "bog_bilberry": ("Blåbärets gråbladiga kusin från myrar och tundra. Bären är vita inuti och sades ge huvudvärk, men det är "
                     "skvattrammen intill som bär skulden.", "Kalla trakter runt hela norra halvklotet"),
    "labrador_tea": ("Läderartade blad, rostbruna och ulliga undertill. Nordens folk och pälsjägare kokade te av dem; starkt "
                     "berusar det, och i Skandinavien höll man mal borta med det.", "Norra Europa, Asien och Nordamerika"),
    "crowberry": ("En matta av barrlika blad på tundran. De svarta bären är vattniga och kärniga men håller sig under snön hela "
                  "vintern och föder fåglar, björnar och samer.", "Arktis och nordliga fjäll"),
    "cloudberry": ("Ett handhögt björnbär med ett enda bärnstensgult bär per stjälk. I Finland och Norge är det dyraste vilda "
                   "bäret, och det finns på den finska tvåeuromyntet.", "Arktiska och boreala myrar"),
    "dwarf_birch": ("En björk som kryper längs marken, i lä för vinden, med blad stora som ett mynt. Den är tundrans ved.",
                    "Arktisk tundra och nordliga fjäll"),
    "bearberry": ("En krypande vintergrön vars mjöliga röda bär björnar älskar. Bladen är rika på arbutin och garvämne: medicin "
                  "för urinblåsan och garvning av ryssläder.", "Hela norden, på fjäll"),
    "alpenrose": ("Alpernas ros, en rododendron som i juli färgar hela sluttningar röda ovanför trädgränsen. Bladen är "
                  "rostbruna undertill.", "Alperna och Pyrenéerna"),
    "mountain_avens": ("Vita blommor med åtta kronblad som vänder sig efter solen. Dess pollen i sjöslam markerar köldperioderna "
                       "i slutet av senaste istiden, yngre dryas.", "Arktis och högfjäll"),
    "dwarf_willow": ("Kanske världens minsta träd: ett vide några centimeter högt vars stam löper under marken. Barken har "
                     "salicin som alla viden.", "Arktis och högfjäll"),
    "arctic_bell_heather": ("Fyrkantiga skott av små fjällika blad och vita klockor. Rik på kåda brinner den även blöt: inuiternas "
                            "bränsle i högarktis.", "Högarktis"),
    "gooseberry": ("En taggig buske i bergsskogar. Engelska odlare höll krusbärsutställningar sedan 1700-talet, om vem som "
                   "odlade det tyngsta bäret.", "Europa, Kaukasus och Nordafrika"),
    "red_elderberry": ("Dess röda bär gör en sjuk råa, men kokta gav de gelé och vin åt folken på Stillahavskusten.",
                       "Svala skogar och berg på norra halvklotet"),
}

SUBSTANCES = {
    "geraniol": ("Geraniol", "Doften i rosenolja: parfymer och insektsmedel"),
    "arbutin": ("Arbutin", "Mjölonets glukosid: urinvägsantiseptikum och hudblekare"),
    "vitamin_c": ("C-vitamin", "Askorbinsyra: botar skörbjugg och hindrar mat från att bli brun"),
}

REACTIONS = {
    "geraniol_distillation": "Rosenblad destillerade med ånga: rosenolja går över, rik på geraniol",
    "vitamin_c_extraction": "Nypon, vinbär eller hjortron pressade och saften kallt indunstad: askorbinsyra kristalliserar "
                            "(Szent-Györgyi 1928)",
    "arbutin_extraction": "Mjölonblad dragna i hett vatten, garvämnet borttaget med blyacetat: arbutin kristalliserar",
}

RESEARCH = {
    "cold_greenhouse": ("Kallt växthus", "Lindes kylmaskin (1876) håller ett glashus kallt: arktiska växter och fjällväxter "
                        "växer var som helst; is duger så länge den räcker"),
    "vitamins": ("Vitaminer", "Szent-Györgyi utvann hexuronsyra ur paprika (1928): C-vitaminet som botar skörbjugg, och med "
                 "samma metoder arbutin ur mjölon"),
}

UI = [
    "eller i ett kylt växthus",
    "Växthuskylare: %s s is kvar",
    "Växthuskylare: går på ström",
    "Växthuskylare: ingen is och ingen ström; lägg i is eller koppla den med tråd till en dynamo",
    "Kylt växthus",
    "Botar: %s",
    "Hortensian kommer att blomma blått",
    "Hortensian kommer att blomma rosa",
    "Växthuskylare",
    "Kyler växthuset den står i för arktiska växter och fjällväxter; den smälter is som läggs i, eller går på ström från en tråd",
]

GUIDE = [
    "# Taggar",
    "Björnbär, slån, rosor, ärttörne, kapris och krusbär sticker den som går igenom dem, lite i taget, som en "
    "sötbärsbuske. Rävar och bin går igenom oskadda.",
    "# Te",
    "Två blad eller blommor och en vattenflaska blir ett te. Varje te ger en mild effekt en kort stund och botar en dålig "
    "effekt, som mjölk botar alla: svart te ger brådska och botar gruvtrötthet; rooibos, motstånd (hunger); hibiskus, "
    "eldmotstånd (svaghet); nypon, absorption (gift); lavendel, återhämtning (illamående); rosmarin, mörkerseende "
    "(blindhet); ljung, snabbhet (långsamhet); skvattram, hälsoboost (förtvining).",
    "# Hortensia",
    "Hortensian blommar rosa på kalkjord och blått på sur jord, där aluminium löses i den. Använd aluminiumsulfat eller "
    "alun på den för blått, och bränd kalk, släckt kalk eller krita för rosa.",
    "# Kalla växthus",
    "Arktiska växter och fjällväxter vill ha ett kylt växthus. En växthuskylare som går, på golvet eller i en vägg, gör det "
    "kallt (Kallt växthus, nivå 4). Den smälter is som läggs i: ett snöblock räcker 30 sekunder, is 2 minuter, packad is "
    "18 minuter, blå is en timme. Utan is går den på ström från en koppartråd, 20 kilowatt. Växter från tempererade trakter "
    "växer där också; en värmare och en kylare i samma rum tar ut varandra.",
]

T = {"shrubs_cold": "kylt växthus"}
