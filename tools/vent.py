"""Relief valve on every machine (3.29.0): its strings and its Field Guide paragraphs (end of the Containers chapter).
Code in MachineBlockEntity (vent, emitOutputs), MachineScreenHandler.onButtonClick and MachineScreen.drawValve.

generate.py merges LANG into en_us and translations(code) into each language; guidebook.py appends GUIDE[code].
"""

LANG = {
    "gui.periodic_elements.machine.valve": "Relief valve",
    "gui.periodic_elements.machine.valve.open": "Open: gases with nowhere to go are vented",
    "gui.periodic_elements.machine.valve.shut": "Shut: the machine stops when a gas has nowhere to go",
    "gui.periodic_elements.machine.valve.hint": "Click to switch. Liquids always wait for flasks.",
}

_K = list(LANG)

# One list per language, in the order of LANG's keys.
_T = {
    "pt_br": ["Válvula de alívio", "Aberta: gases sem para onde ir são liberados",
              "Fechada: a máquina para quando um gás não tem para onde ir",
              "Clique para alternar. Líquidos sempre esperam por frascos."],
    "es_es": ["Válvula de alivio", "Abierta: los gases sin adónde ir se liberan",
              "Cerrada: la máquina se detiene cuando un gas no tiene adónde ir",
              "Haz clic para cambiar. Los líquidos siempre esperan frascos."],
    "fr_fr": ["Soupape de décharge", "Ouverte : les gaz sans issue sont relâchés",
              "Fermée : la machine s'arrête quand un gaz n'a nulle part où aller",
              "Cliquez pour basculer. Les liquides attendent toujours des fioles."],
    "de_de": ["Überdruckventil", "Offen: Gase ohne Behälter werden abgelassen",
              "Zu: die Maschine hält an, wenn ein Gas nirgendwohin kann",
              "Klicken zum Umschalten. Flüssigkeiten warten immer auf Kolben."],
    "it_it": ["Valvola di sfogo", "Aperta: i gas senza posto vengono liberati",
              "Chiusa: la macchina si ferma quando un gas non ha dove andare",
              "Clicca per cambiare. I liquidi aspettano sempre le fiaschette."],
    "nl_nl": ["Overdrukventiel", "Open: gassen zonder plek worden afgeblazen",
              "Dicht: de machine stopt als een gas nergens heen kan",
              "Klik om te wisselen. Vloeistoffen wachten altijd op kolven."],
    "ru_ru": ["Предохранительный клапан", "Открыт: газы, которым некуда деться, выпускаются",
              "Закрыт: машина останавливается, когда газу некуда деться",
              "Нажмите, чтобы переключить. Жидкости всегда ждут колб."],
    "sv_se": ["Säkerhetsventil", "Öppen: gaser utan plats släpps ut",
              "Stängd: maskinen stannar när en gas inte har någonstans att ta vägen",
              "Klicka för att växla. Vätskor väntar alltid på flaskor."],
    "da_dk": ["Sikkerhedsventil", "Åben: gasser uden plads lukkes ud",
              "Lukket: maskinen standser, når en gas ikke har nogen steder at gøre af sig",
              "Klik for at skifte. Væsker venter altid på kolber."],
}


def translations(code):
    """English -> translation for the valve strings in one language."""
    return {LANG[k]: t for k, t in zip(_K, _T.get(code, []))}


