"""Pflanzenkompendium (3.5.0) auf Deutsch. Siehe compendium_plants.py und compendium_i18n.py."""

TITLE = "Pflanzenkompendium"

GUIDE = [
    "# Das Pflanzenkompendium",
    "Ein Buch und eine beliebige kleine Blume ergeben das Pflanzenkompendium; Forschung ist nicht nötig. Es "
    "funktioniert wie das Baumkompendium: Trag es in der Schnellleiste, und die Ecke oben links nennt das Biom und, "
    "sobald du sie kennst, die Pflanze, die du ansiehst. Blumen, Gräser, Farne, Sträucher, Feldfrüchte, "
    "Wasserpflanzen und die Pflanzen der üppigen Höhlen stehen alle darin.",
    "Um eine Pflanze zu notieren, sieh sie einen Moment lang aus höchstens 8 Blöcken an oder pflücke bzw. ernte sie, "
    "während das Buch in der Schnellleiste ist; Pflanzen unter Wasser werden genauso notiert. Benutze das Buch zum "
    "Lesen: links die Biome, rechts die Pflanzen jedes Bioms.",
    "Kompendiumsnotizen kopieren beide Bücher auf einmal: Wer sie liest, erhält alle Bäume, Pflanzen und Biome darin.",
    "Species Plantarum (Stufe 3): Jede Pflanze zeigt ihren wissenschaftlichen Namen, ihre Familie und wo sie in der "
    "echten Welt wild wächst.",
    "Pflanzengeographie (Stufe 3): Jedes Biom zeigt sein Klima, in diesem Buch wie im Baumkompendium.",
    "Die Rote Liste (Stufe 6): Jede Pflanze zeigt, wie stark sie in der Natur bedroht ist.",
]

UI = [
    "Pflanzenkompendium",
    "Unbekannte Pflanze",
    "Neue Pflanze in deinem Kompendium: %s",
    "%s von %s Pflanzen und %s von %s Biomen notiert.",
    "Trag dieses Buch in der Schnellleiste. Sieh eine Pflanze einen Moment lang an oder pflücke bzw. ernte sie, um sie "
    "zu notieren; betritt ein Biom, um das Biom zu notieren. Wähle links ein Biom.",
    "Pflanzen",
    "Eine Pflanze nur dieser Welt: Keine echte Art entspricht ihr.",
    "Pflanzenkompendium",
    "In der Schnellleiste zeigt es das Biom und nennt die Pflanzen, die du kennst; benutze es, um über sie zu lesen",
]

