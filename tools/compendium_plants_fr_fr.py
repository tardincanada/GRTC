"""Compendium des plantes (3.5.0) en français. Voir compendium_plants.py et compendium_i18n.py."""

TITLE = "Compendium des plantes"

GUIDE = [
    "# Le Compendium des plantes",
    "Un livre et n'importe quelle petite fleur font le Compendium des plantes ; aucune recherche n'est nécessaire. Il "
    "fonctionne comme le Compendium des arbres : gardez-le dans la barre d'action et le coin supérieur gauche nomme le "
    "biome et, une fois que vous la connaissez, la plante que vous regardez. Fleurs, herbes, fougères, arbustes, "
    "cultures, plantes aquatiques et plantes des grottes luxuriantes y sont toutes.",
    "Pour noter une plante, regardez-la un instant à 8 blocs au plus, ou cueillez-la ou récoltez-la, avec le livre dans "
    "la barre d'action ; les plantes sous l'eau se notent de la même façon. Utilisez le livre pour lire : les biomes à "
    "gauche, les plantes de chacun à droite.",
    "Les Notes du Compendium copient les deux livres à la fois : qui les lit gagne tous les arbres, plantes et biomes "
    "qu'elles contiennent.",
    "Species Plantarum (niveau 3) : chaque plante montre son nom scientifique, sa famille et où elle pousse à l'état "
    "sauvage dans le monde réel.",
    "Géographie des plantes (niveau 3) : chaque biome montre son climat, dans ce livre comme dans le Compendium des "
    "arbres.",
    "La Liste rouge (niveau 6) : chaque plante montre à quel point elle est menacée dans la nature.",
]

UI = [
    "Compendium des plantes",
    "Plante inconnue",
    "Nouvelle plante dans votre Compendium : %s",
    "%s plantes sur %s et %s biomes sur %s notés.",
    "Gardez ce livre dans la barre d'action. Regardez une plante un instant, ou cueillez-la ou récoltez-la, pour la "
    "noter ; entrez dans un biome pour noter le biome. Choisissez un biome à gauche.",
    "Plantes",
    "Une plante de ce monde seulement : aucune espèce réelle ne lui correspond.",
    "Compendium des plantes",
    "Dans la barre d'action, il montre le biome et nomme les plantes que vous connaissez ; utilisez-le pour lire à "
    "leur sujet",
]

