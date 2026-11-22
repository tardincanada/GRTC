# Hand-edited textures

PNG files here replace the generated ones. The path below this folder is the path below
`src/main/resources/assets/periodic_elements/textures/` (for example `block/granite_ore.png`).

`python3 tools/texture_pack.py import <folder>` puts edited textures here (and in the resources),
and `tools/generate.py` copies this folder over its output on every run, so edits are not lost.
Delete a file here to go back to the generated texture (then run tools/generate.py).
