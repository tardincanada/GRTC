"""Italian (it_it) text of the Field Guide (2.5.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the it_it lang file.
"""

DECIMAL = ","

INTRO = [
    "# Benvenuto",
    "Ogni blocco e ogni oggetto di questo mondo è fatto di elementi reali. Questa guida segue l'albero tecnologico della "
    "mod dall'età della pietra fino agli ultimi elementi, un capitolo per livello.",
    "# Unità",
    "1 unità è 1 grammo. Un blocco vale 1000 unità, un lingotto o un oggetto di elemento o sostanza 111,1, una pepita "
    "12,35 e un secchio 1000. Passa il cursore su un blocco o un oggetto per vedere di cosa è fatto; tieni premuto Maiusc "
    "per vedere tutto.",
    "# Ricerca",
    "Ogni livello ha la sua stazione di ricerca. Metti i campioni di un progetto nei suoi 9 slot, scegli il progetto e "
    "premi Ricerca. I progetti chiave (segnati con una stella) alzano il tuo livello tecnologico; ti servono i progetti "
    "chiave di un livello prima di poter piazzare la stazione successiva. /research mostra ciò che sai.",
    "I trattati scritti in una stazione insegnano tutto ciò che sai a un altro giocatore.",
    "# Macchine",
    "Una macchina scompone ogni oggetto che entra secondo la sua composizione e applica ogni reazione che hai studiato, "
    "finché è abbastanza calda ma non troppo. I prodotti escono come oggetti, gli scarti come scoria, e alcuni gas "
    "vengono sfiatati. Liquidi e gas riempiono beute di vetro vuote dal pannello laterale della macchina. Alcune "
    "reazioni richiedono un secondo ingrediente nello slot del reagente.",
    "Le macchine a combustibile bruciano ciò che è nello slot del combustibile: la legna arriva a 650 °C, il carbone di "
    "legna e il carbone a 1100 °C, i combustibili del petrolio a 1200 °C e il gas a 1300 °C. Il mantice (aria forzata) "
    "aggiunge 250 °C. Carbone di legna e carbone servono anche come carbonio che riduce i minerali. Le macchine "
    "elettriche prendono corrente da una pila voltaica, una dinamo o un altro blocco di energia adiacente o collegato "
    "con filo di rame.",
    "Le normali fornaci non fondono più i minerali di ferro, rame e oro: i metalli escono dalle macchine.",
    "# Livello 0",
    "Senza alcuna ricerca puoi costruire una carbonaia (8 blocchi di terra intorno a un tronco) e carbonizzare i tronchi "
    "in carbone di legna.",
]

CONTAINERS = [
    "# Beute di vetro",
    "Ogni oggetto liquido o gassoso è una beuta di vetro tappata che contiene 111,1 unità. Cinque blocchi di vetro danno "
    "quattro beute, senza ricerca. Una macchina riempie una beuta vuota dallo slot superiore del suo pannello laterale "
    "per ogni liquido o gas che produce; senza beute si ferma e le chiede. Quando una macchina, una ricetta, una fornace "
    "o un progetto di ricerca usa il contenuto di una beuta, ti restituisce la beuta vuota.",
    "# Bombole d'acciaio e compressore",
    "Compressione dei gas (livello 4) sblocca le bombole d'acciaio e il compressore. Una bombola contiene 1000 unità di "
    "un gas compresso: il contenuto di nove beute in un solo oggetto. Monta un compressore in uno dei due slot di "
    "potenziamento di una macchina e metti bombole vuote nel secondo slot del suo pannello laterale: la macchina "
    "riempirà allora bombole con ogni gas che produce, una bombola alla volta. I liquidi vanno ancora nelle beute. Una "
    "bombola di gas combustibile brucia come nove beute.",
    "Le macchine accettano le bombole in ingresso come qualsiasi altro oggetto e restituiscono la bombola vuota.",
    "# Serbatoio di stoccaggio",
    "Chimica pneumatica (livello 3) sblocca il serbatoio di stoccaggio. Contiene 16 000 unità (144 beute) di un solo "
    "liquido o gas. Usalo con una beuta o una bombola piena per svuotarla dentro, con una beuta vuota per prelevarne una "
    "e a mano vuota per leggerne il livello. Funzionano anche le tramogge: inserisci i contenitori pieni o vuoti "
    "dall'alto o dai lati e raccogli ciò che esce dal basso. Un comparatore di redstone legge quanto è pieno. Se lo "
    "rompi, il contenuto si disperde.",
]

