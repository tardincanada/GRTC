"""Spanish (es_es) text of the Field Guide (2.5.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the es_es lang file.
"""

DECIMAL = ","

INTRO = [
    "# Bienvenida",
    "Cada bloque y cada objeto de este mundo está hecho de elementos reales. Esta guía sigue el árbol tecnológico del "
    "mod desde la Edad de Piedra hasta los últimos elementos, un capítulo por nivel.",
    "# Unidades",
    "1 unidad es 1 gramo. Un bloque son 1000 unidades, un lingote o un objeto de elemento o sustancia 111,1, una pepita "
    "12,35 y un cubo 1000. Pasa el cursor por encima de un bloque u objeto para ver de qué está hecho; mantén Mayús "
    "para verlo todo.",
    "# Investigación",
    "Cada nivel tiene su estación de investigación. Pon las muestras de un proyecto en sus 9 ranuras, elige el proyecto "
    "y pulsa Investigar. Los proyectos clave (marcados con una estrella) suben tu nivel tecnológico; necesitas los "
    "proyectos clave de un nivel antes de poder colocar la estación siguiente. /research muestra lo que sabes.",
    "Los tratados escritos en una estación enseñan todo lo que sabes a otro jugador.",
    "# Máquinas",
    "Una máquina descompone cada objeto que entra según su composición y aplica cada reacción que hayas investigado "
    "mientras esté lo bastante caliente y no demasiado. Los productos salen como objetos, los desechos como escoria, y "
    "algunos gases se expulsan. Los líquidos y los gases llenan matraces de vidrio vacíos desde el panel lateral de la "
    "máquina. Algunas reacciones necesitan un segundo ingrediente en la ranura de reactivo.",
    "Las máquinas calentadas a fuego queman el combustible de la ranura de combustible: la madera llega a 650 °C, el "
    "carbón vegetal y el carbón a 1100 °C, los combustibles del petróleo a 1200 °C y el gas a 1300 °C. Los fuelles "
    "(aire forzado) añaden 250 °C. El carbón vegetal y el carbón también sirven como el carbono que reduce las menas. "
    "Las máquinas eléctricas toman corriente de una pila voltaica, una dinamo u otro bloque de energía contiguo o "
    "unido por hilo de cobre.",
    "Los hornos normales ya no funden menas de hierro, cobre ni oro: los metales salen de las máquinas.",
    "# Nivel 0",
    "Sin ninguna investigación puedes construir una carbonera (8 bloques de tierra alrededor de un tronco) y "
    "carbonizar troncos en carbón vegetal.",
]

CONTAINERS = [
    "# Matraces de vidrio",
    "Cada objeto líquido o gaseoso es un matraz de vidrio tapado que contiene 111,1 unidades. Cinco vidrios dan cuatro "
    "matraces, sin investigación. Una máquina llena un matraz vacío desde la ranura superior de su panel lateral por "
    "cada líquido o gas que produce; sin matraces, se detiene y los pide. Cuando una máquina, una receta, un horno o un "
    "proyecto de investigación usa el contenido de un matraz, recuperas el matraz vacío.",
    "# Bombonas de acero y compresor",
    "Compresión de gases (nivel 4) desbloquea las bombonas de acero y el compresor. Una bombona contiene 1000 unidades "
    "de un gas comprimido: lo de nueve matraces en un solo objeto. Instala un compresor en una de las dos ranuras de "
    "mejora de una máquina y pon bombonas vacías en la segunda ranura de su panel lateral: la máquina llenará entonces "
    "bombonas con cada gas que produzca, una bombona cada vez. Los líquidos siguen yendo a matraces. Una bombona de gas "
    "combustible arde como nueve matraces.",
    "Las máquinas aceptan bombonas como entrada igual que cualquier otro objeto y devuelven la bombona vacía.",
    "# Tanque de almacenamiento",
    "Química neumática (nivel 3) desbloquea el tanque de almacenamiento. Contiene 16 000 unidades (144 matraces) de un "
    "solo líquido o gas. Úsalo con un matraz o una bombona llenos para vaciarlos dentro, con un matraz vacío para sacar "
    "uno, y con la mano vacía para leer su nivel. Las tolvas también sirven: mete los recipientes llenos o vacíos por "
    "arriba o por los lados, y recoge lo que sale por abajo. Un comparador lee cuánto está lleno. Romperlo derrama su "
    "contenido.",
]

