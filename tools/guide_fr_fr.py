"""French (fr_fr) text of the Field Guide (2.5.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the fr_fr lang file.
"""

DECIMAL = ","

INTRO = [
    "# Bienvenue",
    "Chaque bloc et chaque objet de ce monde est fait de vrais éléments. Ce guide suit l'arbre technologique du mod, "
    "de l'âge de pierre jusqu'aux derniers éléments, un chapitre par niveau.",
    "# Unités",
    "1 unité vaut 1 gramme. Un bloc fait 1000 unités, un lingot ou un objet d'élément ou de substance 111,1, une pépite "
    "12,35 et un seau 1000. Survolez un bloc ou un objet pour voir de quoi il est fait ; maintenez Maj pour tout voir.",
    "# Recherche",
    "Chaque niveau a sa station de recherche. Placez les échantillons d'un projet dans ses 9 emplacements, choisissez le "
    "projet et appuyez sur Rechercher. Les projets clés (marqués d'une étoile) élèvent votre niveau technologique ; il "
    "vous faut les projets clés d'un niveau avant de pouvoir poser la station suivante. /research montre ce que vous savez.",
    "Les traités écrits à une station enseignent tout ce que vous savez à un autre joueur.",
    "# Machines",
    "Une machine décompose chaque objet entrant selon sa composition et y fait tourner chaque réaction que vous avez "
    "recherchée, tant qu'elle est assez chaude et pas trop chaude. Les produits sortent en objets, les déchets en "
    "scories, et certains gaz sont rejetés. Les liquides et les gaz remplissent des fioles en verre vides depuis le "
    "panneau latéral de la machine. Certaines réactions demandent un second ingrédient dans l'emplacement de réactif.",
    "Les machines chauffées au feu brûlent le combustible de l'emplacement de combustible : le bois atteint 650 °C, le "
    "charbon de bois et le charbon 1100 °C, les combustibles pétroliers 1200 °C et le gaz 1300 °C. Les soufflets (air "
    "forcé) ajoutent 250 °C. Le charbon de bois et le charbon servent aussi de carbone pour réduire les minerais. Les "
    "machines électriques prennent le courant d'une pile voltaïque, d'une dynamo ou d'un autre bloc d'énergie voisin ou "
    "relié par du fil de cuivre.",
    "Les fourneaux ordinaires ne fondent plus les minerais de fer, de cuivre ou d'or : les métaux viennent des machines.",
    "# Niveau 0",
    "Sans aucune recherche, vous pouvez construire une meule de charbonnier (8 terres autour d'une bûche) et y "
    "carboniser des bûches en charbon de bois.",
]

CONTAINERS = [
    "# Fioles en verre",
    "Chaque objet liquide ou gazeux est une fiole en verre bouchée contenant 111,1 unités. Cinq verres font quatre "
    "fioles, sans recherche. Une machine remplit une fiole vide depuis l'emplacement du haut de son panneau latéral "
    "pour chaque liquide ou gaz qu'elle produit ; sans fiole, elle s'arrête et en réclame. Quand une machine, une "
    "recette, un fourneau ou un projet de recherche utilise le contenu d'une fiole, vous récupérez la fiole vide.",
    "# Bouteilles en acier et compresseur",
    "Compression des gaz (niveau 4) débloque les bouteilles en acier et le compresseur. Une bouteille contient 1000 "
    "unités d'un gaz comprimé : l'équivalent de neuf fioles en un seul objet. Installez un compresseur dans l'un des "
    "deux emplacements d'amélioration d'une machine et mettez des bouteilles vides dans le deuxième emplacement de son "
    "panneau latéral : la machine remplit alors des bouteilles avec chaque gaz qu'elle produit, une bouteille à la fois. "
    "Les liquides vont toujours dans des fioles. Une bouteille de gaz combustible brûle comme neuf fioles.",
    "Les machines acceptent les bouteilles en entrée comme n'importe quel objet et rendent la bouteille vide.",
    "# Réservoir de stockage",
    "Chimie pneumatique (niveau 3) débloque le réservoir de stockage. Il contient 16 000 unités (144 fioles) d'un seul "
    "liquide ou gaz. Utilisez-le avec une fiole ou une bouteille pleine pour la vider dedans, avec une fiole vide pour en "
    "tirer une, et la main vide pour lire son niveau. Les entonnoirs marchent aussi : mettez les récipients pleins ou "
    "vides par le haut ou les côtés, et récupérez ce qui sort par le bas. Un comparateur lit son remplissage. Le casser "
    "répand son contenu.",
]

