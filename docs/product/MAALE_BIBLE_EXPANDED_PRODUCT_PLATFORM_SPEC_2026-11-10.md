# Maale Bible — Expanded Product & Platform Specification

**Document ID:** MB-PRODUCT-A1-02  
**Version:** 1.1 — Expanded product-platform specification  
**Date:** 2026-11-10  
**Repository:** `akefadaka-git/maale-bible`  
**Status:** A1 proposal for Owner review; not frozen; no implementation authorization implied.

## 1. Executive product vision

Maale Bible is intended to become a Maale-first, multilingual, offline-first Bible, study, learning, collaboration and publishing platform. It must serve individual readers while also offering serious tools for families, small groups, churches, pastors, teachers, Bible researchers, translators, ministries, publishers and content creators.

The platform's competitive proposition is not simply “another Bible app.” It is a trustworthy Scripture-centred workspace connecting verified Bible texts, personal study, collaborative learning, local-language publishing and ministry workflows—designed around Maale and African language realities, with international-quality product engineering.

**Non-negotiable principle: Scripture access remains free.** Every Bible edition that the platform is legally permitted to provide for free reading remains free to read. Premium charges are for advanced tools, workflow capacity, collaboration administration, eligible supplementary content and professional services—not for spiritual status or access to God's Word.

## 2. Owner-approved product decisions

- Brand identity: hybrid Maale heritage + royal elegance + future-classic.
- Membership: five premium levels—Silver, Gold, Platinum, Diamond and Elite.
- Bible access: all legally available Bible editions remain free to read.
- Existing UI: evolve and upgrade; do not discard useful existing work.
- Default interface language: Maale.
- Guest reading: available without registration.
- Login: username or mobile number plus password.
- OTP: registration verification only.
- Private study notes: local/private by default; no automatic account sync.
- Content: provenance, rights, licence, attribution, integrity and compatibility must be checked source by source.
- Repository: `akefadaka-git/maale-bible` is canonical.

## 3. Product audiences and jobs-to-be-done

1. **Individual readers:** read, listen, search, save passages and continue offline.
2. **Families and friends:** share reading plans, prayer requests and moderated discussions.
3. **Small groups:** organise sessions, questions, attendance/participation and follow-up.
4. **Pastors and ministry leaders:** prepare sermons, lessons, study guides and presentations.
5. **Teachers and students:** build courses, assignments, reading schedules and learning resources.
6. **Bible researchers and translators:** compare editions, annotate passages, manage terminology and document sources.
7. **Churches and organisations:** manage groups, publish approved resources, communicate events and view aggregate engagement.
8. **Content creators and publishers:** prepare verse graphics, devotionals, social posts, audio/video scripts and shareable study material.

Organisation and professional workflows must be opt-in, permission-controlled and separately priced only where there is a real service cost or premium value.

## 4. Product architecture and navigation

### 4.1 Primary mobile navigation

Use four persistent destinations:
- **Home:** continue reading, daily passage, plans, recent activity and shortcuts.
- **Bible:** editions, languages, books, chapters, downloads and search.
- **Study:** personal workspace, notes, highlights, bookmarks, commentary and tools.
- **Profile:** account, language, appearance, membership, donations, integrations and settings.

Contextual destinations are opened from these areas:
- Reader
- Listen
- Groups
- Create / Publish
- Courses
- Events
- Organisation workspace

Do not overcrowd the bottom navigation. On larger screens, use adaptive navigation rail/sidebar and multi-pane layouts.

### 4.2 Information architecture

- Scripture Library
- Reader and Listening
- Personal Study
- Commentary and Research
- Plans and Courses
- Groups and Community
- Create and Publish
- Church / Organisation Workspace
- Membership and Billing
- Integrations and Developer Platform
- Help, Feedback and Governance

## 5. Scripture library and reading

### 5.1 Bible edition selection

Filter and compare by language, edition, testament, completeness, licence, attribution, availability and offline installation state. Interface language and Bible text language are independent.

### 5.2 Reader

- Maale-first typography, readable Ethiopic and supported scripts.
- Adjustable type size, line height, margins and reading width.
- Light, dark and system themes.
- Book/chapter/verse navigation and continuous reading.
- Parallel editions and side-by-side comparison where content is installed.
- Search within installed scripture.
- Verse selection actions: copy, share, bookmark, highlight, note, compare, cross-reference and add to a study.
- Audio controls only where properly licensed audio exists.
- Reading position and offline state must be reliable.

