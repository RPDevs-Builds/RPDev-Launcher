package iamrp.dev.launcher.gestures

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import iamrp.dev.launcher.RPDevLauncher

class AssistantGestureReceiver : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == Intent.ACTION_ASSIST || intent.action == Intent.ACTION_SEARCH_LONG_PRESS) {
            val neoLauncher = runCatching { RPDevLauncher.getLauncher(this) }.getOrNull()
            neoLauncher?.gestureController?.onLaunchAssistant()
        }
        finish()
    }
}