# Graph Report - aimlsem5  (2026-10-31)

## Corpus Check
- 65 files · ~83,273 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 22 file(s) not represented in the graph (top: .ttf 8, (none) 4, .xml 2)

## Summary
- 963 nodes · 2086 edges · 52 communities (32 shown, 20 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 207 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cd11d51a`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- bookkeeping.py
- WellbeingUi.kt
- respond
- app.js
- MainActivity.kt
- CareUi.kt
- Gentle Breathing Exercise
- engine.py
- Sahay Updated System Architecture (ARCHITECTURE.md)
- SahayViewModel
- SahayApp
- SessionStore.kt
- Screen
- SahayViewModel.kt
- Sahay README
- Stitch Account and Sharing References
- Settings screen (account + Language and comfort)
- Care Conversation Screen (Android)
- Patient-Clinician Care Conversation (messages and files)
- Explore/Activities Screen QA (English)
- Composable
- Patient-Clinician Messaging and Files
- ModelError
- test_model_configuration.py
- Mobile UI Refinement
- .send
- model-tunnel
- Rebuild and emulator validation - 13 October 2026
- .transcribeAudio
- KnowledgeAgent
- Explore Wellbeing Activities (no streaks)
- kratos-stream.sh
- ModelClient
- SpecialistModel
- model-tunnel
- test_engine.py
- CLAUDE.md
- backend
- SahayHandler
- Model Configuration Update (12 Oct 2026)
- ModelHttpTests
- compose.yaml
- Kratos LLM restored — 13 October 2026

## God Nodes (most connected - your core abstractions)
1. `SahayViewModel` - 133 edges
2. `Sahay Updated System Architecture (ARCHITECTURE.md)` - 43 edges
3. `respond()` - 31 edges
4. `SahayApp()` - 27 edges
5. `WellbeingLabel()` - 27 edges
6. `ModelClient` - 24 edges
7. `Screen` - 22 edges
8. `handle()` - 22 edges
9. `local()` - 21 edges
10. `render()` - 21 edges

## Surprising Connections (you probably didn't know these)
- `Sahay Updated Architecture PDF (5 Oct snapshot)` --semantically_similar_to--> `Sahay Updated System Architecture (ARCHITECTURE.md)`  [INFERRED] [semantically similar]
  output/pdf/Sahay_Updated_Architecture.pdf → ARCHITECTURE.md
- `Invented Source Links Found in First Pass` --conceptually_related_to--> `_passes_output_check()`  [INFERRED]
  evals/README.md → engine.py
- `Salted scrypt Passwords and Hashed Tokens` --references--> `_password_hash()`  [INFERRED]
  ARCHITECTURE.md → bookkeeping.py
- `Admin Bookkeeping Screens` --conceptually_related_to--> `handle()`  [INFERRED]
  DOCTOR_UI_STITCH_PROMPT.md → bookkeeping.py
- `Endpoint Credential Isolation and Redirect Rejection` --references--> `ModelClient`  [INFERRED]
  MODEL_CONFIGURATION_VALIDATION.md → engine.py

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Settings screen QA variants (guest, expired, clinician, Hindi)** — artifacts_qa_settings_guest, artifacts_qa_settings_final, artifacts_qa_settings_clinician, artifacts_qa_settings_clinician_final, artifacts_qa_settings_hindi [EXTRACTED 1.00]
- **Eight-agent turn pipeline** — architecture_language_agent, architecture_safety_agent, architecture_supervisor_agent, architecture_emotional_support_agent, architecture_knowledge_agent, architecture_activity_agent, architecture_visit_preparation_agent, architecture_review_agent [EXTRACTED 1.00]
- **Stitch account and sharing screen set** — design_stitch_17ea2dc4de4f4a759e81a7f5e4ac27cc, design_stitch_191f37e26ece44c496fcebfd37ebc9d1, design_stitch_6adec3129fbd401b94be22a0a2dc9c5a, design_stitch_9b3a2e501b6e4ee5ae93e7dc912854c8 [EXTRACTED 1.00]
- **Patient self-care activities (explore, grounding, journal)** — artifacts_qa_refinement_explore, artifacts_qa_refinement_grounding_final, artifacts_qa_refinement_journal_keyboard_final [INFERRED 0.75]
- **Calming activities (arrival garden, breathing, sound sanctuary)** — artifacts_qa_signed_out, artifacts_qa_breathing_exercise, artifacts_qa_sound [INFERRED 0.85]
- **Care file request send flow (draft, request, attach, confirm, history)** — artifacts_qa_refinement_care_keyboard, artifacts_qa_refinement_care_draft_restored, artifacts_qa_refinement_care_file_sent, artifacts_qa_refinement_care_attachment, artifacts_qa_refinement_care_history_final, artifacts_qa_refinement_care_history_sent_final [INFERRED 0.85]
- **Care conversation file attach and send flow** — artifacts_qa_clinician_files, artifacts_qa_file_ready, artifacts_qa_file_sent, artifacts_qa_file_ready_file_attachment [INFERRED 0.85]
- **Clinician portal screen flow (auth, queue, messages, settings)** — artifacts_qa_refinement_clinician_auth, artifacts_qa_refinement_clinician_queue, artifacts_qa_refinement_clinician_tabs_final, artifacts_qa_refinement_clinician_care, artifacts_qa_refinement_clinician_settings_final [INFERRED 0.85]
- **Clinician session expiry and restore QA flow** — artifacts_qa_clinician_queue, artifacts_qa_clinician_queue_final, artifacts_qa_clinician_restored_final, artifacts_qa_clinician_restored [INFERRED 0.85]
- **Model selection and testing flow** — architecture_model_config, engine_model_for_request, engine_test_model_connection, model_configuration_validation_provider_error, model_configuration_validation_credential_isolation [INFERRED 0.85]
- **Sahay Shared Brand Assets (Android + Web)** — android_app_src_main_res_drawable_nodpi_courtyard, android_app_src_main_res_drawable_nodpi_sahay_emblem, web_assets_courtyard, web_assets_sahay_emblem [INFERRED 0.85]
- **Sahay calming activities (plan, breathing, memory game)** — artifacts_qa_plan, sahay_gentle_breathing, sahay_garden_pairs_game, sahay_calm_non_pressuring_design [INFERRED 0.85]
- **Keyboard-overlap QA screenshots** — artifacts_qa_model_settings_keyboard, artifacts_qa_refinement_auth_email_keyboard, artifacts_qa_refinement_auth_keyboard, sahay_keyboard_safe_sticky_cta [INFERRED 0.85]
- **Sahay main tab screens** — sahay_today_screen, sahay_explore_screen, sahay_settings_screen, sahay_bottom_navigation [INFERRED 0.85]
- **Sahay Android QA Screenshots** — artifacts_qa_activities, artifacts_qa_activities_hindi, artifacts_qa_arrival, artifacts_qa_arrival_final, artifacts_qa_art [INFERRED 0.85]
- **Tamil small-screen refinement QA pass** — artifacts_qa_refinement_small_tamil_breathing, artifacts_qa_refinement_small_tamil_breathing_final, artifacts_qa_refinement_small_tamil_explore, artifacts_qa_refinement_small_tamil_explore_final, artifacts_qa_refinement_small_tamil_navigation_final, artifacts_qa_refinement_small_tamil_settings [INFERRED 0.85]

## Communities (52 total, 20 thin omitted)

### Community 0 - "bookkeeping.py"
Cohesion: 0.08
Nodes (29): Speech Flow (Device or API STT/TTS), ApiError, _audit(), _auth(), connection(), _create_user(), handle(), init_db() (+21 more)

### Community 1 - "WellbeingUi.kt"
Cohesion: 0.05
Nodes (29): AmbientAudio, AgentInspector(), CareConversation(), CareInbox(), CareMessage(), SettingsPage(), SettingSwitch(), CalmFrame() (+21 more)

### Community 4 - "app.js"
Cohesion: 0.14
Nodes (39): CARD_COPY, CARD_FALLBACK, cardCopy(), changeView(), chat(), clearAll(), e(), exercise() (+31 more)

### Community 8 - "Gentle Breathing Exercise"
Cohesion: 0.09
Nodes (29): QA: Garden Pairs memory game (8 face-down cards, 0 of 4 pairs), QA: Garden Pairs memory game (reveal state), QA: AI model settings with keyboard open, QA: AI model settings provider HTTP 403 error, QA: Today plan screen (sound sanctuary, gentle breath, companion), QA: Arrival screen 'Take a little pause' with garden, QA: Care team sign in, email field with keyboard (CTA disabled), QA: Care team sign in, password field with keyboard (CTA enabled) (+21 more)

### Community 10 - "engine.py"
Cohesion: 0.14
Nodes (16): ActivityAgent, AgentGraph, ReviewAgent, SafetyAgent, State, SupervisorAgent, Supervisor Agent, Turn Execution Flow (+8 more)

### Community 11 - "Sahay Updated System Architecture (ARCHITECTURE.md)"
Cohesion: 0.13
Nodes (19): EmotionalSupportAgent, VisitAgent, On-device Demo Fallback, Sahay Updated System Architecture (ARCHITECTURE.md), Activity Agent, Public Agent Trace (agent_trace), POST /api/message, Two-way Care Messages and Files (+11 more)

### Community 13 - "SahayApp"
Cohesion: 0.22
Nodes (22): nativeText(), BottomNavigation(), ChatScreen(), ExerciseScreen(), HelpCard(), HelpScreen(), InfoScreen(), MessageBubble() (+14 more)

### Community 14 - "SessionStore.kt"
Cohesion: 0.11
Nodes (4): Content, LanguageOption, SessionStore, StoredSession

### Community 15 - "Screen"
Cohesion: 0.10
Nodes (21): Screen, ACTIVITY, ADMIN, AGENT_INSPECT, CARE, CHAT, CLINICIAN_AUTH, CLINICIAN_DETAIL (+13 more)

### Community 16 - "SahayViewModel.kt"
Cohesion: 0.10
Nodes (3): AccountHttpError, CareQuestion, ChatMessage

### Community 17 - "Sahay README"
Cohesion: 0.16
Nodes (14): Sahay for Android README, Sahay_Pixel_9 Low-memory Emulator, Clinician File Request Fulfillment, gemma3:12b via Ollama (resume-ollama), Live Gemma Evaluation (Fictional Course Cases), Invented Source Links Found in First Pass, 19 Fictional Live Gemma Cases, Sahay Overhaul Validation (5 Oct 2026) (+6 more)

### Community 18 - "Stitch Account and Sharing References"
Cohesion: 0.14
Nodes (16): Sahay Android App (Kotlin/Jetpack Compose), Four-Language Support (en, hi, bn, ta), Role Routing (Patient/Clinician/Admin), Sahay Wellbeing Prototype, SahayViewModel, SessionStore AES-GCM Session Encryption, Stitch Screen: Clinician Invited Sign-In, Stitch Screen: Share Visit Brief & Consent (+8 more)

### Community 19 - "Settings screen (account + Language and comfort)"
Cohesion: 0.20
Nodes (13): Bottom nav: Today/Talk/Explore/Care team/Settings, One gentle breath (two-minute breathing, no holds), Today screen (refined): breathing card first, check-in collapsed to row, Settings - clinician account Dr. Meera Rao (demo), Settings - clinician account (final, with Help + privacy toggle), Settings - guest with 'Session expired' banner, Settings - private guest session (patient/care team sign in), Settings in Hindi (partial localization, English strings remain) (+5 more)

### Community 20 - "Care Conversation Screen (Android)"
Cohesion: 0.15
Nodes (16): Care Conversation Screen (Android), File Attachment Chip, Gentle Breathing Exercise, Medical File Request Flow, Privacy Hide Toggle and Help Icon (App Bar), Gentle Breathing Screen Screenshot, Care Conversation Pending Attachment Screenshot, Care Send Confirmation Snackbar Screenshot (+8 more)

### Community 21 - "Patient-Clinician Care Conversation (messages and files)"
Cohesion: 0.19
Nodes (15): Gentle Breathing Exercise Screen, Gentle Breathing Exercise, Clinician Patient Conversation Files Screen, Patient-Clinician Care Conversation (messages and files), Clinician Shared Visit Briefs Queue, Clinician Sign-In Session Expired Screen, Care Team Sign-In / Accept Invite, Shared Visit Briefs (patient-approved snapshots) (+7 more)

### Community 23 - "Explore/Activities Screen QA (English)"
Cohesion: 0.19
Nodes (12): Courtyard Background Image (Android), Sahay Emblem (Android), Explore/Activities Screen QA (English), Explore/Activities Screen QA (Hindi), Arrival Screen QA (Initial), Arrival Screen QA (Final), Colour Your Own Sky Drawing Screen QA, Sahay Mental Wellbeing App (+4 more)

### Community 24 - "Composable"
Cohesion: 0.47
Nodes (10): ClinicianAuthScreen(), ClinicianQueueScreen(), displayIst(), FlowButton(), FlowCard(), FlowShell(), PasswordField(), PatientAccountScreen() (+2 more)

### Community 25 - "Patient-Clinician Messaging and Files"
Cohesion: 0.24
Nodes (9): Care Conversation Thread (Return, Final), Clinician Sign-in Screen, Clinician Patient Conversations List, Clinician Review Queue (Shared Visit Briefs), Clinician Settings Screen (Final), Clinician Review Queue with Bottom Tabs (Final), Sahay Clinician Portal, Multilingual and Larger-Text Accessibility (+1 more)

### Community 26 - "ModelError"
Cohesion: 0.20
Nodes (4): LanguageAgent, Language Agent, ModelError, _NoModelRedirect

### Community 27 - "test_model_configuration.py"
Cohesion: 0.11
Nodes (6): POST /api/model/test, model_for_request(), test_model_connection(), LocalProvider, ModelConfigurationTests, wire()

### Community 28 - "Mobile UI Refinement"
Cohesion: 0.31
Nodes (3): Mobile UI Refinement, Hoober: How Users Hold Mobile Devices (2013), Mehrotra et al.: Action Bar Adaptations for One-Handed Use (2022)

### Community 31 - "model-tunnel"
Cohesion: 0.12
Nodes (16): depends_on, environment, context, dockerfile, LLM_API_KEY, LLM_API_URL, LLM_MODEL, LLM_TIMEOUT (+8 more)

### Community 32 - "Rebuild and emulator validation - 13 October 2026"
Cohesion: 0.22
Nodes (8): Artifacts, Emulator and memory, Fix made during checking, Graphify, Live AI limitation, Rebuild and emulator validation - 13 October 2026, Result, Verification

### Community 34 - "KnowledgeAgent"
Cohesion: 0.50
Nodes (3): KnowledgeAgent, Knowledge Agent, Four NCI Information Cards

### Community 36 - "Explore Wellbeing Activities (no streaks)"
Cohesion: 0.67
Nodes (4): Patient Explore Tab, Five-Senses Grounding Exercise (Final), Journal Reflection with Keyboard (Final), Explore Wellbeing Activities (no streaks)

### Community 41 - "ModelClient"
Cohesion: 0.21
Nodes (4): GET /api/health, ModelClient, public_model_configuration(), main()

### Community 42 - "SpecialistModel"
Cohesion: 0.18
Nodes (3): AgentTests, CatalogueParityTests, SpecialistModel

### Community 43 - "model-tunnel"
Cohesion: 0.15
Nodes (13): environment, context, dockerfile, LLM_API_URL, LLM_MODEL, LLM_TIMEOUT, build, mem_limit (+5 more)

### Community 44 - "test_engine.py"
Cohesion: 0.21
Nodes (4): FakeCrisisModel, FakeFabricatedCitationModel, FakeSafeModel, FakeUnsafeModel

### Community 46 - "backend"
Cohesion: 0.18
Nodes (11): build, cpus, env_file, extra_hosts, mem_limit, ports, restart, volumes (+3 more)

### Community 48 - "Model Configuration Update (12 Oct 2026)"
Cohesion: 0.40
Nodes (4): OpenCode space-bunny-free Default Model, Model Configuration Update (12 Oct 2026), provider_error Field on Fallbacks, OpenCode Free-Tier HTTP 403 Limitation

### Community 50 - "compose.yaml"
Cohesion: 0.40
Nodes (4): Docker Course Deployment, services, volumes, sahay-data

## Knowledge Gaps
- **38 isolated node(s):** `WELCOME`, `RESTORING`, `HOME`, `ACTIVITY`, `CARE` (+33 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 273 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **20 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SahayViewModel` connect `SahayViewModel` to `WellbeingUi.kt`, `.transcribeAudio`, `MainActivity.kt`, `.accountRequest`, `SahayApp`, `SessionStore.kt`, `SahayViewModel.kt`, `.navigate`, `Composable`, `.send`, `JSONObject`?**
  _High betweenness centrality (0.183) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `SahayViewModel` (e.g. with `Content` and `SessionStore`) actually correct?**
  _`SahayViewModel` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `WELCOME`, `RESTORING`, `HOME` to the rest of the system?**
  _38 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `bookkeeping.py` be split into smaller, more focused modules?**
  _Cohesion score 0.07659850697825381 - nodes in this community are weakly interconnected._
- **Why does `Sahay README` connect `Sahay README` to `bookkeeping.py`, `KnowledgeAgent`, `Sahay Updated System Architecture (ARCHITECTURE.md)`, `SahayHandler`, `Model Configuration Update (12 Oct 2026)`, `Stitch Account and Sharing References`, `test_model_configuration.py`, `Mobile UI Refinement`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `SahayApp()` (e.g. with `.onCreate()` and `ClinicianAuthScreen()`) actually correct?**
  _`SahayApp()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Should `WellbeingUi.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05401234567901234 - nodes in this community are weakly interconnected._