UPGRADES = [
    "# Emplacements d'amélioration",
    "Chaque machine a deux emplacements d'amélioration en bas de son panneau latéral. Une amélioration fonctionne dès "
    "que son propriétaire l'a recherchée ; deux identiques ne comptent qu'une fois. Les améliorations marquées « à "
    "combustible » ne font rien dans les machines électriques.",
    "- Compresseur (Compression des gaz, niveau 4) : remplit des bouteilles en acier avec les gaz que produit la machine.",
    "- Éjecteur de sortie (Moulin automatique, niveau 3) : déplace les produits de la machine vers un coffre, un tonneau "
    "ou un entonnoir voisin. Il n'alimente jamais directement une autre machine.",
    "- Régénérateur (Four à régénération, niveau 4) : une machine à combustible perd deux fois moins de chaleur, elle "
    "brûle donc moins et chauffe plus vite.",
    "- Revêtement réfractaire (Réfractaires basiques, niveau 4) : une machine à combustible peut dépasser sa propre "
    "limite de 150 °C, si son combustible et ses soufflets le permettent.",
    "- Hotte de récupération des gaz (Récupération des sous-produits, niveau 4) : les liquides et gaz que la machine "
    "rejetterait (dioxyde de carbone, dioxyde de soufre, acide chlorhydrique et d'autres) vont dans des fioles ou des "
    "bouteilles. La vapeur et les gaz mélangés comme le gaz de houille sont toujours rejetés. Le gaz capté n'arrête "
    "jamais la machine : sans récipient ni place, il est rejeté malgré tout.",
    "- Agitateur mécanique (Moteur électrique, niveau 4) : traite la charge 50 % plus vite. Son moteur brûle un quart de "
    "combustible en plus pendant la réaction, ou tire du courant en plus dans une machine électrique.",
    "- Lit catalytique (Lits catalytiques, niveau 5) : les réactions sur un catalyseur solide (fer, nickel, platine, "
    "platine-rhodium, pentoxyde de vanadium, bromure de cobalt-manganèse) vont deux fois plus vite : ammoniac, procédé "
    "de contact, acide nitrique, hydrogénation du charbon, reformage, nylon et plus encore.",
    "- Convoyeur (Caoutchouc Buna, niveau 5) : un éjecteur de sortie sur courroie en caoutchouc ; il déplace les "
    "produits finis quatre par quatre au lieu d'un par un.",
    "- Calorifuge en mousse (Styrène et polystyrène, niveau 6) : une machine dont la limite est de 300 °C ou moins perd "
    "70 % de chaleur en moins.",
    "- Filtre à air stérile (Pénicilline, niveau 6) : le tonneau de fermentation, la cuve à vinaigre, le fermenteur en "
    "cuve profonde et le bioréacteur produisent 10 % de plus. Le fermenteur en cuve profonde ne fonctionne pas sans lui.",
]

