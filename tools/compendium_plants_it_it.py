"""Compendio delle piante (3.5.0) in italiano. Vedi compendium_plants.py e compendium_i18n.py."""

TITLE = "Compendio delle piante"

GUIDE = [
    "# Il Compendio delle piante",
    "Un libro e un fiore piccolo qualsiasi fanno il Compendio delle piante; non serve alcuna ricerca. Funziona come il "
    "Compendio degli alberi: tienilo nella barra rapida e l'angolo in alto a sinistra indica il bioma e, quando già la "
    "conosci, la pianta che stai guardando. Fiori, erbe, felci, arbusti, colture, piante acquatiche e le piante delle "
    "grotte rigogliose ci sono tutti.",
    "Per annotare una pianta guardala per un attimo da non più di 8 blocchi, oppure coglila o raccoglila, con il libro "
    "nella barra rapida; le piante sott'acqua si annotano allo stesso modo. Usa il libro per leggere: i biomi a "
    "sinistra, le piante di ciascuno a destra.",
    "Le Note del Compendio copiano entrambi i libri in una volta: chi le legge ottiene tutti gli alberi, le piante "
    "e i biomi che contengono.",
    "Species Plantarum (livello 3): ogni pianta mostra il nome scientifico, la famiglia e dove cresce spontanea nel "
    "mondo reale.",
    "Geografia delle piante (livello 3): ogni bioma mostra il suo clima, in questo libro come nel Compendio degli "
    "alberi.",
    "La Lista rossa (livello 6): ogni pianta mostra quanto è minacciata in natura.",
]

UI = [
    "Compendio delle piante",
    "Pianta sconosciuta",
    "Nuova pianta nel tuo Compendio: %s",
    "%s piante su %s e %s biomi su %s annotati.",
    "Tieni questo libro nella barra rapida. Guarda una pianta per un attimo, oppure coglila o raccoglila, per "
    "annotarla; entra in un bioma per annotare il bioma. Scegli un bioma a sinistra.",
    "Piante",
    "Una pianta solo di questo mondo: nessuna specie reale le corrisponde.",
    "Compendio delle piante",
    "Nella barra rapida mostra il bioma e dà il nome alle piante che conosci; usalo per leggere di loro",
]

