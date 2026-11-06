"""German (de_de) text of the Field Guide (2.5.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the de_de lang file.
"""

DECIMAL = ","

INTRO = [
    "# Willkommen",
    "Jeder Block und jeder Gegenstand dieser Welt besteht aus echten Elementen. Dieses Handbuch folgt dem "
    "Technologiebaum der Mod von der Steinzeit bis zu den letzten Elementen, ein Kapitel pro Stufe.",
    "# Einheiten",
    "1 Einheit ist 1 Gramm. Ein Block sind 1000 Einheiten, ein Barren oder ein Element- oder Substanzgegenstand 111,1, "
    "ein Klumpen 12,35 und ein Eimer 1000. Fahre mit dem Mauszeiger über einen Block oder Gegenstand, um zu sehen, "
    "woraus er besteht; halte Umschalt gedrückt, um alles zu sehen.",
    "# Forschung",
    "Jede Stufe hat ihre eigene Forschungsstation. Lege die Proben eines Projekts in ihre 9 Felder, wähle das Projekt "
    "und drücke Forschen. Schlüsselprojekte (mit einem Stern markiert) heben deine Technologiestufe; du brauchst die "
    "Schlüsselprojekte einer Stufe, bevor du die nächste Station aufstellen kannst. /research zeigt, was du weißt.",
    "An einer Station geschriebene Abhandlungen bringen einem anderen Spieler alles bei, was du weißt.",
    "# Maschinen",
    "Eine Maschine zerlegt jeden Gegenstand, der hineinkommt, nach seiner Zusammensetzung und wendet jede Reaktion an, "
    "die du erforscht hast, solange sie heiß genug und nicht zu heiß ist. Produkte kommen als Gegenstände heraus, "
    "Abfall als Schlacke, und manche Gase werden abgelassen. Flüssigkeiten und Gase füllen leere Glaskolben aus dem "
    "Seitenleiste der Maschine. Manche Reaktionen brauchen eine zweite Zutat im Reagenzfeld.",
    "Brennstoffbeheizte Maschinen verbrennen, was im Brennstofffeld liegt: Holz erreicht 650 °C, Holzkohle und Kohle "
    "1100 °C, Erdölbrennstoffe 1200 °C und Gas 1300 °C. Der Blasebalg (Gebläseluft) bringt 250 °C mehr. Holzkohle und "
    "Kohle dienen auch als Kohlenstoff, der Erze reduziert. Elektrische Maschinen beziehen Strom aus einer Voltaschen "
    "Säule, einem Dynamo oder einem anderen Energieblock, der daneben steht oder über Kupferdraht verbunden ist.",
    "Normale Öfen schmelzen keine Eisen-, Kupfer- oder Golderze mehr: die Metalle kommen aus den Maschinen.",
    "# Stufe 0",
    "Ganz ohne Forschung kannst du einen Kohlenmeiler bauen (8 Erdblöcke um einen Stamm) und Stämme zu Holzkohle "
    "verkohlen.",
]

CONTAINERS = [
    "# Glaskolben",
    "Jeder flüssige oder gasförmige Gegenstand ist ein verschlossener Glaskolben mit 111,1 Einheiten. Fünf Glas ergeben "
    "vier Kolben, ohne Forschung. Eine Maschine füllt für jede Flüssigkeit und jedes Gas, das sie erzeugt, einen "
    "leeren Kolben aus dem oberen Feld ihres Seitenleistes; ohne Kolben hält sie an und verlangt welche. Wenn eine "
    "Maschine, ein Rezept, ein Ofen oder ein Forschungsprojekt den Inhalt eines Kolbens verbraucht, bekommst du den "
    "leeren Kolben zurück.",
    "# Stahlflaschen und Kompressor",
    "Gasverdichtung (Stufe 4) schaltet Stahlflaschen und den Kompressor frei. Eine Flasche fasst 1000 Einheiten eines "
    "verdichteten Gases: so viel wie neun Kolben in einem einzigen Gegenstand. Baue einen Kompressor in eines der zwei "
    "Erweiterungsfelder einer Maschine ein und lege leere Flaschen ins zweite Feld ihres Seitenleistes: dann füllt die "
    "Maschine jedes Gas, das sie erzeugt, in Flaschen, eine Flasche nach der anderen. Flüssigkeiten gehen weiterhin in "
    "Kolben. Eine Flasche Brenngas brennt wie neun Kolben.",
    "Maschinen nehmen Flaschen wie jeden anderen Gegenstand als Eingabe und geben die leere Flasche zurück.",
    "# Lagertank",
    "Pneumatische Chemie (Stufe 3) schaltet den Lagertank frei. Er fasst 16 000 Einheiten (144 Kolben) einer einzigen "
    "Flüssigkeit oder eines Gases. Benutze ihn mit einem vollen Kolben oder einer vollen Flasche, um sie hineinzuleeren, "
    "mit einem leeren Kolben, um einen herauszunehmen, und mit leerer Hand, um den Füllstand abzulesen. Trichter "
    "funktionieren auch: führe volle oder leere Behälter von oben oder von der Seite zu und nimm unten heraus, was "
    "herauskommt. Ein Redstone-Komparator liest, wie voll er ist. Wenn du ihn abbaust, läuft sein Inhalt aus.",
]

