"""Plantekompendium (3.5.0) på dansk. Se compendium_plants.py og compendium_i18n.py."""

TITLE = "Plantekompendium"

GUIDE = [
    "# Plantekompendiet",
    "En bog og en hvilken som helst lille blomst bliver til et Plantekompendium; der kræves ingen forskning. Det "
    "virker som Trækompendiet: hav det i hurtigbjælken, så viser øverste venstre hjørne biomet og, når du kender den, "
    "planten du ser på. Blomster, græsser, bregner, buske, afgrøder, vandplanter og planterne i frodige huler er alle "
    "med.",
    "For at notere en plante skal du se på den et øjeblik fra højst 8 blokke, eller plukke eller høste den, med bogen i "
    "hurtigbjælken; planter under vand noteres på samme måde. Brug bogen til at læse: biomer til venstre, hvert bioms "
    "planter til højre.",
    "Kompendienoter kopierer begge bøger på én gang: den, der læser dem, får alle træer, planter og biomer i dem.",
    "Species Plantarum (niveau 3): hver plante viser sit videnskabelige navn, sin familie og hvor den vokser vildt i "
    "den virkelige verden.",
    "Plantegeografi (niveau 3): hvert biom viser sit klima, i denne bog som i Trækompendiet.",
    "Den Røde Liste (niveau 6): hver plante viser, hvor truet den er i naturen.",
]

UI = [
    "Plantekompendium",
    "Ukendt plante",
    "Ny plante i dit Kompendium: %s",
    "%s af %s planter og %s af %s biomer noteret.",
    "Hav bogen i hurtigbjælken. Se på en plante et øjeblik, eller pluk eller høst den, for at notere den; gå ind i et "
    "biom for at notere biomet. Vælg et biom til venstre.",
    "Planter",
    "En plante kun fra denne verden: ingen virkelig art svarer til den.",
    "Plantekompendium",
    "I hurtigbjælken viser det biomet og navngiver de planter, du kender; brug det til at læse om dem",
]

