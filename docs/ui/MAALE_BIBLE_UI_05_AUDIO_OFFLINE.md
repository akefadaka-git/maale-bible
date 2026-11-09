# Maale Bible UI-05 — Audio and Offline Listening Foundation

## Objective
Introduce a dedicated listening destination and a calm, responsive player/library experience without implying that audio assets or playback infrastructure already exist.

## Implemented
- Dedicated Listen destination in bottom navigation.
- Responsive listening screen with width-constrained content.
- Player presentation with waveform motif, track metadata placeholders, disabled seek/progress and playback control.
- Audio library empty state with future book/chapter/language/narrator discovery described as planned.
- Offline listening status card describing future download queue, storage usage, integrity checks and removal controls.
- Explicit unavailable states for playback, library assets and downloads.

## Not implemented
- Audio playback engine/service, background playback, media notification/session, audio focus, streaming, audio downloads, download queue, storage management, asset integrity verification, chapter/verse synchronization, narration/language metadata, or audio files.
- No playback or download controls are enabled.

## Integrity and scope
- No audio recordings, copyrighted assets, external endpoints, new dependencies, permissions, storage writes, or persistence added.
- The UI does not fabricate a narrator, language, duration, recording, or scripture/audio pairing.
- Future audio sources must be checked independently for provenance, licence/permissions, attribution, integrity, and compatibility before integration.
- No merge, signing, deployment, or publication is authorized.

## Verification
- Source diff and exact commit should be reviewed.
- Android build, unit/instrumented tests, emulator/device matrix, accessibility/TalkBack audit, and real-device audio tests remain NOT RUN unless supported by exact evidence.
