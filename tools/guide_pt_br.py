"""Portuguese (pt_br) text of the Field Guide (2.4.0). Each list has exactly as many paragraphs as its English twin in
guidebook.py (levels are taken from the English); generated chapters use the templates in T and the pt_br lang file.
"""

INTRO = [
    "# Bem-vindo",
    "Todo bloco e item deste mundo é feito de elementos reais. Este guia segue a árvore tecnológica do mod da Idade "
    "da Pedra até os últimos elementos, um capítulo por nível.",
    "# Unidades",
    "1 unidade é 1 grama. Um bloco tem 1000 unidades, um lingote ou um item de elemento ou substância 111,1, uma pepita "
    "12,35 e um balde 1000. Passe o mouse sobre qualquer bloco ou item para ver do que ele é feito; segure Shift para "
    "ver todas as partes.",
    "# Pesquisa",
    "Cada nível tem uma estação de pesquisa. Coloque as amostras de um projeto nos 9 espaços dela, escolha o projeto e "
    "aperte Pesquisar. Os projetos chave (marcados com uma estrela) sobem o seu nível tecnológico; você precisa dos "
    "projetos chave de um nível antes de poder colocar a próxima estação. /research mostra o que você sabe.",
    "Tratados escritos numa estação ensinam tudo o que você sabe a outro jogador.",
    "# Máquinas",
    "Uma máquina decompõe cada item de entrada na sua composição e roda sobre ele todas as reações que você pesquisou, "
    "enquanto estiver quente o bastante e não quente demais. Os produtos saem como itens, os resíduos como escória, e "
    "alguns gases são liberados. Líquidos e gases enchem Frascos de Vidro vazios pelo painel lateral da máquina. "
    "Algumas reações precisam de um segundo ingrediente no espaço de reagente.",
    "Máquinas aquecidas a fogo queimam o combustível do espaço de combustível: a madeira chega a 650 °C, o carvão "
    "vegetal e o carvão a 1100 °C, os combustíveis de petróleo a 1200 °C e o gás a 1300 °C. Foles (ar forçado) somam "
    "250 °C. O carvão vegetal e o carvão também servem de carbono para reduzir minérios. Máquinas elétricas recebem "
    "corrente de uma Pilha Voltaica, um Dínamo ou outro bloco de energia ao lado delas ou ligado por Fio de Cobre.",
    "As fornalhas comuns não fundem mais minérios de ferro, cobre ou ouro: os metais vêm das máquinas.",
    "# Nível 0",
    "Sem pesquisa nenhuma você pode construir uma Carvoaria (8 terras em volta de uma tora) e queimar toras em carvão "
    "vegetal.",
]

CONTAINERS = [
    "# Frascos de Vidro",
    "Todo item de líquido ou gás é um Frasco de Vidro tampado com 111,1 unidades. Cinco vidros fazem quatro frascos, "
    "sem pesquisa. Uma máquina enche um frasco vazio pelo espaço de cima do seu painel lateral para cada líquido ou gás "
    "que produz; sem frascos ali, ela para e os pede. Quando uma máquina, uma receita, uma fornalha ou um projeto de "
    "pesquisa usa o conteúdo de um frasco, você recebe o frasco vazio de volta.",
    "# Cilindros de Aço e o Compressor",
    "Compressão de Gases (nível 4) desbloqueia os Cilindros de Aço e o Compressor. Um cilindro guarda 1000 unidades de "
    "um gás comprimido: o equivalente a nove frascos num só item. Instale um Compressor num dos dois espaços de "
    "melhoria de uma máquina e coloque cilindros vazios no segundo espaço do painel lateral: a máquina passa a encher "
    "cilindros com todo gás que produz, um cilindro de cada vez. Os líquidos continuam indo para frascos. Um cilindro "
    "de gás combustível queima como nove frascos.",
    "As máquinas aceitam cilindros como entrada como qualquer outro item e devolvem o cilindro vazio.",
    "# Tanque de Armazenamento",
    "Química Pneumática (nível 3) desbloqueia o Tanque de Armazenamento. Ele guarda 16.000 unidades (144 frascos) de "
    "um líquido ou gás. Use-o segurando um frasco ou cilindro cheio para despejar, segurando um frasco vazio para "
    "retirar um, e com a mão vazia para ler o nível. Funis também funcionam: coloque recipientes cheios ou vazios por "
    "cima ou pelos lados, e retire o que sai por baixo. Um comparador lê o quanto está cheio. Quebrá-lo derrama o "
    "conteúdo.",
]

