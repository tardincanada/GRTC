"""Tree Compendium (3.1.0): the real facts behind every tree, in English (translations in compendium_i18n.py).

DATA[species] = (family, typical height in metres, IUCN Red List category, native range, about).
"about" is the base page (no research needed): what the tree looks like and one true thing worth knowing.
Families and ranges show after Species Plantarum, the IUCN category after The Red List.
Written from general botanical knowledge (2026-11); the IUCN category is the global assessment where one exists,
"NE" where the species was never assessed or exists only in cultivation.
"""

# Vanilla trees: the real species each one stands for (None: a tree of this world only).
VANILLA = {
    "oak": {"name": "Oak", "scientific": "Quercus robur", "icon": "minecraft:oak_sapling",
            "blocks": ["oak_log", "oak_wood", "stripped_oak_log", "stripped_oak_wood", "oak_leaves", "oak_sapling"],
            "biomes": ["plains", "sunflower_plains", "forest", "flower_forest", "dark_forest", "swamp", "windswept_hills",
                       "windswept_forest", "meadow"]},
    "birch": {"name": "Birch", "scientific": "Betula pendula", "icon": "minecraft:birch_sapling",
              "blocks": ["birch_log", "birch_wood", "stripped_birch_log", "stripped_birch_wood", "birch_leaves", "birch_sapling"],
              "biomes": ["birch_forest", "old_growth_birch_forest", "forest", "flower_forest", "meadow"]},
    "spruce": {"name": "Spruce", "scientific": "Picea abies", "icon": "minecraft:spruce_sapling",
               "blocks": ["spruce_log", "spruce_wood", "stripped_spruce_log", "stripped_spruce_wood", "spruce_leaves",
                          "spruce_sapling"],
               "biomes": ["taiga", "snowy_taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga", "windswept_hills",
                          "windswept_forest", "grove", "snowy_plains", "windswept_gravelly_hills"]},
    "jungle": {"name": "Jungle Tree", "scientific": None, "icon": "minecraft:jungle_sapling",
               "blocks": ["jungle_log", "jungle_wood", "stripped_jungle_log", "stripped_jungle_wood", "jungle_leaves",
                          "jungle_sapling"],
               "biomes": ["jungle", "sparse_jungle", "bamboo_jungle"]},
    "acacia": {"name": "Acacia", "scientific": "Vachellia tortilis", "icon": "minecraft:acacia_sapling",
               "blocks": ["acacia_log", "acacia_wood", "stripped_acacia_log", "stripped_acacia_wood", "acacia_leaves",
                          "acacia_sapling"],
               "biomes": ["savanna", "savanna_plateau", "windswept_savanna"]},
    "dark_oak": {"name": "Dark Oak", "scientific": None, "icon": "minecraft:dark_oak_sapling",
                 "blocks": ["dark_oak_log", "dark_oak_wood", "stripped_dark_oak_log", "stripped_dark_oak_wood",
                            "dark_oak_leaves", "dark_oak_sapling"],
                 "biomes": ["dark_forest"]},
    "mangrove": {"name": "Mangrove", "scientific": "Rhizophora mangle", "icon": "minecraft:mangrove_propagule",
                 "blocks": ["mangrove_log", "mangrove_wood", "stripped_mangrove_log", "stripped_mangrove_wood",
                            "mangrove_leaves", "mangrove_propagule", "mangrove_roots", "muddy_mangrove_roots"],
                 "biomes": ["mangrove_swamp"]},
    "cherry": {"name": "Cherry Tree", "scientific": "Prunus serrulata", "icon": "minecraft:cherry_sapling",
               "blocks": ["cherry_log", "cherry_wood", "stripped_cherry_log", "stripped_cherry_wood", "cherry_leaves",
                          "cherry_sapling"],
               "biomes": ["cherry_grove"]},
    "azalea": {"name": "Azalea", "scientific": "Rhododendron", "icon": "minecraft:flowering_azalea",
               "blocks": ["azalea_leaves", "flowering_azalea_leaves", "azalea", "flowering_azalea"],
               "biomes": ["lush_caves"]},
}

DATA = {
    # ---- Vanilla ----
    "oak": ("Fagaceae", 35, "LC", "Europe to the Caucasus",
            "A broad, long-lived tree with lobed leaves and acorns. One oak can host hundreds of insect species, and its "
            "timber built Europe's ships and cathedrals for a thousand years."),
    "birch": ("Betulaceae", 25, "LC", "Europe and northern Asia",
              "A slender pioneer with white bark that peels in papery strips. Birch bark is waterproof and burns even when "
              "damp; people made canoes, boxes and the first written letters of Novgorod on it."),
    "spruce": ("Pinaceae", 45, "LC", "Northern and central Europe",
               "A tall conical conifer with short square needles and hanging cones. Its light, even-grained wood gives the "
               "soundboards of violins and pianos; it is the classic Christmas tree."),
    "jungle": (None, 30, None, None,
               "A tree of this world only: a towering rainforest trunk hung with vines and cocoa pods. Real rainforests hold "
               "hundreds of tree species in a single hectare instead."),
    "acacia": ("Fabaceae", 12, "LC", "Africa and the Arabian Peninsula",
               "The umbrella thorn: a flat crown of tiny leaves on a forked trunk, armed with long spines. Giraffes reach "
               "past the thorns with tongues half a metre long."),
    "dark_oak": (None, 25, None, None,
                 "A tree of this world only: a thick four-trunked oak that shades the forest floor so deeply that "
                 "mushrooms grow huge beneath it."),
    "mangrove": ("Rhizophoraceae", 20, "LC", "Tropical Atlantic and eastern Pacific coasts",
                 "The red mangrove stands on arching stilt roots in salt water. Its seeds sprout while still on the tree "
                 "and drop as spears that float for months before rooting."),
    "cherry": ("Rosaceae", 10, "NE", "Japan, Korea and China",
               "The Japanese flowering cherry: grown for its clouds of pink blossom, not for fruit. Its brief bloom each "
               "spring is celebrated in Japan as hanami."),
    "azalea": ("Ericaceae", 3, "NE", "Mountains of Asia, Europe and North America",
               "Azaleas are shrubby rhododendrons with funnel-shaped flowers. Honey from some species is poisonous; "
               "in this world they mark the lush caves below."),
}