UPGRADES = [
    "# Ranuras de mejora",
    "Cada máquina tiene dos ranuras de mejora en la parte inferior de su panel lateral. Una mejora funciona en cuanto su "
    "propietario la ha investigado; dos iguales cuentan una sola vez. Las mejoras marcadas «de combustible» no hacen "
    "nada en las máquinas eléctricas.",
    "- Compresor (Compresión de gases, nivel 4): llena bombonas de acero con los gases que produce la máquina.",
    "- Expulsor de salida (Molino automático, nivel 3): mueve los productos de la máquina a un cofre, barril o tolva "
    "contiguos. Nunca alimenta directamente a otra máquina.",
    "- Regenerador (Horno regenerativo, nivel 4): una máquina de combustible pierde la mitad de calor, así que quema "
    "menos combustible y se calienta antes.",
    "- Revestimiento refractario (Refractarios básicos, nivel 4): una máquina de combustible puede pasar 150 °C de su "
    "propio límite, si su combustible y sus fuelles llegan a tanto.",
    "- Campana de recuperación de gases (Recuperación de subproductos, nivel 4): los líquidos y gases que la máquina "
    "expulsaría (dióxido de carbono, dióxido de azufre, ácido clorhídrico y otros) van a matraces o bombonas. El vapor y "
    "los gases mezclados como el gas de hulla se siguen expulsando. El gas recogido nunca detiene la máquina: sin "
    "recipiente ni espacio, se expulsa de todos modos.",
    "- Agitador mecánico (Motor eléctrico, nivel 4): procesa la carga un 50 % más rápido. Su motor quema una cuarta "
    "parte más de combustible mientras la carga reacciona, o consume corriente extra en una máquina eléctrica.",
    "- Lecho catalítico (Lechos catalíticos, nivel 5): las reacciones sobre un catalizador sólido (hierro, níquel, "
    "platino, platino-rodio, pentóxido de vanadio, bromuro de cobalto-manganeso) van el doble de rápido: amoníaco, "
    "proceso de contacto, ácido nítrico, hidrogenación del carbón, reformado, nailon y más.",
    "- Cinta transportadora (Caucho Buna, nivel 5): un expulsor de salida sobre una cinta de caucho; mueve los "
    "productos terminados de cuatro en cuatro en lugar de uno a uno.",
    "- Aislamiento de espuma (Estireno y poliestireno, nivel 6): una máquina cuyo límite es de 300 °C o menos pierde "
    "un 70 % menos de calor.",
    "- Filtro de aire estéril (Penicilina, nivel 6): el barril de fermentación, la cuba de vinagre, el fermentador de "
    "tanque profundo y el biorreactor producen un 10 % más. El fermentador de tanque profundo no funciona sin él.",
]