UPGRADES = [
    "# Erweiterungsfelder",
    "Jede Maschine hat zwei Erweiterungsfelder unten in ihrem Seitenleiste. Eine Erweiterung wirkt, sobald ihr Besitzer "
    "sie erforscht hat; zwei gleiche zählen nur einmal. Als „brennstoffbeheizt“ markierte Erweiterungen bewirken bei "
    "elektrischen Maschinen nichts.",
    "- Kompressor (Gasverdichtung, Stufe 4): füllt die Gase, die die Maschine erzeugt, in Stahlflaschen.",
    "- Ausgabeschieber (Automatische Mühle, Stufe 3): befördert die Produkte der Maschine in eine Truhe, ein Fass oder "
    "einen Trichter daneben. Beliefert nie direkt eine andere Maschine.",
    "- Regenerator (Regenerativofen, Stufe 4): eine brennstoffbeheizte Maschine verliert halb so viel Wärme, verbrennt "
    "also weniger Brennstoff und heizt schneller auf.",
    "- Feuerfestauskleidung (Basische Feuerfeststoffe, Stufe 4): eine brennstoffbeheizte Maschine kann ihre eigene "
    "Grenze um 150 °C überschreiten, wenn ihr Brennstoff und ihr Blasebalg so weit reichen.",
    "- Gasrückgewinnungshaube (Nebenproduktgewinnung, Stufe 4): Flüssigkeiten und Gase, die die Maschine ablassen "
    "würde (Kohlenstoffdioxid, Schwefeldioxid, Salzsäure und andere), gehen in Kolben oder Flaschen. Dampf und "
    "Mischgase wie Stadtgas werden weiterhin abgelassen. Aufgefangenes Gas hält die Maschine nie an: ohne Behälter "
    "oder Platz wird es trotzdem abgelassen.",
    "- Rührwerk (Elektromotor, Stufe 4): arbeitet die Ladung 50 % schneller ab. Sein Motor verbrennt ein Viertel mehr "
    "Brennstoff, während die Ladung reagiert, oder zieht in einer elektrischen Maschine zusätzlichen Strom.",
    "- Katalysatorbett (Katalysatorbetten, Stufe 5): Reaktionen an einem festen Katalysator (Eisen, Nickel, Platin, "
    "Platin-Rhodium, Vanadiumpentoxid, Cobalt-Mangan-Bromid) laufen doppelt so schnell: Ammoniak, Kontaktverfahren, "
    "Salpetersäure, Kohlehydrierung, Reformieren, Nylon und mehr.",
    "- Förderband (Buna-Kautschuk, Stufe 5): ein Ausgabeschieber auf einem Gummiband; befördert fertige Produkte zu "
    "viert statt einzeln.",
    "- Schaumdämmung (Styrol und Polystyrol, Stufe 6): eine Maschine, deren Grenze bei 300 °C oder darunter liegt, "
    "verliert 70 % weniger Wärme.",
    "- Sterilluftfilter (Penicillin, Stufe 6): Gärfass, Essigbottich, Tieftankfermenter und Bioreaktor erzeugen 10 % "
    "mehr. Der Tieftankfermenter läuft nicht ohne ihn.",
]