UPGRADES = [
    "# Slot di potenziamento",
    "Ogni macchina ha due slot di potenziamento in fondo al pannello laterale. Un potenziamento funziona appena il suo "
    "proprietario lo ha studiato; due uguali contano una volta sola. I potenziamenti segnati «a combustibile» non fanno "
    "nulla sulle macchine elettriche.",
    "- Compressore (Compressione dei gas, livello 4): riempie bombole d'acciaio con i gas prodotti dalla macchina.",
    "- Espulsore di uscita (Mulino automatico, livello 3): sposta i prodotti della macchina in un baule, un barile o una "
    "tramoggia adiacenti. Non alimenta mai direttamente un'altra macchina.",
    "- Rigeneratore (Forno a rigenerazione, livello 4): una macchina a combustibile perde metà del calore, quindi brucia "
    "meno combustibile e si scalda prima.",
    "- Rivestimento refrattario (Refrattari basici, livello 4): una macchina a combustibile può superare di 150 °C il "
    "proprio limite, se il suo combustibile e il mantice ci arrivano.",
    "- Cappa di recupero gas (Recupero dei sottoprodotti, livello 4): i liquidi e i gas che la macchina sfiaterebbe "
    "(anidride carbonica, anidride solforosa, acido cloridrico e altri) vanno in beute o bombole. Il vapore e i gas misti "
    "come il gas di città vengono ancora sfiatati. Il gas raccolto non ferma mai la macchina: senza contenitore o "
    "spazio viene sfiatato comunque.",
    "- Agitatore meccanico (Motore elettrico, livello 4): lavora la carica il 50% più in fretta. Il suo motore brucia un "
    "quarto di combustibile in più mentre la carica reagisce, o consuma corrente extra in una macchina elettrica.",
    "- Letto catalitico (Letti catalitici, livello 5): le reazioni su un catalizzatore solido (ferro, nichel, platino, "
    "platino-rodio, pentossido di vanadio, bromuro di cobalto-manganese) vanno al doppio della velocità: ammoniaca, "
    "processo di contatto, acido nitrico, idrogenazione del carbone, reforming, nylon e altro.",
    "- Nastro trasportatore (Gomma Buna, livello 5): un espulsore di uscita su un nastro di gomma; sposta i prodotti "
    "finiti quattro alla volta invece che uno.",
    "- Coibentazione in schiuma (Stirene e polistirene, livello 6): una macchina il cui limite è di 300 °C o meno perde "
    "il 70% di calore in meno.",
    "- Filtro ad aria sterile (Penicillina, livello 6): la botte di fermentazione, la vasca dell'aceto, il fermentatore "
    "a vasca profonda e il bioreattore producono il 10% in più. Il fermentatore a vasca profonda non funziona senza.",
]

