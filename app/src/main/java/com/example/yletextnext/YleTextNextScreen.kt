package com.example.yletextnext

import android.content.Context
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager

// pääkomponentti, joka hallitsee koko näkymää
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YleTextNextScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    // navigaatiokontrolleri Composenavigaatiolle

    val navController = rememberNavController()
    val context = LocalContext.current
    val favoritesManager = remember { FavoritesManager(context) }
    val favorites = favoritesManager.favorites
    val apiService = remember { YleApiService.create() }

    // funktio sivun sisällön hakemiseen API:sta
    val getPageContent: suspend (Int) -> List<Map<String, Any>> = { pageNumber ->
        try {
            // haetaan teletext sivu API:sta
            val response = apiService.getTeletextPage(
                pageNumber,
                BuildConfig.YLE_API_ID,
                BuildConfig.YLE_API_KEY
            )
            //  API vastauksen muunto "käyttökelpoiseen" muotoon
            response.teletext?.page?.subpage?.flatMap { subpage ->
                subpage.content.filter { it.type == "text" }.flatMap { item ->
                    item.line.map { line ->
                        mapOf(
                            "Text" to (line.Text ?: ""),
                            "link" to (line.link ?: "")
                        )
                    }
                }
            } ?: emptyList()
        } catch (e: Exception) {
            //kirjataan virhe ja palautetaan tyhjä lista
            Log.e("YleTextNextScreen", "Error fetching page: ${e.message}")
            emptyList()
        }
    }

    NavHost(navController, startDestination = "home?page=101") {
        composable("home?page={page}") { backStackEntry ->
            val page = backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: 101

            // Kotinäkymä

            HomeScreen(
                navController = navController,
                isDarkMode = isDarkMode,
                favorites = favorites,
                initialPageNumber = page,
                onAddFavorite = { pageNumber, pageTitle ->
                    favoritesManager.addFavorite(pageNumber, pageTitle)
                },
                onRemoveFavorite = { pageNumber ->
                    favoritesManager.removeFavorite(pageNumber)
                },
                getPageContent = getPageContent
            )
        }

        composable("favorites") {
            FavoritesScreen(
                favorites = favorites,
                navController = navController,
                onBack = { navController.popBackStack() },
                getPageContent = getPageContent
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange,
                onLanguageChange = onLanguageChange
            )
        }
    }
}