FARM = [
    "# Fette und Seife",
    "Ausschmelzen (Stufe 1) schaltet den Seifenkessel frei. Hinein kommen Fleisch, Knochen, Eier und Milch; der Kessel "
    "köchelt sie zwischen 80 und 150 °C. Der Talg schwimmt oben, und das übrige Fleisch und die Knochen kommen als "
    "Proteinmehl heraus, das Kühe, Schafe, Ziegen, Schweine und Hühner fressen und das guten Kompost ergibt. Talg mit "
    "Faden ergibt Talgkerzen: Licht 12, rußend, und sie brennen drei Tage nach dem Aufstellen herunter.",
    "Seifensieden: lege Talg oder Pflanzenöl in den Kessel und Holzasche ins Reagenzfeld, um Schmierseife zu erhalten, "
    "mit Glycerin in Kolben. Kernseife (Stufe 2) verwendet stattdessen Natriumcarbonat oder härtet Schmierseife mit "
    "Salz.",
    "Benutze ein Stück Seife, um dich zu waschen: du bist zehn Minuten lang Sauber, und Sauber hebt den Hunger durch "
    "verrottetes Fleisch und rohes Hühnchen auf. Mit einer gefärbten Lederrüstung in der anderen Hand wäscht die Seife "
    "die Farbe heraus. Seife mit Wolle oder einem Bett im Handwerksfeld wäscht sie weiß.",
    "# Öl, Leder und Maische",
    "Ölpressen (Stufe 1): die Ölpresse presst Kürbis-, Melonen- und Weizenkerne, Kakaobohnen und Sonnenblumen kalt. "
    "Pflanzenöl füllt Kolben; der übrige Ölkuchen ist Viehfutter. Talg und Pflanzenöl brennen auch als Brennstoff.",
    "Grubengerbung (Stufe 1): die Lohgrube macht Fleisch und Häute ohne Handwerksfeld zu Leder, langsam: vier "
    "verrottete Fleischstücke ergeben in etwa drei Spieltagen ein Leder. Lege Eichen- oder Fichtenstämme wegen ihres "
    "Gerbstoffs ins Reagenzfeld; die ausgelaugte Gerberlohe, die übrig bleibt, brennt als Brennstoff. Alaunherstellung "
    "(Stufe 2) liefert Kalialaun, und zwei Kaninchenfelle mit einem Alaun werden im Handwerksfeld zu Leder gegerbt.",
    "Maischen (Stufe 1): das Gärfass verwandelt die Stärke von Weizen und Kartoffeln in Zucker und dann in Alkohol. "
    "Stärkezucker (Stufe 3) beschleunigt den ersten Schritt mit etwas Schwefelsäure im Aufschlussbottich. Süßes "
    "Vitriolöl (Stufe 2): mit Schwefelsäure im Alembik destilliertes Ethanol ergibt Diethylether.",
    "# Dünger",
    "Superphosphat (Stufe 3) löst Knochen oder Phosphatgestein in Schwefelsäure; Gaswasser (Stufe 3) wäscht das "
    "Ammoniak aus dem Koksofen (lege Kolben in sein Kolbenfeld), und die Bleikammer macht daraus Ammoniumsulfat. Benutze "
    "einen der beiden Dünger auf Ackerboden: für die nächsten acht Ernten wächst die Feldfrucht anderthalbmal so "
    "schnell, und jede reife Ernte gibt eins mehr.",
    "# Kautschuk",
    "Kautschukbäume wachsen wild im Dschungel (Dschungel, lichter Dschungel und Bambusdschungel). Ihr Laub lässt "
    "Setzlinge fallen, und der Setzling wächst überall, doch ein Kautschukbaum gibt nur im Dschungel Latex. "
    "Vulkanisation (Stufe 3) schaltet den Zapfhahn frei: befestige ihn seitlich an einem Kautschukbaumstamm und benutze "
    "ihn mit einem leeren Glaskolben; er füllt alle fünf Minuten einen Kolben und hält vier bereit. Trichter können ihm "
    "von oben leere Kolben geben und unten den Latex abziehen. Außerhalb des Dschungels meldet er, dass das Klima nicht "
    "passt.",
    "Im Vulkanisierkessel wird Latex mit Schwefel im Reagenzfeld zwischen 130 und 160 °C zu vulkanisiertem Kautschuk; "
    "ohne Schwefel trocknet er nur zu Rohkautschuk. Vulkanisierter Kautschuk ergibt Turnschuhe (deine Schritte erzeugen "
    "keine Vibrationen, und Magma und Schildkröteneier bemerken dich nicht) und Gummiräder: benutze sie an einer Lore, "
    "und sie fährt 25 % schneller.",
]