FARM = [
    "# Grassi e sapone",
    "Fusione dei grassi animali (livello 1) sblocca la caldaia del sapone. Ci vanno carne, ossa, uova e latte; la "
    "caldaia li fa sobbollire tra 80 e 150 °C. Il sego galleggia, e la carne e le ossa che restano escono come farina "
    "proteica, che mangiano mucche, pecore, capre, maiali e galline e che fa un buon compost. Il sego con la cordicella "
    "dà candele di sego: luce 12, fumose, e si consumano tre giorni dopo essere state piazzate.",
    "Fabbricazione del sapone: metti sego o olio vegetale nella caldaia e cenere di legna nello slot del reagente per "
    "ottenere sapone molle, con glicerolo nelle beute. Sapone duro (livello 2) usa invece carbonato di sodio, oppure "
    "indurisce il sapone molle con il sale.",
    "Usa una saponetta per lavarti: resti Pulito per dieci minuti, e Pulito elimina la Fame della carne marcia e del "
    "pollo crudo. Con un'armatura di cuoio tinta nell'altra mano, il sapone le toglie la tinta. Sapone con lana o un "
    "letto nella griglia di fabbricazione li lava di bianco.",
    "# Olio, cuoio e mosto",
    "Spremitura dell'olio (livello 1): il torchio da olio spreme a freddo semi di zucca, di anguria e di grano, semi di "
    "cacao e girasoli. L'olio vegetale riempie le beute; il panello che resta è mangime per il bestiame. Anche il sego e "
    "l'olio vegetale bruciano come combustibile.",
    "Concia in fossa (livello 1): la fossa di concia trasforma carne e pelli in cuoio senza griglia di fabbricazione, "
    "lentamente: quattro carni marce danno un cuoio in circa tre giorni di gioco. Metti tronchi di quercia o di abete "
    "nello slot del reagente per il loro tannino; la scorza esausta che resta brucia come combustibile. Produzione "
    "dell'allume (livello 2) dà allume di potassio, e due pelli di coniglio con un allume si conciano in cuoio nella "
    "griglia di fabbricazione.",
    "Ammostamento (livello 1): la botte di fermentazione trasforma l'amido di grano e patate in zucchero e poi in alcol. "
    "Zucchero d'amido (livello 3) rende più rapido il primo passo con un po' di acido solforico nella vasca di "
    "digestione. Olio dolce di vetriolo (livello 2): l'etanolo distillato con acido solforico nell'alambicco dà etere "
    "dietilico.",
    "# Fertilizzanti",
    "Superfosfato (livello 3) scioglie ossa o fosforite in acido solforico; Acque ammoniacali (livello 3) lava "
    "l'ammoniaca del forno a coke (lascia beute nel suo slot delle beute) e la camera di piombo la trasforma in solfato "
    "di ammonio. Usa uno dei due fertilizzanti sulla terra zappata: per gli otto raccolti successivi la coltura cresce "
    "una volta e mezzo più in fretta e ogni raccolto maturo dà un frutto in più.",
    "# Gomma",
    "Gli alberi della gomma crescono spontanei nelle giungle (giungla, giungla rada e giungla di bambù). Le loro foglie "
    "lasciano cadere arbusti, e l'arbusto cresce ovunque, ma un albero della gomma dà lattice solo in una giungla. "
    "Vulcanizzazione (livello 3) sblocca lo spillatore: fissalo sul fianco di un tronco di albero della gomma e usalo con "
    "una beuta di vetro vuota; riempie una beuta ogni cinque minuti e ne tiene quattro. Le tramogge possono dargli beute "
    "vuote dall'alto e prelevare il lattice dal basso. Fuori dalla giungla avverte che il clima non è adatto.",
    "Nel vulcanizzatore, il lattice con zolfo nello slot del reagente vulcanizza in gomma vulcanizzata tra 130 e 160 °C; "
    "senza zolfo si essicca soltanto in gomma grezza. La gomma vulcanizzata dà scarpe da ginnastica (i tuoi passi non "
    "fanno vibrazioni, e magma e uova di tartaruga non ti notano) e ruote di gomma: usale su un vagonetto e correrà il "
    "25% più veloce.",
]

