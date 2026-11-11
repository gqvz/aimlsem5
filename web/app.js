import { languages, t } from './i18n.js';

const root = document.getElementById('app');
const SETTINGS_KEY = 'sahay:settings:v1';
const HISTORY_KEY = 'sahay:history:v1';
const QUESTIONS_KEY = 'sahay:questions:v1';
const PHONE_KEY = 'sahay:phone:v1';
const CARD_FALLBACK = [
  { id: 'feelings', title: 'Emotions and Cancer — National Cancer Institute', summary: 'Cancer can bring up many emotions. A nurse or hospital social worker can help you find emotional and practical support.', url: 'https://www.cancer.gov/about-cancer/coping/feelings' },
  { id: 'relaxation', title: 'Meditation and Relaxation — National Cancer Institute', summary: 'Some people with cancer use relaxation or mindfulness to cope with stress and anxiety.', url: 'https://www.cancer.gov/about-cancer/coping/feelings/relaxation' },
  { id: 'communication', title: 'Communication in Cancer Care — National Cancer Institute', summary: 'Writing down concerns can make it easier to discuss what matters during a care visit.', url: 'https://www.cancer.gov/about-cancer/coping/adjusting-to-cancer/communication-pdq' },
  { id: 'infection', title: 'Infection during Cancer Treatment — National Cancer Institute', summary: 'Contact your health care team if you have signs of infection, including fever.', url: 'https://www.cancer.gov/about-cancer/treatment/side-effects/infection' },
];
const CARD_COPY = {
  hi: {
    feelings: ['कैंसर और भावनाएँ', 'कैंसर के साथ कई तरह की भावनाएँ आ सकती हैं। अस्पताल की नर्स या सामाजिक कार्यकर्ता भावनात्मक और व्यावहारिक सहायता ढूँढ़ने में मदद कर सकते हैं।'],
    relaxation: ['ध्यान और विश्राम', 'कुछ लोग तनाव और चिंता से निपटने के लिए विश्राम या ध्यान का उपयोग करते हैं। जो तरीका सहज लगे वही चुनें।'],
    communication: ['कैंसर देखभाल में बातचीत', 'अपनी चिंताएँ और सवाल लिखकर रखने से मुलाकात में महत्वपूर्ण बातों पर चर्चा करना आसान हो सकता है।'],
    infection: ['कैंसर उपचार के दौरान संक्रमण', 'कुछ कैंसर उपचार संक्रमण का जोखिम बढ़ा सकते हैं। बुखार सहित संक्रमण के लक्षण हों तो अपनी देखभाल टीम से संपर्क करें।'],
  },
  bn: {
    feelings: ['ক্যানসার ও অনুভূতি', 'ক্যানসারের সঙ্গে নানা অনুভূতি আসতে পারে। হাসপাতালের নার্স বা সমাজকর্মী মানসিক ও বাস্তব সহায়তা খুঁজতে সাহায্য করতে পারেন।'],
    relaxation: ['ধ্যান ও বিশ্রাম', 'কেউ কেউ চাপ ও উদ্বেগ সামলাতে বিশ্রাম বা মননচর্চা ব্যবহার করেন। আপনার স্বচ্ছন্দ উপায়টি বেছে নিন।'],
    communication: ['ক্যানসার চিকিৎসায় কথা বলা', 'উদ্বেগ ও প্রশ্ন লিখে রাখলে চিকিৎসা সাক্ষাতে গুরুত্বপূর্ণ কথা বলা সহজ হতে পারে।'],
    infection: ['ক্যানসার চিকিৎসার সময় সংক্রমণ', 'কিছু ক্যানসার চিকিৎসায় সংক্রমণের ঝুঁকি বাড়তে পারে। জ্বরসহ সংক্রমণের লক্ষণ থাকলে চিকিৎসা দলের সঙ্গে যোগাযোগ করুন।'],
  },
  ta: {
    feelings: ['புற்றுநோயும் உணர்ச்சிகளும்', 'புற்றுநோயுடன் பல உணர்ச்சிகள் வரலாம். மருத்துவமனை செவிலியர் அல்லது சமூகப் பணியாளர் உணர்ச்சி மற்றும் நடைமுறை ஆதரவைக் கண்டுபிடிக்க உதவலாம்.'],
    relaxation: ['தியானமும் ஓய்வும்', 'சிலர் மன அழுத்தத்தையும் கவலையையும் சமாளிக்க ஓய்வு அல்லது மனக்குவிப்பைப் பயன்படுத்துகிறார்கள். உங்களுக்கு ஏற்றதைத் தேர்ந்தெடுக்கவும்.'],
    communication: ['புற்றுநோய் சிகிச்சையில் உரையாடல்', 'கவலைகளையும் கேள்விகளையும் எழுதிவைத்தால் சிகிச்சைச் சந்திப்பில் முக்கியமானவற்றைப் பேச எளிதாகலாம்.'],
    infection: ['புற்றுநோய் சிகிச்சையின்போது தொற்று', 'சில புற்றுநோய் சிகிச்சைகள் தொற்று அபாயத்தை அதிகரிக்கலாம். காய்ச்சல் உள்ளிட்ட தொற்று அறிகுறிகள் இருந்தால் சிகிச்சைக் குழுவைத் தொடர்புகொள்ளுங்கள்.'],
  },
};
function cardCopy(card) { return CARD_COPY[state.lang]?.[card.id] || [card.title.replace(' — National Cancer Institute', ''), card.summary]; }