PLANTS = {
    "dandelion": ("Pissenlit", "Europe et Asie ; aujourd'hui sur tous les continents sauf l'Antarctique",
                  "Sa tête jaune se ferme la nuit puis mûrit en une boule de graines que le vent emporte sur des "
                  "kilomètres. Tout en lui se mange, et ses feuilles faisaient une salade de printemps bien avant la "
                  "laitue."),
    "poppy": ("Coquelicot", "Europe, Afrique du Nord et Asie occidentale",
              "Le coquelicot suit la charrue : ses graines attendent des décennies dans le sol et germent quand on "
              "retourne la terre, c'est pourquoi il a couvert les champs de bataille des Flandres après 1914."),
    "blue_orchid": ("Orchidée bleue", "Forêts de collines de l'Assam, de Birmanie et du nord de la Thaïlande",
                    "L'une des très rares orchidées vraiment bleues. Dans la nature, elle s'accroche aux troncs des "
                    "forêts fraîches de collines ; les collectionneurs ont vidé des vallées entières, et son commerce "
                    "à l'état sauvage est aujourd'hui interdit."),
    "allium": ("Allium", "Montagnes d'Iran, d'Afghanistan et d'Asie centrale",
               "Un oignon d'ornement : une boule de centaines de petites fleurs violettes au bout d'une longue tige. "
               "Écrasez une feuille, elle sent l'oignon, comme ses cousins l'ail et le poireau."),
    "azure_bluet": ("Houstonie bleue", "Est de l'Amérique du Nord",
                    "Une minuscule fleur à quatre pétales qui couvre pelouses et prés de bleu pâle au printemps. Si "
                    "petite soit-elle, elle est de la famille du café."),
    "tulip": ("Tulipe", "Steppes de montagne de Turquie et d'Asie centrale",
              "Venue de l'Empire ottoman aux Pays-Bas au XVIe siècle ; en 1637 un seul bulbe pouvait coûter plus "
              "qu'une maison, jusqu'au krach. Les pétales striés si recherchés étaient causés par un virus."),
    "oxeye_daisy": ("Marguerite", "Europe et Asie tempérée",
                    "La marguerite blanche des prés et des bords de route. Chaque fleur est en fait des centaines de "
                    "petites fleurs : les jaunes au centre, les blanches autour."),
    "cornflower": ("Bleuet", "Sud-est de l'Europe et Moyen-Orient, répandu avec le grain dans toute l'Europe",
                   "Jadis mauvaise herbe de tous les champs de blé, il a presque disparu d'Europe quand les "
                   "agriculteurs ont trié leurs semences et employé des herbicides. Son bleu a donné son nom à une "
                   "couleur."),
    "lily_of_the_valley": ("Muguet", "Bois frais d'Europe et d'Asie",
                           "Des clochettes blanches et parfumées dans les bois ombragés du printemps. Toute la plante "
                           "est toxique : elle contient des glycosides cardiaques comme la digitale, que la médecine "
                           "emploie contre l'insuffisance cardiaque."),
    "sunflower": ("Tournesol", "Amérique du Nord ; cultivé au Mexique il y a environ 4 000 ans",
                  "Les jeunes têtes suivent le soleil d'est en ouest chaque jour ; la fleur ouverte reste tournée vers "
                  "l'est. Ses graines donnent l'une des grandes huiles de cuisine du monde."),
    "lilac": ("Lilas", "Collines rocheuses des Balkans",
              "Un arbuste de la famille de l'olivier aux grappes de fleurs violettes et parfumées à la fin du "
              "printemps. Sauvage seulement sur les pentes des Balkans, les jardins l'ont porté dans le monde entier."),
    "rose_bush": ("Rosier", "Europe, nord-ouest de l'Afrique et Asie occidentale",
                  "L'églantier grimpe dans les haies avec ses épines en crochet. Ses cynorhodons rouges contiennent "
                  "plus de vitamine C que l'orange ; dans la Grande-Bretagne en guerre, les enfants les cueillaient "
                  "pour en faire du sirop."),
    "peony": ("Pivoine", "Sibérie, Mongolie et nord de la Chine",
              "De grandes fleurs roses dont les boutons suintent un sucre que les fourmis viennent boire. Cultivée en "
              "Chine depuis plus de mille ans, on l'y appelle la reine des fleurs."),
    "pitcher_plant": ("Sarracénie", "Tourbières de l'est de l'Amérique du Nord",
                      "Une plante carnivore des sols pauvres et détrempés : ses feuilles sont des urnes d'eau de pluie "
                      "où les insectes se noient et sont digérés. Dans ce monde, elle ne pousse que de graines "
                      "anciennes que les renifleurs déterrent."),
    "grass": ("Herbe", "Europe, Asie du Nord et Amérique du Nord",
              "Le pâturin des prés, l'herbe des pâtures et des pelouses. Les graminées poussent par la base de la "
              "feuille, pas par la pointe, et survivent ainsi au pâturage et à la tonte qui tueraient la plupart des "
              "plantes."),
    "fern": ("Fougère", "Presque le monde entier, sur tous les continents sauf l'Antarctique",
             "La fougère aigle est plus ancienne que toute fleur : sans fleurs ni graines, elle se répand par des "
             "spores et des tiges qui courent sous terre. Ses frondes sont toxiques pour le bétail et les chevaux."),
    "dead_bush": ("Buisson mort", "Déserts du sud-ouest des États-Unis et du nord du Mexique",
                  "Pas morte, elle attend : la créosote cesse de pousser pendant la sécheresse et reverdit après la "
                  "pluie. Un anneau de ses clones dans le Mojave, King Clone, a environ 11 700 ans."),
    "sweet_berry_bush": ("Buisson à baies sucrées", "Forêts du nord et toundra d'Europe, d'Asie et d'Amérique du Nord",
                         "L'airelle rouge, un petit arbuste toujours vert de la forêt boréale. Ses baies rouges et "
                         "acidulées se gardent tout l'hiver sans se gâter, grâce à leur propre acide benzoïque."),
    "bamboo": ("Bambou", "Centre et sud de la Chine",
               "Une herbe géante : une pousse de bambou moso peut grandir d'un mètre en un jour et atteint sa taille en "
               "deux mois. Un bosquet entier fleurit une fois en plusieurs décennies, tout ensemble, puis meurt."),
    "cactus": ("Cactus", "Le désert de Sonora, en Arizona et au Mexique",
               "Le saguaro stocke l'eau dans un tronc plissé qui gonfle après la pluie ; un adulte peut en contenir "
               "plus d'une tonne. Il vit jusqu'à 150 ans et ne fait son premier bras que vers 70 ans."),
    "sugar_cane": ("Canne à sucre", "Nouvelle-Guinée, où elle fut cultivée pour la première fois il y a environ "
                   "8 000 ans",
                   "Une herbe géante aux tiges pleines de jus sucré ; réduit, il donne l'essentiel du sucre du monde. "
                   "Les plantations des Amériques se sont bâties sur elle et sur le travail forcé de personnes réduites "
                   "en esclavage."),
    "moss": ("Mousse", "Sols humides de tout le monde tempéré",
             "Le polytric commun, l'une des plus hautes mousses. Les mousses n'ont ni racines, ni fleurs, ni graines ; "
             "elles boivent par leurs feuilles et ont besoin d'un film d'eau pour que leurs spermatozoïdes nagent "
             "jusqu'aux ovules."),
    "wheat": ("Blé", "Le Croissant fertile, de trois herbes sauvages croisées il y a environ 9 000 ans",
              "Le blé tendre porte les génomes entiers de trois ancêtres sauvages. Il fournit un cinquième des calories "
              "de l'humanité, et son gluten permet à la pâte de s'étirer et de retenir le gaz du pain qui lève."),
    "carrot": ("Carotte", "Sauvage en Europe et en Asie du Sud-Ouest ; cultivée pour sa racine d'abord en "
               "Afghanistan",
               "Les premières carottes potagères étaient violettes ou jaunes ; les orange ont été sélectionnées aux "
               "Pays-Bas au XVIIe siècle. L'orange est du bêta-carotène, que le corps change en vitamine A."),
    "potato": ("Pomme de terre", "Les Andes du Pérou et de Bolivie",
               "Les paysans andins en cultivaient des milliers de variétés ; l'Europe n'en planta que quelques-unes, "
               "et quand le mildiou frappa en 1845 l'Irlande perdit un million de personnes dans la famine. Les "
               "pommes de terre vertes contiennent un poison, la solanine."),
    "beetroot": ("Betterave", "Côtes de la Méditerranée et de l'Atlantique, comme bette maritime sauvage",
                 "La betterave rouge, la blette et la betterave sucrière sont une seule espèce sélectionnée de trois "
                 "façons. La sucrière, créée en Prusse au XVIIIe siècle, donne aujourd'hui un cinquième du sucre du "
                 "monde."),
    "melon": ("Pastèque", "Nord-est de l'Afrique",
              "La pastèque est faite d'eau aux neuf dixièmes environ. On a laissé ses graines dans des tombes "
              "égyptiennes, et les voyageurs du désert l'emportaient comme gourde."),
    "pumpkin": ("Citrouille", "Argentine et Uruguay",
                "Une plante rampante dont les potirons géants ont dépassé 1 200 kilos, les fruits les plus lourds de "
                "toutes les plantes. Les peuples des Amériques cultivaient les courges avec le maïs et le haricot."),
    "cocoa": ("Cacao", "Le haut bassin de l'Amazone",
              "Un petit arbre de l'ombre de la forêt dont les cabosses poussent directement sur le tronc. Les Mayas "
              "et les Aztèques buvaient ses fèves grillées et moulues ; son nom savant veut dire nourriture des dieux."),
    "kelp": ("Varech", "Côtes froides du Pacifique et des mers australes",
             "Le varech géant n'est pas une plante mais une algue brune. Des flotteurs pleins de gaz tiennent ses "
             "frondes dressées, et il peut pousser d'un demi-mètre par jour en forêts sous-marines pleines de poissons "
             "et de loutres de mer."),
    "seagrass": ("Herbe marine", "Côtes peu profondes de l'Atlantique Nord et du Pacifique Nord",
                 "Une plante à fleurs retournée à la mer, qui fleurit même sous l'eau. Ses herbiers abritent les "
                 "jeunes poissons et enfouissent du carbone dans la vase."),
    "lily_pad": ("Nénuphar", "Lacs et étangs d'Europe, d'Afrique du Nord et du Moyen-Orient",
                 "Le nénuphar blanc s'enracine dans la vase et fait flotter ses feuilles au bout de longues tiges, dont "
                 "les canaux d'air portent l'oxygène jusqu'aux racines. Ses feuilles respirent par leur face "
                 "supérieure."),
    "torchflower": ("Fleur-torche", None,
                    "Une fleur ancienne, d'avant le commencement du monde ; les renifleurs déterrent ses graines dans "
                    "le sable des ruines des mers chaudes. Elle ressemble à une flamme mais ne donne aucune lumière."),
    "pink_petals": ("Pétales roses", None,
                    "Des tapis de petites fleurs roses sous les cerisiers, jusqu'à quatre par bloc. Aucune vraie fleur "
                    "ne pousse ainsi : on dirait des fleurs de cerisier tombées qui ont pris racine."),
    "spore_blossom": ("Fleur sporifère", None,
                      "Une fleur suspendue au plafond des grottes qui laisse tomber une lente pluie de spores vertes. "
                      "Les fleurs font du pollen et des graines, pas des spores ; celle-ci n'a besoin d'aucun soleil."),
    "dripleaf": ("Foliogoutte", None,
                 "De larges feuilles sur de hautes tiges qui basculent un instant après qu'on y pose le pied, comme une "
                 "trappe. Elle pousse dans l'argile et l'eau des grottes luxuriantes."),
    "glow_berries": ("Baies lumineuses", None,
                     "Des lianes pendues aux voûtes des grottes, aux baies qui brillent de leur propre lumière. Les "
                     "lucioles et certains champignons font de la lumière, mais aucun vrai fruit."),
    "glow_lichen": ("Lichen lumineux", None,
                    "Un lichen qui luit sur les parois des grottes. Les vrais lichens sont l'association d'un "
                    "champignon et d'une algue ou d'une bactérie ; aucun ne brille."),
    "vine": ("Lianes", None,
             "Des lianes feuillues sans fleurs ni fruits qui pendent des arbres de la jungle et des chênes des marais "
             "et poussent vers le bas. Les vraies plantes grimpantes montent vers la lumière."),
}

BIOMES = {
    "river": "Une rivière : de l'eau douce qui court vers la mer, avec des roseaux sur les berges et des plantes "
             "aquatiques dans son lit.",
    "ocean": "Mer ouverte tempérée comme l'Atlantique Nord : eau fraîche brassée par les tempêtes, avec du varech et "
             "des herbiers près des côtes.",
    "cold_ocean": "Mer froide comme le Pacifique Nord : eau sombre et riche en nutriments, où le varech forme des "
                  "forêts.",
    "lukewarm_ocean": "Mer douce comme la Méditerranée : eau claire et salée avec des herbiers sur le fond de sable.",
    "warm_ocean": "Mer tropicale comme les Caraïbes : eau chaude et claire sur des récifs de corail et des herbiers.",
}
