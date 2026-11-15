# Genesis A4 external review and sign-off register

**Status:** OPEN — candidates identified; no reviewer has yet accepted or signed off.  
**Artifact under review:** PR #63, branch `integration/maale-learning-centre-2026-11-14`  
**Publication gate:** `draft_not_for_release`

## Reviewer candidates identified from public institutional profiles

These are evidence-backed candidates for invitation, not appointments, endorsements or completed reviews.

| Gate | Candidate / route | Publicly documented qualification | Review required |
|---|---|---|---|
| Maale translation and language | **Tamene Lale Gego (BTh)**, The Word for the World Ethiopia | TWFTW profile says he translated the complete Bible into his mother tongue Maale and has experience in exegetical checking and advising translation teams. | Review or supervise Genesis learning-content translation into Maale; assess idiom, orthography, terminology, register, naturalness and Scripture alignment; identify a second community reviewer. |
| Amharic language/editorial | **Dr. Haileyesus E. Woldemariam**, Global Translation Consultant | Public profile lists Biblical Hebrew and Greek, PhD on Amharic grammar, MPhil and MA in Linguistics, Bible translation research and consulting translation projects. | Independent Amharic review of all 50 chapter guides, 4 inductive studies, 110 quizzes and supporting modules; report terminology, grammar, register and theological clarity. |
| Biblical / theological review | **Dr. Misgana Mathewos**, Ethiopian Graduate School of Theology | Public profile lists PhD in Old Testament Studies; research includes Genesis 1–11 and a doctoral dissertation on Genesis 25:19–28:8. | Independent Genesis exegesis and theological review across the introduction, chapter guides, commentary, study questions and quiz answer rationales. Must disclose any conflict of interest. |
| English editorial | **Institutional referral requested** from EGST or TWFTW | No independent English editor has been confirmed. | English copy-edit, biblical terminology consistency, readability, reference presentation, rationale quality and editorial style sheet review. |

### Public evidence

- TWFTW faculty profile: https://training.twftw.org/our-faculty/ (Tamene Lale Gego, lines 78–85).
- Dr. Haileyesus E. Woldemariam profile: https://translation.bible/staff-profile/haileyesus-e-woldemariam/
- Dr. Misgana Mathewos profile: https://egst.edu.et/teachers/dr-misgana-mathewos/
- EGST contact: https://egst.edu.et/contact-us/ — `info@egst.edu.et`.
- TWFTW UK contact route: https://www.twftw.org.uk/ — `admin-uk@twftw.org`.
- Public profile contact for Dr. Woldemariam: `info@biblesocieties.org`.
- Public profile contact for Dr. Mathewos: `misganamathews@gmail.com`.

## Required review deliverables

Each reviewer must receive a frozen review bundle identifying:
- PR head SHA and the exact content package SHA.
- Source edition and pinned source hashes.
- All content sections in the review scope.
- Review checklist and issue-report template.
- A clear statement that the content is unpublished draft material.

Each sign-off must record:
1. Reviewer full name and role.
2. Qualifications and relevant language/theological expertise.
3. Institution or community affiliation.
4. Conflict-of-interest declaration.
5. Exact reviewed commit SHA and artifact hashes.
6. Scope reviewed, methods and any limitations.
7. Findings, required corrections and residual concerns.
8. Decision: `APPROVED`, `APPROVED_WITH_NONBLOCKING_NOTES`, or `REJECTED`.
9. Date and signed written confirmation.

## Maale translation workflow

1. Agree Maale dialect, orthography and terminology conventions with the qualified translator and local language community.
2. Translate the English source learning content into Maale; do not translate or alter the licensed Scripture text in this workstream.
3. Back-translate selected high-risk theological terms and quiz answers.
4. Conduct independent Maale community review for comprehension and naturalness.
5. Record translator and reviewer sign-offs against the frozen commit.
6. Keep English fallback explicitly disclosed until Maale content is reviewed and installed.

## A4 closure rule

Do not mark Genesis `VERIFIED-CLOSED` until all four review gates (Maale, Amharic, English editorial, independent biblical/theological), exact-source reference validation, rights review and exact-head technical QA have signed evidence. No reviewer has signed off as of this register's creation. A5 deployment and publication approval remain separate.
