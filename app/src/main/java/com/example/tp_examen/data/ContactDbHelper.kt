package com.example.tp_examen.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE ${ContactContract.TABLE} (
                ${ContactContract.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ContactContract.COL_NOM} TEXT NOT NULL,
                ${ContactContract.COL_PRENOM} TEXT NOT NULL,
                ${ContactContract.COL_TELEPHONE} TEXT NOT NULL,
                ${ContactContract.COL_EMAIL} TEXT,
                ${ContactContract.COL_PHOTO_URI} TEXT,
                ${ContactContract.COL_LATITUDE} REAL,
                ${ContactContract.COL_LONGITUDE} REAL
            )
        """.trimIndent()
        db.execSQL(createTable)
        insertDemo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Pour l'instant : on repart de zéro (acceptable en développement).
        // Pour le bonus (migration), on remplacera ça par un ALTER TABLE.
        db.execSQL("DROP TABLE IF EXISTS ${ContactContract.TABLE}")
        onCreate(db)
    }

    private fun insertContact(
        db: SQLiteDatabase,
        nom: String,
        prenom: String,
        telephone: String,
        email: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        val values = ContentValues().apply {
            put(ContactContract.COL_NOM, nom)
            put(ContactContract.COL_PRENOM, prenom)
            put(ContactContract.COL_TELEPHONE, telephone)
            put(ContactContract.COL_EMAIL, email)
            put(ContactContract.COL_LATITUDE, latitude)
            put(ContactContract.COL_LONGITUDE, longitude)
        }
        db.insert(ContactContract.TABLE, null, values)
    }

    private fun insertDemo(db: SQLiteDatabase) {
        insertContact(db, "Ben Ali", "Ahmed", "+216 20 111 222", "ahmed@mail.com", 33.8076, 10.8451)
        insertContact(db, "Trabelsi", "Sara", "22 333 444", "sara@mail.com", 34.7406, 10.7603)
        insertContact(db, "O'Neil", "Sean", "55 666 777", null, 36.8065, 10.1815)
        insertContact(db, "Gharbi", "Mouna", "98 123 456", "mouna@mail.com", null, null)   // sans GPS
        insertContact(db, "L'Ariana", "Yassine", "50 987 654", null, null, null)           // sans GPS
    }

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {
            return instance ?: synchronized(this) {
                instance ?: ContactDbHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}