TAR = [
    "# Steinkohlenteer",
    "Teerdestillation (Stufe 3): sobald erforscht, fängt der Koksofen den Teer der Kohle in Kolben auf (lege leere "
    "Kolben in sein Kolbenfeld), etwa einen Kolben pro 13 Kohle. Die Teerdestille zerlegt Steinkohlenteer in Benzol, "
    "Toluol, Phenol (Karbolsäure) und Anthracen und lässt Pech zurück. Pech mit Kies und Sand ergibt Teermakadam, einen "
    "Straßenblock, so schnell wie Asphalt. Benzol, Phenol, Teer und Pech brennen als Brennstoff. Nitrobenzol (Stufe 3) "
    "nitriert Benzol im Aufschlussbottich, und Anilin (Stufe 4) reduziert es mit Eisen.",
    "# Farbstoffe",
    "Berliner Blau (Stufe 2) schaltet den Färbebottich frei: verrottetes Fleisch oder Fleisch mit Eisenvitriol im "
    "Reagenzfeld ergibt das Pigment, und der Bottich macht daraus blauen Farbstoff, 8 pro Gegenstand. Die Teerfarbstoffe "
    "ergeben 32 normale Farbstoffe pro Kolben: Fuchsin (Anilin und weißes Arsenik, Magenta), Pikrinsäure (Phenol und "
    "Salpetersäure im Aufschlussbottich, gelb) und Alizarin (Anthracen und Ätznatron im Autoklav, rot). Lege den "
    "Farbstoff in den Färbebottich, um normale Farbstoffe zu erhalten.",
    "# Infektion und Medizin",
    "Ein Treffer von einem Zombie, Wüstenzombie, Ertrunkenen oder Zombiedorfbewohner infiziert dich 1-mal von 200, "
    "verrottetes Fleisch zu essen 1-mal von 100; Sauber zu sein halbiert beides. Eine Infektion dauert einen Spieltag "
    "mit Schwäche und Hunger. Unbehandelt heilt sie 7-mal von 10 von selbst; die übrigen werden zur Sepsis, 1 Schaden "
    "alle 10 Sekunden, bis du stirbst. Karbolseife (Antiseptische Chirurgie: Phenol und Kernseife) und Aspirin (Stufe "
    "4: Phenol im Autoklav mit Natriumcarbonat, dann Essigsäure im Aufschlussbottich) heilen eine Infektion, bevor sie "
    "sich verschlimmert. Nur Sulfonamide (Stufe 5) und die Antibiotika der Stufe 6 heilen Sepsis. Milch heilt keins "
    "von beiden. Die Spielregel periodicElementsInfection schaltet die Infektion ab.",
    "Aspirin nimmt außerdem Übelkeit und Abbaulähmung und gibt 10 Sekunden Regeneration. Äthernarkose (Ether auf "
    "Wolle), angewendet auf einen Zombiedorfbewohner, dessen Heilung schon begonnen hat, halbiert die Heilungsdauer.",
    "# Sprengstoffe",
    "Dynamit (Stufe 4): Glycerin aus dem Seifensieden, kalt (5 bis 25 °C) im Aufschlussbottich nitriert, ergibt "
    "Nitroglycerin; mit Sand und Papier ergibt es Dynamit, und vier Stangen mit Faden ergeben eine Bergbauladung. Zünde "
    "sie mit einem Feuerzeug oder mit Redstone: nach 4 Sekunden bricht sie jeden Block eines 5x5x5-Würfels, der weniger "
    "widerstandsfähig als Obsidian ist, und alle fallen. Mit ausgeschaltetem mobGriefing verletzt sie nur, was in der "
    "Nähe ist. Cordit (Stufe 4) knetet Nitroglycerin mit rauchlosem Pulver; Corditpatronen feuern aus dem "
    "Hinterladergewehr 25 % stärker und ohne Rauch.",
    "# Feldfrüchte und Schädlinge",
    "Bordeauxbrühe (Kupfervitriol, Branntkalk und Wasser), auf eine Feldfrucht oder einen Süßbeerstrauch gesprüht, "
    "verhindert, dass sie weggespült wird oder im Dunkeln welkt, und Beerensträucher kratzen nicht mehr. Insektenpulver: "
    "die Ölpresse mahlt Margeriten zu Pyrethrum, und das Insektenspray verletzt Spinnen, Silberfischchen, Endermiten "
    "und Bienen bis zu 4 Blöcke voraus.",
    "# Vakuumpumpe",
    "Vakuumdestillation (Stufe 4) schaltet die Erweiterung Vakuumpumpe frei: in einem Alembik, einer Teerdestille, "
    "einem Seifenkessel, einem Kristallisator, einer Salzpfanne oder einer Raffineriedestille läuft jede Reaktion "
    "100 °C kühler ab, sodass die Maschine schneller aufheizt und weniger Wärme verliert.",
]

