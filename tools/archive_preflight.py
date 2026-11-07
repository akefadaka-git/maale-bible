#!/usr/bin/env python3
"""Fail-closed metadata preflight for an authorized ZIP source package.

This utility reads ZIP central-directory metadata and hashes the archive. It
never extracts or prints archive file contents. It does not authorize source
acquisition, content intake, or publication. Python 3.10+; standard library.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import stat
import sys
import unicodedata
import zipfile
from pathlib import PurePosixPath
from pathlib import Path
from typing import Any

DRIVE_PREFIX = re.compile(r"^[A-Za-z]:")


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _safe_member_name(name: str) -> tuple[str | None, str | None]:
    if not name or "\x00" in name:
        return None, "empty or NUL-containing archive path"
    if "\\" in name:
        return None, "backslash path separator is not permitted"
    if name.startswith("/") or name.startswith("//") or DRIVE_PREFIX.match(name):
        return None, "absolute or drive-qualified archive path"
    path = PurePosixPath(name)
    if any(part in {"", ".", ".."} for part in path.parts):
        return None, "non-canonical or parent-traversal archive path"
    normalized = unicodedata.normalize("NFC", path.as_posix())
    return normalized, None


def preflight_zip(
    archive_path: Path,
    *,
    max_entries: int = 2000,
    max_file_size: int = 50 * 1024 * 1024,
    max_total_size: int = 250 * 1024 * 1024,
    max_compression_ratio: float = 100.0,
) -> dict[str, Any]:
    """Inspect central-directory metadata only; never extract archive members."""
    errors: list[dict[str, str]] = []
    members: list[dict[str, Any]] = []
    try:
        archive_hash = sha256_file(archive_path)
        with zipfile.ZipFile(archive_path, "r") as archive:
            infos = archive.infolist()
            if len(infos) > max_entries:
                errors.append({"code": "ENTRY_LIMIT_EXCEEDED", "message": "Archive entry count exceeds configured limit"})

            seen: dict[str, str] = {}
            total_size = 0
            for info in infos:
                normalized, path_error = _safe_member_name(info.filename)
                if path_error:
                    errors.append({"code": "UNSAFE_PATH", "message": f"{info.filename!r}: {path_error}"})
                    continue

                collision_key = normalized.casefold()
                if collision_key in seen:
                    errors.append({
                        "code": "DUPLICATE_NORMALIZED_PATH",
                        "message": f"{info.filename!r} collides with {seen[collision_key]!r} after normalization",
                    })
                else:
                    seen[collision_key] = info.filename

                if info.flag_bits & 0x1:
                    errors.append({"code": "ENCRYPTED_ENTRY", "message": f"{info.filename!r}: encrypted ZIP entries are not accepted"})

                mode = (info.external_attr >> 16) & 0xFFFF
                file_type = stat.S_IFMT(mode)
                is_directory = info.is_dir()
                if file_type == stat.S_IFLNK:
                    errors.append({"code": "SYMLINK_ENTRY", "message": f"{info.filename!r}: symbolic links are not accepted"})
                elif file_type not in (0, stat.S_IFREG, stat.S_IFDIR):
                    errors.append({"code": "SPECIAL_FILE_ENTRY", "message": f"{info.filename!r}: special filesystem entries are not accepted"})

                suffix = PurePosixPath(normalized).suffix.lower()
                if suffix in {".zip", ".tar", ".tgz", ".gz", ".7z", ".rar"}:
                    errors.append({"code": "NESTED_ARCHIVE", "message": f"{info.filename!r}: nested archives are not accepted"})

                if not is_directory:
                    total_size += info.file_size
                    if info.file_size > max_file_size:
                        errors.append({"code": "FILE_SIZE_LIMIT_EXCEEDED", "message": f"{info.filename!r}: uncompressed size exceeds configured limit"})
                    if info.compress_size == 0 and info.file_size > 0:
                        errors.append({"code": "INVALID_COMPRESSION_RATIO", "message": f"{info.filename!r}: non-empty entry has zero compressed size"})
                    elif info.compress_size > 0 and info.file_size / info.compress_size > max_compression_ratio:
                        errors.append({"code": "COMPRESSION_RATIO_LIMIT_EXCEEDED", "message": f"{info.filename!r}: compression ratio exceeds configured limit"})

                members.append({
                    "path": normalized,
                    "compressed_size": info.compress_size,
                    "uncompressed_size": info.file_size,
                    "is_directory": is_directory,
                })

            if total_size > max_total_size:
                errors.append({"code": "TOTAL_SIZE_LIMIT_EXCEEDED", "message": "Total uncompressed size exceeds configured limit"})

    except (OSError, zipfile.BadZipFile, zipfile.LargeZipFile) as exc:
        return {
            "preflight_version": "1.0.0",
            "status": "ERROR",
            "archive_sha256": None,
            "entry_count": 0,
            "total_uncompressed_size": 0,
            "members": [],
            "errors": [{"code": "ARCHIVE_READ_ERROR", "message": str(exc)}],
            "extraction_performed": False,
            "content_intake_authorized": False,
        }

    return {
        "preflight_version": "1.0.0",
        "status": "FAIL" if errors else "PASS",
        "archive_sha256": archive_hash,
        "entry_count": len(members),
        "total_uncompressed_size": sum(item["uncompressed_size"] for item in members if not item["is_directory"]),
        "members": members,
        "errors": errors,
        "extraction_performed": False,
        "content_intake_authorized": False,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("archive", type=Path)
    parser.add_argument("--max-entries", type=int, default=2000)
    parser.add_argument("--max-file-size", type=int, default=50 * 1024 * 1024)
    parser.add_argument("--max-total-size", type=int, default=250 * 1024 * 1024)
    parser.add_argument("--max-compression-ratio", type=float, default=100.0)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    if min(args.max_entries, args.max_file_size, args.max_total_size) < 1 or args.max_compression_ratio <= 0:
        parser.error("all configured limits must be positive")

    report = preflight_zip(
        args.archive,
        max_entries=args.max_entries,
        max_file_size=args.max_file_size,
        max_total_size=args.max_total_size,
        max_compression_ratio=args.max_compression_ratio,
    )
    rendered = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(rendered, encoding="utf-8")
    else:
        print(rendered, end="")
    return 0 if report["status"] == "PASS" else 1 if report["status"] == "FAIL" else 2


if __name__ == "__main__":
    raise SystemExit(main())