# ---- Stage 1: tropical and arid ----
DATA.update({
    "mahogany": ("Meliaceae", 45, "VU", "Mexico to the southern Amazon",
                 "Big-leaf mahogany: a straight giant with reddish, stable timber. Centuries of logging for fine furniture "
                 "made it rare, and its trade is now controlled by treaty."),
    "brazil_nut": ("Lecythidaceae", 50, "VU", "Amazon rainforest",
                   "One of the tallest Amazon trees. Its nuts grow inside a hard ball the size of a coconut that only the "
                   "agouti can gnaw open; the agouti buries the spare nuts and plants new trees."),
    "brazilwood": ("Fabaceae", 15, "EN", "Atlantic Forest of Brazil",
                   "A thorny tree with orange-red heartwood that gave Brazil its name. Its red dye was shipped to Europe "
                   "from 1501; today its wood makes the finest violin bows."),
    "sapodilla": ("Sapotaceae", 30, "LC", "Southern Mexico and Central America",
                  "An evergreen with sweet brown fruit that tastes of pear and malt. Its milky latex, chicle, was the "
                  "original chewing gum."),
    "andiroba": ("Meliaceae", 30, "LC", "Amazon basin and the Caribbean",
                 "A rainforest relative of mahogany with large woody fruits. The bitter oil pressed from its seeds is "
                 "used against insect bites and as a lamp oil."),
    "acai": ("Arecaceae", 20, "LC", "Amazon estuary",
             "A slender palm that grows in clumps on flooded riverbanks. Its purple berries are a staple food at the "
             "mouth of the Amazon, and its heart is eaten as palm heart."),
    "babassu": ("Arecaceae", 20, "LC", "Eastern Amazon and northeast Brazil",
                "A tall palm with huge upright fronds. Its hard nuts hold an oil much like coconut oil; women in Maranhão "
                "crack them by hand with an axe and a club."),
    "copaiba": ("Fabaceae", 25, "LC", "Brazil, Paraguay and Bolivia",
                "A tree whose trunk holds an oily resin that flows from a drilled hole. Copaiba balsam was sold in "
                "European pharmacies from the 1600s."),
    "cinchona": ("Rubiaceae", 15, "NE", "Andes of Ecuador and Peru",
                 "A small Andean tree whose bitter bark holds quinine, the first cure for malaria. It appears on the coat "
                 "of arms of Peru."),
    "oil_palm": ("Arecaceae", 20, "LC", "West and Central Africa",
                 "A feather palm whose orange fruit bunches give palm oil, the most produced vegetable oil on Earth. "
                 "One bunch can weigh 40 kilograms."),
    "teak": ("Lamiaceae", 40, "EN", "India, Myanmar, Laos and Thailand",
             "A tall deciduous tree with leaves as big as a dinner plate. Its oily golden wood resists water and termites, "
             "so it was the timber of ship decks."),
    "cinnamon": ("Lauraceae", 12, "NE", "Sri Lanka and southern India",
                 "An evergreen laurel whose inner bark, peeled and dried, curls into cinnamon sticks. Its scent comes from "
                 "cinnamaldehyde."),
    "camphor_laurel": ("Lauraceae", 30, "LC", "China, Japan and Korea",
                       "A broad evergreen whose leaves smell of camphor when crushed. Camphor distilled from its wood "
                       "made celluloid, the first plastic film."),
    "sal": ("Dipterocarpaceae", 35, "LC", "Himalayan foothills of India and Nepal",
            "A tall, nearly evergreen tree that forms whole forests. Its hard wood frames Indian houses and its resin is "
            "burned as incense; the Buddha is said to have been born under one."),
    "nutmeg": ("Myristicaceae", 15, "DD", "Banda Islands of Indonesia",
               "An evergreen whose seed is nutmeg and whose red seed lace is mace. Wars were fought over the tiny Banda "
               "Islands where it alone grew."),
    "baobab": ("Malvaceae", 25, "LC", "Tropical Africa",
               "A huge bottle-shaped trunk that stores water for the dry season. Baobabs can live more than 2,000 years; "
               "their dry fruit pulp is rich in vitamin C."),
    "gum_arabic_tree": ("Fabaceae", 7, "LC", "Sahel and Sudan",
                        "A small thorny tree of the Sahel. Its sap hardens into gum arabic, used in soft drinks, stamps, "
                        "watercolours and printing inks."),
    "marula": ("Anacardiaceae", 15, "LC", "Southern and East Africa",
               "A savanna tree with plum-sized yellow fruits loved by elephants. The fruit is made into beer and "
               "liqueur, and its nuts give a stable oil."),
    "shea": ("Sapotaceae", 15, "VU", "West and Central African savanna",
             "A slow-growing tree that first fruits after 15 years. Its nuts give shea butter, a fat used in food, "
             "soap and chocolate."),
    "eucalyptus": ("Myrtaceae", 55, "LC", "Tasmania and southeast Australia",
                   "The Tasmanian blue gum: a fast-growing giant with peeling bark and blue juvenile leaves. Its oil, "
                   "eucalyptol, clears the nose; its plantations feed paper mills worldwide."),
    "pequi": ("Caryocaraceae", 10, "LC", "Cerrado of Brazil",
              "A twisted cerrado tree with a yellow, strongly scented fruit. Under the pulp the seed hides fine spines, "
              "so the fruit is gnawed, never bitten."),
    "barbatimao": ("Fabaceae", 6, "LC", "Cerrado of Brazil",
                   "A small cerrado tree with thick corky bark. The bark is so rich in tannin that it was used to tan "
                   "leather and to treat wounds."),
    "yellow_ipe": ("Bignoniaceae", 20, "LC", "Tropical South America",
                   "In the dry season it drops every leaf and flowers bright yellow. Its wood, ipê, is one of the "
                   "hardest and densest timbers in the world."),
    "jatoba": ("Fabaceae", 30, "LC", "Central and South America",
               "A large tree with paired leaflets and pods full of sweet, powdery pulp. Its resin, copal, hardened "
               "into amber over millions of years."),
    "sandalwood": ("Santalaceae", 10, "VU", "India, Indonesia and northern Australia",
                   "A half-parasite whose roots tap other plants' roots for water. Its scented heartwood and oil are "
                   "so valuable that wild trees have been cut almost out."),
    "dragon_tree": ("Asparagaceae", 10, "VU", "Socotra Island, Yemen",
                    "An umbrella of stiff leaves on thick branches, found only on Socotra. Its red resin, dragon's blood, "
                    "was a pigment and varnish in the ancient world."),
    "frankincense": ("Burseraceae", 5, "NT", "Oman, Yemen and Somalia",
                     "A small tree that grows on bare rock. Cuts in its papery bark weep a milky resin that dries into "
                     "frankincense, traded across Arabia for 5,000 years."),
    "myrrh": ("Burseraceae", 4, "LC", "Horn of Africa and southern Arabia",
              "A spiny shrubby tree of dry stony ground. Its reddish resin, myrrh, was used for perfume, medicine and "
              "embalming in ancient Egypt."),
    "date_palm": ("Arecaceae", 25, "NE", "Middle East and North Africa",
                  "The desert's crop palm, grown in oases for at least 7,000 years. Female trees bear clusters of up to "
                  "a thousand dates each."),
    "mesquite": ("Fabaceae", 12, "LC", "Mexico, Central America and the Caribbean",
                 "A thorny tree with roots that can reach water 50 metres down. Its sweet pods feed cattle and were "
                 "ground into flour by desert peoples."),
    "joshua_tree": ("Asparagaceae", 12, "LC", "Mojave Desert",
                    "A giant yucca with spiky branching arms, not a true tree. Only one moth pollinates its flowers, "
                    "and the moth's larvae eat only its seeds."),
    "saxaul": ("Amaranthaceae", 8, "LC", "Deserts of Central Asia",
               "A leafless desert tree with green twigs instead of leaves. Its wood is so dense it sinks in water and "
               "burns like coal."),
    "argan": ("Sapotaceae", 10, "VU", "Southwest Morocco",
              "A thorny, drought-proof tree that goats climb to eat the fruit. Its kernels give argan oil, used in "
              "cooking and cosmetics."),
    "palo_verde": ("Fabaceae", 8, "LC", "Sonoran Desert",
                   "Its name means green stick: the bark is green and does most of the photosynthesis once the tiny "
                   "leaves fall in drought. In spring it turns bright yellow with flowers."),
    "pinyon_pine": ("Pinaceae", 10, "LC", "Colorado Plateau and the US Southwest",
                    "A small, slow pine of dry hills. Its large nuts were a main food of the peoples of the Great Basin "
                    "and are still picked every autumn."),
    "utah_juniper": ("Cupressaceae", 8, "LC", "Great Basin and the Colorado Plateau",
                     "A gnarled juniper of dry slopes that can live 650 years. Its blue berry-like cones are really "
                     "fleshy cones, eaten by birds."),
    "desert_ironwood": ("Fabaceae", 10, "LC", "Sonoran Desert",
                        "One of the heaviest woods in the world: it sinks in water. Many desert plants start their "
                        "lives in its shade, so it is called a nurse tree."),
    "tamarisk": ("Tamaricaceae", 12, "LC", "North Africa to India",
                 "A salt cedar with tiny scale leaves that sweat salt. It grows where the ground water is too salty "
                 "for other trees."),
    "ponderosa_pine": ("Pinaceae", 50, "LC", "Western North America",
                       "A tall pine with orange plated bark that smells of vanilla on warm days. Its thick bark lets it "
                       "survive the low fires that sweep its forests."),
    "gambel_oak": ("Fagaceae", 10, "LC", "US Southwest and the Rocky Mountains",
                   "A shrubby oak that spreads by roots into thickets. Its acorns fed deer, turkeys and the peoples of "
                   "the region."),
    "mountain_mahogany": ("Rosaceae", 8, "LC", "Mountains of western North America",
                          "Not a mahogany but a rose relative. Its seeds carry a feathery tail that twists with "
                          "humidity and drills the seed into the soil."),
    "elephant_tree": ("Burseraceae", 5, "LC", "Sonoran Desert and Baja California",
                      "A squat tree with a swollen, water-storing trunk and papery peeling bark. Its twigs smell of "
                      "turpentine and give a copal resin."),
    "coconut_palm": ("Arecaceae", 25, "NE", "Tropical shores of the Indian and Pacific Oceans",
                     "The palm of tropical beaches. Its nut floats and stays alive for months at sea, which is how it "
                     "spread across the oceans."),
    "tropical_almond": ("Combretaceae", 25, "LC", "Tropical Asia, Australia and the Pacific",
                        "A beach tree with tiered branches and big leaves that turn red before falling. Its seeds taste "
                        "like almonds, and its fallen leaves tint water brown with tannin."),
    "casuarina": ("Casuarinaceae", 30, "LC", "Southeast Asia, Australia and the Pacific",
                  "It looks like a pine but is a flowering tree with jointed green twigs. Its roots fix nitrogen, so it "
                  "grows on bare sand."),
    "screw_pine": ("Pandanaceae", 10, "LC", "Pacific and Indian Ocean coasts",
                   "Leaves spiral round the trunk like a screw, and prop roots hold it upright in sand. Its leaves are "
                   "woven into mats and roofs."),
    "sea_grape": ("Polygonaceae", 10, "LC", "Coasts of tropical America",
                  "A salt-tolerant beach tree with round leathery leaves. Its purple fruits hang in grape-like bunches "
                  "and make a tart jelly."),
    "black_mangrove": ("Acanthaceae", 15, "LC", "Tropical Atlantic and Pacific coasts of the Americas",
                       "A mangrove whose roots send up pencil-like breathing shoots through the mud. It pushes out "
                       "salt through its leaves, which glitter with crystals."),
    "white_mangrove": ("Combretaceae", 15, "LC", "Tropical Atlantic coasts of the Americas and West Africa",
                       "It grows on the landward side of the mangrove. Two small glands at the base of each leaf "
                       "give off sugar and salt."),
    "buttonwood": ("Combretaceae", 12, "LC", "Tropical coasts of the Americas and West Africa",
                   "A mangrove of the upper shore with button-like seed heads. Its dense wood was burned for charcoal "
                   "in the Caribbean."),
    "nipa_palm": ("Arecaceae", 9, "LC", "Mangroves of Asia and the western Pacific",
                  "A stemless palm whose trunk runs under the mud, with only the fronds above. Its sweet sap is tapped "
                  "for sugar, vinegar and alcohol."),
    "buriti": ("Arecaceae", 35, "LC", "Amazon and the Cerrado wetlands",
               "A tall fan palm of swamps and riverbanks. Its scaly red fruit is very rich in vitamin A; its leaf "
               "fibre is woven into hammocks."),
})