INDUSTRY = [
    "# Carbid und Acetylen",
    "Calciumcarbid (Stufe 5): Branntkalk mit Koks im Brennstofffeld des Lichtbogenofens schmilzt bei 1800 °C oder mehr "
    "zu Carbid. Im Aufschlussbottich mit Wasser im Reagenzfeld ergibt Carbid Acetylen (lege Kolben, oder Flaschen und "
    "einen Kompressor, in den Bottich) und lässt Löschkalk zurück.",
    "Die Karbidlampe (Messing, Glas, Carbid und ein Wassereimer) gibt Licht 14, in einer beliebigen Hand gehalten oder "
    "auf dem Kopf getragen, eine halbe Stunde lang. Benutze sie mit Calciumcarbid im Inventar, um sie nachzufüllen; "
    "eine leere Lampe geht nicht kaputt.",
    "Autogenschweißen: stelle einen Schweißbrenner aus einer Acetylen- und einer Sauerstoffflasche, einem Stahlbarren "
    "und einem Kupferbarren her. Halte ein beschädigtes Metallwerkzeug oder Rüstungsteil in der anderen Hand und benutze "
    "den Brenner: ein Barren aus seinem eigenen Metall aus deinem Inventar repariert ein Viertel, ohne Erfahrung. Der "
    "Brenner schafft 64 Schweißungen; stelle ihn mit frischen Flaschen neu her, um ihn aufzufüllen.",
    "Kalkstickstoff: Carbid im Kalkofen zwischen 1000 und 1150 °C mit Stickstoff im Reagenzfeld nimmt den Stickstoff "
    "als Calciumcyanamid auf, einen Dünger.",
    "# Dünger aus der Luft",
    "Harnstoffsynthese: Ammoniak im Hochdruckreaktor zwischen 170 und 200 °C, mit Kohlenstoffdioxid im Reagenzfeld "
    "(fange es mit einer Gasrückgewinnungshaube an einem Kalkofen oder Gärfass auf). Prilldünger schaltet den Prillturm "
    "frei, eine elektrische Maschine: Ammoniak mit Salpetersäure wird zu Ammoniumnitrat, und Harnstoff mit PK-Mischung "
    "(Superphosphat und Pottasche) zu NPK-Prills.",
    "Kalkstickstoff, Harnstoff und Ammoniumnitrat wirken auf Ackerboden wie Superphosphat. NPK-Prills lassen die "
    "Feldfrucht acht Ernten lang doppelt so schnell wachsen, jede mit einem mehr.",
    "# Kunststoffe und Kautschuk",
    "Formaldehyd: Methanol über Silber im Ostwald-Konverter zwischen 600 und 700 °C. Bakelit: Phenol mit Formaldehyd im "
    "Reagenzfeld, im Autoklav zwischen 150 und 200 °C. Vier Bakelit ergeben vier Bakelitblöcke, braun, oder mit einem "
    "Farbstoff schwarz, rot, grün, elfenbeinfarben oder bernsteinfarben. Bakelit dient auch als Gehäuse des "
    "Geigerzählers anstelle von Messing.",
    "Neopren: Acetylen mit Salzsäure im Hochdruckreaktor. Ein vollständiger Neoprenanzug gibt im Wasser Gunst des "
    "Delfins, und keines seiner Teile lässt die Kälte des Pulverschnees durch.",
    "Buna-Kautschuk, ohne Dschungel: Ethanol über dem Katalysator in der Crackanlage zwischen 380 und 450 °C ergibt "
    "Butadien; Benzol mit Ethylen im Reagenzfeld zwischen 550 und 600 °C ergibt Styrol; Butadien mit Styrol im "
    "Aufschlussbottich zwischen 40 und 60 °C ergibt Buna-Kautschuk. Er dient für Gummiräder und die Erweiterung "
    "Förderband.",
    "# Sprengstoffe",
    "TNT: Toluol aus der Teerdestille, im Aufschlussbottich nitriert. Ein TNT mit drei Papier ergibt drei Blöcke "
    "normales TNT. Vier TNT, ein Stahlbarren und Faden ergeben eine Abrissladung: wie eine Bergbauladung gezündet, "
    "bricht sie einen 3x3x3-Würfel aus allem außer Grundgestein, Obsidian eingeschlossen, und alles fällt.",
    "# Gärung, Kraftstoff und Medizin",
    "Acetongärung: Weizen oder Kartoffeln im Gärfass, mit Calcit im Reagenzfeld, vergären zu Aceton, Butanol und etwas "
    "Ethanol statt zu Zucker. Biodiesel: Talg oder Pflanzenöl im Seifenkessel zwischen 40 und 150 °C mit Methanol im "
    "Reagenzfeld. Biodiesel und Butanol treiben den Dieselgenerator an und brennen in Öfen.",
    "Sulfonamide: Anilin mit Ammoniumsulfat im Aufschlussbottich ergibt Sulfanilamid. Benutze es, um eine Infektion in "
    "jedem Stadium zu heilen, Sepsis eingeschlossen.",
]

