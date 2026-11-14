#!/usr/bin/env python3
"""Fail CI when Android string resources drift across EN, AM and Maale locales."""
from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "app" / "src" / "main" / "res"
LOCALES = ("values-am", "values-b+mdy")
RESOURCE_TAGS = {"string", "plurals", "string-array", "integer-array"}
PLACEHOLDER = re.compile(r"%(?:[0-9]+\\$)?[sd]")


def read_resources(path: Path) -> dict[str, str]:
    root = ET.parse(path).getroot()
    result: dict[str, str] = {}
    for node in root:
        if node.tag not in RESOURCE_TAGS or "name" not in node.attrib:
            continue
        key = f"{node.tag}:{node.attrib['name']}"
        if node.tag == "string":
            value = "".join(node.itertext())
        else:
            value = "|".join("".join(item.itertext()) for item in node.findall("item"))
        result[key] = value
    return result


def resource_files(directory: Path) -> set[str]:
    if not directory.exists():
        return set()
    return {
        path.name
        for path in directory.glob("*.xml")
        if any(node.tag in RESOURCE_TAGS for node in ET.parse(path).getroot())
    }


def main() -> int:
    base_dir = RES / "values"
    errors: list[str] = []
    for locale in LOCALES:
        locale_dir = RES / locale
        filenames = resource_files(base_dir) | resource_files(locale_dir)
        for filename in sorted(filenames):
            base_path = base_dir / filename
            locale_path = locale_dir / filename
            if not base_path.exists():
                errors.append(f"{locale}/{filename}: localized resource file has no default counterpart")
                continue
            if not locale_path.exists():
                errors.append(f"{locale}/{filename}: localized resource file is missing")
                continue
            base = read_resources(base_path)
            translated = read_resources(locale_path)
            for key in sorted(base.keys() - translated.keys()):
                errors.append(f"{locale}/{filename}: missing {key}")
            for key in sorted(translated.keys() - base.keys()):
                errors.append(f"{locale}/{filename}: extra {key} has no default resource")
            for key in sorted(base.keys() & translated.keys()):
                expected = sorted(PLACEHOLDER.findall(base[key]))
                actual = sorted(PLACEHOLDER.findall(translated[key]))
                if expected != actual:
                    errors.append(
                        f"{locale}/{filename}: placeholder mismatch for {key}: "
                        f"default={expected}, locale={actual}"
                    )
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print("PASS: EN, AM and Maale Android resource keys and format placeholders are aligned.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
