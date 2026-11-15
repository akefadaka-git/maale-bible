#!/usr/bin/env python3
"""Validate Genesis EN/AM learning-package coverage and mirrored Android asset."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DOC = ROOT / "docs/content/books/GEN.json"
ASSET = ROOT / "app/src/main/assets/content/books/GEN.json"
REF = re.compile(r"\b(GEN|EXO)\s+(\d+)(?::(\d+))?")

def fail(errors: list[str], message: str) -> None:
    errors.append(message)

def validate() -> list[str]:
    errors: list[str] = []
    try:
        doc = json.loads(DOC.read_text(encoding="utf-8"))
        asset = json.loads(ASSET.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        return [f"Unable to read/parse Genesis package: {exc}"]

    if doc.get("publication_state") != "draft_not_for_release":
        fail(errors, "Genesis must remain draft_not_for_release.")
    if doc.get("content_rights", {}).get("scripture_text_included") is not False:
        fail(errors, "Learning package must not embed scripture text.")
    if doc.get("book", {}).get("usfm_id") != "GEN":
        fail(errors, "Package identity is not GEN.")

    locales = doc.get("locales", {})
    en, am = locales.get("en", {}), locales.get("am", {})
    if set(locales) != {"en", "am"}:
        fail(errors, "Only explicitly authored EN and AM locales may be present; do not fabricate Maale.")
    if asset.get("locales", {}).get("en") != en or asset.get("locales", {}).get("am") != am:
        fail(errors, "Android asset EN/AM locales differ from documentation source.")

    for locale_name, locale in (("EN", en), ("AM", am)):
        intro = locale.get("introduction", {})
        for field in ("title", "summary", "authorship", "setting", "audience_and_purpose", "date_and_composition", "genre", "theological_contribution"):
            if not isinstance(intro.get(field), str) or not intro[field].strip():
                fail(errors, f"{locale_name} introduction missing {field}.")
        if not intro.get("canonical_connections"):
            fail(errors, f"{locale_name} introduction missing canonical_connections.")
        for key, expected in (("chapter_guides", 50), ("inductive_studies", 4), ("quiz", 110)):
            rows = locale.get(key, [])
            if len(rows) != expected:
                fail(errors, f"{locale_name} {key}: expected {expected}, found {len(rows)}.")
        guides = locale.get("chapter_guides", [])
        for chapter, guide in enumerate(guides, start=1):
            if guide.get("chapter") != chapter:
                fail(errors, f"{locale_name} chapter guide order/identity mismatch at {chapter}.")
            for field in ("title", "summary", "observation", "interpretation", "application", "references", "literary_context", "cross_references", "discussion_prompts", "review", "chapter_specific_study"):
                if not guide.get(field):
                    fail(errors, f"{locale_name} chapter {chapter} missing {field}.")
            if not guide.get("references") or f"GEN {chapter}" not in guide["references"]:
                fail(errors, f"{locale_name} chapter {chapter} lacks its chapter reference.")

        for study in locale.get("inductive_studies", []):
            if not study.get("prayer_prompt"):
                fail(errors, f"{locale_name} inductive study {study.get('id')} lacks a prayer prompt.")

        for item in locale.get("quiz", []):
            if not item.get("prompt") or not item.get("explanation") or not item.get("references") or not item.get("learning_objective") or not item.get("difficulty"):
                fail(errors, f"{locale_name} quiz {item.get('id')} lacks prompt, explanation or references.")
            if item.get("type") not in {"true_false", "multiple_choice", "multiple_select", "short_answer"}:
                fail(errors, f"{locale_name} quiz {item.get('id')} uses an unsupported Android question type.")
            if item.get("type") == "true_false" and type(item.get("correct_answer")) is not bool:
                fail(errors, f"{locale_name} quiz {item.get('id')} true/false answer is not Boolean.")
            if item.get("type") in {"multiple_choice", "multiple_select"}:
                options = item.get("options", [])
                if len(options) != 4:
                    fail(errors, f"{locale_name} quiz {item.get('id')} must have four options.")
                if item.get("type") == "multiple_choice" and item.get("correct_option_id") not in {"A", "B", "C", "D"}:
                    fail(errors, f"{locale_name} quiz {item.get('id')} has invalid single-choice answer.")
                if item.get("type") == "multiple_select":
                    keys = item.get("correct_option_ids", [])
                    if len(keys) not in {2, 3} or not set(keys).issubset({"A", "B", "C", "D"}):
                        fail(errors, f"{locale_name} quiz {item.get('id')} has invalid multiple-select keys.")
            if item.get("type") == "short_answer" and not item.get("accepted_answers"):
                fail(errors, f"{locale_name} quiz {item.get('id')} lacks accepted short answers.")
            for ref in item.get("references", []):
                match = REF.search(ref)
                if match and match.group(1) == "GEN" and not 1 <= int(match.group(2)) <= 50:
                    fail(errors, f"{locale_name} quiz {item.get('id')} has out-of-book chapter reference {ref}.")

    en_quiz, am_quiz = en.get("quiz", []), am.get("quiz", [])
    if len(en_quiz) == len(am_quiz):
        for source, translated in zip(en_quiz, am_quiz):
            if translated.get("id") != source.get("id") + "-AM":
                fail(errors, f"Amharic quiz ID mismatch for {source.get('id')}.")
            source_answer = source.get("correct_answer", source.get("correct_option_ids", source.get("accepted_answers", source.get("correct_option_id"))))
            translated_answer = translated.get("correct_answer", translated.get("correct_option_ids", translated.get("accepted_answers", translated.get("correct_option_id"))))
            if source_answer != translated_answer:
                fail(errors, f"Quiz answer mismatch for {source.get('id')}.")
            if translated.get("explanation_locale") != "am":
                fail(errors, f"Amharic explanation locale missing for {translated.get('id')}.")

    for key, expected in (("key_verses", 18), ("memory_verses", 7), ("concordance", 14), ("dictionary", 5), ("timeline", 5)):
        if len(en.get(key, [])) != expected or len(am.get(key, [])) != expected:
            fail(errors, f"Supporting module {key} must have EN/AM coverage of {expected}.")

    en_commentary = en.get("commentary", {}).get("units", [])
    am_commentary = am.get("commentary", {}).get("units", [])
    if len(en_commentary) != 6 or len(am_commentary) != 6:
        fail(errors, "Genesis commentary must contain six EN and six AM draft units.")
    elif [unit.get("range") for unit in en_commentary] != [unit.get("range") for unit in am_commentary]:
        fail(errors, "Amharic commentary ranges do not match English source units.")
    if not doc.get("language_coverage", {}).get("mdy", {}).get("status", "").startswith("not_translated"):
        fail(errors, "Maale language coverage must remain explicitly untranslated until qualified review.")

    status = am.get("content_status", {})
    for key in ("chapter_guides", "quiz", "inductive_studies", "key_verses", "memory_verses", "concordance", "dictionary", "timeline"):
        if not status.get(key):
            fail(errors, f"Amharic content status missing explicit state for {key}.")
    if doc.get("review_gates", {}).get("maale_language_review") != "required":
        fail(errors, "Maale specialist language review must remain an explicit release gate.")

    return errors

if __name__ == "__main__":
    problems = validate()
    if problems:
        print("\n".join(problems), file=sys.stderr)
        raise SystemExit(1)
    print("PASS: Genesis EN/AM coverage, quiz-answer parity, references, draft gate and Android asset mirror.")
