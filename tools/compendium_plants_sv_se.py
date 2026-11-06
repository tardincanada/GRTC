"""Växtkompendium (3.5.0) på svenska. Se compendium_plants.py och compendium_i18n.py."""

TITLE = "Växtkompendium"

GUIDE = [
    "# Växtkompendiet",
    "En bok och vilken liten blomma som helst blir ett Växtkompendium; ingen forskning behövs. Det fungerar som "
    "Trädkompendiet: ha det i snabbfältet så visar övre vänstra hörnet biomet och, när du väl känner den, växten du "
    "tittar på. Blommor, gräs, ormbunkar, buskar, grödor, vattenväxter och växterna i grönskande grottor finns alla "
    "med.",
    "För att anteckna en växt, titta på den en stund från högst 8 block, eller plocka eller skörda den, med boken i "
    "snabbfältet; växter under vatten antecknas på samma sätt. Använd boken för att läsa: biom till vänster, varje "
    "bioms växter till höger.",
    "Kompendieanteckningar kopierar båda böckerna på en gång: den som läser dem får alla träd, växter och biom i dem.",
    "Species Plantarum (nivå 3): varje växt visar sitt vetenskapliga namn, sin familj och var den växer vild i den "
    "verkliga världen.",
    "Växtgeografi (nivå 3): varje biom visar sitt klimat, i den här boken som i Trädkompendiet.",
    "Rödlistan (nivå 6): varje växt visar hur hotad den är i det vilda.",
]

UI = [
    "Växtkompendium",
    "Okänd växt",
    "Ny växt i ditt Kompendium: %s",
    "%s av %s växter och %s av %s biom antecknade.",
    "Ha boken i snabbfältet. Titta på en växt en stund, eller plocka eller skörda den, för att anteckna den; gå in i "
    "ett biom för att anteckna biomet. Välj ett biom till vänster.",
    "Växter",
    "En växt som bara finns i den här världen: ingen verklig art motsvarar den.",
    "Växtkompendium",
    "I snabbfältet visar det biomet och namnger växterna du känner; använd det för att läsa om dem",
]