UPGRADES = [
    "# Espaços de melhoria",
    "Toda máquina tem dois espaços de melhoria no fim do painel lateral. Uma melhoria funciona quando o dono da "
    "máquina a pesquisou; duas iguais contam como uma. As melhorias marcadas como de máquina a combustível não fazem "
    "nada em máquinas elétricas.",
    "- Compressor (Compressão de Gases, nível 4): enche Cilindros de Aço com os gases que a máquina produz.",
    "- Empurrador de Saída (Moinho Automático, nível 3): leva os produtos da máquina para um baú, barril ou funil ao "
    "lado. Ele nunca alimenta outra máquina diretamente.",
    "- Regenerador (Forno Regenerativo, nível 4): uma máquina a combustível perde metade do calor, então gasta menos "
    "combustível e esquenta mais rápido.",
    "- Revestimento Refratário (Refratários Básicos, nível 4): uma máquina a combustível pode passar 150 °C do seu "
    "limite, se o combustível e os Foles chegarem lá.",
    "- Coifa de Recuperação de Gases (Recuperação de Subprodutos, nível 4): os líquidos e gases que a máquina "
    "liberaria (dióxido de carbono, dióxido de enxofre, ácido clorídrico e outros) vão para frascos ou cilindros. "
    "Vapor e gases misturados como o gás de hulha continuam sendo liberados. O gás captado nunca para a máquina: sem "
    "recipiente ou espaço, ele é liberado assim mesmo.",
    "- Agitador Mecânico (Motor Elétrico, nível 4): processa a carga 50% mais rápido. O seu motor queima um quarto a "
    "mais de combustível enquanto a carga reage, ou puxa corrente extra numa máquina elétrica.",
    "- Leito Catalítico (Leitos Catalíticos, nível 5): reações sobre um catalisador sólido (ferro, níquel, platina, "
    "platina-ródio, pentóxido de vanádio, brometo de cobalto e manganês) rodam duas vezes mais rápido: amônia, o "
    "processo de contato, ácido nítrico, hidrogenação do carvão, reforma, náilon e outras.",
    "- Esteira Transportadora (Borracha Buna, nível 5): um Empurrador de Saída sobre uma correia de borracha; leva os "
    "produtos prontos de quatro em quatro em vez de um.",
    "- Isolamento de Espuma (Estireno e Poliestireno, nível 6): uma máquina com limite de 300 °C ou menos perde 70% "
    "menos calor.",
    "- Filtro de Ar Estéril (Penicilina, nível 6): o Barril de Fermentação, o Tanque de Vinagre, o Fermentador de "
    "Tanque Profundo e o Biorreator produzem 10% mais. O Fermentador de Tanque Profundo não funciona sem um.",
]

