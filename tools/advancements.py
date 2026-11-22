"""The "Elementar" advancement tab (3.60.0, Guilherme): a progression map of every research project.

Layout (vanilla places each advancement to the right of its parent):
  root "Elementar" (granted on the first tick in a world; reward: a Field Guide, so every player starts with one)
    -> Charcoal Pit, Charcoal, and Potter's and Smith's Workshop (level 1 station) -> level 1 projects ...
  Each later station hangs from the deepest key project of the level before it, and each project hangs from the
  deepest project of its own level that it requires, or from its level's station. Every requirement is still listed
  in the description.

Station and starter nodes are earned by holding the item (vanilla inventory_changed). Project nodes use the
"minecraft:impossible" criterion "done": ResearchAdvancements.java grants and revokes it to match the player's
research every time their research is synced (join, research, treatise, /research, Book of Progress).
Key projects show a toast and a chat line; optional ones are earned silently, so a world that already has
research does not flood the screen on its first load with this version.

generate.py calls generate_advancements() and merges LANG / translations(code); FIRST_STEPS goes on top of the
Field Guide's first chapter (guidebook.py).
"""
import json
import shutil
from pathlib import Path

M = "periodic_elements"
TAB = "elementar"

LANG = {
    f"advancement.{M}.root": "Elementar",
    f"advancement.{M}.root.desc": "From the Stone Age to the last elements. Research grows this tree; your Field Guide explains the rest.",
    f"advancement.{M}.charcoal_pit.desc": "Craft a Charcoal Pit: 8 dirt around a log. It burns logs into charcoal with no research at all.",
    f"advancement.{M}.charcoal.desc": "Burn logs into charcoal in a furnace or a Charcoal Pit. Early machines and research need a lot of it.",
    f"advancement.{M}.workshop.desc": "Craft the first research station: brick, raw copper, brick / charcoal, crafting table, charcoal / 3 cobblestone. Then research Fired Pottery in it.",
    f"advancement.{M}.station.desc": "Research station for level %s. You can place it once you reach tech level %s.",
    f"advancement.{M}.key": "★ Key project: raises your tech level",
    f"advancement.{M}.needs": "Needs: %s",
}

_K = list(LANG)

