package com.example.yletextnext

// teletext-tiedot
data class TeletextResponse(
    val teletext: Teletext? = null
)

// sisältää sivutiedon
data class Teletext(
    val page: TeletextPage? = null
)

// sisältää alisivut
data class TeletextPage(
    val subpage: List<TeletextSubpage> = emptyList() // lista alisivuista, oletuksena tyhjä
)

// sisältää sivun sisällön.
data class TeletextSubpage(
    val content: List<TeletextContent> = emptyList() // sivun sisällöstä
)

//kuvaa yksittäistä sisältöelementtiä
data class TeletextContent(
    val type: String? = null,
    val line: List<TeletextLine> = emptyList() // lista riveistä
)

// kuvaa yksittäistä tekstiriviä
data class TeletextLine(
    val Text: String? = null,  // Rivin teksti
    val link: String? = null   // Linkki rivissä
)