# ---- Stage 2: temperate ----
DATA.update({
    "eastern_cottonwood": ("Salicaceae", 35, "LC", "Eastern and central North America",
                           "A fast-growing poplar of river bottoms with triangular leaves. In early summer its seeds "
                           "float away on tufts of white cotton."),
    "american_elm": ("Ulmaceae", 30, "LC", "Eastern North America",
                     "A vase-shaped tree that once lined the streets of American towns. Dutch elm disease, a fungus "
                     "spread by beetles, killed most of them after 1930."),
    "ash": ("Oleaceae", 35, "NT", "Europe to the Caucasus",
            "A tall tree with feathered leaves and black winter buds. Its tough, springy wood made tool handles, oars "
            "and bows; ash dieback, a fungus, now threatens it."),
    "walnut": ("Juglandaceae", 25, "NT", "Central Asia to the Balkans",
               "The common walnut: a broad tree with fragrant leaves and edible nuts. Its roots release juglone, which "
               "keeps many other plants from growing nearby."),
    "white_mulberry": ("Moraceae", 15, "LC", "Northern China",
                       "A small tree whose leaves are the only food of the silkworm. Its flower stamens snap open faster "
                       "than any known plant movement, flinging pollen."),
    "linden": ("Malvaceae", 30, "LC", "Europe and western Siberia",
               "The small-leaved lime: heart-shaped leaves and fragrant flowers that bees love. Its soft white wood is "
               "a favourite of carvers, and its inner bark gave rope."),
    "sweet_chestnut": ("Fagaceae", 30, "LC", "Southern Europe and Asia Minor",
                       "A broad tree with long toothed leaves and spiny husks holding chestnuts. In many mountain "
                       "villages chestnuts were the bread of the poor."),
    "pear": ("Rosaceae", 15, "NE", "Europe and western Asia",
             "The pear tree: white spring blossom and juicy fruit, grown since antiquity. Pear wood is hard and fine "
             "enough to cut printing blocks and rulers."),
    "elder": ("Adoxaceae", 8, "LC", "Europe, North Africa and western Asia",
              "A shrubby tree with flat heads of scented cream flowers and dark purple berries. The berries must be "
              "cooked: raw, they upset the stomach."),
    "beech": ("Fagaceae", 40, "LC", "Western and central Europe",
              "A tall tree with smooth grey bark and a dense shade where little else grows. Its small nuts, beechmast, "
              "fattened pigs in autumn."),
    "sugar_maple": ("Sapindaceae", 35, "LC", "Eastern Canada and the northeast United States",
                    "In late winter its sap rises with about 3% sugar; forty litres boil down to one litre of syrup. "
                    "Its leaf is on the flag of Canada."),
    "hornbeam": ("Betulaceae", 25, "LC", "Europe to Iran",
                 "A tree with fluted grey trunk and the hardest wood in Europe. It made cogs for mills, butchers' "
                 "blocks and piano hammers."),
    "southern_magnolia": ("Magnoliaceae", 25, "LC", "Southeast United States",
                          "An evergreen with glossy leaves and huge white flowers that smell of lemon. Magnolias are "
                          "an old lineage, pollinated by beetles before bees existed."),
    "tulip_tree": ("Magnoliaceae", 45, "LC", "Eastern North America",
                   "One of the tallest hardwoods in the east, with leaves shaped like a tulip's outline and green-and-"
                   "orange tulip-like flowers."),
    "paulownia": ("Paulowniaceae", 15, "LC", "Central and western China",
                  "One of the fastest-growing trees, with huge leaves and violet flowers. In Japan its light wood made "
                  "the chests given to daughters on marriage."),
    "judas_tree": ("Fabaceae", 10, "LC", "Southern Europe and western Asia",
                   "In spring pink-purple flowers burst straight out of its trunk and branches before the leaves. The "
                   "flowers are edible and taste sweet and sour."),
    "quaking_aspen": ("Salicaceae", 25, "LC", "Canada and the western United States",
                      "Its leaves tremble at the slightest breeze. One aspen grove in Utah, Pando, is a single plant "
                      "with 47,000 stems sharing one root system."),
    "alder": ("Betulaceae", 25, "LC", "Europe, North Africa and western Asia",
              "A riverside tree whose roots fix nitrogen with bacteria. Its wood lasts under water: Venice stands on "
              "alder piles."),
    "rowan": ("Rosaceae", 15, "LC", "Europe and northern Asia",
              "A small tree with feathered leaves and bunches of scarlet berries that birds strip in autumn. Sorbic "
              "acid, a common food preservative, was first found in them."),
    "black_cherry": ("Rosaceae", 25, "LC", "Eastern North America and Mexico",
                     "A forest cherry with dark flaky bark. Its reddish-brown wood is prized for furniture; its leaves "
                     "and seeds are poisonous to cattle."),
    "yew": ("Taxaceae", 15, "LC", "Europe, North Africa and western Asia",
            "A dark evergreen that can live over 2,000 years. Almost every part is poisonous; its springy wood made the "
            "English longbow, and its relative gave the cancer drug taxol."),
    "black_walnut": ("Juglandaceae", 30, "LC", "Eastern United States",
                     "A tall walnut with dark, prized timber and thick-shelled nuts. The green husks stain hands and "
                     "cloth dark brown."),
    "holly": ("Aquifoliaceae", 12, "LC", "Western and southern Europe",
              "An evergreen with spiny glossy leaves; only female trees bear the red berries. Upper leaves out of reach "
              "of browsing deer often lose their spines."),
    "bald_cypress": ("Cupressaceae", 35, "LC", "Southeast United States",
                     "A swamp conifer that drops its needles in winter. Knobby 'knees' rise from its roots above the "
                     "water; its wood resists rot for centuries."),
    "water_tupelo": ("Nyssaceae", 30, "LC", "Southeast United States",
                     "A swamp tree with a swollen base standing in water for months. Bees make a pale honey from its "
                     "flowers that never crystallises."),
    "white_willow": ("Salicaceae", 25, "LC", "Europe and western Asia",
                     "A riverside willow with silvery leaves. Its bark holds salicin, the natural model for aspirin; "
                     "a hybrid of it gives the wood of cricket bats."),
    "red_maple": ("Sapindaceae", 30, "LC", "Eastern North America",
                  "The most common tree in eastern North America, red in flower, stalk and autumn leaf. Its sap is "
                  "tapped too, but gives less sugar than sugar maple."),
    "ginkgo": ("Ginkgoaceae", 25, "EN", "Southwest China",
               "A living fossil, the last of a group that grew with the dinosaurs. Its fan leaves turn gold at once in "
               "autumn; six ginkgos survived the Hiroshima bomb."),
    "japanese_maple": ("Sapindaceae", 8, "LC", "Japan, Korea and China",
                       "A small maple with deeply cut leaves that blaze red in autumn. Japanese gardeners have bred "
                       "hundreds of forms over three centuries."),
    "sugi": ("Cupressaceae", 50, "NT", "Japan",
             "Japan's national tree, a towering conifer planted around shrines. Some sugi on Yakushima island are "
             "thought to be over 2,000 years old."),
    "lacquer_tree": ("Anacardiaceae", 15, "NE", "China and the Himalaya",
                     "Its sap, urushi, hardens in damp air into the glossy lacquer of East Asian bowls. The raw sap "
                     "burns the skin like poison ivy, its relative."),
    "cork_oak": ("Fagaceae", 20, "LC", "Western Mediterranean",
                 "An evergreen oak with thick spongy bark that is stripped every nine years without killing the tree. "
                 "Portugal produces half the world's cork."),
    "olive": ("Oleaceae", 10, "NE", "Mediterranean Basin",
              "A gnarled evergreen with silver-grey leaves, grown for oil for 6,000 years. Olive trees can live and "
              "fruit for more than a thousand years."),
    "cedar_of_lebanon": ("Pinaceae", 35, "VU", "Mountains of Lebanon, Syria and Turkey",
                         "A majestic conifer with flat layered branches, on the flag of Lebanon. Its scented, lasting "
                         "wood built temples and ships of the ancient world."),
    "black_pine": ("Pinaceae", 40, "LC", "Southern Europe and Turkey",
                   "A pine with long stiff dark needles that bears drought, wind and poor soil. It is tapped for resin "
                   "and planted as a windbreak."),
    "carob": ("Fabaceae", 12, "LC", "Eastern Mediterranean",
              "An evergreen with long brown pods of sweet pulp. Its seeds are so even in weight that they became the "
              "carat, the unit for gems and gold."),
    "stone_pine": ("Pinaceae", 25, "LC", "Mediterranean Basin",
                   "The umbrella pine of Rome, with a flat spreading crown. Its large seeds are the pine nuts of pesto "
                   "and Mediterranean sweets."),
    "aleppo_pine": ("Pinaceae", 20, "LC", "Mediterranean Basin",
                    "A drought-proof pine of hot coasts. Greek retsina wine was first flavoured by its resin, used to "
                    "seal amphorae."),
    "monterey_cypress": ("Cupressaceae", 25, "VU", "Two groves on the coast of central California",
                         "Wild only in two coastal groves, but planted around the world as a windbreak. Ocean winds "
                         "sculpt it into flat-topped shapes."),
    "apple": ("Rosaceae", 10, "NE", "Tian Shan mountains of Central Asia",
              "The orchard apple, descended from wild apples of Kazakhstan. Seeds never come true: every named "
              "variety is grafted from a single original tree."),
})

