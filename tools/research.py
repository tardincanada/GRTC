"""Research projects from data/research.csv. Called by generate.py."""
import csv
import json
from pathlib import Path

CATEGORIES = {"metallurgy", "chemistry", "machines", "materials", "food", "tools", "weapons", "knowledge", "electricity"}


def generate_research(csv_path, mod_items, runtime_dir, lang, mod_id):
    with Path(csv_path).open(newline="", encoding="utf-8") as f:
        rows = list(csv.DictReader(f))
    projects = {}
    for r in rows:
        pid = r["id"].strip()
        assert pid not in projects, f"duplicate research {pid}"
        level = int(r["level"])
        assert 1 <= level <= 7, pid
        assert r["key"] in ("yes", "no"), pid
        assert r["category"] in CATEGORIES, (pid, r["category"])
        samples = []
        for s in filter(None, r["samples"].split(";")):
            item, count = s.split("*")
            for choice in item.split("|"):  # "a|b": either item will do
                ns, path = choice.split(":")
                if ns == mod_id:
                    assert path in mod_items, f"{pid}: unknown mod item {choice}"
            samples.append({"item": item, "count": int(count)})
        projects[pid] = {
            "id": pid, "level": level, "key": r["key"] == "yes", "category": r["category"],
            "date": r["date"], "requires": [x for x in r["requires"].split(";") if x],
            "samples": samples, "unlocks": [x for x in r["unlocks"].split(";") if x],
        }
        lang[f"research.{mod_id}.{pid}"] = r["name"]
        lang[f"research.{mod_id}.{pid}.desc"] = r["description"]
    for p in projects.values():
        for req in p["requires"]:
            assert req in projects, f"{p['id']} requires unknown {req}"
            assert projects[req]["level"] <= p["level"], f"{p['id']} requires a later-level project {req}"

    # No cycles, and every key project is reachable.
    state = {}

    def visit(pid):
        assert state.get(pid) != "open", f"research cycle at {pid}"
        if state.get(pid) == "done":
            return
        state[pid] = "open"
        for req in projects[pid]["requires"]:
            visit(req)
        state[pid] = "done"

    for pid in projects:
        visit(pid)
    for lvl in sorted({p["level"] for p in projects.values()}):
        assert any(p["key"] and p["level"] == lvl for p in projects.values()), f"level {lvl} has no key project"

    (runtime_dir / "research.json").write_text(json.dumps(list(projects.values()), indent=1, ensure_ascii=False) + "\n",
                                               encoding="utf-8")
    return projects
