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
    rights = doc.get("content_rights", {})
    if rights.get("scripture_text_included") is not False or rights.get("direct_quotations") is not False:
        fail(errors, "Learning package must not embed Scripture text or direct quotations.")
    if rights.get("source_license") != "CC BY-SA 4.0" or rights.get("source_license_url") != "https://creativecommons.org/licenses/by-sa/4.0/":
        fail(errors, "Pinned source license metadata is missing or incorrect.")
    if rights.get("source_archive_sha256") != "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11":
        fail(errors, "Owner-accepted source archive hash is missing or incorrect.")
    if rights.get("genesis_usfm_sha256") != "cc9b7703d660bd39dfda8a358f9a3fb27d97f05b04f8851b3cf3e536f9e1f392":
        fail(errors, "Owner-accepted Genesis source hash is missing or incorrect.")
    if "Maale Bible (ጌኤዦ ማፃኣፖ)" not in rights.get("required_attribution", ""):
        fail(errors, "Required Maale Bible attribution is missing.")
    if rights.get("no_endorsement_required") is not True or rights.get("share_alike_required_for_scripture_adaptations") is not True:
        fail(errors, "No-endorsement and ShareAlike obligations must be recorded.")
    if rights.get("source_images_imported") is not False:
        fail(errors, "Source image rights must remain separately gated.")
    if doc.get("book", {}).get("usfm_id") != "GEN":
        fail(errors, "Package identity is not GEN.")

    locales = doc.get("locales", {})
    def contains_quote(node):
        if isinstance(node, dict):
            return any(contains_quote(value) for value in node.values())
        if isinstance(node, list):
            return any(contains_quote(value) for value in node)
        return isinstance(node, str) and any(mark in node for mark in ('"', "“", "”", "‘", "’"))
    if contains_quote(locales):
        fail(errors, "Learning content contains quoted text; verify that no direct Scripture quotation is embedded.")
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
    expected_types = {"multiple_choice": 6, "true_false": 96, "multiple_select": 4, "short_answer": 4}
    for locale_name, rows in (("EN", en_quiz), ("AM", am_quiz)):
        actual_types = {kind: sum(1 for row in rows if row.get("type") == kind) for kind in expected_types}
        if actual_types != expected_types:
            fail(errors, f"{locale_name} quiz type coverage mismatch: {actual_types}.")
        generic_explanations = {
            "the statement is supported by the cited passage.",
            "the statement is not supported by the cited passage; the reference gives the accurate narrative context."
        }
        for row in rows:
            if str(row.get("explanation", "")).strip().lower() in generic_explanations:
                fail(errors, f"{locale_name} quiz {row.get('id')} still has a generic explanation.")
    if len(en_quiz) == len(am_quiz):
        for source, translated in zip(en_quiz, am_quiz):
            if translated.get("id") != source.get("id") + "-AM":
                fail(errors, f"Amharic quiz ID mismatch for {source.get('id')}.")
            if source.get("type") != "short_answer":
                source_answer = source.get("correct_answer", source.get("correct_option_ids", source.get("correct_option_id")))
                translated_answer = translated.get("correct_answer", translated.get("correct_option_ids", translated.get("correct_option_id")))
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
    maale_gate = str(doc.get("review_gates", {}).get("maale_language_review", "")).lower()
    if not any(token in maale_gate for token in ("required", "pending", "not translated")):
        fail(errors, "Maale specialist language review must remain an explicit release gate.")

    return errors

if __name__ == "__main__":
    problems = validate()
    if problems:
        print("\n".join(problems), file=sys.stderr)
        raise SystemExit(1)
    print("PASS: Genesis EN/AM coverage, quiz-answer parity, references, draft gate and Android asset mirror.")
