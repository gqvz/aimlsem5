# Sahay rewrite plan (non-UI)

Branch: `rewrite/backend`. Scope: backend, API contract, safety pipeline, data layer,
Android non-UI layers (networking, state, persistence). Out of scope: all Compose UI,
the design system, screen layout and visual copy. That is being redone separately.

Status: **plan only, no code changed yet.** Decisions that need a human are in section 7.

## 1. What I read

`engine.py`, `agents.py`, `server.py`, `bookkeeping.py`, `care.py`, `speech.py`, the
tests, and the Android view model, session store and networking calls. I did not read
`web/app.js` line by line; it is treated as a second client of the same API.

## 2. Findings

Everything marked **verified** was reproduced by running the code, not just read.

### Safety (highest priority, this is a cancer-patient mental-health app)

| # | Finding | Evidence |
|---|---------|----------|
| S1 | Bengali medication-dose detection is broken. The pattern for "ওষুধের মাত্রা" starts with a Devanagari `ओ` (U+0913), not a Bengali `ও`. | **verified**: `আমার ওষুধের মাত্রা কত?` is not matched |
| S2 | Output safety filter is English-only. Hindi, Bengali and Tamil replies are only checked by the Review agent, which is the same LLM that wrote them. | **verified**: an unsafe Hindi reply passes `_passes_output_check`; the English equivalent is blocked |
| S3 | Crisis detection is a fixed phrase list. It misses ordinary wordings and typos, and with no model configured (demo mode) nothing else backs it up. | **verified**: `i want to kil myself`, `I dont want to be alive anymore`, `no reason to go on` all score `ordinary` |
| S4 | Safety rules, response copy, and routing live in the same 350-line file as the HTTP model client. | read |
| S5 | A normal turn makes up to five sequential LLM calls (Language, Supervisor, draft, translate back, Review) with a 60 s timeout each. Worst case is five minutes for a user who may be distressed. | read |

### Security

| # | Finding |
|---|---------|
| X1 | `POST /api/message` and `POST /api/model/test` take a client-supplied `model_config.url`. The server will POST to any http(s) host, including internal addresses. This is a server-side request forgery vector, and it is unauthenticated. |
| X2 | `/api/message`, `/api/model/test`, `/api/speech/*` need no login and have no rate limit, so anyone who can reach the server can spend the LLM, STT and TTS budget. |
| X3 | No login throttling or lockout. Sessions are never pruned. |
| X4 | Slow-body clients are not bounded: the server reads `Content-Length` bytes with no read timeout. |
| X5 | Authorisation is hand-checked in each handler (`_auth(..., roles)` plus per-query ownership). It works today, but one forgotten check is an IDOR, and nothing enforces it structurally. |

### Structure

- Two hand-rolled dispatchers (`care.handle`, `bookkeeping.handle`) returning `None` to fall through, plus a `startswith` allow-list in `server.py` that must be kept in sync by hand.
- No request or response schemas; validation is `_string()` calls and `isinstance` checks.
- No migrations: schema is `CREATE TABLE IF NOT EXISTS` at start-up.
- `agents.py` imports private helpers (`_json_object`, `_matches`, ...) from `engine.py`; agents share one mutable `State` with an `issue` flag whose meaning changes by agent.
- The activity catalogue exists in three places (Android, `agents.py`, web); I added a parity test but it is a patch.
- Android: networking is `Thread {}` + `main.post` with a `sessionEpoch` counter; one 896-line view model owns auth, care, admin, speech, plans, settings and navigation.
- Safety copy is duplicated across four languages inside `engine.py` as Python dicts.

### Worth keeping (do not regress)

Rule-based risk gate that runs before any model call; no request logging; scrypt password
hashing and hashed session tokens; redirect rejection and credential isolation on model
endpoints; audit trail; consent-gated sharing; magic-byte checks on uploads; the offline
and demo fallbacks.

## 2a. Progress

