# Graph Report - aiml  (2026-10-12)

## Corpus Check
- 122 files · ~273,271 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 21 file(s) not represented in the graph (top: .ttf 8, (none) 4, .xml 2)

## Summary
- 894 nodes · 1988 edges · 40 communities (24 shown, 16 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 199 edges (avg confidence: 0.87)
- Token cost: 474,564 input · 0 output

## Community Hubs (Navigation)
- HTTP Server & Bookkeeping
- Android Care & Settings UI
- Model Client & Live Evals
- Web Client (app.js)
- MainActivity & Recording
- Care/Settings UI Primitives
- QA Screens: Activities & Settings
- ViewModel Account & Care Actions
- Multi-Agent Graph Core
- Specialist Agents & Architecture
- ViewModel Content Actions
- Patient Screens (Chat/Help)
- Content & Session Store
- Screen Routing Enum
- ViewModel State Types
- Deployment & Validation Docs
- Android App & Stitch Designs
- Settings & Comfort UX
- Care Conversation Flow
- Clinician Care Screens
- Brand & Arrival Visuals
- Clinician Account Screens
- Clinician Portal QA
- Language Agent & Model Errors
- Care Messaging Tests
- Mobile UX Principles
- Answer & Source Cards
- Bookkeeping Tests
- Model Configuration Overrides
- Audio Transcription
- Knowledge Agent
- Explore Activities

## God Nodes (most connected - your core abstractions)
1. `SahayViewModel` - 130 edges
2. `Sahay Updated System Architecture (ARCHITECTURE.md)` - 43 edges
3. `respond()` - 31 edges
4. `SahayApp()` - 27 edges
5. `WellbeingLabel()` - 25 edges
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
- **Eight-agent turn pipeline** — architecture_language_agent, architecture_safety_agent, architecture_supervisor_agent, architecture_emotional_support_agent, architecture_knowledge_agent, architecture_activity_agent, architecture_visit_preparation_agent, architecture_review_agent [EXTRACTED 1.00]
- **Stitch account and sharing screen set** — design_stitch_17ea2dc4de4f4a759e81a7f5e4ac27cc, design_stitch_191f37e26ece44c496fcebfd37ebc9d1, design_stitch_6adec3129fbd401b94be22a0a2dc9c5a, design_stitch_9b3a2e501b6e4ee5ae93e7dc912854c8 [EXTRACTED 1.00]
- **Model selection and testing flow** — architecture_model_config, engine_model_for_request, engine_test_model_connection, model_configuration_validation_provider_error, model_configuration_validation_credential_isolation [INFERRED 0.85]
- **Sahay Android QA Screenshots** — artifacts_qa_activities, artifacts_qa_activities_hindi, artifacts_qa_arrival, artifacts_qa_arrival_final, artifacts_qa_art [INFERRED 0.85]
- **Sahay Shared Brand Assets (Android + Web)** — android_app_src_main_res_drawable_nodpi_courtyard, android_app_src_main_res_drawable_nodpi_sahay_emblem, web_assets_courtyard, web_assets_sahay_emblem [INFERRED 0.85]
- **Clinician session expiry and restore QA flow** — artifacts_qa_clinician_queue, artifacts_qa_clinician_queue_final, artifacts_qa_clinician_restored_final, artifacts_qa_clinician_restored [INFERRED 0.85]
- **Care conversation file attach and send flow** — artifacts_qa_clinician_files, artifacts_qa_file_ready, artifacts_qa_file_sent, artifacts_qa_file_ready_file_attachment [INFERRED 0.85]
- **Sahay calming activities (plan, breathing, memory game)** — artifacts_qa_plan, sahay_gentle_breathing, sahay_garden_pairs_game, sahay_calm_non_pressuring_design [INFERRED 0.85]
- **Keyboard-overlap QA screenshots** — artifacts_qa_model_settings_keyboard, artifacts_qa_refinement_auth_email_keyboard, artifacts_qa_refinement_auth_keyboard, sahay_keyboard_safe_sticky_cta [INFERRED 0.85]
- **Care file request send flow (draft, request, attach, confirm, history)** — artifacts_qa_refinement_care_keyboard, artifacts_qa_refinement_care_draft_restored, artifacts_qa_refinement_care_file_sent, artifacts_qa_refinement_care_attachment, artifacts_qa_refinement_care_history_final, artifacts_qa_refinement_care_history_sent_final [INFERRED 0.85]
- **Clinician portal screen flow (auth, queue, messages, settings)** — artifacts_qa_refinement_clinician_auth, artifacts_qa_refinement_clinician_queue, artifacts_qa_refinement_clinician_tabs_final, artifacts_qa_refinement_clinician_care, artifacts_qa_refinement_clinician_settings_final [INFERRED 0.85]
- **Patient self-care activities (explore, grounding, journal)** — artifacts_qa_refinement_explore, artifacts_qa_refinement_grounding_final, artifacts_qa_refinement_journal_keyboard_final [INFERRED 0.75]
- **Tamil small-screen refinement QA pass** — artifacts_qa_refinement_small_tamil_breathing, artifacts_qa_refinement_small_tamil_breathing_final, artifacts_qa_refinement_small_tamil_explore, artifacts_qa_refinement_small_tamil_explore_final, artifacts_qa_refinement_small_tamil_navigation_final, artifacts_qa_refinement_small_tamil_settings [INFERRED 0.85]
- **Sahay main tab screens** — sahay_today_screen, sahay_explore_screen, sahay_settings_screen, sahay_bottom_navigation [INFERRED 0.85]
- **Settings screen QA variants (guest, expired, clinician, Hindi)** — artifacts_qa_settings_guest, artifacts_qa_settings_final, artifacts_qa_settings_clinician, artifacts_qa_settings_clinician_final, artifacts_qa_settings_hindi [EXTRACTED 1.00]
- **Calming activities (arrival garden, breathing, sound sanctuary)** — artifacts_qa_signed_out, artifacts_qa_breathing_exercise, artifacts_qa_sound [INFERRED 0.85]

## Communities (40 total, 16 thin omitted)

### Community 0 - "HTTP Server & Bookkeeping"
Cohesion: 0.06
Nodes (32): GET /api/health, Speech Flow (Device or API STT/TTS), ApiError, _audit(), _auth(), connection(), _create_user(), handle() (+24 more)

### Community 1 - "Android Care & Settings UI"
Cohesion: 0.06
Nodes (27): AmbientAudio, AgentInspector(), CareConversation(), CareInbox(), CareMessage(), SettingsPage(), SettingSwitch(), CalmFrame() (+19 more)

### Community 2 - "Model Client & Live Evals"
Cohesion: 0.05
Nodes (15): POST /api/model/test, model_for_request(), ModelClient, respond(), test_model_connection(), main(), AgentTests, SpecialistModel (+7 more)

### Community 4 - "Web Client (app.js)"
Cohesion: 0.14
Nodes (39): CARD_COPY, CARD_FALLBACK, cardCopy(), changeView(), chat(), clearAll(), e(), exercise() (+31 more)

### Community 8 - "QA Screens: Activities & Settings"
Cohesion: 0.09
Nodes (29): QA: Garden Pairs memory game (8 face-down cards, 0 of 4 pairs), QA: Garden Pairs memory game (reveal state), QA: AI model settings with keyboard open, QA: AI model settings provider HTTP 403 error, QA: Today plan screen (sound sanctuary, gentle breath, companion), QA: Arrival screen 'Take a little pause' with garden, QA: Care team sign in, email field with keyboard (CTA disabled), QA: Care team sign in, password field with keyboard (CTA enabled) (+21 more)

### Community 10 - "Multi-Agent Graph Core"
Cohesion: 0.16
Nodes (15): AgentGraph, ReviewAgent, SafetyAgent, State, SupervisorAgent, Supervisor Agent, Turn Execution Flow, choose_action() (+7 more)

### Community 11 - "Specialist Agents & Architecture"
Cohesion: 0.11
Nodes (21): ActivityAgent, EmotionalSupportAgent, VisitAgent, On-device Demo Fallback, Sahay Updated System Architecture (ARCHITECTURE.md), Activity Agent, Public Agent Trace (agent_trace), POST /api/message (+13 more)

### Community 13 - "Patient Screens (Chat/Help)"
Cohesion: 0.22
Nodes (22): nativeText(), BottomNavigation(), ChatScreen(), ExerciseScreen(), HelpCard(), HelpScreen(), InfoScreen(), MessageBubble() (+14 more)

### Community 14 - "Content & Session Store"
Cohesion: 0.11
Nodes (3): Content, LanguageOption, SessionStore

### Community 15 - "Screen Routing Enum"
Cohesion: 0.10
Nodes (21): Screen, ACTIVITY, ADMIN, AGENT_INSPECT, CARE, CHAT, CLINICIAN_AUTH, CLINICIAN_DETAIL (+13 more)

### Community 16 - "ViewModel State Types"
Cohesion: 0.11
Nodes (3): AccountHttpError, CareQuestion, ChatMessage

### Community 17 - "Deployment & Validation Docs"
Cohesion: 0.14
Nodes (17): Sahay for Android README, Sahay_Pixel_9 Low-memory Emulator, Docker Course Deployment, Clinician File Request Fulfillment, gemma3:12b via Ollama (resume-ollama), compose.yaml (Backend Service), compose.gemma.yaml (Optional SSH Model Bridge), Live Gemma Evaluation (Fictional Course Cases) (+9 more)

### Community 18 - "Android App & Stitch Designs"
Cohesion: 0.15
Nodes (15): Sahay Android App (Kotlin/Jetpack Compose), Four-Language Support (en, hi, bn, ta), Role Routing (Patient/Clinician/Admin), Sahay Wellbeing Prototype, SahayViewModel, SessionStore AES-GCM Session Encryption, Stitch Screen: Clinician Invited Sign-In, Stitch Screen: Share Visit Brief & Consent (+7 more)

### Community 19 - "Settings & Comfort UX"
Cohesion: 0.20
Nodes (13): Bottom nav: Today/Talk/Explore/Care team/Settings, One gentle breath (two-minute breathing, no holds), Today screen (refined): breathing card first, check-in collapsed to row, Settings - clinician account Dr. Meera Rao (demo), Settings - clinician account (final, with Help + privacy toggle), Settings - guest with 'Session expired' banner, Settings - private guest session (patient/care team sign in), Settings in Hindi (partial localization, English strings remain) (+5 more)

### Community 20 - "Care Conversation Flow"
Cohesion: 0.15
Nodes (16): Care Conversation Screen (Android), File Attachment Chip, Gentle Breathing Exercise, Medical File Request Flow, Privacy Hide Toggle and Help Icon (App Bar), Gentle Breathing Screen Screenshot, Care Conversation Pending Attachment Screenshot, Care Send Confirmation Snackbar Screenshot (+8 more)

### Community 21 - "Clinician Care Screens"
Cohesion: 0.19
Nodes (15): Gentle Breathing Exercise Screen, Gentle Breathing Exercise, Clinician Patient Conversation Files Screen, Patient-Clinician Care Conversation (messages and files), Clinician Shared Visit Briefs Queue, Clinician Sign-In Session Expired Screen, Care Team Sign-In / Accept Invite, Shared Visit Briefs (patient-approved snapshots) (+7 more)

### Community 23 - "Brand & Arrival Visuals"
Cohesion: 0.19
Nodes (12): Courtyard Background Image (Android), Sahay Emblem (Android), Explore/Activities Screen QA (English), Explore/Activities Screen QA (Hindi), Arrival Screen QA (Initial), Arrival Screen QA (Final), Colour Your Own Sky Drawing Screen QA, Sahay Mental Wellbeing App (+4 more)

### Community 24 - "Clinician Account Screens"
Cohesion: 0.47
Nodes (10): ClinicianAuthScreen(), ClinicianQueueScreen(), displayIst(), FlowButton(), FlowCard(), FlowShell(), PasswordField(), PatientAccountScreen() (+2 more)

### Community 25 - "Clinician Portal QA"
Cohesion: 0.24
Nodes (9): Care Conversation Thread (Return, Final), Clinician Sign-in Screen, Clinician Patient Conversations List, Clinician Review Queue (Shared Visit Briefs), Clinician Settings Screen (Final), Clinician Review Queue with Bottom Tabs (Final), Sahay Clinician Portal, Multilingual and Larger-Text Accessibility (+1 more)

### Community 26 - "Language Agent & Model Errors"
Cohesion: 0.20
Nodes (4): LanguageAgent, Language Agent, ModelError, _NoModelRedirect

### Community 28 - "Mobile UX Principles"
Cohesion: 0.31
Nodes (3): Mobile UI Refinement, Hoober: How Users Hold Mobile Devices (2013), Mehrotra et al.: Action Bar Adaptations for One-Handed Use (2022)

### Community 32 - "Model Configuration Overrides"
Cohesion: 0.40
Nodes (4): OpenCode space-bunny-free Default Model, Model Configuration Update (12 Oct 2026), provider_error Field on Fallbacks, OpenCode Free-Tier HTTP 403 Limitation

### Community 34 - "Knowledge Agent"
Cohesion: 0.50
Nodes (3): KnowledgeAgent, Knowledge Agent, Four NCI Information Cards

### Community 36 - "Explore Activities"
Cohesion: 0.67
Nodes (4): Patient Explore Tab, Five-Senses Grounding Exercise (Final), Journal Reflection with Keyboard (Final), Explore Wellbeing Activities (no streaks)

## Knowledge Gaps
- **38 isolated node(s):** `WELCOME`, `RESTORING`, `HOME`, `ACTIVITY`, `CARE` (+33 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 231 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SahayViewModel` connect `ViewModel Content Actions` to `Android Care & Settings UI`, `Audio Transcription`, `MainActivity & Recording`, `ViewModel Account & Care Actions`, `Patient Screens (Chat/Help)`, `Content & Session Store`, `ViewModel State Types`, `ViewModel Navigation`, `Clinician Account Screens`, `Answer & Source Cards`, `Care HTTP Requests`?**
  _High betweenness centrality (0.199) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `SahayViewModel` (e.g. with `Content` and `SessionStore`) actually correct?**
  _`SahayViewModel` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `WELCOME`, `RESTORING`, `HOME` to the rest of the system?**
  _38 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `HTTP Server & Bookkeeping` be split into smaller, more focused modules?**
  _Cohesion score 0.061128526645768025 - nodes in this community are weakly interconnected._
- **Why does `Sahay README` connect `Deployment & Validation Docs` to `HTTP Server & Bookkeeping`, `Model Configuration Overrides`, `Model Client & Live Evals`, `Knowledge Agent`, `Specialist Agents & Architecture`, `Android App & Stitch Designs`, `Mobile UX Principles`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `SahayApp()` (e.g. with `.onCreate()` and `ClinicianAuthScreen()`) actually correct?**
  _`SahayApp()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Should `Android Care & Settings UI` be split into smaller, more focused modules?**
  _Cohesion score 0.058018018018018015 - nodes in this community are weakly interconnected._