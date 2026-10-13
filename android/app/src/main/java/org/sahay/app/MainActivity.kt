package org.sahay.app

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var model: SahayViewModel
    private var speech: TextToSpeech? = null
    private var speechReady = false
    private var player: MediaPlayer? = null
    private var speechFile: File? = null
    private var recorder: MediaRecorder? = null
    private var recording by mutableStateOf<File?>(null)
    private var voiceEpoch = 0

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val words = result.data?.getStringArrayExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                ?: result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!words.isNullOrBlank()) model.setTranscript(words) else show(model.t("speechError"))
        }
    }
    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        if (allowed) startRecording() else model.note("Microphone permission was denied. You can still type.")
    }
    private val documentPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) Thread {
            val selected = runCatching {
                var name = "attachment"
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if(cursor.moveToFirst()) {
                        name = cursor.getString(0)
                        if(!cursor.isNull(1) && cursor.getLong(1) > 5 * 1024 * 1024) error("Choose a file under 5 MB")
                    }
                }
                val mime = contentResolver.getType(uri) ?: "text/plain"
                val bytes = contentResolver.openInputStream(uri)?.use { input ->
                    val output = ByteArrayOutputStream(); val buffer = ByteArray(8192)
                    while (output.size() <= 5 * 1024 * 1024) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray()
                } ?: error("Could not read file")
                if(bytes.size > 5 * 1024 * 1024) error("Choose a file under 5 MB")
                PendingFile(name, mime, bytes)
            }
            runOnUiThread { selected.onSuccess(model::updatePendingFile).onFailure { model.note(it.message ?: "Could not attach file") } }
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        model = ViewModelProvider(this)[SahayViewModel::class.java]
        speech = TextToSpeech(this) { status -> speechReady = status == TextToSpeech.SUCCESS }
        setContent {
            val density=LocalDensity.current
            CompositionLocalProvider(LocalWellbeingLanguage provides model.language, LocalDensity provides Density(density.density, density.fontScale * if(model.largeText) 1.16f else 1f)) {
                SahayTheme {
                    SahayApp(model, ::record, ::speak, ::stopSpeaking, ::openUrl, ::dial, ::show,
                        onAttachFile = { documentPicker.launch(arrayOf("application/pdf", "image/png", "image/jpeg", "text/plain")) },
                        onOpenFile = { file -> model.downloadAttachment(file, ::openDownloadedFile) })
                    if(recording != null) AlertDialog(onDismissRequest = { cancelRecording() },
                        title = { Text("Record your message") },
                        text = { Text("Recording for up to one minute. Tap Transcribe to send this clip to your configured speech provider, then review the words before sending to Sahay.") },
                        confirmButton = { TextButton({ finishRecording() }) { Text("Transcribe") } },
                        dismissButton = { TextButton({ cancelRecording() }) { Text("Cancel and delete") } })
                }
            }
        }
    }
    private fun openDownloadedFile(name: String, mime: String, bytes: ByteArray) {
        val directory=File(cacheDir,"care").apply { mkdirs() }
        directory.listFiles()?.filter { System.currentTimeMillis()-it.lastModified()>3600000 }?.forEach { it.delete() }
        val safeName=name.replace(Regex("[^a-zA-Z0-9._ -]"),"_").take(120).takeIf {it.isNotBlank() && it!="." && it!=".."} ?: "attachment"
        val file=File(directory,safeName)
        file.writeBytes(bytes)
        val uri=FileProvider.getUriForFile(this,"$packageName.files",file)
        try { startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).setDataAndType(uri,mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),"Open shared file")) }
        catch(_: ActivityNotFoundException) { show("No app can open this file type") }
    }
    private fun record() {
        if (model.speechProcessing) { model.note("Wait for this transcript, or keep typing."); return }
        if(model.useSpeechApi) {
            if(!model.speechSttReady) { model.note("The speech API is awaiting configuration. Use device speech or text in the meantime."); return }
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED) startRecording()
            else micPermission.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        val speechTag=supportedLanguages.firstOrNull {it.code==model.language}?.speech ?: "en-IN"
        val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,speechTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT,model.t("mic"))
        }
        try { speechLauncher.launch(intent) } catch(_: ActivityNotFoundException) {show(nativeText(model.language,"speechUnavailable"))}
    }
    private fun startRecording() {
        cancelRecording()
        val file=File(cacheDir,"sahay-recording.m4a")
        runCatching {
            recorder = (if(Build.VERSION.SDK_INT>=31) MediaRecorder(this) else MediaRecorder()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                setMaxDuration(60000)
                setOnInfoListener { _,what,_ -> if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) runOnUiThread {finishRecording(autoStopped=true)} }
                prepare(); start()
            }
            recording=file
        }.onFailure {cancelRecording(); model.note("Recording couldn't start. Check microphone access or type instead.")}
    }
    private fun finishRecording(autoStopped: Boolean = false) {
        val file=recording ?: return
        val stopped=autoStopped || runCatching {recorder?.stop()}.isSuccess
        recorder?.release(); recorder=null; recording=null
        if(stopped && file.exists()) {val bytes=file.readBytes(); file.delete(); model.transcribeAudio(bytes)}
        else {file.delete(); model.note("The recording was too short. Try again.")}
    }
    private fun cancelRecording() {
        runCatching {recorder?.stop()}; recorder?.release(); recorder=null
        recording?.delete(); recording=null
    }
    private fun speak(text: String) {
        stopSpeaking()
        val epoch = voiceEpoch
        if(model.useSpeechApi) {
            if(!model.speechTtsReady) {model.note("The playback API is awaiting configuration. Device voices can be selected in Settings."); return}
            model.requestSpeech(text) {bytes ->
                if (epoch != voiceEpoch) return@requestSpeech
                val file=File(cacheDir,"sahay-speech.mp3"); file.writeBytes(bytes); speechFile=file
                runCatching {player=MediaPlayer().apply {setDataSource(file.absolutePath); prepare(); setOnCompletionListener {stopSpeaking()}; start()}}.onFailure {stopSpeaking(); model.note("Could not play spoken reply")}
            }
            return
        }
        val engine=speech
        if(!speechReady || engine==null) {show(nativeText(model.language,"noVoice")); return}
        val locale=Locale.forLanguageTag(supportedLanguages.firstOrNull {it.code==model.language}?.speech ?: "en-IN")
        if(engine.isLanguageAvailable(locale)<TextToSpeech.LANG_AVAILABLE) {show(nativeText(model.language,"noVoice")); return}
        engine.language=locale; engine.setSpeechRate(.88f); engine.speak(text,TextToSpeech.QUEUE_FLUSH,null,"sahay-reply")
    }
    private fun stopSpeaking() {
        voiceEpoch++
        speech?.stop(); runCatching {player?.stop()}; player?.release(); player=null; speechFile?.delete(); speechFile=null
    }
    private fun openUrl(value: String) {try {startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse(value)))} catch(_: ActivityNotFoundException) {show(value)}}
    private fun dial(number: String) {try {startActivity(Intent(Intent.ACTION_DIAL,android.net.Uri.parse("tel:$number")))} catch(_: ActivityNotFoundException) {show(number)}}
    private fun show(message: String) {Toast.makeText(this,message,Toast.LENGTH_LONG).show()}
    override fun onStop() { AmbientAudio.stop(); cancelRecording(); stopSpeaking(); super.onStop() }
    override fun onDestroy() {cancelRecording(); stopSpeaking(); speech?.shutdown(); speech=null; super.onDestroy()}
}
