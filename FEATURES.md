# Sahay: features and project brief

Sahay is an Android emotional wellbeing prototype for adults affected by cancer in India. It is an AI assistant, not a clinician: it does not diagnose, prescribe, monitor conversations or contact anyone automatically. Status as of 31 October 2026.

## 1. Implemented features

| Area | Features |
| --- | --- |
| Patient home | Optional garden arrival, breathing invitation, mood and energy check-in, weekly mood view, AI-suggested activity plan |
| AI companion | Text chat, voice input with transcript review before sending, spoken replies, curated source cards, visible agent handoffs |
| 14 offline activities | Breathing, grounding, meditation, body scan, safe place, sound sanctuary, sleep wind-down, matching pairs, stretch, drawing, small comforts, worry box, journal, fictional support notes |
| Visit preparation | Question list, AI-drafted brief, patient editing, clinician selection, explicit consent, revocation |
| Care communication | Two-way patient-clinician messages, file sharing (PDF, PNG, JPEG, text), clinician file requests |
| Clinician and admin | Review queue, status and notes, invitation-only clinician accounts, deactivation, audit view |
| Help | Always-available Help with 112, Tele-MANAS 14416 and a saved care-team number |
| Safety | Rule-based crisis and urgent-medical routing before any model call, fixed multilingual help replies, medical-advice boundary, output review with fixed fallbacks |
| Privacy and access | Quick Hide, local storage off by default, clear-data control, larger text, reduced motion, four languages |

## 2. Technology

| Layer | Stack |
| --- | --- |
| Client | Kotlin, Jetpack Compose, Material 3, Android Keystore (AES-GCM session storage); API 26+ |
| Backend | Python 3.12 standard-library HTTP server, JSON API, SQLite |
| AI | Custom eight-agent request graph (Language, Safety, Supervisor, Emotional support, Knowledge, Activity, Visit preparation, Review), any OpenAI-compatible model endpoint, tested with Gemma through Ollama |
| Deployment | Docker Compose, non-root container, private SSH model bridge |
| Security | Salted scrypt passwords, hashed tokens, role and ownership checks, file validation, audit log |

## 3. Indian cultural significance

These are design rationales, not validated cultural outcomes.

- **Name:** *Sahay* comes from the Hindi word for help or support.
- **Language access:** English, Hindi, Bengali and Tamil in native scripts. Mixed Hinglish input is accepted, reflecting everyday code-switching.
- **India-specific help:** routes people to 112 and Tele-MANAS (14416), not foreign helplines.
- **Family-centred care:** nondisclosure of a cancer diagnosis is common in family-centric settings, and stigma is a recognised barrier to care \[6]\[7]. Quick Hide and patient-controlled sharing protect privacy on shared phones and in shared homes.
- **Care-gap fit:** Indian reviews report unmet psycho-oncology need and few trained professionals \[7]\[8]. Sahay supports people between visits and points them to humans; it does not replace care.
- **Visual language:** the courtyard imagery and terracotta palette draw on the familiar calm of an Indian home.

**Gap:** there is no family or caregiver role yet, and no native-speaker or patient review of the copy has been done.

## 4. Background search and prior art

**Status: preliminary.** Web screening was done on 31 October 2026 from abstracts and search snippets, not full-text review. Check each reference before formal submission. The `resources/` folder cited by `ARCHITECTURE.md` is not in this repository.