TAR = [
    "# Catrame di carbone",
    "Distillazione del catrame (livello 3): una volta studiata, il forno a coke raccoglie il catrame del carbone in "
    "beute (lascia beute vuote nel suo slot delle beute), circa una beuta ogni 13 carboni. Il distillatore di catrame "
    "separa il catrame di carbone in benzene, toluene, fenolo (acido fenico) e antracene, e lascia la pece. La pece con "
    "ghiaia e sabbia dà il macadam, un blocco stradale veloce come l'asfalto. Benzene, fenolo, catrame e pece bruciano "
    "come combustibile. Nitrobenzene (livello 3) nitra il benzene nella vasca di digestione, e Anilina (livello 4) lo "
    "riduce con il ferro.",
    "# Coloranti",
    "Blu di Prussia (livello 2) sblocca il tino di tintura: carne marcia o carne con vetriolo verde nello slot del "
    "reagente dà il pigmento, e il tino lo trasforma in colorante blu, 8 per oggetto. I coloranti del catrame danno 32 "
    "coloranti normali per beuta: fucsina (anilina e arsenico bianco, magenta), acido picrico (fenolo e acido nitrico "
    "nella vasca di digestione, giallo) e alizarina (antracene e soda caustica nell'autoclave, rosso). Metti il "
    "colorante nel tino di tintura per ottenere coloranti normali.",
    "# Infezione e medicina",
    "Un colpo di zombi, mummificato, annegato o villico zombi ti infetta 1 volta su 200, e mangiare carne marcia 1 volta "
    "su 100; essere Pulito dimezza entrambe le probabilità. Un'Infezione dura un giorno di gioco con Debolezza e Fame. "
    "Senza cure guarisce da sola 7 volte su 10; le altre diventano Sepsi, 1 danno ogni 10 secondi finché non muori. Il "
    "sapone fenicato (Chirurgia antisettica: fenolo e sapone duro) e l'aspirina (livello 4: fenolo nell'autoclave con "
    "carbonato di sodio, poi acido acetico nella vasca di digestione) curano un'Infezione prima che peggiori. Solo i "
    "sulfamidici (livello 5) e gli antibiotici del livello 6 curano la Sepsi. Il latte non cura nessuna delle due. La "
    "regola di gioco periodicElementsInfection disattiva l'infezione.",
    "L'aspirina toglie anche la Nausea e l'Affaticamento e dà 10 secondi di Rigenerazione. L'etere anestetico (etere su "
    "lana), usato su un villico zombi la cui cura è già iniziata, dimezza la durata della cura.",
    "# Esplosivi",
    "Dinamite (livello 4): il glicerolo della fabbricazione del sapone, nitrato a freddo (da 5 a 25 °C) nella vasca di "
    "digestione, dà nitroglicerina; con sabbia e carta dà dinamite, e quattro candelotti con una cordicella danno una "
    "carica da mina. Accendila con un acciarino o con la redstone: dopo 4 secondi rompe ogni blocco di un cubo 5x5x5 "
    "meno resistente dell'ossidiana e li fa cadere tutti. Con mobGriefing disattivato ferisce solo ciò che è vicino. "
    "Cordite (livello 4) impasta la nitroglicerina con la polvere senza fumo; le cartucce di cordite sparano dal fucile "
    "a retrocarica il 25% più forte e senza fumo.",
    "# Colture e parassiti",
    "La poltiglia bordolese (vetriolo azzurro, calce viva e acqua) spruzzata su una coltura o un cespuglio di bacche "
    "dolci impedisce che l'acqua la porti via o che appassisca al buio, e i cespugli di bacche smettono di graffiare. "
    "Polvere insetticida: il torchio da olio macina le margherite in piretro, e lo spray insetticida ferisce ragni, "
    "pesciolini d'argento, endermite e api fino a 4 blocchi davanti a te.",
    "# Pompa a vuoto",
    "Distillazione sotto vuoto (livello 4) sblocca il potenziamento Pompa a vuoto: in un alambicco, un distillatore di "
    "catrame, una caldaia del sapone, un cristallizzatore, una salina o un distillatore di raffineria, ogni reazione "
    "avviene 100 °C più in basso, così la macchina si scalda prima e perde meno calore.",
]

