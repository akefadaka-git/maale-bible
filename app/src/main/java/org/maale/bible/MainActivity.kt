package org.maale.bible
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.maale.bible.ui.MaaleBibleApp
import org.maale.bible.ui.theme.MaaleBibleTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaaleBibleTheme { MaaleBibleApp() } }
    }
}