FARM = [
    "# Graxaria e sabão",
    "Graxaria (nível 1) desbloqueia o Caldeirão de Sabão. Carne, ossos, ovos e leite entram; o caldeirão os cozinha "
    "de 80 a 150 °C. O sebo flutua, e a carne e o osso que sobram saem como Farinha de Proteína, que vacas, ovelhas, "
    "cabras, porcos e galinhas comem e que vira um bom composto. Sebo com linha faz Velas de Sebo: luz 12, "
    "fumacentas, e se consomem três dias depois de colocadas.",
    "Fabricação de Sabão: coloque sebo ou óleo vegetal no caldeirão e cinza de madeira no espaço de reagente para "
    "obter sabão mole, com glicerol em frascos. O Sabão Duro (nível 2) usa barrilha, ou endurece o sabão mole com sal.",
    "Use uma barra de sabão para se lavar: você fica Limpo por dez minutos, e Limpo tira a Fome da carne podre e do "
    "frango cru. Com uma armadura de couro tingida na outra mão, o sabão esfrega o corante para fora. Sabão com lã ou "
    "uma cama na mesa de trabalho os lava até ficarem brancos.",
    "# Óleo, couro e mosto",
    "Prensagem de Óleo (nível 1): a Prensa de Óleo espreme a frio sementes de abóbora, melancia, trigo e cacau e "
    "girassóis. O óleo vegetal enche frascos; a Torta de Oleaginosas que sobra é ração para o gado. O sebo e o óleo "
    "vegetal também queimam como combustível.",
    "Curtimento em Poço (nível 1): o Poço de Curtume transforma carne e peles em couro sem mesa de trabalho, devagar: "
    "quatro carnes podres fazem um couro em cerca de três dias de jogo. Coloque toras de carvalho ou de pinheiro no "
    "espaço de reagente pelo tanino; a Casca de Curtume Gasta que sobra queima como combustível. Fabricação de Alúmen "
    "(nível 2) dá alúmen de potássio, e duas peles de coelho com um alúmen viram couro na mesa de trabalho.",
    "Mosturação (nível 1): o Barril de Fermentação transforma o amido do trigo e das batatas em açúcar e depois em "
    "álcool. Açúcar de Amido (nível 3) faz a primeira etapa mais rápido com um pouco de ácido sulfúrico no Tanque de "
    "Digestão. Óleo Doce de Vitríolo (nível 2): etanol destilado com ácido sulfúrico no Alambique dá éter dietílico.",
    "# Fertilizante",
    "Superfosfato (nível 3) dissolve ossos ou rocha fosfática em ácido sulfúrico; Água de Gás (nível 3) lava a amônia "
    "do Forno de Coque (deixe frascos no espaço de frascos dele) e a Câmara de Chumbo a transforma em sulfato de "
    "amônio. Use qualquer um dos fertilizantes em terra arada: nas próximas oito colheitas a lavoura cresce metade "
    "mais rápido e uma colheita madura dá uma unidade a mais.",
    "# Borracha",
    "As seringueiras crescem nativas nas selvas (selva, selva esparsa e selva de bambu). As folhas deixam cair mudas, "
    "e a muda cresce em qualquer lugar, mas uma seringueira só dá látex numa selva. Vulcanização (nível 3) desbloqueia "
    "a Bica de Sangria: fixe-a na lateral de um tronco de seringueira e use-a com um Frasco de Vidro vazio; ela enche "
    "um frasco a cada cinco minutos e guarda quatro. Funis podem dar frascos vazios a ela por cima e tirar o látex por "
    "baixo. Fora da selva ela avisa que o clima é errado.",
    "No Vulcanizador, látex com enxofre no espaço de reagente cura em borracha vulcanizada de 130 a 160 °C; sem "
    "enxofre ele só seca em borracha crua. A borracha vulcanizada faz Tênis (seus passos não fazem vibrações, e o "
    "magma e os ovos de tartaruga não te notam) e Rodas de Borracha: use-as num carrinho de mina e ele anda 25% mais "
    "rápido.",
]

