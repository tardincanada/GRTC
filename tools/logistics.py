"""Belts, large hoppers, the Belt Sorter and larger chests (3.40.0): textures, models, loot, tags, recipes and text.

Code in src/main/java/com/periodicelements/logistics/ and client/BeltRenderer, client/CrateScreen.
generate.py calls generate_logistics() and merges translations(code).
"""
import json
import random

from PIL import Image

BELTS = {  # id: (name, items, blocks per second, pixels the top scrolls per tick)
    "canvas_belt": ("Canvas Belt", 8, 2, 1),
    "rubber_belt": ("Rubber Belt", 16, 4, 2),
    "roller_conveyor": ("Roller Conveyor", 32, 10, 4),
}
HOPPERS = {"steel_hopper": ("Steel Hopper", 9, 16), "stainless_hopper": ("Stainless Steel Hopper", 18, 64)}
CHESTS = {"iron_chest": ("Iron Chest", 54), "steel_chest": ("Steel Chest", 78), "stainless_chest": ("Stainless Steel Chest", 108)}
SORTER = "belt_sorter"

# Research-gated items (the chests need no research: their metal is the gate).
ITEM_UNLOCKS = set(BELTS) | set(HOPPERS) | {SORTER}

LANG = {
    "block.periodic_elements.canvas_belt": "Canvas Belt",
    "block.periodic_elements.canvas_belt.desc": "Carries 8 items at a time, 2 blocks a second, into the next belt or the inventory in front",
    "block.periodic_elements.rubber_belt": "Rubber Belt",
    "block.periodic_elements.rubber_belt.desc": "Carries 16 items at a time, 4 blocks a second, into the next belt or the inventory in front",
    "block.periodic_elements.roller_conveyor": "Roller Conveyor",
    "block.periodic_elements.roller_conveyor.desc": "Carries 32 items at a time, 10 blocks a second, into the next belt or the inventory in front",
    "block.periodic_elements.belt_sorter": "Belt Sorter",
    "block.periodic_elements.belt_sorter.desc": "Turns chosen items aside, down into a hopper or out to a side; in Forbidden mode it turns aside all but the chosen ones",
    "block.periodic_elements.steel_hopper": "Steel Hopper",
    "block.periodic_elements.steel_hopper.desc": "9 places; moves 16 items every 4 ticks, 32 times a vanilla hopper",
    "block.periodic_elements.stainless_hopper": "Stainless Steel Hopper",
    "block.periodic_elements.stainless_hopper.desc": "18 places; moves 64 items every 4 ticks, 128 times a vanilla hopper",
    "gui.periodic_elements.sorter.mode": "Mode",
    "gui.periodic_elements.sorter.required": "Required",
    "gui.periodic_elements.sorter.required.tip": "Items like those in the filter are turned aside, down or to a side; all others go straight on",
    "gui.periodic_elements.sorter.forbidden": "Forbidden",
    "gui.periodic_elements.sorter.forbidden.tip": "Items like those in the filter go straight on; all others are turned aside, down or to a side",
    "block.periodic_elements.iron_chest": "Iron Chest",
    "block.periodic_elements.iron_chest.desc": "54 places in one block, as many as a double chest",
    "block.periodic_elements.steel_chest": "Steel Chest",
    "block.periodic_elements.steel_chest.desc": "78 places in one block",
    "block.periodic_elements.stainless_chest": "Stainless Steel Chest",
    "block.periodic_elements.stainless_chest.desc": "108 places in one block, twice a double chest",
}

# The two new research projects' names and descriptions (data/research.csv) go through the same translation table.
RESEARCH_TEXT = [
    "Rubber Belt Conveyor",
    "Thomas Robins' rubber-covered belt carries coal and ore for miles (1891): the faster Rubber Belt, the Steel Hopper to keep up with it, and the Belt Sorter that turns chosen items aside",
    "Moving Assembly Line",
    "Ford's line brings the work to the worker on rollers and chains (1913): the Roller Conveyor and the Stainless Steel Hopper",
]

_K = list(LANG.values()) + RESEARCH_TEXT

