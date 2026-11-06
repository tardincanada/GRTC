"""Compêndio de Árvores (3.1.0) em português do Brasil. Ver compendium_i18n.py."""

TITLE = "Compêndio de Árvores"

GUIDE = [
    "# O Compêndio de Árvores",
    "Um livro e uma muda qualquer fazem o Compêndio de Árvores; não precisa de pesquisa. Deixe-o em qualquer espaço da "
    "barra de itens e o canto superior esquerdo da tela mostra o bioma onde você está e, quando você já a conhece, a "
    "árvore para a qual está olhando.",
    "Para anotar uma árvore, olhe por um instante para o tronco, as folhas ou a muda a até 8 blocos, ou derrube-a, com "
    "o livro na barra de itens. Entrar num bioma anota o bioma. Use o livro para ler: biomas à esquerda, as árvores de "
    "cada um à direita, com o que cada árvore é, a altura, onde cresce e o que dá.",
    "Agache e use o Compêndio com um livro na outra mão para fazer Notas do Compêndio; quem as lê soma ao próprio "
    "Compêndio todas as árvores e biomas que estão nelas.",
    "Species Plantarum (nível 3): cada árvore mostra o nome científico, a família e onde cresce nativa no mundo real, "
    "segundo Lineu (1753).",
    "Geografia das Plantas (nível 3): cada bioma mostra o seu clima, e o canto da tela também, segundo Humboldt (1807).",
    "A Lista Vermelha (nível 6): cada árvore mostra quanto está ameaçada na natureza, segundo a UICN (1964).",
]

UI = [
    "Compêndio de Árvores",
    "Árvore desconhecida",
    "cerca de %s °C, %s mm de chuva por ano",
    "Nova árvore no seu Compêndio: %s",
    "Novo bioma no seu Compêndio: %s",
    "Biomas",
    "%s de %s árvores e %s de %s biomas anotados.",
    "Deixe este livro na barra de itens. Olhe por um instante para uma árvore, ou derrube-a, para anotá-la; entre num "
    "bioma para anotar o bioma. Escolha um bioma à esquerda.",
    "Você ainda não esteve aqui.",
    "Árvores",
    "???",
    "Clima: cerca de %s °C por ano, %s mm de chuva.",
    "Altura: até %s m.",
    "Família: %s.",
    "Nativa de: %s.",
    "Lista Vermelha: %s.",
    "Cresce em: %s.",
    "Dá: %s.",
    "Uma árvore só deste mundo: nenhuma espécie real corresponde a ela.",
    "Anotada pela primeira vez em X %s, Z %s.",
    "Pesquise Species Plantarum (nível 3) para conhecer o nome científico, a família e a área nativa.",
    "Voltar",
    "Marcar no Atlas",
    "Segure um livro na outra mão para copiar suas notas",
    "Notas do Compêndio feitas: %s entradas",
    "Somado aos seus compêndios: %s entradas novas",
    "Estas notas estão vazias",
    "Pouco preocupante",
    "Quase ameaçada",
    "Vulnerável",
    "Em perigo",
    "Criticamente em perigo",
    "Dados insuficientes",
    "Não avaliada",
    "Na barra de itens mostra o bioma e dá nome às árvores que você conhece; use-o para ler sobre elas",
    "Notas do Compêndio",
    "Use-as para somar aos seus compêndios todas as árvores, plantas e biomas anotados nelas; são gastas",
    "Species Plantarum",
    "Lineu contou estames com uma lupa e deu a cada planta dois nomes em latim: o Compêndio de Árvores mostra o nome "
    "científico, a família e a área nativa de cada árvore",
    "Geografia das Plantas",
    "Humboldt levou barômetro e termômetro ao Chimborazo e viu as florestas mudarem com a altitude e o calor: o "
    "Compêndio mostra o clima de cada bioma",
    "A Lista Vermelha",
    "A UICN começa a listar as espécies em risco de extinção: o Compêndio mostra quanto cada árvore está ameaçada na "
    "natureza",
    "Carvalho",
    "Bétula",
    "Pinheiro",
    "Árvore da selva",
    "Acácia",
    "Carvalho escuro",
    "Mangue",
    "Cerejeira",
    "Azaleia",
]

