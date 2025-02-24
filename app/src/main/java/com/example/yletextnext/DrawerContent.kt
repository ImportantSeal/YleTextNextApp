package com.example.yletextnext
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun DrawerContent(
    onNavigateToPage: (Int) -> Unit,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    ModalDrawerSheet {
        Text(
            stringResource(R.string.menu),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Divider()

        // valikon sivut
        val pages = listOf(
            stringResource(R.string.home_page) to 100,
            stringResource(R.string.domestic_news) to 102,
            stringResource(R.string.foreign_news) to 130,
            stringResource(R.string.economy) to 160,
            stringResource(R.string.stock_exchange) to 175,
            stringResource(R.string.news_in_english) to 190,
            stringResource(R.string.emergency_warnings) to 112,
            stringResource(R.string.regional_news) to 500,
            stringResource(R.string.emergency_info) to 866,
            stringResource(R.string.radiation_safety) to 867
        )

        // luo valikon kohdat sivuille
        pages.forEach { (title, page) ->
            NavigationDrawerItem(
                label = { Text(title) },
                selected = false,
                onClick = {
                    onNavigateToPage(page) // navigoi valittuun sivuun
                    scope.launch { drawerState.close() } // suljetaan drawer valinnan jälkeen
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}