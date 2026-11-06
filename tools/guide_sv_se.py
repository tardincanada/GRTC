"""Swedish (sv_se) text of the Field Guide (2.5.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the sv_se lang file.
"""

DECIMAL = ","

INTRO = [
    "# Välkommen",
    "Varje block och föremål i den här världen består av verkliga grundämnen. Den här handboken följer modens "
    "teknikträd från stenåldern till de sista grundämnena, ett kapitel per nivå.",
    "# Enheter",
    "1 enhet är 1 gram. Ett block är 1000 enheter, en tacka eller ett grundämnes- eller ämnesföremål 111,1, en klump "
    "12,35 och en hink 1000. Håll muspekaren över ett block eller föremål för att se vad det består av; håll ned "
    "Skift för att se allt.",
    "# Forskning",
    "Varje nivå har sin egen forskningsstation. Lägg ett projekts prover i dess 9 platser, välj projektet och tryck på "
    "Forska. Nyckelprojekt (märkta med en stjärna) höjer din teknivå; du behöver en nivås nyckelprojekt innan du kan "
    "ställa upp nästa station. /research visar vad du kan.",
    "Avhandlingar som skrivs vid en station lär en annan spelare allt du kan.",
    "# Maskiner",
    "En maskin delar upp varje föremål som kommer in efter dess sammansättning och utför varje reaktion du har "
    "forskat fram, så länge den är tillräckligt varm och inte för varm. Produkter kommer ut som föremål, avfall som "
    "slagg, och vissa gaser släpps ut. Vätskor och gaser fyller tomma glaskolvar från maskinens sidopanel. Vissa "
    "reaktioner behöver en andra ingrediens på reagensplatsen.",
    "Bränsleeldade maskiner bränner det som ligger på bränsleplatsen: trä når 650 °C, träkol och kol 1100 °C, "
    "petroleumbränslen 1200 °C och gas 1300 °C. Bälgen (forcerad luft) ger 250 °C till. Träkol och kol fungerar också "
    "som kolet som reducerar malmer. Elektriska maskiner tar ström från en Voltas stapel, en dynamo eller ett annat "
    "kraftblock som står bredvid eller är anslutet med koppartråd.",
    "Vanliga ugnar smälter inte längre järn-, koppar- eller guldmalm: metallerna kommer ur maskinerna.",
    "# Nivå 0",
    "Helt utan forskning kan du bygga en kolmila (8 jordblock runt en stam) och förkola stammar till träkol.",
]

CONTAINERS = [
    "# Glaskolvar",
    "Varje flytande eller gasformigt föremål är en förseglad glaskolv med 111,1 enheter. Fem glas ger fyra kolvar, "
    "utan forskning. En maskin fyller en tom kolv från den översta platsen i sin sidopanel för varje vätska och gas "
    "den bildar; utan kolvar stannar den och ber om fler. När en maskin, ett recept, en ugn eller ett "
    "forskningsprojekt förbrukar innehållet i en kolv får du tillbaka den tomma kolven.",
    "# Stålflaskor och kompressor",
    "Gaskomprimering (nivå 4) låser upp stålflaskor och kompressorn. En flaska rymmer 1000 enheter komprimerad gas: "
    "lika mycket som nio kolvar i ett enda föremål. Sätt en kompressor i en av maskinens två uppgraderingsplatser och "
    "lägg tomma flaskor på den andra platsen i dess sidopanel: då fyller maskinen varje gas den bildar på flaskor, en "
    "flaska i taget. Vätskor går fortfarande i kolvar. En flaska bränngas brinner som nio kolvar.",
    "Maskiner tar emot flaskor som vilket annat föremål som helst och lämnar tillbaka den tomma flaskan.",
    "# Lagringstank",
    "Pneumatisk kemi (nivå 3) låser upp lagringstanken. Den rymmer 16 000 enheter (144 kolvar) av en enda vätska "
    "eller gas. Använd den med en full kolv eller flaska för att tömma i den, med en tom kolv för att ta ut en, och "
    "med tom hand för att läsa av nivån. Trattar fungerar också: mata fulla eller tomma behållare uppifrån eller från "
    "sidan och ta ut det som kommer ut underifrån. En redstonejämförare läser av hur full den är. Om du bryter den "
    "rinner innehållet ut.",
]