FARM = [
    "# Équarrissage et savon",
    "Équarrissage (niveau 1) débloque la chaudière à savon. Viande, os, œufs et lait y entrent ; la chaudière les fait "
    "mijoter entre 80 et 150 °C. Le suif remonte, et la viande et l'os restants sortent en farine de protéines, que "
    "mangent vaches, moutons, chèvres, cochons et poules et qui se composte bien. Du suif avec de la ficelle donne des "
    "chandelles de suif : lumière 12, fumeuses, et elles se consument trois jours après avoir été posées.",
    "Cuisson du savon : mettez du suif ou de l'huile végétale dans la chaudière et de la cendre de bois dans "
    "l'emplacement de réactif pour du savon mou, avec du glycérol en fioles. Savon dur (niveau 2) utilise de la soude à "
    "la place, ou durcit le savon mou avec du sel.",
    "Utilisez un pain de savon pour vous laver : vous êtes Propre pendant dix minutes, et Propre efface la Faim de la "
    "chair putréfiée et du poulet cru. Avec une armure en cuir teinte dans l'autre main, le savon en retire la teinture. "
    "Du savon avec de la laine ou un lit dans la grille d'artisanat les lave en blanc.",
    "# Huile, cuir et moût",
    "Pressage de l'huile (niveau 1) : la presse à huile presse à froid les graines de citrouille, de pastèque et de blé, "
    "les fèves de cacao et les tournesols. L'huile végétale remplit des fioles ; le tourteau restant nourrit le bétail. "
    "Le suif et l'huile végétale brûlent aussi comme combustible.",
    "Tannage en fosse (niveau 1) : la fosse à tan change la chair et les peaux en cuir sans grille d'artisanat, "
    "lentement : quatre chairs putréfiées donnent un cuir en environ trois jours de jeu. Mettez des bûches de chêne ou "
    "d'épicéa dans l'emplacement de réactif pour leur tanin ; l'écorce de tan épuisée qui reste brûle comme "
    "combustible. Fabrication de l'alun (niveau 2) donne l'alun de potassium, et deux peaux de lapin avec un alun se "
    "mégissent en cuir dans la grille d'artisanat.",
    "Brassage (niveau 1) : le tonneau de fermentation change l'amidon du blé et des pommes de terre en sucre puis en "
    "alcool. Sucre d'amidon (niveau 3) fait la première étape plus vite avec un peu d'acide sulfurique dans la cuve de "
    "digestion. Huile douce de vitriol (niveau 2) : l'éthanol distillé avec de l'acide sulfurique dans l'alambic donne "
    "l'éther diéthylique.",
    "# Engrais",
    "Superphosphate (niveau 3) dissout les os ou la roche phosphatée dans l'acide sulfurique ; Eaux ammoniacales "
    "(niveau 3) lave l'ammoniac du four à coke (gardez des fioles dans son emplacement à fioles) et la chambre de plomb "
    "le change en sulfate d'ammoniaque. Utilisez l'un ou l'autre engrais sur la terre labourée : pendant les huit "
    "récoltes suivantes, la culture y pousse moitié plus vite et une récolte mûre en donne un de plus.",
    "# Caoutchouc",
    "Les hévéas poussent à l'état sauvage dans les jungles (jungle, jungle clairsemée et jungle de bambous). Leurs "
    "feuilles donnent des pousses, et la pousse prend partout, mais un hévéa ne donne du latex que dans une jungle. "
    "Vulcanisation (niveau 3) débloque la saignée : fixez-la sur le côté d'une bûche d'hévéa et utilisez-la avec une "
    "fiole en verre vide ; elle remplit une fiole toutes les cinq minutes et en garde quatre. Les entonnoirs peuvent lui "
    "fournir des fioles vides par le haut et prendre le latex par le bas. Hors de la jungle, elle signale le mauvais climat.",
    "Dans le vulcanisateur, le latex avec du soufre dans l'emplacement de réactif se vulcanise en caoutchouc entre 130 et "
    "160 °C ; sans soufre il sèche seulement en caoutchouc brut. Le caoutchouc vulcanisé fait des baskets (vos pas ne "
    "font aucune vibration, et le magma et les œufs de tortue ne vous remarquent pas) et des roues en caoutchouc : "
    "utilisez-les sur un wagonnet et il roule 25 % plus vite.",
]