LIFE = [
    "# Antibiotika",
    "Penicillin (Stufe 6) schaltet den Tieftankfermenter frei, eine elektrische Maschine, die nur mit einem "
    "Sterilluftfilter läuft. Glucose mit Milch im Reagenzfeld (wegen ihrer Lactose) züchtet zwischen 20 und 37 °C "
    "Penicillin; Streptomycin braucht Proteinmehl im Reagenzfeld. Beide Antibiotika heilen eine Infektion oder Sepsis "
    "und machen dich 20 Minuten lang immun gegen Infektion. Benutze Penicillin an einem Zombiedorfbewohner mit Schwäche, "
    "und seine Heilung beginnt, ohne goldenen Apfel. Das verbrauchte Myzel aus dem Tank ist Dünger.",
    "# Pestizide",
    "DDT: Benzol mit Chlor im Aufschlussbottich. Ein DDT-Stäuber, auf den Boden angewendet, macht ihn drei Spieltage "
    "lang tödlich für Spinnen, Silberfischchen, Endermiten und Bienen; Hühner darauf legen nur halb so viel. Der "
    "Unkrautvernichter (2,4-D, aus Phenol und Chlor) tötet Blumen, Farne, Ranken, Sträucher, Setzlinge und Laub rund "
    "um den Block, verschont aber Gras und Feldfrüchte. Das Vorauflaufmittel (Atrazin, aus Ammoniak und Chlor mit Koks "
    "im Hochdruckreaktor) verhindert zehn Spieltage lang, dass sich Gras und Myzel auf dem Boden ausbreiten. Jedes "
    "Sprühgerät hat 16 Anwendungen und wirkt 4 Blöcke im Umkreis.",
    "# Kunststoffe",
    "Styrol und Polystyrol: Styrol im Hochdruckreaktor. Acht Polystyrol um einen Kolben Methan ergeben Schaumdämmung; "
    "vier Polystyrol und Methan ergeben vier Dämmungsblöcke, und Eis oder Schnee, das die Dämmung berührt, schmilzt "
    "nie.",
    "# Sprengstoffe",
    "Plastiksprengstoff: Formaldehyd und Ammoniak ergeben Hexamin, und kalt nitriertes Hexamin ergibt RDX. Zwei RDX, "
    "ein Kupferbarren, ein Stahlbarren und Faden ergeben eine Hohlladung: setze sie an eine Wand und zünde sie, und sie "
    "öffnet einen 1x1 großen, 16 Blöcke tiefen Tunnel durch alles außer Grundgestein. ANFO: sechs Ammoniumnitrat, "
    "Diesel, Papier und Faden ergeben eine ANFO-Ladung, die einen 9x9x9-Würfel aus Gestein bricht; drei von vier "
    "Blöcken fallen.",
    "# Strahlenschutzmaßnahmen",
    "Kaliumiodid (Natriumiodid aus Seetang mit Kalisalz im Kristallisator) senkt die Dosis aus Spaltprodukten einen "
    "Spieltag lang um 40 %. Eine Berliner-Blau-Kapsel (Berliner Blau und Proteinmehl) senkt sie um ein Viertel. "
    "Amifostin (Stufe 7) halbiert eine Stunde lang den Schaden jeder Strahlung.",
    "# Biotechnologie",
    "Rekombinante DNA (Stufe 7) schaltet den Bioreaktor frei (er braucht einen integrierten Schaltkreis). Er vergärt die "
    "Cellulose aus Stämmen, Laub oder Stroh zu Ethanol, Glucose zu PLA und Protein mit Pflanzenöl zum mRNA-Impfstoff, "
    "sieben Spieltage ohne Infektion. Seine Biomasse ist Dünger. PLA ergibt Blumentöpfe und Rahmen und taugt als "
    "Kompost.",
    "Glyphosat: Formaldehyd mit Phosphorsäure im Hochdruckreaktor. Der Feldräumer tötet alle Pflanzen rund um den Block, "
    "Feldfrüchte eingeschlossen, außer Weizen aus tolerantem Saatgut (acht Weizenkörner um ein Glyphosat). Toleranter "
    "Weizen wächst außerdem ein Zehntel schneller.",
]