# ---- Stage 3: cold and mountain ----
DATA.update({
    "balsam_fir": ("Pinaceae", 20, "LC", "Canada and the northeast United States",
                   "A fragrant fir with blisters of clear resin in its bark. Canada balsam, the resin, was the glue "
                   "for microscope slides and lenses because it has the same refraction as glass."),
    "silver_fir": ("Pinaceae", 50, "LC", "Mountains of central and southern Europe",
                   "A tall fir with silver-white bark and needles with two white bands beneath. It was the first "
                   "Christmas tree of Alsace."),
    "douglas_fir": ("Pinaceae", 75, "LC", "Western North America",
                    "One of the tallest trees on Earth, with cones whose bracts look like mouse tails. Its strong "
                    "timber built bridges, ships' masts and houses."),
    "siberian_fir": ("Pinaceae", 30, "LC", "Siberia and the Urals",
                     "A narrow fir of the taiga, very hardy to cold. Oil distilled from its needles is used in "
                     "medicine and scented soap."),
    "green_alder": ("Betulaceae", 4, "LC", "Mountains and tundra of the northern hemisphere",
                    "A bushy alder of avalanche paths and tundra. It bends under snow without breaking and fixes "
                    "nitrogen in poor soils."),
    "sycamore_maple": ("Sapindaceae", 35, "LC", "Mountains of central and southern Europe",
                       "A large maple with winged seeds that spin like helicopters. Its pale wood makes the backs of "
                       "violins and kitchen tables."),
    "atlas_cedar": ("Pinaceae", 35, "EN", "Atlas Mountains of Morocco and Algeria",
                    "A mountain cedar with blue-green needles in tufts. Barbary macaques live in its forests, which "
                    "shrink from drought and overgrazing."),
    "siberian_pine": ("Pinaceae", 40, "LC", "Siberia and Mongolia",
                      "The Siberian 'cedar', a five-needled pine with large wingless nuts. Nutcrackers spread its "
                      "seeds by burying them for winter."),
    "western_red_cedar": ("Cupressaceae", 60, "LC", "Pacific Northwest of North America",
                          "A huge conifer of rainy coasts with stringy bark. Its rot-proof wood gave the peoples of "
                          "the coast their canoes, houses and totem poles."),
    "sea_buckthorn": ("Elaeagnaceae", 6, "LC", "Europe and Asia",
                      "A thorny shrub of dunes and gravel with silvery leaves. Its orange berries cling to the "
                      "branches and are rich in vitamin C and oil."),
    "european_larch": ("Pinaceae", 45, "LC", "Alps and Carpathians",
                       "A conifer that turns gold and drops its needles every autumn. Its resin, Venice turpentine, "
                       "was used by painters, and its wood resists rot."),
    "dahurian_larch": ("Pinaceae", 30, "LC", "Eastern Siberia and northeast Asia",
                       "The northernmost tree on Earth, growing on permafrost at 72° north. It survives winters "
                       "below minus 60 °C."),
    "siberian_dwarf_pine": ("Pinaceae", 3, "LC", "Northeast Siberia and Japan",
                            "A creeping pine that lies flat under the snow in winter and rises again in spring. "
                            "Its nuts feed bears and sables."),
    "eastern_white_pine": ("Pinaceae", 50, "LC", "Eastern Canada and the northeast United States",
                           "The tallest tree of eastern North America, with soft needles in fives. The British crown "
                           "reserved the biggest ones for the masts of its navy."),
    "bristlecone_pine": ("Pinaceae", 15, "LC", "White Mountains and the Great Basin, USA",
                         "The oldest living trees: one, Methuselah, is over 4,800 years old. Their rings have been "
                         "used to calibrate radiocarbon dating."),
    "swiss_pine": ("Pinaceae", 25, "LC", "Alps and Carpathians",
                   "A slow, five-needled pine near the tree line, with fragrant wood used for alpine rooms. Its seeds "
                   "are spread by the spotted nutcracker."),
    "whitebark_pine": ("Pinaceae", 20, "EN", "Mountains of western North America",
                       "A high-mountain pine whose cones never open by themselves; Clark's nutcracker breaks them. "
                       "Blister rust and beetles are killing it."),
    "lodgepole_pine": ("Pinaceae", 30, "LC", "Western North America",
                       "Straight and slim, used by plains peoples for tipi poles. Many of its cones open only in "
                       "the heat of a forest fire."),
    "mountain_pine": ("Pinaceae", 5, "LC", "Mountains of central and southern Europe",
                      "A low, many-stemmed pine of the alpine tree line that holds snow and soil on steep slopes. Its "
                      "oil is used in cough remedies."),
    "scots_pine": ("Pinaceae", 35, "LC", "Europe and northern Asia",
                   "The most widespread pine in the world, with orange upper bark. Its resin gave turpentine and "
                   "pine tar for the ropes and hulls of sailing ships."),
    "arctic_willow": ("Salicaceae", 0.2, "LC", "Arctic tundra",
                      "A tree only a hand high that creeps over the tundra. Its stems may be centuries old; its "
                      "leaves feed musk oxen and hares."),
    "coast_redwood": ("Cupressaceae", 115, "EN", "Coast of northern California and southern Oregon",
                      "The tallest trees on Earth: Hyperion stands 116 metres. Fog from the Pacific gives them "
                      "water in the dry summer."),
    "giant_sequoia": ("Cupressaceae", 85, "EN", "Western slopes of the Sierra Nevada",
                      "The largest trees by volume: the General Sherman tree weighs about 2,000 tonnes. Its bark is "
                      "half a metre thick and shrugs off fire."),
    "western_hemlock": ("Pinaceae", 60, "LC", "Pacific Northwest of North America",
                        "A shade-loving conifer with a drooping top that grows up under other trees. Its bark is rich "
                        "in tannin and its pulp makes paper."),
    "common_juniper": ("Cupressaceae", 5, "LC", "Across the northern hemisphere",
                       "The most widespread conifer on Earth, a shrub of heaths and tundra. Its blue cones, juniper "
                       "berries, flavour gin."),
})

