"""Danish (da_dk) text of the Field Guide (2.5.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the da_dk lang file.
"""

DECIMAL = ","

INTRO = [
    "# Velkommen",
    "Hver blok og genstand i denne verden består af virkelige grundstoffer. Denne håndbog følger modets "
    "teknologitræ fra stenalderen til de sidste grundstoffer, ét kapitel pr. niveau.",
    "# Enheder",
    "1 enhed er 1 gram. En blok er 1000 enheder, en barre eller en grundstof- eller stofgenstand 111,1, en klump "
    "12,35 og en spand 1000. Hold musen over en blok eller genstand for at se, hvad den består af; hold Shift nede "
    "for at se det hele.",
    "# Forskning",
    "Hvert niveau har sin egen forskningsstation. Læg et projekts prøver i dens 9 pladser, vælg projektet, og tryk "
    "på Forsk. Nøgleprojekter (markeret med en stjerne) hæver dit teknologiniveau; du skal have et niveaus "
    "nøgleprojekter, før du kan stille den næste station op. /research viser, hvad du kan.",
    "Afhandlinger skrevet ved en station lærer en anden spiller alt, hvad du kan.",
    "# Maskiner",
    "En maskine deler hver genstand, der kommer ind, op efter dens sammensætning og udfører hver reaktion, du har "
    "forsket i, så længe den er varm nok og ikke for varm. Produkter kommer ud som genstande, affald som slagge, og "
    "nogle gasser udledes. Væsker og gasser fylder tomme glaskolber fra maskinens sidepanel. Nogle reaktioner kræver "
    "en anden ingrediens på reagenspladsen.",
    "Brændselsfyrede maskiner brænder det, der ligger på brændselspladsen: træ når 650 °C, trækul og kul 1100 °C, "
    "petroleumsbrændstoffer 1200 °C og gas 1300 °C. Blæsebælgen (blæst luft) giver 250 °C mere. Trækul og kul tjener "
    "også som det kulstof, der reducerer malme. Elektriske maskiner får strøm fra en Voltas søjle, en dynamo eller en "
    "anden kraftblok, der står ved siden af eller er forbundet med kobbertråd.",
    "Almindelige ovne smelter ikke længere jern-, kobber- eller guldmalm: metallerne kommer fra maskinerne.",
    "# Niveau 0",
    "Helt uden forskning kan du bygge en kulmile (8 jordblokke omkring en stamme) og forkulle stammer til trækul.",
]

CONTAINERS = [
    "# Glaskolber",
    "Hver flydende eller gasformig genstand er en forseglet glaskolbe med 111,1 enheder. Fem glas giver fire kolber, "
    "uden forskning. En maskine fylder en tom kolbe fra den øverste plads i sit sidepanel for hver væske og gas, den "
    "danner; uden kolber stopper den og beder om flere. Når en maskine, en opskrift, en ovn eller et "
    "forskningsprojekt bruger indholdet af en kolbe, får du den tomme kolbe tilbage.",
    "# Stålflasker og kompressor",
    "Gaskomprimering (niveau 4) låser op for stålflasker og kompressoren. En flaske rummer 1000 enheder komprimeret "
    "gas: lige så meget som ni kolber i én genstand. Sæt en kompressor i en af en maskines to opgraderingspladser, og "
    "læg tomme flasker på den anden plads i dens sidepanel: så fylder maskinen hver gas, den danner, på flasker, én "
    "flaske ad gangen. Væsker går stadig i kolber. En flaske brændselsgas brænder som ni kolber.",
    "Maskiner tager flasker ind som enhver anden genstand og giver den tomme flaske tilbage.",
    "# Lagertank",
    "Pneumatisk kemi (niveau 3) låser op for lagertanken. Den rummer 16.000 enheder (144 kolber) af én væske eller "
    "gas. Brug den med en fuld kolbe eller flaske for at tømme den i, med en tom kolbe for at tage en ud og med tom "
    "hånd for at aflæse niveauet. Tragte virker også: før fulde eller tomme beholdere ind fra oven eller fra siden, og "
    "tag det, der kommer ud, fra bunden. En redstone-komparator aflæser, hvor fuld den er. Hvis du ødelægger den, "
    "løber indholdet ud.",
]