UPGRADES = [
    "# Uppgraderingsplatser",
    "Varje maskin har två uppgraderingsplatser längst ned i sin sidopanel. En uppgradering verkar så snart dess ägare "
    "har forskat fram den; två likadana räknas bara en gång. Uppgraderingar märkta ”bränsleeldad” gör ingenting i "
    "elektriska maskiner.",
    "- Kompressor (Gaskomprimering, nivå 4): fyller de gaser maskinen bildar på stålflaskor.",
    "- Utmatare (Automatisk kvarn, nivå 3): flyttar maskinens produkter till en kista, tunna eller tratt bredvid. Den "
    "matar aldrig en annan maskin direkt.",
    "- Regenerator (Regenerativ ugn, nivå 4): en bränsleeldad maskin förlorar hälften så mycket värme, så den bränner "
    "mindre bränsle och värms upp snabbare.",
    "- Eldfast infodring (Basiska eldfasta material, nivå 4): en bränsleeldad maskin kan gå 150 °C över sin egen "
    "gräns om bränslet och bälgen räcker så långt.",
    "- Gasåtervinningshuv (Återvinning av biprodukter, nivå 4): vätskor och gaser som maskinen skulle släppa ut "
    "(koldioxid, svaveldioxid, saltsyra och andra) går i kolvar eller flaskor. Ånga och blandgaser som lysgas släpps "
    "fortfarande ut. Infångad gas stoppar aldrig maskinen: utan behållare eller plats släpps den ut ändå.",
    "- Mekanisk omrörare (Elmotor, nivå 4): bearbetar chargen 50 % snabbare. Motorn bränner en fjärdedel mer bränsle "
    "medan chargen reagerar, eller drar extra ström i en elektrisk maskin.",
    "- Katalysatorbädd (Katalysatorbäddar, nivå 5): reaktioner på en fast katalysator (järn, nickel, platina, "
    "platina-rodium, vanadinpentoxid, kobolt-manganbromid) går dubbelt så fort: ammoniak, kontaktmetoden, "
    "salpetersyra, kolhydrering, reformering, nylon med mera.",
    "- Transportband (Bunagummi, nivå 5): en utmatare på ett gummiband; flyttar färdiga produkter fyra i taget i "
    "stället för en.",
    "- Skumisolering (Styren och polystyren, nivå 6): en maskin vars gräns ligger på 300 °C eller lägre förlorar 70 % "
    "mindre värme.",
    "- Sterilluftfilter (Penicillin, nivå 6): jäsfat, vinägerkar, djuptanksfermentor och bioreaktor ger 10 % mer. "
    "Djuptanksfermentorn går inte utan det.",
]