| Phase | State |
|-------|-------|
| 0 Safety net | done: safety corpus (66 labelled cases), API contract tests, `ruff`/`pytest` config |
| 1 Dangerous fixes | done: S1, S2, X1-X4 |
| 2 Safety module | done: `sahay/domain/safety` (normalisation, typo repair, wider multilingual rules), S3 closed on the corpus |
| 2b Output guard | done: `sahay/domain/guard`, see below |
| 3 API layer | done: FastAPI + SQLAlchemy + Alembic behind the same URLs; legacy handlers removed; OpenAPI snapshot in `contract/openapi.json` |
| 4 Pipeline | done: `sahay/domain/pipeline.py`; ordinary turn is 3 model calls (was up to 6), one shared deadline, guard + review merged into one judge call |
| 5 Android non-UI layers | not started (see note) |
| 6 Content as data | not started |

### Output guard (added after plan review)

Before this, the only check on model text was a regex list plus a Review agent that was the
same model grading its own work. Every model-written string now also passes, in order:

1. **deterministic**: forbidden claims per language, unapproved phone numbers, dose-like quantities, unlisted URLs;
2. **similarity**: offline character n-gram match against a bank of prohibited statements in all four languages;
3. **judge**: an independent model (`SAHAY_GUARD_LLM_*`, falling back to the main model and saying so in the trace) that sees the reply only as quoted data.