TAR = [
    "# Goudron de houille",
    "Distillation du goudron (niveau 3) : une fois recherchée, le four à coke recueille le goudron du charbon en fioles "
    "(gardez des fioles vides dans son emplacement à fioles), environ une fiole pour 13 charbons. L'alambic à goudron "
    "sépare le goudron de houille en benzène, toluène, phénol (acide phénique) et anthracène, et laisse le brai. Le "
    "brai avec du gravier et du sable donne du macadam, un bloc de route aussi rapide que l'asphalte. Benzène, phénol, "
    "goudron et brai brûlent tous comme combustible. Nitrobenzène (niveau 3) nitre le benzène dans la cuve de "
    "digestion, et Aniline (niveau 4) le réduit avec du fer.",
    "# Teintures",
    "Bleu de Prusse (niveau 2) débloque la cuve de teinture : chair putréfiée ou viande avec du vitriol vert dans "
    "l'emplacement de réactif donne le pigment, et la cuve le change en teinture bleue, 8 par objet. Les colorants du "
    "goudron donnent 32 teintures ordinaires par fiole : fuchsine (aniline et arsenic blanc, magenta), acide picrique "
    "(phénol et acide nitrique dans la cuve de digestion, jaune) et alizarine (anthracène et soude caustique dans "
    "l'autoclave, rouge). Mettez le colorant dans la cuve de teinture pour obtenir des teintures ordinaires.",
    "# Infection et médecine",
    "Un coup de zombie, de momifié, de noyé ou de villageois zombie vous infecte 1 fois sur 200, et manger de la chair "
    "putréfiée 1 fois sur 100 ; être Propre divise les deux par deux. Une Infection dure un jour de jeu avec Faiblesse "
    "et Faim. Sans traitement, elle guérit seule 7 fois sur 10 ; les autres fois elle tourne en Septicémie, 1 dégât "
    "toutes les 10 secondes jusqu'à la mort. Le savon phéniqué (Chirurgie antiseptique : phénol et savon dur) et "
    "l'aspirine (niveau 4 : phénol dans l'autoclave avec de la soude, puis acide acétique dans la cuve de digestion) "
    "guérissent une Infection avant qu'elle ne tourne. Seuls les sulfamides (niveau 5) et les antibiotiques du niveau "
    "6 guérissent la Septicémie. Le lait ne guérit ni l'une ni l'autre. La règle de jeu periodicElementsInfection "
    "désactive l'infection.",
    "L'aspirine supprime aussi Nausée et Fatigue de minage et donne 10 secondes de Régénération. L'éther anesthésique "
    "(éther sur de la laine), utilisé sur un villageois zombie dont la guérison a commencé, divise par deux la durée de "
    "la guérison.",
    "# Explosifs",
    "Dynamite (niveau 4) : le glycérol de la cuisson du savon, nitré à froid (5 à 25 °C) dans la cuve de digestion, "
    "donne la nitroglycérine ; avec du sable et du papier elle fait de la dynamite, et quatre bâtons avec de la ficelle "
    "font une charge de mine. Allumez-la avec un briquet ou de la redstone : au bout de 4 secondes elle brise chaque "
    "bloc d'un cube de 5x5x5 moins solide que l'obsidienne et chacun tombe. Avec mobGriefing désactivé, elle ne blesse "
    "que ce qui se tient près. Cordite (niveau 4) malaxe la nitroglycérine avec la poudre sans fumée ; les cartouches de "
    "cordite tirent du fusil à chargement par la culasse 25 % plus fort et sans fumée.",
    "# Cultures et nuisibles",
    "La bouillie bordelaise (vitriol bleu, chaux vive et eau) pulvérisée sur une culture ou un buisson à baies l'empêche "
    "de se faire emporter ou de flétrir dans le noir, et les buissons à baies cessent de griffer. Poudre insecticide : "
    "la presse à huile moud les marguerites en pyrèthre, et l'insecticide en aérosol blesse araignées, poissons "
    "d'argent, endermites et abeilles sur 4 blocs devant vous.",
    "# Pompe à vide",
    "Distillation sous vide (niveau 4) débloque l'amélioration Pompe à vide : dans un alambic, un alambic à goudron, "
    "une chaudière à savon, un cristallisoir, un marais salant ou un alambic de raffinerie, chaque réaction a lieu "
    "100 °C plus froid, la machine chauffe donc plus tôt et perd moins de chaleur.",
]