UPGRADES = [
    "# Opgraderingspladser",
    "Hver maskine har to opgraderingspladser nederst i sit sidepanel. En opgradering virker, så snart dens ejer har "
    "forsket i den; to ens tæller kun én gang. Opgraderinger markeret \"brændselsfyret\" gør intet i elektriske "
    "maskiner.",
    "- Kompressor (Gaskomprimering, niveau 4): fylder de gasser, maskinen danner, på stålflasker.",
    "- Udskubber (Automatisk mølle, niveau 3): flytter maskinens produkter til en kiste, tønde eller tragt ved siden "
    "af. Den fodrer aldrig en anden maskine direkte.",
    "- Regenerator (Regenerativ ovn, niveau 4): en brændselsfyret maskine mister halvt så meget varme, så den brænder "
    "mindre brændsel og varmer hurtigere op.",
    "- Ildfast foring (Basiske ildfaste materialer, niveau 4): en brændselsfyret maskine kan gå 150 °C over sin egen "
    "grænse, hvis dens brændsel og blæsebælg rækker så langt.",
    "- Gasgenvindingshætte (Genvinding af biprodukter, niveau 4): væsker og gasser, som maskinen ville udlede "
    "(kuldioxid, svovldioxid, saltsyre og andre), går i kolber eller flasker. Damp og blandede gasser som bygas "
    "udledes stadig. Opfanget gas stopper aldrig maskinen: uden beholder eller plads udledes den alligevel.",
    "- Mekanisk omrører (Elmotor, niveau 4): bearbejder chargen 50 % hurtigere. Motoren brænder en fjerdedel mere "
    "brændsel, mens chargen reagerer, eller trækker ekstra strøm i en elektrisk maskine.",
    "- Katalysatorleje (Katalysatorlejer, niveau 5): reaktioner på en fast katalysator (jern, nikkel, platin, "
    "platin-rhodium, vanadiumpentoxid, kobolt-manganbromid) går dobbelt så hurtigt: ammoniak, kontaktprocessen, "
    "salpetersyre, kulhydrogenering, reformering, nylon og mere.",
    "- Transportbånd (Bunagummi, niveau 5): en udskubber på et gummibånd; flytter færdige produkter fire ad gangen i "
    "stedet for én.",
    "- Skumisolering (Styren og polystyren, niveau 6): en maskine, hvis grænse ligger på 300 °C eller derunder, "
    "mister 70 % mindre varme.",
    "- Sterilluftfilter (Penicillin, niveau 6): gæringstønde, eddikekar, dybtanksfermentor og bioreaktor giver 10 % "
    "mere. Dybtanksfermentoren kører ikke uden det.",
]

