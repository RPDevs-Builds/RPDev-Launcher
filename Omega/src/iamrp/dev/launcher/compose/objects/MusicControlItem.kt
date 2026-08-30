package iamrp.dev.launcher.compose.objects

import android.media.AudioManager
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.launcher3.R
import iamrp.dev.launcher.compose.icons.Phosphor
import iamrp.dev.launcher.compose.icons.phosphor.Pause
import iamrp.dev.launcher.compose.icons.phosphor.Play
import iamrp.dev.launcher.compose.icons.phosphor.SkipBack
import iamrp.dev.launcher.compose.icons.phosphor.SkipForward

class MusicControlItem(
    val icon: ImageVector,
    @StringRes val description: Int,
    val onClick: (AudioManager) -> Unit
) {
    // TODO fix descriptions
    companion object {
        val PLAY = MusicControlItem(
            Phosphor.Play,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
        }

        val PAUSE = MusicControlItem(
            Phosphor.Pause,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PAUSE
                )
            )
        }

        val PREVIOUS = MusicControlItem(
            Phosphor.SkipBack,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS
                )
            )
        }

        val NEXT = MusicControlItem(
            Phosphor.SkipForward,
            R.string.dash_media_player
        ) { musicManager ->
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_NEXT
                )
            )
            musicManager.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_NEXT
                )
            )
        }
    }
}