TAR = [
    "# Alcatrão de hulha",
    "Destilação do Alcatrão (nível 3): com ela pesquisada, o Forno de Coque capta o alcatrão do carvão em frascos "
    "(deixe frascos vazios no espaço de frascos dele), cerca de um frasco a cada 13 carvões. O Alambique de Alcatrão "
    "corta o alcatrão de hulha em benzeno, tolueno, fenol (ácido carbólico) e antraceno, e deixa piche de alcatrão. "
    "Piche com cascalho e areia faz Asfalto de Alcatrão, um bloco de estrada tão rápido quanto o Asfalto. Benzeno, "
    "fenol, alcatrão e piche queimam como combustível. Nitrobenzeno (nível 3) nitra o benzeno no Tanque de Digestão, e "
    "Anilina (nível 4) o reduz com ferro.",
    "# Corantes",
    "Azul da Prússia (nível 2) desbloqueia o Tanque de Tingimento: carne podre ou carne com vitríolo verde no espaço "
    "de reagente faz o pigmento, e o tanque o transforma em corante azul, 8 por item. Os corantes de alcatrão dão 32 "
    "corantes comuns por frasco: Fucsina (anilina e arsênico branco, magenta), Ácido Pícrico (fenol e ácido nítrico "
    "no Tanque de Digestão, amarelo) e Alizarina (antraceno e soda cáustica na Autoclave, vermelho). Coloque o corante "
    "no Tanque de Tingimento para obter os corantes comuns.",
    "# Infecção e remédios",
    "Um golpe de zumbi, zumbi do deserto, afogado ou aldeão zumbi te infecta 1 vez em 200, e comer carne podre 1 vez "
    "em 100; estar Limpo corta as duas chances pela metade. Uma Infecção dura um dia de jogo com Fraqueza e Fome. Sem "
    "tratamento, ela passa sozinha 7 vezes em 10; nas outras vira Sepse, 1 de dano a cada 10 segundos até você morrer. "
    "Sabão Carbólico (Cirurgia Antisséptica: fenol e sabão duro) e Aspirina (nível 4: fenol na Autoclave com barrilha, "
    "depois ácido acético no Tanque de Digestão) curam uma Infecção antes que ela se agrave. Só as Sulfas (nível 5) e "
    "os antibióticos do nível 6 curam a Sepse. O leite não cura nenhuma das duas. A regra de jogo "
    "periodicElementsInfection desliga a infecção.",
    "A aspirina também tira a Náusea e a Fadiga de Mineração e dá 10 segundos de Regeneração. O Éter Anestésico (éter "
    "na lã), usado num aldeão zumbi cuja cura começou, corta pela metade o tempo da cura.",
    "# Explosivos",
    "Dinamite (nível 4): o glicerol da fabricação de sabão, nitrado a frio (5 a 25 °C) no Tanque de Digestão, dá "
    "nitroglicerina; com areia e papel ela faz dinamite, e quatro bananas com linha fazem uma Carga de Mineração. "
    "Acenda com pederneira e aço ou redstone: depois de 4 segundos ela quebra todo bloco mais fraco que obsidiana "
    "num cubo 5x5x5 e todos eles caem. Com mobGriefing desligado ela só fere o que estiver perto. Cordite (nível 4) "
    "amassa nitroglicerina com pólvora sem fumaça; os Cartuchos de Cordite disparam do Fuzil de Retrocarga 25% mais "
    "fortes e sem fumaça.",
    "# Lavouras e pragas",
    "A Calda Bordalesa (vitríolo azul, cal virgem e água) pulverizada numa lavoura ou arbusto de bagas impede que ela "
    "seja lavada pela chuva ou murche no escuro, e os arbustos de bagas param de arranhar. Pó Inseticida: a Prensa de "
    "Óleo mói margaridas em piretro, e um Spray Inseticida fere aranhas, traças, endermites e abelhas 4 blocos à "
    "frente.",
    "# Bomba de Vácuo",
    "Destilação a Vácuo (nível 4) desbloqueia a melhoria Bomba de Vácuo: num Alambique, Alambique de Alcatrão, "
    "Caldeirão de Sabão, Cristalizador, Salina ou Alambique de Refinaria, toda reação roda 100 °C mais fria, então a "
    "máquina esquenta antes e perde menos calor.",
]