FARM = [
    "# Fetter och tvål",
    "Fettsmältning (nivå 1) låser upp såpkitteln. In går kött, ben, ägg och mjölk; kitteln sjuder dem mellan 80 och "
    "150 °C. Talgen flyter upp, och resten av köttet och benen kommer ut som proteinmjöl, som kor, får, getter, grisar "
    "och höns äter och som ger bra kompost. Talg med tråd ger talgljus: ljus 12, sotande, och de brinner ned tre dagar "
    "efter att de ställts ut.",
    "Såpkokning: lägg talg eller vegetabilisk olja i kitteln och träaska på reagensplatsen för att få såpa, med "
    "glycerol i kolvar. Tvål (nivå 2) använder natriumkarbonat i stället, eller gör såpa hård med salt.",
    "Använd en bit tvål för att tvätta dig: du är Ren i tio minuter, och Ren tar bort hungern från ruttet kött och "
    "rå kyckling. Med en färgad läderrustning i andra handen tvättar tvålen ur färgen. Tvål med ull eller en säng i "
    "tillverkningsrutnätet tvättar den vit.",
    "# Olja, läder och mäsk",
    "Oljepressning (nivå 1): oljepressen kallpressar pumpa-, melon- och vetefrön, kakaobönor och solrosor. Vegetabilisk "
    "olja fyller kolvar; oljekakan som blir kvar är djurfoder. Talg och vegetabilisk olja brinner också som bränsle.",
    "Gropgarvning (nivå 1): garvgropen gör kött och hudar till läder utan tillverkningsrutnät, långsamt: fyra bitar "
    "ruttet kött ger ett läder på ungefär tre speldagar. Lägg ek- eller granstammar på reagensplatsen för deras "
    "garvämne; den urlakade barken som blir kvar brinner som bränsle. Alunframställning (nivå 2) ger kalialun, och två "
    "kaninskinn med en alun garvas till läder i tillverkningsrutnätet.",
    "Mäskning (nivå 1): jäsfatet gör stärkelsen i vete och potatis till socker och sedan till alkohol. "
    "Stärkelsesocker (nivå 3) påskyndar det första steget med lite svavelsyra i uppslutningskaret. Söt vitriololja "
    "(nivå 2): etanol destillerad med svavelsyra i alembiken ger dietyleter.",
    "# Gödsel",
    "Superfosfat (nivå 3) löser ben eller råfosfat i svavelsyra; Gasvatten (nivå 3) tvättar ammoniaken ur koksugnen "
    "(lägg kolvar på dess kolvplats), och blykammaren gör den till ammoniumsulfat. Använd någon av de två gödslen på "
    "åkerjord: under de åtta följande skördarna växer grödan en och en halv gång så fort, och varje mogen skörd ger en "
    "till.",
    "# Gummi",
    "Gummiträd växer vilt i djungeln (djungel, gles djungel och bambudjungel). Deras löv fäller plantor, och plantan "
    "växer var som helst, men ett gummiträd ger bara latex i djungeln. Vulkanisering (nivå 3) låser upp tapprören: "
    "fäst det på sidan av en gummiträdsstam och använd det med en tom glaskolv; det fyller en kolv var femte minut och "
    "håller fyra redo. Trattar kan ge det tomma kolvar uppifrån och ta ut latexen underifrån. Utanför djungeln säger "
    "det att klimatet inte passar.",
    "I vulkaniseringspannan blir latex med svavel på reagensplatsen vulkaniserat gummi mellan 130 och 160 °C; utan "
    "svavel torkar den bara till rågummi. Vulkaniserat gummi ger gymnastikskor (dina steg skapar inga vibrationer, "
    "och magma och sköldpaddsägg märker dig inte) och gummihjul: använd dem på en gruvvagn så går den 25 % fortare.",
]

