package sample

import android.content.Context
import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import generated.resources.Res
import java.time.LocalDate
import javax.crypto.Cipher
import platform.Foundation.NSString

class CommonPlatformLeaks(
    private val context: Context,
    private val value: NSString
) {
    val today = LocalDate.now()
    val cipher = Cipher.getInstance("AES")
    val uuid = java.util.UUID.randomUUID()

    fun composeStuff() {
        val ctx = LocalContext.current
        val view = LocalView.current
        val owner = LocalLifecycleOwner.current
        BackHandler { }
        val title = ctx.resources.getString(R.string.app_name)
        val color = ctx.resources.getColor(R.color.primary)
        val drawable = ctx.resources.getDrawable(R.drawable.ic_launcher)
        val resourceId = R.string.app_name
        val generated = Res.string.app_name
        val resources: Resources? = null
    }
}