# One list per language, in the order of _K.
_T = {
    'pt_br': ['Esteira de Lona', 'Leva 8 itens por vez, 2 blocos por segundo, para a próxima esteira ou o inventário à frente', 'Esteira de Borracha', 'Leva 16 itens por vez, 4 blocos por segundo, para a próxima esteira ou o inventário à frente', 'Esteira de Roletes', 'Leva 32 itens por vez, 10 blocos por segundo, para a próxima esteira ou o inventário à frente', 'Separador de Esteira', 'Desvia os itens escolhidos, para um funil abaixo ou para um lado; no modo Proibidos desvia todos menos os escolhidos', 'Funil de Aço', '9 espaços; move 16 itens a cada 4 ticks, 32 vezes um funil comum', 'Funil de Aço Inox', '18 espaços; move 64 itens a cada 4 ticks, 128 vezes um funil comum', 'Modo', 'Obrigatórios', 'Os itens iguais aos do filtro são desviados, para baixo ou para um lado; os outros seguem em frente', 'Proibidos', 'Os itens iguais aos do filtro seguem em frente; todos os outros são desviados, para baixo ou para um lado', 'Baú de Ferro', '54 espaços num só bloco, tanto quanto um baú duplo', 'Baú de Aço', '78 espaços num só bloco', 'Baú de Aço Inox', '108 espaços num só bloco, o dobro de um baú duplo', 'Correia Transportadora de Borracha', 'A correia revestida de borracha de Thomas Robins leva carvão e minério por quilômetros (1891): a Esteira de Borracha, mais rápida, o Funil de Aço para acompanhá-la e o Separador de Esteira, que desvia os itens escolhidos', 'Linha de Montagem Móvel', 'A linha de Ford leva o trabalho ao operário sobre roletes e correntes (1913): a Esteira de Roletes e o Funil de Aço Inox'],
    'es_es': ['Cinta de Lona', 'Lleva 8 objetos a la vez, 2 bloques por segundo, a la siguiente cinta o al inventario de delante', 'Cinta de Caucho', 'Lleva 16 objetos a la vez, 4 bloques por segundo, a la siguiente cinta o al inventario de delante', 'Transportador de Rodillos', 'Lleva 32 objetos a la vez, 10 bloques por segundo, a la siguiente cinta o al inventario de delante', 'Clasificador de Cinta', 'Aparta los objetos elegidos, a una tolva de abajo o a un lado; en modo Prohibidos aparta todos menos los elegidos', 'Tolva de Acero', '9 huecos; mueve 16 objetos cada 4 ticks, 32 veces una tolva normal', 'Tolva de Acero Inoxidable', '18 huecos; mueve 64 objetos cada 4 ticks, 128 veces una tolva normal', 'Modo', 'Obligatorios', 'Los objetos iguales a los del filtro se apartan, abajo o a un lado; los demás siguen recto', 'Prohibidos', 'Los objetos iguales a los del filtro siguen recto; todos los demás se apartan, abajo o a un lado', 'Cofre de Hierro', '54 huecos en un solo bloque, tantos como un cofre doble', 'Cofre de Acero', '78 huecos en un solo bloque', 'Cofre de Acero Inoxidable', '108 huecos en un solo bloque, el doble de un cofre doble', 'Cinta Transportadora de Caucho', 'La cinta recubierta de caucho de Thomas Robins lleva carbón y mineral durante kilómetros (1891): la Cinta de Caucho, más rápida, la Tolva de Acero para seguirle el ritmo y el Clasificador de Cinta, que aparta los objetos elegidos', 'Cadena de Montaje Móvil', 'La cadena de Ford lleva el trabajo al obrero sobre rodillos y cadenas (1913): el Transportador de Rodillos y la Tolva de Acero Inoxidable'],
    'fr_fr': ['Tapis en toile', "Transporte 8 objets à la fois, 2 blocs par seconde, vers le tapis suivant ou l'inventaire devant", 'Tapis en caoutchouc', "Transporte 16 objets à la fois, 4 blocs par seconde, vers le tapis suivant ou l'inventaire devant", 'Convoyeur à rouleaux', "Transporte 32 objets à la fois, 10 blocs par seconde, vers le tapis suivant ou l'inventaire devant", 'Trieur de tapis', 'Écarte les objets choisis, dans un entonnoir dessous ou sur un côté ; en mode Interdits il écarte tout sauf les objets choisis', 'Entonnoir en acier', '9 places ; déplace 16 objets toutes les 4 ticks, 32 fois un entonnoir ordinaire', 'Entonnoir en acier inoxydable', '18 places ; déplace 64 objets toutes les 4 ticks, 128 fois un entonnoir ordinaire', 'Mode', 'Obligatoires', 'Les objets comme ceux du filtre sont écartés, en bas ou sur un côté ; les autres vont tout droit', 'Interdits', 'Les objets comme ceux du filtre vont tout droit ; tous les autres sont écartés, en bas ou sur un côté', 'Coffre en fer', "54 places dans un seul bloc, autant qu'un grand coffre", 'Coffre en acier', '78 places dans un seul bloc', 'Coffre en acier inoxydable', '108 places dans un seul bloc, deux fois un grand coffre', 'Convoyeur à bande en caoutchouc', "La bande recouverte de caoutchouc de Thomas Robins transporte charbon et minerai sur des kilomètres (1891) : le Tapis en caoutchouc, plus rapide, l'Entonnoir en acier pour suivre et le Trieur de tapis qui écarte les objets choisis", 'Chaîne de montage mobile', "La chaîne de Ford amène le travail à l'ouvrier sur rouleaux et chaînes (1913) : le Convoyeur à rouleaux et l'Entonnoir en acier inoxydable"],
    'de_de': ['Segeltuchband', 'Trägt 8 Gegenstände auf einmal, 2 Blöcke pro Sekunde, zum nächsten Band oder in das Inventar davor', 'Gummiband', 'Trägt 16 Gegenstände auf einmal, 4 Blöcke pro Sekunde, zum nächsten Band oder in das Inventar davor', 'Rollenförderer', 'Trägt 32 Gegenstände auf einmal, 10 Blöcke pro Sekunde, zum nächsten Band oder in das Inventar davor', 'Bandsortierer', 'Schleust gewählte Gegenstände aus, in einen Trichter darunter oder zur Seite; im Modus Verboten alle außer den gewählten', 'Stahltrichter', '9 Plätze; bewegt 16 Gegenstände alle 4 Ticks, 32-mal ein normaler Trichter', 'Edelstahltrichter', '18 Plätze; bewegt 64 Gegenstände alle 4 Ticks, 128-mal ein normaler Trichter', 'Modus', 'Erforderlich', 'Gegenstände wie im Filter werden ausgeschleust, nach unten oder zur Seite; alle anderen gehen geradeaus', 'Verboten', 'Gegenstände wie im Filter gehen geradeaus; alle anderen werden ausgeschleust, nach unten oder zur Seite', 'Eisentruhe', '54 Plätze in einem Block, so viele wie eine große Truhe', 'Stahltruhe', '78 Plätze in einem Block', 'Edelstahltruhe', '108 Plätze in einem Block, doppelt so viele wie eine große Truhe', 'Gummigurtförderer', "Thomas Robins' gummierter Gurt trägt Kohle und Erz kilometerweit (1891): das schnellere Gummiband, der Stahltrichter, der mithält, und der Bandsortierer, der gewählte Gegenstände ausschleust", 'Fließband', 'Fords Band bringt die Arbeit auf Rollen und Ketten zum Arbeiter (1913): der Rollenförderer und der Edelstahltrichter'],
    'it_it': ['Nastro di tela', "Porta 8 oggetti alla volta, 2 blocchi al secondo, al nastro successivo o all'inventario davanti", 'Nastro di gomma', "Porta 16 oggetti alla volta, 4 blocchi al secondo, al nastro successivo o all'inventario davanti", 'Trasportatore a rulli', "Porta 32 oggetti alla volta, 10 blocchi al secondo, al nastro successivo o all'inventario davanti", 'Smistatore per nastro', 'Devia gli oggetti scelti, in una tramoggia sotto o di lato; in modalità Vietati devia tutti tranne quelli scelti', "Tramoggia d'acciaio", '9 posti; sposta 16 oggetti ogni 4 tick, 32 volte una tramoggia normale', "Tramoggia d'acciaio inox", '18 posti; sposta 64 oggetti ogni 4 tick, 128 volte una tramoggia normale', 'Modalità', 'Obbligatori', 'Gli oggetti come quelli del filtro vengono deviati, in basso o di lato; gli altri vanno dritti', 'Vietati', 'Gli oggetti come quelli del filtro vanno dritti; tutti gli altri vengono deviati, in basso o di lato', 'Baule di ferro', '54 posti in un solo blocco, quanti una cassa grande', "Baule d'acciaio", '78 posti in un solo blocco', "Baule d'acciaio inox", '108 posti in un solo blocco, il doppio di una cassa grande', 'Nastro trasportatore in gomma', "Il nastro rivestito di gomma di Thomas Robins porta carbone e minerale per chilometri (1891): il Nastro di gomma, più veloce, la Tramoggia d'acciaio per stargli dietro e lo Smistatore per nastro che devia gli oggetti scelti", 'Catena di montaggio mobile', "La catena di Ford porta il lavoro all'operaio su rulli e catene (1913): il Trasportatore a rulli e la Tramoggia d'acciaio inox"],
    'nl_nl': ['Canvas lopende band', 'Draagt 8 voorwerpen tegelijk, 2 blokken per seconde, naar de volgende band of de inventaris ervoor', 'Rubberen lopende band', 'Draagt 16 voorwerpen tegelijk, 4 blokken per seconde, naar de volgende band of de inventaris ervoor', 'Rollentransporteur', 'Draagt 32 voorwerpen tegelijk, 10 blokken per seconde, naar de volgende band of de inventaris ervoor', 'Bandsorteerder', 'Buigt gekozen voorwerpen af, omlaag in een trechter of opzij; in de stand Verboden alles behalve de gekozen', 'Stalen trechter', '9 plaatsen; verplaatst 16 voorwerpen per 4 ticks, 32 keer een gewone trechter', 'Roestvrijstalen trechter', '18 plaatsen; verplaatst 64 voorwerpen per 4 ticks, 128 keer een gewone trechter', 'Stand', 'Verplicht', 'Voorwerpen zoals in het filter worden afgebogen, omlaag of opzij; alle andere gaan rechtdoor', 'Verboden', 'Voorwerpen zoals in het filter gaan rechtdoor; alle andere worden afgebogen, omlaag of opzij', 'IJzeren kist', '54 plaatsen in één blok, zoveel als een dubbele kist', 'Stalen kist', '78 plaatsen in één blok', 'Roestvrijstalen kist', '108 plaatsen in één blok, twee keer een dubbele kist', 'Rubberen transportband', 'De met rubber beklede band van Thomas Robins draagt kolen en erts kilometers ver (1891): de snellere rubberen lopende band, de stalen trechter om bij te blijven en de bandsorteerder die gekozen voorwerpen afbuigt', 'Lopende montageband', 'Fords band brengt het werk naar de arbeider op rollen en kettingen (1913): de rollentransporteur en de roestvrijstalen trechter'],
    'ru_ru': ['Брезентовый конвейер', 'Везёт 8 предметов за раз, 2 блока в секунду, на следующий конвейер или в инвентарь впереди', 'Резиновый конвейер', 'Везёт 16 предметов за раз, 4 блока в секунду, на следующий конвейер или в инвентарь впереди', 'Роликовый конвейер', 'Везёт 32 предмета за раз, 10 блоков в секунду, на следующий конвейер или в инвентарь впереди', 'Конвейерный сортировщик', 'Отводит выбранные предметы вниз в воронку или вбок; в режиме «Запрещённые» отводит всё, кроме выбранных', 'Стальная воронка', '9 ячеек; перемещает 16 предметов каждые 4 тика, в 32 раза больше обычной воронки', 'Воронка из нержавеющей стали', '18 ячеек; перемещает 64 предмета каждые 4 тика, в 128 раз больше обычной воронки', 'Режим', 'Обязательные', 'Предметы как в фильтре отводятся вниз или вбок; остальные идут прямо', 'Запрещённые', 'Предметы как в фильтре идут прямо; все остальные отводятся вниз или вбок', 'Железный сундук', '54 ячейки в одном блоке, как в двойном сундуке', 'Стальной сундук', '78 ячеек в одном блоке', 'Сундук из нержавеющей стали', '108 ячеек в одном блоке, вдвое больше двойного сундука', 'Резиновый ленточный конвейер', 'Прорезиненная лента Томаса Робинса везёт уголь и руду на километры (1891): более быстрый резиновый конвейер, стальная воронка, чтобы поспевать за ним, и конвейерный сортировщик, отводящий выбранные предметы', 'Подвижный сборочный конвейер', 'Конвейер Форда подаёт работу рабочему на роликах и цепях (1913): роликовый конвейер и воронка из нержавеющей стали'],
    'sv_se': ['Kanvasband', 'Bär 8 föremål åt gången, 2 block i sekunden, till nästa band eller förrådet framför', 'Gummiband', 'Bär 16 föremål åt gången, 4 block i sekunden, till nästa band eller förrådet framför', 'Rullbana', 'Bär 32 föremål åt gången, 10 block i sekunden, till nästa band eller förrådet framför', 'Bandsorterare', 'Leder bort valda föremål, ner i en tratt eller åt sidan; i läget Förbjudna leder den bort allt utom de valda', 'Ståltratt', '9 platser; flyttar 16 föremål var 4:e tick, 32 gånger en vanlig tratt', 'Rostfri ståltratt', '18 platser; flyttar 64 föremål var 4:e tick, 128 gånger en vanlig tratt', 'Läge', 'Obligatoriska', 'Föremål som i filtret leds bort, nedåt eller åt sidan; alla andra går rakt fram', 'Förbjudna', 'Föremål som i filtret går rakt fram; alla andra leds bort, nedåt eller åt sidan', 'Järnkista', '54 platser i ett block, lika många som en dubbelkista', 'Stålkista', '78 platser i ett block', 'Rostfri stålkista', '108 platser i ett block, dubbelt så många som en dubbelkista', 'Gummitransportband', 'Thomas Robins gummiklädda band bär kol och malm i kilometer (1891): det snabbare gummibandet, ståltratten som hänger med och bandsorteraren som leder bort valda föremål', 'Rörligt löpande band', 'Fords band för arbetet till arbetaren på rullar och kedjor (1913): rullbanan och den rostfria ståltratten'],
    'da_dk': ['Lærredsbånd', 'Bærer 8 genstande ad gangen, 2 blokke i sekundet, til næste bånd eller lageret foran', 'Gummibånd', 'Bærer 16 genstande ad gangen, 4 blokke i sekundet, til næste bånd eller lageret foran', 'Rullebane', 'Bærer 32 genstande ad gangen, 10 blokke i sekundet, til næste bånd eller lageret foran', 'Båndsorterer', 'Leder valgte genstande væk, ned i en tragt eller til siden; i tilstanden Forbudte leder den alt undtagen de valgte væk', 'Ståltragt', '9 pladser; flytter 16 genstande hver 4. tick, 32 gange en almindelig tragt', 'Rustfri ståltragt', '18 pladser; flytter 64 genstande hver 4. tick, 128 gange en almindelig tragt', 'Tilstand', 'Påkrævede', 'Genstande som i filteret ledes væk, nedad eller til siden; alle andre går ligeud', 'Forbudte', 'Genstande som i filteret går ligeud; alle andre ledes væk, nedad eller til siden', 'Jernkiste', '54 pladser i én blok, lige så mange som en dobbeltkiste', 'Stålkiste', '78 pladser i én blok', 'Rustfri stålkiste', '108 pladser i én blok, dobbelt så mange som en dobbeltkiste', 'Gummitransportbånd', "Thomas Robins' gummibelagte bånd bærer kul og malm i kilometervis (1891): det hurtigere gummibånd, ståltragten der kan følge med, og båndsortereren der leder valgte genstande væk", 'Samlebånd', 'Fords samlebånd bringer arbejdet til arbejderen på ruller og kæder (1913): rullebanen og den rustfri ståltragt'],
}
for _code, _vals in _T.items():
    assert len(_vals) == len(_K), (_code, len(_vals), len(_K))