TAR = [
    "# Stenkolstjära",
    "Tjärdestillation (nivå 3): när den är framforskad fångar koksugnen kolets tjära i kolvar (lägg tomma kolvar på "
    "dess kolvplats), ungefär en kolv per 13 kol. Tjärdestillatorn delar stenkolstjära i bensen, toluen, fenol "
    "(karbolsyra) och antracen och lämnar beck kvar. Beck med grus och sand ger tjärmakadam, ett vägblock lika "
    "snabbt som asfalt. Bensen, fenol, tjära och beck brinner som bränsle. Nitrobensen (nivå 3) nitrerar bensen i "
    "uppslutningskaret, och Anilin (nivå 4) reducerar det med järn.",
    "# Färgämnen",
    "Berlinerblått (nivå 2) låser upp färgkaret: ruttet kött eller kött med järnvitriol på reagensplatsen ger "
    "pigmentet, och karet gör det till blått färgämne, 8 per föremål. Tjärfärgerna ger 32 vanliga färgämnen per kolv: "
    "fuchsin (anilin och vit arsenik, magenta), pikrinsyra (fenol och salpetersyra i uppslutningskaret, gul) och "
    "alizarin (antracen och kaustik soda i autoklaven, röd). Lägg färgen i färgkaret för att få vanliga färgämnen.",
    "# Infektion och medicin",
    "En träff från en zombie, uttorkad zombie, drunknad zombie eller zombiebybo infekterar dig 1 gång av 200, att äta "
    "ruttet kött 1 gång av 100; att vara Ren halverar båda. En infektion varar en speldag med svaghet och hunger. "
    "Obehandlad läker den av sig själv 7 gånger av 10; resten blir sepsis, 1 skada var tionde sekund tills du dör. "
    "Karbolsåpa (Antiseptisk kirurgi: fenol och tvål) och aspirin (nivå 4: fenol i autoklaven med natriumkarbonat, "
    "sedan ättiksyra i uppslutningskaret) botar en infektion innan den blir värre. Bara sulfapreparat (nivå 5) och "
    "antibiotikan på nivå 6 botar sepsis. Mjölk botar ingetdera. Spelregeln periodicElementsInfection stänger av "
    "infektion.",
    "Aspirin tar också bort illamående och trötthet och ger 10 sekunders återhämtning. Eterbedövning (eter på ull), "
    "använd på en zombiebybo vars botande redan har börjat, halverar tiden det tar.",
    "# Sprängämnen",
    "Dynamitpatron (nivå 4): glycerol från såpkokningen, nitrerad kallt (5 till 25 °C) i uppslutningskaret, ger "
    "nitroglycerin; med sand och papper ger den dynamitpatroner, och fyra patroner med tråd ger en gruvladdning. Tänd "
    "den med tändstål eller redstone: efter 4 sekunder bryter den varje block i en kub på 5x5x5 som är mindre "
    "motståndskraftigt än obsidian, och allt faller. Med mobGriefing avstängt skadar den bara det som är i närheten. "
    "Kordit (nivå 4) knådar nitroglycerin med röksvagt krut; korditpatroner skjuter 25 % starkare ur "
    "bakladdningsgeväret och utan rök.",
    "# Grödor och skadedjur",
    "Bordeauxvätska (kopparvitriol, bränd kalk och vatten), sprutad på en gröda eller en sötbärsbuske, hindrar att "
    "den spolas bort eller vissnar i mörkret, och bärbuskar slutar riva. Insektspulver: oljepressen maler prästkragar "
    "till pyretrum, och insektssprayen skadar spindlar, silverfiskar, endermites och bin upp till 4 block framåt.",
    "# Vakuumpump",
    "Vakuumdestillation (nivå 4) låser upp uppgraderingen vakuumpump: i en alembik, tjärdestillator, såpkittel, "
    "kristallisator, saltpanna eller raffinaderidestillator går varje reaktion 100 °C svalare, så maskinen värms upp "
    "snabbare och förlorar mindre värme.",
]

