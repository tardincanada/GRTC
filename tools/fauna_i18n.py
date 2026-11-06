"""Translations of the animals (3.50.0): English -> translation per language. Filled per language in fauna_<code>.py."""
import importlib


def text(code):
    try:
        m = importlib.import_module(f"fauna_{code}")
    except ModuleNotFoundError:
        return {}
    return dict(m.TR)
