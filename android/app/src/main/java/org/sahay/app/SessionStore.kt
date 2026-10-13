package org.sahay.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredSession(val token: String, val role: String, val name: String, val contact: String, val server: String)

/** Keeps the demo session across process restarts without saving its bearer token as plain text. */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("sahay_session", Context.MODE_PRIVATE)
    private val alias = "sahay_demo_session_v1"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build())
        }.generateKey()
    }

    fun save(session: StoredSession): Boolean = runCatching {
        val plain = JSONObject().put("token", session.token).put("role", session.role)
            .put("name", session.name).put("contact", session.contact).put("server", session.server)
            .toString().toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val packed = cipher.iv + cipher.doFinal(plain)
        prefs.edit().putString("session", Base64.encodeToString(packed, Base64.NO_WRAP)).commit()
    }.getOrDefault(false)

    fun load(): StoredSession? {
        val value = prefs.getString("session", null) ?: return null
        return runCatching {
            val packed = Base64.decode(value, Base64.NO_WRAP)
            require(packed.size > 12)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, packed.copyOfRange(0, 12)))
            }
            val data = JSONObject(String(cipher.doFinal(packed.copyOfRange(12, packed.size)), Charsets.UTF_8))
            StoredSession(data.getString("token"), data.getString("role"), data.getString("name"),
                data.optString("contact"), data.getString("server"))
        }.getOrElse { clear(); null }
    }

    fun clear() { prefs.edit().remove("session").commit() }
}