INDUSTRY = [
    "# Karbid och acetylen",
    "Kalciumkarbid (nivå 5): bränd kalk med koks på ljusbågsugnens bränsleplats smälter till karbid vid 1800 °C eller "
    "mer. I uppslutningskaret med vatten på reagensplatsen ger karbid acetylen (lägg kolvar, eller flaskor och en "
    "kompressor, i karet) och lämnar släckt kalk kvar.",
    "Karbidlampan (mässing, glas, karbid och en vattenhink) ger ljus 14, hållen i vilken hand som helst eller buren på "
    "huvudet, i en halvtimme. Använd den med kalciumkarbid i förrådet för att fylla på den; en tom lampa går inte "
    "sönder.",
    "Gassvetsning: tillverka en svetsbrännare av en acetylenflaska och en syreflaska, en ståltacka och en koppartacka. "
    "Håll ett skadat metallverktyg eller rustningsdel i andra handen och använd brännaren: en tacka av samma metall ur "
    "ditt förråd lagar en fjärdedel, utan erfarenhet. Brännaren klarar 64 svetsningar; tillverka den på nytt med nya "
    "flaskor för att fylla på den.",
    "Kalkkväve: karbid i kalkugnen mellan 1000 och 1150 °C med kväve på reagensplatsen tar upp kvävet som "
    "kalciumcyanamid, ett gödselmedel.",
    "# Gödsel ur luften",
    "Ureasyntes: ammoniak i högtrycksreaktorn mellan 170 och 200 °C, med koldioxid på reagensplatsen (fånga den med en "
    "gasåtervinningshuv på en kalkugn eller ett jäsfat). Prillad gödsel låser upp prilltornet, en elektrisk maskin: "
    "ammoniak med salpetersyra blir ammoniumnitrat, och urea med PK-blandning (superfosfat och pottaska) blir "
    "NPK-priller.",
    "Kalkkväve, urea och ammoniumnitrat verkar på åkerjord som superfosfat. NPK-priller får grödan att växa dubbelt så "
    "fort i åtta skördar, var och en med en till.",
    "# Plaster och gummi",
    "Formaldehyd: metanol över silver i Ostwaldkonvertern mellan 600 och 700 °C. Bakelit: fenol med formaldehyd på "
    "reagensplatsen, i autoklaven mellan 150 och 200 °C. Fyra bakelit ger fyra bakelitblock, bruna, eller med ett "
    "färgämne svarta, röda, gröna, elfenbensfärgade eller bärnstensfärgade. Bakelit fungerar också som hölje till "
    "geigermätaren i stället för mässing.",
    "Neopren: acetylen med saltsyra i högtrycksreaktorn. En hel våtdräkt av neopren ger delfingåva i vattnet, och "
    "ingen av dess delar släpper igenom pudersnöns kyla.",
    "Bunagummi, utan djungel: etanol över katalysatorn i krackningsanläggningen mellan 380 och 450 °C ger butadien; "
    "bensen med eten på reagensplatsen mellan 550 och 600 °C ger styren; butadien med styren i uppslutningskaret "
    "mellan 40 och 60 °C ger bunagummi. Det används till gummihjul och uppgraderingen transportband.",
    "# Sprängämnen",
    "TNT: toluen från tjärdestillatorn, nitrerat i uppslutningskaret. En TNT med tre papper ger tre block vanlig "
    "dynamit. Fyra TNT, en ståltacka och tråd ger en rivningsladdning: tänd som en gruvladdning bryter den en kub på "
    "3x3x3 av allt utom berggrund, obsidian inräknat, och allt faller.",
    "# Jäsning, bränsle och medicin",
    "Acetonjäsning: vete eller potatis i jäsfatet, med kalcit på reagensplatsen, jäser till aceton, butanol och lite "
    "etanol i stället för socker. Biodiesel: talg eller vegetabilisk olja i såpkitteln mellan 40 och 150 °C med "
    "metanol på reagensplatsen. Biodiesel och butanol driver dieselgeneratorn och brinner i ugnar.",
    "Sulfapreparat: anilin med ammoniumsulfat i uppslutningskaret ger sulfanilamid. Använd det för att bota en "
    "infektion i vilket skede som helst, sepsis inräknat.",
]