TREES = {
    "oak": ("Europa até o Cáucaso",
            "Uma árvore larga e longeva, de folhas lobadas e bolotas. Um só carvalho abriga centenas de espécies de "
            "insetos, e sua madeira construiu os navios e catedrais da Europa por mil anos."),
    "birch": ("Europa e norte da Ásia",
              "Uma pioneira esguia de casca branca que se solta em tiras finas como papel. A casca é impermeável e queima "
              "até úmida; dela se fizeram canoas, caixas e as primeiras cartas escritas de Novgorod."),
    "spruce": ("Norte e centro da Europa",
               "Uma conífera alta e cônica, de agulhas curtas e quadradas e pinhas pendentes. Sua madeira leve e regular "
               "faz os tampos de violinos e pianos; é a árvore de Natal clássica."),
    "jungle": ("",
               "Uma árvore só deste mundo: um tronco altíssimo de floresta tropical, coberto de cipós e frutos de cacau. "
               "As florestas tropicais reais têm centenas de espécies de árvore num único hectare."),
    "acacia": ("África e Península Arábica",
               "A acácia-guarda-chuva: copa achatada de folhinhas sobre um tronco bifurcado, armada de espinhos longos. "
               "As girafas passam pelos espinhos com línguas de meio metro."),
    "dark_oak": ("",
                 "Uma árvore só deste mundo: um carvalho grosso de quatro troncos que sombreia tanto o chão da floresta "
                 "que os cogumelos crescem enormes debaixo dele."),
    "mangrove": ("Costas tropicais do Atlântico e do Pacífico oriental",
                 "O mangue-vermelho se apoia em raízes-escora arqueadas dentro da água salgada. Suas sementes germinam "
                 "ainda na árvore e caem como lanças que flutuam por meses antes de enraizar."),
    "cherry": ("Japão, Coreia e China",
               "A cerejeira-japonesa: cultivada pelas nuvens de flores rosadas, não pelo fruto. Sua floração breve a "
               "cada primavera é celebrada no Japão como hanami."),
    "azalea": ("Montanhas da Ásia, Europa e América do Norte",
               "Azaleias são rododendros arbustivos de flores em funil. O mel de algumas espécies é venenoso; neste "
               "mundo elas marcam as cavernas exuberantes logo abaixo."),
    "mahogany": ("Do México ao sul da Amazônia",
                 "O mogno: um gigante reto de madeira avermelhada e estável. Séculos de corte para móveis finos o "
                 "tornaram raro, e hoje seu comércio é controlado por tratado internacional."),
    "brazil_nut": ("Floresta amazônica",
                   "Uma das árvores mais altas da Amazônia. As castanhas crescem dentro de um ouriço duro do tamanho de "
                   "um coco que só a cutia consegue roer; a cutia enterra as que sobram e planta novas árvores."),
    "brazilwood": ("Mata Atlântica",
                   "Uma árvore espinhosa de cerne vermelho-alaranjado que deu nome ao Brasil. Seu corante vermelho ia "
                   "para a Europa desde 1501; hoje sua madeira faz os melhores arcos de violino."),
    "sapodilla": ("Sul do México e América Central",
                  "Uma árvore sempre-verde de fruto marrom e doce com gosto de pera e malte. Seu látex leitoso, o "
                  "chicle, foi o primeiro chiclete."),
    "andiroba": ("Bacia amazônica e Caribe",
                 "Parente amazônica do mogno, de frutos grandes e lenhosos. O óleo amargo das sementes é usado contra "
                 "picadas de insetos e como óleo de lamparina."),
    "acai": ("Estuário do Amazonas",
             "Uma palmeira esguia que cresce em touceiras nas margens alagadas. Seus frutos roxos são alimento básico "
             "na foz do Amazonas, e o miolo do tronco é o palmito."),
    "babassu": ("Leste da Amazônia e Nordeste do Brasil",
                "Uma palmeira alta de folhas enormes e eretas. Seus cocos duros guardam um óleo parecido com o de coco; "
                "as quebradeiras do Maranhão os abrem à mão com machado e porrete."),
    "copaiba": ("Brasil, Paraguai e Bolívia",
                "Uma árvore cujo tronco guarda um óleo-resina que escorre de um furo feito nele. O bálsamo de copaíba "
                "era vendido nas boticas europeias desde o século XVII."),
    "cinchona": ("Andes do Equador e do Peru",
                 "Uma pequena árvore andina cuja casca amarga contém quinina, a primeira cura da malária. Ela aparece no "
                 "brasão do Peru."),
    "oil_palm": ("África Ocidental e Central",
                 "Uma palmeira cujos cachos de frutos alaranjados dão o óleo de palma, o óleo vegetal mais produzido "
                 "do mundo. Um cacho pode pesar 40 quilos."),
    "teak": ("Índia, Mianmar, Laos e Tailândia",
             "Uma árvore alta e caducifólia de folhas do tamanho de um prato. Sua madeira dourada e oleosa resiste à "
             "água e aos cupins, por isso era a madeira dos conveses de navio."),
    "cinnamon": ("Sri Lanka e sul da Índia",
                 "Um loureiro sempre-verde cuja casca interna, tirada e seca, se enrola em paus de canela. O aroma vem "
                 "do cinamaldeído."),
    "camphor_laurel": ("China, Japão e Coreia",
                       "Uma árvore larga e sempre-verde cujas folhas cheiram a cânfora quando amassadas. A cânfora "
                       "destilada da madeira fez o celuloide, o primeiro filme plástico."),
    "sal": ("Sopé do Himalaia, na Índia e no Nepal",
            "Uma árvore alta, quase sempre-verde, que forma florestas inteiras. Sua madeira dura estrutura casas "
            "indianas e sua resina é queimada como incenso; diz-se que Buda nasceu sob uma delas."),
    "nutmeg": ("Ilhas Banda, Indonésia",
               "Uma árvore sempre-verde cuja semente é a noz-moscada e cujo arilo vermelho é o macis. Houve guerras "
               "pelas minúsculas Ilhas Banda, o único lugar onde ela crescia."),
    "baobab": ("África tropical",
               "Um enorme tronco em forma de garrafa que guarda água para a seca. Baobás podem viver mais de 2.000 "
               "anos; a polpa seca do fruto é rica em vitamina C."),
    "gum_arabic_tree": ("Sahel e Sudão",
                        "Uma pequena árvore espinhosa do Sahel. Sua seiva endurece na goma arábica, usada em "
                        "refrigerantes, selos, aquarelas e tintas de impressão."),
    "marula": ("Sul e leste da África",
               "Uma árvore da savana de frutos amarelos do tamanho de uma ameixa, adorados pelos elefantes. O fruto "
               "vira cerveja e licor, e as sementes dão um óleo estável."),
    "shea": ("Savanas da África Ocidental e Central",
             "Uma árvore lenta que só frutifica depois de 15 anos. Suas castanhas dão a manteiga de karité, gordura "
             "usada em comida, sabão e chocolate."),
    "eucalyptus": ("Tasmânia e sudeste da Austrália",
                   "O eucalipto-azul: um gigante de crescimento rápido, casca que descasca e folhas jovens azuladas. Seu "
                   "óleo, o eucaliptol, desentope o nariz; suas plantações abastecem fábricas de papel no mundo todo."),
    "pequi": ("Cerrado",
              "Uma árvore retorcida do cerrado de fruto amarelo e cheiro forte. Sob a polpa o caroço esconde espinhos "
              "finos, então o pequi se rói, nunca se morde."),
    "barbatimao": ("Cerrado",
                   "Uma pequena árvore do cerrado de casca grossa e suberosa. A casca é tão rica em tanino que servia "
                   "para curtir couro e tratar feridas."),
    "yellow_ipe": ("América do Sul tropical",
                   "Na seca ela perde todas as folhas e floresce em amarelo vivo. Sua madeira, o ipê, está entre as "
                   "mais duras e densas do mundo."),
    "jatoba": ("Américas Central e do Sul",
               "Uma árvore grande de folíolos aos pares e vagens cheias de polpa doce e farinhenta. Sua resina, o "
               "copal, virou âmbar ao longo de milhões de anos."),
    "sandalwood": ("Índia, Indonésia e norte da Austrália",
                   "Uma semiparasita cujas raízes tiram água das raízes de outras plantas. O cerne perfumado e o óleo "
                   "valem tanto que as árvores selvagens foram quase todas cortadas."),
    "dragon_tree": ("Ilha de Socotorá, Iêmen",
                    "Um guarda-chuva de folhas duras sobre galhos grossos, só encontrado em Socotorá. Sua resina "
                    "vermelha, o sangue-de-dragão, era pigmento e verniz no mundo antigo."),
    "frankincense": ("Omã, Iêmen e Somália",
                     "Uma pequena árvore que cresce sobre a rocha nua. Cortes na casca fina vertem uma resina leitosa "
                     "que seca no olíbano, comerciado pela Arábia há 5.000 anos."),
    "myrrh": ("Chifre da África e sul da Arábia",
              "Uma arvoreta espinhosa de chão seco e pedregoso. Sua resina avermelhada, a mirra, servia de perfume, "
              "remédio e para embalsamar no Egito antigo."),
    "date_palm": ("Oriente Médio e Norte da África",
                  "A palmeira cultivada do deserto, plantada em oásis há pelo menos 7.000 anos. As árvores fêmeas dão "
                  "cachos de até mil tâmaras cada um."),
    "mesquite": ("México, América Central e Caribe",
                 "Uma árvore espinhosa de raízes que alcançam água a 50 metros de profundidade. Suas vagens doces "
                 "alimentam o gado e eram moídas em farinha pelos povos do deserto."),
    "joshua_tree": ("Deserto de Mojave",
                    "Uma iúca gigante de braços espinhosos e ramificados, não uma árvore de verdade. Só uma mariposa "
                    "poliniza suas flores, e as larvas dessa mariposa só comem suas sementes."),
    "saxaul": ("Desertos da Ásia Central",
               "Uma árvore do deserto sem folhas, com raminhos verdes no lugar delas. Sua madeira é tão densa que "
               "afunda na água e queima como carvão."),
    "argan": ("Sudoeste do Marrocos",
              "Uma árvore espinhosa e resistente à seca, em que as cabras sobem para comer o fruto. As amêndoas dão "
              "o óleo de argan, usado na cozinha e em cosméticos."),
    "palo_verde": ("Deserto de Sonora",
                   "O nome quer dizer pau verde: a casca é verde e faz quase toda a fotossíntese quando as folhinhas "
                   "caem na seca. Na primavera fica amarela de flores."),
    "pinyon_pine": ("Planalto do Colorado e sudoeste dos EUA",
                    "Um pinheiro pequeno e lento de colinas secas. Seus pinhões grandes eram um alimento principal dos "
                    "povos da Grande Bacia e ainda são colhidos todo outono."),
    "utah_juniper": ("Grande Bacia e Planalto do Colorado",
                     "Um zimbro retorcido de encostas secas que pode viver 650 anos. Suas 'bagas' azuis são na verdade "
                     "pinhas carnosas, comidas pelos pássaros."),
    "desert_ironwood": ("Deserto de Sonora",
                        "Uma das madeiras mais pesadas do mundo: afunda na água. Muitas plantas do deserto começam a "
                        "vida à sua sombra, por isso é chamada de árvore-ama."),
    "tamarisk": ("Do Norte da África à Índia",
                 "Um cedro-do-sal de folhinhas em escama que transpiram sal. Cresce onde a água do solo é salgada "
                 "demais para outras árvores."),
    "ponderosa_pine": ("Oeste da América do Norte",
                       "Um pinheiro alto de casca alaranjada em placas que cheira a baunilha nos dias quentes. A casca "
                       "grossa o deixa sobreviver aos incêndios rasteiros que varrem suas florestas."),
    "gambel_oak": ("Sudoeste dos EUA e Montanhas Rochosas",
                   "Um carvalho arbustivo que se espalha pelas raízes em moitas. Suas bolotas alimentavam cervos, perus "
                   "e os povos da região."),
    "mountain_mahogany": ("Montanhas do oeste da América do Norte",
                          "Não é mogno, e sim parente da rosa. Suas sementes têm uma cauda plumosa que se torce com a "
                          "umidade e perfura o solo para enterrar a semente."),
    "elephant_tree": ("Deserto de Sonora e Baixa Califórnia",
                      "Uma árvore atarracada de tronco inchado que guarda água e casca fina que descasca. Seus raminhos "
                      "cheiram a terebintina e dão uma resina copal."),
    "coconut_palm": ("Litorais tropicais dos oceanos Índico e Pacífico",
                     "A palmeira das praias tropicais. O coco flutua e continua vivo por meses no mar, e foi assim que "
                     "se espalhou pelos oceanos."),
    "tropical_almond": ("Ásia tropical, Austrália e Pacífico",
                        "Uma árvore de praia de galhos em andares e folhas grandes que ficam vermelhas antes de cair. "
                        "As sementes têm gosto de amêndoa, e as folhas caídas tingem a água de marrom com tanino."),
    "casuarina": ("Sudeste Asiático, Austrália e Pacífico",
                  "Parece um pinheiro, mas é uma árvore com flores e raminhos verdes articulados. Suas raízes fixam "
                  "nitrogênio, então cresce na areia nua."),
    "screw_pine": ("Litorais do Pacífico e do Índico",
                   "As folhas giram em volta do tronco como um parafuso, e raízes-escora a mantêm de pé na areia. Suas "
                   "folhas são trançadas em esteiras e telhados."),
    "sea_grape": ("Litorais da América tropical",
                  "Uma árvore de praia tolerante ao sal, de folhas redondas e coriáceas. Os frutos roxos pendem em "
                  "cachos como uvas e dão uma geleia azedinha."),
    "black_mangrove": ("Costas tropicais do Atlântico e do Pacífico nas Américas",
                       "Um mangue cujas raízes mandam brotos respiratórios como lápis pela lama. Ele expele o sal pelas "
                       "folhas, que brilham com cristais."),
    "white_mangrove": ("Costas atlânticas tropicais das Américas e da África Ocidental",
                       "Cresce do lado de terra do manguezal. Duas pequenas glândulas na base de cada folha soltam "
                       "açúcar e sal."),
    "buttonwood": ("Costas tropicais das Américas e da África Ocidental",
                   "Um mangue da parte alta da praia, de frutos em forma de botão. Sua madeira densa era queimada como "
                   "carvão no Caribe."),
    "nipa_palm": ("Manguezais da Ásia e do Pacífico ocidental",
                  "Uma palmeira sem caule aparente: o tronco corre por baixo da lama e só as folhas ficam de fora. Sua "
                  "seiva doce é extraída para açúcar, vinagre e álcool."),
    "buriti": ("Amazônia e veredas do Cerrado",
               "Uma palmeira alta em leque de brejos e beiras de rio. O fruto vermelho e escamoso é riquíssimo em "
               "vitamina A; a fibra das folhas vira redes."),
    "eastern_cottonwood": ("Leste e centro da América do Norte",
                           "Um choupo de crescimento rápido das várzeas, de folhas triangulares. No início do verão "
                           "suas sementes voam em tufos de algodão branco."),
    "american_elm": ("Leste da América do Norte",
                     "Uma árvore em forma de vaso que já ladeou as ruas das cidades americanas. A doença holandesa do "
                     "olmo, um fungo levado por besouros, matou a maioria delas depois de 1930."),
    "ash": ("Europa até o Cáucaso",
            "Uma árvore alta de folhas compostas e gemas pretas no inverno. Sua madeira dura e elástica fez cabos de "
            "ferramentas, remos e arcos; um fungo, a morte-do-freixo, agora a ameaça."),
    "walnut": ("Da Ásia Central aos Bálcãs",
               "A nogueira comum: uma árvore larga de folhas aromáticas e nozes comestíveis. Suas raízes liberam "
               "juglona, que impede muitas outras plantas de crescer por perto."),
    "white_mulberry": ("Norte da China",
                       "Uma árvore pequena cujas folhas são o único alimento do bicho-da-seda. Os estames de suas "
                       "flores se abrem mais rápido que qualquer outro movimento vegetal conhecido, lançando o pólen."),
    "linden": ("Europa e oeste da Sibéria",
               "A tília-de-folhas-pequenas: folhas em coração e flores perfumadas que as abelhas adoram. Sua madeira "
               "branca e macia é a preferida dos entalhadores, e a entrecasca dava cordas."),
    "sweet_chestnut": ("Sul da Europa e Ásia Menor",
                       "Uma árvore larga de folhas longas e dentadas e ouriços espinhosos com castanhas. Em muitas "
                       "aldeias de montanha a castanha era o pão dos pobres."),
    "pear": ("Europa e oeste da Ásia",
             "A pereira: flores brancas na primavera e frutos suculentos, cultivada desde a Antiguidade. A madeira de "
             "pereira é dura e fina o bastante para gravar matrizes de impressão e réguas."),
    "elder": ("Europa, Norte da África e oeste da Ásia",
              "Uma arvoreta de ramalhetes achatados de flores creme perfumadas e bagas roxo-escuras. As bagas precisam "
              "ser cozidas: cruas, fazem mal ao estômago."),
    "beech": ("Oeste e centro da Europa",
              "Uma árvore alta de casca cinza e lisa e sombra tão densa que pouco cresce embaixo. Seus frutinhos, a "
              "faia, engordavam os porcos no outono."),
    "sugar_maple": ("Leste do Canadá e nordeste dos EUA",
                    "No fim do inverno sua seiva sobe com cerca de 3% de açúcar; quarenta litros fervidos dão um litro "
                    "de xarope. Sua folha está na bandeira do Canadá."),
    "hornbeam": ("Da Europa ao Irã",
                 "Uma árvore de tronco cinza e canelado e a madeira mais dura da Europa. Fez dentes de engrenagem de "
                 "moinhos, tábuas de açougue e martelos de piano."),
    "southern_magnolia": ("Sudeste dos EUA",
                          "Uma sempre-verde de folhas brilhantes e enormes flores brancas com cheiro de limão. As "
                          "magnólias são uma linhagem antiga, polinizada por besouros antes de existirem abelhas."),
    "tulip_tree": ("Leste da América do Norte",
                   "Uma das folhosas mais altas do leste, de folhas com o contorno de uma tulipa e flores verdes e "
                   "laranja parecidas com tulipas."),
    "paulownia": ("Centro e oeste da China",
                  "Uma das árvores de crescimento mais rápido, de folhas enormes e flores violeta. No Japão sua madeira "
                  "leve fazia as arcas dadas às filhas no casamento."),
    "judas_tree": ("Sul da Europa e oeste da Ásia",
                   "Na primavera flores rosa-arroxeadas brotam direto do tronco e dos galhos, antes das folhas. As "
                   "flores são comestíveis, de gosto agridoce."),
    "quaking_aspen": ("Canadá e oeste dos EUA",
                      "Suas folhas tremem com a menor brisa. Um bosque de álamos em Utah, Pando, é uma só planta com "
                      "47.000 troncos ligados a uma mesma raiz."),
    "alder": ("Europa, Norte da África e oeste da Ásia",
              "Uma árvore de beira de rio cujas raízes fixam nitrogênio com bactérias. Sua madeira dura debaixo "
              "d'água: Veneza está assentada sobre estacas de amieiro."),
    "rowan": ("Europa e norte da Ásia",
              "Uma árvore pequena de folhas compostas e cachos de bagas escarlates que os pássaros limpam no outono. "
              "O ácido sórbico, conservante comum de alimentos, foi descoberto nelas."),
    "black_cherry": ("Leste da América do Norte e México",
                     "Uma cerejeira de floresta de casca escura que se solta em placas. Sua madeira castanho-"
                     "avermelhada é valorizada em móveis; folhas e sementes envenenam o gado."),
    "yew": ("Europa, Norte da África e oeste da Ásia",
            "Uma sempre-verde escura que pode viver mais de 2.000 anos. Quase tudo nela é venenoso; sua madeira "
            "elástica fez o arco longo inglês, e uma parente deu o taxol, remédio contra o câncer."),
    "black_walnut": ("Leste dos EUA",
                     "Uma nogueira alta de madeira escura e valiosa e nozes de casca grossa. A casca verde do fruto "
                     "mancha mãos e panos de marrom-escuro."),
    "holly": ("Oeste e sul da Europa",
              "Uma sempre-verde de folhas brilhantes e espinhosas; só as árvores fêmeas dão bagas vermelhas. As folhas "
              "de cima, fora do alcance dos cervos, muitas vezes perdem os espinhos."),
    "bald_cypress": ("Sudeste dos EUA",
                     "Uma conífera de pântano que perde as agulhas no inverno. 'Joelhos' nodosos sobem das raízes acima "
                     "da água; sua madeira resiste à podridão por séculos."),
    "water_tupelo": ("Sudeste dos EUA",
                     "Uma árvore de pântano de base inchada que passa meses dentro d'água. As abelhas fazem com suas "
                     "flores um mel claro que nunca cristaliza."),
    "white_willow": ("Europa e oeste da Ásia",
                     "Um salgueiro de beira de rio de folhas prateadas. Sua casca contém salicina, o modelo natural da "
                     "aspirina; um híbrido dele dá a madeira dos tacos de críquete."),
    "red_maple": ("Leste da América do Norte",
                  "A árvore mais comum do leste da América do Norte, vermelha na flor, no pecíolo e na folha de outono. "
                  "Sua seiva também é extraída, mas dá menos açúcar que a do bordo-açucareiro."),
    "ginkgo": ("Sudoeste da China",
               "Um fóssil vivo, o último de um grupo que cresceu com os dinossauros. Suas folhas em leque ficam douradas "
               "todas de uma vez no outono; seis ginkgos sobreviveram à bomba de Hiroshima."),
    "japanese_maple": ("Japão, Coreia e China",
                       "Um bordo pequeno de folhas muito recortadas que ficam vermelho-fogo no outono. Os jardineiros "
                       "japoneses criaram centenas de variedades em três séculos."),
    "sugi": ("Japão",
             "A árvore nacional do Japão, uma conífera altíssima plantada em volta dos santuários. Alguns sugis da ilha "
             "de Yakushima teriam mais de 2.000 anos."),
    "lacquer_tree": ("China e Himalaia",
                     "Sua seiva, o urushi, endurece no ar úmido na laca brilhante das tigelas do Leste Asiático. A seiva "
                     "crua queima a pele como a hera venenosa, sua parente."),
    "cork_oak": ("Mediterrâneo ocidental",
                 "Um carvalho sempre-verde de casca grossa e esponjosa, tirada a cada nove anos sem matar a árvore. "
                 "Portugal produz metade da cortiça do mundo."),
    "olive": ("Bacia do Mediterrâneo",
              "Uma sempre-verde retorcida de folhas cinza-prateadas, cultivada pelo azeite há 6.000 anos. Oliveiras "
              "podem viver e dar frutos por mais de mil anos."),
    "cedar_of_lebanon": ("Montanhas do Líbano, da Síria e da Turquia",
                         "Uma conífera majestosa de galhos chatos em camadas, na bandeira do Líbano. Sua madeira "
                         "perfumada e durável construiu templos e navios do mundo antigo."),
    "black_pine": ("Sul da Europa e Turquia",
                   "Um pinheiro de agulhas longas, duras e escuras que aguenta seca, vento e solo pobre. É resinado e "
                   "plantado como quebra-vento."),
    "carob": ("Mediterrâneo oriental",
              "Uma sempre-verde de vagens marrons e compridas com polpa doce. Suas sementes têm peso tão regular que "
              "viraram o quilate, unidade das gemas e do ouro."),
    "stone_pine": ("Bacia do Mediterrâneo",
                   "O pinheiro-guarda-chuva de Roma, de copa achatada e larga. Suas sementes grandes são os pinoles do "
                   "pesto e dos doces mediterrâneos."),
    "aleppo_pine": ("Bacia do Mediterrâneo",
                    "Um pinheiro resistente à seca das costas quentes. O vinho grego retsina ganhou seu sabor da resina "
                    "desse pinheiro, usada para vedar ânforas."),
    "monterey_cypress": ("Dois bosques no litoral central da Califórnia",
                         "Selvagem só em dois bosques costeiros, mas plantado no mundo todo como quebra-vento. Os ventos "
                         "do oceano o esculpem em formas de topo achatado."),
    "apple": ("Montanhas Tian Shan, na Ásia Central",
              "A macieira de pomar, descendente das maçãs selvagens do Cazaquistão. As sementes nunca repetem a mãe: "
              "toda variedade com nome é enxertada de uma única árvore original."),
    "balsam_fir": ("Canadá e nordeste dos EUA",
                   "Um abeto perfumado com bolhas de resina transparente na casca. O bálsamo do Canadá, essa resina, "
                   "era a cola de lâminas de microscópio e lentes por ter a mesma refração do vidro."),
    "silver_fir": ("Montanhas do centro e do sul da Europa",
                   "Um abeto alto de casca branco-prateada e agulhas com duas faixas brancas por baixo. Foi a primeira "
                   "árvore de Natal da Alsácia."),
    "douglas_fir": ("Oeste da América do Norte",
                    "Uma das árvores mais altas da Terra, de pinhas com brácteas que parecem rabos de rato. Sua madeira "
                    "forte construiu pontes, mastros de navio e casas."),
    "siberian_fir": ("Sibéria e Urais",
                     "Um abeto estreito da taiga, muito resistente ao frio. O óleo destilado de suas agulhas é usado em "
                     "remédios e sabonetes perfumados."),
    "green_alder": ("Montanhas e tundras do hemisfério norte",
                    "Um amieiro arbustivo de corredores de avalanche e tundra. Verga sob a neve sem quebrar e fixa "
                    "nitrogênio em solos pobres."),
    "sycamore_maple": ("Montanhas do centro e do sul da Europa",
                       "Um bordo grande de sementes aladas que giram como hélices. Sua madeira clara faz o fundo dos "
                       "violinos e mesas de cozinha."),
    "atlas_cedar": ("Montes Atlas, no Marrocos e na Argélia",
                    "Um cedro de montanha de agulhas verde-azuladas em tufos. Os macacos-de-gibraltar vivem em suas "
                    "florestas, que encolhem com a seca e o pastoreio."),
    "siberian_pine": ("Sibéria e Mongólia",
                      "O 'cedro' siberiano, um pinheiro de cinco agulhas com pinhões grandes e sem asa. Os quebra-nozes "
                      "espalham as sementes enterrando-as para o inverno."),
    "western_red_cedar": ("Noroeste da América do Norte, no Pacífico",
                          "Uma conífera enorme de costas chuvosas, de casca fibrosa. Sua madeira à prova de podridão "
                          "deu aos povos da costa canoas, casas e totens."),
    "sea_buckthorn": ("Europa e Ásia",
                      "Um arbusto espinhoso de dunas e cascalho, de folhas prateadas. Suas bagas laranja grudam nos "
                      "galhos e são ricas em vitamina C e óleo."),
    "european_larch": ("Alpes e Cárpatos",
                       "Uma conífera que fica dourada e perde as agulhas todo outono. Sua resina, a terebintina de "
                       "Veneza, era usada pelos pintores, e sua madeira resiste à podridão."),
    "dahurian_larch": ("Leste da Sibéria e nordeste da Ásia",
                       "A árvore mais ao norte da Terra, crescendo sobre o permafrost a 72° norte. Aguenta invernos "
                       "abaixo de 60 graus negativos."),
    "siberian_dwarf_pine": ("Nordeste da Sibéria e Japão",
                            "Um pinheiro rasteiro que se deita sob a neve no inverno e se levanta de novo na "
                            "primavera. Seus pinhões alimentam ursos e zibelinas."),
    "eastern_white_pine": ("Leste do Canadá e nordeste dos EUA",
                           "A árvore mais alta do leste da América do Norte, de agulhas macias em grupos de cinco. A "
                           "coroa britânica reservava as maiores para os mastros de sua marinha."),
    "bristlecone_pine": ("Montanhas Brancas e Grande Bacia, EUA",
                         "As árvores vivas mais velhas: uma delas, Matusalém, tem mais de 4.800 anos. Seus anéis "
                         "serviram para calibrar a datação por radiocarbono."),
    "swiss_pine": ("Alpes e Cárpatos",
                   "Um pinheiro lento de cinco agulhas perto do limite das árvores, de madeira perfumada usada em "
                   "quartos alpinos. Suas sementes são espalhadas pelo quebra-nozes-malhado."),
    "whitebark_pine": ("Montanhas do oeste da América do Norte",
                       "Um pinheiro de alta montanha cujas pinhas nunca se abrem sozinhas; o quebra-nozes-de-clark as "
                       "quebra. A ferrugem e os besouros estão matando a espécie."),
    "lodgepole_pine": ("Oeste da América do Norte",
                       "Reto e fino, usado pelos povos das planícies como mastro de tipi. Muitas de suas pinhas só se "
                       "abrem no calor de um incêndio florestal."),
    "mountain_pine": ("Montanhas do centro e do sul da Europa",
                      "Um pinheiro baixo de muitos troncos no limite alpino das árvores, que segura neve e solo em "
                      "encostas íngremes. Seu óleo é usado em remédios para tosse."),
    "scots_pine": ("Europa e norte da Ásia",
                   "O pinheiro mais difundido do mundo, de casca alaranjada no alto. Sua resina deu terebintina e "
                   "alcatrão para os cabos e cascos dos veleiros."),
    "arctic_willow": ("Tundra ártica",
                      "Uma árvore de um palmo de altura que rasteja pela tundra. Seus caules podem ter séculos; suas "
                      "folhas alimentam bois-almiscarados e lebres."),
    "coast_redwood": ("Litoral do norte da Califórnia e sul do Oregon",
                      "As árvores mais altas da Terra: Hyperion tem 116 metros. A neblina do Pacífico lhes dá água no "
                      "verão seco."),
    "giant_sequoia": ("Encostas ocidentais da Serra Nevada",
                      "As maiores árvores em volume: a General Sherman pesa cerca de 2.000 toneladas. Sua casca tem "
                      "meio metro de espessura e resiste ao fogo."),
    "western_hemlock": ("Noroeste da América do Norte, no Pacífico",
                        "Uma conífera de sombra de topo pendente que cresce debaixo de outras árvores. Sua casca é rica "
                        "em tanino e sua polpa faz papel."),
    "common_juniper": ("Todo o hemisfério norte",
                       "A conífera mais difundida da Terra, um arbusto de charnecas e tundras. Suas pinhas azuis, as "
                       "bagas de zimbro, dão sabor ao gim."),
    "araucaria": ("Sul do Brasil e nordeste da Argentina",
                  "Uma conífera em forma de candelabro cujos galhos terminam em tufos no alto. Suas sementes grandes, o "
                  "pinhão, são comida de outono no Sul; o corte deixou só uma pequena parte de suas matas."),
    "brazilian_rosewood": ("Mata Atlântica",
                           "Jacarandá-da-bahia: cerne escuro, perfumado e rajado, já foi o melhor para violões e móveis. "
                           "Desde 1992 seu comércio internacional é proibido."),
    "mango": ("Índia, Bangladesh e Mianmar",
              "Uma grande sempre-verde cultivada há 4.000 anos pelo fruto doce. A seiva e a casca da manga têm o mesmo "
              "irritante da hera venenosa, sua parente."),
    "avocado": ("México e América Central",
                "Um loureiro sempre-verde de fruto rico em óleo, não em açúcar. Seu caroço grande talvez fosse "
                "espalhado por preguiças-gigantes hoje extintas."),
    "cashew": ("Nordeste do Brasil",
               "A castanha pende abaixo de um 'pseudofruto' carnoso que se come ou vira suco. A casca guarda um óleo "
               "cáustico, por isso as castanhas são torradas antes de abertas."),
    "orange": ("Sul da China e Sudeste Asiático (cultivada)",
               "A laranja doce, um antigo cruzamento de pomelo e tangerina. O Brasil produz mais laranjas que qualquer "
               "outro país."),
    "lemon": ("Nordeste da Índia e Mianmar (cultivado)",
              "Uma sempre-verde espinhosa de fruto azedo rico em vitamina C e ácido cítrico. Uma ração de suco de limão "
              "protegia os marinheiros do escorbuto."),
    "papaya": ("Sul do México e América Central",
               "Uma árvore rápida de caule mole que frutifica já no primeiro ano. Seu látex contém papaína, enzima que "
               "amacia a carne."),
    "kapok": ("Américas tropicais e África Ocidental",
              "Um gigante de raízes tabulares que se ergue acima da copa da floresta. Suas vagens dão uma fibra leve e "
              "impermeável que já encheu coletes salva-vidas; os maias a tinham como sagrada."),
    "sweet_cherry": ("Europa e oeste da Ásia",
                     "A cerejeira-brava, mãe das cerejas doces de pomar. Sua madeira avermelhada é apreciada pelos "
                     "marceneiros, e os pássaros espalham seus caroços."),
    "plum": ("Europa e oeste da Ásia (cultivada)",
             "A ameixeira europeia, um antigo híbrido cultivado em pomares. Ameixas secas são passas; o fruto azul tem "
             "uma camada de cera em pó."),
    "peach": ("Noroeste da China (cultivado)",
              "Uma árvore pequena de flores rosadas na primavera e fruto aveludado. Na China o pêssego simboliza vida "
              "longa."),
    "apricot": ("Ásia Central e China",
                "Uma frutífera de floração precoce das montanhas secas. Suas amêndoas dão um óleo e, cruas, um veneno "
                "amargo como o das amêndoas amargas."),
    "fig": ("Sudoeste da Ásia e Mediterrâneo",
            "O 'fruto' é uma inflorescência virada para dentro, polinizada por uma vespa minúscula que entra nela. O "
            "figo esteve entre as primeiras plantas cultivadas pelo homem."),
    "pomegranate": ("Do Irã ao norte da Índia",
                    "Uma arvoreta cujo fruto coriáceo guarda centenas de sementes vermelhas e suculentas. A casca é rica "
                    "em tanino e tingia tapetes orientais."),
    "almond": ("Sudoeste da Ásia",
               "Uma parente do pessegueiro cultivada pela semente. As amêndoas selvagens são amargas e venenosas; a "
               "perda de um único gene fez a amêndoa doce que comemos."),
    "pistachio": ("Ásia Central e Irã",
                  "Uma árvore do deserto cujas nozes maduras abrem a casca sozinhas. Há árvores machos e fêmeas, então "
                  "os pomares plantam um macho para cada oito fêmeas."),
    "hazel": ("Europa e oeste da Ásia",
              "Uma arvoreta de muitos troncos com amentilhos no fim do inverno. Seus brotos retos eram trançados em "
              "cercas, e as varinhas de rabdomancia eram cortadas dela."),
    "pecan": ("Vale do Mississippi e México",
              "Uma nogueira alta de várzeas, de nozes doces e casca fina. Era alimento dos povos nativos muito antes de "
              "virar cultivo."),
    "plane_tree": ("Do sudeste da Europa ao Irã",
                   "Uma árvore de sombra larga cuja casca se solta em placas. Conta-se que Hipócrates ensinava sob um "
                   "plátano na ilha de Cós."),
    "horse_chestnut": ("Montanhas dos Bálcãs",
                       "Uma árvore larga de cachos de flores brancas e sementes marrons brilhantes. Apesar do nome, as "
                       "sementes são venenosas para as pessoas."),
}

