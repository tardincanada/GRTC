"""Dutch (nl_nl) text of the Field Guide (2.5.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the nl_nl lang file.
"""

DECIMAL = ","

INTRO = [
    "# Welkom",
    "Elk blok en elk voorwerp in deze wereld is gemaakt van echte elementen. Deze gids volgt de technologieboom van de "
    "mod van de steentijd tot de laatste elementen, één hoofdstuk per niveau.",
    "# Eenheden",
    "1 eenheid is 1 gram. Een blok is 1000 eenheden, een staaf of een element- of stofvoorwerp 111,1, een klompje 12,35 "
    "en een emmer 1000. Beweeg de muis over een blok of voorwerp om te zien waarvan het gemaakt is; houd Shift "
    "ingedrukt voor alle delen.",
    "# Onderzoek",
    "Elk niveau heeft een onderzoeksstation. Leg de monsters van een project in zijn 9 vakken, kies het project en druk "
    "op Onderzoeken. Sleutelprojecten (met een ster) verhogen je technologieniveau; je hebt de sleutelprojecten van een "
    "niveau nodig voordat je het volgende station kunt plaatsen. /research toont wat je weet.",
    "Verhandelingen die bij een station geschreven zijn, leren een andere speler alles wat jij weet.",
    "# Machines",
    "Een machine ontleedt elk ingevoerd voorwerp in zijn samenstelling en laat er elke reactie op los die je onderzocht "
    "hebt, zolang ze heet genoeg en niet te heet is. Producten komen eruit als voorwerpen, afval als slak, en sommige "
    "gassen worden uitgestoten. Vloeistoffen en gassen vullen lege glazen kolven vanuit het zijpaneel van de machine. "
    "Sommige reacties hebben een tweede ingrediënt in het reagensvak nodig.",
    "Machines die met vuur verwarmd worden, verbranden brandstof uit het brandstofvak: hout haalt 650 °C, houtskool en "
    "steenkool 1100 °C, oliebrandstoffen 1200 °C en gas 1300 °C. Een blaasbalg (geforceerde lucht) voegt 250 °C toe. "
    "Houtskool en steenkool dienen ook als de koolstof die ertsen reduceert. Elektrische machines halen stroom uit een "
    "zuil van Volta, een dynamo of een ander energieblok ernaast of verbonden met koperdraad.",
    "Gewone ovens smelten geen ijzer-, koper- of gouderts meer: metalen komen uit de machines.",
    "# Niveau 0",
    "Zonder enig onderzoek kun je een kolenmeiler bouwen (8 aarde rond een stam) en stammen tot houtskool branden.",
]

CONTAINERS = [
    "# Glazen kolven",
    "Elk vloeistof- of gasvoorwerp is een afgesloten glazen kolf met 111,1 eenheden. Vijf glas maken vier kolven, "
    "zonder onderzoek. Een machine vult voor elke vloeistof of elk gas dat ze maakt een lege kolf uit het bovenste vak "
    "van haar zijpaneel; zonder kolven stopt ze en vraagt erom. Als een machine, recept, oven of onderzoeksproject de "
    "inhoud van een kolf gebruikt, krijg je de lege kolf terug.",
    "# Stalen gasflessen en de compressor",
    "Gascompressie (niveau 4) ontgrendelt stalen gasflessen en de compressor. Een gasfles bevat 1000 eenheden van één "
    "samengeperst gas: negen kolven in één voorwerp. Zet een compressor in een van de twee upgradevakken van een machine "
    "en leg lege gasflessen in het tweede vak van haar zijpaneel: de machine vult dan gasflessen met elk gas dat ze "
    "maakt, één gasfles tegelijk. Vloeistoffen gaan nog steeds in kolven. Een gasfles brandbaar gas brandt als negen "
    "kolven.",
    "Machines nemen gasflessen aan als invoer zoals elk ander voorwerp en geven de lege gasfles terug.",
    "# Opslagtank",
    "Pneumatische chemie (niveau 3) ontgrendelt de opslagtank. Hij bevat 16.000 eenheden (144 kolven) van één vloeistof "
    "of gas. Gebruik hem met een volle kolf of gasfles om die erin te gieten, met een lege kolf om er een uit te tappen, "
    "en met een lege hand om het niveau af te lezen. Trechters werken ook: stop volle of lege houders er van boven of "
    "opzij in, en haal wat eruit komt van onderen weg. Een redstonevergelijker leest hoe vol hij is. Als je hem breekt, "
    "loopt de inhoud weg.",
]