PLANTS = {
    "dandelion": ("Maskros", "Europa och Asien; nu på alla kontinenter utom Antarktis",
                  "Den gula blomkorgen sluter sig om natten och mognar till en fröboll som vinden bär i "
                  "kilometer. Allt på den går att äta, och bladen var vårsallad långt före salladen."),
    "poppy": ("Vallmo", "Europa, Nordafrika och Västasien",
              "Kornvallmon följer plogen: fröna väntar i årtionden i jorden och gror när marken vänds, därför täckte "
              "den Flanderns slagfält efter 1914."),
    "blue_orchid": ("Blå orkidé", "Bergsskogar i Assam, Myanmar och norra Thailand",
                    "En av mycket få verkligt blå orkidéer. I det vilda klamrar den sig fast på stammar i svala "
                    "bergsskogar; samlare har tömt hela dalar, och handeln med vilda plantor är nu förbjuden."),
    "allium": ("Allium", "Berg i Iran, Afghanistan och Centralasien",
               "En prydnadslök: en boll av hundratals små lila blommor på en hög stjälk. Krossa ett blad och det "
               "luktar lök, som släktingarna vitlök och purjo."),
    "azure_bluet": ("Houstonia", "Östra Nordamerika",
                    "En pytteliten blomma med fyra kronblad som täcker gräsmattor och ängar med ljusblått på våren. "
                    "Liten som den är hör den till kaffefamiljen."),
    "tulip": ("Tulpan", "Bergsstäpper i Turkiet och Centralasien",
              "Förd från Osmanska riket till Nederländerna på 1500-talet; 1637 kunde en enda lök kosta mer än ett hus, "
              "tills marknaden kraschade. De eftertraktade randiga kronbladen orsakades av ett virus."),
    "oxeye_daisy": ("Prästkrage", "Europa och tempererade Asien",
                    "Ängarnas och vägkanternas vita prästkrage. Varje blomma är egentligen hundratals små blommor: gula "
                    "i mitten, vita runt kanten."),
    "cornflower": ("Blåklint", "Sydöstra Europa och Mellanöstern, spridd med säden över hela Europa",
                   "En gång ogräs i varje vetefält; den försvann nästan från Europa när bönderna började rensa utsädet "
                   "och spruta ogräsmedel. Dess blå gav namn åt en färg."),
    "lily_of_the_valley": ("Liljekonvalj", "Svala skogar i hela Europa och Asien",
                           "Doftande vita klockor i skuggiga vårskogar. Hela växten är giftig: den innehåller "
                           "hjärtglykosider som fingerborgsblomman, som medicinen använder vid hjärtsvikt."),
    "sunflower": ("Solros", "Nordamerika; odlad i Mexiko för omkring 4 000 år sedan",
                  "Unga blomkorgar följer solen från öst till väst varje dag; öppna blommor vänder sig mot öster för "
                  "gott. Ur fröna pressas en av världens viktigaste matoljor."),
    "lilac": ("Syren", "Steniga kullar på Balkan",
              "En buske i olivfamiljen med doftande lila blomklasar sent på våren. Vild växer den bara på Balkans "
              "sluttningar, men trädgårdarna har spridit den över världen."),
    "rose_bush": ("Rosenbuske", "Europa, nordvästra Afrika och Västasien",
                  "Stenrosen klättrar genom häckar med krokiga taggar. Dess röda nypon har mer C-vitamin än apelsiner; "
                  "i krigets Storbritannien plockade barn dem till sirap."),
    "peony": ("Pion", "Sibirien, Mongoliet och norra Kina",
              "Stora rosa blommor vars knoppar utsöndrar socker som myror kommer och dricker. Odlad i Kina i över "
              "tusen år kallas den där blommornas drottning."),
    "pitcher_plant": ("Flugtrumpet", "Myrar i östra Nordamerika",
                      "En köttätande växt på mager, blöt mark: bladen är kannor med regnvatten där insekter drunknar "
                      "och bryts ned. I den här världen växer den bara ur urgamla frön som sniffare gräver upp."),
    "grass": ("Gräs", "Europa, norra Asien och Nordamerika",
              "Ängsgröe, betesmarkernas och gräsmattornas gräs. Gräs växer från bladets bas, inte från spetsen, och "
              "klarar därför bete och klippning som skulle döda de flesta växter."),
    "fern": ("Ormbunke", "Nästan hela världen, på alla kontinenter utom Antarktis",
             "Örnbräken är äldre än någon blomma: den har varken blommor eller frön och sprider sig med sporer och "
             "underjordiska stammar. Bladen är giftiga för boskap och hästar."),
    "dead_bush": ("Död buske", "Öknar i sydvästra USA och norra Mexiko",
                  "Inte död, bara väntande: kreosotbusken slutar växa i torka och grönskar efter regn. En ring av dess "
                  "kloner i Mojaveöknen, King Clone, är omkring 11 700 år gammal."),
    "sweet_berry_bush": ("Sötbärsbuske", "Nordliga skogar och tundra i Europa, Asien och Nordamerika",
                         "Lingonet, en låg vintergrön buske i den boreala skogen. Dess syrliga röda bär håller sig hela "
                         "vintern tack vare sin egen bensoesyra."),
    "bamboo": ("Bambu", "Centrala och södra Kina",
               "Ett jättegräs: ett moso-bambuskott kan växa en meter på en dag och når full höjd på två månader. Ett "
               "helt bestånd blommar en gång på många årtionden, allt på en gång, och dör sedan."),
    "cactus": ("Kaktus", "Sonoraöknen i Arizona och Mexiko",
               "Saguaron lagrar vatten i en veckad stam som sväller efter regn; en fullvuxen rymmer över ett ton. Den "
               "lever upp till 150 år och får sin första arm vid omkring 70."),
    "sugar_cane": ("Sockerrör", "Nya Guinea, där det först odlades för omkring 8 000 år sedan",
                   "Ett jättegräs vars stjälkar är fulla av söt saft; nedkokt ger den det mesta av världens socker. "
                   "Plantagerna i Amerika byggdes på den och på förslavade människors tvångsarbete."),
    "moss": ("Mossa", "Fuktig mark i hela den tempererade världen",
             "Björnmossa, en av de högsta mossorna. Mossor har varken rötter, blommor eller frön; de dricker genom "
             "bladen och behöver en vattenhinna för att spermierna ska kunna simma till äggcellerna."),
    "wheat": ("Vete", "Bördiga halvmånen, ur tre vilda gräs korsade för omkring 9 000 år sedan",
              "Brödvete bär hela arvsmassan från tre vilda förfäder. Det ger en femtedel av mänsklighetens kalorier, "
              "och dess gluten låter degen töjas och hålla gasen i jäsande bröd."),
    "carrot": ("Morot", "Vild i Europa och sydvästra Asien; först odlad för roten i Afghanistan",
               "De första ätbara morötterna var lila eller gula; orange förädlades fram i Nederländerna på 1600-talet. "
               "Det orange är betakaroten, som kroppen gör om till A-vitamin."),
    "potato": ("Potatis", "Anderna i Peru och Bolivia",
               "Andinska bönder odlade tusentals sorter; Europa planterade bara ett fåtal, och när potatisbladmögeln "
               "slog till 1845 förlorade Irland en miljon människor i svält. Grön potatis innehåller giftet solanin."),
    "beetroot": ("Rödbeta", "Medelhavets och Atlantens kuster, som vild strandbeta",
                 "Rödbeta, mangold och sockerbeta är en art förädlad på tre sätt. Sockerbetan, förädlad i Preussen på "
                 "1700-talet, ger i dag en femtedel av världens socker."),
    "melon": ("Vattenmelon", "Nordöstra Afrika",
              "Vattenmelonen består till omkring nio tiondelar av vatten. Dess frön lades i egyptiska gravar, och "
              "ökenresenärer bar den som vattenflaska."),
    "pumpkin": ("Pumpa", "Argentina och Uruguay",
                "En krypande reva vars jättepumpor har passerat 1 200 kilo, de tyngsta frukterna av alla växter. "
                "Amerikas folk odlade pumpor tillsammans med majs och bönor."),
    "cocoa": ("Kakao", "Övre Amazonasbäckenet",
              "Ett litet träd i regnskogens skugga vars frukter växer direkt på stammen. Mayafolket och aztekerna drack "
              "dess rostade, malda frön; namnet betyder gudarnas föda."),
    "kelp": ("Kelp", "Kalla kuster i Stilla havet och de södra haven",
             "Jättekelp är ingen växt utan en brunalg. Gasfyllda blåsor håller bladen uppe, och den kan växa en halv "
             "meter om dagen till undervattensskogar fulla av fisk och havsuttrar."),
    "seagrass": ("Sjögräs", "Grunda kuster i norra Atlanten och norra Stilla havet",
                 "En blomväxt som återvände till havet och till och med blommar under vatten. Dess ängar skyddar "
                 "ungfisk och begraver kol i dyn."),
    "lily_pad": ("Näckrosblad", "Sjöar och dammar i Europa, Nordafrika och Mellanöstern",
                 "Den vita näckrosen rotar sig i dyn och låter bladen flyta på långa skaft, med luftkanaler som för "
                 "syre ned till rötterna. Bladen andas genom ovansidan."),
    "torchflower": ("Fackelblomma", None,
                    "En urgammal blomma från tiden före världens början; sniffare gräver upp dess frön ur sanden vid "
                    "ruiner i varma hav. Den liknar en låga men ger inget ljus."),
    "pink_petals": ("Rosa kronblad", None,
                    "Mattor av små rosa blommor under körsbärsträd, upp till fyra per block. Ingen verklig blomma "
                    "växer så: de ser ut som fallna körsbärsblommor som har slagit rot."),
    "spore_blossom": ("Sporblomma", None,
                      "En blomma som hänger från grottak och släpper ett långsamt regn av gröna sporer. Blommor bildar "
                      "pollen och frön, inte sporer; den här behöver inget solljus alls."),
    "dripleaf": ("Droppblad", None,
                 "Breda blad på höga stjälkar som tippar en stund efter att något ställt sig på dem, som en fallucka. "
                 "Den växer i leran och vattnet i grönskande grottor."),
    "glow_berries": ("Glödbär", None,
                     "Revor som hänger från grottak med bär som lyser av egen kraft. Eldflugor och vissa svampar lyser, "
                     "men ingen verklig frukt."),
    "glow_lichen": ("Glödlav", None,
                    "En lav som glimmar på grottväggar. Verkliga lavar är samarbeten mellan en svamp och en alg eller "
                    "en bakterie; ingen av dem lyser."),
    "vine": ("Klätterväxter", None,
             "Lövrika revor utan blommor eller frukt som hänger från djungelträd och träskekar och växer nedåt. "
             "Verkliga klätterväxter växer uppåt mot ljuset."),
}

BIOMES = {
    "river": "En flod: sötvatten på väg mot havet, med vass längs stränderna och vattenväxter på botten.",
    "ocean": "Öppet tempererat hav som norra Atlanten: svalt vatten som stormar rör om, med kelp och sjögräs nära "
             "kusterna.",
    "cold_ocean": "Kallt hav som norra Stilla havet: mörkt, näringsrikt vatten där kelp växer till skogar.",
    "lukewarm_ocean": "Milt hav som Medelhavet: klart, salt vatten med sjögräsängar på sandbotten.",
    "warm_ocean": "Tropiskt hav som Karibien: varmt, klart vatten över korallrev och sjögräsängar.",
}
