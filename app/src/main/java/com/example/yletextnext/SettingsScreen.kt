package com.example.yletextnext

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.util.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

// funktio, joka päivittää sovelluksen kielen
fun updateLocale(context: Context, language: String) {
    // luodaan uusi locale valitulle kielelle
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    // asetetaan configiin uusi kieliasetus
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    // haetaan konteksti ja shared preferences asetuksia varten
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    // luetaan tallennettu kieliasetus tai oletuksena "fi"
    val currentLanguage = remember { mutableStateOf(sharedPreferences.getString("language", "fi") ?: "fi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // otsikko
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // tumma tila -kytkin
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.dark_mode))
            // switch muuttaa tilaa ja kutsuu onDarkModeChange -callbackia
            Switch(
                checked = isDarkMode,
                onCheckedChange = { checked ->
                    onDarkModeChange(checked)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // kielen vaihtamisen otsikko
        Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // dropdown-valikko kielen valintaan
        DropdownMenuBox(
            currentLanguage = currentLanguage.value,
            onLanguageSelected = { selectedLanguage ->
                // päivitetään muistissa olevaa kieltä
                currentLanguage.value = selectedLanguage

                // tallennetaan uusi kieli shared preferencesiin
                with(sharedPreferences.edit()) {
                    putString("language", selectedLanguage)
                    apply()
                }

                // kutsutaan callbackia kielenvaihdolle
                onLanguageChange(selectedLanguage)

                // päivitetään sovelluksen locale ja käynnistetään mainactivity uudelleen
                updateLocale(context, selectedLanguage)
                val activity = context as Activity
                activity.finish()
                activity.startActivity(Intent(context, MainActivity::class.java))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // takaisin-painike
        Button(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun DropdownMenuBox(currentLanguage: String, onLanguageSelected: (String) -> Unit) {
    // määritellään kielivalinnat: suomi ja englanti
    val languages = listOf("fi" to "suomi", "en" to "english")
    var expanded by remember { mutableStateOf(false) }

    Box {
        // painike, josta avataan dropdown-menu kielen vaihtamiseksi
        Button(onClick = { expanded = true }) {
            // näytetään valitun kielen nimi
            Text(languages.find { it.first == currentLanguage }?.second ?: "suomi")
        }

        // dropdown-menu, josta käyttäjä voi valita kielen
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // käydään läpi jokainen kielivaihtoehto
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        // kutsutaan callbackia kun kieli valitaan
                        onLanguageSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