MINING = [
    "# Vorschlaghämmer und Grabschaufeln",
    "Steinbruchhämmer (Stufe 2) lehrt den Vorschlaghammer (fünf Barren um einen Stock, über einem Stock) und die "
    "Grabschaufel (drei Barren und zwei Stöcke), aus Stahl und jedem späteren Werkzeugmetall. Jedes bricht ein "
    "3x3-Quadrat auf der Seite, die du triffst, mit drei Zehnteln der Geschwindigkeit, und nutzt sich um einen Punkt "
    "pro Block ab. Schleiche, um einen einzelnen Block abzubauen. Es brechen nur Blöcke, die zum Werkzeug passen und "
    "nicht härter sind als der anvisierte; nichts mit Inventar und keine Maschine bricht je; was fällt, geht direkt in "
    "dein Inventar.",
    "# Sprengen",
    "Gesteinssprengung (Stufe 2): vier Schwarzpulver, Papier und Faden ergeben zwei Sprengladungen. Mit einem "
    "Feuerzeug oder mit Redstone gezündet, bricht eine Ladung einen 3x3x3-Block aus Stein und lässt alles fallen; "
    "Erde, Holz und Maschinen bleiben verschont.",
    "# Bohrer und Bohrkronen",
    "@4 Jeder Bohrer braucht eine Bohrkrone: halte die Bohrkrone in der anderen Hand, schleiche und benutze den Bohrer. "
    "Die Bohrkrone entscheidet, was er abbauen kann und wie schnell (Wolframstahl 8,5, Schnellarbeitsstahl 10, "
    "Hartmetall 12, Diamant 13), und nutzt sich um einen Punkt pro Schlag ab. Verzaubere die Bohrkrone, nicht den "
    "Bohrer; eine eingesetzte Bohrkrone leiht dem Bohrer ihre Verzauberungen, alle außer Reparatur. Schleiche und "
    "benutze ihn ohne Bohrkrone in der anderen Hand, um den Modus zu wechseln. Ein Bohrer verbraucht 1,5 kJ pro "
    "Härtepunkt jedes Blocks: 2,25 für Stein, 4,5 für Tiefenschiefer.",
    "Gesteinsbohrer (Stufe 4): der Luftkompressor, eine elektrische Maschine, füllt Stahlflaschen mit Druckluft, ohne "
    "die Erweiterung Kompressor zu brauchen. Der Druckluft-Gesteinsbohrer verbraucht eine Flasche pro 600 kJ (etwa 400 "
    "Stein) und gibt die leere zurück; er baut einen Block, einen 1x3- oder einen 3x3-Tunnel ab und bremst dich, "
    "solange du ihn hältst.",
    "# Akkuwerkzeuge",
    "Bleiakku (Stufe 4): der Akkupack fasst 1500 kJ. Verbinde eine Ladebank mit dem Kupferdraht und benutze den Akku "
    "darauf; er lädt mit bis zu 5 kJ pro Tick.",
    "Der Bohrhammer (Stufe 5) ist der Gesteinsbohrer mit Akku, ohne dich zu bremsen. Die Kettensäge (Stufe 5) fällt "
    "einen ganzen Baum, bis zu 128 Stämme, für 3 kJ pro Stamm.",
    "Der Schwere Bergbaubohrer (Stufe 6) bietet zusätzlich einen 5x5-Modus und einen Adermodus, der einem Erz bis zu "
    "32 Blöcke folgt; mit einem Bleiakku bremst er dich.",
    "Der Lithium-Ionen-Akku (Stufe 7) fasst 6000 kJ, wird zuerst verbraucht und hebt diese Verlangsamung auf. "
    "Erzsuchender Bohrer (Stufe 7): im Adermodus benutzt du den Schweren Bergbaubohrer an einem Erz, um die Größe und "
    "die Elemente der Ader zu lesen, bevor du gräbst.",
]