### 5.3 Scripture integrity

Scripture text is immutable source content. AI, commentary, user notes, translations, summaries and generated content must be visually and technically separated from the canonical Bible text. Never silently paraphrase or modify a verse. Every quotation must retain edition and reference metadata.

## 6. Export, print and portability

Export is a first-class product capability, not a hidden share-only action. Add a visible **Export / Download / Print** action to eligible documents and workspaces.

### 6.1 Exportable objects

- Personal notes and notebooks.
- Highlights and bookmark lists.
- Study collections and research dossiers.
- Commentary authored by the user.
- Sermons, lesson plans and teaching outlines.
- Group study guides and permitted discussion summaries.
- Reading plans and completion records.
- Course materials and assignments.
- Presentation slides and handouts.
- Social media assets and verse cards.
- Organisation reports and permitted aggregate analytics.
- User-owned profile and data archive.

### 6.2 Formats

Support according to object type:
- PDF for print-ready documents.
- DOCX for editable documents.
- PPTX for presentation decks.
- TXT and Markdown for portable text.
- CSV for tabular records and reports.
- ICS for calendar events and reading schedules.
- PNG/JPEG for image cards.
- ZIP for a user data archive containing supported files and a manifest.

Offer page size, orientation, typography, theme, title page, footnotes, reference style, language and attribution controls where relevant.

### 6.3 Copyright and export controls

Never assume that a user may export or redistribute an entire copyrighted Bible edition. Apply edition-specific licence rules, quotation limits, attribution and destination restrictions. Personal notes and original user-authored material remain exportable subject to privacy and applicable law. Public or commercial redistribution requires explicit rights.

### 6.4 Portability and deletion

Provide an understandable user-data export and account deletion workflow. Explain which records can be exported, what cannot be exported due to third-party rights, and how deletion affects backups and shared group records.

## 7. Commentary, annotations and research

Provide clearly separated commentary layers:

1. **Private personal notes:** private by default, local-first.
2. **Personal commentary:** user's own interpretation, observations and cross-references.
3. **Shared group commentary:** visible only to selected group members.
4. **Public commentary:** published under a chosen author identity and moderation policy.
5. **Editorial / expert commentary:** sourced, attributed, versioned and reviewed.
6. **AI-assisted study suggestions:** explicitly labelled as generated assistance, never presented as Scripture or authoritative doctrine.

Features:
- Attach commentary to a verse, passage, chapter, topic or study.
- Rich text, headings, references, citations, images and attachments.
- Version history and drafts.
- Tags, collections and search.
- Private, group, organisation and public visibility controls.
- Export and print.
- Report, correction and moderation workflows for public material.

Every commentary item must identify its author/source, visibility, creation/update time and any AI assistance. Do not present an individual's opinion as an official church or denominational position.

## 8. Groups, community and discussion

### 8.1 Group types

- Family reading group.
- Friends / accountability group.
- Small Bible study group.
- Church congregation group.
- Youth / children group with enhanced safeguarding.
- Class, seminary or training cohort.
- Ministry team.
- Public topic community, only after moderation capabilities exist.

### 8.2 Group capabilities

- Create private, invite-only or discoverable groups.
- Roles: owner, administrator, moderator, facilitator, member and read-only guest.
- Group reading plans and shared schedules.
- Passage-based discussion threads.
- Questions, replies, reactions and polls.
- Prayer requests with explicit visibility and optional expiry.
- Meeting schedule, attendance/participation and reminders.
- Shared study guides and permitted attachments.
- Group announcements and pinned resources.
- Moderation queue, reporting, blocking and member removal.
- Group-level privacy and retention settings.
- Export group-owned materials only with appropriate role permission and participant privacy controls.

### 8.3 Discussion safety

Provide reporting, moderation, anti-spam, rate limits, block/mute, child-safety controls, administrator audit logs and an appeals process. Private group discussions must not become public by default. Prayer and personal disclosures are sensitive: minimise collection, restrict access and avoid using them for advertising or model training.

### 8.4 Premium value

Basic participation should remain accessible. Premium value can come from group-owner administration tools, larger group capacity, advanced scheduling, structured study templates, facilitator dashboards, organisation management and storage—without charging ordinary participants simply to read or join a basic study.

## 9. Social publishing and creator studio

Build a **Create Studio** for producing attractive, attribution-aware material from Scripture and original user content.