INDUSTRY = [
    "# Carbeto e acetileno",
    "Carbeto de Cálcio (nível 5): cal virgem com coque no espaço de combustível do Forno Elétrico a Arco se funde em "
    "carbeto a 1800 °C ou mais. No Tanque de Digestão com água no espaço de reagente, o carbeto dá acetileno (deixe "
    "frascos, ou cilindros e um Compressor, no tanque) e deixa cal hidratada.",
    "A Lanterna de Carbureto (latão, vidro, carbeto e um balde de água) dá luz 14 segurada em qualquer mão ou usada na "
    "cabeça, por meia hora. Use-a com carbeto de cálcio no inventário para recarregar; uma lanterna vazia não se perde.",
    "Solda Oxiacetilênica: fabrique um Maçarico de Solda com um cilindro de acetileno e um de oxigênio, um lingote de "
    "aço e um lingote de cobre. Segure uma ferramenta ou peça de armadura de metal danificada na outra mão e use o "
    "maçarico: um lingote do mesmo metal do seu inventário conserta um quarto dela, sem experiência. O maçarico faz 64 "
    "soldas; fabrique-o de novo com cilindros novos para recarregá-lo.",
    "Cal Nitrogenada: carbeto no Forno de Cal de 1000 a 1150 °C com nitrogênio no espaço de reagente absorve o "
    "nitrogênio como cianamida de cálcio, um fertilizante.",
    "# Fertilizante do ar",
    "Síntese da Ureia: amônia no Reator de Alta Pressão de 170 a 200 °C, com dióxido de carbono no espaço de reagente "
    "(capture-o de um Forno de Cal ou Barril de Fermentação com uma Coifa de Recuperação de Gases). Fertilizante "
    "Perolado desbloqueia a Torre de Perolação, uma máquina elétrica: amônia com ácido nítrico vira nitrato de amônio, "
    "e ureia com Mistura PK (superfosfato e potássio) vira Grânulos NPK.",
    "Cal nitrogenada, ureia e nitrato de amônio funcionam em terra arada como o superfosfato. Os Grânulos NPK fazem a "
    "lavoura crescer duas vezes mais rápido, por oito colheitas, cada uma dando uma unidade a mais.",
    "# Plásticos e borracha",
    "Formaldeído: metanol sobre prata no Conversor de Ostwald de 600 a 700 °C. Baquelite: fenol com formaldeído no "
    "espaço de reagente, na Autoclave de 150 a 200 °C. Quatro baquelites fazem quatro blocos de baquelite, marrons, ou "
    "pretos, vermelhos, verdes, marfim ou âmbar com um corante. A baquelite também serve de caixa para o Contador "
    "Geiger no lugar do latão.",
    "Neoprene: acetileno com ácido clorídrico no Reator de Alta Pressão. Uma Roupa de Neoprene completa dá Graça do "
    "Golfinho na água, e nenhuma peça dela deixa entrar o frio da neve fofa.",
    "Borracha Buna, sem selva: etanol sobre o catalisador na Unidade de Craqueamento de 380 a 450 °C dá butadieno; "
    "benzeno com etileno no espaço de reagente de 550 a 600 °C dá estireno; butadieno com estireno no Tanque de "
    "Digestão de 40 a 60 °C dá borracha Buna. Ela faz Rodas de Borracha e a melhoria Esteira Transportadora.",
    "# Explosivos",
    "TNT: tolueno do Alambique de Alcatrão, nitrado no Tanque de Digestão. Um TNT com três papéis faz três blocos de "
    "TNT comum. Quatro TNT, um lingote de aço e linha fazem uma Carga de Demolição: acesa como uma Carga de Mineração, "
    "ela quebra um cubo 3x3x3 de qualquer coisa menos rocha-matriz, obsidiana inclusive, e deixa cair tudo.",
    "# Fermentação, combustível e remédios",
    "Fermentação Acetônica: trigo ou batatas no Barril de Fermentação, com calcita no espaço de reagente, fermentam em "
    "acetona, butanol e um pouco de etanol em vez de açúcar. Biodiesel: sebo ou óleo vegetal no Caldeirão de Sabão de "
    "40 a 150 °C com metanol no espaço de reagente. Biodiesel e butanol movem o Gerador a Diesel e queimam em "
    "fornalhas.",
    "Sulfas: anilina com sulfato de amônio no Tanque de Digestão dá sulfanilamida. Use-a para curar uma Infecção em "
    "qualquer estágio, Sepse inclusive.",
]