FARM = [
    "# Grasas y jabón",
    "Fundición de grasas (nivel 1) desbloquea la caldera de jabón. Entran carne, huesos, huevos y leche; la caldera los "
    "cuece a fuego lento entre 80 y 150 °C. El sebo flota, y la carne y el hueso que quedan salen como harina de "
    "proteínas, que comen vacas, ovejas, cabras, cerdos y gallinas y que sirve de buen compost. Sebo con hilo da velas "
    "de sebo: luz 12, humeantes, y se consumen tres días después de colocarlas.",
    "Fabricación de jabón: pon sebo o aceite vegetal en la caldera y ceniza de madera en la ranura de reactivo para "
    "obtener jabón blando, con glicerol en matraces. Jabón duro (nivel 2) usa carbonato de sodio en su lugar, o "
    "endurece el jabón blando con sal.",
    "Usa una pastilla de jabón para lavarte: quedas Limpio durante diez minutos, y Limpio elimina el Hambre de la carne "
    "podrida y del pollo crudo. Con una armadura de cuero teñida en la otra mano, el jabón le quita el tinte. Jabón con "
    "lana o una cama en la cuadrícula de fabricación los lava en blanco.",
    "# Aceite, cuero y mosto",
    "Prensado de aceite (nivel 1): la prensa de aceite prensa en frío semillas de calabaza, sandía y trigo, granos de "
    "cacao y girasoles. El aceite vegetal llena matraces; la torta oleaginosa que queda es pienso para el ganado. El "
    "sebo y el aceite vegetal también arden como combustible.",
    "Curtido en foso (nivel 1): el foso de curtido convierte carne y pieles en cuero sin cuadrícula de fabricación, "
    "despacio: cuatro carnes podridas dan un cuero en unos tres días de juego. Pon troncos de roble o de abeto en la "
    "ranura de reactivo por su tanino; la corteza curtiente agotada que queda arde como combustible. Fabricación de "
    "alumbre (nivel 2) da alumbre de potasio, y dos pieles de conejo con un alumbre se curten en cuero en la cuadrícula "
    "de fabricación.",
    "Maceración (nivel 1): el barril de fermentación convierte el almidón del trigo y las patatas en azúcar y luego en "
    "alcohol. Azúcar de almidón (nivel 3) hace el primer paso más rápido con un poco de ácido sulfúrico en la cuba de "
    "digestión. Aceite dulce de vitriolo (nivel 2): el etanol destilado con ácido sulfúrico en el alambique da éter "
    "dietílico.",
    "# Fertilizantes",
    "Superfosfato (nivel 3) disuelve huesos o roca fosfórica en ácido sulfúrico; Aguas amoniacales (nivel 3) lava el "
    "amoníaco del horno de coque (deja matraces en su ranura de matraces) y la cámara de plomo lo convierte en sulfato "
    "de amonio. Usa cualquiera de los dos fertilizantes en tierra de cultivo: durante las ocho cosechas siguientes, el "
    "cultivo crece la mitad más rápido y cada cosecha madura da uno más.",
    "# Caucho",
    "Los árboles del caucho crecen silvestres en las junglas (jungla, jungla dispersa y jungla de bambú). Sus hojas "
    "sueltan brotes, y el brote crece en cualquier sitio, pero un árbol del caucho solo da látex en una jungla. "
    "Vulcanización (nivel 3) desbloquea el sangrador: fíjalo al lado de un tronco de árbol del caucho y úsalo con un "
    "matraz de vidrio vacío; llena un matraz cada cinco minutos y guarda cuatro. Las tolvas pueden darle matraces "
    "vacíos por arriba y sacar el látex por abajo. Fuera de la jungla avisa de que el clima no es el adecuado.",
    "En el vulcanizador, el látex con azufre en la ranura de reactivo se cura en caucho vulcanizado entre 130 y 160 °C; "
    "sin azufre solo se seca en caucho en bruto. El caucho vulcanizado da zapatillas (tus pasos no producen "
    "vibraciones, y el magma y los huevos de tortuga no te notan) y ruedas de caucho: úsalas en una vagoneta y "
    "correrá un 25 % más rápido.",
]