GUIDE = {
    "en_us": [
        "# Relief valve",
        "Under the upgrade slots of every machine's side panel is a relief valve. Shut (red, the default), the machine "
        "stops when a gas it makes has no empty flask or cylinder, or no room in the output slots. Click it open "
        "(green) and that gas is vented into the air instead, so the machine keeps working; gas that finds a "
        "container is still kept. Liquids always wait for flasks.",
    ],
    "pt_br": [
        "# Válvula de alívio",
        "Abaixo dos espaços de melhoria, no painel lateral de toda máquina, há uma válvula de alívio. Fechada "
        "(vermelha, o padrão), a máquina para quando um gás que ela produz não tem frasco ou cilindro vazio, ou não "
        "há espaço nos espaços de saída. Clique para abri-la (verde) e esse gás é liberado no ar, e a máquina segue "
        "trabalhando; o gás que encontra recipiente continua sendo guardado. Líquidos sempre esperam por frascos.",
    ],
    "es_es": [
        "# Válvula de alivio",
        "Bajo las ranuras de mejora del panel lateral de cada máquina hay una válvula de alivio. Cerrada (roja, por "
        "defecto), la máquina se detiene cuando un gas que produce no tiene frasco o cilindro vacío, o no hay sitio "
        "en las ranuras de salida. Haz clic para abrirla (verde) y ese gas se libera al aire, así la máquina sigue "
        "trabajando; el gas que encuentra recipiente se sigue guardando. Los líquidos siempre esperan frascos.",
    ],
    "fr_fr": [
        "# Soupape de décharge",
        "Sous les emplacements d'amélioration du panneau latéral de chaque machine se trouve une soupape de "
        "décharge. Fermée (rouge, par défaut), la machine s'arrête quand un gaz qu'elle produit n'a ni fiole ni "
        "bouteille vide, ou plus de place dans les emplacements de sortie. Cliquez pour l'ouvrir (vert) : ce gaz "
        "est relâché dans l'air et la machine continue ; le gaz qui trouve un récipient est toujours gardé. Les "
        "liquides attendent toujours des fioles.",
    ],
    "de_de": [
        "# Überdruckventil",
        "Unter den Upgrade-Plätzen im Seitenfeld jeder Maschine sitzt ein Überdruckventil. Geschlossen (rot, "
        "Standard) hält die Maschine an, wenn ein Gas, das sie erzeugt, keinen leeren Kolben oder Zylinder oder "
        "keinen Platz in den Ausgabeplätzen findet. Klicke es auf (grün), dann wird dieses Gas in die Luft "
        "abgelassen und die Maschine arbeitet weiter; Gas, das einen Behälter findet, wird weiter aufgefangen. "
        "Flüssigkeiten warten immer auf Kolben.",
    ],
    "it_it": [
        "# Valvola di sfogo",
        "Sotto gli slot di potenziamento del pannello laterale di ogni macchina c'è una valvola di sfogo. Chiusa "
        "(rossa, predefinita), la macchina si ferma quando un gas che produce non ha una fiaschetta o una bombola "
        "vuota, o non c'è posto negli slot di uscita. Clicca per aprirla (verde) e quel gas viene liberato "
        "nell'aria, così la macchina continua a lavorare; il gas che trova un contenitore viene ancora conservato. "
        "I liquidi aspettano sempre le fiaschette.",
    ],
    "nl_nl": [
        "# Overdrukventiel",
        "Onder de upgradevakken in het zijpaneel van elke machine zit een overdrukventiel. Dicht (rood, de "
        "standaard) stopt de machine als een gas dat ze maakt geen lege kolf of cilinder vindt, of geen plek in de "
        "uitvoervakken. Klik het open (groen) en dat gas wordt in de lucht afgeblazen, zodat de machine doorwerkt; "
        "gas dat een houder vindt, wordt nog steeds bewaard. Vloeistoffen wachten altijd op kolven.",
    ],
    "ru_ru": [
        "# Предохранительный клапан",
        "Под слотами улучшений на боковой панели каждой машины есть предохранительный клапан. Закрытый (красный, "
        "по умолчанию) — машина останавливается, когда для газа, который она производит, нет пустой колбы или "
        "баллона или нет места в выходных слотах. Нажмите, чтобы открыть его (зелёный), и этот газ выпускается в "
        "воздух, а машина продолжает работать; газ, которому нашлась ёмкость, по-прежнему сохраняется. Жидкости "
        "всегда ждут колб.",
    ],
    "sv_se": [
        "# Säkerhetsventil",
        "Under uppgraderingsplatserna i varje maskins sidopanel sitter en säkerhetsventil. Stängd (röd, standard) "
        "stannar maskinen när en gas den gör saknar tom flaska eller cylinder, eller plats i utdataplatserna. "
        "Klicka upp den (grön) så släpps gasen ut i luften och maskinen fortsätter arbeta; gas som hittar en "
        "behållare sparas fortfarande. Vätskor väntar alltid på flaskor.",
    ],
    "da_dk": [
        "# Sikkerhedsventil",
        "Under opgraderingspladserne i hver maskines sidepanel sidder en sikkerhedsventil. Lukket (rød, standard) "
        "standser maskinen, når en gas, den laver, mangler en tom kolbe eller cylinder eller plads i "
        "outputpladserne. Klik den åben (grøn), så lukkes gassen ud i luften, og maskinen arbejder videre; gas, "
        "der finder en beholder, gemmes stadig. Væsker venter altid på kolber.",
    ],
}