INDUSTRY = [
    "# Carbure et acétylène",
    "Carbure de calcium (niveau 5) : la chaux vive avec du coke dans l'emplacement de combustible du four électrique à "
    "arc fond en carbure à 1800 °C et plus. Dans la cuve de digestion avec de l'eau dans l'emplacement de réactif, le "
    "carbure donne de l'acétylène (gardez des fioles, ou des bouteilles et un compresseur, dans la cuve) et laisse de la "
    "chaux éteinte.",
    "La lampe à carbure (laiton, verre, carbure et un seau d'eau) donne une lumière 14 tenue dans l'une ou l'autre main "
    "ou portée sur la tête, pendant une demi-heure. Utilisez-la avec du carbure de calcium dans votre inventaire pour la "
    "recharger ; une lampe vide reste entière.",
    "Soudage oxyacétylénique : fabriquez un chalumeau avec une bouteille d'acétylène et une d'oxygène, un lingot d'acier "
    "et un lingot de cuivre. Tenez un outil ou une pièce d'armure en métal abîmé dans l'autre main et utilisez le "
    "chalumeau : un lingot de son propre métal tiré de votre inventaire en répare un quart, sans expérience. Le "
    "chalumeau fait 64 soudures ; refabriquez-le avec des bouteilles neuves pour le recharger.",
    "Chaux azotée : le carbure dans le four à chaux entre 1000 et 1150 °C avec de l'azote dans l'emplacement de réactif "
    "absorbe l'azote en cyanamide calcique, un engrais.",
    "# Engrais tirés de l'air",
    "Synthèse de l'urée : l'ammoniac dans le réacteur haute pression entre 170 et 200 °C, avec du dioxyde de carbone "
    "dans l'emplacement de réactif (captez-le d'un four à chaux ou d'un tonneau de fermentation avec une hotte de "
    "récupération des gaz). Engrais en granulés débloque la tour de prilling, une machine électrique : l'ammoniac avec "
    "l'acide nitrique devient nitrate d'ammonium, et l'urée avec le mélange PK (superphosphate et potasse) devient "
    "granulés NPK.",
    "La chaux azotée, l'urée et le nitrate d'ammonium agissent sur la terre labourée comme le superphosphate. Les "
    "granulés NPK font pousser la culture deux fois plus vite, pendant huit récoltes, chacune donnant un de plus.",
    "# Plastiques et caoutchouc",
    "Formaldéhyde : le méthanol sur de l'argent dans le convertisseur d'Ostwald entre 600 et 700 °C. Bakélite : le phénol "
    "avec du formaldéhyde dans l'emplacement de réactif, dans l'autoclave entre 150 et 200 °C. Quatre bakélites font "
    "quatre blocs de bakélite, bruns, ou noirs, rouges, verts, ivoire ou ambre avec une teinture. La bakélite sert aussi "
    "de boîtier au compteur Geiger à la place du laiton.",
    "Néoprène : l'acétylène avec l'acide chlorhydrique dans le réacteur haute pression. Une combinaison de plongée "
    "complète en néoprène donne Grâce du dauphin dans l'eau, et aucune de ses pièces ne laisse entrer le froid de la "
    "neige poudreuse.",
    "Caoutchouc Buna, sans jungle : l'éthanol sur le catalyseur dans l'unité de craquage entre 380 et 450 °C donne le "
    "butadiène ; le benzène avec de l'éthylène dans l'emplacement de réactif entre 550 et 600 °C donne le styrène ; le "
    "butadiène avec le styrène dans la cuve de digestion entre 40 et 60 °C donne le caoutchouc Buna. Il fait les roues en "
    "caoutchouc et l'amélioration Convoyeur.",
    "# Explosifs",
    "TNT : le toluène de l'alambic à goudron, nitré dans la cuve de digestion. Un TNT avec trois papiers donne trois "
    "blocs de TNT ordinaire. Quatre TNT, un lingot d'acier et de la ficelle font une charge de démolition : allumée "
    "comme une charge de mine, elle brise un cube de 3x3x3 de tout sauf la bedrock, obsidienne comprise, et tout tombe.",
    "# Fermentation, carburant et médecine",
    "Fermentation acétonique : du blé ou des pommes de terre dans le tonneau de fermentation, avec de la calcite dans "
    "l'emplacement de réactif, fermentent en acétone, butanol et un peu d'éthanol au lieu de sucre. Biodiesel : du suif "
    "ou de l'huile végétale dans la chaudière à savon entre 40 et 150 °C avec du méthanol dans l'emplacement de réactif. "
    "Le biodiesel et le butanol font tourner le groupe électrogène diesel et brûlent dans les fourneaux.",
    "Sulfamides : l'aniline avec le sulfate d'ammonium dans la cuve de digestion donne le sulfanilamide. Utilisez-le "
    "pour guérir une Infection à l'un ou l'autre stade, Septicémie comprise.",
]

