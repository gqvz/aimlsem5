# Sahay for Android

This native Kotlin/Jetpack Compose app uses the team's Stitch visual language. It now has a calm interactive arrival, Today/check-in/agent plan, Talk, nine working Explore activities, two-way Care messages/files, full-page Settings, patient-approved brief sharing, clinician review and admin bookkeeping. Core conversation, help, primary account/share controls, new activity labels and breathing/grounding guidance support English, Hindi, Bengali and Tamil. Secondary explanations and clinician/admin screens remain English-first. Installed Android speech is available; the backend speech provider adapters are wired for future API access.

## Build and install

Open this `android/` directory in Android Studio, or use the checked-in Gradle wrapper with Android SDK 37 and Build Tools 36.0.0:

```bash
cd android
./gradlew assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The APK is at `android/app/build/outputs/apk/debug/app-debug.apk`. Minimum Android version is API 26 (Android 8). No iPhone build is planned.

## Connect the chat backend

The app defaults to `http://10.0.2.2:8000`, Android Emulator's address for a server on the host computer. In the repository root, run:

```bash
docker compose up -d --build
```

For a physical phone on the same trusted network, start the server with `SAHAY_HOST=0.0.0.0 python3 server.py`. In the app's **Settings**, change **Backend address** to `http://YOUR_COMPUTER_LAN_IP:8000` and tap **Check connection**. The phone and computer must be able to reach each other. This course prototype uses cleartext HTTP for local development; account and share testing must use fictional personas.

If the backend is unavailable, the app shows **On-device demo** and uses fixed, local responses and help routes. AI translation and generated replies run only through the Python backend when `LLM_API_URL` and `LLM_MODEL` are configured. Set `LLM_API_KEY` only when the provider requires it. Keep any model key on the backend; never put it in the APK. The Stitch design key is unrelated to the chat model. Account, share, and clinician review screens need the backend; they do not use an on-device fake account.

## Privacy and limitations

History and question storage are off by default. The user can opt in to local device storage and clear saved content in Settings. A care-team phone number is saved only when explicitly entered. A patient can explicitly share a selected brief and questions with a demo clinician; ordinary chat and audio are not automatically shared. Help buttons open Android's dialer; Sahay cannot place a call or automatically contact a clinician. Android's speech recognition service may process audio according to the installed provider's settings, so use fictional speech for the class demo. The fixed local keyword routes and live-model checks are not validated for clinical use.

After sign-in, the account token is encrypted using Android Keystore and restored on app launch only after the backend verifies it. A clinician returns to the review queue and an admin to the account dashboard. Sign-out removes the saved token, local conversation/questions/journal/number and in-memory care content. Settings opens a full page for account editing, password change, sign-out, language, text/motion/volume preferences, voice selection, local data and connection controls. A password change signs out every session for that account.

The four NCI information cards and India help links use the sources listed in the repository [README](../README.md). Bundled UI strings and fixed responses are generated from the web prototype's translation and backend files; update both surfaces together when changing safety copy.

## Low-memory modern emulator

This PC has `Sahay_Pixel_9`, an API35 Google APIs x86_64 AVD with Pixel 9 proportions. Its render resolution is 720×1616, density 280 (about 411×923dp), two cores and 1536 MB guest RAM. Build with the emulator closed; Gradle is limited to one worker, a 1200 MB heap and an in-process Kotlin compiler.

```bash
# Finish the build before starting the emulator.
./gradlew assembleDebug --offline --no-daemon
systemd-run --user --scope --unit=sahay-emulator \
  -p MemoryHigh=3G -p MemoryMax=3300M -p MemorySwapMax=256M \
  "$ANDROID_HOME/emulator/emulator" -avd Sahay_Pixel_9 \
  -memory 1536 -cores 2 -gpu host -no-snapshot -no-boot-anim
```

The systemd limits protect the desktop if emulator memory grows. On a machine without systemd, run the emulator command directly. Use `adb emu kill` before another full rebuild. AVD configuration is local to `~/.android/avd` and is not bundled in the APK.

## Working flows

- Today: select mood/energy → create plan → launch suggested activity → inspect agent handoffs.
- Explore: breathing/grounding/meditation, three ambient sound modes, matching pairs, gentle guided stretch, drawing/PNG export, private journal and fictional encouragement notes.
- Questions: save/edit questions → ask Visit preparation to draft a brief → edit/preview/share explicitly.
- Care team: sign in → start/open conversation → send text or attach PDF/image/text. Clinicians can mark a message as a file request; patients can attach a reply. Either side can send an unsolicited file.
- Settings: edit account, password/sign-out, language, text/motion/volume, voice API/device selection, saving/clear, help and backend connection.

Sound stops and timers pause when backgrounded. No activity completion is sent to a clinician. **Read this guidance aloud** uses the selected Android or backend voice. Provider mode records M4A and offers transcription review before sending; backend API configuration is described in the repository README. A physical device with a real microphone is still required for speech quality testing.

## Change the AI model

Open **Settings → AI model**, enter the full Chat Completions endpoint and model name, then tap **Save and test model**. These values are saved for this device and used by chat, AI translation, activity planning and visit drafting. They are independent of the backend address and do not sign you out. **Use backend default** restores the backend configuration.

The current backend default is `https://opencode.ai/zen/v1/chat/completions` / `space-bunny-free`. Direct free-tier access currently returns HTTP 403 outside OpenCode; the app displays the provider failure. Providers requiring credentials need a backend `LLM_API_KEY` for that configured endpoint. Real model reachability is tested separately from backend health.
