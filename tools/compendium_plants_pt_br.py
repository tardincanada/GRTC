"""Compêndio de Plantas (3.5.0) em português do Brasil. Ver compendium_plants.py e compendium_i18n.py."""

TITLE = "Compêndio de Plantas"

GUIDE = [
    "# O Compêndio de Plantas",
    "Um livro e uma flor pequena qualquer fazem o Compêndio de Plantas; não precisa de pesquisa. Funciona como o "
    "Compêndio de Árvores: deixe-o na barra de itens e o canto superior esquerdo mostra o bioma e, quando você já a "
    "conhece, a planta para a qual está olhando. Flores, capins, samambaias, arbustos, lavouras, plantas aquáticas e "
    "as plantas das cavernas exuberantes estão todos nele.",
    "Para anotar uma planta, olhe por um instante para ela a até 8 blocos, ou colha-a, com o livro na barra de itens; "
    "plantas debaixo d'água são anotadas do mesmo jeito. Use o livro para ler: biomas à esquerda, as plantas de cada "
    "um à direita.",
    "As Notas do Compêndio copiam os dois livros de uma vez: quem as lê ganha todas as árvores, plantas e biomas que "
    "estão nelas.",
    "Species Plantarum (nível 3): cada planta mostra o nome científico, a família e onde cresce nativa no mundo real.",
    "Geografia das Plantas (nível 3): cada bioma mostra o seu clima, neste livro como no Compêndio de Árvores.",
    "A Lista Vermelha (nível 6): cada planta mostra quanto está ameaçada na natureza.",
]

UI = [
    "Compêndio de Plantas",
    "Planta desconhecida",
    "Nova planta no seu Compêndio: %s",
    "%s de %s plantas e %s de %s biomas anotados.",
    "Deixe este livro na barra de itens. Olhe por um instante para uma planta, ou colha-a, para anotá-la; entre num "
    "bioma para anotar o bioma. Escolha um bioma à esquerda.",
    "Plantas",
    "Uma planta só deste mundo: nenhuma espécie real corresponde a ela.",
    "Compêndio de Plantas",
    "Na barra de itens mostra o bioma e dá nome às plantas que você conhece; use-o para ler sobre elas",
]

