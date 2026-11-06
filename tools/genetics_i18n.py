"""Translations for tools/genetics.py (3.37.0): every English string of crop genetics stages 2 and 3, in the order of
crops_i18n.CODES (pt_br, fr_fr, es_es, nl_nl, it_it, de_de, sv_se, da_dk, ru_ru), and the Field Guide section
"Modern breeding" in each language (same paragraphs and headings as genetics.GUIDE)."""


def _same(s):
    return (s,) * 9


S = {
    "%s: %s": _same("%s: %s"),
    "%s: %s, %s%% %s": _same("%s: %s, %s%% %s"),
    "Bt": _same("Bt"),
    "DREB": _same("DREB"),
    "PCR": ("PCR", "PCR", "PCR", "PCR", "PCR", "PCR", "PCR", "PCR", "ПЦР"),
    "CRISPR": _same("CRISPR"),
    "Stature": ("Porte", "Taille", "Porte", "Lengte", "Statura", "Wuchshöhe", "Växthöjd", "Vækst", "Рост"),
    "semi-dwarf": ("semianão", "semi-nain", "semienano", "halfdwerg", "semi-nano", "halbzwergig", "halvdvärg",
                   "halvdværg", "полукарликовый"),
    "tall": ("alto", "haut", "alto", "hoog", "alto", "hochwüchsig", "hög", "høj", "высокий"),
    "glyphosate tolerant": ("tolerante ao glifosato", "tolérant au glyphosate", "tolerante al glifosato",
                            "glyfosaattolerant", "tollerante al glifosato", "glyphosattolerant", "glyfosattolerant",
                            "glyphosattolerant", "устойчив к глифосату"),
    "golden rice": ("arroz dourado", "riz doré", "arroz dorado", "gouden rijst", "riso dorato", "Goldener Reis",
                    "gyllene ris", "gylden ris", "золотой рис"),
    "F1 hybrid: vigorous, but its seeds do not keep it": (
        "Híbrido F1: vigoroso, mas suas sementes não mantêm isso", "Hybride F1 : vigoureux, mais ses graines ne le gardent pas",
        "Híbrido F1: vigoroso, pero sus semillas no lo conservan", "F1-hybride: groeikrachtig, maar zijn zaad houdt dat niet",
        "Ibrido F1: vigoroso, ma i suoi semi non lo conservano", "F1-Hybride: wüchsig, aber ihre Samen behalten das nicht",
        "F1-hybrid: livskraftig, men fröna behåller det inte", "F1-hybrid: kraftig, men frøene beholder det ikke",
        "Гибрид F1: мощный, но его семена этого не сохраняют"),
    "Pure line: breeds true": ("Linhagem pura: os filhos saem iguais", "Lignée pure : se reproduit à l'identique",
                               "Línea pura: los hijos salen iguales", "Zuivere lijn: teelt zuiver",
                               "Linea pura: i figli sono uguali", "Reine Linie: vererbt sich unverändert",
                               "Ren linje: avkomman blir lika", "Ren linje: afkommet bliver ens",
                               "Чистая линия: потомство одинаковое"),
    "Mixed line: its seeds will vary": ("Linhagem mista: suas sementes vão variar", "Lignée mêlée : ses graines varieront",
                                        "Línea mixta: sus semillas variarán", "Gemengde lijn: zijn zaad zal verschillen",
                                        "Linea mista: i suoi semi varieranno", "Gemischte Linie: ihre Samen werden variieren",
                                        "Blandad linje: fröna kommer att variera", "Blandet linje: frøene vil variere",
                                        "Смешанная линия: семена будут разными"),
    "Inoculated: its roots fix nitrogen": (
        "Inoculada: suas raízes fixam nitrogênio", "Inoculée : ses racines fixent l'azote",
        "Inoculada: sus raíces fijan nitrógeno", "Geënt: zijn wortels binden stikstof",
        "Inoculata: le sue radici fissano l'azoto", "Geimpft: ihre Wurzeln binden Stickstoff",
        "Ympad: rötterna binder kväve", "Podet: rødderne binder kvælstof",
        "Инокулировано: корни связывают азот"),
    "You do not know how traits are inherited yet (Mendel's Laws, level 4)": (
        "Você ainda não sabe como as características são herdadas (Leis de Mendel, nível 4)",
        "Tu ne sais pas encore comment les caractères se transmettent (Lois de Mendel, niveau 4)",
        "Aún no sabes cómo se heredan los rasgos (Leyes de Mendel, nivel 4)",
        "Je weet nog niet hoe eigenschappen overerven (Wetten van Mendel, niveau 4)",
        "Non sai ancora come si ereditano i caratteri (Leggi di Mendel, livello 4)",
        "Du weißt noch nicht, wie Merkmale vererbt werden (Mendelsche Regeln, Stufe 4)",
        "Du vet ännu inte hur egenskaper ärvs (Mendels lagar, nivå 4)",
        "Du ved endnu ikke, hvordan egenskaber nedarves (Mendels love, niveau 4)",
        "Ты ещё не знаешь, как наследуются признаки (Законы Менделя, уровень 4)"),
    "Use the notebook on seeds held in your other hand": (
        "Use o caderno com sementes na outra mão", "Utilise le carnet avec des graines dans l'autre main",
        "Usa el cuaderno con semillas en la otra mano", "Gebruik het schrift met zaad in je andere hand",
        "Usa il quaderno con dei semi nell'altra mano", "Benutze das Heft mit Samen in der anderen Hand",
        "Använd anteckningsboken med frön i andra handen", "Brug notesbogen med frø i den anden hånd",
        "Используй тетрадь, держа семена в другой руке"),
    "First parent noted: %s. Now use it on the other parent's seeds": (
        "Primeiro genitor anotado: %s. Agora use nas sementes do outro genitor",
        "Premier parent noté : %s. Utilise-le maintenant sur les graines de l'autre parent",
        "Primer progenitor anotado: %s. Ahora úsalo con las semillas del otro progenitor",
        "Eerste ouder genoteerd: %s. Gebruik het nu op het zaad van de andere ouder",
        "Primo genitore annotato: %s. Ora usalo sui semi dell'altro genitore",
        "Erster Elternteil notiert: %s. Jetzt auf die Samen des anderen Elternteils anwenden",
        "Första föräldern antecknad: %s. Använd den nu på den andra förälderns frön",
        "Første forælder noteret: %s. Brug den nu på den anden forælders frø",
        "Первый родитель записан: %s. Теперь примени к семенам второго родителя"),
    "These are not the same kind of plant: they will not cross": (
        "Não são o mesmo tipo de planta: não cruzam", "Ce ne sont pas les mêmes plantes : elles ne se croiseront pas",
        "No son la misma clase de planta: no se cruzarán", "Dit is niet dezelfde soort plant: ze kruisen niet",
        "Non sono la stessa pianta: non si incroceranno", "Das ist nicht dieselbe Pflanzenart: sie kreuzen sich nicht",
        "Det är inte samma sorts växt: de korsas inte", "Det er ikke samme slags plante: de krydses ikke",
        "Это растения разных видов: они не скрещиваются"),
    "Cross with %s:": ("Cruzamento com %s:", "Croisement avec %s :", "Cruce con %s:", "Kruising met %s:",
                       "Incrocio con %s:", "Kreuzung mit %s:", "Korsning med %s:", "Krydsning med %s:", "Скрещивание с %s:"),
    "You do not know about root bacteria yet (Nitrogen Fixation, level 4)": (
        "Você ainda não conhece as bactérias das raízes (Fixação de nitrogênio, nível 4)",
        "Tu ne connais pas encore les bactéries des racines (Fixation de l'azote, niveau 4)",
        "Aún no conoces las bacterias de las raíces (Fijación de nitrógeno, nivel 4)",
        "Je kent de wortelbacteriën nog niet (Stikstofbinding, niveau 4)",
        "Non conosci ancora i batteri delle radici (Fissazione dell'azoto, livello 4)",
        "Du kennst die Wurzelbakterien noch nicht (Stickstofffixierung, Stufe 4)",
        "Du känner ännu inte till rotbakterierna (Kvävefixering, nivå 4)",
        "Du kender endnu ikke rodbakterierne (Kvælstoffiksering, niveau 4)",
        "Ты ещё не знаешь о клубеньковых бактериях (Азотфиксация, уровень 4)"),
    "Only soybeans, beans and alfalfa take rhizobia": (
        "Só soja, feijão e alfafa aceitam rizóbios", "Seuls le soja, le haricot et la luzerne prennent les rhizobiums",
        "Solo la soja, la judía y la alfalfa aceptan rizobios", "Alleen soja, bonen en luzerne nemen rhizobia op",
        "Solo soia, fagioli ed erba medica accolgono i rizobi", "Nur Soja, Bohnen und Luzerne nehmen Rhizobien an",
        "Bara sojabönor, bönor och lusern tar emot rhizobier", "Kun sojabønner, bønner og lucerne tager rhizobier",
        "Ризобии принимают только соя, фасоль и люцерна"),
    "Inoculated: harvested ripe, it leaves the ground fertilized": (
        "Inoculada: colhida madura, deixa a terra adubada", "Inoculée : récoltée mûre, elle laisse le sol fertilisé",
        "Inoculada: cosechada madura, deja la tierra abonada", "Geënt: rijp geoogst laat hij de grond bemest achter",
        "Inoculata: raccolta matura, lascia il terreno concimato", "Geimpft: reif geerntet hinterlässt sie gedüngten Boden",
        "Ympad: skördad mogen lämnar den jorden gödslad", "Podet: høstet moden efterlader den jorden gødet",
        "Инокулировано: собранное спелым, оставит землю удобренной"),
    "You do not know how to put a gene into a plant yet (Transgenesis, level 7)": (
        "Você ainda não sabe pôr um gene numa planta (Transgênese, nível 7)",
        "Tu ne sais pas encore introduire un gène dans une plante (Transgenèse, niveau 7)",
        "Aún no sabes introducir un gen en una planta (Transgénesis, nivel 7)",
        "Je weet nog niet hoe je een gen in een plant brengt (Transgenese, niveau 7)",
        "Non sai ancora inserire un gene in una pianta (Transgenesi, livello 7)",
        "Du weißt noch nicht, wie man ein Gen in eine Pflanze bringt (Transgenese, Stufe 7)",
        "Du vet ännu inte hur man för in en gen i en växt (Transgenes, nivå 7)",
        "Du ved endnu ikke, hvordan man sætter et gen ind i en plante (Transgenese, niveau 7)",
        "Ты ещё не умеешь вводить ген в растение (Трансгенез, уровень 7)"),
    "Hold a gene construct in your other hand": (
        "Segure um construto gênico na outra mão", "Tiens une construction génique dans l'autre main",
        "Sostén una construcción génica en la otra mano", "Houd een genconstruct in je andere hand",
        "Tieni un costrutto genico nell'altra mano", "Halte ein Genkonstrukt in der anderen Hand",
        "Håll en genkonstruktion i andra handen", "Hold et genkonstrukt i den anden hånd",
        "Держи генную конструкцию в другой руке"),
    "The golden rice gene works only in rice": (
        "O gene do arroz dourado só funciona no arroz", "Le gène du riz doré ne marche que dans le riz",
        "El gen del arroz dorado solo funciona en el arroz", "Het gouden-rijstgen werkt alleen in rijst",
        "Il gene del riso dorato funziona solo nel riso", "Das Gen des Goldenen Reises wirkt nur in Reis",
        "Genen för gyllene ris fungerar bara i ris", "Genet for gylden ris virker kun i ris",
        "Ген золотого риса работает только в рисе"),
    "The %s took": ("O %s pegou", "Le %s a pris", "El %s prendió", "Het %s is aangeslagen", "Il %s ha attecchito",
                    "Das %s hat angeschlagen", "%s tog", "%s tog", "%s прижился"),
    "Missed: the gene did not take": ("Errou: o gene não pegou", "Raté : le gène n'a pas pris",
                                      "Fallo: el gen no prendió", "Mis: het gen sloeg niet aan",
                                      "Mancato: il gene non ha attecchito", "Daneben: das Gen hat nicht angeschlagen",
                                      "Miss: genen tog inte", "Forbi: genet tog ikke", "Промах: ген не прижился"),
    "You do not know how to edit genes yet (CRISPR, level 7)": (
        "Você ainda não sabe editar genes (CRISPR, nível 7)", "Tu ne sais pas encore éditer les gènes (CRISPR, niveau 7)",
        "Aún no sabes editar genes (CRISPR, nivel 7)", "Je weet nog niet hoe je genen bewerkt (CRISPR, niveau 7)",
        "Non sai ancora modificare i geni (CRISPR, livello 7)", "Du weißt noch nicht, wie man Gene editiert (CRISPR, Stufe 7)",
        "Du vet ännu inte hur man redigerar gener (CRISPR, nivå 7)",
        "Du ved endnu ikke, hvordan man redigerer gener (CRISPR, niveau 7)",
        "Ты ещё не умеешь редактировать гены (CRISPR, уровень 7)"),
    "Only wheat and rice have the Stature gene": (
        "Só trigo e arroz têm o gene do Porte", "Seuls le blé et le riz ont le gène de la Taille",
        "Solo el trigo y el arroz tienen el gen del Porte", "Alleen tarwe en rijst hebben het Lengte-gen",
        "Solo grano e riso hanno il gene della Statura", "Nur Weizen und Reis haben das Wuchshöhen-Gen",
        "Bara vete och ris har genen för Växthöjd", "Kun hvede og ris har genet for Vækst",
        "Ген роста есть только у пшеницы и риса"),
    "%s edited: %s": ("%s editado: %s", "%s édité : %s", "%s editado: %s", "%s bewerkt: %s", "%s modificato: %s",
                      "%s editiert: %s", "%s redigerad: %s", "%s redigeret: %s", "%s отредактирован: %s"),
    "Target gene: %s": ("Gene-alvo: %s", "Gène visé : %s", "Gen objetivo: %s", "Doelgen: %s", "Gene bersaglio: %s",
                        "Zielgen: %s", "Målgen: %s", "Målgen: %s", "Целевой ген: %s"),
    "Silo: %s waiting, %s silage ready": (
        "Silo: %s esperando, %s de silagem pronta", "Silo : %s en attente, %s d'ensilage prêt",
        "Silo: %s esperando, %s de ensilado listo", "Silo: %s wachtend, %s kuilvoer klaar",
        "Silo: %s in attesa, %s di insilato pronto", "Silo: %s wartend, %s Silage fertig",
        "Silo: %s väntar, %s ensilage klar", "Silo: %s venter, %s ensilage klar",
        "Силос: %s ждёт, %s силоса готово"),
    "The Silo takes maize, sorghum or alfalfa": (
        "O Silo aceita milho, sorgo ou alfafa", "Le Silo prend du maïs, du sorgho ou de la luzerne",
        "El Silo acepta maíz, sorgo o alfalfa", "De Silo neemt maïs, sorghum of luzerne",
        "Il Silo accetta mais, sorgo o erba medica", "Das Silo nimmt Mais, Sorghum oder Luzerne",
        "Silon tar majs, durra eller lusern", "Siloen tager majs, durra eller lucerne",
        "В силосную башню кладут кукурузу, сорго или люцерну"),
    "Seed Irradiator: %s seeds waiting, %s irradiated": (
        "Irradiador de sementes: %s sementes esperando, %s irradiadas",
        "Irradiateur de graines : %s graines en attente, %s irradiées",
        "Irradiador de semillas: %s semillas esperando, %s irradiadas",
        "Zaadbestraler: %s zaden wachtend, %s bestraald", "Irradiatore di semi: %s semi in attesa, %s irradiati",
        "Samenbestrahler: %s Samen wartend, %s bestrahlt", "Frö-bestrålare: %s frön väntar, %s bestrålade",
        "Frøbestråler: %s frø venter, %s bestrålet", "Облучатель семян: %s семян ждёт, %s облучено"),
    "Seed Irradiator: no power (40 kJ a seed); %s waiting, %s done": (
        "Irradiador de sementes: sem energia (40 kJ por semente); %s esperando, %s prontas",
        "Irradiateur de graines : pas d'énergie (40 kJ par graine) ; %s en attente, %s faites",
        "Irradiador de semillas: sin energía (40 kJ por semilla); %s esperando, %s hechas",
        "Zaadbestraler: geen stroom (40 kJ per zaad); %s wachtend, %s klaar",
        "Irradiatore di semi: senza energia (40 kJ a seme); %s in attesa, %s fatti",
        "Samenbestrahler: kein Strom (40 kJ je Samen); %s wartend, %s fertig",
        "Frö-bestrålare: ingen kraft (40 kJ per frö); %s väntar, %s klara",
        "Frøbestråler: ingen strøm (40 kJ pr. frø); %s venter, %s færdige",
        "Облучатель семян: нет энергии (40 кДж на семя); %s ждёт, %s готово"),
    "The Seed Irradiator takes seeds of one kind at a time": (
        "O Irradiador de sementes aceita sementes de um tipo por vez",
        "L'Irradiateur de graines prend des graines d'une seule sorte à la fois",
        "El Irradiador de semillas acepta semillas de una sola clase a la vez",
        "De Zaadbestraler neemt zaad van één soort tegelijk", "L'Irradiatore di semi accetta semi di un tipo alla volta",
        "Der Samenbestrahler nimmt Samen nur einer Sorte auf einmal", "Frö-bestrålaren tar frön av en sort i taget",
        "Frøbestråleren tager frø af én slags ad gangen", "Облучатель принимает семена только одного вида за раз"),
    "Thermocycler: %s seeds waiting, %s sorted": (
        "Termociclador: %s sementes esperando, %s separadas", "Thermocycleur : %s graines en attente, %s triées",
        "Termociclador: %s semillas esperando, %s clasificadas", "Thermocycler: %s zaden wachtend, %s gesorteerd",
        "Termociclatore: %s semi in attesa, %s smistati", "Thermocycler: %s Samen wartend, %s sortiert",
        "Termocykler: %s frön väntar, %s sorterade", "Termocykler: %s frø venter, %s sorteret",
        "Термоциклер: %s семян ждёт, %s рассортировано"),
    "Thermocycler: no power (10 kJ a seed); %s waiting, %s sorted": (
        "Termociclador: sem energia (10 kJ por semente); %s esperando, %s separadas",
        "Thermocycleur : pas d'énergie (10 kJ par graine) ; %s en attente, %s triées",
        "Termociclador: sin energía (10 kJ por semilla); %s esperando, %s clasificadas",
        "Thermocycler: geen stroom (10 kJ per zaad); %s wachtend, %s gesorteerd",
        "Termociclatore: senza energia (10 kJ a seme); %s in attesa, %s smistati",
        "Thermocycler: kein Strom (10 kJ je Samen); %s wartend, %s sortiert",
        "Termocykler: ingen kraft (10 kJ per frö); %s väntar, %s sorterade",
        "Termocykler: ingen strøm (10 kJ pr. frø); %s venter, %s sorteret",
        "Термоциклер: нет энергии (10 кДж на семя); %s ждёт, %s рассортировано"),
    "Thermocycler: no marker; sneak-use a seed on it to set one": (
        "Termociclador: sem marcador; use uma semente nele agachado para definir",
        "Thermocycleur : pas de marqueur ; utilise une graine dessus accroupi pour en définir un",
        "Termociclador: sin marcador; usa una semilla en él agachado para fijarlo",
        "Thermocycler: geen marker; gebruik er gebukt een zaad op om er een te kiezen",
        "Termociclatore: nessun marcatore; usa un seme su di esso da accovacciato per impostarlo",
        "Thermocycler: kein Marker; benutze schleichend einen Samen darauf, um ihn festzulegen",
        "Termocykler: ingen markör; använd ett frö på den smygande för att välja en",
        "Termocykler: ingen markør; brug et frø på den sneget for at vælge en",
        "Термоциклер: нет маркера; примени семя, крадучись, чтобы задать его"),
    "The Thermocycler takes seeds of one kind at a time": (
        "O Termociclador aceita sementes de um tipo por vez", "Le Thermocycleur prend des graines d'une seule sorte à la fois",
        "El Termociclador acepta semillas de una sola clase a la vez", "De Thermocycler neemt zaad van één soort tegelijk",
        "Il Termociclatore accetta semi di un tipo alla volta", "Der Thermocycler nimmt Samen nur einer Sorte auf einmal",
        "Termocyklern tar frön av en sort i taget", "Termocykleren tager frø af én slags ad gangen",
        "Термоциклер принимает семена только одного вида за раз"),
    "Marker set: %s": ("Marcador definido: %s", "Marqueur défini : %s", "Marcador fijado: %s", "Marker gekozen: %s",
                       "Marcatore impostato: %s", "Marker festgelegt: %s", "Markör vald: %s", "Markør valgt: %s",
                       "Маркер задан: %s"),
    "DNA Sequencer: needs %s kJ of power": (
        "Sequenciador de DNA: precisa de %s kJ de energia", "Séquenceur d'ADN : il faut %s kJ d'énergie",
        "Secuenciador de ADN: necesita %s kJ de energía", "DNA-sequencer: heeft %s kJ stroom nodig",
        "Sequenziatore di DNA: servono %s kJ di energia", "DNA-Sequenzierer: braucht %s kJ Strom",
        "DNA-sekvenserare: behöver %s kJ kraft", "DNA-sekventeringsmaskine: kræver %s kJ strøm",
        "ДНК-секвенатор: нужно %s кДж энергии"),
    "Crossing Notebook": ("Caderno de cruzamentos", "Carnet de croisements", "Cuaderno de cruces", "Kruisingsschrift",
                          "Quaderno degli incroci", "Kreuzungsheft", "Korsningsbok", "Krydsningsbog",
                          "Тетрадь скрещиваний"),
    "Mendel's Laws: use it on seeds, then on other seeds: it works out what share of the seeds of that cross will show each trait": (
        "Leis de Mendel: use em sementes, depois em outras: calcula que parte das sementes desse cruzamento mostrará cada característica",
        "Lois de Mendel : utilise-le sur des graines, puis sur d'autres : il calcule quelle part des graines de ce croisement montrera chaque caractère",
        "Leyes de Mendel: úsalo con unas semillas y luego con otras: calcula qué parte de las semillas de ese cruce mostrará cada rasgo",
        "Wetten van Mendel: gebruik het op zaad en dan op ander zaad: het berekent welk deel van het zaad van die kruising elke eigenschap toont",
        "Leggi di Mendel: usalo su dei semi, poi su altri: calcola quale parte dei semi di quell'incrocio mostrerà ogni carattere",
        "Mendelsche Regeln: auf Samen anwenden, dann auf andere: es berechnet, welcher Anteil der Samen dieser Kreuzung jedes Merkmal zeigt",
        "Mendels lagar: använd den på frön, sedan på andra frön: den räknar ut hur stor del av fröna från korsningen som visar varje egenskap",
        "Mendels love: brug den på frø og så på andre frø: den regner ud, hvor stor en del af frøene fra krydsningen der viser hver egenskab",
        "Законы Менделя: примени к семенам, затем к другим: тетрадь рассчитает, какая доля семян от скрещивания покажет каждый признак"),
    "Silage": ("Silagem", "Ensilage", "Ensilado", "Kuilvoer", "Insilato", "Silage", "Ensilage", "Ensilage", "Силос"),
    "Inoculant": ("Inoculante", "Inoculum", "Inoculante", "Entstof", "Inoculante", "Impfmittel", "Ympmedel", "Podemiddel",
                  "Инокулянт"),
    "Nitrogen Fixation: use it on soybeans, beans or alfalfa: harvested ripe, they leave their farmland strongly fertilized": (
        "Fixação de nitrogênio: use em soja, feijão ou alfafa: colhidos maduros, deixam a terra arada bem adubada",
        "Fixation de l'azote : utilise-le sur le soja, le haricot ou la luzerne : récoltés mûrs, ils laissent leur terre bien fertilisée",
        "Fijación de nitrógeno: úsalo en soja, judías o alfalfa: cosechadas maduras, dejan su tierra bien abonada",
        "Stikstofbinding: gebruik het op soja, bonen of luzerne: rijp geoogst laten ze hun akker sterk bemest achter",
        "Fissazione dell'azoto: usalo su soia, fagioli o erba medica: raccolti maturi, lasciano il campo ben concimato",
        "Stickstofffixierung: auf Soja, Bohnen oder Luzerne anwenden: reif geerntet hinterlassen sie ihr Feld stark gedüngt",
        "Kvävefixering: använd den på sojabönor, bönor eller lusern: skördade mogna lämnar de åkern väl gödslad",
        "Kvælstoffiksering: brug den på sojabønner, bønner eller lucerne: høstet modne efterlader de marken godt gødet",
        "Азотфиксация: примени к сое, фасоли или люцерне: собранные спелыми, они оставят пашню хорошо удобренной"),
    "Balanced Feed": ("Ração balanceada", "Aliment équilibré", "Pienso equilibrado", "Uitgebalanceerd voer",
                      "Mangime bilanciato", "Mischfutter", "Balanserat foder", "Balanceret foder",
                      "Сбалансированный корм"),
    "Cobalt-60 Source": ("Fonte de cobalto-60", "Source de cobalt 60", "Fuente de cobalto-60", "Kobalt-60-bron",
                         "Sorgente di cobalto-60", "Cobalt-60-Quelle", "Kobolt-60-källa", "Kobolt-60-kilde",
                         "Источник кобальта-60"),
    "Gene Gun": ("Pistola de genes", "Canon à gènes", "Pistola génica", "Genkanon", "Pistola genica", "Genkanone",
                 "Genpistol", "Genkanon", "Генная пушка"),
    "Transgenesis: hold a gene construct in your other hand and use the gun on a crop: half the shots take": (
        "Transgênese: segure um construto gênico na outra mão e use a pistola numa lavoura: metade dos tiros pega",
        "Transgenèse : tiens une construction génique dans l'autre main et utilise le canon sur une culture : la moitié des tirs prend",
        "Transgénesis: sostén una construcción génica en la otra mano y usa la pistola en un cultivo: la mitad de los disparos prende",
        "Transgenese: houd een genconstruct in je andere hand en gebruik het kanon op een gewas: de helft van de schoten slaat aan",
        "Transgenesi: tieni un costrutto genico nell'altra mano e usa la pistola su una coltura: metà dei colpi attecchisce",
        "Transgenese: halte ein Genkonstrukt in der anderen Hand und benutze die Kanone an einer Feldfrucht: die Hälfte der Schüsse schlägt an",
        "Transgenes: håll en genkonstruktion i andra handen och använd pistolen på en gröda: hälften av skotten tar",
        "Transgenese: hold et genkonstrukt i den anden hånd og brug kanonen på en afgrøde: halvdelen af skuddene tager",
        "Трансгенез: держи генную конструкцию в другой руке и примени пушку к посеву: прививается половина выстрелов"),
    "CRISPR Kit": ("Kit CRISPR", "Kit CRISPR", "Kit CRISPR", "CRISPR-kit", "Kit CRISPR", "CRISPR-Kit", "CRISPR-kit",
                   "CRISPR-sæt", "Набор CRISPR"),
    "CRISPR: use it on a crop to write the useful form of a gene into both its copies; sneak-use it to choose the gene": (
        "CRISPR: use numa lavoura para escrever a forma útil de um gene nas duas cópias; use agachado para escolher o gene",
        "CRISPR : utilise-le sur une culture pour écrire la forme utile d'un gène dans ses deux copies ; accroupi, il choisit le gène",
        "CRISPR: úsalo en un cultivo para escribir la forma útil de un gen en sus dos copias; agachado eliges el gen",
        "CRISPR: gebruik het op een gewas om de nuttige vorm van een gen in beide kopieën te schrijven; gebukt kies je het gen",
        "CRISPR: usalo su una coltura per scrivere la forma utile di un gene in entrambe le copie; da accovacciato scegli il gene",
        "CRISPR: an einer Feldfrucht benutzen, um die nützliche Form eines Gens in beide Kopien zu schreiben; schleichend wählst du das Gen",
        "CRISPR: använd den på en gröda för att skriva in den nyttiga formen av en gen i båda kopiorna; smygande väljer du gen",
        "CRISPR: brug det på en afgrøde for at skrive den nyttige form af et gen ind i begge kopier; sneget vælger du genet",
        "CRISPR: примени к посеву, чтобы записать полезную форму гена в обе копии; крадучись выбираешь ген"),
    "Bt Gene": ("Gene Bt", "Gène Bt", "Gen Bt", "Bt-gen", "Gene Bt", "Bt-Gen", "Bt-gen", "Bt-gen", "Ген Bt"),
    "Gene Gun: caterpillars never take the plant": (
        "Pistola de genes: a lagarta nunca pega a planta", "Canon à gènes : les chenilles n'attaquent jamais la plante",
        "Pistola génica: las orugas nunca atacan la planta", "Genkanon: rupsen tasten de plant nooit aan",
        "Pistola genica: i bruchi non attaccano mai la pianta", "Genkanone: Raupen befallen die Pflanze nie",
        "Genpistol: larver angriper aldrig växten", "Genkanon: larver angriber aldrig planten",
        "Генная пушка: гусеницы никогда не нападают на растение"),
    "EPSPS Gene": ("Gene EPSPS", "Gène EPSPS", "Gen EPSPS", "EPSPS-gen", "Gene EPSPS", "EPSPS-Gen", "EPSPS-gen",
                   "EPSPS-gen", "Ген EPSPS"),
    "Gene Gun: Field Clear spares the plant": (
        "Pistola de genes: o Limpa-Campo poupa a planta", "Canon à gènes : Champ net épargne la plante",
        "Pistola génica: Campo limpio respeta la planta", "Genkanon: Veld vrij spaart de plant",
        "Pistola genica: Campo pulito risparmia la pianta", "Genkanone: Feldräumer verschont die Pflanze",
        "Genpistol: Fältrensare skonar växten", "Genkanon: Markrydder skåner planten",
        "Генная пушка: Расчистка поля щадит растение"),
    "Golden Rice Gene": ("Gene do arroz dourado", "Gène du riz doré", "Gen del arroz dorado", "Gouden-rijstgen",
                         "Gene del riso dorato", "Gen des Goldenen Reises", "Gen för gyllene ris", "Gen for gylden ris",
                         "Ген золотого риса"),
    "Gene Gun, rice only: its grains carry vitamin A and can be eaten raw": (
        "Pistola de genes, só arroz: os grãos têm vitamina A e podem ser comidos crus",
        "Canon à gènes, riz seulement : ses grains contiennent de la vitamine A et se mangent crus",
        "Pistola génica, solo arroz: sus granos llevan vitamina A y se pueden comer crudos",
        "Genkanon, alleen rijst: de korrels bevatten vitamine A en zijn rauw eetbaar",
        "Pistola genica, solo riso: i chicchi contengono vitamina A e si mangiano crudi",
        "Genkanone, nur Reis: die Körner enthalten Vitamin A und sind roh essbar",
        "Genpistol, bara ris: kornen har A-vitamin och kan ätas råa",
        "Genkanon, kun ris: kernerne har A-vitamin og kan spises rå",
        "Генная пушка, только рис: зёрна содержат витамин A и съедобны сырыми"),
    "DREB Gene": ("Gene DREB", "Gène DREB", "Gen DREB", "DREB-gen", "Gene DREB", "DREB-Gen", "DREB-gen", "DREB-gen",
                  "Ген DREB"),
    "Gene Gun: the plant tolerates drought": (
        "Pistola de genes: a planta tolera a seca", "Canon à gènes : la plante tolère la sécheresse",
        "Pistola génica: la planta tolera la sequía", "Genkanon: de plant verdraagt droogte",
        "Pistola genica: la pianta tollera la siccità", "Genkanone: die Pflanze verträgt Trockenheit",
        "Genpistol: växten tål torka", "Genkanon: planten tåler tørke", "Генная пушка: растение переносит засуху"),
    "Silo": ("Silo", "Silo", "Silo", "Silo", "Silo", "Silo", "Silo", "Silo", "Силосная башня"),
    "Silage: put maize, sorghum or alfalfa in by hand or from a hopper; one silage every 5 seconds comes out below": (
        "Silagem: ponha milho, sorgo ou alfafa à mão ou por um funil; sai uma silagem a cada 5 segundos por baixo",
        "Ensilage : mets du maïs, du sorgho ou de la luzerne à la main ou par un entonnoir ; un ensilage sort dessous toutes les 5 secondes",
        "Ensilado: echa maíz, sorgo o alfalfa a mano o con una tolva; sale un ensilado cada 5 segundos por abajo",
        "Kuilvoer: doe er met de hand of via een trechter maïs, sorghum of luzerne in; elke 5 seconden komt er onderaan kuilvoer uit",
        "Insilato: metti mais, sorgo o erba medica a mano o con una tramoggia; ogni 5 secondi esce un insilato sotto",
        "Silage: Mais, Sorghum oder Luzerne von Hand oder per Trichter einfüllen; alle 5 Sekunden kommt unten eine Silage heraus",
        "Ensilage: lägg i majs, durra eller lusern för hand eller med en tratt; var 5:e sekund kommer en ensilage ut nedtill",
        "Ensilage: kom majs, durra eller lucerne i med hånden eller fra en tragt; hvert 5. sekund kommer en ensilage ud forneden",
        "Силос: закладывай кукурузу, сорго или люцерну вручную или воронкой; каждые 5 секунд снизу выходит силос"),
    "Seed Irradiator": ("Irradiador de sementes", "Irradiateur de graines", "Irradiador de semillas", "Zaadbestraler",
                        "Irradiatore di semi", "Samenbestrahler", "Frö-bestrålare", "Frøbestråler", "Облучатель семян"),
    "X-ray Mutagenesis: X-rays seeds put in it (40 kJ a seed): 3 in 10 die, the rest mutate 20 times as often": (
        "Mutagênese por raios X: irradia as sementes postas nele (40 kJ por semente): 3 em 10 morrem, as outras mutam 20 vezes mais",
        "Mutagenèse par rayons X : irradie les graines qu'on y met (40 kJ par graine) : 3 sur 10 meurent, les autres mutent 20 fois plus",
        "Mutagénesis por rayos X: irradia las semillas que se le echan (40 kJ por semilla): 3 de cada 10 mueren, las demás mutan 20 veces más",
        "Röntgenmutagenese: bestraalt het zaad dat erin gaat (40 kJ per zaad): 3 op 10 sterven, de rest muteert 20 keer zo vaak",
        "Mutagenesi con raggi X: irradia i semi messi dentro (40 kJ a seme): 3 su 10 muoiono, gli altri mutano 20 volte più spesso",
        "Röntgenmutagenese: bestrahlt eingefüllte Samen (40 kJ je Samen): 3 von 10 sterben, die übrigen mutieren 20-mal so oft",
        "Röntgenmutagenes: bestrålar frön som läggs i (40 kJ per frö): 3 av 10 dör, resten muterar 20 gånger så ofta",
        "Røntgenmutagenese: bestråler frø, der kommes i (40 kJ pr. frø): 3 ud af 10 dør, resten muterer 20 gange så ofte",
        "Рентгеновский мутагенез: облучает заложенные семена (40 кДж на семя): 3 из 10 гибнут, остальные мутируют в 20 раз чаще"),
    "Gamma Garden": ("Jardim gama", "Jardin gamma", "Jardín gamma", "Gammatuin", "Giardino gamma", "Gammagarten",
                     "Gammaträdgård", "Gammahave", "Гамма-поле"),
    "Gamma Garden: crops ripening within 6 blocks give seeds that mutate 20 times as often. Radioactive": (
        "Jardim gama: lavouras que amadurecem a até 6 blocos dão sementes que mutam 20 vezes mais. Radioativo",
        "Jardin gamma : les cultures qui mûrissent à 6 blocs ou moins donnent des graines qui mutent 20 fois plus. Radioactif",
        "Jardín gamma: los cultivos que maduran a 6 bloques o menos dan semillas que mutan 20 veces más. Radiactivo",
        "Gammatuin: gewassen die binnen 6 blokken rijpen geven zaad dat 20 keer zo vaak muteert. Radioactief",
        "Giardino gamma: le colture che maturano entro 6 blocchi danno semi che mutano 20 volte più spesso. Radioattivo",
        "Gammagarten: Feldfrüchte, die im Umkreis von 6 Blöcken reifen, geben Samen, die 20-mal so oft mutieren. Radioaktiv",
        "Gammaträdgård: grödor som mognar inom 6 block ger frön som muterar 20 gånger så ofta. Radioaktiv",
        "Gammahave: afgrøder, der modner inden for 6 blokke, giver frø, der muterer 20 gange så ofte. Radioaktiv",
        "Гамма-поле: посевы, созревающие в пределах 6 блоков, дают семена, мутирующие в 20 раз чаще. Радиоактивно"),
    "DNA Sequencer": ("Sequenciador de DNA", "Séquenceur d'ADN", "Secuenciador de ADN", "DNA-sequencer",
                      "Sequenziatore di DNA", "DNA-Sequenzierer", "DNA-sekvenserare", "DNA-sekventeringsmaskine",
                      "ДНК-секвенатор"),
    "DNA Sequencing: use seeds on it to read their whole genome (20 kJ a seed)": (
        "Sequenciamento de DNA: use sementes nele para ler o genoma inteiro (20 kJ por semente)",
        "Séquençage de l'ADN : utilise des graines dessus pour lire tout leur génome (20 kJ par graine)",
        "Secuenciación de ADN: usa semillas en él para leer su genoma entero (20 kJ por semilla)",
        "DNA-sequencing: gebruik er zaad op om het hele genoom te lezen (20 kJ per zaad)",
        "Sequenziamento del DNA: usa dei semi su di esso per leggerne l'intero genoma (20 kJ a seme)",
        "DNA-Sequenzierung: Samen darauf anwenden, um ihr ganzes Genom zu lesen (20 kJ je Samen)",
        "DNA-sekvensering: använd frön på den för att läsa hela genomet (20 kJ per frö)",
        "DNA-sekventering: brug frø på den for at læse hele genomet (20 kJ pr. frø)",
        "Секвенирование ДНК: примени к нему семена, чтобы прочитать весь геном (20 кДж на семя)"),
    "Thermocycler": ("Termociclador", "Thermocycleur", "Termociclador", "Thermocycler", "Termociclatore",
                     "Thermocycler", "Termocykler", "Termocykler", "Термоциклер"),
    "PCR: sneak-use a seed on it to set the marker; seeds put in are sorted: those with all the marker's useful traits go below, the rest to the sides (10 kJ a seed)": (
        "PCR: use uma semente nele agachado para definir o marcador; as sementes postas são separadas: as que têm todas as características úteis do marcador vão para baixo, as outras para os lados (10 kJ por semente)",
        "PCR : utilise une graine dessus accroupi pour définir le marqueur ; les graines mises sont triées : celles qui ont tous les caractères utiles du marqueur vont dessous, les autres sur les côtés (10 kJ par graine)",
        "PCR: usa una semilla en él agachado para fijar el marcador; las semillas que se echan se clasifican: las que tienen todos los rasgos útiles del marcador van abajo, las demás a los lados (10 kJ por semilla)",
        "PCR: gebruik er gebukt een zaad op als marker; zaad dat erin gaat wordt gesorteerd: zaad met alle nuttige eigenschappen van de marker gaat naar onder, de rest naar de zijkanten (10 kJ per zaad)",
        "PCR: usa un seme su di esso da accovacciato per impostare il marcatore; i semi messi dentro vengono smistati: quelli con tutti i caratteri utili del marcatore vanno sotto, gli altri ai lati (10 kJ a seme)",
        "PCR: schleichend einen Samen darauf anwenden legt den Marker fest; eingefüllte Samen werden sortiert: die mit allen nützlichen Merkmalen des Markers nach unten, die übrigen zur Seite (10 kJ je Samen)",
        "PCR: använd ett frö på den smygande för att välja markör; frön som läggs i sorteras: de med alla markörens nyttiga egenskaper går nedåt, resten åt sidorna (10 kJ per frö)",
        "PCR: brug et frø på den sneget for at vælge markør; frø, der kommes i, sorteres: dem med alle markørens nyttige egenskaber går nedad, resten til siderne (10 kJ pr. frø)",
        "ПЦР: примени семя, крадучись, чтобы задать маркер; заложенные семена сортируются: с всеми полезными признаками маркера идут вниз, остальные в стороны (10 кДж на семя)"),
    # research
    "Nitrogen Fixation": ("Fixação de nitrogênio", "Fixation de l'azote", "Fijación de nitrógeno", "Stikstofbinding",
                          "Fissazione dell'azoto", "Stickstofffixierung", "Kvävefixering", "Kvælstoffiksering",
                          "Азотфиксация"),
    "Hybrid Maize": ("Milho híbrido", "Maïs hybride", "Maíz híbrido", "Hybride maïs", "Mais ibrido", "Hybridmais",
                     "Hybridmajs", "Hybridmajs", "Гибридная кукуруза"),
    "X-ray Mutagenesis": ("Mutagênese por raios X", "Mutagenèse par rayons X", "Mutagénesis por rayos X",
                          "Röntgenmutagenese", "Mutagenesi con raggi X", "Röntgenmutagenese", "Röntgenmutagenes",
                          "Røntgenmutagenese", "Рентгеновский мутагенез"),
    "DNA Double Helix": ("Dupla hélice do DNA", "Double hélice de l'ADN", "Doble hélice del ADN", "Dubbele helix van DNA",
                         "Doppia elica del DNA", "DNA-Doppelhelix", "DNA:s dubbelspiral", "DNA's dobbeltspiral",
                         "Двойная спираль ДНК"),
    "DNA Sequencing": ("Sequenciamento de DNA", "Séquençage de l'ADN", "Secuenciación de ADN", "DNA-sequencing",
                       "Sequenziamento del DNA", "DNA-Sequenzierung", "DNA-sekvensering", "DNA-sekventering",
                       "Секвенирование ДНК"),
    "Transgenesis": ("Transgênese", "Transgenèse", "Transgénesis", "Transgenese", "Transgenesi", "Transgenese",
                     "Transgenes", "Transgenese", "Трансгенез"),
    "Goffart packed green maize in a pit and it kept all winter: the Silo turns maize, sorghum and alfalfa into silage": (
        "Goffart socou milho verde num fosso e ele durou o inverno todo: o Silo transforma milho, sorgo e alfafa em silagem",
        "Goffart tasse du maïs vert dans une fosse et il se garde tout l'hiver : le Silo change maïs, sorgho et luzerne en ensilage",
        "Goffart apisona maíz verde en un foso y se conserva todo el invierno: el Silo convierte maíz, sorgo y alfalfa en ensilado",
        "Goffart stampt groene maïs in een kuil en die blijft de hele winter goed: de Silo maakt kuilvoer van maïs, sorghum en luzerne",
        "Goffart pigia mais verde in una fossa e si conserva tutto l'inverno: il Silo trasforma mais, sorgo ed erba medica in insilato",
        "Goffart stampft grünen Mais in eine Grube, und er hält den ganzen Winter: das Silo macht aus Mais, Sorghum und Luzerne Silage",
        "Goffart packar grön majs i en grop och den håller hela vintern: silon gör ensilage av majs, durra och lusern",
        "Goffart pakker grøn majs i en grube, og den holder hele vinteren: siloen gør majs, durra og lucerne til ensilage",
        "Гоффар утрамбовал зелёную кукурузу в яму, и она сохранилась всю зиму: силосная башня делает силос из кукурузы, сорго и люцерны"),
    "Hellriegel and Wilfarth show that bacteria in the root nodules of legumes take nitrogen from the air: Inoculant gives a legume its rhizobia": (
        "Hellriegel e Wilfarth mostram que bactérias nos nódulos das raízes das leguminosas tiram nitrogênio do ar: o Inoculante dá rizóbios a uma leguminosa",
        "Hellriegel et Wilfarth montrent que les bactéries des nodosités des légumineuses prennent l'azote de l'air : l'Inoculum donne ses rhizobiums à une légumineuse",
        "Hellriegel y Wilfarth muestran que las bacterias de los nódulos de las leguminosas toman nitrógeno del aire: el Inoculante da rizobios a una leguminosa",
        "Hellriegel en Wilfarth tonen aan dat bacteriën in de wortelknolletjes van vlinderbloemigen stikstof uit de lucht halen: Entstof geeft een vlinderbloemige zijn rhizobia",
        "Hellriegel e Wilfarth mostrano che i batteri nei noduli delle leguminose prendono azoto dall'aria: l'Inoculante dà i rizobi a una leguminosa",
        "Hellriegel und Wilfarth zeigen, dass Bakterien in den Wurzelknöllchen der Hülsenfrüchtler Stickstoff aus der Luft holen: Impfmittel gibt einer Hülsenfrucht ihre Rhizobien",
        "Hellriegel och Wilfarth visar att bakterier i baljväxternas rotknölar tar kväve ur luften: ympmedel ger en baljväxt dess rhizobier",
        "Hellriegel og Wilfarth viser, at bakterier i bælgplanternes rodknolde tager kvælstof fra luften: podemiddel giver en bælgplante dens rhizobier",
        "Хелльригель и Вильфарт показали, что бактерии в клубеньках бобовых берут азот из воздуха: инокулянт даёт бобовому его ризобии"),
    "Shull and Jones cross inbred lines of maize: the first generation grows with hybrid vigour. The Seed Lens marks hybrids and pure lines": (
        "Shull e Jones cruzam linhagens endogâmicas de milho: a primeira geração cresce com vigor híbrido. A Lupa de sementes marca híbridos e linhagens puras",
        "Shull et Jones croisent des lignées consanguines de maïs : la première génération pousse avec la vigueur hybride. La Loupe à graines signale hybrides et lignées pures",
        "Shull y Jones cruzan líneas endogámicas de maíz: la primera generación crece con vigor híbrido. La Lupa de semillas marca híbridos y líneas puras",
        "Shull en Jones kruisen ingeteelde maïslijnen: de eerste generatie groeit met hybride groeikracht. De Zadenloep toont hybriden en zuivere lijnen",
        "Shull e Jones incrociano linee pure di mais: la prima generazione cresce con vigore ibrido. La Lente per semi segnala ibridi e linee pure",
        "Shull und Jones kreuzen Inzuchtlinien von Mais: die erste Generation wächst mit Heterosis. Die Samenlupe zeigt Hybriden und reine Linien",
        "Shull och Jones korsar inavlade majslinjer: första generationen växer med hybridkraft. Fröluppen visar hybrider och rena linjer",
        "Shull og Jones krydser indavlede majslinjer: første generation vokser med hybridkraft. Frøluppen viser hybrider og rene linjer",
        "Шелл и Джонс скрещивают инбредные линии кукурузы: первое поколение растёт с гибридной силой. Семенная лупа отмечает гибриды и чистые линии"),
    "Feed with energy, protein, minerals and salt in the right shares: animals grow and breed faster on it": (
        "Ração com energia, proteína, minerais e sal na proporção certa: os animais crescem e procriam mais rápido",
        "Un aliment avec énergie, protéines, minéraux et sel dans les bonnes proportions : les animaux grandissent et se reproduisent plus vite",
        "Pienso con energía, proteína, minerales y sal en su justa medida: los animales crecen y crían más rápido",
        "Voer met energie, eiwit, mineralen en zout in de juiste verhouding: dieren groeien en kweken er sneller op",
        "Mangime con energia, proteine, minerali e sale nelle giuste proporzioni: gli animali crescono e si riproducono più in fretta",
        "Futter mit Energie, Eiweiß, Mineralien und Salz im richtigen Verhältnis: Tiere wachsen und vermehren sich schneller",
        "Foder med energi, protein, mineraler och salt i rätt proportioner: djuren växer och förökar sig fortare",
        "Foder med energi, protein, mineraler og salt i de rette forhold: dyrene vokser og formerer sig hurtigere",
        "Корм с энергией, белком, минералами и солью в нужных долях: животные быстрее растут и размножаются"),
    "Stadler X-rays barley seeds and the plants come out changed: the Seed Irradiator makes mutations twenty times as common": (
        "Stadler irradia sementes de cevada com raios X e as plantas saem mudadas: o Irradiador de sementes torna as mutações vinte vezes mais comuns",
        "Stadler irradie des graines d'orge aux rayons X et les plantes sortent changées : l'Irradiateur de graines rend les mutations vingt fois plus fréquentes",
        "Stadler irradia semillas de cebada con rayos X y las plantas salen cambiadas: el Irradiador de semillas hace las mutaciones veinte veces más comunes",
        "Stadler bestraalt gerstzaad met röntgenstraling en de planten komen veranderd op: de Zaadbestraler maakt mutaties twintig keer zo vaak",
        "Stadler irradia semi d'orzo con i raggi X e le piante nascono cambiate: l'Irradiatore di semi rende le mutazioni venti volte più comuni",
        "Stadler bestrahlt Gerstensamen mit Röntgenstrahlen, und die Pflanzen wachsen verändert: der Samenbestrahler macht Mutationen zwanzigmal so häufig",
        "Stadler bestrålar kornfrön med röntgen och plantorna blir förändrade: frö-bestrålaren gör mutationer tjugo gånger vanligare",
        "Stadler bestråler bygfrø med røntgen, og planterne kommer ændrede op: frøbestråleren gør mutationer tyve gange så almindelige",
        "Стадлер облучает семена ячменя рентгеном, и растения выходят изменёнными: облучатель семян делает мутации в двадцать раз чаще"),
    "Franklin's X-ray picture, Watson and Crick's model: genes are written in DNA. The Seed Lens shows every trait, and the stature gene of wheat and rice": (
        "A foto de raios X de Franklin, o modelo de Watson e Crick: os genes estão escritos no DNA. A Lupa de sementes mostra todas as características e o gene do porte do trigo e do arroz",
        "Le cliché aux rayons X de Franklin, le modèle de Watson et Crick : les gènes sont écrits dans l'ADN. La Loupe à graines montre tous les caractères, et le gène de la taille du blé et du riz",
        "La foto de rayos X de Franklin, el modelo de Watson y Crick: los genes están escritos en el ADN. La Lupa de semillas muestra todos los rasgos y el gen del porte del trigo y el arroz",
        "Franklins röntgenfoto, het model van Watson en Crick: genen zijn in DNA geschreven. De Zadenloep toont alle eigenschappen en het lengte-gen van tarwe en rijst",
        "La foto ai raggi X di Franklin, il modello di Watson e Crick: i geni sono scritti nel DNA. La Lente per semi mostra ogni carattere e il gene della statura di grano e riso",
        "Franklins Röntgenbild, das Modell von Watson und Crick: Gene sind in DNA geschrieben. Die Samenlupe zeigt alle Merkmale und das Wuchshöhen-Gen von Weizen und Reis",
        "Franklins röntgenbild, Watsons och Cricks modell: generna är skrivna i DNA. Fröluppen visar alla egenskaper och växthöjdsgenen hos vete och ris",
        "Franklins røntgenbillede, Watsons og Cricks model: generne er skrevet i DNA. Frøluppen viser alle egenskaber og vækstgenet hos hvede og ris",
        "Рентгенограмма Франклин, модель Уотсона и Крика: гены записаны в ДНК. Семенная лупа показывает все признаки и ген роста пшеницы и риса"),
    "A cobalt-60 source in a field of crops (Japan 1960): seeds of the plants around it mutate twenty times as often": (
        "Uma fonte de cobalto-60 num campo de lavouras (Japão, 1960): as sementes das plantas em volta mutam vinte vezes mais",
        "Une source de cobalt 60 dans un champ (Japon, 1960) : les graines des plantes autour mutent vingt fois plus",
        "Una fuente de cobalto-60 en un campo de cultivos (Japón, 1960): las semillas de las plantas de alrededor mutan veinte veces más",
        "Een kobalt-60-bron midden in een veld gewassen (Japan 1960): het zaad van de planten eromheen muteert twintig keer zo vaak",
        "Una sorgente di cobalto-60 in un campo coltivato (Giappone, 1960): i semi delle piante intorno mutano venti volte più spesso",
        "Eine Cobalt-60-Quelle mitten im Feld (Japan 1960): die Samen der Pflanzen ringsum mutieren zwanzigmal so oft",
        "En kobolt-60-källa i ett fält med grödor (Japan 1960): fröna från plantorna runt den muterar tjugo gånger så ofta",
        "En kobolt-60-kilde i en mark med afgrøder (Japan 1960): frøene fra planterne omkring den muterer tyve gange så ofte",
        "Источник кобальта-60 среди посевов (Япония, 1960): семена растений вокруг мутируют в двадцать раз чаще"),
    "Sanger reads DNA letter by letter: the DNA Sequencer shows the whole genome of a seed": (
        "Sanger lê o DNA letra por letra: o Sequenciador de DNA mostra o genoma inteiro de uma semente",
        "Sanger lit l'ADN lettre par lettre : le Séquenceur d'ADN montre tout le génome d'une graine",
        "Sanger lee el ADN letra a letra: el Secuenciador de ADN muestra el genoma entero de una semilla",
        "Sanger leest DNA letter voor letter: de DNA-sequencer toont het hele genoom van een zaad",
        "Sanger legge il DNA lettera per lettera: il Sequenziatore di DNA mostra l'intero genoma di un seme",
        "Sanger liest DNA Buchstabe für Buchstabe: der DNA-Sequenzierer zeigt das ganze Genom eines Samens",
        "Sanger läser DNA bokstav för bokstav: DNA-sekvenseraren visar ett fröns hela genom",
        "Sanger læser DNA bogstav for bogstav: DNA-sekventeringsmaskinen viser et frøs hele genom",
        "Сэнгер читает ДНК буква за буквой: ДНК-секвенатор показывает весь геном семени"),
    "Mullis copies a stretch of DNA a billion times by heating and cooling it: the Thermocycler tests seeds for a marker's genes and sorts them": (
        "Mullis copia um trecho de DNA um bilhão de vezes aquecendo e esfriando: o Termociclador testa as sementes para os genes de um marcador e as separa",
        "Mullis copie un bout d'ADN un milliard de fois en le chauffant et le refroidissant : le Thermocycleur teste les graines pour les gènes d'un marqueur et les trie",
        "Mullis copia un tramo de ADN mil millones de veces calentándolo y enfriándolo: el Termociclador analiza las semillas buscando los genes de un marcador y las clasifica",
        "Mullis kopieert een stuk DNA een miljard keer door het te verwarmen en af te koelen: de Thermocycler test zaad op de genen van een marker en sorteert het",
        "Mullis copia un tratto di DNA un miliardo di volte scaldandolo e raffreddandolo: il Termociclatore cerca nei semi i geni di un marcatore e li smista",
        "Mullis kopiert ein Stück DNA milliardenfach durch Erhitzen und Abkühlen: der Thermocycler prüft Samen auf die Gene eines Markers und sortiert sie",
        "Mullis kopierar en DNA-bit en miljard gånger genom att värma och kyla den: termocyklern testar frön för en markörs gener och sorterar dem",
        "Mullis kopierer et stykke DNA en milliard gange ved at varme og køle det: termocykleren tester frø for en markørs gener og sorterer dem",
        "Муллис копирует участок ДНК миллиард раз, нагревая и охлаждая его: термоциклер проверяет семена на гены маркера и сортирует их"),
    "Sanford shoots gold beads coated with DNA into plant cells: the Gene Gun gives crops the Bt, EPSPS, golden rice and DREB genes": (
        "Sanford atira bolinhas de ouro cobertas de DNA em células vegetais: a Pistola de genes dá às lavouras os genes Bt, EPSPS, do arroz dourado e DREB",
        "Sanford tire des billes d'or enrobées d'ADN dans des cellules végétales : le Canon à gènes donne aux cultures les gènes Bt, EPSPS, du riz doré et DREB",
        "Sanford dispara bolitas de oro recubiertas de ADN a células vegetales: la Pistola génica da a los cultivos los genes Bt, EPSPS, del arroz dorado y DREB",
        "Sanford schiet met DNA beklede goudbolletjes in plantencellen: het Genkanon geeft gewassen de genen Bt, EPSPS, gouden rijst en DREB",
        "Sanford spara sferette d'oro rivestite di DNA in cellule vegetali: la Pistola genica dà alle colture i geni Bt, EPSPS, del riso dorato e DREB",
        "Sanford schießt mit DNA überzogene Goldkügelchen in Pflanzenzellen: die Genkanone gibt Feldfrüchten die Gene Bt, EPSPS, Goldener Reis und DREB",
        "Sanford skjuter DNA-täckta guldkulor in i växtceller: genpistolen ger grödor generna Bt, EPSPS, gyllene ris och DREB",
        "Sanford skyder DNA-dækkede guldkugler ind i planteceller: genkanonen giver afgrøder generne Bt, EPSPS, gylden ris og DREB",
        "Сэнфорд выстреливает покрытые ДНК золотые шарики в клетки растений: генная пушка даёт посевам гены Bt, EPSPS, золотого риса и DREB"),
    "Charpentier and Doudna turn a bacterial defence into scissors for DNA: the CRISPR Kit rewrites one gene of a crop": (
        "Charpentier e Doudna transformam uma defesa bacteriana em tesoura para DNA: o Kit CRISPR reescreve um gene de uma lavoura",
        "Charpentier et Doudna font d'une défense bactérienne des ciseaux à ADN : le Kit CRISPR réécrit un gène d'une culture",
        "Charpentier y Doudna convierten una defensa bacteriana en tijeras para el ADN: el Kit CRISPR reescribe un gen de un cultivo",
        "Charpentier en Doudna maken van een bacteriële afweer een schaar voor DNA: de CRISPR-kit herschrijft één gen van een gewas",
        "Charpentier e Doudna trasformano una difesa batterica in forbici per il DNA: il Kit CRISPR riscrive un gene di una coltura",
        "Charpentier und Doudna machen aus einer Bakterienabwehr eine Schere für DNA: das CRISPR-Kit schreibt ein Gen einer Feldfrucht um",
        "Charpentier och Doudna gör ett bakteriellt försvar till en sax för DNA: CRISPR-kitet skriver om en gen i en gröda",
        "Charpentier og Doudna gør et bakterieforsvar til en saks til DNA: CRISPR-sættet omskriver et gen i en afgrøde",
        "Шарпантье и Дудна превращают защиту бактерий в ножницы для ДНК: набор CRISPR переписывает один ген посева"),
    "Mendel counted his peas: each trait is two factors, one from each parent. The Seed Lens shows the genes themselves": None,
    # reaction
    "Cobalt 60 activation": ("Ativação de cobalto-60", "Activation du cobalt 60", "Activación de cobalto-60",
                             "Kobalt-60-activering", "Attivazione del cobalto-60", "Cobalt-60-Aktivierung",
                             "Kobolt-60-aktivering", "Kobolt-60-aktivering", "Активация кобальта-60"),
    "Deuterons from heavy water fired at cobalt-59 make cobalt-60 (Co-59 + d -> Co-60 + p), a strong gamma source; put heavy water in the reagent slot": (
        "Dêuterons da água pesada disparados contra cobalto-59 fazem cobalto-60 (Co-59 + d -> Co-60 + p), uma forte fonte gama; ponha água pesada no espaço do reagente",
        "Des deutons de l'eau lourde tirés sur du cobalt 59 donnent du cobalt 60 (Co-59 + d -> Co-60 + p), une forte source gamma ; mets de l'eau lourde dans l'emplacement du réactif",
        "Deuterones del agua pesada lanzados contra cobalto-59 dan cobalto-60 (Co-59 + d -> Co-60 + p), una fuerte fuente gamma; pon agua pesada en la ranura del reactivo",
        "Deuteronen uit zwaar water op kobalt-59 geschoten maken kobalt-60 (Co-59 + d -> Co-60 + p), een sterke gammabron; doe zwaar water in het reagensvak",
        "Deuteroni dall'acqua pesante sparati sul cobalto-59 danno cobalto-60 (Co-59 + d -> Co-60 + p), una forte sorgente gamma; metti acqua pesante nello slot del reagente",
        "Deuteronen aus schwerem Wasser, auf Cobalt-59 geschossen, ergeben Cobalt-60 (Co-59 + d -> Co-60 + p), eine starke Gammaquelle; schweres Wasser in den Reagenzplatz legen",
        "Deuteroner från tungt vatten som skjuts mot kobolt-59 ger kobolt-60 (Co-59 + d -> Co-60 + p), en stark gammakälla; lägg tungt vatten i reagensplatsen",
        "Deuteroner fra tungt vand skudt mod kobolt-59 giver kobolt-60 (Co-59 + d -> Co-60 + p), en stærk gammakilde; læg tungt vand i reagenspladsen",
        "Дейтроны из тяжёлой воды, выпущенные в кобальт-59, дают кобальт-60 (Co-59 + d -> Co-60 + p), сильный гамма-источник; положи тяжёлую воду в ячейку реагента"),
}
S = {k: v for k, v in S.items() if v is not None}