function readJSON(key, fallback) { try { return JSON.parse(localStorage.getItem(key)) ?? fallback; } catch { return fallback; } }
const saved = readJSON(SETTINGS_KEY, {});
const state = {
  lang: languages.some(x => x.code === saved.lang) ? saved.lang : 'en',
  onboarded: Boolean(saved.onboarded),
  audio: Boolean(saved.audio),
  persist: Boolean(saved.persist),
  view: saved.onboarded ? 'chat' : 'welcome',
  messages: saved.persist ? readJSON(HISTORY_KEY, []) : [],
  questions: saved.persist ? readJSON(QUESTIONS_KEY, []) : [],
  phone: localStorage.getItem(PHONE_KEY) || '',
  topic: '', transcript: '', recording: false, transcriptOpen: false,
  hidden: false, pending: false, mode: 'demo', online: navigator.onLine,
  toast: '', lastRequest: null, lastUrgentText: '', cards: CARD_FALLBACK,
  exerciseRemaining: 120, exerciseRunning: false,
  bedtimeDismissed: '', wasPastBedtime: null, ayurFocus: 'all',
};
let toastTimer;
let exerciseTimer;
let recognition;

function e(value) { return String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char])); }
function icon(name, extra = '') { return `<svg class="${extra}" aria-hidden="true"><use href="#i-${name}"></use></svg>`; }
function local(key) { return t(state.lang, key); }
function persistSettings() { localStorage.setItem(SETTINGS_KEY, JSON.stringify({ lang: state.lang, onboarded: state.onboarded, audio: state.audio, persist: state.persist })); }
function persistContent() {
  if (state.persist) {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(state.messages.slice(-30)));
    localStorage.setItem(QUESTIONS_KEY, JSON.stringify(state.questions));
  } else {
    localStorage.removeItem(HISTORY_KEY);
    localStorage.removeItem(QUESTIONS_KEY);
  }
}
function toast(message) {
  state.toast = message;
  clearTimeout(toastTimer);
  render();
  toastTimer = setTimeout(() => { state.toast = ''; render(); }, 4500);
}
function changeView(view) {
  if (state.view === 'exercise' && view !== 'exercise') stopExercise();
  state.view = view;
  state.transcriptOpen = false;
  render();
  window.scrollTo({ top: 0, behavior: 'instant' });
}
function header() {
  const title = { chat: local('chat'), paths: local('paths'), exercise: local('copingTitle'), info: local('info'), questions: local('questions'), help: local('urgentHelp'), ayurveda: 'Ayurveda' }[state.view] || '';
  return `<header class="topbar"><div class="topbar-inner"><div class="topbar-main">
    <a class="brand" href="#chat" data-action="nav" data-view="chat"><img src="/assets/sahay-emblem.png" alt="" /><span><span class="brand-name">Sahay</span><span class="brand-sub">${e(local('subtitle'))}</span></span></a>
    <div class="header-actions"><button class="help-button" type="button" data-action="nav" data-view="help">${icon('help')}<span>${e(local('help'))}</span></button>
    <button class="icon-button" type="button" data-action="audio" aria-label="${e(state.audio ? local('stopAudio') : local('listen'))}" aria-pressed="${state.audio}">${icon(state.audio ? 'volume' : 'volume-off')}</button>
    <button class="icon-button" type="button" data-action="hide" aria-label="${e(local('quickHide'))}">${icon('eye-off')}</button></div></div>
    <div class="topbar-sub"><label class="language-chip">${icon('globe')}<span class="sr-only">${e(local('languageTitle'))}</span><select id="header-lang" aria-label="${e(local('languageTitle'))}">${languages.map(x => `<option value="${x.code}" ${state.lang === x.code ? 'selected' : ''}>${e(x.label)}</option>`).join('')}</select></label><span class="current-view-label">${e(title)}</span></div></div></header>`;
}
function nav() {
  const items = [['chat', 'message', 'chat'], ['paths', 'heart', 'paths'], ['info', 'book', 'info'], ['questions', 'list', 'questions'], ['help', 'help', 'help']];
  return `<nav class="bottom-nav" aria-label="Main navigation"><div class="nav-inner">${items.map(([view, image, label]) => `<button type="button" class="nav-button ${view === 'help' ? 'help-nav' : ''}" data-action="nav" data-view="${view}" ${state.view === view ? 'aria-current="page"' : ''}>${icon(image)}<span>${e(local(label))}</span></button>`).join('')}</div></nav>`;
}
function welcome() {
  const topics = [['scan', 'topicScan'], ['visit', 'topicVisit'], ['travel', 'topicTravel'], ['family', 'topicFamily']];
  return `<main class="content welcome-content" id="main"><div class="intro-center"><div class="brand-badge"><img src="/assets/sahay-emblem.png" alt="" />${e(local('aiLabel'))}</div><h1>${e(local('welcomeTitle'))}</h1><p>${e(local('welcomeLead'))}</p></div>
    <div class="hero-image"><img src="/assets/courtyard.jpg" alt="A quiet sunlit courtyard" /><span class="hero-caption">Sahay · सहाय · সহায় · சகாய்</span></div>
    <section class="soft-panel section-gap" aria-labelledby="boundary-title"><div class="title-row"><span class="small-icon">${icon('shield')}</span><h2 id="boundary-title">${e(local('boundaryTitle'))}</h2></div>
    <div class="notice">${icon('help')}<p>${e(local('boundary'))}</p></div><div class="notice warning">${icon('help')}<p>${e(local('urgentNote'))}</p></div></section>
    <section class="section-gap" aria-labelledby="language-title"><div class="section-header"><h2 id="language-title">${e(local('languageTitle'))}</h2></div><p class="section-help">${e(local('languageLead'))}</p><div class="lang-grid">${languages.map(x => `<button type="button" class="lang-option ${state.lang === x.code ? 'selected' : ''}" data-action="language" data-lang="${x.code}" aria-pressed="${state.lang === x.code}"><strong>${e(x.label)}</strong><span>${e(x.detail)}</span></button>`).join('')}</div></section>
    <section class="panel section-gap" aria-labelledby="privacy-title"><div class="title-row"><span class="small-icon">${icon('volume')}</span><h2 id="privacy-title">${e(local('privacyTitle'))}</h2></div><p class="section-help">${e(local('privacyLead'))}</p>
    <div class="toggle-row"><span><strong>${e(local('audioLabel'))}</strong><small>${e(local('audioHint'))}</small></span><button class="switch" type="button" role="switch" aria-label="${e(local('audioLabel'))}" aria-checked="${state.audio}" data-action="audio"></button></div>
    <div class="toggle-row"><span><strong>${e(local('saveLabel'))}</strong><small>${e(local('saveHint'))}</small></span><button class="switch" type="button" role="switch" aria-label="${e(local('saveLabel'))}" aria-checked="${state.persist}" data-action="persist"></button></div><div class="privacy-tip">${e(local('hideHint'))}</div></section>
    <section class="section-gap" aria-labelledby="topic-title"><div class="section-header"><h2 id="topic-title">${e(local('topicTitle'))}</h2></div><p class="section-help">${e(local('topicLead'))}</p><div class="topic-chips">${topics.map(([value, key]) => `<button class="topic-chip ${state.topic === value ? 'selected' : ''}" type="button" data-action="topic" data-topic="${value}" aria-pressed="${state.topic === value}">${e(local(key))}</button>`).join('')}</div></section>
    <button class="primary-button full" type="button" data-action="begin">${e(local('start'))} ${icon('arrow-right')}</button><p class="fine-print">${e(local('acknowledge'))}</p></main>`;
}
function statusBand() {
  const cls = !state.online ? 'offline' : '';
  const text = !state.online ? local('statusOffline') : (state.mode === 'live' ? local('statusLive') : local('statusDemo'));
  return `<div class="status-band ${cls}" role="status"><span class="status-dot"></span>${e(text)}</div>`;
}
function formatTime() { return new Intl.DateTimeFormat(state.lang === 'en' ? 'en-IN' : state.lang + '-IN', { hour: 'numeric', minute: '2-digit' }).format(new Date()); }
function messageView(msg, index) {
  const source = msg.card && msg.card.url ? `<a class="source-link" href="${e(msg.card.url)}" target="_blank" rel="noopener noreferrer">${icon('book')}<span>${e(msg.card.title)}</span></a>` : '';
  return `<article class="message ${msg.role === 'user' ? 'user' : 'ai'}"><div class="message-meta">${msg.role === 'user' ? e(local('chat')) : 'Sahay · AI'} · ${e(msg.time || '')}</div><div class="bubble">${e(msg.text)}${source}</div>${msg.role === 'ai' ? `<div class="bubble-actions"><button class="text-button" type="button" data-action="speak" data-index="${index}">${icon('play')}${e(local('listen'))}</button>${msg.route === 'crisis' || msg.route === 'urgent_medical' ? `<button class="text-button" type="button" data-action="nav" data-view="help">${e(local('urgentHelp'))}</button>` : ''}</div>` : ''}</article>`;
}
function chat() {
  const msgs = state.messages.length ? state.messages : [{ role: 'ai', text: local('intro'), time: '', route: 'ordinary' }];
  return `<main class="content" id="main">${bedtimeCard()}${statusBand()}<div class="ai-banner"><span class="small-icon">${icon('shield')}</span><div><strong>${e(local('aiLabel'))}</strong><p>${e(local('boundary'))}</p></div></div>
    <div class="chat-stream" aria-live="polite"><div class="date-pill">${e(new Intl.DateTimeFormat(state.lang + '-IN', { day: 'numeric', month: 'short' }).format(new Date()))}</div>${msgs.map(messageView).join('')}${state.pending ? `<div class="typing" role="status">${e(local('thinking'))}</div>` : ''}</div>
    ${state.lastRequest && !state.pending ? `<div class="section-actions"><button class="quiet-button" type="button" data-action="retry">${icon('refresh')}${e(local('retry'))}</button></div>` : ''}</main>
    <div class="composer-wrap"><form id="chat-form" class="composer"><label for="chat-input" class="sr-only">${e(local('chat'))}</label><textarea id="chat-input" name="text" rows="1" maxlength="4000" placeholder="${e(local('typePlaceholder'))}" ${state.pending ? 'disabled' : ''}>${e(state.topic ? local('topic' + state.topic[0].toUpperCase() + state.topic.slice(1)) : '')}</textarea><button type="button" class="mic-button" data-action="record" aria-label="${e(local('mic'))}" ${state.pending ? 'disabled' : ''}>${icon('mic')}</button><button type="submit" class="send-button" aria-label="${e(local('send'))}" ${state.pending ? 'disabled' : ''}>${icon('send')}</button></form><div class="composer-hint">${e(local('transcriptHint'))}</div></div>`;
}
function paths() {
  const options = [
    ['reflect', 'message', 'reflectTitle', 'reflectBody'], ['coping', 'wind', 'copingTitle', 'copingBody'],
    ['info', 'book', 'infoTitle', 'infoBody'], ['questions', 'list', 'questionsTitle', 'questionsBody'],
  ];
  return `<main class="content" id="main">${bedtimeCard()}${statusBand()}<div class="card path-intro"><span class="eyebrow">${e(local('aiLabel'))}</span><h1>${e(local('pathsTitle'))}</h1><p>${e(local('pathsLead'))}</p></div><div class="path-grid">${options.map(([action, image, title, body]) => `<button class="path-card" type="button" data-action="path" data-path="${action}"><span class="small-icon">${icon(image)}</span><span class="path-copy"><strong>${e(local(title))}</strong><span>${e(local(body))}</span></span>${icon('chevron')}</button>`).join('')}</div>${ayurTip()}<div class="soft-panel path-footer">${icon('shield')} ${e(local('boundary'))}</div></main>`;
}
function exercise() {
  const elapsed = 120 - state.exerciseRemaining;
  const step = Math.min(3, Math.floor(elapsed / 30));
  const mins = Math.floor(state.exerciseRemaining / 60);
  const secs = String(state.exerciseRemaining % 60).padStart(2, '0');
  return `<main class="content" id="main"><div class="exercise-header"><span class="language-chip">${icon('wind')}${e(local('copingTitle'))}</span><button type="button" class="help-button" data-action="exercise-stop">${icon('x')}${e(local('exerciseStop'))}</button></div>
    <h1 style="margin-top:21px">${e(local('exerciseTitle'))}</h1><div class="soft-panel section-gap">${e(local('exerciseHint'))}</div>
    <div class="exercise-stage"><div class="breath-ring"><div class="breath-ring-inner">${icon('wind')}<span>${mins}:${secs}</span></div></div><h2 aria-live="polite">${e(local('exerciseSteps')[step])}</h2></div>
    <div class="flex-between"><span class="mini-label">${step + 1} / 4</span><strong>${mins}:${secs}</strong></div><div class="progress-track" aria-label="Exercise progress"><span style="width:${elapsed / 120 * 100}%"></span></div>
    <div class="exercise-controls" style="margin-top:19px"><button type="button" class="primary-button" data-action="exercise-toggle">${icon(state.exerciseRunning ? 'pause' : 'play')}${e(local(state.exerciseRunning ? 'exercisePause' : elapsed ? 'exerciseResume' : 'exerciseStart'))}</button><button type="button" class="secondary-button" data-action="exercise-stop">${e(local('exerciseDone'))}</button></div>
    <div class="notice warning" style="margin-top:20px">${icon('help')}<p>${e(local('exerciseCaution'))} <button class="text-button" type="button" data-action="nav" data-view="help">${e(local('urgentHelp'))}</button></p></div></main>`;
}
function info() {
  return `<main class="content" id="main"><span class="eyebrow">${e(local('infoTitle'))}</span><h1>${e(local('infoHeading'))}</h1><p class="section-help">${e(local('infoLead'))}</p><div class="card-list">${state.cards.map(card => `<article class="card info-card"><span class="mini-label">National Cancer Institute</span><h3>${e(cardCopy(card)[0])}</h3><p>${e(cardCopy(card)[1])}</p><div class="info-card-footer"><a href="${e(card.url)}" target="_blank" rel="noopener noreferrer">${icon('book')}${e(local('source'))}</a><button class="text-button" type="button" data-action="ask-card" data-card="${e(card.id)}">${e(local('askAbout'))}${icon('arrow-right')}</button></div></article>`).join('')}</div><div class="notice warning">${icon('help')}<p>${e(local('helpMedical'))}</p></div></main>`;
}
function questions() {
  return `<main class="content" id="main"><span class="eyebrow">${e(local('questionsTitle'))}</span><h1>${e(local('questionsHeading'))}</h1><p class="section-help">${e(local('questionsLead'))}</p>
    <div class="question-list">${state.questions.length ? state.questions.map((q, index) => `<div class="question-row ${q.done ? 'done' : ''}"><button class="check-button" type="button" data-action="toggle-question" data-index="${index}" aria-label="${e(q.done ? 'Mark as not discussed' : 'Mark as discussed')}">${icon('check')}</button><span class="question-text">${e(q.text)}</span><span class="question-actions"><button type="button" data-action="edit-question" data-index="${index}" aria-label="${e(local('edit'))}">${icon('pencil')}</button><button type="button" data-action="delete-question" data-index="${index}" aria-label="${e(local('delete'))}">${icon('trash')}</button></span></div>`).join('') : `<div class="empty-state">${icon('list')}<p>${e(local('emptyQuestions'))}</p></div>`}</div>
    <form id="question-form" class="question-form"><label for="question-input" class="sr-only">${e(local('questionPlaceholder'))}</label><input id="question-input" maxlength="300" placeholder="${e(local('questionPlaceholder'))}" required /><button type="submit" class="primary-button">${icon('plus')}${e(local('addQuestion'))}</button></form>
    <div class="divider"></div><span class="mini-label">${e(local('questionsTitle'))}</span><div class="topic-chips" style="margin-top:8px">${local('suggestedQuestions').map((text,index) => `<button type="button" class="topic-chip" data-action="suggest-question" data-index="${index}">+ ${e(text)}</button>`).join('')}</div>
    <div class="section-actions"><button type="button" class="quiet-button" data-action="copy-questions" ${state.questions.length ? '' : 'disabled'}>${icon('download')}${e(local('copyQuestions'))}</button><button type="button" class="quiet-button" data-action="clear">${icon('trash')}${e(local('clear'))}</button></div></main>`;
}
function help() {
  const phone = state.phone.replace(/[^0-9+]/g, '');
  return `<main class="content" id="main"><span class="eyebrow">India · Human help</span><h1>${e(local('helpTitle'))}</h1><div class="help-hero"><p>${e(state.lastUrgentText || local('helpLead'))}</p></div>
    <div class="card help-card emergency-card"><div class="title-row"><span class="small-icon warm">${icon('phone')}</span><h3>${e(local('emergency'))}</h3></div><p>${e(local('emergencyHint'))}</p><a class="phone-link" href="tel:112">${icon('phone')}${e(local('call'))} 112</a></div>
    <div class="card help-card"><div class="title-row"><span class="small-icon">${icon('heart')}</span><h3>${e(local('mentalHealth'))}</h3></div><p>${e(local('mentalHealthHint'))}</p><a class="phone-link" href="tel:14416">${icon('phone')}${e(local('call'))} 14416</a></div>
    <div class="card help-card"><div class="title-row"><span class="small-icon">${icon('phone')}</span><h3>${e(local('careTeam'))}</h3></div><p>${e(local('careTeamHint'))}</p>${phone ? `<a class="phone-link" href="tel:${e(phone)}">${icon('phone')}${e(local('call'))} ${e(state.phone)}</a>` : ''}
    <form id="phone-form" class="phone-form" style="margin-top:11px"><label for="phone-input" class="sr-only">${e(local('phonePlaceholder'))}</label><input id="phone-input" inputmode="tel" autocomplete="tel" placeholder="${e(local('phonePlaceholder'))}" value="${e(state.phone)}" /><button type="submit" class="secondary-button">${e(local('saveNumber'))}</button></form></div>
    <div class="soft-panel resource-note">${e(local('helpMedical'))}</div><div class="section-actions"><button class="quiet-button" type="button" data-action="nav" data-view="chat">${icon('arrow-left')}${e(local('returnChat'))}</button></div>
    <p class="resource-note">Sources: <a href="https://112.gov.in/" target="_blank" rel="noopener noreferrer">Emergency Response Support System</a> · <a href="https://dghs.mohfw.gov.in/national-mental-health-programme.php" target="_blank" rel="noopener noreferrer">Tele-MANAS</a></p></main>`;
}
function transcriptDrawer() {
  if (!state.transcriptOpen) return '';
  return `<div class="transcript-backdrop" data-action="close-transcript"></div><section class="transcript-drawer" role="dialog" aria-modal="true" aria-labelledby="transcript-title"><div class="flex-between"><h2 id="transcript-title">${e(local('transcriptTitle'))}</h2><button class="icon-button" data-action="close-transcript" aria-label="${e(local('cancel'))}" type="button">${icon('x')}</button></div><p>${e(local('transcriptHint'))}</p><label for="transcript-text" class="sr-only">${e(local('transcriptTitle'))}</label><textarea id="transcript-text" maxlength="4000">${e(state.transcript)}</textarea><div class="drawer-actions"><button type="button" class="quiet-button" data-action="record-again">${icon('mic')}${e(local('recordAgain'))}</button><button type="button" class="primary-button" data-action="send-transcript">${e(local('sendTranscript'))}${icon('send')}</button></div></section>`;
}
function privacyVeil() {
  if (!state.hidden) return '';
  return `<div class="privacy-veil" role="dialog" aria-modal="true" aria-labelledby="veil-title"><img src="/assets/sahay-emblem.png" alt="" /><h1 id="veil-title">${e(local('privacyCover'))}</h1><p>${e(local('privacyCoverHint'))}</p><button type="button" class="primary-button" data-action="unhide">${icon('eye')}${e(local('showAgain'))}</button></div>`;
}
// ---- Bedtime reminder: from 10 pm to 5 am the app gently suggests sleep (dismissible per night). ----
const BEDTIME_HOUR = 22, MORNING_HOUR = 5;
const BEDTIME_COPY = {
  en: ['Time to rest', "It's past 10 pm. Your body heals while you sleep, so try to rest soon.", 'Ayurveda sees the hours before 10 pm as the calmest time to fall asleep. A few warm drops of oil rubbed into the soles of your feet can help you settle.', 'Try a slow breath', 'Not tonight'],
  hi: ['आराम का समय', 'रात के 10 बज चुके हैं। नींद में आपका शरीर ठीक होता है, इसलिए जल्द आराम करने की कोशिश करें।', 'आयुर्वेद के अनुसार रात 10 बजे से पहले का समय सोने के लिए सबसे शांत होता है। तलवों पर गुनगुने तेल की कुछ बूँदें मलने से मन शांत हो सकता है।', 'धीमी साँस लें', 'आज रात नहीं'],
  bn: ['বিশ্রামের সময়', 'রাত ১০টা পেরিয়ে গেছে। ঘুমের মধ্যে শরীর সেরে ওঠে, তাই শিগগির বিশ্রাম নেওয়ার চেষ্টা করুন।', 'আয়ুর্বেদ মতে রাত ১০টার আগের সময় ঘুমানোর জন্য সবচেয়ে শান্ত। পায়ের তলায় কয়েক ফোঁটা উষ্ণ তেল মালিশ করলে মন স্থির হতে পারে।', 'ধীরে শ্বাস নিন', 'আজ রাতে নয়'],
  ta: ['ஓய்வெடுக்கும் நேரம்', 'இரவு 10 மணி கடந்துவிட்டது. தூக்கத்தில் உங்கள் உடல் குணமடைகிறது, எனவே விரைவில் ஓய்வெடுக்க முயலுங்கள்.', 'ஆயுர்வேதத்தின்படி இரவு 10 மணிக்கு முன் தூங்குவது மிக அமைதியானது. பாதங்களில் சில துளி வெதுவெதுப்பான எண்ணெய் தேய்ப்பது மனதை அமைதிப்படுத்தும்.', 'மெதுவாக மூச்சு விடுங்கள்', 'இன்றிரவு வேண்டாம்'],
};
function isPastBedtime(now = new Date()) { return now.getHours() >= BEDTIME_HOUR || now.getHours() < MORNING_HOUR; }
function bedtimeNight(now = new Date()) { const d = new Date(now); if (d.getHours() < MORNING_HOUR) d.setDate(d.getDate() - 1); return d.toDateString(); }
function bedtimeCard() {
  if (!isPastBedtime() || state.bedtimeDismissed === bedtimeNight()) return '';
  const [title, body, tip, start, later] = BEDTIME_COPY[state.lang] || BEDTIME_COPY.en;
  return `<section class="bedtime" role="status" aria-labelledby="bedtime-title"><span class="bedtime-time">${icon('moon')}${e(formatTime())}</span><h2 id="bedtime-title">${e(title)}</h2><p>${e(body)}</p><p class="bedtime-tip">${e(tip)}</p>
    <div class="bedtime-actions"><button class="primary-button" type="button" data-action="path" data-path="coping">${e(start)}</button><button class="text-button" type="button" data-action="bedtime-dismiss">${e(later)}</button></div></section>`;
}
// Re-render at 10 pm (and at 5 am) if the app is left open.
setInterval(() => { const past = isPastBedtime(); if (past !== state.wasPastBedtime) { state.wasPastBedtime = past; if (state.view !== 'welcome' && !state.hidden) render(); } }, 30000);