LIFE = [
    "# Antibiotiques",
    "Pénicilline (niveau 6) débloque le fermenteur en cuve profonde, une machine électrique qui ne fonctionne qu'avec "
    "un filtre à air stérile. Le glucose avec du lait dans l'emplacement de réactif (pour son lactose) fait pousser la "
    "pénicilline entre 20 et 37 °C ; la streptomycine prend plutôt de la farine de protéines dans l'emplacement de "
    "réactif. L'un ou l'autre antibiotique guérit une Infection ou une Septicémie et vous immunise contre l'Infection "
    "pendant 20 minutes. Utilisez la pénicilline sur un villageois zombie sous Faiblesse et sa guérison commence, sans "
    "pomme dorée. Le mycélium épuisé de la cuve est un engrais.",
    "# Pesticides",
    "DDT : le benzène avec du chlore dans la cuve de digestion. Une poudreuse à DDT utilisée sur le sol le rend mortel "
    "pour les araignées, poissons d'argent, endermites et abeilles pendant trois jours de jeu ; les poules dessus pondent "
    "deux fois moins. Le désherbant (2,4-D, du phénol et du chlore) tue fleurs, fougères, lianes, buissons, pousses et "
    "feuilles autour du bloc, mais épargne l'herbe et les cultures. La prélevée (atrazine, de l'ammoniac et du chlore "
    "avec du coke dans le réacteur haute pression) empêche l'herbe et le mycélium de s'étendre sur le sol pendant dix "
    "jours de jeu. Chaque pulvérisateur a 16 utilisations et agit sur 4 blocs alentour.",
    "# Plastiques",
    "Styrène et polystyrène : le styrène dans le réacteur haute pression. Huit polystyrènes autour d'une fiole de méthane "
    "font du calorifuge en mousse ; quatre polystyrènes et du méthane font quatre blocs d'isolant, et la glace ou la "
    "neige qui touche l'isolant ne fond jamais.",
    "# Explosifs",
    "Explosif plastique : le formaldéhyde et l'ammoniac donnent l'hexamine, et l'hexamine nitrée à froid donne le RDX. "
    "Deux RDX, un lingot de cuivre, un lingot d'acier et de la ficelle font une charge creuse : posez-la contre un mur et "
    "allumez-la, elle perce un tunnel de 1x1 sur 16 blocs de profondeur à travers tout sauf la bedrock. ANFO : six "
    "nitrates d'ammonium, du gazole, du papier et de la ficelle font une charge d'ANFO qui brise un cube de roche de "
    "9x9x9 ; trois blocs sur quatre tombent.",
    "# Contre-mesures radiologiques",
    "L'iodure de potassium (iodure de sodium tiré du varech avec du sel de potasse dans le cristallisoir) réduit de 40 % "
    "la dose des produits de fission pendant un jour de jeu. Une gélule de bleu de Prusse (bleu de Prusse et farine de "
    "protéines) la réduit d'un quart. L'amifostine (niveau 7) divise par deux les dégâts de toute radiation pendant une heure.",
    "# Biotechnologie",
    "ADN recombinant (niveau 7) débloque le bioréacteur (il demande un circuit intégré). Il fait fermenter la cellulose "
    "des bûches, feuilles ou de la paille en éthanol, le glucose en PLA, et la protéine avec l'huile végétale en vaccin à "
    "ARNm, sept jours de jeu sans Infection. Sa biomasse est un engrais. Le PLA fait des pots de fleurs et des cadres, et "
    "se composte.",
    "Glyphosate : le formaldéhyde avec l'acide phosphorique dans le réacteur haute pression. Champ net tue toutes les "
    "plantes autour du bloc, cultures comprises, sauf le blé issu de graines tolérantes (huit graines de blé autour d'un "
    "glyphosate). Le blé tolérant pousse aussi un dixième plus vite.",
]

