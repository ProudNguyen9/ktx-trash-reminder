package com.example.data.remote

import android.util.Log
import com.example.data.model.Member
import com.example.data.model.TrashState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseClient {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    data class FirebasePayload(
        val currentTurnIndex: Int,
        val isTrashFull: Boolean,
        val reportedByName: String,
        val reportedAt: Long,
        val members: List<FirebaseMember>
    )

    data class FirebaseMember(
        val id: Int,
        val name: String,
        val email: String
    )

    suspend fun syncToFirebase(
        dbUrl: String,
        apiKey: String,
        state: TrashState,
        members: List<Member>
    ): Boolean = withContext(Dispatchers.IO) {
        if (dbUrl.isBlank()) return@withContext false
        
        val formattedUrl = dbUrl.trim().removeSuffix("/")
        val url = if (apiKey.isNotBlank()) {
            "$formattedUrl/state.json?auth=$apiKey"
        } else {
            "$formattedUrl/state.json"
        }

        val payload = FirebasePayload(
            currentTurnIndex = state.currentTurnIndex,
            isTrashFull = state.isTrashFull,
            reportedByName = state.reportedByName,
            reportedAt = state.reportedAt,
            members = members.map { FirebaseMember(it.id, it.name, it.email) }
        )

        return@withContext try {
            val adapter = moshi.adapter(FirebasePayload::class.java)
            val json = adapter.toJson(payload)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("FirebaseClient", "Successfully synced to Firebase")
                    true
                } else {
                    Log.e("FirebaseClient", "Failed to sync to Firebase: ${response.code} ${response.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseClient", "Error syncing to Firebase", e)
            false
        }
    }

    suspend fun fetchFromFirebase(
        dbUrl: String,
        apiKey: String
    ): FirebasePayload? = withContext(Dispatchers.IO) {
        if (dbUrl.isBlank()) return@withContext null

        val formattedUrl = dbUrl.trim().removeSuffix("/")
        val url = if (apiKey.isNotBlank()) {
            "$formattedUrl/state.json?auth=$apiKey"
        } else {
            "$formattedUrl/state.json"
        }

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (bodyString != null && bodyString != "null") {
                        val adapter = moshi.adapter(FirebasePayload::class.java)
                        adapter.fromJson(bodyString)
                    } else {
                        null
                    }
                } else {
                    Log.e("FirebaseClient", "Failed to fetch from Firebase: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseClient", "Error fetching from Firebase", e)
            null
        }
    }
}
