# CC BY-SA 4.0 Licence Compliance Controls

Status: REQUIREMENTS SPECIFIED — IMPLEMENTATION NOT STARTED  
Applies to: 2008 Maale Full Bible (MDYTWF / mdyeth)  
Rights basis: Owner-supplied written clarification attributed to The Word for the World International and signed by Dr. Tesfaye Yohannes  
Licence: Creative Commons Attribution-ShareAlike 4.0 International

## 1. Purpose and boundary

This document translates the written rights-holder clarification into testable product and release controls. It does not implement the Android app, grant content-intake authorization, or authorize acquisition of mdyeth_usfm.zip.

The exact licence text is authoritative for legal interpretation: https://creativecommons.org/licenses/by-sa/4.0/. This checklist operationalizes the rights-holder's stated requirements; it is not legal advice.

## 2. Required attribution

The following attribution must be displayed without alteration unless a rights-holder-approved correction is recorded:

> Maale Bible (ጌኤዦ ማፃኣፖ) © 2008 The Word for the World International and the Bible Society of Ethiopia.

The product must also provide a working link to:
https://creativecommons.org/licenses/by-sa/4.0/

Attribution must be available in an easily discoverable in-app licence/about location. It must accompany exported or shared scripture text in a form reasonably appropriate to the medium. A distribution package must not strip required notices.

## 3. Compliance control matrix

| ID | Control | Acceptance evidence | Status |
|---|---|---|---|
| LIC-01 | Exact edition attribution displayed | Screenshot/test of licence screen and release build | SPECIFIED; NOT IMPLEMENTED |
| LIC-02 | CC BY-SA 4.0 licence link accessible | Link target and offline fallback licence notice verified | SPECIFIED; NOT IMPLEMENTED |
| LIC-03 | Changes identified | Transformation record names each conversion, formatting, normalization or correction | SPECIFIED; NOT IMPLEMENTED |
| LIC-04 | No implied endorsement | UI, store listing and notices reviewed for endorsement claims/logos | SPECIFIED; NOT IMPLEMENTED |
| LIC-05 | ShareAlike applied to adaptations of scripture text | Release manifest and licence notice confirm adaptations under CC BY-SA 4.0 | SPECIFIED; NOT IMPLEMENTED |
| LIC-06 | Notices preserved in sharing/export | Synthetic export/share tests verify attribution and licence notice travel with text | SPECIFIED; NOT IMPLEMENTED |
| LIC-07 | Third-party assets separately cleared | Asset register contains independent rights evidence for audio, artwork, fonts, recordings, logos and metadata | SPECIFIED; NOT IMPLEMENTED |
| LIC-08 | Release package carries provenance | Source archive hash, file hashes, transformation record and licence evidence linked | SPECIFIED; NOT IMPLEMENTED |
| LIC-09 | No licence overclaim | Product copy does not imply that CC BY-SA covers unrelated assets or services | SPECIFIED; NOT IMPLEMENTED |
| LIC-10 | Release approval is gated | Release checklist blocks if any required licence control lacks evidence | SPECIFIED; NOT IMPLEMENTED |

## 4. Modification and provenance notice

Every transformation of the licensed text must be recorded. The record must include:

- Original source package identifier and SHA-256.
- Input file path and SHA-256.
- Output file path and SHA-256.
- Transformation name, version and exact configuration.
- Date/time and operator.
- Whether the operation changes text, structure, formatting, encoding or metadata.
- Reviewer and verification result.

The user-facing notice must accurately state that changes were made. It must not imply endorsement by The Word for the World International or the Bible Society of Ethiopia.

Do not silently normalize, correct, rewrite, transliterate, paraphrase or otherwise adapt scripture text. Such changes require a controlled transformation record and ShareAlike treatment.

## 5. ShareAlike boundary

The rights-holder response states that adaptations of the scripture text must be licensed under CC BY-SA 4.0. The project must distinguish:

- The licensed scripture text and adaptations of that text.
- Original application code and independent software components.
- User-created notes, bookmarks and annotations.
- Third-party assets and metadata with separate rights.

Do not assume the scripture licence automatically relicenses the entire application or independent user content. Do not assume independent components are excluded if they incorporate or adapt scripture text. Escalate uncertain boundary cases for legal review before distribution.

## 6. User-generated content

Bookmarks, personal annotations and notes must remain distinguishable from the licensed source text. Sharing a user annotation must not silently transform it into an official translation correction. If an export combines scripture text and user content, preserve attribution and licence notices for the scripture portion and clearly label user-added material.

## 7. Separate asset rights

The written confirmation concerns the 2008 Maale Full Bible text and its eBible USFM package/supporting files as described in the response. It does not establish rights to audio recordings, cover art, photographs, fonts, logos, maps, video, third-party metadata, or other assets unless expressly included in separate evidence. Each asset type requires its own rights register and release decision.

## 8. Release acceptance

No content-bearing build may be released until:
1. Rights evidence is retained and reviewed.
2. Owner has separately authorized acquisition and content intake.
3. Exact source package and derived-file provenance are recorded.
4. LIC-01 through LIC-10 have passing evidence or a documented, Owner-approved non-applicability decision.
5. A reviewer independent of the implementation checks attribution, licence link, modification notice, ShareAlike treatment, and asset boundaries.
6. The Owner approves the immutable content baseline and release.

Current state: controls specified; none are represented as implemented or verified in an application. A2 remains in progress.