_T = {
    "pt_br": ["Elementar",
              "Da Idade da Pedra aos últimos elementos. A pesquisa faz esta árvore crescer; o seu Guia de Campo explica o resto.",
              "Faça uma Carvoaria: 8 terras em volta de uma tora. Ela queima toras em carvão vegetal sem pesquisa nenhuma.",
              "Queime toras em carvão vegetal numa fornalha ou numa Carvoaria. As primeiras máquinas e pesquisas usam muito carvão.",
              "Faça a primeira estação de pesquisa: tijolo, cobre bruto, tijolo / carvão vegetal, bancada de trabalho, carvão vegetal / 3 pedregulhos. Depois pesquise Cerâmica Queimada nela.",
              "Estação de pesquisa do nível %s. Você pode colocá-la quando chegar ao nível tecnológico %s.",
              "★ Projeto chave: sobe o seu nível tecnológico",
              "Precisa de: %s"],
    "es_es": ["Elementar",
              "De la Edad de Piedra a los últimos elementos. La investigación hace crecer este árbol; tu Guía de campo explica el resto.",
              "Fabrica una Carbonera: 8 de tierra alrededor de un tronco. Convierte troncos en carbón vegetal sin ninguna investigación.",
              "Quema troncos para hacer carbón vegetal en un horno o en una Carbonera. Las primeras máquinas e investigaciones necesitan mucho.",
              "Fabrica la primera estación de investigación: ladrillo, cobre en bruto, ladrillo / carbón vegetal, mesa de trabajo, carbón vegetal / 3 rocas. Luego investiga Cerámica cocida en ella.",
              "Estación de investigación del nivel %s. Puedes colocarla cuando llegues al nivel tecnológico %s.",
              "★ Proyecto clave: sube tu nivel tecnológico",
              "Necesita: %s"],
    "fr_fr": ["Elementar",
              "De l'âge de pierre aux derniers éléments. La recherche fait pousser cet arbre ; votre Guide de terrain explique le reste.",
              "Fabriquez une Meule de charbonnier : 8 terre autour d'une bûche. Elle change les bûches en charbon de bois sans aucune recherche.",
              "Brûlez des bûches en charbon de bois dans un fourneau ou une Meule de charbonnier. Les premières machines et recherches en demandent beaucoup.",
              "Fabriquez la première station de recherche : brique, cuivre brut, brique / charbon de bois, établi, charbon de bois / 3 pierres. Recherchez-y ensuite Poterie cuite.",
              "Station de recherche du niveau %s. Vous pouvez la poser une fois au niveau technologique %s.",
              "★ Projet clé : fait monter votre niveau technologique",
              "Nécessite : %s"],
    "de_de": ["Elementar",
              "Von der Steinzeit bis zu den letzten Elementen. Forschung lässt diesen Baum wachsen; dein Feldhandbuch erklärt den Rest.",
              "Stelle einen Kohlenmeiler her: 8 Erde um einen Stamm. Er brennt Stämme ganz ohne Forschung zu Holzkohle.",
              "Brenne Stämme in einem Ofen oder Kohlenmeiler zu Holzkohle. Die ersten Maschinen und Forschungen brauchen viel davon.",
              "Stelle die erste Forschungsstation her: Ziegel, Rohkupfer, Ziegel / Holzkohle, Werkbank, Holzkohle / 3 Bruchstein. Erforsche darin zuerst Gebrannte Keramik.",
              "Forschungsstation für Stufe %s. Du kannst sie ab Technikstufe %s aufstellen.",
              "★ Schlüsselprojekt: hebt deine Technikstufe",
              "Benötigt: %s"],
    "it_it": ["Elementar",
              "Dall'età della pietra agli ultimi elementi. La ricerca fa crescere questo albero; la tua Guida da campo spiega il resto.",
              "Crea una Carbonaia: 8 terra attorno a un tronco. Trasforma i tronchi in carbone vegetale senza alcuna ricerca.",
              "Brucia tronchi per ottenere carbone vegetale in una fornace o in una Carbonaia. Le prime macchine e ricerche ne usano molto.",
              "Crea la prima stazione di ricerca: mattone, rame grezzo, mattone / carbone vegetale, banco da lavoro, carbone vegetale / 3 pietrisco. Poi ricerca Ceramica cotta al suo interno.",
              "Stazione di ricerca del livello %s. Puoi piazzarla quando raggiungi il livello tecnologico %s.",
              "★ Progetto chiave: alza il tuo livello tecnologico",
              "Richiede: %s"],
    "nl_nl": ["Elementar",
              "Van de steentijd tot de laatste elementen. Onderzoek laat deze boom groeien; je Veldgids legt de rest uit.",
              "Maak een Kolenmeiler: 8 aarde rond een stam. Hij brandt stammen tot houtskool, zonder enig onderzoek.",
              "Brand stammen tot houtskool in een oven of een Kolenmeiler. De eerste machines en onderzoeken hebben er veel van nodig.",
              "Maak het eerste onderzoeksstation: baksteen, ruw koper, baksteen / houtskool, werkbank, houtskool / 3 keien. Onderzoek daarna Gebakken aardewerk erin.",
              "Onderzoeksstation voor niveau %s. Je kunt het plaatsen zodra je technologieniveau %s bereikt.",
              "★ Sleutelproject: verhoogt je technologieniveau",
              "Vereist: %s"],
    "sv_se": ["Elementar",
              "Från stenåldern till de sista grundämnena. Forskning får trädet att växa; din Fälthandbok förklarar resten.",
              "Tillverka en Kolmila: 8 jord runt en stock. Den bränner stockar till träkol utan någon forskning.",
              "Bränn stockar till träkol i en ugn eller en Kolmila. De första maskinerna och forskningsprojekten behöver mycket av det.",
              "Tillverka den första forskningsstationen: tegelsten, råkoppar, tegelsten / träkol, arbetsbänk, träkol / 3 kullersten. Forska sedan om Bränd keramik i den.",
              "Forskningsstation för nivå %s. Du kan placera den när du når teknologinivå %s.",
              "★ Nyckelprojekt: höjer din teknologinivå",
              "Kräver: %s"],
    "da_dk": ["Elementar",
              "Fra stenalderen til de sidste grundstoffer. Forskning får træet til at vokse; din Felthåndbog forklarer resten.",
              "Lav en Kulmile: 8 jord omkring en træstamme. Den brænder stammer til trækul uden nogen forskning.",
              "Brænd stammer til trækul i en ovn eller en Kulmile. De første maskiner og forskningsprojekter bruger meget af det.",
              "Lav den første forskningsstation: mursten, råt kobber, mursten / trækul, arbejdsbord, trækul / 3 brosten. Forsk derefter i Brændt keramik i den.",
              "Forskningsstation for niveau %s. Du kan placere den, når du når teknologiniveau %s.",
              "★ Nøgleprojekt: hæver dit teknologiniveau",
              "Kræver: %s"],
    "ru_ru": ["Elementar",
              "От каменного века до последних элементов. Исследования растят это дерево; Полевой справочник объясняет остальное.",
              "Сделайте углежогную яму: 8 земли вокруг бревна. Она пережигает брёвна в древесный уголь без всяких исследований.",
              "Пережгите брёвна в древесный уголь в печи или в углежогной яме. Первым машинам и исследованиям его нужно много.",
              "Сделайте первую исследовательскую станцию: кирпич, рудная медь, кирпич / древесный уголь, верстак, древесный уголь / 3 булыжника. Затем исследуйте в ней Обожжённую керамику.",
              "Исследовательская станция уровня %s. Её можно поставить, когда вы достигнете технологического уровня %s.",
              "★ Ключевой проект: повышает ваш технологический уровень",
              "Требует: %s"],
}

