/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs
 *
 * Licensed under the GNU General Public License v3.0
 */

package iamrp.dev.launcher.actions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebhookActionActivity : Activity() {

    companion object {
        const val ACTION_TRIGGER = "iamrp.dev.launcher.ACTION_TRIGGER_WEBHOOK"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_METHOD = "extra_method"
        const val EXTRA_HEADERS = "extra_headers"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_LABEL = "extra_label"

        private val httpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }

        fun createIntent(
            context: Context,
            label: String,
            url: String,
            method: String = "GET",
            headers: String? = null,
            body: String? = null
        ): Intent {
            return Intent(context, WebhookActionActivity::class.java).apply {
                action = ACTION_TRIGGER
                putExtra(EXTRA_LABEL, label)
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_METHOD, method)
                putExtra(EXTRA_HEADERS, headers)
                putExtra(EXTRA_BODY, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val label = intent.getStringExtra(EXTRA_LABEL) ?: "REST Action"
        val url = intent.getStringExtra(EXTRA_URL)
        val method = intent.getStringExtra(EXTRA_METHOD) ?: "GET"
        val headersJson = intent.getStringExtra(EXTRA_HEADERS)
        val body = intent.getStringExtra(EXTRA_BODY)

        if (url.isNullOrBlank()) {
            Toast.makeText(this, "⚠️ Invalid or empty Webhook URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Toast.makeText(this, "⚡ Sending $method request: $label...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBuilder = Request.Builder()
                    .url(url)
                    .header("User-Agent", "RPDev-Launcher/1.0.0 (Webhook Action)")

                if (!headersJson.isNullOrBlank()) {
                    try {
                        val json = JSONObject(headersJson)
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            requestBuilder.header(key, json.getString(key))
                        }
                    } catch (_: Exception) {
                    }
                }

                if (method.equals("POST", ignoreCase = true) || method.equals("PUT", ignoreCase = true)) {
                    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                    val requestBody = (body ?: "{}").toRequestBody(mediaType)
                    if (method.equals("POST", ignoreCase = true)) {
                        requestBuilder.post(requestBody)
                    } else {
                        requestBuilder.put(requestBody)
                    }
                } else {
                    requestBuilder.get()
                }

                val response = httpClient.newCall(requestBuilder.build()).execute()
                val code = response.code

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@WebhookActionActivity,
                            "✓ $label: HTTP $code Success",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@WebhookActionActivity,
                            "⚠️ $label Failed: HTTP $code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@WebhookActionActivity,
                        "❌ $label Error: ${e.localizedMessage ?: "Connection Failed"}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }
}
