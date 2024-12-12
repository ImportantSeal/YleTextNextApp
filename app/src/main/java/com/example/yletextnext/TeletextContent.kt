package com.example.yletextnext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// sealed class, joka määrittelee kaksi eri tyyppiä tekstin osille: tavallinen teksti ja linkki
sealed class TextPart {
    data class Text(val content: String) : TextPart() // tavallinen tekstiosa
    data class Link(val content: String, val pageNumber: Int) : TextPart() // Linkkiosa jossa on sivunumero
}

// määritellään monospace-fontti teletextin ulkoasuun sopivaksi
private val TeletextFont = FontFamily.Monospace

// vakiot fontin koolle
private val FontSize = 16.sp


@Composable
fun TeletextContent(
    teletextPage: List<Map<String, Any>>,
    onNavigateToPage: (Int) -> Unit
) {
    // näytetään kaikki rivit
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // läy läpi jokainen rivi teletext-sivulta
        teletextPage.forEach { line ->
            // haetaan tekstisisältö rivistä, jos se on null käytetään tyhjää merkkijonoa
            val text = line["Text"] as? String ?: ""

            // jaetaan rivi tekstin osiin (tekstit ja linkit)
            val textParts = splitTextWithLinks(text)

            // Näytetään jaettu rivi TeletextLine komponentilla
            TeletextLine(
                textParts = textParts,
                onNavigateToPage = onNavigateToPage,
                modifier = Modifier.padding(vertical = 2.dp) //lisätään pystysuuntainen väli rivien väliin
            )
        }
    }
}


@Composable
fun TeletextLine(
    textParts: List<TextPart>,
    onNavigateToPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // rivi jossa tekstin osat näytetään vaakasuunnassa
    Row(
        modifier = modifier
            .fillMaxWidth()            // täyttää koko leveyden
            .wrapContentHeight(),      // korkeus mukautuu sisällön mukaan
        horizontalArrangement = Arrangement.Start // tekstin osat sijoitetaan vasemmalle
    ) {
        // käydään läpi kaikki tekstin osat
        textParts.forEach { part ->
            when (part) {
                // Jos osa on tavallista tekstiä, näytä se TeletextText-komponentilla
                is TextPart.Text -> TeletextText(part.content)

                // jos osa on linkki näytä se TeletextLink-komponentilla
                is TextPart.Link -> TeletextLink(part.content, part.pageNumber, onNavigateToPage)
            }
            //  tilaa osien väliin
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}


@Composable
fun TeletextText(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 14.sp,                       //fontin koko
            lineHeight = 18.sp,                     // rivin korkeus
            color = MaterialTheme.colorScheme.onBackground, // käytetään teeman mukaista taustaväriä
            fontFamily = FontFamily.Monospace       // monospace fontti
        )
    )
}


@Composable
fun TeletextLink(content: String, pageNumber: Int, onNavigateToPage: (Int) -> Unit) {
    // Määritellään linkin tyyli
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.onBackground, // linkin väri
        fontWeight = FontWeight.Bold,                  // lihavoitu fontti
        textDecoration = TextDecoration.Underline,     // Aaleviivaus linkille
        fontFamily = FontFamily.Monospace              // monospace-fontti
    )

    // näytetään klikattava teksti linkin tyylillä
    ClickableText(
        text = AnnotatedString(content, spanStyle = linkStyle),
        onClick = { onNavigateToPage(pageNumber) },    //kutsutaan funktiota, kun linkkiä klikataan
        modifier = Modifier.padding(horizontal = 2.dp) // hieman tilaa linkin ympärille
    )
}


fun splitTextWithLinks(text: String): List<TextPart> {
    val regex = Regex("""\b\d{3}\b""") //tunnistaa kolmen numeron sarjat linkkejä varten
    val parts = mutableListOf<TextPart>()
    var lastIndex = 0

    // regex-osumien läpi käynti
    regex.findAll(text).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1

        // tavallisen tekstin lisäys ennen linkkiä
        if (lastIndex < start) {
            val substring = text.substring(lastIndex, start)
            parts.add(TextPart.Text(substring.trim()))
        }

        //lisätään tunnistettu linkki
        parts.add(TextPart.Link(match.value, match.value.toInt()))
        lastIndex = end
    }

    // lisätääb loput tekstistä jos sitä on jäljellä
    if (lastIndex < text.length) {
        parts.add(TextPart.Text(text.substring(lastIndex).trim()))
    }

    return parts
}