UPGRADES = [
    "# Upgradevakken",
    "Elke machine heeft twee upgradevakken onderaan haar zijpaneel. Een upgrade werkt zodra de eigenaar haar onderzocht "
    "heeft; twee dezelfde tellen één keer. Upgrades die als 'op brandstof' gemarkeerd zijn, doen niets in elektrische "
    "machines.",
    "- Compressor (Gascompressie, niveau 4): vult stalen gasflessen met de gassen die de machine maakt.",
    "- Uitvoerduwer (Automatische molen, niveau 3): verplaatst de producten van de machine naar een kist, vat of trechter "
    "ernaast. Hij voedt nooit rechtstreeks een andere machine.",
    "- Regenerator (Regeneratieve oven, niveau 4): een machine op brandstof verliest half zoveel warmte, dus ze verbrandt "
    "minder brandstof en warmt sneller op.",
    "- Vuurvaste bekleding (Basische vuurvaste materialen, niveau 4): een machine op brandstof kan 150 °C boven haar eigen "
    "grens draaien, als haar brandstof en blaasbalg zo ver komen.",
    "- Gasopvangkap (Terugwinning van bijproducten, niveau 4): de vloeistoffen en gassen die de machine zou uitstoten "
    "(koolstofdioxide, zwaveldioxide, zoutzuur en andere) gaan in kolven of gasflessen. Stoom en gemengde gassen zoals "
    "lichtgas worden nog steeds uitgestoten. Opgevangen gas stopt de machine nooit: zonder houder of ruimte wordt het "
    "toch uitgestoten.",
    "- Mechanische roerder (Elektromotor, niveau 4): werkt de lading 50% sneller af. De motor verbrandt een kwart meer "
    "brandstof terwijl de lading reageert, of trekt extra stroom in een elektrische machine.",
    "- Katalysatorbed (Katalysatorbedden, niveau 5): reacties op een vaste katalysator (ijzer, nikkel, platina, "
    "platina-rodium, vanadiumpentoxide, kobalt-mangaanbromide) gaan twee keer zo snel: ammoniak, het contactproces, "
    "salpeterzuur, kolenhydrogenering, reformen, nylon en meer.",
    "- Transportband (Bunarubber, niveau 5): een uitvoerduwer op een rubberen band; hij verplaatst afgewerkte producten "
    "met vier tegelijk in plaats van één.",
    "- Schuimisolatie (Styreen en polystyreen, niveau 6): een machine met een grens van 300 °C of minder verliest 70% "
    "minder warmte.",
    "- Steriel luchtfilter (Penicilline, niveau 6): het gistingsvat, de azijnkuip, de tankfermentor en de bioreactor "
    "maken 10% meer product. De tankfermentor draait niet zonder.",
]