// ---- Ayurvedic self-care: daily practices and herbs, for mind and body. No doses on purpose. ----
const AYUR_SAFETY = 'Herbs and supplements can change how chemotherapy, radiotherapy, hormone therapy and other medicines work. Please ask your oncologist or pharmacist before taking any herb as a tea, powder or capsule. These are traditional ideas for comfort, not treatment.';
const AYUR_PRACTICES = [
  { name: 'A steady daily rhythm', local: 'Dinacharya', focus: 'both', how: 'Wake, eat and sleep at roughly the same times each day. A predictable rhythm is the foundation of Ayurvedic self-care and helps both energy and mood.' },
  { name: 'Alternate-nostril breathing', local: 'Nadi Shodhana', focus: 'mind', how: 'Sit comfortably. Close the right nostril with your thumb and breathe in through the left; switch and breathe out through the right. Continue slowly for a few rounds, with no breath holds.', care: 'Stop if you feel light-headed or short of breath.' },
  { name: 'Humming bee breath', local: 'Bhramari', focus: 'mind', how: 'Breathe in gently, then hum softly like a bee as you breathe out. Let the vibration settle your face and chest. Five or six rounds are enough.' },
  { name: 'Warm oil self-massage', local: 'Abhyanga', focus: 'body', how: 'Warm a little sesame or coconut oil and massage it slowly into your arms, legs and shoulders before a warm bath. It is a grounding, nurturing ritual.', care: 'Skip skin being treated with radiotherapy, broken skin, and areas around ports or lines. Ask your care team if unsure.' },
  { name: 'Foot massage before bed', local: 'Padabhyanga', focus: 'both', how: 'Rub a few drops of warm oil into the soles of your feet at bedtime. Many people find this helps the mind settle for sleep. Wear socks afterwards to avoid slipping.' },
  { name: 'Warm, simple meals', local: 'Ahara', focus: 'body', how: 'Favour freshly cooked, warm, easy-to-digest food such as khichdi, soups and dal. Make lunch your main meal and keep dinner light and early.', care: 'Follow any diet your oncology team or dietitian has given you.' },
  { name: 'Sip warm water', local: 'Ushnodaka', focus: 'body', how: 'Sip warm or room-temperature water through the day rather than very cold drinks. Ayurveda considers this gentle on digestion.' },
  { name: 'A calm evening routine', local: 'Ratricharya', focus: 'mind', how: 'Dim lights and put screens away an hour before bed. Read, listen to soft sound, or sit quietly, and aim to be asleep before 10 pm.' },
  { name: 'Gentle restorative yoga', local: 'Yoga', focus: 'both', how: "Child's pose (Balasana) or resting with your legs up a wall (Viparita Karani) for a few minutes can ease tension. Use cushions and move only as far as is comfortable.", care: 'Check with your care team before new movement if you have bone pain, low blood counts or recent surgery.' },
];
const AYUR_HERBS = [
  { name: 'Tulsi', local: 'Holy basil', focus: 'both', use: 'Drunk as a warm tea to support calm and resilience to stress, and for a soothing throat.', caution: 'In concentrated forms it may affect blood clotting and blood sugar.' },
  { name: 'Ashwagandha', local: 'Withania somnifera', focus: 'mind', use: 'A rasayana (rejuvenating) herb traditionally used for stress, restful sleep and strength. Some studies suggest it may lower stress.', caution: 'Can interact with thyroid, sedative, diabetes, blood pressure and immune-suppressing medicines. Rare liver problems have been reported. Avoid in pregnancy.' },
  { name: 'Brahmi', local: 'Bacopa monnieri', focus: 'mind', use: 'Traditionally used to support memory, focus and a quieter mind.', caution: 'May upset the stomach and can add to the effect of sedatives.' },
  { name: 'Jatamansi', local: 'Spikenard', focus: 'mind', use: 'Traditionally used to calm an unsettled mind and encourage sleep.', caution: 'Evidence is limited. It may add to the effect of sleeping tablets or sedatives.' },
  { name: 'Haldi', local: 'Turmeric', focus: 'body', use: 'Used in everyday cooking and warm haldi doodh (golden milk) for its soothing, anti-inflammatory reputation.', caution: 'Cooking amounts are generally considered safe. Supplements may interact with blood thinners and some chemotherapy drugs.' },
  { name: 'Shunthi', local: 'Dried ginger', focus: 'body', use: 'Used in tea or food to ease nausea and support digestion. Some studies suggest ginger may help treatment-related nausea.', caution: 'Larger amounts may increase bleeding risk with blood thinners or low platelets.' },
  { name: 'Amla', local: 'Indian gooseberry', focus: 'body', use: 'A vitamin-C-rich fruit valued as a rasayana for general vitality and digestion.', caution: 'Concentrated extracts may affect blood sugar and clotting.' },
  { name: 'Triphala', local: 'Three fruits', focus: 'body', use: 'A traditional blend used for gentle digestion and bowel regularity.', caution: 'It can loosen stools. Avoid if you have diarrhoea.' },
];
const FOCUS_LABEL = { mind: 'For the mind', body: 'For the body', both: 'Mind and body' };
function dayOfYear() { const now = new Date(); return Math.floor((now - new Date(now.getFullYear(), 0, 0)) / 86400000); }
function ayurTip() {
  const tip = AYUR_PRACTICES[dayOfYear() % AYUR_PRACTICES.length];
  return `<section class="ayur-tip" aria-labelledby="ayur-tip-title"><span class="eyebrow">${icon('leaf')} From Ayurveda today</span><h2 id="ayur-tip-title">${e(tip.name)}</h2><span class="ayur-meta">${e(tip.local)} · ${e(FOCUS_LABEL[tip.focus])}</span><p>${e(tip.how)}</p>${tip.care ? `<p class="ayur-meta">${e(tip.care)}</p>` : ''}
    <div><button class="text-button" type="button" data-action="nav" data-view="ayurveda">More herbs and practices ${icon('chevron')}</button></div></section>`;
}
function ayurItem(title, local, focus, body, care, warn) {
  return `<details><summary><span><strong>${e(title)}</strong><small>${e(local)} · ${e(FOCUS_LABEL[focus])}</small></span>${icon('chevron')}</summary><p>${e(body)}</p>${care ? `<div class="ayur-care ${warn ? 'warn' : ''}">${e(care)}</div>` : ''}</details>`;
}
function ayurveda() {
  const f = state.ayurFocus;
  const keep = x => f === 'all' || x.focus === 'both' || x.focus === f;
  return `<main class="content" id="main"><div class="card path-intro"><span class="eyebrow">${icon('leaf')} Ayurveda</span><h1>Ayurvedic self-care</h1><p>Ayurveda, India's traditional system of wellbeing, focuses on daily rhythm, warmth and balance. Choose anything that feels comfortable today.</p></div>
    <div class="ayur-safety">${icon('help')}<p>${e(AYUR_SAFETY)}</p></div>
    <div class="ayur-filter" role="group" aria-label="Filter">${[['all', 'All'], ['mind', 'Mind'], ['body', 'Body']].map(([id, label]) => `<button type="button" class="topic-chip ${f === id ? 'selected' : ''}" data-action="ayur-focus" data-focus="${id}" aria-pressed="${f === id}">${label}</button>`).join('')}</div>
    <h2>Daily practices</h2><div class="ayur-list">${AYUR_PRACTICES.filter(keep).map(p => ayurItem(p.name, p.local, p.focus, p.how, p.care, false)).join('')}</div>
    <h2>Herbs traditionally used</h2><p class="section-help">Open a herb to read about it. No doses are given on purpose.</p><div class="ayur-list">${AYUR_HERBS.filter(keep).map(h => ayurItem(h.name, h.local, h.focus, h.use, 'Check first: ' + h.caution, true)).join('')}</div>
    <button class="primary-button full" type="button" data-action="path" data-path="coping">Try a calming breath ${icon('arrow-right')}</button></main>`;
}
function render() {
  document.documentElement.lang = state.lang;
  document.title = `Sahay · ${local('welcomeTitle')}`;
  root.innerHTML = `<div class="app-shell ${state.view === 'chat' ? 'chat-view' : ''} ${state.view === 'welcome' ? 'welcome-view' : ''}">${state.view === 'welcome' ? '' : header()}${({ welcome, chat, paths, exercise, info, questions, help, ayurveda })[state.view]()}${state.view === 'welcome' ? '' : nav()}</div>${transcriptDrawer()}${privacyVeil()}${state.toast ? `<div class="toast" role="status">${e(state.toast)}</div>` : ''}`;
}

