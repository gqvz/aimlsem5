# Graph Report - aiml  (2026-10-13)

## Corpus Check
- 53 files · ~276,021 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 21 file(s) not represented in the graph (top: .ttf 8, (none) 4, .xml 2)

## Summary
- 905 nodes · 2011 edges · 46 communities (27 shown, 19 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 203 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5751b6fb`
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
- CareTests
- Mobile UI Refinement
- .send
- BookkeepingTests
- test_model_configuration.py
- .transcribeAudio
- .run
- Explore Wellbeing Activities (no streaks)
- SpecialistModel
- SahayHandler
- test_engine.py
- ModelClient
- ActivityAgent
- CLAUDE.md

## God Nodes (most connected - your core abstractions)
1. `SahayViewModel` - 133 edges
2. `Sahay Updated System Architecture (ARCHITECTURE.md)` - 43 edges
3. `respond()` - 31 edges
4. `SahayApp()` - 27 edges
5. `WellbeingLabel()` - 27 edges
6. `ModelClient` - 24 edges
7. `Screen` - 22 edges
8. `local()` - 21 edges
9. `render()` - 21 edges
10. `MainActivity` - 20 edges

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

## Communities (46 total, 19 thin omitted)

### Community 0 - "bookkeeping.py"
Cohesion: 0.09
Nodes (28): Speech Flow (Device or API STT/TTS), ApiError, _audit(), _auth(), connection(), _create_user(), handle(), init_db() (+20 more)

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
Cohesion: 0.21
Nodes (10): ReviewAgent, SafetyAgent, choose_action(), choose_card(), _fallback_response(), _json_object(), _matches(), more_cautious() (+2 more)

### Community 11 - "Sahay Updated System Architecture (ARCHITECTURE.md)"
Cohesion: 0.16
Nodes (15): On-device Demo Fallback, Sahay Updated System Architecture (ARCHITECTURE.md), Public Agent Trace (agent_trace), POST /api/message, Two-way Care Messages and Files, Fixed Localized Fallback, No Whole-Turn Deadline (limitation), Response Mode (live/fixed/demo/fallback) (+7 more)

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
Cohesion: 0.14
Nodes (17): Sahay for Android README, Sahay_Pixel_9 Low-memory Emulator, Docker Course Deployment, Clinician File Request Fulfillment, gemma3:12b via Ollama (resume-ollama), compose.yaml (Backend Service), compose.gemma.yaml (Optional SSH Model Bridge), Live Gemma Evaluation (Fictional Course Cases) (+9 more)

### Community 18 - "Stitch Account and Sharing References"
Cohesion: 0.15
Nodes (15): Sahay Android App (Kotlin/Jetpack Compose), Four-Language Support (en, hi, bn, ta), Role Routing (Patient/Clinician/Admin), Sahay Wellbeing Prototype, SahayViewModel, SessionStore AES-GCM Session Encryption, Stitch Screen: Clinician Invited Sign-In, Stitch Screen: Share Visit Brief & Consent (+7 more)

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

### Community 28 - "Mobile UI Refinement"
Cohesion: 0.31
Nodes (3): Mobile UI Refinement, Hoober: How Users Hold Mobile Devices (2013), Mehrotra et al.: Action Bar Adaptations for One-Handed Use (2022)

### Community 32 - "test_model_configuration.py"
Cohesion: 0.11
Nodes (6): POST /api/model/test, model_for_request(), test_model_connection(), LocalProvider, ModelConfigurationTests, wire()

### Community 34 - ".run"
Cohesion: 0.12
Nodes (14): AgentGraph, EmotionalSupportAgent, KnowledgeAgent, State, SupervisorAgent, VisitAgent, Emotional Support Agent, Knowledge Agent (+6 more)

### Community 36 - "Explore Wellbeing Activities (no streaks)"
Cohesion: 0.67
Nodes (4): Patient Explore Tab, Five-Senses Grounding Exercise (Final), Journal Reflection with Keyboard (Final), Explore Wellbeing Activities (no streaks)

### Community 40 - "SpecialistModel"
Cohesion: 0.18
Nodes (3): AgentTests, CatalogueParityTests, SpecialistModel

### Community 41 - "SahayHandler"
Cohesion: 0.24
Nodes (3): GET /api/health, public_model_configuration(), SahayHandler

### Community 42 - "test_engine.py"
Cohesion: 0.21
Nodes (4): FakeCrisisModel, FakeFabricatedCitationModel, FakeSafeModel, FakeUnsafeModel

### Community 44 - "ActivityAgent"
Cohesion: 0.33
Nodes (4): ActivityAgent, Activity Agent, Nine Local Activities, Sahay Updated Architecture PDF (5 Oct snapshot)

## Knowledge Gaps
- **38 isolated node(s):** `WELCOME`, `RESTORING`, `HOME`, `ACTIVITY`, `CARE` (+33 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 237 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **19 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SahayViewModel` connect `SahayViewModel` to `WellbeingUi.kt`, `.transcribeAudio`, `MainActivity.kt`, `.accountRequest`, `SahayApp`, `SessionStore.kt`, `SahayViewModel.kt`, `.navigate`, `Composable`, `.send`, `JSONObject`?**
  _High betweenness centrality (0.200) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `SahayViewModel` (e.g. with `Content` and `SessionStore`) actually correct?**
  _`SahayViewModel` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `WELCOME`, `RESTORING`, `HOME` to the rest of the system?**
  _38 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `bookkeeping.py` be split into smaller, more focused modules?**
  _Cohesion score 0.08691834942932397 - nodes in this community are weakly interconnected._
- **Why does `Sahay README` connect `Sahay README` to `bookkeeping.py`, `test_model_configuration.py`, `.run`, `SahayHandler`, `Sahay Updated System Architecture (ARCHITECTURE.md)`, `Stitch Account and Sharing References`, `Mobile UI Refinement`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `SahayApp()` (e.g. with `.onCreate()` and `ClinicianAuthScreen()`) actually correct?**
  _`SahayApp()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Should `WellbeingUi.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05401234567901234 - nodes in this community are weakly interconnected._