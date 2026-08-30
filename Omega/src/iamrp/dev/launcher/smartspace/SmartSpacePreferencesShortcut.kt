package iamrp.dev.launcher.smartspace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import iamrp.dev.launcher.compose.navigation.Routes
import iamrp.dev.launcher.preferences.PreferenceActivity

class SmartSpacePreferencesShortcut : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(PreferenceActivity.navigateIntent(this, Routes.PREFS_WIDGETS))
        finish()
    }
}