async function refreshStatus() {
  try {
    const response = await fetch('/api/health', { cache: 'no-store' });
    const data = await response.json();
    state.mode = data.mode === 'live' ? 'live' : 'demo';
    const cardsResponse = await fetch('/api/cards', { cache: 'no-store' });
    const cards = await cardsResponse.json();
    if (Array.isArray(cards.cards)) state.cards = cards.cards;
    render();
  } catch { state.mode = 'demo'; render(); }
}
function stopSpeaking() { if ('speechSynthesis' in window) window.speechSynthesis.cancel(); }
function speak(text) {
  if (!('speechSynthesis' in window)) { toast(local('noSpeech')); return; }
  stopSpeaking();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = languages.find(x => x.code === state.lang)?.speech || 'en-IN';
  utterance.rate = .92;
  const voices = speechSynthesis.getVoices();
  const target = voices.find(x => x.lang.toLowerCase() === utterance.lang.toLowerCase()) || voices.find(x => x.lang.toLowerCase().startsWith(state.lang));
  if (target) utterance.voice = target;
  speechSynthesis.speak(utterance);
}
function startRecording() {
  const Engine = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!Engine) { toast(local('noSpeech')); return; }
  if (state.recording && recognition) { recognition.stop(); return; }
  recognition = new Engine();
  recognition.lang = languages.find(x => x.code === state.lang)?.speech || 'en-IN';
  recognition.interimResults = false;
  recognition.continuous = false;
  recognition.onresult = event => { state.transcript = Array.from(event.results).map(x => x[0].transcript).join(' '); state.transcriptOpen = true; state.recording = false; render(); document.getElementById('transcript-text')?.focus(); };
  recognition.onerror = () => { state.recording = false; toast(local('speechError')); };
  recognition.onend = () => { state.recording = false; render(); };
  try { recognition.start(); state.recording = true; toast(local('listening')); } catch { toast(local('speechError')); }
}
async function sendMessage(text, requestedAction) {
  const clean = text.trim();
  if (!clean || state.pending) return;
  state.topic = '';
  const request = { text: clean, language: state.lang, action: requestedAction || null, history: state.messages.slice(-4).map(x => x.text) };
  state.lastRequest = null;
  state.messages.push({ role: 'user', text: clean, time: formatTime() });
  state.pending = true; state.view = 'chat';
  persistContent(); render();
  window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
  try {
    const response = await fetch('/api/message', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) });
    const data = await response.json();
    if (!response.ok) throw Error(data.error || 'Request failed');
    state.messages.push({ role: 'ai', text: data.text, time: formatTime(), route: data.route, card: data.card });
    if (data.route === 'crisis' || data.route === 'urgent_medical') { state.lastUrgentText = data.text; state.view = 'help'; stopSpeaking(); }
    else if (state.audio) speak(data.text);
    persistContent();
  } catch {
    state.lastRequest = request;
    toast(local('replyError'));
  } finally {
    state.pending = false; render();
    if (state.view === 'chat') window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
  }
}
async function retryMessage() {
  if (!state.lastRequest || state.pending) return;
  const request = state.lastRequest; state.pending = true; state.lastRequest = null; render();
  try {
    const response = await fetch('/api/message', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) });
    const data = await response.json();
    if (!response.ok) throw Error(data.error || 'Request failed');
    state.messages.push({ role: 'ai', text: data.text, time: formatTime(), route: data.route, card: data.card });
    if (data.route === 'crisis' || data.route === 'urgent_medical') { state.lastUrgentText = data.text; state.view = 'help'; }
    else if (state.audio) speak(data.text);
    persistContent();
  } catch { state.lastRequest = request; toast(local('replyError')); }
  finally { state.pending = false; render(); }
}
function stopExercise() { clearInterval(exerciseTimer); exerciseTimer = null; state.exerciseRunning = false; }
function toggleExercise() {
  if (state.exerciseRemaining <= 0) state.exerciseRemaining = 120;
  if (state.exerciseRunning) { stopExercise(); render(); return; }
  state.exerciseRunning = true;
  exerciseTimer = setInterval(() => {
    state.exerciseRemaining = Math.max(0, state.exerciseRemaining - 1);
    if (state.exerciseRemaining === 0) stopExercise();
    if (state.view === 'exercise') render();
  }, 1000);
  render();
}
function clearAll() {
  if (!window.confirm(local('clearConfirm'))) return;
  stopSpeaking(); stopExercise();
  state.messages = []; state.questions = []; state.lastRequest = null; state.lastUrgentText = ''; state.phone = '';
  localStorage.removeItem(HISTORY_KEY); localStorage.removeItem(QUESTIONS_KEY); localStorage.removeItem(PHONE_KEY);
  changeView('chat');
}