LIFE = [
    "# Antibiotika",
    "Penicillin (nivå 6) låser upp djuptanksfermentorn, en elektrisk maskin som bara går med ett sterilluftfilter. "
    "Glukos med mjölk på reagensplatsen (för dess laktos) odlar penicillin mellan 20 och 37 °C; streptomycin behöver "
    "proteinmjöl på reagensplatsen. Båda antibiotikan botar en infektion eller sepsis och gör dig immun mot infektion "
    "i 20 minuter. Använd penicillin på en zombiebybo med svaghet så börjar botandet, utan gyllene äpple. Det "
    "förbrukade mycelet från tanken är gödsel.",
    "# Bekämpningsmedel",
    "DDT: bensen med klor i uppslutningskaret. En DDT-pudrare som används på marken gör den dödlig för spindlar, "
    "silverfiskar, endermites och bin i tre speldagar; höns på den värper bara hälften så mycket. Ogräsmedlet (2,4-D, "
    "av fenol och klor) dödar blommor, ormbunkar, rankor, buskar, plantor och löv runt blocket men skonar gräs och "
    "grödor. Förgroningsmedlet (atrazin, av ammoniak och klor med koks i högtrycksreaktorn) hindrar gräs och mycel "
    "från att sprida sig på marken i tio speldagar. Varje spruta har 16 användningar och verkar 4 block runt om.",
    "# Plaster",
    "Styren och polystyren: styren i högtrycksreaktorn. Åtta polystyren runt en kolv metan ger skumisolering; fyra "
    "polystyren och metan ger fyra isoleringsblock, och is eller snö som rör vid isoleringen smälter aldrig.",
    "# Sprängämnen",
    "Plastsprängämne: formaldehyd och ammoniak ger hexamin, och hexamin nitrerat kallt ger RDX. Två RDX, en "
    "koppartacka, en ståltacka och tråd ger en riktad laddning: sätt den mot en vägg och tänd den, så öppnar den en "
    "tunnel på 1x1, 16 block djup, genom allt utom berggrund. ANFO: sex ammoniumnitrat, diesel, papper och tråd ger "
    "en ANFO-laddning, som bryter en kub på 9x9x9 av berg; tre av fyra block faller.",
    "# Strålskyddsåtgärder",
    "Kaliumjodid (natriumjodid ur tång med kaliumsalt i kristallisatorn) sänker dosen från fissionsprodukter med 40 % "
    "i en speldag. En berlinerblåttkapsel (berlinerblått och proteinmjöl) sänker den med en fjärdedel. Amifostin "
    "(nivå 7) halverar skadan från all strålning i en timme.",
    "# Bioteknik",
    "Rekombinant DNA (nivå 7) låser upp bioreaktorn (den behöver en integrerad krets). Den jäser cellulosan i stammar, "
    "löv eller halm till etanol, glukos till PLA och protein med vegetabilisk olja till mRNA-vaccinet, sju speldagar "
    "utan infektion. Dess biomassa är gödsel. PLA ger blomkrukor och föremålsramar och duger som kompost.",
    "Glyfosat: formaldehyd med fosforsyra i högtrycksreaktorn. Fältrensaren dödar alla växter runt blocket, grödor "
    "inräknade, utom vete från toleranta frön (åtta vetefrön runt en glyfosat). Tolerant vete växer också en tiondel "
    "fortare.",
]