def translations(code):
    """English -> translation for these strings in one language."""
    return dict(zip(_K, _T.get(code, [])))


def _dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def _img(fill=(0, 0, 0, 0), h=16):
    return Image.new("RGBA", (16, h), fill)


def _noise(base, var, seed, h=16):
    rng = random.Random(seed)
    img = _img(h=h)
    px = img.load()
    for y in range(h):
        for x in range(16):
            d = rng.randint(-var, var)
            px[x, y] = tuple(max(0, min(255, c + d)) for c in base) + (255,)
    return img


def _rect(img, x0, y0, x1, y1, c):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = tuple(c[:3]) + (255,)


# --- Belts -----------------------------------------------------------------------------------------------------

BELT_LOOK = {  # surface colour, stripe colour, frame colour
    "canvas_belt": ((196, 170, 120), (150, 124, 82), (120, 86, 50)),
    "rubber_belt": ((46, 46, 50), (96, 96, 102), (150, 150, 158)),
    "roller_conveyor": ((176, 182, 190), (110, 116, 126), (70, 76, 86)),
}


def belt_surface(bid):
    """One still picture of the moving surface; rows repeat every 8 pixels so it can scroll."""
    surf, stripe, frame = BELT_LOOK[bid]
    img = _noise(surf, 6, sum(map(ord, bid)))
    px = img.load()
    for y in range(16):
        for x in range(2, 14):
            phase = y % 8
            if bid == "roller_conveyor":
                # Rollers: a light top and a dark gap, every 4 pixels.
                c = surf if y % 4 in (0, 1) else stripe if y % 4 == 2 else (60, 64, 72)
                px[x, y] = tuple(c) + (255,)
            elif bid == "rubber_belt":
                # Chevrons pointing forward (towards the top of the texture).
                if phase == (abs(x - 7.5) // 1.5) % 8 or phase == (abs(x - 7.5) // 1.5 + 1) % 8:
                    px[x, y] = tuple(stripe) + (255,)
            else:
                # Canvas: woven cross bands.
                if phase in (0, 1):
                    px[x, y] = tuple(stripe) + (255,)
    # Side rails stay still in every frame (drawn after scrolling).
    return img


def belt_top_frames(bid, shift):
    surf, stripe, frame = BELT_LOOK[bid]
    base = belt_surface(bid)
    frames = 8 // shift if shift < 8 else 1
    sheet = _img(h=16 * frames)
    for k in range(frames):
        f = _img()
        for y in range(16):
            row = base.crop((0, (y + k * shift) % 16, 16, (y + k * shift) % 16 + 1))
            f.paste(row, (0, y))
        _rect(f, 0, 0, 1, 15, frame)
        _rect(f, 14, 0, 15, 15, frame)
        px = f.load()
        for y in range(16):
            px[1, y] = tuple(min(255, c + 30) for c in frame) + (255,)
            px[14, y] = tuple(max(0, c - 25) for c in frame) + (255,)
        sheet.paste(f, (0, 16 * k))
    return sheet, frames


def belt_side(bid):
    surf, stripe, frame = BELT_LOOK[bid]
    img = _noise(frame, 8, len(bid))
    _rect(img, 0, 12, 15, 12, tuple(min(255, c + 35) for c in frame))
    _rect(img, 0, 15, 15, 15, tuple(max(0, c - 35) for c in frame))
    for x in (2, 13):  # bolts / roller ends
        _rect(img, x, 13, x, 14, (40, 40, 44) if bid != "canvas_belt" else (90, 60, 34))
    if bid == "roller_conveyor":
        for x in range(1, 16, 4):
            _rect(img, x, 13, x + 1, 14, (200, 205, 212))
    return img


def belt_item(bid):
    """Flat inventory picture: the belt seen from above at an angle, with an arrow."""
    top, _ = belt_top_frames(bid, 8)
    img = _img()
    surf, stripe, frame = BELT_LOOK[bid]
    face = top.crop((0, 0, 16, 16)).resize((16, 9), Image.NEAREST)
    img.paste(face, (0, 3))
    _rect(img, 0, 12, 15, 14, frame)
    _rect(img, 0, 14, 15, 14, tuple(max(0, c - 40) for c in frame))
    px = img.load()
    for i, (x0, x1) in enumerate([(7, 8), (6, 9), (5, 10)]):
        for x in range(x0, x1 + 1):
            px[x, 5 + i] = (236, 200, 60, 255)
    _rect(img, 7, 8, 8, 10, (236, 200, 60))
    return img


# --- Sorter ----------------------------------------------------------------------------------------------------

def sorter_textures():
    steel = (150, 156, 164)
    side = _noise(steel, 8, 31)
    _rect(side, 0, 0, 15, 0, (190, 196, 204))
    _rect(side, 0, 15, 15, 15, (90, 96, 104))
    _rect(side, 4, 6, 11, 11, (40, 42, 48))     # side outlet
    _rect(side, 4, 6, 11, 6, (90, 96, 104))
    front = side.copy()
    _rect(front, 3, 5, 12, 12, (30, 32, 36))    # straight-on outlet
    _rect(front, 3, 5, 12, 5, (90, 96, 104))
    _rect(front, 4, 11, 11, 11, (60, 62, 68))
    top = _noise(steel, 6, 37)
    _rect(top, 0, 0, 15, 0, (190, 196, 204))
    _rect(top, 2, 3, 13, 13, (60, 64, 70))
    for gx in range(3):  # the 3 x 3 filter window
        for gy in range(3):
            _rect(top, 3 + gx * 4, 4 + gy * 3, 4 + gx * 4, 5 + gy * 3, (120, 170, 210))
    px = top.load()
    for i, (x0, x1) in enumerate([(7, 8), (6, 9)]):  # arrow to the front (texture top = north = front)
        for x in range(x0, x1 + 1):
            px[x, 1 + i] = (236, 200, 60, 255)
    bottom = _noise((110, 116, 124), 6, 41)
    _rect(bottom, 4, 4, 11, 11, (30, 32, 36))   # chute down
    # The mode lamp in the back corners (3.40.1): green for "required", red for "forbidden".
    top_forbidden = top.copy()
    for img, lamp in ((top, (70, 210, 90)), (top_forbidden, (225, 60, 50))):
        _rect(img, 1, 14, 2, 15, lamp)
        _rect(img, 13, 14, 14, 15, lamp)
    return {"belt_sorter_side": side, "belt_sorter_front": front, "belt_sorter_top": top,
            "belt_sorter_top_forbidden": top_forbidden, "belt_sorter_bottom": bottom}


# --- Hoppers ---------------------------------------------------------------------------------------------------

HOPPER_LOOK = {"steel_hopper": (112, 118, 128), "stainless_hopper": (184, 190, 198)}


def hopper_textures(hid):
    c = HOPPER_LOOK[hid]
    outside = _noise(c, 7, len(hid) * 13)
    _rect(outside, 0, 0, 15, 0, tuple(min(255, v + 40) for v in c))
    _rect(outside, 0, 15, 15, 15, tuple(max(0, v - 40) for v in c))
    for x in (1, 14):
        for y in (1, 14):
            _rect(outside, x, y, x, y, tuple(max(0, v - 55) for v in c))  # rivets
    inside = _noise(tuple(max(0, v - 45) for v in c), 6, len(hid) * 17)
    top = _img()
    rim = tuple(min(255, v + 15) for v in c)
    _rect(top, 0, 0, 15, 15, rim)
    _rect(top, 2, 2, 13, 13, (0, 0, 0))
    top.putalpha(255)
    px = top.load()
    for y in range(2, 14):
        for x in range(2, 14):
            px[x, y] = (0, 0, 0, 0)
    _rect(top, 0, 0, 15, 0, tuple(min(255, v + 45) for v in c))
    return {f"{hid}_outside": outside, f"{hid}_inside": inside, f"{hid}_top": top}


# --- Chests ----------------------------------------------------------------------------------------------------

CHEST_LOOK = {"iron_chest": (166, 166, 170), "steel_chest": (104, 110, 120), "stainless_chest": (200, 206, 214)}


def chest_textures(cid):
    c = CHEST_LOOK[cid]
    lite = tuple(min(255, v + 40) for v in c)
    dark = tuple(max(0, v - 50) for v in c)
    side = _noise(c, 6, len(cid) * 7)
    _rect(side, 0, 0, 15, 0, lite)
    _rect(side, 0, 0, 0, 15, lite)
    _rect(side, 0, 15, 15, 15, dark)
    _rect(side, 15, 0, 15, 15, dark)
    _rect(side, 1, 5, 14, 5, dark)          # lid seam
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        _rect(side, x, y, x, y, dark)
    front = side.copy()
    _rect(front, 6, 4, 9, 8, (60, 60, 64))  # latch
    _rect(front, 7, 5, 8, 7, (220, 186, 80))
    top = _noise(c, 6, len(cid) * 11)
    _rect(top, 0, 0, 15, 0, lite)
    _rect(top, 0, 0, 0, 15, lite)
    _rect(top, 0, 15, 15, 15, dark)
    _rect(top, 15, 0, 15, 15, dark)
    _rect(top, 3, 3, 12, 3, dark)
    _rect(top, 3, 12, 12, 12, dark)
    _rect(top, 3, 3, 3, 12, dark)
    _rect(top, 12, 3, 12, 12, dark)
    return {f"{cid}_side": side, f"{cid}_front": front, f"{cid}_top": top}


# --- Recipes ---------------------------------------------------------------------------------------------------

def _shaped(pattern, key, result, count=1):
    return {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": pattern,
            "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in key.items()},
            "result": {"id": f"periodic_elements:{result}", "count": count}}


RECIPES = {
    "canvas_belt": _shaped(["LLL", "IPI"], {"L": "minecraft:leather", "I": "minecraft:iron_ingot", "P": "#minecraft:planks"}, "canvas_belt", 6),
    "rubber_belt": _shaped(["RRR", "SBS"], {"R": "#periodic_elements:rubbers", "S": "periodic_elements:steel_ingot",
                                            "B": "periodic_elements:canvas_belt"}, "rubber_belt", 6),
    "roller_conveyor": _shaped(["SSS", "DBD"], {"S": "periodic_elements:stainless_steel_ingot", "D": "minecraft:redstone",
                                                "B": "periodic_elements:rubber_belt"}, "roller_conveyor", 6),
    "belt_sorter": _shaped([" C ", "SBS", " H "], {"C": "minecraft:comparator", "S": "periodic_elements:steel_ingot",
                                                  "B": "periodic_elements:canvas_belt", "H": "minecraft:hopper"}, "belt_sorter"),
    "steel_hopper": _shaped(["S S", "SHS", " S "], {"S": "periodic_elements:steel_ingot", "H": "minecraft:hopper"}, "steel_hopper"),
    "stainless_hopper": _shaped(["S S", "SHS", " S "], {"S": "periodic_elements:stainless_steel_ingot",
                                                        "H": "periodic_elements:steel_hopper"}, "stainless_hopper"),
    "iron_chest": _shaped(["III", "ICI", "III"], {"I": "minecraft:iron_ingot", "C": "minecraft:chest"}, "iron_chest"),
    "steel_chest": _shaped(["SSS", "SCS", "SSS"], {"S": "periodic_elements:steel_ingot", "C": "periodic_elements:iron_chest"}, "steel_chest"),
    "stainless_chest": _shaped(["SSS", "SCS", "SSS"], {"S": "periodic_elements:stainless_steel_ingot",
                                                       "C": "periodic_elements:steel_chest"}, "stainless_chest"),
}


def generate_logistics(assets, data_root, lang, mod_id):
    tex_b = assets / "textures" / "block"
    tex_i = assets / "textures" / "item"
    models = assets / "models"
    states = assets / "blockstates"
    data_dir = data_root / mod_id

    def m(name):
        return f"{mod_id}:block/{name}"

    facing4 = {"north": 0, "east": 90, "south": 180, "west": 270}

    for bid, (_, _, _, shift) in BELTS.items():
        sheet, frames = belt_top_frames(bid, shift)
        sheet.save(tex_b / f"{bid}_top.png")
        if frames > 1:
            (tex_b / f"{bid}_top.png.mcmeta").write_text(json.dumps({"animation": {"frametime": 1}}) + "\n", encoding="utf-8")
        belt_side(bid).save(tex_b / f"{bid}_side.png")
        belt_item(bid).save(tex_i / f"{bid}.png")
        side = m(f"{bid}_side")
        face = {"texture": "#side", "uv": [0, 12, 16, 16]}
        _dump(models / "block" / f"{bid}.json", {
            "parent": "minecraft:block/block",
            "textures": {"particle": side, "top": m(f"{bid}_top"), "side": side},
            "elements": [{"from": [0, 0, 0], "to": [16, 4, 16], "faces": {
                "up": {"texture": "#top", "uv": [0, 0, 16, 16]},
                "down": {"texture": "#side", "uv": [0, 0, 16, 16], "cullface": "down"},
                "north": face, "south": face, "east": face, "west": face}}]})
        _dump(states / f"{bid}.json", {"variants": {f"facing={f}": ({"model": m(bid), "y": y} if y else {"model": m(bid)})
                                                    for f, y in facing4.items()}})
        _dump(models / "item" / f"{bid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{mod_id}:item/{bid}"}})

    for name, img in sorter_textures().items():
        img.save(tex_b / f"{name}.png")
    _dump(models / "block" / f"{SORTER}.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "top": m("belt_sorter_top"), "front": m("belt_sorter_front"), "side": m("belt_sorter_side"),
        "bottom": m("belt_sorter_bottom")}})
    _dump(models / "block" / f"{SORTER}_forbidden.json", {"parent": "minecraft:block/orientable_with_bottom", "textures": {
        "top": m("belt_sorter_top_forbidden"), "front": m("belt_sorter_front"), "side": m("belt_sorter_side"),
        "bottom": m("belt_sorter_bottom")}})
    _dump(states / f"{SORTER}.json", {"variants": {
        f"facing={f},forbidden={fb}": {"model": m(SORTER + ("_forbidden" if fb == "true" else "")), **({"y": y} if y else {})}
        for f, y in facing4.items() for fb in ("false", "true")}})
    _dump(models / "item" / f"{SORTER}.json", {"parent": m(SORTER)})

    for hid in HOPPERS:
        for name, img in hopper_textures(hid).items():
            img.save(tex_b / f"{name}.png")
        tx = {"particle": m(f"{hid}_outside"), "top": m(f"{hid}_top"), "side": m(f"{hid}_outside"), "inside": m(f"{hid}_inside")}
        _dump(models / "block" / f"{hid}.json", {"parent": "minecraft:block/hopper", "textures": tx})
        _dump(models / "block" / f"{hid}_side.json", {"parent": "minecraft:block/hopper_side", "textures": tx})
        variants = {}
        for enabled in ("true", "false"):
            variants[f"enabled={enabled},facing=down"] = {"model": m(hid)}
            for f, y in facing4.items():
                variants[f"enabled={enabled},facing={f}"] = {"model": m(f"{hid}_side"), **({"y": y} if y else {})}
        _dump(states / f"{hid}.json", {"variants": variants})
        _dump(models / "item" / f"{hid}.json", {"parent": m(hid)})

    for cid in CHESTS:
        for name, img in chest_textures(cid).items():
            img.save(tex_b / f"{name}.png")
        _dump(models / "block" / f"{cid}.json", {"parent": "minecraft:block/orientable", "textures": {
            "top": m(f"{cid}_top"), "front": m(f"{cid}_front"), "side": m(f"{cid}_side")}})
        _dump(states / f"{cid}.json", {"variants": {f"facing={f}": ({"model": m(cid), "y": y} if y else {"model": m(cid)})
                                                    for f, y in facing4.items()}})
        _dump(models / "item" / f"{cid}.json", {"parent": m(cid)})

    blocks = list(BELTS) + [SORTER] + list(HOPPERS) + list(CHESTS)
    for b in blocks:
        _dump(data_dir / "loot_table" / "blocks" / f"{b}.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{mod_id}:{b}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for rid, recipe in RECIPES.items():
        _dump(data_dir / "recipe" / f"{rid}.json", recipe)
    _dump(data_dir / "tags" / "item" / "rubbers.json",
          {"values": [f"{mod_id}:vulcanized_rubber", f"{mod_id}:buna_rubber"]})
    for tag, ids in {"mineable/pickaxe": [b for b in blocks if b != "canvas_belt"], "mineable/axe": ["canvas_belt"]}.items():
        path = data_root / "minecraft" / "tags" / "block" / f"{tag}.json"
        values = json.loads(path.read_text(encoding="utf-8"))["values"] if path.exists() else []
        values += [f"{mod_id}:{i}" for i in ids if f"{mod_id}:{i}" not in values]
        _dump(path, {"replace": False, "values": values})
    lang.update(LANG)
    return blocks
