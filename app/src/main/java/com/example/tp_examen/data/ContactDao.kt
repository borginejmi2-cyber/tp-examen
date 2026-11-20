package com.example.tp_examen.data
import android.content.ContentValues
import android.database.Cursor
import android.content.Context

class ContactDao(context: Context) {

    private val helper = ContactDbHelper.getInstance(context)


    fun insert(contact: Contact): Long {
        val db = helper.writableDatabase
        return db.insert(ContactContract.TABLE, null, toValues(contact))

    }


    fun update(contact: Contact): Int {
        val db = helper.writableDatabase
        return db.update(
            ContactContract.TABLE,
            toValues(contact),
            "${ContactContract.COL_ID} = ?",
            arrayOf(contact.id.toString())
        )

    }


    fun delete(id: Long): Int {
        val db = helper.writableDatabase
        return db.delete(
            ContactContract.TABLE,
            "${ContactContract.COL_ID} = ?",
            arrayOf(id.toString())
        )

    }


    fun getAll(): List<Contact> {
        val db = helper.readableDatabase
        val list = mutableListOf<Contact>()
        db.query(
            ContactContract.TABLE,
            null, null, null, null, null,
            ORDER_BY
        ).use { cursor ->
            while (cursor.moveToNext()) list.add(fromCursor(cursor))
        }
        return list
    }

    // ---------- READ (un seul) ----------
    fun getById(id: Long): Contact? {
        val db = helper.readableDatabase
        db.query(
            ContactContract.TABLE,
            null,
            "${ContactContract.COL_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        ).use { cursor ->
            return if (cursor.moveToFirst()) fromCursor(cursor) else null
        }
    }


    fun search(texte: String): List<Contact> {
        val db = helper.readableDatabase
        val list = mutableListOf<Contact>()
        val motif = "%$texte%"
        val selection = "${ContactContract.COL_NOM} LIKE ? OR " +
                "${ContactContract.COL_PRENOM} LIKE ? OR " +
                "${ContactContract.COL_TELEPHONE} LIKE ?"
        db.query(
            ContactContract.TABLE,
            null,
            selection,
            arrayOf(motif, motif, motif),
            null, null,
            ORDER_BY
        ).use { cursor ->
            while (cursor.moveToNext()) list.add(fromCursor(cursor))
        }
        return list
    }

    // ---------- Conversions ----------
    private fun toValues(c: Contact) = ContentValues().apply {
        put(ContactContract.COL_NOM, c.nom)
        put(ContactContract.COL_PRENOM, c.prenom)
        put(ContactContract.COL_TELEPHONE, c.telephone)
        put(ContactContract.COL_EMAIL, c.email)
        put(ContactContract.COL_PHOTO_URI, c.photoUri)
        put(ContactContract.COL_LATITUDE, c.latitude)
        put(ContactContract.COL_LONGITUDE, c.longitude)
    }

    private fun fromCursor(cursor: Cursor): Contact {
        val iLat = cursor.getColumnIndexOrThrow(ContactContract.COL_LATITUDE)
        val iLng = cursor.getColumnIndexOrThrow(ContactContract.COL_LONGITUDE)
        val iEmail = cursor.getColumnIndexOrThrow(ContactContract.COL_EMAIL)
        val iPhoto = cursor.getColumnIndexOrThrow(ContactContract.COL_PHOTO_URI)
        return Contact(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactContract.COL_ID)),
            nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COL_NOM)),
            prenom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COL_PRENOM)),
            telephone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COL_TELEPHONE)),
            email = if (cursor.isNull(iEmail)) null else cursor.getString(iEmail),
            photoUri = if (cursor.isNull(iPhoto)) null else cursor.getString(iPhoto),
            latitude = if (cursor.isNull(iLat)) null else cursor.getDouble(iLat),
            longitude = if (cursor.isNull(iLng)) null else cursor.getDouble(iLng)
        )
    }

    companion object {
        private const val ORDER_BY =
            "${ContactContract.COL_NOM} COLLATE NOCASE ASC, " +
                    "${ContactContract.COL_PRENOM} COLLATE NOCASE ASC"
    }
}