TAR = [
    "# Alquitrán de hulla",
    "Destilación del alquitrán (nivel 3): una vez investigada, el horno de coque recoge el alquitrán del carbón en "
    "matraces (deja matraces vacíos en su ranura de matraces), cerca de un matraz por cada 13 carbones. El alambique de "
    "alquitrán separa el alquitrán de hulla en benceno, tolueno, fenol (ácido fénico) y antraceno, y deja brea. La brea "
    "con grava y arena da macadán, un bloque de carretera tan rápido como el asfalto. Benceno, fenol, alquitrán y brea "
    "arden como combustible. Nitrobenceno (nivel 3) nitra el benceno en la cuba de digestión, y Anilina (nivel 4) lo "
    "reduce con hierro.",
    "# Tintes",
    "Azul de Prusia (nivel 2) desbloquea la cuba de tinte: carne podrida o carne con vitriolo verde en la ranura de "
    "reactivo da el pigmento, y la cuba lo convierte en tinte azul, 8 por objeto. Los colorantes del alquitrán dan 32 "
    "tintes normales por matraz: fucsina (anilina y arsénico blanco, magenta), ácido pícrico (fenol y ácido nítrico en "
    "la cuba de digestión, amarillo) y alizarina (antraceno y sosa cáustica en el autoclave, rojo). Pon el colorante en "
    "la cuba de tinte para obtener tintes normales.",
    "# Infección y medicina",
    "Un golpe de zombi, disecado, ahogado o aldeano zombi te infecta 1 vez de cada 200, y comer carne podrida 1 vez de "
    "cada 100; estar Limpio reduce ambas a la mitad. Una Infección dura un día de juego con Debilidad y Hambre. Sin "
    "tratamiento, se cura sola 7 veces de cada 10; las demás se convierte en Sepsis, 1 de daño cada 10 segundos hasta "
    "que mueres. El jabón fénico (Cirugía antiséptica: fenol y jabón duro) y la aspirina (nivel 4: fenol en el "
    "autoclave con carbonato de sodio, luego ácido acético en la cuba de digestión) curan una Infección antes de que se "
    "agrave. Solo las sulfamidas (nivel 5) y los antibióticos del nivel 6 curan la Sepsis. La leche no cura ninguna de "
    "las dos. La regla de juego periodicElementsInfection desactiva la infección.",
    "La aspirina también quita la Náusea y la Fatiga minera y da 10 segundos de Regeneración. El éter anestésico (éter "
    "sobre lana), usado en un aldeano zombi cuya cura ya ha empezado, reduce a la mitad el tiempo de la cura.",
    "# Explosivos",
    "Cartucho de dinamita (nivel 4): el glicerol de la fabricación de jabón, nitrado en frío (de 5 a 25 °C) en la cuba de "
    "digestión, da nitroglicerina; con arena y papel da cartuchos de dinamita, y cuatro cartuchos con hilo dan una carga minera. "
    "Enciéndela con un mechero o con redstone: a los 4 segundos rompe cada bloque de un cubo de 5x5x5 menos resistente "
    "que la obsidiana y todos ellos caen. Con mobGriefing desactivado solo hiere a lo que esté cerca. Cordita (nivel 4) "
    "amasa la nitroglicerina con pólvora sin humo; los cartuchos de cordita disparan desde el fusil de retrocarga un "
    "25 % más fuerte y sin humo.",
    "# Cultivos y plagas",
    "El caldo bordelés (vitriolo azul, cal viva y agua) rociado sobre un cultivo o un arbusto de bayas evita que se lo "
    "lleve el agua o se marchite en la oscuridad, y los arbustos de bayas dejan de arañar. Polvo insecticida: la prensa "
    "de aceite muele margaritas en pelitre, y el insecticida en aerosol hiere a arañas, lepismas, endermites y abejas "
    "hasta 4 bloques por delante.",
    "# Bomba de vacío",
    "Destilación al vacío (nivel 4) desbloquea la mejora Bomba de vacío: en un alambique, un alambique de alquitrán, "
    "una caldera de jabón, un cristalizador, una salina o un alambique de refinería, cada reacción ocurre 100 °C más "
    "fría, así que la máquina se calienta antes y pierde menos calor.",
]

