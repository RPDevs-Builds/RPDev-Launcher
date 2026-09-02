/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs
 *
 * Licensed under the GNU General Public License v3.0
 */

package iamrp.dev.launcher.actions

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.launcher3.R
import java.util.UUID

class WebhookShortcutCreatorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                WebhookConfigDialog(
                    onDismiss = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onConfirm = { name, url, method, headers, body ->
                        createShortcut(name, url, method, headers, body)
                    }
                )
            }
        }
    }

    private fun createShortcut(
        name: String,
        url: String,
        method: String,
        headers: String,
        body: String
    ) {
        val shortcutIntent = WebhookActionActivity.createIntent(
            context = this,
            label = name,
            url = url,
            method = method,
            headers = if (headers.isBlank()) null else headers,
            body = if (body.isBlank()) null else body
        )

        val shortcutId = "webhook_${UUID.randomUUID()}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(this, shortcutId)
                    .setShortLabel(name)
                    .setLongLabel("$method: $name")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_smartspace_preferences))
                    .setIntent(shortcutIntent)
                    .build()

                val resultIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                setResult(Activity.RESULT_OK, resultIntent)
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                Toast.makeText(this, "✓ Shortcut '$name' added to Home", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }

        // Legacy / Standard CREATE_SHORTCUT return
        val returnIntent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, name)
            putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this@WebhookShortcutCreatorActivity, R.drawable.ic_smartspace_preferences)
            )
        }
        setResult(Activity.RESULT_OK, returnIntent)
        Toast.makeText(this, "✓ Shortcut '$name' created", Toast.LENGTH_SHORT).show()
        finish()
    }
}

@Composable
fun WebhookConfigDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String, method: String, headers: String, body: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }
    var method by remember { mutableStateOf("GET") }
    var headers by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "⚡ New Webhook / REST Action",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Create a one-tap shortcut to trigger a REST GET or Webhook Push request in the background.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Shortcut Name") },
                    placeholder = { Text("e.g. Living Room Lights / Sync") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Endpoint URL") },
                    placeholder = { Text("https://homeassistant.local:8123/api/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GET", "POST", "PUT", "DELETE").forEach { m ->
                        OutlinedButton(
                            onClick = { method = m },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (method == m) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Text(
                                text = m,
                                fontWeight = if (method == m) FontWeight.Bold else FontWeight.Normal,
                                color = if (method == m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = headers,
                    onValueChange = { headers = it },
                    label = { Text("Headers (JSON, Optional)") },
                    placeholder = { Text("{\"Authorization\": \"Bearer token\"}") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (method == "POST" || method == "PUT") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Payload / Body (JSON, Optional)") },
                        placeholder = { Text("{\"state\": \"on\"}") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && url.isNotBlank()) {
                                onConfirm(name.trim(), url.trim(), method, headers.trim(), body.trim())
                            }
                        },
                        enabled = name.isNotBlank() && url.isNotBlank()
                    ) {
                        Text("Add Shortcut")
                    }
                }
            }
        }
    }
}