MINING = [
    "# Släggor och skyfflar",
    "Stenbrottsslägga (nivå 2) lär ut släggan (fem tackor runt en pinne, ovanför en pinne) och skyffeln (tre tackor "
    "och två pinnar), av stål och varje senare verktygsmetall. Var och en bryter en 3x3-kvadrat på den sida du "
    "träffar, med tre tiondelar av hastigheten, och slits en poäng per block. Smyg för att bryta ett enda block. Bara "
    "block som passar verktyget och inte är hårdare än det du siktar på bryts; inget med förråd och ingen maskin bryts "
    "någonsin; det som faller går direkt till ditt förråd.",
    "# Sprängning",
    "Bergsprängning (nivå 2): fyra svartkrut, papper och tråd ger två sprängladdningar. Tänd med tändstål eller "
    "redstone bryter en laddning ett 3x3x3-block av sten och låter allt falla; jord, trä och maskiner skonas.",
    "# Borrar och borrkronor",
    "@4 Varje borr behöver en borrkrona: håll borrkronan i andra handen, smyg och använd borren. Borrkronan avgör vad "
    "den kan bryta och hur fort (volframstål 8,5, snabbstål 10, hårdmetall 12, diamant 13), och slits en poäng per "
    "slag. Förtrolla borrkronan, inte borren; en isatt borrkrona lånar ut sina förtrollningar till borren, alla utom "
    "lagning. Smyg och använd den utan borrkrona i andra handen för att byta läge. En borr förbrukar 1,5 kJ per "
    "hårdhetspoäng hos varje block: 2,25 för sten, 4,5 för djupskiffer.",
    "Bergborr (nivå 4): luftkompressorn, en elektrisk maskin, fyller stålflaskor med tryckluft utan att behöva "
    "uppgraderingen kompressor. Tryckluftsbergborren förbrukar en flaska per 600 kJ (omkring 400 sten) och lämnar "
    "tillbaka den tomma; den bryter ett block, en 1x3- eller en 3x3-tunnel och saktar ned dig medan du håller den.",
    "# Batteriverktyg",
    "Blyackumulator (nivå 4): batteripaketet rymmer 1500 kJ. Anslut en laddbänk till koppartråden och använd "
    "batteriet på den; det laddas med upp till 5 kJ per tick.",
    "Borrhammaren (nivå 5) är bergborren med batteri, utan att sakta ned dig. Motorsågen (nivå 5) fäller ett helt "
    "träd, upp till 128 stammar, för 3 kJ per stam.",
    "Den tunga gruvborren (nivå 6) har dessutom ett 5x5-läge och ett ådraläge som följer en malm upp till 32 block; "
    "med blyackumulator saktar den ned dig.",
    "Litiumjonbatteriet (nivå 7) rymmer 6000 kJ, förbrukas först och tar bort den inbromsningen. Malmsökande borr "
    "(nivå 7): i ådraläget använder du den tunga gruvborren på en malm för att läsa av ådrans storlek och grundämnen "
    "innan du gräver.",
]