GUIDE = {
    "pt_br": [
        "# Melhoramento moderno",
        "@4 As Leis de Mendel também ensinam o Caderno de cruzamentos: use-o com sementes na outra mão e depois com as "
        "sementes do outro genitor. Ele escreve, gene por gene, que parte das sementes desse cruzamento mostrará cada "
        "característica.",
        "@4 Silagem (nível 4): o Silo transforma milho, sorgo e alfafa, postos à mão ou por um funil, em silagem, uma a "
        "cada 5 segundos; um funil embaixo a retira. Um cocho de silagem dura o dobro. Fixação de nitrogênio (nível 4): "
        "o Inoculante usado em soja, feijão ou alfafa dá rizóbios às raízes: colhidos maduros, deixam a terra arada bem "
        "adubada.",
        "@5 Milho híbrido (nível 5): a semente de duas linhagens puras diferentes (com as duas cópias iguais de cada "
        "gene) é um híbrido F1: cresce e rende 20% a mais, mas suas próprias sementes perdem isso. A Lupa de sementes "
        "marca híbridos e linhagens puras. Ração balanceada (nível 5): milho em grão, aveia, farinha de proteína, farinha "
        "de osso e sal fazem 4 rações que todo animal da fazenda come: os filhotes crescem duas vezes mais rápido e os "
        "pais ficam prontos para procriar de novo mais cedo.",
        "@5 Mutagênese por raios X (nível 5): o Irradiador de sementes irradia as sementes postas nele (40 kJ por "
        "semente, de pilhas ou dínamos ligados a ele). 3 sementes em 10 morrem; cada cópia de gene das outras muta 20 "
        "vezes mais que na natureza. Plante todas e guarde as raras que forem úteis.",
        "@6 Dupla hélice do DNA (nível 6): a Lupa de sementes mostra todas as características. Trigo e arroz têm um "
        "sétimo gene, S, Porte: plantas ss são semianãs e, com adubo forte (NPK), rendem metade a mais, a Revolução "
        "Verde. O trigo silvestre da grama dos bosques de cerejeiras às vezes traz s.",
        "@6 Jardim gama (nível 6): o Cíclotron faz uma fonte de cobalto-60 com cobalto e água pesada no espaço do "
        "reagente; oito chumbos em volta dela fazem o Jardim gama. Lavouras que amadurecem a até 6 blocos dele dão "
        "sementes que mutam 20 vezes mais. É radioativo: mantenha distância.",
        "@7 Sequenciamento de DNA (nível 7): o Sequenciador de DNA lê o genoma inteiro das sementes usadas nele (20 kJ "
        "por semente). PCR (nível 7): o Termociclador testa cada semente contra uma semente marcadora (use uma semente "
        "nele agachado para definir o marcador): as sementes com todas as características úteis do marcador vão para um "
        "funil embaixo, as outras para funis dos lados (10 kJ por semente).",
        "@7 Transgênese (nível 7): com um construto gênico na outra mão, a Pistola de genes usada numa lavoura põe o gene "
        "nela; metade dos tiros pega. Bt: a lagarta nunca pega. EPSPS: o Limpa-Campo a poupa. Arroz dourado (só arroz): "
        "os grãos têm vitamina A e podem ser comidos crus. DREB: tolera a seca. As sementes herdam um transgene de "
        "qualquer um dos pais. CRISPR (nível 7): o Kit CRISPR escreve a forma útil de um gene nas duas cópias de uma "
        "lavoura; use-o agachado para escolher o gene.",
    ],
    "fr_fr": [
        "# Sélection moderne",
        "@4 Les Lois de Mendel apprennent aussi le Carnet de croisements : utilise-le avec des graines dans l'autre "
        "main, puis avec les graines de l'autre parent. Il écrit, gène par gène, quelle part des graines de ce "
        "croisement montrera chaque caractère.",
        "@4 Ensilage (niveau 4) : le Silo change le maïs, le sorgho et la luzerne, mis à la main ou par un entonnoir, en "
        "ensilage, un toutes les 5 secondes ; un entonnoir dessous le retire. Une mangeoire d'ensilage dure deux fois "
        "plus. Fixation de l'azote (niveau 4) : l'Inoculum utilisé sur le soja, le haricot ou la luzerne donne des "
        "rhizobiums à leurs racines : récoltés mûrs, ils laissent leur terre bien fertilisée.",
        "@5 Maïs hybride (niveau 5) : la graine de deux lignées pures différentes (deux copies égales de chaque gène) "
        "est un hybride F1 : il pousse et rapporte 20 % de plus, mais ses propres graines le perdent. La Loupe à graines "
        "signale hybrides et lignées pures. Aliment équilibré (niveau 5) : grains de maïs, avoine, farine de protéines, "
        "poudre d'os et sel font 4 aliments que mangent tous les animaux de ferme : les petits grandissent deux fois plus "
        "vite et les parents sont prêts à se reproduire plus tôt.",
        "@5 Mutagenèse par rayons X (niveau 5) : l'Irradiateur de graines irradie les graines qu'on y met (40 kJ par "
        "graine, de piles ou dynamos reliées). 3 graines sur 10 meurent ; chaque copie de gène des autres mute 20 fois "
        "plus que dans la nature. Sème-les toutes et garde les rares utiles.",
        "@6 Double hélice de l'ADN (niveau 6) : la Loupe à graines montre tous les caractères. Le blé et le riz ont un "
        "septième gène, S, Taille : les plantes ss sont semi-naines et, sur engrais fort (NPK), rapportent moitié plus, "
        "la Révolution verte. Le blé sauvage de l'herbe des cerisaies porte parfois s.",
        "@6 Jardin gamma (niveau 6) : le Cyclotron fait une source de cobalt 60 avec du cobalt et de l'eau lourde dans "
        "l'emplacement du réactif ; huit plombs autour font le Jardin gamma. Les cultures qui mûrissent à 6 blocs ou "
        "moins donnent des graines qui mutent 20 fois plus. Il est radioactif : garde tes distances.",
        "@7 Séquençage de l'ADN (niveau 7) : le Séquenceur d'ADN lit tout le génome des graines utilisées dessus (20 kJ "
        "par graine). PCR (niveau 7) : le Thermocycleur teste chaque graine contre une graine marqueur (utilise une graine "
        "dessus accroupi pour le définir) : les graines qui ont tous les caractères utiles du marqueur vont dans un "
        "entonnoir dessous, les autres dans des entonnoirs sur les côtés (10 kJ par graine).",
        "@7 Transgenèse (niveau 7) : avec une construction génique dans l'autre main, le Canon à gènes utilisé sur une "
        "culture y met le gène ; la moitié des tirs prend. Bt : les chenilles ne l'attaquent jamais. EPSPS : Champ net "
        "l'épargne. Riz doré (riz seulement) : ses grains contiennent de la vitamine A et se mangent crus. DREB : tolère "
        "la sécheresse. Les graines héritent d'un transgène de l'un ou l'autre parent. CRISPR (niveau 7) : le Kit CRISPR "
        "écrit la forme utile d'un gène dans les deux copies d'une culture ; accroupi, il choisit le gène.",
    ],
    "es_es": [
        "# Mejora moderna",
        "@4 Las Leyes de Mendel también enseñan el Cuaderno de cruces: úsalo con semillas en la otra mano y luego con las "
        "semillas del otro progenitor. Escribe, gen por gen, qué parte de las semillas de ese cruce mostrará cada rasgo.",
        "@4 Ensilado (nivel 4): el Silo convierte maíz, sorgo y alfalfa, echados a mano o con una tolva, en ensilado, uno "
        "cada 5 segundos; una tolva debajo lo saca. Un comedero de ensilado dura el doble. Fijación de nitrógeno (nivel "
        "4): el Inoculante usado en soja, judías o alfalfa da rizobios a sus raíces: cosechadas maduras, dejan su tierra "
        "bien abonada.",
        "@5 Maíz híbrido (nivel 5): la semilla de dos líneas puras distintas (dos copias iguales de cada gen) es un "
        "híbrido F1: crece y rinde un 20% más, pero sus propias semillas lo pierden. La Lupa de semillas marca híbridos y "
        "líneas puras. Pienso equilibrado (nivel 5): granos de maíz, avena, harina de proteínas, harina de hueso y sal "
        "dan 4 piensos que come todo animal de granja: las crías crecen el doble de rápido y los padres vuelven a estar "
        "listos para criar antes.",
        "@5 Mutagénesis por rayos X (nivel 5): el Irradiador de semillas irradia las semillas que se le echan (40 kJ por "
        "semilla, de pilas o dinamos unidos a él). 3 semillas de cada 10 mueren; cada copia de gen de las demás muta 20 "
        "veces más que en la naturaleza. Siémbralas todas y quédate con las raras útiles.",
        "@6 Doble hélice del ADN (nivel 6): la Lupa de semillas muestra todos los rasgos. El trigo y el arroz tienen un "
        "séptimo gen, S, Porte: las plantas ss son semienanas y con abono fuerte (NPK) rinden la mitad más, la Revolución "
        "Verde. El trigo silvestre de la hierba de los cerezales a veces lleva s.",
        "@6 Jardín gamma (nivel 6): el Ciclotrón hace una fuente de cobalto-60 con cobalto y agua pesada en la ranura del "
        "reactivo; ocho plomos alrededor hacen el Jardín gamma. Los cultivos que maduran a 6 bloques o menos dan semillas "
        "que mutan 20 veces más. Es radiactivo: guarda las distancias.",
        "@7 Secuenciación de ADN (nivel 7): el Secuenciador de ADN lee el genoma entero de las semillas usadas en él (20 "
        "kJ por semilla). PCR (nivel 7): el Termociclador compara cada semilla con una semilla marcadora (usa una semilla "
        "en él agachado para fijarla): las semillas con todos los rasgos útiles del marcador van a una tolva debajo, las "
        "demás a tolvas a los lados (10 kJ por semilla).",
        "@7 Transgénesis (nivel 7): con una construcción génica en la otra mano, la Pistola génica usada en un cultivo le "
        "mete el gen; prende la mitad de los disparos. Bt: las orugas nunca lo atacan. EPSPS: Campo limpio lo respeta. "
        "Arroz dorado (solo arroz): sus granos llevan vitamina A y se comen crudos. DREB: tolera la sequía. Las semillas "
        "heredan un transgén de cualquiera de los padres. CRISPR (nivel 7): el Kit CRISPR escribe la forma útil de un gen "
        "en las dos copias de un cultivo; agachado eliges el gen.",
    ],
    "nl_nl": [
        "# Moderne veredeling",
        "@4 De Wetten van Mendel leren ook het Kruisingsschrift: gebruik het met zaad in je andere hand en dan met het "
        "zaad van de andere ouder. Het schrijft per gen op welk deel van het zaad van die kruising elke eigenschap toont.",
        "@4 Kuilvoer (niveau 4): de Silo maakt van maïs, sorghum en luzerne, met de hand of via een trechter erin gedaan, "
        "kuilvoer, één per 5 seconden; een trechter eronder haalt het eruit. Een voerbak met kuilvoer gaat twee keer zo "
        "lang mee. Stikstofbinding (niveau 4): Entstof op soja, bonen of luzerne geeft hun wortels rhizobia: rijp "
        "geoogst laten ze hun akker sterk bemest achter.",
        "@5 Hybride maïs (niveau 5): zaad van twee verschillende zuivere lijnen (twee gelijke kopieën van elk gen) is een "
        "F1-hybride: hij groeit en brengt 20% meer op, maar zijn eigen zaad verliest dat. De Zadenloep toont hybriden en "
        "zuivere lijnen. Uitgebalanceerd voer (niveau 5): maïskorrels, haver, eiwitmeel, beendermeel en zout geven 4 "
        "voer dat elk boerderijdier eet: jongen groeien twee keer zo snel en ouders kunnen eerder weer kweken.",
        "@5 Röntgenmutagenese (niveau 5): de Zaadbestraler bestraalt het zaad dat erin gaat (40 kJ per zaad, van "
        "zuilen of dynamo's die eraan vast zitten). 3 op 10 zaden sterven; elke genkopie van de rest muteert 20 keer zo "
        "vaak als in de natuur. Zaai ze allemaal en houd de zeldzame nuttige.",
        "@6 Dubbele helix van DNA (niveau 6): de Zadenloep toont alle eigenschappen. Tarwe en rijst hebben een zevende "
        "gen, S, Lengte: ss-planten zijn halfdwerg en brengen op sterke mest (NPK) de helft meer op, de Groene Revolutie. "
        "Wilde tarwe uit het gras van kersenbosjes draagt soms s.",
        "@6 Gammatuin (niveau 6): het Cyclotron maakt een kobalt-60-bron van kobalt met zwaar water in het reagensvak; "
        "acht lood eromheen maken de Gammatuin. Gewassen die binnen 6 blokken ervan rijpen geven zaad dat 20 keer zo "
        "vaak muteert. Hij is radioactief: houd afstand.",
        "@7 DNA-sequencing (niveau 7): de DNA-sequencer leest het hele genoom van zaad dat erop gebruikt wordt (20 kJ "
        "per zaad). PCR (niveau 7): de Thermocycler test elk zaad tegen een markerzaad (gebruik er gebukt een zaad op om "
        "de marker te kiezen): zaad met alle nuttige eigenschappen van de marker gaat naar een trechter eronder, de rest "
        "naar trechters aan de zijkanten (10 kJ per zaad).",
        "@7 Transgenese (niveau 7): met een genconstruct in je andere hand brengt het Genkanon, gebruikt op een gewas, "
        "het gen erin; de helft van de schoten slaat aan. Bt: rupsen tasten het nooit aan. EPSPS: Veld vrij spaart het. "
        "Gouden rijst (alleen rijst): de korrels bevatten vitamine A en zijn rauw eetbaar. DREB: verdraagt droogte. Zaad "
        "erft een transgen van elk van beide ouders. CRISPR (niveau 7): de CRISPR-kit schrijft de nuttige vorm van één "
        "gen in beide kopieën van een gewas; gebukt kies je het gen.",
    ],
    "it_it": [
        "# Miglioramento moderno",
        "@4 Le Leggi di Mendel insegnano anche il Quaderno degli incroci: usalo con dei semi nell'altra mano, poi con i "
        "semi dell'altro genitore. Scrive, gene per gene, quale parte dei semi di quell'incrocio mostrerà ogni carattere.",
        "@4 Insilato (livello 4): il Silo trasforma mais, sorgo ed erba medica, messi a mano o con una tramoggia, in "
        "insilato, uno ogni 5 secondi; una tramoggia sotto lo raccoglie. Una mangiatoia di insilato dura il doppio. "
        "Fissazione dell'azoto (livello 4): l'Inoculante usato su soia, fagioli o erba medica dà i rizobi alle radici: "
        "raccolti maturi, lasciano il campo ben concimato.",
        "@5 Mais ibrido (livello 5): il seme di due linee pure diverse (due copie uguali di ogni gene) è un ibrido F1: "
        "cresce e rende il 20% in più, ma i suoi semi lo perdono. La Lente per semi segnala ibridi e linee pure. Mangime "
        "bilanciato (livello 5): chicchi di mais, avena, farina proteica, farina d'ossa e sale danno 4 mangimi che ogni "
        "animale della fattoria mangia: i piccoli crescono due volte più in fretta e i genitori tornano pronti prima.",
        "@5 Mutagenesi con raggi X (livello 5): l'Irradiatore di semi irradia i semi messi dentro (40 kJ a seme, da pile o "
        "dinamo collegate). 3 semi su 10 muoiono; ogni copia di gene degli altri muta 20 volte più spesso che in natura. "
        "Seminali tutti e tieni i rari utili.",
        "@6 Doppia elica del DNA (livello 6): la Lente per semi mostra ogni carattere. Grano e riso hanno un settimo "
        "gene, S, Statura: le piante ss sono semi-nane e con concime forte (NPK) rendono la metà in più, la Rivoluzione "
        "verde. Il grano selvatico dell'erba dei boschi di ciliegi a volte porta s.",
        "@6 Giardino gamma (livello 6): il Ciclotrone fa una sorgente di cobalto-60 con cobalto e acqua pesante nello "
        "slot del reagente; otto piombi intorno fanno il Giardino gamma. Le colture che maturano entro 6 blocchi danno "
        "semi che mutano 20 volte più spesso. È radioattivo: stai a distanza.",
        "@7 Sequenziamento del DNA (livello 7): il Sequenziatore di DNA legge l'intero genoma dei semi usati su di esso "
        "(20 kJ a seme). PCR (livello 7): il Termociclatore confronta ogni seme con un seme marcatore (usa un seme su di "
        "esso da accovacciato per impostarlo): i semi con tutti i caratteri utili del marcatore vanno in una tramoggia "
        "sotto, gli altri in tramogge ai lati (10 kJ a seme).",
        "@7 Transgenesi (livello 7): con un costrutto genico nell'altra mano, la Pistola genica usata su una coltura vi "
        "inserisce il gene; metà dei colpi attecchisce. Bt: i bruchi non la attaccano mai. EPSPS: Campo pulito la "
        "risparmia. Riso dorato (solo riso): i chicchi contengono vitamina A e si mangiano crudi. DREB: tollera la "
        "siccità. I semi ereditano un transgene da uno qualsiasi dei genitori. CRISPR (livello 7): il Kit CRISPR scrive "
        "la forma utile di un gene in entrambe le copie di una coltura; da accovacciato scegli il gene.",
    ],
    "de_de": [
        "# Moderne Züchtung",
        "@4 Die Mendelschen Regeln lehren auch das Kreuzungsheft: benutze es mit Samen in der anderen Hand, dann mit den "
        "Samen des anderen Elternteils. Es schreibt Gen für Gen auf, welcher Anteil der Samen dieser Kreuzung jedes "
        "Merkmal zeigt.",
        "@4 Silage (Stufe 4): das Silo macht aus Mais, Sorghum und Luzerne, von Hand oder per Trichter eingefüllt, "
        "Silage, eine alle 5 Sekunden; ein Trichter darunter holt sie heraus. Ein Futtertrog mit Silage reicht doppelt so "
        "lange. Stickstofffixierung (Stufe 4): Impfmittel an Soja, Bohnen oder Luzerne gibt ihren Wurzeln Rhizobien: reif "
        "geerntet hinterlassen sie ihr Feld stark gedüngt.",
        "@5 Hybridmais (Stufe 5): der Samen zweier verschiedener reiner Linien (zwei gleiche Kopien jedes Gens) ist eine "
        "F1-Hybride: sie wächst und trägt 20 % mehr, aber ihre eigenen Samen verlieren das. Die Samenlupe zeigt Hybriden "
        "und reine Linien. Mischfutter (Stufe 5): Maiskörner, Hafer, Proteinmehl, Knochenmehl und Salz ergeben 4 Futter, "
        "das jedes Nutztier frisst: Jungtiere wachsen doppelt so schnell, und Elterntiere sind früher wieder paarungsbereit.",
        "@5 Röntgenmutagenese (Stufe 5): der Samenbestrahler bestrahlt eingefüllte Samen (40 kJ je Samen, aus "
        "angeschlossenen Säulen oder Dynamos). 3 von 10 Samen sterben; jede Genkopie der übrigen mutiert 20-mal so oft "
        "wie in der Natur. Säe alle aus und behalte die seltenen nützlichen.",
        "@6 DNA-Doppelhelix (Stufe 6): die Samenlupe zeigt alle Merkmale. Weizen und Reis haben ein siebtes Gen, S, "
        "Wuchshöhe: ss-Pflanzen sind halbzwergig und tragen auf starkem Dünger (NPK) die Hälfte mehr, die Grüne "
        "Revolution. Wilder Weizen aus dem Gras von Kirschhainen trägt manchmal s.",
        "@6 Gammagarten (Stufe 6): das Zyklotron macht aus Cobalt mit schwerem Wasser im Reagenzplatz eine "
        "Cobalt-60-Quelle; acht Blei darum ergeben den Gammagarten. Feldfrüchte, die im Umkreis von 6 Blöcken reifen, "
        "geben Samen, die 20-mal so oft mutieren. Er ist radioaktiv: halte Abstand.",
        "@7 DNA-Sequenzierung (Stufe 7): der DNA-Sequenzierer liest das ganze Genom von Samen, die darauf angewandt "
        "werden (20 kJ je Samen). PCR (Stufe 7): der Thermocycler prüft jeden Samen gegen einen Markersamen (schleichend "
        "einen Samen darauf anwenden legt ihn fest): Samen mit allen nützlichen Merkmalen des Markers gehen in einen "
        "Trichter darunter, die übrigen in Trichter an den Seiten (10 kJ je Samen).",
        "@7 Transgenese (Stufe 7): mit einem Genkonstrukt in der anderen Hand bringt die Genkanone, an einer Feldfrucht "
        "benutzt, das Gen hinein; die Hälfte der Schüsse schlägt an. Bt: Raupen befallen sie nie. EPSPS: Feldräumer "
        "verschont sie. Goldener Reis (nur Reis): die Körner enthalten Vitamin A und sind roh essbar. DREB: verträgt "
        "Trockenheit. Samen erben ein Transgen von jedem der beiden Eltern. CRISPR (Stufe 7): das CRISPR-Kit schreibt die "
        "nützliche Form eines Gens in beide Kopien einer Feldfrucht; schleichend wählst du das Gen.",
    ],
    "sv_se": [
        "# Modern förädling",
        "@4 Mendels lagar lär också ut korsningsboken: använd den med frön i andra handen och sedan med den andra "
        "förälderns frön. Den skriver gen för gen hur stor del av fröna från korsningen som visar varje egenskap.",
        "@4 Ensilage (nivå 4): silon gör ensilage av majs, durra och lusern som läggs i för hand eller med en tratt, en "
        "var 5:e sekund; en tratt under tar ut den. Ett foderho med ensilage räcker dubbelt så länge. Kvävefixering (nivå "
        "4): ympmedel på sojabönor, bönor eller lusern ger rötterna rhizobier: skördade mogna lämnar de åkern väl "
        "gödslad.",
        "@5 Hybridmajs (nivå 5): ett frö av två olika rena linjer (två lika kopior av varje gen) är en F1-hybrid: den "
        "växer och ger 20 % mer, men dess egna frön förlorar det. Fröluppen visar hybrider och rena linjer. Balanserat "
        "foder (nivå 5): majskärnor, havre, proteinmjöl, benmjöl och salt ger 4 foder som alla gårdsdjur äter: ungarna "
        "växer dubbelt så fort och föräldrarna blir redo att para sig tidigare.",
        "@5 Röntgenmutagenes (nivå 5): frö-bestrålaren bestrålar frön som läggs i (40 kJ per frö, från staplar eller "
        "dynamor kopplade till den). 3 frön av 10 dör; varje genkopia hos resten muterar 20 gånger så ofta som i naturen. "
        "Så alla och behåll de sällsynta nyttiga.",
        "@6 DNA:s dubbelspiral (nivå 6): fröluppen visar alla egenskaper. Vete och ris har en sjunde gen, S, Växthöjd: "
        "ss-plantor är halvdvärgar och ger hälften mer på stark gödsel (NPK), den gröna revolutionen. Vilt vete från "
        "gräset i körsbärslundar bär ibland s.",
        "@6 Gammaträdgård (nivå 6): cyklotronen gör en kobolt-60-källa av kobolt med tungt vatten i reagensplatsen; åtta "
        "bly runt den ger gammaträdgården. Grödor som mognar inom 6 block från den ger frön som muterar 20 gånger så "
        "ofta. Den är radioaktiv: håll avstånd.",
        "@7 DNA-sekvensering (nivå 7): DNA-sekvenseraren läser hela genomet hos frön som används på den (20 kJ per frö). "
        "PCR (nivå 7): termocyklern testar varje frö mot ett markörfrö (använd ett frö på den smygande för att välja "
        "markör): frön med alla markörens nyttiga egenskaper går till en tratt under, resten till trattar vid sidorna (10 "
        "kJ per frö).",
        "@7 Transgenes (nivå 7): med en genkonstruktion i andra handen för genpistolen, använd på en gröda, in genen; "
        "hälften av skotten tar. Bt: larver angriper den aldrig. EPSPS: Fältrensare skonar den. Gyllene ris (bara ris): "
        "kornen har A-vitamin och kan ätas råa. DREB: tål torka. Frön ärver en transgen från vilken förälder som helst. "
        "CRISPR (nivå 7): CRISPR-kitet skriver in den nyttiga formen av en gen i båda kopiorna hos en gröda; smygande "
        "väljer du gen.",
    ],
    "da_dk": [
        "# Moderne forædling",
        "@4 Mendels love lærer også krydsningsbogen: brug den med frø i den anden hånd og så med den anden forælders "
        "frø. Den skriver gen for gen, hvor stor en del af frøene fra krydsningen der viser hver egenskab.",
        "@4 Ensilage (niveau 4): siloen gør majs, durra og lucerne, kommet i med hånden eller fra en tragt, til ensilage, "
        "en hvert 5. sekund; en tragt nedenunder tager den ud. En foderkrybbe med ensilage holder dobbelt så længe. "
        "Kvælstoffiksering (niveau 4): podemiddel på sojabønner, bønner eller lucerne giver rødderne rhizobier: høstet "
        "modne efterlader de marken godt gødet.",
        "@5 Hybridmajs (niveau 5): et frø af to forskellige rene linjer (to ens kopier af hvert gen) er en F1-hybrid: den "
        "vokser og giver 20 % mere, men dens egne frø mister det. Frøluppen viser hybrider og rene linjer. Balanceret "
        "foder (niveau 5): majskerner, havre, proteinmel, benmel og salt giver 4 foder, som alle husdyr spiser: ungerne "
        "vokser dobbelt så hurtigt, og forældrene er klar til at parre sig igen før.",
        "@5 Røntgenmutagenese (niveau 5): frøbestråleren bestråler frø, der kommes i (40 kJ pr. frø, fra stakke eller "
        "dynamoer koblet til den). 3 frø ud af 10 dør; hver genkopi i resten muterer 20 gange så ofte som i naturen. Så "
        "dem alle og behold de sjældne nyttige.",
        "@6 DNA's dobbeltspiral (niveau 6): frøluppen viser alle egenskaber. Hvede og ris har et syvende gen, S, Vækst: "
        "ss-planter er halvdværge og giver halvdelen mere på stærk gødning (NPK), den grønne revolution. Vild hvede fra "
        "græsset i kirsebærlunde bærer nogle gange s.",
        "@6 Gammahave (niveau 6): cyklotronen laver en kobolt-60-kilde af kobolt med tungt vand i reagenspladsen; otte "
        "bly omkring den giver gammahaven. Afgrøder, der modner inden for 6 blokke fra den, giver frø, der muterer 20 "
        "gange så ofte. Den er radioaktiv: hold afstand.",
        "@7 DNA-sekventering (niveau 7): DNA-sekventeringsmaskinen læser hele genomet i frø, der bruges på den (20 kJ pr. "
        "frø). PCR (niveau 7): termocykleren tester hvert frø mod et markørfrø (brug et frø på den sneget for at vælge "
        "markør): frø med alle markørens nyttige egenskaber går til en tragt nedenunder, resten til tragte i siderne (10 "
        "kJ pr. frø).",
        "@7 Transgenese (niveau 7): med et genkonstrukt i den anden hånd sætter genkanonen, brugt på en afgrøde, genet "
        "ind; halvdelen af skuddene tager. Bt: larver angriber den aldrig. EPSPS: Markrydder skåner den. Gylden ris (kun "
        "ris): kernerne har A-vitamin og kan spises rå. DREB: tåler tørke. Frø arver et transgen fra hvilken som helst "
        "forælder. CRISPR (niveau 7): CRISPR-sættet skriver den nyttige form af et gen ind i begge kopier i en afgrøde; "
        "sneget vælger du genet.",
    ],
    "ru_ru": [
        "# Современная селекция",
        "@4 Законы Менделя учат и тетради скрещиваний: примени её, держа семена в другой руке, а потом к семенам второго "
        "родителя. Она записывает, ген за геном, какая доля семян от этого скрещивания покажет каждый признак.",
        "@4 Силос (уровень 4): силосная башня делает из кукурузы, сорго и люцерны, заложенных вручную или воронкой, силос, "
        "по одному каждые 5 секунд; воронка снизу забирает его. Кормушки с силосом хватает вдвое дольше. Азотфиксация "
        "(уровень 4): инокулянт на сое, фасоли или люцерне даёт их корням ризобии: собранные спелыми, они оставляют "
        "пашню хорошо удобренной.",
        "@5 Гибридная кукуруза (уровень 5): семя двух разных чистых линий (две одинаковые копии каждого гена) это гибрид "
        "F1: он растёт и даёт на 20% больше, но его собственные семена это теряют. Семенная лупа отмечает гибриды и "
        "чистые линии. Сбалансированный корм (уровень 5): зёрна кукурузы, овёс, белковая мука, костная мука и соль дают "
        "4 корма, которые ест любой скот: молодняк растёт вдвое быстрее, а родители раньше готовы снова размножаться.",
        "@5 Рентгеновский мутагенез (уровень 5): облучатель семян облучает заложенные семена (40 кДж на семя от "
        "подключённых батарей или динамо-машин). 3 семени из 10 гибнут; каждая копия гена у остальных мутирует в 20 раз "
        "чаще, чем в природе. Посей все и оставь редкие полезные.",
        "@6 Двойная спираль ДНК (уровень 6): семенная лупа показывает все признаки. У пшеницы и риса есть седьмой ген, S, "
        "Рост: растения ss полукарликовые и на сильном удобрении (NPK) дают в полтора раза больше, это Зелёная "
        "революция. Дикая пшеница из травы вишнёвых рощ иногда несёт s.",
        "@6 Гамма-поле (уровень 6): циклотрон делает источник кобальта-60 из кобальта с тяжёлой водой в ячейке реагента; "
        "восемь свинца вокруг него дают гамма-поле. Посевы, созревающие в пределах 6 блоков, дают семена, мутирующие в 20 "
        "раз чаще. Оно радиоактивно: держись подальше.",
        "@7 Секвенирование ДНК (уровень 7): ДНК-секвенатор читает весь геном семян, применённых к нему (20 кДж на семя). "
        "ПЦР (уровень 7): термоциклер сверяет каждое семя с семенем-маркером (примени к нему семя, крадучись, чтобы задать "
        "маркер): семена со всеми полезными признаками маркера идут в воронку снизу, остальные в воронки по бокам (10 кДж "
        "на семя).",
        "@7 Трансгенез (уровень 7): с генной конструкцией в другой руке генная пушка, применённая к посеву, вводит в него "
        "ген; прививается половина выстрелов. Bt: гусеницы никогда не нападают. EPSPS: Расчистка поля его щадит. Золотой "
        "рис (только рис): зёрна содержат витамин A и съедобны сырыми. DREB: переносит засуху. Семена наследуют трансген "
        "от любого из родителей. CRISPR (уровень 7): набор CRISPR записывает полезную форму одного гена в обе копии "
        "посева; крадучись выбираешь ген.",
    ],
}