INDUSTRY = [
    "# Carburo y acetileno",
    "Carburo de calcio (nivel 5): la cal viva con coque en la ranura de combustible del horno de arco eléctrico se "
    "funde en carburo a 1800 °C o más. En la cuba de digestión con agua en la ranura de reactivo, el carburo da "
    "acetileno (deja matraces, o bombonas y un compresor, en la cuba) y deja cal apagada.",
    "La lámpara de carburo (latón, vidrio, carburo y un cubo de agua) da luz 14 sostenida en cualquier mano o llevada "
    "en la cabeza, durante media hora. Úsala con carburo de calcio en tu inventario para recargarla; una lámpara vacía "
    "no se rompe.",
    "Soldadura oxiacetilénica: fabrica un soplete con una bombona de acetileno y una de oxígeno, un lingote de acero y "
    "un lingote de cobre. Sostén una herramienta o pieza de armadura de metal dañada en la otra mano y usa el soplete: "
    "un lingote de su propio metal sacado de tu inventario repara una cuarta parte, sin experiencia. El soplete hace 64 "
    "soldaduras; vuelve a fabricarlo con bombonas nuevas para recargarlo.",
    "Cal nitrogenada: el carburo en el horno de cal entre 1000 y 1150 °C con nitrógeno en la ranura de reactivo absorbe "
    "el nitrógeno como cianamida cálcica, un fertilizante.",
    "# Fertilizantes del aire",
    "Síntesis de la urea: amoníaco en el reactor de alta presión entre 170 y 200 °C, con dióxido de carbono en la "
    "ranura de reactivo (recógelo de un horno de cal o de un barril de fermentación con una campana de recuperación de "
    "gases). Fertilizante perlado desbloquea la torre de perlado, una máquina eléctrica: el amoníaco con ácido nítrico "
    "se convierte en nitrato de amonio, y la urea con mezcla PK (superfosfato y potasa) en perlas NPK.",
    "La cal nitrogenada, la urea y el nitrato de amonio actúan en la tierra de cultivo como el superfosfato. Las perlas "
    "NPK hacen crecer el cultivo el doble de rápido, durante ocho cosechas, cada una con uno más.",
    "# Plásticos y caucho",
    "Formaldehído: metanol sobre plata en el convertidor Ostwald entre 600 y 700 °C. Baquelita: fenol con formaldehído "
    "en la ranura de reactivo, en el autoclave entre 150 y 200 °C. Cuatro baquelitas dan cuatro bloques de baquelita, "
    "marrones, o negros, rojos, verdes, marfil o ámbar con un tinte. La baquelita también sirve de carcasa al contador "
    "Geiger en lugar del latón.",
    "Neopreno: acetileno con ácido clorhídrico en el reactor de alta presión. Un traje de neopreno completo da Gracia "
    "de delfín en el agua, y ninguna de sus piezas deja pasar el frío de la nieve polvo.",
    "Caucho Buna, sin jungla: el etanol sobre el catalizador en la unidad de craqueo entre 380 y 450 °C da butadieno; "
    "el benceno con etileno en la ranura de reactivo entre 550 y 600 °C da estireno; el butadieno con estireno en la "
    "cuba de digestión entre 40 y 60 °C da caucho Buna. Sirve para las ruedas de caucho y la mejora Cinta "
    "transportadora.",
    "# Explosivos",
    "TNT: el tolueno del alambique de alquitrán, nitrado en la cuba de digestión. Un TNT con tres papeles da tres "
    "bloques de dinamita normal. Cuatro TNT, un lingote de acero e hilo dan una carga de demolición: encendida como "
    "una carga minera, rompe un cubo de 3x3x3 de todo salvo la piedra base, obsidiana incluida, y todo cae.",
    "# Fermentación, combustible y medicina",
    "Fermentación acetónica: trigo o patatas en el barril de fermentación, con calcita en la ranura de reactivo, "
    "fermentan en acetona, butanol y un poco de etanol en lugar de azúcar. Biodiésel: sebo o aceite vegetal en la "
    "caldera de jabón entre 40 y 150 °C con metanol en la ranura de reactivo. El biodiésel y el butanol mueven el "
    "generador diésel y arden en los hornos.",
    "Sulfamidas: la anilina con sulfato de amonio en la cuba de digestión da sulfanilamida. Úsala para curar una "
    "Infección en cualquiera de sus fases, Sepsis incluida.",
]