FARM = [
    "# Uitsmelten en zeep",
    "Uitsmelten (niveau 1) ontgrendelt de zeepketel. Vlees, botten, eieren en melk gaan erin; de ketel laat ze sudderen "
    "op 80 tot 150 °C. Talg drijft boven, en het vlees en bot dat overblijft komt eruit als eiwitmeel, dat koeien, "
    "schapen, geiten, varkens en kippen eten en dat goed composteert. Talg met draad geeft talgkaarsen: licht 12, "
    "rokerig, en ze branden drie dagen na het plaatsen op.",
    "Zeepziederij: leg talg of plantaardige olie in de ketel en houtas in het reagensvak voor zachte zeep, met glycerol "
    "in kolven. Harde zeep (niveau 2) gebruikt in plaats daarvan soda, of maakt zachte zeep hard met zout.",
    "Gebruik een stuk zeep om je te wassen: je bent tien minuten Schoon, en Schoon wast de Honger van bedorven vlees en "
    "rauwe kip weg. Met geverfd leren pantser in je andere hand schrobt de zeep juist de verf eruit. Zeep met wol of "
    "een bed in het werkbankrooster wast het wit.",
    "# Olie, leer en beslag",
    "Olie persen (niveau 1): de oliepers perst pompoen-, meloen- en tarwezaden, cacaobonen en zonnebloemen koud. "
    "Plantaardige olie vult kolven; de oliekoek die overblijft is veevoer. Talg en plantaardige olie branden ook als "
    "brandstof.",
    "Kuillooien (niveau 1): de looikuil maakt van vlees en huiden leer zonder werkbank, langzaam: vier bedorven vlees "
    "geven één leer in ongeveer drie speldagen. Leg eiken- of sparrenstammen in het reagensvak voor hun looizuur; de "
    "uitgeputte looischors die overblijft brandt als brandstof. Aluinbereiding (niveau 2) geeft kaliumaluin, en twee "
    "konijnenhuiden met één aluin worden in het werkbankrooster tot leer gelooid.",
    "Beslag maken (niveau 1): het gistingsvat zet het zetmeel van tarwe en aardappels om in suiker en daarna in "
    "alcohol. Zetmeelsuiker (niveau 3) doet de eerste stap sneller met een beetje zwavelzuur in het ontsluitingsvat. "
    "Zoete vitrioololie (niveau 2): ethanol gedestilleerd met zwavelzuur in de alambiek geeft diëthylether.",
    "# Meststof",
    "Superfosfaat (niveau 3) lost botten of fosfaaterts op in zwavelzuur; Gaswater (niveau 3) wast ammoniak uit de "
    "cokesoven (houd kolven in het kolfvak) en de loodkamer maakt er zwavelzure ammoniak van. Gebruik een van beide "
    "meststoffen op akkerland: de volgende acht oogsten groeit het gewas erop anderhalf keer zo snel en geeft een rijpe "
    "oogst er één meer.",
    "# Rubber",
    "Rubberbomen groeien in het wild in oerwouden (oerwoud, dunbegroeid oerwoud en bamboe-oerwoud). Hun bladeren laten "
    "zaailingen vallen, en de zaailing groeit overal, maar een rubberboom geeft alleen in een oerwoud latex. Vulkanisatie "
    "(niveau 3) ontgrendelt de tapkraan: bevestig hem aan de zijkant van een rubberboomstam en gebruik hem met een lege "
    "glazen kolf; hij vult elke vijf minuten een kolf en houdt er vier vast. Trechters kunnen hem van boven lege kolven "
    "geven en van onderen latex weghalen. Buiten het oerwoud meldt hij het verkeerde klimaat.",
    "In de vulkaniseerketel hardt latex met zwavel in het reagensvak uit tot gevulkaniseerd rubber bij 130 tot 160 °C; "
    "zonder zwavel droogt het alleen tot ruw rubber. Gevulkaniseerd rubber geeft gympen (je stappen maken geen "
    "trillingen, en magma en schildpadeieren merken je niet op) en rubberen wielen: gebruik ze op een mijnkar en hij "
    "rijdt 25% sneller.",
]

