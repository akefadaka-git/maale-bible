#!/usr/bin/env python3
"""Audit Android string-resource coverage and format placeholders for a locale."""

from __future__ import annotations

import argparse
import json
import re
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path

PLACEHOLDER = re.compile(r"(?<!%)%(?:[1-9][0-9]*\\$)?[sdif]")


def read_strings(path: Path) -> tuple[dict[str, str], list[str]]:
    root = ET.parse(path).getroot()
    values: dict[str, str] = {}
    duplicates: list[str] = []
    for node in root:
        if node.tag != "string":
            continue
        name = node.attrib.get("name")
        if not name:
            continue
        if name in values:
            duplicates.append(name)
        values[name] = "".join(node.itertext())
    return values, sorted(set(duplicates))


def placeholders(value: str) -> list[str]:
    return sorted(PLACEHOLDER.findall(value))


def audit(base_path: Path, locale_path: Path) -> dict[str, object]:
    base, base_duplicates = read_strings(base_path)
    locale, locale_duplicates = read_strings(locale_path)
    shared = sorted(base.keys() & locale.keys())
    missing = sorted(base.keys() - locale.keys())
    locale_only = sorted(locale.keys() - base.keys())
    mismatches = [
        {
            "key": key,
            "base": placeholders(base[key]),
            "locale": placeholders(locale[key]),
        }
        for key in shared
        if Counter(placeholders(base[key])) != Counter(placeholders(locale[key]))
    ]
    return {
        "base_file": str(base_path),
        "locale_file": str(locale_path),
        "base_count": len(base),
        "locale_count": len(locale),
        "matched_count": len(shared),
        "coverage_percent": round((len(shared) / len(base) * 100), 2) if base else 100.0,
        "missing_keys": missing,
        "locale_only_keys": locale_only,
        "placeholder_mismatches": mismatches,
        "duplicate_base_keys": base_duplicates,
        "duplicate_locale_keys": locale_duplicates,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base", type=Path, required=True, help="Canonical values/strings.xml")
    parser.add_argument("--locale", type=Path, required=True, help="Localized strings.xml")
    parser.add_argument("--strict", action="store_true", help="Return non-zero if any audit issue exists")
    args = parser.parse_args()
    result = audit(args.base, args.locale)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    has_issues = any(
        result[key]
        for key in (
            "missing_keys",
            "placeholder_mismatches",
            "duplicate_base_keys",
            "duplicate_locale_keys",
        )
    )
    return 1 if args.strict and has_issues else 0


if __name__ == "__main__":
    sys.exit(main())