LIFE = [
    "# Antibióticos",
    "Penicilina (nivel 6) desbloquea el fermentador de tanque profundo, una máquina eléctrica que solo funciona con un "
    "filtro de aire estéril. La glucosa con leche en la ranura de reactivo (por su lactosa) cultiva penicilina entre 20 "
    "y 37 °C; la estreptomicina lleva harina de proteínas en la ranura de reactivo. Cualquiera de los dos antibióticos "
    "cura una Infección o una Sepsis y te hace inmune a la Infección durante 20 minutos. Usa penicilina en un aldeano "
    "zombi con Debilidad y su cura empieza, sin manzana dorada. El micelio agotado del tanque es fertilizante.",
    "# Plaguicidas",
    "DDT: benceno con cloro en la cuba de digestión. Un espolvoreador de DDT usado sobre el suelo lo vuelve mortal para "
    "arañas, lepismas, endermites y abejas durante tres días de juego; las gallinas que están encima ponen la mitad. El "
    "herbicida (2,4-D, de fenol y cloro) mata flores, helechos, enredaderas, arbustos, brotes y hojas alrededor del "
    "bloque, pero respeta la hierba y los cultivos. El preemergente (atrazina, de amoníaco y cloro con coque en el "
    "reactor de alta presión) impide que la hierba y el micelio se extiendan por el suelo durante diez días de juego. "
    "Cada pulverizador tiene 16 usos y actúa a 4 bloques alrededor.",
    "# Plásticos",
    "Estireno y poliestireno: estireno en el reactor de alta presión. Ocho poliestirenos alrededor de un matraz de "
    "metano dan aislamiento de espuma; cuatro poliestirenos y metano dan cuatro bloques de aislante, y el hielo o la "
    "nieve que toque el aislante nunca se derrite.",
    "# Explosivos",
    "Explosivo plástico: el formaldehído y el amoníaco dan hexamina, y la hexamina nitrada en frío da RDX. Dos RDX, un "
    "lingote de cobre, un lingote de acero e hilo dan una carga hueca: colócala contra una pared y enciéndela, y abre "
    "un túnel de 1x1 y 16 bloques de profundidad a través de todo salvo la piedra base. ANFO: seis nitratos de amonio, "
    "gasóleo, papel e hilo dan una carga de ANFO que rompe un cubo de roca de 9x9x9; caen tres de cada cuatro bloques.",
    "# Contramedidas radiológicas",
    "El yoduro de potasio (yoduro de sodio de las algas con sal potásica en el cristalizador) reduce un 40 % la dosis "
    "de los productos de fisión durante un día de juego. Una cápsula de azul de Prusia (azul de Prusia y harina de "
    "proteínas) la reduce una cuarta parte. La amifostina (nivel 7) reduce a la mitad el daño de toda radiación "
    "durante una hora.",
    "# Biotecnología",
    "ADN recombinante (nivel 7) desbloquea el biorreactor (necesita un circuito integrado). Fermenta la celulosa de "
    "troncos, hojas o paja en etanol, la glucosa en PLA, y la proteína con aceite vegetal en la vacuna de ARNm, siete "
    "días de juego sin Infección. Su biomasa es fertilizante. El PLA da macetas y marcos, y sirve de compost.",
    "Glifosato: formaldehído con ácido fosfórico en el reactor de alta presión. Campo limpio mata todas las plantas "
    "alrededor del bloque, cultivos incluidos, salvo el trigo de semillas tolerantes (ocho semillas de trigo alrededor "
    "de un glifosato). El trigo tolerante también crece una décima parte más rápido.",
]

