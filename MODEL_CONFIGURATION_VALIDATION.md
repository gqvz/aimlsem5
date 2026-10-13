# Model configuration update - 12 October 2026

## Delivered

- Backend default: `https://opencode.ai/zen/v1/chat/completions` / `space-bunny-free`, configured in private `.env` and documented in `.env.example`.
- Android Settings has a full AI model card: URL, model name, Save and test model, real status/error and Use backend default.
- Device overrides persist in preferences and apply to chat, translation, activity planning and visit preparation. All invoked agents receive the same selected client. Overrides do not mutate another device's/backend default.
- `POST /api/model/test` makes a short real provider call. Public health exposes sanitized default configuration, independently of model connectivity.
- Provider failures are attached as `provider_error` to fixed assistant fallbacks and surfaced in Android. Existing locally recognized urgent routes still skip model calls.
- Backend credentials are attached only to its exact configured endpoint; changed endpoints do not inherit that key. Redirects are rejected, public configuration redacts URL credentials/query strings, and provider bodies are not echoed to the UI.
- Backend rebuilt/deployed with its persistent volume. The unused, failing project SSH bridge was stopped; remote Gemma remains an optional Compose configuration.

## Verification

- `python3 -m unittest discover -s tests -q`: **40 tests passed**. Nine new tests exercise device model isolation, invalid configuration, real test-call payloads, provider errors, credential redaction, urgent bypass, redirects and real local HTTP dispatch through the API and agents.
- Local HTTP provider test: changed model reached all four model calls in a complete Language/Supervisor/Emotional support/Review turn; the default model remained unchanged for the following default request.
- `assembleDebug --offline --no-daemon`: **BUILD SUCCESSFUL**, 1 minute 11 seconds. Emulator was closed during the build.
- Updated APK installed on Sahay_Pixel_9/API35. Settings populated the requested backend URL/model; Save remained visible above the keyboard.
- Actual Android Save and test received/displayed the backend provider failure. URL and model preferences were verified after force-stop/relaunch.
- Docker backend healthy; `git diff --check` passed.

## Live-provider limitation

Direct curl and the deployed backend's real connection test both returned **HTTP 403** with `FreeTierError`: “OpenCode's free tier can only be used from within OpenCode.” The selected endpoint is saved, but successful standalone Space Bunny generation is not verified. An externally authorized provider configuration is required; no OpenCode-client impersonation or alternate model was introduced.

See [OpenCode Zen documentation](https://opencode.ai/docs/zen/) for the advertised endpoint/model. Earlier Gemma evaluation remains evidence for Gemma, not this provider.

## Artifacts

- APK: `android/app/build/outputs/apk/debug/app-debug.apk`
- Keyboard check: `artifacts/qa/model-settings-keyboard.png`
- Visible provider failure: `artifacts/qa/model-settings-provider-error.png`
- Current design: `ARCHITECTURE.md`; the architecture PDF remains the 5 October snapshot.