MINING = [
    "# Masses et excavatrices",
    "Masses de carrier (niveau 2) enseigne la masse (cinq lingots autour d'un bâton, sur un bâton) et l'excavatrice "
    "(trois lingots et deux bâtons), en acier et dans chaque métal d'outil ultérieur. Chacune brise un carré de 3x3 sur "
    "la face frappée, à trois dixièmes de la vitesse, et s'use d'un point par bloc. Accroupissez-vous pour miner un seul "
    "bloc. Seuls les blocs adaptés à l'outil et pas plus durs que celui visé se brisent ; rien qui ait un inventaire ni "
    "aucune machine ne se brise jamais ; le butin va directement dans votre inventaire.",
    "# Dynamitage",
    "Dynamitage de roche (niveau 2) : quatre poudres noires, du papier et de la ficelle font deux charges de "
    "dynamitage. Allumée avec un briquet ou de la redstone, une charge brise un bloc de pierre de 3x3x3 et fait tout "
    "tomber ; la terre, le bois et les machines sont épargnés.",
    "# Foreuses et forets",
    "@4 Chaque foreuse a besoin d'un foret : tenez le foret dans l'autre main, accroupissez-vous et utilisez la foreuse. "
    "Le foret détermine ce qu'elle peut miner et à quelle vitesse (acier au tungstène 8,5, acier rapide 10, carbure "
    "cémenté 12, diamant 13) et s'use d'un point par coup. Enchantez le foret, pas la foreuse ; un foret monté prête à la "
    "foreuse ses enchantements, tous sauf Raccommodage. Accroupissez-vous et utilisez-la sans foret dans l'autre main "
    "pour changer de mode. Une foreuse dépense 1,5 kJ par point de dureté de chaque bloc : 2,25 pour la pierre, 4,5 pour "
    "l'ardoise des abîmes.",
    "Perforatrice (niveau 4) : le compresseur d'air, une machine électrique, remplit des bouteilles en acier d'air "
    "comprimé, sans amélioration Compresseur. La perforatrice pneumatique consomme une bouteille tous les 600 kJ "
    "(environ 400 pierres) et rend la vide ; elle mine un bloc, un tunnel 1x3 ou 3x3, et vous ralentit tant que vous la "
    "tenez.",
    "# Outils sur batterie",
    "Batterie plomb-acide (niveau 4) : la batterie contient 1 500 kJ. Reliez un banc de charge au fil et utilisez la "
    "batterie dessus ; elle se remplit jusqu'à 5 kJ par tick.",
    "Le perforateur (niveau 5) est la perforatrice sur batterie, sans ralentissement. La tronçonneuse (niveau 5) abat "
    "un arbre entier, jusqu'à 128 bûches, pour 3 kJ par bûche.",
    "La foreuse minière lourde (niveau 6) ajoute un mode 5x5 et un mode filon qui suit un minerai sur jusqu'à 32 blocs ; "
    "avec une batterie plomb-acide, elle vous ralentit.",
    "La batterie lithium-ion (niveau 7) contient 6 000 kJ, est utilisée en premier et supprime ce ralentissement. Foreuse "
    "à prospection (niveau 7) : en mode filon, utilisez la foreuse minière lourde sur un minerai pour lire la taille et "
    "les éléments du filon avant de creuser.",
]

