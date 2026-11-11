# Sahay

Sahay is a course prototype for emotional support for adults affected by cancer in India. The **native Android app** is in [android/](android/README.md); the original web prototype remains available for desktop review. Both use the mobile visual direction and image assets from the team's Google Stitch design. They support English, Hindi, Bengali, and Tamil; typed chat; speech input and spoken replies; a short grounding exercise; cited information cards; editable care-team questions; and direct human-help links.

The app is an AI assistant, not a clinician. It does not diagnose, prescribe, monitor conversations, call anyone, or automatically notify a care team. The **Help** button is always available. India emergency **112** and Tele-MANAS **14416** are linked there, along with a number the user may enter for their own care team. A patient may sign in and explicitly share an editable visit brief with a demo clinician. The clinician can manually review that brief; it is **not an emergency inbox**. These numbers and the medical information cards come from the official sources listed below.

## Run the Android app

See [android/README.md](android/README.md) for APK build, emulator, device, and backend connection instructions. The Android app uses Jetpack Compose and can show a fixed on-device demo response if this server is unavailable. Live AI translation and replies require the Python backend and a configured model endpoint; a private SSH-forwarded Ollama endpoint needs no API key.

## Run the web prototype and backend

Requires Python 3.10+ and a modern browser. No Python packages or build step are required.

```bash
python3 server.py
```

Open <http://127.0.0.1:8000/>. The server binds to localhost by default. To use another port, set `SAHAY_PORT`; to access it from another device on your own network, set `SAHAY_HOST=0.0.0.0` and use the host machine's address. Account sessions and consented shares are stored in a local SQLite database, but this development server has no TLS; keep it on localhost or a trusted test network and use fictional accounts only.

## Clinician demo and accounts

The Android app includes Stitch-inspired role entry, patient registration/sign-in, invited clinician sign-in, a patient preview-and-consent share screen, a clinician review queue/detail view, and an admin account/audit view. The patient chooses the clinician, brief, questions, and optional note. Raw chat and audio are never sent automatically. Revocation removes the brief from the clinician queue, though it cannot undo anything a clinician already saw or copied. There is no real hospital integration or clinical monitoring.

Create fictional admin and clinician accounts once, then open the Android app:

```bash
python3 bookkeeping.py seed-demo > .demo-credentials
chmod 600 .demo-credentials
```

The generated sign-in details are in `.demo-credentials` (ignored by Git). Patient accounts can be registered in the app. The admin can create a clinician invitation code; clinicians can activate an invited account in the app. The database `sahay_demo.sqlite3` is local and ignored by Git. The Android app encrypts its session token with Android Keystore, checks `/api/me` on launch, and opens the matching patient, clinician, or admin home. Server sessions expire after 12 hours. Settings on every role screen shows account identity, display-name and password controls, preferences, and sign-out. Changing a password invalidates all sessions for that account. See [ARCHITECTURE.md](ARCHITECTURE.md) for the data flow and [Stitch design references](design/stitch/) for the account and sharing layouts.

## Model setup and switching

The active local backend uses Kratos Gemma (`gemma3:12b`) through `compose.kratos.yaml` below. `.env.example` provides the optional OpenCode configuration; configure the endpoint and model in `.env` for another provider, then recreate the backend:

```bash
docker compose up -d --build backend
```

**Android → Settings → AI model** shows the complete endpoint URL and model name. Edit both and tap **Save and test model**. The selection persists on that device and is sent as `model_config: {url, model}` on chat, translation, plan and brief requests. It does not change another device's model or sign you out. **Use backend default** removes the override. The backend address is a separate connection setting.

The provider must implement OpenAI-compatible Chat Completions. Some providers require `LLM_API_KEY` on the backend; this key is used only for requests to the configured `LLM_API_URL`, never automatically forwarded to a changed endpoint. Endpoint redirects are rejected. Do not embed keys in a URL or put provider credentials in the APK.

`POST /api/model/test` performs a real short provider call and returns `{ok, model, error?}`. `/api/health` exposes the default URL/name without credentials, but only reports configuration presence, not provider availability. Provider failures also return `provider_error` alongside fixed assistant fallbacks so the app can explain the failure.

On 12 October 2026, a direct unauthenticated request to the requested OpenCode endpoint returned **HTTP 403**, with `FreeTierError`: “OpenCode's free tier can only be used from within OpenCode.” The configuration is implemented, but successful live Space Bunny use requires external access authorized by the provider. The connection test reports this restriction; it is not treated as a live model response. See [OpenCode Zen documentation](https://opencode.ai/docs/zen/) for its API/model access rules.

All eight agents use the chosen endpoint/model through the existing bounded graph. Provider errors retain fixed replies; recognized explicit urgent routes skip slow model calls. Clear both `LLM_API_URL` and `LLM_MODEL` to select deterministic backend demo behavior. The existing `compose.gemma.yaml` remains an explicit alternative for the private remote Gemma bridge and overrides the default endpoint/model when selected.

## Demo flow

