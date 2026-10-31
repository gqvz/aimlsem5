# Graph Report - aiml  (2026-10-31)

## Corpus Check
- 116 files · ~339,477 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 24 file(s) not represented in the graph (top: .ttf 8, (none) 4, .xml 2)

## Summary
- 1414 nodes · 3294 edges · 70 communities (44 shown, 26 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 275 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3465364c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- test_model_configuration.py
- WellbeingLabel
- respond
- SahayUi.kt
- app.js
- MainActivity.kt
- CareUi.kt
- ApiClient
- Gentle Breathing Exercise
- guard/__init__.py
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
- WellbeingUi.kt
- Explore Wellbeing Activities (no streaks)
- kratos-stream.sh
- test_api_contract.py
- test_safety_corpus.py
- .__call__
- CLAUDE.md
- json
- llm.py
- create_app
- test_content.py
- Sahay README
- Models.kt
- ModelError
- Stitch Prompt: Clinician and Account Screens
- Repositories.kt
- pipeline.py
- run
- Model Configuration Update (12 Oct 2026)
- ContractTest
- AuthRepository
- similarity.py
- CareRepository
- ShareRepository
- Kratos LLM restored — 13 October 2026
- .stop
- speech.py
- RateLimiter
- Content
- test_care_api.py

## God Nodes (most connected - your core abstractions)
1. `SahayViewModel` - 135 edges
2. `User` - 57 edges
3. `Sahay Updated System Architecture (ARCHITECTURE.md)` - 43 edges
4. `ApiError` - 35 edges
5. `respond()` - 34 edges
6. `run()` - 32 edges
7. `SahayApp()` - 27 edges
8. `WellbeingLabel()` - 27 edges
9. `check()` - 27 edges
10. `create_app()` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Sahay Updated Architecture PDF (5 Oct snapshot)` --semantically_similar_to--> `Sahay Updated System Architecture (ARCHITECTURE.md)`  [INFERRED] [semantically similar]
  output/pdf/Sahay_Updated_Architecture.pdf → ARCHITECTURE.md
- `Phase 5: Android non-UI layers` --references--> `SahayViewModel`  [INFERRED]
  REWRITE_PLAN.md → android/app/src/main/java/org/sahay/app/SahayViewModel.kt
- `Why the view model is not rewired yet` --references--> `SahayViewModel`  [INFERRED]
  REWRITE_PLAN.md → android/app/src/main/java/org/sahay/app/SahayViewModel.kt
- `Safety (highest priority, this is a cancer-patient mental-health app)` --references--> `_passes_output_check()`  [INFERRED]
  REWRITE_PLAN.md → tests/test_output_filter.py
- `On-device Demo Fallback` --semantically_similar_to--> `Fixed Localized Fallback`  [INFERRED] [semantically similar]
  android/README.md → ARCHITECTURE.md

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

## Communities (70 total, 26 thin omitted)

### Community 0 - "test_model_configuration.py"
Cohesion: 0.15
Nodes (3): LocalProvider, ModelHttpTests, wire()

### Community 1 - "WellbeingLabel"
Cohesion: 0.19
Nodes (27): AgentInspector(), CareConversation(), CareInbox(), CareMessage(), SettingsPage(), SettingSwitch(), CalmFrame(), CalmTopBar() (+19 more)

### Community 2 - "respond"
Cohesion: 0.06
Nodes (11): respond(), AgentTests, CatalogueParityTests, SpecialistModel, EngineTests, FakeCrisisModel, FakeFabricatedCitationModel, FakeSafeModel (+3 more)

### Community 4 - "app.js"
Cohesion: 0.14
Nodes (39): CARD_COPY, CARD_FALLBACK, cardCopy(), changeView(), chat(), clearAll(), e(), exercise() (+31 more)

### Community 7 - "ApiClient"
Cohesion: 0.05
Nodes (31): ApiClient, ApiError, HttpRequest, HttpResponse, HttpTransport, UrlConnectionTransport, ApiClientTest, FakeTransport (+23 more)

### Community 8 - "Gentle Breathing Exercise"
Cohesion: 0.09
Nodes (29): QA: Garden Pairs memory game (8 face-down cards, 0 of 4 pairs), QA: Garden Pairs memory game (reveal state), QA: AI model settings with keyboard open, QA: AI model settings provider HTTP 403 error, QA: Today plan screen (sound sanctuary, gentle breath, companion), QA: Arrival screen 'Take a little pause' with garden, QA: Care team sign in, email field with keyboard (CTA disabled), QA: Care team sign in, password field with keyboard (CTA enabled) (+21 more)

### Community 10 - "guard/__init__.py"
Cohesion: 0.16
Nodes (7): has_medication_directive(), check_deterministic(), GuardVerdict, Completer, judge(), parse_verdict(), test_parse_verdict_ignores_unknown_categories()

### Community 11 - "Sahay Updated System Architecture (ARCHITECTURE.md)"
Cohesion: 0.13
Nodes (22): On-device Demo Fallback, Sahay Updated System Architecture (ARCHITECTURE.md), Activity Agent, Public Agent Trace (agent_trace), GET /api/health, POST /api/message, POST /api/model/test, Emotional Support Agent (+14 more)

### Community 13 - "SahayApp"
Cohesion: 0.22
Nodes (22): nativeText(), BottomNavigation(), ChatScreen(), ExerciseScreen(), HelpCard(), HelpScreen(), InfoScreen(), MessageBubble() (+14 more)

### Community 15 - "Screen"
Cohesion: 0.10
Nodes (21): Screen, ACTIVITY, ADMIN, AGENT_INSPECT, CARE, CHAT, CLINICIAN_AUTH, CLINICIAN_DETAIL (+13 more)

### Community 16 - "SahayViewModel.kt"
Cohesion: 0.12
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
Cohesion: 0.16
Nodes (18): check(), Judge, test_approved_numbers_and_sources_pass(), test_close_paraphrases_are_blocked_without_a_model(), test_curated_copy_is_never_blocked(), test_deterministic_rules(), test_every_exemplar_is_blocked_without_a_model(), test_hedged_stop_taking_sentence_is_blocked_on_purpose() (+10 more)

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
Cohesion: 0.06
Nodes (89): downgrade(), upgrade(), client_ip(), current_user(), json_body(), throttle(), dependency(), accept_invite() (+81 more)

### Community 27 - "db.py"
Cohesion: 0.16
Nodes (3): Settings, get_engine(), _path()

### Community 28 - "Mobile UI Refinement"
Cohesion: 0.31
Nodes (3): Mobile UI Refinement, Hoober: How Users Hold Mobile Devices (2013), Mehrotra et al.: Action Bar Adaptations for One-Handed Use (2022)

### Community 32 - "Rebuild and emulator validation - 13 October 2026"
Cohesion: 0.22
Nodes (8): Artifacts, Emulator and memory, Fix made during checking, Graphify, Live AI limitation, Rebuild and emulator validation - 13 October 2026, Result, Verification

### Community 36 - "Explore Wellbeing Activities (no streaks)"
Cohesion: 0.67
Nodes (4): Patient Explore Tab, Five-Senses Grounding Exercise (Final), Journal Reflection with Keyboard (Final), Explore Wellbeing Activities (no streaks)

### Community 41 - "test_api_contract.py"
Cohesion: 0.09
Nodes (18): get_session(), session_scope(), test_admin_invite_accept_and_deactivate(), test_audit_never_stores_message_text(), test_duplicate_contact_is_conflict(), test_profile_and_password_flow(), test_queue_hides_content_and_other_clinicians_cannot_open_a_share(), test_share_validation() (+10 more)

### Community 42 - "test_safety_corpus.py"
Cohesion: 0.15
Nodes (8): _passes_output_check(), test_calm_reply_passes(), test_english_claims_are_blocked_even_in_a_translated_reply(), test_unsafe_reply_is_blocked(), assess(), _param(), test_patterns_use_only_their_own_script(), test_safety_gate()

### Community 46 - "json"
Cohesion: 0.19
Nodes (3): main(), call(), save()

### Community 47 - "llm.py"
Cohesion: 0.10
Nodes (7): judge_client(), allowed_model_hosts(), model_for_request(), ModelClient, public_model_configuration(), test_model_connection(), ModelConfigurationTests

### Community 48 - "create_app"
Cohesion: 0.17
Nodes (7): create_app(), lifespan(), main(), seed_demo(), init_db(), test_every_route_is_documented_with_its_method(), test_openapi_snapshot_is_current()

### Community 49 - "test_content.py"
Cohesion: 0.13
Nodes (12): bundle(), india_guidance(), india_topics(), _load(), test_activities_are_well_formed(), test_bundle_is_json_and_version_is_stable(), test_cost_worries_get_the_scheme_card_and_name_the_helpline(), test_every_india_checkin_exists_in_every_language() (+4 more)

### Community 50 - "Sahay README"
Cohesion: 0.18
Nodes (11): Sahay for Android README, Sahay_Pixel_9 Low-memory Emulator, Clinician File Request Fulfillment, Knowledge Agent, Sahay Overhaul Validation (5 Oct 2026), Sahay README, Four NCI Information Cards, Quick Hide Screen Cover (+3 more)

### Community 51 - "Models.kt"
Cohesion: 0.23
Nodes (9): CareFile, CareMessage, map(), PlanItem, Reply, SourceCard, strOrNull(), Suggestion (+1 more)

### Community 52 - "ModelError"
Cohesion: 0.22
Nodes (3): BoundedModel, ModelError, _NoModelRedirect

### Community 53 - "Stitch Prompt: Clinician and Account Screens"
Cohesion: 0.22
Nodes (5): Two-way Care Messages and Files, SQLite Persistent Storage (/data volume), Stitch Prompt: Clinician and Account Screens, Admin Bookkeeping Screens, Clinician Review Queue Screen

### Community 54 - "Repositories.kt"
Cohesion: 0.13
Nodes (5): Health, AdminRepository, Attachment, ChatRepository, ModelConfig

### Community 55 - "pipeline.py"
Cohesion: 0.14
Nodes (12): rule_risk(), choose_card(), fallback_response(), is_financial(), route(), safety_gate(), choose_action(), assess() (+4 more)

### Community 56 - "run"
Cohesion: 0.12
Nodes (22): activity_plan(), Ctx, draft(), _history(), localise(), result(), run(), understand() (+14 more)

### Community 57 - "Model Configuration Update (12 Oct 2026)"
Cohesion: 0.40
Nodes (4): OpenCode space-bunny-free Default Model, Model Configuration Update (12 Oct 2026), provider_error Field on Fallbacks, OpenCode Free-Tier HTTP 403 Limitation

### Community 59 - "AuthRepository"
Cohesion: 0.23
Nodes (3): SessionInfo, User, AuthRepository

### Community 60 - "similarity.py"
Cohesion: 0.16
Nodes (8): _cosine(), _grams(), Hit, _norm(), SimilarityGuard, _variants(), normalize(), repair_typos()

### Community 61 - "CareRepository"
Cohesion: 0.20
Nodes (3): CareThread, Contact, CareRepository

### Community 66 - "speech.py"
Cohesion: 0.54
Nodes (4): _headers(), status(), synthesize(), transcribe()

### Community 70 - "test_care_api.py"
Cohesion: 0.14
Nodes (6): file(), test_either_side_can_initiate_and_upload_without_request(), test_invalid_files_do_not_create_message_or_fulfil_request(), test_outsider_cannot_read_write_or_download(), test_request_requires_file_and_fulfils_only_once(), SpeechTests

## Knowledge Gaps
- **45 isolated node(s):** `WELCOME`, `RESTORING`, `HOME`, `ACTIVITY`, `CARE` (+40 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 394 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **26 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SahayViewModel` connect `SahayViewModel` to `WellbeingLabel`, `.transcribeAudio`, `Content`, `MainActivity.kt`, `ApiClient`, `.accountRequest`, `.navigate`, `SahayApp`, `SessionStore.kt`, `SahayViewModel.kt`, `Composable`, `.send`, `JSONObject`?**
  _High betweenness centrality (0.187) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `SahayViewModel` (e.g. with `Content` and `SessionStore`) actually correct?**
  _`SahayViewModel` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `WELCOME`, `RESTORING`, `HOME` to the rest of the system?**
  _45 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `respond` be split into smaller, more focused modules?**
  _Cohesion score 0.06352941176470588 - nodes in this community are weakly interconnected._
- **Why does `revoke_share()` connect `routes.py` to `SahayUi.kt`?**
  _High betweenness centrality (0.185) - this node is a cross-community bridge._
- **Are the 48 inferred relationships involving `User` (e.g. with `current_user()` and `admin_audit()`) actually correct?**
  _`User` has 48 INFERRED edges - model-reasoned connections that need verification._
- **Should `SahayUi.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.029686769518466573 - nodes in this community are weakly interconnected._