PLANTS = {
    "dandelion": ("Löwenzahn", "Europa und Asien; heute auf allen Kontinenten außer der Antarktis",
                  "Sein gelber Kopf schließt sich nachts und reift zur Pusteblume, deren Samen der Wind kilometerweit "
                  "trägt. Alles an ihm ist essbar, und seine Blätter waren Frühlingssalat lange vor dem Kopfsalat."),
    "poppy": ("Mohn", "Europa, Nordafrika und Westasien",
              "Der Klatschmohn folgt dem Pflug: Seine Samen warten jahrzehntelang im Boden und keimen, wenn die Erde "
              "umgegraben wird; darum bedeckte er nach 1914 die Schlachtfelder Flanderns."),
    "blue_orchid": ("Blaue Orchidee", "Bergwälder von Assam, Myanmar und Nordthailand",
                    "Eine der ganz wenigen wirklich blauen Orchideen. In der Natur klammert sie sich an Baumstämme in "
                    "kühlen Bergwäldern; Sammler haben ganze Täler leergeräumt, und der Handel mit wilden Pflanzen ist "
                    "heute verboten."),
    "allium": ("Zierlauch", "Gebirge in Iran, Afghanistan und Zentralasien",
               "Eine Zierzwiebel: eine Kugel aus Hunderten kleiner violetter Blüten auf einem hohen Stiel. Zerreibt man "
               "ein Blatt, riecht es nach Zwiebel, wie seine Verwandten Knoblauch und Lauch."),
    "azure_bluet": ("Porzellansternchen", "Östliches Nordamerika",
                    "Eine winzige vierblättrige Blüte, die im Frühling Rasen und Wiesen hellblau überzieht. So klein "
                    "sie ist, gehört sie zur Familie des Kaffees."),
    "tulip": ("Tulpe", "Gebirgssteppen der Türkei und Zentralasiens",
              "Im 16. Jahrhundert aus dem Osmanischen Reich in die Niederlande gebracht; 1637 kostete eine einzige "
              "Zwiebel mehr als ein Haus, bis der Markt zusammenbrach. Die begehrten gestreiften Blüten verursachte "
              "ein Virus."),
    "oxeye_daisy": ("Margerite", "Europa und das gemäßigte Asien",
                    "Die weiße Margerite der Wiesen und Wegränder. Jede Blüte besteht in Wahrheit aus Hunderten kleiner "
                    "Blüten: gelbe in der Mitte, weiße am Rand."),
    "cornflower": ("Kornblume", "Südosteuropa und Naher Osten, mit dem Getreide über ganz Europa verbreitet",
                   "Einst Unkraut in jedem Weizenfeld, verschwand sie fast aus Europa, als die Bauern ihr Saatgut "
                   "reinigten und Herbizide spritzten. Ihr Blau gab einer Farbe den Namen."),
    "lily_of_the_valley": ("Maiglöckchen", "Kühle Wälder in ganz Europa und Asien",
                           "Duftende weiße Glöckchen in schattigen Frühlingswäldern. Die ganze Pflanze ist giftig: Sie "
                           "enthält Herzglykoside wie der Fingerhut, den die Medizin bei Herzschwäche einsetzt."),
    "sunflower": ("Sonnenblume", "Nordamerika; vor etwa 4000 Jahren in Mexiko angebaut",
                  "Junge Köpfe folgen der Sonne jeden Tag von Ost nach West; offene Blüten bleiben nach Osten "
                  "gerichtet. Aus ihren Samen wird eines der wichtigsten Speiseöle der Welt gepresst."),
    "lilac": ("Flieder", "Felsige Hügel des Balkans",
              "Ein Strauch aus der Familie des Ölbaums mit duftenden violetten Blütenrispen im späten Frühling. Wild "
              "wächst er nur an Balkanhängen, doch die Gärten haben ihn um die Welt getragen."),
    "rose_bush": ("Rosenstrauch", "Europa, Nordwestafrika und Westasien",
                  "Die Hundsrose klettert mit hakigen Stacheln durch Hecken. Ihre roten Hagebutten enthalten mehr "
                  "Vitamin C als Orangen; im Krieg sammelten britische Kinder sie für Sirup."),
    "peony": ("Pfingstrose", "Sibirien, Mongolei und Nordchina",
              "Große rosa Blüten, deren Knospen Zucker ausschwitzen, den Ameisen trinken. In China seit über tausend "
              "Jahren gezüchtet, heißt sie dort die Königin der Blumen."),
    "pitcher_plant": ("Kannenpflanze", "Moore im östlichen Nordamerika",
                      "Eine fleischfressende Pflanze armer, nasser Böden: Ihre Blätter sind Krüge voll Regenwasser, in "
                      "denen Insekten ertrinken und verdaut werden. In dieser Welt wächst sie nur aus uralten Samen, "
                      "die Schnüffler ausgraben."),
    "grass": ("Gras", "Europa, Nordasien und Nordamerika",
              "Das Wiesen-Rispengras, das Gras von Weiden und Rasen. Gräser wachsen vom Grund des Blattes her, nicht "
              "von der Spitze, und überleben so Beweidung und Mähen, die die meisten Pflanzen töten würden."),
    "fern": ("Farn", "Fast weltweit, auf allen Kontinenten außer der Antarktis",
             "Der Adlerfarn ist älter als jede Blume: Er hat weder Blüten noch Samen und breitet sich mit Sporen und "
             "unterirdischen Sprossen aus. Seine Wedel sind giftig für Rinder und Pferde."),
    "dead_bush": ("Toter Busch", "Wüsten im Südwesten der USA und im Norden Mexikos",
                  "Nicht tot, nur wartend: Der Kreosotbusch hört bei Dürre auf zu wachsen und ergrünt nach Regen. Ein "
                  "Ring seiner Klone in der Mojave, King Clone, ist etwa 11 700 Jahre alt."),
    "sweet_berry_bush": ("Süßbeerenstrauch", "Nördliche Wälder und Tundra Europas, Asiens und Nordamerikas",
                         "Die Preiselbeere, ein niedriger immergrüner Strauch des borealen Waldes. Ihre herben roten "
                         "Beeren halten sich den ganzen Winter, dank ihrer eigenen Benzoesäure."),
    "bamboo": ("Bambus", "Mittel- und Südchina",
               "Ein Riesengras: Ein Moso-Bambusspross kann an einem Tag einen Meter wachsen und erreicht seine volle "
               "Höhe in zwei Monaten. Ein ganzer Bestand blüht nur alle paar Jahrzehnte, alle zugleich, und stirbt "
               "dann."),
    "cactus": ("Kaktus", "Die Sonora-Wüste in Arizona und Mexiko",
               "Der Saguaro speichert Wasser in einem gefalteten Stamm, der nach Regen anschwillt; ein ausgewachsener "
               "fasst über eine Tonne. Er wird bis zu 150 Jahre alt und bildet seinen ersten Arm mit etwa 70."),
    "sugar_cane": ("Zuckerrohr", "Neuguinea, wo es vor etwa 8000 Jahren zuerst angebaut wurde",
                   "Ein Riesengras, dessen Halme voll süßem Saft sind; eingekocht gibt er den meisten Zucker der Welt. "
                   "Die Plantagen Amerikas wurden auf ihm und auf der Zwangsarbeit versklavter Menschen errichtet."),
    "moss": ("Moos", "Feuchte Böden der ganzen gemäßigten Welt",
             "Das Goldene Frauenhaarmoos, eines der höchsten Moose. Moose haben weder Wurzeln noch Blüten noch Samen; "
             "sie trinken über die Blätter und brauchen einen Wasserfilm, damit ihre Spermien zu den Eizellen "
             "schwimmen."),
    "wheat": ("Weizen", "Der Fruchtbare Halbmond, aus drei wilden Gräsern vor etwa 9000 Jahren gekreuzt",
              "Brotweizen trägt die ganzen Genome dreier wilder Vorfahren. Er liefert ein Fünftel der Kalorien der "
              "Menschheit, und sein Gluten lässt den Teig sich dehnen und das Gas des aufgehenden Brotes halten."),
    "carrot": ("Karotte", "Wild in Europa und Südwestasien; zuerst in Afghanistan wegen der Wurzel angebaut",
               "Die ersten Speisemöhren waren violett oder gelb; orange wurden sie im 17. Jahrhundert in den "
               "Niederlanden gezüchtet. Das Orange ist Betacarotin, das der Körper in Vitamin A umwandelt."),
    "potato": ("Kartoffel", "Die Anden in Peru und Bolivien",
               "Andenbauern zogen Tausende Sorten; Europa pflanzte nur wenige, und als 1845 die Kraut- und Knollenfäule "
               "kam, verlor Irland eine Million Menschen durch Hunger. Grüne Kartoffeln enthalten das Gift Solanin."),
    "beetroot": ("Rote Bete", "Küsten des Mittelmeers und des Atlantiks, als wilde Meeresbete",
                 "Rote Bete, Mangold und Zuckerrübe sind eine Art, dreifach gezüchtet. Die Zuckerrübe, im 18. "
                 "Jahrhundert in Preußen gezüchtet, liefert heute ein Fünftel des Zuckers der Welt."),
    "melon": ("Wassermelone", "Nordostafrika",
              "Die Wassermelone besteht zu etwa neun Zehnteln aus Wasser. Ihre Samen lagen in ägyptischen Gräbern, und "
              "Wüstenreisende trugen sie als Feldflasche."),
    "pumpkin": ("Kürbis", "Argentinien und Uruguay",
                "Eine kriechende Ranke, deren Riesenkürbisse über 1200 Kilogramm erreicht haben, die schwersten "
                "Früchte aller Pflanzen. Die Völker Amerikas bauten Kürbisse zusammen mit Mais und Bohnen an."),
    "cocoa": ("Kakao", "Das obere Amazonasbecken",
              "Ein kleiner Baum des Regenwaldschattens, dessen Früchte direkt am Stamm wachsen. Maya und Azteken "
              "tranken seine gerösteten, gemahlenen Samen; sein Name bedeutet Speise der Götter."),
    "kelp": ("Seetang", "Kalte Küsten des Pazifiks und der Südmeere",
             "Der Riesentang ist keine Pflanze, sondern eine Braunalge. Gasgefüllte Blasen halten seine Wedel aufrecht, "
             "und er wächst bis zu einem halben Meter am Tag zu Unterwasserwäldern voller Fische und Seeotter."),
    "seagrass": ("Seegras", "Flache Küsten des Nordatlantiks und Nordpazifiks",
                 "Eine Blütenpflanze, die ins Meer zurückkehrte und sogar unter Wasser blüht. Ihre Wiesen bergen "
                 "Jungfische und lagern Kohlenstoff im Schlamm ein."),
    "lily_pad": ("Seerosenblatt", "Seen und Teiche Europas, Nordafrikas und des Nahen Ostens",
                 "Die Weiße Seerose wurzelt im Schlamm und lässt ihre Blätter an langen Stielen schwimmen, deren "
                 "Luftkanäle Sauerstoff zu den Wurzeln leiten. Ihre Blätter atmen über die Oberseite."),
    "torchflower": ("Fackellilie", None,
                    "Eine uralte Blume aus der Zeit vor dem Anfang der Welt; Schnüffler graben ihre Samen aus dem Sand "
                    "der Ruinen warmer Meere. Sie sieht aus wie eine Flamme, gibt aber kein Licht."),
    "pink_petals": ("Rosa Blütenblätter", None,
                    "Teppiche kleiner rosa Blüten unter Kirschbäumen, bis zu vier pro Block. Keine echte Blume wächst "
                    "so: Sie sehen aus wie gefallene Kirschblüten, die Wurzeln geschlagen haben."),
    "spore_blossom": ("Sporenblüte", None,
                      "Eine Blüte, die von Höhlendecken hängt und einen langsamen Regen grüner Sporen fallen lässt. "
                      "Blumen bilden Pollen und Samen, keine Sporen; diese braucht gar kein Sonnenlicht."),
    "dripleaf": ("Tropfblatt", None,
                 "Breite Blätter auf hohen Stielen, die kurz nachdem etwas darauf steht wie eine Falltür umkippen. Es "
                 "wächst im Ton und Wasser der üppigen Höhlen."),
    "glow_berries": ("Leuchtbeeren", None,
                     "Ranken, die von Höhlendecken hängen, mit Beeren, die aus eigener Kraft leuchten. Glühwürmchen und "
                     "manche Pilze leuchten, aber keine echte Frucht."),
    "glow_lichen": ("Leuchtflechte", None,
                    "Eine Flechte, die an Höhlenwänden glimmt. Echte Flechten sind Gemeinschaften aus einem Pilz und "
                    "einer Alge oder einem Bakterium; keine von ihnen leuchtet."),
    "vine": ("Ranken", None,
             "Belaubte Ranken ohne Blüten und Früchte, die von Dschungelbäumen und Sumpfeichen herabhängen und nach "
             "unten wachsen. Echte Kletterpflanzen wachsen hinauf zum Licht."),
}

BIOMES = {
    "river": "Ein Fluss: Süßwasser auf dem Weg zum Meer, mit Schilf am Ufer und Wasserpflanzen im Flussbett.",
    "ocean": "Offenes gemäßigtes Meer wie der Nordatlantik: kühles, von Stürmen durchmischtes Wasser, mit Tang und "
             "Seegras nahe den Küsten.",
    "cold_ocean": "Kaltes Meer wie der Nordpazifik: dunkles, nährstoffreiches Wasser, in dem Tang zu Wäldern wächst.",
    "lukewarm_ocean": "Mildes Meer wie das Mittelmeer: klares, salziges Wasser mit Seegraswiesen auf sandigem Grund.",
    "warm_ocean": "Tropisches Meer wie die Karibik: warmes, klares Wasser über Korallenriffen und Seegraswiesen.",
}
