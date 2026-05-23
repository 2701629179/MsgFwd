package com.phonemonitor.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object HttpClient {
  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .build()

  suspend fun request(method: String, url: String, body: String? = null): Result<Pair<Int, String>> =
    withContext(Dispatchers.IO) {
      try {
        val rb = if (body != null && method.uppercase() in listOf("POST", "PUT", "PATCH"))
          body.toRequestBody("application/json; charset=utf-8".toMediaType()) else null
        val req = Request.Builder().url(url).method(method.uppercase(), rb)
          .addHeader("User-Agent", "NotiMon/1.0").build()
        val rsp = client.newCall(req).execute()
        val b = rsp.body?.string() ?: ""
        Result.success(Pair(rsp.code, b))
      } catch (e: Exception) { Result.failure(e) }
    }
}