root.addEventListener('click', event => {
  const button = event.target.closest('[data-action]');
  if (!button) return;
  const action = button.dataset.action;
  if (action === 'nav') { event.preventDefault(); changeView(button.dataset.view); }
  else if (action === 'language') { state.lang = button.dataset.lang; persistSettings(); render(); }
  else if (action === 'topic') { state.topic = state.topic === button.dataset.topic ? '' : button.dataset.topic; render(); }
  else if (action === 'begin') { state.onboarded = true; persistSettings(); changeView('chat'); }
  else if (action === 'audio') { state.audio = !state.audio; if (!state.audio) stopSpeaking(); persistSettings(); render(); }
  else if (action === 'persist') { state.persist = !state.persist; persistSettings(); persistContent(); render(); }
  else if (action === 'hide') { stopSpeaking(); state.hidden = true; render(); root.querySelector('[data-action="unhide"]')?.focus(); }
  else if (action === 'unhide') { state.hidden = false; render(); root.querySelector('[data-action="hide"]')?.focus(); }
  else if (action === 'record' || action === 'record-again') { state.transcriptOpen = false; startRecording(); }
  else if (action === 'close-transcript') { state.transcriptOpen = false; render(); }
  else if (action === 'send-transcript') { const text = document.getElementById('transcript-text')?.value || ''; state.transcriptOpen = false; sendMessage(text); }
  else if (action === 'speak') { const msg = state.messages[Number(button.dataset.index)]; if (msg) speak(msg.text); }
  else if (action === 'path') {
    const path = button.dataset.path;
    if (path === 'reflect') { changeView('chat'); document.getElementById('chat-input')?.focus(); }
    if (path === 'coping') { state.exerciseRemaining = 120; changeView('exercise'); }
    if (path === 'info') changeView('info');
    if (path === 'questions') changeView('questions');
  }
  else if (action === 'bedtime-dismiss') { state.bedtimeDismissed = bedtimeNight(); render(); }
  else if (action === 'ayur-focus') { state.ayurFocus = button.dataset.focus; render(); }
  else if (action === 'exercise-toggle') toggleExercise();
  else if (action === 'exercise-stop') { stopExercise(); changeView('chat'); }
  else if (action === 'ask-card') { changeView('chat'); const card = state.cards.find(x => x.id === button.dataset.card); if (card) { const input = document.getElementById('chat-input'); input.value = `I would like general information about ${card.id}.`; input.focus(); } }
  else if (action === 'suggest-question') { const q = local('suggestedQuestions')[Number(button.dataset.index)]; if (q && !state.questions.some(x => x.text === q)) { state.questions.push({ text: q, done: false }); persistContent(); render(); } }
  else if (action === 'toggle-question') { const q = state.questions[Number(button.dataset.index)]; if (q) { q.done = !q.done; persistContent(); render(); } }
  else if (action === 'delete-question') { state.questions.splice(Number(button.dataset.index), 1); persistContent(); render(); }
  else if (action === 'edit-question') { const q = state.questions[Number(button.dataset.index)]; if (q) { const updated = window.prompt(local('edit'), q.text); if (updated?.trim()) { q.text = updated.trim().slice(0, 300); persistContent(); render(); } } }
  else if (action === 'copy-questions') { navigator.clipboard?.writeText(state.questions.map((q, i) => `${i + 1}. ${q.text}`).join('\n')).then(() => toast(local('downloaded'))).catch(() => toast(local('replyError'))); }
  else if (action === 'clear') clearAll();
  else if (action === 'retry') retryMessage();
});
root.addEventListener('change', event => {
  if (event.target.id === 'header-lang') { state.lang = event.target.value; persistSettings(); render(); }
});
root.addEventListener('submit', event => {
  if (event.target.id === 'chat-form') { event.preventDefault(); const input = document.getElementById('chat-input'); sendMessage(input?.value || ''); }
  if (event.target.id === 'question-form') { event.preventDefault(); const input = document.getElementById('question-input'); const text = input?.value.trim(); if (text) { state.questions.push({ text: text.slice(0, 300), done: false }); persistContent(); render(); document.getElementById('question-input')?.focus(); } }
  if (event.target.id === 'phone-form') { event.preventDefault(); const value = document.getElementById('phone-input')?.value.trim() || ''; if (/^\+?[0-9()\s-]{7,20}$/.test(value)) { state.phone = value; localStorage.setItem(PHONE_KEY, value); render(); } else toast('Please enter a valid phone number.'); }
});
root.addEventListener('keydown', event => {
  if (event.target.id === 'chat-input' && event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); sendMessage(event.target.value); }
  if (event.key === 'Escape' && state.transcriptOpen) { state.transcriptOpen = false; render(); }
});
window.addEventListener('online', () => { state.online = true; refreshStatus(); });
window.addEventListener('offline', () => { state.online = false; render(); });
window.addEventListener('beforeunload', () => { stopSpeaking(); stopExercise(); });

render();
refreshStatus();
