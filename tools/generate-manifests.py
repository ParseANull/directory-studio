#!/usr/bin/env python3
"""
One-time migration script: converts each plugin/help/test module's
pom-first.xml Felix maven-bundle-plugin <instructions> block into a real,
static META-INF/MANIFEST.MF, checked into git.

This is safe to do mechanically ONLY because every instruction in this
project's pom-first.xml files is a fully explicit, hand-declared header
(Export-Package/Import-Package/Require-Bundle given as literal lists, never
a bnd bytecode-scanning wildcard). Verified before writing this script:

    grep pattern for bare '*' (not '!*') in Export-Package/Import-Package
    across all 31 modules -> zero matches.

Run once from the repo root: python3 tools/generate-manifests.py
Then delete this script along with the pom-first.xml files it consumed.
"""
import re
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

MODULE_DIRS = (
    sorted((REPO_ROOT / "plugins").glob("*/pom-first.xml"))
    + sorted((REPO_ROOT / "helps").glob("*/pom-first.xml"))
    + sorted((REPO_ROOT / "tests").glob("*/pom-first.xml"))
)

NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def load_pom_properties(pom_path):
    """Read the <properties> block of the top-level pom.xml into a dict, so
    ${...} references inside pom-first.xml instructions (e.g. bundle-version
    of transitive ASF dependencies) can be resolved to literal values for
    the static manifest -- Maven would normally interpolate these at build
    time, but a checked-in MANIFEST.MF has no such interpolation step."""
    tree = ET.parse(pom_path)
    root = tree.getroot()
    props = root.find("m:properties", NS)
    result = {}
    if props is not None:
        for child in props:
            result[local_tag(child)] = (child.text or "").strip()
    return result


def resolve_properties(text, props):
    def _sub(match):
        key = match.group(1)
        if key not in props:
            raise KeyError(
                f"Unresolved Maven property '{{{key}}}' -- add it to "
                f"load_pom_properties() lookup or the top-level pom.xml"
            )
        return props[key]

    return re.sub(r"\$\{([a-zA-Z0-9._-]+)\}", _sub, text)

# Felix bnd instructions that map directly to a manifest header of the same
# name, in the order we want them to appear in the generated manifest.
PASSTHROUGH_HEADERS = [
    "Bundle-Localization",
    "Eclipse-LazyStart",
    "Bundle-Activator",
    "Export-Package",
    "Import-Package",
    "Require-Bundle",
    "Bundle-ClassPath",
]

# Instructions that control the *build* (e.g. physically embedding a jar)
# rather than emitting a manifest header themselves. Bundle-ClassPath above
# already carries the resulting header text explicitly in every module that
# uses these, so these two are build-time-only and intentionally skipped.
SKIP_INSTRUCTIONS = {"Embed-Directory", "Embedded-Artifacts"}


def local_tag(elem):
    """Strip the Maven POM XML namespace off a tag name."""
    return elem.tag.split("}", 1)[-1] if "}" in elem.tag else elem.tag


def wrap_header(name, value, width=72):
    """
    Format a manifest header, wrapping comma-separated values across
    continuation lines (single leading space) so no line exceeds `width`
    bytes, matching the style bnd/PDE normally produce.
    """
    items = [p.strip() for p in value.split(",") if p.strip()]
    if not items:
        return f"{name}: \n"

    lines = []
    current = f"{name}: {items[0]}"
    for item in items[1:]:
        candidate = current + "," + item
        if len(candidate) + 1 <= width:  # +1 for the trailing comma we'll add
            current = candidate
        else:
            lines.append(current + ",")
            current = " " + item
    lines.append(current)
    return "\n".join(lines) + "\n"


def get_artifact_id(root):
    el = root.find("m:artifactId", NS)
    return el.text.strip()


def get_instructions(root):
    instr = root.find(".//m:instructions", NS)
    if instr is None:
        return {}
    result = {}
    for child in instr:
        key = local_tag(child)
        text = (child.text or "").strip()
        result[key] = text
    return result


def generate_manifest(pom_first_path):
    # A couple of source files have a stray leading space/newline before the
    # <?xml ?> declaration, which expat rejects outright. Strip before parsing.
    text = pom_first_path.read_text(encoding="utf-8").lstrip()
    root = ET.fromstring(text)
    artifact_id = get_artifact_id(root)
    instructions = get_instructions(root)

    lines = [
        "Manifest-Version: 1.0",
        "Bundle-ManifestVersion: 2",
    ]

    bsn = instructions.get("Bundle-SymbolicName", "${project.artifactId};singleton:=true")
    bsn = bsn.replace("${project.artifactId}", artifact_id)
    lines.append(f"Bundle-SymbolicName: {bsn}")
    lines.append("Bundle-Version: 2.0.0.qualifier")

    for header in PASSTHROUGH_HEADERS:
        if header not in instructions:
            continue
        value = instructions[header]

        if header == "Export-Package" and value == "!*":
            # "!*" means "export nothing" -> correct OSGi equivalent is to
            # simply omit the header entirely.
            continue
        if not value:
            # e.g. an empty self-closing <Import-Package/> -> no explicit
            # imports; dependency wiring is via Require-Bundle instead.
            continue

        lines.append(wrap_header(header, value).rstrip("\n"))

    for skipped in SKIP_INSTRUCTIONS:
        if skipped in instructions:
            print(f"  (skipping build-time-only instruction {skipped}, "
                  f"already reflected in Bundle-ClassPath / build.properties)")

    return artifact_id, "\n".join(lines) + "\n"


def main():
    pom_properties = load_pom_properties(REPO_ROOT / "pom.xml")

    written = []
    for pom_first in MODULE_DIRS:
        module_dir = pom_first.parent
        artifact_id, manifest_text = generate_manifest(pom_first)
        manifest_text = resolve_properties(manifest_text, pom_properties)

        meta_inf = module_dir / "META-INF"
        meta_inf.mkdir(exist_ok=True)
        manifest_path = meta_inf / "MANIFEST.MF"
        manifest_path.write_text(manifest_text, encoding="utf-8")
        written.append(manifest_path.relative_to(REPO_ROOT))
        print(f"wrote {manifest_path.relative_to(REPO_ROOT)}  ({artifact_id})")

    print(f"\n{len(written)} MANIFEST.MF files generated.")


if __name__ == "__main__":
    main()
