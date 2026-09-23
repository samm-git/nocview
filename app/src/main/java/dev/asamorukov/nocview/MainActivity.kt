package dev.asamorukov.nocview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.asamorukov.nocview.ui.navigation.AppNav
import dev.asamorukov.nocview.ui.theme.NocViewTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NocViewTheme {
                AppNav()
            }
        }
    }
}
