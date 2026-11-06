"""Compendio de Plantas (3.5.0) en español. Ver compendium_plants.py y compendium_i18n.py."""

TITLE = "Compendio de Plantas"

GUIDE = [
    "# El Compendio de Plantas",
    "Un libro y cualquier flor pequeña hacen el Compendio de Plantas; no hace falta investigar. Funciona como el "
    "Compendio de Árboles: llévalo en la barra rápida y la esquina superior izquierda nombra el bioma y, cuando ya la "
    "conoces, la planta que miras. Flores, hierbas, helechos, arbustos, cultivos, plantas acuáticas y las plantas de "
    "las cuevas frondosas están todas en él.",
    "Para anotar una planta, mírala un momento desde hasta 8 bloques, o recógela o coséchala, con el libro en la barra "
    "rápida; las plantas bajo el agua se anotan igual. Usa el libro para leer: biomas a la izquierda, las plantas de "
    "cada uno a la derecha.",
    "Las Notas del Compendio copian los dos libros a la vez: quien las lee gana todos los árboles, plantas y biomas "
    "que contienen.",
    "Species Plantarum (nivel 3): cada planta muestra su nombre científico, su familia y dónde crece silvestre en el "
    "mundo real.",
    "Geografía de las Plantas (nivel 3): cada bioma muestra su clima, en este libro como en el Compendio de Árboles.",
    "La Lista Roja (nivel 6): cada planta muestra cuán amenazada está en la naturaleza.",
]

UI = [
    "Compendio de Plantas",
    "Planta desconocida",
    "Nueva planta en tu Compendio: %s",
    "%s de %s plantas y %s de %s biomas anotados.",
    "Lleva este libro en la barra rápida. Mira una planta un momento, o recógela o coséchala, para anotarla; entra en un "
    "bioma para anotar el bioma. Elige un bioma a la izquierda.",
    "Plantas",
    "Una planta solo de este mundo: ninguna especie real le corresponde.",
    "Compendio de Plantas",
    "En la barra rápida muestra el bioma y nombra las plantas que conoces; úsalo para leer sobre ellas",
]