LIFE = [
    "# Antibióticos",
    "Penicilina (nível 6) desbloqueia o Fermentador de Tanque Profundo, uma máquina elétrica que só funciona com um "
    "Filtro de Ar Estéril. Glicose com leite no espaço de reagente (pela lactose) cultiva penicilina de 20 a 37 °C; a "
    "Estreptomicina usa farinha de proteína no espaço de reagente. Qualquer um dos antibióticos cura uma Infecção ou "
    "Sepse e te deixa imune a Infecção por 20 minutos. Use penicilina num aldeão zumbi com Fraqueza e a cura dele "
    "começa, sem maçã dourada. O Micélio Gasto do tanque é fertilizante.",
    "# Pesticidas",
    "DDT: benzeno com cloro no Tanque de Digestão. Uma Polvilhadeira de DDT usada no chão o deixa mortal para aranhas, "
    "traças, endermites e abelhas por três dias de jogo; as galinhas sobre ele botam metade das vezes. O Mata-Mato "
    "(2,4-D, de fenol e cloro) mata flores, samambaias, trepadeiras, arbustos, mudas e folhas em volta do bloco, mas "
    "poupa a grama e as lavouras. O Pré-Emergente (atrazina, de amônia e cloro com coque no Reator de Alta Pressão) "
    "impede a grama e o micélio de se espalharem no chão por dez dias de jogo. Cada pulverizador tem 16 usos e "
    "funciona num raio de 4 blocos.",
    "# Plásticos",
    "Estireno e Poliestireno: estireno no Reator de Alta Pressão. Oito poliestirenos em volta de um frasco de metano "
    "fazem Isolamento de Espuma; quatro poliestirenos e metano fazem quatro blocos de Isolamento, e gelo ou neve "
    "encostados no Isolamento nunca derretem.",
    "# Explosivos",
    "Explosivo Plástico: formaldeído e amônia fazem hexamina, e a hexamina nitrada a frio faz RDX. Dois RDX, um "
    "lingote de cobre, um lingote de aço e linha fazem uma Carga Moldada: coloque-a numa parede e acenda, e ela abre "
    "um túnel 1x1 de 16 blocos de profundidade em qualquer coisa menos rocha-matriz. ANFO: seis nitratos de amônio, "
    "diesel, papel e linha fazem uma Carga de ANFO que quebra um cubo 9x9x9 de rocha; três em cada quatro blocos "
    "caem.",
    "# Contramedidas de radiação",
    "O iodeto de potássio (iodeto de sódio das algas com sal de potássio no Cristalizador) corta em 40% a dose dos "
    "produtos de fissão por um dia de jogo. Uma Cápsula de Azul da Prússia (azul da Prússia e farinha de proteína) a "
    "corta em um quarto. A Amifostina (nível 7) reduz à metade o dano de toda radiação por uma hora.",
    "# Biotecnologia",
    "DNA Recombinante (nível 7) desbloqueia o Biorreator (ele precisa de um circuito integrado). Ele fermenta a "
    "celulose de toras, folhas ou palha em etanol, glicose em PLA, e proteína com óleo vegetal na Vacina de mRNA, "
    "sete dias de jogo sem Infecção. A sua Biomassa é fertilizante. O PLA faz vasos de flores e molduras de itens, e "
    "vira composto.",
    "Glifosato: formaldeído com ácido fosfórico no Reator de Alta Pressão. O Limpa-Campo mata toda planta em volta do "
    "bloco, lavouras também, menos o trigo de Sementes Tolerantes (oito sementes de trigo em volta de um glifosato). O "
    "trigo tolerante também cresce um décimo mais rápido.",
]

MINING = [
    "# Marretas e escavadeiras",
    "Marretas de Pedreira (nível 2) ensina a Marreta (cinco lingotes em volta de um graveto, sobre um graveto) e a "
    "Escavadeira (três lingotes e dois gravetos), em aço e em todo metal de ferramenta posterior. Cada uma quebra um "
    "quadrado 3x3 na face que você acerta, a três décimos da velocidade, e desgasta um ponto por bloco. Agache para "
    "minerar um só bloco. Só quebram os blocos certos para a ferramenta e não mais duros que o bloco mirado; nada com "
    "inventário ou máquina quebra nunca; o que cai vai direto para o seu inventário.",
    "# Detonação",
    "Detonação de Rocha (nível 2): quatro pólvoras negras, papel e linha fazem duas Cargas de Detonação. Acesa com "
    "pederneira e aço ou redstone, uma quebra um bloco 3x3x3 de pedra e deixa cair tudo; terra, madeira e máquinas "
    "são poupadas.",
    "# Perfuratrizes e brocas",
    "@4 Toda perfuratriz precisa de uma broca: segure a broca na outra mão, agache e use a perfuratriz. A broca define "
    "o que ela pode minerar e com que rapidez (aço-tungstênio 8,5, aço rápido 10, metal duro 12, diamante 13) e "
    "desgasta um ponto por golpe. Encante a broca, não a perfuratriz; uma broca encaixada empresta os seus encantamentos "
    "à perfuratriz, todos menos Remendo. Agache e use sem broca na outra mão para trocar de modo. Uma perfuratriz "
    "gasta 1,5 kJ por ponto de dureza de cada bloco: 2,25 para pedra, 4,5 para ardósia.",
    "Perfuratriz de Rocha (nível 4): o Compressor de Ar, uma máquina elétrica, enche Cilindros de Aço com Ar "
    "Comprimido, sem precisar da melhoria Compressor. A Perfuratriz Pneumática gasta um cilindro a cada 600 kJ (cerca "
    "de 400 pedras) e devolve o vazio; ela minera um bloco, um túnel 1x3 ou 3x3, e te deixa mais lento enquanto a "
    "segura.",
    "# Ferramentas a bateria",
    "Bateria de Chumbo-Ácido (nível 4): a Bateria guarda 1.500 kJ. Ligue uma Bancada de Carga ao fio e use a bateria "
    "nela; ela carrega a até 5 kJ por tick.",
    "O Martelete Rotativo (nível 5) é a perfuratriz de rocha a bateria, sem lentidão. A Motosserra (nível 5) derruba "
    "uma árvore inteira, até 128 toras, gastando 3 kJ por tora.",
    "A Perfuratriz Pesada de Mineração (nível 6) acrescenta um modo 5x5 e um modo veio que segue um minério por até 32 "
    "blocos; com uma bateria de chumbo-ácido ela te deixa mais lento.",
    "A Bateria de Íon-Lítio (nível 7) guarda 6.000 kJ, é usada primeiro e tira essa lentidão. Busca de Minério "
    "(nível 7): no modo veio, use a Perfuratriz Pesada de Mineração num minério para ler o tamanho e os elementos do "
    "veio antes de cavar.",
]

