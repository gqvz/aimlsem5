# Kratos LLM restored — 13 October 2026

- Reused the active Kitty/OpenSSH master authenticated as `zeb` on Kratos (`10.25.1.1`). Started the existing stopped `resume-ollama` container; its installed model is `gemma3:12b`.
- Added `compose.kratos.yaml`, `Dockerfile.mux`, and `kratos-stream.sh`. A small private Docker bridge sends each model connection through the existing SSH master using `ssh -W`. No model port is published and no private key is copied. The backend remains healthy on host loopback port 8000.
- Set the ignored `.env` to Gemma and the current mux socket; cleared the backend provider key. Cleared the Android emulator's previous custom provider selection using **Use backend default**.
- `/api/model/test` returned `ok: true`, model `gemma3:12b`.
- Ran all **22 fictional evaluation cases** from inside the deployed backend container: **22 passed**. Ten cases produced live generated responses (plans, visit draft, English/Hindi/Bengali/Tamil/Hinglish chat, doctor questions, curated information). Twelve used intended fixed responses, including explicit urgent cases. Trace checks verified the bounded multi-agent graph; drafts show specialist delegation and Review approval.
- Sent a fictional hospital-appointment worry from Android Talk and received a Gemma reply asking which aspects caused concern. The automation typed the fictional sentence twice while retrying focus; this was an automation artifact. The emulator is left on Talk.
- Evidence: `evals/kratos_mux_results_2026-10-13.json`, `artifacts/qa/2026-10-13/kratos-gemma-chat.png`.
- Verified a missing mux fails without starting a fresh SSH connection. Closing/replacing the master requires updating `SAHAY_SSH_MUX` and recreating the bridge. Keep the terminal/session owning this master open.
- `sh -n kratos-stream.sh` and `git diff --check` passed. Graphify refreshed the code graph: 916 nodes, 2020 edges, 43 communities. Existing Kotlin extractor partial-parse warnings remain; no Android source changed or APK rebuild was needed.

These are course-prototype routing and integration checks, not evidence of clinical effectiveness or native-speaker language review.

## Start/manage this setup

```bash
docker compose -f compose.yaml -f compose.kratos.yaml up -d --build
```
