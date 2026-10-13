# Rebuild and emulator validation - 13 October 2026

## Result

The current Android app was rebuilt, installed and exercised on the Pixel-proportioned API35 emulator. The backend was rebuilt to include the changed activity catalogue. The app is left open on patient Today, in English.

## Fix made during checking

`SahayViewModel.kt`: mood history now follows **Save on this device**. Enabling saving stores existing session check-ins; disabling saving removes `mood_log` alongside saved chat/questions/journal. Clearing local data and sign-out already clear the mood history. The final build includes this fix.

## Verification

- Final `assembleDebug --offline --no-daemon`: **BUILD SUCCESSFUL in 44 seconds**, using one worker and a 768 MB heap with the emulator closed.
- Backend suite: **41 tests passed**. The first sandboxed run could not create sockets for three local HTTP tests; the permitted rerun passed all tests.
- The Android and backend activity catalogues match: **14 IDs**.
- All 14 activity destinations opened: breathing, grounding, meditation, body scan, safe place, sound, wind-down, matching pairs, stretch, art, small comforts, worry box, journal and encouragement notes.
- Stretch (5 steps), body scan (5), safe place (4) and small comforts (3) progressed to completion; small comforts opened the journal with its prompt.
- Worry release cleared the text. It was absent after reopening and absent from device preferences.
- Wind-down started and stopped when the app actually reached the background. The full 15/30-minute fade duration was not timed.
- Weekly mood rendered; existing check-ins persisted when saving was enabled, restored after cold start/arrival, and were removed from disk when saving was disabled.
- Hindi body-scan instructions and controls rendered without clipping. English was restored afterward. This is a rendered check, not bilingual language-quality validation.
- Fictional clinician login opened Shared visit briefs; cold start restored that role's queue. Messages opened Patient conversations. Settings/sign-out returned to patient arrival, then Today.
- The new serif headings and reachable bottom exercise controls were visually inspected. No Sahay entries were found in the final crash buffer.
- `git diff --check` passed.

## Graphify

Used the existing graph for navigation/session/model paths and refreshed the code graph after the storage fix. Current outputs:

- [Interactive graph](graphify-out/graph.html)
- [Graph report](graphify-out/GRAPH_REPORT.md)
- [Graph data](graphify-out/graph.json)

905 nodes, 2,011 edges, 46 communities; structural update used no model API tokens. Existing semantic context was retained.

**Coverage limitation:** graphify reports partial Kotlin extraction for `AccountUi.kt` and `WellbeingUi.kt`, plus no symbols for three Gradle Kotlin configuration files. Some new exercise functions are missing from the graph. The Kotlin compiler succeeded, and those flows were checked directly; graph coverage is not equivalent to compile/runtime correctness.

## Emulator and memory

API35, 720 x 1616 pixels at 280dpi (about 411 x 923dp). Guest RAM was verified from `/proc/meminfo` after explicitly setting QEMU to 1536 MB. The emulator otherwise raised the requested RAM to 2560 MB and stalled under the initial cap.

The final instance is isolated in `sahay-final-check-20261013.scope`, with MemoryHigh 2200 MB, MemoryMax 2250 MB and no additional swap. This PC's swap was already full. Only the idle Gradle daemon belonging to this project and the emulator instance created for this check were stopped; unrelated applications/containers were left running.

## Live AI limitation

The current backend model is `space-bunny-free` at the direct OpenCode endpoint. The real connection test still returned **HTTP 403**, reporting that free-tier use is restricted to OpenCode. The local proxy on port 8787 is currently not listening. Successful live AI replies were not verified in this run; earlier Gemma results remain evidence for Gemma only. Native activities and account/care flows were checked independently.

## Artifacts

- [Installable APK](android/app/build/outputs/apk/debug/app-debug.apk)
- [Activity opening checks](artifacts/qa/2026-10-13/activity-open-checks.json)
- [Activity and mood checks](artifacts/qa/2026-10-13/activity-flow-checks.json)
- [Role and language checks](artifacts/qa/2026-10-13/role-language-checks.json)
- [Final Today screen](artifacts/qa/2026-10-13/today-final.png)
- [Hindi body scan](artifacts/qa/2026-10-13/body-scan-hindi.png)
- [Restored weekly mood](artifacts/qa/2026-10-13/mood-week-restored.png)
- [Clinician review](artifacts/qa/2026-10-13/clinician-review.png)
- [Clinician messages](artifacts/qa/2026-10-13/clinician-messages.png)

APK SHA-256: `df1a82864014f59a135b254072cd2c33b09c90cbbfa9769e80a12e20b6e9fed2`.
