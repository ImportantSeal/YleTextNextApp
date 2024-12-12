package com.example.yletextnext

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.yletextnext.ui.theme.YleTextNextTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // tumman tilan luku asetuksista (tumma tila on oletus)
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val isDarkModeInitially = prefs.getBoolean("dark_mode", true)

        setContent {
            var isDarkMode by remember { mutableStateOf(isDarkModeInitially) }

            // käytetään teemaa ja käynnistetään pääruutu
            YleTextNextTheme(darkTheme = isDarkMode) {
                YleTextNextScreen(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { newValue ->
                        isDarkMode = newValue
                        prefs.edit().putBoolean("dark_mode", newValue).apply()
                    },
                    onLanguageChange = { /* tehdään jos tarpeen */ }
                )
            }
        }
    }
}
