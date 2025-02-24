package com.example.yletextnext

import android.content.Intent
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShareButton(
    pageNumber: Int,
    pageTitle: String,
    teletextData: List<Map<String, Any>>
) {
    // haetaan konteksti composen avulla
    val context = LocalContext.current

    IconButton(
        onClick = {
            // luodaan ja tulostetaan jaettava teksti
            val shareText = buildShareText(pageNumber, pageTitle, teletextData)
            Log.d("ShareButton", "share text:\n$shareText")

            // luodaan intent, jolla avataan sovellusten valintalista
            // käyttäjä voi valita haluamansa sovelluksen, johon tieto jaetaan
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "yletextnext!")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            // käynnistetään intentin valintadialogi
            context.startActivity(Intent.createChooser(shareIntent, "share page via"))
        }
    ) {
        // share-kuvake, joka käynnistää yllä määritetyn toiminnon
        Icon(Icons.Default.Share, contentDescription = "jaa sivu")
    }
}

// funktio rakentaa jaettavan tekstin tekstitv-sivusta
fun buildShareText(
    pageNumber: Int,
    pageTitle: String,
    teletextData: List<Map<String, Any>>
): String {
    val sb = StringBuilder()

    // lisätään otsikkotiedot ja sivunumero
    sb.append("📺 yletextnext\n\n")
    sb.append("📄 sivu: $pageNumber\n\n")

    // käydään läpi jokainen rivitieto ja lisätään se tekstiin
    teletextData.forEach { line ->
        val text = line["Text"] as? String ?: ""
        sb.append("$text\n")
    }

    // lisätään lopuksi linkki sivuun
    sb.append("\n🌐 katso lisää: https://yle.fi/aihe/tekstitv?P=$pageNumber")
    return sb.toString()
}