# Biomes, in the book's order: (mean yearly temperature in °C, yearly rain in mm, about). The climate is that of the
# real place the biome stands for; it shows after Humboldt's Geography of Plants.
BIOMES = {
    "jungle": (26, 2500, "Like the Amazon lowlands: hot and wet all year, with evergreen trees in several storeys and "
                         "a dim forest floor."),
    "sparse_jungle": (25, 1800, "The edge of the rainforest, broken by clearings and rivers, where palms and "
                                "light-loving trees find room."),
    "bamboo_jungle": (24, 2000, "Like the monsoon forests of South and Southeast Asia: a wet season, a dry season, and "
                                "thickets of bamboo under the trees."),
    "savanna": (25, 900, "Grassland with scattered trees, like East Africa: rains in one season, fire and drought in "
                         "the other."),
    "savanna_plateau": (21, 1400, "High savanna like the Brazilian cerrado: twisted trees with thick corky bark on old, "
                                  "poor soils."),
    "windswept_savanna": (24, 500, "Dry, windy, rocky savanna like the Horn of Africa and Socotra, home of the incense "
                                   "trees."),
    "desert": (25, 100, "Hot desert like the Sahara and Arabia: trees live only where roots reach water, as in oases."),
    "badlands": (17, 250, "Eroded red rock country like the Colorado Plateau, with scrub of pinyon and juniper."),
    "wooded_badlands": (12, 400, "The higher, cooler mesas of the US Southwest, where pine and oak woodland grows."),
    "eroded_badlands": (18, 200, "Bare, gullied hills like the Sonoran Desert edge, where only the toughest trees "
                                 "survive."),
    "beach": (26, 1500, "A tropical shore of sand and salt spray, like the Pacific islands and the Caribbean."),
    "mangrove_swamp": (27, 2000, "A tidal forest of salt-tolerant trees on muddy tropical coasts, half land and half "
                                 "sea."),
    "plains": (11, 700, "Open grassland of the temperate zone, like the prairies, with trees along the rivers."),
    "sunflower_plains": (12, 650, "Warm grassland and farm country with hedgerows, orchards and scattered trees."),
    "forest": (9, 800, "Temperate broadleaf forest like that of central Europe, green in summer and bare in winter."),
    "flower_forest": (10, 850, "A sunny, flowering woodland edge, rich in ornamental and fruit-bearing trees."),
    "birch_forest": (6, 650, "Cool forest of birch and other light-loving trees, like southern Scandinavia."),
    "old_growth_birch_forest": (6, 700, "An old, tall northern hardwood forest, like those of New England."),
    "dark_forest": (9, 1000, "A dense old forest where the canopy shuts out most light, like Europe's last primeval "
                             "woods."),
    "swamp": (18, 1300, "A warm wooded wetland like the bayous of the southern United States."),
    "cherry_grove": (14, 1500, "Mountain woodland of East Asia, humid and mild, like the hills of Japan."),
    "windswept_hills": (15, 600, "Mediterranean hills: hot dry summers, mild wet winters and evergreen trees."),
    "stony_shore": (16, 500, "A rocky Mediterranean coast of pines and olives, dry in summer."),
    "taiga": (0, 500, "The boreal forest that circles the north of the world: long winters and conifers."),
    "old_growth_pine_taiga": (4, 700, "Old pine forest with deep moss and giant trunks, like the pinewoods of the "
                                      "Rocky Mountains and Scandinavia."),
    "windswept_forest": (3, 900, "Wind-battered mountain forest of conifers on rocky slopes."),
    "snowy_taiga": (-5, 400, "The coldest taiga, like eastern Siberia, on frozen ground most of the year."),
    "snowy_plains": (-10, 250, "Tundra: a cold plain beyond the tree line where only dwarf trees survive."),
    "snowy_beach": (-2, 400, "A cold northern shore of gravel and dunes."),
    "meadow": (4, 1200, "An alpine meadow just below the tree line, full of flowers in summer."),
    "grove": (0, 1000, "A snowy mountain forest of firs and larches near the tree line."),
    "stony_peaks": (-2, 1300, "Bare rocky summits; only stunted trees grow in sheltered cracks."),
    "windswept_gravelly_hills": (2, 900, "Cold, windy hills of loose gravel with low pines and shrubs."),
    "old_growth_spruce_taiga": (9, 2500, "Temperate rainforest like the Pacific Northwest: the tallest conifers on "
                                         "Earth, fed by fog and rain."),
    "ice_spikes": (-15, 200, "Polar desert of snow and ice where trees creep along the ground."),
    "snowy_slopes": (-5, 1100, "Steep mountain snowfields with dwarf pines at the edge."),
    "frozen_peaks": (-12, 900, "Frozen high peaks; the last trees here are centuries old and knee high."),
    "jagged_peaks": (-10, 1000, "Sharp high ridges above the tree line, with a few ancient pines."),
    "lush_caves": (None, None, "Not a forest but a cave under one: where azalea bushes grow on the surface, "
                               "a lush cave lies below."),
}