FARM = [
    "# Fedt og sæbe",
    "Udsmeltning (niveau 1) låser op for sæbekedlen. Ind går kød, knogler, æg og mælk; kedlen lader dem simre mellem "
    "80 og 150 °C. Talgen flyder op, og resten af kødet og knoglerne kommer ud som proteinmel, som køer, får, geder, "
    "grise og høns æder, og som giver god kompost. Talg med tråd giver tællelys: lys 12, osende, og de brænder ned "
    "tre dage efter, at de er sat op.",
    "Sæbekogning: læg talg eller vegetabilsk olie i kedlen og træaske på reagenspladsen for at få grønsæbe med "
    "glycerol i kolber. Hård sæbe (niveau 2) bruger i stedet natriumcarbonat eller gør grønsæbe hård med salt.",
    "Brug et stykke sæbe til at vaske dig: du er Ren i ti minutter, og Ren fjerner sulten fra råddent kød og rå "
    "kylling. Med en farvet læderrustning i den anden hånd vasker sæben farven ud. Sæbe med uld eller en seng i "
    "fremstillingsgitteret vasker den hvid.",
    "# Olie, læder og mæsk",
    "Oliepresning (niveau 1): oliepressen koldpresser græskar-, melon- og hvedefrø, kakaobønner og solsikker. "
    "Vegetabilsk olie fylder kolber; oliekagen, der bliver tilbage, er dyrefoder. Talg og vegetabilsk olie brænder "
    "også som brændsel.",
    "Garvning i kule (niveau 1): garvekulen gør kød og huder til læder uden fremstillingsgitter, langsomt: fire "
    "stykker råddent kød giver ét læder på omkring tre spildage. Læg ege- eller granstammer på reagenspladsen for "
    "deres garvestof; den udludede garvebark, der bliver tilbage, brænder som brændsel. Alunfremstilling (niveau 2) "
    "giver kalialun, og to kaninskind med en alun garves til læder i fremstillingsgitteret.",
    "Mæskning (niveau 1): gæringstønden gør stivelsen i hvede og kartofler til sukker og derefter til alkohol. "
    "Stivelsessukker (niveau 3) gør det første trin hurtigere med lidt svovlsyre i oplukningskarret. Sød vitriololie "
    "(niveau 2): ethanol destilleret med svovlsyre i alembikken giver diethylæter.",
    "# Gødning",
    "Superfosfat (niveau 3) opløser knogler eller råfosfat i svovlsyre; Gasvand (niveau 3) vasker ammoniakken ud af "
    "koksovnen (læg kolber på dens kolbeplads), og blykammeret gør den til ammoniumsulfat. Brug en af de to "
    "gødninger på landbrugsjord: i de næste otte høster vokser afgrøden halvanden gang så hurtigt, og hver moden høst "
    "giver én mere.",
    "# Gummi",
    "Gummitræer vokser vildt i junglen (jungle, tynd jungle og bambusjungle). Deres løv taber unge træer, og det unge "
    "træ vokser overalt, men et gummitræ giver kun latex i junglen. Vulkanisering (niveau 3) låser op for "
    "tappehanen: fastgør den på siden af en gummitræsstamme, og brug den med en tom glaskolbe; den fylder en kolbe "
    "hvert femte minut og holder fire klar. Tragte kan give den tomme kolber fra oven og trække latexen ud fra "
    "bunden. Uden for junglen siger den, at klimaet ikke passer.",
    "I vulkaniseringskedlen bliver latex med svovl på reagenspladsen til vulkaniseret gummi mellem 130 og 160 °C; "
    "uden svovl tørrer den kun til rågummi. Vulkaniseret gummi giver gummisko (dine skridt skaber ingen vibrationer, "
    "og magma og skildpaddeæg bemærker dig ikke) og gummihjul: brug dem på en minevogn, så kører den 25 % "
    "hurtigere.",
]

