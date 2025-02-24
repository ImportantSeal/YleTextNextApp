package com.example.yletextnext

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // lista suosikeista, jota compose voi havainnoida
    val favorites: SnapshotStateList<Pair<Int, String>> = mutableStateListOf()

    init {
        loadFavorites() //ladataan suosikit käynnistyksen yhteydessä
    }


  //Lataa suosikit SharedPreferencesista

    private fun loadFavorites() {
        val json = sharedPreferences.getString("favorites", null)
        val type = object : TypeToken<MutableList<Pair<Int, String>>>() {}.type
        val loadedFavorites: MutableList<Pair<Int, String>>? = gson.fromJson(json, type)
        if (loadedFavorites != null) {
            favorites.addAll(loadedFavorites)
            Log.d("FavoritesManager", "Loaded favorites: $favorites")
        } else {
            Log.d("FavoritesManager", "No favorites found, starting with an empty list.")
        }
    }

//tallennetaan suosikit SharedPreferencesiin
    fun saveFavorites() {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(favorites)
        editor.putString("favorites", json)
        val success = editor.commit()
        Log.d("FavoritesManager", "Saved favorites successfully: $success, Data: $json")
    }
// sivu lisätään suosikkeihin, jos sitä ei ole jo lisätty
    fun addFavorite(pageNumber: Int, pageTitle: String) {
        if (!favorites.any { it.first == pageNumber }) {
            favorites.add(Pair(pageNumber, pageTitle))
            saveFavorites()
            Log.d("FavoritesManager", "Added favorite: $pageNumber - $pageTitle")
        } else {
            Log.d("FavoritesManager", "Favorite already exists: $pageNumber - $pageTitle")
        }
    }

// Poistaa sivun suosikeista
    fun removeFavorite(pageNumber: Int) {
        val removed = favorites.removeAll { it.first == pageNumber }
        if (removed) {
            saveFavorites()
            Log.d("FavoritesManager", "Removed favorite: $pageNumber")
        }
    }
}