Contract: any failure or unclear verdict blocks and the user sees curated fixed copy. No layer
edits model text. Measured limits, all covered by tests: the offline layers catch restatements
and close paraphrases only (threshold 0.65, chosen so ordinary advice like "talk with your care
team about your worries" is not blocked); real paraphrase and meaning checks depend on the judge.
Exemplars in hi/bn/ta need native-speaker review.

## 3. Target architecture

```
sahay/
  api/            FastAPI app, routers: auth, me, shares, clinician, admin, care, chat, plan, speech, health
  core/           settings (pydantic-settings), security (hashing, tokens, rate limit), errors, deps
  domain/
    safety/       risk classification, medical-boundary, output checks, per-language rules + corpus tests
    content/      sources, fallback copy, activity catalogue  (loaded from versioned data files)
    agents/       pipeline: typed in/out, model injected through a Protocol
  db/             SQLAlchemy models, Alembic migrations, repositories
  providers/      llm client (allow-listed hosts), stt, tts
contract/openapi.json   generated, checked in, diffed in CI
```

Principles:

1. **Safety is a module with a narrow interface**, `assess(text, lang) -> RiskAssessment`, and its own test corpus. Nothing else decides urgency.
2. **Authorisation by dependency**: routes declare `Depends(require_role("clinician"))`; ownership checks live in repository methods that take the acting user, so a query cannot be written without one.
3. **Model access through a port**: `LLM.complete(system, user, max_tokens)`. The pipeline never sees URLs or keys. Fakes in tests replace string-matching on prompts.
4. **Content is data**: activities, source cards and fallback copy become versioned JSON consumed by the backend and exported for the Android and web clients.
5. **One typed contract**: Pydantic models produce OpenAPI; the Android client types are generated from it.

## 4. Phased plan

Each phase ends green (tests pass, the app still works against the backend) and is one PR-sized commit series.

### Phase 0: Safety net, no behaviour change
- Characterisation tests that pin every current endpoint's status and JSON shape.
- Safety corpus: 150+ labelled messages per language (crisis, urgent medical, medication request, dependency, ordinary), marked with the known failures from S1-S3 as expected-fail.
- `ruff` + `mypy` baseline.
- **Exit:** CI green; known failures listed.

### Phase 1: Fix what is dangerous (small, targeted)
- Fix S1 (Bengali pattern) with a unit test over Unicode script of each pattern.
- Run the output filter on both the English draft and the final translated reply; add per-language forbidden-claim patterns.
- Close X1: model endpoint becomes server configuration only. The per-device override is removed or limited to an operator-set allow-list.
- Require auth on `/api/message`, `/api/model/test`, `/api/speech/*` (guest sessions get a short-lived anonymous token with a tight quota, so "use without an account" keeps working).
- Add rate limiting (login, message, speech) and a read timeout on the server.
- **Exit:** safety corpus S1 passes; SSRF test passes; abuse tests pass.

### Phase 2: Safety module
- Extract risk, medical-boundary, dependency and output checks into `domain/safety`.
- Add normalisation (case, punctuation, common typos and transliteration variants) and a broader phrase set; add an **optional** small classifier behind the same interface, off by default.
- Define the failure contract: when the model is unavailable, the pipeline degrades to rules plus fixed copy, and says so.
- **Exit:** S3 cases pass; recall and false-positive numbers per language recorded in the repo.

### Phase 3: API layer
- Move to FastAPI with Pydantic models and routers, behind the same URLs and JSON shapes so Android and web keep working.
- Real dependency-injected auth and role checks; one error envelope.
- SQLAlchemy models + Alembic baseline migration generated from the current schema; session pruning job.
- **Exit:** characterisation tests from Phase 0 pass unchanged; `openapi.json` committed.

### Phase 4: Agent pipeline
- Replace the mutable `State` and seven classes with explicit stages (`understand -> safety -> route -> draft -> verify`), each a pure function of typed input plus the `LLM` port.
- Collapse model round-trips: classify and route in one structured call; translation and verification only when the reply language is not English; hard overall deadline with fixed-copy fallback.
- Keep the agent trace format the app already shows.
- **Exit:** p95 turn latency and call count measured before and after; behaviour tests green.

### Phase 5: Android non-UI layers
Boundary with the UI redesign: the new UI consumes `UiState` and calls intent methods; it never touches networking or storage.
- Generated API client from `openapi.json`; coroutines and `Flow`; one `Result` type for errors.
- Split `SahayViewModel` into feature state holders (session, chat, plan, care, clinician, admin, settings) behind repositories.
- Move persistence to DataStore (settings) and an encrypted store (session, local content); keep the existing Keystore design.
- Typed navigation state in the domain layer.
- Unit tests for each state holder with fake repositories.
- **Exit:** existing screens run on the new layer with no UI changes, or the new UI is wired to it directly.

### Phase 6: Content as data and cleanup
- Activity catalogue, source cards and safety copy as versioned JSON; backend serves `/api/content`; Android bundles a snapshot for offline use.
- Retire the duplicated lists and the parity test.
- Decide the web client's fate (section 7).

## 5. Testing and quality gates

- Unit: safety corpus, content validation, repositories with an in-memory DB, pipeline with a fake LLM.
- API: contract tests from `openapi.json`; auth matrix test that calls every route as every role plus anonymous and asserts the expected status.
- Android: JVM tests for state holders; no emulator needed.
- CI: ruff, mypy, pytest, Gradle unit tests, OpenAPI diff.

## 6. Risks

| Risk | Mitigation |
|------|-----------|
| A "fix" weakens safety | Corpus is written first; changes must not lower recall |
| UI redesign lands against a moving API | Phase 3 keeps URLs and shapes; breaking changes only via a versioned `/v2` |
| Rewrite stalls the working demo | Strangler order; every phase is shippable |
| Translated safety copy is wrong | Native-speaker review before release; keep the clinical wording in one file |
| Scope creep into UI | Phase 5 boundary above; no Compose files touched |

## 7. Decisions needed

1. **Per-device model endpoint** (the `model_config` override): keep it for demos, restricted to an operator allow-list, or remove it?
2. **Anonymous use**: keep guest chat with a quota token, or require an account for the AI?
3. **Web client**: retire it, or keep it as a thin client on the generated API?
4. **Framework**: FastAPI + SQLAlchemy + Alembic is my recommendation. Say so if the course requires standard library only.
5. **Who reviews Hindi, Bengali and Tamil safety wording**, since I can produce but not validate it.
6. **Scope of the Android layer work**: do it now, or wait until the new UI exists to avoid two moving parts?
