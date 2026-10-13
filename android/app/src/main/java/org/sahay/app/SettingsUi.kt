package org.sahay.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

@Composable
fun SettingsPage(model: SahayViewModel, stopSpeaking: () -> Unit) {
    var name by remember(model.accountName) { mutableStateOf(model.accountName) }
    var current by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var editAccount by remember { mutableStateOf(false) }
    var editPassword by remember { mutableStateOf(false) }
    var advanced by remember { mutableStateOf(false) }
    var address by remember(model.serverUrl) { mutableStateOf(model.serverUrl) }
    var modelUrl by remember(model.modelEndpoint) { mutableStateOf(model.modelEndpoint) }
    var modelName by remember(model.modelName) { mutableStateOf(model.modelName) }
    var clearConfirm by remember { mutableStateOf(false) }
    val isPatient = model.accountRole !in setOf("clinician", "admin")
    CalmPage(wellbeingText(model.language,"settings"), model::closeSettings, model) {
        if(model.accountError.isNotBlank()) WellbeingLabel(model.accountError,color=MaterialTheme.colorScheme.error)
        CalmCard {
            WellbeingLabel("Your account",color=CalmInk,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
            if(model.accountRole==null) {
                WellbeingLabel("You're using a private guest session.",color=CalmMuted)
                WellbeingLabel("Sign in to exchange messages and files with a care-team account.",color=CalmMuted,fontSize=14.sp)
                CalmButton("Patient sign in",{ model.navigate(Screen.PATIENT_ACCOUNT) })
                OutlinedButton({ model.navigate(Screen.CLINICIAN_AUTH) },Modifier.fillMaxWidth()) { WellbeingLabel("Care team sign in") }
            } else {
                WellbeingLabel(model.accountName,color=CalmInk,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
                WellbeingLabel(model.accountContact,color=CalmMuted,fontSize=14.sp)
                WellbeingLabel("${model.accountRole} · Course demo account",color=CalmMuted,fontSize=14.sp)
                SettingsLink("Edit display name", { editAccount=!editAccount; model.clearAccountError() }, icon = Icons.Outlined.PersonOutline)
                if(editAccount) {
                    OutlinedTextField(name,{name=it.take(100)},Modifier.fillMaxWidth(),label={WellbeingLabel("Display name")},singleLine=true)
                    CalmButton("Save account name",{model.updateProfile(name)},name.isNotBlank() && !model.accountBusy)
                }
                SettingsLink("Change password", {editPassword=!editPassword; model.clearAccountError()}, icon = Icons.Outlined.Lock)
                if(editPassword) {
                    OutlinedTextField(current,{current=it.take(128)},Modifier.fillMaxWidth(),label={WellbeingLabel("Current password")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
                    OutlinedTextField(password,{password=it.take(128)},Modifier.fillMaxWidth(),label={WellbeingLabel("New password · at least 10 characters")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
                    CalmButton("Update password and sign out",{model.changePassword(current,password)},current.isNotBlank() && password.length>=10 && !model.accountBusy)
                }
                HorizontalDivider(color = CalmSoft)
                SettingsLink("Sign out", {stopSpeaking(); AmbientAudio.stop(); model.signOut()}, icon = Icons.Outlined.Logout, destructive = true)
            }
        }
        CalmCard {
            WellbeingLabel("Language and comfort",color=CalmInk,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
            supportedLanguages.chunked(2).forEach { pair -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                pair.forEach { option -> FilterChip(model.language==option.code,{model.changeLanguage(option.code)},label={WellbeingLabel(option.label)},modifier=Modifier.weight(1f).heightIn(min = 48.dp)) }
            } }
            SettingSwitch("Larger text","Increase the size of text across the app.",model.largeText,model::updateLargeText)
            SettingSwitch("Reduce motion","Use still visuals in the arrival and breathing screens.",model.reduceMotion,model::updateReduceMotion)
            WellbeingLabel("Activity sound volume",color=CalmInk)
            Slider(model.soundVolume,{model.updateSoundVolume(it); AmbientAudio.volume=it})
        }
        if(isPatient) CalmCard {
            WellbeingLabel("Voice and audio",color=CalmInk,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
            SettingSwitch("Read replies aloud","Play spoken responses when a supported voice is available.",model.audioEnabled) { model.setAudio(it); if(!it) stopSpeaking() }
            SettingSwitch("Use the speech API","Record a short clip and review the returned transcript before sending.",model.useSpeechApi,model::setSpeechApi)
            WellbeingLabel(if(model.useSpeechApi) "Transcription: ${if(model.speechSttReady) "ready" else "awaiting API configuration"}\nPlayback: ${if(model.speechTtsReady) "ready" else "awaiting API configuration"}" else "Device speech uses installed Android recognition and voices.",color=CalmMuted,fontSize=14.sp)
            WellbeingLabel("A recorded clip is sent only when you choose Transcribe. Recordings are removed from this device after processing or canceling.",color=CalmMuted,fontSize=14.sp)
        }
        if(isPatient) CalmCard {
            WellbeingLabel("Privacy and local data",color=CalmInk,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
            SettingSwitch("Save on this device","Keep your chat, questions and journal after closing the app.",model.rememberContent,model::setRemember)
            WellbeingLabel("Chat and voice recordings are not automatically shared with your care team. Messages and files you send in Care team are visible to that conversation's two participants.",color=CalmMuted,fontSize=14.sp)
            SettingsLink("Clear local conversation and journal", {clearConfirm=true}, icon = Icons.Outlined.DeleteOutline, destructive = true)
            SettingsLink("Quick hide this screen", model::showCover, icon = Icons.Outlined.VisibilityOff)
        }
        CalmCard {
            WellbeingLabel("Support and information",color=CalmInk,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
            SettingsLink("Human help", {model.navigate(Screen.HELP)}, icon = Icons.Outlined.HelpOutline)
            if(isPatient) SettingsLink("Trusted information", {model.navigate(Screen.INFO)}, icon = Icons.Outlined.MenuBook)
            SettingsLink("AI activity and agent handoffs", {model.navigate(Screen.AGENT_INSPECT)}, icon = Icons.Outlined.AccountTree)
            WellbeingLabel("Sahay is an AI wellbeing course prototype. Care conversations are asynchronous and are not monitored for emergencies.",color=CalmMuted,fontSize=14.sp)
        }
        CalmCard {
            WellbeingLabel("AI model",color=CalmInk,fontSize=20.sp,fontWeight=FontWeight.SemiBold)
            WellbeingLabel("Choose the endpoint and model used for conversations, translation, activity plans and visit drafts.",color=CalmMuted,fontSize=14.sp)
            OutlinedTextField(modelUrl,{modelUrl=it.take(2048)},Modifier.fillMaxWidth(),
                label={WellbeingLabel("Model endpoint URL")},singleLine=true,
                placeholder={Text("https://opencode.ai/zen/v1/chat/completions")},
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Uri,imeAction=ImeAction.Next))
            OutlinedTextField(modelName,{modelName=it.take(200)},Modifier.fillMaxWidth(),
                label={WellbeingLabel("Model name")},singleLine=true,
                placeholder={Text("space-bunny-free")},
                keyboardOptions=KeyboardOptions(imeAction=ImeAction.Done))
            WellbeingLabel(model.modelConnectionMessage,color=if(model.modelConnectionFailed) MaterialTheme.colorScheme.error else CalmMuted,fontSize=14.sp)
            CalmButton(if(model.modelTesting) "Testing connection…" else "Save and test model",
                {model.saveModelConfiguration(modelUrl,modelName)},
                modelUrl.isNotBlank() && modelName.isNotBlank() && !model.modelTesting && !model.busy && !model.planBusy)
            if(model.hasCustomModel) TextButton(onClick=model::useBackendModel,
                enabled = !model.modelTesting && !model.busy && !model.planBusy,
                modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { WellbeingLabel("Use backend default") }
            WellbeingLabel("Saved for this device. Some providers require an API key configured on the backend. Changing the AI model keeps you signed in.",color=CalmMuted,fontSize=14.sp)
        }
        CalmCard {
            SettingsLink("Connection settings", {advanced=!advanced}, icon = Icons.Outlined.Wifi)
            WellbeingLabel(if(model.backendConnected) "Backend connected" else "Backend unavailable · activities work offline",color=CalmMuted,fontSize=14.sp)
            if(advanced) {
                OutlinedTextField(address,{address=it},Modifier.fillMaxWidth(),label={WellbeingLabel("Backend address")},singleLine=true)
                WellbeingLabel("Changing the server signs you out. Provider keys are configured on the backend.",color=CalmMuted,fontSize=14.sp)
                CalmButton("Save and check connection",{model.setServer(address)})
            }
        }
        Spacer(Modifier.height(24.dp))
    }
    if(clearConfirm) AlertDialog(onDismissRequest={clearConfirm=false},title={WellbeingLabel("Clear this device's saved content?")},
        text={WellbeingLabel("Your chat, questions, journal and care-team number will be removed from this device. Sent care messages and files remain in the care conversation.")},
        confirmButton={TextButton({stopSpeaking(); model.clearAll(); clearConfirm=false}){WellbeingLabel("Clear local data")}},dismissButton={TextButton({clearConfirm=false}){WellbeingLabel("Cancel")}})
}
@Composable
private fun SettingSwitch(title: String, detail: String, value: Boolean, changed: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).toggleable(value = value, role = Role.Switch, onValueChange = changed)
        .padding(vertical = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            WellbeingLabel(title, color = CalmInk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            WellbeingLabel(detail, color = CalmMuted, fontSize = 14.sp, lineHeight = 20.sp)
        }
        Switch(value, onCheckedChange = null)
    }
}
