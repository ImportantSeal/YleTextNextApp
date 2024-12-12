package com.example.yletextnext

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch


@Composable
fun FavoritesScreen(
    favorites: MutableList<Pair<Int, String>>,
    navController: NavController,
    onBack: () -> Unit,
    getPageContent: suspend (Int) -> List<Map<String, Any>>
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(stringResource(R.string.favorites), style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(20.dp))

        // tarkistetaan onko suosikkeja
        if (favorites.isEmpty()) {
            Text(stringResource(R.string.no_favorites))
        } else {
            // kaikkien suosikkien läpikäynti
            favorites.forEach { (pageNumber, _) ->
                var pageContent by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
                var pageTitle by remember { mutableStateOf("Sivu $pageNumber") }

                // sivun sisällän haku ja otsikko asetus
                LaunchedEffect(pageNumber) {
                    coroutineScope.launch {
                        val content = getPageContent(pageNumber)
                        pageContent = content

                        // otsikkon haku ensimmäisestä rivistä jos mahdollista
                        val title = content.firstOrNull()?.get("Text") as? String
                        if (!title.isNullOrBlank()) {
                            pageTitle = title
                        }
                    }
                }

                // suosikkisivu ja poistonappi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$pageNumber - $pageTitle",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // suosikkin poisto painike
                    IconButton(
                        onClick = { favorites.removeAll { it.first == pageNumber } }
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "Poista suosikeista",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // takaisin
        Button(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}