PLANTS = {
    "dandelion": ("Tarassaco", "Europa e Asia; oggi in tutti i continenti tranne l'Antartide",
                  "Il capolino giallo si chiude di notte e matura in una palla di semi che il vento porta per "
                  "chilometri. Tutto in lui è commestibile, e le sue foglie erano insalata di primavera molto prima "
                  "della lattuga."),
    "poppy": ("Papavero", "Europa, Nord Africa e Asia occidentale",
              "Il papavero dei campi segue l'aratro: i semi aspettano decenni nel suolo e germogliano quando la terra "
              "viene rivoltata, per questo coprì i campi di battaglia delle Fiandre dopo il 1914."),
    "blue_orchid": ("Orchidea blu", "Foreste collinari dell'Assam, del Myanmar e della Thailandia settentrionale",
                    "Una delle pochissime orchidee davvero blu. In natura si aggrappa ai tronchi nelle fresche foreste "
                    "di collina; i collezionisti hanno svuotato valli intere, e oggi il commercio di piante selvatiche "
                    "è vietato."),
    "allium": ("Allium", "Montagne di Iran, Afghanistan e Asia centrale",
               "Una cipolla ornamentale: una palla di centinaia di fiorellini viola in cima a un lungo stelo. Schiaccia "
               "una foglia e sa di cipolla, come i suoi parenti aglio e porro."),
    "azure_bluet": ("Houstonia", "Nord America orientale",
                    "Un minuscolo fiore a quattro petali che in primavera copre prati e giardini di azzurro pallido. "
                    "Così piccolo, appartiene alla famiglia del caffè."),
    "tulip": ("Tulipano", "Steppe montane della Turchia e dell'Asia centrale",
              "Arrivato dall'Impero ottomano nei Paesi Bassi nel Cinquecento; nel 1637 un solo bulbo poteva costare "
              "più di una casa, finché il mercato crollò. I petali screziati tanto ricercati erano causati da un "
              "virus."),
    "oxeye_daisy": ("Margherita", "Europa e Asia temperata",
                    "La margherita bianca dei prati e dei bordi delle strade. Ogni fiore è in realtà centinaia di "
                    "fiorellini: quelli gialli al centro, quelli bianchi tutt'intorno."),
    "cornflower": ("Fiordaliso", "Europa sudorientale e Medio Oriente, diffuso col grano in tutta Europa",
                   "Un tempo erbaccia di ogni campo di grano, è quasi scomparso dall'Europa quando gli agricoltori hanno "
                   "cominciato a pulire le sementi e a usare diserbanti. Il suo blu ha dato il nome a un colore."),
    "lily_of_the_valley": ("Mughetto", "Boschi freschi d'Europa e d'Asia",
                           "Campanelle bianche e profumate nei boschi ombrosi di primavera. Tutta la pianta è "
                           "velenosa: contiene glicosidi cardiaci come la digitale, che la medicina usa contro lo "
                           "scompenso cardiaco."),
    "sunflower": ("Girasole", "Nord America; coltivato in Messico circa 4.000 anni fa",
                  "I capolini giovani seguono il sole da est a ovest ogni giorno; il fiore aperto resta rivolto a est. "
                  "Dai suoi semi si spreme uno dei principali oli da cucina del mondo."),
    "lilac": ("Lillà", "Colline rocciose dei Balcani",
              "Un arbusto della famiglia dell'olivo con grappoli di fiori viola e profumati a fine primavera. "
              "Spontaneo solo sui pendii balcanici, i giardini lo hanno portato in tutto il mondo."),
    "rose_bush": ("Rosa", "Europa, Africa nordoccidentale e Asia occidentale",
                  "La rosa canina si arrampica sulle siepi con spine a uncino. I suoi cinorrodi rossi hanno più "
                  "vitamina C delle arance; nella Gran Bretagna in guerra i bambini li raccoglievano per farne "
                  "sciroppo."),
    "peony": ("Peonia", "Siberia, Mongolia e Cina settentrionale",
              "Grandi fiori rosa i cui boccioli trasudano zucchero che le formiche vengono a bere. Coltivata in Cina "
              "da più di mille anni, là è chiamata la regina dei fiori."),
    "pitcher_plant": ("Sarracenia", "Torbiere del Nord America orientale",
                      "Una pianta carnivora di suoli poveri e fradici: le foglie sono brocche d'acqua piovana dove gli "
                      "insetti annegano e vengono digeriti. In questo mondo nasce solo da semi antichi che gli annusatori "
                      "dissotterrano."),
    "grass": ("Erba", "Europa, Asia settentrionale e Nord America",
              "La fienarola dei prati, l'erba dei pascoli e dei prati all'inglese. Le graminacee crescono dalla base "
              "della foglia, non dalla punta, e così sopravvivono al pascolo e al taglio che ucciderebbero quasi ogni "
              "pianta."),
    "fern": ("Felce", "Quasi tutto il mondo, in ogni continente tranne l'Antartide",
             "La felce aquilina è più antica di qualunque fiore: non ha fiori né semi e si diffonde con spore e fusti "
             "che corrono sottoterra. Le sue fronde sono velenose per il bestiame e i cavalli."),
    "dead_bush": ("Arbusto secco", "Deserti degli Stati Uniti sudoccidentali e del Messico settentrionale",
                  "Non è morto, aspetta: l'arbusto di creosoto smette di crescere nella siccità e rinverdisce dopo la "
                  "pioggia. Un anello di suoi cloni nel Mojave, King Clone, ha circa 11.700 anni."),
    "sweet_berry_bush": ("Cespuglio di bacche dolci", "Foreste del nord e tundra di Europa, Asia e Nord America",
                         "Il mirtillo rosso, un basso arbusto sempreverde della foresta boreale. Le sue bacche rosse e "
                         "aspre si conservano tutto l'inverno senza guastarsi, grazie al loro acido benzoico."),
    "bamboo": ("Bambù", "Cina centrale e meridionale",
               "Un'erba gigante: un germoglio di bambù moso può crescere un metro in un giorno e raggiunge l'altezza "
               "piena in due mesi. Un intero boschetto fiorisce una volta ogni molti decenni, tutto insieme, e poi "
               "muore."),
    "cactus": ("Cactus", "Il deserto di Sonora, in Arizona e in Messico",
               "Il saguaro immagazzina acqua in un tronco a pieghe che si gonfia dopo la pioggia; uno adulto può "
               "contenerne più di una tonnellata. Vive fino a 150 anni e mette il primo braccio verso i 70."),
    "sugar_cane": ("Canna da zucchero", "Nuova Guinea, dove fu coltivata per la prima volta circa 8.000 anni fa",
                   "Un'erba gigante con fusti pieni di succo dolce; bollito, dà la maggior parte dello zucchero del "
                   "mondo. Le piantagioni delle Americhe furono costruite su di essa e sul lavoro forzato di persone "
                   "ridotte in schiavitù."),
    "moss": ("Muschio", "Terreni umidi di tutto il mondo temperato",
             "Il politrico, uno dei muschi più alti. I muschi non hanno radici, fiori né semi; bevono attraverso le "
             "foglie e hanno bisogno di un velo d'acqua perché i loro spermatozoi nuotino fino alle cellule uovo."),
    "wheat": ("Grano", "La Mezzaluna fertile, da tre erbe selvatiche incrociate circa 9.000 anni fa",
              "Il grano tenero porta i genomi interi di tre antenati selvatici. Fornisce un quinto delle calorie "
              "dell'umanità, e il suo glutine permette all'impasto di allungarsi e trattenere il gas del pane che "
              "lievita."),
    "carrot": ("Carota", "Spontanea in Europa e nell'Asia sudoccidentale; coltivata per la radice prima in "
               "Afghanistan",
               "Le prime carote da mangiare erano viola o gialle; quelle arancioni furono selezionate nei Paesi Bassi "
               "nel Seicento. L'arancione è betacarotene, che il corpo trasforma in vitamina A."),
    "potato": ("Patata", "Le Ande del Perù e della Bolivia",
               "I contadini andini ne coltivavano migliaia di varietà; l'Europa ne piantò solo poche, e quando arrivò la "
               "peronospora nel 1845 l'Irlanda perse un milione di persone per la carestia. Le patate verdi contengono "
               "il veleno solanina."),
    "beetroot": ("Barbabietola", "Coste del Mediterraneo e dell'Atlantico, come bietola marina selvatica",
                 "Barbabietola, bietola e barbabietola da zucchero sono una sola specie selezionata in tre modi. "
                 "Quella da zucchero, nata in Prussia nel Settecento, oggi dà un quinto dello zucchero del mondo."),
    "melon": ("Anguria", "Africa nordorientale",
              "L'anguria è acqua per circa nove decimi. I suoi semi furono lasciati nelle tombe egizie, e i viaggiatori "
              "del deserto la portavano come borraccia."),
    "pumpkin": ("Zucca", "Argentina e Uruguay",
                "Una pianta strisciante le cui zucche giganti hanno superato i 1.200 chili, i frutti più pesanti di "
                "qualunque pianta. I popoli delle Americhe coltivavano le zucche insieme al mais e ai fagioli."),
    "cocoa": ("Cacao", "L'alto bacino dell'Amazzonia",
              "Un piccolo albero all'ombra della foresta i cui frutti spuntano direttamente dal tronco. Maya e Aztechi "
              "bevevano i suoi semi tostati e macinati; il suo nome scientifico significa cibo degli dei."),
    "kelp": ("Kelp", "Coste fredde del Pacifico e dei mari australi",
             "Il kelp gigante non è una pianta ma un'alga bruna. Vesciche piene di gas tengono su le fronde, e può "
             "crescere mezzo metro al giorno formando foreste sottomarine piene di pesci e lontre marine."),
    "seagrass": ("Erba marina", "Coste basse dell'Atlantico settentrionale e del Pacifico settentrionale",
                 "Una pianta da fiore tornata in mare, che fiorisce perfino sott'acqua. Le sue praterie riparano i "
                 "pesci giovani e seppelliscono carbonio nel fango."),
    "lily_pad": ("Ninfea", "Laghi e stagni d'Europa, Nord Africa e Medio Oriente",
                 "La ninfea bianca mette radici nel fango e fa galleggiare le foglie su lunghi steli, i cui canali "
                 "d'aria portano ossigeno alle radici. Le sue foglie respirano dalla pagina superiore."),
    "torchflower": ("Fiore torcia", None,
                    "Un fiore antico, di prima che il mondo cominciasse; gli annusatori ne dissotterrano i semi dalla "
                    "sabbia delle rovine dei mari caldi. Sembra una fiamma ma non fa alcuna luce."),
    "pink_petals": ("Petali rosa", None,
                    "Tappeti di fiorellini rosa sotto i ciliegi, fino a quattro per blocco. Nessun fiore vero cresce "
                    "così: sembrano fiori di ciliegio caduti che hanno messo radici."),
    "spore_blossom": ("Fiore di spore", None,
                      "Un fiore appeso al soffitto delle grotte che lascia cadere una lenta pioggia di spore verdi. I "
                      "fiori fanno polline e semi, non spore; questo non ha bisogno di sole."),
    "dripleaf": ("Gocciafoglia", None,
                 "Foglie larghe su steli alti che si ribaltano un attimo dopo che qualcosa ci si posa, come una "
                 "botola. Cresce nell'argilla e nell'acqua delle grotte rigogliose."),
    "glow_berries": ("Bacche luminose", None,
                     "Rampicanti appesi alle volte delle grotte con bacche che brillano di luce propria. Lucciole e "
                     "alcuni funghi fanno luce, ma nessun frutto vero."),
    "glow_lichen": ("Lichene luminoso", None,
                    "Un lichene che riluce sulle pareti delle grotte. I licheni veri sono unioni di un fungo con "
                    "un'alga o un batterio; nessuno di loro brilla."),
    "vine": ("Rampicanti", None,
             "Liane frondose senza fiori né frutti che pendono dagli alberi della giungla e dalle querce della palude e "
             "crescono verso il basso. I rampicanti veri salgono verso la luce."),
}

BIOMES = {
    "river": "Un fiume: acqua dolce che corre verso il mare, con canne sulle rive e piante acquatiche nel letto.",
    "ocean": "Mare aperto temperato come l'Atlantico settentrionale: acqua fresca rimescolata dalle tempeste, con kelp "
             "ed erba marina vicino alle coste.",
    "cold_ocean": "Mare freddo come il Pacifico settentrionale: acqua scura e ricca di nutrienti, dove il kelp forma "
                  "foreste.",
    "lukewarm_ocean": "Mare mite come il Mediterraneo: acqua limpida e salata con praterie di erba marina sul fondo "
                      "sabbioso.",
    "warm_ocean": "Mare tropicale come i Caraibi: acqua calda e limpida su barriere coralline e praterie di erba "
                  "marina.",
}
