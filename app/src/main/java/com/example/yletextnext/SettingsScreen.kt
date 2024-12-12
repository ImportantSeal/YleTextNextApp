package com.example.yletextnext

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    val currentLanguage = remember { mutableStateOf(sharedPreferences.getString("language", "fi") ?: "fi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // tummantilan kytkin
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.dark_mode))
            Switch(
                checked = isDarkMode,
                onCheckedChange = { checked ->
                    onDarkModeChange(checked)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // kielen vaihto valikko
        Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuBox(
            currentLanguage = currentLanguage.value,
            onLanguageSelected = { selectedLanguage ->
                currentLanguage.value = selectedLanguage
                with(sharedPreferences.edit()) {
                    putString("language", selectedLanguage)
                    apply()
                }
                onLanguageChange(selectedLanguage)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // takaisin painike
        Button(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun DropdownMenuBox(currentLanguage: String, onLanguageSelected: (String) -> Unit) {
    val languages = listOf("fi" to "Suomi", "en" to "English")
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(languages.find { it.first == currentLanguage }?.second ?: "Suomi")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