TAR = [
    "# Stenkulstjære",
    "Tjæredestillation (niveau 3): når den er udforsket, opfanger koksovnen kullets tjære i kolber (læg tomme kolber "
    "på dens kolbeplads), omkring én kolbe pr. 13 kul. Tjæredestillatoren deler stenkulstjære i benzen, toluen, "
    "phenol (karbolsyre) og anthracen og efterlader beg. Beg med grus og sand giver tjæremakadam, en vejblok lige så "
    "hurtig som asfalt. Benzen, phenol, tjære og beg brænder som brændsel. Nitrobenzen (niveau 3) nitrerer benzen i "
    "oplukningskarret, og Anilin (niveau 4) reducerer det med jern.",
    "# Farvestoffer",
    "Berlinerblåt (niveau 2) låser op for farvekarret: råddent kød eller kød med jernvitriol på reagenspladsen giver "
    "pigmentet, og karret gør det til blåt farvestof, 8 pr. genstand. Tjærefarvestofferne giver 32 almindelige "
    "farvestoffer pr. kolbe: fuchsin (anilin og hvid arsenik, magenta), picrinsyre (phenol og salpetersyre i "
    "oplukningskarret, gul) og alizarin (anthracen og kaustisk soda i autoklaven, rød). Læg farven i farvekarret for "
    "at få almindelige farvestoffer.",
    "# Infektion og medicin",
    "Et slag fra en zombie, udtørretskal, draug eller zombielandsbyboer smitter dig 1 gang ud af 200, at spise "
    "råddent kød 1 gang ud af 100; at være Ren halverer begge dele. En infektion varer en spildag med svaghed og "
    "sult. Ubehandlet heler den af sig selv 7 gange ud af 10; resten bliver til sepsis, 1 skade hvert tiende sekund, "
    "til du dør. Karbolsæbe (Antiseptisk kirurgi: phenol og hård sæbe) og aspirin (niveau 4: phenol i autoklaven med "
    "natriumcarbonat, derefter eddikesyre i oplukningskarret) helbreder en infektion, før den bliver værre. Kun "
    "sulfapræparater (niveau 5) og antibiotika på niveau 6 helbreder sepsis. Mælk helbreder ingen af delene. "
    "Spilreglen periodicElementsInfection slår infektion fra.",
    "Aspirin fjerner også kvalme og mine træthed og giver 10 sekunders regenerering. Æterbedøvelse (æter på uld), "
    "brugt på en zombielandsbyboer, hvis helbredelse allerede er begyndt, halverer tiden.",
    "# Sprængstoffer",
    "Dynamit (niveau 4): glycerol fra sæbekogningen, nitreret koldt (5 til 25 °C) i oplukningskarret, giver "
    "nitroglycerin; med sand og papir giver det dynamit, og fire stænger med tråd giver en mineladning. Tænd den med "
    "flint og stål eller redstone: efter 4 sekunder bryder den hver blok i en terning på 5x5x5, der er mindre "
    "modstandsdygtig end obsidian, og det hele falder. Med mobGriefing slået fra skader den kun det, der er i "
    "nærheden. Kordit (niveau 4) ælter nitroglycerin med røgsvagt krudt; korditpatroner skyder 25 % stærkere fra "
    "baglader-riflen og uden røg.",
    "# Afgrøder og skadedyr",
    "Bordeauxvæske (kobbervitriol, brændt kalk og vand), sprøjtet på en afgrøde eller en sød bærbusk, forhindrer, at "
    "den skylles væk eller visner i mørke, og bærbuske holder op med at kradse. Insektpulver: oliepressen maler "
    "okseøjeblomster til pyrethrum, og insektsprayen skader edderkopper, sølvfisk, endermites og bier op til 4 "
    "blokke frem.",
    "# Vakuumpumpe",
    "Vakuumdestillation (niveau 4) låser op for opgraderingen vakuumpumpe: i en alembik, tjæredestillator, "
    "sæbekedel, krystallisator, saltpande eller raffinaderidestillator foregår hver reaktion 100 °C koldere, så "
    "maskinen varmer hurtigere op og mister mindre varme.",
]