PLANTS = {
    "dandelion": ("Dente-de-leão", "Europa e Ásia; hoje em todos os continentes menos a Antártida",
                  "A flor amarela se fecha à noite e vira uma bola de sementes que o vento leva por quilômetros. Tudo "
                  "nela é comestível, e as folhas eram salada de primavera muito antes da alface."),
    "poppy": ("Papoula", "Europa, Norte da África e oeste da Ásia",
              "A papoula-dos-campos segue o arado: as sementes esperam décadas no solo e brotam quando a terra é "
              "revolvida, e por isso cobriu os campos de batalha de Flandres depois de 1914."),
    "blue_orchid": ("Orquídea azul", "Florestas de morro de Assam, Mianmar e norte da Tailândia",
                    "Uma das pouquíssimas orquídeas de fato azuis. Na natureza vive presa a troncos em florestas "
                    "frescas de morro; colecionadores esvaziaram vales inteiros, e hoje o comércio de plantas "
                    "silvestres é proibido."),
    "allium": ("Álio", "Montanhas do Irã, do Afeganistão e da Ásia Central",
               "Uma cebola ornamental: uma bola de centenas de florzinhas roxas no alto de uma haste comprida. Esmague "
               "uma folha e ela cheira a cebola, como o alho e o alho-poró, seus parentes."),
    "azure_bluet": ("Houstônia", "Leste da América do Norte",
                    "Uma florzinha de quatro pétalas que cobre gramados e prados de azul-claro na primavera. Pequena "
                    "assim, é da família do café."),
    "tulip": ("Tulipa", "Estepes de montanha da Turquia e da Ásia Central",
              "Levada do Império Otomano para os Países Baixos nos anos 1500; em 1637 um único bulbo chegou a valer "
              "mais que uma casa, até o mercado quebrar. As pétalas listradas tão cobiçadas eram causadas por um "
              "vírus."),
    "oxeye_daisy": ("Margarida", "Europa e Ásia temperada",
                    "A margarida branca dos prados e beiras de estrada. Cada flor é na verdade centenas de florzinhas: "
                    "as amarelas no meio, as brancas em volta."),
    "cornflower": ("Centáurea-azul", "Sudeste da Europa e Oriente Médio, levada com o trigo por toda a Europa",
                   "Antes era erva daninha de todo trigal, e quase sumiu da Europa quando os agricultores passaram a "
                   "limpar as sementes e usar herbicidas. O seu azul deu nome a uma cor."),
    "lily_of_the_valley": ("Lírio-do-vale", "Bosques frescos da Europa e da Ásia",
                           "Sininhos brancos e perfumados nos bosques sombreados da primavera. A planta inteira é "
                           "venenosa: tem glicosídeos cardíacos como a dedaleira, que a medicina usa contra a "
                           "insuficiência cardíaca."),
    "sunflower": ("Girassol", "América do Norte; cultivado no México há uns 4.000 anos",
                  "As cabeças jovens seguem o sol de leste a oeste todo dia; a flor aberta fica virada para o leste de "
                  "vez. Das sementes se tira um dos principais óleos de cozinha do mundo."),
    "lilac": ("Lilás", "Morros rochosos dos Bálcãs",
              "Um arbusto da família da oliveira com cachos de flores roxas e perfumadas no fim da primavera. Nativo só "
              "das encostas dos Bálcãs, foi levado pelos jardins ao mundo todo."),
    "rose_bush": ("Roseira", "Europa, noroeste da África e oeste da Ásia",
                  "A rosa-canina sobe pelas cercas vivas com espinhos em gancho. Os frutos vermelhos têm mais vitamina "
                  "C que a laranja; na Grã-Bretanha em guerra as crianças os colhiam para fazer xarope."),
    "peony": ("Peônia", "Sibéria, Mongólia e norte da China",
              "Grandes flores cor-de-rosa cujos botões soltam açúcar que as formigas vêm beber. Cultivada na China há "
              "mais de mil anos, lá é chamada de rainha das flores."),
    "pitcher_plant": ("Planta-jarro", "Brejos do leste da América do Norte",
                      "Uma planta carnívora de solos pobres e encharcados: as folhas são jarros de água da chuva onde "
                      "insetos se afogam e são digeridos. Neste mundo só nasce de sementes antigas que os farejadores "
                      "desenterram."),
    "grass": ("Capim", "Europa, norte da Ásia e América do Norte",
              "O capim-do-prado, a grama dos pastos e gramados. As gramíneas crescem pela base da folha, não pela "
              "ponta, e por isso aguentam o pasto e o cortador que matariam a maioria das plantas."),
    "fern": ("Samambaia", "Quase o mundo todo, em todos os continentes menos a Antártida",
             "A samambaia-das-taperas é mais antiga que qualquer flor: não tem flores nem sementes e se espalha por "
             "esporos e caules que correm debaixo da terra. As folhas são venenosas para o gado e os cavalos."),
    "dead_bush": ("Arbusto seco", "Desertos do sudoeste dos Estados Unidos e do norte do México",
                  "Não está morto, só esperando: o arbusto-de-creosoto para de crescer na seca e fica verde depois da "
                  "chuva. Um anel de clones seus no Mojave, o King Clone, tem cerca de 11.700 anos."),
    "sweet_berry_bush": ("Arbusto de bagas doces", "Florestas do norte e tundra da Europa, Ásia e América do Norte",
                         "O mirtilo-vermelho, um arbusto baixo e sempre verde da floresta boreal. As bagas vermelhas e "
                         "azedinhas duram o inverno inteiro sem estragar, graças ao ácido benzoico que elas mesmas "
                         "têm."),
    "bamboo": ("Bambu", "Centro e sul da China",
               "Uma gramínea gigante: um broto de bambu-mossô pode crescer um metro num dia e chega à altura final em "
               "dois meses. Uma touceira inteira floresce uma vez a cada muitas décadas, toda junta, e depois morre."),
    "cactus": ("Cacto", "Deserto de Sonora, no Arizona e no México",
               "O saguaro guarda água num tronco sanfonado que incha depois da chuva; um adulto pode ter mais de uma "
               "tonelada. Vive até 150 anos e só solta o primeiro braço por volta dos 70."),
    "sugar_cane": ("Cana-de-açúcar", "Nova Guiné, onde foi cultivada pela primeira vez há uns 8.000 anos",
                   "Uma gramínea gigante de caule cheio de caldo doce; fervido, dá a maior parte do açúcar do mundo. Os "
                   "engenhos das Américas foram erguidos sobre ela e sobre o trabalho forçado de pessoas "
                   "escravizadas."),
    "moss": ("Musgo", "Chão úmido de todo o mundo temperado",
             "O musgo-capilar, um dos musgos mais altos. Musgos não têm raízes, flores nem sementes; bebem pelas "
             "folhas e precisam de uma película de água para os espermatozoides nadarem até os óvulos."),
    "wheat": ("Trigo", "Crescente Fértil, de três capins selvagens cruzados há uns 9.000 anos",
              "O trigo-de-pão carrega os genomas inteiros de três ancestrais selvagens. Dá um quinto das calorias que a "
              "humanidade come, e o seu glúten deixa a massa esticar e segurar o gás do pão que cresce."),
    "carrot": ("Cenoura", "Silvestre na Europa e no sudoeste da Ásia; cultivada pela raiz primeiro no Afeganistão",
               "As primeiras cenouras de comer eram roxas ou amarelas; as laranja foram criadas nos Países Baixos nos "
               "anos 1600. O laranja é betacaroteno, que o corpo transforma em vitamina A."),
    "potato": ("Batata", "Andes do Peru e da Bolívia",
               "Os agricultores andinos plantavam milhares de variedades; a Europa plantou só algumas, e quando a "
               "requeima chegou em 1845 a Irlanda perdeu um milhão de pessoas para a fome. Batata verde tem o veneno "
               "solanina."),
    "beetroot": ("Beterraba", "Costas do Mediterrâneo e do Atlântico, como a acelga-marítima selvagem",
                 "Beterraba, acelga e beterraba-sacarina são uma só espécie criada de três jeitos. A beterraba-sacarina, "
                 "criada na Prússia nos anos 1700, hoje dá um quinto do açúcar do mundo."),
    "melon": ("Melancia", "Nordeste da África",
              "A melancia é cerca de nove décimos água. Suas sementes foram deixadas em túmulos egípcios, e viajantes "
              "do deserto a levavam como cantil."),
    "pumpkin": ("Abóbora", "Argentina e Uruguai",
                "Uma rama rasteira cujas abóboras gigantes já passaram de 1.200 quilos, os frutos mais pesados de "
                "qualquer planta. Os povos das Américas plantavam abóbora junto com milho e feijão."),
    "cocoa": ("Cacau", "Alto Amazonas",
              "Uma arvoreta da sombra da floresta cujos frutos nascem direto do tronco. Maias e astecas bebiam as "
              "sementes torradas e moídas; o nome científico quer dizer alimento dos deuses."),
    "kelp": ("Alga", "Costas frias do Pacífico e dos mares do sul",
             "A alga-gigante não é planta, é uma alga parda. Boias cheias de gás seguram as folhas em pé, e ela pode "
             "crescer meio metro por dia, formando florestas submarinas cheias de peixes e lontras-marinhas."),
    "seagrass": ("Capim-marinho", "Costas rasas do Atlântico Norte e do Pacífico Norte",
                 "Uma planta com flores que voltou para o mar e até floresce debaixo d'água. Os seus prados abrigam "
                 "peixes jovens e enterram carbono no lodo."),
    "lily_pad": ("Folha de nenúfar", "Lagos e lagoas da Europa, Norte da África e Oriente Médio",
                 "O nenúfar-branco se enraíza no lodo e faz as folhas boiarem em hastes compridas, com canais de ar que "
                 "levam oxigênio até as raízes. As folhas respiram pelo lado de cima."),
    "torchflower": ("Flor-tocha", None,
                    "Uma flor antiga, de antes do começo do mundo; os farejadores desenterram as sementes da areia das "
                    "ruínas dos mares quentes. Parece uma chama, mas não dá luz nenhuma."),
    "pink_petals": ("Pétalas rosas", None,
                    "Tapetes de florzinhas cor-de-rosa debaixo das cerejeiras, até quatro por bloco. Nenhuma flor de "
                    "verdade cresce assim: parecem flores de cerejeira caídas que criaram raiz."),
    "spore_blossom": ("Flor de esporos", None,
                      "Uma flor pendurada no teto das cavernas que solta uma chuva lenta de esporos verdes. Flores "
                      "fazem pólen e sementes, não esporos; esta não precisa de sol nenhum."),
    "dripleaf": ("Gotafolha", None,
                 "Folhas largas em hastes altas que tombam um instante depois que alguém pisa nelas, como um "
                 "alçapão. Cresce na argila e na água das cavernas exuberantes."),
    "glow_berries": ("Bagas brilhantes", None,
                     "Cipós pendurados no teto das cavernas com bagas que brilham com luz própria. Vaga-lumes e alguns "
                     "fungos fazem luz, mas nenhum fruto de verdade faz."),
    "glow_lichen": ("Líquen brilhante", None,
                    "Um líquen que brilha nas paredes das cavernas. Líquens de verdade são parcerias de um fungo com "
                    "uma alga ou uma bactéria; nenhum deles brilha."),
    "vine": ("Trepadeiras", None,
             "Ramas folhosas sem flores nem frutos que pendem das árvores da selva e dos carvalhos do pântano e crescem "
             "para baixo. As trepadeiras de verdade crescem para cima, em busca da luz."),
}

BIOMES = {
    "river": "Um rio: água doce correndo para o mar, com juncos nas margens e plantas aquáticas no leito.",
    "ocean": "Mar aberto temperado como o Atlântico Norte: água fresca agitada pelas tempestades, com algas e "
             "capim-marinho perto das costas.",
    "cold_ocean": "Mar frio como o Pacífico Norte: água escura e rica em nutrientes, onde as algas crescem em "
                  "florestas.",
    "lukewarm_ocean": "Mar ameno como o Mediterrâneo: água clara e salgada com prados de capim-marinho no fundo de "
                      "areia.",
    "warm_ocean": "Mar tropical como o Caribe: água quente e clara sobre recifes de coral e campos de capim-marinho.",
}
