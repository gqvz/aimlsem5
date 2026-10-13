# Sahay overhaul validation — 5 October 2026

## Delivered

Native Compose patient arrival/Today/Explore, nine functioning activities, full-page settings, patient/clinician care conversations and files, clinician requests with patient fulfillment, future STT/TTS adapters, Docker backend and an explicit eight-agent graph. Stitch's teal identity and role screens are preserved. New calm surfaces and interactive activities were designed directly in Compose.

## Evidence

- Kotlin/Compose APK build succeeded with one worker and a 1200 MB heap; no Gradle daemon remains between builds.
- 31 Python tests cover multilingual urgent paths, output vetoes, citations, supervisor delegation, catalogue constraints, brief review, account roles/password/session revocation, participant-only file access, unsolicited files, request fulfillment, invalid/oversized files, inactive participants, and mocked speech provider contracts.
- Real Docker HTTP flow passed: clinician login, patient registration, clinician request, patient file fulfillment, unsolicited clinician upload and authenticated download. SQLite records persisted through container rebuild/restart.
- Live Gemma evaluation: 22/22 fictional cases passed all checks in `evals/overhaul_gemma_results.json`. Initial run had three routing mismatches (ordinary Tamil worry, dependency, medication prompt injection); calibrated classification instructions fixed these. The initial observations are preserved in `evals/overhaul_gemma_initial.json`, with a focused recheck in `evals/overhaul_routing_recheck.json`.
- Docker backend → private Docker SSH bridge → existing remote `resume-ollama` → `gemma3:12b` produced a live ordinary reflection with Language/Safety/Supervisor/Emotional support/Review handoffs. No model port is published by the bridge.
- API35 Pixel 9 proportion emulator: 720×1616 pixels, density 280, 1536 MB guest RAM, two cores. Emulator is isolated in a systemd scope with a 3300 MB hard memory limit; backend is capped at 384 MB. Builds are performed with emulator closed after the earlier OOM.
- Emulator observations: interactive arrival, Today plan → launchable suggestions, activity library, breathing start/pause/countdown, matching-card reveal, drawing and PNG export to `Pictures/Sahay`, sound playback and stopping on background, full settings and Hindi labels, clinician queue and session restoration, conversation attachment display, and an actual Android document-picker PNG upload confirmed in SQLite. Screenshots are in `artifacts/qa`.

## Practical limits and remaining device checks

- The 22-case result is a small fictional regression set, not clinical validation. Rule and model risk classification can still misroute novel wording. Ordinary help remains available independently of the model.
- The final APK was installed and clinician restoration → full Settings → sign-out passed again. The final UI also includes keyboard-aware form spacing, direct Help/Quick hide on the new full pages (keyboard check confirmed the submit button remains reachable), and background cancellation of voice playback.
- No real STT/TTS provider has been supplied. API contract tests use mocks; recognition accuracy, accents/code switching, microphone audio, voices and playback quality require the actual APIs/device. Missing-provider status and readable text fallback are implemented.
- Encouragement notes are fictional; no live peer community/moderation service is implemented.
- Secondary explanatory text and clinician/admin interfaces remain English-first; core conversation/help/account controls plus the new activity labels and principal exercise guidance are bundled in four languages. Translations still need bilingual student review.
- Care directories use synthetic active accounts for the course. There is no EHR/real identity verification, emergency monitoring, unsend, malware scanning, or AI interpretation of medical files.
- Docker uses a persistent SQLite volume and local HTTP for this demo. Remote Gemma uses the authorized existing container. SSH agent restarts require updating its socket path for the optional Docker bridge.

## Reproduce

```bash
python -m unittest discover -s tests -v
# With the key loaded and SAHAY_SSH_AGENT set:
docker compose -f compose.yaml -f compose.gemma.yaml up -d --build
# Host tunnel, if not already running:
ssh -N -L 127.0.0.1:11435:127.0.0.1:11434 zeb@10.25.1.1
LLM_API_URL=http://127.0.0.1:11435/v1/chat/completions \
LLM_MODEL=gemma3:12b LLM_TIMEOUT=120 \
python evals/live_gemma.py --output evals/overhaul_gemma_results.json
# Close the emulator before building:
./android/gradlew -p android assembleDebug --offline --no-daemon
```

See `android/README.md` for the modern emulator launch command and `README.md` for speech contracts and care flows.