TAR = [
    "# Koolteer",
    "Teerdestillatie (niveau 3): als het onderzocht is, vangt de cokesoven de teer van de steenkool op in kolven (houd "
    "lege kolven in het kolfvak), ongeveer één kolf per 13 steenkool. De teerdistilleerder scheidt koolteer in benzeen, "
    "tolueen, fenol (carbolzuur) en antraceen, en laat koolteerpek achter. Pek met grind en zand geeft teermacadam, een "
    "wegblok zo snel als asfalt. Benzeen, fenol, teer en pek branden allemaal als brandstof. Nitrobenzeen (niveau 3) "
    "nitreert benzeen in het ontsluitingsvat, en Aniline (niveau 4) reduceert het met ijzer.",
    "# Kleurstoffen",
    "Pruisisch blauw (niveau 2) ontgrendelt de verfkuip: bedorven vlees of vlees met groene vitriool in het reagensvak "
    "geeft het pigment, en de kuip maakt er blauwe kleurstof van, 8 per voorwerp. De koolteerkleurstoffen geven 32 "
    "gewone kleurstoffen per kolf: fuchsine (aniline en wit arseen, magenta), picrinezuur (fenol en salpeterzuur in "
    "het ontsluitingsvat, geel) en alizarine (antraceen en natronloog in de autoclaaf, rood). Leg de kleurstof in de "
    "verfkuip voor gewone kleurstoffen.",
    "# Infectie en geneeskunde",
    "Een klap van een zombie, mummie, drenkeling of zombiedorpeling besmet je 1 keer op 200, en bedorven vlees eten 1 "
    "keer op 100; Schoon zijn halveert beide. Een Infectie duurt een speldag met Zwakheid en Honger. Onbehandeld gaat "
    "ze 7 op 10 keer vanzelf over; de andere keren wordt ze Sepsis, 1 schade elke 10 seconden tot je sterft. Carbolzeep "
    "(Antiseptische chirurgie: fenol en harde zeep) en aspirine (niveau 4: fenol in de autoclaaf met soda, daarna "
    "azijnzuur in het ontsluitingsvat) genezen een Infectie voordat ze omslaat. Alleen sulfapreparaten (niveau 5) en de "
    "antibiotica van niveau 6 genezen Sepsis. Melk geneest geen van beide. De spelregel periodicElementsInfection zet "
    "infectie uit.",
    "Aspirine verdrijft ook Misselijkheid en Mijnmoeheid en geeft 10 seconden Regeneratie. Ether-verdoving (ether op "
    "wol), gebruikt op een zombiedorpeling wiens genezing begonnen is, halveert de tijd die de genezing duurt.",
    "# Springstoffen",
    "Dynamiet (niveau 4): glycerol uit de zeepziederij, koud genitreerd (5 tot 25 °C) in het ontsluitingsvat, geeft "
    "nitroglycerine; met zand en papier maakt het dynamiet, en vier staven met draad maken een mijnlading. Ontsteek "
    "hem met vuursteen en staal of redstone: na 4 seconden breekt hij elk blok in een kubus van 5x5x5 dat zwakker is "
    "dan obsidiaan, en elk ervan valt. Met mobGriefing uit schaadt hij alleen wat dichtbij staat. Cordiet (niveau 4) "
    "kneedt nitroglycerine met rookzwak kruit; cordietpatronen schieten uit de achterlader 25% harder en zonder rook.",
    "# Gewassen en plagen",
    "Bordeauxse pap (blauwe vitriool, ongebluste kalk en water) gespoten op een gewas of bessenstruik zorgt dat het "
    "niet wegspoelt of in het donker verwelkt, en bessenstruiken prikken niet meer. Insectenpoeder: de oliepers maalt "
    "margrieten tot pyrethrum, en een insectenspray schaadt spinnen, zilvervissen, endermijten en bijen tot 4 blokken "
    "voor je.",
    "# Vacuümpomp",
    "Vacuümdestillatie (niveau 4) ontgrendelt de upgrade Vacuümpomp: in een alambiek, teerdistilleerder, zeepketel, "
    "kristallisator, zoutpan of raffinaderijdistilleerder verloopt elke reactie 100 °C koeler, dus de machine warmt "
    "eerder op en verliest minder warmte.",
]