ISOTOPES = [
    "# Radiation",
    "Les objets radioactifs émettent une dose qui s'accumule tant que vous les portez ou restez près d'eux. Un "
    "électroscope, puis un compteur Geiger, la mesure. Les murs de plomb, la cellule chaude et le réacteur nucléaire "
    "protègent ce qu'ils contiennent. Certains échantillons se désintègrent selon l'horloge du jeu (un jour de jeu vaut "
    "un jour réel pour eux) et se changent en leurs descendants dans votre inventaire.",
    "# Lueur bleue",
    "Les émetteurs les plus forts brillent en bleu dans le noir, comme la lumière Tcherenkov : le radium et plus fort "
    "dans l'air, et dans l'eau aussi le polonium, l'américium, l'actinium et la pechblende, plus vivement. Un réacteur "
    "nucléaire en marche illumine son bassin, et les accélérateurs en marche brillent aussi. La lueur suit la "
    "concentration de l'activité, pas la quantité que vous avez ; l'uranium ne brille jamais. Les objets qui brillent "
    "brillent aussi dans leur case d'inventaire.",
    "# Isotopes naturels",
    "L'infobulle de chaque élément stable liste ses isotopes naturels, du plus abondant au moins abondant.",
    "# Proportions variables",
    "Les objets d'uranium (U-235), de plutonium (Pu-240), de lithium (Li-6), de bore (B-10), de calcium (Ca-48) et "
    "d'hydrogène (H-2) affichent leur propre proportion. Un objet à la proportion naturelle s'empile avec n'importe quel "
    "autre ; un objet enrichi ou appauvri ne s'empile qu'avec des objets à la même proportion. Les machines mélangent les "
    "proportions selon la masse.",
    "- Le calutron (Enrichissement au calutron) sépare l'uranium, le lithium ou le bore en une petite coupe enrichie et "
    "des résidus appauvris. Remettez la coupe pour enrichir davantage.",
    "- L'uranium métal à 3 % d'U-235 ou plus se fritte en pastilles de combustible de réacteur.",
    "- Le plutonium produit dans le réacteur est de qualité militaire (6 % de Pu-240) ; le plutonium du combustible usé "
    "est de qualité réacteur.",
    "- Le lithium à plus de 90 % de Li-6 dans les emplacements d'un réacteur devient du tritium, qui allume les lampes "
    "au tritium pour toujours.",
    "- Le bore à plus de 90 % de B-10 fait des barres de contrôle : avec une dans ses emplacements, un réacteur cesse de "
    "brûler du combustible tant qu'il est plein.",
    "- L'eau lourde de la cascade d'enrichissement (nourrissez-la de glace ou de neige) fait brûler les barres de "
    "combustible à l'uranium naturel.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Premiers pas",
    "group": "Niveau {n}",
    "research": "Recherche",
    "machines": "Machines",
    "reactions": "Réactions",
    "recipe": " Recette : {r}.",
    "recipe_head": "Recette : {r}.",
    "projects": "# Projets (★ = clé)",
    "needs": " Nécessite {x}.",
    "samples": " Échantillons : {x}.",
    "unlocks": " Débloque {x}.",
    "up_to": "{desc}. Jusqu'à {c} °C",
    "electric": ", électrique.",
    "fuel": ", à combustible.",
    "amounts": "Quantités pour 100 unités de charge.",
    "ores": "Minerais",
    "ores_head": "# Minerais",
    "ores_intro": "Les minerais apparaissent dans les tronçons nouvellement explorés. Toucher de soie et Fortune "
                  "fonctionnent comme sur les minerais ordinaires.",
    "ore_line": "{name} : {desc}. Donne {lo}-{hi} {drop} ; nécessite une pioche en {tier}. ",
    "found": "Se trouve {w}.",
    "where": "de y {lo} à {hi} {b}",
    "biomes": "dans : {b}",
    "overworld": "dans la Surface",
    "nether": "dans le Nether",
    "power": "Énergie",
    "power_head": "# Énergie",
    "power_intro": "Les machines électriques prennent le courant des blocs d'énergie voisins, ou reliés par du fil de "
                   "cuivre (jusqu'à 64 blocs de fil) ou du câble isolé (quatre fois plus loin).",
    "power_line": "{name} (niveau {lvl}, {project}) : {desc}.",
    "isotopes": "Radiation et isotopes",
    "containers": "Récipients",
    "upgrades": "Améliorations",
    "farm": "Ferme et graisse",
    "tar": "Goudron et remèdes",
    "industry": "Chimie industrielle",
    "life": "Sciences de la vie",
    "mining": "Minage",
    "crafting": "Artisanat",
    "craft_head": "# Artisanat",
    "craft_intro": "Les recettes sans forme qu'ajoute le mod (les recettes des machines et des blocs sont dans les "
                   "chapitres de niveau).",
    "shaped_head": "# Recettes avec forme (lignes de haut en bas)",
    "or": " ou ",
    "any": "n'importe quel(le) {x}",
    "empty": "vide",
    "reagent": "(réactif)",
    "carbon": " + {c} carbone",
    "nothing": "rien n'est gardé",
    "slag": " ; scories : ",
    "vents": " ; rejette ",
    "temp": ". De {a} à {b} °C.",
    "sep": " ; ",
}

WORDS = {
    # pickaxe tiers
    "wood": "bois", "stone": "pierre", "iron": "fer",
    # biomes and biome tags
    "mountain": "montagnes", "jungle": "jungles", "savanna": "savanes", "desert": "désert", "badlands": "badlands",
    "ocean": "océans", "swamp": "marais", "mangrove swamp": "mangrove", "basalt deltas": "deltas de basalte",
    "beach": "plages", "river": "rivières", "dripstone caves": "grottes de spéléothèmes",
    # item tags
    "logs": "bûche", "logs that burn": "bûche combustible", "oak logs": "bûche de chêne", "planks": "planches",
    "wool": "laine", "beds": "lit", "cable insulation": "isolant de câble", "soap": "savon",
    # upgrades unlocked by research
    "forced air": "air forcé (soufflets)", "steam blast": "soufflage à la vapeur",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["fr_fr"]
T["atlas"] = _ATLAS_TITLE["fr_fr"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["fr_fr"]
T.update(_TREES_T["fr_fr"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("fr_fr")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["fr_fr"]
T["compendium"] = _COMP_TITLE["fr_fr"]

# 3.5.0 Plant Compendium (tools/compendium_plants_fr_fr.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["fr_fr"]
T["plant_compendium"] = _PCOMP_TITLE["fr_fr"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_fr_fr.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["fr_fr"]
T["mineral_compendium"] = _MCOMP_TITLE["fr_fr"]
