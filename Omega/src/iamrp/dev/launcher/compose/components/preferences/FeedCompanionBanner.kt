/*
 * This file is part of RPDev Launcher
 * Copyright (c) 2026 RPDevs
 *
 * Licensed under the GNU General Public License v3.0
 */

package iamrp.dev.launcher.compose.components.preferences

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import iamrp.dev.launcher.actions.WebhookShortcutCreatorActivity

fun isFeedCompanionInstalled(context: Context): Boolean {
    val pm = context.packageManager
    val targetPackages = listOf("com.saulhdev.neofeed", "iamrp.dev.feed", "com.saulhdev.neofeed.dev")
    return targetPackages.any { pkg ->
        try {
            pm.getPackageInfo(pkg, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}

@Composable
fun FeedCompanionBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val installed = isFeedCompanionInstalled(context)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌟 RPDev Feed Companion Hub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (installed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = if (installed) "Connected" else "Add-on Available",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (installed) {
                    "RPDev Feed is active and delivering Privacy Weather (Open-Meteo), Calendar Agenda, Hardware Telemetry, and RSS/JSON Feeds to your left-swipe screen."
                } else {
                    "Keep your launcher lightweight while unlocking powerhouse features. Install the RPDev Feed companion to enable Privacy Weather (Open-Meteo), Calendar Agenda, Hardware Telemetry, and RSS/JSON feeds without background bloat."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (installed) {
                    Button(
                        onClick = {
                            val intent = context.packageManager.getLaunchIntentForPackage("com.saulhdev.neofeed")
                                ?: context.packageManager.getLaunchIntentForPackage("iamrp.dev.feed")
                            if (intent != null) context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open Feed App")
                    }
                } else {
                    Button(
                        onClick = {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/RPDevs-Builds/RPDev-Feed/releases/latest")
                            )
                            context.startActivity(browserIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Get Companion Feed")
                    }
                }

                OutlinedButton(
                    onClick = {
                        val shortcutIntent = Intent(context, WebhookShortcutCreatorActivity::class.java)
                        context.startActivity(shortcutIntent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⚡ Webhook Action")
                }
            }
        }
    }
}