// Kotinäkymän pääkomponentti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isDarkMode: Boolean,
    favorites: SnapshotStateList<Pair<Int, String>>,
    initialPageNumber: Int,
    onAddFavorite: (Int, String) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    getPageContent: suspend (Int) -> List<Map<String, Any>>
) {
    // coroutine scopet, konteksti ja fokuksen hallinta
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // tilanhallintamuuttujat
    var pageNumber by remember { mutableStateOf(initialPageNumber) }
    var inputPageNumber by remember { mutableStateOf("") }
    var teletextData by remember { mutableStateOf<List<Map<String, Any>>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastDragAmount by remember { mutableStateOf(0f) }

    // sivuvalikko tila
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isFavorite = favorites.any { it.first == pageNumber }

    // modaalinen sivuvalikko
    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                onNavigateToPage = { selectedPage ->
                    pageNumber = selectedPage
                },
                scope = coroutineScope,
                drawerState = drawerState
            )
        },
        drawerState = drawerState
    ) {
        // päänäkymän rakenne
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("YleTextNext") },
                    // poistetaan navigationIcon kokonaan tai laitetaan sinne jokin muu ikonipainike esim, valikko
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // sydänikoni
                        IconButton(onClick = {
                            if (isFavorite) {
                                // poistaa suosikeista kutsumalla callbackia
                                onRemoveFavorite(pageNumber)
                            } else {
                                // lisää suosikkeihin kutsumalla callbackia
                                onAddFavorite(pageNumber, "Sivu $pageNumber")
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isFavorite) "Poista suosikeista" else "Lisää suosikkeihin"
                            )
                        }

                        ShareButton(
                            pageNumber = pageNumber,
                            pageTitle = "Sivu $pageNumber", //otsikko
                            teletextData = teletextData ?: emptyList() // sivun sisältö tai tyhjä lista
                        )

                        // asetukset painike navigointiin
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Asetukset")
                        }
                    }
                )
            },
            // alapalkki navigaatiolle ja sivujen selailulle
            bottomBar = {
                BottomAppBar {
                    // edellinen sivu(näytetään vain jos ollaan yli sivun 100)
                    if (pageNumber > 100) {
                        Button(onClick = { pageNumber-- }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {// edelliseen sivuun siirtyminen
                            Text("← ${pageNumber - 1}")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // kotisivu-ikoni ja teksti
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pageNumber = 101 }, // 101 koska se on parempi aloitussivu kun 100
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Kotisivu")
                        Text("Kotisivu", fontSize = 10.sp)
                    }

                    // suosikit-ikoni ja teksti
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("favorites") },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "Suosikit")
                        Text("Suosikit", fontSize = 10.sp)
                    }
                    // seuraava sivu
                    Button(onClick = { pageNumber++ }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp) ) {
                        Text("${pageNumber + 1} →")// näytetään seuraavan sivun numero
                    }
                }
            },
            // pääsisältö
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        // poista fokus koskettamalla, että ei jää häiritsemään taustalle
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            focusManager.clearFocus()
                        }
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        // swipe sivujen vaihtamiseen, koska navigointi sormilla on parempi kuin napeipeilla liikkeessä
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                // swipen lopetuksen käsittely
                                onDragEnd = {
                                    if (lastDragAmount > 0) {
                                        // pyyhkäsy oikeelle: siirry edelliseen sivuun
                                        pageNumber = (pageNumber - 1).coerceAtLeast(100)
                                    } else if (lastDragAmount < 0) {
                                        // pyyhkäsy vasemmalle: siirry seuraavaa sivuun

                                        pageNumber++
                                    }
                                    lastDragAmount = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    lastDragAmount = dragAmount
                                }
                            )
                        }
                ) {
                    // hakukentt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // tekstikenttä
                        OutlinedTextField(
                            value = inputPageNumber,
                            onValueChange = { inputPageNumber = it },
                            label = {
                                Text(
                                    "Sivu: $pageNumber",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp,))
                        // Hae button
                        Button(
                            onClick = {
                                // muunnetaan syötetty sivunumero ja vaihdetaan sivua
                                val newPageNumber = inputPageNumber.toIntOrNull()
                                if (newPageNumber != null) {
                                    pageNumber = newPageNumber
                                }
                                inputPageNumber = "" //tyhjennetään kenttä
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(40.dp) // Napin korkeus
                        ) {
                            Text("Hae", fontSize = 14.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when {
                            isLoading -> Text("Ladataan...", fontSize = 18.sp)
                            errorMessage != null -> Text(errorMessage ?: "", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                            else -> {
                                teletextData?.let { data ->
                                    TeletextContent(
                                        teletextPage = data,
                                        onNavigateToPage = { selectedPage -> pageNumber = selectedPage }
                                    )
                                } ?: Text("Ei sisältöä saatavilla.", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        )
    }


// launchedEffect lataa sivun sisällön aina sivunumeron muuttuessa
    LaunchedEffect(pageNumber) {
        isLoading = true
        errorMessage = null // nollaa mahdolliset aiemmat virheviestit
        try {
            // haetaa sivun sisältö
            teletextData = getPageContent(pageNumber)
            // tarkistetaan onko sisältöä
            if (teletextData.isNullOrEmpty()) {
                errorMessage = "Sivua $pageNumber ei löytynyt."
            }
        } catch (e: Exception) {
            errorMessage = "Virhe: ${e.message}"
        } finally {
            //lopetetaan lataus
            isLoading = false
        }
    }
}