INDUSTRY = [
    "# Carbid en acetyleen",
    "Calciumcarbide (niveau 5): ongebluste kalk met cokes in het brandstofvak van de elektrische vlamboogoven smelt "
    "bij 1800 °C en meer samen tot carbid. In het ontsluitingsvat met water in het reagensvak geeft carbid acetyleen "
    "(houd kolven, of gasflessen en een compressor, in het vat) en laat gebluste kalk achter.",
    "De carbidlamp (messing, glas, carbid en een emmer water) geeft licht 14 in een van beide handen of op het hoofd, "
    "een half uur lang. Gebruik hem met calciumcarbide in je inventaris om hem bij te vullen; een lege lamp blijft heel.",
    "Autogeen lassen: maak een lasbrander uit een gasfles acetyleen en een gasfles zuurstof, een staalstaaf en een "
    "koperstaaf. Houd beschadigd metalen gereedschap of een stuk pantser in je andere hand en gebruik de brander: één "
    "staaf van zijn eigen metaal uit je inventaris herstelt er een kwart van, zonder ervaring. De brander doet 64 lassen; "
    "maak hem opnieuw met verse gasflessen om hem bij te vullen.",
    "Kalkstikstof: carbid in de kalkoven bij 1000 tot 1150 °C met stikstof in het reagensvak neemt de stikstof op als "
    "calciumcyanamide, een meststof.",
    "# Meststof uit de lucht",
    "Ureumsynthese: ammoniak in de hogedrukreactor bij 170 tot 200 °C, met koolstofdioxide in het reagensvak (vang het "
    "op uit een kalkoven of gistingsvat met een gasopvangkap). Prillkunstmest ontgrendelt de prilltoren, een elektrische "
    "machine: ammoniak met salpeterzuur wordt ammoniumnitraat, en ureum met PK-mengsel (superfosfaat en kali) wordt "
    "NPK-korrels.",
    "Kalkstikstof, ureum en ammoniumnitraat werken op akkerland zoals superfosfaat. NPK-korrels laten het gewas twee "
    "keer zo snel groeien, acht oogsten lang, die elk er één meer geven.",
    "# Plastics en rubber",
    "Formaldehyde: methanol over zilver in de Ostwald-converter bij 600 tot 700 °C. Bakeliet: fenol met formaldehyde in "
    "het reagensvak, in de autoclaaf bij 150 tot 200 °C. Vier bakeliet geven vier bakelietblokken, bruin, of met een "
    "kleurstof zwart, rood, groen, ivoor of amber. Bakeliet dient ook als behuizing van een Geigerteller in plaats van "
    "messing.",
    "Neopreen: acetyleen met zoutzuur in de hogedrukreactor. Een volledige wetsuit van neopreen geeft Gunst der "
    "dolfijnen in het water, en geen enkel deel laat de kou van poedersneeuw door.",
    "Bunarubber, zonder oerwoud: ethanol over de katalysator in de kraakinstallatie bij 380 tot 450 °C geeft butadieen; "
    "benzeen met etheen in het reagensvak bij 550 tot 600 °C geeft styreen; butadieen met styreen in het "
    "ontsluitingsvat bij 40 tot 60 °C geeft bunarubber. Het maakt rubberen wielen en de upgrade Transportband.",
    "# Springstoffen",
    "TNT: tolueen uit de teerdistilleerder, genitreerd in het ontsluitingsvat. Eén TNT met drie papier geeft drie "
    "blokken gewoon TNT. Vier TNT, een staalstaaf en draad maken een slooplading: ontstoken zoals een mijnlading breekt "
    "ze een kubus van 3x3x3 van alles behalve bodemsteen, obsidiaan inbegrepen, en alles valt.",
    "# Gisting, brandstof en geneeskunde",
    "Acetongisting: tarwe of aardappels in het gistingsvat, met calciet in het reagensvak, vergisten tot aceton, butanol "
    "en een beetje ethanol in plaats van suiker. Biodiesel: talg of plantaardige olie in de zeepketel bij 40 tot 150 °C "
    "met methanol in het reagensvak. Biodiesel en butanol laten de dieselgenerator draaien en branden in ovens.",
    "Sulfapreparaten: aniline met ammoniumsulfaat in het ontsluitingsvat geeft sulfanilamide. Gebruik het om een "
    "Infectie in beide stadia te genezen, Sepsis inbegrepen.",
]