INDUSTRY = [
    "# Karbid og acetylen",
    "Calciumkarbid (niveau 5): brændt kalk med koks på lysbueovnens brændselsplads smelter til karbid ved 1800 °C "
    "eller mere. I oplukningskarret med vand på reagenspladsen giver karbid acetylen (læg kolber, eller flasker og "
    "en kompressor, i karret) og efterlader læsket kalk.",
    "Karbidlampen (messing, glas, karbid og en vandspand) giver lys 14, holdt i en hvilken som helst hånd eller båret "
    "på hovedet, i en halv time. Brug den med calciumkarbid i dit inventar for at fylde den op; en tom lampe går "
    "ikke i stykker.",
    "Autogensvejsning: fremstil en svejsebrænder af en acetylenflaske og en iltflaske, en stålbarre og en "
    "kobberbarre. Hold et beskadiget metalværktøj eller rustningsstykke i den anden hånd, og brug brænderen: en "
    "barre af dets eget metal fra dit inventar reparerer en fjerdedel, uden erfaring. Brænderen klarer 64 "
    "svejsninger; fremstil den igen med friske flasker for at fylde den op.",
    "Kalkkvælstof: karbid i kalkovnen mellem 1000 og 1150 °C med kvælstof på reagenspladsen optager kvælstoffet som "
    "calciumcyanamid, en gødning.",
    "# Gødning af luft",
    "Urinstofsyntese: ammoniak i højtryksreaktoren mellem 170 og 200 °C med kuldioxid på reagenspladsen (opfang den "
    "med en gasgenvindingshætte på en kalkovn eller gæringstønde). Prillet gødning låser op for prilletårnet, en "
    "elektrisk maskine: ammoniak med salpetersyre bliver til ammoniumnitrat, og urinstof med PK-blanding "
    "(superfosfat og potaske) til NPK-priller.",
    "Kalkkvælstof, urinstof og ammoniumnitrat virker på landbrugsjord som superfosfat. NPK-priller får afgrøden til "
    "at vokse dobbelt så hurtigt i otte høster, hver med én mere.",
    "# Plast og gummi",
    "Formaldehyd: methanol over sølv i Ostwald-konverteren mellem 600 og 700 °C. Bakelit: phenol med formaldehyd på "
    "reagenspladsen, i autoklaven mellem 150 og 200 °C. Fire bakelit giver fire bakelitblokke, brune, eller med et "
    "farvestof sorte, røde, grønne, elfenbensfarvede eller ravfarvede. Bakelit kan også bruges som kabinet til "
    "geigertælleren i stedet for messing.",
    "Neopren: acetylen med saltsyre i højtryksreaktoren. En hel våddragt af neopren giver delfintjeneste i vandet, "
    "og ingen af dens dele lukker pulversneens kulde ind.",
    "Bunagummi, uden jungle: ethanol over katalysatoren i krakanlægget mellem 380 og 450 °C giver butadien; benzen "
    "med ethylen på reagenspladsen mellem 550 og 600 °C giver styren; butadien med styren i oplukningskarret mellem "
    "40 og 60 °C giver bunagummi. Det bruges til gummihjul og opgraderingen transportbånd.",
    "# Sprængstoffer",
    "TNT: toluen fra tjæredestillatoren, nitreret i oplukningskarret. Én TNT med tre papir giver tre blokke "
    "almindelig TNT. Fire TNT, en stålbarre og tråd giver en nedrivningsladning: tændt som en mineladning bryder den "
    "en terning på 3x3x3 af alt undtagen grundfjeld, obsidian medregnet, og det hele falder.",
    "# Gæring, brændstof og medicin",
    "Acetonegæring: hvede eller kartofler i gæringstønden med calcit på reagenspladsen gærer til acetone, butanol og "
    "lidt ethanol i stedet for sukker. Biodiesel: talg eller vegetabilsk olie i sæbekedlen mellem 40 og 150 °C med "
    "methanol på reagenspladsen. Biodiesel og butanol driver dieselgeneratoren og brænder i ovne.",
    "Sulfapræparater: anilin med ammoniumsulfat i oplukningskarret giver sulfanilamid. Brug det til at helbrede en "
    "infektion på ethvert stadium, sepsis medregnet.",
]