Capabilities:
- Verse image cards with editable layouts.
- Multi-verse quote cards only within licence limits.
- Maale, Amharic and English typography templates.
- Short-form devotional and reflection cards.
- Study-guide excerpts.
- Event and reading-plan announcements.
- Audio narration scripts and subtitle files.
- Short-video storyboard and caption drafts.
- Sermon quote cards and presentation graphics.
- Brand kits for authorised churches and organisations.
- Scheduling calendar and publishing queue where external platform APIs permit it.
- Download assets or share through Android's system share sheet.

Social destinations may include WhatsApp, Facebook, Instagram, Telegram, YouTube and other supported services. Initially, use native share intents and downloadable assets. Direct account publishing, analytics or scheduled posting must be implemented only through official platform APIs and approved permissions; never scrape credentials or promise integrations that platforms do not permit.

Generated posts must preserve Bible edition attribution and distinguish verse text from commentary. Add review-before-publish; never auto-publish AI-generated religious content without explicit user action.

## 10. Sermon, teaching and professional workspaces

Offer structured tools for:
- Sermon preparation and sermon archive.
- Sermon outlines, manuscripts and speaker notes.
- Bible study lesson builder.
- Discussion question builder.
- Sunday school and youth lesson preparation.
- Course and curriculum builder.
- Reading and assignment schedules.
- Presentation and handout generator.
- Research notebook and bibliography.
- Passage lists and thematic studies.
- Ministry calendar and event planning.
- Team collaboration and document approval.

These tools must cite the sources used and make it easy to edit, review, export and present the final work. They are productivity aids, not substitutes for theological accountability or pastoral judgement.

## 11. AI-assisted study and research

AI may provide:
- Natural-language search over installed, licensed resources.
- Passage context and cross-reference discovery.
- Comparison of translations with explicit edition labels.
- Study question suggestions.
- Summaries of licensed study resources.
- Draft sermon and lesson outlines.
- Commentary organisation and tagging.
- Translation assistance for user-authored material.
- Social caption and graphic text drafts.
- Audio transcription and subtitle drafts where lawful.

### AI safeguards

- Never fabricate a verse, reference, quotation, source or scholarly claim.
- Cite the Bible edition and source materials used.
- Separate direct quotations, paraphrases and generated interpretations.
- Disclose uncertainty and missing source coverage.
- Do not use private notes, prayer requests or group discussions for model training without specific informed consent.
- Offer an AI-off mode and manual workflows.
- Require human review before publication, teaching distribution or pastoral use.
- Do not represent generated output as a church's official doctrine.

## 12. Integrations and interoperability

### 12.1 User-facing integrations

- Android share sheet and document providers.
- Calendar export (ICS) and supported calendar connections.
- Cloud storage connections for user-selected export/backup destinations.
- Email and messaging share workflows.
- Audio playback and supported Android media controls.
- Accessibility services and system text scaling.

### 12.2 Organisation integrations

Potential future integrations:
- Church websites and content management systems.
- Learning management systems.
- Church management systems.
- Organisation identity / single sign-on.
- Event and calendar platforms.
- Video meeting platforms.
- Payment and donation providers.
- Publisher and Bible content provider feeds.

Every integration requires a named provider, supported API, permission model, data-flow review, security assessment and fallback plan. No provider, API access, cost or compatibility is assumed until verified.

### 12.3 Developer platform

Future professional tier:
- Documented REST API.
- Scoped OAuth 2.0 authorisation.
- Webhooks for approved events.
- API keys for server-to-server integrations with rotation and revocation.
- Rate limits and usage quotas.
- Sandbox environment.
- Audit logs and integration health.
- Versioned schemas and deprecation policy.

Do not expose copyrighted scripture through an API unless the edition's licence explicitly permits it.

## 13. Membership and monetisation

### 13.1 Access principle

Guest and free registered users can read all legally available free Bible editions and use core reading functionality. Premium membership unlocks advanced workflows and capacity, not spiritual status.

### 13.2 Proposed tier ladder

All higher tiers inherit lower-tier benefits. Exact limits, prices, billing periods and final benefits remain unapproved until cost and demand validation.