LIFE = [
    "# Antibiotica",
    "Penicilline (niveau 6) ontgrendelt de tankfermentor, een elektrische machine die alleen draait met een steriel "
    "luchtfilter. Glucose met melk in het reagensvak (voor de lactose) kweekt penicilline bij 20 tot 37 °C; "
    "streptomycine neemt in plaats daarvan eiwitmeel in het reagensvak. Elk van beide antibiotica geneest een Infectie of "
    "Sepsis en maakt je 20 minuten immuun voor Infectie. Gebruik penicilline op een zombiedorpeling met Zwakheid en zijn "
    "genezing begint, zonder gouden appel. Het uitgeputte mycelium van de tank is meststof.",
    "# Bestrijdingsmiddelen",
    "DDT: benzeen met chloor in het ontsluitingsvat. Een DDT-verstuiver gebruikt op de grond maakt die drie speldagen "
    "dodelijk voor spinnen, zilvervissen, endermijten en bijen; kippen erop leggen half zo vaak. Onkruidverdelger "
    "(2,4-D, uit fenol en chloor) doodt bloemen, varens, ranken, struiken, zaailingen en bladeren rond het blok, maar "
    "spaart gras en gewassen. Kiemremmer (atrazine, uit ammoniak en chloor met cokes in de hogedrukreactor) houdt gras en "
    "zwamvlok tien speldagen van de grond. Elke spuit heeft 16 keer gebruik en werkt 4 blokken in het rond.",
    "# Plastics",
    "Styreen en polystyreen: styreen in de hogedrukreactor. Acht polystyreen rond een kolf methaan maken schuimisolatie; "
    "vier polystyreen en methaan maken vier isolatieblokken, en ijs of sneeuw dat isolatie raakt smelt nooit.",
    "# Springstoffen",
    "Kneedbare springstof: formaldehyde en ammoniak maken hexamine, en koud genitreerde hexamine maakt RDX. Twee RDX, een "
    "koperstaaf, een staalstaaf en draad maken een holle lading: plaats hem tegen een muur en ontsteek hem, en hij boort "
    "een tunnel van 1x1 en 16 blokken diep door alles behalve bodemsteen. ANFO: zes ammoniumnitraat, diesel, papier en "
    "draad maken een ANFO-lading die een kubus gesteente van 9x9x9 breekt; drie op de vier blokken vallen.",
    "# Stralingsmaatregelen",
    "Kaliumjodide (natriumjodide uit zeewier met kalizout in de kristallisator) verlaagt de dosis van "
    "splijtingsproducten een speldag lang met 40%. Een Pruisisch-blauwcapsule (Pruisisch blauw en eiwitmeel) verlaagt "
    "haar met een kwart. Amifostine (niveau 7) halveert een uur lang de schade van alle straling.",
    "# Biotechnologie",
    "Recombinant DNA (niveau 7) ontgrendelt de bioreactor (hij heeft een geïntegreerde schakeling nodig). Hij vergist "
    "cellulose uit stammen, bladeren of stro tot ethanol, glucose tot PLA, en eiwit met plantaardige olie tot het "
    "mRNA-vaccin, zeven speldagen zonder Infectie. Zijn biomassa is meststof. PLA maakt bloempotten en voorwerplijsten, "
    "en composteert.",
    "Glyfosaat: formaldehyde met fosforzuur in de hogedrukreactor. Veld vrij doodt elke plant rond het blok, ook "
    "gewassen, behalve tarwe uit tolerante zaden (acht tarwezaden rond een glyfosaat). Tolerante tarwe groeit ook een "
    "tiende sneller.",
]