INDUSTRY = [
    "# Carburo e acetilene",
    "Carburo di calcio (livello 5): la calce viva con coke nello slot del combustibile del forno elettrico ad arco fonde "
    "in carburo a 1800 °C o più. Nella vasca di digestione con acqua nello slot del reagente, il carburo dà acetilene "
    "(lascia beute, o bombole e un compressore, nella vasca) e lascia calce spenta.",
    "La lampada a carburo (ottone, vetro, carburo e un secchio d'acqua) dà luce 14 tenuta in una mano qualsiasi o "
    "indossata in testa, per mezz'ora. Usala con carburo di calcio nell'inventario per ricaricarla; una lampada vuota "
    "non si rompe.",
    "Saldatura ossiacetilenica: fabbrica un cannello con una bombola di acetilene e una di ossigeno, un lingotto "
    "d'acciaio e un lingotto di rame. Tieni un attrezzo o un pezzo d'armatura di metallo danneggiato nell'altra mano e "
    "usa il cannello: un lingotto del suo stesso metallo preso dall'inventario ne ripara un quarto, senza esperienza. Il "
    "cannello fa 64 saldature; rifabbricalo con bombole nuove per ricaricarlo.",
    "Calciocianammide: il carburo nella fornace da calce tra 1000 e 1150 °C con azoto nello slot del reagente assorbe "
    "l'azoto come calciocianammide, un fertilizzante.",
    "# Fertilizzanti dall'aria",
    "Sintesi dell'urea: ammoniaca nel reattore ad alta pressione tra 170 e 200 °C, con anidride carbonica nello slot "
    "del reagente (raccoglila da una fornace da calce o da una botte di fermentazione con una cappa di recupero gas). "
    "Fertilizzante granulare sblocca la torre di prilling, una macchina elettrica: l'ammoniaca con acido nitrico diventa "
    "nitrato di ammonio, e l'urea con miscela PK (superfosfato e potassa) granuli NPK.",
    "Calciocianammide, urea e nitrato di ammonio agiscono sulla terra zappata come il superfosfato. I granuli NPK fanno "
    "crescere la coltura il doppio più in fretta, per otto raccolti, ognuno con un frutto in più.",
    "# Plastiche e gomma",
    "Formaldeide: metanolo su argento nel convertitore Ostwald tra 600 e 700 °C. Bachelite: fenolo con formaldeide nello "
    "slot del reagente, nell'autoclave tra 150 e 200 °C. Quattro bacheliti danno quattro blocchi di bachelite, marroni, "
    "oppure neri, rossi, verdi, avorio o ambra con un colorante. La bachelite serve anche da involucro del contatore "
    "Geiger al posto dell'ottone.",
    "Neoprene: acetilene con acido cloridrico nel reattore ad alta pressione. Una muta completa di neoprene dà Grazia "
    "delfina in acqua, e nessuno dei suoi pezzi fa passare il freddo della neve polverosa.",
    "Gomma Buna, senza giungla: l'etanolo sul catalizzatore nell'impianto di cracking tra 380 e 450 °C dà butadiene; il "
    "benzene con etilene nello slot del reagente tra 550 e 600 °C dà stirene; il butadiene con stirene nella vasca di "
    "digestione tra 40 e 60 °C dà gomma Buna. Serve per le ruote di gomma e per il potenziamento Nastro trasportatore.",
    "# Esplosivi",
    "TNT: il toluene del distillatore di catrame, nitrato nella vasca di digestione. Un TNT con tre carte dà tre blocchi "
    "di TNT normale. Quattro TNT, un lingotto d'acciaio e una cordicella danno una carica da demolizione: accesa come una "
    "carica da mina, rompe un cubo 3x3x3 di tutto tranne la roccia di fondo, ossidiana compresa, e tutto cade.",
    "# Fermentazione, combustibile e medicina",
    "Fermentazione acetonica: grano o patate nella botte di fermentazione, con calcite nello slot del reagente, "
    "fermentano in acetone, butanolo e un po' di etanolo invece che in zucchero. Biodiesel: sego o olio vegetale nella "
    "caldaia del sapone tra 40 e 150 °C con metanolo nello slot del reagente. Biodiesel e butanolo alimentano il "
    "generatore diesel e bruciano nelle fornaci.",
    "Sulfamidici: l'anilina con solfato di ammonio nella vasca di digestione dà sulfanilammide. Usala per curare "
    "un'Infezione in qualsiasi stadio, Sepsi compresa.",
]

