"""Plantencompendium (3.5.0) in het Nederlands. Zie compendium_plants.py en compendium_i18n.py."""

TITLE = "Plantencompendium"

GUIDE = [
    "# Het Plantencompendium",
    "Een boek en een willekeurige kleine bloem maken het Plantencompendium; onderzoek is niet nodig. Het werkt als het "
    "Bomencompendium: houd het in je snelbalk en de hoek linksboven noemt het bioom en, zodra je hem kent, de plant "
    "waar je naar kijkt. Bloemen, grassen, varens, struiken, gewassen, waterplanten en de planten van weelderige "
    "grotten staan er allemaal in.",
    "Om een plant te noteren kijk je er even naar vanaf hooguit 8 blokken, of pluk of oogst je hem, met het boek in je "
    "snelbalk; planten onder water worden net zo genoteerd. Gebruik het boek om te lezen: biomen links, de planten van "
    "elk bioom rechts.",
    "Compendiumnotities kopiëren beide boeken tegelijk: wie ze leest, krijgt alle bomen, planten en biomen erin.",
    "Species Plantarum (niveau 3): elke plant toont zijn wetenschappelijke naam, zijn familie en waar hij in de echte "
    "wereld in het wild groeit.",
    "Plantengeografie (niveau 3): elk bioom toont zijn klimaat, in dit boek net als in het Bomencompendium.",
    "De Rode Lijst (niveau 6): elke plant toont hoe bedreigd hij in het wild is.",
]

UI = [
    "Plantencompendium",
    "Onbekende plant",
    "Nieuwe plant in je Compendium: %s",
    "%s van %s planten en %s van %s biomen genoteerd.",
    "Houd dit boek in je snelbalk. Kijk even naar een plant, of pluk of oogst hem, om hem te noteren; loop een bioom in "
    "om het bioom te noteren. Kies links een bioom.",
    "Planten",
    "Een plant van alleen deze wereld: geen echte soort komt ermee overeen.",
    "Plantencompendium",
    "In de snelbalk toont het het bioom en noemt het de planten die je kent; gebruik het om over ze te lezen",
]