MINING = [
    "# Voorhamers en graafschoppen",
    "Steenhouwershamers (niveau 2) leert de voorhamer (vijf staven rond een stok, boven een stok) en de graafschop (drie "
    "staven en twee stokken), van staal en elk later gereedschapsmetaal. Elk breekt een vierkant van 3x3 over het vlak "
    "dat je raakt, op drie tiende van de snelheid, en slijt één punt per blok. Sluip om één blok te delven. Alleen "
    "blokken waarvoor het gereedschap geschikt is en die niet harder zijn dan het blok waarop je mikte breken; niets met "
    "een inventaris en geen machine breekt ooit; wat eruit valt gaat direct naar je inventaris.",
    "# Opblazen",
    "Gesteente opblazen (niveau 2): vier zwart buskruit, papier en draad maken twee springladingen. Ontstoken met "
    "vuursteen en staal of redstone breekt er één een blok steen van 3x3x3 en valt alles; aarde, hout en machines blijven "
    "gespaard.",
    "# Boren en boorkoppen",
    "@4 Elke boor heeft een boorkop nodig: houd de boorkop in je andere hand, sluip en gebruik de boor. De boorkop bepaalt "
    "wat hij kan delven en hoe snel (wolfraamstaal 8,5, snelstaal 10, hardmetaal 12, diamant 13) en slijt één punt per "
    "slag. Betover de boorkop, niet de boor; een gemonteerde boorkop leent de boor zijn betoveringen, alle behalve "
    "Herstel. Sluip en gebruik zonder boorkop in je andere hand om van modus te wisselen. Een boor betaalt 1,5 kJ per "
    "punt hardheid van elk blok: 2,25 voor steen, 4,5 voor wrevelsteen.",
    "Rotsboor (niveau 4): de luchtcompressor, een elektrische machine, vult stalen gasflessen met perslucht, zonder "
    "compressor-upgrade. De pneumatische rotsboor verbruikt één gasfles per 600 kJ (ongeveer 400 steen) en geeft de lege "
    "terug; hij delft één blok, een tunnel van 1x3 of 3x3, en vertraagt je zolang je hem vasthoudt.",
    "# Accugereedschap",
    "Loodaccu (niveau 4): het accupak bevat 1500 kJ. Sluit een laadbank aan op het koperdraad en gebruik het pak erop; "
    "het laadt met tot 5 kJ per tick.",
    "De boorhamer (niveau 5) is de rotsboor op een accupak, zonder vertraging. De kettingzaag (niveau 5) velt een hele "
    "boom, tot 128 stammen, voor 3 kJ per stam.",
    "De zware mijnboor (niveau 6) voegt een 5x5-modus toe en een adermodus die één erts tot 32 blokken volgt; op een "
    "loodaccupak vertraagt hij je.",
    "Het lithium-ionaccupak (niveau 7) bevat 6000 kJ, wordt als eerste gebruikt en heft die vertraging op. Ertszoekende "
    "boor (niveau 7): gebruik in adermodus de zware mijnboor op een erts om de grootte en de elementen van de ader te "
    "lezen voordat je graaft.",
]