ISOTOPES = [
    "# Radiação",
    "Itens radioativos emitem uma dose que se acumula enquanto você os carrega ou fica perto deles. Um Eletroscópio, "
    "depois um Contador Geiger, a mede. Paredes de chumbo, a Célula Quente e o Reator Nuclear blindam o que guardam. "
    "Algumas amostras decaem no relógio do jogo (um dia de jogo é um dia real para elas) e se transformam nas suas "
    "filhas no seu inventário.",
    "# Brilho azul",
    "Os emissores mais fortes brilham azul no escuro, como a luz de Cherenkov: rádio e mais fortes no ar, e na água "
    "também polônio, amerício, actínio e pechblenda, mais brilhantes. Um Reator Nuclear funcionando ilumina a sua "
    "piscina, e os aceleradores funcionando também brilham. O brilho acompanha o quão concentrada é a atividade, não "
    "o quanto você tem; o urânio nunca brilha. Itens que brilham também brilham no seu espaço do inventário.",
    "# Isótopos naturais",
    "A dica de todo elemento estável lista os seus isótopos naturais, do maior para o menor.",
    "# Proporções que mudam",
    "Itens de urânio (U-235), plutônio (Pu-240), lítio (Li-6), boro (B-10), cálcio (Ca-48) e hidrogênio (H-2) mostram "
    "a sua própria proporção. Um item na proporção natural se empilha com qualquer outro; um enriquecido ou empobrecido "
    "só se empilha com itens na mesma proporção. As máquinas misturam as proporções pela massa.",
    "- O Calutron (Enriquecimento por Calutron) separa urânio, lítio ou boro numa pequena fração enriquecida e num "
    "rejeito empobrecido. Coloque a fração de volta para enriquecer mais.",
    "- Urânio metálico com 3% de U-235 ou mais sinteriza em pastilhas de combustível de reator.",
    "- O plutônio criado no reator é de grau armamento (6% de Pu-240); o plutônio do combustível usado é de grau "
    "reator.",
    "- Lítio com mais de 90% de Li-6 nos espaços de um reator vira trítio, que acende Luzes de Trítio para sempre.",
    "- Boro com mais de 90% de B-10 faz barras de controle: com uma nos seus espaços, um reator para de queimar "
    "combustível enquanto está cheio.",
    "- A água pesada da Cascata de Enriquecimento (alimente-a com gelo ou neve) faz as barras de combustível de urânio "
    "natural queimarem.",
]