| Tier | Intended customer | Proposed premium value |
|---|---|---|
| Free / Guest | Individual reader | Scripture reading, offline access, basic search, local personal tools |
| Silver | Committed individual | Expanded personalisation, larger study workspace, enhanced export options |
| Gold | Serious student / teacher | Advanced search, commentary organisation, study and teaching templates |
| Platinum | Pastor / creator / facilitator | Sermon and lesson builder, creator studio, advanced export and personal productivity |
| Diamond | Group leader / ministry professional | Group administration, collaboration capacity, scheduling and team workflows |
| Elite | Church / organisation / professional team | Organisation workspace, roles, analytics, integrations, API/webhooks and administrative controls |

The five paid levels are Silver, Gold, Platinum, Diamond and Elite. Free/Guest is not a paid tier.

### 13.3 Add-on services

Potential separately priced services, subject to feasibility and legal approval:
- Organisation seats and additional group capacity.
- Additional secure storage.
- Advanced export and publishing capacity.
- Premium licensed study libraries.
- Professional training and onboarding.
- Custom organisation branding.
- Integration/API usage packages.
- Transcription, audio processing or AI usage bundles.
- Managed migration and content preparation.

Do not charge twice for a feature already included in a user's tier. Display usage limits and overage costs before use.

### 13.4 Billing principles

- Clear monthly/annual price and currency.
- Explicit renewal terms.
- Upgrade, downgrade, cancellation and restore-purchase paths.
- Receipts and transaction history.
- Grace-period and expired-subscription behaviour.
- Refund and support policies.
- No dark patterns or forced upgrade interruptions.
- No premium lockout of free Bible content already installed.

## 14. Donations and sponsorship

Donations are voluntary and separate from subscription billing. Provide transparent purpose, suggested/custom amounts, receipts and optional acknowledgements. Sponsorships and grants must not influence Scripture text, translation integrity or editorial independence. No advertising should be inserted into Scripture reading.

## 15. Differentiation and market position

### 15.1 Strategic differentiation

Maale Bible should compete through a combination of capabilities rather than claiming that any single feature is unique:

1. **Maale-first product architecture:** not merely a translated English interface; Maale language, script, terminology, typography and user journeys are foundational.
2. **African multilingual reading and study:** independent interface and Bible-language selection, parallel reading and carefully governed local-language resources.
3. **Verified-content trust layer:** edition provenance, licence, attribution, completeness and integrity visible to users.
4. **Offline-first collaboration:** personal reading remains dependable without connectivity; online collaboration synchronises when available and resolves conflicts safely.
5. **One Scripture-to-workflow environment:** reading, commentary, groups, sermon preparation, teaching, export and publishing in a connected workspace.
6. **Creator and ministry studio:** locally appropriate multilingual social assets, lesson materials and presentations with attribution controls.
7. **Transparent access model:** free Scripture access, clear premium productivity benefits, no spiritual status hierarchy.
8. **Owner-governed content integrity:** source verification and explicit editorial boundaries for Scripture, commentary and AI output.

### 15.2 Competitive position

Public product materials show that established platforms already offer substantial capabilities. YouVersion describes Bible versions, offline access, plans with friends, notes, prayers, comments and sharing. Logos documents advanced search, notes, study workflows, sermon tools, research features and export/print capabilities. Maale Bible must not claim those individual capabilities as unique.

Its opportunity is to combine a genuinely Maale-first and African-language-first experience with verified local content, offline resilience, group and ministry workflows, creator publishing and transparent premium services.

### 15.3 Why users would choose Maale Bible

A user should choose it because:
- Their language is treated as a first-class product language.
- They can trust where Bible editions came from and what rights apply.
- It works reliably with limited or no connectivity.
- Their personal study, group learning and teaching workflows are connected.
- They can create and export useful work without being trapped in the app.
- They can collaborate without surrendering privacy.
- They receive advanced tools at transparent prices while free Scripture access remains protected.

## 16. Privacy, security and governance

- Private notes remain local/private unless the user explicitly enables sync.
- Sharing visibility is explicit at creation and change time.
- Group roles and access are enforced server-side.
- Subscription entitlements are server-authoritative.
- Payment status is verified server-side.
- Export operations check permissions and content rights.
- Organisation administrators cannot automatically read personal notes.
- Prayer requests and group disclosures are protected from public indexing.
- Moderation and audit logs are access-controlled.
- Account data export and deletion are documented.
- No secrets or production credentials in the public repository.

## 17. Visual design and interaction system