ISOTOPES = [
    "# Straling",
    "Radioactieve voorwerpen geven een dosis af die zich opbouwt zolang je ze draagt of er dichtbij staat. Een "
    "elektroscoop, en later een Geigerteller, meet haar. Loden muren, de hete cel en de kernreactor schermen af wat "
    "erin zit. Sommige monsters vervallen op de spelklok (één speldag is voor hen een echte dag) en veranderen in je "
    "inventaris in hun dochters.",
    "# Blauwe gloed",
    "De sterkste stralers gloeien blauw in het donker, zoals cherenkovlicht: radium en sterker in lucht, en in water "
    "ook polonium, americium, actinium en pekblende, feller. Een draaiende kernreactor verlicht zijn bassin, en "
    "draaiende versnellers gloeien ook. De gloed volgt hoe geconcentreerd de activiteit is, niet hoeveel je hebt; uraan "
    "gloeit nooit. Gloeiende voorwerpen gloeien ook in hun inventarisvak.",
    "# Natuurlijke isotopen",
    "De tooltip van elk stabiel element toont zijn natuurlijke isotopen, de grootste eerst.",
    "# Verhoudingen die veranderen",
    "Voorwerpen van uraan (U-235), plutonium (Pu-240), lithium (Li-6), boor (B-10), calcium (Ca-48) en waterstof (H-2) "
    "tonen hun eigen verhouding. Een voorwerp met de natuurlijke verhouding stapelt met elk ander; een verrijkt of "
    "verarmd voorwerp stapelt alleen met voorwerpen van dezelfde verhouding. Machines mengen verhoudingen naar massa.",
    "- De calutron (Calutronverrijking) splitst uraan, lithium of boor in een kleine verrijkte fractie en verarmd "
    "restant. Voer de fractie opnieuw in voor meer.",
    "- Uraanmetaal met 3% U-235 of meer wordt gesinterd tot splijtstofpellets voor de reactor.",
    "- Plutonium dat in de reactor gekweekt wordt, is van wapenkwaliteit (6% Pu-240); plutonium uit gebruikte splijtstof "
    "is van reactorkwaliteit.",
    "- Lithium met meer dan 90% Li-6 in de vakken van een reactor wordt tritium, dat tritiumlichten voor altijd laat "
    "branden.",
    "- Boor met meer dan 90% B-10 maakt regelstaven: met één in zijn vakken verbrandt een reactor geen brandstof zolang "
    "hij vol is.",
    "- Zwaar water uit de verrijkingscascade (voer haar ijs of sneeuw) laat splijtstofstaven van natuurlijk uraan "
    "branden.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Eerste stappen",
    "group": "Niveau {n}",
    "research": "Onderzoek",
    "machines": "Machines",
    "reactions": "Reacties",
    "recipe": " Recept: {r}.",
    "recipe_head": "Recept: {r}.",
    "projects": "# Projecten (★ = sleutel)",
    "needs": " Vereist {x}.",
    "samples": " Monsters: {x}.",
    "unlocks": " Ontgrendelt {x}.",
    "up_to": "{desc}. Tot {c} °C",
    "electric": ", elektrisch.",
    "fuel": ", op brandstof.",
    "amounts": "Hoeveelheden per 100 eenheden lading.",
    "ores": "Ertsen",
    "ores_head": "# Ertsen",
    "ores_intro": "Ertsen verschijnen in nieuw verkende chunks. Zijden streling en Geluk werken zoals op gewone ertsen.",
    "ore_line": "{name}: {desc}. Geeft {lo}-{hi} {drop}; vereist een {tier} houweel. ",
    "found": "Te vinden {w}.",
    "where": "van y {lo} tot {hi} {b}",
    "biomes": "in: {b}",
    "overworld": "in de Bovenwereld",
    "nether": "in de Nether",
    "power": "Energie",
    "power_head": "# Energie",
    "power_intro": "Elektrische machines halen stroom uit energieblokken ernaast, of verbonden door koperdraad (tot 64 "
                   "blokken draad) of geïsoleerde kabel (vier keer zo ver).",
    "power_line": "{name} (niveau {lvl}, {project}): {desc}.",
    "isotopes": "Straling en isotopen",
    "containers": "Houders",
    "upgrades": "Upgrades",
    "farm": "Boerderij en vet",
    "tar": "Teer en geneesmiddelen",
    "industry": "Industriële chemie",
    "life": "Levenswetenschappen",
    "mining": "Mijnbouw",
    "crafting": "Vervaardigen",
    "craft_head": "# Vervaardigen",
    "craft_intro": "De vormloze recepten die de mod toevoegt (recepten voor machines en blokken staan in de hoofdstukken "
                   "per niveau).",
    "shaped_head": "# Recepten met vorm (rijen van boven naar beneden)",
    "or": " of ",
    "any": "elk(e) {x}",
    "empty": "leeg",
    "reagent": "(reagens)",
    "carbon": " + {c} koolstof",
    "nothing": "er wordt niets bewaard",
    "slag": "; slak: ",
    "vents": "; stoot uit: ",
    "temp": ". Van {a} tot {b} °C.",
    "sep": "; ",
}

WORDS = {
    # pickaxe tiers (used as "een {tier} houweel")
    "wood": "houten", "stone": "stenen", "iron": "ijzeren",
    # biomes and biome tags
    "mountain": "bergen", "jungle": "oerwouden", "savanna": "savannes", "desert": "woestijn", "badlands": "badlands",
    "ocean": "oceanen", "swamp": "moerassen", "mangrove swamp": "mangrovemoerassen", "basalt deltas": "basaltdelta's",
    "beach": "stranden", "river": "rivieren", "dripstone caves": "druipsteengrotten",
    # item tags
    "logs": "stam", "logs that burn": "brandbare stam", "oak logs": "eikenstam", "planks": "planken",
    "wool": "wol", "beds": "bed", "cable insulation": "kabelisolatie", "soap": "zeep",
    # upgrades unlocked by research
    "forced air": "geforceerde lucht (blaasbalg)", "steam blast": "stoomblazen",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["nl_nl"]
T["atlas"] = _ATLAS_TITLE["nl_nl"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["nl_nl"]
T.update(_TREES_T["nl_nl"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("nl_nl")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["nl_nl"]
T["compendium"] = _COMP_TITLE["nl_nl"]

# 3.5.0 Plant Compendium (tools/compendium_plants_nl_nl.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["nl_nl"]
T["plant_compendium"] = _PCOMP_TITLE["nl_nl"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_nl_nl.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["nl_nl"]
T["mineral_compendium"] = _MCOMP_TITLE["nl_nl"]
