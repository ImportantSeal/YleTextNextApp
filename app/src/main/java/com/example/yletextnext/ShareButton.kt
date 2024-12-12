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
    val context = LocalContext.current

    IconButton(
        onClick = {
            val shareText = buildShareText(pageNumber, pageTitle, teletextData)
            Log.d("ShareButton", "Share Text:\n$shareText")

            //jakamisintentin luominen ja valintaikkunan avaus sovelluksille
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this Teletext page!")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share page via"))
        }
    ) {
        Icon(Icons.Default.Share, contentDescription = "Jaa sivu")
    }
}


fun buildShareText(
    pageNumber: Int,
    pageTitle: String,
    teletextData: List<Map<String, Any>>
): String {
    val sb = StringBuilder()
    sb.append("📺 YleTextNext\n\n")
    sb.append("📄 Sivu: $pageNumber\n\n")

    // käydään läpi jokainen tekstirivi ja lisää se jakoviestiin
    teletextData.forEach { line ->
        val text = line["Text"] as? String ?: ""
        sb.append("$text\n")
    }

    sb.append("\n🌐 Katso lisää: https://yle.fi/aihe/tekstitv?P=$pageNumber")
    return sb.toString()
}