Retain and evolve the existing design foundation:
- Midnight `#0B1020`
- Deep Indigo `#171D35`
- Royal Gold `#D9B76E`
- Living Cyan `#55D6D0`
- Scripture White `#F5F1E8`
- Light Background `#F7F4ED`

Visual direction: Maale heritage × royal elegance × future-classic.

Requirements:
- Reusable design tokens and Material 3 components.
- Maale-first typography and script validation.
- Premium tier emblems with accessible labels.
- Adaptive phone/tablet layouts.
- Strong hierarchy, generous spacing and readable scripture.
- Light, dark and system themes.
- Subtle purposeful animation.
- WCAG 2.2 AA-aligned accessibility.
- Fast rendering on modest Android devices.
- Consistent empty, loading, offline, error and permission states.

## 18. Screen and workspace inventory

### Core
- Onboarding, language selection, Home, Bible library, edition details, Reader, search, downloads, audio, profile, settings.

### Personal study
- Notes, commentary, highlights, bookmarks, collections, research notebook, cross-reference view, export centre.

### Collaboration
- Group discovery, group home, member/role management, discussion thread, polls, prayer requests, group plan, calendar, moderation and reports.

### Create and publish
- Creator Studio, verse card editor, social post editor, sermon builder, lesson builder, presentation builder, export preview, publishing queue.

### Professional / organisation
- Organisation dashboard, teams, roles, courses, events, analytics, billing, integrations, API console and audit log.

### Membership
- Tier overview, comparison, checkout, subscription management, invoices, restore purchase and usage dashboard.

## 19. Phased implementation and acceptance gates

### A1 — Assessment and specification
- Freeze product principles and approved requirements.
- Define personas, user journeys, navigation and feature catalogue.
- Define privacy, rights, moderation and monetisation boundaries.
- Produce visual design package and tier entitlement matrix.

### A2 — Read-only repository reconciliation
- Inspect canonical main and existing UI branches.
- Map current implementation to the screen and feature inventory.
- Identify dependencies, conflicts and missing architecture.
- No unapproved code changes.

### A3 — Incremental implementation
Suggested sequence:
1. Design system, localisation and navigation.
2. Bible library and reader.
3. Personal study, commentary and export.
4. Identity, sessions and entitlements.
5. Groups and moderation.
6. Creator Studio and social export.
7. Sermon, lesson and professional workspaces.
8. Integrations and developer platform.
9. Subscription, donations and organisation billing.

### A4 — Verification
- Unit, integration and instrumentation tests.
- Offline and low-connectivity tests.
- Localisation and linguistic review.
- Accessibility and performance.
- Security, privacy and entitlement tests.
- Content-rights and export tests.
- Moderation and abuse-resistance tests.

### A5 — Deployment
Separate Owner approval required. No merge, signing, deployment or publication is authorised by this document.

## 20. Success measures

Measure real product value, not vanity metrics:
- Successful first reading session.
- Reading retention and voluntary plan completion.
- Search success and latency.
- Offline reading reliability.
- Translation completeness and user-reported language quality.
- Export success and repeat use.
- Group participation and safety outcomes.
- Creator workflow completion.
- Subscription conversion, renewal and cancellation reasons.
- Support response and issue resolution.
- Content provenance and licence coverage.
- Accessibility defects and crash-free sessions.

Do not collect sensitive spiritual behaviour for advertising. Analytics must be minimised, transparent and consent-aware.

## 21. Open decisions before A1 freeze

1. Exact feature quotas for each paid tier.
2. Whether organisation subscriptions are separate from individual Elite.
3. Which export formats are included in each tier.
4. Group size, storage and collaboration limits.
5. AI provider, privacy model, cost controls and allowed use cases.
6. Initial social platforms and official API availability.
7. Initial calendar, cloud storage and organisation integrations.
8. Payment providers and supported currencies/regions.
9. Moderation staffing, safeguarding and appeals policy.
10. Premium supplementary content catalogue and distribution rights.
11. Data retention and cloud backup policy.
12. Whether a web/desktop companion is in the initial product scope or a later release.

## 22. Explicit non-authorisations

This specification does not authorise:
- Scripture ingestion or redistribution beyond verified rights.
- Paid provider commitments or payment processing.
- External platform account publishing.
- Public release of AI-generated commentary.
- Automatic cloud sync of private notes.
- Main-branch changes, PR merge, signing, deployment or publication.

**A1 status:** Expanded specification prepared for Owner review. A2 repository reconciliation remains the next engineering gate.
