package org.maale.bible

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import org.maale.bible.ui.MaaleBibleApp
import org.maale.bible.ui.theme.MaaleBibleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the host Activity's registry explicit for composables that launch
        // document pickers. This keeps registry ownership at the Activity boundary.
        setContent {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                MaaleBibleTheme {
                    MaaleBibleApp()
                }
            }
        }
    }
}