# Paragraphs put on top of the Field Guide's first chapter ("@0 " keeps them visible from the start).
FIRST_STEPS = {
    "en": [
        "# First steps",
        "@0 1. Charcoal. Burn logs in a furnace, or craft a Charcoal Pit (8 dirt around a log) and burn them there.",
        "@0 2. Bricks and copper. Cook clay balls into bricks in a furnace and mine some copper ore. Furnaces no longer "
        "smelt metal ores, so keep the raw copper.",
        "@0 3. Craft the Potter's and Smith's Workshop: brick, raw copper, brick / charcoal, crafting table, charcoal / "
        "3 cobblestone. It is the research station for level 1.",
        "@0 4. In the Workshop, research Fired Pottery first (8 clay balls, 4 charcoal). Then do the key projects, marked "
        "with a star: Copper Smelting, Lime Burning, Bronze Alloying and Iron Smelting.",
        "@0 5. Press L and open the Elementar tab: it shows every research project as a tree, and what comes next.",
    ],
    "pt_br": [
        "# Primeiros passos",
        "@0 1. Carvão vegetal. Queime toras numa fornalha, ou faça uma Carvoaria (8 terras em volta de uma tora) e queime "
        "as toras nela.",
        "@0 2. Tijolos e cobre. Asse bolas de argila em tijolos numa fornalha e minere um pouco de minério de cobre. A "
        "fornalha não funde mais minérios de metal, então guarde o cobre bruto.",
        "@0 3. Faça a Oficina do Oleiro e do Ferreiro: tijolo, cobre bruto, tijolo / carvão vegetal, bancada de trabalho, "
        "carvão vegetal / 3 pedregulhos. Ela é a estação de pesquisa do nível 1.",
        "@0 4. Na Oficina, pesquise primeiro Cerâmica Queimada (8 bolas de argila, 4 carvões vegetais). Depois faça os "
        "projetos chave, marcados com estrela: Fundição de Cobre, Queima de Cal, Liga de Bronze e Fundição de Ferro.",
        "@0 5. Aperte L e abra a aba Elementar: ela mostra todos os projetos de pesquisa como uma árvore, e o que vem "
        "depois.",
    ],
    "es_es": [
        "# Primeros pasos",
        "@0 1. Carbón vegetal. Quema troncos en un horno, o fabrica una Carbonera (8 de tierra alrededor de un tronco) y "
        "quémalos en ella.",
        "@0 2. Ladrillos y cobre. Cuece bolas de arcilla en un horno para hacer ladrillos y extrae algo de mena de cobre. "
        "Los hornos ya no funden menas de metal, así que guarda el cobre en bruto.",
        "@0 3. Fabrica el Taller del alfarero y del herrero: ladrillo, cobre en bruto, ladrillo / carbón vegetal, mesa de "
        "trabajo, carbón vegetal / 3 rocas. Es la estación de investigación del nivel 1.",
        "@0 4. En el Taller, investiga primero Cerámica cocida (8 bolas de arcilla, 4 de carbón vegetal). Después haz los "
        "proyectos clave, marcados con una estrella: Fundición del cobre, Calcinación de la cal, Aleación del bronce y "
        "Fundición del hierro.",
        "@0 5. Pulsa L y abre la pestaña Elementar: muestra todos los proyectos de investigación como un árbol, y lo que "
        "viene después.",
    ],
    "fr_fr": [
        "# Premiers pas",
        "@0 1. Charbon de bois. Brûlez des bûches dans un fourneau, ou fabriquez une Meule de charbonnier (8 terre autour "
        "d'une bûche) et brûlez-les dedans.",
        "@0 2. Briques et cuivre. Cuisez des boules d'argile en briques dans un fourneau et minez du minerai de cuivre. "
        "Les fourneaux ne fondent plus les minerais de métal : gardez le cuivre brut.",
        "@0 3. Fabriquez l'Atelier du potier et du forgeron : brique, cuivre brut, brique / charbon de bois, établi, "
        "charbon de bois / 3 pierres. C'est la station de recherche du niveau 1.",
        "@0 4. Dans l'Atelier, recherchez d'abord Poterie cuite (8 boules d'argile, 4 charbons de bois). Faites ensuite "
        "les projets clés, marqués d'une étoile : Fonte du cuivre, Cuisson de la chaux, Alliage du bronze et Fonte du fer.",
        "@0 5. Appuyez sur L et ouvrez l'onglet Elementar : il montre tous les projets de recherche sous forme d'arbre, et "
        "ce qui vient ensuite.",
    ],
    "de_de": [
        "# Erste Schritte",
        "@0 1. Holzkohle. Brenne Stämme in einem Ofen, oder stelle einen Kohlenmeiler her (8 Erde um einen Stamm) und "
        "brenne sie darin.",
        "@0 2. Ziegel und Kupfer. Brenne Tonklumpen im Ofen zu Ziegeln und baue etwas Kupfererz ab. Öfen schmelzen keine "
        "Metallerze mehr, also behalte das Rohkupfer.",
        "@0 3. Stelle die Werkstatt des Töpfers und Schmieds her: Ziegel, Rohkupfer, Ziegel / Holzkohle, Werkbank, "
        "Holzkohle / 3 Bruchstein. Sie ist die Forschungsstation für Stufe 1.",
        "@0 4. Erforsche in der Werkstatt zuerst Gebrannte Keramik (8 Tonklumpen, 4 Holzkohle). Dann die mit einem Stern "
        "markierten Schlüsselprojekte: Kupferverhüttung, Kalkbrennen, Bronzelegierung und Eisenverhüttung.",
        "@0 5. Drücke L und öffne den Reiter Elementar: Er zeigt alle Forschungsprojekte als Baum, und was als Nächstes "
        "kommt.",
    ],
    "it_it": [
        "# Primi passi",
        "@0 1. Carbone vegetale. Brucia tronchi in una fornace, oppure crea una Carbonaia (8 terra attorno a un tronco) e "
        "bruciali lì.",
        "@0 2. Mattoni e rame. Cuoci palle d'argilla in mattoni in una fornace e scava un po' di minerale di rame. Le "
        "fornaci non fondono più i minerali di metallo, quindi tieni il rame grezzo.",
        "@0 3. Crea la Bottega del vasaio e del fabbro: mattone, rame grezzo, mattone / carbone vegetale, banco da lavoro, "
        "carbone vegetale / 3 pietrisco. È la stazione di ricerca del livello 1.",
        "@0 4. Nella Bottega, ricerca prima Ceramica cotta (8 palle d'argilla, 4 carbone vegetale). Poi i progetti chiave, "
        "segnati da una stella: Fusione del rame, Cottura della calce, Lega di bronzo e Fusione del ferro.",
        "@0 5. Premi L e apri la scheda Elementar: mostra tutti i progetti di ricerca come un albero, e cosa viene dopo.",
    ],
    "nl_nl": [
        "# Eerste stappen",
        "@0 1. Houtskool. Brand stammen in een oven, of maak een Kolenmeiler (8 aarde rond een stam) en brand ze daarin.",
        "@0 2. Bakstenen en koper. Bak kleiballen tot bakstenen in een oven en hak wat kopererts. Ovens smelten geen "
        "metaalerts meer, dus bewaar het ruwe koper.",
        "@0 3. Maak de Werkplaats van pottenbakker en smid: baksteen, ruw koper, baksteen / houtskool, werkbank, houtskool "
        "/ 3 keien. Het is het onderzoeksstation voor niveau 1.",
        "@0 4. Onderzoek in de Werkplaats eerst Gebakken aardewerk (8 kleiballen, 4 houtskool). Doe daarna de "
        "sleutelprojecten, met een ster gemarkeerd: Kopersmelten, Kalkbranden, Bronslegering en IJzersmelten.",
        "@0 5. Druk op L en open het tabblad Elementar: het toont alle onderzoeksprojecten als een boom, en wat daarna "
        "komt.",
    ],
    "sv_se": [
        "# Första stegen",
        "@0 1. Träkol. Bränn stockar i en ugn, eller tillverka en Kolmila (8 jord runt en stock) och bränn dem där.",
        "@0 2. Tegel och koppar. Bränn lerklumpar till tegelstenar i en ugn och bryt lite kopparmalm. Ugnar smälter inte "
        "längre metallmalm, så spara råkopparn.",
        "@0 3. Tillverka Krukmakarens och smedens verkstad: tegelsten, råkoppar, tegelsten / träkol, arbetsbänk, träkol / "
        "3 kullersten. Den är forskningsstationen för nivå 1.",
        "@0 4. Forska först om Bränd keramik i verkstaden (8 lerklumpar, 4 träkol). Gör sedan nyckelprojekten, märkta med "
        "en stjärna: Koppartillverkning, Kalkbränning, Bronslegering och Järnframställning.",
        "@0 5. Tryck på L och öppna fliken Elementar: den visar alla forskningsprojekt som ett träd, och vad som kommer "
        "sedan.",
    ],
    "da_dk": [
        "# Første skridt",
        "@0 1. Trækul. Brænd stammer i en ovn, eller lav en Kulmile (8 jord omkring en stamme) og brænd dem i den.",
        "@0 2. Mursten og kobber. Brænd lerklumper til mursten i en ovn og bryd lidt kobbermalm. Ovne smelter ikke længere "
        "metalmalm, så gem det rå kobber.",
        "@0 3. Lav Pottemagerens og smedens værksted: mursten, råt kobber, mursten / trækul, arbejdsbord, trækul / 3 "
        "brosten. Det er forskningsstationen for niveau 1.",
        "@0 4. Forsk først i Brændt keramik i værkstedet (8 lerklumper, 4 trækul). Lav derefter nøgleprojekterne, mærket "
        "med en stjerne: Kobbersmeltning, Kalkbrænding, Bronzelegering og Jernudvinding.",
        "@0 5. Tryk på L og åbn fanen Elementar: den viser alle forskningsprojekter som et træ, og hvad der kommer bagefter.",
    ],
    "ru_ru": [
        "# Первые шаги",
        "@0 1. Древесный уголь. Пережгите брёвна в печи или сделайте углежогную яму (8 земли вокруг бревна) и пережгите их "
        "в ней.",
        "@0 2. Кирпичи и медь. Обожгите комки глины в печи в кирпичи и добудьте немного медной руды. Печи больше не "
        "плавят металлические руды, так что сохраните рудную медь.",
        "@0 3. Сделайте Мастерскую гончара и кузнеца: кирпич, рудная медь, кирпич / древесный уголь, верстак, древесный "
        "уголь / 3 булыжника. Это исследовательская станция уровня 1.",
        "@0 4. В Мастерской сначала исследуйте Обожжённую керамику (8 комков глины, 4 древесного угля). Затем ключевые "
        "проекты, отмеченные звёздочкой: Выплавку меди, Обжиг извести, Сплавление бронзы и Выплавку железа.",
        "@0 5. Нажмите L и откройте вкладку Elementar: в ней все исследовательские проекты в виде дерева и то, что идёт "
        "дальше.",
    ],
}