LIFE = [
    "# Antibiotici",
    "Penicillina (livello 6) sblocca il fermentatore a vasca profonda, una macchina elettrica che funziona solo con un "
    "filtro ad aria sterile. Il glucosio con latte nello slot del reagente (per il suo lattosio) coltiva penicillina tra "
    "20 e 37 °C; la streptomicina vuole farina proteica nello slot del reagente. Entrambi gli antibiotici curano "
    "un'Infezione o una Sepsi e ti rendono immune all'Infezione per 20 minuti. Usa la penicillina su un villico zombi "
    "con Debolezza e la sua cura comincia, senza mela d'oro. Il micelio esausto della vasca è un fertilizzante.",
    "# Pesticidi",
    "DDT: benzene con cloro nella vasca di digestione. Un impolveratore di DDT usato sul terreno lo rende mortale per "
    "ragni, pesciolini d'argento, endermite e api per tre giorni di gioco; le galline che ci stanno sopra depongono la "
    "metà. Il diserbante (2,4-D, da fenolo e cloro) uccide fiori, felci, rampicanti, cespugli, arbusti e foglie intorno "
    "al blocco, ma risparmia l'erba e le colture. L'antigerminante (atrazina, da ammoniaca e cloro con coke nel reattore "
    "ad alta pressione) impedisce all'erba e al micelio di espandersi sul terreno per dieci giorni di gioco. Ogni "
    "spruzzatore ha 16 usi e agisce per 4 blocchi intorno.",
    "# Plastiche",
    "Stirene e polistirene: stirene nel reattore ad alta pressione. Otto polistireni intorno a una beuta di metano danno "
    "coibentazione in schiuma; quattro polistireni e metano danno quattro blocchi di isolante, e il ghiaccio o la neve "
    "che tocca l'isolante non si scioglie mai.",
    "# Esplosivi",
    "Esplosivo plastico: formaldeide e ammoniaca danno esammina, e l'esammina nitrata a freddo dà RDX. Due RDX, un "
    "lingotto di rame, un lingotto d'acciaio e una cordicella danno una carica cava: piazzala contro una parete e "
    "accendila, e apre un tunnel 1x1 profondo 16 blocchi attraverso tutto tranne la roccia di fondo. ANFO: sei nitrati "
    "di ammonio, gasolio, carta e cordicella danno una carica di ANFO che rompe un cubo di roccia 9x9x9; cadono tre "
    "blocchi su quattro.",
    "# Contromisure alle radiazioni",
    "Lo ioduro di potassio (ioduro di sodio delle alghe con sale di potassio nel cristallizzatore) riduce del 40% la "
    "dose dei prodotti di fissione per un giorno di gioco. Una capsula di blu di Prussia (blu di Prussia e farina "
    "proteica) la riduce di un quarto. L'amifostina (livello 7) dimezza il danno di ogni radiazione per un'ora.",
    "# Biotecnologia",
    "DNA ricombinante (livello 7) sblocca il bioreattore (richiede un circuito integrato). Fermenta la cellulosa di "
    "tronchi, foglie o paglia in etanolo, il glucosio in PLA, e le proteine con olio vegetale nel vaccino a mRNA, sette "
    "giorni di gioco senza Infezione. La sua biomassa è un fertilizzante. Il PLA dà vasi da fiori e cornici, e serve da "
    "compost.",
    "Glifosato: formaldeide con acido fosforico nel reattore ad alta pressione. Campo pulito uccide tutte le piante "
    "intorno al blocco, colture comprese, tranne il grano dei semi tolleranti (otto semi di grano intorno a un "
    "glifosato). Il grano tollerante cresce anche un decimo più in fretta.",
]