# Templates for the generated chapters; keys match guidebook.EN_T.
T = {
    "start": "Primeiros Passos",
    "group": "Nível {n}",
    "research": "Pesquisa",
    "machines": "Máquinas",
    "reactions": "Reações",
    "recipe": " Receita: {r}.",
    "recipe_head": "Receita: {r}.",
    "projects": "# Projetos (★ = chave)",
    "needs": " Requer {x}.",
    "samples": " Amostras: {x}.",
    "unlocks": " Desbloqueia {x}.",
    "up_to": "{desc}. Até {c} °C",
    "electric": ", elétrica.",
    "fuel": ", a combustível.",
    "amounts": "Quantidades por 100 unidades de carga.",
    "ores": "Minérios",
    "ores_head": "# Minérios",
    "ores_intro": "Os minérios surgem em chunks recém-explorados. Toque de Seda e Fortuna funcionam como nos minérios comuns.",
    "ore_line": "{name}: {desc}. Dá {lo}-{hi} {drop}; precisa de uma picareta de {tier}. ",
    "found": "Encontrado em {w}.",
    "where": "y {lo} a {hi} {b}",
    "biomes": "em {b}",
    "overworld": "na Superfície",
    "nether": "no Nether",
    "power": "Energia",
    "power_head": "# Energia",
    "power_intro": "As máquinas elétricas recebem corrente dos blocos de energia ao lado delas, ou ligados a elas por Fio de "
                   "Cobre (até 64 blocos de fio) ou Cabo Isolado (quatro vezes mais longe).",
    "power_line": "{name} (nível {lvl}, {project}): {desc}.",
    "isotopes": "Radiação e Isótopos",
    "containers": "Recipientes",
    "upgrades": "Melhorias de Máquinas",
    "farm": "Fazenda e Gordura",
    "tar": "Alcatrão e Remédios",
    "industry": "Orgânicos Industriais",
    "life": "Ciências da Vida",
    "mining": "Mineração",
    "crafting": "Fabricação",
    "craft_head": "# Fabricação",
    "craft_intro": "Receitas sem forma que o mod acrescenta (as receitas de máquinas e blocos estão nos capítulos de nível).",
    "shaped_head": "# Receitas com forma (linhas de cima para baixo)",
    "or": " ou ",
    "any": "qualquer {x}",
    "empty": "vazio",
    "reagent": "(reagente)",
    "carbon": " + {c} carbono",
    "nothing": "nada fica",
    "slag": "; escória: ",
    "vents": "; libera ",
    "temp": ". De {a} a {b} °C.",
}

WORDS = {
    # pickaxe tiers
    "wood": "madeira", "stone": "pedra", "iron": "ferro",
    # biomes and biome tags
    "mountain": "montanhas", "jungle": "selvas", "savanna": "savanas", "desert": "deserto", "badlands": "terras áridas",
    "ocean": "oceanos", "swamp": "pântano", "mangrove swamp": "manguezal", "basalt deltas": "deltas de basalto",
    "beach": "praias", "river": "rios", "dripstone caves": "cavernas de espeleotemas",
    # item tags
    "logs": "tora", "logs that burn": "tora que queima", "oak logs": "tora de carvalho", "planks": "tábua",
    "wool": "lã", "beds": "cama", "cable insulation": "isolamento de cabo", "soap": "sabão",
    # upgrades unlocked by research
    "forced air": "ar forçado (Foles)", "steam blast": "sopro a vapor",
}

# 2.7.0 Field Atlas (tools/atlas_i18n.py).
from atlas_i18n import GUIDE as _ATLAS_GUIDE, TITLE as _ATLAS_TITLE  # noqa: E402
ATLAS = _ATLAS_GUIDE["pt_br"]
T["atlas"] = _ATLAS_TITLE["pt_br"]

# 2.8.0 Trees of the world (tools/trees_i18n.py).
from trees_i18n import GUIDE as _TREES_GUIDE, GUIDE_T as _TREES_T, TEXT as _TREES_TEXT, LANGS as _TREES_LANGS  # noqa: E402
TREES = _TREES_GUIDE["pt_br"]
T.update(_TREES_T["pt_br"])
T["trees"] = _TREES_TEXT["Trees of the World"].split(" / ")[_TREES_LANGS.index("pt_br")]

# 3.1.0 Tree Compendium (tools/compendium_i18n.py).
from compendium_i18n import GUIDE as _COMP_GUIDE, TITLE as _COMP_TITLE  # noqa: E402
COMPENDIUM = _COMP_GUIDE["pt_br"]
T["compendium"] = _COMP_TITLE["pt_br"]

# 3.5.0 Plant Compendium (tools/compendium_plants_pt_br.py).
from compendium_i18n import PLANT_GUIDE as _PCOMP_GUIDE, PLANT_TITLE as _PCOMP_TITLE  # noqa: E402
PLANT_COMPENDIUM = _PCOMP_GUIDE["pt_br"]
T["plant_compendium"] = _PCOMP_TITLE["pt_br"]

# 3.15.0 Mineral Compendium (tools/compendium_minerals_pt_br.py).
from compendium_i18n import MINERAL_GUIDE as _MCOMP_GUIDE, MINERAL_TITLE as _MCOMP_TITLE  # noqa: E402
MINERAL_COMPENDIUM = _MCOMP_GUIDE["pt_br"]
T["mineral_compendium"] = _MCOMP_TITLE["pt_br"]