def translations(code):
    """English -> translation for these strings in one language."""
    vals = _T.get(code, [])
    assert not vals or len(vals) == len(_K), (code, len(vals), len(_K))
    return {LANG[k]: t for k, t in zip(_K, vals)}


def _tr(key, *args):
    out = {"translate": key}
    if args:
        out["with"] = list(args)
    return out


def _icon(project, item_models):
    """First unlocked machine or item that exists, else the first sample."""
    for u in project["unlocks"]:
        kind, _, ident = u.partition(":")
        if kind in ("machine", "item") and ident in item_models:
            return f"{M}:{ident}"
    first = project["samples"][0]["item"].split("|")[0]
    return first


def _write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")


def _has_item(item):
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": item}]}}


def generate_advancements(data_dir, assets_dir, research, stations):
    """Writes data/<mod>/advancement/elementar/*.json and the starter loot table; returns the advancement count.

    research: {id: project} from research.py; stations: {station_id: (level, name, desc)} from machines.py."""
    item_models = {p.stem for p in (assets_dir / "models" / "item").glob("*.json")}
    out = data_dir / M / "advancement" / TAB
    if out.exists():
        shutil.rmtree(out)
    n = 0

    def adv(name, parent, icon, title, desc, frame="task", toast=True, chat=False, criteria=None, rewards=None,
            background=None):
        nonlocal n
        display = {"icon": {"id": icon}, "title": title, "description": desc, "frame": frame,
                   "show_toast": toast, "announce_to_chat": chat, "hidden": False}
        if background:
            display["background"] = background
        data = {}
        if parent:
            data["parent"] = f"{M}:{TAB}/{parent}"
        data["display"] = display
        data["criteria"] = criteria or {"done": {"trigger": "minecraft:impossible"}}
        if rewards:
            data["rewards"] = rewards
        _write(out / f"{name}.json", data)
        n += 1

    adv("root", None, f"{M}:field_guide", _tr(f"advancement.{M}.root"), _tr(f"advancement.{M}.root.desc"),
        toast=False, criteria={"start": {"trigger": "minecraft:tick"}},
        rewards={"loot": [f"{M}:advancement/starter_guide"]}, background="minecraft:textures/block/packed_mud.png")
    adv("charcoal_pit", "root", f"{M}:charcoal_pit", _tr(f"block.{M}.charcoal_pit"),
        _tr(f"advancement.{M}.charcoal_pit.desc"), criteria={"has": _has_item(f"{M}:charcoal_pit")})
    adv("charcoal", "root", "minecraft:charcoal", _tr("item.minecraft.charcoal"), _tr(f"advancement.{M}.charcoal.desc"),
        criteria={"has": _has_item("minecraft:charcoal")})

    # Depth of every project inside its own level (longest same-level chain), to pick parents.
    depth = {}

    def d(pid):
        if pid not in depth:
            p = research[pid]
            same = [r for r in p["requires"] if research[r]["level"] == p["level"]]
            depth[pid] = 1 + max((d(r) for r in same), default=0)
        return depth[pid]

    for pid in research:
        d(pid)

    station_of = {lvl: sid for sid, (lvl, _, _) in stations.items()}
    for lvl in sorted(station_of):
        sid = station_of[lvl]
        if lvl == 1:
            parent, desc = "root", _tr(f"advancement.{M}.workshop.desc")  # visible from the very start
        else:
            keys = [p for p in research.values() if p["key"] and p["level"] == lvl - 1]
            parent = "research/" + max(keys, key=lambda p: (depth[p["id"]], -list(research).index(p["id"])))["id"]
            desc = _tr(f"advancement.{M}.station.desc", str(lvl), str(lvl - 1))
        adv(f"station/{sid}", parent, f"{M}:{sid}", _tr(f"block.{M}.{sid}"), desc, frame="goal", chat=True,
            criteria={"has": _has_item(f"{M}:{sid}")})

    order = list(research)
    for pid in sorted(research, key=lambda x: (research[x]["level"], depth[x], order.index(x))):
        p = research[pid]
        same = [r for r in p["requires"] if research[r]["level"] == p["level"]]
        parent = ("research/" + max(same, key=lambda r: (depth[r], -order.index(r)))) if same \
            else f"station/{station_of[p['level']]}"
        parts = [_tr(f"research.{M}.{pid}.desc")]
        if p["key"]:
            parts += ["\n", dict(_tr(f"advancement.{M}.key"), color="gold")]
        if p["requires"]:
            names = []
            for i, r in enumerate(p["requires"]):
                if i:
                    names.append(", ")
                names.append(_tr(f"research.{M}.{r}"))
            parts += ["\n", dict(_tr(f"advancement.{M}.needs", {"text": "", "extra": names}), color="gray")]
        adv(f"research/{pid}", parent, _icon(p, item_models), _tr(f"research.{M}.{pid}"),
            {"text": "", "extra": parts}, frame="goal" if p["key"] else "task", toast=p["key"], chat=p["key"])

    _write(data_dir / M / "loot_table" / "advancement" / "starter_guide.json", {
        "type": "minecraft:advancement_reward",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{M}:field_guide"}]}],
    })
    return n