MINING = [
    "# Mazze e badili",
    "Mazze da cava (livello 2) insegna la mazza (cinque lingotti intorno a un bastone, sopra un bastone) e il badile (tre "
    "lingotti e due bastoni), in acciaio e in ogni metallo da attrezzi successivo. Ognuno rompe un quadrato 3x3 sulla "
    "faccia che colpisci, a tre decimi della velocità, e si usura di un punto per blocco. Accovacciati per scavare un "
    "solo blocco. Si rompono solo i blocchi adatti all'attrezzo e non più duri di quello che hai mirato; nulla con un "
    "inventario e nessuna macchina si rompe mai; ciò che cade va dritto nel tuo inventario.",
    "# Brillamento",
    "Brillamento della roccia (livello 2): quattro polveri nere, carta e cordicella danno due cariche esplosive. Accesa "
    "con un acciarino o con la redstone, una carica rompe un blocco di pietra 3x3x3 e lo fa cadere tutto; terra, legno e "
    "macchine vengono risparmiati.",
    "# Trivelle e punte",
    "@4 Ogni trivella ha bisogno di una punta: tieni la punta nell'altra mano, accovacciati e usa la trivella. La punta "
    "decide cosa può scavare e a che velocità (acciaio al tungsteno 8,5, acciaio rapido 10, metallo duro 12, diamante "
    "13) e si usura di un punto per colpo. Incanta la punta, non la trivella; una punta montata presta alla trivella i "
    "suoi incantesimi, tutti tranne Ripristino. Accovacciati e usala senza punta nell'altra mano per cambiare modalità. "
    "Una trivella consuma 1,5 kJ per punto di durezza di ogni blocco: 2,25 per la pietra, 4,5 per l'ardesia profonda.",
    "Perforatrice da roccia (livello 4): il compressore d'aria, una macchina elettrica, riempie bombole d'acciaio di "
    "aria compressa, senza bisogno del potenziamento Compressore. La perforatrice pneumatica consuma una bombola ogni "
    "600 kJ (circa 400 blocchi di pietra) e restituisce quella vuota; scava un blocco, un tunnel 1x3 o 3x3, e ti "
    "rallenta mentre la tieni in mano.",
    "# Attrezzi a batteria",
    "Batteria al piombo-acido (livello 4): la batteria contiene 1500 kJ. Collega un banco di ricarica al filo di rame e "
    "usaci sopra la batteria; si carica fino a 5 kJ per tick.",
    "Il martello perforatore (livello 5) è la perforatrice da roccia a batteria, senza rallentarti. La motosega (livello "
    "5) abbatte un albero intero, fino a 128 tronchi, per 3 kJ a tronco.",
    "La trivella mineraria pesante (livello 6) aggiunge una modalità 5x5 e una modalità filone che segue un minerale "
    "fino a 32 blocchi; con una batteria al piombo-acido ti rallenta.",
    "La batteria agli ioni di litio (livello 7) contiene 6000 kJ, viene consumata per prima ed elimina quel "
    "rallentamento. Trivella da prospezione (livello 7): in modalità filone, usa la trivella mineraria pesante su un "
    "minerale per leggere la dimensione e gli elementi del filone prima di scavare.",
]