ISOTOPES = [
    "# Strahlung",
    "Radioaktive Gegenstände geben eine Dosis ab, die sich ansammelt, während du sie trägst oder in ihrer Nähe bist. "
    "Ein Elektroskop, später ein Geigerzähler, misst sie. Bleiwände, die Heiße Zelle und der Kernreaktor schirmen ab, "
    "was sie enthalten. Manche Proben zerfallen nach der Spieluhr (ein Spieltag ist für sie ein echter Tag) und werden "
    "in deinem Inventar zu ihren Tochternukliden.",
    "# Blaues Leuchten",
    "Die stärksten Strahler leuchten im Dunkeln blau, wie Tscherenkow-Licht: Radium und die stärksten an der Luft, "
    "und unter Wasser auch Polonium, Americium, Actinium und Pechblende, heller. Ein laufender Kernreaktor erhellt "
    "sein Becken, und laufende Beschleuniger leuchten ebenfalls. Das Leuchten hängt davon ab, wie konzentriert die "
    "Aktivität ist, nicht davon, wie viel du hast; Uran leuchtet nie. Leuchtende Gegenstände leuchten auch in ihrem "
    "Inventarplatz.",
    "# Natürliche Isotope",
    "Die Beschreibung jedes stabilen Elements listet seine natürlichen Isotope auf, vom häufigsten zum seltensten.",
    "# Veränderliche Verhältnisse",
    "Gegenstände aus Uran (U-235), Plutonium (Pu-240), Lithium (Li-6), Bor (B-10), Calcium (Ca-48) und Wasserstoff "
    "(H-2) zeigen ihr eigenes Verhältnis. Ein Gegenstand mit dem natürlichen Verhältnis stapelt sich mit jedem anderen; "
    "ein angereicherter oder abgereicherter stapelt sich nur mit Gegenständen desselben Verhältnisses. Maschinen "
    "mischen die Verhältnisse nach Masse.",
    "- Das Calutron (Calutron-Anreicherung) trennt Uran, Lithium oder Bor in einen kleinen angereicherten Anteil und "
    "abgereicherte Tails. Gib den Anteil erneut hinein, um weiter anzureichern.",
    "- Uranmetall mit 3 % U-235 oder mehr wird zu Brennstoffpellets für den Reaktor gesintert.",
    "- Im Reaktor erbrütetes Plutonium ist waffenfähig (6 % Pu-240); Plutonium aus abgebranntem Brennstoff hat "
    "Reaktorqualität.",
    "- Lithium mit über 90 % Li-6 in den Feldern eines Reaktors wird zu Tritium, das Tritiumleuchten für immer "
    "leuchten lässt.",
    "- Bor mit über 90 % B-10 ergibt Steuerstäbe: mit einem in seinen Feldern verbrennt ein Reaktor keinen Brennstoff, "
    "solange er voll ist.",
    "- Schweres Wasser aus der Anreicherungskaskade (füttere sie mit Eis oder Schnee) erlaubt es, Natururan-Brennstäbe "
    "zu verbrennen.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Erste Schritte",
    "group": "Stufe {n}",
    "research": "Forschung",
    "machines": "Maschinen",
    "reactions": "Reaktionen",
    "recipe": " Rezept: {r}.",
    "recipe_head": "Rezept: {r}.",
    "projects": "# Projekte (★ = Schlüssel)",
    "needs": " Erfordert {x}.",
    "samples": " Proben: {x}.",
    "unlocks": " Schaltet frei: {x}.",
    "up_to": "{desc}. Bis {c} °C",
    "electric": ", elektrisch.",
    "fuel": ", brennstoffbeheizt.",
    "amounts": "Mengen pro 100 Einheiten Ladung.",
    "ores": "Erze",
    "ores_head": "# Erze",
    "ores_intro": "Erze erscheinen in neu erkundeten Chunks. Behutsamkeit und Glück wirken wie bei normalen Erzen.",
    "ore_line": "{name}: {desc}. Lässt {lo}-{hi} {drop} fallen; erfordert eine Spitzhacke aus {tier}. ",
    "found": "Vorkommen {w}.",
    "where": "von y {lo} bis {hi} {b}",
    "biomes": "in: {b}",
    "overworld": "in der Oberwelt",
    "nether": "im Nether",
    "power": "Energie",
    "power_head": "# Energie",
    "power_intro": "Elektrische Maschinen beziehen Strom von Energieblöcken daneben oder über Kupferdraht (bis zu 64 "
                   "Blöcke Draht) oder isoliertes Kabel (viermal so weit) verbundenen.",
    "power_line": "{name} (Stufe {lvl}, {project}): {desc}.",
    "isotopes": "Strahlung und Isotope",
    "containers": "Behälter",
    "upgrades": "Erweiterungen",
    "farm": "Hof und Fette",
    "tar": "Teer und Heilmittel",
    "industry": "Industriechemie",
    "life": "Biowissenschaften",
    "mining": "Bergbau",
    "crafting": "Herstellung",
    "craft_head": "# Herstellung",
    "craft_intro": "Die formlosen Rezepte, die die Mod hinzufügt (Rezepte für Maschinen und Blöcke stehen in den "
                   "Kapiteln der einzelnen Stufen).",
    "shaped_head": "# Geformte Rezepte (Reihen von oben nach unten)",
    "or": " oder ",
    "any": "beliebiger {x}",
    "empty": "leer",
    "reagent": "(Reagenz)",
    "carbon": " + {c} Kohlenstoff",
    "nothing": "nichts wird behalten",
    "slag": "; Schlacke: ",
    "vents": "; lässt ab: ",
    "temp": ". Von {a} bis {b} °C.",
    "sep": "; ",
}

WORDS = {
    # pickaxe tiers
    "wood": "Holz", "stone": "Stein", "iron": "Eisen",
    # biomes and biome tags
    "mountain": "Gebirge", "jungle": "Dschungel", "savanna": "Savannen", "desert": "Wüste",
    "badlands": "Tafelberge", "ocean": "Ozeane", "swamp": "Sümpfe", "mangrove swamp": "Mangrovensümpfe",
    "basalt deltas": "Basaltdeltas", "beach": "Strände", "river": "Flüsse", "dripstone caves": "Tropfsteinhöhlen",
    # item tags
    "logs": "Stamm", "logs that burn": "brennbarer Stamm", "oak logs": "Eichenstamm", "planks": "Bretter",
    "wool": "Wolle", "beds": "Bett", "cable insulation": "Kabelisolierung", "soap": "Seife",
    # upgrades unlocked by research
    "forced air": "Gebläseluft (Blasebalg)", "steam blast": "Dampfgebläse",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["de_de"]
T["atlas"] = _ATLAS_TITLE["de_de"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["de_de"]
T.update(_TREES_T["de_de"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("de_de")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["de_de"]
T["compendium"] = _COMP_TITLE["de_de"]

# 3.5.0 Plant Compendium (tools/compendium_plants_de_de.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["de_de"]
T["plant_compendium"] = _PCOMP_TITLE["de_de"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_de_de.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["de_de"]
T["mineral_compendium"] = _MCOMP_TITLE["de_de"]