MINING = [
    "# Mazos y excavadoras",
    "Mazos de cantero (nivel 2) enseña el mazo (cinco lingotes alrededor de un palo, sobre un palo) y la excavadora "
    "(tres lingotes y dos palos), en acero y en cada metal de herramienta posterior. Cada uno rompe un cuadrado de 3x3 "
    "en la cara que golpeas, a tres décimas de la velocidad, y se desgasta un punto por bloque. Agáchate para picar un "
    "solo bloque. Solo se rompen los bloques adecuados para la herramienta y no más duros que el que apuntaste; nada "
    "con inventario ni ninguna máquina se rompe nunca; lo que sueltan va directo a tu inventario.",
    "# Voladuras",
    "Voladura de roca (nivel 2): cuatro pólvoras negras, papel e hilo dan dos cargas de voladura. Encendida con un "
    "mechero o con redstone, una carga rompe un bloque de piedra de 3x3x3 y lo suelta todo; la tierra, la madera y las "
    "máquinas se salvan.",
    "# Perforadoras y brocas",
    "@4 Toda perforadora necesita una broca: sostén la broca en la otra mano, agáchate y usa la perforadora. La broca "
    "decide qué puede picar y a qué velocidad (acero al wolframio 8,5, acero rápido 10, metal duro 12, diamante 13) y "
    "se desgasta un punto por golpe. Encanta la broca, no la perforadora; una broca montada le presta a la perforadora "
    "sus encantamientos, todos menos Reparación. Agáchate y úsala sin broca en la otra mano para cambiar de modo. Una "
    "perforadora gasta 1,5 kJ por punto de dureza de cada bloque: 2,25 para la piedra, 4,5 para la pizarra profunda.",
    "Perforadora de roca (nivel 4): el compresor de aire, una máquina eléctrica, llena bombonas de acero con aire "
    "comprimido, sin necesidad de la mejora Compresor. La perforadora neumática gasta una bombona cada 600 kJ (unas 400 "
    "piedras) y devuelve la vacía; pica un bloque, un túnel de 1x3 o de 3x3, y te ralentiza mientras la sostienes.",
    "# Herramientas de batería",
    "Batería de plomo-ácido (nivel 4): la batería contiene 1500 kJ. Conecta un banco de carga al hilo de cobre y usa la "
    "batería sobre él; se carga hasta 5 kJ por tick.",
    "El martillo perforador (nivel 5) es la perforadora de roca con batería, sin ralentizarte. La motosierra (nivel 5) "
    "tala un árbol entero, hasta 128 troncos, por 3 kJ por tronco.",
    "La perforadora minera pesada (nivel 6) añade un modo de 5x5 y un modo de veta que sigue una mena hasta 32 bloques; "
    "con una batería de plomo-ácido te ralentiza.",
    "La batería de iones de litio (nivel 7) contiene 6000 kJ, se gasta primero y quita esa lentitud. Perforadora de "
    "prospección (nivel 7): en modo de veta, usa la perforadora minera pesada sobre una mena para leer el tamaño y los "
    "elementos de la veta antes de cavar.",
]

