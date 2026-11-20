package com.example.tp_examen.util

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

class PreferencesManager(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    /** Taille du texte en sp (14, 16 ou 20). */
    fun taillePoliceSp(): Float =
        prefs.getString(KEY_TAILLE, "16")?.toFloatOrNull() ?: 16f

    /** Couleur du nom et du prénom, convertie en Int. */
    fun couleurTexte(): Int =
        try {
            Color.parseColor(prefs.getString(KEY_COULEUR, "#000000"))
        } catch (e: IllegalArgumentException) {
            Color.BLACK
        }

    fun appelDepuisListe(): Boolean = prefs.getBoolean(KEY_APPEL, false)

    companion object {
        const val KEY_TAILLE = "taille_police"
        const val KEY_COULEUR = "couleur_preferee"
        const val KEY_APPEL = "appel_depuis_liste"
    }
}