PLANTS = {
    "dandelion": ("Paardenbloem", "Europa en Azië; nu op elk continent behalve Antarctica",
                  "Zijn gele hoofdje sluit 's nachts en rijpt tot een pluizenbol waarvan de wind de zaden kilometers "
                  "ver draagt. Alles eraan is eetbaar, en zijn bladeren waren voorjaarssla lang voor de kropsla."),
    "poppy": ("Klaproos", "Europa, Noord-Afrika en West-Azië",
              "De klaproos volgt de ploeg: haar zaden wachten tientallen jaren in de grond en kiemen als de aarde "
              "wordt omgewoeld; daarom bedekte ze na 1914 de slagvelden van Vlaanderen."),
    "blue_orchid": ("Blauwe orchidee", "Heuvelbossen van Assam, Myanmar en Noord-Thailand",
                    "Een van de heel weinige echt blauwe orchideeën. In het wild klampt ze zich vast aan stammen in "
                    "koele heuvelbossen; verzamelaars hebben hele dalen leeggehaald, en handel in wilde planten is nu "
                    "verboden."),
    "allium": ("Sierui", "Bergen van Iran, Afghanistan en Centraal-Azië",
               "Een sierui: een bol van honderden kleine paarse bloemen op een hoge steel. Wrijf een blad fijn en het "
               "ruikt naar ui, zoals zijn verwanten knoflook en prei."),
    "azure_bluet": ("Houstonia", "Oostelijk Noord-Amerika",
                    "Een piepklein bloemetje met vier blaadjes dat in het voorjaar gazons en weiden lichtblauw kleurt. "
                    "Hoe klein ook, het hoort bij de koffiefamilie."),
    "tulip": ("Tulp", "Bergsteppen van Turkije en Centraal-Azië",
              "In de 16e eeuw uit het Ottomaanse Rijk naar de Nederlanden gebracht; in 1637 kon één bol meer kosten "
              "dan een huis, tot de markt instortte. De gewilde gestreepte bloemblaadjes kwamen door een virus."),
    "oxeye_daisy": ("Margriet", "Europa en gematigd Azië",
                    "De witte margriet van weiden en bermen. Elke bloem is eigenlijk honderden bloempjes: gele in het "
                    "midden, witte eromheen."),
    "cornflower": ("Korenbloem", "Zuidoost-Europa en het Midden-Oosten, met het graan door heel Europa verspreid",
                   "Ooit onkruid in elk tarweveld, verdween ze bijna uit Europa toen boeren hun zaaigoed gingen "
                   "schonen en onkruidverdelgers gingen spuiten. Haar blauw gaf een kleur zijn naam."),
    "lily_of_the_valley": ("Lelietje-van-dalen", "Koele bossen in heel Europa en Azië",
                           "Geurige witte klokjes in schaduwrijke voorjaarsbossen. De hele plant is giftig: ze bevat "
                           "hartglycosiden zoals het vingerhoedskruid, dat de geneeskunde bij hartfalen gebruikt."),
    "sunflower": ("Zonnebloem", "Noord-Amerika; ongeveer 4000 jaar geleden in Mexico verbouwd",
                  "Jonge hoofden volgen de zon elke dag van oost naar west; open bloemen blijven naar het oosten "
                  "kijken. Uit de zaden wordt een van de belangrijkste spijsoliën ter wereld geperst."),
    "lilac": ("Sering", "Rotsige heuvels van de Balkan",
              "Een struik uit de olijffamilie met geurige paarse bloemtrossen in de late lente. Wild groeit hij alleen "
              "op Balkanhellingen, maar tuinen hebben hem de hele wereld rond gebracht."),
    "rose_bush": ("Rozenstruik", "Europa, Noordwest-Afrika en West-Azië",
                  "De hondsroos klimt met haakdoorns door heggen. Haar rode rozenbottels bevatten meer vitamine C dan "
                  "sinaasappels; in Brittannië in oorlogstijd plukten kinderen ze voor siroop."),
    "peony": ("Pioen", "Siberië, Mongolië en Noord-China",
              "Grote roze bloemen waarvan de knoppen suiker uitzweten die mieren komen drinken. In China al meer dan "
              "duizend jaar gekweekt, heet ze daar de koningin der bloemen."),
    "pitcher_plant": ("Bekerplant", "Venen van oostelijk Noord-Amerika",
                      "Een vleesetende plant van arme, natte grond: haar bladeren zijn kruiken met regenwater waarin "
                      "insecten verdrinken en worden verteerd. In deze wereld groeit ze alleen uit oeroude zaden die "
                      "snuffelaars opgraven."),
    "grass": ("Gras", "Europa, Noord-Azië en Noord-Amerika",
              "Veldbeemdgras, het gras van weiden en gazons. Grassen groeien vanaf de voet van het blad, niet vanaf de "
              "punt, en overleven zo begrazing en maaien die de meeste planten zouden doden."),
    "fern": ("Varen", "Bijna wereldwijd, op elk continent behalve Antarctica",
             "Adelaarsvaren is ouder dan elke bloem: hij heeft geen bloemen of zaden en verspreidt zich met sporen en "
             "ondergrondse stengels. Zijn bladeren zijn giftig voor vee en paarden."),
    "dead_bush": ("Dode struik", "Woestijnen van het zuidwesten van de VS en het noorden van Mexico",
                  "Niet dood, alleen wachtend: de creosootstruik stopt met groeien bij droogte en wordt groen na regen. "
                  "Een ring van zijn klonen in de Mojave, King Clone, is ongeveer 11.700 jaar oud."),
    "sweet_berry_bush": ("Zoetebessenstruik", "Noordelijke bossen en toendra van Europa, Azië en Noord-Amerika",
                         "De rode bosbes, een lage groenblijvende struik van het boreale woud. Haar zure rode bessen "
                         "blijven de hele winter goed dankzij hun eigen benzoëzuur."),
    "bamboo": ("Bamboe", "Midden- en Zuid-China",
               "Een reuzengras: een moso-bamboescheut kan een meter per dag groeien en is in twee maanden volgroeid. Een "
               "heel bamboebos bloeit eens in vele tientallen jaren, allemaal tegelijk, en sterft dan."),
    "cactus": ("Cactus", "De Sonorawoestijn in Arizona en Mexico",
               "De saguaro slaat water op in een geplooide stam die na regen opzwelt; een volgroeide kan meer dan een "
               "ton bevatten. Hij wordt tot 150 jaar oud en krijgt zijn eerste arm rond zijn 70e."),
    "sugar_cane": ("Suikerriet", "Nieuw-Guinea, waar het ongeveer 8000 jaar geleden voor het eerst werd verbouwd",
                   "Een reuzengras met stengels vol zoet sap; ingekookt levert het de meeste suiker ter wereld. De "
                   "plantages van Amerika werden erop gebouwd, en op de dwangarbeid van tot slaaf gemaakte mensen."),
    "moss": ("Mos", "Vochtige grond in de hele gematigde wereld",
             "Gewoon haarmos, een van de hoogste mossen. Mossen hebben geen wortels, bloemen of zaden; ze drinken via "
             "hun blaadjes en hebben een laagje water nodig zodat hun zaadcellen naar de eicellen kunnen zwemmen."),
    "wheat": ("Tarwe", "De Vruchtbare Sikkel, uit drie wilde grassen gekruist zo'n 9000 jaar geleden",
              "Broodtarwe draagt de volledige genomen van drie wilde voorouders. Ze levert een vijfde van de calorieën "
              "van de mensheid, en haar gluten laat deeg rekken en het gas van rijzend brood vasthouden."),
    "carrot": ("Wortel", "Wild in Europa en Zuidwest-Azië; het eerst om de wortel verbouwd in Afghanistan",
               "De eerste eetbare wortels waren paars of geel; oranje werden ze in de 17e eeuw in de Nederlanden "
               "gekweekt. Het oranje is bètacaroteen, dat het lichaam omzet in vitamine A."),
    "potato": ("Aardappel", "De Andes van Peru en Bolivia",
               "Andesboeren kweekten duizenden rassen; Europa plantte er maar een paar, en toen in 1845 de "
               "aardappelziekte toesloeg verloor Ierland een miljoen mensen door honger. Groene aardappelen bevatten "
               "het gif solanine."),
    "beetroot": ("Biet", "Kusten van de Middellandse Zee en de Atlantische Oceaan, als wilde strandbiet",
                 "Rode biet, snijbiet en suikerbiet zijn één soort, op drie manieren gekweekt. De suikerbiet, in de 18e "
                 "eeuw in Pruisen gekweekt, levert nu een vijfde van de suiker ter wereld."),
    "melon": ("Watermeloen", "Noordoost-Afrika",
              "De watermeloen bestaat voor zo'n negen tiende uit water. Haar zaden werden in Egyptische graven gelegd, "
              "en woestijnreizigers namen haar mee als veldfles."),
    "pumpkin": ("Pompoen", "Argentinië en Uruguay",
                "Een kruipende rank waarvan reuzenpompoenen al meer dan 1200 kilo wogen, de zwaarste vruchten van "
                "alle planten. De volken van Amerika teelden pompoenen samen met maïs en bonen."),
    "cocoa": ("Cacao", "Het bovenste Amazonebekken",
              "Een boompje in de schaduw van het regenwoud waarvan de vruchten direct aan de stam groeien. Maya's en "
              "Azteken dronken de geroosterde, gemalen zaden; de naam betekent voedsel van de goden."),
    "kelp": ("Kelp", "Koude kusten van de Grote Oceaan en de zuidelijke zeeën",
             "Reuzenkelp is geen plant maar een bruinwier. Gasgevulde blaasjes houden de bladen overeind, en het kan "
             "een halve meter per dag groeien tot onderwaterbossen vol vissen en zeeotters."),
    "seagrass": ("Zeegras", "Ondiepe kusten van de Noord-Atlantische en Noord-Grote Oceaan",
                 "Een bloeiende plant die terugging naar zee en zelfs onder water bloeit. De velden beschutten jonge "
                 "vissen en leggen koolstof vast in de modder."),
    "lily_pad": ("Waterlelieblad", "Meren en vijvers van Europa, Noord-Afrika en het Midden-Oosten",
                 "De witte waterlelie wortelt in de modder en laat haar bladeren drijven op lange stelen, met "
                 "luchtkanalen die zuurstof naar de wortels brengen. Haar bladeren ademen via de bovenkant."),
    "torchflower": ("Fakkelbloem", None,
                    "Een oeroude bloem van voor het begin van de wereld; snuffelaars graven haar zaden op uit het zand "
                    "van ruïnes in warme zeeën. Ze lijkt op een vlam maar geeft geen licht."),
    "pink_petals": ("Roze bloemblaadjes", None,
                    "Tapijten van kleine roze bloemetjes onder kersenbomen, tot vier per blok. Geen echte bloem groeit "
                    "zo: het lijkt gevallen kersenbloesem die wortel heeft geschoten."),
    "spore_blossom": ("Sporenbloesem", None,
                      "Een bloem die aan grotplafonds hangt en een trage regen van groene sporen laat vallen. Bloemen "
                      "maken stuifmeel en zaden, geen sporen; deze heeft helemaal geen zonlicht nodig."),
    "dripleaf": ("Druppelblad", None,
                 "Brede bladeren op hoge stelen die even nadat er iets op staat omklappen, als een valluik. Het groeit "
                 "in de klei en het water van weelderige grotten."),
    "glow_berries": ("Gloeibessen", None,
                     "Ranken die aan grotplafonds hangen met bessen die uit zichzelf licht geven. Vuurvliegjes en "
                     "sommige zwammen geven licht, maar geen echte vrucht."),
    "glow_lichen": ("Gloeikorstmos", None,
                    "Een korstmos dat op grotwanden gloeit. Echte korstmossen zijn samenwerkingen van een schimmel met "
                    "een alg of een bacterie; geen ervan gloeit."),
    "vine": ("Klimplanten", None,
             "Bebladerde ranken zonder bloemen of vruchten die van jungle-bomen en moeraseiken hangen en naar beneden "
             "groeien. Echte klimplanten groeien omhoog, naar het licht."),
}

BIOMES = {
    "river": "Een rivier: zoet water op weg naar zee, met riet langs de oevers en waterplanten op de bodem.",
    "ocean": "Open gematigde zee zoals de Noord-Atlantische Oceaan: koel water dat door stormen wordt gemengd, met "
             "kelp en zeegras dicht bij de kust.",
    "cold_ocean": "Koude zee zoals de noordelijke Grote Oceaan: donker, voedselrijk water waarin kelp tot bossen "
                  "uitgroeit.",
    "lukewarm_ocean": "Milde zee zoals de Middellandse Zee: helder, zout water met zeegrasvelden op de zandbodem.",
    "warm_ocean": "Tropische zee zoals de Caraïben: warm, helder water boven koraalriffen en zeegrasvelden.",
}
