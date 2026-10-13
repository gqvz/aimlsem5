# Graph Report - aiml  (2026-10-14)

## Corpus Check
- 104 files · ~334,859 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 24 file(s) not represented in the graph (top: .ttf 8, (none) 4, .xml 2)

## Summary
- 1253 nodes · 2913 edges · 57 communities (39 shown, 18 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 269 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `61935e5e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- test_model_configuration.py
- WellbeingUi.kt
- respond
- app.js
- MainActivity.kt
- CareUi.kt
- Gentle Breathing Exercise
- pipeline.py
- Sahay Updated System Architecture (ARCHITECTURE.md)
- SahayViewModel
- SahayApp
- SessionStore.kt
- Screen
- SahayViewModel.kt
- gemma3:12b via Ollama (resume-ollama)
- Stitch Account and Sharing References
- Settings screen (account + Language and comfort)
- Care Conversation Screen (Android)
- Patient-Clinician Care Conversation (messages and files)
- test_guard.py
- Explore/Activities Screen QA (English)
- Composable
- Patient-Clinician Messaging and Files
- routes.py
- db.py
- Mobile UI Refinement
- .send
- Api
- Rebuild and emulator validation - 13 October 2026
- .transcribeAudio
- Explore Wellbeing Activities (no streaks)
- kratos-stream.sh
- test_api_contract.py
- Sahay rewrite plan (non-UI)
- .__call__
- engine.py
- CLAUDE.md
- json
- llm.py
- accounts.py
- ApiError
- Sahay README
- shares.py
- ModelError
- Stitch Prompt: Clinician and Account Screens
- run
- Model Configuration Update (12 Oct 2026)
- Kratos LLM restored — 13 October 2026

## God Nodes (most connected - your core abstractions)
1. `SahayViewModel` - 134 edges
2. `User` - 57 edges
3. `Sahay Updated System Architecture (ARCHITECTURE.md)` - 43 edges
4. `ApiError` - 35 edges
5. `respond()` - 34 edges
6. `run()` - 30 edges
7. `SahayApp()` - 27 edges
8. `WellbeingLabel()` - 27 edges
9. `check()` - 27 edges
10. `Screen` - 22 edges

## Surprising Connections (you probably didn't know these)
- `Sahay Updated Architecture PDF (5 Oct snapshot)` --semantically_similar_to--> `Sahay Updated System Architecture (ARCHITECTURE.md)`  [INFERRED] [semantically similar]
  output/pdf/Sahay_Updated_Architecture.pdf → ARCHITECTURE.md
- `Phase 5: Android non-UI layers` --references--> `SahayViewModel`  [INFERRED]
  REWRITE_PLAN.md → android/app/src/main/java/org/sahay/app/SahayViewModel.kt
- `Safety (highest priority, this is a cancer-patient mental-health app)` --references--> `_passes_output_check()`  [INFERRED]
  REWRITE_PLAN.md → tests/test_output_filter.py
- `On-device Demo Fallback` --semantically_similar_to--> `Fixed Localized Fallback`  [INFERRED] [semantically similar]
  android/README.md → ARCHITECTURE.md
- `Sahay Updated Architecture PDF (5 Oct snapshot)` --references--> `gemma3:12b via Ollama (resume-ollama)`  [EXTRACTED]
  output/pdf/Sahay_Updated_Architecture.pdf → ARCHITECTURE.md

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
- **Sahay Shared Brand Assets (Android + Web)** — android_app_src_main_res_drawable_nodpi_courtyard, android_app_src_main_res_drawable_nodpi_sahay_emblem, web_assets_courtyard, web_assets_sahay_emblem [INFERRED 0.85]
- **Sahay calming activities (plan, breathing, memory game)** — artifacts_qa_plan, sahay_gentle_breathing, sahay_garden_pairs_game, sahay_calm_non_pressuring_design [INFERRED 0.85]
- **Keyboard-overlap QA screenshots** — artifacts_qa_model_settings_keyboard, artifacts_qa_refinement_auth_email_keyboard, artifacts_qa_refinement_auth_keyboard, sahay_keyboard_safe_sticky_cta [INFERRED 0.85]
- **Sahay main tab screens** — sahay_today_screen, sahay_explore_screen, sahay_settings_screen, sahay_bottom_navigation [INFERRED 0.85]
- **Sahay Android QA Screenshots** — artifacts_qa_activities, artifacts_qa_activities_hindi, artifacts_qa_arrival, artifacts_qa_arrival_final, artifacts_qa_art [INFERRED 0.85]
- **Tamil small-screen refinement QA pass** — artifacts_qa_refinement_small_tamil_breathing, artifacts_qa_refinement_small_tamil_breathing_final, artifacts_qa_refinement_small_tamil_explore, artifacts_qa_refinement_small_tamil_explore_final, artifacts_qa_refinement_small_tamil_navigation_final, artifacts_qa_refinement_small_tamil_settings [INFERRED 0.85]

## Communities (57 total, 18 thin omitted)

### Community 0 - "test_model_configuration.py"
Cohesion: 0.10
Nodes (3): LocalProvider, wire(), SpeechTests

### Community 1 - "WellbeingUi.kt"
Cohesion: 0.05
Nodes (29): AmbientAudio, AgentInspector(), CareConversation(), CareInbox(), CareMessage(), SettingsPage(), SettingSwitch(), CalmFrame() (+21 more)

### Community 2 - "respond"
Cohesion: 0.06
Nodes (11): respond(), AgentTests, CatalogueParityTests, SpecialistModel, EngineTests, FakeCrisisModel, FakeFabricatedCitationModel, FakeSafeModel (+3 more)

### Community 4 - "app.js"
Cohesion: 0.14
Nodes (39): CARD_COPY, CARD_FALLBACK, cardCopy(), changeView(), chat(), clearAll(), e(), exercise() (+31 more)

### Community 8 - "Gentle Breathing Exercise"
Cohesion: 0.09
Nodes (29): QA: Garden Pairs memory game (8 face-down cards, 0 of 4 pairs), QA: Garden Pairs memory game (reveal state), QA: AI model settings with keyboard open, QA: AI model settings provider HTTP 403 error, QA: Today plan screen (sound sanctuary, gentle breath, companion), QA: Arrival screen 'Take a little pause' with garden, QA: Care team sign in, email field with keyboard (CTA disabled), QA: Care team sign in, password field with keyboard (CTA enabled) (+21 more)

### Community 10 - "pipeline.py"
Cohesion: 0.18
Nodes (11): activity_plan(), Ctx, draft(), _history(), localise(), result(), route(), safety_gate() (+3 more)

### Community 11 - "Sahay Updated System Architecture (ARCHITECTURE.md)"
Cohesion: 0.13
Nodes (22): On-device Demo Fallback, Sahay Updated System Architecture (ARCHITECTURE.md), Activity Agent, Public Agent Trace (agent_trace), GET /api/health, POST /api/message, POST /api/model/test, Emotional Support Agent (+14 more)

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

### Community 17 - "gemma3:12b via Ollama (resume-ollama)"
Cohesion: 0.28
Nodes (8): Docker Course Deployment, gemma3:12b via Ollama (resume-ollama), compose.yaml (Backend Service), compose.gemma.yaml (Optional SSH Model Bridge), Live Gemma Evaluation (Fictional Course Cases), Invented Source Links Found in First Pass, 19 Fictional Live Gemma Cases, 22/22 Fictional Gemma Cases Passed

### Community 18 - "Stitch Account and Sharing References"
Cohesion: 0.24
Nodes (12): Sahay Android App (Kotlin/Jetpack Compose), Four-Language Support (en, hi, bn, ta), Role Routing (Patient/Clinician/Admin), Sahay Wellbeing Prototype, SahayViewModel, SessionStore AES-GCM Session Encryption, Stitch Screen: Clinician Invited Sign-In, Stitch Screen: Share Visit Brief & Consent (+4 more)

### Community 19 - "Settings screen (account + Language and comfort)"
Cohesion: 0.20
Nodes (13): Bottom nav: Today/Talk/Explore/Care team/Settings, One gentle breath (two-minute breathing, no holds), Today screen (refined): breathing card first, check-in collapsed to row, Settings - clinician account Dr. Meera Rao (demo), Settings - clinician account (final, with Help + privacy toggle), Settings - guest with 'Session expired' banner, Settings - private guest session (patient/care team sign in), Settings in Hindi (partial localization, English strings remain) (+5 more)

### Community 20 - "Care Conversation Screen (Android)"
Cohesion: 0.15
Nodes (16): Care Conversation Screen (Android), File Attachment Chip, Gentle Breathing Exercise, Medical File Request Flow, Privacy Hide Toggle and Help Icon (App Bar), Gentle Breathing Screen Screenshot, Care Conversation Pending Attachment Screenshot, Care Send Confirmation Snackbar Screenshot (+8 more)

### Community 21 - "Patient-Clinician Care Conversation (messages and files)"
Cohesion: 0.19
Nodes (15): Gentle Breathing Exercise Screen, Gentle Breathing Exercise, Clinician Patient Conversation Files Screen, Patient-Clinician Care Conversation (messages and files), Clinician Shared Visit Briefs Queue, Clinician Sign-In Session Expired Screen, Care Team Sign-In / Accept Invite, Shared Visit Briefs (patient-approved snapshots) (+7 more)

### Community 22 - "test_guard.py"
Cohesion: 0.06
Nodes (34): has_medication_directive(), check(), check_deterministic(), GuardVerdict, Completer, judge(), parse_verdict(), _cosine() (+26 more)

### Community 23 - "Explore/Activities Screen QA (English)"
Cohesion: 0.19
Nodes (12): Courtyard Background Image (Android), Sahay Emblem (Android), Explore/Activities Screen QA (English), Explore/Activities Screen QA (Hindi), Arrival Screen QA (Initial), Arrival Screen QA (Final), Colour Your Own Sky Drawing Screen QA, Sahay Mental Wellbeing App (+4 more)

### Community 24 - "Composable"
Cohesion: 0.47
Nodes (10): ClinicianAuthScreen(), ClinicianQueueScreen(), displayIst(), FlowButton(), FlowCard(), FlowShell(), PasswordField(), PatientAccountScreen() (+2 more)

### Community 25 - "Patient-Clinician Messaging and Files"
Cohesion: 0.24
Nodes (9): Care Conversation Thread (Return, Final), Clinician Sign-in Screen, Clinician Patient Conversations List, Clinician Review Queue (Shared Visit Briefs), Clinician Settings Screen (Final), Clinician Review Queue with Bottom Tabs (Final), Sahay Clinician Portal, Multilingual and Larger-Text Accessibility (+1 more)

### Community 26 - "routes.py"
Cohesion: 0.17
Nodes (33): accept_invite(), admin_audit(), admin_users(), cards(), care_contacts(), care_file(), care_messages(), care_open() (+25 more)

### Community 27 - "db.py"
Cohesion: 0.07
Nodes (15): create_app(), lifespan(), client_ip(), current_user(), json_body(), throttle(), dependency(), main() (+7 more)

### Community 28 - "Mobile UI Refinement"
Cohesion: 0.31
Nodes (3): Mobile UI Refinement, Hoober: How Users Hold Mobile Devices (2013), Mehrotra et al.: Action Bar Adaptations for One-Handed Use (2022)

### Community 31 - "Api"
Cohesion: 0.05
Nodes (8): dispose_all(), RateLimiter, Api, ModelHttpTests, assess(), _param(), test_patterns_use_only_their_own_script(), test_safety_gate()

### Community 32 - "Rebuild and emulator validation - 13 October 2026"
Cohesion: 0.22
Nodes (8): Artifacts, Emulator and memory, Fix made during checking, Graphify, Live AI limitation, Rebuild and emulator validation - 13 October 2026, Result, Verification

### Community 36 - "Explore Wellbeing Activities (no streaks)"
Cohesion: 0.67
Nodes (4): Patient Explore Tab, Five-Senses Grounding Exercise (Final), Journal Reflection with Keyboard (Final), Explore Wellbeing Activities (no streaks)

### Community 41 - "test_api_contract.py"
Cohesion: 0.08
Nodes (23): get_session(), session_scope(), test_admin_invite_accept_and_deactivate(), test_audit_never_stores_message_text(), test_duplicate_contact_is_conflict(), test_profile_and_password_flow(), test_queue_hides_content_and_other_clinicians_cannot_open_a_share(), test_share_validation() (+15 more)

### Community 42 - "Sahay rewrite plan (non-UI)"
Cohesion: 0.09
Nodes (21): 1. What I read, 2. Findings, 2a. Progress, 3. Target architecture, 4. Phased plan, 5. Testing and quality gates, 6. Risks, 7. Decisions needed (+13 more)

### Community 44 - "engine.py"
Cohesion: 0.14
Nodes (10): rule_risk(), choose_card(), choose_action(), assess(), matches(), more_cautious(), RiskAssessment, _variants() (+2 more)

### Community 46 - "json"
Cohesion: 0.25
Nodes (5): main(), _headers(), status(), synthesize(), transcribe()

### Community 47 - "llm.py"
Cohesion: 0.11
Nodes (7): judge_client(), allowed_model_hosts(), model_for_request(), ModelClient, public_model_configuration(), test_model_connection(), ModelConfigurationTests

### Community 48 - "accounts.py"
Cohesion: 0.19
Nodes (26): Audit, Invitation, UserSession, hash_password(), hash_token(), new_id(), new_token(), verify_password() (+18 more)

### Community 49 - "ApiError"
Cohesion: 0.17
Nodes (15): downgrade(), upgrade(), ApiError, Base, CareFile, CareMessage, CareThread, attachment() (+7 more)

### Community 50 - "Sahay README"
Cohesion: 0.18
Nodes (11): Sahay for Android README, Sahay_Pixel_9 Low-memory Emulator, Clinician File Request Fulfillment, Knowledge Agent, Sahay Overhaul Validation (5 Oct 2026), Sahay README, Four NCI Information Cards, Quick Hide Screen Cover (+3 more)

### Community 51 - "shares.py"
Cohesion: 0.46
Nodes (9): Share, clinician_view(), create(), mine(), _owned(), queue(), review(), revoke() (+1 more)

### Community 52 - "ModelError"
Cohesion: 0.20
Nodes (3): BoundedModel, ModelError, _NoModelRedirect

### Community 53 - "Stitch Prompt: Clinician and Account Screens"
Cohesion: 0.22
Nodes (5): Two-way Care Messages and Files, SQLite Persistent Storage (/data volume), Stitch Prompt: Clinician and Account Screens, Admin Bookkeeping Screens, Clinician Review Queue Screen

### Community 56 - "run"
Cohesion: 0.24
Nodes (12): fallback_response(), run(), Scripted, test_a_guard_block_replaces_the_reply_with_fixed_copy(), test_english_turn_uses_three_calls(), test_hindi_turn_adds_one_translation_call(), test_model_cannot_lower_a_rule_detected_risk(), test_model_cannot_pick_an_unknown_tool() (+4 more)

### Community 57 - "Model Configuration Update (12 Oct 2026)"
Cohesion: 0.40
Nodes (4): OpenCode space-bunny-free Default Model, Model Configuration Update (12 Oct 2026), provider_error Field on Fallbacks, OpenCode Free-Tier HTTP 403 Limitation

## Knowledge Gaps
- **45 isolated node(s):** `WELCOME`, `RESTORING`, `HOME`, `ACTIVITY`, `CARE` (+40 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 374 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **18 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `revoke_share()` connect `routes.py` to `shares.py`, `SahayUi.kt`?**
  _High betweenness centrality (0.208) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `SahayViewModel` (e.g. with `Content` and `SessionStore`) actually correct?**
  _`SahayViewModel` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `WELCOME`, `RESTORING`, `HOME` to the rest of the system?**
  _45 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `test_model_configuration.py` be split into smaller, more focused modules?**
  _Cohesion score 0.10476190476190476 - nodes in this community are weakly interconnected._
- **Why does `SahayViewModel` connect `SahayViewModel` to `WellbeingUi.kt`, `.navigate`, `.transcribeAudio`, `MainActivity.kt`, `.accountRequest`, `Sahay rewrite plan (non-UI)`, `SahayApp`, `SessionStore.kt`, `SahayViewModel.kt`, `Composable`, `.send`, `JSONObject`?**
  _High betweenness centrality (0.203) - this node is a cross-community bridge._
- **Are the 48 inferred relationships involving `User` (e.g. with `current_user()` and `admin_audit()`) actually correct?**
  _`User` has 48 INFERRED edges - model-reasoned connections that need verification._
- **Should `WellbeingUi.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05401234567901234 - nodes in this community are weakly interconnected._