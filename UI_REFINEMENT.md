# Mobile UI refinement

## Design decisions

Sahay is a calm Android wellbeing space. The visual direction keeps its muted greens and warm, simple artwork. This pass uses the `mobile-app-ui-design` skill and focuses on layout and behavior.

| Finding | Applied change | Rationale |
| --- | --- | --- |
| Activity controls disappear while reading or scrolling | Separate scrollable content from a persistent bottom action area | Keep pause, next, finish and save reachable without finding them again |
| Care composer follows the entire message history | Independently scrolling conversation with a docked composer above the keyboard | Messaging should work regardless of the conversation's length |
| Too many equally prominent choices on Today | One breathing invitation first; optional check-in expands on demand | A person can start without first evaluating the full activity catalogue |
| Nine undifferentiated Explore entries | Rest and reset, A little play, Make and reflect | Meaningful groups support scanning while keeping every activity available |
| Small controls and inconsistent spacing | 48dp icon targets, 56dp primary buttons, 8/16/24dp spacing rhythm, 20dp content margin | Improve selection accuracy and make related content visually coherent |
| Account form headers and submit actions scroll away | Shared fixed app bar, docked sign-in actions, consistent cards and form spacing | Keep back, help, privacy and clinician settings in predictable locations |
| Settings presents a stack of equally weighted buttons | Familiar account/action rows, grouped preferences, full-row switches | Distinguish navigation from values and reduce unnecessary visual competition |
| Rust/green/lavender selection indicators vary by screen | Consistent green selection and clearer input outlines | Reinforce predictable active and editable states |
| Small secondary copy | Main content uses 14/16/20/28sp hierarchy; nav labels retain 12sp | Preserve readability without shrinking labels to squeeze in content |

Primary actions span the bottom content area of focused screens rather than assuming every person uses a right thumb. Secondary back navigation stays in the familiar Android position, with system Back also supported. Gesture and keyboard insets are handled by the parent Scaffold, and consumed before child keyboard padding, to avoid double padding.

There are no streaks or success scores attached to wellbeing. Activity completion uses calm closure rather than performance pressure. Clinical file requests and replies remain explicit, with attachments visible before sending. An unsent care draft survives leaving and returning to the same conversation during the session; sign-out and clearing local data remove it. Bottom navigation uses concise localized labels with full destination descriptions for accessibility. Clinician filters wrap into two rows rather than squeezing four controls together. Clinicians have separate Review, Messages and Settings bottom destinations. Help and sign-in return to the screen they were opened from. Care scrolls to a newly sent message after its response loads, and preserves the reading position when browsing older messages. Transient confirmation messages are positioned above the measured action area so they do not cover Send, Pause or Save. Email fields use a Next keyboard action to reach Password.

## Research used

- [Fitts's Law, Laws of UX](https://lawsofux.com/fittss-law/): acquisition depends on size and distance. Applied as generous targets, separation, and reachable task actions.
- [Law of Proximity, Laws of UX](https://lawsofux.com/law-of-proximity/): related elements should read as a group. Applied as smaller internal gaps and clearer separation between sections.
- [Hick's Law, Laws of UX](https://lawsofux.com/hicks-law/): choice complexity adds decision work. Applied as optional check-in and activity grouping, with one clear first action.
- [Android touch target guidance](https://support.google.com/accessibility/android/answer/7101858?hl=en): 48dp targets and approximately 8dp separation.
- [Compose accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults): interactive Material controls and meaningful merged semantics; preference rows expose the entire label and switch as one action.
- [Compose Material insets](https://developer.android.com/develop/ui/compose/system/material-insets): apply and consume Scaffold padding, respect navigation bars and IME.
- [Material bottom app bars](https://m2.material.io/components/app-bars-bottom/ios): Google's ergonomic guidance supports placing task controls near the bottom. Sahay uses a simple action area for focused tasks and a navigation bar for top-level destinations.
- [Mehrotra, Das and Zanwar, Action Bar Adaptations for One-Handed Use of Smartphones (2022)](https://arxiv.org/abs/2208.08734): an exploratory HCI study supporting examination of reach and grip stability. It does not establish a universal thumb zone or validate Sahay with cancer patients.

- [Steven Hoober, How Do Users Really Hold Mobile Devices? (2013)](https://www.uxmatters.com/mt/archives/2013/02/how-do-users-really-hold-mobile-devices.php): original field observations show varied grips and changing hand use. Used to avoid assuming a single dominant hand or fixed reach map.

## Verification

The final APK built successfully with `assembleDebug --offline --no-daemon` (43 seconds) and was installed on the emulator. Completed checks:

- API 35 emulator, 720 × 1616 pixels at 280dpi (about 411 × 923dp).
- Compact 360 × 808dp configuration at 320dpi with the app's 1.16× larger-text option and Tamil enabled.
- Rendered Today, Explore, Settings, arrival, breathing, clinician sign-in/queue and care conversation screens.
- Measured 56dp primary activity and care actions, plus a comfortably sized activity row.
- Clinician login reached its role-specific review queue; the login action remained above the IME.
- An unsent care draft survived navigation away and back. A fictional file request was accepted by the backend.
- A 47-byte fictional text file was selected from Android Documents and sent without a preceding request.

Further rendered checks passed:

- A newly sent message is visible after the server response, rather than staying behind an older card.
- The send confirmation sits above the composer and leaves the controls exposed.
- Help returns directly to the conversation and retains its unsent draft.
- A cold start restores the clinician review queue.

Final installation checks also passed:

- Clinician Review, Messages and Settings tabs have named accessibility targets; their tap areas were measured on the device.
- Sending into a conversation containing multiple earlier messages and attachments scrolls the new message into view.
- Sign-out clears the saved session; a subsequent cold start shows arrival rather than the clinician queue.
- Grounding advances through all five steps and finishes; sound play/stop/finish controls work; journal Save remains reachable above the keyboard.
- Account Back returns to Settings; Help Back returns to its previous conversation.
- Tamil navigation labels fit at 360dp with larger text. English, normal text size and 280dpi were restored for the final Today screen.
- The Docker backend remains healthy. Kotlin source whitespace checks and `git diff --check` passed.

### Artifacts

- [Final Today screen](artifacts/qa/refinement-today-final.png)
- [Compact Tamil navigation](artifacts/qa/refinement-small-tamil-navigation-final.png)
- [Composer with keyboard](artifacts/qa/refinement-care-keyboard.png)
- [Confirmation above composer](artifacts/qa/refinement-care-confirmation-final.png)
- [Journal action above keyboard](artifacts/qa/refinement-journal-keyboard-final.png)
- [Measured control bounds](artifacts/qa/refinement-checks.json)
- [Installable APK](android/app/build/outputs/apk/debug/app-debug.apk)

APK SHA-256: `a1d4ad19925dc840365508ffda8827b33ea4db67054cbe710178acc2a0f4687c`.

This pass exercised UI changes and account/care APIs. It did not rerun the earlier model evaluation suite.

These are emulator and code checks. Physical-hand reach, TalkBack use by a person, and testing with cancer patients have not been performed.