PLANTS = {
    "dandelion": ("Diente de león", "Europa y Asia; hoy en todos los continentes salvo la Antártida",
                  "Su cabeza amarilla se cierra de noche y madura en una bola de semillas que el viento lleva "
                  "kilómetros. Todo en ella es comestible, y sus hojas fueron ensalada de primavera mucho antes que la "
                  "lechuga."),
    "poppy": ("Amapola", "Europa, norte de África y oeste de Asia",
              "La amapola sigue al arado: sus semillas esperan décadas en el suelo y brotan cuando se remueve la "
              "tierra, y por eso cubrió los campos de batalla de Flandes después de 1914."),
    "blue_orchid": ("Orquídea azul", "Bosques de colina de Assam, Birmania y el norte de Tailandia",
                    "Una de las poquísimas orquídeas de verdad azules. En la naturaleza vive aferrada a troncos en "
                    "bosques frescos de colina; los coleccionistas vaciaron valles enteros, y hoy su comercio "
                    "silvestre está prohibido."),
    "allium": ("Allium", "Montañas de Irán, Afganistán y Asia Central",
               "Una cebolla ornamental: una bola de cientos de florecitas moradas sobre un tallo alto. Aplasta una hoja "
               "y huele a cebolla, como sus parientes el ajo y el puerro."),
    "azure_bluet": ("Houstonia", "Este de Norteamérica",
                    "Una florecita de cuatro pétalos que cubre céspedes y prados de azul pálido en primavera. Tan "
                    "pequeña, es de la familia del café."),
    "tulip": ("Tulipán", "Estepas de montaña de Turquía y Asia Central",
              "Llegó del Imperio otomano a los Países Bajos en el siglo XVI; en 1637 un solo bulbo podía costar más "
              "que una casa, hasta que el mercado se hundió. Los pétalos rayados tan codiciados los causaba un virus."),
    "oxeye_daisy": ("Margarita", "Europa y Asia templada",
                    "La margarita blanca de prados y cunetas. Cada flor es en realidad cientos de florecitas: las "
                    "amarillas en el centro, las blancas alrededor."),
    "cornflower": ("Aciano", "Sureste de Europa y Oriente Medio, extendido con el grano por toda Europa",
                   "Antes era mala hierba de todo trigal, y casi desapareció de Europa cuando los agricultores "
                   "empezaron a limpiar la semilla y usar herbicidas. Su azul dio nombre a un color."),
    "lily_of_the_valley": ("Lirio de los valles", "Bosques frescos de Europa y Asia",
                           "Campanitas blancas y perfumadas en los bosques sombríos de primavera. Toda la planta es "
                           "venenosa: tiene glucósidos cardíacos como la dedalera, que la medicina usa contra la "
                           "insuficiencia cardíaca."),
    "sunflower": ("Girasol", "Norteamérica; cultivado en México hace unos 4000 años",
                  "Las cabezas jóvenes siguen al sol de este a oeste cada día; la flor abierta queda mirando al este. "
                  "De sus semillas se saca uno de los principales aceites de cocina del mundo."),
    "lilac": ("Lila", "Colinas rocosas de los Balcanes",
              "Un arbusto de la familia del olivo con racimos de flores moradas y fragantes a final de primavera. "
              "Silvestre solo en las laderas balcánicas, los jardines lo han llevado por todo el mundo."),
    "rose_bush": ("Rosal", "Europa, noroeste de África y oeste de Asia",
                  "El rosal silvestre trepa por los setos con espinas en gancho. Sus escaramujos rojos tienen más "
                  "vitamina C que la naranja; en la Gran Bretaña en guerra los niños los recogían para hacer jarabe."),
    "peony": ("Peonía", "Siberia, Mongolia y el norte de China",
              "Grandes flores rosas cuyos capullos sueltan azúcar que las hormigas vienen a beber. Cultivada en China "
              "desde hace más de mil años, allí la llaman la reina de las flores."),
    "pitcher_plant": ("Planta jarra", "Turberas del este de Norteamérica",
                      "Una planta carnívora de suelos pobres y encharcados: sus hojas son jarras de agua de lluvia "
                      "donde los insectos se ahogan y son digeridos. En este mundo solo nace de semillas antiguas que "
                      "desentierran los husmeadores."),
    "grass": ("Hierba", "Europa, norte de Asia y Norteamérica",
              "La poa de los prados, la hierba de pastos y céspedes. Las gramíneas crecen desde la base de la hoja, no "
              "desde la punta, y así aguantan el pastoreo y la siega que matarían a casi cualquier planta."),
    "fern": ("Helecho", "Casi todo el mundo, en todos los continentes salvo la Antártida",
             "El helecho común es más antiguo que cualquier flor: no tiene flores ni semillas y se extiende por esporas "
             "y tallos que corren bajo tierra. Sus frondas son venenosas para el ganado y los caballos."),
    "dead_bush": ("Arbusto muerto", "Desiertos del suroeste de Estados Unidos y el norte de México",
                  "No está muerta, solo espera: la gobernadora deja de crecer con la sequía y reverdece tras la lluvia. "
                  "Un anillo de sus clones en el Mojave, King Clone, tiene unos 11 700 años."),
    "sweet_berry_bush": ("Arbusto de bayas dulces", "Bosques del norte y tundra de Europa, Asia y Norteamérica",
                         "El arándano rojo, un arbusto bajo y perenne del bosque boreal. Sus bayas rojas y ácidas "
                         "aguantan todo el invierno sin estropearse gracias a su propio ácido benzoico."),
    "bamboo": ("Bambú", "Centro y sur de China",
               "Una hierba gigante: un brote de bambú moso puede crecer un metro en un día y alcanza su altura en dos "
               "meses. Un bosquete entero florece una vez cada muchas décadas, todo a la vez, y luego muere."),
    "cactus": ("Cactus", "El desierto de Sonora, en Arizona y México",
               "El saguaro guarda agua en un tronco plisado que se hincha tras la lluvia; uno adulto puede contener "
               "más de una tonelada. Vive hasta 150 años y echa su primer brazo hacia los 70."),
    "sugar_cane": ("Caña de azúcar", "Nueva Guinea, donde se cultivó por primera vez hace unos 8000 años",
                   "Una hierba gigante con tallos llenos de jugo dulce; hervido, da la mayor parte del azúcar del "
                   "mundo. Las plantaciones de América se levantaron sobre ella y sobre el trabajo forzado de "
                   "personas esclavizadas."),
    "moss": ("Musgo", "Suelo húmedo de todo el mundo templado",
             "El musgo politrico, uno de los musgos más altos. Los musgos no tienen raíces, flores ni semillas; beben "
             "por las hojas y necesitan una película de agua para que sus espermatozoides naden hasta los óvulos."),
    "wheat": ("Trigo", "El Creciente Fértil, de tres hierbas silvestres cruzadas hace unos 9000 años",
              "El trigo harinero lleva los genomas completos de tres antepasados silvestres. Da una quinta parte de las "
              "calorías que come la humanidad, y su gluten deja que la masa se estire y retenga el gas del pan."),
    "carrot": ("Zanahoria", "Silvestre en Europa y el suroeste de Asia; cultivada por su raíz primero en Afganistán",
               "Las primeras zanahorias de comer eran moradas o amarillas; las naranjas se criaron en los Países Bajos "
               "en el siglo XVII. El naranja es betacaroteno, que el cuerpo convierte en vitamina A."),
    "potato": ("Patata", "Los Andes de Perú y Bolivia",
               "Los agricultores andinos cultivaban miles de variedades; Europa plantó solo unas pocas, y cuando llegó "
               "el mildiu en 1845 Irlanda perdió un millón de personas por el hambre. Las patatas verdes tienen el "
               "veneno solanina."),
    "beetroot": ("Remolacha", "Costas del Mediterráneo y del Atlántico, como la acelga marina silvestre",
                 "La remolacha, la acelga y la remolacha azucarera son una sola especie criada de tres maneras. La "
                 "azucarera, criada en Prusia en el siglo XVIII, da hoy una quinta parte del azúcar del mundo."),
    "melon": ("Sandía", "Noreste de África",
              "La sandía es agua en unas nueve décimas partes. Sus semillas se dejaron en tumbas egipcias, y los "
              "viajeros del desierto la llevaban como cantimplora."),
    "pumpkin": ("Calabaza", "Argentina y Uruguay",
                "Una planta rastrera cuyas calabazas gigantes han pasado de 1200 kilos, los frutos más pesados de "
                "cualquier planta. Los pueblos de América la cultivaban junto con el maíz y el frijol."),
    "cocoa": ("Cacao", "La alta cuenca del Amazonas",
              "Un arbolito de la sombra de la selva cuyas mazorcas brotan directamente del tronco. Mayas y aztecas "
              "bebían sus semillas tostadas y molidas; su nombre científico significa alimento de los dioses."),
    "kelp": ("Kelp", "Costas frías del Pacífico y de los mares del sur",
             "El kelp gigante no es una planta sino un alga parda. Vejigas llenas de gas sostienen sus frondas, y "
             "puede crecer medio metro al día formando bosques submarinos llenos de peces y nutrias marinas."),
    "seagrass": ("Hierba marina", "Costas poco profundas del Atlántico Norte y el Pacífico Norte",
                 "Una planta con flores que volvió al mar y hasta florece bajo el agua. Sus praderas dan refugio a "
                 "peces jóvenes y entierran carbono en el fango."),
    "lily_pad": ("Nenúfar", "Lagos y estanques de Europa, norte de África y Oriente Medio",
                 "El nenúfar blanco echa raíces en el fango y hace flotar sus hojas sobre largos tallos, con canales de "
                 "aire que llevan oxígeno a las raíces. Sus hojas respiran por la cara de arriba."),
    "torchflower": ("Flor de antorcha", None,
                    "Una flor antigua, de antes de que empezara el mundo; los husmeadores desentierran sus semillas de "
                    "la arena de las ruinas de los mares cálidos. Parece una llama, pero no da luz."),
    "pink_petals": ("Pétalos rosas", None,
                    "Alfombras de florecitas rosas bajo los cerezos, hasta cuatro por bloque. Ninguna flor real crece "
                    "así: parecen flores de cerezo caídas que echaron raíz."),
    "spore_blossom": ("Flor de esporas", None,
                      "Una flor que cuelga del techo de las cuevas y suelta una lluvia lenta de esporas verdes. Las "
                      "flores hacen polen y semillas, no esporas; esta no necesita nada de sol."),
    "dripleaf": ("Plantaforma", None,
                 "Hojas anchas sobre tallos altos que se inclinan un momento después de que algo se pose en ellas, como "
                 "una trampilla. Crece en la arcilla y el agua de las cuevas frondosas."),
    "glow_berries": ("Bayas luminosas", None,
                     "Enredaderas que cuelgan del techo de las cuevas con bayas que brillan con luz propia. Las "
                     "luciérnagas y algunos hongos dan luz, pero ningún fruto real lo hace."),
    "glow_lichen": ("Liquen luminoso", None,
                    "Un liquen que brilla en las paredes de las cuevas. Los líquenes reales son alianzas de un hongo "
                    "con un alga o una bacteria; ninguno brilla."),
    "vine": ("Enredaderas", None,
             "Lianas frondosas sin flores ni frutos que cuelgan de los árboles de la jungla y de los robles del pantano "
             "y crecen hacia abajo. Las trepadoras reales crecen hacia arriba, buscando la luz."),
}

BIOMES = {
    "river": "Un río: agua dulce que corre hacia el mar, con juncos en las orillas y plantas acuáticas en el cauce.",
    "ocean": "Mar abierto templado como el Atlántico Norte: agua fresca removida por las tormentas, con kelp e hierba "
             "marina cerca de las costas.",
    "cold_ocean": "Mar frío como el Pacífico Norte: agua oscura y rica en nutrientes, donde el kelp forma bosques.",
    "lukewarm_ocean": "Mar templado como el Mediterráneo: agua clara y salada con praderas de hierba marina en el "
                      "fondo arenoso.",
    "warm_ocean": "Mar tropical como el Caribe: agua cálida y clara sobre arrecifes de coral y praderas de hierba "
                  "marina.",
}