ISOTOPES = [
    "# Radiazione",
    "Gli oggetti radioattivi emettono una dose che si accumula mentre li porti o stai vicino a loro. Un elettroscopio, e "
    "più avanti un contatore Geiger, la misura. I muri di piombo, la cella calda e il reattore nucleare schermano ciò "
    "che contengono. Alcuni campioni decadono secondo l'orologio del gioco (un giorno di gioco vale per loro un giorno "
    "reale) e si trasformano nei loro discendenti nel tuo inventario.",
    "# Bagliore azzurro",
    "Gli emettitori più forti brillano di azzurro al buio, come la luce Cherenkov: il radio e i più forti nell'aria, e "
    "in acqua anche il polonio, l'americio, l'attinio e la pechblenda, con più intensità. Un reattore nucleare in "
    "funzione illumina la sua piscina, e anche gli acceleratori in funzione brillano. Il bagliore dipende da quanto è "
    "concentrata l'attività, non da quanto ne hai; l'uranio non brilla mai. Gli oggetti che brillano brillano anche nel "
    "loro slot dell'inventario.",
    "# Isotopi naturali",
    "La descrizione di ogni elemento stabile elenca i suoi isotopi naturali, dal più abbondante al meno abbondante.",
    "# Rapporti variabili",
    "Gli oggetti di uranio (U-235), plutonio (Pu-240), litio (Li-6), boro (B-10), calcio (Ca-48) e idrogeno (H-2) "
    "mostrano il proprio rapporto. Un oggetto con il rapporto naturale si impila con qualsiasi altro; uno arricchito o "
    "impoverito si impila solo con oggetti dello stesso rapporto. Le macchine mescolano i rapporti in base alla massa.",
    "- Il calutrone (Arricchimento con calutrone) separa uranio, litio o boro in una piccola frazione arricchita e in "
    "code impoverite. Rimetti dentro la frazione per arricchire ancora.",
    "- L'uranio metallico con il 3% di U-235 o più si sinterizza in pastiglie di combustibile per il reattore.",
    "- Il plutonio prodotto nel reattore è di qualità militare (6% di Pu-240); il plutonio del combustibile esaurito è "
    "di qualità da reattore.",
    "- Il litio con oltre il 90% di Li-6 negli slot di un reattore diventa trizio, che accende le luci al trizio per "
    "sempre.",
    "- Il boro con oltre il 90% di B-10 dà barre di controllo: con una nei suoi slot, un reattore smette di bruciare "
    "combustibile finché è pieno.",
    "- L'acqua pesante della cascata di arricchimento (alimentala con ghiaccio o neve) permette di bruciare barre di "
    "combustibile di uranio naturale.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Primi passi",
    "group": "Livello {n}",
    "research": "Ricerca",
    "machines": "Macchine",
    "reactions": "Reazioni",
    "recipe": " Ricetta: {r}.",
    "recipe_head": "Ricetta: {r}.",
    "projects": "# Progetti (★ = chiave)",
    "needs": " Richiede {x}.",
    "samples": " Campioni: {x}.",
    "unlocks": " Sblocca {x}.",
    "up_to": "{desc}. Fino a {c} °C",
    "electric": ", elettrica.",
    "fuel": ", a combustibile.",
    "amounts": "Quantità per 100 unità di carica.",
    "ores": "Minerali",
    "ores_head": "# Minerali",
    "ores_intro": "I minerali compaiono nei chunk appena esplorati. Tocco di velluto e Fortuna funzionano come sui "
                  "minerali normali.",
    "ore_line": "{name}: {desc}. Rilascia {lo}-{hi} {drop}; richiede un piccone di {tier}. ",
    "found": "Si trova {w}.",
    "where": "da y {lo} a {hi} {b}",
    "biomes": "in: {b}",
    "overworld": "nella Superficie",
    "nether": "nel Nether",
    "power": "Energia",
    "power_head": "# Energia",
    "power_intro": "Le macchine elettriche prendono corrente dai blocchi di energia adiacenti, o collegati con filo di "
                   "rame (fino a 64 blocchi di filo) o cavo isolato (quattro volte più lontano).",
    "power_line": "{name} (livello {lvl}, {project}): {desc}.",
    "isotopes": "Radiazione e isotopi",
    "containers": "Contenitori",
    "upgrades": "Potenziamenti",
    "farm": "Fattoria e grassi",
    "tar": "Catrame e rimedi",
    "industry": "Chimica industriale",
    "life": "Scienze della vita",
    "mining": "Attività mineraria",
    "crafting": "Fabbricazione",
    "craft_head": "# Fabbricazione",
    "craft_intro": "Le ricette senza forma aggiunte dalla mod (le ricette di macchine e blocchi sono nei capitoli di "
                   "ciascun livello).",
    "shaped_head": "# Ricette con forma (righe dall'alto in basso)",
    "or": " o ",
    "any": "qualsiasi {x}",
    "empty": "vuoto",
    "reagent": "(reagente)",
    "carbon": " + {c} di carbonio",
    "nothing": "non si conserva nulla",
    "slag": "; scoria: ",
    "vents": "; sfiata ",
    "temp": ". Da {a} a {b} °C.",
    "sep": "; ",
}

WORDS = {
    # pickaxe tiers
    "wood": "legno", "stone": "pietra", "iron": "ferro",
    # biomes and biome tags
    "mountain": "montagne", "jungle": "giungle", "savanna": "savane", "desert": "deserto",
    "badlands": "calanchi", "ocean": "oceani", "swamp": "paludi", "mangrove swamp": "paludi di mangrovie",
    "basalt deltas": "delta di basalto", "beach": "spiagge", "river": "fiumi", "dripstone caves": "caverne di speleotemi",
    # item tags
    "logs": "tronco", "logs that burn": "tronco combustibile", "oak logs": "tronco di quercia", "planks": "assi",
    "wool": "lana", "beds": "letto", "cable insulation": "isolamento dei cavi", "soap": "sapone",
    # upgrades unlocked by research
    "forced air": "aria forzata (mantice)", "steam blast": "soffiaggio a vapore",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["it_it"]
T["atlas"] = _ATLAS_TITLE["it_it"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["it_it"]
T.update(_TREES_T["it_it"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("it_it")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["it_it"]
T["compendium"] = _COMP_TITLE["it_it"]

# 3.5.0 Plant Compendium (tools/compendium_plants_it_it.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["it_it"]
T["plant_compendium"] = _PCOMP_TITLE["it_it"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_it_it.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["it_it"]
T["mineral_compendium"] = _MCOMP_TITLE["it_it"]
