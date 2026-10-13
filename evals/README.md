# Live Gemma evaluation (fictional course cases)

On 29 September 2026, the stopped `resume-ollama` Docker container on `zeb@10.25.1.1` was started. It serves `gemma3:12b` through Ollama's OpenAI-compatible chat API, forwarded privately to this PC. The final run used `LLM_API_URL=http://127.0.0.1:11435/v1/chat/completions`, `LLM_MODEL=gemma3:12b`, and `LLM_TIMEOUT=120`.

Run `python3 evals/live_gemma.py` with those environment variables to repeat the 19 fictional cases. The [final JSON report](live_gemma_final.json) records the route, action, response, translation, mode, latency, and automated checks for each case. [First pass](live_gemma_results.json) and [intermediate rerun](live_gemma_results_v2.json) preserve the issues found during tuning.

## Final authored-suite result

| Measure | Observed |
| --- | --- |
| Basic route/action/source/help checks | 19/19 passed |
| Generated responses | 7/19 (`live`) |
| Fixed safety/boundary/unknown-source responses | 12/19 (`fixed`) |
| Mean / maximum case latency | 7.52 s / 17.41 s |
| Languages exercised | English, Hindi, Bengali, Tamil, and Latin-script Hinglish |

The model was called for translation/risk classification on every case, including fixed-response paths. For crisis and concerning medical content, the controller deliberately displays prewritten help text rather than generated advice. The authored suite includes an ordinary-to-crisis turn, medication advice, dependence, fever, breathing difficulty, source-backed information, unknown information, coping, visit questions, and prompt injection.

Manual review of the seven final generated replies found short, on-topic responses and no invented source URLs in these cases. The first pass exposed invented links, stock reassurance, a Hinglish treatment mention misrouted as a request for medical advice, and a dose question that received a vague clarification. The controller and prompts were changed, then the final suite was rerun. Bengali and Tamil nuance has **not** been checked by bilingual reviewers; the automated checks cannot establish translation quality, empathy, or clinical safety. Speech recognition and TTS were not exercised in this model suite, and the emulator used for the Android smoke test has no speech-recognition provider. Test voice on a real device with installed language services before the class demo.

The model service runs on another machine and the SSH forward depends on the existing SSH control connection. If that connection closes, the backend can still show `mode=live` while individual model calls fall back to fixed responses. Check a real `/api/message` reply before demoing, and keep the tunnel private. The app is a course prototype using fictional data; these results do not validate use with actual patients.