LIFE = [
    "# Antibiotika",
    "Penicillin (niveau 6) låser op for dybtanksfermentoren, en elektrisk maskine, der kun kører med et "
    "sterilluftfilter. Glukose med mælk på reagenspladsen (for dens laktose) dyrker penicillin mellem 20 og 37 °C; "
    "streptomycin kræver proteinmel på reagenspladsen. Begge antibiotika helbreder en infektion eller sepsis og gør "
    "dig immun over for infektion i 20 minutter. Brug penicillin på en zombielandsbyboer med svaghed, så begynder "
    "helbredelsen, uden gyldent æble. Det brugte svampevæv fra tanken er gødning.",
    "# Pesticider",
    "DDT: benzen med klor i oplukningskarret. En DDT-pudderdåse brugt på jorden gør den dødelig for edderkopper, "
    "sølvfisk, endermites og bier i tre spildage; høns på den lægger kun halvt så meget. Ukrudtsmidlet (2,4-D, af "
    "phenol og klor) dræber blomster, bregner, lianer, buske, unge træer og løv omkring blokken, men skåner græs og "
    "afgrøder. Fremspiringsmidlet (atrazin, af ammoniak og klor med koks i højtryksreaktoren) forhindrer græs og "
    "svampevæv i at brede sig på jorden i ti spildage. Hver sprøjte har 16 brug og virker 4 blokke rundt.",
    "# Plast",
    "Styren og polystyren: styren i højtryksreaktoren. Otte polystyren omkring en kolbe methan giver skumisolering; "
    "fire polystyren og methan giver fire isoleringsblokke, og is eller sne, der rører isoleringen, smelter aldrig.",
    "# Sprængstoffer",
    "Plastisk sprængstof: formaldehyd og ammoniak giver hexamin, og hexamin nitreret koldt giver RDX. To RDX, en "
    "kobberbarre, en stålbarre og tråd giver en hulladning: sæt den på en væg og tænd den, så åbner den en tunnel på "
    "1x1, 16 blokke dyb, gennem alt undtagen grundfjeld. ANFO: seks ammoniumnitrat, diesel, papir og tråd giver en "
    "ANFO-ladning, der bryder en terning på 9x9x9 af sten; tre ud af fire blokke falder.",
    "# Strålebeskyttelsesforanstaltninger",
    "Kaliumiodid (natriumiodid fra tang med kaliumsalt i krystallisatoren) sænker dosen fra fissionsprodukter med "
    "40 % i en spildag. En berlinerblåt-kapsel (berlinerblåt og proteinmel) sænker den med en fjerdedel. Amifostin "
    "(niveau 7) halverer skaden fra al stråling i en time.",
    "# Bioteknologi",
    "Rekombinant DNA (niveau 7) låser op for bioreaktoren (den kræver et integreret kredsløb). Den gærer cellulosen i "
    "stammer, løv eller halm til ethanol, glukose til PLA og protein med vegetabilsk olie til mRNA-vaccinen, syv "
    "spildage uden infektion. Dens biomasse er gødning. PLA giver blomsterkrukker og genstandsrammer og kan bruges "
    "som kompost.",
    "Glyphosat: formaldehyd med fosforsyre i højtryksreaktoren. Markrydderen dræber alle planter omkring blokken, "
    "afgrøder medregnet, undtagen hvede fra tolerante frø (otte hvedefrø omkring et glyphosat). Tolerant hvede vokser "
    "også en tiendedel hurtigere.",
]