# ---- 3.1.0 extras: araucaria and fruit trees ----
DATA.update({
    "araucaria": ("Araucariaceae", 40, "CR", "Southern Brazil and northeast Argentina",
                  "A candelabra-shaped conifer whose branches end in tufts at the top. Its big seeds, pinhão, are an "
                  "autumn food in southern Brazil; logging left only a few percent of its forests."),
    "brazilian_rosewood": ("Fabaceae", 25, "VU", "Atlantic Forest of Brazil",
                           "Jacarandá-da-bahia: dark, scented, streaked heartwood once the finest for guitars and "
                           "furniture. Since 1992 its international trade is banned."),
    "mango": ("Anacardiaceae", 30, "DD", "India, Bangladesh and Myanmar",
              "A large evergreen grown for 4,000 years for its sweet fruit. Its sap and peel hold the same irritant as "
              "poison ivy, a relative."),
    "avocado": ("Lauraceae", 20, "LC", "Mexico and Central America",
                "An evergreen laurel whose fruit is rich in oil, not sugar. Its big seed may have been spread by giant "
                "ground sloths that are now extinct."),
    "cashew": ("Anacardiaceae", 12, "NE", "Northeast Brazil",
               "The nut hangs below a fleshy 'apple' that is eaten as fruit or juice. The shell holds a caustic oil, "
               "so the nuts are roasted before they are opened."),
    "orange": ("Rutaceae", 10, "NE", "Southern China and Southeast Asia (cultivated)",
               "The sweet orange, an ancient cross of pomelo and mandarin. Brazil grows more oranges than any other "
               "country."),
    "lemon": ("Rutaceae", 6, "NE", "Northeast India and Myanmar (cultivated)",
              "A thorny evergreen whose sour fruit is rich in vitamin C and citric acid. A ration of lemon juice "
              "protected sailors from scurvy."),
    "papaya": ("Caricaceae", 8, "DD", "Southern Mexico and Central America",
               "A fast, soft-stemmed tree that fruits in its first year. Its latex holds papain, an enzyme that breaks "
               "down meat."),
    "kapok": ("Malvaceae", 60, "LC", "Tropical Americas and West Africa",
              "A giant with buttress roots that towers over the rainforest canopy. Its pods give a light, waterproof "
              "fibre that once filled life jackets; the Maya held it sacred."),
    "sweet_cherry": ("Rosaceae", 20, "LC", "Europe and western Asia",
                     "The wild cherry, parent of the sweet orchard cherries. Its reddish wood is prized by cabinet "
                     "makers, and birds spread its stones."),
    "plum": ("Rosaceae", 10, "NE", "Europe and western Asia (cultivated)",
             "The European plum, an ancient hybrid grown in orchards. Dried plums are prunes; its blue fruit has a "
             "powdery bloom of wax."),
    "peach": ("Rosaceae", 8, "NE", "Northwest China (cultivated)",
              "A small tree with pink spring blossom and velvety fruit. In China the peach stands for long life."),
    "apricot": ("Rosaceae", 10, "NE", "Central Asia and China",
                "An early-flowering fruit tree of dry mountains. Its kernels give an oil and, raw, a bitter poison "
                "like bitter almonds."),
    "fig": ("Moraceae", 8, "LC", "Southwest Asia and the Mediterranean",
            "Its 'fruit' is an inside-out flower head, pollinated by a tiny wasp that crawls inside. Figs were among "
            "the first plants people ever cultivated."),
    "pomegranate": ("Lythraceae", 8, "LC", "Iran to northern India",
                    "A shrubby tree whose leathery fruit holds hundreds of juicy red seeds. Its rind is rich in tannin "
                    "and dyed oriental carpets."),
    "almond": ("Rosaceae", 8, "DD", "Southwest Asia",
               "A relative of the peach grown for its seed. Wild almonds are bitter and poisonous; a single lost gene "
               "made the sweet almond people eat."),
    "pistachio": ("Anacardiaceae", 8, "NT", "Central Asia and Iran",
                  "A desert tree whose ripe nuts split their shells open. Male and female trees are separate, so "
                  "orchards plant one male for every eight females."),
    "hazel": ("Betulaceae", 6, "LC", "Europe and western Asia",
              "A many-stemmed shrubby tree with catkins in late winter. Its straight shoots were woven into fences, "
              "and divining rods were cut from it."),
    "pecan": ("Juglandaceae", 40, "LC", "Mississippi valley and Mexico",
              "A tall hickory of river bottoms with sweet, thin-shelled nuts. It was a food of Native Americans long "
              "before it became a crop."),
    "plane_tree": ("Platanaceae", 30, "LC", "Southeast Europe to Iran",
                   "A broad shade tree with bark that flakes off in patches. Hippocrates is said to have taught under "
                   "one on the island of Kos."),
    "horse_chestnut": ("Sapindaceae", 25, "VU", "Mountains of the Balkans",
                       "A broad tree with candles of white flowers and shiny brown conkers. Its seeds are poisonous "
                       "to people despite the name."),
})