ISOTOPES = [
    "# Strålning",
    "Radioaktiva föremål ger en dos som byggs upp medan du bär dem eller är nära dem. Ett elektroskop, senare en "
    "geigermätare, mäter den. Blyväggar, den heta cellen och kärnreaktorn skärmar av det de innehåller. Vissa prover "
    "sönderfaller efter spelklockan (en speldag är en verklig dag för dem) och blir till sina döttrar i ditt förråd.",
    "# Blått sken",
    "De starkaste strålarna lyser blått i mörkret, som tjerenkovljus: radium och de starkaste i luften, och under "
    "vatten även polonium, americium, aktinium och uranbeck, starkare. En kärnreaktor i drift lyser upp sin bassäng, "
    "och acceleratorer i drift lyser också. Skenet beror på hur koncentrerad aktiviteten är, inte på hur mycket du "
    "har; uran lyser aldrig. Lysande föremål lyser också på sin plats i förrådet.",
    "# Naturliga isotoper",
    "Beskrivningen av varje stabilt grundämne listar dess naturliga isotoper, från den vanligaste till den "
    "sällsyntaste.",
    "# Varierande förhållanden",
    "Föremål av uran (U-235), plutonium (Pu-240), litium (Li-6), bor (B-10), kalcium (Ca-48) och väte (H-2) visar sitt "
    "eget förhållande. Ett föremål med det naturliga förhållandet staplas med vilket annat som helst; ett anrikat "
    "eller utarmat staplas bara med föremål med samma förhållande. Maskiner blandar förhållandena efter massa.",
    "- Kalutronen (Kalutronanrikning) delar uran, litium eller bor i en liten anrikad andel och en utarmad "
    "restprodukt. Mata in andelen igen för att anrika vidare.",
    "- Uranmetall med 3 % U-235 eller mer sintras till bränslekutsar för reaktorn.",
    "- Plutonium som bildas i reaktorn är av vapenkvalitet (6 % Pu-240); plutonium ur använt bränsle är av "
    "reaktorkvalitet.",
    "- Litium med över 90 % Li-6 i en reaktors platser blir tritium, som får tritiumljus att lysa för alltid.",
    "- Bor med över 90 % B-10 ger styrstavar: med en i sina platser bränner en reaktor inget bränsle så länge den är "
    "full.",
    "- Tungt vatten från anrikningskaskaden (mata den med is eller snö) gör det möjligt att bränna bränslestavar av "
    "naturligt uran.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Första stegen",
    "group": "Nivå {n}",
    "research": "Forskning",
    "machines": "Maskiner",
    "reactions": "Reaktioner",
    "recipe": " Recept: {r}.",
    "recipe_head": "Recept: {r}.",
    "projects": "# Projekt (★ = nyckel)",
    "needs": " Kräver {x}.",
    "samples": " Prover: {x}.",
    "unlocks": " Låser upp: {x}.",
    "up_to": "{desc}. Upp till {c} °C",
    "electric": ", elektrisk.",
    "fuel": ", bränsleeldad.",
    "amounts": "Mängder per 100 enheter charge.",
    "ores": "Malmer",
    "ores_head": "# Malmer",
    "ores_intro": "Malmer dyker upp i nyutforskade chunkar. Silkesvante och Rikedom fungerar som på vanliga malmer.",
    "ore_line": "{name}: {desc}. Ger {lo}-{hi} {drop}; kräver en hacka av {tier}. ",
    "found": "Förekommer {w}.",
    "where": "från y {lo} till {hi} {b}",
    "biomes": "i: {b}",
    "overworld": "i Övervärlden",
    "nether": "i Nether",
    "power": "Kraft",
    "power_head": "# Kraft",
    "power_intro": "Elektriska maskiner tar ström från kraftblock bredvid dem eller anslutna med koppartråd (upp till 64 "
                   "block tråd) eller isolerad kabel (fyra gånger så långt).",
    "power_line": "{name} (nivå {lvl}, {project}): {desc}.",
    "isotopes": "Strålning och isotoper",
    "containers": "Behållare",
    "upgrades": "Uppgraderingar",
    "farm": "Gård och fetter",
    "tar": "Tjära och läkemedel",
    "industry": "Industrikemi",
    "life": "Biovetenskap",
    "mining": "Gruvdrift",
    "crafting": "Tillverkning",
    "craft_head": "# Tillverkning",
    "craft_intro": "De formlösa recept som moden lägger till (recepten för maskiner och block finns i varje nivås "
                   "kapitel).",
    "shaped_head": "# Formade recept (rader uppifrån och ned)",
    "or": " eller ",
    "any": "valfri {x}",
    "empty": "tom",
    "reagent": "(reagens)",
    "carbon": " + {c} kol",
    "nothing": "inget behålls",
    "slag": "; slagg: ",
    "vents": "; släpper ut: ",
    "temp": ". Från {a} till {b} °C.",
    "sep": "; ",
}

WORDS = {
    # pickaxe tiers
    "wood": "trä", "stone": "sten", "iron": "järn",
    # biomes and biome tags
    "mountain": "berg", "jungle": "djungel", "savanna": "savann", "desert": "öken",
    "badlands": "stenöken", "ocean": "hav", "swamp": "träsk", "mangrove swamp": "mangroveträsk",
    "basalt deltas": "basaltdeltan", "beach": "strand", "river": "å", "dripstone caves": "droppstensgrottor",
    # item tags
    "logs": "stam", "logs that burn": "brännbar stam", "oak logs": "ekstam", "planks": "plankor",
    "wool": "ull", "beds": "säng", "cable insulation": "kabelisolering", "soap": "tvål",
    # upgrades unlocked by research
    "forced air": "forcerad luft (bälg)", "steam blast": "ångblästring",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["sv_se"]
T["atlas"] = _ATLAS_TITLE["sv_se"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["sv_se"]
T.update(_TREES_T["sv_se"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("sv_se")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["sv_se"]
T["compendium"] = _COMP_TITLE["sv_se"]

# 3.5.0 Plant Compendium (tools/compendium_plants_sv_se.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["sv_se"]
T["plant_compendium"] = _PCOMP_TITLE["sv_se"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_sv_se.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["sv_se"]
T["mineral_compendium"] = _MCOMP_TITLE["sv_se"]