MINING = [
    "# Forhamre og graveskovle",
    "Stenbrudshamre (niveau 2) lærer dig forhammeren (fem barrer omkring en pind, over en pind) og graveskovlen (tre "
    "barrer og to pinde), af stål og hvert senere værktøjsmetal. Hver af dem bryder et 3x3-kvadrat på den side, du "
    "rammer, med tre tiendedele af hastigheden og slides ét point pr. blok. Snig for at bryde en enkelt blok. Kun "
    "blokke, der passer til værktøjet og ikke er hårdere end den, du sigter på, brydes; intet med inventar og ingen "
    "maskine brydes nogensinde; det, der falder, går direkte i dit inventar.",
    "# Sprængning",
    "Bjergsprængning (niveau 2): fire sortkrudt, papir og tråd giver to sprængladninger. Tændt med flint og stål "
    "eller redstone bryder en ladning en 3x3x3-blok af sten og lader det hele falde; jord, træ og maskiner skånes.",
    "# Bor og borekroner",
    "@4 Hvert bor kræver en borekrone: hold borekronen i den anden hånd, snig, og brug boret. Borekronen afgør, hvad "
    "det kan bryde, og hvor hurtigt (wolframstål 8,5, hurtigstål 10, hårdmetal 12, diamant 13), og slides ét point "
    "pr. slag. Fortryl borekronen, ikke boret; en isat borekrone låner boret sine fortryllelser, alle undtagen "
    "genopbyggelse. Snig og brug det uden borekrone i den anden hånd for at skifte tilstand. Et bor bruger 1,5 kJ "
    "pr. hårdhedspoint i hver blok: 2,25 for sten, 4,5 for dybskifer.",
    "Bjergbor (niveau 4): luftkompressoren, en elektrisk maskine, fylder stålflasker med trykluft uden at kræve "
    "opgraderingen kompressor. Trykluftbjergboret bruger én flaske pr. 600 kJ (omkring 400 sten) og giver den tomme "
    "tilbage; det bryder én blok, en 1x3- eller en 3x3-tunnel og sinker dig, mens du holder det.",
    "# Batteriværktøj",
    "Blyakkumulator (niveau 4): batteripakken rummer 1500 kJ. Forbind en ladebænk med kobbertråden, og brug "
    "batteriet på den; det oplades med op til 5 kJ pr. tick.",
    "Borehammeren (niveau 5) er bjergboret med batteri, uden at sinke dig. Motorsaven (niveau 5) fælder et helt træ, "
    "op til 128 stammer, for 3 kJ pr. stamme.",
    "Det tunge minebor (niveau 6) har desuden en 5x5-tilstand og en åretilstand, der følger en malm op til 32 "
    "blokke; med et blybatteri sinker det dig.",
    "Lithium-ion-pakken (niveau 7) rummer 6000 kJ, bruges først og fjerner den opbremsning. Malmsøgende bor (niveau "
    "7): i åretilstand bruger du det tunge minebor på en malm for at aflæse årens størrelse og grundstoffer, før du "
    "graver.",
]