PLANTS = {
    "dandelion": ("Mælkebøtte", "Europa og Asien; nu på alle kontinenter undtagen Antarktis",
                  "Dens gule blomsterhoved lukker sig om natten og modner til en frøbold, som vinden bærer kilometer "
                  "væk. Alt på den kan spises, og bladene var forårssalat længe før hovedsalaten."),
    "poppy": ("Valmue", "Europa, Nordafrika og Vestasien",
              "Kornvalmuen følger ploven: frøene venter i årtier i jorden og spirer, når jorden vendes, og derfor "
              "dækkede den Flanderns slagmarker efter 1914."),
    "blue_orchid": ("Blå orkidé", "Bjergskove i Assam, Myanmar og det nordlige Thailand",
                    "En af ganske få virkelig blå orkidéer. I naturen klamrer den sig til stammer i kølige bjergskove; "
                    "samlere har tømt hele dale, og handel med vilde planter er nu forbudt."),
    "allium": ("Allium", "Bjerge i Iran, Afghanistan og Centralasien",
               "Et prydløg: en kugle af hundredvis af små lilla blomster på en høj stængel. Knus et blad, og det "
               "lugter af løg, som slægtningene hvidløg og porre."),
    "azure_bluet": ("Houstonia", "Det østlige Nordamerika",
                    "En bitte blomst med fire kronblade, der dækker græsplæner og enge med lyseblåt om foråret. Så "
                    "lille den er, hører den til kaffefamilien."),
    "tulip": ("Tulipan", "Bjergstepper i Tyrkiet og Centralasien",
              "Bragt fra Det Osmanniske Rige til Nederlandene i 1500-tallet; i 1637 kunne et enkelt løg koste mere "
              "end et hus, indtil markedet krakkede. De eftertragtede stribede kronblade skyldtes en virus."),
    "oxeye_daisy": ("Hvid okseøje", "Europa og det tempererede Asien",
                    "Engenes og vejkanternes hvide marguerit. Hver blomst er i virkeligheden hundredvis af små "
                    "blomster: gule i midten, hvide langs kanten."),
    "cornflower": ("Kornblomst", "Sydøsteuropa og Mellemøsten, spredt med kornet over hele Europa",
                   "Engang ukrudt i hver hvedemark; den forsvandt næsten fra Europa, da landmændene begyndte at rense "
                   "udsæden og sprøjte. Dens blå har givet navn til en farve."),
    "lily_of_the_valley": ("Liljekonval", "Kølige skove i hele Europa og Asien",
                           "Duftende hvide klokker i skyggefulde forårsskove. Hele planten er giftig: den indeholder "
                           "hjerteglykosider som fingerbøl, som lægevidenskaben bruger mod hjertesvigt."),
    "sunflower": ("Solsikke", "Nordamerika; dyrket i Mexico for omkring 4.000 år siden",
                  "Unge blomsterhoveder følger solen fra øst til vest hver dag; åbne blomster vender mod øst for "
                  "altid. Af frøene presses en af verdens vigtigste madolier."),
    "lilac": ("Syren", "Klippefyldte bakker på Balkan",
              "En busk i olivenfamilien med duftende lilla blomsterklaser sidst på foråret. Vild vokser den kun på "
              "Balkans skråninger, men haverne har bragt den verden rundt."),
    "rose_bush": ("Rosenbusk", "Europa, Nordvestafrika og Vestasien",
                  "Hunderosen klatrer gennem hække med krogede torne. Dens røde hyben har mere C-vitamin end "
                  "appelsiner; i krigens Storbritannien plukkede børn dem til sirup."),
    "peony": ("Pæon", "Sibirien, Mongoliet og det nordlige Kina",
              "Store lyserøde blomster, hvis knopper udskiller sukker, som myrer kommer for at drikke. Dyrket i Kina i "
              "over tusind år kaldes den der blomsternes dronning."),
    "pitcher_plant": ("Kandeplante", "Moser i det østlige Nordamerika",
                      "En kødædende plante på mager, våd jord: bladene er kander med regnvand, hvor insekter drukner og "
                      "fordøjes. I denne verden vokser den kun af ældgamle frø, som snusere graver op."),
    "grass": ("Græs", "Europa, Nordasien og Nordamerika",
              "Engrapgræs, græsset på græsgange og plæner. Græsser vokser fra bladets grund, ikke fra spidsen, og "
              "overlever derfor afgræsning og slåning, som ville dræbe de fleste planter."),
    "fern": ("Bregne", "Næsten hele verden, på alle kontinenter undtagen Antarktis",
             "Ørnebregne er ældre end nogen blomst: den har hverken blomster eller frø og spreder sig med sporer og "
             "underjordiske stængler. Bladene er giftige for kvæg og heste."),
    "dead_bush": ("Død busk", "Ørkener i det sydvestlige USA og det nordlige Mexico",
                  "Ikke død, bare ventende: kreosotbusken holder op med at vokse i tørke og bliver grøn efter regn. En "
                  "ring af dens kloner i Mojave, King Clone, er omkring 11.700 år gammel."),
    "sweet_berry_bush": ("Sødbærbusk", "Nordlige skove og tundra i Europa, Asien og Nordamerika",
                         "Tyttebær, en lav stedsegrøn busk i den boreale skov. Dens syrlige røde bær holder sig hele "
                         "vinteren takket være deres egen benzoesyre."),
    "bamboo": ("Bambus", "Det centrale og sydlige Kina",
               "Et kæmpegræs: et moso-bambusskud kan vokse en meter på en dag og når fuld højde på to måneder. En hel "
               "bevoksning blomstrer én gang på mange årtier, alle på én gang, og dør så."),
    "cactus": ("Kaktus", "Sonoraørkenen i Arizona og Mexico",
               "Saguaroen gemmer vand i en foldet stamme, der svulmer op efter regn; en voksen kan rumme over et ton. "
               "Den bliver op til 150 år og får sin første arm omkring de 70."),
    "sugar_cane": ("Sukkerrør", "Ny Guinea, hvor det først blev dyrket for omkring 8.000 år siden",
                   "Et kæmpegræs, hvis stængler er fulde af sød saft; kogt ind giver den det meste af verdens sukker. "
                   "Plantagerne i Amerika blev bygget på den og på slavegjorte menneskers tvangsarbejde."),
    "moss": ("Mos", "Fugtig jord i hele den tempererede verden",
             "Almindelig jomfruhår, et af de højeste mosser. Mosser har hverken rødder, blomster eller frø; de drikker "
             "gennem bladene og har brug for en vandhinde, så sædcellerne kan svømme hen til ægcellerne."),
    "wheat": ("Hvede", "Den Frugtbare Halvmåne, af tre vilde græsser krydset for omkring 9.000 år siden",
              "Brødhvede bærer hele arvemassen fra tre vilde forfædre. Den giver en femtedel af menneskehedens "
              "kalorier, og dens gluten lader dejen strække sig og holde på gassen i hævende brød."),
    "carrot": ("Gulerod", "Vild i Europa og Sydvestasien; først dyrket for roden i Afghanistan",
               "De første spiselige gulerødder var lilla eller gule; de orange blev forædlet i Nederlandene i "
               "1600-tallet. Det orange er betacaroten, som kroppen laver om til A-vitamin."),
    "potato": ("Kartoffel", "Andesbjergene i Peru og Bolivia",
               "Andesbønderne dyrkede tusindvis af sorter; Europa plantede kun få, og da kartoffelskimmel slog til i "
               "1845, mistede Irland en million mennesker i hungersnød. Grønne kartofler indeholder giften solanin."),
    "beetroot": ("Rødbede", "Middelhavets og Atlanterhavets kyster, som vild strandbede",
                 "Rødbede, bladbede og sukkerroe er én art forædlet på tre måder. Sukkerroen, forædlet i Preussen i "
                 "1700-tallet, giver i dag en femtedel af verdens sukker."),
    "melon": ("Vandmelon", "Nordøstafrika",
              "Vandmelonen er omkring ni tiendedele vand. Dens frø blev lagt i egyptiske grave, og ørkenrejsende bar den "
              "som feltflaske."),
    "pumpkin": ("Græskar", "Argentina og Uruguay",
                "En krybende ranke, hvis kæmpegræskar har passeret 1.200 kilo, de tungeste frugter af alle planter. "
                "Amerikas folk dyrkede græskar sammen med majs og bønner."),
    "cocoa": ("Kakao", "Det øvre Amazonasbassin",
              "Et lille træ i regnskovens skygge, hvis frugter vokser direkte på stammen. Mayaer og aztekere drak dets "
              "ristede, malede frø; navnet betyder gudernes føde."),
    "kelp": ("Tang", "Kolde kyster i Stillehavet og de sydlige have",
             "Kæmpetang er ikke en plante, men en brunalge. Gasfyldte blærer holder bladene oppe, og den kan vokse en "
             "halv meter om dagen til undervandsskove fulde af fisk og havoddere."),
    "seagrass": ("Havgræs", "Lavvandede kyster i det nordlige Atlanterhav og det nordlige Stillehav",
                 "En blomsterplante, der vendte tilbage til havet og endda blomstrer under vand. Dens enge skjuler "
                 "unge fisk og begraver kulstof i mudderet."),
    "lily_pad": ("Åkandeblad", "Søer og damme i Europa, Nordafrika og Mellemøsten",
                 "Den hvide åkande slår rod i mudderet og lader bladene flyde på lange stilke med luftkanaler, der "
                 "fører ilt ned til rødderne. Bladene ånder gennem oversiden."),
    "torchflower": ("Fakkelblomst", None,
                    "En ældgammel blomst fra før verdens begyndelse; snusere graver dens frø op af sandet ved ruiner i "
                    "varme have. Den ligner en flamme, men giver intet lys."),
    "pink_petals": ("Lyserøde kronblade", None,
                    "Tæpper af små lyserøde blomster under kirsebærtræer, op til fire pr. blok. Ingen virkelig blomst "
                    "vokser sådan: de ligner faldne kirsebærblomster, der har slået rod."),
    "spore_blossom": ("Sporeblomst", None,
                      "En blomst, der hænger fra huleloftet og drysser en langsom regn af grønne sporer. Blomster "
                      "laver pollen og frø, ikke sporer; denne har slet ikke brug for sollys."),
    "dripleaf": ("Dryppeblad", None,
                 "Brede blade på høje stængler, der vipper et øjeblik efter, at noget står på dem, som en faldlem. Den "
                 "vokser i leret og vandet i frodige huler."),
    "glow_berries": ("Glødebær", None,
                     "Ranker, der hænger fra huleloftet, med bær, der lyser af sig selv. Ildfluer og nogle svampe "
                     "lyser, men ingen virkelig frugt."),
    "glow_lichen": ("Glødelav", None,
                    "En lav, der gløder på hulevægge. Virkelige laver er fællesskaber af en svamp og en alge eller en "
                    "bakterie; ingen af dem lyser."),
    "vine": ("Slyngplanter", None,
             "Bladrige ranker uden blomster eller frugt, der hænger fra jungletræer og sumpege og vokser nedad. "
             "Virkelige klatreplanter vokser opad mod lyset."),
}

BIOMES = {
    "river": "En flod: ferskvand på vej mod havet, med siv langs bredderne og vandplanter på bunden.",
    "ocean": "Åbent tempereret hav som det nordlige Atlanterhav: køligt vand, som storme rører rundt i, med tang og "
             "havgræs nær kysterne.",
    "cold_ocean": "Koldt hav som det nordlige Stillehav: mørkt, næringsrigt vand, hvor tang vokser til skove.",
    "lukewarm_ocean": "Mildt hav som Middelhavet: klart, salt vand med havgræsenge på sandbunden.",
    "warm_ocean": "Tropisk hav som Caribien: varmt, klart vand over koralrev og havgræsenge.",
}