1. Choose a language and start.
2. Send a normal worry, such as “I feel anxious before my next visit.”
3. Open **Support paths** and try the grounding timer.
4. Open **Information** for four short NCI cards and direct source links.
5. Add and copy a question for the care team.
6. Type “I have a fever” or a self-harm statement to see the fixed help route. Use fictional test text only.

Conversation history and the question list stay in memory by default. An opt-in toggle stores them in this browser's local storage; **Clear conversation** removes them. A saved care-team number is stored on this device until cleared. **Quick Hide** covers the screen immediately. The server does not store transcripts or chat history.

## Check

```bash
python3 -m unittest discover -s tests -v
python3 -m py_compile engine.py server.py
node --input-type=module --check < web/app.js
node --input-type=module --check < web/i18n.js
```

The tests cover urgent routing in all four languages, treatment advice boundaries, citations, unknown information, and unsafe generated text fallback. For the class evaluation, also manually test speech in each language on the actual demo browser and device, mixed-language utterances, background noise, denied microphone permission, and the emergency route after a normal turn. Rule-based checks are intentionally limited and cannot safely interpret every possible phrasing.

## Sources and credits

- [NCI: Emotions and Cancer](https://www.cancer.gov/about-cancer/coping/feelings)
- [NCI: Meditation and Relaxation](https://www.cancer.gov/about-cancer/coping/feelings/relaxation)
- [NCI: Communication in Cancer Care](https://www.cancer.gov/about-cancer/coping/adjusting-to-cancer/communication-pdq)
- [NCI: Infection and Neutropenia during Cancer Treatment](https://www.cancer.gov/about-cancer/treatment/side-effects/infection)
- [India Emergency Response Support System](https://112.gov.in/)
- [Tele-MANAS, Directorate General of Health Services](https://dghs.mohfw.gov.in/national-mental-health-programme.php)

The emblem, courtyard image, and visual language were adapted from the team's Stitch project `Oncology Wellbeing AI Assistant`. Bundled Noto fonts use the license in [web/assets/fonts/LICENSE](web/assets/fonts/LICENSE). The [ARCHITECTURE.md](ARCHITECTURE.md) file records the broader design, rationale, and planned evaluation.

## Android overhaul (October 2026)

The Android app now opens with an optional interactive garden pause. **Today** supports a mood/energy check-in and an agent-created activity plan. **Explore** opens fourteen working activities: comfortable breathing, five-senses grounding, guided meditation, a slow body scan, a safe-place visualisation, locally generated rain/white/brown noise, a fading sleep wind-down, small comforts, a worry box that stores nothing, garden matching pairs, a gentle guided stretch, a drawing canvas with undo/export, a private journal, and explicitly fictional encouragement notes. There are no streaks or compulsory timers. Activities work without the backend; the plan uses the Activity agent. Primary new labels and grounding/breathing guidance are bundled in Hindi, Bengali and Tamil as well as English. Secondary explanations and clinician/admin copy remain English-first.

**Settings** is a full page with account identity, name/password editing, sign-out, language, larger text, reduced motion, volume, voice provider, local saving/clear controls, support, agent handoffs and connection settings. Clinicians return to their queue on relaunch. Ordinary AI chat, journal content and speech clips are never automatically sent into care conversations.

**Care team** supports two-way notes and unsolicited PDF/image/text attachments. Either role can start a conversation from the opposite-role demo directory. Clinicians can request a medical file; patients can attach a response or upload independently. Attachments are limited to 5 MB each, two per API message (one in the current Android composer). Download authorization checks conversation membership. This is an asynchronous course workflow. It does not interpret files, promise medical review, or deliver emergency monitoring. Demo directories are not a production patient-assignment system.

The backend now uses the eight-agent graph in [agents.py](agents.py), with separate role instructions, shared state, tool allowlists, supervisor delegation and safety/review vetoes. A single configured Gemma model serves the model-backed agents. `agent_trace` exposes public tool decisions; **Settings → AI activity** shows the last chat/plan trace. Deterministic urgent responses skip slow model calls. Activity planning uses only the catalogue; visit preparation returns a patient-editable draft and never sends it.

### Docker backend

```bash
docker compose up -d --build
docker compose exec -T backend python bookkeeping.py seed-demo > .demo-credentials
chmod 600 .demo-credentials
docker compose logs --tail 20 backend
```

Backend URL: `http://127.0.0.1:8000`; Android Emulator uses `http://10.0.2.2:8000`. SQLite accounts, sessions, care files, shares and audit records persist in the `sahay-data` named volume. The nonroot container has a 384 MB memory limit and health check. `docker compose down` retains the data; `down -v` deletes it. This PC uses the installed **rootless** Docker context (`docker context use rootless`), managed by `systemctl --user start docker`. The existing local demo database was copied into the Docker volume for this session, preserving the credentials in `.demo-credentials`.

To enable AI, copy `.env.example` to `.env` and supply the actual `LLM_API_URL` and `LLM_MODEL`. A host tunnel must be reachable from the container; a host loopback URL is not the container's loopback. Rootless Docker also isolates host loopback. Prefer forwarding the model inside the Docker network or use a reachable private endpoint. Never expose a shared model server publicly just to make the demo work.

### Future speech APIs

The app currently offers installed Android recognition/TTS and a selectable provider mode. Set these **backend** environment variables when APIs arrive:

- `STT_API_URL`: complete transcription endpoint; optional `STT_API_KEY`, `STT_MODEL`.
- `TTS_API_URL`: complete synthesis endpoint; optional `TTS_API_KEY`, `TTS_MODEL`, `TTS_VOICE`.

`POST /api/speech/transcribe` accepts `{base64, language}` for an AAC/M4A clip and forwards multipart `file`, `model`, `language`, `response_format=json`; it returns `{text, language}`. `POST /api/speech/synthesize` accepts `{text, language}` and forwards an OpenAI-compatible JSON speech request returning MP3 bytes. The current synthesis adapter relies on the selected voice/model understanding the input script; providers needing explicit language fields require an adapter adjustment. Clips are removed after transcribing/canceling; the user reviews the transcript before sending. Missing providers return a clear 503; text remains usable. Mocked contract tests pass; actual audio quality and language support still require the supplied providers and a microphone-enabled device.

See [OVERHAUL_VALIDATION.md](OVERHAUL_VALIDATION.md) for current test evidence and remaining checks. The web prototype retains its older UI; the overhaul is in the requested native Android app.

### Gemma on the authorized SSH host

The optional `compose.gemma.yaml` adds a small SSH bridge inside the private Docker network. It publishes no model port. The authorized existing `resume-ollama` container on `zeb@10.25.1.1` contains `gemma3:12b` and was started for this demo.

```bash
# In the terminal where your SSH key is loaded:
export SAHAY_SSH_AGENT="$SSH_AUTH_SOCK"
docker compose -f compose.yaml -f compose.gemma.yaml up -d --build
```

The host must already be trusted in `~/.ssh/known_hosts`. The bridge mounts the agent socket, not a private key; after an agent restart, update `SAHAY_SSH_AGENT` in `.env` or your shell and recreate `model-tunnel`. This agent-based bridge is an alternative to the active mux bridge below. Use the same two Compose files for managing the running model-enabled setup. Plain `docker compose up` can run the offline backend; remove the bridge URL from `.env` to select deterministic demo mode. The class runtime does not require an extra local Gemma copy or large local model memory.

### Mobile UI refinement

See [UI_REFINEMENT.md](UI_REFINEMENT.md) for the UX research, shared spacing and controls, reachable task actions, role navigation, rendered checks and screenshots.

### Reuse an existing Kratos SSH mux

The active setup uses `compose.kratos.yaml`. Set `SAHAY_SSH_MUX` in the ignored `.env` to the current **Zeb** master socket under `/run/user/1000/kssh-*`; verify its identity using `ssh -F /dev/null -S "$SAHAY_SSH_MUX" zeb@10.25.1.1 whoami`. The existing remote `resume-ollama` container must be running.

```bash
docker compose -f compose.yaml -f compose.kratos.yaml up -d --build
```

The bridge streams each private Docker connection through the existing SSH master to remote loopback port 11434. It mounts the mux socket, requires no loaded key, and publishes no model port. The socket grants access to that SSH session: keep it limited to this trusted bridge. Closing the master breaks the connection; after reconnecting, update `SAHAY_SSH_MUX` and recreate `model-tunnel` with the same Compose files. There is no automatic new SSH login when the master disappears. Android Settings → **Use backend default** clears any old per-device provider selection.

## Ember theme, bedtime reminder and Ayurveda (November 2026)

Both clients now use the **Ember** terracotta palette from the team's design file: Petal and Sandy Cream surfaces, Blush primary buttons and selections, Burnt Sienna edges, switches and focus, Clay accent text, Olive for help, and Onyx text. Headings use DM Serif Display and interface text uses Inter (both bundled, SIL Open Font License); Hindi, Bengali and Tamil headings stay in the sans. On Android the palette lives in `EmberTheme.kt`; on the web it is the Ember block at the end of `web/styles.css`.

**Bedtime reminder.** From 10 pm to 5 am, Today (Android) and the Conversation and Support paths views (web) show a "Time to rest" card with a link to the wind-down activity (Android) or a slow breath (web). It can be dismissed for the night. Android also shows a one-line reminder once per night on whichever screen is open. Nothing runs outside the app.

**Ayurvedic self-care.** Today and Support paths show a different Ayurvedic practice each day, and a full guide covers daily practices (dinacharya, nadi shodhana, bhramari, abhyanga, padabhyanga, warm meals, evening routine, restorative yoga) and herbs traditionally used for mind and body (tulsi, ashwagandha, brahmi, jatamansi, haldi, shunthi, amla, triphala). Herbs are described without doses, each with interaction cautions, and the guide asks people to check with their oncologist or pharmacist first. It is listed in the backend activity catalogue as `ayurveda`.