ISOTOPES = [
    "# Stråling",
    "Radioaktive genstande giver en dosis, der hober sig op, mens du bærer dem eller er tæt på dem. Et elektroskop, "
    "senere en geigertæller, måler den. Blyvægge, varmcellen og atomreaktoren afskærmer det, de indeholder. Nogle "
    "prøver henfalder efter spiluret (en spildag er en virkelig dag for dem) og bliver til deres datterkerner i dit "
    "inventar.",
    "# Blåt skær",
    "De stærkeste strålere lyser blåt i mørke, som tjerenkovlys: radium og de stærkeste i luft, og under vand også "
    "polonium, americium, actinium og uranbeg, kraftigere. En kørende atomreaktor oplyser sit bassin, og kørende "
    "acceleratorer lyser også. Skæret afhænger af, hvor koncentreret aktiviteten er, ikke af hvor meget du har; uran "
    "lyser aldrig. Lysende genstande lyser også på deres plads i inventaret.",
    "# Naturlige isotoper",
    "Beskrivelsen af hvert stabilt grundstof viser dets naturlige isotoper, fra det mest almindelige til det "
    "sjældneste.",
    "# Variable forhold",
    "Genstande af uran (U-235), plutonium (Pu-240), lithium (Li-6), bor (B-10), calcium (Ca-48) og brint (H-2) viser "
    "deres eget forhold. En genstand med det naturlige forhold stables med enhver anden; en beriget eller forarmet "
    "stables kun med genstande med samme forhold. Maskiner blander forholdene efter masse.",
    "- Calutronen (Calutronberigning) deler uran, lithium eller bor i en lille beriget andel og et forarmet "
    "restprodukt. Fodr andelen ind igen for at berige yderligere.",
    "- Uranmetal med 3 % U-235 eller mere sintres til brændselspellets til reaktoren.",
    "- Plutonium dannet i reaktoren er af våbenkvalitet (6 % Pu-240); plutonium fra brugt brændsel er af "
    "reaktorkvalitet.",
    "- Lithium med over 90 % Li-6 i en reaktors pladser bliver til tritium, der får tritiumlys til at lyse for "
    "evigt.",
    "- Bor med over 90 % B-10 giver styrestave: med én i sine pladser brænder en reaktor intet brændsel, så længe den "
    "er fuld.",
    "- Tungt vand fra berigningskaskaden (fodr den med is eller sne) gør det muligt at brænde brændselsstave af "
    "naturligt uran.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Første skridt",
    "group": "Niveau {n}",
    "research": "Forskning",
    "machines": "Maskiner",
    "reactions": "Reaktioner",
    "recipe": " Opskrift: {r}.",
    "recipe_head": "Opskrift: {r}.",
    "projects": "# Projekter (★ = nøgle)",
    "needs": " Kræver {x}.",
    "samples": " Prøver: {x}.",
    "unlocks": " Låser op for: {x}.",
    "up_to": "{desc}. Op til {c} °C",
    "electric": ", elektrisk.",
    "fuel": ", brændselsfyret.",
    "amounts": "Mængder pr. 100 enheder charge.",
    "ores": "Malme",
    "ores_head": "# Malme",
    "ores_intro": "Malme dukker op i nyudforskede chunks. Silkeberøring og Held virker som på almindelige malme.",
    "ore_line": "{name}: {desc}. Giver {lo}-{hi} {drop}; kræver en hakke af {tier}. ",
    "found": "Findes {w}.",
    "where": "fra y {lo} til {hi} {b}",
    "biomes": "i: {b}",
    "overworld": "i Oververdenen",
    "nether": "i Nether",
    "power": "Kraft",
    "power_head": "# Kraft",
    "power_intro": "Elektriske maskiner får strøm fra kraftblokke ved siden af dem eller forbundet med kobbertråd (op "
                   "til 64 blokke tråd) eller isoleret kabel (fire gange så langt).",
    "power_line": "{name} (niveau {lvl}, {project}): {desc}.",
    "isotopes": "Stråling og isotoper",
    "containers": "Beholdere",
    "upgrades": "Opgraderinger",
    "farm": "Gård og fedt",
    "tar": "Tjære og lægemidler",
    "industry": "Industriel kemi",
    "life": "Biovidenskab",
    "mining": "Minedrift",
    "crafting": "Fremstilling",
    "craft_head": "# Fremstilling",
    "craft_intro": "De formløse opskrifter, som modet tilføjer (opskrifter på maskiner og blokke står i hvert niveaus "
                   "kapitel).",
    "shaped_head": "# Formede opskrifter (rækker oppefra og ned)",
    "or": " eller ",
    "any": "enhver {x}",
    "empty": "tom",
    "reagent": "(reagens)",
    "carbon": " + {c} kulstof",
    "nothing": "intet beholdes",
    "slag": "; slagge: ",
    "vents": "; udleder: ",
    "temp": ". Fra {a} til {b} °C.",
    "sep": "; ",
}

WORDS = {
    # pickaxe tiers
    "wood": "træ", "stone": "sten", "iron": "jern",
    # biomes and biome tags
    "mountain": "bjerge", "jungle": "jungle", "savanna": "savanne", "desert": "ørken",
    "badlands": "stenørken", "ocean": "hav", "swamp": "sump", "mangrove swamp": "mangrovesump",
    "basalt deltas": "basaltdeltaer", "beach": "strand", "river": "flod", "dripstone caves": "drypstensgrotter",
    # item tags
    "logs": "stamme", "logs that burn": "brændbar stamme", "oak logs": "egestamme", "planks": "planker",
    "wool": "uld", "beds": "seng", "cable insulation": "kabelisolering", "soap": "sæbe",
    # upgrades unlocked by research
    "forced air": "blæst luft (blæsebælg)", "steam blast": "dampblæst",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["da_dk"]
T["atlas"] = _ATLAS_TITLE["da_dk"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["da_dk"]
T.update(_TREES_T["da_dk"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("da_dk")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["da_dk"]
T["compendium"] = _COMP_TITLE["da_dk"]

# 3.5.0 Plant Compendium (tools/compendium_plants_da_dk.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["da_dk"]
T["plant_compendium"] = _PCOMP_TITLE["da_dk"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_da_dk.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["da_dk"]
T["mineral_compendium"] = _MCOMP_TITLE["da_dk"]