BIOMES = {
    "jungle": "Como as terras baixas da Amazônia: quente e úmida o ano todo, com árvores sempre-verdes em vários andares "
              "e o chão da mata na penumbra.",
    "sparse_jungle": "A borda da floresta tropical, cortada por clareiras e rios, onde palmeiras e árvores que gostam "
                     "de luz encontram espaço.",
    "bamboo_jungle": "Como as florestas de monção do sul e sudeste da Ásia: uma estação chuvosa, uma seca e touceiras "
                     "de bambu sob as árvores.",
    "savanna": "Campo com árvores esparsas, como a África Oriental: chuvas numa estação, fogo e seca na outra.",
    "savanna_plateau": "Savana alta como o cerrado brasileiro: árvores retorcidas de casca grossa e suberosa em solos "
                       "velhos e pobres.",
    "windswept_savanna": "Savana seca, ventosa e pedregosa como o Chifre da África e Socotorá, terra das árvores do "
                         "incenso.",
    "desert": "Deserto quente como o Saara e a Arábia: as árvores só vivem onde as raízes alcançam água, como nos "
              "oásis.",
    "badlands": "Terra de rocha vermelha erodida como o Planalto do Colorado, com mato de pinheiros-piñon e zimbros.",
    "wooded_badlands": "As mesas mais altas e frescas do sudoeste dos EUA, onde cresce um bosque de pinheiros e "
                       "carvalhos.",
    "eroded_badlands": "Morros nus e cheios de ravinas como a borda do Deserto de Sonora, onde só as árvores mais duras "
                       "sobrevivem.",
    "beach": "Um litoral tropical de areia e maresia, como as ilhas do Pacífico e o Caribe.",
    "mangrove_swamp": "Uma floresta de maré de árvores tolerantes ao sal em costas tropicais lamacentas, meio terra, "
                      "meio mar.",
    "plains": "Campos abertos da zona temperada, como as pradarias, com árvores ao longo dos rios.",
    "sunflower_plains": "Campos quentes e terra de lavoura com cercas vivas, pomares e árvores esparsas.",
    "forest": "Floresta temperada de folhosas como a do centro da Europa, verde no verão e nua no inverno.",
    "flower_forest": "Uma borda de mata ensolarada e florida, rica em árvores ornamentais e frutíferas.",
    "birch_forest": "Floresta fresca de bétulas e outras árvores que gostam de luz, como o sul da Escandinávia.",
    "old_growth_birch_forest": "Uma floresta antiga e alta de folhosas do norte, como as da Nova Inglaterra.",
    "dark_forest": "Uma floresta antiga e fechada onde a copa barra quase toda a luz, como as últimas matas primitivas "
                   "da Europa.",
    "swamp": "Um alagado arborizado e quente como os bayous do sul dos EUA.",
    "cherry_grove": "Mata de montanha do Leste Asiático, úmida e amena, como as colinas do Japão.",
    "windswept_hills": "Colinas mediterrâneas: verões quentes e secos, invernos amenos e chuvosos e árvores "
                       "sempre-verdes.",
    "stony_shore": "Uma costa mediterrânea rochosa de pinheiros e oliveiras, seca no verão.",
    "taiga": "A floresta boreal que circunda o norte do mundo: invernos longos e coníferas.",
    "old_growth_pine_taiga": "Pinhal antigo de musgo fundo e troncos gigantes, como os pinhais das Montanhas Rochosas "
                             "e da Escandinávia.",
    "windswept_forest": "Floresta de montanha castigada pelo vento, de coníferas em encostas rochosas.",
    "snowy_taiga": "A taiga mais fria, como o leste da Sibéria, sobre chão congelado quase o ano todo.",
    "snowy_plains": "Tundra: uma planície fria além do limite das árvores, onde só sobrevivem árvores anãs.",
    "snowy_beach": "Uma costa fria do norte, de cascalho e dunas.",
    "meadow": "Um prado alpino logo abaixo do limite das árvores, cheio de flores no verão.",
    "grove": "Uma floresta de montanha nevada, de abetos e lariços perto do limite das árvores.",
    "stony_peaks": "Cumes rochosos e nus; só árvores atrofiadas crescem nas fendas abrigadas.",
    "windswept_gravelly_hills": "Colinas frias e ventosas de cascalho solto, com pinheiros baixos e arbustos.",
    "old_growth_spruce_taiga": "Floresta temperada úmida como o noroeste do Pacífico: as coníferas mais altas da "
                               "Terra, alimentadas por neblina e chuva.",
    "ice_spikes": "Deserto polar de neve e gelo, onde as árvores rastejam pelo chão.",
    "snowy_slopes": "Campos de neve em encostas íngremes, com pinheiros anões na borda.",
    "frozen_peaks": "Picos altos e congelados; as últimas árvores aqui têm séculos e chegam ao joelho.",
    "jagged_peaks": "Cristas altas e afiadas acima do limite das árvores, com alguns pinheiros antiquíssimos.",
    "lush_caves": "Não é uma floresta, e sim a caverna debaixo de uma: onde arbustos de azaleia crescem na superfície, "
                  "há uma caverna exuberante logo abaixo.",
}
