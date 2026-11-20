package com.creatorpro.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Offline-first: kalau BASE_URL masih placeholder (bawaan), langsung pakai
 * OfflineAi tanpa menyentuh internet. Kalau pemilik aplikasi sudah deploy
 * backend dan mengisi BASE_URL, coba online dulu; kalau server gagal,
 * otomatis jatuh kembali ke OfflineAi supaya pengguna tidak pernah
 * mentok di layar error.
 */
object CreatorApi {
    // Ganti dengan alamat HTTPS backend milikmu setelah backend dipasang.
    // Biarkan apa adanya kalau mau aplikasi jalan 100% offline.
    private const val BASE_URL = "https://YOUR-BACKEND-DOMAIN.com"

    fun isOnlineConfigured(): Boolean =
        !BASE_URL.contains("YOUR-BACKEND-DOMAIN") && BASE_URL.startsWith("https://")

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generate(tool: String, topic: String, audience: String): String {
        val cleanTopic = topic.trim()
        require(cleanTopic.isNotBlank()) { "Topik tidak boleh kosong." }
        val cleanAudience = audience.trim().ifBlank { "Penonton umum" }
        if (!isOnlineConfigured()) {
            return OfflineAi.generate(tool, cleanTopic, cleanAudience)
        }
        return try {
            generateOnline(tool, cleanTopic, cleanAudience)
        } catch (e: Exception) {
            // Server gagal / tidak terjangkau: jangan biarkan pengguna mentok.
            OfflineAi.generate(tool, cleanTopic, cleanAudience)
        }
    }

    private suspend fun generateOnline(tool: String, topic: String, audience: String): String = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("tool", tool)
            .put("topic", topic)
            .put("audience", audience)
        val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/api/generate")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Server mengembalikan HTTP ${response.code}: $text")
            val result = JSONObject(text).optString("result", "").trim()
            if (result.isBlank()) error("Server tidak mengirim hasil.")
            result
        }
    }
}