ISOTOPES = [
    "# Radiación",
    "Los objetos radiactivos emiten una dosis que se acumula mientras los llevas o estás cerca de ellos. Un "
    "electroscopio, y más tarde un contador Geiger, la mide. Los muros de plomo, la celda caliente y el reactor nuclear "
    "blindan lo que contienen. Algunas muestras se desintegran según el reloj del juego (un día de juego es para ellas "
    "un día real) y se convierten en sus descendientes en tu inventario.",
    "# Brillo azul",
    "Los emisores más fuertes brillan en azul en la oscuridad, como la luz de Cherenkov: el radio y los más fuertes en "
    "el aire, y en el agua también el polonio, el americio, el actinio y la pechblenda, con más intensidad. Un reactor "
    "nuclear en marcha ilumina su piscina, y los aceleradores en marcha también brillan. El brillo depende de lo "
    "concentrada que esté la actividad, no de cuánto tengas; el uranio nunca brilla. Los objetos que brillan también "
    "brillan en su casilla del inventario.",
    "# Isótopos naturales",
    "La descripción de cada elemento estable enumera sus isótopos naturales, del más abundante al menos.",
    "# Proporciones variables",
    "Los objetos de uranio (U-235), plutonio (Pu-240), litio (Li-6), boro (B-10), calcio (Ca-48) e hidrógeno (H-2) "
    "muestran su propia proporción. Un objeto con la proporción natural se apila con cualquier otro; uno enriquecido o "
    "empobrecido solo se apila con objetos de la misma proporción. Las máquinas mezclan las proporciones según la masa.",
    "- El calutrón (Enriquecimiento por calutrón) separa el uranio, el litio o el boro en una pequeña fracción "
    "enriquecida y unas colas empobrecidas. Vuelve a meter la fracción para enriquecer más.",
    "- El uranio metálico con un 3 % de U-235 o más se sinteriza en pastillas de combustible para el reactor.",
    "- El plutonio generado en el reactor es de grado militar (6 % de Pu-240); el plutonio del combustible gastado es "
    "de grado reactor.",
    "- El litio con más de un 90 % de Li-6 en las ranuras de un reactor se convierte en tritio, que enciende las luces "
    "de tritio para siempre.",
    "- El boro con más de un 90 % de B-10 da barras de control: con una en sus ranuras, un reactor deja de quemar "
    "combustible mientras esté lleno.",
    "- El agua pesada de la cascada de enriquecimiento (aliméntala con hielo o nieve) permite quemar barras de "
    "combustible de uranio natural.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Primeros pasos",
    "group": "Nivel {n}",
    "research": "Investigación",
    "machines": "Máquinas",
    "reactions": "Reacciones",
    "recipe": " Receta: {r}.",
    "recipe_head": "Receta: {r}.",
    "projects": "# Proyectos (★ = clave)",
    "needs": " Requiere {x}.",
    "samples": " Muestras: {x}.",
    "unlocks": " Desbloquea {x}.",
    "up_to": "{desc}. Hasta {c} °C",
    "electric": ", eléctrica.",
    "fuel": ", de combustible.",
    "amounts": "Cantidades por cada 100 unidades de carga.",
    "ores": "Menas",
    "ores_head": "# Menas",
    "ores_intro": "Las menas aparecen en los chunks recién explorados. Toque de seda y Fortuna funcionan como en las "
                  "menas normales.",
    "ore_line": "{name}: {desc}. Suelta {lo}-{hi} {drop}; requiere un pico de {tier}. ",
    "found": "Se encuentra {w}.",
    "where": "de y {lo} a {hi} {b}",
    "biomes": "en: {b}",
    "overworld": "en la Superficie",
    "nether": "en el Nether",
    "power": "Energía",
    "power_head": "# Energía",
    "power_intro": "Las máquinas eléctricas toman corriente de los bloques de energía contiguos, o unidos por hilo de "
                   "cobre (hasta 64 bloques de hilo) o cable aislado (cuatro veces más lejos).",
    "power_line": "{name} (nivel {lvl}, {project}): {desc}.",
    "isotopes": "Radiación e isótopos",
    "containers": "Recipientes",
    "upgrades": "Mejoras",
    "farm": "Granja y grasas",
    "tar": "Alquitrán y remedios",
    "industry": "Química industrial",
    "life": "Ciencias de la vida",
    "mining": "Minería",
    "crafting": "Fabricación",
    "craft_head": "# Fabricación",
    "craft_intro": "Las recetas sin forma que añade el mod (las recetas de máquinas y bloques están en los capítulos "
                   "de cada nivel).",
    "shaped_head": "# Recetas con forma (filas de arriba abajo)",
    "or": " o ",
    "any": "cualquier {x}",
    "empty": "vacío",
    "reagent": "(reactivo)",
    "carbon": " + {c} de carbono",
    "nothing": "no se guarda nada",
    "slag": "; escoria: ",
    "vents": "; expulsa ",
    "temp": ". De {a} a {b} °C.",
    "sep": "; ",
}

WORDS = {
    # pickaxe tiers
    "wood": "madera", "stone": "piedra", "iron": "hierro",
    # biomes and biome tags
    "mountain": "montañas", "jungle": "junglas", "savanna": "sabanas", "desert": "desierto",
    "badlands": "tierras baldías", "ocean": "océanos", "swamp": "pantanos", "mangrove swamp": "manglares",
    "basalt deltas": "deltas de basalto", "beach": "playas", "river": "ríos", "dripstone caves": "cuevas kársticas",
    # item tags
    "logs": "tronco", "logs that burn": "tronco combustible", "oak logs": "tronco de roble", "planks": "tablones",
    "wool": "lana", "beds": "cama", "cable insulation": "aislamiento de cables", "soap": "jabón",
    # upgrades unlocked by research
    "forced air": "aire forzado (fuelles)", "steam blast": "soplado de vapor",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["es_es"]
T["atlas"] = _ATLAS_TITLE["es_es"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["es_es"]
T.update(_TREES_T["es_es"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("es_es")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["es_es"]
T["compendium"] = _COMP_TITLE["es_es"]

# 3.5.0 Plant Compendium (tools/compendium_plants_es_es.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["es_es"]
T["plant_compendium"] = _PCOMP_TITLE["es_es"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_es_es.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["es_es"]
T["mineral_compendium"] = _MCOMP_TITLE["es_es"]