| # | Source | Relevance to Sahay |
| --- | --- | --- |
| 1 | [Jiang et al., J Med Internet Res 2025](https://www.jmir.org/2025/1/e76968): systematic review, 17 studies, 1,817 patients | Conversational agents are feasible in cancer care; psychological benefit is limited |
| 2 | [Xu et al., JMIR Cancer 2021](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC8669585/) | Overview of chatbots in oncology |
| 3 | [Wu et al., MindfulAgents, CHI 2026](https://arxiv.org/abs/2603.06926) | Multi-agent, expert-aligned mindfulness personalisation |
| 4 | [5-stage taxonomy of AI agents, JMIR Mental Health 2026](https://mental.jmir.org/2026/1/e91746) | Places therapist-in-the-loop systems on an autonomy scale |
| 5 | [HEALTH-PARIKSHA](https://arxiv.org/abs/2410.13671), [Samiksha](https://arxiv.org/abs/2509.24506), [SurakshaEval](https://arxiv.org/abs/2608.07862) | Indian-language health and safety evaluations; Indic-script, code-mixed and culturally specific queries remain hard for models |
| 6 | [Maya & Chawak 2021](https://publications.iith.ac.in/publication/nondisclosure-of-a-cancer-diagnosis-and-prognosis-recommendations) | Cancer nondisclosure in family-centric cultures |
| 7 | [Brahmbhatt & Desai 2023](https://www.ijcmph.com/index.php/ijcmph/article/view/10982) | Unmet psycho-oncology need and stigma in India |
| 8 | [ORF commentary](https://www.orfonline.org/expert-speak/integrating-psycho-oncological-support-in-cancer-care) | Psycho-oncology referral and caregiving gaps (grey literature) |
| 9 | [Wysa in cancer, NCT07167056](https://clinicaltrials.gov/study/NCT07167056) | Closest competing trial: US, app added to psychotherapy, no results seen |

**Prior IP: not yet done properly.** A limited Google Patents query found no patent matching the whole concept. The closest hits were [US20170300648A1](https://patents.google.com/patent/US20170300648A1/en) (conversational patient data collection with an emotion model) and [US11120895B2](https://patents.google.com/patent/US11120895B2/en) (clinician-controlled mental health assessment). Their legal status was not checked, and this says nothing about patentability or freedom to operate.

Required next: search InPASS (Indian Patent Office), Espacenet, USPTO and WIPO PATENTSCOPE using classes such as G16H 20/70 and G06N, keep a search log, and get a qualified patent professional's review.

## 5. Novelty

**Working hypothesis, to be confirmed by the searches above.** The screening found no single system combining all of the following:

1. Safety routing on both the original-language text and its English translation, before specialist generation.
2. Fixed multilingual crisis replies that bypass the model for explicit urgent input.
3. Bounded agent handoffs with no database, messaging or clinician-contact authority, and a visible trace.
4. Patient-edited, consent-gated, revocable visit briefs instead of chat monitoring.
5. Oncology-specific emotional support in Indian languages, with offline activities when AI is unavailable.

Individual elements exist in prior work. Any novelty lies in the combination for this population, and it is not yet a patent claim.

## 6. Target application, end users and validation

**Application:** emotional support and visit preparation between oncology appointments. It is not for diagnosis, treatment or emergency monitoring.

| End user | Need | Validation measure |
| --- | --- | --- |
| Adults with cancer in India using Android, in the four supported languages | Private support, activities, help with visits | Usability, acceptability, distress on a validated scale |
| Oncology clinicians and counsellors | Patient-approved context | Brief usefulness and accuracy, review burden |
| Family caregivers (indirect) | Not yet served | Needs interviews before adding a role |
| Hospital administrators | Access control | Access-control and audit review |

**Evidence so far:** 41 backend tests and 22/22 fictional Gemma routing cases on 13 October 2026. These are engineering checks, not clinical evidence. The default OpenCode model has returned HTTP 403, and the newest design-system UI has not been compiled.

**Proposed plan (not started):**

1. Safety: expert-authored crisis and medical test sets in each language, including mixed-language and speech input, measuring missed-crisis rate.
2. Language: native-speaker review of all copy, especially safety text.
3. Usability: moderated sessions with survivors or patient advocates in each language.
4. Pilot: with ethics committee approval and clinical partners, a feasibility study measuring engagement, safety events and distress change.

Only fictional data may be used until ethics approval and production security are in place.

## 7. Standardisation and interoperability

| Standard already in use | Benefit to other groups |
| --- | --- |
| JSON over HTTP under `/api/*` | Any web or mobile client can integrate |
| OpenAI-compatible Chat Completions | Models can be swapped by URL and name |
| OpenAI-compatible STT and TTS adapters | Speech providers can be swapped |
| Docker Compose and `.env` configuration | Same local deployment across teams |
| SQLite through a small data layer | Simple to migrate to a shared database |
| ISO language codes (`en`, `hi`, `bn`, `ta`) | Language handling aligns across projects |

**Not yet standardised:**

- No published OpenAPI contract, and routes carry no version number.
- No CI/CD pipeline in this repository.
- Custom bearer sessions, not OAuth 2.0 or OIDC, so no single sign-on across projects.
- No FHIR or ABDM integration for health records.
- No TLS, rate limiting or SSRF protection on the guest model endpoint option, and no shared logging or monitoring.

**Recommended cross-group baseline:** OpenAPI 3 contracts under `/api/v1`, OIDC for identity, Docker images built by shared CI, PostgreSQL for shared deployments, FHIR for clinical data exchange, and TLS everywhere. The group should agree on this before integration.
