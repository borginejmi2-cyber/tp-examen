package com.example.tp_examen.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object PhotoUtils {

    private const val MAX_SIDE = 800

    fun nouveauFichier(context: Context): File {
        val dossier = File(context.filesDir, "photos").also { it.mkdirs() }
        return File(dossier, "contact_${System.currentTimeMillis()}.jpg")
    }

    /**
     * Lit l'image source, la réduit (max ~800 px) et l'enregistre dans dest (stockage interne).
     * À appeler hors du thread principal.
     */
    fun importer(context: Context, source: Uri, dest: File): Boolean {
        return try {
            val resolver = context.contentResolver

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }

            var sample = 1
            while (bounds.outWidth / sample > MAX_SIDE * 2 || bounds.outHeight / sample > MAX_SIDE * 2) {
                sample *= 2
            }

            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = resolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return false

            FileOutputStream(dest).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}